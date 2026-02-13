package com.brickroad.starcreator_webservice.ProbabilityReport;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Tracks per-planet-type statistics for the probability report.
 * One instance per planet type (e.g., "Gas Giant", "Terrestrial Planet", etc.)
 */
public class PlanetTypeBreakdown {

    private int count = 0;

    // Composition classification
    private final Map<String, Integer> compositionClasses = new HashMap<>();

    // Surface temperature bins
    private final Map<String, Integer> surfaceTempBins = new HashMap<>();

    // Atmosphere classification
    private final Map<String, Integer> atmosphereClasses = new HashMap<>();

    // Habitable zone position
    private final Map<String, Integer> hzPositions = new HashMap<>();

    // Tidal locking
    private int tidallyLocked = 0;

    // Magnetic protection level
    private final Map<String, Integer> protectionLevels = new HashMap<>();

    // Mass bins
    private final Map<String, Integer> massBins = new HashMap<>();

    // Geological activity (surface types only)
    private final Map<String, Integer> geologicalActivity = new HashMap<>();

    // Water inventory (rocky types only)
    private final Map<String, Integer> waterInventories = new HashMap<>();

    // Habitability class
    private final Map<String, Integer> habitabilityClasses = new HashMap<>();

    // Moon count bins
    private final Map<String, Integer> moonCountBins = new HashMap<>();

    // Rings
    private int withRings = 0;

    // Averages
    private double massSum = 0;
    private double radiusSum = 0;
    private double gravitySum = 0;
    private double tempSum = 0;
    private int physicalCount = 0;

    void increment() { count++; }
    int getCount() { return count; }

    void addCompositionClass(String val) {
        compositionClasses.merge(val, 1, Integer::sum);
    }

    void addSurfaceTempBin(String bin) {
        surfaceTempBins.merge(bin, 1, Integer::sum);
    }

    void addAtmosphereClass(String val) {
        atmosphereClasses.merge(val, 1, Integer::sum);
    }

    void addHzPosition(String val) {
        hzPositions.merge(val, 1, Integer::sum);
    }

    void addTidallyLocked() { tidallyLocked++; }

    void addProtectionLevel(String val) {
        protectionLevels.merge(val, 1, Integer::sum);
    }

    void addMassBin(String bin) {
        massBins.merge(bin, 1, Integer::sum);
    }

    void addGeologicalActivity(String val) {
        geologicalActivity.merge(val, 1, Integer::sum);
    }

    void addWaterInventory(String val) {
        waterInventories.merge(val, 1, Integer::sum);
    }

    void addHabitabilityClass(String val) {
        habitabilityClasses.merge(val, 1, Integer::sum);
    }

    void addMoonCountBin(String bin) {
        moonCountBins.merge(bin, 1, Integer::sum);
    }

    void addRings() { withRings++; }

    void addPhysicalProps(double mass, double radius, double gravity, double temp) {
        massSum += mass;
        radiusSum += radius;
        gravitySum += gravity;
        tempSum += temp;
        physicalCount++;
    }

