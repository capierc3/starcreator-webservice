package com.brickroad.starcreator_webservice.request;

import com.brickroad.starcreator_webservice.model.enums.Population;
import com.brickroad.starcreator_webservice.model.enums.SystemType;

public class SystemRequest {

    private String name;
    private Population population;
    private SystemType systemType;

    public String getName() {
        if(name == null) {
            return "";
        }
        return name;
    }

    public Population getPopulation() {
        return population;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPopulation(Population population) {
        this.population = population;
    }

    public SystemType getSystemType() {
        return systemType;
    }

    public void setSystemType(SystemType systemType) {
        this.systemType = systemType;
    }
}
