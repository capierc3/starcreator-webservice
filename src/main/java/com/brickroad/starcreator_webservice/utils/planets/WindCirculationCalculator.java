package com.brickroad.starcreator_webservice.utils.planets;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.utils.CelestialBodyUtils;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import org.springframework.stereotype.Component;

@Component
public class WindCirculationCalculator {

    // ================================================================
    // MAIN ENTRY POINT
    // ================================================================

    public void calculate(PlanetaryWeather weather, Planet planet) {
        String atmClass = planet.getAtmosphereClassification();
        double surfaceTemp = planet.getSurfaceTemp() != null ? planet.getSurfaceTemp() : 250.0;
        double pressureAtm = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 1.0;
        double rotationHours = planet.getRotationPeriodHours() != null ? planet.getRotationPeriodHours() : 24.0;
        double earthRadius = planet.getEarthRadius() != null ? planet.getEarthRadius() : 1.0;
        double surfaceGravity = planet.getSurfaceGravity() != null ? planet.getSurfaceGravity() : 1.0;
        boolean tidallyLocked = Boolean.TRUE.equals(planet.getTidallyLocked());
        double scaleHeightKm = weather.getScaleHeightKm() != null ? weather.getScaleHeightKm() : 8.5;

        // Temperature gradient (equator-to-pole) from Phase 3
        double equatorialTemp = weather.getMeanEquatorialTempK() != null ? weather.getMeanEquatorialTempK() : surfaceTemp + 10;
        double polarTemp = weather.getMeanPolarTempK() != null ? weather.getMeanPolarTempK() : surfaceTemp - 10;
        double tempGradient = equatorialTemp - polarTemp;

        if (CelestialBodyUtils.isGasGiantAtmosphere(atmClass)) {
            calculateGasGiantCirculation(weather, rotationHours, earthRadius, tempGradient, atmClass);
            return;
        }

        if (tidallyLocked) {
            calculateTidallyLockedCirculation(weather, pressureAtm, tempGradient);
            return;
        }

        // Standard rocky world circulation
        calculateRockyCirculation(weather, rotationHours, earthRadius, pressureAtm,
                surfaceGravity, scaleHeightKm, tempGradient, surfaceTemp, atmClass);

        // Super-rotation check
        checkSuperRotation(weather, rotationHours, pressureAtm, surfaceTemp);
    }

    // ================================================================
    // STANDARD ROCKY WORLD CIRCULATION
    // ================================================================

    private void calculateRockyCirculation(PlanetaryWeather weather, double rotationHours,
                                           double earthRadius, double pressureAtm,
                                           double surfaceGravity, double scaleHeightKm,
                                           double tempGradient, double surfaceTemp, String atmClass) {

        // --- Circulation cell count ---
        // Simplified Rossby number approach: N_cells ~ (Ω×R) / sqrt(ΔT×g×H)
        // Fast rotation + large planet → more cells
        // Slow rotation → fewer cells (1 Hadley per hemisphere)
        // Earth: 24h, 1R → 3 cells per hemisphere

        double angularVelocity = 2.0 * Math.PI / (rotationHours * 3600); // rad/s
        double radiusKm = earthRadius * CelestialBodyUtils.EARTH_RADIUS_KM;
        double radiusM = radiusKm * 1000.0;
        double gravityMs2 = surfaceGravity * CelestialBodyUtils.EARTH_GRAVITY_MS2;

        // Thermal Rossby number: Ro_T = (ΔT × g × H) / (Ω² × R²)
        // Low Ro_T → many cells. High Ro_T → few cells.
        double scaleHeightM = scaleHeightKm * 1000.0;
        double numerator = Math.max(1.0, tempGradient) * gravityMs2 * scaleHeightM;
        double denominator = Math.max(1e-14, angularVelocity * angularVelocity * radiusM * radiusM);
        double thermalRossby = numerator / denominator;

        int cellCount;
        String pattern;
        if (thermalRossby > 5.0) {
            // Very slow rotation or small planet: single Hadley cell
            cellCount = 1;
            pattern = "SINGLE_HADLEY";
        } else if (thermalRossby > 0.5) {
            // Moderate: 2-cell pattern
            cellCount = 2;
            pattern = "TWO_CELL";
        } else if (thermalRossby > 0.05) {
            // Earth-like: 3-cell pattern (Hadley, Ferrel, Polar)
            cellCount = 3;
            pattern = "THREE_CELL";
        } else {
            // Very fast rotation: multi-banded (4+ cells)
            cellCount = (int) Math.min(8, 3 + Math.round(Math.log10(1.0 / thermalRossby)));
            pattern = "MULTI_BANDED";
        }

        weather.setCirculationCellCount(cellCount);
        weather.setCirculationPattern(pattern);

        // --- Jet streams ---
        // Number: roughly cellCount - 1 major jets per hemisphere (boundaries between cells)
        // Speed: driven by rotation rate and temperature gradient
        int jetCount = Math.max(1, cellCount - 1) * 2; // Both hemispheres
        weather.setJetStreamCount(jetCount);

        // Jet stream speed: Earth jets ~30-70 m/s
        // Scales with rotation rate and temperature gradient
        double rotFactor = CelestialBodyUtils.EARTH_ROTATION_HOURS / rotationHours; // Higher for faster rotation
        double jetSpeed = 40.0 * rotFactor * Math.sqrt(tempGradient / 50.0);
        jetSpeed = Math.max(5.0, Math.min(200.0, jetSpeed));
        weather.setJetStreamSpeedMs(round2(jetSpeed));

        // --- Surface winds ---
        calculateSurfaceWinds(weather, pressureAtm, rotationHours, tempGradient, surfaceGravity, surfaceTemp, atmClass);
    }

