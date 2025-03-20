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
    private double luminosity;
    private double minHabitableZone;
    private double maxHabitableZone;
    private HabitableZone lifeSupporting;
    private double distToBarycenter;
    private double innerDisk;
    private double snowLine;
    private double pebbleIsoLine;
    private double iceLineMethane;
    private double iceLineCO;
    private double iceLineCO2;
    private double stabilityBoundary;
    private double oortClouds;

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

    public double getDistToBarycenter() {
        return distToBarycenter;
    }

    public void setDistToBarycenter(double distToBarycenter) {
        this.distToBarycenter = distToBarycenter;
    }

    public double getLuminosity() {
        return luminosity;
    }

    public void setLuminosity(double luminosity) {
        this.luminosity = luminosity;
    }

    public double getInnerDisk() {
        return innerDisk;
    }

    public void setInnerDisk(double innerDisk) {
        this.innerDisk = innerDisk;
    }

    public double getSnowLine() {
        return snowLine;
    }

    public void setSnowLine(double snowLine) {
        this.snowLine = snowLine;
    }

    public double getIceLineMethane() {
        return iceLineMethane;
    }

    public void setIceLineMethane(double iceLineMethane) {
        this.iceLineMethane = iceLineMethane;
    }

    public double getStabilityBoundary() {
        return stabilityBoundary;
    }

    public void setStabilityBoundary(double stabilityBoundary) {
        this.stabilityBoundary = stabilityBoundary;
    }

    public double getOortClouds() {
        return oortClouds;
    }

    public void setOortClouds(double oortClouds) {
        this.oortClouds = oortClouds;
    }

    public double getPebbleIsoLine() {
        return pebbleIsoLine;
    }

    public void setPebbleIsoLine(double pebbleIsoLine) {
        this.pebbleIsoLine = pebbleIsoLine;
    }

    public double getIceLineCO() {
        return iceLineCO;
    }

    public void setIceLineCO(double iceLineCO) {
        this.iceLineCO = iceLineCO;
    }

    public double getIceLineCO2() {
        return iceLineCO2;
    }

    public void setIceLineCO2(double iceLineCO2) {
        this.iceLineCO2 = iceLineCO2;
    }
}
