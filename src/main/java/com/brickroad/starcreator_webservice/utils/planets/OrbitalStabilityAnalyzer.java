package com.brickroad.starcreator_webservice.utils.planets;

/**
 * Orbital stability analysis utilities using Gladman (1993) criterion
 * and Chambers et al. (1996) instability timescale estimates.
 *
 * Used during planet generation to enforce dynamically stable orbits,
 * and post-generation to annotate systems with stability metadata.
 */
public class OrbitalStabilityAnalyzer {

    /** Earth masses per solar mass */
    private static final double SOLAR_MASS_IN_EARTH = 333000.0;

    /**
     * Gladman (1993) critical separation factor.
     * Two planets need Δa > GLADMAN_FACTOR * R_H,mutual to be Hill-stable.
     * 3.46 corresponds to the circular-orbit limit; eccentric orbits need more.
     */
    private static final double GLADMAN_FACTOR = 3.46;

    /**
     * Safety margin above Gladman minimum for generation.
     * We target 2x Gladman to produce robustly stable systems by default.
     */
    private static final double GENERATION_SAFETY_FACTOR = 2.0;

    // =========================================================================
    // Core calculations
    // =========================================================================

    /**
     * Mutual Hill radius for two adjacent planets (in AU).
     *
     * R_H = ((m1 + m2) / (3 * M_star))^(1/3) * (a1 + a2) / 2
     */
    public static double mutualHillRadius(double sma1AU, double mass1Earth,
                                           double sma2AU, double mass2Earth,
                                           double starMassSolar) {
        double starMassEarth = starMassSolar * SOLAR_MASS_IN_EARTH;
        double mu = (mass1Earth + mass2Earth) / (3.0 * starMassEarth);
        return Math.cbrt(mu) * (sma1AU + sma2AU) / 2.0;
    }

    /**
     * Compute the orbital separation in units of mutual Hill radii (Δ).
     * Gladman criterion: system is Hill-stable if Δ > 3.46 for all pairs.
     *
     * @return Δ = (a2 - a1) / R_H,mutual
     */
    public static double gladmanDelta(double sma1AU, double mass1Earth,
                                       double sma2AU, double mass2Earth,
                                       double starMassSolar) {
        double rH = mutualHillRadius(sma1AU, mass1Earth, sma2AU, mass2Earth, starMassSolar);
        if (rH <= 0) return Double.MAX_VALUE;
        return (sma2AU - sma1AU) / rH;
    }

    /**
     * Check if two orbits physically cross (aphelion of inner >= perihelion of outer).
     */
    public static boolean orbitsAreCrossing(double sma1, double ecc1, double sma2, double ecc2) {
        double aphelion1 = sma1 * (1.0 + ecc1);
        double perihelion2 = sma2 * (1.0 - ecc2);
        return aphelion1 >= perihelion2;
    }

    /**
     * Clearance between aphelion of inner orbit and perihelion of outer orbit (AU).
     * Positive = gap, negative = overlap.
     */
    public static double orbitClearanceAU(double sma1, double ecc1, double sma2, double ecc2) {
        double aphelion1 = sma1 * (1.0 + ecc1);
        double perihelion2 = sma2 * (1.0 - ecc2);
        return perihelion2 - aphelion1;
    }

    /**
     * Minimum SMA separation needed for Gladman stability at the generation safety factor.
     * Used to enforce minimum orbit spacing during planet placement.
     *
     * @return minimum Δa in AU for the pair to be considered safe
     */
    public static double minimumSafeSpacingAU(double sma1AU, double mass1Earth,
                                                double mass2EstEarth,
                                                double starMassSolar) {
        // Estimate sma2 ≈ sma1 for Hill radius calculation (refined iteratively in practice)
        double rH = mutualHillRadius(sma1AU, mass1Earth, sma1AU, mass2EstEarth, starMassSolar);
        return GLADMAN_FACTOR * GENERATION_SAFETY_FACTOR * rH;
    }

    /**
     * Maximum eccentricity for the outer planet that won't cause orbit crossing,
     * given the inner planet's orbit and the outer planet's SMA.
     *
     * perihelion_outer > aphelion_inner
     * sma2 * (1 - e2) > sma1 * (1 + e1)
     * e2 < 1 - (sma1 * (1 + e1)) / sma2
     *
     * Includes a safety margin to keep orbits from grazing.
     */
    public static double maxEccentricityForClearance(double sma1, double ecc1,
                                                       double sma2, double safetyMarginAU) {
        double aphelion1 = sma1 * (1.0 + ecc1);
        double maxEcc = 1.0 - (aphelion1 + safetyMarginAU) / sma2;
        return Math.max(0.001, maxEcc);
    }

