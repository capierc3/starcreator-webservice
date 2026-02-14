package com.brickroad.starcreator_webservice.probabilityreport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonReportBuilder {

    private final ProbabilityCounts counts;
    private final PerformanceTimer timer;
    private final StarDataCollector starData;
    private final PlanetDataCollector planetData;
    private final MoonDataCollector moonData;
    private final RingDataCollector ringData;
    private final BeltDataCollector beltData;

    public JsonReportBuilder(ProbabilityCounts counts, PerformanceTimer timer,
                             StarDataCollector starData, PlanetDataCollector planetData,
                             MoonDataCollector moonData, RingDataCollector ringData,
                             BeltDataCollector beltData) {
        this.counts = counts;
        this.timer = timer;
        this.starData = starData;
        this.planetData = planetData;
        this.moonData = moonData;
        this.ringData = ringData;
        this.beltData = beltData;
    }

    public void saveReport(File targetFolder) throws IOException {
        Map<String, Object> report = buildReport();

        File file = new File(targetFolder, "system_report_" + counts.getSystemCount() + "_systems.json");
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(file, report);

        System.out.println("JSON report saved to: " + file.getAbsolutePath());
    }

    private Map<String, Object> buildReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("metadata", buildMetadata());
        report.put("summary", buildSummary());
        report.put("stars", buildStarData());
        report.put("planets", buildPlanetData());
        report.put("moons", buildMoonData());
        report.put("rings", buildRingData());
        report.put("belts", buildBeltData());
        report.put("weather", buildWeatherData());
        return report;
    }

    private Map<String, Object> buildMetadata() {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("systemCount", counts.getSystemCount());
        meta.put("generationTime", timer.getFinalTime());
        meta.put("averageTimePerSystemMs", round(timer.averageLap()));
        meta.put("totalTimeMs", timer.getTotalTime());
        meta.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return meta;
    }

    private Map<String, Object> buildSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("systems", counts.getSystemCount());
        summary.put("stars", counts.getStarCount());
        summary.put("planets", counts.getPlanetCount());
        summary.put("moons", counts.getMoonCount());
        summary.put("rings", counts.getRingCount());
        summary.put("belts", counts.getBeltCount());
        summary.put("asteroids", counts.getAsteroidCount());
        return summary;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Stars
    // ═══════════════════════════════════════════════════════════════

    private Map<String, Object> buildStarData() {
        Map<String, Object> stars = new LinkedHashMap<>();
        stars.put("amounts", starData.getStarAmounts());
        stars.put("types", starData.getStarTypes());
        stars.put("roles", starData.getStarRoles());
        stars.put("binaryConfigurations", starData.getBinaryConfigurations());

        // Per-type breakdown
        Map<String, Object> perType = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Map<String, Integer>>> entry : starData.getStarTypesData().entrySet()) {
            Map<String, Object> typeEntry = new LinkedHashMap<>();
            typeEntry.put("count", starData.getStarTypes().getOrDefault(entry.getKey(), 0));
            Map<String, Map<String, Integer>> data = entry.getValue();
            if (data.containsKey("activity")) typeEntry.put("activity", data.get("activity"));
            if (data.containsKey("flare class")) typeEntry.put("flareClass", data.get("flare class"));
            if (data.containsKey("spot %")) typeEntry.put("spotCoverage", data.get("spot %"));
            if (data.containsKey("xray Luminosity")) typeEntry.put("xrayLuminosity", data.get("xray Luminosity"));
            if (data.containsKey("evolutionary stage")) typeEntry.put("evolutionaryStage", data.get("evolutionary stage"));
            if (data.containsKey("planets per system")) typeEntry.put("planetsPerSystem", data.get("planets per system"));
            if (data.containsKey("hz inner AU")) typeEntry.put("hzInnerAU", data.get("hz inner AU"));
            if (data.containsKey("hz outer AU")) typeEntry.put("hzOuterAU", data.get("hz outer AU"));
            perType.put(entry.getKey(), typeEntry);
        }
        stars.put("perType", perType);
        return stars;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Planets
    // ═══════════════════════════════════════════════════════════════

    private Map<String, Object> buildPlanetData() {
        Map<String, Object> planets = new LinkedHashMap<>();
        planets.put("types", planetData.getPlanetTypes());
        planets.put("compositionClasses", planetData.getCompositionClasses());
        planets.put("surfaceTempBins", planetData.getSurfaceTempBins());
        planets.put("atmosphereClassifications", planetData.getAtmosphereClassifications());
        planets.put("habitableZonePositions", planetData.getHzPositions());
        planets.put("tidalLocking", planetData.getTidalLockCounts());
        planets.put("magneticProtectionLevels", planetData.getProtectionLevels());
        planets.put("magnetopauseBins", planetData.getMagnetopauseBins());
        planets.put("atmLossRateBins", planetData.getAtmLossRateBins());
        planets.put("auroralFrequencies", planetData.getAuroralFrequencies());
        planets.put("auroralIntensities", planetData.getAuroralIntensities());
        planets.put("beltIntensityInner", planetData.getBeltIntensityInner());
        planets.put("beltIntensityOuter", planetData.getBeltIntensityOuter());
        planets.put("waterInventories", planetData.getWaterInventories());
        planets.put("waterPhases", planetData.getWaterPhases());
        planets.put("habitabilityClasses", planetData.getHabitabilityClasses());
        planets.put("colonizationSuitabilities", planetData.getColonizationSuitabilities());
        planets.put("terraformingPotentials", planetData.getTerraformingPotentials());
        planets.put("biosignaturePotentials", planetData.getBiosignaturePotentials());
        planets.put("lifeComplexityPotentials", planetData.getLifeComplexityPotentials());
        planets.put("geologicalActivity", planetData.getGeologicalActivity());
        planets.put("tectonicLevels", planetData.getTectonicLevels());
        planets.put("volcanismTypes", planetData.getVolcanismTypes());

        // Summary stats
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalRockyPlanets", planetData.getTotalRockyPlanets());
        summary.put("planetsWithRings", planetData.getPlanetsWithRings());
        summary.put("planetsWithGeology", planetData.getPlanetsWithGeology());
        summary.put("planetsWithLiquidWater", planetData.getPlanetsWithLiquidWater());
        summary.put("planetsWithIce", planetData.getPlanetsWithIce());
        summary.put("planetsWithSubsurfaceWater", planetData.getPlanetsWithSubsurfaceWater());
        summary.put("breathablePlanets", planetData.getBreathablePlanets());
        summary.put("habAssessmentCount", planetData.getHabAssessmentCount());
        if (planetData.getPhysicalPropsCount() > 0) {
            summary.put("avgMass", round(planetData.getTotalMass() / planetData.getPhysicalPropsCount()));
            summary.put("avgRadius", round(planetData.getTotalRadius() / planetData.getPhysicalPropsCount()));
            summary.put("avgGravity", round(planetData.getTotalGravity() / planetData.getPhysicalPropsCount()));
        }
        if (planetData.getHabAssessmentCount() > 0) {
            summary.put("avgEsi", round(planetData.getEsiSum() / planetData.getHabAssessmentCount()));
            summary.put("avgHabScore", round(planetData.getHabScoreSum() / planetData.getHabAssessmentCount()));
        }
        planets.put("summary", summary);

        // Cross-reference data
        Map<String, Object> crossRef = new LinkedHashMap<>();
        // Activity vs atmosphere stripping
        Map<String, Object> actAtm = new LinkedHashMap<>();
        for (Map.Entry<String, int[]> e : planetData.getActivityVsAtmosphere().entrySet()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("totalRocky", e.getValue()[0]);
            entry.put("strippedNone", e.getValue()[1]);
            entry.put("stripRate", e.getValue()[0] > 0 ? round(e.getValue()[1] * 100.0 / e.getValue()[0]) : 0);
            actAtm.put(e.getKey(), entry);
        }
        crossRef.put("activityVsAtmosphere", actAtm);

        // Activity vs protection level
        Map<String, Object> actProt = new LinkedHashMap<>();
        for (Map.Entry<String, int[]> e : planetData.getActivityVsProtection().entrySet()) {
            int[] c = e.getValue();
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("total", c[0]);
            entry.put("NONE", c[1]);
            entry.put("MINIMAL", c[2]);
            entry.put("MODERATE", c[3]);
            entry.put("STRONG", c[4]);
            entry.put("EXCEPTIONAL", c[5]);
            actProt.put(e.getKey(), entry);
        }
        crossRef.put("activityVsProtection", actProt);

        // Distance vs magnetopause
        Map<String, Object> distMag = new LinkedHashMap<>();
        for (Map.Entry<String, double[]> e : planetData.getDistanceVsMagnetopause().entrySet()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("avgMagnetopause", round(e.getValue()[0] / e.getValue()[1]));
            entry.put("sampleCount", (int) e.getValue()[1]);
            distMag.put(e.getKey(), entry);
        }
        crossRef.put("distanceVsMagnetopause", distMag);
        planets.put("crossReference", crossRef);

        // Per-type breakdown (single-star / non-P-type systems)
        Map<String, Object> perType = new LinkedHashMap<>();
        for (Map.Entry<String, PlanetTypeBreakdown> entry : planetData.getPerTypeData().entrySet()) {
            perType.put(entry.getKey(), entry.getValue().toJson());
        }
        planets.put("perType", perType);

        // Per-type breakdown (P-type binary systems)
        Map<String, Object> perTypePType = new LinkedHashMap<>();
        for (Map.Entry<String, PlanetTypeBreakdown> entry : planetData.getPerTypeDataPType().entrySet()) {
            perTypePType.put(entry.getKey(), entry.getValue().toJson());
        }
        planets.put("perTypePType", perTypePType);

        // Per-type breakdown (trinary systems)
        Map<String, Object> perTypeTrinary = new LinkedHashMap<>();
        for (Map.Entry<String, PlanetTypeBreakdown> entry : planetData.getPerTypeDataTrinary().entrySet()) {
            perTypeTrinary.put(entry.getKey(), entry.getValue().toJson());
        }
        planets.put("perTypeTrinary", perTypeTrinary);

        return planets;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Moons
    // ═══════════════════════════════════════════════════════════════

    private Map<String, Object> buildMoonData() {
        Map<String, Object> moons = new LinkedHashMap<>();
        moons.put("types", moonData.getMoonTypes());
        moons.put("compositionTypes", moonData.getMoonCompositionTypes());
        moons.put("tidalHeatingLevels", moonData.getMoonTidalHeatingLevels());
        moons.put("moonsWithSubsurfaceOcean", moonData.getMoonsWithSubsurfaceOcean());
        moons.put("moonsAssessed", moonData.getMoonsAssessed());

        // Magnetic fields
        Map<String, Object> magField = new LinkedHashMap<>();
        magField.put("moonsWithMagField", moonData.getMoonsWithMagField());
        magField.put("dynamoTypes", moonData.getMoonDynamoTypes());
        magField.put("protectionLevels", moonData.getMoonProtectionLevels());
        moons.put("magneticFields", magField);

        // Water
        Map<String, Object> water = new LinkedHashMap<>();
        water.put("inventories", moonData.getMoonWaterInventories());
        water.put("moonsWithLiquidWater", moonData.getMoonsWithLiquidWater());
        water.put("moonsWithIce", moonData.getMoonsWithIce());
        water.put("moonsWithSubsurfaceWater", moonData.getMoonsWithSubsurfaceWater());
        moons.put("water", water);

        // Habitability
        Map<String, Object> hab = new LinkedHashMap<>();
        hab.put("moonHabCount", moonData.getMoonHabCount());
        if (moonData.getMoonHabCount() > 0) {
            hab.put("avgEsi", round(moonData.getMoonEsiSum() / moonData.getMoonHabCount()));
            hab.put("avgHabScore", round(moonData.getMoonHabScoreSum() / moonData.getMoonHabCount()));
        }
        hab.put("habitabilityClasses", moonData.getMoonHabitabilityClasses());
        hab.put("colonizationSuitabilities", moonData.getMoonColonizationSuitabilities());
        hab.put("biosignaturePotentials", moonData.getMoonBiosignaturePotentials());
        hab.put("lifeComplexity", moonData.getMoonLifeComplexity());
        hab.put("radiationBeltDose", moonData.getMoonRadiationBeltDose());
        hab.put("tidalContribution", moonData.getMoonTidalContribution());
        moons.put("habitability", hab);

        // Weather
        Map<String, Object> weather = new LinkedHashMap<>();
        weather.put("moonsWithWeather", moonData.getMoonsWithWeather());
        weather.put("moonsWithPrecipitation", moonData.getMoonsWithPrecipitation());
        weather.put("moonsWithLightning", moonData.getMoonsWithLightning());
        weather.put("moonsWithParentPlanetVisible", moonData.getMoonsWithParentPlanetVisible());
        weather.put("moonsWithPlanetaryEclipses", moonData.getMoonsWithPlanetaryEclipses());
        weather.put("moonExtremeEventTotal", moonData.getMoonExtremeEventTotal());
        weather.put("skyColors", moonData.getMoonWeatherSkyColor());
        weather.put("cloudCoverage", moonData.getMoonWeatherCloudClass());
        weather.put("windIntensity", moonData.getMoonWeatherWindIntensity());
        weather.put("severity", moonData.getMoonWeatherSeverity());
        weather.put("exposureRating", moonData.getMoonWeatherExposure());
        if (!moonData.getMoonWeatherTidalRangeBins().isEmpty()) {
            weather.put("tidalRangeBins", moonData.getMoonWeatherTidalRangeBins());
        }
        moons.put("weather", weather);

        return moons;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Rings & Belts
    // ═══════════════════════════════════════════════════════════════

    private Map<String, Object> buildRingData() {
        Map<String, Object> rings = new LinkedHashMap<>();
        rings.put("types", ringData.getRingTypes());
        return rings;
    }

    private Map<String, Object> buildBeltData() {
        Map<String, Object> belts = new LinkedHashMap<>();
        belts.put("types", beltData.getBeltTypes());
        belts.put("asteroidTypes", beltData.getAsteroidTypes());
        belts.put("dwarfPlanetsInBelts", counts.getTempCount());
        return belts;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Weather
    // ═══════════════════════════════════════════════════════════════

    private Map<String, Object> buildWeatherData() {
        Map<String, Object> weather = new LinkedHashMap<>();
        weather.put("all", buildWeatherBucket(planetData.getWeatherAll()));
        weather.put("surface", buildWeatherBucket(planetData.getWeatherSurface()));
        weather.put("gasIce", buildWeatherBucket(planetData.getWeatherGas()));
        return weather;
    }

    private Map<String, Object> buildWeatherBucket(PlanetDataCollector.WeatherBucket b) {
        Map<String, Object> bucket = new LinkedHashMap<>();
        bucket.put("count", b.getCount());
        if (b.getCount() == 0) return bucket;

        bucket.put("skyColors", b.getSkyColors());
        bucket.put("cloudCoverageClass", b.getCloudCoverageClass());
        bucket.put("windIntensity", b.getWindIntensity());
        bucket.put("stormFrequency", b.getStormFrequency());
        bucket.put("severity", b.getSeverity());
        bucket.put("exposureRating", b.getExposureRating());
        bucket.put("circulationPattern", b.getCirculationPattern());
        if (!b.getLightningType().isEmpty()) bucket.put("lightningType", b.getLightningType());
        if (!b.getTidalRangeBins().isEmpty()) bucket.put("tidalRangeBins", b.getTidalRangeBins());

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("withPrecipitation", b.getWithPrecipitation());
        stats.put("withDustStorms", b.getWithDustStorms());
        stats.put("withLightning", b.getWithLightning());
        stats.put("withSuperRotation", b.getWithSuperRotation());
        stats.put("withGreatDarkSpot", b.getWithGreatDarkSpot());
        stats.put("avgCloudCoverage", round(b.getTotalCloudCoverage() / b.getCount()));
        if (b.getWindSpeedCount() > 0) {
            stats.put("avgWindSpeed", round(b.getTotalWindSpeed() / b.getWindSpeedCount()));
        }
        stats.put("avgCloudLayers", round(b.getCloudLayerTotal() * 1.0 / b.getCount()));
        stats.put("avgPrecipTypes", round(b.getPrecipTypeTotal() * 1.0 / b.getCount()));
        stats.put("extremeEventTotal", b.getExtremeEventTotal());
        stats.put("eclipseTotal", b.getEclipseTotal());
        bucket.put("stats", stats);

        return bucket;
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
