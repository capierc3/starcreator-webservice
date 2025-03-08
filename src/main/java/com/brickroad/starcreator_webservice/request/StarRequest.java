package com.brickroad.starcreator_webservice.request;

import com.brickroad.starcreator_webservice.model.enums.StarType;

public class StarRequest {

    private String name;
    private StarType type;
    private boolean habitable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StarType getType() {
        return type;
    }

    public void setType(StarType type) {
        this.type = type;
    }

    public boolean isHabitable() {
        return habitable;
    }

    public void setHabitable(boolean habitable) {
        this.habitable = habitable;
    }
}
