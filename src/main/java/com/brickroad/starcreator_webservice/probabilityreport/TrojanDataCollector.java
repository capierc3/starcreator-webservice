package com.brickroad.starcreator_webservice.probabilityreport;

import com.brickroad.starcreator_webservice.entity.ud.Asteroid;
import com.brickroad.starcreator_webservice.entity.ud.OrbitalBand;
import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Collects statistics on Trojan swarm bands for the probability report.
 * Tracks formation rates, mass distributions, object counts, swarm sizes,
 * and parent planet type correlations.
 */
@Getter
public class TrojanDataCollector {

    // ── Asteroid delegation ──
    private AsteroidDataCollector asteroidDataCollector;

    public TrojanDataCollector() {}

    public TrojanDataCollector(AsteroidDataCollector asteroidDataCollector) {
        this.asteroidDataCollector = asteroidDataCollector;
    }

    // ── Lagrange Point Distribution ──
    private final Map<String, Integer> lagrangePointCounts = new HashMap<>();

    // ── Parent Planet Type ──
    private final Map<String, Integer> parentPlanetTypes = new HashMap<>();

    // ── Mass Distribution ──
    private final Map<String, Integer> massBins = new HashMap<>();

    // ── Object Count Distribution ──
    private final Map<String, Integer> objectCountBins = new HashMap<>();

    // ── Libration Amplitude Distribution ──
    private final Map<String, Integer> librationAmplitudeBins = new HashMap<>();

    // ── Swarm Width (radial extent in AU) ──
    private final Map<String, Integer> widthBins = new HashMap<>();

    // ── Tier Classification ──
    private final Map<String, Integer> tierCounts = new HashMap<>();

    // ── Counters ──
    private int totalTrojans = 0;
    private int trojansWithAsteroids = 0;
    private int trojansWithMoons = 0;
    private int totalNotableAsteroids = 0;

    // ── Aggregates for averages ──
    private double totalMassSum = 0;
    private int massCount = 0;
    private double totalWidthSum = 0;
    private int widthCount = 0;
    private long totalObjectCountSum = 0;
    private int objectCountCount = 0;
    private double totalLibrationSum = 0;
    private int librationCount = 0;

    // ── Per Planet-Type Breakdown ──
    private final Map<String, TrojanPlanetTypeBreakdown> perPlanetType = new LinkedHashMap<>();

    public void analyzeData(OrbitalBand trojan) {
        totalTrojans++;

        // Lagrange point
        String lp = trojan.getLagrangePoint() != null ? trojan.getLagrangePoint() : "Unknown";
        lagrangePointCounts.merge(lp, 1, Integer::sum);

        // Parent planet type
        String planetType = "Unknown";
        if (trojan.getPlanet() != null && trojan.getPlanet().getPlanetType() != null) {
            planetType = trojan.getPlanet().getPlanetType();
        }
        parentPlanetTypes.merge(planetType, 1, Integer::sum);

        // Mass
        if (trojan.getTotalMassEarthMasses() != null) {
            double mass = trojan.getTotalMassEarthMasses();
            massBins.merge(binMass(mass), 1, Integer::sum);
            totalMassSum += mass;
            massCount++;
        }

        // Object count
        if (trojan.getEstimatedObjectCount() != null) {
            long count = trojan.getEstimatedObjectCount();
            objectCountBins.merge(binObjectCount(count), 1, Integer::sum);
            totalObjectCountSum += count;
            objectCountCount++;
        }

        // Libration amplitude
        if (trojan.getLibrationAmplitudeDeg() != null) {
            double amp = trojan.getLibrationAmplitudeDeg();
            librationAmplitudeBins.merge(binLibration(amp), 1, Integer::sum);
            totalLibrationSum += amp;
            librationCount++;
        }

        // Width (radial extent)
        Double width = trojan.getWidth();
        if (width != null) {
            widthBins.merge(binWidth(width), 1, Integer::sum);
            totalWidthSum += width;
            widthCount++;
        }

        // Tier classification
        double massEarth = trojan.getTotalMassEarthMasses() != null ? trojan.getTotalMassEarthMasses() : 0;
        String tier;
        if (massEarth >= 1e-6) {
            tier = "Large (≥1e-6 M⊕)";
        } else if (massEarth >= 1e-9) {
            tier = "Medium (≥1e-9 M⊕)";
        } else {
            tier = "Tiny (<1e-9 M⊕)";
        }
        tierCounts.merge(tier, 1, Integer::sum);

        // Notable asteroids
        int asteroidCount = trojan.getNotableAsteroids() != null ? trojan.getNotableAsteroids().size() : 0;
        if (asteroidCount > 0) {
            trojansWithAsteroids++;
            totalNotableAsteroids += asteroidCount;
            if (asteroidDataCollector != null) {
                for (Asteroid asteroid : trojan.getNotableAsteroids()) {
                    asteroidDataCollector.analyzeData(asteroid, "Trojan");
                }
            }
        }

        // Per planet-type breakdown
        TrojanPlanetTypeBreakdown breakdown = perPlanetType.computeIfAbsent(planetType, k -> new TrojanPlanetTypeBreakdown());
        breakdown.analyze(trojan, lp, tier);
    }

