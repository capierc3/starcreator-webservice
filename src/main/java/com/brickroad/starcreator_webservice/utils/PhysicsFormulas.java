package com.brickroad.starcreator_webservice.utils;

import com.brickroad.starcreator_webservice.entity.ud.PlanetaryMagneticField;
import com.brickroad.starcreator_webservice.utils.planets.StellarEnvironment;

/**
 * Single source of truth for deterministic physics formulas.
 * <p>
 * Both creator classes (during initial generation) and {@link
 * com.brickroad.starcreator_webservice.service.DerivedFieldCalculator}
 * (during DB reload) call these methods. This avoids formula duplication
 * and guarantees identical results on both paths.
 * <p>
 * All methods are pure functions — no randomness, no side effects.
 */
public final class PhysicsFormulas {

    private PhysicsFormulas() {} // Prevent instantiation

    // ══════════════════════════════════════════════════════════════════
    //  Consolidated Constants
    // ══════════════════════════════════════════════════════════════════

    /** Earth mass in kilograms. */
    public static final double EARTH_MASS_KG = 5.972e24;

    /** Earth equatorial radius in kilometers. */
    public static final double EARTH_RADIUS_KM = 6371.0;

    /** ISI standard surface gravity of Earth in m/s². */
    public static final double EARTH_GRAVITY_MS2 = 9.80665;

    /** Earth's average surface magnetic field in microteslas. */
    public static final double EARTH_SURFACE_FIELD_uT = 50.0;

    /** Earth's magnetic dipole moment in A·m². */
    public static final double EARTH_MAGNETIC_MOMENT = 7.91e22;

    // ══════════════════════════════════════════════════════════════════
    //  Body Mass / Radius Conversions
    // ══════════════════════════════════════════════════════════════════

    /** Convert Earth-mass units to kilograms. */
    public static double earthMassToKg(double earthMass) {
        return earthMass * EARTH_MASS_KG;
    }

    /** Convert Earth-radius units to kilometers. */
    public static double earthRadiusToKm(double earthRadius) {
        return earthRadius * EARTH_RADIUS_KM;
    }

    // ══════════════════════════════════════════════════════════════════
    //  Bulk Physical Properties
    // ══════════════════════════════════════════════════════════════════

    /**
     * Bulk density from mass and radius.
     *
     * @param massKg   body mass in kilograms
     * @param radiusKm body radius in kilometers
     * @return density in g/cm³
     */
    public static double density(double massKg, double radiusKm) {
        double radiusM = radiusKm * 1000.0;
        double volumeM3 = (4.0 / 3.0) * Math.PI * Math.pow(radiusM, 3);
        double densityKgM3 = massKg / volumeM3;
        return densityKgM3 / 1000.0;  // kg/m³ → g/cm³
    }

    /**
     * Minimum rotation period before rotational breakup (centrifugal force
     * exceeds self-gravity at the equator).
     *
     * Derived from balancing centripetal acceleration with gravitational:
     *   T_breakup = sqrt(3π / (G × ρ))
     *
     * Reference values:
     *   Jupiter  (ρ 1.33 g/cm³) → 2.85 hrs  (actual 9.9 hrs, ~3.5× margin)
     *   Saturn   (ρ 0.69 g/cm³) → 3.97 hrs  (actual 10.7 hrs, ~2.7× margin)
     *   Neptune  (ρ 1.64 g/cm³) → 2.57 hrs  (actual 16.1 hrs, ~6.3× margin)
     *   Earth    (ρ 5.51 g/cm³) → 1.40 hrs  (actual 24 hrs, ~17× margin)
     *
     * @param densityGCm3 mean density in g/cm³
     * @return breakup period in hours
     */
    public static double rotationalBreakupPeriodHours(double densityGCm3) {
        double densityKgM3 = densityGCm3 * 1000.0;
        double periodSeconds = Math.sqrt(3.0 * Math.PI / (ConversionFormulas.GRAVITATIONAL_CONSTANT * densityKgM3));
        return periodSeconds / 3600.0;
    }

