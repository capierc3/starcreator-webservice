package com.brickroad.starcreator_webservice.model.geological;

import com.brickroad.starcreator_webservice.model.enums.TerrainType; /**
 * Represents the percentage distribution of terrain types on a planet
 */
public class TerrainDistribution {
    private TerrainType terrainType;
    private Double coveragePercent;
    private String description;

    public TerrainDistribution(TerrainType terrainType, Double coveragePercent) {
        this.terrainType = terrainType;
        this.coveragePercent = coveragePercent;
    }

    public TerrainType getTerrainType() { return terrainType; }
    public void setTerrainType(TerrainType terrainType) { this.terrainType = terrainType; }

    public Double getCoveragePercent() { return coveragePercent; }
    public void setCoveragePercent(Double coveragePercent) { this.coveragePercent = coveragePercent; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
