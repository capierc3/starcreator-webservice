package com.brickroad.starcreator_webservice.utils.planets;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.utils.CelestialBodyUtils;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StormCalculator {

    // ================================================================
    // MAIN ENTRY POINT
    // ================================================================

    public void calculate(PlanetaryClimate weather, Planet planet) {
        String atmClass = planet.getAtmosphereClassification();
        double surfaceTemp = planet.getSurfaceTemp() != null ? planet.getSurfaceTemp() : 250.0;
        double pressureAtm = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 1.0;
        double rotationHours = planet.getRotationPeriodHours() != null ? Math.abs(planet.getRotationPeriodHours()) : 24.0;
        double liquidWaterPercent = planet.getLiquidWaterCoveragePercent() != null ? planet.getLiquidWaterCoveragePercent() : 0.0;
        boolean tidallyLocked = Boolean.TRUE.equals(planet.getTidallyLocked());
        String erosionLevel = planet.getErosionLevel();
        String convectionLevel = planet.getAtmosphericConvectionLevel();

        // Wind data from Phase 4
        double meanWindMs = weather.getMeanSurfaceWindSpeedMs() != null ? weather.getMeanSurfaceWindSpeedMs() : 7.0;
        double maxGustMs = weather.getMaxGustSpeedMs() != null ? weather.getMaxGustSpeedMs() : 20.0;

        // Cloud data from Phase 5
        double cloudCoverage = weather.getCloudCoveragePercent() != null ? weather.getCloudCoveragePercent() : 30.0;
        boolean hasPrecip = Boolean.TRUE.equals(weather.getHasPrecipitation());

        List<ExtremeClimateEvent> events = new ArrayList<>();

        if (CelestialBodyUtils.isGasGiantAtmosphere(atmClass)) {
            calculateGasGiantStorms(weather, events, atmClass, rotationHours, planet);
        } else {
            calculateRockyStorms(weather, events, atmClass, surfaceTemp, pressureAtm,
                    rotationHours, liquidWaterPercent, tidallyLocked, meanWindMs,
                    cloudCoverage, hasPrecip, convectionLevel);
        }

        // Dust storms (rocky worlds with thin atmospheres and surface material)
        calculateDustStorms(weather, events, atmClass, pressureAtm, surfaceTemp, erosionLevel);

        // Lightning assessment
        calculateLightning(weather, events, atmClass, pressureAtm, cloudCoverage, hasPrecip, surfaceTemp);

        // Special extreme weather for specific atmosphere types
        calculateSpecialExtremeWeather(events, atmClass, surfaceTemp, pressureAtm,
                liquidWaterPercent, rotationHours, tidallyLocked);

        weather.setExtremeClimateEvents(events);
    }

    // ================================================================
    // ROCKY WORLD STORMS
    // ================================================================

    private void calculateRockyStorms(PlanetaryClimate weather, List<ExtremeClimateEvent> events,
                                       String atmClass, double surfaceTemp, double pressureAtm,
                                       double rotationHours, double liquidWaterPercent,
                                       boolean tidallyLocked, double meanWindMs,
                                       double cloudCoverage, boolean hasPrecip, String convectionLevel) {

        // Tropical cyclone potential:
        // Requires: Coriolis force (not tidally locked, rotation < ~200h) + warm liquid surface (>300K)
        boolean hasCoriolis = !tidallyLocked && rotationHours < 200;
        boolean warmOcean = liquidWaterPercent > 10 && surfaceTemp > 295;

        if (hasCoriolis && warmOcean) {
            // Cyclone-capable world
            double cycloneIntensity = Math.sqrt(liquidWaterPercent / 100.0)
                    * Math.sqrt((surfaceTemp - 295) / 25.0)
                    * (24.0 / Math.max(5.0, rotationHours));
            cycloneIntensity = Math.max(0.1, Math.min(3.0, cycloneIntensity));

            double stormWindMs = 40.0 * cycloneIntensity * RandomUtils.rollRange(0.8, 1.3);
            stormWindMs = Math.max(20.0, Math.min(200.0, stormWindMs));
            weather.setTypicalStormWindSpeedMs(round2(stormWindMs));

            if (cycloneIntensity > 0.8) {
                weather.setStormFrequency("FREQUENT");
            } else if (cycloneIntensity > 0.3) {
                weather.setStormFrequency("OCCASIONAL");
            } else {
                weather.setStormFrequency("RARE");
            }

            ExtremeClimateEvent cyclone = new ExtremeClimateEvent();
            cyclone.setEventName("Tropical Cyclones");
            cyclone.setEventType("CYCLONE");
            cyclone.setSeverity(cycloneIntensity > 2.0 ? "EXTREME" : cycloneIntensity > 1.0 ? "HIGH" : "MODERATE");
            cyclone.setFrequency(weather.getStormFrequency());
            cyclone.setDescription(String.format(
                    "Tropical cyclones form over warm oceans with winds up to %.0f m/s", stormWindMs));
            events.add(cyclone);

            // Hypercanes: extreme cyclones on very warm ocean worlds
            if (surfaceTemp > 320 && liquidWaterPercent > 50 && rotationHours < 30) {
                ExtremeClimateEvent hypercane = new ExtremeClimateEvent();
                hypercane.setEventName("Hypercane Events");
                hypercane.setEventType("HYPERCANE");
                hypercane.setSeverity("CATASTROPHIC");
                hypercane.setFrequency("RARE");
                hypercane.setDescription(
                        "Extreme ocean heating drives hypercane formation with wind speeds exceeding 200 m/s and stratospheric cloud tops");
                events.add(hypercane);
            }
        } else {
            // Non-cyclone storm frequency: thick atmosphere + high cloud cover can still produce frequent storms
            // (e.g., Titan-like methane storms, Venus-like convective activity, dense CO2 worlds)
            if (hasPrecip && cloudCoverage > 70 && pressureAtm > 1.0) {
                weather.setStormFrequency("FREQUENT");
            } else if (hasPrecip && cloudCoverage > 40) {
                weather.setStormFrequency("OCCASIONAL");
            } else {
                weather.setStormFrequency("RARE");
            }
            weather.setTypicalStormWindSpeedMs(round2(maxWindForNonCyclone(meanWindMs, pressureAtm)));
        }

        // Convective storms (thunderstorms) — require thick atmosphere and temperature gradients
        if (pressureAtm > 0.3 && cloudCoverage > 20 && hasPrecip) {
            String convSeverity = "MODERATE".equals(convectionLevel) || "HIGH".equals(convectionLevel)
                    ? "HIGH" : "MODERATE";
            ExtremeClimateEvent convective = new ExtremeClimateEvent();
            convective.setEventName("Convective Storms");
            convective.setEventType("SUPERSTORM");
            convective.setSeverity(convSeverity);
            convective.setFrequency(cloudCoverage > 60 ? "FREQUENT" : "OCCASIONAL");
            convective.setDescription("Convective updrafts drive intense storm cells with heavy precipitation");
            events.add(convective);
        }
    }

    private double maxWindForNonCyclone(double meanWindMs, double pressureAtm) {
        double stormWind = meanWindMs * RandomUtils.rollRange(2.0, 4.0);
        if (pressureAtm < 0.1) stormWind *= 1.5; // Thin atmosphere → higher velocity gusts
        return Math.max(10.0, Math.min(100.0, stormWind));
    }

    // ================================================================
    // GAS GIANT STORMS
    // ================================================================

    private void calculateGasGiantStorms(PlanetaryClimate weather, List<ExtremeClimateEvent> events,
                                          String atmClass, double rotationHours, Planet planet) {
        // Gas giants always have intense storm activity
        weather.setStormFrequency("CONSTANT");

        double jetSpeed = weather.getJetStreamSpeedMs() != null ? weather.getJetStreamSpeedMs() : 150.0;
        double stormWind = jetSpeed * RandomUtils.rollRange(0.8, 1.5);
        weather.setTypicalStormWindSpeedMs(round2(Math.min(800.0, stormWind)));

        // Great storms / persistent vortices
        boolean hasGreatStorm = Boolean.TRUE.equals(planet.getHasGreatStorm());
        Integer numberOfStorms = planet.getNumberOfMajorStorms();
        int stormCount = numberOfStorms != null ? numberOfStorms : (hasGreatStorm ? 1 : 0);

        if (hasGreatStorm || stormCount > 0) {
            for (int i = 0; i < Math.max(1, stormCount); i++) {
                ExtremeClimateEvent greatStorm = new ExtremeClimateEvent();
                double diameterFactor = RandomUtils.rollRange(0.05, 0.3); // Fraction of planet diameter
                double vortexWindMs = jetSpeed * RandomUtils.rollRange(1.0, 2.5);
                double ageYears = Math.pow(10, RandomUtils.rollRange(1.0, 3.0)); // 10 to 1000 years

                String stormName;
                if ("ICE_GIANT".equals(atmClass)) {
                    stormName = i == 0 ? "Great Dark Spot" : "Dark Vortex " + (i + 1);
                } else {
                    stormName = i == 0 ? "Great Anticyclonic Vortex" : "Major Storm System " + (i + 1);
                }

                greatStorm.setEventName(stormName);
                greatStorm.setEventType("GREAT_VORTEX");
                greatStorm.setSeverity("EXTREME");
                greatStorm.setFrequency("CONSTANT");
                greatStorm.setDescription(String.format(
                        "Persistent anticyclonic vortex spanning %.0f%% of the planet's diameter " +
                        "with internal wind speeds of %.0f m/s, estimated age %.0f years",
                        diameterFactor * 100, vortexWindMs, ageYears));
                events.add(greatStorm);
            }
        }

        // Band-boundary storms — shear-driven between alternating jet streams
        ExtremeClimateEvent bandStorms = new ExtremeClimateEvent();
        bandStorms.setEventName("Band-Boundary Shear Storms");
        bandStorms.setEventType("SUPERSTORM");
        bandStorms.setSeverity("HIGH");
        bandStorms.setFrequency("CONSTANT");
        bandStorms.setDescription("Turbulent storms form at boundaries between alternating wind bands " +
                "where jet streams shear against each other");
        events.add(bandStorms);

        // Massive convective plumes (eruption-like upwellings)
        ExtremeClimateEvent plumes = new ExtremeClimateEvent();
        plumes.setEventName("Convective Eruption Plumes");
        plumes.setEventType("SUPERSTORM");
        plumes.setSeverity("EXTREME");
        plumes.setFrequency("OCCASIONAL");
        plumes.setDescription("Massive convective upwellings punch through cloud decks, " +
                "disrupting banding patterns for weeks before dissipating");
        events.add(plumes);
    }

    // ================================================================
    // DUST STORMS
    // ================================================================

    private void calculateDustStorms(PlanetaryClimate weather, List<ExtremeClimateEvent> events,
                                      String atmClass, double pressureAtm, double surfaceTemp,
                                      String erosionLevel) {
        // Dust storms require: thin atmosphere + rocky surface + temperature gradients
        // Mars-like is the archetype. Also possible on dry Earth-like worlds.
        boolean thinAtmosphere = pressureAtm > 0.001 && pressureAtm < 1.0;
        boolean hasErosion = erosionLevel != null && !"NONE".equals(erosionLevel);
        boolean isMarsSimilar = "MARS_LIKE".equals(atmClass);

        if (isMarsSimilar || (thinAtmosphere && hasErosion)) {
            weather.setHasDustStorms(true);

            // Can dust storms go global? Requires sufficient dust and thermal contrast
            boolean canBeGlobal = isMarsSimilar && pressureAtm > 0.003;
            weather.setDustStormsCanBeGlobal(canBeGlobal);

            ExtremeClimateEvent dustStorm = new ExtremeClimateEvent();
            if (canBeGlobal) {
                dustStorm.setEventName("Planet-Encircling Dust Storms");
                dustStorm.setEventType("GLOBAL_DUST");
                dustStorm.setSeverity("EXTREME");
                dustStorm.setFrequency("OCCASIONAL");
                dustStorm.setDescription(
                        "Regional dust storms can cascade into planet-encircling events " +
                        "that block sunlight for weeks, raising atmospheric temperature while cooling the surface");
            } else {
                dustStorm.setEventName("Regional Dust Storms");
                dustStorm.setEventType("DUST_STORM");
                dustStorm.setSeverity("MODERATE");
                dustStorm.setFrequency("FREQUENT");
                dustStorm.setDescription("Thermal winds lift fine surface particles into " +
                        "regional dust storms that can persist for days");
            }
            events.add(dustStorm);
        } else {
            weather.setHasDustStorms(false);
            weather.setDustStormsCanBeGlobal(false);
        }
    }

    // ================================================================
    // LIGHTNING
    // ================================================================

    private void calculateLightning(PlanetaryClimate weather, List<ExtremeClimateEvent> events,
                                     String atmClass, double pressureAtm, double cloudCoverage,
                                     boolean hasPrecip, double surfaceTemp) {

        boolean hasLightning = false;
        String lightningType = null;

        switch (atmClass) {
            case "JOVIAN":
            case "ICE_GIANT":
                // Gas giant lightning: massive scale, detected on Jupiter and Saturn
                hasLightning = true;
                lightningType = "CONVECTIVE_GIANT";
                ExtremeClimateEvent gasLightning = new ExtremeClimateEvent();
                gasLightning.setEventName("Giant-Scale Lightning");
                gasLightning.setEventType("LIGHTNING_STORM");
                gasLightning.setSeverity("HIGH");
                gasLightning.setFrequency("CONSTANT");
                gasLightning.setDescription("Lightning discharges thousands of times more powerful than terrestrial bolts, " +
                        "generated by massive convective cells within deep cloud layers");
                events.add(gasLightning);
                break;

            case "VOLCANIC":
                // Volcanic lightning in eruption plumes
                hasLightning = true;
                lightningType = "VOLCANIC";
                ExtremeClimateEvent volcLightning = new ExtremeClimateEvent();
                volcLightning.setEventName("Volcanic Lightning");
                volcLightning.setEventType("LIGHTNING_STORM");
                volcLightning.setSeverity("HIGH");
                volcLightning.setFrequency("FREQUENT");
                volcLightning.setDescription("Electrostatic discharges within volcanic eruption plumes " +
                        "and ash-laden atmosphere");
                events.add(volcLightning);
                break;

            case "MARS_LIKE":
                // Electrostatic dust discharges — not true lightning but analogous
                if (Boolean.TRUE.equals(weather.getHasDustStorms())) {
                    hasLightning = true;
                    lightningType = "DUST_ELECTROSTATIC";
                }
                break;

            default:
                // Rocky worlds with convective precipitation → conventional lightning
                if (pressureAtm > 0.3 && hasPrecip && cloudCoverage > 20) {
                    hasLightning = true;
                    lightningType = "CONVECTIVE";
                }
                break;
        }

        weather.setHasLightning(hasLightning);
        weather.setLightningType(lightningType);
    }

    // ================================================================
    // SPECIAL EXTREME WEATHER
    // ================================================================

    private void calculateSpecialExtremeWeather(List<ExtremeClimateEvent> events, String atmClass,
                                                 double surfaceTemp, double pressureAtm,
                                                 double liquidWaterPercent, double rotationHours,
                                                 boolean tidallyLocked) {

        // Methane monsoons (Titan-like)
        if ("TITAN_LIKE".equals(atmClass)) {
            ExtremeClimateEvent monsoon = new ExtremeClimateEvent();
            monsoon.setEventName("Methane Monsoons");
            monsoon.setEventType("MONSOON");
            monsoon.setSeverity("MODERATE");
            monsoon.setFrequency("SEASONAL");
            monsoon.setDescription("Seasonal methane rainstorms drench the surface, " +
                    "carving river channels and filling hydrocarbon lakes");
            events.add(monsoon);
        }

        // Sulfuric acid virga storms (Venus-like)
        if ("VENUS_LIKE".equals(atmClass)) {
            ExtremeClimateEvent virga = new ExtremeClimateEvent();
            virga.setEventName("Sulfuric Acid Virga");
            virga.setEventType("SUPERSTORM");
            virga.setSeverity("HIGH");
            virga.setFrequency("CONSTANT");
            virga.setDescription("Continuous sulfuric acid precipitation evaporates in the extreme heat " +
                    "before reaching the surface, creating perpetual virga curtains in the upper atmosphere");
            events.add(virga);
        }

        // Corrosive rain storms
        if ("CORROSIVE".equals(atmClass) && pressureAtm > 0.1) {
            ExtremeClimateEvent acidRain = new ExtremeClimateEvent();
            acidRain.setEventName("Corrosive Precipitation Events");
            acidRain.setEventType("SUPERSTORM");
            acidRain.setSeverity("EXTREME");
            acidRain.setFrequency("FREQUENT");
            acidRain.setDescription("Highly corrosive acid rain that dissolves exposed metals and " +
                    "degrades unprotected equipment rapidly");
            events.add(acidRain);
        }

        // Iron/silicate rain on exotic ultrahot worlds
        if ("EXOTIC".equals(atmClass) && surfaceTemp > 1800) {
            ExtremeClimateEvent metalRain = new ExtremeClimateEvent();
            metalRain.setEventName("Molten Metal Rain");
            metalRain.setEventType("SUPERSTORM");
            metalRain.setSeverity("CATASTROPHIC");
            metalRain.setFrequency("CONSTANT");
            metalRain.setDescription("Extreme day-night temperature contrast drives iron and silicate " +
                    "condensation on the nightside, raining molten metal and liquid rock");
            events.add(metalRain);
        }

        // Tidally locked terminator storms
        if (tidallyLocked && pressureAtm > 0.5) {
            ExtremeClimateEvent terminatorStorm = new ExtremeClimateEvent();
            terminatorStorm.setEventName("Terminator Convergence Storms");
            terminatorStorm.setEventType("SUPERSTORM");
            terminatorStorm.setSeverity("HIGH");
            terminatorStorm.setFrequency("CONSTANT");
            terminatorStorm.setDescription("Permanent convergence zone along the day-night terminator " +
                    "drives continuous storm activity where hot and cold air masses collide");
            events.add(terminatorStorm);
        }

        // Ammonia blizzards
        if ("AMMONIA".equals(atmClass) && surfaceTemp < 250) {
            ExtremeClimateEvent ammoniaBlizzard = new ExtremeClimateEvent();
            ammoniaBlizzard.setEventName("Ammonia Blizzards");
            ammoniaBlizzard.setEventType("SUPERSTORM");
            ammoniaBlizzard.setSeverity("HIGH");
            ammoniaBlizzard.setFrequency("FREQUENT");
            ammoniaBlizzard.setDescription("Dense ammonia snowfall driven by strong convection, " +
                    "reducing visibility to near-zero and coating surfaces in toxic ice");
            events.add(ammoniaBlizzard);
        }
    }

    // ================================================================
    // UTILITY
    // ================================================================

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
