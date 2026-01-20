package com.brickroad.starcreator_webservice.Creators;

import com.brickroad.starcreator_webservice.model.enums.TerrainType;
import com.brickroad.starcreator_webservice.model.geological.GeologicalFeatureRef;
import com.brickroad.starcreator_webservice.model.geological.GeologicalTemplateRef;
import com.brickroad.starcreator_webservice.model.planets.Planet;
import com.brickroad.starcreator_webservice.model.planets.PlanetaryGeology;
import com.brickroad.starcreator_webservice.model.geological.TerrainDistribution;
import com.brickroad.starcreator_webservice.repos.GeologicalTemplateRepository;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeologyCreator {

    @Autowired
    private GeologicalTemplateRepository geologicalTemplateRepository;

    private List<GeologicalTemplateRef> cachedTemplates;

    @PostConstruct
    public void init() {
        cachedTemplates = geologicalTemplateRepository.findAll();
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
        if (template == null) {
            return generateBasicGeology(activityScore, isGasGiant, planet);
        }

        return generateGeologyFromTemplate(template, planet, activityScore);
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

        // Collect values from features for aggregation
        Double mountainCoverage = null;
        Double maxElev = null;
        Double minElev = null;
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

        // Process all features
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

        List<TerrainDistribution> terrainDist = generateTerrainDistribution(planet);
        builder.terrainDistribution(terrainDist);

        return builder.build();
    }

    private PlanetaryGeology generateBasicGeology(double activityScore, boolean isGasGiant, Planet planet) {

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
                    .terrain(null, null, null, null, 5.0);
        } else {
            builder.plateTectonics(false, null, "None")
                    .volcanism(false, "None", 0, "None")
                    .terrain(5.0, 0.0, 2.0, -1.0, 2.0)
                    .cratering("Heavy", 50000)
                    .erosion("None", "None");
        }

        return builder.build();
    }

    private List<TerrainDistribution> generateTerrainDistribution(Planet planet) {
        List<TerrainDistribution> terrains = new ArrayList<>();
        String planetType = planet.getPlanetType();
        double waterCoverage = planet.getWaterCoveragePercent() != null ?
                planet.getWaterCoveragePercent() : 0;
        double remainingPercent = 100.0 - waterCoverage;

        if (isGasGiant(planetType)) {
            // Gas giants don't have terrain
            return terrains;
        }

        if (planetType.contains("Ocean")) {
            addTerrain(terrains, TerrainType.AQUATIC_DEEP, waterCoverage * 0.8);
            addTerrain(terrains, TerrainType.AQUATIC_SHALLOW, waterCoverage * 0.2);
            addTerrain(terrains, TerrainType.ISLAND, remainingPercent * 0.7);
            addTerrain(terrains, TerrainType.MOUNTAINS, remainingPercent * 0.3);
        } else if (planetType.contains("Desert")) {
            addTerrain(terrains, TerrainType.DESERT, remainingPercent * 0.7);
            addTerrain(terrains, TerrainType.PLAINS, remainingPercent * 0.15);
            addTerrain(terrains, TerrainType.MOUNTAINS, remainingPercent * 0.15);
        } else if (planetType.contains("Lava")) {
            addTerrain(terrains, TerrainType.WASTE_LANDS, remainingPercent * 0.6);
            addTerrain(terrains, TerrainType.MOUNTAINS, remainingPercent * 0.4);
        } else if (planetType.contains("Ice")) {
            addTerrain(terrains, TerrainType.ARCTIC_FROZEN, remainingPercent * 0.5);
            addTerrain(terrains, TerrainType.GLACIER, remainingPercent * 0.3);
            addTerrain(terrains, TerrainType.TUNDRA, remainingPercent * 0.2);
        } else if (planetType.contains("Terrestrial")) {
            if (waterCoverage > 0) {
                addTerrain(terrains, TerrainType.AQUATIC_DEEP, waterCoverage * 0.6);
                addTerrain(terrains, TerrainType.AQUATIC_SHALLOW, waterCoverage * 0.4);
            }
            addTerrain(terrains, TerrainType.FOREST, remainingPercent * 0.3);
            addTerrain(terrains, TerrainType.GRASSLAND, remainingPercent * 0.25);
            addTerrain(terrains, TerrainType.MOUNTAINS, remainingPercent * 0.15);
            addTerrain(terrains, TerrainType.DESERT, remainingPercent * 0.15);
            addTerrain(terrains, TerrainType.JUNGLE, remainingPercent * 0.1);
            addTerrain(terrains, TerrainType.TUNDRA, remainingPercent * 0.05);
        } else {
            // Generic rocky world
            addTerrain(terrains, TerrainType.PLAINS, remainingPercent * 0.4);
            addTerrain(terrains, TerrainType.MOUNTAINS, remainingPercent * 0.3);
            addTerrain(terrains, TerrainType.HILLS, remainingPercent * 0.2);
            addTerrain(terrains, TerrainType.CANYON, remainingPercent * 0.1);
        }

        return terrains;
    }

    private void addTerrain(List<TerrainDistribution> terrains, TerrainType type, double percent) {
        if (percent > 0.5) {
            terrains.add(new TerrainDistribution(type, Math.round(percent * 100.0) / 100.0));
        }
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


}