    /**
     * Surface gravity as a multiple of Earth gravity (g).
     *
     * @param massKg   body mass in kilograms
     * @param radiusKm body radius in kilometers
     * @return gravity in multiples of Earth g (9.80665 m/s²)
     */
    public static double surfaceGravityG(double massKg, double radiusKm) {
        double radiusM = radiusKm * 1000.0;
        double gravityMs2 = (ConversionFormulas.GRAVITATIONAL_CONSTANT * massKg) / (radiusM * radiusM);
        return gravityMs2 / EARTH_GRAVITY_MS2;
    }

    /**
     * Escape velocity in km/s.
     *
     * @param massKg   body mass in kilograms
     * @param radiusKm body radius in kilometers
     * @return escape velocity in km/s
     */
    public static double escapeVelocityKmS(double massKg, double radiusKm) {
        double radiusM = radiusKm * 1000.0;
        double velocityMs = Math.sqrt((2 * ConversionFormulas.GRAVITATIONAL_CONSTANT * massKg) / radiusM);
        return velocityMs / 1000.0;
    }

    // ══════════════════════════════════════════════════════════════════
    //  Orbital Period — Kepler's Third Law
    // ══════════════════════════════════════════════════════════════════

    /**
     * Orbital period for a body orbiting a star (Kepler simplified).
     * T(years) = sqrt(a³ / M★), then converted to days.
     *
     * @param semiMajorAxisAU semi-major axis in AU
     * @param starMassSolar   central star mass in solar masses
     * @return orbital period in Earth days
     */
    public static double orbitalPeriodDaysAU(double semiMajorAxisAU, double starMassSolar) {
        double periodYears = Math.sqrt(Math.pow(semiMajorAxisAU, 3) / starMassSolar);
        return periodYears * 365.25;
    }

    /**
     * Orbital period for a body orbiting a planet (Newton's generalized form).
     * T = 2π·sqrt(a³ / (G·M)), then converted to days.
     *
     * @param semiMajorAxisKm semi-major axis in kilometers
     * @param centralMassKg   central body mass in kilograms
     * @return orbital period in Earth days
     */
    public static double orbitalPeriodDaysKM(double semiMajorAxisKm, double centralMassKg) {
        double semiMajorAxisM = semiMajorAxisKm * 1000.0;
        double periodSeconds = 2 * Math.PI * Math.sqrt(
                Math.pow(semiMajorAxisM, 3) /
                        (ConversionFormulas.GRAVITATIONAL_CONSTANT * centralMassKg));
        return periodSeconds / (24.0 * 3600.0);
    }

    // ══════════════════════════════════════════════════════════════════
    //  Stellar Derived Properties
    // ══════════════════════════════════════════════════════════════════

    /**
     * Inner edge of the habitable zone.
     *
     * @param effectiveLuminosity effective luminosity from {@link StellarEnvironment#effectiveLuminosity}
     * @return inner HZ boundary in AU
     */
    public static double habitableZoneInnerAU(double effectiveLuminosity) {
        return Math.sqrt(effectiveLuminosity / 1.1);
    }

    /**
     * Outer edge of the habitable zone.
     *
     * @param effectiveLuminosity effective luminosity from {@link StellarEnvironment#effectiveLuminosity}
     * @return outer HZ boundary in AU
     */
    public static double habitableZoneOuterAU(double effectiveLuminosity) {
        return Math.sqrt(effectiveLuminosity / 0.53);
    }

    /**
     * Estimated remaining main-sequence lifetime.
     *
     * @param solarMass star mass in solar masses
     * @param ageMY     current age in millions of years
     * @return remaining MS lifetime in My (≥ 0)
     */
    public static double estimatedRemainingMsMy(double solarMass, double ageMY) {
        double msLifespan = 10000.0 / Math.pow(solarMass, 2.5);
        return Math.max(0, msLifespan - ageMY);
    }

    // ══════════════════════════════════════════════════════════════════
    //  Gravitational Boundaries
    // ══════════════════════════════════════════════════════════════════

    /**
     * Hill sphere radius — the region where a body's gravity dominates over
     * its parent's gravitational pull.
     *
     * @param orbitSmaKm  semi-major axis of the body's orbit in km
     * @param bodyMassKg  mass of the body in kg
     * @param parentMassKg mass of the gravitational parent in kg
     * @return Hill sphere radius in km
     */
    public static double hillSphereRadiusKm(double orbitSmaKm, double bodyMassKg, double parentMassKg) {
        return orbitSmaKm * Math.cbrt(bodyMassKg / (3.0 * parentMassKg));
    }

