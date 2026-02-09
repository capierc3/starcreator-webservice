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

    static void analyzeData(Star star) {
        STAR_TYPES.put(star.getType(), STAR_TYPES.getOrDefault(star.getType(), 0) + 1);
        Map<String, Map<String, Integer>> starTypesData = STAR_TYPES_DATA.getOrDefault(star.getType(), new HashMap<>());

        Map<String, Integer> activityData = starTypesData.getOrDefault("activity", new HashMap<>());
        activityData.put(star.getActivityLevel(), activityData.getOrDefault(star.getActivityLevel(),0) + 1);
        starTypesData.put("activity", activityData);

        Map<String, Integer> flareData = starTypesData.getOrDefault("flare class", new HashMap<>());
        flareData.put(star.getFlareClass(), flareData.getOrDefault(star.getFlareClass(),0) + 1);
        starTypesData.put("flare class", flareData);

        Map<String, Integer> spotData = starTypesData.getOrDefault("spot %", new HashMap<>());
        int spotPercent = (int) Math.round(star.getStarspotCoveragePercent());
        spotData.put(Integer.toString(spotPercent), spotData.getOrDefault(Integer.toString(spotPercent),0) + 1);
        starTypesData.put("spot %", spotData);

        Map<String, Integer> xrayData = starTypesData.getOrDefault("xray Luminosity", new HashMap<>());
        xrayData.put(star.getXrayLuminosityClass(), xrayData.getOrDefault(star.getXrayLuminosityClass(),0) + 1);
        starTypesData.put("xray Luminosity", xrayData);

        Map<String, Integer> evoData = starTypesData.getOrDefault("evolutionary stage", new HashMap<>());
        evoData.put(star.getEvolutionaryStage(), evoData.getOrDefault(star.getEvolutionaryStage(),0) + 1);
        starTypesData.put("evolutionary stage", evoData);

        STAR_TYPES_DATA.put(star.getType(), starTypesData);
    }

    static void printData(PrintWriter writer, ProbabilityCounts counts) {
        writer.println("---");
        writer.println("## Star Amounts");
        writer.println("| Amount | Count | % |");
        writer.println("| --- | --- | --- |");
        STAR_AMOUNTS.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> writer.println("| " + entry.getKey() + " Star System | " + entry.getValue() + " | " + (entry.getValue() * 100.0) / counts.getSystemCount() + "% |"));
        writer.println("---");
        writer.println("## Star Types");
        writer.println("| Star Type | Count | % |");
        writer.println("| --- | --- | --- |");
        STAR_TYPES.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> writer.println("| " + entry.getKey() + " | " + entry.getValue() + " | " + (entry.getValue() * 100.0) / counts.getStarCount() + "% |"));

        for (Map.Entry<String, Map<String, Map<String, Integer>>> entry : STAR_TYPES_DATA.entrySet()) {
            Map<String, Map<String, Integer>> starTypeData = entry.getValue();
            int starTypeCount = STAR_TYPES.getOrDefault(entry.getKey(), 0);
            writer.println("---");
            writer.println("### " + entry.getKey() + " Star Type Data");
            writer.println("");

            Map<String, Integer> activityData = starTypeData.getOrDefault("activity", new HashMap<>());
            writer.println("| Activity Level | Count | % |");
            writer.println("| --- | --- | --- |");
            activityData.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(activity -> writer.println("| " + activity.getKey() + " | " + activity.getValue() + " | " + (activity.getValue() * 100.0) / starTypeCount + "% |"));
            writer.println("");

            Map<String, Integer> flareData = starTypeData.getOrDefault("flare class", new HashMap<>());
            writer.println("| Flare Class | Count | % |");
            writer.println("| --- | --- | --- |");
            flareData.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(flare -> writer.println("| " + flare.getKey() + " | " + flare.getValue() + " | " + (flare.getValue() * 100.0) / starTypeCount + "% |"));
            writer.println("");

            Map<String, Integer> spotData = starTypeData.getOrDefault("spot %", new HashMap<>());
            writer.println("| Spot Coverage % | Count | % |");
            writer.println("| --- | --- | --- |");
            spotData.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(spot -> writer.println("| " + spot.getKey() + " | " + spot.getValue() + " | " + (spot.getValue() * 100.0) / starTypeCount + "% |"));
            writer.println("");

            Map<String, Integer> xrayData = starTypeData.getOrDefault("xray Luminosity", new HashMap<>());
            writer.println("| X-Ray Luminosity | Count | % |");
            writer.println("| --- | --- | --- |");
            xrayData.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(xray -> writer.println("| " + xray.getKey() + " | " + xray.getValue() + " | " + (xray.getValue() * 100.0) / starTypeCount + "% |"));
            writer.println("");

            Map<String, Integer> evoData = starTypeData.getOrDefault("evolutionary stage", new HashMap<>());
            writer.println("| Evolutionary Stage | Count | % |");
            writer.println("| --- | --- | --- |");
            evoData.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(evo -> writer.println("| " + evo.getKey() + " | " + evo.getValue() + " | " + (evo.getValue() * 100.0) / starTypeCount + "% |"));
            writer.println("");

        }
    }

}
