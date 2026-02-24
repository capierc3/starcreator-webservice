package com.brickroad.starcreator_webservice.utils;

import com.brickroad.starcreator_webservice.entity.ud.PlanetaryMagneticField.ProtectionLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins every formula in {@link PhysicsFormulas} to known reference values.
 * These tests lock down the single source of truth so that any drift in
 * constants or formulas is caught immediately during refactoring.
 */
class PhysicsFormulasTest {

    // ── Reference constants for computing expected values ──
    private static final double EARTH_MASS_KG = 5.972e24;
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double G = 6.67430e-11; // ConversionFormulas.GRAVITATIONAL_CONSTANT

    // ══════════════════════════════════════════════════════════════════
    // Mass / Radius Conversions
    // ══════════════════════════════════════════════════════════════════

    @Test
    void testEarthMassToKg() {
        assertEquals(EARTH_MASS_KG, PhysicsFormulas.earthMassToKg(1.0), 1e18,
                "1 Earth mass should be 5.972e24 kg");
        assertEquals(0.0, PhysicsFormulas.earthMassToKg(0.0), 1e-10,
                "0 Earth masses should be 0 kg");
        // Jupiter ~317.8 Earth masses
        assertEquals(317.8 * EARTH_MASS_KG, PhysicsFormulas.earthMassToKg(317.8), 1e21);
    }

    @Test
    void testEarthRadiusToKm() {
        assertEquals(EARTH_RADIUS_KM, PhysicsFormulas.earthRadiusToKm(1.0), 1e-10,
                "1 Earth radius should be 6371.0 km");
        // Jupiter ~11.2 Earth radii
        assertEquals(11.2 * EARTH_RADIUS_KM, PhysicsFormulas.earthRadiusToKm(11.2), 1e-6);
    }

    @Test
    void testMassKgToEarthMasses() {
        assertEquals(1.0, PhysicsFormulas.massKgToEarthMasses(EARTH_MASS_KG), 1e-10,
                "Earth mass in kg should convert back to 1.0 Earth masses");
        assertEquals(0.0, PhysicsFormulas.massKgToEarthMasses(0.0), 1e-10);
    }

    // ══════════════════════════════════════════════════════════════════
    // Bulk Physical Properties
    // ══════════════════════════════════════════════════════════════════

    @Test
    void testDensity_earth() {
        double density = PhysicsFormulas.density(EARTH_MASS_KG, EARTH_RADIUS_KM);
        // Earth's actual density: ~5.51 g/cm³
        assertEquals(5.51, density, 0.02,
                "Earth density should be approximately 5.51 g/cm³");
    }

    @Test
    void testSurfaceGravityG_earth() {
        double gravity = PhysicsFormulas.surfaceGravityG(EARTH_MASS_KG, EARTH_RADIUS_KM);
        // Should be very close to 1.0g (may not be exact due to equatorial vs mean radius)
        assertEquals(1.0, gravity, 0.01,
                "Earth surface gravity should be approximately 1.0g");
    }

    @Test
    void testEscapeVelocityKmS_earth() {
        double escapeVel = PhysicsFormulas.escapeVelocityKmS(EARTH_MASS_KG, EARTH_RADIUS_KM);
        // Earth's escape velocity: ~11.186 km/s
        assertEquals(11.186, escapeVel, 0.05,
                "Earth escape velocity should be approximately 11.186 km/s");
    }

    @Test
    void testDensity_surfaceGravity_escapeVelocity_consistency() {
        // Mars: 0.107 Earth masses, 0.532 Earth radii
        double marsMassKg = PhysicsFormulas.earthMassToKg(0.107);
        double marsRadiusKm = PhysicsFormulas.earthRadiusToKm(0.532);

        double density = PhysicsFormulas.density(marsMassKg, marsRadiusKm);
        double gravity = PhysicsFormulas.surfaceGravityG(marsMassKg, marsRadiusKm);
        double escapeVel = PhysicsFormulas.escapeVelocityKmS(marsMassKg, marsRadiusKm);

        // Mars density ~3.93 g/cm³, gravity ~0.38g, escape velocity ~5.03 km/s
        assertEquals(3.93, density, 0.15, "Mars density");
        assertEquals(0.378, gravity, 0.02, "Mars surface gravity");
        assertEquals(5.03, escapeVel, 0.15, "Mars escape velocity");
    }

    // ══════════════════════════════════════════════════════════════════
    // Orbital Period
    // ══════════════════════════════════════════════════════════════════

    @Test
    void testOrbitalPeriodDaysAU_earth() {
        double period = PhysicsFormulas.orbitalPeriodDaysAU(1.0, 1.0);
        assertEquals(365.25, period, 1e-10,
                "Earth orbital period should be exactly 365.25 days (by formula)");
    }

