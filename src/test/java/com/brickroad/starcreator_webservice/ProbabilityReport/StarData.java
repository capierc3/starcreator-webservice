package com.brickroad.starcreator_webservice.ProbabilityReport;

import com.brickroad.starcreator_webservice.entity.ud.Star;
import lombok.Getter;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;


@Getter
public class StarData {

    static final Map<String, Integer> STAR_TYPES = new HashMap<>();
    static final Map<String, Map<String, Map<String, Integer>>> STAR_TYPES_DATA = new HashMap<>();
    static final Map<Integer, Integer> STAR_AMOUNTS = new HashMap<>();
    static final Map<String, Integer> STAR_ROLES = new HashMap<>();

    static void analyzeData(Star star) {
        STAR_TYPES.put(star.getType(), STAR_TYPES.getOrDefault(star.getType(), 0) + 1);

        // Star role
        String role = star.getStarRole() != null ? star.getStarRole().name() : "UNKNOWN";
        STAR_ROLES.put(role, STAR_ROLES.getOrDefault(role, 0) + 1);

        Map<String, Map<String, Integer>> starTypesData = STAR_TYPES_DATA.getOrDefault(star.getType(), new HashMap<>());

        Map<String, Integer> activityData = starTypesData.getOrDefault("activity", new HashMap<>());
        activityData.put(star.getActivityLevel(), activityData.getOrDefault(star.getActivityLevel(),0) + 1);
        starTypesData.put("activity", activityData);

        Map<String, Integer> flareData = starTypesData.getOrDefault("flare class", new HashMap<>());
        flareData.put(star.getFlareClass(), flareData.getOrDefault(star.getFlareClass(),0) + 1);
        starTypesData.put("flare class", flareData);

        Map<String, Integer> spotData = starTypesData.getOrDefault("spot %", new HashMap<>());
        String spotBin = binSpotCoverage(star.getStarspotCoveragePercent() != null
                ? star.getStarspotCoveragePercent() : 0.0);
        spotData.put(spotBin, spotData.getOrDefault(spotBin, 0) + 1);
        starTypesData.put("spot %", spotData);

        Map<String, Integer> xrayData = starTypesData.getOrDefault("xray Luminosity", new HashMap<>());
        xrayData.put(star.getXrayLuminosityClass(), xrayData.getOrDefault(star.getXrayLuminosityClass(),0) + 1);
        starTypesData.put("xray Luminosity", xrayData);

        Map<String, Integer> evoData = starTypesData.getOrDefault("evolutionary stage", new HashMap<>());
        evoData.put(star.getEvolutionaryStage(), evoData.getOrDefault(star.getEvolutionaryStage(),0) + 1);
        starTypesData.put("evolutionary stage", evoData);

        Map<String, Integer> planetCountData = starTypesData.getOrDefault("planets per system", new HashMap<>());
        int planetCount = star.getSystem() != null ? star.getSystem().getPlanets().size() : 0;
        String planetBin = binPlanetCount(planetCount);
        planetCountData.put(planetBin, planetCountData.getOrDefault(planetBin, 0) + 1);
        starTypesData.put("planets per system", planetCountData);

        STAR_TYPES_DATA.put(star.getType(), starTypesData);
    }

    static void printData(PrintWriter writer, ProbabilityCounts counts) {
        ReportUtils.printSection(writer, "Star Amounts");
        writer.println("| Amount | Count | % |");
        writer.println("| --- | --- | --- |");
        STAR_AMOUNTS.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .forEach(entry -> writer.println("| " + entry.getKey() + " Star System | " + entry.getValue()
                        + " | " + ReportUtils.pct(entry.getValue(), counts.getSystemCount()) + "% |"));
        writer.println("");

        ReportUtils.printLinkedTable(writer, STAR_TYPES, counts.getStarCount(), "Star Type");
        writer.println("| Star Type | Count | % |");
        writer.println("| --- | --- | --- |");
        STAR_TYPES.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> writer.println("| " + entry.getKey() + " | " + entry.getValue()
                        + " | " + ReportUtils.pct(entry.getValue(), counts.getStarCount()) + "% |"));
        writer.println("");

        // Star Roles
        ReportUtils.printSubSection(writer, "Star Roles");
        ReportUtils.printSortedTable(writer, STAR_ROLES, counts.getStarCount(), "Role");

        // Per-type breakdown
        for (Map.Entry<String, Map<String, Map<String, Integer>>> entry : STAR_TYPES_DATA.entrySet()) {
            Map<String, Map<String, Integer>> starTypeData = entry.getValue();
            int starTypeCount = STAR_TYPES.getOrDefault(entry.getKey(), 0);

            // Condense uniform types to a single line
            if (isUniformType(starTypeData)) {
                writer.println("---");
                ReportUtils.printAnchor(writer, entry.getKey());
                writer.println("### " + entry.getKey() + " (" + starTypeCount + ")");
                writer.println("");
                Map<String, Integer> activityData = starTypeData.getOrDefault("activity", new HashMap<>());
                Map<String, Integer> evoData = starTypeData.getOrDefault("evolutionary stage", new HashMap<>());
                String activity = activityData.keySet().stream().findFirst().orElse("N/A");
                String evo = evoData.keySet().stream().findFirst().orElse("N/A");
                writer.println("*Uniform profile — Activity: " + activity + ", Stage: " + evo + "*");
                writer.println("");
                continue;
            }

            writer.println("---");
            ReportUtils.printAnchor(writer, entry.getKey());
            writer.println("### " + entry.getKey() + " Star Type Data");
            writer.println("");

            printStarSubTable(writer, starTypeData, "activity", "Activity Level", starTypeCount);
            printStarSubTable(writer, starTypeData, "flare class", "Flare Class", starTypeCount);
            printStarSubTable(writer, starTypeData, "spot %", "Spot Coverage", starTypeCount);
            printStarSubTable(writer, starTypeData, "xray Luminosity", "X-Ray Luminosity", starTypeCount);
            printStarSubTable(writer, starTypeData, "evolutionary stage", "Evolutionary Stage", starTypeCount);
            printStarSubTable(writer, starTypeData, "planets per system", "Planets Per System", starTypeCount);
        }
    }

    private static boolean isUniformType(Map<String, Map<String, Integer>> typeData) {
        for (Map<String, Integer> subcategory : typeData.values()) {
            if (subcategory.size() > 1) return false;
        }
        return true;
    }

    private static void printStarSubTable(PrintWriter writer, Map<String, Map<String, Integer>> typeData,
                                          String key, String label, int total) {
        Map<String, Integer> data = typeData.getOrDefault(key, new HashMap<>());
        if (data.isEmpty()) return;
        ReportUtils.printSortedTable(writer, data, total, label);
    }

    private static String binSpotCoverage(double pct) {
        if (pct < 0.1) return "0% (none)";
        if (pct < 1.0) return "<1% (trace)";
        if (pct <= 2) return "1-2% (low)";
        if (pct <= 5) return "3-5% (moderate)";
        if (pct <= 10) return "6-10% (high)";
        if (pct <= 25) return "11-25% (very high)";
        return "26%+ (extreme)";
    }

    private static String binPlanetCount(int count) {
        if (count == 0) return "0";
        if (count <= 2) return "1-2";
        if (count <= 4) return "3-4";
        if (count <= 6) return "5-6";
        if (count <= 8) return "7-8";
        return "9+";
    }
}