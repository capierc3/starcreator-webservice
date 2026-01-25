package com.brickroad.starcreator_webservice.utils.planets;

import com.brickroad.starcreator_webservice.entity.ud.PlanetaryTerrainDistribution;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
public class PlanetaryGeology {

    private String activityLevel;
    private Double activityScore;

    // Tectonics (terrestrial)
    private Boolean hasPlateTectonics;
    private Integer numberOfTectonicPlates;
    private String tectonicActivityLevel;

    // Volcanism (all types)
    private Boolean hasVolcanicActivity;
    private String volcanismType;
    private Integer estimatedActiveVolcanoes;
    private String volcanicIntensity;

    // Terrain (terrestrial)
    private Double mountainCoveragePercent;
    private Double averageElevationKm;
    private Double maxElevationKm;
    private Double minElevationKm;
    private Double terrainRoughness;

    // Impact history
    private String crateringLevel;
    private Integer estimatedVisibleCraters;

    // Erosion
    private String erosionLevel;
    private String primaryErosionAgent;

    // Gas giant features
    private Boolean hasGreatStorm;
    private Integer numberOfMajorStorms;
    private String atmosphericConvectionLevel;

    // Terrain distribution
    private List<PlanetaryTerrainDistribution> terrainDistribution = new ArrayList<>();

    public static class Builder {
        private PlanetaryGeology geology = new PlanetaryGeology();

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

        public Builder terrainDistribution(List<PlanetaryTerrainDistribution> distribution) {
            geology.terrainDistribution = distribution;
            return this;
        }

        public PlanetaryGeology build() {
            return geology;
        }
    }

}