    /**
     * Roche limit — the distance within which a body held together only by
     * gravity will be torn apart by tidal forces.
     *
     * @param primaryRadiusKm  radius of the primary body in km
     * @param primaryDensity   density of the primary body in g/cm³
     * @param secondaryDensity density of the secondary body in g/cm³
     * @return Roche limit in km
     */
    public static double rocheLimitKm(double primaryRadiusKm, double primaryDensity, double secondaryDensity) {
        return 2.46 * primaryRadiusKm * Math.cbrt(primaryDensity / secondaryDensity);
    }

    // ══════════════════════════════════════════════════════════════════
    //  Atmosphere
    // ══════════════════════════════════════════════════════════════════

    /**
     * Atmospheric scale height: H = kT / (μ·m_u·g).
     *
     * @param surfaceTempK           surface temperature in Kelvin
     * @param surfaceGravityMs2      surface gravity in m/s²
     * @param meanMolWeightDaltons   mean molecular weight in Daltons (AMU)
     * @return scale height in km
     */
    public static double scaleHeightKm(double surfaceTempK, double surfaceGravityMs2,
                                        double meanMolWeightDaltons) {
        if (surfaceGravityMs2 <= 0 || meanMolWeightDaltons <= 0) return 0;
        double scaleHeightM = (CelestialBodyUtils.BOLTZMANN_K * surfaceTempK)
                / (meanMolWeightDaltons * CelestialBodyUtils.AMU_KG * surfaceGravityMs2);
        return scaleHeightM / 1000.0;
    }

    // ══════════════════════════════════════════════════════════════════
    //  Magnetic Field — Surface Field
    // ══════════════════════════════════════════════════════════════════

    /** Average surface magnetic field in microteslas. */
    public static double surfaceFieldAvgMicrotesla(double strengthComparedToEarth) {
        return EARTH_SURFACE_FIELD_uT * strengthComparedToEarth;
    }

    /** Minimum surface field (60% of average). */
    public static double surfaceFieldMinMicrotesla(double avgFieldMicrotesla) {
        return avgFieldMicrotesla * 0.6;
    }

    /** Maximum surface field (140% of average). */
    public static double surfaceFieldMaxMicrotesla(double avgFieldMicrotesla) {
        return avgFieldMicrotesla * 1.4;
    }

    /**
     * Magnetic dipole moment.
     *
     * @param strengthComparedToEarth field strength relative to Earth
     * @param planetRadiusKm          planet radius in km
     * @return magnetic moment in A·m²
     */
    public static double magneticMoment(double strengthComparedToEarth, double planetRadiusKm) {
        double radiusRatio = planetRadiusKm / EARTH_RADIUS_KM;
        return EARTH_MAGNETIC_MOMENT * strengthComparedToEarth * Math.pow(radiusRatio, 3);
    }

    // ══════════════════════════════════════════════════════════════════
    //  Magnetic Field — Magnetosphere (deterministic core)
    // ══════════════════════════════════════════════════════════════════

    /**
     * Chapman-Ferraro magnetopause standoff distance (deterministic physics).
     * R_mp = (μ₀/(4π) · M² / (2 · P_ram))^(1/6), then normalized to planet radii.
     * <p>
     * The creator applies random variance (±15%) on top of this result;
     * the reload path uses this value directly.
     *
     * @param magneticMoment  magnetic dipole moment in A·m²
     * @param ramPressurePa   stellar wind ram pressure in Pascals
     * @param planetRadiusM   planet radius in meters
     * @return magnetopause distance in planet radii, clamped to [1.5, 100]
     */
    public static double magnetopauseStandoffRadii(double magneticMoment, double ramPressurePa,
                                                    double planetRadiusM) {
        if (ramPressurePa <= 0 || planetRadiusM <= 0) return 10.0;
        double mu0_over_4pi = 1e-7;  // T·m/A
        double standoffM = Math.pow(
                mu0_over_4pi * magneticMoment * magneticMoment / (2.0 * ramPressurePa),
                1.0 / 6.0);
        double radii = standoffM / planetRadiusM;
        return Math.max(1.5, Math.min(100.0, radii));
    }

