package com.brickroad.starcreator_webservice.ProbabilityReport;

import com.brickroad.starcreator_webservice.entity.ud.*;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
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

    // --- Moon Weather ---
    private static int moonsWithWeather = 0;
    private static final Map<String, Integer> MOON_WEATHER_SEVERITY = new HashMap<>();
    private static final Map<String, Integer> MOON_WEATHER_EXPOSURE = new HashMap<>();
    private static final Map<String, Integer> MOON_WEATHER_SKY_COLOR = new HashMap<>();
    private static final Map<String, Integer> MOON_WEATHER_CLOUD_CLASS = new HashMap<>();
    private static final Map<String, Integer> MOON_WEATHER_WIND_INTENSITY = new HashMap<>();
    private static final Map<String, Integer> MOON_WEATHER_TIDAL_RANGE_BINS = new HashMap<>();
    private static int moonsWithPrecipitation = 0;
    private static int moonsWithLightning = 0;
    private static int moonsWithPlanetaryEclipses = 0;
    private static int moonSiblingAppearanceTotal = 0;
    private static int moonsWithParentPlanetVisible = 0;
    private static int moonExtremeEventTotal = 0;

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

        // --- Weather ---
        analyzeMoonWeatherData(moon);
    }

    private static void analyzeMoonWeatherData(Moon moon) {
        PlanetaryWeather w = moon.getWeather();
        if (w == null) return;

        moonsWithWeather++;

        // Sky color
        String skyColor = w.getSkyColor() != null ? w.getSkyColor() : "NULL";
        MOON_WEATHER_SKY_COLOR.put(skyColor, MOON_WEATHER_SKY_COLOR.getOrDefault(skyColor, 0) + 1);

        // Cloud coverage
        String cloudClass = w.getCloudCoverageClass() != null ? w.getCloudCoverageClass() : "NULL";
        MOON_WEATHER_CLOUD_CLASS.put(cloudClass, MOON_WEATHER_CLOUD_CLASS.getOrDefault(cloudClass, 0) + 1);

        // Wind
        String windIntensity = w.getWindIntensity() != null ? w.getWindIntensity() : "NULL";
        MOON_WEATHER_WIND_INTENSITY.put(windIntensity, MOON_WEATHER_WIND_INTENSITY.getOrDefault(windIntensity, 0) + 1);

        // Severity & exposure
        String severity = w.getWeatherSeverity() != null ? w.getWeatherSeverity() : "NULL";
        MOON_WEATHER_SEVERITY.put(severity, MOON_WEATHER_SEVERITY.getOrDefault(severity, 0) + 1);

        String exposure = w.getOutdoorExposureRating() != null ? w.getOutdoorExposureRating() : "NULL";
        MOON_WEATHER_EXPOSURE.put(exposure, MOON_WEATHER_EXPOSURE.getOrDefault(exposure, 0) + 1);

        // Precipitation & storms
        if (Boolean.TRUE.equals(w.getHasPrecipitation())) moonsWithPrecipitation++;
        if (Boolean.TRUE.equals(w.getHasLightning())) moonsWithLightning++;

        // Tidal range (from parent planet + siblings)
        if (w.getTidalRangeMeters() != null) {
            String tidalBin = binTidalRange(w.getTidalRangeMeters());
            MOON_WEATHER_TIDAL_RANGE_BINS.put(tidalBin, MOON_WEATHER_TIDAL_RANGE_BINS.getOrDefault(tidalBin, 0) + 1);
        }

        // Extreme events
        if (w.getExtremeWeatherEvents() != null) {
            moonExtremeEventTotal += w.getExtremeWeatherEvents().size();
        }

        // Phase 7: Parent planet and sibling moon sky data
        // Convention: index 0 is the parent planet (moonId=null), remaining entries are siblings.
        // Note: moonId is null for all entries pre-DB, so we use list position instead.
        List<MoonSkyAppearance> skyApps = w.getMoonSkyAppearances();
        if (skyApps != null && !skyApps.isEmpty()) {
            moonsWithParentPlanetVisible++;
            int siblingCount = Math.max(0, skyApps.size() - 1);
            moonSiblingAppearanceTotal += siblingCount;
        }

        // Planetary eclipses
        List<EclipseData> eclipses = w.getEclipseData();
        if (eclipses != null && !eclipses.isEmpty()) {
            boolean hasPlanetaryEclipse = eclipses.stream()
                    .anyMatch(e -> "PLANET_SOLAR".equals(e.getEclipseSource()));
            if (hasPlanetaryEclipse) moonsWithPlanetaryEclipses++;
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
        ReportUtils.beginCollapsible(writer, "Moon Types", 2);
        ReportUtils.printSortedTable(writer, MOON_TYPES, counts.getMoonCount(), "Moon Type");

        ReportUtils.printSubSection(writer, "Moon Composition Types");
        ReportUtils.printSortedTable(writer, MOON_COMPOSITION_TYPES, counts.getMoonCount(), "Composition");

        ReportUtils.printSubSection(writer, "Tidal Heating Levels");
        ReportUtils.printSortedTable(writer, MOON_TIDAL_HEATING_LEVELS, counts.getMoonCount(), "Level");

        writer.println("Subsurface Oceans (from MoonCreator): " + moonsWithSubsurfaceOcean
                + " (" + ReportUtils.pct(moonsWithSubsurfaceOcean, counts.getMoonCount()) + "%)");
        writer.println("");

        // --- Detailed Analysis (assessed moons) ---
        ReportUtils.beginCollapsible(writer, "Moon Detailed Analysis (mass >= 0.0005 Earth)", 3);
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

        // --- Weather ---
        printMoonWeatherData(writer, counts);

        ReportUtils.endCollapsible(writer); // close detailed analysis

        ReportUtils.endCollapsible(writer); // close Moon Types
    }

    private static void printMoonWeatherData(PrintWriter writer, ProbabilityCounts counts) {
        ReportUtils.printSubSubSection(writer, "Moon Weather");
        writer.println("Moons with weather data: " + moonsWithWeather + " of " + counts.getMoonCount()
                + " total (" + ReportUtils.pct(moonsWithWeather, counts.getMoonCount()) + "%)");
        if (moonsWithWeather == 0) return;

        writer.println("- With precipitation: " + moonsWithPrecipitation
                + " (" + ReportUtils.pct(moonsWithPrecipitation, moonsWithWeather) + "%)");
        writer.println("- With lightning: " + moonsWithLightning
                + " (" + ReportUtils.pct(moonsWithLightning, moonsWithWeather) + "%)");
        writer.println("- With parent planet visible in sky: " + moonsWithParentPlanetVisible
                + " (" + ReportUtils.pct(moonsWithParentPlanetVisible, moonsWithWeather) + "%)");
        writer.println("- With planetary eclipses: " + moonsWithPlanetaryEclipses
                + " (" + ReportUtils.pct(moonsWithPlanetaryEclipses, moonsWithWeather) + "%)");
        writer.println("- Avg sibling moons visible per moon: "
                + String.format("%.1f", moonSiblingAppearanceTotal * 1.0 / moonsWithWeather));
        writer.println("- Total extreme weather events: " + moonExtremeEventTotal
                + " (avg " + String.format("%.1f", moonExtremeEventTotal * 1.0 / moonsWithWeather) + "/moon)");
        writer.println("");

        ReportUtils.printSortedTable(writer, MOON_WEATHER_SKY_COLOR, moonsWithWeather, "Sky Color");
        ReportUtils.printSortedTable(writer, MOON_WEATHER_CLOUD_CLASS, moonsWithWeather, "Cloud Coverage");
        ReportUtils.printSortedTable(writer, MOON_WEATHER_WIND_INTENSITY, moonsWithWeather, "Wind Intensity");
        ReportUtils.printSortedTable(writer, MOON_WEATHER_SEVERITY, moonsWithWeather, "Weather Severity");
        ReportUtils.printSortedTable(writer, MOON_WEATHER_EXPOSURE, moonsWithWeather, "Exposure Rating");

        if (!MOON_WEATHER_TIDAL_RANGE_BINS.isEmpty()) {
            ReportUtils.printSubSubSection(writer, "Moon Tidal Range (from Parent Planet + Siblings)");
            ReportUtils.printSortedTableByKey(writer, MOON_WEATHER_TIDAL_RANGE_BINS, moonsWithWeather, "Tidal Range");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  HTML OUTPUT
    // ═══════════════════════════════════════════════════════════════

    static void printHtml(PrintWriter w, ProbabilityCounts counts) {
        w.println("<hr>");
        HtmlReportUtils.beginCollapsible(w, "Moon Types", 2);
        HtmlReportUtils.printSortedTable(w, MOON_TYPES, counts.getMoonCount(), "Moon Type");

        HtmlReportUtils.printSubSection(w, "Moon Composition Types");
        HtmlReportUtils.printSortedTable(w, MOON_COMPOSITION_TYPES, counts.getMoonCount(), "Composition");

        HtmlReportUtils.printSubSection(w, "Tidal Heating Levels");
        HtmlReportUtils.printSortedTable(w, MOON_TIDAL_HEATING_LEVELS, counts.getMoonCount(), "Level");

        w.println("<p>Subsurface Oceans (from MoonCreator): " + moonsWithSubsurfaceOcean
                + " (" + HtmlReportUtils.pct(moonsWithSubsurfaceOcean, counts.getMoonCount()) + "%)</p>");

        // Detailed Analysis
        HtmlReportUtils.beginCollapsible(w, "Moon Detailed Analysis (mass >= 0.0005 Earth)", 3);
        w.println("<p>Moons assessed: " + moonsAssessed + " of " + counts.getMoonCount()
                + " total (" + HtmlReportUtils.pct(moonsAssessed, counts.getMoonCount()) + "%)</p>");

        // Magnetic Fields
        HtmlReportUtils.printSubSubSection(w, "Moon Magnetic Fields");
        w.println("<p>Moons with magnetic field data: " + moonsWithMagField
                + " (" + HtmlReportUtils.pct(moonsWithMagField, moonsAssessed) + "% of assessed)</p>");
        HtmlReportUtils.printSortedTable(w, MOON_DYNAMO_TYPES, moonsWithMagField, "Dynamo Type");
        HtmlReportUtils.printSortedTable(w, MOON_PROTECTION_LEVELS, moonsWithMagField, "Protection Level");

        // Water
        HtmlReportUtils.printSubSubSection(w, "Moon Water System");
        w.println("<p>With liquid surface water: " + moonsWithLiquidWater
                + " (" + HtmlReportUtils.pct(moonsWithLiquidWater, counts.getMoonCount()) + "%)</p>");
        w.println("<p>With ice coverage: " + moonsWithIce
                + " (" + HtmlReportUtils.pct(moonsWithIce, counts.getMoonCount()) + "%)</p>");
        w.println("<p>With subsurface water: " + moonsWithSubsurfaceWater
                + " (" + HtmlReportUtils.pct(moonsWithSubsurfaceWater, counts.getMoonCount()) + "%)</p>");
        HtmlReportUtils.printSortedTable(w, MOON_WATER_INVENTORIES, counts.getMoonCount(), "Water Inventory");

        // Habitability
        HtmlReportUtils.printSubSubSection(w, "Moon Habitability");
        w.println("<p>Moons with habitability assessment: " + moonHabCount + "</p>");
        w.println("<p>Average ESI: " + String.format("%.4f", moonEsiSum / Math.max(1, moonHabCount)) + "</p>");
        w.println("<p>Average Habitability Score: " + String.format("%.1f", moonHabScoreSum / Math.max(1, moonHabCount)) + "</p>");

        HtmlReportUtils.printSortedTable(w, MOON_HABITABILITY_CLASSES, moonHabCount, "Habitability Class");
        HtmlReportUtils.printSortedTable(w, MOON_COLONIZATION_SUITABILITIES, moonHabCount, "Colonization Suitability");
        HtmlReportUtils.printSortedTable(w, MOON_BIOSIGNATURE_POTENTIALS, moonHabCount, "Biosignature Potential");
        HtmlReportUtils.printSortedTable(w, MOON_LIFE_COMPLEXITY, moonHabCount, "Life Complexity");
        HtmlReportUtils.printSortedTable(w, MOON_RADIATION_BELT_DOSE, moonHabCount, "Radiation Belt Surface Dose");
        HtmlReportUtils.printSortedTable(w, MOON_TIDAL_CONTRIBUTION, moonHabCount, "Tidal Heating Contribution");

        // Weather
        printHtmlMoonWeatherData(w, counts);

        HtmlReportUtils.endCollapsible(w); // close detailed analysis
        HtmlReportUtils.endCollapsible(w); // close Moon Types
    }

    private static void printHtmlMoonWeatherData(PrintWriter w, ProbabilityCounts counts) {
        HtmlReportUtils.printSubSubSection(w, "Moon Weather");
        w.println("<p>Moons with weather data: " + moonsWithWeather + " of " + counts.getMoonCount()
                + " total (" + HtmlReportUtils.pct(moonsWithWeather, counts.getMoonCount()) + "%)</p>");
        if (moonsWithWeather == 0) return;

        w.println("<p>With precipitation: " + moonsWithPrecipitation
                + " (" + HtmlReportUtils.pct(moonsWithPrecipitation, moonsWithWeather) + "%)</p>");
        w.println("<p>With lightning: " + moonsWithLightning
                + " (" + HtmlReportUtils.pct(moonsWithLightning, moonsWithWeather) + "%)</p>");
        w.println("<p>With parent planet visible in sky: " + moonsWithParentPlanetVisible
                + " (" + HtmlReportUtils.pct(moonsWithParentPlanetVisible, moonsWithWeather) + "%)</p>");
        w.println("<p>With planetary eclipses: " + moonsWithPlanetaryEclipses
                + " (" + HtmlReportUtils.pct(moonsWithPlanetaryEclipses, moonsWithWeather) + "%)</p>");
        w.println("<p>Avg sibling moons visible per moon: "
                + String.format("%.1f", moonSiblingAppearanceTotal * 1.0 / moonsWithWeather) + "</p>");
        w.println("<p>Total extreme weather events: " + moonExtremeEventTotal
                + " (avg " + String.format("%.1f", moonExtremeEventTotal * 1.0 / moonsWithWeather) + "/moon)</p>");

        HtmlReportUtils.printSortedTable(w, MOON_WEATHER_SKY_COLOR, moonsWithWeather, "Sky Color");
        HtmlReportUtils.printSortedTable(w, MOON_WEATHER_CLOUD_CLASS, moonsWithWeather, "Cloud Coverage");
        HtmlReportUtils.printSortedTable(w, MOON_WEATHER_WIND_INTENSITY, moonsWithWeather, "Wind Intensity");
        HtmlReportUtils.printSortedTable(w, MOON_WEATHER_SEVERITY, moonsWithWeather, "Weather Severity");
        HtmlReportUtils.printSortedTable(w, MOON_WEATHER_EXPOSURE, moonsWithWeather, "Exposure Rating");

        if (!MOON_WEATHER_TIDAL_RANGE_BINS.isEmpty()) {
            HtmlReportUtils.printSubSubSection(w, "Moon Tidal Range (from Parent Planet + Siblings)");
            HtmlReportUtils.printSortedTableByKey(w, MOON_WEATHER_TIDAL_RANGE_BINS, moonsWithWeather, "Tidal Range");
        }
    }
}