package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.PlanetaryHabitability;
import com.brickroad.starcreator_webservice.entity.ud.PlanetaryMagneticField;
import com.brickroad.starcreator_webservice.entity.ud.Star;
import com.brickroad.starcreator_webservice.enums.ColonizationSuitability;
import com.brickroad.starcreator_webservice.enums.HabitabilityClass;
import com.brickroad.starcreator_webservice.enums.TerraformingPotential;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HabitabilityCreator {

    private static final double EARTH_RADIUS = 1.0;        // Earth radii
    private static final double EARTH_DENSITY = 5.51;       // g/cm³
    private static final double EARTH_ESCAPE_VEL = 11.186;  // km/s
    private static final double EARTH_SURFACE_TEMP = 288.0; // K
    private static final double EARTH_GEOTHERMAL_FLUX = 87.0; // mW/m²

    public PlanetaryHabitability assess(Planet planet, Star parentStar) {
        PlanetaryHabitability hab = new PlanetaryHabitability();
        hab.setPlanet(planet);

        String planetType = planet.getPlanetType() != null ? planet.getPlanetType().toLowerCase() : "";

        if (isGaseousBody(planetType)) {
            assessGasGiant(hab, planet, parentStar);
            return hab;
        }

        calculateESI(hab, planet);
        assessRadiationEnvironment(hab, planet, parentStar);
        assessAtmosphericHabitability(hab, planet);
        assessWaterPotential(hab, planet);
        assessGeologicalHabitability(hab, planet, parentStar);
        assessOrbitalEnvironment(hab, planet, parentStar);
        assessBiosignatures(hab, planet, parentStar);
        classifyHabitability(hab, planet, parentStar);

        return hab;
    }

    // ================================================================
    // ESI CALCULATION
    // ================================================================

    private void calculateESI(PlanetaryHabitability hab, Planet planet) {
        Double radius = planet.getEarthRadius();
        Double density = planet.getDensity();
        Double escapeVel = planet.getEscapeVelocity();
        Double surfaceTemp = planet.getSurfaceTemp();

        if (radius == null || density == null || escapeVel == null || surfaceTemp == null) {
            hab.setEsiTotal(0.0);
            hab.setEsiInterior(0.0);
            hab.setEsiSurface(0.0);
            return;
        }

        // ESI_i = (1 - |x_i - x_ref| / (x_i + x_ref))^w_i
        double esiRadius = esiComponent(radius, EARTH_RADIUS, 0.57);
        double esiDensity = esiComponent(density, EARTH_DENSITY, 1.07);
        double esiEscapeVel = esiComponent(escapeVel, EARTH_ESCAPE_VEL, 0.70);
        double esiTemp = esiComponent(surfaceTemp, EARTH_SURFACE_TEMP, 5.58);

        double interior = Math.sqrt(esiRadius * esiDensity);
        double surface = Math.sqrt(esiEscapeVel * esiTemp);
        double total = Math.sqrt(interior * surface);

        hab.setEsiInterior(clamp01(interior));
        hab.setEsiSurface(clamp01(surface));
        hab.setEsiTotal(clamp01(total));
    }

    private double esiComponent(double value, double reference, double weight) {
        if (value + reference == 0) return 0;
        double ratio = Math.abs(value - reference) / (value + reference);
        return Math.pow(1.0 - ratio, weight);
    }

    // ================================================================
    // RADIATION ENVIRONMENT
    // ================================================================

    private void assessRadiationEnvironment(PlanetaryHabitability hab, Planet planet, Star parentStar) {
        if (parentStar == null || planet.getSemiMajorAxisAU() == null) {
            hab.setUvSurfaceFluxEarth(0.0);
            hab.setUvHazardLevel("UNKNOWN");
            hab.setCosmicRayFluxEarth(1.0);
            hab.setRadiationBeltSurfaceDose("UNKNOWN");
            return;
        }

        double luminosity = parentStar.getSolarLuminosity() == 0 ? parentStar.getSolarLuminosity() : 1.0;
        double distance = planet.getSemiMajorAxisAU();

        // UV flux relative to Earth (scales with luminosity / distance²)
        // Hot stars produce proportionally more UV than cool stars
        double spectralUvFactor = getSpectralUvFactor(parentStar);
        double rawUvFlux = (luminosity * spectralUvFactor) / (distance * distance);

        // Atmospheric attenuation
        double atmAttenuation = getAtmosphericUvAttenuation(planet);
        double surfaceUvFlux = rawUvFlux * atmAttenuation;
        hab.setUvSurfaceFluxEarth(surfaceUvFlux);

        // UV hazard classification
        if (surfaceUvFlux < 0.5) hab.setUvHazardLevel("NONE");
        else if (surfaceUvFlux < 1.5) hab.setUvHazardLevel("LOW");
        else if (surfaceUvFlux < 5.0) hab.setUvHazardLevel("MODERATE");
        else if (surfaceUvFlux < 20.0) hab.setUvHazardLevel("HIGH");
        else if (surfaceUvFlux < 100.0) hab.setUvHazardLevel("EXTREME");
        else hab.setUvHazardLevel("STERILIZING");

        // Cosmic ray flux — magnetic field is the main shield
        PlanetaryMagneticField magField = planet.getMagneticField();
        double cosmicRayFactor = 1.0;
        if (magField != null && magField.getProtectionLevel() != null) {
            cosmicRayFactor = switch (magField.getProtectionLevel()) {
                case EXCEPTIONAL -> 0.1;
                case STRONG -> 0.3;
                case MODERATE -> 0.6;
                case MINIMAL -> 0.9;
                case NONE -> 1.5;
            };
        }
        // Atmosphere also shields (mass column density)
        double pressure = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 0;
        if (pressure > 0.5) cosmicRayFactor *= 0.5;
        else if (pressure > 0.1) cosmicRayFactor *= 0.7;
        hab.setCosmicRayFluxEarth(cosmicRayFactor);

        // Ionizing radiation (sum of UV + cosmic ray + stellar particles)
        double ionizing = 2.4 * cosmicRayFactor + surfaceUvFlux * 0.5; // Earth baseline ~2.4 mSv/yr
        hab.setIonizingRadiationSurfaceMsvYr(ionizing);

        // Stellar particle flux
        double particleFlux = 0;
        if (parentStar.getStellarWindDensityAt1AU() != null) {
            particleFlux = parentStar.getStellarWindDensityAt1AU() / (distance * distance);
        }
        hab.setStellarParticleFlux(particleFlux);

        // Radiation belt dose
        if (magField != null && magField.getMagnetosphereExists() != null && magField.getMagnetosphereExists()) {
            Double fieldStrength = magField.getStrengthComparedToEarth();
            if (fieldStrength != null && fieldStrength > 5.0) {
                hab.setRadiationBeltSurfaceDose("MODERATE");
            } else if (fieldStrength != null && fieldStrength > 50.0) {
                hab.setRadiationBeltSurfaceDose("DANGEROUS");
            } else {
                hab.setRadiationBeltSurfaceDose("NEGLIGIBLE");
            }
        } else {
            hab.setRadiationBeltSurfaceDose("NONE");
        }

        // Flare exposure risk
        String flareClass = parentStar.getFlareClass();
        Double flareFreq = parentStar.getFlareFrequencyPerDay();
        if (flareClass == null || "NONE".equals(flareClass) || "NANOFLARE".equals(flareClass)) {
            hab.setFlareExposureRisk("NONE");
        } else if ("MICROFLARE".equals(flareClass)) {
            hab.setFlareExposureRisk("LOW");
        } else if (distance > 1.0) {
            hab.setFlareExposureRisk("LOW");
        } else if ("C_CLASS".equals(flareClass)) {
            hab.setFlareExposureRisk(distance < 0.3 ? "MODERATE" : "LOW");
        } else if ("M_CLASS".equals(flareClass)) {
            hab.setFlareExposureRisk(distance < 0.5 ? "HIGH" : "MODERATE");
        } else if ("X_CLASS".equals(flareClass)) {
            hab.setFlareExposureRisk(distance < 0.5 ? "EXTREME" : "HIGH");
        } else if ("SUPERFLARE".equals(flareClass)) {
            hab.setFlareExposureRisk("EXTREME");
        } else {
            hab.setFlareExposureRisk("MODERATE");
        }
    }

    // ================================================================
    // ATMOSPHERIC HABITABILITY
    // ================================================================

    private void assessAtmosphericHabitability(PlanetaryHabitability hab, Planet planet) {
        String atmClass = planet.getAtmosphereClassification() != null ? planet.getAtmosphereClassification() : "";
        double pressure = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 0;
        String composition = planet.getAtmosphereComposition() != null ? planet.getAtmosphereComposition() : "";

        // Parse oxygen percentage from atmosphere composition
        double o2Percent = parseGasPercentage(composition, "O2");
        hab.setOxygenPercentage(o2Percent);

        // Breathability check
        List<String> issues = new ArrayList<>();

        if (pressure < 0.5) issues.add("Pressure too low (" + String.format("%.3f", pressure) + " atm)");
        if (pressure > 4.0) issues.add("Pressure dangerously high (" + String.format("%.1f", pressure) + " atm)");
        if (o2Percent < 16.0) issues.add("Insufficient O2 (" + String.format("%.1f", o2Percent) + "%)");
        if (o2Percent > 25.0) issues.add("O2 toxicity risk (" + String.format("%.1f", o2Percent) + "%)");
        if ("NONE".equals(atmClass)) issues.add("No atmosphere");

        // Check for toxic gases
        double co2Percent = parseGasPercentage(composition, "CO2");
        double coPercent = parseGasPercentage(composition, "CO");
        double so2Percent = parseGasPercentage(composition, "SO2");
        double h2sPercent = parseGasPercentage(composition, "H2S");
        double hclPercent = parseGasPercentage(composition, "HCl");

        if (co2Percent > 5.0) issues.add("Toxic CO2 levels (" + String.format("%.1f", co2Percent) + "%)");
        if (coPercent > 0.01) issues.add("Toxic CO (" + String.format("%.3f", coPercent) + "%)");
        if (so2Percent > 0.001) issues.add("Toxic SO2");
        if (h2sPercent > 0.001) issues.add("Toxic H2S");
        if (hclPercent > 0.001) issues.add("Corrosive HCl");

        boolean breathable = issues.isEmpty() && pressure >= 0.5 && pressure <= 4.0
                && o2Percent >= 16.0 && o2Percent <= 25.0;

        hab.setIsBreathable(breathable);
        hab.setBreathabilityIssues(issues.isEmpty() ? null : String.join("; ", issues));

        // Oxygen source
        if (o2Percent > 1.0) {
            if ("EARTH_LIKE".equals(atmClass)) {
                hab.setOxygenSource("BIOLOGICAL");
            } else {
                hab.setOxygenSource("PHOTOCHEMICAL");
            }
        } else if (o2Percent > 0.01) {
            hab.setOxygenSource("GEOLOGICAL");
        } else {
            hab.setOxygenSource("NONE");
        }

        // Ozone layer (requires O2 > ~1% and UV flux)
        boolean hasOzone = o2Percent > 1.0 && pressure > 0.1;
        hab.setHasOzoneLayer(hasOzone);
        if (hasOzone) {
            // Earth has ~300 DU, scales roughly with O2 percentage and pressure
            double ozone = 300.0 * (o2Percent / 21.0) * Math.min(1.0, pressure);
            hab.setOzoneColumnDobson(ozone);
        }

        // Greenhouse warming
        double greenhouse = estimateGreenhouseWarming(planet);
        hab.setGreenhouseWarningK(greenhouse);

        // Atmospheric retention score (0-1, likelihood of keeping atmosphere over Gyr)
        double retention = calculateAtmosphericRetention(planet);
        hab.setAtmosphericRetentionScore(retention);

        // Toxic gas hazard
        if (so2Percent > 0.01 || hclPercent > 0.01 || h2sPercent > 0.01) {
            hab.setToxicGasHazard("LETHAL");
        } else if (co2Percent > 10.0 || coPercent > 0.1) {
            hab.setToxicGasHazard("HIGH");
        } else if (co2Percent > 2.0 || coPercent > 0.01) {
            hab.setToxicGasHazard("MODERATE");
        } else if (co2Percent > 0.5) {
            hab.setToxicGasHazard("LOW");
        } else {
            hab.setToxicGasHazard("NONE");
        }
    }

    // ================================================================
    // WATER POTENTIAL
    // ================================================================

    private void assessWaterPotential(PlanetaryHabitability hab, Planet planet) {
        Double temp = planet.getSurfaceTemp();
        Double pressure = planet.getSurfacePressure();
        String waterInv = planet.getWaterInventory();

        // Mean surface temp in habitable range?
        hab.setMeanSurfaceTempHabitable(temp != null && temp >= 273.0 && temp <= 373.0);

        // Surface liquid water possible? (from WaterCreator's assessment)
        Double liquidPercent = planet.getLiquidWaterCoveragePercent();
        hab.setSurfaceLiquidWaterPossible(liquidPercent != null && liquidPercent > 0.1);

        // Water phase at surface
        if (temp == null || pressure == null) {
            hab.setWaterPhaseAtSurface("UNKNOWN");
        }

        double liquidPct = planet.getLiquidWaterCoveragePercent() != null ? planet.getLiquidWaterCoveragePercent() : 0.0;
        double icePct = planet.getIceCoveragePercent() != null ? planet.getIceCoveragePercent() : 0.0;

        if (temp > 647) {
            hab.setWaterPhaseAtSurface("SUPERCRITICAL");
        } else if (temp > 373 && pressure < 1.0) {
            hab.setWaterPhaseAtSurface("VAPOR");
        } else if (liquidPct > 0.1 && icePct > 0.1) {
            hab.setWaterPhaseAtSurface("MIXED");
        } else if (liquidPct > 0.1) {
            hab.setWaterPhaseAtSurface("LIQUID");
        } else if (icePct > 0.1) {
            hab.setWaterPhaseAtSurface("ICE");
        } else if ("NONE".equals(planet.getWaterInventory()) || (liquidPct <= 0 && icePct <= 0)) {
            hab.setWaterPhaseAtSurface("NONE");
        } else {
            hab.setWaterPhaseAtSurface("VAPOR");
        }

        // Habitable surface fraction (where liquid water COULD exist)
        if (temp != null && temp >= 253 && temp <= 393 && pressure != null && pressure >= 0.006) {
            // Rough estimate: fraction of surface within liquid water temperature range
            // Accounts for latitude variation (equator is warmer than poles)
            double centerDeviation = Math.abs(temp - 300.0);
            double fraction = Math.max(0, 1.0 - centerDeviation / 100.0);
            hab.setHabitableSurfaceFraction(clamp01(fraction));
        } else {
            hab.setHabitableSurfaceFraction(0.0);
        }

        // Subsurface ocean
        hab.setSubsurfaceOceanPossible(Boolean.TRUE.equals(planet.getHasSubsurfaceWater()));

        // Water source likelihood (from inventory)
        hab.setWaterSourceLikelihood(waterInv != null ? waterInv : "NONE");
    }

    // ================================================================
    // GEOLOGICAL HABITABILITY
    // ================================================================

    private void assessGeologicalHabitability(PlanetaryHabitability hab, Planet planet, Star parentStar) {
        // Carbon cycle requires plate tectonics + volcanism + water
        boolean hasTectonics = Boolean.TRUE.equals(planet.getHasPlateTectonics());
        boolean hasVolcanism = Boolean.TRUE.equals(planet.getHasVolcanicActivity());
        String waterInv = planet.getWaterInventory();
        boolean hasWater = waterInv != null && !"NONE".equals(waterInv) && !"TRACE".equals(waterInv);

        boolean carbonCycle = hasTectonics && hasVolcanism && hasWater;
        hab.setHasCarbonCycle(carbonCycle);

        if (carbonCycle) {
            Double activityScore = planet.getActivityScore();
            if (activityScore != null && activityScore > 5.0) {
                hab.setCarbonCycleStrength("STRONG");
            } else if (activityScore != null && activityScore > 2.0) {
                hab.setCarbonCycleStrength("MODERATE");
            } else {
                hab.setCarbonCycleStrength("WEAK");
            }
        } else if (hasVolcanism && hasWater) {
            hab.setCarbonCycleStrength("WEAK"); // Partial cycle without full tectonics
            hab.setHasCarbonCycle(true);
        } else {
            hab.setCarbonCycleStrength("NONE");
        }

        // Geothermal heat flux (Earth ~87 mW/m²)
        Double activityScore = planet.getActivityScore();
        double mass = planet.getEarthMass() != null ? planet.getEarthMass() : 1.0;
        if (activityScore != null) {
            // Rough scaling: activity score correlates with heat production
            double flux = EARTH_GEOTHERMAL_FLUX * (activityScore / 5.0) * Math.pow(mass, 0.3);
            hab.setGeothermalHeatFluxMwM2(flux);
        } else {
            hab.setGeothermalHeatFluxMwM2(0.0);
        }

        // Magnetic protection adequate?
        PlanetaryMagneticField magField = planet.getMagneticField();
        if (magField != null && magField.getProtectionLevel() != null) {
            boolean adequate = magField.getProtectionLevel() == PlanetaryMagneticField.ProtectionLevel.STRONG
                    || magField.getProtectionLevel() == PlanetaryMagneticField.ProtectionLevel.EXCEPTIONAL;
            hab.setMagneticProtectionAdequate(adequate);
        } else {
            hab.setMagneticProtectionAdequate(false);
        }

        // Tidal heating contribution
        if (parentStar != null && planet.getSemiMajorAxisAU() != null && planet.getEccentricity() != null) {
            double ecc = planet.getEccentricity();
            double dist = planet.getSemiMajorAxisAU();
            if (ecc > 0.1 && dist < 0.3) {
                hab.setTidalHeatingContribution(ecc > 0.3 ? "DOMINANT" : "SIGNIFICANT");
            } else if (ecc > 0.05 && dist < 1.0) {
                hab.setTidalHeatingContribution("MINOR");
            } else {
                hab.setTidalHeatingContribution("NONE");
            }
        } else {
            hab.setTidalHeatingContribution("NONE");
        }

        // Nutrient cycling potential
        if (hasTectonics && hasVolcanism && hasWater) {
            hab.setNutrientCyclingPotential("HIGH");
        } else if (hasVolcanism && hasWater) {
            hab.setNutrientCyclingPotential("MODERATE");
        } else if (hasVolcanism || hasWater) {
            hab.setNutrientCyclingPotential("LOW");
        } else {
            hab.setNutrientCyclingPotential("NONE");
        }
    }

    // ================================================================
    // ORBITAL & STELLAR ENVIRONMENT
    // ================================================================

    private void assessOrbitalEnvironment(PlanetaryHabitability hab, Planet planet, Star parentStar) {
        if (parentStar == null) {
            hab.setStellarLifetimeRemainingMy(null);
            hab.setTimeInHabitableZoneMy(null);
            hab.setOrbitalStabilityScore(0.5);
            hab.setTidalLockRisk("UNKNOWN");
            hab.setObliquityStability("UNKNOWN");
            return;
        }

        // Stellar lifetime remaining
        Double remainingMs = parentStar.getEstimatedRemainingMsMy();
        hab.setStellarLifetimeRemainingMy(remainingMs);

        // Time in habitable zone (approximate: planet age if currently in HZ)
        String hzPosition = planet.getHabitableZonePosition();
        if ("habitable".equals(hzPosition) && planet.getAgeMY() != null) {
            // Simplified: assume planet has been roughly in HZ for its lifetime
            // (HZ migration is slow for main sequence stars)
            hab.setTimeInHabitableZoneMy(planet.getAgeMY() * RandomUtils.rollRange(0.5, 1.0));
        } else {
            hab.setTimeInHabitableZoneMy(0.0);
        }

        // Orbital stability score
        double stability = 1.0;
        Double ecc = planet.getEccentricity();
        if (ecc != null) {
            if (ecc > 0.3) stability -= 0.3;
            else if (ecc > 0.1) stability -= 0.1;
        }
        // Binary systems reduce stability
        if (parentStar.getSystem() != null && parentStar.getSystem().getStars() != null
                && parentStar.getSystem().getStars().size() > 1) {
            stability -= 0.15;
        }
        hab.setOrbitalStabilityScore(clamp01(stability));

        // Tidal lock risk
        Boolean locked = planet.getTidallyLocked();
        if (Boolean.TRUE.equals(locked)) {
            hab.setTidalLockRisk("LOCKED");
        } else if (planet.getSemiMajorAxisAU() != null) {
            double dist = planet.getSemiMajorAxisAU();
            double starMass = parentStar.getSolarMass() == 0 ? parentStar.getSolarMass() : 1.0;
            // Tidal lock timescale roughly: distance^6 / starMass^2
            double lockTimescale = Math.pow(dist, 6) / Math.pow(starMass, 2) * 1e4;
            Double age = planet.getAgeMY();
            if (age != null && lockTimescale < age) {
                hab.setTidalLockRisk("HIGH");
            } else if (age != null && lockTimescale < age * 3) {
                hab.setTidalLockRisk("MODERATE");
            } else if (dist < 0.3 && starMass < 0.5) {
                hab.setTidalLockRisk("HIGH");
            } else {
                hab.setTidalLockRisk("LOW");
            }
        } else {
            hab.setTidalLockRisk("LOW");
        }

        // Obliquity stability (large moons stabilize, no moon = chaotic)
        Integer moonCount = planet.getNumberOfMoons();
        double mass = planet.getEarthMass() != null ? planet.getEarthMass() : 1.0;
        if (moonCount != null && moonCount > 0 && mass > 0.3) {
            hab.setObliquityStability("STABLE");
        } else if (mass > 0.5) {
            hab.setObliquityStability("OSCILLATING");
        } else {
            hab.setObliquityStability("CHAOTIC");
        }

        // Day/night temperature range
        Double rotation = planet.getRotationPeriodHours();
        Double surfaceTemp = planet.getSurfaceTemp();
        double pressure = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 0;
        if (Boolean.TRUE.equals(locked) && surfaceTemp != null) {
            // Tidally locked: extreme range unless thick atmosphere redistributes heat
            double baseRange = surfaceTemp * 0.8;
            if (pressure > 1.0) baseRange *= 0.3; // Thick atm redistributes heat
            else if (pressure > 0.1) baseRange *= 0.5;
            hab.setDayNightTempRangeK(baseRange);
        } else if (rotation != null && surfaceTemp != null) {
            // Slow rotators have larger day/night range
            double rotFactor = Math.min(3.0, rotation / 24.0);
            double baseRange = 30.0 * rotFactor; // Earth ~10-15K average
            if (pressure < 0.1) baseRange *= 3.0; // No atmosphere buffer
            hab.setDayNightTempRangeK(baseRange);
        }
    }

    // ================================================================
    // BIOSIGNATURE & LIFE ASSESSMENT
    // ================================================================

    private void assessBiosignatures(PlanetaryHabitability hab, Planet planet, Star parentStar) {
        List<String> indicators = new ArrayList<>();
        List<String> energySources = new ArrayList<>();
        int bioScore = 0;

        String atmClass = planet.getAtmosphereClassification() != null ? planet.getAtmosphereClassification() : "";
        Double temp = planet.getSurfaceTemp();
        String waterInv = planet.getWaterInventory();
        boolean hasLiquidWater = Boolean.TRUE.equals(hab.getSurfaceLiquidWaterPossible());
        boolean hasSubsurfaceWater = Boolean.TRUE.equals(planet.getHasSubsurfaceWater());

        // O2 + CH4 coexistence (strong biosignature - thermodynamically unstable without biology)
        double o2Pct = hab.getOxygenPercentage() != null ? hab.getOxygenPercentage() : 0;
        String composition = planet.getAtmosphereComposition() != null ? planet.getAtmosphereComposition() : "";
        double ch4Pct = parseGasPercentage(composition, "CH4");
        if (o2Pct > 1.0 && ch4Pct > 0.0001) {
            indicators.add("O2-CH4 thermodynamic disequilibrium");
            bioScore += 30;
        }

        // N2-O2 atmosphere with trace CO2 (biological signature)
        if ("EARTH_LIKE".equals(atmClass) && o2Pct > 15.0) {
            indicators.add("N2-O2 atmosphere with biological O2 levels");
            bioScore += 25;
        }

        // Liquid water present
        if (hasLiquidWater) {
            indicators.add("Surface liquid water present");
            bioScore += 15;
            energySources.add("Stellar radiation (surface)");
        }

        // Subsurface water with geothermal
        if (hasSubsurfaceWater && Boolean.TRUE.equals(planet.getHasVolcanicActivity())) {
            indicators.add("Geothermally heated subsurface water");
            bioScore += 10;
            energySources.add("Geothermal vents");
            energySources.add("Chemical gradients");
        }

        // Temperature in habitable range
        if (temp != null && temp >= 273 && temp <= 373) {
            bioScore += 10;
        } else if (temp != null && temp >= 253 && temp <= 393) {
            bioScore += 3; // Extremophile range
        }

        // Stellar radiation as energy source
        if (parentStar != null && planet.getSemiMajorAxisAU() != null) {
            double luminosity = parentStar.getSolarLuminosity() == 0 ? parentStar.getSolarLuminosity() : 0;
            double flux = luminosity / (planet.getSemiMajorAxisAU() * planet.getSemiMajorAxisAU());
            if (flux > 0.1 && flux < 5.0) {
                if (!energySources.contains("Stellar radiation (surface)")) {
                    energySources.add("Stellar radiation");
                }
            }
        }

        // Photosynthesis viability (PAR flux 400-700nm)
        boolean photoViable = false;
        if (parentStar != null && planet.getSemiMajorAxisAU() != null) {
            String spectral = parentStar.getSpectralType();
            double distance = planet.getSemiMajorAxisAU();
            double luminosity = parentStar.getSolarLuminosity() == 0 ? parentStar.getSolarLuminosity() : 1.0;
            double parFlux = luminosity / (distance * distance);
            // M dwarfs produce less PAR (more IR), but life could adapt
            if ("M".equals(spectral)) parFlux *= 0.3;
            photoViable = parFlux > 0.05 && hasLiquidWater;
            if (photoViable) energySources.add("Photosynthetically active radiation");
        }
        hab.setPhotosynthesisViable(photoViable);

        // Classify biosignature potential
        if (bioScore >= 50) hab.setBiosignaturePotential("STRONG");
        else if (bioScore >= 30) hab.setBiosignaturePotential("HIGH");
        else if (bioScore >= 15) hab.setBiosignaturePotential("MODERATE");
        else if (bioScore >= 5) hab.setBiosignaturePotential("LOW");
        else if (hasSubsurfaceWater) hab.setBiosignaturePotential("SPECULATIVE");
        else hab.setBiosignaturePotential("NONE");

        hab.setBiosignatureIndicators(indicators.isEmpty() ? null : String.join("; ", indicators));
        hab.setEnergySourcesForLife(energySources.isEmpty() ? null : String.join("; ", energySources));

        // Life complexity potential
        if (bioScore >= 50 && hasLiquidWater && Boolean.TRUE.equals(hab.getHasCarbonCycle())) {
            hab.setLifeComplexityPotential("COMPLEX_MULTICELLULAR");
        } else if (bioScore >= 30 && (hasLiquidWater || hasSubsurfaceWater)) {
            hab.setLifeComplexityPotential("SIMPLE_MULTICELLULAR");
        } else if (bioScore >= 10) {
            hab.setLifeComplexityPotential("MICROBIAL_ONLY");
        } else if (hasSubsurfaceWater) {
            hab.setLifeComplexityPotential("MICROBIAL_ONLY");
        } else {
            hab.setLifeComplexityPotential("NONE");
        }
    }

    // ================================================================
    // FINAL CLASSIFICATION
    // ================================================================

    private void classifyHabitability(PlanetaryHabitability hab, Planet planet, Star parentStar) {
        double score = 0;
        String limitingFactor = null;

        // ESI contribution (0-25 points)
        double esi = hab.getEsiTotal() != null ? hab.getEsiTotal() : 0;
        score += esi * 25;

        // Breathable atmosphere (0-20 points)
        if (Boolean.TRUE.equals(hab.getIsBreathable())) {
            score += 20;
        } else {
            double pressure = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 0;
            if (pressure > 0.5 && pressure < 4.0) score += 8;
            else if (pressure > 0.1) score += 3;
            if (limitingFactor == null && hab.getBreathabilityIssues() != null) {
                limitingFactor = hab.getBreathabilityIssues();
            }
        }

        // Liquid water (0-20 points)
        if (Boolean.TRUE.equals(hab.getSurfaceLiquidWaterPossible())) {
            Double liquidPercent = planet.getLiquidWaterCoveragePercent();
            if (liquidPercent != null && liquidPercent > 10) score += 20;
            else if (liquidPercent != null && liquidPercent > 1) score += 12;
            else score += 5;
        } else if (Boolean.TRUE.equals(hab.getSubsurfaceOceanPossible())) {
            score += 5;
        }
        if (score < 30 && limitingFactor == null) {
            Double temp = planet.getSurfaceTemp();
            if (temp != null) {
                if (temp < 273) limitingFactor = String.format("Surface temperature %.0fK — below freezing", temp);
                else if (temp > 373) limitingFactor = String.format("Surface temperature %.0fK — too hot for liquid water", temp);
            }
        }

        // Magnetic protection (0-10 points)
        if (Boolean.TRUE.equals(hab.getMagneticProtectionAdequate())) {
            score += 10;
        } else if (planet.getMagneticField() != null) {
            PlanetaryMagneticField.ProtectionLevel level = planet.getMagneticField().getProtectionLevel();
            if (level == PlanetaryMagneticField.ProtectionLevel.MODERATE) score += 5;
            if (limitingFactor == null && (level == PlanetaryMagneticField.ProtectionLevel.NONE
                    || level == PlanetaryMagneticField.ProtectionLevel.MINIMAL)) {
                limitingFactor = "Insufficient magnetic field protection — atmosphere being stripped";
            }
        }

        // Geological activity (0-10 points)
        if (Boolean.TRUE.equals(hab.getHasCarbonCycle())) score += 10;
        else if (Boolean.TRUE.equals(planet.getHasVolcanicActivity())) score += 4;

        // Stellar environment (0-10 points)
        if (hab.getStellarLifetimeRemainingMy() != null && hab.getStellarLifetimeRemainingMy() > 1000) score += 5;
        if (hab.getOrbitalStabilityScore() != null && hab.getOrbitalStabilityScore() > 0.8) score += 3;
        if ("NONE".equals(hab.getFlareExposureRisk()) || "LOW".equals(hab.getFlareExposureRisk())) score += 2;
        else if ("EXTREME".equals(hab.getFlareExposureRisk()) && limitingFactor == null) {
            limitingFactor = "Extreme stellar flare exposure";
        }

        // UV/Radiation penalty
        String uvLevel = hab.getUvHazardLevel();
        if ("STERILIZING".equals(uvLevel)) { score -= 15; if (limitingFactor == null) limitingFactor = "Sterilizing UV radiation"; }
        else if ("EXTREME".equals(uvLevel)) score -= 8;
        else if ("HIGH".equals(uvLevel)) score -= 3;

        score = Math.max(0, Math.min(100, score));
        hab.setHabitabilityScore(score);

        // Classification
        HabitabilityClass hClass;
        if (score >= 80 && Boolean.TRUE.equals(hab.getIsBreathable()) && Boolean.TRUE.equals(hab.getSurfaceLiquidWaterPossible())) {
            hClass = HabitabilityClass.EARTH_ANALOG;
        } else if (score >= 60 && Boolean.TRUE.equals(hab.getSurfaceLiquidWaterPossible())) {
            hClass = HabitabilityClass.HABITABLE_MARGINAL;
        } else if (score >= 40 && (Boolean.TRUE.equals(hab.getSurfaceLiquidWaterPossible()) || "MODERATE".equals(hab.getBiosignaturePotential()) || "HIGH".equals(hab.getBiosignaturePotential()))) {
            hClass = HabitabilityClass.BIOSPHERE_POSSIBLE;
        } else if (Boolean.TRUE.equals(hab.getSubsurfaceOceanPossible())) {
            hClass = HabitabilityClass.SUBSURFACE_HABITABLE;
        } else if (isInHabitableZone(planet) && "STERILIZING".equals(uvLevel)) {
            hClass = HabitabilityClass.STERILIZED;
        } else if (isInHabitableZone(planet) && esi > 0.5) {
            hClass = score >= 30 ? HabitabilityClass.TERRAFORMABLE_EASY : HabitabilityClass.TERRAFORMABLE_HARD;
        } else if (esi > 0.3) {
            hClass = HabitabilityClass.TERRAFORMABLE_HARD;
        } else if (hasMiningValue(planet)) {
            hClass = HabitabilityClass.RESOURCE_WORLD;
        } else if (planet.getSurfaceTemp() != null && (planet.getSurfaceTemp() > 500 || planet.getSurfaceTemp() < 100)) {
            hClass = HabitabilityClass.EXTREME_ENVIRONMENT;
        } else {
            hClass = HabitabilityClass.INHOSPITABLE;
        }
        hab.setHabitabilityClass(hClass);

        // Terraforming potential
        TerraformingPotential terraform;
        if (hClass == HabitabilityClass.EARTH_ANALOG) {
            terraform = TerraformingPotential.UNNECESSARY;
        } else if (hClass == HabitabilityClass.HABITABLE_MARGINAL) {
            terraform = TerraformingPotential.UNNECESSARY;
        } else if (hClass == HabitabilityClass.TERRAFORMABLE_EASY) {
            terraform = TerraformingPotential.FEASIBLE;
        } else if (hClass == HabitabilityClass.TERRAFORMABLE_HARD) {
            terraform = TerraformingPotential.DIFFICULT;
        } else if (hClass == HabitabilityClass.BIOSPHERE_POSSIBLE || hClass == HabitabilityClass.SUBSURFACE_HABITABLE) {
            terraform = TerraformingPotential.MODERATE;
        } else if (hClass == HabitabilityClass.STERILIZED) {
            terraform = TerraformingPotential.DIFFICULT;
        } else if (hClass == HabitabilityClass.EXTREME_ENVIRONMENT) {
            terraform = TerraformingPotential.EXTREMELY_DIFFICULT;
        } else {
            terraform = TerraformingPotential.NONE;
        }
        hab.setTerraformingPotential(terraform);

        // Terraforming challenges
        List<String> challenges = new ArrayList<>();
        if (!Boolean.TRUE.equals(hab.getMagneticProtectionAdequate())) challenges.add("No magnetic field protection");
        if (!"NONE".equals(hab.getToxicGasHazard()) && !"LOW".equals(hab.getToxicGasHazard())) challenges.add("Toxic atmosphere");
        String waterInv = planet.getWaterInventory();
        if ("NONE".equals(waterInv) || "TRACE".equals(waterInv)) challenges.add("Insufficient water");
        if (planet.getSurfaceTemp() != null && planet.getSurfaceTemp() < 200) challenges.add("Extreme cold");
        if (planet.getSurfaceTemp() != null && planet.getSurfaceTemp() > 400) challenges.add("Extreme heat");
        if ("EXTREME".equals(hab.getFlareExposureRisk())) challenges.add("Hostile stellar environment");
        if (planet.getSurfacePressure() != null && planet.getSurfacePressure() < 0.01) challenges.add("Near-vacuum surface pressure");
        hab.setTerraformingChallenges(challenges.isEmpty() ? null : String.join("; ", challenges));

        // Colonization suitability
        ColonizationSuitability colony;
        if (Boolean.TRUE.equals(hab.getIsBreathable()) && score >= 70
                && !"HIGH".equals(uvLevel) && !"EXTREME".equals(uvLevel) && !"STERILIZING".equals(uvLevel)) {
            colony = ColonizationSuitability.SHIRT_SLEEVE;
        } else if (score >= 40 && planet.getSurfacePressure() != null && planet.getSurfacePressure() > 0.3) {
            colony = ColonizationSuitability.ASSISTED;
        } else if (score >= 15 || hClass == HabitabilityClass.RESOURCE_WORLD || hClass == HabitabilityClass.SUBSURFACE_HABITABLE) {
            colony = ColonizationSuitability.DOME_ONLY;
        } else {
            colony = ColonizationSuitability.UNINHABITABLE;
        }
        hab.setColonizationSuitability(colony);

        // Set limiting factor
        if (limitingFactor == null && score < 50) {
            limitingFactor = determineLimitingFactor(hab, planet);
        }
        hab.setLimitingFactor(limitingFactor);
    }

    private void assessGasGiant(PlanetaryHabitability hab, Planet planet, Star parentStar) {
        hab.setEsiTotal(0.0);
        hab.setEsiInterior(0.0);
        hab.setEsiSurface(0.0);
        hab.setIsBreathable(false);
        hab.setBreathabilityIssues("No solid surface; crushing atmospheric pressure");
        hab.setSurfaceLiquidWaterPossible(false);
        hab.setWaterPhaseAtSurface("NONE");
        hab.setHabitabilityClass(HabitabilityClass.INHOSPITABLE);
        hab.setHabitabilityScore(0.0);
        hab.setTerraformingPotential(TerraformingPotential.NONE);
        hab.setColonizationSuitability(ColonizationSuitability.UNINHABITABLE);
        hab.setLimitingFactor("No solid surface — gas giant");

        if (parentStar != null) {
            hab.setStellarLifetimeRemainingMy(parentStar.getEstimatedRemainingMsMy());
        }
    }

    // ================================================================
    // HELPER METHODS
    // ================================================================

    private double parseGasPercentage(String composition, String gasFormula) {
        if (composition == null || composition.isEmpty() || "None".equals(composition)) return 0.0;
        String regex = "(?:^|, )" + Pattern.quote(gasFormula) + "\\s+([\\d.]+)%";
        Matcher matcher = Pattern.compile(regex).matcher(composition);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private double getSpectralUvFactor(Star star) {
        String spectral = star.getSpectralType();
        if (spectral == null) return 1.0;
        return switch (spectral) {
            case "O" -> 100.0;
            case "B" -> 30.0;
            case "A" -> 5.0;
            case "F" -> 2.0;
            case "G" -> 1.0;
            case "K" -> 0.4;
            case "M" -> 0.1;
            default -> 0.01; // Brown dwarfs etc.
        };
    }

    private double getAtmosphericUvAttenuation(Planet planet) {
        String atmClass = planet.getAtmosphereClassification();
        if (atmClass == null || "NONE".equals(atmClass)) return 1.0; // No attenuation
        double pressure = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 0;
        // Thicker atmosphere = more UV absorbed
        double pressureFactor = Math.max(0.01, 1.0 / (1.0 + pressure * 2.0));
        // Earth-like with ozone: strong UV filter
        if ("EARTH_LIKE".equals(atmClass)) pressureFactor *= 0.01;
        return pressureFactor;
    }

    private double estimateGreenhouseWarming(Planet planet) {
        String atmClass = planet.getAtmosphereClassification();
        double pressure = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 0;
        if (atmClass == null || "NONE".equals(atmClass) || pressure < 0.001) return 0;

        String composition = planet.getAtmosphereComposition() != null ? planet.getAtmosphereComposition() : "";
        double co2Pct = parseGasPercentage(composition, "CO2");
        double ch4Pct = parseGasPercentage(composition, "CH4");
        double h2oPct = parseGasPercentage(composition, "H2O");

        // Simple greenhouse model
        double greenhouse = 0;
        greenhouse += co2Pct * 0.5 * Math.log1p(pressure); // CO2 contribution
        greenhouse += ch4Pct * 25.0; // CH4 is 25x more potent than CO2
        greenhouse += h2oPct * 1.5; // Water vapor feedback
        greenhouse += pressure * 5.0; // Pressure broadening

        // Dense CO2 atmospheres (Venus-like)
        if ("DENSE_CO2".equals(atmClass)) greenhouse = Math.max(greenhouse, 400.0);

        return Math.min(600.0, greenhouse); // Cap at Venus-like levels
    }

    private double calculateAtmosphericRetention(Planet planet) {
        double escapeVel = planet.getEscapeVelocity() != null ? planet.getEscapeVelocity() : 5.0;
        PlanetaryMagneticField magField = planet.getMagneticField();

        // Higher escape velocity = better retention
        double score = Math.min(1.0, escapeVel / 15.0);

        // Magnetic field helps retention
        if (magField != null && magField.getAtmosphericLossRateFactor() != null) {
            double lossFactor = magField.getAtmosphericLossRateFactor();
            if (lossFactor > 5.0) score *= 0.3;
            else if (lossFactor > 2.0) score *= 0.6;
            else if (lossFactor < 0.5) score *= 1.2;
        }

        return clamp01(score);
    }

    private boolean isInHabitableZone(Planet planet) {
        return "habitable".equals(planet.getHabitableZonePosition());
    }

    private boolean isGaseousBody(String planetType) {
        return planetType.contains("gas giant") || planetType.contains("ice giant")
                || planetType.contains("hot jupiter") || planetType.contains("super-jupiter")
                || planetType.contains("mini-neptune") || planetType.contains("sub-neptune")
                || planetType.contains("puffy");
    }

    private boolean hasMiningValue(Planet planet) {
        String composition = planet.getCompositionClassification();
        return "IRON_RICH".equals(composition) || "CARBON_RICH".equals(composition)
                || "SILICATE_RICH".equals(composition);
    }

    private String determineLimitingFactor(PlanetaryHabitability hab, Planet planet) {
        Double temp = planet.getSurfaceTemp();
        if (temp != null && temp < 200) return String.format("Extreme cold (%.0fK)", temp);
        if (temp != null && temp > 500) return String.format("Extreme heat (%.0fK)", temp);
        if ("STERILIZING".equals(hab.getUvHazardLevel())) return "Sterilizing radiation environment";
        if (planet.getSurfacePressure() != null && planet.getSurfacePressure() < 0.006)
            return "No atmosphere — below water triple point pressure";
        String waterInv = planet.getWaterInventory();
        if ("NONE".equals(waterInv)) return "No water in any form";
        if (!Boolean.TRUE.equals(hab.getMagneticProtectionAdequate()))
            return "Insufficient magnetic protection";
        return "Multiple limiting factors";
    }

    private double clamp01(double value) {
        return Math.max(0, Math.min(1, value));
    }
}
