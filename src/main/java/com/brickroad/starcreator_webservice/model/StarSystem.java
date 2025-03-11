package com.brickroad.starcreator_webservice.model;

import com.brickroad.starcreator_webservice.model.enums.BinaryType;
import com.brickroad.starcreator_webservice.model.enums.Population;
import com.brickroad.starcreator_webservice.model.enums.SystemType;
import com.brickroad.starcreator_webservice.model.planets.Planet;

import java.util.ArrayList;
import java.util.List;

public class StarSystem {

    private String name;
    private ArrayList<Star> stars;
    private ArrayList<Planet> planets;
    private ArrayList<Body> bodies;
    private ArrayList<Body> orderSystem;
    private ArrayList<StarSystem> nearBySystems;
    private double size;
    private double habitLow;
    private double habitHigh;
    private Population population;
    private SystemType systemType;
    private BinaryType binaryType;
    private double eccentricity;
    private double periastron;
    private double apastron;
    private double semiMajorAxis;
    double x,y,z;
    String id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Star> getStars() {
        return stars;
    }

    public void setStars(ArrayList<Star> stars) {
        this.stars = stars;
    }

    public ArrayList<Planet> getPlanets() {
        return planets;
    }

    public void setPlanets(ArrayList<Planet> planets) {
        this.planets = planets;
    }

    public ArrayList<Body> getBodies() {
        return bodies;
    }

    public void setBodies(ArrayList<Body> bodies) {
        this.bodies = bodies;
    }

    public ArrayList<Body> getOrderSystem() {
        return orderSystem;
    }

    public void setOrderSystem(ArrayList<Body> orderSystem) {
        this.orderSystem = orderSystem;
    }

    public ArrayList<StarSystem> getNearBySystems() {
        return nearBySystems;
    }

    public void setNearBySystems(ArrayList<StarSystem> nearBySystems) {
        this.nearBySystems = nearBySystems;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public double getHabitLow() {
        return habitLow;
    }

    public void setHabitLow(double habitLow) {
        this.habitLow = habitLow;
    }

    public double getHabitHigh() {
        return habitHigh;
    }

    public void setHabitHigh(double habitHigh) {
        this.habitHigh = habitHigh;
    }

    public Population getPopulation() {
        return population;
    }

    public void setPopulation(Population population) {
        this.population = population;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SystemType getSystemType() {
        return systemType;
    }

    public void setSystemType(SystemType systemType) {
        this.systemType = systemType;
    }

    public double getEccentricity() {
        return eccentricity;
    }

    public void setEccentricity(double eccentricity) {
        this.eccentricity = eccentricity;
    }

    public BinaryType getBinaryType() {
        return binaryType;
    }

    public void setBinaryType(BinaryType binaryType) {
        this.binaryType = binaryType;
    }

    public double getPeriastron() {
        return periastron;
    }

    public void setPeriastron(double periastron) {
        this.periastron = periastron;
    }

    public double getApastron() {
        return apastron;
    }

    public void setApastron(double apastron) {
        this.apastron = apastron;
    }

    public double getSemiMajorAxis() {
        return semiMajorAxis;
    }

    public void setSemiMajorAxis(double semiMajorAxis) {
        this.semiMajorAxis = semiMajorAxis;
    }
}
