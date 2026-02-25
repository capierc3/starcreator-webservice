package com.brickroad.starcreator_webservice.probabilityreport;

import com.brickroad.starcreator_webservice.entity.ud.Asteroid;
import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Collects unified statistics on notable asteroids from all sources (belts and trojans).
 * Tracks spectral types, physical properties, size distributions, and source breakdowns.
 */
@Getter
public class AsteroidDataCollector {

    // ── Source Tracking ──
    private final Map<String, Integer> sourceCounts = new HashMap<>();

    // ── Spectral Type Distribution ──
    private final Map<String, Integer> asteroidTypes = new HashMap<>();

    // ── Size Distribution (diameter km) ──
    private final Map<String, Integer> diameterBins = new HashMap<>();

    // ── Mass Distribution (Earth masses) ──
    private final Map<String, Integer> massBins = new HashMap<>();

    // ── Density Distribution (g/cm³) ──
    private final Map<String, Integer> densityBins = new HashMap<>();

    // ── Albedo Distribution ──
    private final Map<String, Integer> albedoBins = new HashMap<>();

    // ── Cratering Level ──
    private final Map<String, Integer> crateringLevels = new HashMap<>();

    // ── Orbital Distance (AU) ──
    private final Map<String, Integer> distanceBins = new HashMap<>();

    // ── Counters ──
    private int totalAsteroids = 0;
    private int totalWithMoons = 0;
    private int totalMoonCount = 0;
    private int totalDifferentiated = 0;
    private int totalWithRegolith = 0;

    // ── Aggregates for averages ──
    private double diameterSum = 0;
    private int diameterCount = 0;
    private double massSum = 0;
    private int massCount = 0;
    private double densitySum = 0;
    private int densityCount = 0;
    private double albedoSum = 0;
    private int albedoCount = 0;

    // ── Per Asteroid-Type Breakdown ──
    private final Map<String, AsteroidTypeBreakdown> perTypeData = new LinkedHashMap<>();

