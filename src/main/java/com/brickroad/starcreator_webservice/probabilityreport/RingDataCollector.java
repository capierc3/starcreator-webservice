package com.brickroad.starcreator_webservice.probabilityreport;

import com.brickroad.starcreator_webservice.entity.ud.OrbitalBand;
import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class RingDataCollector {

    // ── Existing ──
    private final Map<String, Integer> ringTypes = new HashMap<>();

    // ── Optical & Visual Properties ──
    private final Map<String, Integer> opticalDepthBins = new HashMap<>();
    private final Map<String, Integer> colorDistribution = new HashMap<>();
    private final Map<String, Integer> visibilityDistribution = new HashMap<>();

    // ── Stability & Origin ──
    private final Map<String, Integer> stabilityDistribution = new HashMap<>();
    private final Map<String, Integer> originTypes = new HashMap<>();

    // ── Physical Properties ──
    private final Map<String, Integer> thicknessBins = new HashMap<>();
    private final Map<String, Integer> particleSizeBins = new HashMap<>();
    private final Map<String, Integer> ageBins = new HashMap<>();

    // ── Parent Tracking ──
    private final Map<String, Integer> parentPlanetTypes = new HashMap<>();

    // ── Boolean Counters ──
    private int shepherdMoonCount = 0;
    private int ringsWithGaps = 0;

    // ── Per-Type Breakdown ──
    private final Map<String, RingTypeBreakdown> perTypeData = new LinkedHashMap<>();

    public void analyzeData(OrbitalBand ring) {
        String type = ring.getBandType() != null ? ring.getBandType() : "Unknown";
        ringTypes.merge(type, 1, Integer::sum);

        // Optical depth
        if (ring.getOpticalDepth() != null) {
            opticalDepthBins.merge(binOpticalDepth(ring.getOpticalDepth()), 1, Integer::sum);
        }

        // Color
        if (ring.getColor() != null) {
            colorDistribution.merge(ring.getColor(), 1, Integer::sum);
        }

        // Visibility
        if (ring.getVisibility() != null) {
            visibilityDistribution.merge(ring.getVisibility(), 1, Integer::sum);
        }

        // Stability
        if (ring.getStability() != null) {
            stabilityDistribution.merge(ring.getStability(), 1, Integer::sum);
        }

        // Origin
        if (ring.getOriginType() != null) {
            originTypes.merge(ring.getOriginType(), 1, Integer::sum);
        }

        // Thickness
        if (ring.getThicknessKm() != null) {
            thicknessBins.merge(binThickness(ring.getThicknessKm()), 1, Integer::sum);
        }

        // Particle size (use max particle size)
        if (ring.getParticleSizeMaxM() != null) {
            particleSizeBins.merge(binParticleSize(ring.getParticleSizeMaxM()), 1, Integer::sum);
        }

        // Age
        if (ring.getEstimatedAgeMY() != null) {
            ageBins.merge(binAge(ring.getEstimatedAgeMY()), 1, Integer::sum);
        }

        // Parent planet type
        if (ring.getPlanet() != null && ring.getPlanet().getPlanetType() != null) {
            parentPlanetTypes.merge(ring.getPlanet().getPlanetType(), 1, Integer::sum);
        }

        // Structure flags
        if (Boolean.TRUE.equals(ring.getHasShepherdMoons())) shepherdMoonCount++;
        if (Boolean.TRUE.equals(ring.getHasGaps())) ringsWithGaps++;

        // Per-type breakdown
        RingTypeBreakdown breakdown = perTypeData.computeIfAbsent(type, k -> new RingTypeBreakdown());
        breakdown.analyze(ring);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Bin Methods
    // ═══════════════════════════════════════════════════════════════

    private String binOpticalDepth(double depth) {
        if (depth < 0.01) return "a: <0.01 (nearly transparent)";
        if (depth < 0.1) return "b: 0.01-0.1 (transparent)";
        if (depth < 0.5) return "c: 0.1-0.5 (translucent)";
        if (depth < 1.0) return "d: 0.5-1.0 (moderate)";
        return "e: 1.0+ (opaque)";
    }

    private String binThickness(double km) {
        if (km < 0.01) return "a: <10 m";
        if (km < 0.1) return "b: 10-100 m";
        if (km < 1.0) return "c: 0.1-1 km";
        if (km < 10) return "d: 1-10 km";
        return "e: 10+ km";
    }

    private String binParticleSize(double meters) {
        if (meters < 0.001) return "a: <1 mm (dust)";
        if (meters < 0.01) return "b: 1-10 mm (pebble)";
        if (meters < 1.0) return "c: 1 cm - 1 m (cobble)";
        if (meters < 10) return "d: 1-10 m (boulder)";
        return "e: 10+ m (moonlet)";
    }

    private String binAge(double my) {
        if (my < 10) return "a: <10 MY (young)";
        if (my < 100) return "b: 10-100 MY";
        if (my < 1000) return "c: 100-1K MY";
        if (my < 4000) return "d: 1K-4K MY";
        return "e: 4K+ MY (primordial)";
    }

    // ═══════════════════════════════════════════════════════════════
    //  Per-Type Breakdown
    // ═══════════════════════════════════════════════════════════════

    @Getter
    public static class RingTypeBreakdown {
        private int count = 0;
        private final Map<String, Integer> colors = new HashMap<>();
        private final Map<String, Integer> visibilities = new HashMap<>();
        private final Map<String, Integer> stabilities = new HashMap<>();
        private final Map<String, Integer> origins = new HashMap<>();

        private double opticalDepthSum = 0;
        private int opticalDepthCount = 0;
        private double thicknessSum = 0;
        private int thicknessCount = 0;
        private int shepherdCount = 0;
        private int gapsCount = 0;

        void analyze(OrbitalBand ring) {
            count++;

            if (ring.getColor() != null) colors.merge(ring.getColor(), 1, Integer::sum);
            if (ring.getVisibility() != null) visibilities.merge(ring.getVisibility(), 1, Integer::sum);
            if (ring.getStability() != null) stabilities.merge(ring.getStability(), 1, Integer::sum);
            if (ring.getOriginType() != null) origins.merge(ring.getOriginType(), 1, Integer::sum);

            if (ring.getOpticalDepth() != null) {
                opticalDepthSum += ring.getOpticalDepth();
                opticalDepthCount++;
            }
            if (ring.getThicknessKm() != null) {
                thicknessSum += ring.getThicknessKm();
                thicknessCount++;
            }

            if (Boolean.TRUE.equals(ring.getHasShepherdMoons())) shepherdCount++;
            if (Boolean.TRUE.equals(ring.getHasGaps())) gapsCount++;
        }

        public Map<String, Object> toJson() {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("count", count);
            if (!colors.isEmpty()) json.put("colors", colors);
            if (!visibilities.isEmpty()) json.put("visibilities", visibilities);
            if (!stabilities.isEmpty()) json.put("stabilities", stabilities);
            if (!origins.isEmpty()) json.put("origins", origins);
            if (opticalDepthCount > 0) json.put("avgOpticalDepth", Math.round(opticalDepthSum / opticalDepthCount * 1000.0) / 1000.0);
            if (thicknessCount > 0) json.put("avgThicknessKm", Math.round(thicknessSum / thicknessCount * 1000.0) / 1000.0);
            json.put("withShepherdMoons", shepherdCount);
            json.put("withGaps", gapsCount);
            return json;
        }
    }
}