    /**
     * Called externally when a Trojan moon is detected (planet-level tracking).
     * Also tracks per planet-type for the summary table.
     */
    public void incrementTrojansWithMoons(String planetType) {
        trojansWithMoons++;
        if (planetType != null) {
            TrojanPlanetTypeBreakdown breakdown = perPlanetType.get(planetType);
            if (breakdown != null) {
                breakdown.incrementWithMoons();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Bin Methods
    // ═══════════════════════════════════════════════════════════════

    private String binMass(double massEarth) {
        if (massEarth < 1e-12) return "a: <1e-12 (trace)";
        if (massEarth < 1e-10) return "b: 1e-12 to 1e-10";
        if (massEarth < 1e-9) return "c: 1e-10 to 1e-9";
        if (massEarth < 1e-7) return "d: 1e-9 to 1e-7";
        if (massEarth < 1e-6) return "e: 1e-7 to 1e-6";
        if (massEarth < 1e-4) return "f: 1e-6 to 1e-4";
        return "g: ≥1e-4 (massive)";
    }

    private String binObjectCount(long count) {
        if (count < 10) return "a: <10";
        if (count < 100) return "b: 10-100";
        if (count < 1_000) return "c: 100-1K";
        if (count < 10_000) return "d: 1K-10K";
        if (count < 100_000) return "e: 10K-100K";
        if (count < 1_000_000) return "f: 100K-1M";
        return "g: 1M+ (Jupiter-class)";
    }

    private String binLibration(double degrees) {
        if (degrees < 10) return "a: <10° (tight)";
        if (degrees < 25) return "b: 10-25°";
        if (degrees < 40) return "c: 25-40° (typical)";
        if (degrees < 55) return "d: 40-55°";
        if (degrees < 70) return "e: 55-70° (wide)";
        return "f: 70°+ (very wide)";
    }

    private String binWidth(double widthAU) {
        if (widthAU < 0.01) return "a: <0.01 AU (tiny)";
        if (widthAU < 0.05) return "b: 0.01-0.05 AU";
        if (widthAU < 0.1) return "c: 0.05-0.1 AU";
        if (widthAU < 0.5) return "d: 0.1-0.5 AU";
        if (widthAU < 1.0) return "e: 0.5-1.0 AU";
        return "f: 1.0+ AU (massive)";
    }

    // ═══════════════════════════════════════════════════════════════
    //  Per Planet-Type Breakdown
    // ═══════════════════════════════════════════════════════════════

    @Getter
    public static class TrojanPlanetTypeBreakdown {
        private int count = 0;
        private int planetsWithTrojans = 0;  // tracks unique planets (L4+L5 = 1 planet)
        private final Map<String, Integer> lagrangePoints = new HashMap<>();
        private final Map<String, Integer> tiers = new HashMap<>();

        private double massSum = 0;
        private int massCount = 0;
        private long objectCountSum = 0;
        private int objectCountCount = 0;
        private int withAsteroids = 0;
        private int withMoons = 0;

        void analyze(OrbitalBand trojan, String lagrangePoint, String tier) {
            count++;
            lagrangePoints.merge(lagrangePoint, 1, Integer::sum);
            tiers.merge(tier, 1, Integer::sum);

            if (trojan.getTotalMassEarthMasses() != null) {
                massSum += trojan.getTotalMassEarthMasses();
                massCount++;
            }
            if (trojan.getEstimatedObjectCount() != null) {
                objectCountSum += trojan.getEstimatedObjectCount();
                objectCountCount++;
            }
            if (trojan.getNotableAsteroids() != null && !trojan.getNotableAsteroids().isEmpty()) {
                withAsteroids++;
            }
        }

        void incrementWithMoons() {
            withMoons++;
        }

        public Map<String, Object> toJson() {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("swarmCount", count);
            if (!lagrangePoints.isEmpty()) json.put("lagrangePoints", lagrangePoints);
            if (!tiers.isEmpty()) json.put("tiers", tiers);
            if (massCount > 0) json.put("avgMassEarth", round(massSum / massCount));
            if (objectCountCount > 0) {
                json.put("avgObjectCount", objectCountSum / objectCountCount);
                json.put("totalObjectCount", objectCountSum);
            }
            json.put("withNotableAsteroids", withAsteroids);
            json.put("withMoons", withMoons);
            return json;
        }

        private double round(double val) {
            return Math.round(val * 1e12) / 1e12; // Need high precision for tiny masses
        }
    }
}
