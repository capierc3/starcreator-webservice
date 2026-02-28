package com.brickroad.starcreator_webservice.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BinaryStabilityLimits} — Holman & Wiegert (1999) stability
 * limits with full eccentricity-dependent polynomial fits.
 * <p>
 * Pure unit tests — no Spring context, no database.
 */
class BinaryStabilityLimitsTest {

    private static final double SAFETY = 0.90;

    // ══════════════════════════════════════════════════════════════════
    // S-type backward compatibility (e = 0)
    // ══════════════════════════════════════════════════════════════════

    @Test
    void sType_circularOrbit_matchesOldFormula() {
        // Old formula: (0.464 - 0.380 * mu) * a_bin * 0.90
        double sep = 10.0, host = 1.0, comp = 1.0;
        double mu = comp / (host + comp); // 0.5
        double expected = (0.464 - 0.380 * mu) * sep * SAFETY;

        assertEquals(expected,
                BinaryStabilityLimits.sTypeCriticalSMA(sep, host, comp),
                1e-10, "3-param overload should match old circular formula");
        assertEquals(expected,
                BinaryStabilityLimits.sTypeCriticalSMA(sep, host, comp, 0.0),
                1e-10, "4-param with e=0 should match old formula");
    }

    @Test
    void sType_unequalMass_circularOrbit() {
        // mu = 0.1 / 1.1 ≈ 0.0909
        double sep = 50.0, host = 1.0, comp = 0.1;
        double mu = comp / (host + comp);
        double expected = (0.464 - 0.380 * mu) * sep * SAFETY;

        assertEquals(expected,
                BinaryStabilityLimits.sTypeCriticalSMA(sep, host, comp, 0.0),
                1e-10);
    }

    // ══════════════════════════════════════════════════════════════════
    // S-type with eccentricity
    // ══════════════════════════════════════════════════════════════════

    @Test
    void sType_eccentricityReducesStableZone() {
        double sep = 100.0, host = 1.0, comp = 1.0;
        double circular = BinaryStabilityLimits.sTypeCriticalSMA(sep, host, comp, 0.0);
        double moderate = BinaryStabilityLimits.sTypeCriticalSMA(sep, host, comp, 0.3);
        double high = BinaryStabilityLimits.sTypeCriticalSMA(sep, host, comp, 0.6);

        assertTrue(circular > moderate,
                "Moderate eccentricity should shrink S-type stable zone");
        assertTrue(moderate > high,
                "Higher eccentricity should shrink S-type stable zone further");
    }

    @Test
    void sType_knownValue_equalMass_e05() {
        // a_bin=100, mu=0.5, e=0.5
        // a_crit = (0.464 - 0.380*0.5 - 0.631*0.5 + 0.586*0.5*0.5
        //         + 0.150*0.25 - 0.198*0.5*0.25) * 100
        //        = (0.464 - 0.190 - 0.3155 + 0.1465 + 0.0375 - 0.02475) * 100
        //        = 0.11775 * 100 = 11.775
        // with safety: 11.775 * 0.90 = 10.5975
        double result = BinaryStabilityLimits.sTypeCriticalSMA(100.0, 1.0, 1.0, 0.5);
        assertEquals(10.5975, result, 0.01, "Hand-computed S-type reference value");
    }

    @Test
    void sType_knownValue_mu03_e05() {
        // a_bin=100, host=0.7, comp=0.3 → mu=0.3
        // a_crit = (0.464 - 0.380*0.3 - 0.631*0.5 + 0.586*0.3*0.5
        //         + 0.150*0.25 - 0.198*0.3*0.25) * 100
        //        = (0.464 - 0.114 - 0.3155 + 0.0879 + 0.0375 - 0.01485) * 100
        //        = 0.14505 * 100 = 14.505
        // with safety: 14.505 * 0.90 = 13.0545
        double result = BinaryStabilityLimits.sTypeCriticalSMA(100.0, 0.7, 0.3, 0.5);
        assertEquals(13.0545, result, 0.01, "Hand-computed S-type reference: mu=0.3, e=0.5");
    }

    @Test
    void sType_neverReturnsNegative() {
        // Extreme eccentricity should not produce negative critical SMA
        double result = BinaryStabilityLimits.sTypeCriticalSMA(10.0, 1.0, 1.0, 0.99);
        assertTrue(result >= 0.0, "S-type critical SMA should never be negative");
    }

    @Test
    void sType_fallback_zeroMass() {
        double result = BinaryStabilityLimits.sTypeCriticalSMA(10.0, 0.0, 0.0, 0.3);
        assertEquals(3.0, result, 1e-10, "Fallback: 0.3 * a_bin when total mass is 0");
    }

    // ══════════════════════════════════════════════════════════════════
    // P-type backward compatibility (e = 0)
    // ══════════════════════════════════════════════════════════════════

    @Test
    void pType_circularOrbit_3paramMatchesFull() {
        // Full formula at e=0: (1.60 + 4.12*mu - 5.09*mu²) * a_bin / 0.90
        // Note: the full H&W polynomial includes a mu² term that the old
        // simplified code omitted. The full formula is more accurate.
        double sep = 5.0, m1 = 1.0, m2 = 0.5;

        assertEquals(
                BinaryStabilityLimits.pTypeCriticalSMA(sep, m1, m2, 0.0),
                BinaryStabilityLimits.pTypeCriticalSMA(sep, m1, m2),
                1e-10, "3-param overload should match 4-param with e=0");
    }

