package com.brickroad.starcreator_webservice.probabilityreport;

import lombok.Getter;

import java.util.*;

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

    // Raw distance values for statistical analysis
    private final List<Double> distanceValues = new ArrayList<>();

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
    public void addDistanceValue(double au) { distanceValues.add(au); }

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

        // Distance statistics with IQR outlier exclusion
        if (!distanceValues.isEmpty()) {
            json.put("distanceStats", computeDistanceStats());
        }

        return json;
    }

    public Map<String, Object> computeDistanceStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        List<Double> sorted = new ArrayList<>(distanceValues);
        Collections.sort(sorted);

        int n = sorted.size();
        stats.put("count", n);
        stats.put("min", round3(sorted.get(0)));
        stats.put("max", round3(sorted.get(n - 1)));
        stats.put("mean", round3(sorted.stream().mapToDouble(Double::doubleValue).average().orElse(0)));
        stats.put("median", round3(percentile(sorted, 50)));

        // IQR-based outlier exclusion (need at least 4 samples)
        if (n >= 4) {
            double q1 = percentile(sorted, 25);
            double q3 = percentile(sorted, 75);
            double iqr = q3 - q1;
            double lowerFence = q1 - 1.5 * iqr;
            double upperFence = q3 + 1.5 * iqr;

            List<Double> inliers = sorted.stream()
                    .filter(v -> v >= lowerFence && v <= upperFence)
                    .toList();

            int excluded = n - inliers.size();
            double iqrMean = inliers.stream().mapToDouble(Double::doubleValue).average().orElse(0);

            stats.put("q1", round3(q1));
            stats.put("q3", round3(q3));
            stats.put("iqrMean", round3(iqrMean));
            stats.put("outliersExcluded", excluded);
        }

        return stats;
    }

    /** Linear interpolation percentile (0-100 scale) */
    private double percentile(List<Double> sorted, double pct) {
        if (sorted.size() == 1) return sorted.get(0);
        double idx = (pct / 100.0) * (sorted.size() - 1);
        int lo = (int) Math.floor(idx);
        int hi = Math.min(lo + 1, sorted.size() - 1);
        double frac = idx - lo;
        return sorted.get(lo) + frac * (sorted.get(hi) - sorted.get(lo));
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private double round3(double val) {
        return Math.round(val * 1000.0) / 1000.0;
    }
}
