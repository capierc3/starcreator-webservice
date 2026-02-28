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
    private final TrojanDataCollector trojanData;
    private final AsteroidDataCollector asteroidData;
    private final BeltDataCollector beltData;
    private final OrbitStabilityCollector stabilityData;

    public JsonReportBuilder(ProbabilityCounts counts, PerformanceTimer timer,
                             StarDataCollector starData, PlanetDataCollector planetData,
                             MoonDataCollector moonData, RingDataCollector ringData,
                             TrojanDataCollector trojanData, AsteroidDataCollector asteroidData,
                             BeltDataCollector beltData, OrbitStabilityCollector stabilityData) {
        this.counts = counts;
        this.timer = timer;
        this.starData = starData;
        this.planetData = planetData;
        this.moonData = moonData;
        this.ringData = ringData;
        this.trojanData = trojanData;
        this.asteroidData = asteroidData;
        this.beltData = beltData;
        this.stabilityData = stabilityData;
    }

    public void saveReport(File targetFolder) throws IOException {
        Map<String, Object> report = buildReport();

        File file = new File(targetFolder, "system_report_" + counts.getSystemCount() + "_systems.json");
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(file, report);

        System.out.println("JSON report saved to: " + file.getAbsolutePath());
    }

    public Map<String, Object> buildReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("metadata", buildMetadata());
        report.put("summary", buildSummary());
        report.put("stars", buildStarData());
        report.put("planets", buildPlanetData());
        report.put("moons", buildMoonData());
        report.put("rings", buildRingData());
        report.put("trojans", buildTrojanData());
        report.put("notableAsteroids", buildAsteroidData());
        report.put("belts", buildBeltData());
        report.put("climate", buildClimateData());
        report.put("orbitalStability", buildOrbitalStabilityData());
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
        summary.put("trojans", counts.getTrojanCount());
        summary.put("belts", counts.getBeltCount());
        summary.put("asteroids", counts.getAsteroidCount());
        summary.put("moonlets", counts.getMoonletCount());
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

            // Planet formations for this star type
            Map<String, double[]> formations = starData.getPlanetFormationsByStarType().get(entry.getKey());
            if (formations != null && !formations.isEmpty()) {
                int totalPlanets = formations.values().stream().mapToInt(v -> (int) v[0]).sum();
                Map<String, Object> formationsJson = new LinkedHashMap<>();
                formationsJson.put("totalPlanets", totalPlanets);
                Map<String, Object> planetTypes = new LinkedHashMap<>();
                formations.entrySet().stream()
                        .sorted((a, b) -> Integer.compare((int) b.getValue()[0], (int) a.getValue()[0]))
                        .forEach(fe -> {
                            Map<String, Object> ptEntry = new LinkedHashMap<>();
                            ptEntry.put("count", (int) fe.getValue()[0]);
                            ptEntry.put("percent", round(fe.getValue()[0] * 100.0 / Math.max(1, totalPlanets)));
                            double minAU = fe.getValue()[1];
                            double maxAU = fe.getValue()[2];
                            if (minAU >= 0) ptEntry.put("minDistanceAU", roundAU(minAU));
                            if (maxAU >= 0) ptEntry.put("maxDistanceAU", roundAU(maxAU));
                            planetTypes.put(fe.getKey(), ptEntry);
                        });
                formationsJson.put("planetTypes", planetTypes);
                typeEntry.put("planetFormations", formationsJson);
            }

            perType.put(entry.getKey(), typeEntry);
        }
        stars.put("perType", perType);

        // Companion star orbital element distributions
        if (!starData.getCompanionEccentricityBins().isEmpty()) {
            Map<String, Object> companionOrbits = new LinkedHashMap<>();
            companionOrbits.put("eccentricity", starData.getCompanionEccentricityBins());
            companionOrbits.put("inclination", starData.getCompanionInclinationBins());
            companionOrbits.put("separation", starData.getCompanionSeparationBins());
            companionOrbits.put("orbitalPeriod", starData.getCompanionOrbitalPeriodBins());
            stars.put("companionOrbits", companionOrbits);
        }

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
        summary.put("planetsWithTrojans", planetData.getPlanetsWithTrojans());
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
        moons.put("orbitDistanceBins", moonData.getOrbitDistanceBins());
        moons.put("eccentricityBins", moonData.getEccentricityBins());
        moons.put("geologicalActivity", moonData.getGeologicalActivity());
        moons.put("atmosphereClassifications", moonData.getAtmosphereClassifications());

        // Tidal heating by planet type
        Map<String, Object> tidalByPlanet = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Integer>> entry : moonData.getTidalHeatingByPlanetType().entrySet()) {
            tidalByPlanet.put(entry.getKey(), entry.getValue());
        }
        moons.put("tidalHeatingByPlanetType", tidalByPlanet);

        // Tidal heating by moon type
        Map<String, Object> tidalByMoon = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Integer>> entry : moonData.getTidalHeatingByMoonType().entrySet()) {
            tidalByMoon.put(entry.getKey(), entry.getValue());
        }
        moons.put("tidalHeatingByMoonType", tidalByMoon);

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

        // Climate
        Map<String, Object> climate = new LinkedHashMap<>();
        climate.put("moonsWithClimate", moonData.getMoonsWithClimate());
        climate.put("moonsWithPrecipitation", moonData.getMoonsWithPrecipitation());
        climate.put("moonsWithLightning", moonData.getMoonsWithLightning());
        climate.put("moonsWithParentPlanetVisible", moonData.getMoonsWithParentPlanetVisible());
        climate.put("moonsWithPlanetaryEclipses", moonData.getMoonsWithPlanetaryEclipses());
        climate.put("moonExtremeEventTotal", moonData.getMoonExtremeEventTotal());
        climate.put("skyColors", moonData.getMoonClimateSkyColor());
        climate.put("cloudCoverage", moonData.getMoonClimateCloudClass());
        climate.put("windIntensity", moonData.getMoonClimateWindIntensity());
        climate.put("severity", moonData.getMoonClimateSeverity());
        climate.put("exposureRating", moonData.getMoonClimateExposure());
        if (!moonData.getMoonClimateTidalRangeBins().isEmpty()) {
            climate.put("tidalRangeBins", moonData.getMoonClimateTidalRangeBins());
        }
        moons.put("climate", climate);

        return moons;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Rings & Belts
    // ═══════════════════════════════════════════════════════════════

    private Map<String, Object> buildRingData() {
        Map<String, Object> rings = new LinkedHashMap<>();
        rings.put("types", ringData.getRingTypes());

        // New distribution maps
        if (!ringData.getOpticalDepthBins().isEmpty()) rings.put("opticalDepthBins", ringData.getOpticalDepthBins());
        if (!ringData.getColorDistribution().isEmpty()) rings.put("colorDistribution", ringData.getColorDistribution());
        if (!ringData.getVisibilityDistribution().isEmpty()) rings.put("visibilityDistribution", ringData.getVisibilityDistribution());
        if (!ringData.getStabilityDistribution().isEmpty()) rings.put("stabilityDistribution", ringData.getStabilityDistribution());
        if (!ringData.getOriginTypes().isEmpty()) rings.put("originTypes", ringData.getOriginTypes());
        if (!ringData.getThicknessBins().isEmpty()) rings.put("thicknessBins", ringData.getThicknessBins());
        if (!ringData.getParticleSizeBins().isEmpty()) rings.put("particleSizeBins", ringData.getParticleSizeBins());
        if (!ringData.getAgeBins().isEmpty()) rings.put("ageBins", ringData.getAgeBins());
        if (!ringData.getParentPlanetTypes().isEmpty()) rings.put("parentPlanetTypes", ringData.getParentPlanetTypes());

        // Summary
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalRings", counts.getRingCount());
        summary.put("withShepherdMoons", ringData.getShepherdMoonCount());
        summary.put("withGaps", ringData.getRingsWithGaps());
        rings.put("summary", summary);

        // Per-type breakdown
        if (!ringData.getPerTypeData().isEmpty()) {
            Map<String, Object> perType = new LinkedHashMap<>();
            for (Map.Entry<String, RingDataCollector.RingTypeBreakdown> entry : ringData.getPerTypeData().entrySet()) {
                perType.put(entry.getKey(), entry.getValue().toJson());
            }
            rings.put("perType", perType);
        }

        return rings;
    }

    private Map<String, Object> buildTrojanData() {
        Map<String, Object> trojans = new LinkedHashMap<>();

        // Distribution maps
        if (!trojanData.getLagrangePointCounts().isEmpty()) trojans.put("lagrangePoints", trojanData.getLagrangePointCounts());
        if (!trojanData.getParentPlanetTypes().isEmpty()) trojans.put("parentPlanetTypes", trojanData.getParentPlanetTypes());
        if (!trojanData.getMassBins().isEmpty()) trojans.put("massBins", trojanData.getMassBins());
        if (!trojanData.getObjectCountBins().isEmpty()) trojans.put("objectCountBins", trojanData.getObjectCountBins());
        if (!trojanData.getLibrationAmplitudeBins().isEmpty()) trojans.put("librationAmplitudeBins", trojanData.getLibrationAmplitudeBins());
        if (!trojanData.getWidthBins().isEmpty()) trojans.put("widthBins", trojanData.getWidthBins());
        if (!trojanData.getTierCounts().isEmpty()) trojans.put("tierClassification", trojanData.getTierCounts());

        // Summary
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalTrojanSwarms", trojanData.getTotalTrojans());
        summary.put("planetsWithTrojans", planetData.getPlanetsWithTrojans());
        summary.put("trojansWithNotableAsteroids", trojanData.getTrojansWithAsteroids());
        summary.put("totalNotableAsteroids", trojanData.getTotalNotableAsteroids());
        summary.put("trojansWithMoons", trojanData.getTrojansWithMoons());
        if (trojanData.getMassCount() > 0) {
            summary.put("avgMassEarth", roundScientific(trojanData.getTotalMassSum() / trojanData.getMassCount()));
        }
        if (trojanData.getWidthCount() > 0) {
            summary.put("avgWidthAU", round(trojanData.getTotalWidthSum() / trojanData.getWidthCount()));
        }
        if (trojanData.getObjectCountCount() > 0) {
            summary.put("avgObjectCount", trojanData.getTotalObjectCountSum() / trojanData.getObjectCountCount());
        }
        if (trojanData.getLibrationCount() > 0) {
            summary.put("avgLibrationAmplitudeDeg", round(trojanData.getTotalLibrationSum() / trojanData.getLibrationCount()));
        }
        trojans.put("summary", summary);

        // Per planet-type breakdown
        if (!trojanData.getPerPlanetType().isEmpty()) {
            Map<String, Object> perType = new LinkedHashMap<>();
            for (Map.Entry<String, TrojanDataCollector.TrojanPlanetTypeBreakdown> entry : trojanData.getPerPlanetType().entrySet()) {
                perType.put(entry.getKey(), entry.getValue().toJson());
            }
            trojans.put("perPlanetType", perType);
        }

        // Cross-reference summary table: planet type → trojan spawning overview
        // Combines total planet counts (from planetData) with trojan stats (from trojanData)
        Map<String, Object> summaryTable = new LinkedHashMap<>();
        for (Map.Entry<String, PlanetTypeBreakdown> ptEntry : planetData.getPerTypeData().entrySet()) {
            String typeName = ptEntry.getKey();
            PlanetTypeBreakdown ptb = ptEntry.getValue();
            int totalPlanets = ptb.getCount();
            int planetsWithSwarms = ptb.getWithTrojans();
            if (planetsWithSwarms == 0) continue; // skip types with no trojans

            TrojanDataCollector.TrojanPlanetTypeBreakdown tptb = trojanData.getPerPlanetType().get(typeName);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("totalPlanets", totalPlanets);
            row.put("planetsWithSwarms", planetsWithSwarms);
            row.put("pctWithSwarms", round(planetsWithSwarms * 100.0 / totalPlanets));
            if (tptb != null) {
                row.put("swarmCount", tptb.getCount());
                row.put("pctSwarmsWithAsteroids", tptb.getCount() > 0
                        ? round(tptb.getWithAsteroids() * 100.0 / tptb.getCount()) : 0);
                row.put("pctPlanetsWithMoons", planetsWithSwarms > 0
                        ? round(tptb.getWithMoons() * 100.0 / planetsWithSwarms) : 0);
                row.put("totalObjectCount", tptb.getObjectCountSum());
            }
            summaryTable.put(typeName, row);
        }
        if (!summaryTable.isEmpty()) {
            trojans.put("planetTypeSummary", summaryTable);
        }

        return trojans;
    }

    private Map<String, Object> buildAsteroidData() {
        Map<String, Object> asteroids = new LinkedHashMap<>();

        // Source distribution (Belt vs Trojan)
        if (!asteroidData.getSourceCounts().isEmpty()) asteroids.put("sourceCounts", asteroidData.getSourceCounts());

        // Spectral type distribution
        if (!asteroidData.getAsteroidTypes().isEmpty()) asteroids.put("spectralTypes", asteroidData.getAsteroidTypes());

        // Physical property distributions
        if (!asteroidData.getDiameterBins().isEmpty()) asteroids.put("diameterBins", asteroidData.getDiameterBins());
        if (!asteroidData.getMassBins().isEmpty()) asteroids.put("massBins", asteroidData.getMassBins());
        if (!asteroidData.getDensityBins().isEmpty()) asteroids.put("densityBins", asteroidData.getDensityBins());
        if (!asteroidData.getAlbedoBins().isEmpty()) asteroids.put("albedoBins", asteroidData.getAlbedoBins());

        // Cratering and orbital distance
        if (!asteroidData.getCrateringLevels().isEmpty()) asteroids.put("crateringLevels", asteroidData.getCrateringLevels());
        if (!asteroidData.getDistanceBins().isEmpty()) asteroids.put("orbitalDistanceBins", asteroidData.getDistanceBins());

        // Summary
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalNotableAsteroids", asteroidData.getTotalAsteroids());
        summary.put("fromBelts", asteroidData.getSourceCounts().getOrDefault("Belt", 0));
        summary.put("fromTrojans", asteroidData.getSourceCounts().getOrDefault("Trojan", 0));
        summary.put("differentiated", asteroidData.getTotalDifferentiated());
        summary.put("withRegolith", asteroidData.getTotalWithRegolith());
        if (asteroidData.getDiameterCount() > 0) {
            summary.put("avgDiameterKm", round(asteroidData.getDiameterSum() / asteroidData.getDiameterCount()));
        }
        if (asteroidData.getMassCount() > 0) {
            summary.put("avgMassEarth", roundScientific(asteroidData.getMassSum() / asteroidData.getMassCount()));
        }
        if (asteroidData.getDensityCount() > 0) {
            summary.put("avgDensity", round(asteroidData.getDensitySum() / asteroidData.getDensityCount()));
        }
        if (asteroidData.getAlbedoCount() > 0) {
            summary.put("avgAlbedo", round(asteroidData.getAlbedoSum() / asteroidData.getAlbedoCount()));
        }
        asteroids.put("summary", summary);

        // Per spectral-type breakdown
        if (!asteroidData.getPerTypeData().isEmpty()) {
            Map<String, Object> perType = new LinkedHashMap<>();
            for (Map.Entry<String, AsteroidDataCollector.AsteroidTypeBreakdown> entry : asteroidData.getPerTypeData().entrySet()) {
                perType.put(entry.getKey(), entry.getValue().toJson());
            }
            asteroids.put("perSpectralType", perType);
        }

        return asteroids;
    }

    private Map<String, Object> buildBeltData() {
        Map<String, Object> belts = new LinkedHashMap<>();
        belts.put("types", beltData.getBeltTypes());
        belts.put("asteroidTypes", beltData.getAsteroidTypes());

        // New distribution maps
        if (!beltData.getCompositionTypes().isEmpty()) belts.put("compositionTypes", beltData.getCompositionTypes());
        if (!beltData.getWidthBins().isEmpty()) belts.put("widthBins", beltData.getWidthBins());
        if (!beltData.getInnerEdgeBins().isEmpty()) belts.put("innerEdgeBins", beltData.getInnerEdgeBins());
        if (!beltData.getOuterEdgeBins().isEmpty()) belts.put("outerEdgeBins", beltData.getOuterEdgeBins());
        if (!beltData.getMassBins().isEmpty()) belts.put("massBins", beltData.getMassBins());
        if (!beltData.getEccentricityBins().isEmpty()) belts.put("eccentricityBins", beltData.getEccentricityBins());
        if (!beltData.getInclinationBins().isEmpty()) belts.put("inclinationBins", beltData.getInclinationBins());
        if (!beltData.getObjectCountBins().isEmpty()) belts.put("objectCountBins", beltData.getObjectCountBins());
        if (!beltData.getParentStarTypes().isEmpty()) belts.put("parentStarTypes", beltData.getParentStarTypes());
        if (!beltData.getBinaryConfigBelts().isEmpty()) belts.put("binaryConfigBelts", beltData.getBinaryConfigBelts());

        // Summary
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalBelts", counts.getBeltCount());
        summary.put("dwarfPlanetsInBelts", counts.getDwarfPlanetCount());
        summary.put("withGaps", beltData.getBeltsWithGaps());
        summary.put("withCollisionalFamilies", beltData.getBeltsWithCollisionalFamilies());
        summary.put("withDwarfPlanets", beltData.getBeltsWithDwarfPlanets());
        if (beltData.getMassCount() > 0) {
            summary.put("avgMassEarth", round(beltData.getTotalMassSum() / beltData.getMassCount()));
        }
        if (beltData.getWidthCount() > 0) {
            summary.put("avgWidthAU", round(beltData.getTotalWidthSum() / beltData.getWidthCount()));
        }
        belts.put("summary", summary);

        // Per-type breakdown
        if (!beltData.getPerTypeData().isEmpty()) {
            Map<String, Object> perType = new LinkedHashMap<>();
            for (Map.Entry<String, BeltDataCollector.BeltTypeBreakdown> entry : beltData.getPerTypeData().entrySet()) {
                perType.put(entry.getKey(), entry.getValue().toJson());
            }
            belts.put("perType", perType);
        }

        return belts;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Climate
    // ═══════════════════════════════════════════════════════════════

    private Map<String, Object> buildClimateData() {
        Map<String, Object> climate = new LinkedHashMap<>();
        climate.put("all", buildWeatherBucket(planetData.getClimateAll()));
        climate.put("surface", buildWeatherBucket(planetData.getClimateSurface()));
        climate.put("gasIce", buildWeatherBucket(planetData.getClimateGas()));
        return climate;
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

    // ═══════════════════════════════════════════════════════════════
    //  Orbital Stability
    // ═══════════════════════════════════════════════════════════════

    private Map<String, Object> buildOrbitalStabilityData() {
        Map<String, Object> stability = new LinkedHashMap<>();
        int totalPairs = stabilityData.getTotalAdjacentPairs();
        int multiPlanetSystems = stabilityData.getSystemsWithMultiplePlanets();

        // Summary
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalAdjacentPairs", totalPairs);
        summary.put("multiPlanetSystems", multiPlanetSystems);
        summary.put("systemsAllStable", stabilityData.getSystemsAllStable());
        summary.put("systemsAnyMarginalOrWorse", stabilityData.getSystemsAnyMarginalOrWorse());
        summary.put("systemsAnyCrossing", stabilityData.getSystemsAnyCrossing());
        summary.put("crossingPairCount", stabilityData.getCrossingPairCount());
        summary.put("doomedButAliveCount", stabilityData.getDoomedButAliveCount());
        summary.put("meanGladmanDelta", round(stabilityData.getMeanGladmanDelta()));
        summary.put("medianGladmanDelta", round(stabilityData.getMedianGladmanDelta()));
        summary.put("meanPerSystemMinDelta", round(stabilityData.getMeanPerSystemMinDelta()));
        summary.put("medianMinSmaGapAU", roundAU(stabilityData.getMedianMinSmaGapAU()));
        summary.put("gladmanFloorOverridePercent", round(stabilityData.getGladmanFloorOverridePercent()));
        if (stabilityData.getMaxSmaCount() > 0) {
            summary.put("avgSystemOuterExtentAU", round(stabilityData.getMaxSmaSum() / stabilityData.getMaxSmaCount()));
        }
        stability.put("summary", summary);

        // Core stability classifications
        stability.put("stabilityClassification", stabilityData.getStabilityClassification());
        stability.put("stabilityByStarType", stabilityData.getStabilityByStarType());
        stability.put("stabilityByOrbitalPosition", stabilityData.getStabilityByOrbitalPosition());
        stability.put("stabilityByBinaryConfig", stabilityData.getStabilityByBinaryConfig());

        // Gladman delta
        Map<String, Object> gladman = new LinkedHashMap<>();
        gladman.put("deltaBins", stabilityData.getGladmanDeltaBins());
        gladman.put("systemsWithAnyPairBelowCritical", stabilityData.getSystemsWithAnyPairBelowCritical());
        stability.put("gladmanDelta", gladman);

        // Orbit crossing
        Map<String, Object> crossing = new LinkedHashMap<>();
        crossing.put("crossingPairCount", stabilityData.getCrossingPairCount());
        if (!stabilityData.getCrossingByPlanetTypePair().isEmpty()) {
            crossing.put("crossingByPlanetTypePair", stabilityData.getCrossingByPlanetTypePair());
        }
        stability.put("orbitCrossing", crossing);

        // Eccentricity
        Map<String, Object> ecc = new LinkedHashMap<>();
        ecc.put("eccentricityBins", stabilityData.getEccentricityBins());
        Map<String, Object> eccByPos = new LinkedHashMap<>();
        for (Map.Entry<String, double[]> e : stabilityData.getEccentricityByPosition().entrySet()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("meanEccentricity", e.getValue()[1] > 0 ? round(e.getValue()[0] / e.getValue()[1]) : 0);
            entry.put("sampleCount", (int) e.getValue()[1]);
            eccByPos.put(e.getKey(), entry);
        }
        ecc.put("eccentricityByPosition", eccByPos);
        stability.put("eccentricity", ecc);

        // Timescale
        Map<String, Object> timescale = new LinkedHashMap<>();
        timescale.put("timescaleBins", stabilityData.getTimescaleBins());
        timescale.put("timescaleVsAge", stabilityData.getTimescaleVsAgeBins());
        timescale.put("doomedButAlive", stabilityData.getDoomedButAliveCount());
        stability.put("timescale", timescale);

        // Spacing
        Map<String, Object> spacing = new LinkedHashMap<>();
        spacing.put("smaRatioBins", stabilityData.getSmaRatioBins());
        spacing.put("pairsNeedingWiderSpacing", stabilityData.getPairsWhereGladmanFloorWouldWiden());
        spacing.put("totalPairsChecked", stabilityData.getTotalSpacingPairsChecked());
        spacing.put("gladmanFloorOverridePercent", round(stabilityData.getGladmanFloorOverridePercent()));
        stability.put("spacing", spacing);

        // System-level
        Map<String, Object> systemLevel = new LinkedHashMap<>();
        systemLevel.put("planetsPerSystem", stabilityData.getPlanetsPerSystemBins());
        systemLevel.put("systemOuterExtent", stabilityData.getSystemOuterExtentBins());
        stability.put("systemLevel", systemLevel);

        // Belt stability
        if (stabilityData.getTotalBeltsAnalyzed() > 0) {
            Map<String, Object> beltStability = new LinkedHashMap<>();
            beltStability.put("totalBeltsAnalyzed", stabilityData.getTotalBeltsAnalyzed());
            beltStability.put("beltOverlapCount", stabilityData.getBeltOverlapCount());
            beltStability.put("beltPlanetOverlapCount", stabilityData.getBeltPlanetOverlapCount());
            beltStability.put("beltsExceedingStabilityLimit", stabilityData.getBeltsExceedingStabilityLimit());
            beltStability.put("beltsBelowCavityLimit", stabilityData.getBeltsBelowCavityLimit());
            if (!stabilityData.getBeltOverlapDetails().isEmpty()) {
                beltStability.put("beltOverlapDetails", stabilityData.getBeltOverlapDetails());
            }
            if (!stabilityData.getBeltPlanetOverlapDetails().isEmpty()) {
                beltStability.put("beltPlanetOverlapDetails", stabilityData.getBeltPlanetOverlapDetails());
            }
            stability.put("beltStability", beltStability);
        }

        // Planet-star stability
        int totalPlanetStarIssues = stabilityData.getPlanetStarCrossingCount()
                + stabilityData.getPlanetsExceedingSTypeCritical()
                + stabilityData.getPlanetsBelowPTypeCritical();
        if (totalPlanetStarIssues > 0 || stabilityData.getTotalBeltsAnalyzed() > 0) {
            Map<String, Object> planetStarStability = new LinkedHashMap<>();
            planetStarStability.put("planetStarCrossingCount", stabilityData.getPlanetStarCrossingCount());
            planetStarStability.put("planetsExceedingSTypeCritical", stabilityData.getPlanetsExceedingSTypeCritical());
            planetStarStability.put("planetsBelowPTypeCritical", stabilityData.getPlanetsBelowPTypeCritical());
            stability.put("planetStarStability", planetStarStability);
        }

        return stability;
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private double roundAU(double val) {
        return Math.round(val * 10000.0) / 10000.0;
    }

    /** High-precision rounding for very small values like Trojan masses in Earth masses */
    private double roundScientific(double val) {
        return Math.round(val * 1e12) / 1e12;
    }
}
