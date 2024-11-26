package com.brickroad.starcreator_webservice.request;

import com.brickroad.starcreator_webservice.model.enums.PlanetType;

public class PlanetRequest {

    private String name;
    private PlanetType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlanetType getType() {
        return type;
    }

    public void setType(String type) {
        this.type = PlanetType.getEnum(type);
    }
}
