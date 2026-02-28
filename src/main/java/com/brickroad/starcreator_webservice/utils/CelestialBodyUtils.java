package com.brickroad.starcreator_webservice.utils;

import com.brickroad.starcreator_webservice.entity.ud.Atmosphere;
import com.brickroad.starcreator_webservice.entity.ud.AtmosphereComponent;
import com.brickroad.starcreator_webservice.enums.AtmosphereGas;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CelestialBodyUtils {

    private CelestialBodyUtils() {} // Prevent instantiation

    // ================================================================
    // PLANET TYPE CLASSIFICATION
    // ================================================================

    public static boolean isGasGiant(String planetType) {
        if (planetType == null) return false;
        String lower = planetType.toLowerCase();
        return lower.contains("gas giant")
                || lower.contains("jupiter")
                || lower.contains("ice giant")
                || lower.contains("neptune")
                || lower.contains("mini-neptune")
                || lower.contains("sub-neptune")
                || lower.contains("puffy");
    }

    public static boolean isJovianGasGiant(String planetType) {
        if (planetType == null) return false;
        String lower = planetType.toLowerCase();
        return (lower.contains("gas giant") || lower.contains("jupiter") || lower.contains("puffy"))
                && !lower.contains("ice")
                && !lower.contains("neptune");
    }

    public static boolean isIceGiant(String planetType) {
        if (planetType == null) return false;
        String lower = planetType.toLowerCase();
        return lower.contains("ice giant")
                || lower.contains("neptune")
                || lower.contains("mini-neptune")
                || lower.contains("sub-neptune");
    }

    public static boolean isGasGiantAtmosphere(String atmosphereClassification) {
        return "JOVIAN".equals(atmosphereClassification)
                || "ICE_GIANT".equals(atmosphereClassification);
    }

    public static boolean isRockyBody(String planetType) {
        return !isGasGiant(planetType);
    }

    // ================================================================
    // ATMOSPHERE COMPOSITION PARSING
    // ================================================================

    public static double parseGasPercentage(String compositionString, String gasFormula) {
        if (compositionString == null || compositionString.isEmpty() || "None".equals(compositionString)) {
            return 0.0;
        }
        String regex = "(?:^|, )" + Pattern.quote(gasFormula) + "\\s+([\\d.]+)%";
        Matcher matcher = Pattern.compile(regex).matcher(compositionString);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    public static double calculateMeanMolecularWeight(Atmosphere atmosphere) {
        if (atmosphere == null) return 29.0; // Earth-like fallback

        List<AtmosphereComponent> components = atmosphere.getComponents();
        if (components != null && !components.isEmpty()) {
            double totalWeight = 0.0;
            double totalPercent = 0.0;
            for (AtmosphereComponent comp : components) {
                Double mw = comp.getMolecularWeight();
                double pct = comp.percentage();
                if (mw != null && pct > 0) {
                    totalWeight += (pct / 100.0) * mw;
                    totalPercent += pct;
                }
            }
            if (totalPercent > 0 && totalWeight > 0) {
                // Normalize in case percentages don't sum to exactly 100
                return totalWeight * (100.0 / totalPercent);
            }
        }

        // Fallback: estimate from classification
        return estimateMolecularWeightFromClassification(atmosphere.getClassification());
    }

    public static double calculateMeanMolecularWeightFromString(String compositionString) {
        if (compositionString == null || compositionString.isEmpty() || "None".equals(compositionString)) {
            return 29.0;
        }

        double totalWeight = 0.0;
        double totalPercent = 0.0;

        for (AtmosphereGas gas : AtmosphereGas.values()) {
            double pct = parseGasPercentage(compositionString, gas.getFormula());
            if (pct > 0) {
                totalWeight += (pct / 100.0) * gas.getMolecularWeight();
                totalPercent += pct;
            }
        }

        if (totalPercent > 0 && totalWeight > 0) {
            return totalWeight * (100.0 / totalPercent);
        }
        return 29.0; // Earth-like fallback
    }

    public static double estimateMolecularWeightFromClassification(String classification) {
        if (classification == null) return 29.0;
        return switch (classification) {
            case "EARTH_LIKE" -> 29.0;   // N2/O2 mix
            case "VENUS_LIKE", "MARS_LIKE" -> 44.0;   // CO2 dominated
            case "TITAN_LIKE" -> 28.0;   // N2 dominated
            case "JOVIAN" -> 2.3;    // H2/He (Jupiter: ~2.22)
            case "ICE_GIANT" -> 2.6;    // H2/He with heavier admixture
            case "AMMONIA" -> 17.0;   // NH3 dominated
            case "VOLCANIC" -> 50.0;   // SO2/CO2 mix
            case "REDUCING" -> 6.0;    // H2/CH4 mix
            case "CORROSIVE" -> 55.0;   // Cl2/F2 heavy
            case "EXOTIC" -> 40.0;   // Metallic vapors, variable
            default -> 29.0;
        };
    }

    // ================================================================
    // PHYSICAL CONSTANTS (commonly needed across creators)
    // ================================================================

    /** Boltzmann constant in J/K */
    public static final double BOLTZMANN_K = 1.380649e-23;

    /** Atomic mass unit in kg (1 dalton) */
    public static final double AMU_KG = 1.66053906660e-27;

    /** Stefan-Boltzmann constant in W⋅m⁻²⋅K⁻⁴ */
    public static final double STEFAN_BOLTZMANN = 5.670374419e-8;

    /** Standard gravity in m/s² */
    public static final double EARTH_GRAVITY_MS2 = PhysicsFormulas.EARTH_GRAVITY_MS2;

    /** Earth surface temperature in K */
    public static final double EARTH_SURFACE_TEMP_K = 288.0;

    /** Earth scale height in km */
    public static final double EARTH_SCALE_HEIGHT_KM = 8.5;

    /** Earth rotation period in hours */
    public static final double EARTH_ROTATION_HOURS = 24.0;

    /** Earth radius in km */
    public static final double EARTH_RADIUS_KM = PhysicsFormulas.EARTH_RADIUS_KM;
}