    /**
     * Analyze a single notable asteroid.
     * @param asteroid the asteroid entity
     * @param source "Belt" or "Trojan"
     */
    public void analyzeData(Asteroid asteroid, String source) {
        totalAsteroids++;
        sourceCounts.merge(source, 1, Integer::sum);

        // Spectral type
        String typeCode = "Unknown";
        if (asteroid.getAsteroidType() != null && asteroid.getAsteroidType().getCode() != null) {
            typeCode = asteroid.getAsteroidType().getCode();
        }
        asteroidTypes.merge(typeCode, 1, Integer::sum);

        // Per-type breakdown
        AsteroidTypeBreakdown typeBreakdown = perTypeData.computeIfAbsent(typeCode, k -> new AsteroidTypeBreakdown());
        typeBreakdown.increment();
        typeBreakdown.addSource(source);

        // Diameter from dimensionsKm (format: "A x B x C")
        double diameter = parseLargestDimension(asteroid.getDimensionsKm());
        if (diameter > 0) {
            diameterBins.merge(binDiameter(diameter), 1, Integer::sum);
            diameterSum += diameter;
            diameterCount++;
            typeBreakdown.addDiameter(diameter);
        }

        // Mass
        if (asteroid.getEarthMass() != null && asteroid.getEarthMass() > 0) {
            massBins.merge(binMass(asteroid.getEarthMass()), 1, Integer::sum);
            massSum += asteroid.getEarthMass();
            massCount++;
        }

        // Density
        if (asteroid.getDensity() != null && asteroid.getDensity() > 0) {
            densityBins.merge(binDensity(asteroid.getDensity()), 1, Integer::sum);
            densitySum += asteroid.getDensity();
            densityCount++;
            typeBreakdown.addDensity(asteroid.getDensity());
        }

        // Albedo
        if (asteroid.getAlbedo() != null) {
            albedoBins.merge(binAlbedo(asteroid.getAlbedo()), 1, Integer::sum);
            albedoSum += asteroid.getAlbedo();
            albedoCount++;
            typeBreakdown.addAlbedo(asteroid.getAlbedo());
        }

        // Differentiation
        if (Boolean.TRUE.equals(asteroid.getIsDifferentiated())) {
            totalDifferentiated++;
            typeBreakdown.incrementDifferentiated();
        }

        // Moons
        if (Boolean.TRUE.equals(asteroid.getHasMoon())) {
            totalWithMoons++;
            if (asteroid.getMoonCount() != null) {
                totalMoonCount += asteroid.getMoonCount();
            }
            typeBreakdown.incrementWithMoons();
        }

        // Cratering
        if (asteroid.getCrateringLevel() != null) {
            crateringLevels.merge(asteroid.getCrateringLevel(), 1, Integer::sum);
        }

        // Regolith
        if (Boolean.TRUE.equals(asteroid.getHasRegolith())) {
            totalWithRegolith++;
        }

        // Orbital distance
        if (asteroid.getSemiMajorAxisAu() != null) {
            distanceBins.merge(binDistance(asteroid.getSemiMajorAxisAu()), 1, Integer::sum);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Parsing
    // ═══════════════════════════════════════════════════════════════

    /**
     * Parse the largest dimension from "A x B x C" format.
     * Returns the first (largest) value, or 0 if unparseable.
     */
    private double parseLargestDimension(String dimensionsKm) {
        if (dimensionsKm == null || dimensionsKm.isBlank()) return 0;
        try {
            String[] parts = dimensionsKm.split("\\s*x\\s*");
            return Double.parseDouble(parts[0].trim());
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Bin Methods
    // ═══════════════════════════════════════════════════════════════

    private String binDiameter(double km) {
        if (km < 50) return "a: <50 km";
        if (km < 100) return "b: 50-100 km";
        if (km < 200) return "c: 100-200 km";
        if (km < 400) return "d: 200-400 km";
        if (km < 700) return "e: 400-700 km";
        return "f: 700+ km";
    }

    private String binMass(double earthMass) {
        if (earthMass < 1e-10) return "a: <1e-10 M\u2295";
        if (earthMass < 1e-8) return "b: 1e-10 to 1e-8 M\u2295";
        if (earthMass < 1e-6) return "c: 1e-8 to 1e-6 M\u2295";
        if (earthMass < 1e-4) return "d: 1e-6 to 1e-4 M\u2295";
        return "e: \u22651e-4 M\u2295";
    }

    private String binDensity(double density) {
        if (density < 1.5) return "a: <1.5 (icy/porous)";
        if (density < 3.0) return "b: 1.5-3.0 (carbonaceous)";
        if (density < 5.0) return "c: 3.0-5.0 (silicate)";
        if (density < 7.0) return "d: 5.0-7.0 (metallic)";
        return "e: 7.0+ (iron-rich)";
    }

    private String binAlbedo(double albedo) {
        if (albedo < 0.05) return "a: <0.05 (very dark)";
        if (albedo < 0.10) return "b: 0.05-0.10 (dark)";
        if (albedo < 0.20) return "c: 0.10-0.20 (moderate)";
        if (albedo < 0.40) return "d: 0.20-0.40 (bright)";
        return "e: 0.40+ (very bright)";
    }

    private String binDistance(double au) {
        if (au < 1.0) return "a: <1 AU";
        if (au < 3.0) return "b: 1-3 AU";
        if (au < 10.0) return "c: 3-10 AU";
        if (au < 30.0) return "d: 10-30 AU";
        if (au < 50.0) return "e: 30-50 AU";
        return "f: 50+ AU";
    }

    // ═══════════════════════════════════════════════════════════════
    //  Per Asteroid-Type Breakdown
    // ═══════════════════════════════════════════════════════════════

    @Getter
    public static class AsteroidTypeBreakdown {
        private int count = 0;
        private final Map<String, Integer> sources = new HashMap<>();

        private double diameterSum = 0;
        private int diameterCount = 0;
        private double densitySum = 0;
        private int densityCount = 0;
        private double albedoSum = 0;
        private int albedoCount = 0;
        private int differentiated = 0;
        private int withMoons = 0;

        void increment() { count++; }
        void addSource(String source) { sources.merge(source, 1, Integer::sum); }
        void addDiameter(double km) { diameterSum += km; diameterCount++; }
        void addDensity(double d) { densitySum += d; densityCount++; }
        void addAlbedo(double a) { albedoSum += a; albedoCount++; }
        void incrementDifferentiated() { differentiated++; }
        void incrementWithMoons() { withMoons++; }

        public Map<String, Object> toJson() {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("count", count);
            if (!sources.isEmpty()) json.put("sources", sources);
            if (diameterCount > 0) json.put("avgDiameterKm", round(diameterSum / diameterCount));
            if (densityCount > 0) json.put("avgDensity", round(densitySum / densityCount));
            if (albedoCount > 0) json.put("avgAlbedo", round3(albedoSum / albedoCount));
            json.put("differentiated", differentiated);
            json.put("withMoons", withMoons);
            return json;
        }

        private double round(double val) {
            return Math.round(val * 100.0) / 100.0;
        }

        private double round3(double val) {
            return Math.round(val * 1000.0) / 1000.0;
        }
    }
}
