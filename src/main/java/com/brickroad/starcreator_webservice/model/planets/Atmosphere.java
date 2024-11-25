package com.brickroad.starcreator_webservice.model.planets;

import com.brickroad.starcreator_webservice.model.enums.AtmosphereType;

import java.util.ArrayList;

public class Atmosphere {

    private final ArrayList<AtmosphericProperty> atmosphericComposite;
    private String atmosphereThickness;
    private double atmosphericPressure;
    private String description;

    public Atmosphere() {
        atmosphericComposite = new ArrayList<>();
    }

    public void addComposite(AtmosphereType type, int percent) {
        atmosphericComposite.add(new AtmosphericProperty(type, percent));
    }

    public boolean compositeContainsType(AtmosphereType type) {
        return atmosphericComposite.stream().anyMatch(ap -> ap.getType().equals(type));
    }

    public void compositeUpdatePercent(AtmosphereType type, int percent) {
        atmosphericComposite.stream().filter(ap -> ap.getType().equals(type)).forEach(ap -> ap.updatePercent(percent));
    }

    public ArrayList<AtmosphericProperty> getAtmosphericComposite() {
        return atmosphericComposite;
    }

    public String getAtmosphereThickness() {
        return atmosphereThickness;
    }

    public void setAtmosphereThickness(String atmosphereThickness) {
        this.atmosphereThickness = atmosphereThickness;
    }

    public double getAtmosphericPressure() {
        return atmosphericPressure;
    }

    public void setAtmosphericPressure(double atmosphericPressure) {
        this.atmosphericPressure = atmosphericPressure;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
