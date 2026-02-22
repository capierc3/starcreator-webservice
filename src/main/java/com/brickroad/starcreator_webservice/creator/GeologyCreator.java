package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ref.GeologicalFeatureRef;
import com.brickroad.starcreator_webservice.entity.ref.GeologicalTemplateRef;
import com.brickroad.starcreator_webservice.entity.ref.TerrainCategoryRef;
import com.brickroad.starcreator_webservice.entity.ref.TerrainTypeRef;
import com.brickroad.starcreator_webservice.entity.ud.Moon;
import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.TerrainDistribution;
import com.brickroad.starcreator_webservice.entity.ud.TerrainProperties;
// PlanetaryGeology is a static inner class at the bottom of this file
import com.brickroad.starcreator_webservice.repository.GeologicalTemplateRepository;
import com.brickroad.starcreator_webservice.repository.TerrainCategoryRefRepository;
import com.brickroad.starcreator_webservice.repository.TerrainTypeRefRepository;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeologyCreator {

    @Autowired
    private GeologicalTemplateRepository geologicalTemplateRepository;

    @Autowired
    private TerrainTypeRefRepository terrainTypeRefRepository;

    @Autowired
    private TerrainCategoryRefRepository terrainCategoryRepository;

    private Map<String, TerrainTypeRef> cachedTerrainTypes;
    private Map<String, TerrainCategoryRef> cachedTerrainCategories;

    @PostConstruct
    public void init() {
        cachedTerrainTypes = terrainTypeRefRepository.findAll().stream()
                .collect(Collectors.toMap(TerrainTypeRef::getName, t -> t));
        cachedTerrainCategories = terrainCategoryRepository.findAll().stream()
                .collect(Collectors.toMap(TerrainCategoryRef::getCategory, c -> c));
    }

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

        // Storm fields stay on Planet directly
        planet.setHasGreatStorm(geology.getHasGreatStorm());
        planet.setNumberOfMajorStorms(geology.getNumberOfMajorStorms());
        planet.setAtmosphericConvectionLevel(geology.getAtmosphericConvectionLevel());

        if (!isGasGiant(planet.getPlanetType())) {

            List<TerrainDistribution> terrainDist = generateTerrainDistribution(planet, terrain);
            terrain.setTerrainDistribution(terrainDist);

            if (!terrainDist.isEmpty() && geology.getMaxElevationKm() == null) {
                double mountainCoverage = calculateMountainCoverage(terrainDist);
                terrain.setMountainCoveragePercent(mountainCoverage);

                if (mountainCoverage > 15) {
                    terrain.setMaxElevationKm(RandomUtils.rollRange(4.0, 8.0));
                    terrain.setMinElevationKm(RandomUtils.rollRange(-3.0, -1.0));
                    terrain.setAverageElevationKm(RandomUtils.rollRange(0.0, 2.0));
                    terrain.setTerrainRoughness(RandomUtils.rollRange(3.0, 6.0));
                } else if (mountainCoverage > 5) {
                    terrain.setMaxElevationKm(RandomUtils.rollRange(2.0, 5.0));
                    terrain.setMinElevationKm(RandomUtils.rollRange(-2.0, -0.5));
                    terrain.setAverageElevationKm(RandomUtils.rollRange(-0.5, 1.0));
                    terrain.setTerrainRoughness(RandomUtils.rollRange(2.0, 4.0));
                } else {
                    terrain.setMaxElevationKm(RandomUtils.rollRange(0.5, 2.0));
                    terrain.setMinElevationKm(RandomUtils.rollRange(-1.0, -0.2));
                    terrain.setAverageElevationKm(RandomUtils.rollRange(-0.2, 0.5));
                    terrain.setTerrainRoughness(RandomUtils.rollRange(1.0, 2.5));
                }
            }
        }

        terrain.setLabel("Planet terrain");
        return terrain;
    }

    public TerrainProperties createMoonTerrain(Moon moon, String geologicalActivity, Boolean hasCryovolcanism) {
        TerrainProperties terrain = new TerrainProperties();
        String activityLevel = mapMoonActivityToTemplateLevel(geologicalActivity);

        terrain.setGeologicalActivity(geologicalActivity);
        terrain.setHasCryovolcanism(hasCryovolcanism);

        List<GeologicalTemplateRef> templates = geologicalTemplateRepository
                .findByPlanetTypeAndActivityLevel(moon.getMoonType(), activityLevel);
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

    public TerrainProperties createAsteroidTerrain(String surfaceFeatures, String crateringLevel, Boolean hasRegolith, Double regolithDepthM) {
        TerrainProperties terrain = new TerrainProperties();
        terrain.setSurfaceFeatures(surfaceFeatures);
        terrain.setCrateringLevel(crateringLevel);
        terrain.setHasRegolith(hasRegolith);
        terrain.setRegolithDepthM(regolithDepthM);
        terrain.setLabel("Asteroid terrain");
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
                    hasTectonics = !value.equals("None") && !value.equals("N/A");
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
            if (planet.getWaterCoveragePercent() != null && planet.getWaterCoveragePercent() > 50) {
                minElev = -RandomUtils.rollRange(3.0, 11.0);
            } else {
                minElev = -RandomUtils.rollRange(0.5, 3.0);
            }
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

    private List<TerrainDistribution> generateTerrainDistribution(Planet planet, TerrainProperties terrain) {
        List<TerrainDistribution> terrains = new ArrayList<>();
        String planetType = planet.getPlanetType();

        double waterCoverage = planet.getWaterCoveragePercent() != null ?
                planet.getWaterCoveragePercent() : 0;
        double landPercent = 100.0 - waterCoverage;
        Double surfaceTemp = planet.getSurfaceTemp();

        if (isGasGiant(planetType)) {
            return terrains;
        }

        List<TerrainTypeRef> viableTerrains = getViableTerrains(planet, waterCoverage);

        if (waterCoverage > 1.0) {
            boolean waterIsFrozen = surfaceTemp != null && surfaceTemp < 273.0;

            if (waterIsFrozen) {
                List<TerrainTypeRef> iceTerrains = filterByCategory(viableTerrains, "ICE");
                if (!iceTerrains.isEmpty()) {
                    distributePercentageAcrossCategories(terrains, planet, terrain, iceTerrains, waterCoverage);
                }
            } else {
                List<TerrainTypeRef> aquaticTerrains = filterByCategory(viableTerrains, "AQUATIC");
                if (!aquaticTerrains.isEmpty()) {
                    distributePercentageAcrossCategories(terrains, planet, terrain, aquaticTerrains, waterCoverage);
                }
            }
        }

        if (landPercent > 1.0) {
            List<TerrainTypeRef> landTerrains = viableTerrains.stream()
                    .filter(t -> !t.getCategory().equals("AQUATIC"))
                    .collect(Collectors.toList());

            if (!landTerrains.isEmpty()) {
                distributePercentageAcrossCategories(terrains, planet, terrain, landTerrains, landPercent);
            }
        }

        double totalCoverage = terrains.stream()
                .mapToDouble(TerrainDistribution::getCoveragePercent)
                .sum();

        if (totalCoverage < 95.0 && !viableTerrains.isEmpty()) {
            double missingPercent = 100.0 - totalCoverage;
            List<TerrainTypeRef> fillTerrains = viableTerrains.stream()
                    .filter(t -> !t.getCategory().equals("ARTIFICIAL"))
                    .filter(t -> !t.getCategory().equals("AQUATIC"))
                    .collect(Collectors.toList());

            if (!fillTerrains.isEmpty()) {
                distributePercentageAcrossCategories(terrains, planet, terrain, fillTerrains, missingPercent);
            }
        }

        return consolidateDuplicateTerrains(terrains);
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

    private List<TerrainTypeRef> getViableTerrains(Planet planet, double waterCoverage) {
        boolean hasAtmosphere = planet.getAtmosphereComposition() != null &&
                !planet.getAtmosphereComposition().isEmpty() &&
                !planet.getAtmosphereComposition().equals("None");
        boolean hasHeavyCratering = "Heavy".equals(planet.getCrateringLevel()) ||
                "Extreme".equals(planet.getCrateringLevel()) ||
                (planet.getEstimatedVisibleCraters() != null && planet.getEstimatedVisibleCraters() > 10000);
        boolean hasVolcanism = Boolean.TRUE.equals(planet.getHasVolcanicActivity());

        List<TerrainTypeRef> viableTerrains = terrainTypeRefRepository.findViableTerrainTypes(
                planet.getSurfaceTemp(),
                waterCoverage > 0,
                hasAtmosphere,
                planet.getSurfacePressure()
        );

        if (viableTerrains.isEmpty()) {
            Double surfaceTemp = planet.getSurfaceTemp();
            viableTerrains = cachedTerrainTypes.values().stream()
                    .filter(t -> "BARREN".equals(t.getCategory()) || "EXOTIC".equals(t.getCategory()))
                    .filter(t -> surfaceTemp == null || t.getMinTemperatureK() == null || surfaceTemp >= t.getMinTemperatureK())
                    .filter(t -> surfaceTemp == null || t.getMaxTemperatureK() == null || surfaceTemp <= t.getMaxTemperatureK())
                    .collect(Collectors.toList());
        }


        if (viableTerrains.isEmpty()) {
            Double surfaceTemp = planet.getSurfaceTemp();
            viableTerrains = cachedTerrainTypes.values().stream()
                    .filter(t -> "BARREN".equals(t.getCategory()))
                    .filter(t -> surfaceTemp == null || t.getMinTemperatureK() == null || surfaceTemp >= t.getMinTemperatureK())
                    .filter(t -> surfaceTemp == null || t.getMaxTemperatureK() == null || surfaceTemp <= t.getMaxTemperatureK())
                    .collect(Collectors.toList());
        }

        if (hasHeavyCratering || hasVolcanism) {
            viableTerrains = applyGeologicalWeightBoosts(viableTerrains, hasHeavyCratering, hasVolcanism);
        }

        if (planet.getHasVolcanicActivity() == null) {
            viableTerrains = viableTerrains.stream()
                    .filter(t -> !t.getCategory().equals("VOLCANIC"))
                    .collect(Collectors.toList());
        }

        viableTerrains = viableTerrains.stream()
                .filter(terrain -> !isExcludedByPlanetType(terrain, planet))
                .filter(terrain -> meetsCompositionRequirement(terrain, planet))
                .collect(Collectors.toList());

        // Ultimate Question of Life, the Universe, and Everything...
        if (RandomUtils.rollRange(0, 1000000) != 42) {
            viableTerrains = viableTerrains.stream()
                    .filter(t -> !t.getCategory().equals("ARTIFICIAL"))
                    .collect(Collectors.toList());
        }

        return viableTerrains;
    }

    private boolean isExcludedByPlanetType(TerrainTypeRef terrain, Planet planet) {
        if (terrain.getExcludedPlanetTypes() == null ||
                terrain.getExcludedPlanetTypes().length == 0) {
            return false;
        }

        String planetType = planet.getPlanetType();
        if (planetType == null) {
            return false;
        }

        return Arrays.asList(terrain.getExcludedPlanetTypes()).contains(planetType);
    }

    private boolean meetsCompositionRequirement(TerrainTypeRef terrain, Planet planet) {
        String planetComposition = planet.getCompositionClassification();

        if (planetComposition == null) {
            return true;
        }

        if (terrain.getRequiredCompositionClasses() != null &&
                terrain.getRequiredCompositionClasses().length > 0) {
            return Arrays.asList(terrain.getRequiredCompositionClasses())
                    .contains(planetComposition);
        }

        if (terrain.getExcludedCompositionClasses() != null &&
                terrain.getExcludedCompositionClasses().length > 0) {
            return !Arrays.asList(terrain.getExcludedCompositionClasses())
                    .contains(planetComposition);
        }

        return true;
    }

    private List<TerrainTypeRef> applyGeologicalWeightBoosts(List<TerrainTypeRef> terrains, boolean hasHeavyCratering, boolean hasVolcanism) {
        List<TerrainTypeRef> weighted = new ArrayList<>();
        for (TerrainTypeRef terrain : terrains) {
            int effectiveWeight = terrain.getEffectiveWeight(hasHeavyCratering, hasVolcanism);
            int baseWeight = terrain.getRarityWeight() != null ? terrain.getRarityWeight() : 100;

            int copies = 1 + (effectiveWeight - baseWeight) / 50;
            for (int i = 0; i < copies; i++) {
                weighted.add(terrain);
            }
        }
        return weighted.isEmpty() ? terrains : weighted;
    }

    private void distributePercentageAcrossCategories(List<TerrainDistribution> terrains, Planet planet, TerrainProperties terrain, List<TerrainTypeRef> viableTerrains, double totalPercent) {
        if (viableTerrains.isEmpty() || totalPercent < 0.5) {
            return;
        }

        Map<String, List<TerrainTypeRef>> terrainsByCategory = viableTerrains.stream()
                .collect(Collectors.groupingBy(TerrainTypeRef::getCategory));

        Map<String, Integer> categoryWeights = new HashMap<>();
        for (String category : terrainsByCategory.keySet()) {
            TerrainCategoryRef categoryRef = cachedTerrainCategories.get(category);
            if (categoryRef != null) {
                categoryWeights.put(category, categoryRef.getBaseWeight());
            } else {
                categoryWeights.put(category, 50); // Fallback weight
            }
        }

        int totalWeight = categoryWeights.values().stream().mapToInt(Integer::intValue).sum();

        if (totalWeight == 0) {
            distributePercentageEvenly(terrains, planet, terrain, viableTerrains, totalPercent);
            return;
        }

        List<String> majorCategories = new ArrayList<>();
        List<String> rareCategories = new ArrayList<>();

        for (String category : terrainsByCategory.keySet()) {
            TerrainCategoryRef categoryRef = cachedTerrainCategories.get(category);
            if (categoryRef != null && categoryRef.getIsRare()) {
                rareCategories.add(category);
            } else {
                majorCategories.add(category);
            }
        }

        double majorPercent = totalPercent * RandomUtils.rollRange(0.90, 0.95);
        double rarePercent = totalPercent - majorPercent;

        int numMajor = Math.min(majorCategories.size(), RandomUtils.rollRange(2, 4));
        List<String> selectedMajor = selectWeightedRandomCategories(majorCategories, categoryWeights, numMajor);

        distributeAmongCategories(terrains, planet, terrain, terrainsByCategory, categoryWeights,
                cachedTerrainCategories, selectedMajor, majorPercent);

        // Optionally add 1-2 rare categories with small percentages
        if (!rareCategories.isEmpty() && rarePercent > 0.5) {
            int numRare = Math.min(rareCategories.size(), RandomUtils.rollRange(1, 2));
            List<String> selectedRare = selectWeightedRandomCategories(rareCategories, categoryWeights, numRare);
            distributeAmongCategories(terrains, planet, terrain, terrainsByCategory, categoryWeights,
                    cachedTerrainCategories, selectedRare, rarePercent);
        }
    }

    private void distributeAmongCategories(List<TerrainDistribution> terrains, Planet planet, TerrainProperties terrain, Map<String, List<TerrainTypeRef>> terrainsByCategory,
                                           Map<String, Integer> categoryWeights, Map<String, TerrainCategoryRef> categoryRefs, List<String> categories, double totalPercent) {
        double remainingPercent = totalPercent;

        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            List<TerrainTypeRef> categoryTerrains = terrainsByCategory.get(category);
            TerrainCategoryRef categoryRef = categoryRefs.get(category);

            double categoryPercent;
            if (i == categories.size() - 1) {
                categoryPercent = remainingPercent;
            } else {
                int categoryWeight = categoryWeights.get(category);
                int selectedWeight = categories.stream().mapToInt(categoryWeights::get).sum();

                double basePercent = (totalPercent * categoryWeight) / (double) selectedWeight;
                double variance = RandomUtils.rollRange(-0.3, 0.3);
                categoryPercent = basePercent * (1.0 + variance);

                if (categoryRef != null) {
                    categoryPercent = Math.max(categoryPercent, categoryRef.getTypicalMinCoverage());
                    categoryPercent = Math.min(categoryPercent, categoryRef.getTypicalMaxCoverage());
                }

                double reserveForOthers = (categories.size() - i - 1) * 1.0;
                categoryPercent = Math.min(categoryPercent, remainingPercent - reserveForOthers);
            }

            if (categoryPercent >= 0.5) {
                distributePercentageWithinCategory(terrains, planet, terrain, categoryTerrains, categoryPercent);
                remainingPercent -= categoryPercent;
            }
        }
    }

    private List<String> selectWeightedRandomCategories(List<String> categories, Map<String, Integer> weights, int count) {
        List<String> selected = new ArrayList<>();
        List<String> available = new ArrayList<>(categories);

        count = Math.min(count, available.size());

        for (int i = 0; i < count; i++) {
            if (available.isEmpty()) break;

            int totalWeight = available.stream()
                    .mapToInt(weights::get)
                    .sum();

            if (totalWeight == 0) {
                selected.add(available.remove(RandomUtils.rollRange(0, available.size() - 1)));
                continue;
            }

            int randomWeight = RandomUtils.rollRange(1, totalWeight);
            int currentWeight = 0;

            for (String category : available) {
                currentWeight += weights.get(category);
                if (randomWeight <= currentWeight) {
                    selected.add(category);
                    available.remove(category);
                    break;
                }
            }
        }

        return selected;
    }

    private void distributePercentageWithinCategory(List<TerrainDistribution> terrains, Planet planet, TerrainProperties terrainProps, List<TerrainTypeRef> categoryTerrains, double categoryPercent) {
        if (categoryTerrains.isEmpty()) {
            return;
        }

        // Determine geological modifiers for weight calculation
        boolean heavyCratering = "Heavy".equals(planet.getCrateringLevel()) ||
                "Extreme".equals(planet.getCrateringLevel()) ||
                (planet.getEstimatedVisibleCraters() != null && planet.getEstimatedVisibleCraters() > 10000);
        boolean volcanism = Boolean.TRUE.equals(planet.getHasVolcanicActivity());

        List<TerrainTypeRef> shuffled = new ArrayList<>(categoryTerrains);
        Collections.shuffle(shuffled);
        int numTerrains = Math.min(shuffled.size(), RandomUtils.rollRange(1, 3));
        List<TerrainTypeRef> selectedTerrains = shuffled.subList(0, numTerrains);

        int totalWeight = selectedTerrains.stream()
                .mapToInt(t -> t.getEffectiveWeight(heavyCratering, volcanism))
                .sum();

        double remainingPercent = categoryPercent;

        for (int i = 0; i < selectedTerrains.size(); i++) {
            TerrainTypeRef terrainType = selectedTerrains.get(i);
            double percent;

            if (i == selectedTerrains.size() - 1) {
                percent = remainingPercent;
            } else {
                if (totalWeight > 0) {
                    double basePercent = (categoryPercent * terrainType.getEffectiveWeight(heavyCratering, volcanism)) / (double) totalWeight;
                    double variance = RandomUtils.rollRange(-0.2, 0.2);
                    percent = basePercent * (1.0 + variance);
                } else {
                    percent = categoryPercent / selectedTerrains.size();
                }

                percent = Math.max(percent, terrainType.getTypicalCoverageMin());
                percent = Math.min(percent, terrainType.getTypicalCoverageMax());

                double reserveForOthers = (selectedTerrains.size() - i - 1) * 0.5;
                percent = Math.min(percent, remainingPercent - reserveForOthers);
            }

            if (percent >= 0.5) {
                addTerrainDirect(terrains, terrainProps, terrainType, percent);
                remainingPercent -= percent;
            }
        }
    }

    private void distributePercentageEvenly(List<TerrainDistribution> terrains, Planet planet, TerrainProperties terrainProps, List<TerrainTypeRef> terrainTypes, double totalPercent) {
        if (terrainTypes.isEmpty() || totalPercent < 0.5) {
            return;
        }

        List<TerrainTypeRef> selectedTerrains = new ArrayList<>(terrainTypes);
        java.util.Collections.shuffle(selectedTerrains);
        int numTerrains = Math.min(selectedTerrains.size(), RandomUtils.rollRange(2, 4));
        selectedTerrains = selectedTerrains.subList(0, numTerrains);

        double remainingPercent = totalPercent;

        for (int i = 0; i < selectedTerrains.size(); i++) {
            TerrainTypeRef terrainType = selectedTerrains.get(i);
            double percent;

            if (i == selectedTerrains.size() - 1) {
                percent = remainingPercent;
            } else {
                double basePercent = totalPercent / selectedTerrains.size();
                double variance = RandomUtils.rollRange(-0.3, 0.3);
                percent = basePercent * (1.0 + variance);

                percent = Math.max(percent, terrainType.getTypicalCoverageMin());
                percent = Math.min(percent, terrainType.getTypicalCoverageMax());

                double reserveForOthers = (selectedTerrains.size() - i - 1) * 1.0;
                percent = Math.min(percent, remainingPercent - reserveForOthers);
            }

            if (percent >= 0.5) {
                addTerrainDirect(terrains, terrainProps, terrainType, percent);
                remainingPercent -= percent;
            }

            if (remainingPercent < 0) {
                remainingPercent = 0;
            }
        }
    }

    private void addTerrainDirect(List<TerrainDistribution> terrains, TerrainProperties terrainProps, TerrainTypeRef terrainType, double percent) {
        if (percent < 0.5) {
            return;
        }

        TerrainDistribution distribution = new TerrainDistribution(
                terrainType,
                Math.round(percent * 100.0) / 100.0
        );
        distribution.setTerrain(terrainProps);
        terrains.add(distribution);
    }

    private List<TerrainTypeRef> filterByCategory(List<TerrainTypeRef> terrains, String category) {
        return terrains.stream()
                .filter(t -> t.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    private double calculateMountainCoverage(List<TerrainDistribution> terrains) {
        return terrains.stream()
                .filter(t -> t.getTerrainType().getCategory().equals("MOUNTAIN"))
                .mapToDouble(TerrainDistribution::getCoveragePercent)
                .sum();
    }

    private List<TerrainDistribution> consolidateDuplicateTerrains(List<TerrainDistribution> terrains) {
        Map<Integer, TerrainDistribution> consolidated = new LinkedHashMap<>();

        for (TerrainDistribution dist : terrains) {
            Integer terrainId = dist.getTerrainType().getId();

            if (consolidated.containsKey(terrainId)) {
                // Add coverage to existing entry
                TerrainDistribution existing = consolidated.get(terrainId);
                double newCoverage = existing.getCoveragePercent() + dist.getCoveragePercent();
                existing.setCoveragePercent(Math.round(newCoverage * 100.0) / 100.0);
            } else {
                consolidated.put(terrainId, dist);
            }
        }

        return new ArrayList<>(consolidated.values());
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
