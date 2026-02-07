package com.brickroad.starcreator_webservice.utils.planets;

import com.brickroad.starcreator_webservice.entity.ud.Star;

public class StellarEnvironment {

    private static final double SOLAR_WIND_DENSITY_AT_1AU = 6.0;       // protons/cm³
    private static final double SOLAR_WIND_VELOCITY_MS = 400_000.0;    // m/s
    private static final double SOLAR_RAM_PRESSURE_PA = 2.0e-9;        // ~2 nPa
    private static final double PROTON_MASS_KG = 1.67e-27;

    public static double windDensityAtDistance(Star star, double distanceAU) {
        double baseDensity = star.getStellarWindDensityAt1AU() != null
                ? star.getStellarWindDensityAt1AU() : SOLAR_WIND_DENSITY_AT_1AU;
        return baseDensity / (distanceAU * distanceAU);
    }

    public static double windRamPressureAtDistance(Star star, double distanceAU) {
        double density = windDensityAtDistance(star, distanceAU); // protons/cm³
        double densitySI = density * 1e6; // convert to protons/m³
        double velocity = star.getStellarWindVelocityKmS() != null
                ? star.getStellarWindVelocityKmS() * 1000.0 : SOLAR_WIND_VELOCITY_MS;
        return 0.5 * densitySI * PROTON_MASS_KG * velocity * velocity;
    }

    public static double windPressureNormalized(Star star, double distanceAU) {
        return windRamPressureAtDistance(star, distanceAU) / SOLAR_RAM_PRESSURE_PA;
    }

    public static double activityMultiplier(Star star) {
        double multiplier = 1.0;

        // Activity level contribution
        if (star.getActivityLevel() != null) {
            multiplier *= switch (star.getActivityLevel()) {
                case "HYPERACTIVE" -> 5.0;
                case "VERY_ACTIVE" -> 3.0;
                case "ACTIVE" -> 1.5;
                case "MODERATE" -> 1.0;
                case "LOW" -> 0.6;
                case "INACTIVE" -> 0.3;
                default -> 1.0;
            };
        }

        // Grand minimum suppresses activity
        if (Boolean.TRUE.equals(star.getInGrandMinimum())) {
            double depth = star.getGrandMinimumDepth() != null ? star.getGrandMinimumDepth() : 0.8;
            multiplier *= (1.0 - depth * 0.7); // Up to 70% reduction
        }

        return multiplier;
    }

    public static double xrayRadiationFactor(Star star) {
        if (star.getXrayLuminosityClass() == null) return 1.0;

        double baseFactor = switch (star.getXrayLuminosityClass()) {
            case "INTENSE" -> 100.0;
            case "BRIGHT" -> 10.0;
            case "MODERATE" -> 3.0;
            case "DIM" -> 1.0;
            case "DARK" -> 0.1;
            default -> 1.0;
        };

        // Scale by 1/distance² (X-ray flux falls off with distance)
        // Caller should divide by distanceAU² if needed for distance-specific calculation
        return baseFactor;
    }

    public static double atmosphericStrippingFactor(Star star, double distanceAU) {
        double windFactor = windPressureNormalized(star, distanceAU);
        double activity = activityMultiplier(star);
        double xray = xrayRadiationFactor(star) / (distanceAU * distanceAU);

        // Combine: wind is the primary driver, activity modulates it, xray adds photolytic loss
        return windFactor * activity * (1.0 + 0.2 * Math.log10(Math.max(1.0, xray)));
    }

    public static double effectiveLuminosity(Star star) {
        double luminosity = star.getSolarLuminosity();

        Double spotCoverage = star.getStarspotCoveragePercent();
        Double spotContrast = star.getStarspotTempContrastK();
        double surfaceTemp = star.getSurfaceTemp();

        if (spotCoverage == null || spotCoverage < 0.01 || spotContrast == null || surfaceTemp <= 0) {
            return luminosity;
        }

        double f = spotCoverage / 100.0; // fractional coverage
        double tempRatio = 1.0 - (spotContrast / surfaceTemp);
        if (tempRatio <= 0) tempRatio = 0.01; // safety

        // Stefan-Boltzmann: flux ratio = T⁴ ratio
        double spotFluxRatio = Math.pow(tempRatio, 4);

        // Effective luminosity = L * (unspotted fraction + spotted fraction * reduced flux)
        double effectiveFraction = (1.0 - f) + f * spotFluxRatio;

        // Grand minimum further dims the star
        if (Boolean.TRUE.equals(star.getInGrandMinimum())) {
            double depth = star.getGrandMinimumDepth() != null ? star.getGrandMinimumDepth() : 0.8;
            // Maunder Minimum reduced solar luminosity by ~0.1-0.25%
            effectiveFraction *= (1.0 - depth * 0.003);
        }

        return luminosity * effectiveFraction;
    }

    public static double evolutionaryPlanetFactor(Star star) {
        if (star.getEvolutionaryStage() == null) return 1.0;

        return switch (star.getEvolutionaryStage()) {
            case "PRE_MAIN_SEQUENCE" -> 0.5;  // Still forming, fewer stable planets
            case "EARLY_MAIN_SEQUENCE" -> 1.0;
            case "MID_MAIN_SEQUENCE" -> 1.0;
            case "LATE_MAIN_SEQUENCE" -> 0.9;  // Some inner migration/loss
            case "SUBGIANT" -> 0.7;            // Expanding, inner planets threatened
            case "RED_GIANT", "ASYMPTOTIC_GIANT" -> 0.4; // Inner planets consumed
            case "WHITE_DWARF_COOLING" -> 0.3;  // Most planets lost
            case "BROWN_DWARF_COOLING" -> 0.8;
            default -> 1.0;
        };
    }

    public static double tidalLockingThresholdAU(Star star) {
        double baseLock = 0.1; // Current default

        if (star.getEvolutionaryStage() != null) {
            baseLock *= switch (star.getEvolutionaryStage()) {
                case "PRE_MAIN_SEQUENCE" -> 0.3;   // Not enough time
                case "EARLY_MAIN_SEQUENCE" -> 0.7;
                case "MID_MAIN_SEQUENCE" -> 1.0;
                case "LATE_MAIN_SEQUENCE" -> 1.5;   // More time = wider lock zone
                case "SUBGIANT", "RED_GIANT" -> 2.0;
                default -> 1.0;
            };
        }

        // Scale with stellar mass (heavier stars = stronger tidal torque)
        baseLock *= Math.pow(star.getSolarMass(), 1.0 / 3.0);

        return baseLock;
    }

    public static boolean isSurfaceSterilizing(Star star, double distanceAU) {
        if (Boolean.TRUE.equals(star.getSuperflareCapable())) {
            return distanceAU < 0.5; // Superflares sterilize out to ~0.5 AU
        }
        if ("X_CLASS".equals(star.getFlareClass())) {
            return distanceAU < 0.15; // X-class flares dangerous very close
        }
        return false;
    }
}