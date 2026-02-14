package com.brickroad.starcreator_webservice.probabilityreport;

import com.brickroad.starcreator_webservice.entity.ud.*;
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

    private int moonsWithWeather = 0;
    private final Map<String, Integer> moonWeatherSeverity = new HashMap<>();
    private final Map<String, Integer> moonWeatherExposure = new HashMap<>();
    private final Map<String, Integer> moonWeatherSkyColor = new HashMap<>();
    private final Map<String, Integer> moonWeatherCloudClass = new HashMap<>();
    private final Map<String, Integer> moonWeatherWindIntensity = new HashMap<>();
    private final Map<String, Integer> moonWeatherTidalRangeBins = new HashMap<>();
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

        analyzeMoonWeatherData(moon);
    }

    private void analyzeMoonWeatherData(Moon moon) {
        PlanetaryWeather w = moon.getWeather();
        if (w == null) return;

        moonsWithWeather++;

        String skyColor = w.getSkyColor() != null ? w.getSkyColor() : "NULL";
        moonWeatherSkyColor.merge(skyColor, 1, Integer::sum);

        String cloudClass = w.getCloudCoverageClass() != null ? w.getCloudCoverageClass() : "NULL";
        moonWeatherCloudClass.merge(cloudClass, 1, Integer::sum);

        String windIntensity = w.getWindIntensity() != null ? w.getWindIntensity() : "NULL";
        moonWeatherWindIntensity.merge(windIntensity, 1, Integer::sum);

        String severity = w.getWeatherSeverity() != null ? w.getWeatherSeverity() : "NULL";
        moonWeatherSeverity.merge(severity, 1, Integer::sum);

        String exposure = w.getOutdoorExposureRating() != null ? w.getOutdoorExposureRating() : "NULL";
        moonWeatherExposure.merge(exposure, 1, Integer::sum);

        if (Boolean.TRUE.equals(w.getHasPrecipitation())) moonsWithPrecipitation++;
        if (Boolean.TRUE.equals(w.getHasLightning())) moonsWithLightning++;

        if (w.getTidalRangeMeters() != null) {
            String tidalBin = binTidalRange(w.getTidalRangeMeters());
            moonWeatherTidalRangeBins.merge(tidalBin, 1, Integer::sum);
        }

        if (w.getExtremeWeatherEvents() != null) {
            moonExtremeEventTotal += w.getExtremeWeatherEvents().size();
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

    private String binTidalRange(double meters) {
        if (meters < 0.1) return "a: <0.1m (negligible)";
        if (meters < 1.0) return "b: 0.1-1m (Earth-like)";
        if (meters < 5.0) return "c: 1-5m (moderate)";
        if (meters < 20.0) return "d: 5-20m (strong)";
        if (meters < 100.0) return "e: 20-100m (extreme)";
        return "f: 100m+ (catastrophic)";
    }
}
