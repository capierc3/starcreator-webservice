package com.brickroad.starcreator_webservice.model;

import com.brickroad.starcreator_webservice.model.enums.HabitableZone;
import com.brickroad.starcreator_webservice.model.enums.StarType;


public class Star {

    private String name;
    private StarType starType;
    private String description;
    private double solarMass;
    private double solarRadius;
    private double temperature;
    private double minHabitableZone;
    private double maxHabitableZone;
    private HabitableZone lifeSupporting;

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

    public double getMinHabitableZone() {
        return minHabitableZone;
    }

    public void setMinHabitableZone(double minHabitableZone) {
        this.minHabitableZone = minHabitableZone;
    }

    public double getMaxHabitableZone() {
        return maxHabitableZone;
    }

    public void setMaxHabitableZone(double maxHabitableZone) {
        this.maxHabitableZone = maxHabitableZone;
    }

    public HabitableZone getLifeSupporting() {
        return lifeSupporting;
    }

    public void setLifeSupporting(HabitableZone lifeSupporting) {
        this.lifeSupporting = lifeSupporting;
    }
}
