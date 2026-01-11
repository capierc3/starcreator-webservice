package com.brickroad.starcreator_webservice.model.planets;

import com.brickroad.starcreator_webservice.model.Body;
import com.brickroad.starcreator_webservice.model.enums.PlanetSubType;
import com.brickroad.starcreator_webservice.model.enums.PlanetType;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Planet extends Body {

    @JsonIgnore
    private int planetSize;
    @JsonIgnore
    private int densityRating;
    private String axisTilt;
    private double tiltDegree;
    private double rotation;
    private String rotationDir;
    private String[] orbitingBodies;
    private Atmosphere atmosphere;
    private Surface surface;
    private String systemName;
    private PlanetType planetType;
    private double mass;
    private double densityDouble;
    private PlanetSubType subType;
    private double gravityInGs;

    public Planet(){
        super();
        atmosphere = new Atmosphere();
        surface = new Surface();
    }

    public int getPlanetSize() {
        return planetSize;
    }

    public void setPlanetSize(int planetSize) {
        this.planetSize = planetSize;
    }

    public int getDensityRating() {
        return densityRating;
    }

    public void setDensityRating(int densityRating) {
        this.densityRating = densityRating;
    }

    public String getAxisTilt() {
        return axisTilt;
    }

    public void setAxisTilt(String axisTilt) {
        this.axisTilt = axisTilt;
    }

    public double getTiltDegree() {
        return tiltDegree;
    }

    public void setTiltDegree(double tiltDegree) {
        this.tiltDegree = tiltDegree;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public String getRotationDir() {
        return rotationDir;
    }

    public void setRotationDir(String rotationDir) {
        this.rotationDir = rotationDir;
    }

    public String[] getOrbitingBodies() {
        return orbitingBodies;
    }

    public void setOrbitingBodies(String[] orbitingBodies) {
        this.orbitingBodies = orbitingBodies;
    }

    public Atmosphere getAtmosphere() {
        return atmosphere;
    }

    public void setAtmosphere(Atmosphere atmosphere) {
        this.atmosphere = atmosphere;
    }

    public Surface getSurface() {
        return surface;
    }

    public void setSurface(Surface surface) {
        this.surface = surface;
    }

    @Override
    public String getSystemName() {
        return systemName;
    }

    @Override
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public PlanetType getPlanetType() {
        return planetType;
    }

    public void setPlanetType(PlanetType planetType) {
        this.planetType = planetType;
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public double getDensityDouble() {
        return densityDouble;
    }

    public void setDensityDouble(double densityDouble) {
        this.densityDouble = densityDouble;
    }

    public PlanetSubType getSubType() {
        return subType;
    }

    public void setSubType(PlanetSubType subType) {
        this.subType = subType;
    }

    public double getGravityInGs() {
        return gravityInGs;
    }

    public void setGravityInGs(double gravityInGs) {
        this.gravityInGs = gravityInGs;
    }
}