    @Test
    void testOrbitalPeriodDaysAU_mars() {
        double period = PhysicsFormulas.orbitalPeriodDaysAU(1.524, 1.0);
        // Mars period: ~687 days
        assertEquals(687.0, period, 1.0,
                "Mars orbital period should be approximately 687 days");
    }

    @Test
    void testOrbitalPeriodDaysKM_moon() {
        // Earth-Moon system: SMA = 384,400 km, central mass = Earth
        double period = PhysicsFormulas.orbitalPeriodDaysKM(384400.0, EARTH_MASS_KG);
        // Moon's actual orbital period: ~27.3 days
        assertEquals(27.3, period, 0.2,
                "Moon orbital period should be approximately 27.3 days");
    }

    // ══════════════════════════════════════════════════════════════════
    // Stellar Derived Properties
    // ══════════════════════════════════════════════════════════════════

    @Test
    void testHabitableZoneInnerAU() {
        double inner = PhysicsFormulas.habitableZoneInnerAU(1.0);
        assertEquals(Math.sqrt(1.0 / 1.1), inner, 1e-10,
                "HZ inner for solar luminosity should be sqrt(1/1.1)");
    }

    @Test
    void testHabitableZoneOuterAU() {
        double outer = PhysicsFormulas.habitableZoneOuterAU(1.0);
        assertEquals(Math.sqrt(1.0 / 0.53), outer, 1e-10,
                "HZ outer for solar luminosity should be sqrt(1/0.53)");
    }

    @Test
    void testHabitableZone_higherLuminosity() {
        // A 4x solar luminosity star should have HZ at 2x the distance (sqrt scaling)
        double inner1 = PhysicsFormulas.habitableZoneInnerAU(1.0);
        double inner4 = PhysicsFormulas.habitableZoneInnerAU(4.0);
        assertEquals(inner1 * 2.0, inner4, 1e-10,
                "4x luminosity HZ should be 2x farther out");
    }

    @Test
    void testEstimatedRemainingMsMy_sun() {
        // Sun: 1.0 solar mass, 4600 My old
        // MS lifetime = 10000 / 1.0^2.5 = 10000 My
        // Remaining = 10000 - 4600 = 5400 My
        double remaining = PhysicsFormulas.estimatedRemainingMsMy(1.0, 4600.0);
        assertEquals(5400.0, remaining, 1e-10);
    }

    @Test
    void testEstimatedRemainingMsMy_expired() {
        // Star that outlived its MS lifetime — should clamp to 0
        double remaining = PhysicsFormulas.estimatedRemainingMsMy(1.0, 15000.0);
        assertEquals(0.0, remaining, 1e-10,
                "Expired star should return 0 remaining MS lifetime");
    }

    @Test
    void testEstimatedRemainingMsMy_massiveStar() {
        // 2.0 solar masses: MS lifetime = 10000 / 2^2.5 = 10000 / 5.657 ≈ 1768 My
        double remaining = PhysicsFormulas.estimatedRemainingMsMy(2.0, 500.0);
        double expectedLifespan = 10000.0 / Math.pow(2.0, 2.5);
        assertEquals(expectedLifespan - 500.0, remaining, 0.1);
    }

    // ══════════════════════════════════════════════════════════════════
    // Gravitational Boundaries
    // ══════════════════════════════════════════════════════════════════

    @Test
    void testHillSphereRadiusKm() {
        // Earth-Sun: a = 1 AU in km, mass = Earth, parent = Sun
        double earthOrbitKm = 1.496e8; // ~1 AU in km
        double sunMassKg = 1.989e30;
        double hillSphere = PhysicsFormulas.hillSphereRadiusKm(earthOrbitKm, EARTH_MASS_KG, sunMassKg);
        // Earth's Hill sphere: ~1.5 million km
        assertEquals(1.5e6, hillSphere, 0.1e6,
                "Earth's Hill sphere should be approximately 1.5 million km");
    }

    @Test
    void testRocheLimitKm() {
        // Using Earth as primary (ρ=5.51), rocky secondary (ρ=3.3)
        double roche = PhysicsFormulas.rocheLimitKm(EARTH_RADIUS_KM, 5.51, 3.3);
        // Roche = 2.46 * 6371 * cbrt(5.51/3.3) ≈ 2.46 * 6371 * 1.186 ≈ 18579 km
        double expected = 2.46 * EARTH_RADIUS_KM * Math.cbrt(5.51 / 3.3);
        assertEquals(expected, roche, 1e-6,
                "Roche limit formula should match exact calculation");
    }

    // ══════════════════════════════════════════════════════════════════
    // Atmosphere — Scale Height
    // ══════════════════════════════════════════════════════════════════