    // ================================================================
    // GAS GIANT CIRCULATION
    // ================================================================

    private void calculateGasGiantCirculation(PlanetaryWeather weather, double rotationHours,
                                              double earthRadius, double tempGradient, String atmClass) {
        // Gas giants: very fast rotation → many alternating bands
        // Jupiter (9.9h): ~15 bands visible. Saturn (10.7h): ~10 bands. Neptune (16h): ~6.
        double rotFactor = CelestialBodyUtils.EARTH_ROTATION_HOURS / rotationHours;
        int cellCount = (int) Math.round(3 + rotFactor * 5 * Math.sqrt(earthRadius));
        cellCount = Math.max(4, Math.min(25, cellCount));

        weather.setCirculationCellCount(cellCount);
        weather.setCirculationPattern("MULTI_BANDED");

        // Jet streams: between every band
        int jetCount = cellCount * 2; // Alternating prograde/retrograde
        weather.setJetStreamCount(jetCount);

        // Gas giant jet speeds: extreme
        // Jupiter: 100-180 m/s at cloud tops. Neptune: up to 580 m/s!
        double baseJetSpeed;
        if ("ICE_GIANT".equals(atmClass)) {
            // Ice giants have surprisingly fast winds — Neptune holds the record
            baseJetSpeed = RandomUtils.rollRange(200.0, 600.0);
        } else {
            // Jovian
            baseJetSpeed = RandomUtils.rollRange(80.0, 200.0);
        }
        baseJetSpeed *= rotFactor; // Faster rotation → faster jets
        weather.setJetStreamSpeedMs(round2(Math.min(700.0, baseJetSpeed)));

        // Surface winds (at cloud top level for gas giants)
        double meanWind = baseJetSpeed * 0.4;
        double maxGust = baseJetSpeed * 1.5;
        weather.setMeanSurfaceWindSpeedMs(round2(meanWind));
        weather.setMaxGustSpeedMs(round2(maxGust));
        weather.setWindIntensity(classifyWindIntensity(meanWind));

        weather.setHasSuperRotation(false); // Gas giants have differential rotation, not super-rotation
    }

    // ================================================================
    // TIDALLY LOCKED CIRCULATION
    // ================================================================

    private void calculateTidallyLockedCirculation(PlanetaryWeather weather,
                                                   double pressureAtm, double tempGradient) {
        // Tidally locked: single massive substellar-to-antistellar circulation
        // Hot air rises at substellar point, flows to nightside, descends, returns at surface
        weather.setCirculationCellCount(1);
        weather.setCirculationPattern("TIDALLY_LOCKED_SUBSTELLAR");

        // One major jet around the terminator ring
        weather.setJetStreamCount(1);

        // Jet speed depends on day-night temperature difference and atmosphere thickness
        double jetSpeed = 15.0 * Math.sqrt(tempGradient);
        if (pressureAtm > 1) jetSpeed *= Math.sqrt(pressureAtm); // Denser → more wind energy
        jetSpeed = Math.max(5.0, Math.min(150.0, jetSpeed));
        weather.setJetStreamSpeedMs(round2(jetSpeed));

        // Surface winds: convergent at substellar, divergent at antistellar
        double meanSurface = jetSpeed * 0.5;
        double maxGust = jetSpeed * 1.8;
        weather.setMeanSurfaceWindSpeedMs(round2(meanSurface));
        weather.setMaxGustSpeedMs(round2(maxGust));
        weather.setWindIntensity(classifyWindIntensity(meanSurface));

        // Tidally locked planets can exhibit atmospheric super-rotation
        // if thick enough — like GCMs predict for hot Jupiters
        if (pressureAtm > 2.0 && tempGradient > 30) {
            weather.setHasSuperRotation(true);
            weather.setSuperRotationFactor(round2(1.5 + pressureAtm * 0.1));
        } else {
            weather.setHasSuperRotation(false);
        }
    }

