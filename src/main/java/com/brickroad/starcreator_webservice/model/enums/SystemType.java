package com.brickroad.starcreator_webservice.model.enums;

import com.brickroad.starcreator_webservice.model.Star;

import static com.brickroad.starcreator_webservice.utils.CreatorUtils.*;

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

    public static SystemType getRandomSystemType(Star star) {
        if (star.getSolarMass() < MAX_LOW_MASS) {
            return SystemType.values()[getWeightedEnumIndex(LOW_MASS_WEIGHTS)];
        } else if (star.getSolarMass() > MIN_HIGH_MASS) {
            return SystemType.values()[getWeightedEnumIndex(HIGH_MASS_WEIGHTS)];
        } else {
            return SystemType.values()[getWeightedEnumIndex(SUN_LIKE_WEIGHTS)];
        }
    }

    public int getStarCount() {
        return starCount;
    }
}
