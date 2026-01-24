package com.brickroad.starcreator_webservice.utils;

import com.brickroad.starcreator_webservice.model.enums.BinaryConfiguration;
import com.brickroad.starcreator_webservice.model.starSystems.StarSystem;
import com.brickroad.starcreator_webservice.model.stars.Star;

public class TemperatureCalculator {

    private static final double AU_TO_KM = 149_597_870.7;
    private static final double AU_TO_METERS = 149_597_870_700.0;

    public static double calculateSingleStarTemperature(Star star, double distanceAU, Double albedo) {
        double effectiveAlbedo = (albedo != null) ? albedo : 0.3;

        double starTempK = star.getSurfaceTemp();
        double starRadiusAU = star.getRadius() / AU_TO_KM;
        double distanceRatio = starRadiusAU / (2 * distanceAU);

        return starTempK * Math.sqrt(distanceRatio) * Math.pow(1 - effectiveAlbedo, 0.25);
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
}