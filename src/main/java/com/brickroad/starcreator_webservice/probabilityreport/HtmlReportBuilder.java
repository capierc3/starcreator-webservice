package com.brickroad.starcreator_webservice.probabilityreport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HtmlReportBuilder {

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

    private static final String HEADER_IMAGE_PATH = "/report/headerImg.png";

    public HtmlReportBuilder(ProbabilityCounts counts, PerformanceTimer timer,
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

    public void saveReport(File targetFolder) {
        File file = new File(targetFolder, "system_report_" + counts.getSystemCount() + "_systems.html");

        try (PrintWriter w = new PrintWriter(new FileWriter(file))) {
            printPageHeader(w);
            printSidebarToc(w);
            beginMainContent(w);
            printHtmlHeader(w);

            printStarHtml(w);
            printPlanetHtml(w);
            printMoonHtml(w);
            printRingHtml(w);
            printTrojanHtml(w);
            printAsteroidHtml(w);
            printBeltHtml(w);
            printOrbitalStabilityHtml(w);

            printPageFooter(w);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("HTML report saved to: " + file.getAbsolutePath());
    }

    // ═══════════════════════════════════════════════════════════════
    //  Page Structure
    // ═══════════════════════════════════════════════════════════════

    private void printHtmlHeader(PrintWriter w) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");

        w.println("<div class=\"report-header\">");
        w.println("<h1>System Probability Report</h1>");
        w.println("<p class=\"subtitle\">" + LocalDateTime.now().format(formatter) + "</p>");
        w.println("<p class=\"subtitle\">Created " + fmt(counts.getSystemCount()) + " systems in " + timer.getFinalTime() + "</p>");
        w.println("</div>");

        w.println("<div class=\"stats-grid\">");
        statCard(w, fmt(counts.getSystemCount()), "Systems");
        statCard(w, fmt(counts.getStarCount()), "Stars");
        statCard(w, fmt(counts.getPlanetCount()), "Planets");
        statCard(w, fmt(counts.getMoonCount()), "Moons");
        statCard(w, fmt(counts.getMoonletCount()), "Moonlets");
        statCard(w, fmt(counts.getRingCount()), "Rings");
        statCard(w, fmt(counts.getBeltCount()), "Belts");
        statCard(w, fmt(counts.getAsteroidCount()), "Asteroids");
        statCard(w, timer.averageLap() + "ms", "Avg / System");
        w.println("</div>");
    }

    private void printSidebarToc(PrintWriter w) {
        w.println("<div class=\"toc-title\">Contents</div>");
        w.println("<ol>");
        tocLink(w, "Star Amounts");
        tocLink(w, "Planet Types");
        tocLink(w, "Per-Planet-Type Breakdown (Single-Star)");
        tocLink(w, "Per-Planet-Type Breakdown (P-Type Binary)");
        tocLink(w, "Per-Planet-Type Breakdown (Trinary)");
        tocLink(w, "Atmosphere & Magnetic Fields");
        tocLink(w, "Geology (Rocky/Surface Planets)");
        tocLink(w, "Water System (Rocky/Surface Planets Only)");
        tocLink(w, "Planetary Habitability");
        tocLink(w, "Planetary Climate");
        tocLink(w, "Surface Planet Climate");
        tocLink(w, "Gas / Ice Giant Climate");
        tocLink(w, "Moon Types");
        tocLink(w, "Ring Types");
        tocLink(w, "Trojan Swarms");
        tocLink(w, "Notable Asteroids");
        tocLink(w, "Belt Types");
        tocLink(w, "Orbital Stability");
        w.println("</ol>");
    }

    // ═══════════════════════════════════════════════════════════════
    //  Star HTML
    // ═══════════════════════════════════════════════════════════════

    private void printStarHtml(PrintWriter w) {
        w.println("<hr>");
        beginCollapsible(w, "Star Amounts", 2);

        // Star amounts table
        w.println("<table>");
        w.println("<thead><tr><th>Amount</th><th>Count</th><th>%</th><th class=\"bar-col\">Distribution</th></tr></thead>");
        w.println("<tbody>");
        starData.getStarAmounts().entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    double pctVal = entry.getValue() * 100.0 / Math.max(1, counts.getSystemCount());
                    w.println("<tr><td>" + entry.getKey() + " Star System</td><td>" + fmt(entry.getValue())
                            + "</td><td>" + String.format("%.1f", pctVal) + "%</td><td>"
                            + bar(pctVal) + "</td></tr>");
                });
        w.println("</tbody></table>");

        // System Configuration breakdown (binary/trinary)
        if (!starData.getBinaryConfigurations().isEmpty()) {
            printSubSection(w, "System Configuration");
            printSortedTable(w, starData.getBinaryConfigurations(), counts.getSystemCount(), "Configuration");
        }

        printLinkedTable(w, starData.getStarTypes(), counts.getStarCount(), "Star Type");

        printSubSection(w, "Star Roles");
        printSortedTable(w, starData.getStarRoles(), counts.getStarCount(), "Role");

        // Per-type breakdown
        for (Map.Entry<String, Map<String, Map<String, Integer>>> entry : starData.getStarTypesData().entrySet()) {
            Map<String, Map<String, Integer>> starTypeData = entry.getValue();
            int starTypeCount = starData.getStarTypes().getOrDefault(entry.getKey(), 0);

            if (starData.isUniformType(starTypeData)) {
                printAnchor(w, entry.getKey());
                beginCollapsible(w, entry.getKey() + " (" + starTypeCount + ")", 3);
                Map<String, Integer> activityData = starTypeData.getOrDefault("activity", new HashMap<>());
                Map<String, Integer> evoData = starTypeData.getOrDefault("evolutionary stage", new HashMap<>());
                String activity = activityData.keySet().stream().findFirst().orElse("N/A");
                String evo = evoData.keySet().stream().findFirst().orElse("N/A");
                w.println("<p class=\"note\">Uniform profile — Activity: " + esc(activity)
                        + ", Stage: " + esc(evo) + "</p>");
                printPlanetFormationsTable(w, entry.getKey());
                endCollapsible(w);
                continue;
            }

            printAnchor(w, entry.getKey());
            beginCollapsible(w, entry.getKey() + " Star Type Data", 3);

            printStarSubTable(w, starTypeData, "activity", "Activity Level", starTypeCount);
            printStarSubTable(w, starTypeData, "flare class", "Flare Class", starTypeCount);
            printStarSubTable(w, starTypeData, "spot %", "Spot Coverage", starTypeCount);
            printStarSubTable(w, starTypeData, "xray Luminosity", "X-Ray Luminosity", starTypeCount);
            printStarSubTable(w, starTypeData, "evolutionary stage", "Evolutionary Stage", starTypeCount);
            printStarSubTable(w, starTypeData, "planets per system", "Planets Per System", starTypeCount);
            printStarSubTable(w, starTypeData, "hz inner AU", "Habitable Zone Inner Edge (AU)", starTypeCount);
            printStarSubTable(w, starTypeData, "hz outer AU", "Habitable Zone Outer Edge (AU)", starTypeCount);
            printPlanetFormationsTable(w, entry.getKey());

            endCollapsible(w);
        }

        endCollapsible(w);
    }

    private void printStarSubTable(PrintWriter w, Map<String, Map<String, Integer>> typeData,
                                   String key, String label, int total) {
        Map<String, Integer> data = typeData.getOrDefault(key, new HashMap<>());
        if (data.isEmpty()) return;
        printSortedTable(w, data, total, label);
    }

    private void printPlanetFormationsTable(PrintWriter w, String starType) {
        Map<String, double[]> formations = starData.getPlanetFormationsByStarType().get(starType);
        if (formations == null || formations.isEmpty()) return;

        int totalPlanets = formations.values().stream().mapToInt(v -> (int) v[0]).sum();

        w.println("<h3>Planet Formations</h3>");
        w.println("<p class=\"note\">Planet types produced by " + esc(starType) + " stars (" + fmt(totalPlanets) + " total planets)</p>");
        w.println("<table>");
        w.println("<thead><tr><th>Planet Type</th><th>Count</th><th>Distance Range (AU)</th><th>%</th><th class=\"bar-col\">Distribution</th></tr></thead>");
        w.println("<tbody>");
        formations.entrySet().stream()
                .sorted((a, b) -> Integer.compare((int) b.getValue()[0], (int) a.getValue()[0]))
                .forEach(e -> {
                    int count = (int) e.getValue()[0];
                    double minAU = e.getValue()[1];
                    double maxAU = e.getValue()[2];
                    double pctVal = count * 100.0 / Math.max(1, totalPlanets);
                    String distRange;
                    if (minAU < 0 && maxAU < 0) {
                        distRange = "N/A";
                    } else if (Math.abs(minAU - maxAU) < 0.0001) {
                        distRange = String.format("%.3f AU", minAU);
                    } else {
                        distRange = String.format("%.3f - %.3f AU", minAU, maxAU);
                    }
                    w.println("<tr><td>" + esc(e.getKey()) + "</td><td>" + fmt(count)
                            + "</td><td>" + distRange + "</td><td>" + String.format("%.1f", pctVal)
                            + "%</td><td>" + bar(pctVal) + "</td></tr>");
                });
        w.println("</tbody></table>");
    }

    // ═══════════════════════════════════════════════════════════════
    //  Planet HTML
    // ═══════════════════════════════════════════════════════════════

    private void printPlanetHtml(PrintWriter w) {
        w.println("<hr>");
        beginCollapsible(w, "Planet Types", 2);

        w.println("<p><strong>Summary:</strong> " + fmt(counts.getPlanetCount()) + " planets across "
                + fmt(counts.getSystemCount()) + " systems (avg "
                + String.format("%.1f", counts.getPlanetCount() * 1.0 / Math.max(1, counts.getSystemCount()))
                + " per system)</p>");
        w.println("<p>With rings: " + fmt(planetData.getPlanetsWithRings()) + " ("
                + pct(planetData.getPlanetsWithRings(), counts.getPlanetCount()) + "%)</p>");
        if (planetData.getPhysicalPropsCount() > 0) {
            w.println("<p>Avg mass: " + String.format("%.2f", planetData.getTotalMass() / planetData.getPhysicalPropsCount()) + " M&#8853;"
                    + " | Avg radius: " + String.format("%.2f", planetData.getTotalRadius() / planetData.getPhysicalPropsCount()) + " R&#8853;"
                    + " | Avg gravity: " + String.format("%.2f", planetData.getTotalGravity() / planetData.getPhysicalPropsCount()) + " g</p>");
        }

        printLinkedTable(w, planetData.getPlanetTypes(), counts.getPlanetCount(), "Planet Type");

        printSubSection(w, "Composition Classification");
        printSortedTable(w, planetData.getCompositionClasses(), counts.getPlanetCount(), "Classification");

        printSubSection(w, "Surface Temperature Distribution");
        printSortedTableByKey(w, planetData.getSurfaceTempBins(), counts.getPlanetCount(), "Temperature Range");

        endCollapsible(w);

        printPlanetPerTypeBreakdown(w);
        printAtmosphereHtml(w);
        printGeologyHtml(w);
        printWaterAndHabHtml(w);
        printClimateHtml(w);
    }

    private void printPlanetPerTypeBreakdown(PrintWriter w) {
        // Single-star systems (non-P-type)
        w.println("<hr>");
        beginCollapsible(w, "Per-Planet-Type Breakdown (Single-Star)", 2);
        w.println("<p class=\"note\">Planets in Single, S-Type, and Hierarchical systems</p>");

        planetData.getPerTypeData().entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().getCount(), a.getValue().getCount()))
                .forEach(entry -> printPlanetTypeHtml(w, entry.getKey(), entry.getValue()));

        endCollapsible(w);

        // P-type binary systems
        w.println("<hr>");
        beginCollapsible(w, "Per-Planet-Type Breakdown (P-Type Binary)", 2);
        w.println("<p class=\"note\">Planets in circumbinary (P-Type) systems where planets orbit both stars</p>");

        if (planetData.getPerTypeDataPType().isEmpty()) {
            w.println("<p>No P-Type binary systems found in this sample.</p>");
        } else {
            planetData.getPerTypeDataPType().entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue().getCount(), a.getValue().getCount()))
                    .forEach(entry -> printPlanetTypeHtml(w, entry.getKey(), entry.getValue()));
        }

        endCollapsible(w);

        // Trinary systems
        w.println("<hr>");
        beginCollapsible(w, "Per-Planet-Type Breakdown (Trinary)", 2);
        w.println("<p class=\"note\">Planets in trinary systems (Hierarchical Binary+Third and Hierarchical Triple)</p>");

        if (planetData.getPerTypeDataTrinary().isEmpty()) {
            w.println("<p>No trinary systems found in this sample.</p>");
        } else {
            planetData.getPerTypeDataTrinary().entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue().getCount(), a.getValue().getCount()))
                    .forEach(entry -> printPlanetTypeHtml(w, entry.getKey(), entry.getValue()));
        }

        endCollapsible(w);
    }

    private void printPlanetTypeHtml(PrintWriter w, String planetType, PlanetTypeBreakdown ptb) {
        printAnchor(w, planetType);
        beginCollapsible(w, planetType + " (" + ptb.getCount() + ")", 3);

        if (ptb.getPhysicalCount() > 0) {
            w.println("<p>Avg mass: " + String.format("%.2f", ptb.getMassSum() / ptb.getPhysicalCount()) + " M&#8853;"
                    + " | Avg radius: " + String.format("%.2f", ptb.getRadiusSum() / ptb.getPhysicalCount()) + " R&#8853;"
                    + " | Avg gravity: " + String.format("%.2f", ptb.getGravitySum() / ptb.getPhysicalCount()) + " g"
                    + " | Avg temp: " + String.format("%.0f", ptb.getTempSum() / ptb.getPhysicalCount()) + " K</p>");
        }
        w.println("<p>Tidally locked: " + ptb.getTidallyLocked() + " (" + pct(ptb.getTidallyLocked(), ptb.getCount()) + "%)"
                + " | With rings: " + ptb.getWithRings() + " (" + pct(ptb.getWithRings(), ptb.getCount()) + "%)</p>");

        if (!ptb.getCompositionClasses().isEmpty())
            printSortedTable(w, ptb.getCompositionClasses(), ptb.getCount(), "Composition");
        if (!ptb.getSurfaceTempBins().isEmpty())
            printSortedTableByKey(w, ptb.getSurfaceTempBins(), ptb.getCount(), "Temperature");
        if (!ptb.getAtmosphereClasses().isEmpty())
            printSortedTable(w, ptb.getAtmosphereClasses(), ptb.getCount(), "Atmosphere");
        if (!ptb.getHzPositions().isEmpty())
            printSortedTable(w, ptb.getHzPositions(), ptb.getCount(), "HZ Position");
        if (!ptb.getProtectionLevels().isEmpty())
            printSortedTable(w, ptb.getProtectionLevels(), ptb.getCount(), "Protection");
        if (!ptb.getMassBins().isEmpty())
            printSortedTableByKey(w, ptb.getMassBins(), ptb.getCount(), "Mass Range");
        if (!ptb.getSemiMajorAxisBins().isEmpty())
            printSortedTableByKey(w, ptb.getSemiMajorAxisBins(), ptb.getCount(), "Semi-Major Axis (AU)");
        if (!ptb.getDistanceValues().isEmpty())
            printDistanceStats(w, ptb);
        if (!ptb.getTidalLockByDistance().isEmpty())
            printTidalLockByDistanceTable(w, ptb.getTidalLockByDistance());
        if (!ptb.getMoonCountBins().isEmpty())
            printSortedTableByKey(w, ptb.getMoonCountBins(), ptb.getCount(), "Moon Count");
        if (!ptb.getMoonletBins().isEmpty())
            printSortedTableByKey(w, ptb.getMoonletBins(), ptb.getCount(), "Additional Moonlets");
        if (!ptb.getGeologicalActivity().isEmpty())
            printSortedTable(w, ptb.getGeologicalActivity(), ptb.getCount(), "Geological Activity");
        if (!ptb.getWaterInventories().isEmpty())
            printSortedTable(w, ptb.getWaterInventories(), ptb.getCount(), "Water Inventory");
        if (!ptb.getHabitabilityClasses().isEmpty())
            printSortedTable(w, ptb.getHabitabilityClasses(), ptb.getCount(), "Habitability Class");

        endCollapsible(w);
    }

    private void printDistanceStats(PrintWriter w, PlanetTypeBreakdown ptb) {
        java.util.Map<String, Object> ds = ptb.computeDistanceStats();

        w.println("<h3>Distance Statistics (AU)</h3>");
        w.println("<div class=\"stats-grid\">");
        statCard(w, String.valueOf(ds.get("min")), "Closest");
        statCard(w, String.valueOf(ds.get("max")), "Farthest");
        statCard(w, String.valueOf(ds.get("median")), "Median");
        if (ds.containsKey("iqrMean")) {
            statCard(w, String.valueOf(ds.get("iqrMean")), "IQR Mean*");
        } else {
            statCard(w, String.valueOf(ds.get("mean")), "Mean");
        }
        w.println("</div>");

        if (ds.containsKey("outliersExcluded")) {
            int excluded = ((Number) ds.get("outliersExcluded")).intValue();
            if (excluded > 0) {
                w.println("<p class=\"note\">* IQR Mean excludes " + excluded
                        + " outlier(s) outside Q1&#8211;Q3 &plusmn; 1.5&times;IQR (Q1="
                        + ds.get("q1") + " AU, Q3=" + ds.get("q3") + " AU)</p>");
            } else {
                w.println("<p class=\"note\">* No outliers detected (Q1="
                        + ds.get("q1") + " AU, Q3=" + ds.get("q3") + " AU)</p>");
            }
        }
    }

    private void printTidalLockByDistanceTable(PrintWriter w, Map<String, int[]> data) {
        w.println("<h3>Tidal Locking by Distance</h3>");
        w.println("<table>");
        w.println("<thead><tr><th>Distance Bin</th><th>Total</th><th>Locked</th><th>Lock Rate</th><th class=\"bar-col\">Distribution</th></tr></thead>");
        w.println("<tbody>");
        data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    int total = e.getValue()[0];
                    int locked = e.getValue()[1];
                    double rate = total > 0 ? (locked * 100.0 / total) : 0;
                    w.println("<tr><td>" + esc(e.getKey()) + "</td><td>" + fmt(total)
                            + "</td><td>" + fmt(locked) + "</td><td>" + String.format("%.1f", rate)
                            + "%</td><td>" + bar(rate) + "</td></tr>");
                });
        w.println("</tbody></table>");
    }

    private void printAtmosphereHtml(PrintWriter w) {
        w.println("<hr>");
        beginCollapsible(w, "Atmosphere & Magnetic Fields", 2);

        printSubSection(w, "Atmosphere Classifications (All Planets)");
        printSortedTable(w, planetData.getAtmosphereClassifications(), counts.getPlanetCount(), "Classification");

        printSubSection(w, "Tidal Locking");
        printSortedTable(w, planetData.getTidalLockCounts(), counts.getPlanetCount(), "Status");

        printSubSection(w, "Habitable Zone Positions");
        printSortedTable(w, planetData.getHzPositions(), counts.getPlanetCount(), "Position");

        printSubSection(w, "Magnetic Protection Levels");
        printSortedTable(w, planetData.getProtectionLevels(), counts.getPlanetCount(), "Level");

        printSubSection(w, "Magnetopause Distance Distribution");
        printSortedTableByKey(w, planetData.getMagnetopauseBins(), counts.getPlanetCount(), "Range (planet radii)");

        // Average Magnetopause by Distance
        w.println("<h3>Average Magnetopause by Distance from Star</h3>");
        w.println("<p class=\"note\">Should show compression (smaller magnetopause) closer to star</p>");
        w.println("<table>");
        w.println("<thead><tr><th>Distance Bin</th><th>Avg Magnetopause (radii)</th><th>Sample Count</th></tr></thead>");
        w.println("<tbody>");
        planetData.getDistanceVsMagnetopause().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    double avg = e.getValue()[0] / e.getValue()[1];
                    w.println("<tr><td>" + esc(e.getKey()) + "</td><td>"
                            + String.format("%.1f", avg) + "</td><td>" + (int) e.getValue()[1] + "</td></tr>");
                });
        w.println("</tbody></table>");

        printSubSection(w, "Atmospheric Loss Rate Distribution");
        printSortedTableByKey(w, planetData.getAtmLossRateBins(), counts.getPlanetCount(), "Range");

        printSubSection(w, "Auroral Frequency");
        printSortedTable(w, planetData.getAuroralFrequencies(), counts.getPlanetCount(), "Frequency");

        printSubSection(w, "Auroral Intensity");
        printSortedTable(w, planetData.getAuroralIntensities(), counts.getPlanetCount(), "Intensity");

        // Radiation Belt Intensity
        w.println("<h3>Radiation Belt Intensity (Inner / Outer)</h3>");
        w.println("<table>");
        w.println("<thead><tr><th>Inner Belt</th><th>Count</th><th>%</th></tr></thead><tbody>");
        planetData.getBeltIntensityInner().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> w.println("<tr><td>" + esc(e.getKey()) + "</td><td>" + e.getValue()
                        + "</td><td>" + pct(e.getValue(), counts.getPlanetCount()) + "%</td></tr>"));
        w.println("</tbody></table>");

        w.println("<table>");
        w.println("<thead><tr><th>Outer Belt</th><th>Count</th><th>%</th></tr></thead><tbody>");
        planetData.getBeltIntensityOuter().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> w.println("<tr><td>" + esc(e.getKey()) + "</td><td>" + e.getValue()
                        + "</td><td>" + pct(e.getValue(), counts.getPlanetCount()) + "%</td></tr>"));
        w.println("</tbody></table>");

        // Cross-reference tables
        w.println("<h3>Star Activity vs Rocky Planet Atmosphere Stripping</h3>");
        w.println("<p class=\"note\">Shows % of rocky planets that lost their atmosphere, grouped by parent star activity</p>");
        w.println("<table class=\"xref-table\">");
        w.println("<thead><tr><th>Star Activity</th><th>Total Rocky</th><th>Stripped (NONE)</th><th>Strip Rate</th></tr></thead>");
        w.println("<tbody>");
        planetData.getActivityVsAtmosphere().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    int total = e.getValue()[0];
                    int stripped = e.getValue()[1];
                    double rate = total > 0 ? (stripped * 100.0 / total) : 0;
                    w.println("<tr><td>" + esc(e.getKey()) + "</td><td>" + total
                            + "</td><td>" + stripped + "</td><td>" + String.format("%.1f", rate) + "%</td></tr>");
                });
        w.println("</tbody></table>");

        w.println("<h3>Star Activity vs Protection Level (Rocky Planets)</h3>");
        w.println("<p class=\"note\">Expectation: HYPERACTIVE/VERY_ACTIVE stars should shift protection toward NONE/MINIMAL</p>");
        w.println("<table class=\"xref-table\">");
        w.println("<thead><tr><th>Star Activity</th><th>Total</th><th>NONE</th><th>MINIMAL</th><th>MODERATE</th><th>STRONG</th><th>EXCEPTIONAL</th></tr></thead>");
        w.println("<tbody>");
        planetData.getActivityVsProtection().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    int[] c = e.getValue();
                    w.println("<tr><td>" + esc(e.getKey()) + "</td><td>" + c[0] + "</td><td>" + c[1]
                            + "</td><td>" + c[2] + "</td><td>" + c[3] + "</td><td>" + c[4] + "</td><td>" + c[5] + "</td></tr>");
                });
        w.println("</tbody></table>");

        endCollapsible(w);
    }

    private void printGeologyHtml(PrintWriter w) {
        if (planetData.getGeologicalActivity().isEmpty() && planetData.getTectonicLevels().isEmpty()) return;

        w.println("<hr>");
        beginCollapsible(w, "Geology (Rocky/Surface Planets)", 2);
        w.println("<p>Planets with geological data: " + fmt(planetData.getPlanetsWithGeology()) + "</p>");

        if (!planetData.getGeologicalActivity().isEmpty()) {
            printSubSection(w, "Geological Activity");
            printSortedTable(w, planetData.getGeologicalActivity(), planetData.getPlanetsWithGeology(), "Activity");
        }
        if (!planetData.getTectonicLevels().isEmpty()) {
            printSubSection(w, "Tectonic Activity Level");
            printSortedTable(w, planetData.getTectonicLevels(), planetData.getPlanetsWithGeology(), "Level");
        }
        if (!planetData.getVolcanismTypes().isEmpty()) {
            printSubSection(w, "Volcanism Type");
            printSortedTable(w, planetData.getVolcanismTypes(), planetData.getPlanetsWithGeology(), "Type");
        }

        endCollapsible(w);
    }

    private void printWaterAndHabHtml(PrintWriter w) {
        // Water System
        w.println("<hr>");
        beginCollapsible(w, "Water System (Rocky/Surface Planets Only)", 2);
        w.println("<p>Total rocky/surface planets analyzed: " + fmt(planetData.getTotalRockyPlanets()) + "</p>");
        w.println("<p>With liquid surface water: " + fmt(planetData.getPlanetsWithLiquidWater())
                + " (" + pct(planetData.getPlanetsWithLiquidWater(), planetData.getTotalRockyPlanets()) + "%)</p>");
        w.println("<p>With ice coverage: " + fmt(planetData.getPlanetsWithIce())
                + " (" + pct(planetData.getPlanetsWithIce(), planetData.getTotalRockyPlanets()) + "%)</p>");
        w.println("<p>With subsurface water: " + fmt(planetData.getPlanetsWithSubsurfaceWater())
                + " (" + pct(planetData.getPlanetsWithSubsurfaceWater(), planetData.getTotalRockyPlanets()) + "%)</p>");

        printSubSection(w, "Water Inventory Distribution");
        printSortedTable(w, planetData.getWaterInventories(), planetData.getTotalRockyPlanets(), "Inventory");

        printSubSection(w, "Water Phase at Surface");
        printSortedTable(w, planetData.getWaterPhases(), planetData.getHabAssessmentCount(), "Phase");

        endCollapsible(w);

        // Habitability
        w.println("<hr>");
        beginCollapsible(w, "Planetary Habitability", 2);
        w.println("<p>Planets assessed: " + fmt(planetData.getHabAssessmentCount()) + "</p>");
        w.println("<p>Average ESI: " + String.format("%.3f", planetData.getEsiSum() / Math.max(1, planetData.getHabAssessmentCount())) + "</p>");
        w.println("<p>Average Habitability Score: " + String.format("%.1f", planetData.getHabScoreSum() / Math.max(1, planetData.getHabAssessmentCount())) + "</p>");
        w.println("<p>Breathable atmospheres: " + fmt(planetData.getBreathablePlanets())
                + " (" + String.format("%.2f", planetData.getBreathablePlanets() * 100.0 / Math.max(1, planetData.getHabAssessmentCount())) + "%)</p>");

        printSubSection(w, "Habitability Class Distribution");
        printSortedTable(w, planetData.getHabitabilityClasses(), planetData.getHabAssessmentCount(), "Class");

        printSubSection(w, "Colonization Suitability");
        printSortedTable(w, planetData.getColonizationSuitabilities(), planetData.getHabAssessmentCount(), "Suitability");

        printSubSection(w, "Terraforming Potential");
        printSortedTable(w, planetData.getTerraformingPotentials(), planetData.getHabAssessmentCount(), "Potential");

        printSubSection(w, "Biosignature Potential");
        printSortedTable(w, planetData.getBiosignaturePotentials(), planetData.getHabAssessmentCount(), "Potential");

        printSubSection(w, "Life Complexity Potential");
        printSortedTable(w, planetData.getLifeComplexityPotentials(), planetData.getHabAssessmentCount(), "Potential");

        endCollapsible(w);
    }

    private void printClimateHtml(PrintWriter w) {
        PlanetDataCollector.WeatherBucket all = planetData.getClimateAll();
        PlanetDataCollector.WeatherBucket surface = planetData.getClimateSurface();
        PlanetDataCollector.WeatherBucket gas = planetData.getClimateGas();

        w.println("<hr>");
        beginCollapsible(w, "Planetary Climate", 2);

        if (all.getCount() == 0) {
            w.println("<p>No planets with climate data.</p>");
            endCollapsible(w);
            return;
        }

        w.println("<div class=\"stats-grid\">");
        statCard(w, String.valueOf(all.getCount()), "Total");
        statCard(w, String.valueOf(surface.getCount()), "Surface");
        statCard(w, String.valueOf(gas.getCount()), "Gas / Ice");
        statCard(w, String.format("%.1f%%", all.getTotalCloudCoverage() / all.getCount()), "Avg Cloud Cover");
        statCard(w, all.getWindSpeedCount() > 0 ? String.format("%.1f m/s", all.getTotalWindSpeed() / all.getWindSpeedCount()) : "N/A", "Avg Wind Speed");
        statCard(w, String.valueOf(all.getExtremeEventTotal()), "Extreme Events");
        w.println("</div>");

        if (surface.getCount() > 0) {
            printClimateBucketHtml(w, surface, "Surface Planet Climate");
        }
        if (gas.getCount() > 0) {
            printClimateBucketHtml(w, gas, "Gas / Ice Giant Climate");
        }

        endCollapsible(w);
    }

    private void printClimateBucketHtml(PrintWriter w, PlanetDataCollector.WeatherBucket b, String title) {
        w.println("<hr>");
        beginCollapsible(w, title, 3);

        w.println("<div class=\"stats-grid\">");
        statCard(w, String.valueOf(b.getCount()), "Planets");
        statCard(w, b.getWithPrecipitation() + " (" + pct(b.getWithPrecipitation(), b.getCount()) + "%)", "Precipitation");
        statCard(w, b.getWithDustStorms() + " (" + pct(b.getWithDustStorms(), b.getCount()) + "%)", "Dust Storms");
        statCard(w, b.getWithLightning() + " (" + pct(b.getWithLightning(), b.getCount()) + "%)", "Lightning");
        statCard(w, b.getWithSuperRotation() + " (" + pct(b.getWithSuperRotation(), b.getCount()) + "%)", "Super-Rotation");
        statCard(w, b.getWithGreatDarkSpot() + " (" + pct(b.getWithGreatDarkSpot(), b.getCount()) + "%)", "Great Dark Spot");
        statCard(w, String.format("%.1f%%", b.getTotalCloudCoverage() / b.getCount()), "Avg Cloud Cover");
        if (b.getWindSpeedCount() > 0) {
            statCard(w, String.format("%.1f m/s", b.getTotalWindSpeed() / b.getWindSpeedCount()), "Avg Wind Speed");
        }
        statCard(w, String.format("%.1f", b.getCloudLayerTotal() * 1.0 / b.getCount()), "Avg Cloud Layers");
        statCard(w, String.format("%.1f", b.getPrecipTypeTotal() * 1.0 / b.getCount()), "Avg Precip Types");
        statCard(w, b.getExtremeEventTotal() + " (avg " + String.format("%.1f", b.getExtremeEventTotal() * 1.0 / b.getCount()) + ")", "Extreme Events");
        statCard(w, String.valueOf(b.getEclipseTotal()), "Eclipse Configs");
        w.println("</div>");

        printSubSection(w, "Sky Color");
        printSortedTable(w, b.getSkyColors(), b.getCount(), "Sky Color");

        printSubSection(w, "Cloud Coverage Class");
        printSortedTable(w, b.getCloudCoverageClass(), b.getCount(), "Coverage");

        printSubSection(w, "Wind Intensity");
        printSortedTable(w, b.getWindIntensity(), b.getCount(), "Intensity");

        printSubSection(w, "Circulation Pattern");
        printSortedTable(w, b.getCirculationPattern(), b.getCount(), "Pattern");

        printSubSection(w, "Storm Frequency");
        printSortedTable(w, b.getStormFrequency(), b.getCount(), "Frequency");

        if (!b.getLightningType().isEmpty()) {
            printSubSection(w, "Lightning Type");
            printSortedTable(w, b.getLightningType(), b.getWithLightning(), "Type");
        }

        printSubSection(w, "Climate Severity");
        printSortedTable(w, b.getSeverity(), b.getCount(), "Severity");

        printSubSection(w, "Outdoor Exposure Rating");
        printSortedTable(w, b.getExposureRating(), b.getCount(), "Rating");

        if (!b.getTidalRangeBins().isEmpty()) {
            printSubSection(w, "Tidal Range Distribution");
            printSortedTableByKey(w, b.getTidalRangeBins(), b.getCount(), "Tidal Range");
        }

        endCollapsible(w);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Moon HTML
    // ═══════════════════════════════════════════════════════════════

    private void printMoonHtml(PrintWriter w) {
        w.println("<hr>");
        beginCollapsible(w, "Moon Types", 2);
        printSortedTable(w, moonData.getMoonTypes(), counts.getMoonCount(), "Moon Type");

        printSubSection(w, "Moon Composition Types");
        printSortedTable(w, moonData.getMoonCompositionTypes(), counts.getMoonCount(), "Composition");

        printSubSection(w, "Tidal Heating Levels");
        printSortedTable(w, moonData.getMoonTidalHeatingLevels(), counts.getMoonCount(), "Level");

        printSubSection(w, "Orbit Distance (Planet Radii)");
        printSortedTableByKey(w, moonData.getOrbitDistanceBins(), counts.getMoonCount(), "Distance Bin");

        printSubSection(w, "Eccentricity Distribution");
        printSortedTableByKey(w, moonData.getEccentricityBins(), counts.getMoonCount(), "Eccentricity");

        printSubSection(w, "Tidal Heating by Planet Type");
        printNestedTidalTable(w, moonData.getTidalHeatingByPlanetType());

        printSubSection(w, "Tidal Heating by Moon Type");
        printNestedTidalTable(w, moonData.getTidalHeatingByMoonType());

        printSubSection(w, "Geological Activity");
        printSortedTable(w, moonData.getGeologicalActivity(), counts.getMoonCount(), "Activity");

        printSubSection(w, "Atmosphere Classifications");
        printSortedTable(w, moonData.getAtmosphereClassifications(), counts.getMoonCount(), "Classification");

        w.println("<p>Subsurface Oceans (from MoonCreator): " + fmt(moonData.getMoonsWithSubsurfaceOcean())
                + " (" + pct(moonData.getMoonsWithSubsurfaceOcean(), counts.getMoonCount()) + "%)</p>");

        // Detailed Analysis
        beginCollapsible(w, "Moon Detailed Analysis (mass >= 0.0005 Earth)", 3);
        w.println("<p>Moons assessed: " + fmt(moonData.getMoonsAssessed()) + " of " + fmt(counts.getMoonCount())
                + " total (" + pct(moonData.getMoonsAssessed(), counts.getMoonCount()) + "%)</p>");

        w.println("<h4>Moon Magnetic Fields</h4>");
        w.println("<p>Moons with magnetic field data: " + fmt(moonData.getMoonsWithMagField())
                + " (" + pct(moonData.getMoonsWithMagField(), moonData.getMoonsAssessed()) + "% of assessed)</p>");
        printSortedTable(w, moonData.getMoonDynamoTypes(), moonData.getMoonsWithMagField(), "Dynamo Type");
        printSortedTable(w, moonData.getMoonProtectionLevels(), moonData.getMoonsWithMagField(), "Protection Level");

        w.println("<h4>Moon Water System</h4>");
        w.println("<p>With liquid surface water: " + fmt(moonData.getMoonsWithLiquidWater())
                + " (" + pct(moonData.getMoonsWithLiquidWater(), counts.getMoonCount()) + "%)</p>");
        w.println("<p>With ice coverage: " + fmt(moonData.getMoonsWithIce())
                + " (" + pct(moonData.getMoonsWithIce(), counts.getMoonCount()) + "%)</p>");
        w.println("<p>With subsurface water: " + fmt(moonData.getMoonsWithSubsurfaceWater())
                + " (" + pct(moonData.getMoonsWithSubsurfaceWater(), counts.getMoonCount()) + "%)</p>");
        printSortedTable(w, moonData.getMoonWaterInventories(), counts.getMoonCount(), "Water Inventory");

        w.println("<h4>Moon Habitability</h4>");
        w.println("<p>Moons with habitability assessment: " + fmt(moonData.getMoonHabCount()) + "</p>");
        w.println("<p>Average ESI: " + String.format("%.4f", moonData.getMoonEsiSum() / Math.max(1, moonData.getMoonHabCount())) + "</p>");
        w.println("<p>Average Habitability Score: " + String.format("%.1f", moonData.getMoonHabScoreSum() / Math.max(1, moonData.getMoonHabCount())) + "</p>");

        printSortedTable(w, moonData.getMoonHabitabilityClasses(), moonData.getMoonHabCount(), "Habitability Class");
        printSortedTable(w, moonData.getMoonColonizationSuitabilities(), moonData.getMoonHabCount(), "Colonization Suitability");
        printSortedTable(w, moonData.getMoonBiosignaturePotentials(), moonData.getMoonHabCount(), "Biosignature Potential");
        printSortedTable(w, moonData.getMoonLifeComplexity(), moonData.getMoonHabCount(), "Life Complexity");
        printSortedTable(w, moonData.getMoonRadiationBeltDose(), moonData.getMoonHabCount(), "Radiation Belt Surface Dose");
        printSortedTable(w, moonData.getMoonTidalContribution(), moonData.getMoonHabCount(), "Tidal Heating Contribution");

        // Moon Climate
        printMoonClimateHtml(w);

        endCollapsible(w); // close detailed analysis
        endCollapsible(w); // close Moon Types
    }

    private void printNestedTidalTable(PrintWriter w, Map<String, Map<String, Integer>> data) {
        w.println("<table class=\"xref-table\">");
        w.println("<thead><tr><th>Type</th><th>NONE</th><th>LOW</th><th>MODERATE</th><th>HIGH</th><th>EXTREME</th><th>Total</th></tr></thead>");
        w.println("<tbody>");
        data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    Map<String, Integer> levels = e.getValue();
                    int none = levels.getOrDefault("NONE", 0);
                    int low = levels.getOrDefault("LOW", 0);
                    int moderate = levels.getOrDefault("MODERATE", 0);
                    int high = levels.getOrDefault("HIGH", 0);
                    int extreme = levels.getOrDefault("EXTREME", 0);
                    int total = none + low + moderate + high + extreme;
                    w.println("<tr><td>" + esc(e.getKey()) + "</td><td>" + fmt(none) + "</td><td>"
                            + fmt(low) + "</td><td>" + fmt(moderate) + "</td><td>" + fmt(high) + "</td><td>"
                            + fmt(extreme) + "</td><td>" + fmt(total) + "</td></tr>");
                });
        w.println("</tbody></table>");
    }

    private void printMoonClimateHtml(PrintWriter w) {
        w.println("<h4>Moon Climate</h4>");
        w.println("<p>Moons with climate data: " + fmt(moonData.getMoonsWithClimate()) + " of " + fmt(counts.getMoonCount())
                + " total (" + pct(moonData.getMoonsWithClimate(), counts.getMoonCount()) + "%)</p>");
        if (moonData.getMoonsWithClimate() == 0) return;

        w.println("<p>With precipitation: " + fmt(moonData.getMoonsWithPrecipitation())
                + " (" + pct(moonData.getMoonsWithPrecipitation(), moonData.getMoonsWithClimate()) + "%)</p>");
        w.println("<p>With lightning: " + fmt(moonData.getMoonsWithLightning())
                + " (" + pct(moonData.getMoonsWithLightning(), moonData.getMoonsWithClimate()) + "%)</p>");
        w.println("<p>With parent planet visible in sky: " + fmt(moonData.getMoonsWithParentPlanetVisible())
                + " (" + pct(moonData.getMoonsWithParentPlanetVisible(), moonData.getMoonsWithClimate()) + "%)</p>");
        w.println("<p>With planetary eclipses: " + fmt(moonData.getMoonsWithPlanetaryEclipses())
                + " (" + pct(moonData.getMoonsWithPlanetaryEclipses(), moonData.getMoonsWithClimate()) + "%)</p>");
        w.println("<p>Total extreme climate events: " + fmt(moonData.getMoonExtremeEventTotal())
                + " (avg " + String.format("%.1f", moonData.getMoonExtremeEventTotal() * 1.0 / moonData.getMoonsWithClimate()) + "/moon)</p>");

        printSortedTable(w, moonData.getMoonClimateSkyColor(), moonData.getMoonsWithClimate(), "Sky Color");
        printSortedTable(w, moonData.getMoonClimateCloudClass(), moonData.getMoonsWithClimate(), "Cloud Coverage");
        printSortedTable(w, moonData.getMoonClimateWindIntensity(), moonData.getMoonsWithClimate(), "Wind Intensity");
        printSortedTable(w, moonData.getMoonClimateSeverity(), moonData.getMoonsWithClimate(), "Climate Severity");
        printSortedTable(w, moonData.getMoonClimateExposure(), moonData.getMoonsWithClimate(), "Exposure Rating");

        if (!moonData.getMoonClimateTidalRangeBins().isEmpty()) {
            w.println("<h4>Moon Tidal Range (from Parent Planet + Siblings)</h4>");
            printSortedTableByKey(w, moonData.getMoonClimateTidalRangeBins(), moonData.getMoonsWithClimate(), "Tidal Range");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Orbital Stability HTML
    // ═══════════════════════════════════════════════════════════════

    private void printOrbitalStabilityHtml(PrintWriter w) {
        w.println("<hr>");
        beginCollapsible(w, "Orbital Stability", 2);

        int totalPairs = stabilityData.getTotalAdjacentPairs();
        int multiPlanetSystems = stabilityData.getSystemsWithMultiplePlanets();

        if (totalPairs == 0) {
            w.println("<p>No adjacent planet pairs found for stability analysis.</p>");
            endCollapsible(w);
            return;
        }

        // Summary stat cards
        w.println("<div class=\"stats-grid\">");
        statCard(w, fmt(totalPairs), "Adjacent Pairs");
        statCard(w, fmt(multiPlanetSystems), "Multi-Planet Systems");
        statCard(w, pct(stabilityData.getSystemsAllStable(), multiPlanetSystems) + "%", "All-Stable Systems");
        statCard(w, fmt(stabilityData.getCrossingPairCount()), "Orbit Crossings");
        statCard(w, String.format("%.2f", stabilityData.getMeanGladmanDelta()), "Mean Gladman \u0394");
        statCard(w, String.format("%.2f", stabilityData.getMedianGladmanDelta()), "Median Gladman \u0394");
        statCard(w, fmt(stabilityData.getDoomedButAliveCount()), "Doomed But Alive");
        statCard(w, String.format("%.1f%%", stabilityData.getGladmanFloorOverridePercent()), "Need Wider Spacing");
        w.println("</div>");

        // Section 1: Stability Classification
        printSubSection(w, "Stability Classification");
        w.println("<p class=\"note\">CROSSING = orbits intersect, UNSTABLE = within mutual Hill sphere, "
                + "MARGINAL = gap/mutualHill &lt; 2, STABLE = gap/mutualHill &ge; 2</p>");
        printSortedTable(w, stabilityData.getStabilityClassification(), totalPairs, "Classification");

        // Stability by Star Type
        printSubSection(w, "Stability by Star Type");
        w.println("<p class=\"note\">M-dwarfs pack planets tighter and are the primary stress test for stability</p>");
        printStabilityCrossRefTable(w, stabilityData.getStabilityByStarType());

        // Stability by Orbital Position
        printSubSection(w, "Stability by Pair Position (Inner Planet Index)");
        w.println("<p class=\"note\">Position 1 = innermost pair (planets 1&#x2194;2), position 2 = next pair (2&#x2194;3), etc.</p>");
        printStabilityByPositionTable(w, stabilityData.getStabilityByOrbitalPosition());

        // Stability by Binary Configuration
        printSubSection(w, "Stability by System Configuration");
        w.println("<p class=\"note\">P_TYPE (circumbinary) should be hardest for stability</p>");
        printStabilityCrossRefTable(w, stabilityData.getStabilityByBinaryConfig());

        // Section 2: Gladman Delta Distribution
        printSubSection(w, "Gladman \u0394 Distribution");
        w.println("<p class=\"note\">Gladman separation ratio: gap between orbits divided by mutual Hill sphere (\u0394 = 3.46). "
                + "Values below 3.46 indicate pairs within the critical mutual Hill sphere radius.</p>");
        printSortedTableByKey(w, stabilityData.getGladmanDeltaBins(), totalPairs, "Gladman \u0394 Range");
        w.println("<p>Systems with any pair below \u0394 = 3.46: <strong>"
                + fmt(stabilityData.getSystemsWithAnyPairBelowCritical())
                + "</strong> of " + fmt(multiPlanetSystems) + " ("
                + pct(stabilityData.getSystemsWithAnyPairBelowCritical(), multiPlanetSystems) + "%)</p>");
        w.println("<p>Mean per-system min \u0394: <strong>"
                + String.format("%.2f", stabilityData.getMeanPerSystemMinDelta()) + "</strong></p>");
        w.println("<p>Median min SMA gap per system: <strong>"
                + String.format("%.4f", stabilityData.getMedianMinSmaGapAU()) + " AU</strong></p>");

        // Section 3: Orbit Crossing Detection
        printSubSection(w, "Orbit Crossing Detection");
        if (stabilityData.getCrossingPairCount() == 0) {
            w.println("<p>No orbit crossings detected.</p>");
        } else {
            w.println("<p>Total crossing pairs: <strong>" + fmt(stabilityData.getCrossingPairCount())
                    + "</strong> of " + fmt(totalPairs) + " ("
                    + pct(stabilityData.getCrossingPairCount(), totalPairs) + "%)</p>");
            if (!stabilityData.getCrossingByPlanetTypePair().isEmpty()) {
                w.println("<h4>Crossings by Planet Type Pair</h4>");
                printSortedTable(w, stabilityData.getCrossingByPlanetTypePair(),
                        stabilityData.getCrossingPairCount(), "Planet Type Pair");
            }
            if (!stabilityData.getCrossingDetails().isEmpty()) {
                w.println("<h4>Crossing Details (first " + Math.min(20, stabilityData.getCrossingDetails().size()) + ")</h4>");
                w.println("<table>");
                w.println("<thead><tr><th>#</th><th>Clearance (AU)</th><th>Inner Ecc</th><th>Outer Ecc</th></tr></thead>");
                w.println("<tbody>");
                int limit = Math.min(20, stabilityData.getCrossingDetails().size());
                for (int i = 0; i < limit; i++) {
                    double[] d = stabilityData.getCrossingDetails().get(i);
                    w.println("<tr><td>" + (i + 1) + "</td><td>" + String.format("%.6f", d[0])
                            + "</td><td>" + String.format("%.4f", d[1])
                            + "</td><td>" + String.format("%.4f", d[2]) + "</td></tr>");
                }
                w.println("</tbody></table>");
            }
        }

        // Section 4: Eccentricity Validation
        printSubSection(w, "Eccentricity Distribution");
        w.println("<p class=\"note\">Current generator: uniform random [0, 0.2]. No neighbor cap or Gladman floor on eccentricity.</p>");
        printSortedTableByKey(w, stabilityData.getEccentricityBins(),
                getTotalFromMap(stabilityData.getEccentricityBins()), "Eccentricity Range");

        printSubSection(w, "Mean Eccentricity by Orbital Position");
        w.println("<p class=\"note\">Inner planets should trend toward lower eccentricity in stable systems</p>");
        printEccentricityByPositionTable(w, stabilityData.getEccentricityByPosition());

        // Section 5: Timescale Analysis
        printSubSection(w, "Stability Timescale Estimates");
        w.println("<p class=\"note\">Chambers et al. (1996) approximation: "
                + "\u03C4 \u2248 P<sub>inner</sub> \u00D7 e<sup>(0.6 \u00D7 \u0394)</sup>. "
                + "Rough estimate; actual N-body timescales may differ by orders of magnitude.</p>");
        printSortedTableByKey(w, stabilityData.getTimescaleBins(), totalPairs, "Timescale Range");

        printSubSection(w, "Timescale vs System Age");
        w.println("<p class=\"note\">\"should_not_exist\" = timescale &lt; star age (physically implausible); "
                + "\"doomed_but_alive\" = 2-10\u00D7 age (interesting for worldbuilding)</p>");
        printSortedTable(w, stabilityData.getTimescaleVsAgeBins(), totalPairs, "Category");
        w.println("<p>Total pairs with timescale &lt; star age: <strong>"
                + fmt(stabilityData.getDoomedButAliveCount()) + "</strong> of " + fmt(totalPairs)
                + " (" + pct(stabilityData.getDoomedButAliveCount(), totalPairs) + "%)</p>");

        // Section 6: Spacing Metrics
        printSubSection(w, "SMA Ratio Distribution (Adjacent Pairs)");
        w.println("<p class=\"note\">Kepler systems peak around 1.5-2.0\u00D7. "
                + "Current Titius-Bode spacing: inner 1.3-2.0\u00D7, outer 1.5-3.0\u00D7 (\u00B115% variance)</p>");
        printSortedTableByKey(w, stabilityData.getSmaRatioBins(), totalPairs, "SMA Ratio (a\u2099\u208A\u2081/a\u2099)");
        w.println("<p>Pairs where Gladman floor (\u0394 &lt; 3.46) would require wider spacing: <strong>"
                + fmt(stabilityData.getPairsWhereGladmanFloorWouldWiden()) + "</strong> of "
                + fmt(stabilityData.getTotalSpacingPairsChecked()) + " ("
                + String.format("%.1f", stabilityData.getGladmanFloorOverridePercent()) + "%)</p>");

        // Section 7: System-Level Summary
        printSubSection(w, "System-Level Summary");
        w.println("<h4>Planets Per System</h4>");
        printSortedTableByKey(w, stabilityData.getPlanetsPerSystemBins(),
                counts.getSystemCount(), "Planets Per System");
        w.println("<p>Systems with all pairs STABLE: <strong>" + fmt(stabilityData.getSystemsAllStable())
                + "</strong> of " + fmt(multiPlanetSystems) + " multi-planet systems ("
                + pct(stabilityData.getSystemsAllStable(), multiPlanetSystems) + "%)</p>");
        w.println("<p>Systems with any MARGINAL or worse: <strong>"
                + fmt(stabilityData.getSystemsAnyMarginalOrWorse()) + "</strong> ("
                + pct(stabilityData.getSystemsAnyMarginalOrWorse(), multiPlanetSystems) + "%)</p>");
        w.println("<p>Systems with any CROSSING: <strong>" + fmt(stabilityData.getSystemsAnyCrossing())
                + "</strong> (" + pct(stabilityData.getSystemsAnyCrossing(), multiPlanetSystems) + "%)</p>");
        if (stabilityData.getMaxSmaCount() > 0) {
            w.println("<p>Average system outer extent (max SMA): <strong>"
                    + String.format("%.2f", stabilityData.getMaxSmaSum() / stabilityData.getMaxSmaCount()) + " AU</strong></p>");
        }
        w.println("<h4>System Outer Extent</h4>");
        printSortedTableByKey(w, stabilityData.getSystemOuterExtentBins(), counts.getSystemCount(), "Outer Extent");

        // Section 8: Belt Stability
        if (stabilityData.getTotalBeltsAnalyzed() > 0) {
            printSubSection(w, "Belt Stability Analysis");
            w.println("<p class=\"note\">Checks belt-belt overlaps, belt-planet intersections, "
                    + "and binary stability limit violations</p>");

            w.println("<div class=\"stats-grid\">");
            statCard(w, fmt(stabilityData.getTotalBeltsAnalyzed()), "Belts Analyzed");
            statCard(w, fmt(stabilityData.getBeltOverlapCount()), "Belt-Belt Overlaps");
            statCard(w, fmt(stabilityData.getBeltPlanetOverlapCount()), "Belt-Planet Overlaps");
            statCard(w, fmt(stabilityData.getBeltsExceedingStabilityLimit()), "Exceed S-Type Limit");
            statCard(w, fmt(stabilityData.getBeltsBelowCavityLimit()), "Below P-Type Cavity");
            w.println("</div>");

            if (!stabilityData.getBeltOverlapDetails().isEmpty()) {
                w.println("<h4>Belt-Belt Overlap Details (first " + stabilityData.getBeltOverlapDetails().size() + ")</h4>");
                w.println("<table>");
                w.println("<thead><tr><th>#</th><th>Overlap Description</th></tr></thead>");
                w.println("<tbody>");
                for (int i = 0; i < stabilityData.getBeltOverlapDetails().size(); i++) {
                    w.println("<tr><td>" + (i + 1) + "</td><td>" + esc(stabilityData.getBeltOverlapDetails().get(i)) + "</td></tr>");
                }
                w.println("</tbody></table>");
            }

            if (!stabilityData.getBeltPlanetOverlapDetails().isEmpty()) {
                w.println("<h4>Belt-Planet Overlap Details (first " + stabilityData.getBeltPlanetOverlapDetails().size() + ")</h4>");
                w.println("<table>");
                w.println("<thead><tr><th>#</th><th>Overlap Description</th></tr></thead>");
                w.println("<tbody>");
                for (int i = 0; i < stabilityData.getBeltPlanetOverlapDetails().size(); i++) {
                    w.println("<tr><td>" + (i + 1) + "</td><td>" + esc(stabilityData.getBeltPlanetOverlapDetails().get(i)) + "</td></tr>");
                }
                w.println("</tbody></table>");
            }
        }

        endCollapsible(w);
    }

    private void printStabilityCrossRefTable(PrintWriter w, Map<String, Map<String, Integer>> data) {
        w.println("<table class=\"xref-table\">");
        w.println("<thead><tr><th>Category</th><th>STABLE</th><th>MARGINAL</th><th>UNSTABLE</th><th>CROSSING</th><th>Total</th></tr></thead>");
        w.println("<tbody>");
        data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    Map<String, Integer> cats = e.getValue();
                    int stable = cats.getOrDefault("STABLE", 0);
                    int marginal = cats.getOrDefault("MARGINAL", 0);
                    int unstable = cats.getOrDefault("UNSTABLE", 0);
                    int crossing = cats.getOrDefault("CROSSING", 0);
                    int total = stable + marginal + unstable + crossing;
                    w.println("<tr><td>" + esc(e.getKey()) + "</td><td>" + fmt(stable)
                            + "</td><td>" + fmt(marginal) + "</td><td>" + fmt(unstable)
                            + "</td><td>" + fmt(crossing) + "</td><td>" + fmt(total) + "</td></tr>");
                });
        w.println("</tbody></table>");
    }

    private void printStabilityByPositionTable(PrintWriter w, Map<Integer, Map<String, Integer>> data) {
        w.println("<table class=\"xref-table\">");
        w.println("<thead><tr><th>Pair Position</th><th>STABLE</th><th>MARGINAL</th><th>UNSTABLE</th><th>CROSSING</th><th>Total</th></tr></thead>");
        w.println("<tbody>");
        data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    Map<String, Integer> cats = e.getValue();
                    int stable = cats.getOrDefault("STABLE", 0);
                    int marginal = cats.getOrDefault("MARGINAL", 0);
                    int unstable = cats.getOrDefault("UNSTABLE", 0);
                    int crossing = cats.getOrDefault("CROSSING", 0);
                    int total = stable + marginal + unstable + crossing;
                    w.println("<tr><td>" + e.getKey() + " &#x2194; " + (e.getKey() + 1) + "</td><td>" + fmt(stable)
                            + "</td><td>" + fmt(marginal) + "</td><td>" + fmt(unstable)
                            + "</td><td>" + fmt(crossing) + "</td><td>" + fmt(total) + "</td></tr>");
                });
        w.println("</tbody></table>");
    }

    private void printEccentricityByPositionTable(PrintWriter w, Map<String, double[]> data) {
        w.println("<table>");
        w.println("<thead><tr><th>Position Group</th><th>Mean Eccentricity</th><th>Sample Count</th></tr></thead>");
        w.println("<tbody>");
        data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    double avg = e.getValue()[1] > 0 ? e.getValue()[0] / e.getValue()[1] : 0;
                    w.println("<tr><td>" + esc(e.getKey()) + "</td><td>"
                            + String.format("%.4f", avg) + "</td><td>" + (int) e.getValue()[1] + "</td></tr>");
                });
        w.println("</tbody></table>");
    }

    private int getTotalFromMap(Map<String, Integer> map) {
        return map.values().stream().mapToInt(Integer::intValue).sum();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Ring & Belt HTML
    // ═══════════════════════════════════════════════════════════════

    private void printRingHtml(PrintWriter w) {
        w.println("<hr>");
        beginCollapsible(w, "Ring Types", 2);

        // Summary stat cards
        w.println("<div class=\"stats-grid\">");
        statCard(w, fmt(counts.getRingCount()), "Total Rings");
        statCard(w, fmt(ringData.getShepherdMoonCount()), "With Shepherd Moons");
        statCard(w, fmt(ringData.getRingsWithGaps()), "With Gaps");
        w.println("</div>");

        printSortedTable(w, ringData.getRingTypes(), counts.getRingCount(), "Ring Type");

        if (!ringData.getOpticalDepthBins().isEmpty()) {
            printSubSection(w, "Optical Depth Distribution");
            printSortedTableByKey(w, ringData.getOpticalDepthBins(), counts.getRingCount(), "Optical Depth");
        }

        if (!ringData.getColorDistribution().isEmpty()) {
            printSubSection(w, "Ring Colors");
            printSortedTable(w, ringData.getColorDistribution(), counts.getRingCount(), "Color");
        }

        if (!ringData.getVisibilityDistribution().isEmpty()) {
            printSubSection(w, "Visibility");
            printSortedTable(w, ringData.getVisibilityDistribution(), counts.getRingCount(), "Visibility");
        }

        if (!ringData.getStabilityDistribution().isEmpty()) {
            printSubSection(w, "Ring Stability");
            printSortedTable(w, ringData.getStabilityDistribution(), counts.getRingCount(), "Stability");
        }

        if (!ringData.getOriginTypes().isEmpty()) {
            printSubSection(w, "Formation Origin");
            printSortedTable(w, ringData.getOriginTypes(), counts.getRingCount(), "Origin");
        }

        if (!ringData.getThicknessBins().isEmpty()) {
            printSubSection(w, "Ring Thickness");
            printSortedTableByKey(w, ringData.getThicknessBins(), counts.getRingCount(), "Thickness");
        }

        if (!ringData.getParticleSizeBins().isEmpty()) {
            printSubSection(w, "Maximum Particle Size");
            printSortedTableByKey(w, ringData.getParticleSizeBins(), counts.getRingCount(), "Particle Size");
        }

        if (!ringData.getAgeBins().isEmpty()) {
            printSubSection(w, "Ring Age Distribution");
            printSortedTableByKey(w, ringData.getAgeBins(), counts.getRingCount(), "Age Range");
        }

        if (!ringData.getParentPlanetTypes().isEmpty()) {
            printSubSection(w, "Rings by Parent Planet Type");
            printSortedTable(w, ringData.getParentPlanetTypes(), counts.getRingCount(), "Planet Type");
        }

        // Per-type breakdown
        if (!ringData.getPerTypeData().isEmpty()) {
            printSubSection(w, "Per Ring Type Breakdown");
            for (Map.Entry<String, RingDataCollector.RingTypeBreakdown> entry : ringData.getPerTypeData().entrySet()) {
                RingDataCollector.RingTypeBreakdown rtb = entry.getValue();
                beginCollapsible(w, entry.getKey() + " (" + rtb.getCount() + ")", 3);
                w.println("<p>Shepherd moons: " + rtb.getShepherdCount()
                        + " (" + pct(rtb.getShepherdCount(), rtb.getCount()) + "%)"
                        + " | Gaps: " + rtb.getGapsCount()
                        + " (" + pct(rtb.getGapsCount(), rtb.getCount()) + "%)</p>");
                if (rtb.getOpticalDepthCount() > 0) {
                    w.println("<p>Avg optical depth: " + String.format("%.3f", rtb.getOpticalDepthSum() / rtb.getOpticalDepthCount()) + "</p>");
                }
                if (!rtb.getColors().isEmpty()) printSortedTable(w, rtb.getColors(), rtb.getCount(), "Color");
                if (!rtb.getVisibilities().isEmpty()) printSortedTable(w, rtb.getVisibilities(), rtb.getCount(), "Visibility");
                if (!rtb.getStabilities().isEmpty()) printSortedTable(w, rtb.getStabilities(), rtb.getCount(), "Stability");
                if (!rtb.getOrigins().isEmpty()) printSortedTable(w, rtb.getOrigins(), rtb.getCount(), "Origin");
                endCollapsible(w);
            }
        }

        endCollapsible(w);
    }

    private void printTrojanHtml(PrintWriter w) {
        w.println("<hr>");
        beginCollapsible(w, "Trojan Swarms", 2);

        // Summary stat cards
        w.println("<div class=\"stats-grid\">");
        statCard(w, fmt(trojanData.getTotalTrojans()), "Total Swarms");
        statCard(w, fmt(planetData.getPlanetsWithTrojans()), "Planets With Trojans");
        statCard(w, fmt(trojanData.getTrojansWithAsteroids()), "With Notable Asteroids");
        statCard(w, fmt(trojanData.getTrojansWithMoons()), "With Trojan Moons");
        if (trojanData.getMassCount() > 0) {
            statCard(w, String.format("%.2e M\u2295", trojanData.getTotalMassSum() / trojanData.getMassCount()), "Avg Mass");
        }
        if (trojanData.getObjectCountCount() > 0) {
            statCard(w, fmt((int) (trojanData.getTotalObjectCountSum() / trojanData.getObjectCountCount())), "Avg Object Count");
        }
        w.println("</div>");

        if (!trojanData.getLagrangePointCounts().isEmpty()) {
            printSubSection(w, "Lagrange Point Distribution");
            printSortedTable(w, trojanData.getLagrangePointCounts(), trojanData.getTotalTrojans(), "Lagrange Point");
        }

        if (!trojanData.getParentPlanetTypes().isEmpty()) {
            printSubSection(w, "Trojans by Parent Planet Type");
            printSortedTable(w, trojanData.getParentPlanetTypes(), trojanData.getTotalTrojans(), "Planet Type");
        }

        if (!trojanData.getTierCounts().isEmpty()) {
            printSubSection(w, "Tier Classification");
            printSortedTable(w, trojanData.getTierCounts(), trojanData.getTotalTrojans(), "Tier");
        }

        if (!trojanData.getMassBins().isEmpty()) {
            printSubSection(w, "Mass Distribution (Earth Masses)");
            printSortedTableByKey(w, trojanData.getMassBins(), trojanData.getTotalTrojans(), "Mass Bin");
        }

        if (!trojanData.getObjectCountBins().isEmpty()) {
            printSubSection(w, "Estimated Object Count");
            printSortedTableByKey(w, trojanData.getObjectCountBins(), trojanData.getTotalTrojans(), "Object Count");
        }

        if (!trojanData.getLibrationAmplitudeBins().isEmpty()) {
            printSubSection(w, "Libration Amplitude");
            printSortedTableByKey(w, trojanData.getLibrationAmplitudeBins(), trojanData.getTotalTrojans(), "Amplitude");
        }

        if (!trojanData.getWidthBins().isEmpty()) {
            printSubSection(w, "Swarm Width (Radial Extent)");
            printSortedTableByKey(w, trojanData.getWidthBins(), trojanData.getTotalTrojans(), "Width");
        }

        // Planet type summary table (cross-reference)
        printTrojanPlanetTypeSummaryTable(w);

        // Per planet-type breakdown
        if (!trojanData.getPerPlanetType().isEmpty()) {
            printSubSection(w, "Per Planet Type Breakdown");
            for (Map.Entry<String, TrojanDataCollector.TrojanPlanetTypeBreakdown> entry : trojanData.getPerPlanetType().entrySet()) {
                TrojanDataCollector.TrojanPlanetTypeBreakdown ptb = entry.getValue();
                beginCollapsible(w, entry.getKey() + " (" + ptb.getCount() + " swarms)", 3);
                w.println("<p>Notable asteroids: " + ptb.getWithAsteroids()
                        + " (" + pct(ptb.getWithAsteroids(), ptb.getCount()) + "%)</p>");
                if (ptb.getMassCount() > 0) {
                    w.println("<p>Avg mass: " + String.format("%.2e M\u2295", ptb.getMassSum() / ptb.getMassCount()) + "</p>");
                }
                if (ptb.getObjectCountCount() > 0) {
                    w.println("<p>Avg object count: " + fmt((int) (ptb.getObjectCountSum() / ptb.getObjectCountCount())) + "</p>");
                }
                if (!ptb.getLagrangePoints().isEmpty()) printSortedTable(w, ptb.getLagrangePoints(), ptb.getCount(), "Lagrange Point");
                if (!ptb.getTiers().isEmpty()) printSortedTable(w, ptb.getTiers(), ptb.getCount(), "Tier");
                endCollapsible(w);
            }
        }

        endCollapsible(w);
    }

    private void printTrojanPlanetTypeSummaryTable(PrintWriter w) {
        // Cross-reference: total planets per type vs trojan formation stats
        boolean hasData = false;
        for (Map.Entry<String, PlanetTypeBreakdown> e : planetData.getPerTypeData().entrySet()) {
            if (e.getValue().getWithTrojans() > 0) { hasData = true; break; }
        }
        if (!hasData) return;

        printSubSection(w, "Trojan Formation by Planet Type");
        w.println("<table>");
        w.println("<thead><tr>"
                + "<th>Planet Type</th>"
                + "<th>Planets</th>"
                + "<th>% With Swarms</th>"
                + "<th>Swarms</th>"
                + "<th>% With Asteroids</th>"
                + "<th>% With Moons</th>"
                + "<th>Total Objects</th>"
                + "</tr></thead>");
        w.println("<tbody>");

        planetData.getPerTypeData().entrySet().stream()
                .filter(e -> e.getValue().getWithTrojans() > 0)
                .sorted((a, b) -> Integer.compare(b.getValue().getWithTrojans(), a.getValue().getWithTrojans()))
                .forEach(e -> {
                    String typeName = e.getKey();
                    PlanetTypeBreakdown ptb = e.getValue();
                    int totalPlanets = ptb.getCount();
                    int planetsWithSwarms = ptb.getWithTrojans();
                    double pctWithSwarms = planetsWithSwarms * 100.0 / Math.max(1, totalPlanets);

                    TrojanDataCollector.TrojanPlanetTypeBreakdown tptb = trojanData.getPerPlanetType().get(typeName);
                    int swarmCount = tptb != null ? tptb.getCount() : 0;
                    int swarmsWithAsteroids = tptb != null ? tptb.getWithAsteroids() : 0;
                    int planetsWithMoons = tptb != null ? tptb.getWithMoons() : 0;
                    long totalObjects = tptb != null ? tptb.getObjectCountSum() : 0;

                    double pctWithAsteroids = swarmCount > 0 ? swarmsWithAsteroids * 100.0 / swarmCount : 0;
                    double pctWithMoons = planetsWithSwarms > 0 ? planetsWithMoons * 100.0 / planetsWithSwarms : 0;

                    w.println("<tr>"
                            + "<td>" + esc(typeName) + "</td>"
                            + "<td>" + fmt(totalPlanets) + "</td>"
                            + "<td>" + String.format("%.1f%%", pctWithSwarms) + "</td>"
                            + "<td>" + fmt(swarmCount) + "</td>"
                            + "<td>" + String.format("%.1f%%", pctWithAsteroids) + "</td>"
                            + "<td>" + String.format("%.1f%%", pctWithMoons) + "</td>"
                            + "<td>" + fmt(totalObjects) + "</td>"
                            + "</tr>");
                });

        w.println("</tbody></table>");
    }

    private void printAsteroidHtml(PrintWriter w) {
        w.println("<hr>");
        beginCollapsible(w, "Notable Asteroids", 2);

        // Summary stat cards
        w.println("<div class=\"stats-grid\">");
        statCard(w, fmt(asteroidData.getTotalAsteroids()), "Total Notable");
        statCard(w, fmt(asteroidData.getSourceCounts().getOrDefault("Belt", 0)), "From Belts");
        statCard(w, fmt(asteroidData.getSourceCounts().getOrDefault("Trojan", 0)), "From Trojans");
        statCard(w, fmt(asteroidData.getTotalWithMoons()), "With Moons");
        statCard(w, fmt(asteroidData.getTotalDifferentiated()), "Differentiated");
        statCard(w, fmt(asteroidData.getTotalWithRegolith()), "With Regolith");
        if (asteroidData.getDiameterCount() > 0) {
            statCard(w, String.format("%.1f km", asteroidData.getDiameterSum() / asteroidData.getDiameterCount()), "Avg Diameter");
        }
        if (asteroidData.getDensityCount() > 0) {
            statCard(w, String.format("%.2f g/cm\u00B3", asteroidData.getDensitySum() / asteroidData.getDensityCount()), "Avg Density");
        }
        w.println("</div>");

        // Spectral type distribution
        if (!asteroidData.getAsteroidTypes().isEmpty()) {
            printSubSection(w, "Spectral Type Distribution");
            printSortedTable(w, asteroidData.getAsteroidTypes(), asteroidData.getTotalAsteroids(), "Type");
        }

        // Source breakdown
        if (!asteroidData.getSourceCounts().isEmpty()) {
            printSubSection(w, "Source Breakdown");
            printSortedTable(w, asteroidData.getSourceCounts(), asteroidData.getTotalAsteroids(), "Source");
        }

        // Diameter distribution
        if (!asteroidData.getDiameterBins().isEmpty()) {
            printSubSection(w, "Diameter Distribution");
            printSortedTableByKey(w, asteroidData.getDiameterBins(), asteroidData.getTotalAsteroids(), "Diameter");
        }

        // Mass distribution
        if (!asteroidData.getMassBins().isEmpty()) {
            printSubSection(w, "Mass Distribution");
            printSortedTableByKey(w, asteroidData.getMassBins(), asteroidData.getTotalAsteroids(), "Mass");
        }

        // Density distribution
        if (!asteroidData.getDensityBins().isEmpty()) {
            printSubSection(w, "Density Distribution");
            printSortedTableByKey(w, asteroidData.getDensityBins(), asteroidData.getTotalAsteroids(), "Density");
        }

        // Albedo distribution
        if (!asteroidData.getAlbedoBins().isEmpty()) {
            printSubSection(w, "Albedo Distribution");
            printSortedTableByKey(w, asteroidData.getAlbedoBins(), asteroidData.getTotalAsteroids(), "Albedo");
        }

        // Cratering levels
        if (!asteroidData.getCrateringLevels().isEmpty()) {
            printSubSection(w, "Cratering Levels");
            printSortedTable(w, asteroidData.getCrateringLevels(), asteroidData.getTotalAsteroids(), "Level");
        }

        // Orbital distance
        if (!asteroidData.getDistanceBins().isEmpty()) {
            printSubSection(w, "Orbital Distance Distribution");
            printSortedTableByKey(w, asteroidData.getDistanceBins(), asteroidData.getTotalAsteroids(), "Distance");
        }

        // Per spectral-type breakdown
        if (!asteroidData.getPerTypeData().isEmpty()) {
            printSubSection(w, "Per Spectral Type Breakdown");
            for (Map.Entry<String, AsteroidDataCollector.AsteroidTypeBreakdown> entry : asteroidData.getPerTypeData().entrySet()) {
                AsteroidDataCollector.AsteroidTypeBreakdown atb = entry.getValue();
                beginCollapsible(w, entry.getKey() + " (" + atb.getCount() + " asteroids)", 3);
                if (!atb.getSources().isEmpty()) {
                    w.println("<p>Sources: ");
                    atb.getSources().forEach((src, cnt) ->
                        w.print(src + ": " + cnt + " (" + pct(cnt, atb.getCount()) + "%) "));
                    w.println("</p>");
                }
                if (atb.getDiameterCount() > 0) {
                    w.println("<p>Avg diameter: " + String.format("%.1f km", atb.getDiameterSum() / atb.getDiameterCount()) + "</p>");
                }
                if (atb.getDensityCount() > 0) {
                    w.println("<p>Avg density: " + String.format("%.2f g/cm\u00B3", atb.getDensitySum() / atb.getDensityCount()) + "</p>");
                }
                if (atb.getAlbedoCount() > 0) {
                    w.println("<p>Avg albedo: " + String.format("%.3f", atb.getAlbedoSum() / atb.getAlbedoCount()) + "</p>");
                }
                w.println("<p>Differentiated: " + atb.getDifferentiated()
                        + " (" + pct(atb.getDifferentiated(), atb.getCount()) + "%)</p>");
                w.println("<p>With moons: " + atb.getWithMoons()
                        + " (" + pct(atb.getWithMoons(), atb.getCount()) + "%)</p>");
                endCollapsible(w);
            }
        }

        endCollapsible(w);
    }

    private void printBeltHtml(PrintWriter w) {
        w.println("<hr>");
        beginCollapsible(w, "Belt Types", 2);

        // Summary stat cards
        w.println("<div class=\"stats-grid\">");
        statCard(w, fmt(counts.getBeltCount()), "Total Belts");
        if (beltData.getMassCount() > 0) {
            statCard(w, String.format("%.4f M\u2295", beltData.getTotalMassSum() / beltData.getMassCount()), "Avg Mass");
        }
        if (beltData.getWidthCount() > 0) {
            statCard(w, String.format("%.2f AU", beltData.getTotalWidthSum() / beltData.getWidthCount()), "Avg Width");
        }
        statCard(w, fmt(beltData.getBeltsWithGaps()), "With Gaps");
        statCard(w, fmt(beltData.getBeltsWithResonanceGaps()), "Resonance Gaps");
        statCard(w, fmt(beltData.getBeltsWithCollisionalFamilies()), "Collisional Families");
        statCard(w, fmt(beltData.getBeltsWithDwarfPlanets()), "With Dwarf Planets");
        statCard(w, fmt(counts.getDwarfPlanetCount()), "Total Dwarf Planets");
        w.println("</div>");

        printSortedTable(w, beltData.getBeltTypes(), counts.getBeltCount(), "Belt Type");

        printSubSection(w, "Asteroid Types");
        printSortedTable(w, beltData.getAsteroidTypes(), counts.getAsteroidCount(), "Asteroid Type");

        if (!beltData.getCompositionTypes().isEmpty()) {
            printSubSection(w, "Belt Composition");
            printSortedTable(w, beltData.getCompositionTypes(), counts.getBeltCount(), "Composition");
        }

        if (!beltData.getWidthBins().isEmpty()) {
            printSubSection(w, "Belt Width Distribution");
            printSortedTableByKey(w, beltData.getWidthBins(), counts.getBeltCount(), "Width");
        }

        if (!beltData.getInnerEdgeBins().isEmpty()) {
            printSubSection(w, "Inner Edge Distribution");
            printSortedTableByKey(w, beltData.getInnerEdgeBins(), counts.getBeltCount(), "Inner Edge");
        }

        if (!beltData.getOuterEdgeBins().isEmpty()) {
            printSubSection(w, "Outer Edge Distribution");
            printSortedTableByKey(w, beltData.getOuterEdgeBins(), counts.getBeltCount(), "Outer Edge");
        }

        if (!beltData.getMassBins().isEmpty()) {
            printSubSection(w, "Belt Mass Distribution");
            printSortedTableByKey(w, beltData.getMassBins(), counts.getBeltCount(), "Mass Range");
        }

        if (!beltData.getEccentricityBins().isEmpty()) {
            printSubSection(w, "Average Eccentricity Distribution");
            printSortedTableByKey(w, beltData.getEccentricityBins(), counts.getBeltCount(), "Eccentricity");
        }

        if (!beltData.getInclinationBins().isEmpty()) {
            printSubSection(w, "Average Inclination Distribution");
            printSortedTableByKey(w, beltData.getInclinationBins(), counts.getBeltCount(), "Inclination");
        }

        if (!beltData.getObjectCountBins().isEmpty()) {
            printSubSection(w, "Estimated Object Count Distribution");
            printSortedTableByKey(w, beltData.getObjectCountBins(), counts.getBeltCount(), "Object Count");
        }

        if (!beltData.getParentStarTypes().isEmpty()) {
            printSubSection(w, "Belts by Parent Star Type");
            printSortedTable(w, beltData.getParentStarTypes(), counts.getBeltCount(), "Star Type");
        }

        if (!beltData.getBinaryConfigBelts().isEmpty()) {
            printSubSection(w, "Belts by System Configuration");
            printSortedTable(w, beltData.getBinaryConfigBelts(), counts.getBeltCount(), "Configuration");
        }

        // Per-type breakdown
        if (!beltData.getPerTypeData().isEmpty()) {
            printSubSection(w, "Per Belt Type Breakdown");
            for (Map.Entry<String, BeltDataCollector.BeltTypeBreakdown> entry : beltData.getPerTypeData().entrySet()) {
                BeltDataCollector.BeltTypeBreakdown btb = entry.getValue();
                beginCollapsible(w, entry.getKey() + " (" + btb.getCount() + ")", 3);

                w.println("<div class=\"stats-grid\">");
                statCard(w, String.valueOf(btb.getCount()), "Count");
                if (btb.getWidthCount() > 0) {
                    statCard(w, String.format("%.2f AU", btb.getWidthSum() / btb.getWidthCount()), "Avg Width");
                }
                if (btb.getMassCount() > 0) {
                    statCard(w, String.format("%.4f M\u2295", btb.getMassSum() / btb.getMassCount()), "Avg Mass");
                }
                if (btb.getEdgeCount() > 0) {
                    statCard(w, String.format("%.2f AU", btb.getInnerEdgeSum() / btb.getEdgeCount()), "Avg Inner Edge");
                    statCard(w, String.format("%.2f AU", btb.getOuterEdgeSum() / btb.getEdgeCount()), "Avg Outer Edge");
                }
                if (btb.getEccCount() > 0) {
                    statCard(w, String.format("%.4f", btb.getEccSum() / btb.getEccCount()), "Avg Eccentricity");
                }
                statCard(w, String.valueOf(btb.getGapsCount()), "With Gaps");
                statCard(w, String.valueOf(btb.getFamiliesCount()), "Families");
                statCard(w, String.valueOf(btb.getTotalDwarfPlanets()), "Dwarf Planets");
                statCard(w, String.valueOf(btb.getBeltsWithDwarfPlanets()), "Belts w/ Dwarfs");
                w.println("</div>");

                if (!btb.getCompositionTypes().isEmpty()) {
                    printSortedTable(w, btb.getCompositionTypes(), btb.getCount(), "Composition");
                }
                endCollapsible(w);
            }
        }

        endCollapsible(w);
    }

    // ═══════════════════════════════════════════════════════════════
    //  HTML Utility Methods (inline from HtmlReportUtils)
    // ═══════════════════════════════════════════════════════════════

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    private String fmt(int value) {
        return NUMBER_FORMAT.format(value);
    }

    private String fmt(long value) {
        return NUMBER_FORMAT.format(value);
    }

    private String pct(int count, int total) {
        return String.format("%.1f", count * 100.0 / Math.max(1, total));
    }

    private void printSortedTable(PrintWriter w, Map<String, Integer> data, int total, String col1Name) {
        w.println("<table>");
        w.println("<thead><tr><th>" + col1Name + "</th><th>Count</th><th>%</th><th class=\"bar-col\">Distribution</th></tr></thead>");
        w.println("<tbody>");
        data.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> {
                    double pctVal = e.getValue() * 100.0 / Math.max(1, total);
                    w.println("<tr><td>" + esc(e.getKey()) + "</td><td>" + fmt(e.getValue())
                            + "</td><td>" + String.format("%.1f", pctVal) + "%</td><td>"
                            + bar(pctVal) + "</td></tr>");
                });
        w.println("</tbody></table>");
    }

    private void printSortedTableByKey(PrintWriter w, Map<String, Integer> data, int total, String col1Name) {
        w.println("<table>");
        w.println("<thead><tr><th>" + col1Name + "</th><th>Count</th><th>%</th><th class=\"bar-col\">Distribution</th></tr></thead>");
        w.println("<tbody>");
        data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    double pctVal = e.getValue() * 100.0 / Math.max(1, total);
                    w.println("<tr><td>" + esc(e.getKey()) + "</td><td>" + fmt(e.getValue())
                            + "</td><td>" + String.format("%.1f", pctVal) + "%</td><td>"
                            + bar(pctVal) + "</td></tr>");
                });
        w.println("</tbody></table>");
    }

    private void printLinkedTable(PrintWriter w, Map<String, Integer> data, int total, String col1Name) {
        w.println("<table>");
        w.println("<thead><tr><th>" + col1Name + "</th><th>Count</th><th>%</th><th class=\"bar-col\">Distribution</th></tr></thead>");
        w.println("<tbody>");
        data.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> {
                    String anchor = toAnchor(e.getKey());
                    double pctVal = e.getValue() * 100.0 / Math.max(1, total);
                    w.println("<tr><td><a href=\"#" + anchor + "\">" + esc(e.getKey()) + "</a></td><td>"
                            + fmt(e.getValue()) + "</td><td>" + String.format("%.1f", pctVal) + "%</td><td>"
                            + bar(pctVal) + "</td></tr>");
                });
        w.println("</tbody></table>");
    }

    private String toAnchor(String text) {
        return text.toLowerCase().replace(" ", "-").replaceAll("[^a-z0-9\\-]", "");
    }

    private void printAnchor(PrintWriter w, String text) {
        w.println("<span id=\"" + toAnchor(text) + "\"></span>");
    }

    private void printSubSection(PrintWriter w, String title) {
        w.println("<h3>" + esc(title) + "</h3>");
    }

    private void beginCollapsible(PrintWriter w, String title, int headingLevel) {
        String id = toAnchor(title);
        w.println("<details class=\"section\" id=\"" + id + "\">");
        w.println("<summary><h" + headingLevel + ">" + esc(title) + "</h" + headingLevel + "></summary>");
        w.println("<div class=\"section-body\">");
    }

    private void endCollapsible(PrintWriter w) {
        w.println("</div>");
        w.println("</details>");
    }

    private void statCard(PrintWriter w, String value, String label) {
        w.println("<div class=\"stat-card\">");
        w.println("<span class=\"stat-value\">" + value + "</span>");
        w.println("<span class=\"stat-label\">" + esc(label) + "</span>");
        w.println("</div>");
    }

    private void tocLink(PrintWriter w, String sectionTitle) {
        String anchor = toAnchor(sectionTitle);
        w.println("<li><a href=\"#" + anchor + "\">" + esc(sectionTitle) + "</a></li>");
    }

    private String bar(double pctVal) {
        double clamped = Math.min(100, Math.max(0, pctVal));
        return "<div class=\"bar\"><div class=\"bar-fill\" style=\"width:" + String.format("%.1f", clamped) + "%\"></div></div>";
    }

    private String esc(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    // ═══════════════════════════════════════════════════════════════
    //  Page Template (CSS & JS from HtmlReportUtils)
    // ═══════════════════════════════════════════════════════════════

    private void printPageHeader(PrintWriter w) {
        w.println("<!DOCTYPE html>");
        w.println("<html lang=\"en\">");
        w.println("<head>");
        w.println("<meta charset=\"UTF-8\">");
        w.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        w.println("<title>Star System Probability Report</title>");
        w.println("<style>");
        w.println(CSS);
        w.println("</style>");
        w.println("</head>");
        w.println("<body>");

        w.println("<div class=\"hero-banner\">");
        w.println("<img src=\"" + HEADER_IMAGE_PATH + "\" alt=\"Star Creator API\">");
        w.println("</div>");

        w.println("<div class=\"page-grid\">");
        w.println("<aside class=\"toc-sidebar\" id=\"toc-sidebar\">");
    }

    private void beginMainContent(PrintWriter w) {
        w.println("</aside>");
        w.println("<main class=\"main-content\">");
    }

    private void printPageFooter(PrintWriter w) {
        w.println("</main>");
        w.println("</div>");
        w.println("<script>");
        w.println(JS);
        w.println("</script>");
        w.println("</body>");
        w.println("</html>");
    }

    // ═══════════════════════════════════════════════════════════════
    //  CSS (same styles as test HtmlReportUtils)
    // ═══════════════════════════════════════════════════════════════

    private static final String CSS = """
            :root {
              --bg: #0b0e17;
              --surface: #131825;
              --surface2: #1a2035;
              --border: #2a3050;
              --text: #c8d0e0;
              --text-muted: #6b7394;
              --accent: #4e8cff;
              --accent2: #7b61ff;
              --gold: #f0b860;
              --green: #4cd080;
              --red: #f06060;
              --cyan: #40d8d8;
              --toc-width: 240px;
            }
            * { margin: 0; padding: 0; box-sizing: border-box; }
            html { scroll-behavior: smooth; scroll-padding-top: 1rem; }
            body {
              font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
              background: var(--bg);
              color: var(--text);
              line-height: 1.6;
              min-height: 100vh;
            }
            .hero-banner {
              width: 100%;
              background: var(--surface);
              border-bottom: 1px solid var(--border);
              text-align: center;
              overflow: hidden;
            }
            .hero-banner img {
              width: 100%;
              max-height: 280px;
              object-fit: cover;
              object-position: center;
              display: block;
            }
            .page-grid {
              display: grid;
              grid-template-columns: var(--toc-width) 1fr;
              max-width: 1400px;
              margin: 0 auto;
              gap: 0;
            }
            .toc-sidebar {
              position: sticky;
              top: 0;
              height: 100vh;
              overflow-y: auto;
              padding: 1.2rem 0 2rem 1rem;
              background: var(--surface);
              border-right: 1px solid var(--border);
              scrollbar-width: thin;
              scrollbar-color: var(--border) transparent;
              z-index: 50;
            }
            .toc-sidebar::-webkit-scrollbar { width: 5px; }
            .toc-sidebar::-webkit-scrollbar-track { background: transparent; }
            .toc-sidebar::-webkit-scrollbar-thumb { background: var(--border); border-radius: 3px; }
            .toc-sidebar .toc-title {
              font-size: 0.75rem;
              text-transform: uppercase;
              letter-spacing: 1px;
              color: var(--text-muted);
              padding: 0 0.6rem 0.8rem;
              border-bottom: 1px solid var(--border);
              margin-bottom: 0.5rem;
            }
            .toc-sidebar ol { list-style: none; padding: 0; margin: 0; }
            .toc-sidebar li { margin: 0; }
            .toc-sidebar a {
              display: block;
              padding: 0.35rem 0.6rem 0.35rem 0.8rem;
              color: var(--text-muted);
              text-decoration: none;
              font-size: 0.82rem;
              border-left: 2px solid transparent;
              transition: all 0.15s ease;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }
            .toc-sidebar a:hover {
              color: var(--text);
              background: rgba(78, 140, 255, 0.06);
            }
            .toc-sidebar a.active {
              color: var(--accent);
              border-left-color: var(--accent);
              background: rgba(78, 140, 255, 0.08);
              font-weight: 600;
            }
            .main-content {
              padding: 2rem 2.5rem;
              min-width: 0;
            }
            .report-header {
              text-align: center;
              padding: 2.5rem 2rem;
              margin-bottom: 2rem;
              background: linear-gradient(135deg, var(--surface) 0%, var(--surface2) 100%);
              border: 1px solid var(--border);
              border-radius: 16px;
              position: relative;
              overflow: hidden;
            }
            .report-header::before {
              content: '';
              position: absolute;
              top: 0; left: 0; right: 0; height: 3px;
              background: linear-gradient(90deg, var(--accent), var(--accent2), var(--cyan));
            }
            .report-header h1 {
              font-size: 2rem;
              font-weight: 700;
              background: linear-gradient(135deg, var(--accent), var(--cyan));
              -webkit-background-clip: text;
              -webkit-text-fill-color: transparent;
              background-clip: text;
              margin-bottom: 0.5rem;
            }
            .report-header .subtitle { color: var(--text-muted); font-size: 0.95rem; }
            .stats-grid {
              display: grid;
              grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
              gap: 0.8rem;
              margin: 1.5rem 0;
            }
            .stat-card {
              background: var(--surface);
              border: 1px solid var(--border);
              border-radius: 10px;
              padding: 0.8rem 1rem;
              text-align: center;
            }
            .stat-card .stat-value {
              font-size: 1.5rem;
              font-weight: 700;
              color: var(--accent);
              display: block;
            }
            .stat-card .stat-label {
              font-size: 0.72rem;
              color: var(--text-muted);
              text-transform: uppercase;
              letter-spacing: 0.5px;
            }
            hr { border: none; height: 1px; background: var(--border); margin: 2rem 0; }
            h2 { font-size: 1.5rem; color: var(--text); margin: 0.5rem 0; }
            h3 { font-size: 1.15rem; color: var(--text); margin: 1.2rem 0 0.5rem; }
            h4 { font-size: 1rem; color: var(--text-muted); margin: 1rem 0 0.4rem; }
            details.section {
              background: var(--surface);
              border: 1px solid var(--border);
              border-radius: 12px;
              margin: 1rem 0;
              overflow: hidden;
              scroll-margin-top: 1rem;
            }
            details.section > summary {
              cursor: pointer;
              padding: 0.8rem 1.2rem;
              background: var(--surface2);
              border-bottom: 1px solid var(--border);
              list-style: none;
              display: flex;
              align-items: center;
              gap: 0.5rem;
              user-select: none;
              transition: background 0.2s;
            }
            details.section > summary:hover { background: #1e2845; }
            details.section > summary::before {
              content: '\\25B6';
              font-size: 0.7rem;
              color: var(--accent);
              transition: transform 0.2s;
              flex-shrink: 0;
            }
            details.section[open] > summary::before { transform: rotate(90deg); }
            details.section > summary::-webkit-details-marker { display: none; }
            details.section > summary h2,
            details.section > summary h3,
            details.section > summary h4 { margin: 0; display: inline; font-size: inherit; }
            details.section > summary h2 { font-size: 1.3rem; }
            details.section > summary h3 { font-size: 1.05rem; }
            details.section > summary h4 { font-size: 0.95rem; }
            .section-body { padding: 1.2rem; }
            details.section details.section { border-radius: 8px; margin: 0.6rem 0; }
            details.section details.section > summary { padding: 0.6rem 1rem; }
            table { width: 100%; border-collapse: collapse; margin: 0.8rem 0 1.2rem; font-size: 0.88rem; }
            thead th {
              text-align: left; padding: 0.6rem 0.8rem; background: var(--surface2);
              color: var(--text-muted); font-weight: 600; text-transform: uppercase;
              font-size: 0.75rem; letter-spacing: 0.5px; border-bottom: 2px solid var(--border);
            }
            tbody td { padding: 0.5rem 0.8rem; border-bottom: 1px solid #1a2035; }
            tbody tr:hover { background: rgba(78, 140, 255, 0.04); }
            td a { color: var(--accent); text-decoration: none; }
            td a:hover { text-decoration: underline; }
            .bar-col { width: 30%; }
            .bar { width: 100%; height: 8px; background: var(--surface); border-radius: 4px; overflow: hidden; }
            .bar-fill {
              height: 100%; background: linear-gradient(90deg, var(--accent), var(--accent2));
              border-radius: 4px; min-width: 2px; transition: width 0.3s ease;
            }
            p { margin: 0.5rem 0; }
            .note { color: var(--text-muted); font-style: italic; font-size: 0.88rem; }
            strong { color: var(--text); }
            .xref-table { font-size: 0.85rem; }
            .xref-table th { font-size: 0.72rem; }
            .xref-table td { text-align: center; }
            .xref-table td:first-child { text-align: left; }
            @media (max-width: 900px) {
              .page-grid { grid-template-columns: 1fr; }
              .toc-sidebar {
                position: fixed; left: -280px; top: 0; width: 280px; height: 100vh;
                transition: left 0.3s ease; box-shadow: 4px 0 20px rgba(0,0,0,0.5);
              }
              .toc-sidebar.open { left: 0; }
              .toc-toggle { display: flex !important; }
              .main-content { padding: 1.5rem 1rem; }
              .hero-banner img { max-height: 180px; }
            }
            @media (min-width: 901px) { .toc-toggle { display: none !important; } }
            @media (max-width: 600px) {
              .stats-grid { grid-template-columns: repeat(auto-fit, minmax(100px, 1fr)); gap: 0.5rem; }
              table { font-size: 0.8rem; }
              .bar-col { display: none; }
              .report-header h1 { font-size: 1.4rem; }
              .report-header { padding: 1.5rem 1rem; }
            }
            """;

    // ═══════════════════════════════════════════════════════════════
    //  JS (same scripts as test HtmlReportUtils)
    // ═══════════════════════════════════════════════════════════════

    private static final String JS = """
            document.addEventListener('DOMContentLoaded', () => {
              const expandBtn = document.createElement('button');
              expandBtn.textContent = 'Expand All';
              expandBtn.style.cssText = 'position:fixed;bottom:1.5rem;right:1.5rem;padding:0.6rem 1.2rem;' +
                'background:var(--accent);color:#fff;border:none;border-radius:8px;cursor:pointer;' +
                'font-size:0.85rem;z-index:100;box-shadow:0 4px 12px rgba(0,0,0,0.4);transition:background 0.2s;';
              expandBtn.addEventListener('mouseenter', () => expandBtn.style.background = 'var(--accent2)');
              expandBtn.addEventListener('mouseleave', () => expandBtn.style.background = 'var(--accent)');
              let expanded = false;
              expandBtn.addEventListener('click', () => {
                expanded = !expanded;
                document.querySelectorAll('details.section').forEach(d => d.open = expanded);
                expandBtn.textContent = expanded ? 'Collapse All' : 'Expand All';
              });
              document.body.appendChild(expandBtn);
              const tocToggle = document.createElement('button');
              tocToggle.className = 'toc-toggle';
              tocToggle.innerHTML = '&#9776;';
              tocToggle.style.cssText = 'position:fixed;top:0.8rem;left:0.8rem;padding:0.4rem 0.7rem;' +
                'background:var(--surface2);color:var(--accent);border:1px solid var(--border);' +
                'border-radius:6px;cursor:pointer;font-size:1.2rem;z-index:200;display:none;';
              const sidebar = document.getElementById('toc-sidebar');
              tocToggle.addEventListener('click', () => sidebar.classList.toggle('open'));
              document.body.appendChild(tocToggle);
              sidebar.querySelectorAll('a').forEach(a => {
                a.addEventListener('click', () => sidebar.classList.remove('open'));
              });
              sidebar.querySelectorAll('a[href^="#"]').forEach(link => {
                link.addEventListener('click', (e) => {
                  const targetId = link.getAttribute('href').substring(1);
                  const target = document.getElementById(targetId);
                  if (target && target.tagName === 'DETAILS') { target.open = true; }
                });
              });
              const tocLinks = sidebar.querySelectorAll('a[href^="#"]');
              const sectionIds = Array.from(tocLinks).map(a => a.getAttribute('href').substring(1));
              const sections = sectionIds.map(id => document.getElementById(id)).filter(Boolean);
              if (sections.length === 0) return;
              let activeLink = null;
              function setActive(id) {
                if (activeLink) activeLink.classList.remove('active');
                const link = sidebar.querySelector('a[href="#' + id + '"]');
                if (link) {
                  link.classList.add('active');
                  activeLink = link;
                  const sidebarRect = sidebar.getBoundingClientRect();
                  const linkRect = link.getBoundingClientRect();
                  if (linkRect.top < sidebarRect.top || linkRect.bottom > sidebarRect.bottom) {
                    link.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
                  }
                }
              }
              const observer = new IntersectionObserver((entries) => {
                let best = null;
                let bestTop = Infinity;
                entries.forEach(entry => {
                  if (entry.isIntersecting) {
                    const top = entry.boundingClientRect.top;
                    if (top < bestTop) { bestTop = top; best = entry.target; }
                  }
                });
                if (best) setActive(best.id);
              }, { rootMargin: '-10% 0px -70% 0px', threshold: 0 });
              let ticking = false;
              window.addEventListener('scroll', () => {
                if (ticking) return;
                ticking = true;
                requestAnimationFrame(() => {
                  let current = null;
                  for (const section of sections) {
                    const rect = section.getBoundingClientRect();
                    if (rect.top <= window.innerHeight * 0.3) { current = section; }
                  }
                  if (current) setActive(current.id);
                  ticking = false;
                });
              });
              sections.forEach(s => observer.observe(s));
              if (sections.length > 0) setActive(sections[0].id);
            });
            """;
}
