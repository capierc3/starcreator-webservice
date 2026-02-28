package com.brickroad.starcreator_webservice.probabilityreport;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.enums.BinaryConfiguration;
import com.brickroad.starcreator_webservice.utils.BinaryStabilityLimits;
import com.brickroad.starcreator_webservice.utils.planets.OrbitalStabilityAnalyzer;
import lombok.Getter;

import java.util.*;

import static com.brickroad.starcreator_webservice.utils.planets.OrbitalStabilityAnalyzer.GLADMAN_FACTOR;

@Getter
public class OrbitStabilityCollector {

    // ===== Section 1: Core Stability Metrics =====
    private final Map<String, Integer> stabilityClassification = new HashMap<>();
    private final Map<String, Map<String, Integer>> stabilityByStarType = new HashMap<>();
    private final Map<Integer, Map<String, Integer>> stabilityByOrbitalPosition = new HashMap<>();
    private final Map<String, Map<String, Integer>> stabilityByBinaryConfig = new HashMap<>();

    // ===== Section 2: Gladman Delta Distribution =====
    private final Map<String, Integer> gladmanDeltaBins = new HashMap<>();
    private final List<Double> allGladmanDeltas = new ArrayList<>();
    private final List<Double> perSystemMinDeltas = new ArrayList<>();
    private int systemsWithAnyPairBelowCritical = 0;
    private int totalAdjacentPairs = 0;

    // ===== Section 3: Orbit Crossing Detection =====
    private int crossingPairCount = 0;
    private final List<double[]> crossingDetails = new ArrayList<>();
    private final Map<String, Integer> crossingByPlanetTypePair = new HashMap<>();

    // ===== Section 4: Eccentricity Validation =====
    private final Map<String, Integer> eccentricityBins = new HashMap<>();
    private final Map<String, double[]> eccentricityByPosition = new HashMap<>();

    // ===== Section 5: Timescale Analysis =====
    private final Map<String, Integer> timescaleBins = new HashMap<>();
    private int doomedButAliveCount = 0;
    private final Map<String, Integer> timescaleVsAgeBins = new HashMap<>();

    // ===== Section 6: Spacing Metrics =====
    private final Map<String, Integer> smaRatioBins = new HashMap<>();
    private final List<Double> minSmaGapsAU = new ArrayList<>();
    private int pairsWhereGladmanFloorWouldWiden = 0;
    private int totalSpacingPairsChecked = 0;

    // ===== Section 7: System-Level Summary =====
    private final Map<String, Integer> planetsPerSystemBins = new HashMap<>();
    private int systemsAllStable = 0;
    private int systemsAnyMarginalOrWorse = 0;
    private int systemsAnyCrossing = 0;
    private int systemsWithMultiplePlanets = 0;
    private double maxSmaSum = 0;
    private int maxSmaCount = 0;
    private final Map<String, Integer> systemOuterExtentBins = new HashMap<>();

    // ===== Section 8: Belt Stability =====
    private int totalBeltsAnalyzed = 0;
    private int beltOverlapCount = 0;
    private int beltPlanetOverlapCount = 0;
    private int beltsExceedingStabilityLimit = 0;
    private int beltsBelowCavityLimit = 0;
    private final List<String> beltOverlapDetails = new ArrayList<>();
    private final List<String> beltPlanetOverlapDetails = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════════
    //  Main Analysis Method
    // ═══════════════════════════════════════════════════════════════

