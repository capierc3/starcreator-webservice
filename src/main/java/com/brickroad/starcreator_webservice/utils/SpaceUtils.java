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

    public enum DistUnits {METER,AU,LY,LS}
    public enum TimeUnits {DAYS,HOURS,MINUTES,SECONDS}

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

}