    @Test
    void testScaleHeightKm_earth() {
        // Earth: T=288K, g=9.80665 m/s², mean mol weight ≈29 Da (N2/O2 mix)
        double scaleHeight = PhysicsFormulas.scaleHeightKm(288.0, 9.80665, 29.0);
        // Earth's scale height: ~8.5 km
        assertEquals(8.5, scaleHeight, 0.2,
                "Earth atmospheric scale height should be approximately 8.5 km");
    }

    @Test
    void testScaleHeightKm_zeroGravity() {
        assertEquals(0.0, PhysicsFormulas.scaleHeightKm(300.0, 0.0, 29.0), 1e-10,
                "Zero gravity should return 0 scale height");
    }

    @Test
    void testScaleHeightKm_zeroMolWeight() {
        assertEquals(0.0, PhysicsFormulas.scaleHeightKm(300.0, 9.8, 0.0), 1e-10,
                "Zero molecular weight should return 0 scale height");
    }

    @Test
    void testScaleHeightKm_negativeGravity() {
        assertEquals(0.0, PhysicsFormulas.scaleHeightKm(300.0, -5.0, 29.0), 1e-10,
                "Negative gravity should return 0 scale height");
    }

    // ══════════════════════════════════════════════════════════════════
    // Magnetic Field — Surface Field
    // ══════════════════════════════════════════════════════════════════

    @Test
    void testSurfaceFieldMicrotesla_earth() {
        double avg = PhysicsFormulas.surfaceFieldAvgMicrotesla(1.0);
        assertEquals(50.0, avg, 1e-10, "Earth-strength field avg should be 50 µT");

        double min = PhysicsFormulas.surfaceFieldMinMicrotesla(avg);
        assertEquals(30.0, min, 1e-10, "Min should be 60% of avg");

        double max = PhysicsFormulas.surfaceFieldMaxMicrotesla(avg);
        assertEquals(70.0, max, 1e-10, "Max should be 140% of avg");
    }

    @Test
    void testMagneticMoment_earth() {
        // For strength=1.0, radius=Earth: should return EARTH_MAGNETIC_MOMENT
        double moment = PhysicsFormulas.magneticMoment(1.0, EARTH_RADIUS_KM);
        assertEquals(7.91e22, moment, 1e18,
                "Earth-equivalent magnetic moment should be ~7.91e22 A·m²");
    }

    @Test
    void testMagneticMoment_scaling() {
        // Moment scales with strength and radius³
        double moment1 = PhysicsFormulas.magneticMoment(1.0, EARTH_RADIUS_KM);
        double moment2 = PhysicsFormulas.magneticMoment(2.0, EARTH_RADIUS_KM);
        assertEquals(moment1 * 2.0, moment2, 1e15,
                "Double strength should double the moment");

        // 2x radius → 8x moment (radius cubed)
        double momentBigPlanet = PhysicsFormulas.magneticMoment(1.0, EARTH_RADIUS_KM * 2.0);
        assertEquals(moment1 * 8.0, momentBigPlanet, 1e18,
                "Double radius should 8x the moment (r³ scaling)");
    }

    // ══════════════════════════════════════════════════════════════════
    // Magnetic Field — Magnetosphere
    // ══════════════════════════════════════════════════════════════════

    @Test
    void testMagnetopauseStandoffRadii_positiveInputs() {
        double moment = 7.91e22; // Earth-like
        double ramPressure = 2e-9; // ~typical solar wind at 1 AU in Pascals
        double planetRadiusM = EARTH_RADIUS_KM * 1000.0;

        double standoff = PhysicsFormulas.magnetopauseStandoffRadii(moment, ramPressure, planetRadiusM);
        assertTrue(standoff >= 1.5, "Standoff should be at least 1.5 planet radii");
        assertTrue(standoff <= 100.0, "Standoff should be at most 100 planet radii");
    }

    @Test
    void testMagnetopauseStandoffRadii_zeroPressure() {
        double standoff = PhysicsFormulas.magnetopauseStandoffRadii(7.91e22, 0.0, 6.371e6);
        assertEquals(10.0, standoff, 1e-10,
                "Zero ram pressure should return 10.0 default");
    }

    @Test
    void testBowShockMultiplier() {
        assertEquals(1.45, PhysicsFormulas.bowShockMultiplier(300), 1e-10,
                "Low wind speed → 1.45 multiplier");
        assertEquals(1.35, PhysicsFormulas.bowShockMultiplier(500), 1e-10,
                "Medium wind speed → 1.35 multiplier");
        assertEquals(1.3, PhysicsFormulas.bowShockMultiplier(700), 1e-10,
                "High wind speed → 1.3 multiplier");
        // Boundary: exactly 400 → should be ≤400 bracket (1.45)
        assertEquals(1.45, PhysicsFormulas.bowShockMultiplier(400), 1e-10,
                "Exactly 400 km/s → 1.45 (not > 400)");
        // Boundary: exactly 600 → should be >400 bracket (1.35)
        assertEquals(1.35, PhysicsFormulas.bowShockMultiplier(600), 1e-10,
                "Exactly 600 km/s → 1.35 (not > 600)");
    }

