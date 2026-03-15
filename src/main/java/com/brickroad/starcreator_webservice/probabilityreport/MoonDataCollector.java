package com.brickroad.starcreator_webservice.probabilityreport;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.model.climate.*;
import com.brickroad.starcreator_webservice.model.habitability.*;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class MoonDataCollector {

    private final Map<String, Integer> moonTypes = new HashMap<>();
    private final Map<String, Integer> moonDynamoTypes = new HashMap<>();
    private final Map<String, Integer> moonProtectionLevels = new HashMap<>();
    private int moonsWithMagField = 0;
    private int moonsAssessed = 0;

    private final Map<String, Integer> moonWaterInventories = new HashMap<>();
    private int moonsWithLiquidWater = 0;
    private int moonsWithIce = 0;
    private int moonsWithSubsurfaceWater = 0;
    private int moonsWithSubsurfaceOcean = 0;

    private final Map<String, Integer> moonHabitabilityClasses = new HashMap<>();
    private final Map<String, Integer> moonColonizationSuitabilities = new HashMap<>();
    private final Map<String, Integer> moonBiosignaturePotentials = new HashMap<>();
    private final Map<String, Integer> moonLifeComplexity = new HashMap<>();
    private final Map<String, Integer> moonRadiationBeltDose = new HashMap<>();
    private final Map<String, Integer> moonTidalContribution = new HashMap<>();
    private double moonEsiSum = 0.0;
    private double moonHabScoreSum = 0.0;
    private int moonHabCount = 0;

    private final Map<String, Integer> moonCompositionTypes = new HashMap<>();
    private final Map<String, Integer> moonTidalHeatingLevels = new HashMap<>();

    // Orbit distance bins (in planet radii)
    private final Map<String, Integer> orbitDistanceBins = new HashMap<>();
    // Eccentricity bins
    private final Map<String, Integer> eccentricityBins = new HashMap<>();
    // Tidal heating broken down by parent planet type
    private final Map<String, Map<String, Integer>> tidalHeatingByPlanetType = new HashMap<>();
    // Tidal heating broken down by moon type
    private final Map<String, Map<String, Integer>> tidalHeatingByMoonType = new HashMap<>();
    // Geological activity counts
    private final Map<String, Integer> geologicalActivity = new HashMap<>();
    // Volcanism type distribution
    private final Map<String, Integer> volcanismTypes = new HashMap<>();
    // Volcanism type cross-tabulated by composition type
    private final Map<String, Map<String, Integer>> volcanismByComposition = new HashMap<>();
    // Erosion agent distribution
    private final Map<String, Integer> erosionAgents = new HashMap<>();
    // Axial tilt bins, separated by tidal locking status
    private final Map<String, Integer> axialTiltLockedBins = new HashMap<>();
    private final Map<String, Integer> axialTiltUnlockedBins = new HashMap<>();
    // Atmosphere classifications (like the planet version)
    private final Map<String, Integer> atmosphereClassifications = new HashMap<>();

    private int moonsWithClimate = 0;
    private final Map<String, Integer> moonClimateSeverity = new HashMap<>();
    private final Map<String, Integer> moonClimateExposure = new HashMap<>();
    private final Map<String, Integer> moonClimateSkyColor = new HashMap<>();
    private final Map<String, Integer> moonClimateCloudClass = new HashMap<>();
    private final Map<String, Integer> moonClimateWindIntensity = new HashMap<>();
    private final Map<String, Integer> moonClimateTidalRangeBins = new HashMap<>();
    private int moonsWithPrecipitation = 0;
    private int moonsWithLightning = 0;
    private int moonsWithPlanetaryEclipses = 0;
    private int moonSiblingAppearanceTotal = 0;
    private int moonsWithParentPlanetVisible = 0;
    private int moonExtremeEventTotal = 0;

    public void analyzeData(Moon moon) {
        moonTypes.merge(moon.getMoonType(), 1, Integer::sum);

        String compType = moon.getCompositionType() != null ? moon.getCompositionType() : "NULL";
        moonCompositionTypes.merge(compType, 1, Integer::sum);

        String tidalLevel = moon.getTidalHeatingLevel() != null ? moon.getTidalHeatingLevel() : "NONE";
        moonTidalHeatingLevels.merge(tidalLevel, 1, Integer::sum);

        // Orbit distance bins (in planet radii)
        if (moon.getSemiMajorAxisKm() != null && moon.getPlanet() != null && moon.getPlanet().getRadius() > 0) {
            double orbitRadii = moon.getSemiMajorAxisKm() / moon.getPlanet().getRadius();
            orbitDistanceBins.merge(binOrbitDistance(orbitRadii), 1, Integer::sum);
        }

        // Eccentricity bins
        if (moon.getEccentricity() != null) {
            eccentricityBins.merge(binEccentricity(moon.getEccentricity()), 1, Integer::sum);
        }

        // Tidal heating by parent planet type
        if (moon.getPlanet() != null && moon.getPlanet().getPlanetType() != null) {
            String planetType = moon.getPlanet().getPlanetType();
            tidalHeatingByPlanetType.computeIfAbsent(planetType, k -> new HashMap<>())
                    .merge(tidalLevel, 1, Integer::sum);
        }

        // Tidal heating by moon type
        tidalHeatingByMoonType.computeIfAbsent(moon.getMoonType(), k -> new HashMap<>())
                .merge(tidalLevel, 1, Integer::sum);

        // Geological activity
        String geoActivity = moon.getGeologicalActivity() != null ? moon.getGeologicalActivity() : "NONE";
        geologicalActivity.merge(geoActivity, 1, Integer::sum);

        // Volcanism type
        String volcType = moon.getVolcanismType() != null ? moon.getVolcanismType() : "None";
        volcanismTypes.merge(volcType, 1, Integer::sum);

        // Volcanism by composition cross-tab
        volcanismByComposition.computeIfAbsent(compType, k -> new HashMap<>())
                .merge(volcType, 1, Integer::sum);

        // Erosion agent
        String erosionAgent = moon.getPrimaryErosionAgent() != null ? moon.getPrimaryErosionAgent() : "None";
        erosionAgents.merge(erosionAgent, 1, Integer::sum);

        // Axial tilt bins (separated by tidal locking)
        if (moon.getAxialTilt() != null) {
            String tiltBin = binAxialTilt(moon.getAxialTilt());
            if (Boolean.TRUE.equals(moon.getTidallyLocked())) {
                axialTiltLockedBins.merge(tiltBin, 1, Integer::sum);
            } else {
                axialTiltUnlockedBins.merge(tiltBin, 1, Integer::sum);
            }
        }

        // Atmosphere classification
        if (Boolean.TRUE.equals(moon.getHasAtmosphere()) && moon.getAtmosphere() != null
                && moon.getAtmosphere().getClassification() != null) {
            atmosphereClassifications.merge(moon.getAtmosphere().getClassification(), 1, Integer::sum);
        } else {
            atmosphereClassifications.merge("NONE", 1, Integer::sum);
        }

        if (Boolean.TRUE.equals(moon.getHasSubsurfaceOcean())) {
            moonsWithSubsurfaceOcean++;
        }

        PlanetaryMagneticField mf = moon.getMagneticField();
        PlanetaryHabitability hab = moon.getHabitability();

        boolean fullyAssessed = hab != null || mf != null;
        if (fullyAssessed) {
            moonsAssessed++;
        }

        String waterInv = moon.getWaterInventory();
        if (waterInv != null) {
            moonWaterInventories.merge(waterInv, 1, Integer::sum);
            Double liquidPct = moon.getLiquidWaterCoveragePercent();
            Double icePct = moon.getIceCoveragePercent();
            if (liquidPct != null && liquidPct > 0.1) moonsWithLiquidWater++;
            if (icePct != null && icePct > 0.1) moonsWithIce++;
            if (Boolean.TRUE.equals(moon.getHasSubsurfaceWater())) moonsWithSubsurfaceWater++;
        }

        if (mf != null) {
            moonsWithMagField++;
            String dynamoType = mf.getDynamoType() != null ? mf.getDynamoType().name() : "NULL";
            moonDynamoTypes.merge(dynamoType, 1, Integer::sum);
            String protection = mf.getProtectionLevel() != null ? mf.getProtectionLevel().name() : "NULL";
            moonProtectionLevels.merge(protection, 1, Integer::sum);
        }

        if (hab != null) {
            moonHabCount++;
            String habClass = hab.getHabitabilityClass() != null ? hab.getHabitabilityClass().name() : "NULL";
            moonHabitabilityClasses.merge(habClass, 1, Integer::sum);
            String colSuit = hab.getColonizationSuitability() != null ? hab.getColonizationSuitability().name() : "NULL";
            moonColonizationSuitabilities.merge(colSuit, 1, Integer::sum);
            String bioPot = hab.getBiosignaturePotential() != null ? hab.getBiosignaturePotential() : "NONE";
            moonBiosignaturePotentials.merge(bioPot, 1, Integer::sum);
            String lifePot = hab.getLifeComplexityPotential() != null ? hab.getLifeComplexityPotential() : "NONE";
            moonLifeComplexity.merge(lifePot, 1, Integer::sum);
            String radDose = hab.getRadiationBeltSurfaceDose() != null ? hab.getRadiationBeltSurfaceDose() : "NONE";
            moonRadiationBeltDose.merge(radDose, 1, Integer::sum);
            String tidalContrib = hab.getTidalHeatingContribution() != null ? hab.getTidalHeatingContribution() : "NONE";
            moonTidalContribution.merge(tidalContrib, 1, Integer::sum);
            if (hab.getEsiTotal() != null) moonEsiSum += hab.getEsiTotal();
            if (hab.getHabitabilityScore() != null) moonHabScoreSum += hab.getHabitabilityScore();
        }

        analyzeMoonClimateData(moon);
    }

    private void analyzeMoonClimateData(Moon moon) {
        PlanetaryClimate w = moon.getClimate();
        if (w == null) return;

        moonsWithClimate++;

        String skyColor = w.getSkyColor() != null ? w.getSkyColor() : "NULL";
        moonClimateSkyColor.merge(skyColor, 1, Integer::sum);

        String cloudClass = w.getCloudCoverageClass() != null ? w.getCloudCoverageClass() : "NULL";
        moonClimateCloudClass.merge(cloudClass, 1, Integer::sum);

        String windIntensity = w.getWindIntensity() != null ? w.getWindIntensity() : "NULL";
        moonClimateWindIntensity.merge(windIntensity, 1, Integer::sum);

        String severity = w.getWeatherSeverity() != null ? w.getWeatherSeverity() : "NULL";
        moonClimateSeverity.merge(severity, 1, Integer::sum);

        String exposure = w.getOutdoorExposureRating() != null ? w.getOutdoorExposureRating() : "NULL";
        moonClimateExposure.merge(exposure, 1, Integer::sum);

        if (Boolean.TRUE.equals(w.getHasPrecipitation())) moonsWithPrecipitation++;
        if (Boolean.TRUE.equals(w.getHasLightning())) moonsWithLightning++;

        if (w.getTidalRangeMeters() != null) {
            String tidalBin = binTidalRange(w.getTidalRangeMeters());
            moonClimateTidalRangeBins.merge(tidalBin, 1, Integer::sum);
        }

        if (w.getExtremeClimateEvents() != null) {
            moonExtremeEventTotal += w.getExtremeClimateEvents().size();
        }

        List<MoonSkyAppearance> skyApps = w.getMoonSkyAppearances();
        if (skyApps != null && !skyApps.isEmpty()) {
            moonsWithParentPlanetVisible++;
            int siblingCount = Math.max(0, skyApps.size() - 1);
            moonSiblingAppearanceTotal += siblingCount;
        }

        List<EclipseData> eclipses = w.getEclipseData();
        if (eclipses != null && !eclipses.isEmpty()) {
            boolean hasPlanetaryEclipse = eclipses.stream()
                    .anyMatch(e -> "PLANET_SOLAR".equals(e.getEclipseSource()));
            if (hasPlanetaryEclipse) moonsWithPlanetaryEclipses++;
        }
    }

    private String binOrbitDistance(double radii) {
        if (radii < 5) return "a: 2-5 Rp";
        if (radii < 10) return "b: 5-10 Rp";
        if (radii < 20) return "c: 10-20 Rp";
        if (radii < 40) return "d: 20-40 Rp";
        return "e: 40+ Rp";
    }

    private String binEccentricity(double ecc) {
        if (ecc < 0.005) return "a: <0.005";
        if (ecc < 0.01) return "b: 0.005-0.01";
        if (ecc < 0.05) return "c: 0.01-0.05";
        return "d: 0.05+";
    }

    private String binAxialTilt(double degrees) {
        if (degrees < 5) return "a: 0-5\u00B0";
        if (degrees < 10) return "b: 5-10\u00B0";
        if (degrees < 15) return "c: 10-15\u00B0";
        if (degrees < 25) return "d: 15-25\u00B0";
        return "e: 25\u00B0+";
    }

    private String binTidalRange(double meters) {
        if (meters < 0.1) return "a: <0.1m (negligible)";
        if (meters < 1.0) return "b: 0.1-1m (Earth-like)";
        if (meters < 5.0) return "c: 1-5m (moderate)";
        if (meters < 20.0) return "d: 5-20m (strong)";
        if (meters < 100.0) return "e: 20-100m (extreme)";
        return "f: 100m+ (catastrophic)";
    }
}