    void print(PrintWriter writer, String planetType) {
        ReportUtils.printAnchor(writer, planetType);
        ReportUtils.beginCollapsible(writer, planetType + " (" + count + ")", 3);

        // Summary line
        if (physicalCount > 0) {
            writer.println("Avg mass: " + String.format("%.2f", massSum / physicalCount) + " M⊕"
                    + " | Avg radius: " + String.format("%.2f", radiusSum / physicalCount) + " R⊕"
                    + " | Avg gravity: " + String.format("%.2f", gravitySum / physicalCount) + " g"
                    + " | Avg temp: " + String.format("%.0f", tempSum / physicalCount) + " K");
        }
        writer.println("Tidally locked: " + tidallyLocked + " (" + ReportUtils.pct(tidallyLocked, count) + "%)"
                + " | With rings: " + withRings + " (" + ReportUtils.pct(withRings, count) + "%)");
        writer.println("");

        // Composition
        if (!compositionClasses.isEmpty()) {
            ReportUtils.printSortedTable(writer, compositionClasses, count, "Composition");
        }

        // Temperature
        if (!surfaceTempBins.isEmpty()) {
            ReportUtils.printSortedTableByKey(writer, surfaceTempBins, count, "Temperature");
        }

        // Atmosphere
        if (!atmosphereClasses.isEmpty()) {
            ReportUtils.printSortedTable(writer, atmosphereClasses, count, "Atmosphere");
        }

        // HZ Position
        if (!hzPositions.isEmpty()) {
            ReportUtils.printSortedTable(writer, hzPositions, count, "HZ Position");
        }

        // Protection Level
        if (!protectionLevels.isEmpty()) {
            ReportUtils.printSortedTable(writer, protectionLevels, count, "Protection");
        }

        // Mass distribution
        if (!massBins.isEmpty()) {
            ReportUtils.printSortedTableByKey(writer, massBins, count, "Mass Range");
        }

        // Moon count
        if (!moonCountBins.isEmpty()) {
            ReportUtils.printSortedTableByKey(writer, moonCountBins, count, "Moon Count");
        }

        // Geological activity (only if data exists - surface types)
        if (!geologicalActivity.isEmpty()) {
            ReportUtils.printSortedTable(writer, geologicalActivity, count, "Geological Activity");
        }

        // Water inventory (only if data exists - rocky types)
        if (!waterInventories.isEmpty()) {
            ReportUtils.printSortedTable(writer, waterInventories, count, "Water Inventory");
        }

        // Habitability (only if data exists)
        if (!habitabilityClasses.isEmpty()) {
            ReportUtils.printSortedTable(writer, habitabilityClasses, count, "Habitability Class");
        }

        ReportUtils.endCollapsible(writer);
    }

    // ═══════════════════════════════════════════════════════════════
    //  HTML OUTPUT
    // ═══════════════════════════════════════════════════════════════

    void printHtml(PrintWriter w, String planetType) {
        HtmlReportUtils.printAnchor(w, planetType);
        HtmlReportUtils.beginCollapsible(w, planetType + " (" + count + ")", 3);

        if (physicalCount > 0) {
            w.println("<p>Avg mass: " + String.format("%.2f", massSum / physicalCount) + " M&#8853;"
                    + " | Avg radius: " + String.format("%.2f", radiusSum / physicalCount) + " R&#8853;"
                    + " | Avg gravity: " + String.format("%.2f", gravitySum / physicalCount) + " g"
                    + " | Avg temp: " + String.format("%.0f", tempSum / physicalCount) + " K</p>");
        }
        w.println("<p>Tidally locked: " + tidallyLocked + " (" + HtmlReportUtils.pct(tidallyLocked, count) + "%)"
                + " | With rings: " + withRings + " (" + HtmlReportUtils.pct(withRings, count) + "%)</p>");

        if (!compositionClasses.isEmpty())
            HtmlReportUtils.printSortedTable(w, compositionClasses, count, "Composition");
        if (!surfaceTempBins.isEmpty())
            HtmlReportUtils.printSortedTableByKey(w, surfaceTempBins, count, "Temperature");
        if (!atmosphereClasses.isEmpty())
            HtmlReportUtils.printSortedTable(w, atmosphereClasses, count, "Atmosphere");
        if (!hzPositions.isEmpty())
            HtmlReportUtils.printSortedTable(w, hzPositions, count, "HZ Position");
        if (!protectionLevels.isEmpty())
            HtmlReportUtils.printSortedTable(w, protectionLevels, count, "Protection");
        if (!massBins.isEmpty())
            HtmlReportUtils.printSortedTableByKey(w, massBins, count, "Mass Range");
        if (!moonCountBins.isEmpty())
            HtmlReportUtils.printSortedTableByKey(w, moonCountBins, count, "Moon Count");
        if (!geologicalActivity.isEmpty())
            HtmlReportUtils.printSortedTable(w, geologicalActivity, count, "Geological Activity");
        if (!waterInventories.isEmpty())
            HtmlReportUtils.printSortedTable(w, waterInventories, count, "Water Inventory");
        if (!habitabilityClasses.isEmpty())
            HtmlReportUtils.printSortedTable(w, habitabilityClasses, count, "Habitability Class");

        HtmlReportUtils.endCollapsible(w);
    }
}
