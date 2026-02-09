package com.brickroad.starcreator_webservice.ProbabilityReport;

import com.brickroad.starcreator_webservice.entity.ud.*;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class PlanetData {

    private static final Map<String, Integer> PLANET_TYPES = new HashMap<>();
    private static final Map<String, Integer> ATMOSPHERE_CLASSIFICATIONS = new HashMap<>();
    private static final Map<String, Integer> PROTECTION_LEVELS = new HashMap<>();
    private static final Map<String, Integer> AURORAL_FREQUENCIES = new HashMap<>();
    private static final Map<String, Integer> AURORAL_INTENSITIES = new HashMap<>();
    private static final Map<String, Integer> TIDAL_LOCK_COUNTS = new HashMap<>(); // "LOCKED" / "NOT_LOCKED"
    private static final Map<String, Integer> HZ_POSITIONS = new HashMap<>();

    private static final Map<String, Integer> MAGNETOPAUSE_BINS = new HashMap<>();
    private static final Map<String, Integer> ATM_LOSS_RATE_BINS = new HashMap<>();
    private static final Map<String, Integer> BELT_INTENSITY_INNER = new HashMap<>();
    private static final Map<String, Integer> BELT_INTENSITY_OUTER = new HashMap<>();

    private static final Map<String, int[]> ACTIVITY_VS_ATMOSPHERE = new HashMap<>(); // [total_rocky, stripped_count]
    private static final Map<String, int[]> ACTIVITY_VS_PROTECTION = new HashMap<>(); // [total, none, minimal, moderate, strong, exceptional]
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

    static void analyzeData(Planet planet, ProbabilityCounts counts) {
        PLANET_TYPES.put(planet.getPlanetType(), PLANET_TYPES.getOrDefault(planet.getPlanetType(), 0) + 1);

        // --- Stellar Activity Integration Data ---

        // Atmosphere classification
        String atmClass = planet.getAtmosphereClassification() != null ? planet.getAtmosphereClassification() : "NULL";
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
            // Protection level
            String protection = mf.getProtectionLevel() != null ? mf.getProtectionLevel().name() : "NULL";
            PROTECTION_LEVELS.put(protection, PROTECTION_LEVELS.getOrDefault(protection, 0) + 1);

            // Auroral frequency
            String auroraFreq = mf.getAuroralFrequency() != null ? mf.getAuroralFrequency().name() : "NONE";
            AURORAL_FREQUENCIES.put(auroraFreq, AURORAL_FREQUENCIES.getOrDefault(auroraFreq, 0) + 1);

            // Auroral intensity
            String auroraInt = mf.getAuroralIntensity() != null ? mf.getAuroralIntensity().name() : "NONE";
            AURORAL_INTENSITIES.put(auroraInt, AURORAL_INTENSITIES.getOrDefault(auroraInt, 0) + 1);

            // Radiation belt intensity
            String innerBelt = mf.getInnerBeltIntensity() != null ? mf.getInnerBeltIntensity().name() : "NULL";
            BELT_INTENSITY_INNER.put(innerBelt, BELT_INTENSITY_INNER.getOrDefault(innerBelt, 0) + 1);
            String outerBelt = mf.getOuterBeltIntensity() != null ? mf.getOuterBeltIntensity().name() : "NULL";
            BELT_INTENSITY_OUTER.put(outerBelt, BELT_INTENSITY_OUTER.getOrDefault(outerBelt, 0) + 1);

            // Magnetopause distance binned
            if (mf.getMagnetopauseDistancePlanetRadii() != null) {
                String mpBin = binMagnetopause(mf.getMagnetopauseDistancePlanetRadii());
                MAGNETOPAUSE_BINS.put(mpBin, MAGNETOPAUSE_BINS.getOrDefault(mpBin, 0) + 1);
            }

            // Atmospheric loss rate binned
            if (mf.getAtmosphericLossRateFactor() != null) {
                String lossBin = binLossRate(mf.getAtmosphericLossRateFactor());
                ATM_LOSS_RATE_BINS.put(lossBin, ATM_LOSS_RATE_BINS.getOrDefault(lossBin, 0) + 1);
            }

            // Distance vs magnetopause (for compression analysis)
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
        if (parentStar != null && isRockyType(planet.getPlanetType())) {
            String actLevel = parentStar.getActivityLevel() != null ? parentStar.getActivityLevel() : "UNKNOWN";

            int[] atmCounts = ACTIVITY_VS_ATMOSPHERE.getOrDefault(actLevel, new int[]{0, 0});
            atmCounts[0]++; // total rocky planets
            if ("NONE".equals(planet.getAtmosphereClassification())) {
                atmCounts[1]++; // stripped
            }
            ACTIVITY_VS_ATMOSPHERE.put(actLevel, atmCounts);

            // Activity vs protection level
            if (mf != null && mf.getProtectionLevel() != null) {
                // [total, NONE, MINIMAL, MODERATE, STRONG, EXCEPTIONAL]
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

        // Existing moon/ring tracking
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
    }

    private static void analyzeWaterData(Planet planet) {
        String type = planet.getPlanetType();
        if (type == null) return;

        // Only track water for rocky/surface bodies
        boolean isRocky = type.contains("Terrestrial") || type.contains("Super-Earth")
                || type.contains("Desert") || type.contains("Ocean")
                || type.contains("Iron") || type.contains("Carbon")
                || type.contains("Lava") || type.contains("Hot Rocky")
                || type.contains("Ice World") || type.contains("Dwarf");

        if (!isRocky) return;
        totalRockyPlanets++;

        String inventory = planet.getWaterInventory() != null ? planet.getWaterInventory() : "NULL";
        WATER_INVENTORIES.put(inventory, WATER_INVENTORIES.getOrDefault(inventory, 0) + 1);

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

        // Track water phase from habitability assessment
        String waterPhase = hab.getWaterPhaseAtSurface() != null ? hab.getWaterPhaseAtSurface() : "NULL";
        WATER_PHASES.put(waterPhase, WATER_PHASES.getOrDefault(waterPhase, 0) + 1);
    }

    static void printData(PrintWriter writer, ProbabilityCounts counts) {
        writer.println("---");
        writer.println("## Planet Types");
        writer.println("");
        writer.println("| Planet Type | Count | % |");
        writer.println("| --- | --- | --- |");
        PLANET_TYPES.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> writer.println("| " + entry.getKey() + " | " + entry.getValue() + " | " + (entry.getValue() * 100.0) / counts.getPlanetCount() + "% |"));

        writer.println("---");
        printAtmosphereData(writer, counts);
        writer.println("---");
        printWaterAndHab(writer);
    }

    private static void printAtmosphereData(PrintWriter writer, ProbabilityCounts counts) {
        // --- Atmosphere Classifications ---
        writer.println("### Atmosphere Classifications (All Planets)");
        writer.println("");
        writer.println("| Classification | Count | % |");
        writer.println("| --- | --- | --- |");
        ATMOSPHERE_CLASSIFICATIONS.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> writer.println("| " + e.getKey() + " | " + e.getValue() + " | " +
                        String.format("%.1f", e.getValue() * 100.0 / counts.getPlanetCount()) + "% |"));
        writer.println("");

        // --- Tidal Locking ---
        writer.println("### Tidal Locking");
        writer.println("");
        writer.println("| Status | Count | % |");
        writer.println("| --- | --- | --- |");
        TIDAL_LOCK_COUNTS.forEach((k, v) -> writer.println("| " + k + " | " + v + " | " +
                String.format("%.1f", v * 100.0 / counts.getPlanetCount()) + "% |"));
        writer.println("");

        // --- Habitable Zone Positions ---
        writer.println("### Habitable Zone Positions");
        writer.println("");
        writer.println("| Position | Count | % |");
        writer.println("| --- | --- | --- |");
        HZ_POSITIONS.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> writer.println("| " + e.getKey() + " | " + e.getValue() + " | " +
                        String.format("%.1f", e.getValue() * 100.0 / counts.getPlanetCount()) + "% |"));
        writer.println("");

        // --- Protection Levels ---
        writer.println("### Magnetic Protection Levels");
        writer.println("");
        writer.println("| Level | Count | % |");
        writer.println("| --- | --- | --- |");
        PROTECTION_LEVELS.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> writer.println("| " + e.getKey() + " | " + e.getValue() + " | " +
                        String.format("%.1f", e.getValue() * 100.0 / counts.getPlanetCount()) + "% |"));
        writer.println("");

        // --- Magnetopause Distance Distribution ---
        writer.println("### Magnetopause Distance Distribution");
        writer.println("");
        writer.println("| Range (planet radii) | Count | % |");
        writer.println("| --- | --- | --- |");
        MAGNETOPAUSE_BINS.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> writer.println("| " + e.getKey() + " | " + e.getValue() + " | " +
                        String.format("%.1f", e.getValue() * 100.0 / counts.getPlanetCount()) + "% |"));
        writer.println("");

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
                    writer.println("| " + e.getKey() + " | " + String.format("%.1f", avg) + " | " +
                            (int) e.getValue()[1] + " |");
                });
        writer.println("");

        // --- Atmospheric Loss Rate Distribution ---
        writer.println("### Atmospheric Loss Rate Distribution");
        writer.println("");
        writer.println("| Range | Count | % |");
        writer.println("| --- | --- | --- |");
        ATM_LOSS_RATE_BINS.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> writer.println("| " + e.getKey() + " | " + e.getValue() + " | " +
                        String.format("%.1f", e.getValue() * 100.0 / counts.getPlanetCount()) + "% |"));
        writer.println("");

        // --- Auroral Frequency ---
        writer.println("### Auroral Frequency");
        writer.println("");
        writer.println("| Frequency | Count | % |");
        writer.println("| --- | --- | --- |");
        AURORAL_FREQUENCIES.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> writer.println("| " + e.getKey() + " | " + e.getValue() + " | " +
                        String.format("%.1f", e.getValue() * 100.0 / counts.getPlanetCount()) + "% |"));
        writer.println("");

        // --- Auroral Intensity ---
        writer.println("### Auroral Intensity");
        writer.println("");
        writer.println("| Intensity | Count | % |");
        writer.println("| --- | --- | --- |");
        AURORAL_INTENSITIES.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> writer.println("| " + e.getKey() + " | " + e.getValue() + " | " +
                        String.format("%.1f", e.getValue() * 100.0 / counts.getPlanetCount()) + "% |"));
        writer.println("");

        // --- Radiation Belt Intensity ---
        writer.println("### Radiation Belt Intensity (Inner / Outer)");
        writer.println("");
        writer.println("| Inner Belt | Count | % |");
        writer.println("| --- | --- | --- |");
        BELT_INTENSITY_INNER.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> writer.println("| " + e.getKey() + " | " + e.getValue() + " | " +
                        String.format("%.1f", e.getValue() * 100.0 / counts.getPlanetCount()) + "% |"));
        writer.println("");
        writer.println("| Outer Belt | Count | % |");
        writer.println("| --- | --- | --- |");
        BELT_INTENSITY_OUTER.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> writer.println("| " + e.getKey() + " | " + e.getValue() + " | " +
                        String.format("%.1f", e.getValue() * 100.0 / counts.getPlanetCount()) + "% |"));
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
    }

    private static void printWaterAndHab(PrintWriter writer) {
        // ============================================================
        // WATER SYSTEM
        // ============================================================
        writer.println("## Water System (Rocky/Surface Planets Only)");
        writer.println("");
        writer.println("Total rocky/surface planets analyzed: " + totalRockyPlanets);
        writer.println("- With liquid surface water: " + planetsWithLiquidWater
                + " (" + String.format("%.1f", planetsWithLiquidWater * 100.0 / Math.max(1, totalRockyPlanets)) + "%)");
        writer.println("- With ice coverage: " + planetsWithIce
                + " (" + String.format("%.1f", planetsWithIce * 100.0 / Math.max(1, totalRockyPlanets)) + "%)");
        writer.println("- With subsurface water: " + planetsWithSubsurfaceWater
                + " (" + String.format("%.1f", planetsWithSubsurfaceWater * 100.0 / Math.max(1, totalRockyPlanets)) + "%)");
        writer.println("");

        writer.println("### Water Inventory Distribution");
        writer.println("");
        writer.println("| Inventory | Count | % |");
        writer.println("| --- | --- | --- |");
        WATER_INVENTORIES.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> writer.println("| " + entry.getKey() + " | " + entry.getValue()
                        + " | " + String.format("%.1f", entry.getValue() * 100.0 / Math.max(1, totalRockyPlanets)) + "% |"));

        writer.println("");
        writer.println("### Water Phase at Surface");
        writer.println("");
        writer.println("| Phase | Count | % |");
        writer.println("| --- | --- | --- |");
        WATER_PHASES.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> writer.println("| " + entry.getKey() + " | " + entry.getValue()
                        + " | " + String.format("%.1f", entry.getValue() * 100.0 / Math.max(1, habAssessmentCount)) + "% |"));

        // ============================================================
        // HABITABILITY
        // ============================================================
        writer.println("");
        writer.println("---");
        writer.println("## Planetary Habitability");
        writer.println("");
        writer.println("Planets assessed: " + habAssessmentCount);
        writer.println("- Average ESI: " + String.format("%.3f", esiSum / Math.max(1, habAssessmentCount)));
        writer.println("- Average Habitability Score: " + String.format("%.1f", habScoreSum / Math.max(1, habAssessmentCount)));
        writer.println("- Breathable atmospheres: " + breathablePlanets
                + " (" + String.format("%.2f", breathablePlanets * 100.0 / Math.max(1, habAssessmentCount)) + "%)");
        writer.println("");

        writer.println("### Habitability Class Distribution");
        writer.println("");
        writer.println("| Class | Count | % |");
        writer.println("| --- | --- | --- |");
        HABITABILITY_CLASSES.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> writer.println("| " + entry.getKey() + " | " + entry.getValue()
                        + " | " + String.format("%.1f", entry.getValue() * 100.0 / Math.max(1, habAssessmentCount)) + "% |"));

        writer.println("");
        writer.println("### Colonization Suitability");
        writer.println("");
        writer.println("| Suitability | Count | % |");
        writer.println("| --- | --- | --- |");
        COLONIZATION_SUITABILITIES.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> writer.println("| " + entry.getKey() + " | " + entry.getValue()
                        + " | " + String.format("%.1f", entry.getValue() * 100.0 / Math.max(1, habAssessmentCount)) + "% |"));

        writer.println("");
        writer.println("### Terraforming Potential");
        writer.println("");
        writer.println("| Potential | Count | % |");
        writer.println("| --- | --- | --- |");
        TERRAFORMING_POTENTIALS.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> writer.println("| " + entry.getKey() + " | " + entry.getValue()
                        + " | " + String.format("%.1f", entry.getValue() * 100.0 / Math.max(1, habAssessmentCount)) + "% |"));

        writer.println("");
        writer.println("### Biosignature Potential");
        writer.println("");
        writer.println("| Potential | Count | % |");
        writer.println("| --- | --- | --- |");
        BIOSIGNATURE_POTENTIALS.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> writer.println("| " + entry.getKey() + " | " + entry.getValue()
                        + " | " + String.format("%.1f", entry.getValue() * 100.0 / Math.max(1, habAssessmentCount)) + "% |"));

        writer.println("");
        writer.println("### Life Complexity Potential");
        writer.println("");
        writer.println("| Potential | Count | % |");
        writer.println("| --- | --- | --- |");
        LIFE_COMPLEXITY_POTENTIALS.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> writer.println("| " + entry.getKey() + " | " + entry.getValue()
                        + " | " + String.format("%.1f", entry.getValue() * 100.0 / Math.max(1, habAssessmentCount)) + "% |"));
    }

    private static boolean isRockyType(String planetType) {
        if (planetType == null) return false;
        return planetType.contains("Terrestrial") || planetType.contains("Super-Earth")
                || planetType.contains("Rocky") || planetType.contains("Desert")
                || planetType.contains("Ocean") || planetType.contains("Lava")
                || planetType.contains("Carbon") || planetType.contains("Iron");
    }

    private static String binMagnetopause(double radii) {
        if (radii < 3) return "01: <3 (extreme compression)";
        if (radii < 6) return "02: 3-6 (heavy compression)";
        if (radii < 10) return "03: 6-10 (moderate compression)";
        if (radii < 20) return "04: 10-20 (Earth-like)";
        if (radii < 40) return "05: 20-40 (expanded)";
        if (radii < 70) return "06: 40-70 (large)";
        return "07: 70+ (massive)";
    }

    private static String binLossRate(double rate) {
        if (rate < 0.3) return "01: <0.3 (very low)";
        if (rate < 1.0) return "02: 0.3-1.0 (low)";
        if (rate < 3.0) return "03: 1.0-3.0 (moderate)";
        if (rate < 8.0) return "04: 3.0-8.0 (high)";
        if (rate < 15.0) return "05: 8.0-15.0 (very high)";
        return "06: 15+ (extreme)";
    }

    private static String binDistance(double distanceAU) {
        if (distanceAU < 0.1) return "01: <0.1 AU";
        if (distanceAU < 0.3) return "02: 0.1-0.3 AU";
        if (distanceAU < 1.0) return "03: 0.3-1.0 AU";
        if (distanceAU < 3.0) return "04: 1.0-3.0 AU";
        if (distanceAU < 10.0) return "05: 3.0-10.0 AU";
        return "06: 10+ AU";
    }
}
