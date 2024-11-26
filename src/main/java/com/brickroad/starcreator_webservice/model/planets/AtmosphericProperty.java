package com.brickroad.starcreator_webservice.model.planets;

import com.brickroad.starcreator_webservice.model.enums.AtmosphereType;

public class AtmosphericProperty {

    private AtmosphereType type;
    private int percent;

    AtmosphericProperty(AtmosphereType type, int percent) {
        this.type = type;
        this.percent = percent;
    }

    public AtmosphereType getType() {
        return type;
    }

    public void setType(AtmosphereType type) {
        this.type = type;
    }

    public int getPercent() {
        return percent;
    }

    public void updatePercent(int percent) {
        this.percent += percent;
    }
}
