package com.brickroad.starcreator_webservice.model.constants;

import io.cucumber.java.it.Ma;

import java.util.LinkedHashMap;

public class PlanetConstants {

    public static final LinkedHashMap<String,Double[]> DENSITY_RATINGS = new LinkedHashMap<>();
    public static final LinkedHashMap<String, Object[]> SIZE_VALUES = new LinkedHashMap<>();
    public static final LinkedHashMap<String, Integer[]> ATMOSPHERIC_PRESSURE = new LinkedHashMap<>();
    public static final LinkedHashMap<String, Integer[]> TILTS = new LinkedHashMap<>();
    public static final LinkedHashMap<Integer, Integer[]> LAND_COVER_LIQUID = new LinkedHashMap<>();
    public static final double EARTH_MASS = 5.972 * Math.pow(10,24);
    public static final double EARTH_RADIUS = 6.371 * Math.pow(10,6);
    public static final double EARTH_DENSITY = 5.51;// g cm^3
    public static final double EARTH_GRAVITY = 9.81;

    static {
        DENSITY_RATINGS.put("Negligible", new Double[]{0.0, 0.0});
        DENSITY_RATINGS.put("Very Low", new Double[]{0.01, 0.04});
        DENSITY_RATINGS.put("Low", new Double[]{0.05, 0.10});
        DENSITY_RATINGS.put("Light", new Double[]{0.2, 0.4});
        DENSITY_RATINGS.put("Below Average", new Double[]{0.5, 0.7});
        DENSITY_RATINGS.put("Average", new Double[]{0.8, 1.2});
        DENSITY_RATINGS.put("Above Average", new Double[]{1.3, 1.7});
        DENSITY_RATINGS.put("Heavy", new Double[]{1.8, 2.0});
        DENSITY_RATINGS.put("Very Heavy", new Double[]{2.1, 2.5});
        DENSITY_RATINGS.put("Massive", new Double[]{2.6, 2.7});
        DENSITY_RATINGS.put("Enormous", new Double[]{2.8, 3.0});
        DENSITY_RATINGS.put("Extreme", new Double[]{3.0, 3.0});

        SIZE_VALUES.put("Minuscule", new Object[] {1,10.0,1500});
        SIZE_VALUES.put("Tiny", new Object[] {2,10.0,1000});
        SIZE_VALUES.put("Small", new Object[] {4,10.0,1000});
        SIZE_VALUES.put("Average", new Object[] {10,10.0,1000});
        SIZE_VALUES.put("Large", new Object[] {10,10.0,2000});
        SIZE_VALUES.put("Huge", new Object[] {10,10.0,3000});
        SIZE_VALUES.put("Enormous", new Object[] {10,10.0,4000});
        SIZE_VALUES.put("Massive", new Object[] {10,10.0,5000});

        ATMOSPHERIC_PRESSURE.put("Negligible/None", new Integer[]{0,1});
        ATMOSPHERIC_PRESSURE.put("Trace", new Integer[]{1,2});
        ATMOSPHERIC_PRESSURE.put("Light", new Integer[]{2,4});
        ATMOSPHERIC_PRESSURE.put("Thin", new Integer[]{4,6});
        ATMOSPHERIC_PRESSURE.put("Thinner",new Integer[]{6,8});
        ATMOSPHERIC_PRESSURE.put("Below Standard", new Integer[]{8,10});
        ATMOSPHERIC_PRESSURE.put("Standard", new Integer[]{10,20});
        ATMOSPHERIC_PRESSURE.put("High", new Integer[]{20,40});
        ATMOSPHERIC_PRESSURE.put("Thick", new Integer[]{40,80});
        ATMOSPHERIC_PRESSURE.put("Slightly Dense", new Integer[]{80,100});
        ATMOSPHERIC_PRESSURE.put("Dense", new Integer[]{100,200});
        ATMOSPHERIC_PRESSURE.put("Very Dense", new Integer[]{200,300});
        ATMOSPHERIC_PRESSURE.put("Super Dense", new Integer[]{300,400});
        ATMOSPHERIC_PRESSURE.put("Ultra Dense", new Integer[]{40,40});

        TILTS.put("None", new Integer[]{0,0});
        TILTS.put("Slight", new Integer[]{0,4});
        TILTS.put("Minor", new Integer[]{5,14});
        TILTS.put("Notable", new Integer[]{15,24});
        TILTS.put("Moderate", new Integer[]{25,34});
        TILTS.put("Large", new Integer[]{35,44});
        TILTS.put("Great", new Integer[]{45,54});
        TILTS.put("Severe", new Integer[]{55,64});
        TILTS.put("Huge", new Integer[]{65,74});
        TILTS.put("Extreme", new Integer[]{75,90});

        LAND_COVER_LIQUID.put(1, new Integer[]{0,0});
        LAND_COVER_LIQUID.put(2, new Integer[]{1,5});
        LAND_COVER_LIQUID.put(3, new Integer[]{6,15});
        LAND_COVER_LIQUID.put(4, new Integer[]{16,25});
        LAND_COVER_LIQUID.put(5, new Integer[]{26,35});
        LAND_COVER_LIQUID.put(6, new Integer[]{36,45});
        LAND_COVER_LIQUID.put(7, new Integer[]{46,55});
        LAND_COVER_LIQUID.put(8, new Integer[]{56,65});
        LAND_COVER_LIQUID.put(9, new Integer[]{66,75});
        LAND_COVER_LIQUID.put(10, new Integer[]{76,85});
        LAND_COVER_LIQUID.put(11, new Integer[]{86,95,});
        LAND_COVER_LIQUID.put(12, new Integer[]{95,100});

    }

}
