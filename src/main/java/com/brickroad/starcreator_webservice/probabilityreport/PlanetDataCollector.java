package com.brickroad.starcreator_webservice.probabilityreport;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.enums.BinaryConfiguration;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class PlanetDataCollector {

    private final Map<String, Integer> planetTypes = new HashMap<>();
    private final Map<String, Integer> atmosphereClassifications = new HashMap<>();
    private final Map<String, Integer> protectionLevels = new HashMap<>();
    private final Map<String, Integer> auroralFrequencies = new HashMap<>();
    private final Map<String, Integer> auroralIntensities = new HashMap<>();
    private final Map<String, Integer> tidalLockCounts = new HashMap<>();
    private final Map<String, Integer> hzPositions = new HashMap<>();

    private final Map<String, Integer> magnetopauseBins = new HashMap<>();
    private final Map<String, Integer> atmLossRateBins = new HashMap<>();
    private final Map<String, Integer> beltIntensityInner = new HashMap<>();
    private final Map<String, Integer> beltIntensityOuter = new HashMap<>();

    private final Map<String, int[]> activityVsAtmosphere = new HashMap<>();
    private final Map<String, int[]> activityVsProtection = new HashMap<>();
    private final Map<String, double[]> distanceVsMagnetopause = new HashMap<>();

    private final Map<String, Integer> waterInventories = new HashMap<>();
    private final Map<String, Integer> waterPhases = new HashMap<>();
    private int planetsWithLiquidWater = 0;
    private int planetsWithIce = 0;
    private int planetsWithSubsurfaceWater = 0;
    private int totalRockyPlanets = 0;

    private final Map<String, Integer> habitabilityClasses = new HashMap<>();
    private final Map<String, Integer> colonizationSuitabilities = new HashMap<>();
    private final Map<String, Integer> terraformingPotentials = new HashMap<>();
    private final Map<String, Integer> biosignaturePotentials = new HashMap<>();
    private final Map<String, Integer> lifeComplexityPotentials = new HashMap<>();
    private double esiSum = 0.0;
    private double habScoreSum = 0.0;
    private int habAssessmentCount = 0;
    private int breathablePlanets = 0;

    private int planetsWithRings = 0;
    private final Map<String, Integer> compositionClasses = new HashMap<>();
    private final Map<String, Integer> surfaceTempBins = new HashMap<>();
    private final Map<String, Integer> geologicalActivity = new HashMap<>();
    private final Map<String, Integer> tectonicLevels = new HashMap<>();
    private final Map<String, Integer> volcanismTypes = new HashMap<>();
    private final Map<String, PlanetTypeBreakdown> perTypeData = new HashMap<>();
    private final Map<String, PlanetTypeBreakdown> perTypeDataPType = new HashMap<>();
    private final Map<String, PlanetTypeBreakdown> perTypeDataTrinary = new HashMap<>();
    private double totalMass = 0;
    private double totalRadius = 0;
    private double totalGravity = 0;
    private int physicalPropsCount = 0;
    private int planetsWithGeology = 0;

    // Weather
    private final WeatherBucket weatherAll = new WeatherBucket();
    private final WeatherBucket weatherSurface = new WeatherBucket();
    private final WeatherBucket weatherGas = new WeatherBucket();

    // References to other collectors for delegation
    private final MoonDataCollector moonDataCollector;
    private final RingDataCollector ringDataCollector;

    public PlanetDataCollector(MoonDataCollector moonDataCollector, RingDataCollector ringDataCollector) {
        this.moonDataCollector = moonDataCollector;
        this.ringDataCollector = ringDataCollector;
    }

    public void analyzeData(Planet planet, ProbabilityCounts counts) {
        planetTypes.merge(planet.getPlanetType(), 1, Integer::sum);

        // Per-type breakdown — route to P-type or single-star map
        Map<String, PlanetTypeBreakdown> targetPerTypeMap = getPerTypeMapFor(planet);
        PlanetTypeBreakdown typeData = targetPerTypeMap.computeIfAbsent(planet.getPlanetType(), k -> new PlanetTypeBreakdown());
        typeData.increment();
        typeData.addCompositionClass(planet.getCompositionClassification() != null ? planet.getCompositionClassification() : "NULL");
        if (planet.getSurfaceTemp() != null) {
            typeData.addSurfaceTempBin(binTemperature(planet.getSurfaceTemp()));
        }
        typeData.addAtmosphereClass(planet.getAtmosphereClassification() != null ? planet.getAtmosphereClassification() : "NULL");
        typeData.addHzPosition(planet.getHabitableZonePosition() != null ? planet.getHabitableZonePosition() : "unknown");
        if (Boolean.TRUE.equals(planet.getTidallyLocked())) typeData.addTidallyLocked();
        if (Boolean.TRUE.equals(planet.getHasRings())) typeData.addRings();
        if (planet.getEarthMass() != null) {
            typeData.addMassBin(binMass(planet.getEarthMass()));
            typeData.addPhysicalProps(
                    planet.getEarthMass(),
                    planet.getEarthRadius() != null ? planet.getEarthRadius() : 0,
                    planet.getSurfaceGravity() != null ? planet.getSurfaceGravity() : 0,
                    planet.getSurfaceTemp() != null ? planet.getSurfaceTemp() : 0);
        }
        typeData.addMoonCountBin(binMoonCount(planet.getMoons().size()));
        if (planet.getAdditionalMoonlets() != null && planet.getAdditionalMoonlets() > 0) {
            typeData.addMoonletBin(binMoonletCount(planet.getAdditionalMoonlets()));
            counts.incrementMoonletCount(planet.getAdditionalMoonlets());
        }
        if (planet.getSemiMajorAxisAU() != null) {
            typeData.addSemiMajorAxisBin(binDistance(planet.getSemiMajorAxisAU()));
        }

        // Composition classification
        String compClass = planet.getCompositionClassification() != null ? planet.getCompositionClassification() : "NULL";
        compositionClasses.merge(compClass, 1, Integer::sum);

        // Rings
        if (Boolean.TRUE.equals(planet.getHasRings())) planetsWithRings++;

        // Geology (surface/rocky planets only)
        if (isSurfaceType(planet.getPlanetType()) && planet.getGeologicalActivity() != null) {
            planetsWithGeology++;
            geologicalActivity.merge(planet.getGeologicalActivity(), 1, Integer::sum);
            typeData.addGeologicalActivity(planet.getGeologicalActivity());
            if (planet.getTectonicActivityLevel() != null) {
                tectonicLevels.merge(planet.getTectonicActivityLevel(), 1, Integer::sum);
            }
            if (planet.getVolcanismType() != null) {
                volcanismTypes.merge(planet.getVolcanismType(), 1, Integer::sum);
            }
        }

        // Surface temperature bins
        if (planet.getSurfaceTemp() != null) {
            String tempBin = binTemperature(planet.getSurfaceTemp());
            surfaceTempBins.merge(tempBin, 1, Integer::sum);
        }

        // Physical property averages
        if (planet.getEarthMass() != null) {
            totalMass += planet.getEarthMass();
            totalRadius += planet.getEarthRadius() != null ? planet.getEarthRadius() : 0;
            totalGravity += planet.getSurfaceGravity() != null ? planet.getSurfaceGravity() : 0;
            physicalPropsCount++;
        }

        // Atmosphere classification
        String atmClass = planet.getAtmosphereClassification() != null ?
                planet.getAtmosphereClassification() : "NULL";
        atmosphereClassifications.merge(atmClass, 1, Integer::sum);

        // Habitable zone position
        String hzPos = planet.getHabitableZonePosition() != null ? planet.getHabitableZonePosition() : "unknown";
        hzPositions.merge(hzPos, 1, Integer::sum);

        // Tidal locking
        String lockStatus = Boolean.TRUE.equals(planet.getTidallyLocked()) ? "LOCKED" : "NOT_LOCKED";
        tidalLockCounts.merge(lockStatus, 1, Integer::sum);

        // Magnetic field data
        PlanetaryMagneticField mf = planet.getMagneticField();
        if (mf != null) {
            String protection = mf.getProtectionLevel() != null ? mf.getProtectionLevel().name() : "NULL";
            protectionLevels.merge(protection, 1, Integer::sum);
            typeData.addProtectionLevel(protection);

            String auroraFreq = mf.getAuroralFrequency() != null ? mf.getAuroralFrequency().name() : "NONE";
            auroralFrequencies.merge(auroraFreq, 1, Integer::sum);

            String auroraInt = mf.getAuroralIntensity() != null ? mf.getAuroralIntensity().name() : "NONE";
            auroralIntensities.merge(auroraInt, 1, Integer::sum);

            String innerBelt = mf.getInnerBeltIntensity() != null ? mf.getInnerBeltIntensity().name() : "NULL";
            beltIntensityInner.merge(innerBelt, 1, Integer::sum);
            String outerBelt = mf.getOuterBeltIntensity() != null ? mf.getOuterBeltIntensity().name() : "NULL";
            beltIntensityOuter.merge(outerBelt, 1, Integer::sum);

            if (mf.getMagnetopauseDistancePlanetRadii() != null) {
                String mpBin = binMagnetopause(mf.getMagnetopauseDistancePlanetRadii());
                magnetopauseBins.merge(mpBin, 1, Integer::sum);
            }

            if (mf.getAtmosphericLossRateFactor() != null) {
                String lossBin = binLossRate(mf.getAtmosphericLossRateFactor());
                atmLossRateBins.merge(lossBin, 1, Integer::sum);
            }

            if (planet.getSemiMajorAxisAU() != null && mf.getMagnetopauseDistancePlanetRadii() != null) {
                String distBin = binDistance(planet.getSemiMajorAxisAU());
                double[] stats = distanceVsMagnetopause.getOrDefault(distBin, new double[]{0, 0});
                stats[0] += mf.getMagnetopauseDistancePlanetRadii();
                stats[1] += 1;
                distanceVsMagnetopause.put(distBin, stats);
            }
        }

        // Cross-reference: star activity vs atmosphere stripping (rocky planets only)
        Star parentStar = planet.getParentStar();
        if (parentStar != null && isRockyTypeWithAtmospherePotential(planet.getPlanetType())) {
            String actLevel = parentStar.getActivityLevel() != null ?
                    parentStar.getActivityLevel() : "UNKNOWN";

            int[] atmCounts = activityVsAtmosphere.getOrDefault(actLevel, new int[]{0, 0});
            atmCounts[0]++;
            if ("NONE".equals(planet.getAtmosphereClassification())) {
                atmCounts[1]++;
            }
            activityVsAtmosphere.put(actLevel, atmCounts);

            if (mf != null && mf.getProtectionLevel() != null) {
                int[] protCounts = activityVsProtection.getOrDefault(actLevel, new int[]{0, 0, 0, 0, 0, 0});
                protCounts[0]++;
                switch (mf.getProtectionLevel()) {
                    case NONE -> protCounts[1]++;
                    case MINIMAL -> protCounts[2]++;
                    case MODERATE -> protCounts[3]++;
                    case STRONG -> protCounts[4]++;
                    case EXCEPTIONAL -> protCounts[5]++;
                }
                activityVsProtection.put(actLevel, protCounts);
            }
        }

        // Moon/Ring tracking
        counts.incrementMoonCount(planet.getMoons().size());
        for (Moon moon : planet.getMoons()) {
            moonDataCollector.analyzeData(moon);
        }

        counts.incrementRingCount(planet.getRings().size());
        for (Ring ring : planet.getRings()) {
            ringDataCollector.analyzeData(ring);
        }

        analyzeWaterData(planet);
        analyzeHabitabilityData(planet);
        analyzeWeatherData(planet);
    }

    private void analyzeWaterData(Planet planet) {
        String type = planet.getPlanetType();
        if (type == null) return;

        boolean isRocky = type.contains("Terrestrial") || type.contains("Super-Earth")
                || type.contains("Desert") || type.contains("Ocean")
                || type.contains("Iron") || type.contains("Carbon")
                || type.contains("Lava") || type.contains("Hot Rocky")
                || type.contains("Ice World") || type.contains("Dwarf");

        if (!isRocky) return;
        totalRockyPlanets++;

        String inventory = planet.getWaterInventory() != null ?
                planet.getWaterInventory() : "NULL";
        waterInventories.merge(inventory, 1, Integer::sum);
        PlanetTypeBreakdown typeData = getPerTypeMapFor(planet).get(planet.getPlanetType());
        if (typeData != null) typeData.addWaterInventory(inventory);

        Double liquidPct = planet.getLiquidWaterCoveragePercent();
        Double icePct = planet.getIceCoveragePercent();

        if (liquidPct != null && liquidPct > 0.1) planetsWithLiquidWater++;
        if (icePct != null && icePct > 0.1) planetsWithIce++;
        if (Boolean.TRUE.equals(planet.getHasSubsurfaceWater())) planetsWithSubsurfaceWater++;
    }

    private void analyzeHabitabilityData(Planet planet) {
        PlanetaryHabitability hab = planet.getHabitability();
        if (hab == null) return;

        habAssessmentCount++;

        String habClass = hab.getHabitabilityClass() != null ? hab.getHabitabilityClass().name() : "NULL";
        habitabilityClasses.merge(habClass, 1, Integer::sum);

        PlanetTypeBreakdown typeData = getPerTypeMapFor(planet).get(planet.getPlanetType());
        if (typeData != null) typeData.addHabitabilityClass(habClass);

        String colSuit = hab.getColonizationSuitability() != null ? hab.getColonizationSuitability().name() : "NULL";
        colonizationSuitabilities.merge(colSuit, 1, Integer::sum);

        String terrPot = hab.getTerraformingPotential() != null ? hab.getTerraformingPotential().name() : "NULL";
        terraformingPotentials.merge(terrPot, 1, Integer::sum);

        String bioPot = hab.getBiosignaturePotential() != null ? hab.getBiosignaturePotential() : "NONE";
        biosignaturePotentials.merge(bioPot, 1, Integer::sum);

        String lifePot = hab.getLifeComplexityPotential() != null ? hab.getLifeComplexityPotential() : "NONE";
        lifeComplexityPotentials.merge(lifePot, 1, Integer::sum);

        if (hab.getEsiTotal() != null) esiSum += hab.getEsiTotal();
        if (hab.getHabitabilityScore() != null) habScoreSum += hab.getHabitabilityScore();
        if (Boolean.TRUE.equals(hab.getIsBreathable())) breathablePlanets++;

        String waterPhase = hab.getWaterPhaseAtSurface() != null ? hab.getWaterPhaseAtSurface() : "NULL";
        waterPhases.merge(waterPhase, 1, Integer::sum);
    }

    private BinaryConfiguration getBinaryConfig(Planet planet) {
        Star parentStar = planet.getParentStar();
        if (parentStar == null) return null;
        StarSystem system = parentStar.getSystem();
        if (system == null) return null;
        return system.getBinaryConfiguration();
    }

    private Map<String, PlanetTypeBreakdown> getPerTypeMapFor(Planet planet) {
        BinaryConfiguration config = getBinaryConfig(planet);
        if (config == null) return perTypeData;
        return switch (config) {
            case P_TYPE -> perTypeDataPType;
            case HIERARCHICAL_BINARY_THIRD, HIERARCHICAL_TRIPLE -> perTypeDataTrinary;
            default -> perTypeData;
        };
    }

    private boolean isGasType(String planetType) {
        if (planetType == null) return false;
        return planetType.contains("Gas Giant") || planetType.contains("Ice Giant")
                || planetType.contains("Jupiter") || planetType.contains("Neptune")
                || planetType.contains("Sub-Neptune") || planetType.contains("Mini-Neptune")
                || planetType.contains("Puffy");
    }

    private void analyzeWeatherData(Planet planet) {
        PlanetaryWeather w = planet.getWeather();
        if (w == null) return;

        WeatherBucket split = isGasType(planet.getPlanetType()) ? weatherGas : weatherSurface;

        for (WeatherBucket bucket : new WeatherBucket[]{weatherAll, split}) {
            bucket.count++;

            String skyColor = w.getSkyColor() != null ? w.getSkyColor() : "NULL";
            bucket.skyColors.merge(skyColor, 1, Integer::sum);

            String cloudClass = w.getCloudCoverageClass() != null ? w.getCloudCoverageClass() : "NULL";
            bucket.cloudCoverageClass.merge(cloudClass, 1, Integer::sum);

            if (w.getCloudCoveragePercent() != null) bucket.totalCloudCoverage += w.getCloudCoveragePercent();

            String wind = w.getWindIntensity() != null ? w.getWindIntensity() : "NULL";
            bucket.windIntensity.merge(wind, 1, Integer::sum);

            if (w.getMeanSurfaceWindSpeedMs() != null) {
                bucket.totalWindSpeed += w.getMeanSurfaceWindSpeedMs();
                bucket.windSpeedCount++;
            }

            String circulation = w.getCirculationPattern() != null ? w.getCirculationPattern() : "NULL";
            bucket.circulationPattern.merge(circulation, 1, Integer::sum);

            if (Boolean.TRUE.equals(w.getHasSuperRotation())) bucket.withSuperRotation++;

            String stormFreq = w.getStormFrequency() != null ? w.getStormFrequency() : "NULL";
            bucket.stormFrequency.merge(stormFreq, 1, Integer::sum);

            if (Boolean.TRUE.equals(w.getHasDustStorms())) bucket.withDustStorms++;
            if (Boolean.TRUE.equals(w.getHasLightning())) {
                bucket.withLightning++;
                String lt = w.getLightningType() != null ? w.getLightningType() : "UNKNOWN";
                bucket.lightningType.merge(lt, 1, Integer::sum);
            }

            if (Boolean.TRUE.equals(w.getHasPrecipitation())) bucket.withPrecipitation++;

            String sev = w.getWeatherSeverity() != null ? w.getWeatherSeverity() : "NULL";
            bucket.severity.merge(sev, 1, Integer::sum);

            String exp = w.getOutdoorExposureRating() != null ? w.getOutdoorExposureRating() : "NULL";
            bucket.exposureRating.merge(exp, 1, Integer::sum);

            if (w.getTidalRangeMeters() != null) {
                bucket.tidalRangeBins.merge(binTidalRange(w.getTidalRangeMeters()), 1, Integer::sum);
            }

            if (Boolean.TRUE.equals(w.getHasGreatDarkSpot())) bucket.withGreatDarkSpot++;

            if (w.getCloudLayers() != null) bucket.cloudLayerTotal += w.getCloudLayers().size();
            if (w.getPrecipitationTypes() != null) bucket.precipTypeTotal += w.getPrecipitationTypes().size();
            if (w.getExtremeWeatherEvents() != null) bucket.extremeEventTotal += w.getExtremeWeatherEvents().size();
            if (w.getEclipseData() != null) bucket.eclipseTotal += w.getEclipseData().size();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Bin Methods
    // ═══════════════════════════════════════════════════════════════

    private String binTidalRange(double meters) {
        if (meters < 0.1) return "a: <0.1m (negligible)";
        if (meters < 1.0) return "b: 0.1-1m (Earth-like)";
        if (meters < 5.0) return "c: 1-5m (moderate)";
        if (meters < 20.0) return "d: 5-20m (strong)";
        if (meters < 100.0) return "e: 20-100m (extreme)";
        return "f: 100m+ (catastrophic)";
    }

    private String binMass(double earthMass) {
        if (earthMass < 0.01) return "01: <0.01 (asteroid-class)";
        if (earthMass < 0.1) return "02: 0.01-0.1 (sub-dwarf)";
        if (earthMass < 0.5) return "03: 0.1-0.5 (sub-Earth)";
        if (earthMass < 2.0) return "04: 0.5-2.0 (Earth-class)";
        if (earthMass < 10) return "05: 2-10 (super-Earth)";
        if (earthMass < 50) return "06: 10-50 (Neptune-class)";
        if (earthMass < 300) return "07: 50-300 (sub-Jupiter)";
        if (earthMass < 1000) return "08: 300-1000 (Jupiter-class)";
        return "09: 1000+ (super-Jupiter)";
    }

    private String binMoonCount(int count) {
        if (count == 0) return "0";
        if (count <= 2) return "1-2";
        if (count <= 5) return "3-5";
        if (count <= 10) return "6-10";
        if (count <= 25) return "11-25";
        if (count <= 50) return "26-50";
        return "51+";
    }

    private String binMoonletCount(int count) {
        if (count <= 5) return "1-5";
        if (count <= 10) return "6-10";
        if (count <= 25) return "11-25";
        if (count <= 50) return "26-50";
        if (count <= 100) return "51-100";
        if (count <= 250) return "101-250";
        return "251+";
    }

    private boolean isSurfaceType(String planetType) {
        if (planetType == null) return false;
        return planetType.contains("Terrestrial") || planetType.contains("Super-Earth")
                || planetType.contains("Rocky") || planetType.contains("Desert")
                || planetType.contains("Ocean") || planetType.contains("Lava")
                || planetType.contains("Carbon") || planetType.contains("Iron")
                || planetType.contains("Ice World") || planetType.contains("Dwarf")
                || planetType.contains("Hot Rocky");
    }

    private String binMagnetopause(double radii) {
        if (radii < 3) return "a: <3 (extreme compression)";
        if (radii < 6) return "b: 3-6 (heavy compression)";
        if (radii < 10) return "c: 6-10 (moderate compression)";
        if (radii < 20) return "d: 10-20 (Earth-like)";
        if (radii < 40) return "e: 20-40 (expanded)";
        if (radii < 70) return "f: 40-70 (large)";
        return "g: 70+ (massive)";
    }

    private String binLossRate(double rate) {
        if (rate < 0.3) return "a: <0.3 (very low)";
        if (rate < 1.0) return "b: 0.3-1.0 (low)";
        if (rate < 3.0) return "c: 1.0-3.0 (moderate)";
        if (rate < 8.0) return "d: 3.0-8.0 (high)";
        if (rate < 15.0) return "e: 8.0-15.0 (very high)";
        return "f: 15+ (extreme)";
    }

    private String binDistance(double distanceAU) {
        if (distanceAU < 0.1) return "a: <0.1 AU";
        if (distanceAU < 0.3) return "b: 0.1-0.3 AU";
        if (distanceAU < 1.0) return "c: 0.3-1.0 AU";
        if (distanceAU < 3.0) return "d: 1.0-3.0 AU";
        if (distanceAU < 10.0) return "e: 3.0-10.0 AU";
        return "f: 10+ AU";
    }

    private String binTemperature(double tempK) {
        if (tempK < 50) return "01: <50K (ultra-cold)";
        if (tempK < 150) return "02: 50-150K (cryogenic)";
        if (tempK < 273) return "03: 150-273K (sub-freezing)";
        if (tempK < 373) return "04: 273-373K (liquid water)";
        if (tempK < 700) return "05: 373-700K (hot)";
        if (tempK < 1500) return "06: 700-1500K (very hot)";
        return "07: 1500K+ (extreme)";
    }

    private boolean isRockyTypeWithAtmospherePotential(String planetType) {
        if (planetType == null) return false;
        return planetType.contains("Terrestrial") || planetType.contains("Super-Earth")
                || planetType.contains("Desert") || planetType.contains("Ocean")
                || planetType.contains("Lava");
    }

    // ═══════════════════════════════════════════════════════════════
    //  WeatherBucket inner class
    // ═══════════════════════════════════════════════════════════════

    @Getter
    public static class WeatherBucket {
        int count = 0;
        final Map<String, Integer> skyColors = new HashMap<>();
        final Map<String, Integer> cloudCoverageClass = new HashMap<>();
        final Map<String, Integer> windIntensity = new HashMap<>();
        final Map<String, Integer> stormFrequency = new HashMap<>();
        final Map<String, Integer> severity = new HashMap<>();
        final Map<String, Integer> exposureRating = new HashMap<>();
        final Map<String, Integer> circulationPattern = new HashMap<>();
        final Map<String, Integer> lightningType = new HashMap<>();
        final Map<String, Integer> tidalRangeBins = new HashMap<>();
        int withPrecipitation = 0;
        int withDustStorms = 0;
        int withLightning = 0;
        int withSuperRotation = 0;
        int withGreatDarkSpot = 0;
        double totalCloudCoverage = 0;
        double totalWindSpeed = 0;
        int windSpeedCount = 0;
        int cloudLayerTotal = 0;
        int precipTypeTotal = 0;
        int extremeEventTotal = 0;
        int eclipseTotal = 0;
    }
}
