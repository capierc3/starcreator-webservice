package com.brickroad.starcreator_webservice.utils;

/**
 * Binary star orbital stability limits from Holman & Wiegert (1999, AJ 117:621).
 * <p>
 * Provides critical semi-major axis boundaries for stable orbits in binary
 * star systems. Uses the circular-orbit (e_bin = 0) simplification since
 * binary eccentricity is not tracked in the data model.
 * <p>
 * Also incorporates the debris-disk "dead zone" suppression from
 * Yelverton et al. (2019), which found a statistically significant lack
 * of debris disks in medium-separation binaries (25–135 AU).
 * <p>
 * All methods are pure functions — no randomness, no side effects.
 */
public final class BinaryStabilityLimits {

    private BinaryStabilityLimits() {} // Prevent instantiation

    /**
     * Safety margin applied inward for S-type (shrinks allowed zone)
     * and outward for P-type (enlarges minimum distance).
     * 10% buffer accounts for unmodeled eccentricity and resonance effects.
     */
    private static final double SAFETY_MARGIN = 0.90;

    /**
     * Maximum stable semi-major axis for an S-type (circumstellar) orbit.
     * <p>
     * A body orbiting one star in a binary must stay within this distance
     * to remain gravitationally bound. Beyond this limit, the companion
     * star's perturbations will eject the body on ~10^3–10^6 year timescales.
     * <p>
     * For circular orbits: {@code a_crit = (0.464 − 0.380·μ) · a_bin}
     * where μ = m_companion / (m_host + m_companion).
     *
     * @param binarySepAU        binary semi-major axis in AU
     * @param hostMassSolar      mass of the star being orbited (solar masses)
     * @param companionMassSolar mass of the companion star (solar masses)
     * @return maximum stable semi-major axis in AU (with 10% safety margin)
     */
    public static double sTypeCriticalSMA(double binarySepAU,
                                           double hostMassSolar,
                                           double companionMassSolar) {
        double totalMass = hostMassSolar + companionMassSolar;
        if (totalMass <= 0) return binarySepAU * 0.3; // fallback
        double mu = companionMassSolar / totalMass;
        double aCrit = (0.464 - 0.380 * mu) * binarySepAU;
        return aCrit * SAFETY_MARGIN;
    }

    /**
     * Minimum stable semi-major axis for a P-type (circumbinary) orbit.
     * <p>
     * A body orbiting both stars must be at least this far from the
     * barycenter. Closer orbits are destabilised by the binary's
     * gravitational potential, which clears an inner cavity.
     * <p>
     * For circular orbits: {@code a_crit = (1.60 + 4.12·μ) · a_bin}
     * where μ = m_lesser / (m1 + m2).
     *
     * @param binarySepAU binary semi-major axis in AU
     * @param m1Solar     mass of star 1 (solar masses)
     * @param m2Solar     mass of star 2 (solar masses)
     * @return minimum stable semi-major axis in AU (with 10% safety margin outward)
     */
    public static double pTypeCriticalSMA(double binarySepAU,
                                           double m1Solar,
                                           double m2Solar) {
        double totalMass = m1Solar + m2Solar;
        if (totalMass <= 0) return binarySepAU * 3.0; // fallback
        double mu = Math.min(m1Solar, m2Solar) / totalMass;
        double aCrit = (1.60 + 4.12 * mu) * binarySepAU;
        return aCrit / SAFETY_MARGIN; // push outward for safety
    }

    /**
     * Probability multiplier for belt formation based on binary separation.
     * <p>
     * Yelverton et al. (2019) found a "dead zone" at 25–135 AU where
     * the binary separation is comparable to typical debris disk radii,
     * gravitationally clearing both circumstellar and circumbinary material.
     * Detection rates drop to near zero in this range.
     * <ul>
     *   <li>Close binaries (&lt;25 AU): ~8% detection rate (reduced but possible)</li>
     *   <li>Medium binaries (25–135 AU): ~0% detections (the dead zone)</li>
     *   <li>Wide binaries (&gt;135 AU): ~19% detection rate (comparable to single stars)</li>
     * </ul>
     *
     * @param binarySepAU binary semi-major axis in AU
     * @return probability multiplier for belt formation (0.15 to 1.0)
     */
    public static double beltFormationProbability(double binarySepAU) {
        if (binarySepAU < 25.0 || binarySepAU > 135.0) {
            return 1.0; // Outside dead zone
        }
        // Linear ramp: worst at center (~80 AU), better at edges
        double center = 80.0;
        double halfWidth = 55.0;
        double distFromCenter = Math.abs(binarySepAU - center) / halfWidth;
        return Math.max(0.15, distFromCenter); // minimum 15% chance
    }
}
