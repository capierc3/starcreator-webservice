package com.brickroad.starcreator_webservice.utils;

public class ConversionFormulas {

    public static final double solMass = 1.989e30;
    public static final double solRadius = 696340.0;
    public static final double AU_TO_KM = 149_597_870.7;
    public static final double AU_TO_METERS = 149_597_870_700.0;
    public static final double GRAVITATIONAL_CONSTANT = 6.67430e-11;

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
