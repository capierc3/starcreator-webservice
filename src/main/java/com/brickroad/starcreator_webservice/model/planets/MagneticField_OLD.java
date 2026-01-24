package com.brickroad.starcreator_webservice.model.planets;

import com.brickroad.starcreator_webservice.utils.RandomUtils;

public class MagneticField_OLD {

    private final String variation;
    private final int fluxPeriodHrs;
    private final double fluxPeekMicroteslas;
    private final double fluxLowMicroteslas;
    private final double microteslasHigh;
    private final double microteslasLow;
    private final double comparedToEarthStrength;

    public static final double EARTH_MICROTESLAS_LOW = 30.0;
    public static final double EARTH_MICROTESLAS_HIGH = 60.0;
    public static final double EARTH_MICROTESLAS_AVG = 45.0;
    public static final Object[][] MAGNETIC_FIELD = new Object[][]{
            {49, "No regional variance"}
            ,{52,"Higher at North Pole"}
            ,{55,"Higher at South Pole"}
            ,{59,"Higher at Both Poles"}
            ,{70,"Higher at Equator"}
            ,{75,"Higher in Random Spots"}
            ,{80,"Fluxing - Poles"}
            ,{85,"Fluxing - Equator"}
            ,{90,"Fluxing - Random Spots"}
            ,{96,"Fluxing - Whole Planet"}
            ,{97,"Unstable - Poles"}
            ,{98,"Unstable - Equator"}
            ,{99,"Unstable - Random Spots"}
            ,{100,"Unstable - Whole Planet"}
    };

    public MagneticField_OLD(int densityRating, double rotationInHrs) {
        double rotationSpeedCompared = 24/rotationInHrs;
        int randomNum = ((RandomUtils.rollRange(-8,0)) + (densityRating));
        if (randomNum <= 0) {
            comparedToEarthStrength = 0;
            microteslasLow = 0;
            microteslasHigh = 0;
            variation = (String) MAGNETIC_FIELD[0][1];
            fluxPeriodHrs = 0;
            fluxLowMicroteslas = 0;
            fluxPeekMicroteslas = 0;
        } else {
            double start = (1.0 / 32.0) * rotationSpeedCompared;
            double high = Math.pow(2, randomNum) * start;
            double low = Math.pow(2, (randomNum - 1)) * start;
            comparedToEarthStrength = RandomUtils.rollRange(low, high);
            microteslasLow = EARTH_MICROTESLAS_LOW * comparedToEarthStrength;
            microteslasHigh = EARTH_MICROTESLAS_HIGH * comparedToEarthStrength;
            variation = RandomUtils.getStringFromArray(MAGNETIC_FIELD);
            if (variation.contains("Fluxing")) {
                fluxPeekMicroteslas = (EARTH_MICROTESLAS_AVG * comparedToEarthStrength) * 2;
                fluxLowMicroteslas = (EARTH_MICROTESLAS_AVG * comparedToEarthStrength) * (RandomUtils.rollDice(60) * .01);
                fluxPeriodHrs = RandomUtils.rollDice(2400);
            } else {
                fluxPeriodHrs = 0;
                fluxLowMicroteslas = 0;
                fluxPeekMicroteslas = 0;
            }
        }
    }

    public String getVariation() {
        return variation;
    }

    public int getFluxPeriodHrs() {
        return fluxPeriodHrs;
    }

    public double getFluxPeekMicroteslas() {
        return fluxPeekMicroteslas;
    }

    public double getFluxLowMicroteslas() {
        return fluxLowMicroteslas;
    }

    public double getMicroteslasHigh() {
        return microteslasHigh;
    }

    public double getMicroteslasLow() {
        return microteslasLow;
    }

    public double getComparedToEarthStrength() {
        return comparedToEarthStrength;
    }
}
