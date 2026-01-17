package com.brickroad.starcreator_webservice.utils;

public class ConversionFormulas {

    public static final double solMass = 1.989e30;
    public static final double solRadius = 696340.0;

    public static double solarMassToKG(double solarMass) {
        return solarMass * 1.989e30;
    }

    public static double solarRadiusToKM(double solarRadius) {
        return solarRadius * 696340.0;
    }

    public static double radiusToCircumference(double radiusKm) {
        return (2 * Math.PI * radiusKm);
    }



}
