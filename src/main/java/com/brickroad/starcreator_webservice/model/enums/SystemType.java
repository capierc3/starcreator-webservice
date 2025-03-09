package com.brickroad.starcreator_webservice.model.enums;

import com.brickroad.starcreator_webservice.model.Star;
import com.brickroad.starcreator_webservice.utils.RandomUtils;

public enum SystemType {

    SINGLE(1),
    BINARY(2),
    TRINARY(3);

    private final int starCount;

    SystemType(int starCount) {
        this.starCount = starCount;
    }

    private static final int[] LOW_MASS_WEIGHTS = {75,25,0};
    private static final int[] SUN_LIKE_WEIGHTS = {55,40,5};
    private static final int[] HIGH_MASS_WEIGHTS = {20,50,30};

    public static final double MAX_LOW_MASS = .5;
    public static final int MIN_HIGH_MASS = 5;

    public static SystemType getRandomSystemType(Star star) {
        if (star.getSolarMass() < MAX_LOW_MASS) {
            return getWeightedSystemType(LOW_MASS_WEIGHTS);
        } else if (star.getSolarMass() > MIN_HIGH_MASS) {
            return getWeightedSystemType(HIGH_MASS_WEIGHTS);
        } else {
            return getWeightedSystemType(SUN_LIKE_WEIGHTS);
        }
    }

    public int getStarCount() {
        return starCount;
    }

    private static SystemType getWeightedSystemType(int[] weights) {
        int totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }
        int random = RandomUtils.rollRange(1, totalWeight);
        int cumulativeWeight = 0;
        for (int i = 0; i < weights.length; i++) {
            cumulativeWeight += weights[i];
            if (random <= cumulativeWeight) {
                return SystemType.values()[i];
            }
        }
        throw new IllegalStateException("Failed to select a weighted enum.");
    }

}
