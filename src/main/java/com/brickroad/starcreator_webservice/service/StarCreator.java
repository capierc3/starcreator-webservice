package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.model.Star;
import com.brickroad.starcreator_webservice.model.enums.StarType;
import com.brickroad.starcreator_webservice.utils.CreatorUtils;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import com.brickroad.starcreator_webservice.utils.SpaceUtils;

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

}
