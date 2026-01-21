package com.brickroad.starcreator_webservice.model.planets;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the geological characteristics of a planet.
 * Works for both terrestrial and gas giant planets.
 */
public class PlanetaryGeology {

    private String activityLevel; // "Highly Active", "Moderately Active", etc.
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

    // All getters and setters
    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public Double getActivityScore() { return activityScore; }
    public void setActivityScore(Double activityScore) { this.activityScore = activityScore; }

    public Boolean getHasPlateTectonics() { return hasPlateTectonics; }
    public void setHasPlateTectonics(Boolean hasPlateTectonics) { this.hasPlateTectonics = hasPlateTectonics; }

    public Integer getNumberOfTectonicPlates() { return numberOfTectonicPlates; }
    public void setNumberOfTectonicPlates(Integer numberOfTectonicPlates) { this.numberOfTectonicPlates = numberOfTectonicPlates; }

    public String getTectonicActivityLevel() { return tectonicActivityLevel; }
    public void setTectonicActivityLevel(String tectonicActivityLevel) { this.tectonicActivityLevel = tectonicActivityLevel; }

    public Boolean getHasVolcanicActivity() { return hasVolcanicActivity; }
    public void setHasVolcanicActivity(Boolean hasVolcanicActivity) { this.hasVolcanicActivity = hasVolcanicActivity; }

    public String getVolcanismType() { return volcanismType; }
    public void setVolcanismType(String volcanismType) { this.volcanismType = volcanismType; }

    public Integer getEstimatedActiveVolcanoes() { return estimatedActiveVolcanoes; }
    public void setEstimatedActiveVolcanoes(Integer estimatedActiveVolcanoes) { this.estimatedActiveVolcanoes = estimatedActiveVolcanoes; }

    public String getVolcanicIntensity() { return volcanicIntensity; }
    public void setVolcanicIntensity(String volcanicIntensity) { this.volcanicIntensity = volcanicIntensity; }

    public Double getMountainCoveragePercent() { return mountainCoveragePercent; }
    public void setMountainCoveragePercent(Double mountainCoveragePercent) { this.mountainCoveragePercent = mountainCoveragePercent; }

    public Double getAverageElevationKm() { return averageElevationKm; }
    public void setAverageElevationKm(Double averageElevationKm) { this.averageElevationKm = averageElevationKm; }

    public Double getMaxElevationKm() { return maxElevationKm; }
    public void setMaxElevationKm(Double maxElevationKm) { this.maxElevationKm = maxElevationKm; }

    public Double getMinElevationKm() { return minElevationKm; }
    public void setMinElevationKm(Double minElevationKm) { this.minElevationKm = minElevationKm; }

    public Double getTerrainRoughness() { return terrainRoughness; }
    public void setTerrainRoughness(Double terrainRoughness) { this.terrainRoughness = terrainRoughness; }

    public String getCrateringLevel() { return crateringLevel; }
    public void setCrateringLevel(String crateringLevel) { this.crateringLevel = crateringLevel; }

    public Integer getEstimatedVisibleCraters() { return estimatedVisibleCraters; }
    public void setEstimatedVisibleCraters(Integer estimatedVisibleCraters) { this.estimatedVisibleCraters = estimatedVisibleCraters; }

    public String getErosionLevel() { return erosionLevel; }
    public void setErosionLevel(String erosionLevel) { this.erosionLevel = erosionLevel; }

    public String getPrimaryErosionAgent() { return primaryErosionAgent; }
    public void setPrimaryErosionAgent(String primaryErosionAgent) { this.primaryErosionAgent = primaryErosionAgent; }

    public Boolean getHasGreatStorm() { return hasGreatStorm; }
    public void setHasGreatStorm(Boolean hasGreatStorm) { this.hasGreatStorm = hasGreatStorm; }

    public Integer getNumberOfMajorStorms() { return numberOfMajorStorms; }
    public void setNumberOfMajorStorms(Integer numberOfMajorStorms) { this.numberOfMajorStorms = numberOfMajorStorms; }

    public String getAtmosphericConvectionLevel() { return atmosphericConvectionLevel; }
    public void setAtmosphericConvectionLevel(String atmosphericConvectionLevel) { this.atmosphericConvectionLevel = atmosphericConvectionLevel; }

    public List<PlanetaryTerrainDistribution> getTerrainDistribution() { return terrainDistribution; }
    public void setTerrainDistribution(List<PlanetaryTerrainDistribution> terrainDistribution) { this.terrainDistribution = terrainDistribution; }

}

