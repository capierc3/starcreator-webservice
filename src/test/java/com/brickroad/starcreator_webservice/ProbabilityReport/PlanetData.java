package com.brickroad.starcreator_webservice.ProbabilityReport;

import com.brickroad.starcreator_webservice.entity.ud.*;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanetData {

    private static final Map<String, Integer> PLANET_TYPES = new HashMap<>();
    private static final Map<String, Integer> ATMOSPHERE_CLASSIFICATIONS = new HashMap<>();
    private static final Map<String, Integer> PROTECTION_LEVELS = new HashMap<>();
    private static final Map<String, Integer> AURORAL_FREQUENCIES = new HashMap<>();
    private static final Map<String, Integer> AURORAL_INTENSITIES = new HashMap<>();
    private static final Map<String, Integer> TIDAL_LOCK_COUNTS = new HashMap<>();
    private static final Map<String, Integer> HZ_POSITIONS = new HashMap<>();

    private static final Map<String, Integer> MAGNETOPAUSE_BINS = new HashMap<>();
    private static final Map<String, Integer> ATM_LOSS_RATE_BINS = new HashMap<>();
    private static final Map<String, Integer> BELT_INTENSITY_INNER = new HashMap<>();
    private static final Map<String, Integer> BELT_INTENSITY_OUTER = new HashMap<>();

    private static final Map<String, int[]> ACTIVITY_VS_ATMOSPHERE = new HashMap<>();
    private static final Map<String, int[]> ACTIVITY_VS_PROTECTION = new HashMap<>();
    private static final Map<String, double[]> DISTANCE_VS_MAGNETOPAUSE = new HashMap<>();

    private static final Map<String, Integer> WATER_INVENTORIES = new HashMap<>();
    private static final Map<String, Integer> WATER_PHASES = new HashMap<>();
    private static int planetsWithLiquidWater = 0;
    private static int planetsWithIce = 0;
    private static int planetsWithSubsurfaceWater = 0;
    private static int totalRockyPlanets = 0;

    private static final Map<String, Integer> HABITABILITY_CLASSES = new HashMap<>();
    private static final Map<String, Integer> COLONIZATION_SUITABILITIES = new HashMap<>();
    private static final Map<String, Integer> TERRAFORMING_POTENTIALS = new HashMap<>();
    private static final Map<String, Integer> BIOSIGNATURE_POTENTIALS = new HashMap<>();
    private static final Map<String, Integer> LIFE_COMPLEXITY_POTENTIALS = new HashMap<>();
    private static double esiSum = 0.0;
    private static double habScoreSum = 0.0;
    private static int habAssessmentCount = 0;
    private static int breathablePlanets = 0;

    // --- NEW: Additional tracking ---
    private static int planetsWithRings = 0;
    private static final Map<String, Integer> COMPOSITION_CLASSES = new HashMap<>();
    private static final Map<String, Integer> SURFACE_TEMP_BINS = new HashMap<>();
    private static final Map<String, Integer> GEOLOGICAL_ACTIVITY = new HashMap<>();
    private static final Map<String, Integer> TECTONIC_LEVELS = new HashMap<>();
    private static final Map<String, Integer> VOLCANISM_TYPES = new HashMap<>();
    private static final Map<String, PlanetTypeBreakdown> PER_TYPE_DATA = new HashMap<>();
    private static double totalMass = 0;
    private static double totalRadius = 0;
    private static double totalGravity = 0;
    private static int physicalPropsCount = 0;
    private static int planetsWithGeology = 0;

    // --- Weather tracking (split: all / surface / gas) ---
    /** Holds all weather counters for a category of planets. */
    static class WeatherBucket {
        int count = 0;
        final Map<String, Integer> skyColors = new HashMap<>();
        final Map<String, Integer> cloudCoverageClass = new HashMap<>();
        final Map<String, Integer> windIntensity = new HashMap<>();
        final Map<String, Integer> stormFrequency = new HashMap<>();
        final Map<String, Integer> severity = new HashMap<>();
        final Map<String, Integer> exposureRating = new HashMap<>();
        final Map<String, Integer> circulationPattern = new HashMap<>();
        final Map<String, Integer> lightningType = new HashMap<>();
        final Map<String, Integer> tidalRangeBins = new HashMap<>();
        int withPrecipitation = 0;
        int withDustStorms = 0;
        int withLightning = 0;
        int withSuperRotation = 0;
        int withGreatDarkSpot = 0;
        double totalCloudCoverage = 0;
        double totalWindSpeed = 0;
        int windSpeedCount = 0;
        int cloudLayerTotal = 0;
        int precipTypeTotal = 0;
        int extremeEventTotal = 0;
        int eclipseTotal = 0;
    }

    private static final WeatherBucket WEATHER_ALL = new WeatherBucket();
    private static final WeatherBucket WEATHER_SURFACE = new WeatherBucket();
    private static final WeatherBucket WEATHER_GAS = new WeatherBucket();

    static void analyzeData(Planet planet, ProbabilityCounts counts) {
        PLANET_TYPES.put(planet.getPlanetType(), PLANET_TYPES.getOrDefault(planet.getPlanetType(), 0) + 1);

        // --- Per-type breakdown ---
                PlanetTypeBreakdown typeData = PER_TYPE_DATA.computeIfAbsent(planet.getPlanetType(), k -> new PlanetTypeBreakdown());
        typeData.increment();
        typeData.addCompositionClass(planet.getCompositionClassification() != null ? planet.getCompositionClassification() : "NULL");
        if (planet.getSurfaceTemp() != null) {
            typeData.addSurfaceTempBin(binTemperature(planet.getSurfaceTemp()));
        }
        typeData.addAtmosphereClass(planet.getAtmosphereClassification() != null ? planet.getAtmosphereClassification() : "NULL");
        typeData.addHzPosition(planet.getHabitableZonePosition() != null ? planet.getHabitableZonePosition() : "unknown");
        if (Boolean.TRUE.equals(planet.getTidallyLocked())) typeData.addTidallyLocked();
        if (Boolean.TRUE.equals(planet.getHasRings())) typeData.addRings();
        if (planet.getEarthMass() != null) {
            typeData.addMassBin(binMass(planet.getEarthMass()));
            typeData.addPhysicalProps(
                    planet.getEarthMass(),
                    planet.getEarthRadius() != null ? planet.getEarthRadius() : 0,
                    planet.getSurfaceGravity() != null ? planet.getSurfaceGravity() : 0,
                    planet.getSurfaceTemp() != null ? planet.getSurfaceTemp() : 0);
        }
        typeData.addMoonCountBin(binMoonCount(planet.getMoons().size()));

        // --- NEW: Composition classification ---
        String compClass = planet.getCompositionClassification() != null ? planet.getCompositionClassification() : "NULL";
        COMPOSITION_CLASSES.put(compClass, COMPOSITION_CLASSES.getOrDefault(compClass, 0) + 1);

        // --- NEW: Rings ---
        if (Boolean.TRUE.equals(planet.getHasRings())) planetsWithRings++;

        // --- NEW: Geology (surface/rocky planets only) ---
        if (isSurfaceType(planet.getPlanetType()) && planet.getGeologicalActivity() != null) {
            planetsWithGeology++;
            GEOLOGICAL_ACTIVITY.put(planet.getGeologicalActivity(),
                    GEOLOGICAL_ACTIVITY.getOrDefault(planet.getGeologicalActivity(), 0) + 1);
            typeData.addGeologicalActivity(planet.getGeologicalActivity());
            if (planet.getTectonicActivityLevel() != null) {
                TECTONIC_LEVELS.put(planet.getTectonicActivityLevel(),
                        TECTONIC_LEVELS.getOrDefault(planet.getTectonicActivityLevel(), 0) + 1);
            }
            if (planet.getVolcanismType() != null) {
                VOLCANISM_TYPES.put(planet.getVolcanismType(),
                        VOLCANISM_TYPES.getOrDefault(planet.getVolcanismType(), 0) + 1);
            }
        }

        // --- NEW: Surface temperature bins ---
        if (planet.getSurfaceTemp() != null) {
            String tempBin = binTemperature(planet.getSurfaceTemp());
            SURFACE_TEMP_BINS.put(tempBin, SURFACE_TEMP_BINS.getOrDefault(tempBin, 0) + 1);
        }

        // --- NEW: Physical property averages ---
        if (planet.getEarthMass() != null) {
            totalMass += planet.getEarthMass();
            totalRadius += planet.getEarthRadius() != null ? planet.getEarthRadius() : 0;
            totalGravity += planet.getSurfaceGravity() != null ? planet.getSurfaceGravity() : 0;
            physicalPropsCount++;
        }

        // --- Stellar Activity Integration Data ---

        // Atmosphere classification
        String atmClass = planet.getAtmosphereClassification() != null ?
                planet.getAtmosphereClassification() : "NULL";
        ATMOSPHERE_CLASSIFICATIONS.put(atmClass, ATMOSPHERE_CLASSIFICATIONS.getOrDefault(atmClass, 0) + 1);

        // Habitable zone position
        String hzPos = planet.getHabitableZonePosition() != null ? planet.getHabitableZonePosition() : "unknown";
        HZ_POSITIONS.put(hzPos, HZ_POSITIONS.getOrDefault(hzPos, 0) + 1);

        // Tidal locking
        String lockStatus = Boolean.TRUE.equals(planet.getTidallyLocked()) ? "LOCKED" : "NOT_LOCKED";
        TIDAL_LOCK_COUNTS.put(lockStatus, TIDAL_LOCK_COUNTS.getOrDefault(lockStatus, 0) + 1);

        // Magnetic field data
        PlanetaryMagneticField mf = planet.getMagneticField();
        if (mf != null) {
            String protection = mf.getProtectionLevel() != null ? mf.getProtectionLevel().name() : "NULL";
            PROTECTION_LEVELS.put(protection, PROTECTION_LEVELS.getOrDefault(protection, 0) + 1);
            typeData.addProtectionLevel(protection);

            String auroraFreq = mf.getAuroralFrequency() != null ? mf.getAuroralFrequency().name() : "NONE";
            AURORAL_FREQUENCIES.put(auroraFreq, AURORAL_FREQUENCIES.getOrDefault(auroraFreq, 0) + 1);

            String auroraInt = mf.getAuroralIntensity() != null ? mf.getAuroralIntensity().name() : "NONE";
            AURORAL_INTENSITIES.put(auroraInt, AURORAL_INTENSITIES.getOrDefault(auroraInt, 0) + 1);

            String innerBelt = mf.getInnerBeltIntensity() != null ? mf.getInnerBeltIntensity().name() : "NULL";
            BELT_INTENSITY_INNER.put(innerBelt, BELT_INTENSITY_INNER.getOrDefault(innerBelt, 0) + 1);
            String outerBelt = mf.getOuterBeltIntensity() != null ? mf.getOuterBeltIntensity().name() : "NULL";
            BELT_INTENSITY_OUTER.put(outerBelt, BELT_INTENSITY_OUTER.getOrDefault(outerBelt, 0) + 1);

            if (mf.getMagnetopauseDistancePlanetRadii() != null) {
                String mpBin = binMagnetopause(mf.getMagnetopauseDistancePlanetRadii());
                MAGNETOPAUSE_BINS.put(mpBin, MAGNETOPAUSE_BINS.getOrDefault(mpBin, 0) + 1);
            }

            if (mf.getAtmosphericLossRateFactor() != null) {
                String lossBin = binLossRate(mf.getAtmosphericLossRateFactor());
                ATM_LOSS_RATE_BINS.put(lossBin, ATM_LOSS_RATE_BINS.getOrDefault(lossBin, 0) + 1);
            }

            if (planet.getSemiMajorAxisAU() != null && mf.getMagnetopauseDistancePlanetRadii() != null) {
                String distBin = binDistance(planet.getSemiMajorAxisAU());
                double[] stats = DISTANCE_VS_MAGNETOPAUSE.getOrDefault(distBin, new double[]{0, 0});
                stats[0] += mf.getMagnetopauseDistancePlanetRadii();
                stats[1] += 1;
                DISTANCE_VS_MAGNETOPAUSE.put(distBin, stats);
            }
        }

        // Cross-reference: star activity vs atmosphere stripping (rocky planets only)
        Star parentStar = planet.getParentStar();
        if (parentStar != null && isRockyTypeWithAtmospherePotential(planet.getPlanetType())) {
            String actLevel = parentStar.getActivityLevel() != null ?
                    parentStar.getActivityLevel() : "UNKNOWN";

            int[] atmCounts = ACTIVITY_VS_ATMOSPHERE.getOrDefault(actLevel, new int[]{0, 0});
            atmCounts[0]++;
            if ("NONE".equals(planet.getAtmosphereClassification())) {
                atmCounts[1]++;
            }
            ACTIVITY_VS_ATMOSPHERE.put(actLevel, atmCounts);

            if (mf != null && mf.getProtectionLevel() != null) {
                int[] protCounts = ACTIVITY_VS_PROTECTION.getOrDefault(actLevel, new int[]{0, 0, 0, 0, 0, 0});
                protCounts[0]++;
                switch (mf.getProtectionLevel()) {
                    case NONE -> protCounts[1]++;
                    case MINIMAL -> protCounts[2]++;
                    case MODERATE -> protCounts[3]++;
                    case STRONG -> protCounts[4]++;
                    case EXCEPTIONAL -> protCounts[5]++;
                }
                ACTIVITY_VS_PROTECTION.put(actLevel, protCounts);
            }
        }

        // Moon/Ring tracking
        counts.incrementMoonCount(planet.getMoons().size());
        for (Moon moon : planet.getMoons()) {
            MoonData.analyzeData(moon);
        }

        counts.incrementRingCount(planet.getRings().size());
        for (Ring ring : planet.getRings()) {
            RingData.analyzeData(ring);
        }

        analyzeWaterData(planet);
        analyzeDataHabitabilityData(planet);
        analyzeWeatherData(planet);
    }

    private static void analyzeWaterData(Planet planet) {
        String type = planet.getPlanetType();
        if (type == null) return;

        boolean isRocky = type.contains("Terrestrial") || type.contains("Super-Earth")
                || type.contains("Desert") || type.contains("Ocean")
                || type.contains("Iron") || type.contains("Carbon")
                || type.contains("Lava") || type.contains("Hot Rocky")
                || type.contains("Ice World") || type.contains("Dwarf");

        if (!isRocky) return;
        totalRockyPlanets++;

        String inventory = planet.getWaterInventory() != null ?
                planet.getWaterInventory() : "NULL";
        WATER_INVENTORIES.put(inventory, WATER_INVENTORIES.getOrDefault(inventory, 0) + 1);
        PlanetTypeBreakdown typeData = PER_TYPE_DATA.get(planet.getPlanetType());
        if (typeData != null) typeData.addWaterInventory(inventory);

        Double liquidPct = planet.getLiquidWaterCoveragePercent();
        Double icePct = planet.getIceCoveragePercent();

        if (liquidPct != null && liquidPct > 0.1) planetsWithLiquidWater++;
        if (icePct != null && icePct > 0.1) planetsWithIce++;
        if (Boolean.TRUE.equals(planet.getHasSubsurfaceWater())) planetsWithSubsurfaceWater++;
    }

    private static void analyzeDataHabitabilityData(Planet planet) {
        PlanetaryHabitability hab = planet.getHabitability();
        if (hab == null) return;

        habAssessmentCount++;

        String habClass = hab.getHabitabilityClass() != null ? hab.getHabitabilityClass().name() : "NULL";
        HABITABILITY_CLASSES.put(habClass, HABITABILITY_CLASSES.getOrDefault(habClass, 0) + 1);

        PlanetTypeBreakdown typeData = PER_TYPE_DATA.get(planet.getPlanetType());
        if (typeData != null) typeData.addHabitabilityClass(habClass);

        String colSuit = hab.getColonizationSuitability() != null ? hab.getColonizationSuitability().name() : "NULL";
        COLONIZATION_SUITABILITIES.put(colSuit, COLONIZATION_SUITABILITIES.getOrDefault(colSuit, 0) + 1);

        String terrPot = hab.getTerraformingPotential() != null ? hab.getTerraformingPotential().name() : "NULL";
        TERRAFORMING_POTENTIALS.put(terrPot, TERRAFORMING_POTENTIALS.getOrDefault(terrPot, 0) + 1);

        String bioPot = hab.getBiosignaturePotential() != null ? hab.getBiosignaturePotential() : "NONE";
        BIOSIGNATURE_POTENTIALS.put(bioPot, BIOSIGNATURE_POTENTIALS.getOrDefault(bioPot, 0) + 1);

        String lifePot = hab.getLifeComplexityPotential() != null ? hab.getLifeComplexityPotential() : "NONE";
        LIFE_COMPLEXITY_POTENTIALS.put(lifePot, LIFE_COMPLEXITY_POTENTIALS.getOrDefault(lifePot, 0) + 1);

        if (hab.getEsiTotal() != null) esiSum += hab.getEsiTotal();
        if (hab.getHabitabilityScore() != null) habScoreSum += hab.getHabitabilityScore();
        if (Boolean.TRUE.equals(hab.getIsBreathable())) breathablePlanets++;

        String waterPhase = hab.getWaterPhaseAtSurface() != null ? hab.getWaterPhaseAtSurface() : "NULL";
        WATER_PHASES.put(waterPhase, WATER_PHASES.getOrDefault(waterPhase, 0) + 1);
    }

    private static boolean isGasType(String planetType) {
        if (planetType == null) return false;
        return planetType.contains("Gas Giant") || planetType.contains("Ice Giant")
                || planetType.contains("Hot Jupiter") || planetType.contains("Hot Neptune")
                || planetType.contains("Sub-Neptune") || planetType.contains("Mini-Neptune");
    }

    private static void analyzeWeatherData(Planet planet) {
        PlanetaryWeather w = planet.getWeather();
        if (w == null) return;

        // Determine which split bucket
        WeatherBucket split = isGasType(planet.getPlanetType()) ? WEATHER_GAS : WEATHER_SURFACE;

        // Populate both the combined (ALL) and the split bucket
        for (WeatherBucket bucket : new WeatherBucket[]{WEATHER_ALL, split}) {
            bucket.count++;

            String skyColor = w.getSkyColor() != null ? w.getSkyColor() : "NULL";
            bucket.skyColors.merge(skyColor, 1, Integer::sum);

            String cloudClass = w.getCloudCoverageClass() != null ? w.getCloudCoverageClass() : "NULL";
            bucket.cloudCoverageClass.merge(cloudClass, 1, Integer::sum);

            if (w.getCloudCoveragePercent() != null) bucket.totalCloudCoverage += w.getCloudCoveragePercent();

            String wind = w.getWindIntensity() != null ? w.getWindIntensity() : "NULL";
            bucket.windIntensity.merge(wind, 1, Integer::sum);

            if (w.getMeanSurfaceWindSpeedMs() != null) {
                bucket.totalWindSpeed += w.getMeanSurfaceWindSpeedMs();
                bucket.windSpeedCount++;
            }

            String circulation = w.getCirculationPattern() != null ? w.getCirculationPattern() : "NULL";
            bucket.circulationPattern.merge(circulation, 1, Integer::sum);

            if (Boolean.TRUE.equals(w.getHasSuperRotation())) bucket.withSuperRotation++;

            String stormFreq = w.getStormFrequency() != null ? w.getStormFrequency() : "NULL";
            bucket.stormFrequency.merge(stormFreq, 1, Integer::sum);

            if (Boolean.TRUE.equals(w.getHasDustStorms())) bucket.withDustStorms++;
            if (Boolean.TRUE.equals(w.getHasLightning())) {
                bucket.withLightning++;
                String lt = w.getLightningType() != null ? w.getLightningType() : "UNKNOWN";
                bucket.lightningType.merge(lt, 1, Integer::sum);
            }

            if (Boolean.TRUE.equals(w.getHasPrecipitation())) bucket.withPrecipitation++;

            String sev = w.getWeatherSeverity() != null ? w.getWeatherSeverity() : "NULL";
            bucket.severity.merge(sev, 1, Integer::sum);

            String exp = w.getOutdoorExposureRating() != null ? w.getOutdoorExposureRating() : "NULL";
            bucket.exposureRating.merge(exp, 1, Integer::sum);

            if (w.getTidalRangeMeters() != null) {
                bucket.tidalRangeBins.merge(binTidalRange(w.getTidalRangeMeters()), 1, Integer::sum);
            }

            if (Boolean.TRUE.equals(w.getHasGreatDarkSpot())) bucket.withGreatDarkSpot++;

            if (w.getCloudLayers() != null) bucket.cloudLayerTotal += w.getCloudLayers().size();
            if (w.getPrecipitationTypes() != null) bucket.precipTypeTotal += w.getPrecipitationTypes().size();
            if (w.getExtremeWeatherEvents() != null) bucket.extremeEventTotal += w.getExtremeWeatherEvents().size();
            if (w.getEclipseData() != null) bucket.eclipseTotal += w.getEclipseData().size();
        }
    }

    private static String binTidalRange(double meters) {
        if (meters < 0.1) return "a: <0.1m (negligible)";
        if (meters < 1.0) return "b: 0.1-1m (Earth-like)";
        if (meters < 5.0) return "c: 1-5m (moderate)";
        if (meters < 20.0) return "d: 5-20m (strong)";
        if (meters < 100.0) return "e: 20-100m (extreme)";
        return "f: 100m+ (catastrophic)";
    }

    static void printData(PrintWriter writer, ProbabilityCounts counts) {
        writer.println("---");
        ReportUtils.beginCollapsible(writer, "Planet Types", 2);

        // Summary stats
        writer.println("**Summary:** " + counts.getPlanetCount() + " planets across "
                + counts.getSystemCount() + " systems (avg "
                + String.format("%.1f", counts.getPlanetCount() * 1.0 / Math.max(1, counts.getSystemCount()))
                + " per system)");
        writer.println("- With rings: " + planetsWithRings + " ("
                + ReportUtils.pct(planetsWithRings, counts.getPlanetCount()) + "%)");
        if (physicalPropsCount > 0) {
            writer.println("- Avg mass: " + String.format("%.2f", totalMass / physicalPropsCount) + " M⊕"
                    + " | Avg radius: " + String.format("%.2f", totalRadius / physicalPropsCount) + " R⊕"
                    + " | Avg gravity: " + String.format("%.2f", totalGravity / physicalPropsCount) + " g");
        }
        writer.println("");

        ReportUtils.printLinkedTable(writer, PLANET_TYPES, counts.getPlanetCount(), "Planet Type");

        // --- Composition Classification ---
        ReportUtils.printSubSection(writer, "Composition Classification");
        ReportUtils.printSortedTable(writer, COMPOSITION_CLASSES, counts.getPlanetCount(), "Classification");

        // --- Surface Temperature ---
        ReportUtils.printSubSection(writer, "Surface Temperature Distribution");
        ReportUtils.printSortedTableByKey(writer, SURFACE_TEMP_BINS, counts.getPlanetCount(), "Temperature Range");

        ReportUtils.endCollapsible(writer);

        printPerTypeBreakdown(writer);
        printAtmosphereData(writer, counts);
        printGeologyData(writer);
        printWaterAndHab(writer);
        printWeatherData(writer);
    }

    private static void printAtmosphereData(PrintWriter writer, ProbabilityCounts counts) {
        writer.println("---");
        ReportUtils.beginCollapsible(writer, "Atmosphere & Magnetic Fields", 2);

        ReportUtils.printSubSection(writer, "Atmosphere Classifications (All Planets)");
        ReportUtils.printSortedTable(writer, ATMOSPHERE_CLASSIFICATIONS, counts.getPlanetCount(), "Classification");

        ReportUtils.printSubSection(writer, "Tidal Locking");
        ReportUtils.printSortedTable(writer, TIDAL_LOCK_COUNTS, counts.getPlanetCount(), "Status");

        ReportUtils.printSubSection(writer, "Habitable Zone Positions");
        ReportUtils.printSortedTable(writer, HZ_POSITIONS, counts.getPlanetCount(), "Position");

        ReportUtils.printSubSection(writer, "Magnetic Protection Levels");
        ReportUtils.printSortedTable(writer, PROTECTION_LEVELS, counts.getPlanetCount(), "Level");

        // --- Magnetopause Distance Distribution ---
        ReportUtils.printSubSection(writer, "Magnetopause Distance Distribution");
        ReportUtils.printSortedTableByKey(writer, MAGNETOPAUSE_BINS, counts.getPlanetCount(), "Range (planet radii)");

        // --- Average Magnetopause by Distance from Star ---
        writer.println("### Average Magnetopause by Distance from Star");
        writer.println("");
        writer.println("*Should show compression (smaller magnetopause) closer to star*");
        writer.println("");
        writer.println("| Distance Bin | Avg Magnetopause (radii) | Sample Count |");
        writer.println("| --- | --- | --- |");
        DISTANCE_VS_MAGNETOPAUSE.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    double avg = e.getValue()[0] / e.getValue()[1];
                    writer.println("| " + e.getKey() + " | " + String.format("%.1f", avg)
                            + " | " + (int) e.getValue()[1] + " |");
                });
        writer.println("");

        // --- Atmospheric Loss Rate Distribution ---
        ReportUtils.printSubSection(writer, "Atmospheric Loss Rate Distribution");
        ReportUtils.printSortedTableByKey(writer, ATM_LOSS_RATE_BINS, counts.getPlanetCount(), "Range");

        // --- Auroral Frequency ---
        ReportUtils.printSubSection(writer, "Auroral Frequency");
        ReportUtils.printSortedTable(writer, AURORAL_FREQUENCIES, counts.getPlanetCount(), "Frequency");

        // --- Auroral Intensity ---
        ReportUtils.printSubSection(writer, "Auroral Intensity");
        ReportUtils.printSortedTable(writer, AURORAL_INTENSITIES, counts.getPlanetCount(), "Intensity");

        // --- Radiation Belt Intensity ---
        writer.println("### Radiation Belt Intensity (Inner / Outer)");
        writer.println("");
        writer.println("| Inner Belt | Count | % |");
        writer.println("| --- | --- | --- |");
        BELT_INTENSITY_INNER.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> writer.println("| " + e.getKey() + " | " + e.getValue() + " | " +
                        ReportUtils.pct(e.getValue(), counts.getPlanetCount()) + "% |"));
        writer.println("");
        writer.println("| Outer Belt | Count | % |");
        writer.println("| --- | --- | --- |");
        BELT_INTENSITY_OUTER.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> writer.println("| " + e.getKey() + " | " + e.getValue() + " | " +
                        ReportUtils.pct(e.getValue(), counts.getPlanetCount()) + "% |"));
        writer.println("");

        // =====================================================
        // CROSS-REFERENCE: Star Activity vs Planet Effects
        // =====================================================
        writer.println("### Star Activity vs Rocky Planet Atmosphere Stripping");
        writer.println("");
        writer.println("*Shows % of rocky planets that lost their atmosphere, grouped by parent star activity*");
        writer.println("");
        writer.println("| Star Activity | Total Rocky | Stripped (NONE) | Strip Rate |");
        writer.println("| --- | --- | --- | --- |");
        ACTIVITY_VS_ATMOSPHERE.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    int total = e.getValue()[0];
                    int stripped = e.getValue()[1];
                    double rate = total > 0 ? (stripped * 100.0 / total) : 0;
                    writer.println("| " + e.getKey() + " | " + total + " | " + stripped + " | " +
                            String.format("%.1f", rate) + "% |");
                });
        writer.println("");

        writer.println("### Star Activity vs Protection Level (Rocky Planets)");
        writer.println("");
        writer.println("*Expectation: HYPERACTIVE/VERY_ACTIVE stars should shift protection toward NONE/MINIMAL*");
        writer.println("");
        writer.println("| Star Activity | Total | NONE | MINIMAL | MODERATE | STRONG | EXCEPTIONAL |");
        writer.println("| --- | --- | --- | --- | --- | --- | --- |");
        ACTIVITY_VS_PROTECTION.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    int[] c = e.getValue();
                    writer.println("| " + e.getKey() + " | " + c[0] + " | " + c[1] + " | " + c[2] +
                            " | " + c[3] + " | " + c[4] + " | " + c[5] + " |");
                });
        writer.println("");

        ReportUtils.endCollapsible(writer);
    }

    private static void printGeologyData(PrintWriter writer) {
        if (GEOLOGICAL_ACTIVITY.isEmpty() && TECTONIC_LEVELS.isEmpty()) return;

        writer.println("---");
        ReportUtils.beginCollapsible(writer, "Geology (Rocky/Surface Planets)", 2);
        writer.println("Planets with geological data: " + planetsWithGeology);
        writer.println("");

        if (!GEOLOGICAL_ACTIVITY.isEmpty()) {
            ReportUtils.printSubSection(writer, "Geological Activity");
            ReportUtils.printSortedTable(writer, GEOLOGICAL_ACTIVITY, planetsWithGeology, "Activity");
        }

        if (!TECTONIC_LEVELS.isEmpty()) {
            ReportUtils.printSubSection(writer, "Tectonic Activity Level");
            ReportUtils.printSortedTable(writer, TECTONIC_LEVELS, planetsWithGeology, "Level");
        }

        if (!VOLCANISM_TYPES.isEmpty()) {
            ReportUtils.printSubSection(writer, "Volcanism Type");
            ReportUtils.printSortedTable(writer, VOLCANISM_TYPES, planetsWithGeology, "Type");
        }

        ReportUtils.endCollapsible(writer);
    }

    private static void printWaterAndHab(PrintWriter writer) {
        // ============================================================
        // WATER SYSTEM
        // ============================================================
        writer.println("---");
        ReportUtils.beginCollapsible(writer, "Water System (Rocky/Surface Planets Only)", 2);
        writer.println("Total rocky/surface planets analyzed: " + totalRockyPlanets);
        writer.println("- With liquid surface water: " + planetsWithLiquidWater
                + " (" + ReportUtils.pct(planetsWithLiquidWater, totalRockyPlanets) + "%)");
        writer.println("- With ice coverage: " + planetsWithIce
                + " (" + ReportUtils.pct(planetsWithIce, totalRockyPlanets) + "%)");
        writer.println("- With subsurface water: " + planetsWithSubsurfaceWater
                + " (" + ReportUtils.pct(planetsWithSubsurfaceWater, totalRockyPlanets) + "%)");
        writer.println("");

        ReportUtils.printSubSection(writer, "Water Inventory Distribution");
        ReportUtils.printSortedTable(writer, WATER_INVENTORIES, totalRockyPlanets, "Inventory");

        ReportUtils.printSubSection(writer, "Water Phase at Surface");
        ReportUtils.printSortedTable(writer, WATER_PHASES, habAssessmentCount, "Phase");

        ReportUtils.endCollapsible(writer);

        // ============================================================
        // HABITABILITY
        // ============================================================
        writer.println("---");
        ReportUtils.beginCollapsible(writer, "Planetary Habitability", 2);
        writer.println("Planets assessed: " + habAssessmentCount);
        writer.println("- Average ESI: " + String.format("%.3f", esiSum / Math.max(1, habAssessmentCount)));
        writer.println("- Average Habitability Score: " + String.format("%.1f", habScoreSum / Math.max(1, habAssessmentCount)));
        writer.println("- Breathable atmospheres: " + breathablePlanets
                + " (" + String.format("%.2f", breathablePlanets * 100.0 / Math.max(1, habAssessmentCount)) + "%)");
        writer.println("");

        ReportUtils.printSubSection(writer, "Habitability Class Distribution");
        ReportUtils.printSortedTable(writer, HABITABILITY_CLASSES, habAssessmentCount, "Class");

        ReportUtils.printSubSection(writer, "Colonization Suitability");
        ReportUtils.printSortedTable(writer, COLONIZATION_SUITABILITIES, habAssessmentCount, "Suitability");

        ReportUtils.printSubSection(writer, "Terraforming Potential");
        ReportUtils.printSortedTable(writer, TERRAFORMING_POTENTIALS, habAssessmentCount, "Potential");

        ReportUtils.printSubSection(writer, "Biosignature Potential");
        ReportUtils.printSortedTable(writer, BIOSIGNATURE_POTENTIALS, habAssessmentCount, "Potential");

        ReportUtils.printSubSection(writer, "Life Complexity Potential");
        ReportUtils.printSortedTable(writer, LIFE_COMPLEXITY_POTENTIALS, habAssessmentCount, "Potential");

        ReportUtils.endCollapsible(writer);
    }

    private static void printWeatherData(PrintWriter writer) {
        WeatherBucket a = WEATHER_ALL;
        writer.println("---");
        ReportUtils.beginCollapsible(writer, "Planetary Weather", 2);
        writer.println("Planets with weather data: " + a.count);
        writer.println("- Surface planets: " + WEATHER_SURFACE.count);
        writer.println("- Gas / Ice Giant planets: " + WEATHER_GAS.count);
        if (a.count == 0) {
            ReportUtils.endCollapsible(writer);
            return;
        }
        writer.println("");

        ReportUtils.endCollapsible(writer);

        // Surface Planet Weather
        if (WEATHER_SURFACE.count > 0) {
            writer.println("---");
            printWeatherBucket(writer, WEATHER_SURFACE, "Surface Planet Weather");
        }

        // Gas / Ice Giant Weather
        if (WEATHER_GAS.count > 0) {
            writer.println("---");
            printWeatherBucket(writer, WEATHER_GAS, "Gas / Ice Giant Weather");
        }
    }

    private static void printWeatherBucket(PrintWriter writer, WeatherBucket b, String title) {
        ReportUtils.beginCollapsible(writer, title, 2);
        writer.println("Planets with weather data: " + b.count);
        if (b.count == 0) {
            ReportUtils.endCollapsible(writer);
            return;
        }

        writer.println("- With precipitation: " + b.withPrecipitation
                + " (" + ReportUtils.pct(b.withPrecipitation, b.count) + "%)");
        writer.println("- With dust storms: " + b.withDustStorms
                + " (" + ReportUtils.pct(b.withDustStorms, b.count) + "%)");
        writer.println("- With lightning: " + b.withLightning
                + " (" + ReportUtils.pct(b.withLightning, b.count) + "%)");
        writer.println("- With super-rotation: " + b.withSuperRotation
                + " (" + ReportUtils.pct(b.withSuperRotation, b.count) + "%)");
        writer.println("- With great dark spot: " + b.withGreatDarkSpot
                + " (" + ReportUtils.pct(b.withGreatDarkSpot, b.count) + "%)");
        writer.println("- Avg cloud coverage: " + String.format("%.1f", b.totalCloudCoverage / b.count) + "%");
        if (b.windSpeedCount > 0) {
            writer.println("- Avg surface wind speed: " + String.format("%.1f", b.totalWindSpeed / b.windSpeedCount) + " m/s");
        }
        writer.println("- Avg cloud layers/planet: " + String.format("%.1f", b.cloudLayerTotal * 1.0 / b.count));
        writer.println("- Avg precipitation types/planet: " + String.format("%.1f", b.precipTypeTotal * 1.0 / b.count));
        writer.println("- Total extreme weather events: " + b.extremeEventTotal
                + " (avg " + String.format("%.1f", b.extremeEventTotal * 1.0 / b.count) + "/planet)");
        writer.println("- Total eclipse configurations: " + b.eclipseTotal);
        writer.println("");

        ReportUtils.printSubSection(writer, "Sky Color");
        ReportUtils.printSortedTable(writer, b.skyColors, b.count, "Sky Color");

        ReportUtils.printSubSection(writer, "Cloud Coverage Class");
        ReportUtils.printSortedTable(writer, b.cloudCoverageClass, b.count, "Coverage");

        ReportUtils.printSubSection(writer, "Wind Intensity");
        ReportUtils.printSortedTable(writer, b.windIntensity, b.count, "Intensity");

        ReportUtils.printSubSection(writer, "Circulation Pattern");
        ReportUtils.printSortedTable(writer, b.circulationPattern, b.count, "Pattern");

        ReportUtils.printSubSection(writer, "Storm Frequency");
        ReportUtils.printSortedTable(writer, b.stormFrequency, b.count, "Frequency");

        if (!b.lightningType.isEmpty()) {
            ReportUtils.printSubSection(writer, "Lightning Type");
            ReportUtils.printSortedTable(writer, b.lightningType, b.withLightning, "Type");
        }

        ReportUtils.printSubSection(writer, "Weather Severity");
        ReportUtils.printSortedTable(writer, b.severity, b.count, "Severity");

        ReportUtils.printSubSection(writer, "Outdoor Exposure Rating");
        ReportUtils.printSortedTable(writer, b.exposureRating, b.count, "Rating");

        if (!b.tidalRangeBins.isEmpty()) {
            ReportUtils.printSubSection(writer, "Tidal Range Distribution");
            ReportUtils.printSortedTableByKey(writer, b.tidalRangeBins, b.count, "Tidal Range");
        }

        ReportUtils.endCollapsible(writer);
    }

    private static void printPerTypeBreakdown(PrintWriter writer) {
        writer.println("---");
        ReportUtils.beginCollapsible(writer, "Per-Planet-Type Breakdown", 2);
        writer.println("*Detailed breakdown of key properties for each planet type*");
        writer.println("");

        // Print in order of count (most common first)
        PER_TYPE_DATA.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().getCount(), a.getValue().getCount()))
                .forEach(entry -> entry.getValue().print(writer, entry.getKey()));

        ReportUtils.endCollapsible(writer);
    }

    private static String binMass(double earthMass) {
        if (earthMass < 0.01) return "01: <0.01 (asteroid-class)";
        if (earthMass < 0.1) return "02: 0.01-0.1 (sub-dwarf)";
        if (earthMass < 0.5) return "03: 0.1-0.5 (sub-Earth)";
        if (earthMass < 2.0) return "04: 0.5-2.0 (Earth-class)";
        if (earthMass < 10) return "05: 2-10 (super-Earth)";
        if (earthMass < 50) return "06: 10-50 (Neptune-class)";
        if (earthMass < 300) return "07: 50-300 (sub-Jupiter)";
        if (earthMass < 1000) return "08: 300-1000 (Jupiter-class)";
        return "09: 1000+ (super-Jupiter)";
    }

    private static String binMoonCount(int count) {
        if (count == 0) return "0";
        if (count <= 2) return "1-2";
        if (count <= 5) return "3-5";
        if (count <= 10) return "6-10";
        if (count <= 25) return "11-25";
        if (count <= 50) return "26-50";
        return "51+";
    }

    private static boolean isRockyType(String planetType) {
        if (planetType == null) return false;
        return planetType.contains("Terrestrial") || planetType.contains("Super-Earth")
                || planetType.contains("Rocky") || planetType.contains("Desert")
                || planetType.contains("Ocean") || planetType.contains("Lava")
                || planetType.contains("Carbon") || planetType.contains("Iron");
    }

    private static boolean isSurfaceType(String planetType) {
        if (planetType == null) return false;
        return planetType.contains("Terrestrial") || planetType.contains("Super-Earth")
                || planetType.contains("Rocky") || planetType.contains("Desert")
                || planetType.contains("Ocean") || planetType.contains("Lava")
                || planetType.contains("Carbon") || planetType.contains("Iron")
                || planetType.contains("Ice World") || planetType.contains("Dwarf")
                || planetType.contains("Hot Rocky");
    }

    private static String binMagnetopause(double radii) {
        if (radii < 3) return "a: <3 (extreme compression)";
        if (radii < 6) return "b: 3-6 (heavy compression)";
        if (radii < 10) return "c: 6-10 (moderate compression)";
        if (radii < 20) return "d: 10-20 (Earth-like)";
        if (radii < 40) return "e: 20-40 (expanded)";
        if (radii < 70) return "f: 40-70 (large)";
        return "g: 70+ (massive)";
    }

    private static String binLossRate(double rate) {
        if (rate < 0.3) return "a: <0.3 (very low)";
        if (rate < 1.0) return "b: 0.3-1.0 (low)";
        if (rate < 3.0) return "c: 1.0-3.0 (moderate)";
        if (rate < 8.0) return "d: 3.0-8.0 (high)";
        if (rate < 15.0) return "e: 8.0-15.0 (very high)";
        return "f: 15+ (extreme)";
    }

    private static String binDistance(double distanceAU) {
        if (distanceAU < 0.1) return "a: <0.1 AU";
        if (distanceAU < 0.3) return "b: 0.1-0.3 AU";
        if (distanceAU < 1.0) return "c: 0.3-1.0 AU";
        if (distanceAU < 3.0) return "d: 1.0-3.0 AU";
        if (distanceAU < 10.0) return "e: 3.0-10.0 AU";
        return "f: 10+ AU";
    }

    private static String binTemperature(double tempK) {
        if (tempK < 50) return "01: <50K (ultra-cold)";
        if (tempK < 150) return "02: 50-150K (cryogenic)";
        if (tempK < 273) return "03: 150-273K (sub-freezing)";
        if (tempK < 373) return "04: 273-373K (liquid water)";
        if (tempK < 700) return "05: 373-700K (hot)";
        if (tempK < 1500) return "06: 700-1500K (very hot)";
        return "07: 1500K+ (extreme)";
    }

    private static boolean isRockyTypeWithAtmospherePotential(String planetType) {
        if (planetType == null) return false;
        // Exclude types where NONE is a common natural template outcome
        return planetType.contains("Terrestrial") || planetType.contains("Super-Earth")
                || planetType.contains("Desert") || planetType.contains("Ocean")
                || planetType.contains("Lava");
        // Excludes: Iron Planet, Carbon Planet, Hot Rocky Planet, Dwarf Planet
    }

    // ═══════════════════════════════════════════════════════════════
    //  HTML OUTPUT
    // ═══════════════════════════════════════════════════════════════

    static void printHtml(PrintWriter w, ProbabilityCounts counts) {
        w.println("<hr>");
        HtmlReportUtils.beginCollapsible(w, "Planet Types", 2);

        // Summary stats
        w.println("<p><strong>Summary:</strong> " + counts.getPlanetCount() + " planets across "
                + counts.getSystemCount() + " systems (avg "
                + String.format("%.1f", counts.getPlanetCount() * 1.0 / Math.max(1, counts.getSystemCount()))
                + " per system)</p>");
        w.println("<p>With rings: " + planetsWithRings + " ("
                + HtmlReportUtils.pct(planetsWithRings, counts.getPlanetCount()) + "%)</p>");
        if (physicalPropsCount > 0) {
            w.println("<p>Avg mass: " + String.format("%.2f", totalMass / physicalPropsCount) + " M&#8853;"
                    + " | Avg radius: " + String.format("%.2f", totalRadius / physicalPropsCount) + " R&#8853;"
                    + " | Avg gravity: " + String.format("%.2f", totalGravity / physicalPropsCount) + " g</p>");
        }

        HtmlReportUtils.printLinkedTable(w, PLANET_TYPES, counts.getPlanetCount(), "Planet Type");

        HtmlReportUtils.printSubSection(w, "Composition Classification");
        HtmlReportUtils.printSortedTable(w, COMPOSITION_CLASSES, counts.getPlanetCount(), "Classification");

        HtmlReportUtils.printSubSection(w, "Surface Temperature Distribution");
        HtmlReportUtils.printSortedTableByKey(w, SURFACE_TEMP_BINS, counts.getPlanetCount(), "Temperature Range");

        HtmlReportUtils.endCollapsible(w);

        printHtmlPerTypeBreakdown(w);
        printHtmlAtmosphereData(w, counts);
        printHtmlGeologyData(w);
        printHtmlWaterAndHab(w);
        printHtmlWeatherData(w);
    }

    private static void printHtmlAtmosphereData(PrintWriter w, ProbabilityCounts counts) {
        w.println("<hr>");
        HtmlReportUtils.beginCollapsible(w, "Atmosphere & Magnetic Fields", 2);

        HtmlReportUtils.printSubSection(w, "Atmosphere Classifications (All Planets)");
        HtmlReportUtils.printSortedTable(w, ATMOSPHERE_CLASSIFICATIONS, counts.getPlanetCount(), "Classification");

        HtmlReportUtils.printSubSection(w, "Tidal Locking");
        HtmlReportUtils.printSortedTable(w, TIDAL_LOCK_COUNTS, counts.getPlanetCount(), "Status");

        HtmlReportUtils.printSubSection(w, "Habitable Zone Positions");
        HtmlReportUtils.printSortedTable(w, HZ_POSITIONS, counts.getPlanetCount(), "Position");

        HtmlReportUtils.printSubSection(w, "Magnetic Protection Levels");
        HtmlReportUtils.printSortedTable(w, PROTECTION_LEVELS, counts.getPlanetCount(), "Level");

        HtmlReportUtils.printSubSection(w, "Magnetopause Distance Distribution");
        HtmlReportUtils.printSortedTableByKey(w, MAGNETOPAUSE_BINS, counts.getPlanetCount(), "Range (planet radii)");

        // Average Magnetopause by Distance
        w.println("<h3>Average Magnetopause by Distance from Star</h3>");
        w.println("<p class=\"note\">Should show compression (smaller magnetopause) closer to star</p>");
        w.println("<table>");
        w.println("<thead><tr><th>Distance Bin</th><th>Avg Magnetopause (radii)</th><th>Sample Count</th></tr></thead>");
        w.println("<tbody>");
        DISTANCE_VS_MAGNETOPAUSE.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    double avg = e.getValue()[0] / e.getValue()[1];
                    w.println("<tr><td>" + HtmlReportUtils.esc(e.getKey()) + "</td><td>"
                            + String.format("%.1f", avg) + "</td><td>" + (int) e.getValue()[1] + "</td></tr>");
                });
        w.println("</tbody></table>");

        HtmlReportUtils.printSubSection(w, "Atmospheric Loss Rate Distribution");
        HtmlReportUtils.printSortedTableByKey(w, ATM_LOSS_RATE_BINS, counts.getPlanetCount(), "Range");

        HtmlReportUtils.printSubSection(w, "Auroral Frequency");
        HtmlReportUtils.printSortedTable(w, AURORAL_FREQUENCIES, counts.getPlanetCount(), "Frequency");

        HtmlReportUtils.printSubSection(w, "Auroral Intensity");
        HtmlReportUtils.printSortedTable(w, AURORAL_INTENSITIES, counts.getPlanetCount(), "Intensity");

        // Radiation Belt Intensity
        w.println("<h3>Radiation Belt Intensity (Inner / Outer)</h3>");
        w.println("<table>");
        w.println("<thead><tr><th>Inner Belt</th><th>Count</th><th>%</th></tr></thead><tbody>");
        BELT_INTENSITY_INNER.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> w.println("<tr><td>" + HtmlReportUtils.esc(e.getKey()) + "</td><td>" + e.getValue()
                        + "</td><td>" + HtmlReportUtils.pct(e.getValue(), counts.getPlanetCount()) + "%</td></tr>"));
        w.println("</tbody></table>");

        w.println("<table>");
        w.println("<thead><tr><th>Outer Belt</th><th>Count</th><th>%</th></tr></thead><tbody>");
        BELT_INTENSITY_OUTER.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> w.println("<tr><td>" + HtmlReportUtils.esc(e.getKey()) + "</td><td>" + e.getValue()
                        + "</td><td>" + HtmlReportUtils.pct(e.getValue(), counts.getPlanetCount()) + "%</td></tr>"));
        w.println("</tbody></table>");

        // Cross-reference: Star Activity vs Atmosphere Stripping
        w.println("<h3>Star Activity vs Rocky Planet Atmosphere Stripping</h3>");
        w.println("<p class=\"note\">Shows % of rocky planets that lost their atmosphere, grouped by parent star activity</p>");
        w.println("<table class=\"xref-table\">");
        w.println("<thead><tr><th>Star Activity</th><th>Total Rocky</th><th>Stripped (NONE)</th><th>Strip Rate</th></tr></thead>");
        w.println("<tbody>");
        ACTIVITY_VS_ATMOSPHERE.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    int total = e.getValue()[0];
                    int stripped = e.getValue()[1];
                    double rate = total > 0 ? (stripped * 100.0 / total) : 0;
                    w.println("<tr><td>" + HtmlReportUtils.esc(e.getKey()) + "</td><td>" + total
                            + "</td><td>" + stripped + "</td><td>" + String.format("%.1f", rate) + "%</td></tr>");
                });
        w.println("</tbody></table>");

        // Cross-reference: Star Activity vs Protection Level
        w.println("<h3>Star Activity vs Protection Level (Rocky Planets)</h3>");
        w.println("<p class=\"note\">Expectation: HYPERACTIVE/VERY_ACTIVE stars should shift protection toward NONE/MINIMAL</p>");
        w.println("<table class=\"xref-table\">");
        w.println("<thead><tr><th>Star Activity</th><th>Total</th><th>NONE</th><th>MINIMAL</th><th>MODERATE</th><th>STRONG</th><th>EXCEPTIONAL</th></tr></thead>");
        w.println("<tbody>");
        ACTIVITY_VS_PROTECTION.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    int[] c = e.getValue();
                    w.println("<tr><td>" + HtmlReportUtils.esc(e.getKey()) + "</td><td>" + c[0] + "</td><td>" + c[1]
                            + "</td><td>" + c[2] + "</td><td>" + c[3] + "</td><td>" + c[4] + "</td><td>" + c[5] + "</td></tr>");
                });
        w.println("</tbody></table>");

        HtmlReportUtils.endCollapsible(w);
    }

    private static void printHtmlGeologyData(PrintWriter w) {
        if (GEOLOGICAL_ACTIVITY.isEmpty() && TECTONIC_LEVELS.isEmpty()) return;

        w.println("<hr>");
        HtmlReportUtils.beginCollapsible(w, "Geology (Rocky/Surface Planets)", 2);
        w.println("<p>Planets with geological data: " + planetsWithGeology + "</p>");

        if (!GEOLOGICAL_ACTIVITY.isEmpty()) {
            HtmlReportUtils.printSubSection(w, "Geological Activity");
            HtmlReportUtils.printSortedTable(w, GEOLOGICAL_ACTIVITY, planetsWithGeology, "Activity");
        }
        if (!TECTONIC_LEVELS.isEmpty()) {
            HtmlReportUtils.printSubSection(w, "Tectonic Activity Level");
            HtmlReportUtils.printSortedTable(w, TECTONIC_LEVELS, planetsWithGeology, "Level");
        }
        if (!VOLCANISM_TYPES.isEmpty()) {
            HtmlReportUtils.printSubSection(w, "Volcanism Type");
            HtmlReportUtils.printSortedTable(w, VOLCANISM_TYPES, planetsWithGeology, "Type");
        }

        HtmlReportUtils.endCollapsible(w);
    }

    private static void printHtmlWaterAndHab(PrintWriter w) {
        // Water System
        w.println("<hr>");
        HtmlReportUtils.beginCollapsible(w, "Water System (Rocky/Surface Planets Only)", 2);
        w.println("<p>Total rocky/surface planets analyzed: " + totalRockyPlanets + "</p>");
        w.println("<p>With liquid surface water: " + planetsWithLiquidWater
                + " (" + HtmlReportUtils.pct(planetsWithLiquidWater, totalRockyPlanets) + "%)</p>");
        w.println("<p>With ice coverage: " + planetsWithIce
                + " (" + HtmlReportUtils.pct(planetsWithIce, totalRockyPlanets) + "%)</p>");
        w.println("<p>With subsurface water: " + planetsWithSubsurfaceWater
                + " (" + HtmlReportUtils.pct(planetsWithSubsurfaceWater, totalRockyPlanets) + "%)</p>");

        HtmlReportUtils.printSubSection(w, "Water Inventory Distribution");
        HtmlReportUtils.printSortedTable(w, WATER_INVENTORIES, totalRockyPlanets, "Inventory");

        HtmlReportUtils.printSubSection(w, "Water Phase at Surface");
        HtmlReportUtils.printSortedTable(w, WATER_PHASES, habAssessmentCount, "Phase");

        HtmlReportUtils.endCollapsible(w);

        // Habitability
        w.println("<hr>");
        HtmlReportUtils.beginCollapsible(w, "Planetary Habitability", 2);
        w.println("<p>Planets assessed: " + habAssessmentCount + "</p>");
        w.println("<p>Average ESI: " + String.format("%.3f", esiSum / Math.max(1, habAssessmentCount)) + "</p>");
        w.println("<p>Average Habitability Score: " + String.format("%.1f", habScoreSum / Math.max(1, habAssessmentCount)) + "</p>");
        w.println("<p>Breathable atmospheres: " + breathablePlanets
                + " (" + String.format("%.2f", breathablePlanets * 100.0 / Math.max(1, habAssessmentCount)) + "%)</p>");

        HtmlReportUtils.printSubSection(w, "Habitability Class Distribution");
        HtmlReportUtils.printSortedTable(w, HABITABILITY_CLASSES, habAssessmentCount, "Class");

        HtmlReportUtils.printSubSection(w, "Colonization Suitability");
        HtmlReportUtils.printSortedTable(w, COLONIZATION_SUITABILITIES, habAssessmentCount, "Suitability");

        HtmlReportUtils.printSubSection(w, "Terraforming Potential");
        HtmlReportUtils.printSortedTable(w, TERRAFORMING_POTENTIALS, habAssessmentCount, "Potential");

        HtmlReportUtils.printSubSection(w, "Biosignature Potential");
        HtmlReportUtils.printSortedTable(w, BIOSIGNATURE_POTENTIALS, habAssessmentCount, "Potential");

        HtmlReportUtils.printSubSection(w, "Life Complexity Potential");
        HtmlReportUtils.printSortedTable(w, LIFE_COMPLEXITY_POTENTIALS, habAssessmentCount, "Potential");

        HtmlReportUtils.endCollapsible(w);
    }

    private static void printHtmlWeatherData(PrintWriter w) {
        w.println("<hr>");
        HtmlReportUtils.beginCollapsible(w, "Planetary Weather", 2);

        if (WEATHER_ALL.count == 0) {
            w.println("<p>No planets with weather data.</p>");
            HtmlReportUtils.endCollapsible(w);
            return;
        }

        // Overall summary stat cards
        WeatherBucket a = WEATHER_ALL;
        HtmlReportUtils.beginStatGrid(w);
        HtmlReportUtils.statCard(w, String.valueOf(a.count), "Total");
        HtmlReportUtils.statCard(w, String.valueOf(WEATHER_SURFACE.count), "Surface");
        HtmlReportUtils.statCard(w, String.valueOf(WEATHER_GAS.count), "Gas / Ice");
        HtmlReportUtils.statCard(w, String.format("%.1f%%", a.totalCloudCoverage / a.count), "Avg Cloud Cover");
        HtmlReportUtils.statCard(w, a.windSpeedCount > 0 ? String.format("%.1f m/s", a.totalWindSpeed / a.windSpeedCount) : "N/A", "Avg Wind Speed");
        HtmlReportUtils.statCard(w, String.valueOf(a.extremeEventTotal), "Extreme Events");
        HtmlReportUtils.endStatGrid(w);

        // ── Surface Planet Weather ──
        if (WEATHER_SURFACE.count > 0) {
            printHtmlWeatherBucket(w, WEATHER_SURFACE,
                    "Surface Planet Weather", "surface-planet-weather");
        }

        // ── Gas / Ice Giant Weather ──
        if (WEATHER_GAS.count > 0) {
            printHtmlWeatherBucket(w, WEATHER_GAS,
                    "Gas / Ice Giant Weather", "gas-ice-giant-weather");
        }

        HtmlReportUtils.endCollapsible(w);
    }

    /**
     * Prints a complete weather sub-section for a single WeatherBucket.
     */
    private static void printHtmlWeatherBucket(PrintWriter w, WeatherBucket b,
                                               String title, String anchorId) {
        w.println("<hr>");
        HtmlReportUtils.beginCollapsible(w, title, 3);

        // Stat cards for this bucket
        HtmlReportUtils.beginStatGrid(w);
        HtmlReportUtils.statCard(w, String.valueOf(b.count), "Planets");
        HtmlReportUtils.statCard(w, b.withPrecipitation + " (" + HtmlReportUtils.pct(b.withPrecipitation, b.count) + "%)", "Precipitation");
        HtmlReportUtils.statCard(w, b.withDustStorms + " (" + HtmlReportUtils.pct(b.withDustStorms, b.count) + "%)", "Dust Storms");
        HtmlReportUtils.statCard(w, b.withLightning + " (" + HtmlReportUtils.pct(b.withLightning, b.count) + "%)", "Lightning");
        HtmlReportUtils.statCard(w, b.withSuperRotation + " (" + HtmlReportUtils.pct(b.withSuperRotation, b.count) + "%)", "Super-Rotation");
        HtmlReportUtils.statCard(w, b.withGreatDarkSpot + " (" + HtmlReportUtils.pct(b.withGreatDarkSpot, b.count) + "%)", "Great Dark Spot");
        HtmlReportUtils.statCard(w, String.format("%.1f%%", b.totalCloudCoverage / b.count), "Avg Cloud Cover");
        if (b.windSpeedCount > 0) {
            HtmlReportUtils.statCard(w, String.format("%.1f m/s", b.totalWindSpeed / b.windSpeedCount), "Avg Wind Speed");
        }
        HtmlReportUtils.statCard(w, String.format("%.1f", b.cloudLayerTotal * 1.0 / b.count), "Avg Cloud Layers");
        HtmlReportUtils.statCard(w, String.format("%.1f", b.precipTypeTotal * 1.0 / b.count), "Avg Precip Types");
        HtmlReportUtils.statCard(w, b.extremeEventTotal + " (avg " + String.format("%.1f", b.extremeEventTotal * 1.0 / b.count) + ")", "Extreme Events");
        HtmlReportUtils.statCard(w, String.valueOf(b.eclipseTotal), "Eclipse Configs");
        HtmlReportUtils.endStatGrid(w);

        // Tables
        HtmlReportUtils.printSubSection(w, "Sky Color");
        HtmlReportUtils.printSortedTable(w, b.skyColors, b.count, "Sky Color");

        HtmlReportUtils.printSubSection(w, "Cloud Coverage Class");
        HtmlReportUtils.printSortedTable(w, b.cloudCoverageClass, b.count, "Coverage");

        HtmlReportUtils.printSubSection(w, "Wind Intensity");
        HtmlReportUtils.printSortedTable(w, b.windIntensity, b.count, "Intensity");

        HtmlReportUtils.printSubSection(w, "Circulation Pattern");
        HtmlReportUtils.printSortedTable(w, b.circulationPattern, b.count, "Pattern");

        HtmlReportUtils.printSubSection(w, "Storm Frequency");
        HtmlReportUtils.printSortedTable(w, b.stormFrequency, b.count, "Frequency");

        if (!b.lightningType.isEmpty()) {
            HtmlReportUtils.printSubSection(w, "Lightning Type");
            HtmlReportUtils.printSortedTable(w, b.lightningType, b.withLightning, "Type");
        }

        HtmlReportUtils.printSubSection(w, "Weather Severity");
        HtmlReportUtils.printSortedTable(w, b.severity, b.count, "Severity");

        HtmlReportUtils.printSubSection(w, "Outdoor Exposure Rating");
        HtmlReportUtils.printSortedTable(w, b.exposureRating, b.count, "Rating");

        if (!b.tidalRangeBins.isEmpty()) {
            HtmlReportUtils.printSubSection(w, "Tidal Range Distribution");
            HtmlReportUtils.printSortedTableByKey(w, b.tidalRangeBins, b.count, "Tidal Range");
        }

        HtmlReportUtils.endCollapsible(w);
    }

    private static void printHtmlPerTypeBreakdown(PrintWriter w) {
        w.println("<hr>");
        HtmlReportUtils.beginCollapsible(w, "Per-Planet-Type Breakdown", 2);
        w.println("<p class=\"note\">Detailed breakdown of key properties for each planet type</p>");

        PER_TYPE_DATA.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().getCount(), a.getValue().getCount()))
                .forEach(entry -> entry.getValue().printHtml(w, entry.getKey()));

        HtmlReportUtils.endCollapsible(w);
    }
}