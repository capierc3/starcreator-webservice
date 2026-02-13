package com.brickroad.starcreator_webservice.utils.planets;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.utils.CelestialBodyUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WeatherNarrativeGenerator {

    // ================================================================
    // MAIN ENTRY POINT
    // ================================================================

    public void generate(PlanetaryWeather weather, Planet planet, Star parentStar) {
        String atmClass = planet.getAtmosphereClassification();
        double surfaceTemp = planet.getSurfaceTemp() != null ? planet.getSurfaceTemp() : 250.0;
        double pressureAtm = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 1.0;

        // Generate weather hazards
        List<WeatherHazard> hazards = generateHazards(weather, planet);
        weather.setWeatherHazards(hazards);

        // Outdoor exposure rating
        calculateOutdoorExposure(weather, planet);

        // Weather severity
        calculateWeatherSeverity(weather, hazards);

        // Weather summary
        generateWeatherSummary(weather, planet, parentStar);
    }

    // ================================================================
    // WEATHER HAZARDS
    // ================================================================

    private List<WeatherHazard> generateHazards(PlanetaryWeather weather, Planet planet) {
        List<WeatherHazard> hazards = new ArrayList<>();
        String atmClass = planet.getAtmosphereClassification();
        double surfaceTemp = planet.getSurfaceTemp() != null ? planet.getSurfaceTemp() : 250.0;
        double pressureAtm = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 1.0;
        double meanWindMs = weather.getMeanSurfaceWindSpeedMs() != null ? weather.getMeanSurfaceWindSpeedMs() : 7.0;
        double maxGustMs = weather.getMaxGustSpeedMs() != null ? weather.getMaxGustSpeedMs() : 20.0;

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
        Double dayNightRange = weather.getDayNightTempRangeK();
        if (dayNightRange != null && dayNightRange > 80) {
            addHazard(hazards, "Extreme Diurnal Temperature Swing", "THERMAL",
                    dayNightRange > 150 ? "EXTREME" : "HIGH", "CONTINUOUS",
                    String.format("%.0fK temperature swing between day and night stresses equipment and personnel",
                            dayNightRange));
        }

        // Wind hazards
        if (meanWindMs > 30) {
            addHazard(hazards, "Extreme Winds", "WIND",
                    meanWindMs > 60 ? "LETHAL" : "EXTREME", "CONTINUOUS",
                    String.format("Sustained winds of %.0f m/s make surface operations dangerous", meanWindMs));
        } else if (maxGustMs > 50) {
            addHazard(hazards, "Dangerous Gusts", "WIND", "HIGH", "FREQUENT",
                    String.format("Wind gusts up to %.0f m/s pose significant risk to surface operations", maxGustMs));
        }

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

        // Chemical hazards by atmosphere type
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

        // Storm hazards (from Phase 6 data)
        if (Boolean.TRUE.equals(weather.getHasDustStorms()) && Boolean.TRUE.equals(weather.getDustStormsCanBeGlobal())) {
            addHazard(hazards, "Global Dust Storms", "STORM", "HIGH", "OCCASIONAL",
                    "Planet-encircling dust storms block solar power and reduce visibility for weeks");
        }

        // Lightning hazard
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

        return hazards;
    }

    private void addHazard(List<WeatherHazard> hazards, String name, String type,
                            String severity, String frequency, String description) {
        WeatherHazard hazard = new WeatherHazard();
        hazard.setHazardName(name);
        hazard.setHazardType(type);
        hazard.setSeverity(severity);
        hazard.setFrequency(frequency);
        hazard.setDescription(description);
        hazards.add(hazard);
    }

    // ================================================================
    // OUTDOOR EXPOSURE RATING
    // ================================================================

    private void calculateOutdoorExposure(PlanetaryWeather weather, Planet planet) {
        String atmClass = planet.getAtmosphereClassification();
        double surfaceTemp = planet.getSurfaceTemp() != null ? planet.getSurfaceTemp() : 250.0;
        double pressureAtm = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 1.0;

        PlanetaryHabitability hab = planet.getHabitability();
        boolean breathable = hab != null && Boolean.TRUE.equals(hab.getIsBreathable());

        // Lethal conditions: immediate death
        if (pressureAtm > 100 || pressureAtm < 0.006 || surfaceTemp > 500 || surfaceTemp < 100) {
            weather.setOutdoorExposureRating("LETHAL_SECONDS");
            weather.setSurvivalTimeDescription("Instantaneous death from " + getLethalCause(surfaceTemp, pressureAtm));
            return;
        }

        // Check for toxic/corrosive atmospheres
        if ("VENUS_LIKE".equals(atmClass) || "CORROSIVE".equals(atmClass) || "EXOTIC".equals(atmClass)
                || "AMMONIA".equals(atmClass)) {
            weather.setOutdoorExposureRating("LETHAL_SECONDS");
            weather.setSurvivalTimeDescription("Immediately lethal atmospheric chemistry");
            return;
        }

        if ("VOLCANIC".equals(atmClass) || "REDUCING".equals(atmClass)) {
            weather.setOutdoorExposureRating("LETHAL_MINUTES");
            weather.setSurvivalTimeDescription("Toxic atmosphere causes death within minutes without protection");
            return;
        }

        // Extreme but survivable for brief periods
        if (surfaceTemp > 350 || surfaceTemp < 150 || pressureAtm > 50 || pressureAtm < 0.01) {
            weather.setOutdoorExposureRating("LETHAL_MINUTES");
            weather.setSurvivalTimeDescription("Extreme conditions cause death within minutes");
            return;
        }

        // Survivable with equipment
        if (surfaceTemp > 320 || surfaceTemp < 200 || pressureAtm > 5 || pressureAtm < 0.3 || !breathable) {
            weather.setOutdoorExposureRating("ASSISTED");
            weather.setSurvivalTimeDescription("Survivable with supplemental life support and thermal protection");
            return;
        }

        // Shirt-sleeve: breathable, comfortable temperature, reasonable pressure
        if (breathable && surfaceTemp > 240 && surfaceTemp < 310 && pressureAtm > 0.5 && pressureAtm < 3.0) {
            weather.setOutdoorExposureRating("SHIRT_SLEEVE");
            weather.setSurvivalTimeDescription("Comfortable outdoors with appropriate clothing");
            return;
        }

        // Default to assisted
        weather.setOutdoorExposureRating("ASSISTED");
        weather.setSurvivalTimeDescription("Survivable with appropriate protective equipment");
    }

    private String getLethalCause(double temp, double pressure) {
        if (pressure < 0.006) return "explosive decompression";
        if (pressure > 100) return "crushing atmospheric pressure";
        if (temp > 500) return "extreme heat";
        if (temp < 100) return "extreme cold";
        return "hostile conditions";
    }

    // ================================================================
    // WEATHER SEVERITY
    // ================================================================

    private void calculateWeatherSeverity(PlanetaryWeather weather, List<WeatherHazard> hazards) {
        if (hazards.isEmpty()) {
            weather.setWeatherSeverity("BENIGN");
            return;
        }

        boolean hasLethal = hazards.stream().anyMatch(h -> "LETHAL".equals(h.getSeverity()));
        boolean hasExtreme = hazards.stream().anyMatch(h -> "EXTREME".equals(h.getSeverity()));
        boolean hasHigh = hazards.stream().anyMatch(h -> "HIGH".equals(h.getSeverity()));
        long hazardCount = hazards.size();

        if (hasLethal && hazardCount > 3) {
            weather.setWeatherSeverity("APOCALYPTIC");
        } else if (hasLethal) {
            weather.setWeatherSeverity("EXTREME");
        } else if (hasExtreme && hazardCount > 2) {
            weather.setWeatherSeverity("SEVERE");
        } else if (hasExtreme || (hasHigh && hazardCount > 2)) {
            weather.setWeatherSeverity("MODERATE");
        } else if (hasHigh) {
            weather.setWeatherSeverity("MILD");
        } else {
            weather.setWeatherSeverity("BENIGN");
        }
    }

    // ================================================================
    // WEATHER SUMMARY
    // ================================================================

    private void generateWeatherSummary(PlanetaryWeather weather, Planet planet, Star parentStar) {
        StringBuilder summary = new StringBuilder();
        String atmClass = planet.getAtmosphereClassification();

        // Opening: atmosphere type characterization
        summary.append(atmosphereOpener(atmClass));

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
        String seasonDesc = weather.getSeasonDescription();
        if (seasonDesc != null && !seasonDesc.isEmpty()) {
            summary.append(". ").append(seasonDesc);
        }

        // Wind characterization
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

        // Cloud and precipitation
        Double cloudCoverage = weather.getCloudCoveragePercent();
        if (cloudCoverage != null) {
            if (cloudCoverage > 90) {
                summary.append(". Perpetual cloud cover blankets the planet");
            } else if (cloudCoverage > 60) {
                summary.append(". Clouds frequently cover the sky");
            } else if (cloudCoverage < 10) {
                summary.append(". Skies are predominantly clear");
            }
        }

        String precipType = weather.getPrimaryPrecipitationType();
        if (precipType != null && !"NONE".equals(precipType)) {
            String readable = precipType.replace('_', ' ').toLowerCase();
            String freq = weather.getPrecipitationFrequency();
            if (freq != null) {
                summary.append(". ").append(capitalize(freq.toLowerCase())).append(" ").append(readable);
            }
        }

        // Notable storm activity
        String stormFreq = weather.getStormFrequency();
        if ("CONSTANT".equals(stormFreq)) {
            summary.append(". Storm activity is constant and intense");
        } else if ("FREQUENT".equals(stormFreq)) {
            summary.append(". Storms occur frequently");
        }

        // Exposure rating as closing
        String exposure = weather.getOutdoorExposureRating();
        if (exposure != null) {
            summary.append(". Outdoor exposure rating: ").append(exposure.replace('_', ' ').toLowerCase());
        }

        summary.append(".");
        weather.setWeatherSummary(truncate(summary.toString(), 1000));
    }

    private String atmosphereOpener(String atmClass) {
        switch (atmClass) {
            case "EARTH_LIKE": return "An Earth-like atmosphere supports familiar weather patterns.";
            case "VENUS_LIKE": return "A crushing Venus-like atmosphere with opaque sulfuric acid clouds.";
            case "MARS_LIKE": return "A thin Mars-like CO2 atmosphere with minimal weather activity.";
            case "TITAN_LIKE": return "A dense nitrogen-methane atmosphere with exotic hydrocarbon weather.";
            case "JOVIAN": return "A deep hydrogen-helium atmosphere with powerful banded weather systems.";
            case "ICE_GIANT": return "A hydrogen-methane atmosphere with extreme wind speeds.";
            case "VOLCANIC": return "A toxic volcanic atmosphere dominated by sulfur compounds.";
            case "AMMONIA": return "An ammonia-rich atmosphere with toxic clouds and precipitation.";
            case "REDUCING": return "A primordial reducing atmosphere with no free oxygen.";
            case "CORROSIVE": return "A highly corrosive atmosphere hostile to all materials.";
            case "EXOTIC": return "An exotic atmosphere with extreme chemistry and temperatures.";
            default: return "An atmosphere with distinctive weather patterns.";
        }
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
