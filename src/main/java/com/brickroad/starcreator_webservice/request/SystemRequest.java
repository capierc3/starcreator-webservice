package com.brickroad.starcreator_webservice.request;

import com.brickroad.starcreator_webservice.model.enums.Population;

public class SystemRequest {

    private String name;
    private Population population;
    private int starCount;

    public String getName() {
        if(name == null) {
            return "";
        }
        return name;
    }

    public Population getPopulation() {
        return population;
    }

    public int getStarCount() {
        return starCount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPopulation(Population population) {
        this.population = population;
    }

    public void setStarCount(int starCount) {
        this.starCount = starCount;
    }
}
