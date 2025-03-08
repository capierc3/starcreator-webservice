package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.model.Star;
import com.brickroad.starcreator_webservice.model.enums.StarType;
import com.brickroad.starcreator_webservice.utils.CreatorUtils;
import com.brickroad.starcreator_webservice.utils.RandomUtils;

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
        star.setSolarMass(RandomUtils.rollRange(star.getStarType().getMinMass(), star.getStarType().getMaxMass()));
        star.setSolarRadius(RandomUtils.rollRange(star.getStarType().getMinRadius(), star.getStarType().getMaxRadius()));
        star.setTemperature(RandomUtils.rollRange(star.getStarType().getMinTemperature(),star.getStarType().getMaxTemperature()) * 1000);
        star.setMinHabitableZone(star.getStarType().getHabitableZone().getMinHabitableZone());
        star.setMaxHabitableZone(star.getStarType().getHabitableZone().getMaxHabitableZone());
        star.setLifeSupporting(star.getStarType().getHabitableZone());
    }

}
