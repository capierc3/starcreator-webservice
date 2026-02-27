package com.brickroad.starcreator_webservice.utils.planets;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.model.climate.*;
import com.brickroad.starcreator_webservice.model.habitability.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ClimateNarrativeGenerator {

    // ================================================================
    // MAIN ENTRY POINT
    // ================================================================

    public void generate(PlanetaryClimate weather, Planet planet, Star parentStar) {
        boolean isMoon = weather.isMoonClimate();

        // Generate weather hazards — moon vs planet
        List<ClimateHazard> hazards = isMoon
                ? generateMoonHazards(weather, planet)
                : generatePlanetHazards(weather, planet);
        weather.setClimateHazards(hazards);

        // Outdoor exposure rating — moon vs planet
        if (isMoon) {
            calculateMoonOutdoorExposure(weather, planet, parentStar);
        } else {
            calculatePlanetOutdoorExposure(weather, planet, parentStar);
        }

        // Weather severity — uses moon-aware thresholds
        calculateWeatherSeverity(weather, hazards);

        // Weather summary — moon vs planet
        if (isMoon) {
            generateMoonWeatherSummary(weather, planet, parentStar);
        } else {
            generatePlanetWeatherSummary(weather, planet, parentStar);
        }
    }

    // ================================================================
    // PLANET WEATHER HAZARDS
    // ================================================================

    private List<ClimateHazard> generatePlanetHazards(PlanetaryClimate weather, Planet planet) {
        List<ClimateHazard> hazards = new ArrayList<>();
        String atmClass = planet.getAtmosphereClassification();
        double surfaceTemp = planet.getSurfaceTemp() != null ? planet.getSurfaceTemp() : 250.0;
        double pressureAtm = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 1.0;
        double meanWindMs = weather.getMeanSurfaceWindSpeedMs() != null ? weather.getMeanSurfaceWindSpeedMs() : 7.0;
        double maxGustMs = weather.getMaxGustSpeedMs() != null ? weather.getMaxGustSpeedMs() : 20.0;
        PlanetaryHabitability hab = planet.getHabitability();

        // Thermal hazards
        if (surfaceTemp > 350) {
            addHazard(hazards, "Extreme Heat", "THERMAL",
                    surfaceTemp > 500 ? "LETHAL" : "EXTREME", "CONTINUOUS",
                    String.format("Surface temperature of %.0fK is lethal to unprotected humans", surfaceTemp));
        } else if (surfaceTemp > 320) {
            addHazard(hazards, "Dangerous Heat", "THERMAL", "HIGH", "CONTINUOUS",
                    "Sustained high temperatures risk heat stroke without cooling equipment");
        } else if (surfaceTemp < 200) {
            addHazard(hazards, "Extreme Cold", "THERMAL",
                    surfaceTemp < 150 ? "LETHAL" : "EXTREME", "CONTINUOUS",
                    String.format("Surface temperature of %.0fK requires full thermal protection", surfaceTemp));
        } else if (surfaceTemp < 250) {
            addHazard(hazards, "Severe Cold", "THERMAL", "HIGH", "CONTINUOUS",
                    "Sub-freezing temperatures require insulated gear for extended exposure");
        }

        // Day-night thermal swing
        addDiurnalSwingHazard(hazards, weather);

        // Wind hazards
        addWindHazards(hazards, meanWindMs, maxGustMs);

        // Pressure hazards
        if (pressureAtm > 10) {
            addHazard(hazards, "Crushing Atmospheric Pressure", "PRESSURE",
                    pressureAtm > 50 ? "LETHAL" : "EXTREME", "CONTINUOUS",
                    String.format("Surface pressure of %.1f atm requires pressure-rated habitats", pressureAtm));
        } else if (pressureAtm < 0.01) {
            addHazard(hazards, "Near-Vacuum Atmosphere", "PRESSURE", "LETHAL", "CONTINUOUS",
                    "Atmospheric pressure too low to sustain life — full pressure suit required");
        } else if (pressureAtm < 0.3) {
            addHazard(hazards, "Low Atmospheric Pressure", "PRESSURE", "HIGH", "CONTINUOUS",
                    "Pressure insufficient for unaided breathing — supplemental pressure required");
        }

        // Chemical hazards
        addChemicalHazards(hazards, atmClass);

        // Storm hazards
        if (Boolean.TRUE.equals(weather.getHasDustStorms()) && Boolean.TRUE.equals(weather.getDustStormsCanBeGlobal())) {
            addHazard(hazards, "Global Dust Storms", "STORM", "HIGH", "OCCASIONAL",
                    "Planet-encircling dust storms block solar power and reduce visibility for weeks");
        }

        // Lightning hazard
        addLightningHazards(hazards, weather);

        // Radiation hazards
        PlanetaryMagneticField mag = planet.getMagneticField();
        boolean hasOzone = hab != null && Boolean.TRUE.equals(hab.getHasOzoneLayer());
        PlanetaryMagneticField.ProtectionLevel protection = mag != null ? mag.getProtectionLevel() : null;

        if (protection == null || protection == PlanetaryMagneticField.ProtectionLevel.NONE) {
            if (!hasOzone) {
                addHazard(hazards, "Unshielded Stellar Radiation", "RADIATION",
                        "EXTREME", "CONTINUOUS",
                        "No magnetosphere or ozone layer — surface exposed to full stellar UV and particle radiation");
            } else {
                addHazard(hazards, "Unshielded Charged Particle Radiation", "RADIATION",
                        "HIGH", "CONTINUOUS",
                        "No magnetosphere — ozone blocks UV but charged particles reach the surface");
            }
        } else if (protection == PlanetaryMagneticField.ProtectionLevel.MINIMAL && !hasOzone) {
            addHazard(hazards, "Elevated Stellar Radiation", "RADIATION",
                    "HIGH", "CONTINUOUS",
                    "Weak magnetic shielding and no ozone — surface radiation levels exceed safe exposure limits");
        } else if (protection == PlanetaryMagneticField.ProtectionLevel.MINIMAL) {
            addHazard(hazards, "Elevated UV Radiation", "RADIATION",
                    "MODERATE", "CONTINUOUS",
                    "Limited magnetic shielding allows elevated UV and charged particle flux at surface");
        }

        return hazards;
    }

    // ================================================================
    // MOON WEATHER HAZARDS
    // ================================================================

    /**
     * Moon-calibrated hazards. Baseline moon conditions (cold, vacuum, no magnetic field)
     * are expected and get downgraded severity. Only conditions exceptional FOR A MOON
     * receive high ratings. This prevents the hazard score from being inflated by
     * properties that are simply normal for any moon with an atmosphere.
     */
    private List<ClimateHazard> generateMoonHazards(PlanetaryClimate weather, Planet planet) {
        List<ClimateHazard> hazards = new ArrayList<>();
        String atmClass = planet.getAtmosphereClassification();
        double surfaceTemp = planet.getSurfaceTemp() != null ? planet.getSurfaceTemp() : 250.0;
        double pressureAtm = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 1.0;
        double meanWindMs = weather.getMeanSurfaceWindSpeedMs() != null ? weather.getMeanSurfaceWindSpeedMs() : 7.0;
        double maxGustMs = weather.getMaxGustSpeedMs() != null ? weather.getMaxGustSpeedMs() : 20.0;
        PlanetaryHabitability hab = planet.getHabitability();

        // === THERMAL HAZARDS ===
        // Extreme cold is baseline for moons — only flag truly extreme cases or unusual heat
        if (surfaceTemp > 350) {
            addHazard(hazards, "Extreme Heat", "THERMAL",
                    surfaceTemp > 500 ? "LETHAL" : "EXTREME", "CONTINUOUS",
                    String.format("Surface temperature of %.0fK — unusually hot for a moon", surfaceTemp));
        } else if (surfaceTemp > 320) {
            addHazard(hazards, "Dangerous Heat", "THERMAL", "HIGH", "CONTINUOUS",
                    "Elevated temperatures require active cooling for surface operations");
        } else if (surfaceTemp < 80) {
            // Ultra-cold even by moon standards (colder than Pluto)
            addHazard(hazards, "Ultra-Cryogenic Conditions", "THERMAL", "EXTREME", "CONTINUOUS",
                    String.format("Surface temperature of %.0fK — extreme even by outer system standards", surfaceTemp));
        } else if (surfaceTemp < 150) {
            // Cold but typical for moons — downgrade from LETHAL to MODERATE
            addHazard(hazards, "Cryogenic Conditions", "THERMAL", "MODERATE", "CONTINUOUS",
                    String.format("Surface temperature of %.0fK requires standard thermal protection", surfaceTemp));
        } else if (surfaceTemp < 250) {
            addHazard(hazards, "Sub-Freezing Conditions", "THERMAL", "LOW", "CONTINUOUS",
                    "Below freezing — thermal regulation required for EVA");
        }

        // Day-night thermal swing — same thresholds, relevant for moons too
        addDiurnalSwingHazard(hazards, weather);

        // === WIND HAZARDS === (same as planets — wind is wind)
        addWindHazards(hazards, meanWindMs, maxGustMs);

        // === PRESSURE HAZARDS ===
        // Near-vacuum is baseline for moons — downgrade significantly
        if (pressureAtm > 10) {
            // Dense atmosphere on a moon is actually unusual and noteworthy
            addHazard(hazards, "Unusually Dense Atmosphere", "PRESSURE",
                    pressureAtm > 50 ? "LETHAL" : "EXTREME", "CONTINUOUS",
                    String.format("Surface pressure of %.1f atm — remarkably dense for a moon", pressureAtm));
        } else if (pressureAtm < 0.001) {
            // Near-total vacuum — still needs noting but not LETHAL
            addHazard(hazards, "Near-Vacuum", "PRESSURE", "MODERATE", "CONTINUOUS",
                    "Negligible atmospheric pressure — full pressure suit mandatory");
        } else if (pressureAtm < 0.01) {
            addHazard(hazards, "Trace Atmosphere", "PRESSURE", "LOW", "CONTINUOUS",
                    "Atmospheric pressure far too low for unaided survival — pressure suit required");
        } else if (pressureAtm < 0.3) {
            addHazard(hazards, "Thin Atmosphere", "PRESSURE", "LOW", "CONTINUOUS",
                    "Low pressure requires supplemental breathing apparatus");
        }

        // === CHEMICAL HAZARDS === (same as planets — toxic is toxic)
        addChemicalHazards(hazards, atmClass);

        // === STORM HAZARDS ===
        if (Boolean.TRUE.equals(weather.getHasDustStorms()) && Boolean.TRUE.equals(weather.getDustStormsCanBeGlobal())) {
            addHazard(hazards, "Global Dust Storms", "STORM", "HIGH", "OCCASIONAL",
                    "Moon-encircling dust storms reduce visibility and block solar power");
        }

        // Lightning — moons won't have CONVECTIVE_GIANT, but volcanic lightning is relevant
        if (Boolean.TRUE.equals(weather.getHasLightning())) {
            String lightningType = weather.getLightningType();
            if ("VOLCANIC".equals(lightningType)) {
                addHazard(hazards, "Volcanic Lightning", "ELECTRICAL", "HIGH", "FREQUENT",
                        "Electrostatic discharges in volcanic plumes");
            }
        }

        // === RADIATION HAZARDS ===
        // No magnetic field is baseline for ~98% of moons — downgrade from EXTREME to MODERATE
        PlanetaryMagneticField mag = planet.getMagneticField();
        boolean hasOzone = hab != null && Boolean.TRUE.equals(hab.getHasOzoneLayer());
        PlanetaryMagneticField.ProtectionLevel protection = mag != null ? mag.getProtectionLevel() : null;

        if (protection == null || protection == PlanetaryMagneticField.ProtectionLevel.NONE) {
            if (!hasOzone) {
                // No mag + no ozone is baseline for nearly all moons
                addHazard(hazards, "Unshielded Radiation Environment", "RADIATION",
                        "MODERATE", "CONTINUOUS",
                        "No magnetosphere or ozone layer — standard radiation shielding required for habitats");
            } else {
                // Has ozone but no mag — unusual for a moon, relatively mild
                addHazard(hazards, "Partially Shielded Radiation", "RADIATION",
                        "LOW", "CONTINUOUS",
                        "Ozone layer provides UV protection but no charged particle shielding");
            }
        } else if (protection == PlanetaryMagneticField.ProtectionLevel.MINIMAL && !hasOzone) {
            // Has some magnetic field — notable for a moon (Ganymede-like)
            addHazard(hazards, "Lightly Shielded Radiation", "RADIATION",
                    "LOW", "CONTINUOUS",
                    "Weak magnetic field provides partial charged particle deflection");
        }
        // MODERATE+ protection on a moon = no radiation hazard worth noting

        return hazards;
    }

    // ================================================================
    // SHARED HAZARD HELPERS
    // ================================================================

    private void addDiurnalSwingHazard(List<ClimateHazard> hazards, PlanetaryClimate weather) {
        Double dayNightRange = weather.getDayNightTempRangeK();
        if (dayNightRange != null && dayNightRange > 80) {
            addHazard(hazards, "Extreme Diurnal Temperature Swing", "THERMAL",
                    dayNightRange > 150 ? "EXTREME" : "HIGH", "CONTINUOUS",
                    String.format("%.0fK temperature swing between day and night stresses equipment and personnel",
                            dayNightRange));
        }
    }

    private void addWindHazards(List<ClimateHazard> hazards, double meanWindMs, double maxGustMs) {
        if (meanWindMs > 30) {
            addHazard(hazards, "Extreme Winds", "WIND",
                    meanWindMs > 60 ? "LETHAL" : "EXTREME", "CONTINUOUS",
                    String.format("Sustained winds of %.0f m/s make surface operations dangerous", meanWindMs));
        } else if (maxGustMs > 50) {
            addHazard(hazards, "Dangerous Gusts", "WIND", "HIGH", "FREQUENT",
                    String.format("Wind gusts up to %.0f m/s pose significant risk to surface operations", maxGustMs));
        }
    }

    private void addChemicalHazards(List<ClimateHazard> hazards, String atmClass) {
        switch (atmClass) {
            case "VENUS_LIKE":
                addHazard(hazards, "Corrosive Sulfuric Acid Clouds", "CHEMICAL", "LETHAL", "CONTINUOUS",
                        "Dense sulfuric acid cloud deck corrodes all exposed materials");
                break;
            case "VOLCANIC":
                addHazard(hazards, "Toxic Volcanic Gases", "CHEMICAL", "EXTREME", "CONTINUOUS",
                        "SO2 and other volcanic gases are immediately toxic to unprotected exposure");
                break;
            case "AMMONIA":
                addHazard(hazards, "Toxic Ammonia Atmosphere", "CHEMICAL", "LETHAL", "CONTINUOUS",
                        "Ammonia-rich atmosphere is immediately lethal without sealed life support");
                break;
            case "CORROSIVE":
                addHazard(hazards, "Corrosive Atmosphere", "CHEMICAL", "LETHAL", "CONTINUOUS",
                        "Highly reactive atmosphere attacks materials and biological tissue on contact");
                break;
            case "REDUCING":
                addHazard(hazards, "Reducing Atmosphere", "CHEMICAL", "EXTREME", "CONTINUOUS",
                        "No free oxygen — hydrogen-rich atmosphere incompatible with human respiration");
                break;
            case "EXOTIC":
                addHazard(hazards, "Exotic Atmospheric Chemistry", "CHEMICAL", "LETHAL", "CONTINUOUS",
                        "Extreme temperatures and exotic chemistry make the atmosphere instantly lethal");
                break;
        }
    }

    private void addLightningHazards(List<ClimateHazard> hazards, PlanetaryClimate weather) {
        if (Boolean.TRUE.equals(weather.getHasLightning())) {
            String lightningType = weather.getLightningType();
            if ("CONVECTIVE_GIANT".equals(lightningType)) {
                addHazard(hazards, "Giant-Scale Lightning", "ELECTRICAL", "EXTREME", "FREQUENT",
                        "Lightning discharges far more powerful than terrestrial bolts");
            } else if ("VOLCANIC".equals(lightningType)) {
                addHazard(hazards, "Volcanic Lightning", "ELECTRICAL", "HIGH", "FREQUENT",
                        "Electrostatic discharges in volcanic plumes endanger aircraft and surface operations");
            }
        }
    }

    private void addHazard(List<ClimateHazard> hazards, String name, String type,
                           String severity, String frequency, String description) {
        ClimateHazard hazard = new ClimateHazard();
        hazard.setHazardName(name);
        hazard.setHazardType(type);
        hazard.setSeverity(severity);
        hazard.setFrequency(frequency);
        hazard.setDescription(description);
        hazards.add(hazard);
    }

    // ================================================================
    // PLANET OUTDOOR EXPOSURE RATING
    // ================================================================

    private void calculatePlanetOutdoorExposure(PlanetaryClimate weather, Planet planet, Star parentStar) {
        String atmClass = planet.getAtmosphereClassification();
        double surfaceTemp = planet.getSurfaceTemp() != null ? planet.getSurfaceTemp() : 250.0;
        double pressureAtm = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 1.0;

        PlanetaryHabitability hab = planet.getHabitability();
        boolean breathable = hab != null && Boolean.TRUE.equals(hab.getIsBreathable());

        int radiationScore = calculateRadiationScore(planet, parentStar);

        // --- LETHAL_SECONDS: instant death ---
        if (pressureAtm > 100 || pressureAtm < 0.006 || surfaceTemp > 500 || surfaceTemp < 100) {
            weather.setOutdoorExposureRating("LETHAL_SECONDS");
            weather.setSurvivalTimeDescription("Instantaneous death from " + getLethalCause(surfaceTemp, pressureAtm));
            return;
        }
        if ("VENUS_LIKE".equals(atmClass) || "CORROSIVE".equals(atmClass) || "EXOTIC".equals(atmClass)
                || "AMMONIA".equals(atmClass)) {
            weather.setOutdoorExposureRating("LETHAL_SECONDS");
            weather.setSurvivalTimeDescription("Immediately lethal atmospheric chemistry");
            return;
        }
        if (radiationScore == 5) {
            weather.setOutdoorExposureRating("LETHAL_SECONDS");
            weather.setSurvivalTimeDescription("Lethal radiation exposure — no magnetosphere, extreme stellar activity");
            return;
        }

        // --- LETHAL_MINUTES: death within minutes ---
        if ("VOLCANIC".equals(atmClass) || "REDUCING".equals(atmClass)) {
            weather.setOutdoorExposureRating("LETHAL_MINUTES");
            weather.setSurvivalTimeDescription("Toxic atmosphere causes death within minutes without protection");
            return;
        }
        if (surfaceTemp > 350 || surfaceTemp < 150 || pressureAtm > 50 || pressureAtm < 0.01) {
            weather.setOutdoorExposureRating("LETHAL_MINUTES");
            weather.setSurvivalTimeDescription("Extreme conditions cause death within minutes");
            return;
        }
        if (radiationScore == 4) {
            weather.setOutdoorExposureRating("LETHAL_MINUTES");
            weather.setSurvivalTimeDescription("Severe radiation exposure — minimal shielding from active star");
            return;
        }

        // --- PRESSURE_SUIT: sealed suit required ---
        if (pressureAtm < 0.3 || pressureAtm > 10) {
            String cause;
            if (pressureAtm < 0.1) cause = "near-vacuum conditions";
            else if (pressureAtm < 0.3) cause = "dangerously low atmospheric pressure";
            else cause = "extreme atmospheric pressure";
            weather.setOutdoorExposureRating("PRESSURE_SUIT");
            weather.setSurvivalTimeDescription("Full pressure suit required — " + cause);
            return;
        }
        if (surfaceTemp > 320 || surfaceTemp < 240) {
            weather.setOutdoorExposureRating("PRESSURE_SUIT");
            weather.setSurvivalTimeDescription("Full environmental suit required — " +
                    (surfaceTemp < 200 ? "extreme cold" : "extreme heat"));
            return;
        }
        if (!breathable && (pressureAtm < 0.5 || pressureAtm > 5)) {
            weather.setOutdoorExposureRating("PRESSURE_SUIT");
            weather.setSurvivalTimeDescription("Full pressure suit required — unbreathable atmosphere with marginal pressure");
            return;
        }
        if (radiationScore == 3) {
            weather.setOutdoorExposureRating("PRESSURE_SUIT");
            weather.setSurvivalTimeDescription("Radiation suit required — insufficient magnetic and atmospheric shielding");
            return;
        }

        // --- ASSISTED: survivable with personal equipment ---
        if (!breathable) {
            weather.setOutdoorExposureRating("ASSISTED");
            weather.setSurvivalTimeDescription("Supplemental oxygen required — atmosphere is not breathable");
            return;
        }
        if (surfaceTemp < 260 || surfaceTemp > 310) {
            weather.setOutdoorExposureRating("ASSISTED");
            weather.setSurvivalTimeDescription(surfaceTemp < 260 ?
                    "Thermal protection required — cold conditions" :
                    "Thermal protection required — hot conditions");
            return;
        }
        if (pressureAtm < 0.5 || pressureAtm > 3.0) {
            weather.setOutdoorExposureRating("ASSISTED");
            weather.setSurvivalTimeDescription(pressureAtm < 0.5 ?
                    "Pressure support required — atmosphere too thin for comfort" :
                    "Pressure adaptation required — atmosphere denser than comfortable");
            return;
        }
        if (radiationScore == 2) {
            weather.setOutdoorExposureRating("ASSISTED");
            weather.setSurvivalTimeDescription("Radiation protection required — elevated stellar radiation limits outdoor exposure");
            return;
        }

        // --- SHIRT_SLEEVE ---
        weather.setOutdoorExposureRating("SHIRT_SLEEVE");
        weather.setSurvivalTimeDescription("Comfortable outdoors with appropriate clothing");
    }

    // ================================================================
    // MOON OUTDOOR EXPOSURE RATING
    // ================================================================

    /**
     * Moon-calibrated exposure rating. Asks "what equipment level do you need to operate here?"
     * rather than "what happens to a naked human?" For moons, vacuum + cold is standard EVA
     * territory (PRESSURE_SUIT), not instant death. LETHAL_SECONDS is reserved for conditions
     * that defeat standard EVA equipment (corrosive chemistry, extreme radiation, crushing pressure).
     */
    private void calculateMoonOutdoorExposure(PlanetaryClimate weather, Planet planet, Star parentStar) {
        String atmClass = planet.getAtmosphereClassification();
        double surfaceTemp = planet.getSurfaceTemp() != null ? planet.getSurfaceTemp() : 250.0;
        double pressureAtm = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 1.0;

        PlanetaryHabitability hab = planet.getHabitability();
        boolean breathable = hab != null && Boolean.TRUE.equals(hab.getIsBreathable());

        int radiationScore = calculateRadiationScore(planet, parentStar);

        // === LETHAL_SECONDS: even a pressure suit won't save you ===
        // Conditions that defeat standard EVA equipment
        if (pressureAtm > 100 || surfaceTemp > 500) {
            weather.setOutdoorExposureRating("LETHAL_SECONDS");
            weather.setSurvivalTimeDescription("Instantaneous death — " + getLethalCause(surfaceTemp, pressureAtm));
            return;
        }
        if ("VENUS_LIKE".equals(atmClass) || "CORROSIVE".equals(atmClass) || "EXOTIC".equals(atmClass)) {
            weather.setOutdoorExposureRating("LETHAL_SECONDS");
            weather.setSurvivalTimeDescription("Lethal atmospheric chemistry defeats standard EVA suits");
            return;
        }
        if (radiationScore >= 5) {
            weather.setOutdoorExposureRating("LETHAL_SECONDS");
            weather.setSurvivalTimeDescription("Lethal radiation — extreme stellar activity overwhelms suit shielding");
            return;
        }

        // === LETHAL_MINUTES: suit failure within minutes ===
        if (surfaceTemp > 400) {
            weather.setOutdoorExposureRating("LETHAL_MINUTES");
            weather.setSurvivalTimeDescription("Extreme heat exceeds EVA suit cooling capacity within minutes");
            return;
        }
        if ("AMMONIA".equals(atmClass)) {
            weather.setOutdoorExposureRating("LETHAL_MINUTES");
            weather.setSurvivalTimeDescription("Ammonia atmosphere risks suit seal breach within minutes");
            return;
        }
        if (radiationScore >= 4) {
            weather.setOutdoorExposureRating("LETHAL_MINUTES");
            weather.setSurvivalTimeDescription("Severe radiation limits EVA to minutes even with shielding");
            return;
        }

        // === PRESSURE_SUIT: standard EVA operations — the expected moon default ===
        // Cold vacuum with manageable chemistry = you need a suit, but you can operate
        if (pressureAtm < 0.3 || surfaceTemp < 200 || surfaceTemp > 350
                || "VOLCANIC".equals(atmClass) || "REDUCING".equals(atmClass)) {
            String cause;
            if ("VOLCANIC".equals(atmClass) || "REDUCING".equals(atmClass)) {
                cause = "toxic atmosphere requires sealed life support";
            } else if (pressureAtm < 0.01) {
                cause = "vacuum conditions";
            } else if (pressureAtm < 0.3) {
                cause = "insufficient atmospheric pressure";
            } else if (surfaceTemp < 200) {
                cause = "cryogenic surface temperatures";
            } else {
                cause = "extreme surface temperatures";
            }
            weather.setOutdoorExposureRating("PRESSURE_SUIT");
            weather.setSurvivalTimeDescription("Full EVA suit required — " + cause);
            return;
        }

        // === ASSISTED: supplemental gear needed ===
        // Rare for moons — requires thick atmosphere, moderate temps (Titan-like)
        if (!breathable) {
            weather.setOutdoorExposureRating("ASSISTED");
            weather.setSurvivalTimeDescription("Supplemental oxygen required — atmosphere is not breathable");
            return;
        }
        if (surfaceTemp < 260 || surfaceTemp > 310) {
            weather.setOutdoorExposureRating("ASSISTED");
            weather.setSurvivalTimeDescription(surfaceTemp < 260 ?
                    "Thermal protection required" : "Active cooling required");
            return;
        }
        if (radiationScore >= 2) {
            weather.setOutdoorExposureRating("ASSISTED");
            weather.setSurvivalTimeDescription("Radiation protection recommended — elevated stellar radiation");
            return;
        }

        // === SHIRT_SLEEVE: exceptional for a moon ===
        weather.setOutdoorExposureRating("SHIRT_SLEEVE");
        weather.setSurvivalTimeDescription("Comfortable outdoors — an exceptionally habitable moon");
    }

    // ================================================================
    // SHARED RADIATION SCORE CALCULATION
    // ================================================================

    private int calculateRadiationScore(Planet planet, Star parentStar) {
        int radiationScore = 0;
        PlanetaryMagneticField mag = planet.getMagneticField();
        PlanetaryHabitability hab = planet.getHabitability();
        PlanetaryMagneticField.ProtectionLevel protectionLevel = mag != null
                ? mag.getProtectionLevel() : null;
        boolean hasOzone = hab != null && Boolean.TRUE.equals(hab.getHasOzoneLayer());
        boolean shielded = mag != null && Boolean.TRUE.equals(mag.getShieldsFromStellarWind());

        String starActivity = parentStar != null && parentStar.getActivityLevel() != null
                ? parentStar.getActivityLevel() : "MODERATE";
        double flareFreq = parentStar != null && parentStar.getFlareFrequencyPerDay() != null
                ? parentStar.getFlareFrequencyPerDay() : 0.0;

        // Star activity contribution
        switch (starActivity) {
            case "EXTREMELY_ACTIVE": radiationScore += 3; break;
            case "VERY_ACTIVE":      radiationScore += 2; break;
            case "ACTIVE":           radiationScore += 1; break;
            case "MODERATE", "QUIET": radiationScore += 0; break;
        }
        // Flare frequency adds additional risk
        if (flareFreq > 10.0) radiationScore += 2;
        else if (flareFreq > 5.0) radiationScore += 1;

        // Magnetic protection reduces score
        if (protectionLevel == PlanetaryMagneticField.ProtectionLevel.EXCEPTIONAL) radiationScore -= 3;
        else if (protectionLevel == PlanetaryMagneticField.ProtectionLevel.STRONG) radiationScore -= 2;
        else if (protectionLevel == PlanetaryMagneticField.ProtectionLevel.MODERATE) radiationScore -= 1;
        else if (protectionLevel == null || protectionLevel == PlanetaryMagneticField.ProtectionLevel.NONE) radiationScore += 1;
        // MINIMAL: no modifier

        // Ozone provides UV shielding
        if (hasOzone) radiationScore -= 1;
        // Stellar wind shielding
        if (shielded) radiationScore -= 1;

        return Math.max(0, Math.min(5, radiationScore));
    }

    private String getLethalCause(double temp, double pressure) {
        if (pressure > 100) return "crushing atmospheric pressure";
        if (temp > 500) return "extreme heat";
        if (pressure < 0.006) return "explosive decompression";
        if (temp < 100) return "extreme cold";
        return "hostile conditions";
    }

    // ================================================================
    // WEATHER SEVERITY
    // ================================================================

    private void calculateWeatherSeverity(PlanetaryClimate weather, List<ClimateHazard> hazards) {
        if (hazards.isEmpty()) {
            weather.setWeatherSeverity("BENIGN");
            return;
        }

        // Score-based approach: each hazard contributes points based on its severity
        int score = 0;
        for (ClimateHazard h : hazards) {
            score += switch (h.getSeverity()) {
                case "LETHAL" -> 4;
                case "EXTREME" -> 3;
                case "HIGH" -> 2;
                case "MODERATE" -> 1;
                default -> 0;
            };
        }

        boolean isMoon = weather.isMoonClimate();

        if (isMoon) {
            // Moon-calibrated thresholds:
            // Most moons land 4-8 from baseline hazards after moon-aware downgrading.
            // This creates a gradient within the moon population:
            //   MILD: 1-3       (unusual warm, pressurized moon with some shielding)
            //   MODERATE: 4-6   (warm-ish moon with decent atmosphere, like a Titan-type)
            //   SEVERE: 7-10    (typical moon — cold, thin atmo, no mag field)
            //   EXTREME: 11-14  (above-average — adds high winds, extreme temp swings, toxic chemistry)
            //   APOCALYPTIC: 15+ (stacking lethal chemistry on top of harsh baseline)
            if (score >= 15) {
                weather.setWeatherSeverity("APOCALYPTIC");
            } else if (score >= 11) {
                weather.setWeatherSeverity("EXTREME");
            } else if (score >= 7) {
                weather.setWeatherSeverity("SEVERE");
            } else if (score >= 4) {
                weather.setWeatherSeverity("MODERATE");
            } else if (score >= 1) {
                weather.setWeatherSeverity("MILD");
            } else {
                weather.setWeatherSeverity("BENIGN");
            }
        } else {
            // Planet thresholds (unchanged)
            if (score >= 12) {
                weather.setWeatherSeverity("APOCALYPTIC");
            } else if (score >= 7) {
                weather.setWeatherSeverity("EXTREME");
            } else if (score >= 4) {
                weather.setWeatherSeverity("SEVERE");
            } else if (score >= 2) {
                weather.setWeatherSeverity("MODERATE");
            } else if (score == 1) {
                weather.setWeatherSeverity("MILD");
            } else {
                weather.setWeatherSeverity("BENIGN");
            }
        }
    }

    // ================================================================
    // PLANET WEATHER SUMMARY
    // ================================================================

    private void generatePlanetWeatherSummary(PlanetaryClimate weather, Planet planet, Star parentStar) {
        StringBuilder summary = new StringBuilder();
        String atmClass = planet.getAtmosphereClassification();

        // Opening: atmosphere type characterization
        summary.append(planetAtmosphereOpener(atmClass));

        // Temperature regime
        double surfaceTemp = planet.getSurfaceTemp() != null ? planet.getSurfaceTemp() : 250.0;
        if (surfaceTemp > 350) {
            summary.append(" Surface temperatures are extremely hot");
        } else if (surfaceTemp > 300) {
            summary.append(" Warm surface temperatures");
        } else if (surfaceTemp > 260) {
            summary.append(" Temperate surface conditions");
        } else if (surfaceTemp > 220) {
            summary.append(" Cold surface temperatures");
        } else {
            summary.append(" Frigid surface conditions");
        }

        // Seasonal info
        appendSeasonInfo(summary, weather);

        // Wind characterization
        appendWindInfo(summary, weather);

        // Cloud and precipitation
        appendCloudInfo(summary, weather, "planet");

        // Precipitation
        appendPrecipitationInfo(summary, weather);

        // Notable storm activity
        appendStormInfo(summary, weather);

        // Exposure rating as closing
        String exposure = weather.getOutdoorExposureRating();
        if (exposure != null) {
            summary.append(". Outdoor exposure rating: ").append(exposure.replace('_', ' ').toLowerCase());
        }

        summary.append(".");
        weather.setWeatherSummary(truncate(summary.toString(), 1000));
    }

    // ================================================================
    // MOON WEATHER SUMMARY
    // ================================================================

    private void generateMoonWeatherSummary(PlanetaryClimate weather, Planet planet, Star parentStar) {
        StringBuilder summary = new StringBuilder();
        String atmClass = planet.getAtmosphereClassification();
        double surfaceTemp = planet.getSurfaceTemp() != null ? planet.getSurfaceTemp() : 250.0;

        // Opening: moon-appropriate atmosphere description
        summary.append(moonAtmosphereOpener(atmClass));

        // Temperature regime — moon-framed
        if (surfaceTemp > 350) {
            summary.append(" Unusually hot surface for a moon");
        } else if (surfaceTemp > 250) {
            summary.append(" Relatively warm surface conditions");
        } else if (surfaceTemp > 150) {
            summary.append(" Typical cold surface conditions");
        } else if (surfaceTemp > 80) {
            summary.append(" Deep cryogenic surface environment");
        } else {
            summary.append(" Ultra-cold surface near absolute zero");
        }

        // Seasonal info
        appendSeasonInfo(summary, weather);

        // Wind
        appendWindInfo(summary, weather);

        // Clouds
        appendCloudInfo(summary, weather, "moon");

        // Precipitation
        appendPrecipitationInfo(summary, weather);

        // Storm activity
        appendStormInfo(summary, weather);

        // Exposure rating as closing — EVA-framed for moons
        String exposure = weather.getOutdoorExposureRating();
        if (exposure != null) {
            summary.append(". EVA rating: ").append(exposure.replace('_', ' ').toLowerCase());
        }

        summary.append(".");
        weather.setWeatherSummary(truncate(summary.toString(), 1000));
    }

    // ================================================================
    // SUMMARY HELPERS
    // ================================================================

    private void appendSeasonInfo(StringBuilder summary, PlanetaryClimate weather) {
        String seasonDesc = weather.getSeasonDescription();
        if (seasonDesc != null && !seasonDesc.isEmpty()) {
            summary.append(". ").append(seasonDesc);
        }
    }

    private void appendWindInfo(StringBuilder summary, PlanetaryClimate weather) {
        String windIntensity = weather.getWindIntensity();
        if (windIntensity != null) {
            switch (windIntensity) {
                case "HURRICANE":
                case "EXTREME":
                    summary.append(". Extreme winds dominate the surface environment");
                    break;
                case "STRONG":
                    summary.append(". Strong persistent winds shape the landscape");
                    break;
                case "MODERATE":
                    summary.append(" with moderate winds");
                    break;
                case "CALM":
                    summary.append(" with calm, nearly still air");
                    break;
            }
        }
    }

    private void appendCloudInfo(StringBuilder summary, PlanetaryClimate weather, String bodyType) {
        Double cloudCoverage = weather.getCloudCoveragePercent();
        if (cloudCoverage != null) {
            if (cloudCoverage > 90) {
                summary.append(". Perpetual cloud cover blankets the ").append(bodyType.equals("moon") ? "surface" : "planet");
            } else if (cloudCoverage > 60) {
                summary.append(". Clouds frequently cover the sky");
            } else if (cloudCoverage < 10) {
                summary.append(". Skies are predominantly clear");
            }
        }
    }

    private void appendPrecipitationInfo(StringBuilder summary, PlanetaryClimate weather) {
        String precipType = weather.getPrimaryPrecipitationType();
        if (precipType != null && !"NONE".equals(precipType)) {
            String readable = precipType.replace('_', ' ').toLowerCase();
            String freq = weather.getPrecipitationFrequency();
            if (freq != null) {
                summary.append(". ").append(capitalize(freq.toLowerCase())).append(" ").append(readable);
            }
        }
    }

    private void appendStormInfo(StringBuilder summary, PlanetaryClimate weather) {
        String stormFreq = weather.getStormFrequency();
        if ("CONSTANT".equals(stormFreq)) {
            summary.append(". Storm activity is constant and intense");
        } else if ("FREQUENT".equals(stormFreq)) {
            summary.append(". Storms occur frequently");
        }
    }

    private String planetAtmosphereOpener(String atmClass) {
        return switch (atmClass) {
            case "EARTH_LIKE" -> "An Earth-like atmosphere supports familiar weather patterns.";
            case "VENUS_LIKE" -> "A crushing Venus-like atmosphere with opaque sulfuric acid clouds.";
            case "MARS_LIKE" -> "A thin Mars-like CO2 atmosphere with minimal weather activity.";
            case "TITAN_LIKE" -> "A dense nitrogen-methane atmosphere with exotic hydrocarbon weather.";
            case "JOVIAN" -> "A deep hydrogen-helium atmosphere with powerful banded weather systems.";
            case "ICE_GIANT" -> "A hydrogen-methane atmosphere with extreme wind speeds.";
            case "VOLCANIC" -> "A toxic volcanic atmosphere dominated by sulfur compounds.";
            case "AMMONIA" -> "An ammonia-rich atmosphere with toxic clouds and precipitation.";
            case "REDUCING" -> "A primordial reducing atmosphere with no free oxygen.";
            case "CORROSIVE" -> "A highly corrosive atmosphere hostile to all materials.";
            case "EXOTIC" -> "An exotic atmosphere with extreme chemistry and temperatures.";
            default -> "An atmosphere with distinctive weather patterns.";
        };
    }

    private String moonAtmosphereOpener(String atmClass) {
        return switch (atmClass) {
            case "EARTH_LIKE" -> "A remarkably Earth-like atmosphere — exceptional for a moon.";
            case "MARS_LIKE" -> "A thin CO2 atmosphere with limited weather activity.";
            case "TITAN_LIKE" -> "A dense nitrogen-methane atmosphere with hydrocarbon weather cycles.";
            case "VOLCANIC" -> "A volcanic atmosphere of sulfur compounds driven by tidal heating.";
            case "AMMONIA" -> "An ammonia-rich atmosphere with toxic precipitation.";
            case "REDUCING" -> "A primordial reducing atmosphere with no free oxygen.";
            case "CORROSIVE" -> "A corrosive atmosphere hostile to equipment and biological tissue.";
            case "EXOTIC" -> "An exotic atmosphere with extreme chemistry.";
            default -> "An atmosphere with distinctive conditions.";
        };
    }

    // ================================================================
    // UTILITY
    // ================================================================

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String truncate(String s, int maxLength) {
        if (s == null) return null;
        return s.length() <= maxLength ? s : s.substring(0, maxLength - 3) + "...";
    }
}