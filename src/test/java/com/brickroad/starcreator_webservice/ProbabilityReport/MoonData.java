package com.brickroad.starcreator_webservice.ProbabilityReport;

import com.brickroad.starcreator_webservice.entity.ud.Moon;
import com.brickroad.starcreator_webservice.entity.ud.PlanetaryHabitability;
import com.brickroad.starcreator_webservice.entity.ud.PlanetaryMagneticField;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class MoonData {

    private static final Map<String, Integer> MOON_TYPES = new HashMap<>();
    // --- Moon Magnetic Field ---
    private static final Map<String, Integer> MOON_DYNAMO_TYPES = new HashMap<>();
    private static final Map<String, Integer> MOON_PROTECTION_LEVELS = new HashMap<>();
    private static int moonsWithMagField = 0;
    private static int moonsAssessed = 0;

    // --- Moon Water ---
    private static final Map<String, Integer> MOON_WATER_INVENTORIES = new HashMap<>();
    private static int moonsWithLiquidWater = 0;
    private static int moonsWithIce = 0;
    private static int moonsWithSubsurfaceWater = 0;
    private static int moonsWithSubsurfaceOcean = 0;

    // --- Moon Habitability ---
    private static final Map<String, Integer> MOON_HABITABILITY_CLASSES = new HashMap<>();
    private static final Map<String, Integer> MOON_COLONIZATION_SUITABILITIES = new HashMap<>();
    private static final Map<String, Integer> MOON_BIOSIGNATURE_POTENTIALS = new HashMap<>();
    private static final Map<String, Integer> MOON_LIFE_COMPLEXITY = new HashMap<>();
    private static final Map<String, Integer> MOON_RADIATION_BELT_DOSE = new HashMap<>();
    private static final Map<String, Integer> MOON_TIDAL_CONTRIBUTION = new HashMap<>();
    private static double moonEsiSum = 0.0;
    private static double moonHabScoreSum = 0.0;
    private static int moonHabCount = 0;

    // --- Moon Composition/Size cross-reference ---
    private static final Map<String, Integer> MOON_COMPOSITION_TYPES = new HashMap<>();
    private static final Map<String, Integer> MOON_TIDAL_HEATING_LEVELS = new HashMap<>();

    static void analyzeData(Moon moon) {
        MOON_TYPES.put(moon.getMoonType(), MOON_TYPES.getOrDefault(moon.getMoonType(), 0) + 1);

        // Composition type
        String compType = moon.getCompositionType() != null ? moon.getCompositionType() : "NULL";
        MOON_COMPOSITION_TYPES.put(compType, MOON_COMPOSITION_TYPES.getOrDefault(compType, 0) + 1);

        // Tidal heating
        String tidalLevel = moon.getTidalHeatingLevel() != null ? moon.getTidalHeatingLevel() : "NONE";
        MOON_TIDAL_HEATING_LEVELS.put(tidalLevel, MOON_TIDAL_HEATING_LEVELS.getOrDefault(tidalLevel, 0) + 1);

        // Subsurface ocean (from original MoonCreator logic, before water system)
        if (Boolean.TRUE.equals(moon.getHasSubsurfaceOcean())) {
            moonsWithSubsurfaceOcean++;
        }

        // Only moons above mass threshold get magnetic field / water / habitability
        PlanetaryMagneticField mf = moon.getMagneticField();
        PlanetaryHabitability hab = moon.getHabitability();

        boolean fullyAssessed = hab != null || mf != null;
        if (fullyAssessed) {
            moonsAssessed++;
        }

        String waterInv = moon.getWaterInventory();
        if (waterInv != null) {
            MOON_WATER_INVENTORIES.put(waterInv, MOON_WATER_INVENTORIES.getOrDefault(waterInv, 0) + 1);

            Double liquidPct = moon.getLiquidWaterCoveragePercent();
            Double icePct = moon.getIceCoveragePercent();
            if (liquidPct != null && liquidPct > 0.1) moonsWithLiquidWater++;
            if (icePct != null && icePct > 0.1) moonsWithIce++;
            if (Boolean.TRUE.equals(moon.getHasSubsurfaceWater())) moonsWithSubsurfaceWater++;
        }

        // --- Magnetic Field ---
        if (mf != null) {
            moonsWithMagField++;

            String dynamoType = mf.getDynamoType() != null ? mf.getDynamoType().name() : "NULL";
            MOON_DYNAMO_TYPES.put(dynamoType, MOON_DYNAMO_TYPES.getOrDefault(dynamoType, 0) + 1);

            String protection = mf.getProtectionLevel() != null ? mf.getProtectionLevel().name() : "NULL";
            MOON_PROTECTION_LEVELS.put(protection, MOON_PROTECTION_LEVELS.getOrDefault(protection, 0) + 1);
        }

        // --- Habitability ---
        if (hab != null) {
            moonHabCount++;

            String habClass = hab.getHabitabilityClass() != null ? hab.getHabitabilityClass().name() : "NULL";
            MOON_HABITABILITY_CLASSES.put(habClass, MOON_HABITABILITY_CLASSES.getOrDefault(habClass, 0) + 1);

            String colSuit = hab.getColonizationSuitability() != null ? hab.getColonizationSuitability().name() : "NULL";
            MOON_COLONIZATION_SUITABILITIES.put(colSuit, MOON_COLONIZATION_SUITABILITIES.getOrDefault(colSuit, 0) + 1);

            String bioPot = hab.getBiosignaturePotential() != null ? hab.getBiosignaturePotential() : "NONE";
            MOON_BIOSIGNATURE_POTENTIALS.put(bioPot, MOON_BIOSIGNATURE_POTENTIALS.getOrDefault(bioPot, 0) + 1);

            String lifePot = hab.getLifeComplexityPotential() != null ? hab.getLifeComplexityPotential() : "NONE";
            MOON_LIFE_COMPLEXITY.put(lifePot, MOON_LIFE_COMPLEXITY.getOrDefault(lifePot, 0) + 1);

            String radDose = hab.getRadiationBeltSurfaceDose() != null ? hab.getRadiationBeltSurfaceDose() : "NONE";
            MOON_RADIATION_BELT_DOSE.put(radDose, MOON_RADIATION_BELT_DOSE.getOrDefault(radDose, 0) + 1);

            String tidalContrib = hab.getTidalHeatingContribution() != null ? hab.getTidalHeatingContribution() : "NONE";
            MOON_TIDAL_CONTRIBUTION.put(tidalContrib, MOON_TIDAL_CONTRIBUTION.getOrDefault(tidalContrib, 0) + 1);

            if (hab.getEsiTotal() != null) moonEsiSum += hab.getEsiTotal();
            if (hab.getHabitabilityScore() != null) moonHabScoreSum += hab.getHabitabilityScore();
        }
    }

    static void printData(PrintWriter writer, ProbabilityCounts counts) {
        ReportUtils.printSection(writer, "Moon Types");
        ReportUtils.printSortedTable(writer, MOON_TYPES, counts.getMoonCount(), "Moon Type");

        ReportUtils.printSubSection(writer, "Moon Composition Types");
        ReportUtils.printSortedTable(writer, MOON_COMPOSITION_TYPES, counts.getMoonCount(), "Composition");

        ReportUtils.printSubSection(writer, "Tidal Heating Levels");
        ReportUtils.printSortedTable(writer, MOON_TIDAL_HEATING_LEVELS, counts.getMoonCount(), "Level");

        writer.println("Subsurface Oceans (from MoonCreator): " + moonsWithSubsurfaceOcean
                + " (" + ReportUtils.pct(moonsWithSubsurfaceOcean, counts.getMoonCount()) + "%)");
        writer.println("");

        // --- Detailed Analysis (assessed moons) ---
        writer.println("---");
        writer.println("### Moon Detailed Analysis (mass >= 0.0005 Earth)");
        writer.println("");
        writer.println("Moons assessed: " + moonsAssessed + " of " + counts.getMoonCount()
                + " total (" + ReportUtils.pct(moonsAssessed, counts.getMoonCount()) + "%)");
        writer.println("");

        // --- Magnetic Fields ---
        ReportUtils.printSubSubSection(writer, "Moon Magnetic Fields");
        writer.println("Moons with magnetic field data: " + moonsWithMagField
                + " (" + ReportUtils.pct(moonsWithMagField, moonsAssessed) + "% of assessed)");
        writer.println("");
        ReportUtils.printSortedTable(writer, MOON_DYNAMO_TYPES, moonsWithMagField, "Dynamo Type");
        ReportUtils.printSortedTable(writer, MOON_PROTECTION_LEVELS, moonsWithMagField, "Protection Level");

        // --- Water ---
        ReportUtils.printSubSubSection(writer, "Moon Water System");
        writer.println("- With liquid surface water: " + moonsWithLiquidWater
                + " (" + ReportUtils.pct(moonsWithLiquidWater, counts.getMoonCount()) + "%)");
        writer.println("- With ice coverage: " + moonsWithIce
                + " (" + ReportUtils.pct(moonsWithIce, counts.getMoonCount()) + "%)");
        writer.println("- With subsurface water: " + moonsWithSubsurfaceWater
                + " (" + ReportUtils.pct(moonsWithSubsurfaceWater, counts.getMoonCount()) + "%)");
        writer.println("");
        ReportUtils.printSortedTable(writer, MOON_WATER_INVENTORIES, counts.getMoonCount(), "Water Inventory");

        // --- Habitability ---
        ReportUtils.printSubSubSection(writer, "Moon Habitability");
        writer.println("Moons with habitability assessment: " + moonHabCount);
        writer.println("- Average ESI: " + String.format("%.4f", moonEsiSum / Math.max(1, moonHabCount)));
        writer.println("- Average Habitability Score: " + String.format("%.1f", moonHabScoreSum / Math.max(1, moonHabCount)));
        writer.println("");

        ReportUtils.printSortedTable(writer, MOON_HABITABILITY_CLASSES, moonHabCount, "Habitability Class");
        ReportUtils.printSortedTable(writer, MOON_COLONIZATION_SUITABILITIES, moonHabCount, "Colonization Suitability");
        ReportUtils.printSortedTable(writer, MOON_BIOSIGNATURE_POTENTIALS, moonHabCount, "Biosignature Potential");
        ReportUtils.printSortedTable(writer, MOON_LIFE_COMPLEXITY, moonHabCount, "Life Complexity");
        ReportUtils.printSortedTable(writer, MOON_RADIATION_BELT_DOSE, moonHabCount, "Radiation Belt Surface Dose");
        ReportUtils.printSortedTable(writer, MOON_TIDAL_CONTRIBUTION, moonHabCount, "Tidal Heating Contribution");
    }

}