    @Test
    void pType_circularOrbit_knownValue() {
        // a_bin=5, mu=1/3, e=0
        // a_crit = (1.60 + 4.12*(1/3) - 5.09*(1/9)) * 5
        //        = (1.60 + 1.37333 - 0.56556) * 5
        //        = 2.40778 * 5 = 12.0389
        // with safety: 12.0389 / 0.90 = 13.3766
        double result = BinaryStabilityLimits.pTypeCriticalSMA(5.0, 1.0, 0.5, 0.0);
        assertEquals(13.377, result, 0.01, "Hand-computed P-type circular reference");
    }

    // ══════════════════════════════════════════════════════════════════
    // P-type with eccentricity
    // ══════════════════════════════════════════════════════════════════

    @Test
    void pType_eccentricityExpandsCavity() {
        double sep = 5.0, m1 = 1.0, m2 = 1.0;
        double circular = BinaryStabilityLimits.pTypeCriticalSMA(sep, m1, m2, 0.0);
        double moderate = BinaryStabilityLimits.pTypeCriticalSMA(sep, m1, m2, 0.3);
        double high = BinaryStabilityLimits.pTypeCriticalSMA(sep, m1, m2, 0.6);

        assertTrue(moderate > circular,
                "Moderate eccentricity should expand P-type inner cavity");
        assertTrue(high > moderate,
                "Higher eccentricity should expand P-type inner cavity further");
    }

    @Test
    void pType_knownValue_equalMass_e05() {
        // a_bin=5, mu=0.5, e=0.5
        // a_crit = (1.60 + 5.10*0.5 - 2.22*0.25 + 4.12*0.5
        //         - 4.27*0.5*0.5 - 5.09*0.25 + 4.61*0.25*0.25) * 5
        //        = (1.60 + 2.55 - 0.555 + 2.06 - 1.0675 - 1.2725 + 0.288125) * 5
        //        = 3.603125 * 5 = 18.015625
        // with safety: 18.015625 / 0.90 = 20.01736...
        double result = BinaryStabilityLimits.pTypeCriticalSMA(5.0, 1.0, 1.0, 0.5);
        assertEquals(20.017, result, 0.01, "Hand-computed P-type reference value");
    }

    @Test
    void pType_fallback_zeroMass() {
        double result = BinaryStabilityLimits.pTypeCriticalSMA(10.0, 0.0, 0.0, 0.3);
        assertEquals(30.0, result, 1e-10, "Fallback: 3.0 * a_bin when total mass is 0");
    }

    // ══════════════════════════════════════════════════════════════════
    // Companion perihelion
    // ══════════════════════════════════════════════════════════════════

    @Test
    void companionPerihelion_circular() {
        assertEquals(10.0, BinaryStabilityLimits.companionPerihelionAU(10.0, 0.0), 1e-10,
                "Circular orbit: perihelion = separation");
    }

    @Test
    void companionPerihelion_eccentric() {
        assertEquals(50.0, BinaryStabilityLimits.companionPerihelionAU(100.0, 0.5), 1e-10,
                "e=0.5: perihelion = 100 * 0.5 = 50 AU");
    }

    @Test
    void companionPerihelion_highEcc() {
        assertEquals(10.0, BinaryStabilityLimits.companionPerihelionAU(100.0, 0.9), 1e-10,
                "e=0.9: perihelion = 100 * 0.1 = 10 AU");
    }

    // ══════════════════════════════════════════════════════════════════
    // Eccentricity clamping
    // ══════════════════════════════════════════════════════════════════

    @Test
    void eccentricity_clampedAtBounds() {
        // Negative eccentricity should be treated as 0
        double negE = BinaryStabilityLimits.sTypeCriticalSMA(10.0, 1.0, 1.0, -0.5);
        double zeroE = BinaryStabilityLimits.sTypeCriticalSMA(10.0, 1.0, 1.0, 0.0);
        assertEquals(zeroE, negE, 1e-10, "Negative eccentricity clamped to 0");

        // e > 0.99 should be clamped
        double over = BinaryStabilityLimits.sTypeCriticalSMA(10.0, 1.0, 1.0, 5.0);
        double max = BinaryStabilityLimits.sTypeCriticalSMA(10.0, 1.0, 1.0, 0.99);
        assertEquals(max, over, 1e-10, "Eccentricity > 0.99 clamped to 0.99");
    }

    // ══════════════════════════════════════════════════════════════════
    // Belt formation probability (unchanged, but sanity check)
    // ══════════════════════════════════════════════════════════════════

    @Test
    void beltFormation_outsideDeadZone() {
        assertEquals(1.0, BinaryStabilityLimits.beltFormationProbability(10.0), 1e-10);
        assertEquals(1.0, BinaryStabilityLimits.beltFormationProbability(200.0), 1e-10);
    }

    @Test
    void beltFormation_insideDeadZone() {
        double center = BinaryStabilityLimits.beltFormationProbability(80.0);
        assertTrue(center < 0.5, "Center of dead zone should have low belt probability");
        assertTrue(center >= 0.15, "Belt probability should not drop below 0.15");
    }
}
