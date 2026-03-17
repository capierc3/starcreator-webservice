package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ref.GeologicalFeatureRef;
import com.brickroad.starcreator_webservice.entity.ref.GeologicalTemplateRef;
import com.brickroad.starcreator_webservice.entity.ud.Moon;
import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.TerrainProperties;
// PlanetaryGeology is a static inner class at the bottom of this file
import com.brickroad.starcreator_webservice.repository.GeologicalTemplateRepository;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GeologyCreator {

    @Autowired
    private GeologicalTemplateRepository geologicalTemplateRepository;

    public TerrainProperties createPlanetTerrain(Planet planet) {
        PlanetaryGeology geology = generateGeology(planet);
        TerrainProperties terrain = new TerrainProperties();

        terrain.setGeologicalActivity(geology.getActivityLevel());
        terrain.setActivityScore(geology.getActivityScore());
        terrain.setHasPlateTectonics(geology.getHasPlateTectonics());
        terrain.setNumberOfTectonicPlates(geology.getNumberOfTectonicPlates());
        terrain.setTectonicActivityLevel(geology.getTectonicActivityLevel());
        terrain.setHasVolcanicActivity(geology.getHasVolcanicActivity());
        terrain.setVolcanismType(geology.getVolcanismType());
        terrain.setEstimatedActiveVolcanoes(geology.getEstimatedActiveVolcanoes());
        terrain.setVolcanicIntensity(geology.getVolcanicIntensity());
        terrain.setMountainCoveragePercent(geology.getMountainCoveragePercent());
        terrain.setAverageElevationKm(geology.getAverageElevationKm());
        terrain.setMaxElevationKm(geology.getMaxElevationKm());
        terrain.setMinElevationKm(geology.getMinElevationKm());
        terrain.setTerrainRoughness(geology.getTerrainRoughness());
        terrain.setCrateringLevel(geology.getCrateringLevel());
        terrain.setEstimatedVisibleCraters(geology.getEstimatedVisibleCraters());
        terrain.setErosionLevel(geology.getErosionLevel());
        terrain.setPrimaryErosionAgent(geology.getPrimaryErosionAgent());

        // Adjust crater count for environmental factors the template doesn't consider:
        // atmosphere (ablates impactors), erosion (degrades craters), water (submerges craters)
        if (!isGasGiant(planet.getPlanetType())) {
            adjustCrateringForEnvironment(terrain, planet);
        }

        // Storm fields stay on Planet directly
        planet.setHasGreatStorm(geology.getHasGreatStorm());
        planet.setNumberOfMajorStorms(geology.getNumberOfMajorStorms());
        planet.setAtmosphericConvectionLevel(geology.getAtmosphericConvectionLevel());

        terrain.setLabel("Planet terrain");
        return terrain;
    }

    /**
     * Adjusts the template/hardcoded crater count for environmental factors that
     * erase, degrade, or hide impact craters over geological time.
     *
     * Templates set cratering based on geological activity alone (mass/age ratio),
     * which determines how fast the surface is resurfaced by tectonics/volcanism.
     * But three additional factors are equally important:
     *
     * 1. Atmospheric shielding — thick atmospheres ablate small impactors before
     *    they reach the surface. Mars (0.006 bar) has heavy cratering; Earth (1 bar)
     *    has ~190 confirmed craters; Venus (90 bar) has only ~1000 despite no tectonics.
     *
     * 2. Surface erosion — wind and water degrade crater rims and fill basins.
     *    On Earth, most craters are invisible within tens of millions of years.
     *
     * 3. Water coverage — craters under oceans or ice sheets aren't visible.
     *    Only exposed continental craters count as "visible."
     *
     * Reference: Earth has activity score ~0.22 (same "Heavy" template range),
     * yet has only ~190 confirmed craters. This method brings the numbers in line.
     */
    private void adjustCrateringForEnvironment(TerrainProperties terrain, Planet planet) {
        Integer baseCraters = terrain.getEstimatedVisibleCraters();
        if (baseCraters == null || baseCraters <= 0) return;

        double adjustedCraters = baseCraters;

        // ── 1. Atmospheric shielding ──
        // Formula: factor = 1 / (1 + pressure × 3)
        // Produces a smooth curve that matches observed solar system cratering:
        //   0.006 bar (Mars)   → 0.98  (almost no protection)
        //   0.1 bar            → 0.77  (thin atmosphere, moderate shielding)
        //   1.0 bar (Earth)    → 0.25  (strong shielding, small impactors burn up)
        //   5.0 bar            → 0.06  (very few craters form)
        //   90 bar (Venus)     → 0.004 (essentially only the largest impactors survive)
        Double pressure = planet.getSurfacePressure();
        if (pressure != null && pressure > 0.01) {
            double atmFactor = 1.0 / (1.0 + pressure * 3.0);
            adjustedCraters *= atmFactor;
        }

        // ── 2. Erosion ──
        // Active erosion agents degrade crater rims and fill basins over time.
        // Wind is less effective than water; volcanic resurfacing is the strongest.
        String erosionLevel = terrain.getErosionLevel();
        if (erosionLevel != null) {
            double erosionFactor = switch (erosionLevel) {
                case "Extreme" -> 0.10;
                case "Heavy"   -> 0.25;
                case "Moderate" -> 0.45;
                case "Light"   -> 0.65;
                case "Minimal" -> 0.85;
                default        -> 1.0;  // "None" or unrecognized
            };
            adjustedCraters *= erosionFactor;
        }

        // Note: water coverage adjustment is handled separately in PlanetCreator
        // after water properties are set (terrain is generated before water).

        int finalCraters = Math.max(0, (int) Math.round(adjustedCraters));

        // Reclassify cratering level based on adjusted count
        String adjustedLevel;
        if (finalCraters < 50) {
            adjustedLevel = "Pristine";
        } else if (finalCraters < 500) {
            adjustedLevel = "Light";
        } else if (finalCraters < 5_000) {
            adjustedLevel = "Moderate";
        } else if (finalCraters < 50_000) {
            adjustedLevel = "Heavy";
        } else if (finalCraters < 500_000) {
            adjustedLevel = "Extreme";
        } else {
            adjustedLevel = "Saturated";
        }

        terrain.setEstimatedVisibleCraters(finalCraters);
        terrain.setCrateringLevel(adjustedLevel);
    }

    public TerrainProperties createMoonTerrain(Moon moon, String geologicalActivity, Boolean hasCryovolcanism) {
        TerrainProperties terrain = new TerrainProperties();
        String activityLevel = mapMoonActivityToTemplateLevel(geologicalActivity);

        terrain.setGeologicalActivity(geologicalActivity);
        terrain.setHasCryovolcanism(hasCryovolcanism);

        List<GeologicalTemplateRef> templates = geologicalTemplateRepository
                .findByPlanetTypeAndActivityLevel(moon.getMoonType(), activityLevel, moon.getCompositionType());
        GeologicalTemplateRef template = selectGeologicalTemplate(templates);
        if (template != null) {
            applyMoonTemplate(terrain, moon, template);
        } else {
            applyBasicMoonGeology(terrain, moon);
        }

        generateMoonSurfaceFeatures(terrain, moon);

        terrain.setLabel("Moon terrain");
        return terrain;
    }

    public PlanetaryGeology generateGeology(Planet planet) {
        double earthMass = planet.getEarthMass();
        double age = (planet.getAgeMY() != null ? planet.getAgeMY() : 4500.0) / 1000.0;
        double activityScore = earthMass / Math.max(age, 0.1);

        String planetType = planet.getPlanetType();
        boolean isGasGiant = isGasGiant(planetType);

        List<GeologicalTemplateRef> templates = geologicalTemplateRepository
                .findByPlanetTypeAndActivityScore(planetType, activityScore);

        GeologicalTemplateRef template = selectGeologicalTemplate(templates);

        PlanetaryGeology geology;
        if (template == null) {
            geology = generateBasicGeology(activityScore, isGasGiant);
        } else {
            geology = generateGeologyFromTemplate(template, planet, activityScore);
        }
        return geology;
    }

    private GeologicalTemplateRef selectGeologicalTemplate(List<GeologicalTemplateRef> templates) {
        if (templates.isEmpty()) {
            return null;
        }

        int totalWeight = templates.stream()
                .mapToInt(GeologicalTemplateRef::getRarityWeight)
                .sum();

        int random = RandomUtils.rollRange(0, totalWeight);
        int currentWeight = 0;

        for (GeologicalTemplateRef template : templates) {
            currentWeight += template.getRarityWeight();
            if (random < currentWeight) {
                return template;
            }
        }

        return templates.getFirst();
    }

    private PlanetaryGeology generateGeologyFromTemplate(GeologicalTemplateRef template, Planet planet, double activityScore) {
        PlanetaryGeology.Builder builder = new PlanetaryGeology.Builder()
                .activityLevel(template.getActivityLevel())
                .activityScore(activityScore);

        Double mountainCoverage = null;
        Double maxElev = null;
        double minElev;
        Double roughness = null;
        String crateringLevel = null;
        Integer craterCount = null;
        String erosionLevel = null;
        String erosionAgent = null;
        Boolean hasTectonics = null;
        String tectonicLevel = null;
        Integer tectonicPlates = null;
        Boolean hasVolcanism = null;
        String volcanismType = null;
        String volcanicIntensity = null;
        Integer activeVolcanoes = null;
        Boolean hasGreatStorm = null;
        Integer majorStorms = null;
        String convectionLevel = null;

        for (GeologicalFeatureRef feature : template.getFeatures()) {
            String type = feature.getFeatureType();
            String value = feature.getFeatureValue();
            Double minVal = feature.getMinValue();
            Double maxVal = feature.getMaxValue();

            switch (type) {
                case "TECTONICS":
                    // Only "Active" and "Hyperactive" represent true plate tectonics.
                    // "Stagnant Lid", "Molten", "Ice Shell" are tectonic regimes
                    // without plate tectonics (no subduction/spreading ridges).
                    hasTectonics = value.equals("Active") || value.equals("Hyperactive");
                    tectonicLevel = value;
                    break;
                case "PLATE_TECTONICS":
                    if (value.equals("true")) hasTectonics = true;
                    break;
                case "TECTONIC_PLATES":
                    if (minVal != null && maxVal != null) {
                        tectonicPlates = RandomUtils.rollRange(minVal.intValue(), maxVal.intValue());
                    }
                    break;
                case "VOLCANISM_TYPE":
                    volcanismType = value;
                    hasVolcanism = !value.equals("None");
                    break;
                case "VOLCANIC_ACTIVITY":
                    hasVolcanism = value.equals("true");
                    break;
                case "VOLCANIC_INTENSITY":
                    volcanicIntensity = value;
                    break;
                case "ACTIVE_VOLCANOES":
                    if (minVal != null && maxVal != null) {
                        activeVolcanoes = RandomUtils.rollRange(minVal.intValue(), maxVal.intValue());
                    }
                    break;
                case "MOUNTAIN_COVERAGE":
                    if (minVal != null && maxVal != null) {
                        mountainCoverage = RandomUtils.rollRange(minVal, maxVal);
                    }
                    break;
                case "MAX_ELEVATION":
                    if (minVal != null && maxVal != null) {
                        maxElev = RandomUtils.rollRange(minVal, maxVal);
                    }
                    break;
                case "TERRAIN_ROUGHNESS":
                    if (minVal != null && maxVal != null) {
                        roughness = RandomUtils.rollRange(minVal, maxVal);
                    }
                    break;
                case "CRATERING_LEVEL":
                    crateringLevel = value;
                    break;
                case "VISIBLE_CRATERS":
                    if (minVal != null && maxVal != null) {
                        craterCount = RandomUtils.rollRange(minVal.intValue(), maxVal.intValue());
                    }
                    break;
                case "EROSION_LEVEL":
                    erosionLevel = value;
                    break;
                case "EROSION_AGENT":
                    erosionAgent = value;
                    break;
                case "ATMOSPHERIC_CONVECTION":
                    convectionLevel = value;
                    break;
                case "GREAT_STORM":
                    if (value.equals("true")) {
                        hasGreatStorm = true;
                    } else if (value.startsWith("random_")) {
                        int chance = Integer.parseInt(value.substring(7));
                        hasGreatStorm = RandomUtils.rollRange(1, 100) < chance;
                    } else {
                        hasGreatStorm = false;
                    }
                    break;
                case "MAJOR_STORMS":
                    if (minVal != null && maxVal != null) {
                        majorStorms = RandomUtils.rollRange(minVal.intValue(), maxVal.intValue());
                    }
                    break;
            }
        }

        if (hasTectonics != null) {
            builder.plateTectonics(hasTectonics, tectonicPlates, tectonicLevel);
        }

        if (hasVolcanism != null) {
            builder.volcanism(hasVolcanism, volcanismType, activeVolcanoes, volcanicIntensity);
        }

        if (mountainCoverage != null || maxElev != null || roughness != null) {
            minElev = -RandomUtils.rollRange(0.5, 3.0);
            Double avgElev = RandomUtils.rollRange(-0.5, 1.5);
            builder.terrain(mountainCoverage, avgElev, maxElev, minElev, roughness);
        }

        if (crateringLevel != null || craterCount != null) {
            builder.cratering(crateringLevel, craterCount);
        }

        if (erosionLevel != null || erosionAgent != null) {
            builder.erosion(erosionLevel, erosionAgent);
        }

        if (hasGreatStorm != null || majorStorms != null || convectionLevel != null) {
            builder.gasGiantStorms(hasGreatStorm, majorStorms, convectionLevel);
        }

        return builder.build();
    }

    private PlanetaryGeology generateBasicGeology(double activityScore, boolean isGasGiant) {
        PlanetaryGeology.Builder builder = new PlanetaryGeology.Builder()
                .activityScore(activityScore);

        if (activityScore > 2.0) {
            builder.activityLevel("Highly Active");
        } else if (activityScore > 0.5) {
            builder.activityLevel("Moderately Active");
        } else if (activityScore > 0.1) {
            builder.activityLevel("Low Activity");
        } else {
            builder.activityLevel("Geologically Dead");
        }

        if (isGasGiant) {
            builder.plateTectonics(false, null, "N/A")
                    .volcanism(false, "Atmospheric", null, null)
                    .gasGiantStorms(false, 0, "Minimal")
                    .terrain(null, null, null, null, 5.0);  // Only roughness
        } else {
            if (activityScore > 2.0) {
                builder.plateTectonics(true, RandomUtils.rollRange(8, 20), "Hyperactive")
                        .volcanism(true, "Silicate", RandomUtils.rollRange(200, 800), "Continuous")
                        .cratering("Pristine", RandomUtils.rollRange(10, 100))
                        .erosion("Heavy", "Volcanic");
            } else if (activityScore > 0.5) {
                builder.plateTectonics(true, RandomUtils.rollRange(5, 15), "Active")
                        .volcanism(true, "Silicate", RandomUtils.rollRange(10, 50), "Moderate")
                        .cratering("Moderate", RandomUtils.rollRange(1000, 10000))
                        .erosion("Moderate", "Wind");
            } else if (activityScore > 0.1) {
                builder.plateTectonics(false, null, "Stagnant Lid")
                        .volcanism(true, "Silicate", RandomUtils.rollRange(1, 10), "Rare")
                        .cratering("Heavy", RandomUtils.rollRange(10000, 100000))
                        .erosion("Minimal", "Wind");
            } else {
                builder.plateTectonics(false, null, "None")
                        .volcanism(false, "None", 0, "None")
                        .cratering("Saturated", RandomUtils.rollRange(100000, 1000000))
                        .erosion("None", "None");
            }
            builder.terrain(null, null, null, null, null);
        }

        return builder.build();
    }

    private boolean isGasGiant(String planetType) {
        return planetType != null && (
                planetType.contains("Gas Giant") ||
                        planetType.contains("Jupiter") ||
                        planetType.contains("Ice Giant") ||
                        planetType.contains("Neptune") ||
                        planetType.contains("Mini-Neptune") ||
                        planetType.contains("Sub-Neptune")
        );
    }

    private String mapMoonActivityToTemplateLevel(String moonActivity) {
        if (moonActivity == null) return "Dead";

        return switch (moonActivity) {
            case "LOW" -> "Low Activity";
            case "MODERATE" -> "Moderately Active";
            case "HIGH" -> "Highly Active";
            default -> "Dead";
        };
    }

    private void applyMoonTemplate(TerrainProperties terrain, Moon moon, GeologicalTemplateRef template) {
        for (GeologicalFeatureRef feature : template.getFeatures()) {
            applyMoonFeature(terrain, feature);
        }
    }

    private void applyBasicMoonGeology(TerrainProperties terrain, Moon moon) {
        String activity = terrain.getGeologicalActivity();
        Boolean hasCryovolcanism = terrain.getHasCryovolcanism();

        if ("HIGH".equals(activity) || "MODERATE".equals(activity)) {
            if (Boolean.TRUE.equals(hasCryovolcanism)) {
                terrain.setVolcanismType("Cryovolcanic");
            } else if ("ICY".equals(moon.getCompositionType())) {
                terrain.setVolcanismType("Cryovolcanic");
            } else {
                terrain.setVolcanismType("Silicate");
            }

            if ("HIGH".equals(activity)) {
                terrain.setVolcanicIntensity("Continuous");
                terrain.setEstimatedActiveVolcanoes(RandomUtils.rollRange(50, 200));
            } else {
                terrain.setVolcanicIntensity("Moderate");
                terrain.setEstimatedActiveVolcanoes(RandomUtils.rollRange(5, 30));
            }
        } else {
            terrain.setVolcanismType("None");
            terrain.setVolcanicIntensity("None");
            terrain.setEstimatedActiveVolcanoes(0);
        }

        if ("HIGH".equals(activity)) {
            terrain.setMountainCoveragePercent(RandomUtils.rollRange(10.0, 25.0));
            terrain.setMaxElevationKm(RandomUtils.rollRange(3.0, 10.0));
            terrain.setTerrainRoughness(RandomUtils.rollRange(5.0, 8.0));
            terrain.setErosionLevel("Heavy");
            terrain.setPrimaryErosionAgent(terrain.getVolcanismType());
        } else if ("MODERATE".equals(activity) || "LOW".equals(activity)) {
            terrain.setMountainCoveragePercent(RandomUtils.rollRange(5.0, 15.0));
            terrain.setMaxElevationKm(RandomUtils.rollRange(1.0, 5.0));
            terrain.setTerrainRoughness(RandomUtils.rollRange(2.0, 4.5));
            terrain.setErosionLevel("Moderate");
            terrain.setPrimaryErosionAgent("Tidal");
        } else {
            terrain.setMountainCoveragePercent(RandomUtils.rollRange(1.0, 5.0));
            terrain.setMaxElevationKm(RandomUtils.rollRange(0.2, 2.0));
            terrain.setTerrainRoughness(RandomUtils.rollRange(0.5, 2.0));
            terrain.setErosionLevel("None");
            terrain.setPrimaryErosionAgent("None");
        }

        if (terrain.getMaxElevationKm() != null) {
            terrain.setAverageElevationKm(terrain.getMaxElevationKm() * 0.15);
            terrain.setMinElevationKm(-terrain.getMaxElevationKm() * 0.4);
        }
    }

    private void applyMoonFeature(TerrainProperties terrain, GeologicalFeatureRef feature) {
        String value = feature.getFeatureValue();
        Double minVal = feature.getMinValue();
        Double maxVal = feature.getMaxValue();

        switch (feature.getFeatureType()) {
            case "VOLCANISM_TYPE":
                terrain.setVolcanismType(value);
                break;

            case "VOLCANIC_INTENSITY":
                terrain.setVolcanicIntensity(value);
                break;

            case "ACTIVE_VOLCANOES":
                if (minVal != null && maxVal != null) {
                    terrain.setEstimatedActiveVolcanoes(
                            RandomUtils.rollRange(minVal.intValue(), maxVal.intValue())
                    );
                }
                break;

            case "MOUNTAIN_COVERAGE":
                if (minVal != null && maxVal != null) {
                    terrain.setMountainCoveragePercent(RandomUtils.rollRange(minVal, maxVal));
                }
                break;

            case "MAX_ELEVATION":
                if (minVal != null && maxVal != null) {
                    double maxElev = RandomUtils.rollRange(minVal, maxVal);
                    terrain.setMaxElevationKm(maxElev);
                    terrain.setAverageElevationKm(maxElev * 0.15);
                    terrain.setMinElevationKm(-maxElev * 0.4);
                }
                break;

            case "TERRAIN_ROUGHNESS":
                if (minVal != null && maxVal != null) {
                    terrain.setTerrainRoughness(RandomUtils.rollRange(minVal, maxVal));
                }
                break;

            case "EROSION_LEVEL":
                terrain.setErosionLevel(value);
                break;

            case "EROSION_AGENT":
                terrain.setPrimaryErosionAgent(value);
                break;
        }
    }

    private void generateMoonSurfaceFeatures(TerrainProperties terrain, Moon moon) {
        StringBuilder features = new StringBuilder();

        if (terrain.getEstimatedActiveVolcanoes() != null && terrain.getEstimatedActiveVolcanoes() > 0) {
            if ("Cryovolcanic".equals(terrain.getVolcanismType())) {
                features.append("Active cryovolcanic plumes, Ice geysers");
            } else if ("Silicate".equals(terrain.getVolcanismType())) {
                features.append("Active lava flows, Volcanic calderas");
            }
        }

        if (Boolean.TRUE.equals(moon.getHasSubsurfaceOcean())) {
            if (!features.isEmpty()) features.append(", ");
            features.append("Subsurface ocean, Tectonic stress patterns");
        }

        if ("EXTREME".equals(terrain.getCrateringLevel())) {
            if (!features.isEmpty()) features.append(", ");
            features.append("Ancient impact basins, Heavily cratered highlands");
        } else if ("HEAVY".equals(terrain.getCrateringLevel())) {
            if (!features.isEmpty()) features.append(", ");
            features.append("Impact crater fields");
        }

        if ("ICY".equals(moon.getCompositionType())) {
            if (!features.isEmpty()) features.append(", ");
            features.append("Water ice plains, Frozen terrain");
        } else if ("ROCKY".equals(moon.getCompositionType())) {
            if (!features.isEmpty()) features.append(", ");
            features.append("Rocky highlands, Silicate plains");
        }

        if (terrain.getMountainCoveragePercent() != null && terrain.getMountainCoveragePercent() > 10) {
            if (!features.isEmpty()) features.append(", ");
            features.append("Mountain ranges");
        }

        terrain.setSurfaceFeatures(!features.isEmpty() ? features.toString() : "Barren surface");
    }

    // ── Internal DTO for planet geology generation (builder pattern) ──

    static class PlanetaryGeology {
        private String activityLevel;
        private Double activityScore;
        private Boolean hasPlateTectonics;
        private Integer numberOfTectonicPlates;
        private String tectonicActivityLevel;
        private Boolean hasVolcanicActivity;
        private String volcanismType;
        private Integer estimatedActiveVolcanoes;
        private String volcanicIntensity;
        private Double mountainCoveragePercent;
        private Double averageElevationKm;
        private Double maxElevationKm;
        private Double minElevationKm;
        private Double terrainRoughness;
        private String crateringLevel;
        private Integer estimatedVisibleCraters;
        private String erosionLevel;
        private String primaryErosionAgent;
        private Boolean hasGreatStorm;
        private Integer numberOfMajorStorms;
        private String atmosphericConvectionLevel;

        public String getActivityLevel() { return activityLevel; }
        public Double getActivityScore() { return activityScore; }
        public Boolean getHasPlateTectonics() { return hasPlateTectonics; }
        public Integer getNumberOfTectonicPlates() { return numberOfTectonicPlates; }
        public String getTectonicActivityLevel() { return tectonicActivityLevel; }
        public Boolean getHasVolcanicActivity() { return hasVolcanicActivity; }
        public String getVolcanismType() { return volcanismType; }
        public Integer getEstimatedActiveVolcanoes() { return estimatedActiveVolcanoes; }
        public String getVolcanicIntensity() { return volcanicIntensity; }
        public Double getMountainCoveragePercent() { return mountainCoveragePercent; }
        public Double getAverageElevationKm() { return averageElevationKm; }
        public Double getMaxElevationKm() { return maxElevationKm; }
        public Double getMinElevationKm() { return minElevationKm; }
        public Double getTerrainRoughness() { return terrainRoughness; }
        public String getCrateringLevel() { return crateringLevel; }
        public Integer getEstimatedVisibleCraters() { return estimatedVisibleCraters; }
        public String getErosionLevel() { return erosionLevel; }
        public String getPrimaryErosionAgent() { return primaryErosionAgent; }
        public Boolean getHasGreatStorm() { return hasGreatStorm; }
        public Integer getNumberOfMajorStorms() { return numberOfMajorStorms; }
        public String getAtmosphericConvectionLevel() { return atmosphericConvectionLevel; }

        static class Builder {
            private final PlanetaryGeology geology = new PlanetaryGeology();

            public Builder activityLevel(String activityLevel) {
                geology.activityLevel = activityLevel;
                return this;
            }

            public Builder activityScore(Double activityScore) {
                geology.activityScore = activityScore;
                return this;
            }

            public Builder plateTectonics(Boolean has, Integer plates, String level) {
                geology.hasPlateTectonics = has;
                geology.numberOfTectonicPlates = plates;
                geology.tectonicActivityLevel = level;
                return this;
            }

            public Builder volcanism(Boolean has, String type, Integer count, String intensity) {
                geology.hasVolcanicActivity = has;
                geology.volcanismType = type;
                geology.estimatedActiveVolcanoes = count;
                geology.volcanicIntensity = intensity;
                return this;
            }

            public Builder terrain(Double mountainCoverage, Double avgElev, Double maxElev,
                                   Double minElev, Double roughness) {
                geology.mountainCoveragePercent = mountainCoverage;
                geology.averageElevationKm = avgElev;
                geology.maxElevationKm = maxElev;
                geology.minElevationKm = minElev;
                geology.terrainRoughness = roughness;
                return this;
            }

            public Builder cratering(String level, Integer count) {
                geology.crateringLevel = level;
                geology.estimatedVisibleCraters = count;
                return this;
            }

            public Builder erosion(String level, String agent) {
                geology.erosionLevel = level;
                geology.primaryErosionAgent = agent;
                return this;
            }

            public Builder gasGiantStorms(Boolean hasGreatStorm, Integer majorStorms, String convection) {
                geology.hasGreatStorm = hasGreatStorm;
                geology.numberOfMajorStorms = majorStorms;
                geology.atmosphericConvectionLevel = convection;
                return this;
            }

            public PlanetaryGeology build() {
                return geology;
            }
        }
    }
}
