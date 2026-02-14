package com.brickroad.starcreator_webservice.utils;

import com.brickroad.starcreator_webservice.enums.BinaryConfiguration;
import com.brickroad.starcreator_webservice.entity.ud.StarSystem;
import com.brickroad.starcreator_webservice.entity.ud.Star;
import com.brickroad.starcreator_webservice.utils.planets.StellarEnvironment;

import static com.brickroad.starcreator_webservice.utils.ConversionFormulas.AU_TO_METERS;

public class TemperatureCalculator {

    public static double calculateSingleStarTemperature(Star star, double distanceAU, Double albedo) {
        return calculateSingleStarTemperatureWithSpots(star, distanceAU, albedo);
    }

    public static double calculateCircumbinaryTemperature(StarSystem system, double distanceAU, Double albedo) {
        double effectiveAlbedo = (albedo != null) ? albedo : 0.3;

        double combinedLuminosity = system.getStars().stream()
                .mapToDouble(Star::getSolarLuminosity)
                .sum();

        return 278.0 * Math.pow(combinedLuminosity * (1 - effectiveAlbedo), 0.25) / Math.sqrt(distanceAU);
    }

    public static double calculatePlanetTemperature(Star star, double distanceAU, Double albedo) {
        StarSystem system = star.getSystem();

        if (system != null && system.getBinaryConfiguration() == BinaryConfiguration.P_TYPE) {
            return calculateCircumbinaryTemperature(system, distanceAU, albedo);
        } else {
            return calculateSingleStarTemperature(star, distanceAU, albedo);
        }
    }

    public static double calculateStellarFlux(double luminosity, double distanceAU) {
        double distanceMeters = distanceAU * AU_TO_METERS;
        double area = 4.0 * Math.PI * distanceMeters * distanceMeters;
        return luminosity / area;
    }

    public static double calculateTemperatureFromFlux(double fluxWattsPerM2, double albedo) {
        double absorbedFlux = fluxWattsPerM2 * (1.0 - albedo);
        // T = (F / (4 * σ))^(1/4) where σ is Stefan-Boltzmann constant
        double stefanBoltzmann = 5.670374419e-8; // W⋅m⁻²⋅K⁻⁴
        return Math.pow(absorbedFlux / (4.0 * stefanBoltzmann), 0.25);
    }

    public static double calculateSingleStarTemperatureWithSpots(Star star, double distanceAU, Double albedo) {
        double effectiveAlbedo = (albedo != null) ? albedo : 0.3;
        double effectiveLum = StellarEnvironment.effectiveLuminosity(star);
        return 278.0 * Math.pow(effectiveLum * (1 - effectiveAlbedo), 0.25) / Math.sqrt(distanceAU);
    }

    public static double estimateGreenhouseWarming(String atmClass, double pressureAtm,
                                                   String atmosphereComposition) {
        if (atmClass == null || "NONE".equals(atmClass) || pressureAtm < 0.001) return 0;

        if ("JOVIAN".equals(atmClass) || "ICE_GIANT".equals(atmClass)) return 0;

        double co2Pct = CelestialBodyUtils.parseGasPercentage(atmosphereComposition, "CO2");
        double ch4Pct = CelestialBodyUtils.parseGasPercentage(atmosphereComposition, "CH4");
        double h2oPct = CelestialBodyUtils.parseGasPercentage(atmosphereComposition, "H2O");

        double greenhouse = 0;

        // CO2: logarithmic — trace amounts still matter. Calibrated so Earth (0.04%, 1 atm) → ~7K
        if (co2Pct > 0) {
            greenhouse += 10.0 * Math.log1p(co2Pct * 25.0) * Math.sqrt(Math.max(pressureAtm, 0.001));
        }

        // H2O: dominant greenhouse gas. Calibrated so Earth (~1-2%, 1 atm) → ~12-18K
        if (h2oPct > 0) {
            greenhouse += 18.0 * Math.log1p(h2oPct) * Math.min(2.0, Math.sqrt(pressureAtm));
        }

        // CH4: very potent per molecule. Calibrated so Titan-level (2-6%) has big impact
        if (ch4Pct > 0) {
            greenhouse += 2.0 * Math.log1p(ch4Pct * 5000.0) * Math.sqrt(Math.max(pressureAtm, 0.001));
        }

        // CO2-H2O synergy: water vapor feedback amplifies CO2 warming
        if (co2Pct > 0 && h2oPct > 0) {
            greenhouse *= 1.2;
        }

        // Pressure broadening: collision-induced absorption
        greenhouse += pressureAtm * 2.0;

        // Venus-like dense CO2 floor
        if ("VENUS_LIKE".equals(atmClass)) greenhouse = Math.max(greenhouse, 400.0);

        return Math.min(600.0, greenhouse);
    }
}