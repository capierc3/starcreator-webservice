package com.brickroad.starcreator_webservice.model.planets;

import com.brickroad.starcreator_webservice.model.enums.TerrainType;

public class SurfaceProperty {

    private TerrainType type;
    private int percent;

    SurfaceProperty(TerrainType type, int percent) {
        this.type = type;
        this.percent = percent;
    }

    public TerrainType getType() {
        return type;
    }

    public void setType(TerrainType type) {
        this.type = type;
    }

    public int getPercent() {
        return percent;
    }

    public void updatePercent(int percent) {
        this.percent += percent;
    }
}