    // =========================================================================
    // Instability timescale estimation
    // =========================================================================

    /**
     * Estimate time to first close encounter / instability using
     * Chambers, Wetherill & Boss (1996) empirical relation:
     *
     *   log10(T_instability / T_inner) ≈ b * Δ + c
     *
     * where Δ is separation in mutual Hill radii, b ≈ 0.76, c ≈ -1.0
     * (calibrated for equal-mass planets; we apply a mass-ratio correction).
     *
     * @return estimated instability timescale in millions of years, or
     *         Double.POSITIVE_INFINITY if Δ is large enough to be indefinitely stable
     */
    public static double estimateInstabilityTimescaleMy(double sma1AU, double mass1Earth,
                                                          double sma2AU, double mass2Earth,
                                                          double starMassSolar) {
        double delta = gladmanDelta(sma1AU, mass1Earth, sma2AU, mass2Earth, starMassSolar);

        // Well above Gladman: effectively infinite stability
        if (delta > 10.0) {
            return Double.POSITIVE_INFINITY;
        }

        // Orbit crossing: instability is rapid (< ~1000 orbits)
        // We still estimate a timescale based on orbital period
        if (delta < GLADMAN_FACTOR) {
            double periodYears = Math.sqrt(Math.pow(sma1AU, 3) / starMassSolar);
            double periodMy = periodYears / 1e6;
            // Crossing orbits destabilize in ~100-10000 orbits
            double nOrbits = Math.max(100, Math.pow(10, 0.5 * delta));
            return periodMy * nOrbits;
        }

        // Chambers relation: log10(T/P_inner) ≈ 0.76 * Δ - 1.0
        double periodYears = Math.sqrt(Math.pow(sma1AU, 3) / starMassSolar);
        double periodMy = periodYears / 1e6;

        double b = 0.76;
        double c = -1.0;

        // Mass-ratio correction: unequal masses are somewhat more stable
        double massRatio = Math.min(mass1Earth, mass2Earth) / Math.max(mass1Earth, mass2Earth);
        double massCorrection = 1.0 + 0.3 * (1.0 - massRatio); // up to +30% for very unequal

        double logToverP = b * delta + c;
        logToverP *= massCorrection;

        double tOverP = Math.pow(10, logToverP);
        double timescaleMy = periodMy * tOverP;

        return timescaleMy;
    }

    // =========================================================================
    // Stability classification
    // =========================================================================

    /**
     * Classify the orbital stability between two adjacent planets.
     */
    public static StabilityResult analyzeStability(double sma1, double ecc1, double mass1Earth,
                                                     double sma2, double ecc2, double mass2Earth,
                                                     double starMassSolar, double systemAgeMy) {
        double delta = gladmanDelta(sma1, mass1Earth, sma2, mass2Earth, starMassSolar);
        boolean crossing = orbitsAreCrossing(sma1, ecc1, sma2, ecc2);
        double clearance = orbitClearanceAU(sma1, ecc1, sma2, ecc2);
        double timescaleMy = estimateInstabilityTimescaleMy(sma1, mass1Earth, sma2, mass2Earth, starMassSolar);

        String classification;
        if (crossing) {
            if (timescaleMy < systemAgeMy) {
                classification = "UNSTABLE"; // Should have destabilized already
            } else {
                classification = "CROSSING"; // Orbits cross but timescale > age (may survive)
            }
        } else if (delta < GLADMAN_FACTOR) {
            if (timescaleMy < systemAgeMy) {
                classification = "UNSTABLE";
            } else {
                classification = "MARGINAL";
            }
        } else if (delta < GLADMAN_FACTOR * 1.5) {
            classification = "MARGINAL";
        } else {
            classification = "STABLE";
        }

        return new StabilityResult(classification, delta, crossing, clearance, timescaleMy);
    }

    // =========================================================================
    // Result record
    // =========================================================================

    public static class StabilityResult {
        public final String classification; // STABLE, MARGINAL, CROSSING, UNSTABLE
        public final double gladmanDelta;
        public final boolean orbitsCrossing;
        public final double clearanceAU;     // positive = gap, negative = overlap
        public final double timescaleMy;     // estimated Myr until instability

        public StabilityResult(String classification, double gladmanDelta,
                               boolean orbitsCrossing, double clearanceAU, double timescaleMy) {
            this.classification = classification;
            this.gladmanDelta = gladmanDelta;
            this.orbitsCrossing = orbitsCrossing;
            this.clearanceAU = clearanceAU;
            this.timescaleMy = timescaleMy;
        }
    }
}
