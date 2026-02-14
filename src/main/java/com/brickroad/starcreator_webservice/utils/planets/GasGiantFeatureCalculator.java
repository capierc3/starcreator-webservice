package com.brickroad.starcreator_webservice.utils.planets;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.utils.CelestialBodyUtils;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import org.springframework.stereotype.Component;

@Component
public class GasGiantFeatureCalculator {

    // Reference values
    private static final double STEFAN_BOLTZMANN = CelestialBodyUtils.STEFAN_BOLTZMANN;
    private static final double AU_TO_M = 1.496e11;

    // ================================================================
    // MAIN ENTRY POINT
    // ================================================================

    public void calculate(PlanetaryWeather weather, Planet planet, Star parentStar) {
        String atmClass = planet.getAtmosphereClassification();
        if (!CelestialBodyUtils.isGasGiantAtmosphere(atmClass)) {
            return; // Only for gas giants
        }

        double rotationHours = planet.getRotationPeriodHours() != null ? planet.getRotationPeriodHours() : 10.0;
        double earthRadius = planet.getEarthRadius() != null ? planet.getEarthRadius() : 11.0;

        // Band structure
        calculateBandStructure(weather, atmClass, rotationHours, earthRadius);

        // Internal heat
        calculateInternalHeat(weather, planet, parentStar, atmClass);

        // Great dark spot (ice giants)
        checkGreatDarkSpot(weather, atmClass, planet);
    }

    // ================================================================
    // BAND STRUCTURE
    // ================================================================

    private void calculateBandStructure(PlanetaryWeather weather, String atmClass,
                                         double rotationHours, double earthRadius) {
        // Number of visible bands correlates with rotation speed and size
        // Jupiter (9.9h, 11.2 R⊕): ~15 visible bands
        // Saturn (10.7h, 9.4 R⊕): ~10 bands (less contrast)
        // Neptune (16h, 3.9 R⊕): ~6 faint bands

        double rotFactor = 24.0 / Math.max(5.0, rotationHours); // Higher for faster rotation
        double sizeFactor = Math.sqrt(earthRadius / 11.0); // Normalized to Jupiter

        int numberOfBands;
        if ("ICE_GIANT".equals(atmClass)) {
            // Ice giants: fewer, broader bands
            numberOfBands = (int) Math.round(3 + rotFactor * 2 * sizeFactor);
            numberOfBands = Math.max(3, Math.min(12, numberOfBands));
        } else {
            // Jovian: many alternating bands
            numberOfBands = (int) Math.round(5 + rotFactor * 5 * sizeFactor);
            numberOfBands = Math.max(5, Math.min(25, numberOfBands));
        }

        weather.setNumberOfBands(numberOfBands);
    }

    // ================================================================
    // INTERNAL HEAT
    // ================================================================

    private void calculateInternalHeat(PlanetaryWeather weather, Planet planet,
                                        Star parentStar, String atmClass) {
        // Gas giants radiate more energy than they receive from their star
        // Jupiter: emits 1.67x what it receives (internal heat from gravitational contraction)
        // Saturn: emits 1.78x (helium rain contributes)
        // Neptune: emits 2.6x (unknown mechanism)
        // Uranus: emits ~1.06x (anomalously low)

        double semiMajorAU = planet.getSemiMajorAxisAU() != null ? planet.getSemiMajorAxisAU() : 5.0;
        double luminosity = parentStar.getSolarLuminosity() != 0 ? parentStar.getSolarLuminosity() : 1.0;
        double earthRadius = planet.getEarthRadius() != null ? planet.getEarthRadius() : 11.0;
        double albedo = planet.getAlbedo() != null ? planet.getAlbedo() : 0.34;

        // Stellar flux at planet distance (W/m²)
        // Solar constant at 1 AU ≈ 1361 W/m²
        double stellarFluxWm2 = 1361.0 * luminosity / (semiMajorAU * semiMajorAU);

        // Absorbed stellar flux (accounting for albedo)
        double absorbedFlux = stellarFluxWm2 * (1.0 - albedo);

        // Internal heat flux estimate
        double internalRatio;
        if ("ICE_GIANT".equals(atmClass)) {
            // Ice giants: wider range
            internalRatio = RandomUtils.rollRange(1.0, 3.0);
        } else {
            // Jovian: moderate internal heat
            internalRatio = RandomUtils.rollRange(1.3, 2.0);
        }

        double internalFlux = absorbedFlux * (internalRatio - 1.0);
        internalFlux = Math.max(0.1, internalFlux); // Floor: at least some internal heat

        weather.setInternalHeatFluxWm2(round2(internalFlux));
        weather.setInternalToStellarRatio(round2(internalRatio));
    }

    // ================================================================
    // GREAT DARK SPOT
    // ================================================================

    private void checkGreatDarkSpot(PlanetaryWeather weather, String atmClass, Planet planet) {
        if ("ICE_GIANT".equals(atmClass)) {
            // Dark spots correlate with atmospheric energy — driven by internal heat.
            // Neptune (ratio ~2.6) has persistent dark spots; Uranus (~1.06) does not.
            // Higher internal heat → more vigorous convection → vortex formation.
            double ratio = weather.getInternalToStellarRatio() != null
                    ? weather.getInternalToStellarRatio() : 1.5;

            // Scale probability: ratio 1.0 → ~10%, ratio 2.0 → ~50%, ratio 3.0 → ~80%
            int chance = (int) Math.min(90, Math.max(5, (ratio - 1.0) * 40 + 10));
            boolean hasSpot = RandomUtils.rollD100() <= chance;
            weather.setHasGreatDarkSpot(hasSpot);
        } else {
            weather.setHasGreatDarkSpot(false);
        }
    }

    // ================================================================
    // UTILITY
    // ================================================================

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
