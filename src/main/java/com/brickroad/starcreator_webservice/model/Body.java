package com.brickroad.starcreator_webservice.model;

import com.brickroad.starcreator_webservice.model.enums.AtmosphereType;
import com.brickroad.starcreator_webservice.model.enums.TerrainType;
import com.brickroad.starcreator_webservice.model.planets.MagneticField;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import com.brickroad.starcreator_webservice.utils.SpaceTravel;

import java.util.Map;

/**
 * Abstract class that holds all shared variables and methods for the different types of Bodies.
 * @author Chase Pierce
 * @version 1.0
 */
public abstract class Body implements Comparable {

    /**Body string Information*/
    protected String type;
    protected String name;
    protected String size;
    Map<TerrainType, Integer> terrain;
    MagneticField magneticField;
    String density;
    /**Body numeric information*/
    double circumference;
    double radius;
    protected double gravity;
    double temp;
    double orbitLength;
    int location;
    /**Distance from sun is in AUs*/
    Double distanceSun;
    public String systemName;

    /**Main Constructor that sets the orbit length of the body*/
    Body(){
        int roll = RandomUtils.rollDice(1,10);
        orbitLength = (RandomUtils.rollDice(10,10)*roll)/365.0;
    }

    /**
     * Setter for the temperature of the body
     * @param temp double
     */
    public void setTemp(double temp){
        this.temp=temp;
    }

    /**
     * Sets the location tag and distance to the sun value
     * @param loc int
     * @param distSun double
     */
    public void setLocation(int loc, double distSun){
        location = loc;
        distanceSun = distSun;
    }

    /**
     *Changes the name of the body by adding the value of its location after the name.
     * @param i int
     */
    public void setName(int i){
        this.name = name+" "+(i > 0 && i < 27 ? String.valueOf((char)(i + 64)) : null);
    }

    /**
     * Getter that returns the bodies name
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Getting that returns the bodies type
     * @return String
     */
    public String getType() {
        return type;
    }

    /**
     * getter that returns the distance to the sun value.
     * @return Double
     */
    public Double getDistanceSun() {
        return distanceSun;
    }

    public MagneticField getMagneticField() {
        return magneticField;
    }

    /**
     * Compares the Bodies by the distance form sun value
     * @param o Body
     * @return int
     */
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

    public Map<TerrainType, Integer> getTerrain() {
        return terrain;
    }
}
