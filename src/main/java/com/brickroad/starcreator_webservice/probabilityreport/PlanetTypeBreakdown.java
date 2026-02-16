package com.brickroad.starcreator_webservice.probabilityreport;

import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class PlanetTypeBreakdown {

    private int count = 0;
    private final Map<String, Integer> compositionClasses = new HashMap<>();
    private final Map<String, Integer> surfaceTempBins = new HashMap<>();
    private final Map<String, Integer> atmosphereClasses = new HashMap<>();
    private final Map<String, Integer> hzPositions = new HashMap<>();
    private int tidallyLocked = 0;
    private final Map<String, Integer> protectionLevels = new HashMap<>();
    private final Map<String, Integer> massBins = new HashMap<>();
    private final Map<String, Integer> geologicalActivity = new HashMap<>();
    private final Map<String, Integer> waterInventories = new HashMap<>();
    private final Map<String, Integer> habitabilityClasses = new HashMap<>();
    private final Map<String, Integer> moonCountBins = new HashMap<>();
    private final Map<String, Integer> semiMajorAxisBins = new HashMap<>();
    private final Map<String, int[]> tidalLockByDistance = new HashMap<>();
    private final Map<String, Integer> moonletBins = new HashMap<>();
    private int withRings = 0;

    private double massSum = 0;
    private double radiusSum = 0;
    private double gravitySum = 0;
    private double tempSum = 0;
    private int physicalCount = 0;

    public void increment() { count++; }

    public void addCompositionClass(String val) { compositionClasses.merge(val, 1, Integer::sum); }
    public void addSurfaceTempBin(String bin) { surfaceTempBins.merge(bin, 1, Integer::sum); }
    public void addAtmosphereClass(String val) { atmosphereClasses.merge(val, 1, Integer::sum); }
    public void addHzPosition(String val) { hzPositions.merge(val, 1, Integer::sum); }
    public void addTidallyLocked() { tidallyLocked++; }
    public void addProtectionLevel(String val) { protectionLevels.merge(val, 1, Integer::sum); }
    public void addMassBin(String bin) { massBins.merge(bin, 1, Integer::sum); }
    public void addGeologicalActivity(String val) { geologicalActivity.merge(val, 1, Integer::sum); }
    public void addWaterInventory(String val) { waterInventories.merge(val, 1, Integer::sum); }
    public void addHabitabilityClass(String val) { habitabilityClasses.merge(val, 1, Integer::sum); }
    public void addMoonCountBin(String bin) { moonCountBins.merge(bin, 1, Integer::sum); }
    public void addSemiMajorAxisBin(String bin) { semiMajorAxisBins.merge(bin, 1, Integer::sum); }
    public void addTidalLockAtDistance(String distanceBin, boolean locked) {
        int[] counts = tidalLockByDistance.computeIfAbsent(distanceBin, k -> new int[]{0, 0});
        counts[0]++; // total
        if (locked) counts[1]++; // locked
    }
    public void addMoonletBin(String bin) { moonletBins.merge(bin, 1, Integer::sum); }
    public void addRings() { withRings++; }

    public void addPhysicalProps(double mass, double radius, double gravity, double temp) {
        massSum += mass;
        radiusSum += radius;
        gravitySum += gravity;
        tempSum += temp;
        physicalCount++;
    }

    public Map<String, Object> toJson() {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("count", count);

        if (physicalCount > 0) {
            Map<String, Object> averages = new LinkedHashMap<>();
            averages.put("avgMass", round(massSum / physicalCount));
            averages.put("avgRadius", round(radiusSum / physicalCount));
            averages.put("avgGravity", round(gravitySum / physicalCount));
            averages.put("avgTemp", round(tempSum / physicalCount));
            json.put("averages", averages);
        }

        json.put("tidallyLocked", tidallyLocked);
        json.put("withRings", withRings);
        if (!compositionClasses.isEmpty()) json.put("compositionClasses", compositionClasses);
        if (!surfaceTempBins.isEmpty()) json.put("surfaceTempBins", surfaceTempBins);
        if (!atmosphereClasses.isEmpty()) json.put("atmosphereClasses", atmosphereClasses);
        if (!hzPositions.isEmpty()) json.put("hzPositions", hzPositions);
        if (!protectionLevels.isEmpty()) json.put("protectionLevels", protectionLevels);
        if (!massBins.isEmpty()) json.put("massBins", massBins);
        if (!moonCountBins.isEmpty()) json.put("moonCountBins", moonCountBins);
        if (!geologicalActivity.isEmpty()) json.put("geologicalActivity", geologicalActivity);
        if (!waterInventories.isEmpty()) json.put("waterInventories", waterInventories);
        if (!habitabilityClasses.isEmpty()) json.put("habitabilityClasses", habitabilityClasses);
        if (!semiMajorAxisBins.isEmpty()) json.put("semiMajorAxisAU", semiMajorAxisBins);
        if (!tidalLockByDistance.isEmpty()) {
            Map<String, Object> tlbd = new LinkedHashMap<>();
            tidalLockByDistance.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> {
                        Map<String, Object> entry = new LinkedHashMap<>();
                        entry.put("total", e.getValue()[0]);
                        entry.put("locked", e.getValue()[1]);
                        entry.put("lockRate", e.getValue()[0] > 0 ? round(e.getValue()[1] * 100.0 / e.getValue()[0]) : 0);
                        tlbd.put(e.getKey(), entry);
                    });
            json.put("tidalLockByDistance", tlbd);
        }
        if (!moonletBins.isEmpty()) json.put("moonletBins", moonletBins);

        return json;
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
