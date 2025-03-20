package com.brickroad.starcreator_webservice.utils;

import com.brickroad.starcreator_webservice.model.Star;

import java.math.BigDecimal;

public class SpaceUtils {

    private static final BigDecimal METERS_TO_AU = new BigDecimal("149597870691.0");
    private static final double GRAVITY = 9.8;
    private static final double AU_TO_LS = 498.66;
    public static final double STEFAN_BOLTZMANN_LAW = 5.67037442;
    public static final double ONE_SOLAR_RADIUS_IN_METERS = 6.955 * Math.pow(10,8);
    public static final double SUNS_TEMPERATURE = 5778;
    public static final double SUNS_LUMINOSITY = 3.828;
    public static final double SUNS_RADIUS_IN_KM = 695700;
    public static final double SUNS_RADIUS_IN_AU = .00465;
    public static final double SUBLIMATION_TEMP = 1500;
    public static final double MASS_OF_SON = 333000;

    public enum DistUnits {KM, METER,AU,LY,LS}
    public enum TimeUnits {DAYS,HOURS,MINUTES,SECONDS}
    public enum IceLineGas {H20, CO2, METHANE, CO}

    public static double TimeToTravel(double distance,double speed, DistUnits dUnit,TimeUnits tUnits,boolean sling){
        if (!sling) {
            double tConvert = 1;
            double dConvert;
            tConvert = switch (tUnits) {
                case DAYS -> 86400;
                case HOURS -> 3600;
                case MINUTES -> 60;
                default -> tConvert;
            };
            dConvert = switch (dUnit) {
                case AU -> METERS_TO_AU.doubleValue() * distance;
                case LS -> (METERS_TO_AU.doubleValue() / AU_TO_LS) * distance;
                default -> METERS_TO_AU.doubleValue();
            };
            return Math.sqrt((2 * dConvert) / (speed*GRAVITY)) / tConvert;
        } else {
            return distance * 6;
        }
    }

    public static double ConvertAUtoLS(double au){
        return au*498.66;
    }

    public static double ConvertLStoAU(double ls){
        return ls/498.66;
    }

    public static double distanceToBarycenter(Star star1, Star star2, double distApart) {
        double combinedMass = star1.getSolarMass() + star2.getSolarMass();
        double massRatio = star1.getSolarMass() / combinedMass;
        return massRatio * distApart;
    }

    public static double solarRadiusToStellarRadius(double solarRadius, DistUnits dUnit) {
        if (dUnit == DistUnits.AU) {
            return solarRadius * SUNS_RADIUS_IN_AU;
        } else if (dUnit == DistUnits.KM) {
            return solarRadius * SUNS_RADIUS_IN_KM;
        } else {
            return 0;
        }
    }

}