    public void analyzeSystem(StarSystem system) {
        BinaryConfiguration binConfig = system.getBinaryConfiguration();
        String configName = binConfig != null ? binConfig.name() : "SINGLE";

        Map<Star, List<Planet>> planetsByParentStar = groupAndSortPlanets(system);

        int totalPlanetsInSystem = 0;
        boolean anyMarginalOrWorse = false;
        boolean anyCrossing = false;
        boolean allStable = true;
        double systemMinDelta = Double.MAX_VALUE;
        double systemMinGapAU = Double.MAX_VALUE;
        double systemMaxSma = 0;

        for (Map.Entry<Star, List<Planet>> entry : planetsByParentStar.entrySet()) {
            Star star = entry.getKey();
            List<Planet> sortedPlanets = entry.getValue();
            double starMassSolar = safe(star.getSolarMass(), 1.0);
            String starType = star.getType() != null ? star.getType() : "UNKNOWN";
            double starAgeMY = star.getAgeMY() != null ? star.getAgeMY() : 5000.0;

            totalPlanetsInSystem += sortedPlanets.size();

            // Collect eccentricity data for all planets
            for (int i = 0; i < sortedPlanets.size(); i++) {
                Planet p = sortedPlanets.get(i);
                collectEccentricityData(p, i + 1);
                double sma = safe(p.getSemiMajorAxisAU(), 0.0);
                if (sma > systemMaxSma) systemMaxSma = sma;
            }

            // Process adjacent pairs
            for (int i = 0; i < sortedPlanets.size() - 1; i++) {
                Planet inner = sortedPlanets.get(i);
                Planet outer = sortedPlanets.get(i + 1);
                int orbitalPos = i + 1;

                StabilityResult result = computeStability(inner, outer, starMassSolar);
                totalAdjacentPairs++;

                // Section 1: Core stability
                String classification = result.classification;
                stabilityClassification.merge(classification, 1, Integer::sum);
                stabilityByStarType
                        .computeIfAbsent(starType, k -> new HashMap<>())
                        .merge(classification, 1, Integer::sum);
                stabilityByOrbitalPosition
                        .computeIfAbsent(orbitalPos, k -> new HashMap<>())
                        .merge(classification, 1, Integer::sum);
                stabilityByBinaryConfig
                        .computeIfAbsent(configName, k -> new HashMap<>())
                        .merge(classification, 1, Integer::sum);

                if (!"STABLE".equals(classification)) {
                    allStable = false;
                    anyMarginalOrWorse = true;
                }
                if ("CROSSING".equals(classification)) anyCrossing = true;

                // Section 2: Gladman delta
                double delta = result.gladmanDelta;
                allGladmanDeltas.add(delta);
                gladmanDeltaBins.merge(binGladmanDelta(delta), 1, Integer::sum);
                if (delta < systemMinDelta) systemMinDelta = delta;

                // Section 3: Orbit crossing
                if (result.clearanceAU < 0) {
                    crossingPairCount++;
                    if (crossingDetails.size() < 1000) {
                        crossingDetails.add(new double[]{
                                result.clearanceAU,
                                safe(inner.getEccentricity(), 0.0),
                                safe(outer.getEccentricity(), 0.0)
                        });
                    }
                    String pairType = safeStr(inner.getPlanetType()) + " + " + safeStr(outer.getPlanetType());
                    crossingByPlanetTypePair.merge(pairType, 1, Integer::sum);
                }

                // Section 5: Timescale
                double timescaleMY = OrbitalStabilityAnalyzer.estimateInstabilityTimescaleMy(
                        safe(inner.getSemiMajorAxisAU(), 1.0),
                        safe(inner.getEarthMass(), 1.0),
                        safe(outer.getSemiMajorAxisAU(), 1.0),
                        safe(outer.getEarthMass(), 1.0),
                        starMassSolar);
                timescaleBins.merge(binTimescale(timescaleMY), 1, Integer::sum);

                if (timescaleMY < starAgeMY) {
                    doomedButAliveCount++;
                }
                String tVsA;
                if (timescaleMY > starAgeMY * 10) {
                    tVsA = "stable_long (>10x age)";
                } else if (timescaleMY > starAgeMY * 2) {
                    tVsA = "doomed_but_alive (2-10x age)";
                } else if (timescaleMY > starAgeMY) {
                    tVsA = "marginal (1-2x age)";
                } else {
                    tVsA = "should_not_exist (<age)";
                }
                timescaleVsAgeBins.merge(tVsA, 1, Integer::sum);

                // Section 6: Spacing
                double smaInner = safe(inner.getSemiMajorAxisAU(), 0.0);
                double smaOuter = safe(outer.getSemiMajorAxisAU(), 0.0);
                if (smaInner > 0) {
                    double smaRatio = smaOuter / smaInner;
                    smaRatioBins.merge(binSmaRatio(smaRatio), 1, Integer::sum);

                    double gapAU = smaOuter - smaInner;
                    if (gapAU < systemMinGapAU) systemMinGapAU = gapAU;
                }
                totalSpacingPairsChecked++;
                if (delta < GLADMAN_FACTOR) {
                    pairsWhereGladmanFloorWouldWiden++;
                }
            }
        }

        // System-level aggregation
        planetsPerSystemBins.merge(binPlanetCount(totalPlanetsInSystem), 1, Integer::sum);
        if (systemMaxSma > 0) {
            maxSmaSum += systemMaxSma;
            maxSmaCount++;
            systemOuterExtentBins.merge(binOuterExtent(systemMaxSma), 1, Integer::sum);
        }

        if (totalPlanetsInSystem >= 2) {
            systemsWithMultiplePlanets++;
            if (allStable) systemsAllStable++;
            if (anyMarginalOrWorse) systemsAnyMarginalOrWorse++;
            if (anyCrossing) systemsAnyCrossing++;

            if (systemMinDelta < Double.MAX_VALUE) {
                perSystemMinDeltas.add(systemMinDelta);
                if (systemMinDelta < GLADMAN_FACTOR) systemsWithAnyPairBelowCritical++;
            }
            if (systemMinGapAU < Double.MAX_VALUE) {
                minSmaGapsAU.add(systemMinGapAU);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Stability Computation (replicates OrbitalAnalysisHtmlGenerator)
    // ═══════════════════════════════════════════════════════════════

    private StabilityResult computeStability(Planet p1, Planet p2, double starMassSolar) {
        double sma1 = safe(p1.getSemiMajorAxisAU(), 1.0);
        double ecc1 = safe(p1.getEccentricity(), 0.0);
        double sma2 = safe(p2.getSemiMajorAxisAU(), 1.0);
        double ecc2 = safe(p2.getEccentricity(), 0.0);
        double mass1 = safe(p1.getEarthMass(), 1.0);
        double mass2 = safe(p2.getEarthMass(), 1.0);

        // Delegate to the canonical physics implementation
        double delta = OrbitalStabilityAnalyzer.gladmanDelta(sma1, mass1, sma2, mass2, starMassSolar);
        boolean crossing = OrbitalStabilityAnalyzer.orbitsAreCrossing(sma1, ecc1, sma2, ecc2);
        double clearance = OrbitalStabilityAnalyzer.orbitClearanceAU(sma1, ecc1, sma2, ecc2);

        // Use the same classification logic as the analyzer
        // (pass 0 for systemAge — we classify based on delta thresholds here,
        //  timescale is computed separately for bins)
        String classification;
        if (crossing) {
            classification = "CROSSING";
        } else if (delta < 3.46) {
            classification = "UNSTABLE";
        } else if (delta < 3.46 * 1.5) {
            classification = "MARGINAL";
        } else {
            classification = "STABLE";
        }

        return new StabilityResult(clearance, delta, classification);
    }

    private record StabilityResult(double clearanceAU, double gladmanDelta, String classification) {}

    // ═══════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════

    private Map<Star, List<Planet>> groupAndSortPlanets(StarSystem system) {
        Map<Star, List<Planet>> result = new LinkedHashMap<>();
        if (system.getPlanets() == null) return result;

        for (Planet planet : system.getPlanets()) {
            if (planet.getParentStar() != null
                    && planet.getSemiMajorAxisAU() != null) {
                result.computeIfAbsent(planet.getParentStar(), k -> new ArrayList<>()).add(planet);
            }
        }
        result.values().forEach(list ->
                list.sort(Comparator.comparingDouble(Planet::getSemiMajorAxisAU)));
        return result;
    }

    private void collectEccentricityData(Planet planet, int position) {
        double ecc = safe(planet.getEccentricity(), 0.0);
        eccentricityBins.merge(binEccentricity(ecc), 1, Integer::sum);

        String posGroup = binEccentricityPosition(position);
        double[] stats = eccentricityByPosition.computeIfAbsent(posGroup, k -> new double[]{0, 0});
        stats[0] += ecc;
        stats[1] += 1;
    }

    private static double safe(Double val, double fallback) {
        return val != null ? val : fallback;
    }

    private static String safeStr(String val) {
        return val != null ? val : "UNKNOWN";
    }

    // ═══════════════════════════════════════════════════════════════
    //  Statistical Getters
    // ═══════════════════════════════════════════════════════════════

    public double getMeanGladmanDelta() {
        return allGladmanDeltas.stream().mapToDouble(d -> d).average().orElse(0);
    }

    public double getMedianGladmanDelta() {
        if (allGladmanDeltas.isEmpty()) return 0;
        List<Double> sorted = new ArrayList<>(allGladmanDeltas);
        Collections.sort(sorted);
        int mid = sorted.size() / 2;
        return sorted.size() % 2 == 0 ? (sorted.get(mid - 1) + sorted.get(mid)) / 2.0 : sorted.get(mid);
    }

    public double getMeanPerSystemMinDelta() {
        return perSystemMinDeltas.stream().mapToDouble(d -> d).average().orElse(0);
    }

    public double getMedianMinSmaGapAU() {
        if (minSmaGapsAU.isEmpty()) return 0;
        List<Double> sorted = new ArrayList<>(minSmaGapsAU);
        Collections.sort(sorted);
        int mid = sorted.size() / 2;
        return sorted.size() % 2 == 0 ? (sorted.get(mid - 1) + sorted.get(mid)) / 2.0 : sorted.get(mid);
    }

    public double getGladmanFloorOverridePercent() {
        return totalSpacingPairsChecked > 0
                ? pairsWhereGladmanFloorWouldWiden * 100.0 / totalSpacingPairsChecked : 0;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Bin Methods
    // ═══════════════════════════════════════════════════════════════

    private String binGladmanDelta(double delta) {
        if (delta < 0) return "a: <0 (crossing)";
        if (delta < 2) return "b: 0-2 (unstable)";
        if (delta < GLADMAN_FACTOR) return "c: 2-3.46 (marginal)";
        if (delta < 5) return "d: 3.46-5";
        if (delta < 7) return "e: 5-7";
        if (delta < 10) return "f: 7-10";
        return "g: 10+";
    }

    private String binTimescale(double my) {
        if (my < 1) return "a: <1 MY";
        if (my < 10) return "b: 1-10 MY";
        if (my < 100) return "c: 10-100 MY";
        if (my < 1_000) return "d: 100-1K MY";
        if (my < 10_000) return "e: 1K-10K MY";
        if (my < 100_000) return "f: 10K-100K MY";
        return "g: >100K MY";
    }

    private String binSmaRatio(double ratio) {
        if (ratio < 1.3) return "a: <1.3";
        if (ratio < 1.5) return "b: 1.3-1.5";
        if (ratio < 2.0) return "c: 1.5-2.0";
        if (ratio < 3.0) return "d: 2.0-3.0";
        if (ratio < 5.0) return "e: 3.0-5.0";
        return "f: 5.0+";
    }

    private String binEccentricity(double ecc) {
        if (ecc < 0.02) return "a: 0-0.02";
        if (ecc < 0.05) return "b: 0.02-0.05";
        if (ecc < 0.10) return "c: 0.05-0.10";
        if (ecc < 0.15) return "d: 0.10-0.15";
        if (ecc < 0.20) return "e: 0.15-0.20";
        return "f: 0.20+";
    }

    private String binPlanetCount(int count) {
        if (count == 0) return "a: 0";
        if (count <= 2) return "b: 1-2";
        if (count <= 4) return "c: 3-4";
        if (count <= 6) return "d: 5-6";
        if (count <= 8) return "e: 7-8";
        return "f: 9+";
    }

    private String binOuterExtent(double au) {
        if (au < 1) return "a: <1 AU";
        if (au < 5) return "b: 1-5 AU";
        if (au < 10) return "c: 5-10 AU";
        if (au < 30) return "d: 10-30 AU";
        if (au < 100) return "e: 30-100 AU";
        return "f: 100+ AU";
    }

    private String binEccentricityPosition(int orbitalPos) {
        if (orbitalPos <= 2) return "a: inner (1-2)";
        if (orbitalPos <= 5) return "b: middle (3-5)";
        return "c: outer (6+)";
    }

    // ═══════════════════════════════════════════════════════════════
    //  Belt Stability Analysis
    // ═══════════════════════════════════════════════════════════════

    /**
     * Analyzes belt stability: belt-belt overlaps, belt-planet overlaps,
     * and binary stability limit violations.
     */
    public void analyzeBelts(StarSystem system) {
        BinaryConfiguration binConfig = system.getBinaryConfiguration();

        for (Star star : system.getStars()) {
            List<OrbitalBand> belts = star.getBands();
            if (belts == null || belts.isEmpty()) continue;

            // Sort belts by inner edge SMA
            List<OrbitalBand> sortedBelts = new ArrayList<>(belts);
            sortedBelts.sort(Comparator.comparingDouble(b ->
                    b.getInnerOrbit() != null && b.getInnerOrbit().getSemiMajorAxis() != null
                            ? b.getInnerOrbit().getSemiMajorAxis() : 0.0));

            totalBeltsAnalyzed += sortedBelts.size();

            // Belt-belt overlap check (adjacent belts around same star)
            for (int i = 0; i < sortedBelts.size() - 1; i++) {
                OrbitalBand b1 = sortedBelts.get(i);
                OrbitalBand b2 = sortedBelts.get(i + 1);

                double b1Outer = getBeltOuterAU(b1);
                double b2Inner = getBeltInnerAU(b2);

                if (b1Outer > b2Inner) {
                    beltOverlapCount++;
                    if (beltOverlapDetails.size() < 20) {
                        beltOverlapDetails.add(String.format("%s [%.2f-%.2f AU] overlaps %s [%.2f-%.2f AU]",
                                safeBeltName(b1), getBeltInnerAU(b1), b1Outer,
                                safeBeltName(b2), b2Inner, getBeltOuterAU(b2)));
                    }
                }
            }

            // Belt-planet overlap check
            List<Planet> planets = star.getPlanets();
            if (planets != null) {
                for (OrbitalBand belt : sortedBelts) {
                    double beltInner = getBeltInnerAU(belt);
                    double beltOuter = getBeltOuterAU(belt);

                    for (Planet planet : planets) {
                        // Skip dwarf planets — they naturally reside within belts
                        String pType = planet.getPlanetType();
                        if (pType != null && pType.toLowerCase().contains("dwarf")) continue;

                        double sma = safe(planet.getSemiMajorAxisAU(), 0.0);
                        double ecc = safe(planet.getEccentricity(), 0.0);
                        if (sma <= 0) continue;

                        double peri = sma * (1 - ecc);
                        double apo = sma * (1 + ecc);

                        // Overlap if planet's orbital range intersects belt's radial range
                        if (peri < beltOuter && apo > beltInner) {
                            beltPlanetOverlapCount++;
                            if (beltPlanetOverlapDetails.size() < 20) {
                                beltPlanetOverlapDetails.add(String.format(
                                        "%s [%.2f-%.2f AU] intersects planet %s orbit [%.2f-%.2f AU]",
                                        safeBeltName(belt), beltInner, beltOuter,
                                        planet.getName() != null ? planet.getName() : "?",
                                        peri, apo));
                            }
                        }
                    }
                }
            }

            // Multi-star stability checks
            if (binConfig != null && system.getBinarySeparationAu() != null) {
                double sepAU = system.getBinarySeparationAu();

                if (binConfig == BinaryConfiguration.S_TYPE_WIDE) {
                    // Check if belt outer edges exceed S-type critical SMA
                    Star companion = findCompanion(system, star);
                    if (companion != null) {
                        double hostMass = safe(star.getSolarMass(), 1.0);
                        double compMass = safe(companion.getSolarMass(), 1.0);
                        double sCrit = BinaryStabilityLimits.sTypeCriticalSMA(sepAU, hostMass, compMass);

                        for (OrbitalBand belt : sortedBelts) {
                            if (getBeltOuterAU(belt) > sCrit) {
                                beltsExceedingStabilityLimit++;
                            }
                        }
                    }
                } else if (binConfig == BinaryConfiguration.P_TYPE) {
                    // Check if belt inner edges are inside P-type critical SMA
                    java.util.List<Star> starList = new java.util.ArrayList<>(system.getStars());
                    double m1 = safe(starList.get(0).getSolarMass(), 1.0);
                    double m2 = starList.size() > 1
                            ? safe(starList.get(1).getSolarMass(), 1.0) : 0.0;
                    double pCrit = BinaryStabilityLimits.pTypeCriticalSMA(sepAU, m1, m2);

                    for (OrbitalBand belt : sortedBelts) {
                        if (getBeltInnerAU(belt) < pCrit) {
                            beltsBelowCavityLimit++;
                        }
                    }
                }
            }
        }
    }

    private double getBeltInnerAU(OrbitalBand belt) {
        return belt.getInnerOrbit() != null && belt.getInnerOrbit().getSemiMajorAxis() != null
                ? belt.getInnerOrbit().getSemiMajorAxis() : 0.0;
    }

    private double getBeltOuterAU(OrbitalBand belt) {
        return belt.getOuterOrbit() != null && belt.getOuterOrbit().getSemiMajorAxis() != null
                ? belt.getOuterOrbit().getSemiMajorAxis() : 0.0;
    }

    private String safeBeltName(OrbitalBand belt) {
        String name = belt.getName();
        return name != null ? name : (belt.getBandType() != null ? belt.getBandType() : "Belt");
    }

    private Star findCompanion(StarSystem system, Star star) {
        for (Star s : system.getStars()) {
            if (s != star) return s;
        }
        return null;
    }
}
