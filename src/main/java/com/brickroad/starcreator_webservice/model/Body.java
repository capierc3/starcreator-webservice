package com.brickroad.starcreator_webservice.model;

import com.brickroad.starcreator_webservice.model.planets.MagneticField;
import com.brickroad.starcreator_webservice.utils.SpaceTravel;

/**
 * Abstract class that holds all shared variables and methods for the different types of Bodies.
 * @author Chase Pierce
 * @version 1.0
 */
public abstract class Body implements Comparable {

    protected String type;
    protected String name;
    protected String size;
    protected MagneticField magneticField;
    protected String density;
    protected double circumference;
    protected double radius;
    protected double gravity;
    protected double temp;
    protected double orbitLength;
    protected int location;
    protected Double distanceSun;
    protected String systemName;

    protected Body(){}

    public void setTemp(double temp){
        this.temp=temp;
    }

    public void setLocation(int loc, double distSun){
        location = loc;
        distanceSun = distSun;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Double getDistanceSun() {
        return distanceSun;
    }

    public MagneticField getMagneticField() {
        return magneticField;
    }

    @Override
    public int compareTo(Object o) {
        try {
            Body b = (Body) o;
            //System.out.println(this.name+": "+this.type);
            return this.distanceSun.compareTo(b.distanceSun);
        } catch (ClassCastException e){
            return 0;
        }
    }

    @Override
    public String toString() {
        return "Name: "+name+
                "\nSize: "+size+
                "\nType: "+type+
                "\nLocation: "+location+
                "\nDistance to Sun: "+(distanceSun*498.66)+"ls"+
                "\n                 "+distanceSun+"AU"+
                "\n                 "+(String.format("%.2f", SpaceTravel.TimeTo(distanceSun,1, SpaceTravel.distUnits.AU, SpaceTravel.timeUnits.Hours,false)))+" hours at 1g"+
                "\n                 "+(String.format("%.2f",SpaceTravel.TimeTo(distanceSun,1, SpaceTravel.distUnits.AU, SpaceTravel.timeUnits.Days,false)))+" days at 1g"+
                "\nTemperature: "+temp+"\u00B0" + "F"+
        "\nOrbit Length: "+String.format("%.2f",orbitLength)+" earth years";
    }

    public String getSize() {
        return size;
    }

    public String getDensity() {
        return density;
    }

    public Double getCircumference() {
        return circumference;
    }

    public Double getRadius() {
        return radius;
    }

    public double getGravity() {
        return gravity;
    }

    public double getTemp() {
        return temp;
    }

    public double getOrbitLength() {
        return orbitLength;
    }

    public int getLocation() {
        return location;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setMagneticField(MagneticField magneticField) {
        this.magneticField = magneticField;
    }

    public void setDensity(String density) {
        this.density = density;
    }

    public void setCircumference(double circumference) {
        this.circumference = circumference;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public void setOrbitLength(double orbitLength) {
        this.orbitLength = orbitLength;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public void setDistanceSun(Double distanceSun) {
        this.distanceSun = distanceSun;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }
}
