package com.brickroad.starcreator_webservice.probabilityreport;

import com.brickroad.starcreator_webservice.entity.ud.Asteroid;
import com.brickroad.starcreator_webservice.entity.ud.OrbitalBand;
import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.StarSystem;
import com.brickroad.starcreator_webservice.enums.BinaryConfiguration;
import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class BeltDataCollector {

    // ── Asteroid delegation ──
    private AsteroidDataCollector asteroidDataCollector;

    public BeltDataCollector() {}

    public BeltDataCollector(AsteroidDataCollector asteroidDataCollector) {
        this.asteroidDataCollector = asteroidDataCollector;
    }

    // ── Existing ──
    private final Map<String, Integer> beltTypes = new HashMap<>();
    private final Map<String, Integer> asteroidTypes = new HashMap<>();

    // ── Composition & Structure ──
    private final Map<String, Integer> compositionTypes = new HashMap<>();

    // ── Orbital Distribution Bins ──
    private final Map<String, Integer> widthBins = new HashMap<>();
    private final Map<String, Integer> innerEdgeBins = new HashMap<>();
    private final Map<String, Integer> outerEdgeBins = new HashMap<>();
    private final Map<String, Integer> massBins = new HashMap<>();
    private final Map<String, Integer> eccentricityBins = new HashMap<>();
    private final Map<String, Integer> inclinationBins = new HashMap<>();
    private final Map<String, Integer> objectCountBins = new HashMap<>();

    // ── Parent Tracking ──
    private final Map<String, Integer> parentStarTypes = new HashMap<>();
    private final Map<String, Integer> binaryConfigBelts = new HashMap<>();

    // ── Boolean Counters ──
    private int beltsWithGaps = 0;
    private int beltsWithCollisionalFamilies = 0;
    private int beltsWithDwarfPlanets = 0;

    // ── Accumulators for Averages ──
    private double totalMassSum = 0;
    private int massCount = 0;
    private double totalWidthSum = 0;
    private int widthCount = 0;
    private int familyCountSum = 0;
    private int familyCountCount = 0;

    // ── Per-Type Breakdown ──
    private final Map<String, BeltTypeBreakdown> perTypeData = new LinkedHashMap<>();

    public void analyzeData(OrbitalBand band, ProbabilityCounts counts, StarSystem system) {
        String typeCode = band.getBeltType() != null ? band.getBeltType().getCode() : band.getBandType();
        beltTypes.merge(typeCode, 1, Integer::sum);

        // Asteroids and dwarf planets
        counts.incrementAsteroidCount(band.getNotableAsteroids().size());
        for (Asteroid asteroid : band.getNotableAsteroids()) {
            asteroidTypes.merge(asteroid.getAsteroidType().getCode(), 1, Integer::sum);
            if (asteroidDataCollector != null) {
                asteroidDataCollector.analyzeData(asteroid, "Belt");
            }
        }
        counts.incrementDwarfPlanetCount(band.getDwarfPlanets().size());
        if (!band.getDwarfPlanets().isEmpty()) beltsWithDwarfPlanets++;

        // Composition
        if (band.getCompositionType() != null) {
            compositionTypes.merge(band.getCompositionType(), 1, Integer::sum);
        }

        // Width
        Double width = band.getWidth();
        if (width != null && width > 0) {
            widthBins.merge(binWidth(width), 1, Integer::sum);
            totalWidthSum += width;
            widthCount++;
        }

        // Inner/outer edge
        if (band.getInnerOrbit() != null && band.getInnerOrbit().getSemiMajorAxis() != null) {
            double inner = band.getInnerOrbit().getSemiMajorAxis();
            innerEdgeBins.merge(binDistance(inner), 1, Integer::sum);
        }
        if (band.getOuterOrbit() != null && band.getOuterOrbit().getSemiMajorAxis() != null) {
            double outer = band.getOuterOrbit().getSemiMajorAxis();
            outerEdgeBins.merge(binDistance(outer), 1, Integer::sum);
        }

        // Mass
        if (band.getTotalMassEarthMasses() != null && band.getTotalMassEarthMasses() > 0) {
            massBins.merge(binMass(band.getTotalMassEarthMasses()), 1, Integer::sum);
            totalMassSum += band.getTotalMassEarthMasses();
            massCount++;
        }

        // Eccentricity
        if (band.getAverageEccentricity() != null) {
            eccentricityBins.merge(binEccentricity(band.getAverageEccentricity()), 1, Integer::sum);
        }

        // Inclination
        if (band.getAverageInclinationDeg() != null) {
            inclinationBins.merge(binInclination(band.getAverageInclinationDeg()), 1, Integer::sum);
        }

        // Object count
        if (band.getEstimatedObjectCount() != null && band.getEstimatedObjectCount() > 0) {
            objectCountBins.merge(binObjectCount(band.getEstimatedObjectCount()), 1, Integer::sum);
        }

        // Parent star type
        if (band.getStar() != null && band.getStar().getType() != null) {
            parentStarTypes.merge(band.getStar().getType(), 1, Integer::sum);
        }

        // Binary configuration tracking
        BinaryConfiguration binConfig = system.getBinaryConfiguration();
        String configName = binConfig != null ? binConfig.name() : "SINGLE";
        binaryConfigBelts.merge(configName, 1, Integer::sum);

        // Structure flags
        if (Boolean.TRUE.equals(band.getHasGaps())) beltsWithGaps++;
        if (Boolean.TRUE.equals(band.getHasCollisionalFamilies())) {
            beltsWithCollisionalFamilies++;
            if (band.getFamilyCount() != null) {
                familyCountSum += band.getFamilyCount();
                familyCountCount++;
            }
        }

        // Per-type breakdown
        String bandType = band.getBandType() != null ? band.getBandType() : "Unknown";
        BeltTypeBreakdown breakdown = perTypeData.computeIfAbsent(bandType, k -> new BeltTypeBreakdown());
        breakdown.analyze(band);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Bin Methods
    // ═══════════════════════════════════════════════════════════════

    private String binWidth(double au) {
        if (au < 0.5) return "a: <0.5 AU";
        if (au < 2) return "b: 0.5-2 AU";
        if (au < 10) return "c: 2-10 AU";
        if (au < 30) return "d: 10-30 AU";
        return "e: 30+ AU";
    }

    private String binDistance(double au) {
        if (au < 1) return "a: <1 AU";
        if (au < 3) return "b: 1-3 AU";
        if (au < 10) return "c: 3-10 AU";
        if (au < 30) return "d: 10-30 AU";
        if (au < 50) return "e: 30-50 AU";
        if (au < 100) return "f: 50-100 AU";
        return "g: 100+ AU";
    }

    private String binMass(double earthMasses) {
        if (earthMasses < 0.001) return "a: <0.001 M\u2295";
        if (earthMasses < 0.01) return "b: 0.001-0.01 M\u2295";
        if (earthMasses < 0.1) return "c: 0.01-0.1 M\u2295";
        if (earthMasses < 1.0) return "d: 0.1-1.0 M\u2295";
        return "e: 1.0+ M\u2295";
    }

    private String binEccentricity(double ecc) {
        if (ecc < 0.05) return "a: 0-0.05";
        if (ecc < 0.10) return "b: 0.05-0.10";
        if (ecc < 0.15) return "c: 0.10-0.15";
        if (ecc < 0.20) return "d: 0.15-0.20";
        return "e: 0.20+";
    }

    private String binInclination(double deg) {
        if (deg < 5) return "a: <5\u00B0";
        if (deg < 10) return "b: 5-10\u00B0";
        if (deg < 20) return "c: 10-20\u00B0";
        if (deg < 30) return "d: 20-30\u00B0";
        return "e: 30+\u00B0";
    }

    private String binObjectCount(long count) {
        if (count < 1_000) return "a: <1K";
        if (count < 10_000) return "b: 1K-10K";
        if (count < 100_000) return "c: 10K-100K";
        if (count < 1_000_000) return "d: 100K-1M";
        if (count < 1_000_000_000) return "e: 1M-1B";
        return "f: 1B+";
    }

    // ═══════════════════════════════════════════════════════════════
    //  Per-Type Breakdown
    // ═══════════════════════════════════════════════════════════════

    @Getter
    public static class BeltTypeBreakdown {
        private int count = 0;
        private final Map<String, Integer> compositionTypes = new HashMap<>();

        // Accumulators
        private double widthSum = 0;
        private int widthCount = 0;
        private double massSum = 0;
        private int massCount = 0;
        private double innerEdgeSum = 0;
        private double outerEdgeSum = 0;
        private int edgeCount = 0;
        private double eccSum = 0;
        private int eccCount = 0;
        private double incSum = 0;
        private int incCount = 0;
        private long objectCountSum = 0;
        private int objectCountCount = 0;
        private int gapsCount = 0;
        private int familiesCount = 0;
        private int beltsWithDwarfPlanets = 0;
        private int totalDwarfPlanets = 0;
        private final Map<String, Integer> dwarfCompositionTypes = new HashMap<>();

        void analyze(OrbitalBand band) {
            count++;

            if (band.getCompositionType() != null) {
                compositionTypes.merge(band.getCompositionType(), 1, Integer::sum);
            }

            Double width = band.getWidth();
            if (width != null && width > 0) {
                widthSum += width;
                widthCount++;
            }

            if (band.getTotalMassEarthMasses() != null && band.getTotalMassEarthMasses() > 0) {
                massSum += band.getTotalMassEarthMasses();
                massCount++;
            }

            if (band.getInnerOrbit() != null && band.getInnerOrbit().getSemiMajorAxis() != null
                    && band.getOuterOrbit() != null && band.getOuterOrbit().getSemiMajorAxis() != null) {
                innerEdgeSum += band.getInnerOrbit().getSemiMajorAxis();
                outerEdgeSum += band.getOuterOrbit().getSemiMajorAxis();
                edgeCount++;
            }

            if (band.getAverageEccentricity() != null) {
                eccSum += band.getAverageEccentricity();
                eccCount++;
            }

            if (band.getAverageInclinationDeg() != null) {
                incSum += band.getAverageInclinationDeg();
                incCount++;
            }

            if (band.getEstimatedObjectCount() != null && band.getEstimatedObjectCount() > 0) {
                objectCountSum += band.getEstimatedObjectCount();
                objectCountCount++;
            }

            if (Boolean.TRUE.equals(band.getHasGaps())) gapsCount++;
            if (Boolean.TRUE.equals(band.getHasCollisionalFamilies())) familiesCount++;
            int dwarfs = band.getDwarfPlanets().size();
            if (dwarfs > 0) beltsWithDwarfPlanets++;
            totalDwarfPlanets += dwarfs;
            for (Planet dwarf : band.getDwarfPlanets()) {
                String comp = dwarf.getCompositionClassification();
                dwarfCompositionTypes.merge(comp != null ? comp : "UNKNOWN", 1, Integer::sum);
            }
        }

        public Map<String, Object> toJson() {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("count", count);
            if (!compositionTypes.isEmpty()) json.put("compositionTypes", compositionTypes);
            if (widthCount > 0) json.put("avgWidthAU", Math.round(widthSum / widthCount * 1000.0) / 1000.0);
            if (massCount > 0) json.put("avgMassEarth", Math.round(massSum / massCount * 10000.0) / 10000.0);
            if (edgeCount > 0) {
                json.put("avgInnerEdgeAU", Math.round(innerEdgeSum / edgeCount * 1000.0) / 1000.0);
                json.put("avgOuterEdgeAU", Math.round(outerEdgeSum / edgeCount * 1000.0) / 1000.0);
            }
            if (eccCount > 0) json.put("avgEccentricity", Math.round(eccSum / eccCount * 10000.0) / 10000.0);
            if (incCount > 0) json.put("avgInclinationDeg", Math.round(incSum / incCount * 100.0) / 100.0);
            if (objectCountCount > 0) json.put("avgObjectCount", objectCountSum / objectCountCount);
            json.put("withGaps", gapsCount);
            json.put("withCollisionalFamilies", familiesCount);
            json.put("withDwarfPlanets", beltsWithDwarfPlanets);
            json.put("totalDwarfPlanets", totalDwarfPlanets);
            if (!dwarfCompositionTypes.isEmpty()) json.put("dwarfCompositionTypes", dwarfCompositionTypes);
            return json;
        }
    }
}
