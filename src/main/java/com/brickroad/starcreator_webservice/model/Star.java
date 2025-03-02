package com.brickroad.starcreator_webservice.model;

import com.brickroad.starcreator_webservice.model.enums.StarType;


public class Star {

    private String name;
    private StarType starType;
    private String description;
    private double solarMass;
    private double solarRadius;
    private double temperature;

    public Star() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSolarMass() {
        return solarMass;
    }

    public void setSolarMass(double solarMass) {
        this.solarMass = solarMass;
    }

    public double getSolarRadius() {
        return solarRadius;
    }

    public void setSolarRadius(double solarRadius) {
        this.solarRadius = solarRadius;
    }

    public StarType getStarType() {
        return starType;
    }

    public void setStarType(StarType starType) {
        this.starType = starType;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