    // ================================================================
    // SURFACE WIND SPEEDS (rocky worlds)
    // ================================================================

    private void calculateSurfaceWinds(PlanetaryWeather weather, double pressureAtm,
                                       double rotationHours, double tempGradient,
                                       double surfaceGravity, double surfaceTemp, String atmClass) {
        // Surface wind speed model:
        // Base: driven by pressure gradient (from temperature gradient)
        // Modified by: atmosphere density, rotation, gravity, terrain

        // Pressure gradient force → wind
        // Earth: ~7 m/s global mean surface wind
        double baseWind = 7.0;

        // Temperature gradient contribution
        baseWind *= Math.sqrt(Math.max(1.0, tempGradient) / 50.0);

        // Rotation: faster rotation → stronger Coriolis → more organized but not necessarily stronger surface winds
        // Very slow rotation → weak pressure-driven winds
        double rotFactor = Math.sqrt(CelestialBodyUtils.EARTH_ROTATION_HOURS / Math.max(1.0, rotationHours));
        baseWind *= Math.max(0.3, Math.min(2.0, rotFactor));

        // Atmosphere density: thin atmospheres can have high velocities but low force
        // Dense atmospheres: more mass to move → generally slower surface winds
        // Mars: thin atm → high wind speed to carry dust, but low dynamic pressure
        if (pressureAtm < 0.01) {
            baseWind *= 2.5;  // Thin atm: high velocity winds
        } else if (pressureAtm < 0.1) {
            baseWind *= 1.5;
        } else if (pressureAtm > 10) {
            baseWind *= 0.5;  // Dense atm: sluggish surface winds
        } else if (pressureAtm > 50) {
            baseWind *= 0.2;  // Very dense
        }

        // Gravity: higher gravity → lower scale height → steeper pressure gradients near surface
        baseWind *= Math.pow(surfaceGravity, 0.3); // Modest effect

        // Add some natural variance
        double meanWind = baseWind * RandomUtils.rollRange(0.8, 1.2);
        meanWind = Math.max(0.5, Math.min(100.0, meanWind));

        // Gust speeds: typically 2-3x mean
        double gustFactor;
        if ("MARS_LIKE".equals(atmClass)) {
            gustFactor = RandomUtils.rollRange(3.0, 5.0); // Mars: dramatic gusts
        } else if ("VENUS_LIKE".equals(atmClass)) {
            gustFactor = RandomUtils.rollRange(1.2, 1.5); // Venus surface: sluggish
        } else {
            gustFactor = RandomUtils.rollRange(2.0, 3.5);
        }
        double maxGust = meanWind * gustFactor;

        weather.setMeanSurfaceWindSpeedMs(round2(meanWind));
        weather.setMaxGustSpeedMs(round2(maxGust));
        weather.setWindIntensity(classifyWindIntensity(meanWind));
    }

    // ================================================================
    // SUPER-ROTATION CHECK (rocky worlds)
    // ================================================================

    private void checkSuperRotation(PlanetaryWeather weather, double rotationHours,
                                    double pressureAtm, double surfaceTemp) {
        // Super-rotation requires:
        // 1. Slow rotation (>100 hours — Venus: 5832h, but onset around 100-200h)
        // 2. Dense atmosphere (>1 atm)
        // 3. Significant stellar heating (warm surface)

        boolean slowRotation = rotationHours > 100;
        boolean denseAtmosphere = pressureAtm > 2.0;
        boolean warmEnough = surfaceTemp > 200;

        if (slowRotation && denseAtmosphere && warmEnough) {
            weather.setHasSuperRotation(true);
            // Super-rotation factor: how many times faster atmosphere moves
            // Venus: ~60x at cloud tops. Scales with pressure and rotation slowness.
            double factor = Math.log10(rotationHours / 24.0) * Math.log10(pressureAtm + 1) * 5;
            factor = Math.max(1.5, Math.min(100.0, factor));
            weather.setSuperRotationFactor(round2(factor));

            // Super-rotating winds are much faster than surface-relative
            double superWind = weather.getMeanSurfaceWindSpeedMs() * factor * 0.5;
            weather.setJetStreamSpeedMs(round2(Math.max(weather.getJetStreamSpeedMs(), superWind)));
        } else {
            weather.setHasSuperRotation(false);
        }
    }

    // ================================================================
    // WIND CLASSIFICATION
    // ================================================================

    private String classifyWindIntensity(double meanWindMs) {
        if (meanWindMs < 2) return "CALM";
        if (meanWindMs < 8) return "LIGHT";
        if (meanWindMs < 18) return "MODERATE";
        if (meanWindMs < 35) return "STRONG";
        if (meanWindMs < 60) return "EXTREME";
        return "HURRICANE";
    }

    // ================================================================
    // UTILITY
    // ================================================================

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
