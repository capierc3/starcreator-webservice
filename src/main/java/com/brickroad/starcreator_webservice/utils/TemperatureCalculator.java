package com.brickroad.starcreator_webservice.utils;

import com.brickroad.starcreator_webservice.model.enums.BinaryConfiguration;
import com.brickroad.starcreator_webservice.model.starSystems.StarSystem;
import com.brickroad.starcreator_webservice.model.stars.Star;

public class TemperatureCalculator {

    private static final double AU_TO_KM = 149597870.7;

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

    public static double calculateEquilibriumTemperature(double luminosity, double distanceAU, double albedo) {
        // T = 278 K × (L × (1-A))^0.25 / sqrt(d)
        return 278.0 * Math.pow(luminosity * (1 - albedo), 0.25) / Math.sqrt(distanceAU);
    }
}