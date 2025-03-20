package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.model.Star;
import com.brickroad.starcreator_webservice.model.enums.StarType;
import com.brickroad.starcreator_webservice.utils.CreatorUtils;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import com.brickroad.starcreator_webservice.utils.SpaceUtils;
import io.cucumber.java.it.Ma;

import static com.brickroad.starcreator_webservice.utils.SpaceUtils.solarRadiusToStellarRadius;

public class StarCreator {

    private static Star star;

    public static Star createStar(StarType starType, String name, boolean isHabitable) {
        star = new Star();
        star.setName(name == null ? CreatorUtils.generateSystemName() : name);
        if (starType == null && isHabitable) {
            star.setStarType(StarType.getHabitableRandom());
        } else {
            star.setStarType(starType == null ? StarType.getWeightedRandom() : starType);
        }
        generateStarData();
        return star;
    }

    private static void generateStarData() {
        star.setDescription(star.getStarType().getDescription());
        star.setSolarRadius(RandomUtils.rollRange(star.getStarType().getMinRadius(), star.getStarType().getMaxRadius()));
        star.setSolarMass(RandomUtils.rollRange(star.getStarType().getMinMass(), star.getStarType().getMaxMass()));
        star.setTemperature(RandomUtils.rollRange(star.getStarType().getMinTemperature(),star.getStarType().getMaxTemperature()) * 100);
        star.setLifeSupporting(star.getStarType().getHabitableZone());
        star.setDistToBarycenter(0);
        generateLuminosityData(star);
        generateBoundaries(star);

    }

    private static void generateLuminosityData(Star star) {
        double tempRatio = star.getTemperature() / SpaceUtils.SUNS_TEMPERATURE;
        double luminosity = Math.pow(star.getSolarRadius(), 2) * Math.pow(tempRatio, 4);
        star.setLuminosity(luminosity);
        double in = Math.sqrt(luminosity) * 0.95;
        double out = Math.sqrt(luminosity) * 1.37;
        star.setMinHabitableZone(in);
        star.setMaxHabitableZone(out);
    }

    private static void generateBoundaries(Star star) {
        //inner disk
        double innerDiskEdge = solarRadiusToStellarRadius(star.getSolarRadius(), SpaceUtils.DistUnits.AU) / 2;
        double tempOverSublimationSquare = star.getTemperature() / SpaceUtils.SUBLIMATION_TEMP;
        tempOverSublimationSquare = Math.pow(tempOverSublimationSquare, 2);
        star.setInnerDisk(innerDiskEdge * tempOverSublimationSquare);
        //Pebble Isolation Line
        double c, a;
        if (star.getSolarMass() >= 20) {
            c = 8;
            a = .7;
        } else if (star.getSolarMass() >= 2) {
            c = 6;
            a = .7;
        } else if (star.getSolarMass() >= .3) {
            c = 5;
            a = (2d/3);
        } else {
            c = 3.5;
            a = .6;
        }
        double pebbleIsoLine = Math.pow(star.getSolarMass(),a);
        star.setPebbleIsoLine(pebbleIsoLine * c);
        //Ice Lines
        star.setSnowLine(getIceLine(star, SpaceUtils.IceLineGas.H20));
        star.setIceLineCO2(getIceLine(star, SpaceUtils.IceLineGas.CO2));
        star.setIceLineMethane(getIceLine(star, SpaceUtils.IceLineGas.METHANE));
        star.setIceLineCO(getIceLine(star, SpaceUtils.IceLineGas.CO));

    }

    private static double getIceLine(Star star, SpaceUtils.IceLineGas iceLineGas) {
        double condensationTempConstant = switch (iceLineGas) {
            case H20 -> 2.7;
            case CO2 -> 11;
            case METHANE -> 20;
            case CO -> 40;
        };
        return condensationTempConstant * Math.pow(star.getLuminosity(), .5);
    }

}