    @Test
    void testMagnetotailMultiplier() {
        // At 400 km/s: 15 + 10*(400/400) = 25
        assertEquals(25.0, PhysicsFormulas.magnetotailMultiplier(400), 1e-10,
                "400 km/s wind → 25x multiplier");
        // At 2000 km/s: 15 + 10*(2000/400) = 65, capped at 60
        assertEquals(60.0, PhysicsFormulas.magnetotailMultiplier(2000), 1e-10,
                "2000 km/s wind → 60x (capped)");
        // At 0 km/s: 15 + 10*(0/400) = 15
        assertEquals(15.0, PhysicsFormulas.magnetotailMultiplier(0), 1e-10,
                "0 km/s wind → 15x base multiplier");
    }

    @Test
    void testSurfacePowerFluxWm2_positive() {
        double flux = PhysicsFormulas.surfacePowerFluxWm2(7.91e22, 6.371e6);
        assertTrue(flux > 0, "Surface power flux should be positive for real inputs");
    }

    // ══════════════════════════════════════════════════════════════════
    // Magnetic Field — Protection Level
    // ══════════════════════════════════════════════════════════════════

    @Test
    void testProtectionLevel_none() {
        var result = PhysicsFormulas.calculateProtectionLevel(0.01, 1.0);
        assertEquals(ProtectionLevel.NONE, result.level());
        assertFalse(result.shieldsFromStellarWind());
        assertFalse(result.shieldsFromCosmicRays());
    }

    @Test
    void testProtectionLevel_strong_earthlike() {
        // Earth-like: strength=1.0, threat=1.0
        // effectiveThreat = cbrt(1.0) = 1.0
        // ratio = 1.0 / 1.0 = 1.0 → STRONG (0.8 ≤ 1.0 < 2.5)
        var result = PhysicsFormulas.calculateProtectionLevel(1.0, 1.0);
        assertEquals(ProtectionLevel.STRONG, result.level());
        assertTrue(result.shieldsFromStellarWind());
        assertTrue(result.shieldsFromCosmicRays());
    }

    @Test
    void testProtectionLevel_exceptional() {
        // Very strong field, low threat → EXCEPTIONAL
        var result = PhysicsFormulas.calculateProtectionLevel(5.0, 1.0);
        assertEquals(ProtectionLevel.EXCEPTIONAL, result.level());
        assertTrue(result.shieldsFromStellarWind());
        assertTrue(result.shieldsFromCosmicRays());
    }

    @Test
    void testProtectionLevel_weakFieldCap() {
        // Weak field (0.05) with very low threat (0.01)
        // Without cap: ratio = 0.05 / cbrt(0.01) = 0.05 / 0.2154 = 0.232 → MINIMAL
        // With cap: min(ratio, 0.75) when field < 0.1 → capped at 0.232 (already below 0.75)
        // Actually 0.232 < 0.3 → MINIMAL
        var result = PhysicsFormulas.calculateProtectionLevel(0.05, 0.01);
        assertEquals(ProtectionLevel.MINIMAL, result.level());

        // Very weak field, very low threat — ensure cap prevents STRONG/EXCEPTIONAL
        // field=0.09 (below 0.1 cap), threat=0.00001 → ratio uncapped would be huge
        // cbrt(0.00001) = 0.02154
        // ratio = 0.09 / 0.02154 = 4.178 → would be EXCEPTIONAL
        // but cap at 0.75 → MODERATE (0.3 ≤ 0.75 < 0.8)
        var cappedResult = PhysicsFormulas.calculateProtectionLevel(0.09, 0.00001);
        assertEquals(ProtectionLevel.MODERATE, cappedResult.level(),
                "Weak field cap should prevent EXCEPTIONAL classification");
    }

    @Test
    void testProtectionLevel_highThreat_dampening() {
        // Strong field in very hostile environment
        // strength=1.0, threat=100.0
        // effectiveThreat = cbrt(100) * (1 + 0.3*log10(100)) = 4.642 * 1.6 = 7.427
        // ratio = 1.0 / 7.427 = 0.1347 → MINIMAL
        var result = PhysicsFormulas.calculateProtectionLevel(1.0, 100.0);
        // Even Earth-strength field is overwhelmed by extreme threat
        assertTrue(result.level() == ProtectionLevel.MINIMAL
                        || result.level() == ProtectionLevel.NONE,
                "Earth-strength field should be overwhelmed by 100x threat");
    }
}