    /**
     * Bow shock distance multiplier based on stellar wind Mach number.
     *
     * @param windSpeedKmS stellar wind velocity in km/s
     * @return multiplier to apply to magnetopause distance
     */
    public static double bowShockMultiplier(double windSpeedKmS) {
        if (windSpeedKmS > 600) return 1.3;
        if (windSpeedKmS > 400) return 1.35;
        return 1.45;
    }

    /**
     * Magnetotail length multiplier.
     *
     * @param windSpeedKmS stellar wind velocity in km/s
     * @return multiplier to apply to magnetopause distance
     */
    public static double magnetotailMultiplier(double windSpeedKmS) {
        double multiplier = 15.0 + 10.0 * (windSpeedKmS / 400.0);
        return Math.min(60.0, multiplier);
    }

    /**
     * Surface magnetic power flux (deterministic approximation).
     *
     * @param magneticMoment magnetic dipole moment in A·m²
     * @param planetRadiusM  planet radius in meters
     * @return power flux in W/m²
     */
    public static double surfacePowerFluxWm2(double magneticMoment, double planetRadiusM) {
        double surfaceAreaM2 = 4 * Math.PI * planetRadiusM * planetRadiusM;
        double totalPower = magneticMoment * magneticMoment * 1e-7 / Math.pow(planetRadiusM, 3);
        return totalPower / surfaceAreaM2;
    }

    // ══════════════════════════════════════════════════════════════════
    //  Magnetic Field — Protection Level
    // ══════════════════════════════════════════════════════════════════

    /**
     * Result of the protection-level calculation.
     */
    public record ProtectionResult(
            PlanetaryMagneticField.ProtectionLevel level,
            boolean shieldsFromStellarWind,
            boolean shieldsFromCosmicRays
    ) {}

    /**
     * Determine magnetic protection level based on field strength and stellar
     * environment threat. This is the canonical calculation used by both the
     * {@link com.brickroad.starcreator_webservice.creator.MagneticFieldCreator}
     * during generation and the
     * {@link com.brickroad.starcreator_webservice.service.DerivedFieldCalculator}
     * during reload.
     *
     * @param fieldStrength  magnetic field strength relative to Earth
     * @param threatFactor   stellar atmospheric stripping factor (1.0 = solar at 1 AU)
     * @return protection classification
     */
    public static ProtectionResult calculateProtectionLevel(double fieldStrength, double threatFactor) {
        // Protection ratio: field strength vs. threat
        // Cube-root dampening with log boost for high-threat environments
        double effectiveThreat = Math.cbrt(threatFactor);
        if (threatFactor > 3.0) {
            effectiveThreat *= (1.0 + 0.3 * Math.log10(threatFactor));
        }

        double protectionRatio = fieldStrength / Math.max(0.01, effectiveThreat);

        // Weak fields are capped regardless of low threat
        if (fieldStrength < 0.1) {
            protectionRatio = Math.min(protectionRatio, 0.75);
        }

        PlanetaryMagneticField.ProtectionLevel level;
        boolean shieldsWind;
        boolean shieldsCosmic;

        if (protectionRatio < 0.05) {
            level = PlanetaryMagneticField.ProtectionLevel.NONE;
            shieldsWind = false;
            shieldsCosmic = false;
        } else if (protectionRatio < 0.3) {
            level = PlanetaryMagneticField.ProtectionLevel.MINIMAL;
            shieldsWind = false;
            shieldsCosmic = false;
        } else if (protectionRatio < 0.8) {
            level = PlanetaryMagneticField.ProtectionLevel.MODERATE;
            shieldsWind = true;
            shieldsCosmic = false;
        } else if (protectionRatio < 2.5) {
            level = PlanetaryMagneticField.ProtectionLevel.STRONG;
            shieldsWind = true;
            shieldsCosmic = true;
        } else {
            level = PlanetaryMagneticField.ProtectionLevel.EXCEPTIONAL;
            shieldsWind = true;
            shieldsCosmic = true;
        }

        return new ProtectionResult(level, shieldsWind, shieldsCosmic);
    }

    // ══════════════════════════════════════════════════════════════════
    //  OrbitalBand — Unit Conversion
    // ══════════════════════════════════════════════════════════════════

    /**
     * Convert mass in kg to Earth-mass units.
     *
     * @param massKg mass in kilograms
     * @return mass in Earth masses
     */
    public static double massKgToEarthMasses(double massKg) {
        return massKg / EARTH_MASS_KG;
    }
}
