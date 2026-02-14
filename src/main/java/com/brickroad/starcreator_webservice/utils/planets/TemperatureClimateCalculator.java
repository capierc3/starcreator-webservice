package com.brickroad.starcreator_webservice.utils.planets;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.utils.CelestialBodyUtils;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class TemperatureClimateCalculator {

    // ================================================================
    // MAIN ENTRY POINT
    // ================================================================

    public void calculate(PlanetaryWeather weather, Planet planet, Star parentStar, StarSystem system) {
        String atmClass = planet.getAtmosphereClassification();
        double surfaceTemp = planet.getSurfaceTemp() != null ? planet.getSurfaceTemp() : 250.0;
        double pressureAtm = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 1.0;
        double rotationHours = planet.getRotationPeriodHours() != null ? planet.getRotationPeriodHours() : 24.0;
        double axialTilt = planet.getAxialTilt() != null ? planet.getAxialTilt() : 0.0;
        double eccentricity = planet.getEccentricity() != null ? planet.getEccentricity() : 0.0;
        boolean tidallyLocked = Boolean.TRUE.equals(planet.getTidallyLocked());

        // Thermal inertia class
        String thermalInertia = calculateThermalInertia(planet, atmClass, pressureAtm);
        weather.setThermalInertiaClass(thermalInertia);

        // Day/night temperature range
        double dayNightRange = calculateDayNightRange(
                surfaceTemp, pressureAtm, rotationHours, tidallyLocked, thermalInertia, atmClass);
        weather.setDayNightTempRangeK(round2(dayNightRange));

        // Seasonal temperature amplitude
        double seasonalAmp = calculateSeasonalAmplitude(surfaceTemp, axialTilt, eccentricity, pressureAtm, atmClass);
        weather.setSeasonalTempAmplitudeK(round2(seasonalAmp));

        // Season description
        weather.setSeasonDescription(generateSeasonDescription(axialTilt, eccentricity, seasonalAmp, tidallyLocked));

        // Equatorial and polar temperatures
        calculateLatitudinalTemperatures(weather, surfaceTemp, axialTilt, dayNightRange, atmClass);

        // Climate zones
        List<ClimateZone> zones = generateClimateZones(weather, planet, tidallyLocked, atmClass);
        weather.setClimateZones(zones);
    }

    // ================================================================
    // THERMAL INERTIA
    // ================================================================

    private String calculateThermalInertia(Planet planet, String atmClass, double pressureAtm) {
        if (CelestialBodyUtils.isGasGiantAtmosphere(atmClass)) {
            return "EXTREME";
        }

        double liquidWater = planet.getLiquidWaterCoveragePercent() != null ? planet.getLiquidWaterCoveragePercent() : 0.0;
        double icePercent = planet.getIceCoveragePercent() != null ? planet.getIceCoveragePercent() : 0.0;

        // Dense atmosphere dominates
        if (pressureAtm > 50) return "EXTREME";  // Venus-class
        if (pressureAtm > 5) return "HIGH";

        // Ocean coverage is the biggest surface factor
        if (liquidWater > 60) return "HIGH";
        if (liquidWater > 30) return "MODERATE";

        // Ice coverage provides some thermal buffering
        if (icePercent > 50 && pressureAtm > 0.5) return "MODERATE";

        // Pressure provides atmospheric thermal buffering
        if (pressureAtm > 1.0) return "MODERATE";
        if (pressureAtm > 0.1) return "LOW";

        return "LOW"; // Thin atmosphere, dry rocky surface
    }

    // ================================================================
    // DAY/NIGHT TEMPERATURE RANGE
    // ================================================================


    private double calculateDayNightRange(double surfaceTemp, double pressureAtm, double rotationHours,
                                          boolean tidallyLocked, String thermalInertia, String atmClass) {

        if (CelestialBodyUtils.isGasGiantAtmosphere(atmClass)) {
            // Gas giants: internal heat + massive atmosphere = minimal day/night variation
            // Jupiter: a few K at cloud tops
            return RandomUtils.rollRange(1.0, 5.0);
        }

        if (tidallyLocked) {
            // Tidally locked: permanent day/night. Range depends on heat redistribution.
            // With thick atmosphere: moderate (atmosphere transports heat)
            // Without: extreme (Mercury-like)
            double baseRange = surfaceTemp * 0.6;
            if (pressureAtm > 5.0) return baseRange * 0.1;      // Dense atm redistribution (like if Venus were locked)
            if (pressureAtm > 1.0) return baseRange * 0.25;     // Good redistribution
            if (pressureAtm > 0.1) return baseRange * 0.5;      // Partial redistribution
            return Math.min(baseRange, 500.0);                    // Near-vacuum: extreme
        }

        // Non-locked rotation: base range from rotation period
        // Earth: 24h → ~12K. Mars: 24.6h → ~70K (thin atm). Venus: 5832h → ~1K (thick atm)
        double rotationFactor = Math.sqrt(rotationHours / CelestialBodyUtils.EARTH_ROTATION_HOURS);

        // Base range assuming Earth-like conditions
        double baseRange = 12.0 * rotationFactor;

        // Atmospheric damping — the dominant factor
        // Dense atmosphere redistributes heat, reducing day/night swing
        double atmDamping;
        if (pressureAtm > 50) {
            atmDamping = 0.02;  // Venus-class: almost no variation
        } else if (pressureAtm > 5) {
            atmDamping = 0.1;
        } else if (pressureAtm > 1) {
            atmDamping = 0.5 + 0.5 / pressureAtm;
        } else if (pressureAtm > 0.01) {
            // Thin atm: less damping
            atmDamping = 2.0 + 3.0 * (1.0 - pressureAtm);
        } else {
            // Near-vacuum: massive swings
            atmDamping = 10.0;
        }

        double range = baseRange * atmDamping;

        // Thermal inertia modifier
        switch (thermalInertia) {
            case "EXTREME" -> range *= 0.1;
            case "HIGH" -> range *= 0.5;
            case "MODERATE" -> {} // No change
            case "LOW" -> range *= 1.5;
        }

        // Scale with absolute temperature (hotter worlds radiate more efficiently)
        range *= Math.sqrt(surfaceTemp / CelestialBodyUtils.EARTH_SURFACE_TEMP_K);

        // Clamp to physical limits
        return Math.max(0.5, Math.min(range, surfaceTemp * 0.9));
    }

    // ================================================================
    // SEASONAL AMPLITUDE
    // ================================================================

    private double calculateSeasonalAmplitude(double surfaceTemp, double axialTilt,
                                              double eccentricity, double pressureAtm, String atmClass) {

        if (CelestialBodyUtils.isGasGiantAtmosphere(atmClass)) {
            // Gas giants: internal heat dominates, minimal seasonal effect
            // But tilt can create some banding variation
            return axialTilt > 20 ? RandomUtils.rollRange(2.0, 8.0) : RandomUtils.rollRange(0.5, 3.0);
        }

        // Tilt contribution: ΔT_tilt ∝ sin(tilt) × base_temp_factor
        // Earth: sin(23.4°) ≈ 0.40 → ~15K
        double tiltRadians = Math.toRadians(axialTilt);
        double tiltContribution = surfaceTemp * 0.13 * Math.sin(tiltRadians);
        // 0.13 calibrated so Earth (288K, 23.4°) → ~15K

        // Eccentricity contribution: flux varies as (1/(1-e)² - 1/(1+e)²)
        // Earth e=0.017 → ~3.4% flux variation → ~1K
        // Mars e=0.093 → ~19% flux variation → ~10K
        double fluxVariation = 4.0 * eccentricity; // Approximate: fractional flux change ≈ 4e
        double eccContribution = surfaceTemp * fluxVariation * 0.1;

        double totalSeasonal = tiltContribution + eccContribution;

        // Atmospheric damping
        if (pressureAtm > 50) {
            totalSeasonal *= 0.05;  // Dense: almost no seasons
        } else if (pressureAtm > 5) {
            totalSeasonal *= 0.2;
        } else if (pressureAtm > 1) {
            totalSeasonal *= 0.7;
        } else if (pressureAtm < 0.1) {
            totalSeasonal *= 1.5;  // Thin atm amplifies
        }

        return Math.max(0.0, totalSeasonal);
    }

    // ================================================================
    // SEASON DESCRIPTION
    // ================================================================

    private String generateSeasonDescription(double axialTilt, double eccentricity,
                                             double seasonalAmp, boolean tidallyLocked) {
        if (tidallyLocked) {
            return "No traditional seasons — permanent day and night hemispheres with a twilight terminator band";
        }

        if (seasonalAmp < 2.0) {
            return "Essentially no seasonal variation — stable climate year-round";
        } else if (seasonalAmp < 8.0) {
            String driver = axialTilt > 10 ? "mild axial tilt" : "slight orbital eccentricity";
            return "Minimal seasons driven by " + driver + ". Temperature variation barely noticeable.";
        } else if (seasonalAmp < 20.0) {
            return "Moderate four-season cycle. Temperate zones experience distinct but manageable seasons.";
        } else if (seasonalAmp < 40.0) {
            return "Pronounced seasons with significant temperature swings. Polar regions experience extended freeze-thaw cycles.";
        } else if (seasonalAmp < 80.0) {
            return "Extreme seasons — polar regions may become uninhabitable in winter. " +
                    "Massive atmospheric circulation shifts between seasons.";
        } else {
            String cause = axialTilt > 50
                    ? "extreme axial tilt (" + String.format("%.0f", axialTilt) + "°)"
                    : "high eccentricity and tilt combination";
            return "Apocalyptic seasonal swings driven by " + cause +
                    ". Hemispheres alternate between scorching and freezing. " +
                    "Habitability may be limited to brief transitional windows.";
        }
    }

    // ================================================================
    // LATITUDINAL TEMPERATURES
    // ================================================================

    private void calculateLatitudinalTemperatures(PlanetaryWeather weather, double surfaceTemp,
                                                  double axialTilt, double dayNightRange, String atmClass) {

        if (CelestialBodyUtils.isGasGiantAtmosphere(atmClass)) {
            // Gas giants: equator slightly warmer than poles due to insolation geometry
            // But internal heat reduces the gradient
            double gradient = RandomUtils.rollRange(5.0, 20.0);
            weather.setMeanEquatorialTempK(round2(surfaceTemp + gradient / 2));
            weather.setMeanPolarTempK(round2(surfaceTemp - gradient / 2));
            return;
        }

        // Equator-to-pole temperature gradient
        // Driven by: insolation geometry (cos(latitude)), atmospheric heat transport, axial tilt
        // Earth: equator ~300K, poles ~250K → gradient ~50K
        // Low tilt → steeper gradient (less polar heating)
        // High tilt → reduced gradient (poles get more direct light part of year)

        double tiltFactor = 1.0 - 0.3 * Math.sin(Math.toRadians(axialTilt)); // High tilt reduces gradient
        double baseGradient = surfaceTemp * 0.15 * tiltFactor; // ~15% of surface temp

        weather.setMeanEquatorialTempK(round2(surfaceTemp + baseGradient * 0.3));
        weather.setMeanPolarTempK(round2(surfaceTemp - baseGradient * 0.7));
    }

    // ================================================================
    // CLIMATE ZONES
    // ================================================================

    private List<ClimateZone> generateClimateZones(PlanetaryWeather weather, Planet planet,
                                                   boolean tidallyLocked, String atmClass) {
        List<ClimateZone> zones = new ArrayList<>();
        double surfaceTemp = planet.getSurfaceTemp() != null ? planet.getSurfaceTemp() : 250.0;
        double axialTilt = planet.getAxialTilt() != null ? planet.getAxialTilt() : 0.0;
        double equatorialTemp = weather.getMeanEquatorialTempK() != null ? weather.getMeanEquatorialTempK() : surfaceTemp;
        double polarTemp = weather.getMeanPolarTempK() != null ? weather.getMeanPolarTempK() : surfaceTemp - 30;

        if (CelestialBodyUtils.isGasGiantAtmosphere(atmClass)) {
            zones.addAll(generateGasGiantZones(equatorialTemp, polarTemp));
            return zones;
        }

        if (tidallyLocked) {
            zones.addAll(generateTidallyLockedZones(surfaceTemp, planet));
            return zones;
        }

        // Standard latitude-based climate zones
        // Tropical band: 0° to ~tilt° latitude on each side
        // Temperate: tilt° to ~(90-tilt)° (or ~66.5° for Earth)
        // Polar: remaining

        double tropicalBound = Math.max(5.0, axialTilt); // Minimum 5° tropical band
        double polarBound = 90.0 - tropicalBound; // Arctic/Antarctic circle equivalent

        // Coverage percentages (from spherical geometry: area ∝ sin(latitude))
        double tropicalCoverage = 100.0 * Math.sin(Math.toRadians(tropicalBound));
        double polarCoverage = 100.0 * (1.0 - Math.sin(Math.toRadians(polarBound)));
        double temperateCoverage = 100.0 - tropicalCoverage - polarCoverage;

        // Ensure sane values
        tropicalCoverage = Math.max(5.0, Math.min(80.0, tropicalCoverage));
        polarCoverage = Math.max(2.0, Math.min(40.0, polarCoverage));
        temperateCoverage = 100.0 - tropicalCoverage - polarCoverage;

        // Temperature interpolation
        double tempTempK = (equatorialTemp + polarTemp) / 2.0;

        ClimateZone tropical = new ClimateZone();
        tropical.setZoneName("TROPICAL");
        tropical.setLatitudeStartDegrees(-tropicalBound);
        tropical.setLatitudeEndDegrees(tropicalBound);
        tropical.setCoveragePercent(round2(tropicalCoverage));
        tropical.setMeanTempK(round2(equatorialTemp));
        tropical.setDescription(describeTropicalZone(equatorialTemp));
        zones.add(tropical);

        ClimateZone temperateN = new ClimateZone();
        temperateN.setZoneName("TEMPERATE");
        temperateN.setLatitudeStartDegrees(tropicalBound);
        temperateN.setLatitudeEndDegrees(polarBound);
        temperateN.setCoveragePercent(round2(temperateCoverage));
        temperateN.setMeanTempK(round2(tempTempK));
        temperateN.setDescription(describeTemperateZone(tempTempK, weather.getSeasonalTempAmplitudeK()));
        zones.add(temperateN);

        ClimateZone polar = new ClimateZone();
        polar.setZoneName("POLAR");
        polar.setLatitudeStartDegrees(polarBound);
        polar.setLatitudeEndDegrees(90.0);
        polar.setCoveragePercent(round2(polarCoverage));
        polar.setMeanTempK(round2(polarTemp));
        polar.setDescription(describePolarZone(polarTemp));
        zones.add(polar);

        return zones;
    }

    // ================================================================
    // TIDALLY LOCKED ZONES
    // ================================================================

    private List<ClimateZone> generateTidallyLockedZones(double surfaceTemp, Planet planet) {
        List<ClimateZone> zones = new ArrayList<>();
        double pressureAtm = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 1.0;

        // Heat redistribution factor
        double redistribution = pressureAtm > 5 ? 0.8 : pressureAtm > 1 ? 0.5 : pressureAtm > 0.1 ? 0.3 : 0.1;

        double substellarTemp = surfaceTemp * (1.0 + 0.3 * (1.0 - redistribution));
        double antistellarTemp = surfaceTemp * (1.0 - 0.4 * (1.0 - redistribution));
        double terminatorTemp = (substellarTemp + antistellarTemp) / 2.0;

        ClimateZone substellar = new ClimateZone();
        substellar.setZoneName("SUBSTELLAR");
        substellar.setCoveragePercent(30.0);
        substellar.setMeanTempK(round2(substellarTemp));
        substellar.setDescription("Permanent dayside centered on the substellar point. " +
                String.format("Peak temperature ~%.0fK.", substellarTemp));
        zones.add(substellar);

        ClimateZone terminator = new ClimateZone();
        terminator.setZoneName("TERMINATOR");
        terminator.setCoveragePercent(20.0);
        terminator.setMeanTempK(round2(terminatorTemp));
        terminator.setDescription("Permanent twilight ring. Most temperate region — " +
                String.format("~%.0fK mean.", terminatorTemp) +
                (pressureAtm > 0.5 ? " Best candidate for habitation." : ""));
        zones.add(terminator);

        ClimateZone antistellar = new ClimateZone();
        antistellar.setZoneName("ANTISTELLAR");
        antistellar.setCoveragePercent(50.0);
        antistellar.setMeanTempK(round2(antistellarTemp));
        antistellar.setDescription("Permanent nightside. " +
                (antistellarTemp < 150 ? "Cryogenic conditions — atmosphere may freeze out." :
                        String.format("Dark but moderated by atmospheric heat transport (~%.0fK).", antistellarTemp)));
        zones.add(antistellar);

        return zones;
    }

    // ================================================================
    // GAS GIANT ZONES
    // ================================================================

    private List<ClimateZone> generateGasGiantZones(double equatorialTemp, double polarTemp) {
        List<ClimateZone> zones = new ArrayList<>();

        ClimateZone equatorial = new ClimateZone();
        equatorial.setZoneName("EQUATORIAL_BAND");
        equatorial.setLatitudeStartDegrees(-20.0);
        equatorial.setLatitudeEndDegrees(20.0);
        equatorial.setCoveragePercent(34.0);
        equatorial.setMeanTempK(round2(equatorialTemp));
        equatorial.setDescription("Equatorial zone — highest wind speeds, strongest convective activity");
        zones.add(equatorial);

        double midTemp = (equatorialTemp + polarTemp) / 2.0;

        ClimateZone midLat = new ClimateZone();
        midLat.setZoneName("MID_LATITUDE_BAND");
        midLat.setLatitudeStartDegrees(20.0);
        midLat.setLatitudeEndDegrees(60.0);
        midLat.setCoveragePercent(46.0);
        midLat.setMeanTempK(round2(midTemp));
        midLat.setDescription("Mid-latitude bands — alternating light and dark cloud belts");
        zones.add(midLat);

        ClimateZone polarZone = new ClimateZone();
        polarZone.setZoneName("POLAR_REGION");
        polarZone.setLatitudeStartDegrees(60.0);
        polarZone.setLatitudeEndDegrees(90.0);
        polarZone.setCoveragePercent(20.0);
        polarZone.setMeanTempK(round2(polarTemp));
        polarZone.setDescription("Polar vortex region — cyclonic storm systems common");
        zones.add(polarZone);

        return zones;
    }

    // ================================================================
    // ZONE DESCRIPTIONS
    // ================================================================

    private String describeTropicalZone(double tempK) {
        if (tempK > 320) return "Scorching equatorial belt — extreme heat dominates";
        if (tempK > 290) return "Warm tropical zone with strong convective activity";
        if (tempK > 260) return "Cool tropical zone — habitable but chilly by Earth standards";
        if (tempK > 230) return "Cold equatorial belt — sub-freezing even at the warmest latitudes";
        return "Frozen equatorial zone — extreme cold planet-wide";
    }

    private String describeTemperateZone(double tempK, Double seasonalAmp) {
        double amp = seasonalAmp != null ? seasonalAmp : 0;
        String seasonNote = amp > 20 ? " with dramatic seasonal swings" :
                amp > 8 ? " with distinct seasons" : " with mild seasonal variation";
        if (tempK > 300) return "Warm temperate zone" + seasonNote;
        if (tempK > 270) return "Earth-like temperate zone" + seasonNote;
        if (tempK > 240) return "Cold temperate zone — snow-dominant winters" + seasonNote;
        if (tempK > 200) return "Subarctic conditions — habitable only in summer" + seasonNote;
        return "Permafrost zone — perpetually frozen" + seasonNote;
    }

    private String describePolarZone(double tempK) {
        if (tempK > 273) return "Unusually warm polar region — no permanent ice caps";
        if (tempK > 230) return "Seasonal ice caps — summer thaw exposes surface";
        if (tempK > 170) return "Permanent ice caps with seasonal variation at margins";
        if (tempK > 100) return "Deep-frozen polar region — CO2 may condense at surface";
        return "Cryogenic polar region — atmospheric gases may freeze out";
    }

    // ================================================================
    // UTILITY
    // ================================================================

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
