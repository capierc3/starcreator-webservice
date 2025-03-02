package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.model.Star;
import com.brickroad.starcreator_webservice.model.enums.StarType;
import com.brickroad.starcreator_webservice.utils.CreatorUtils;
import com.brickroad.starcreator_webservice.utils.RandomUtils;

public class StarCreator {

    private static Star star;

    public static Star createStar() {
        star = new Star();
        star.setName(CreatorUtils.generateSystemName());
        star.setStarType(StarType.getWeightedRandom());
        generateStarData();
        return star;
    }

    public static Star createStar(String name) {
        star = new Star();
        star.setName(name);
        star.setStarType(StarType.getWeightedRandom());
        generateStarData();
        return star;
    }

    public static Star createStar(StarType starType) {
        star = new Star();
        star.setName(CreatorUtils.generateSystemName());
        star.setStarType(starType);
        generateStarData();
        return star;
    }

    public static Star createStar(StarType starType, String name) {
        star = new Star();
        star.setName(name);
        star.setStarType(starType);
        generateStarData();
        return star;
    }

    private static void generateStarData() {
        star.setDescription(star.getStarType().getDescription());
        generateMass();
        generateRadius();
        generateTemp();
    }

    private static void generateMass() {
        double massLow = star.getStarType().getMass()[0];
        double massHigh = star.getStarType().getMass()[1];
        star.setSolarMass(RandomUtils.rollRange(massLow, massHigh));
    }

    private static void generateRadius() {
        double radiusLow = star.getStarType().getRadius()[0];
        double radiusHigh = star.getStarType().getRadius()[1];
        star.setSolarRadius(RandomUtils.rollRange(radiusLow, radiusHigh));
    }
    private static void generateTemp() {
        double tempLow = star.getStarType().getTemperature()[0];
        double tempHigh = star.getStarType().getTemperature()[1];
        star.setTemperature(RandomUtils.rollRange(tempLow,tempHigh) * 1000);
    }

}
