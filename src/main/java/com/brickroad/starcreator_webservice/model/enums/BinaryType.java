package com.brickroad.starcreator_webservice.model.enums;

import com.brickroad.starcreator_webservice.model.Star;

import static com.brickroad.starcreator_webservice.utils.CreatorUtils.MAX_LOW_MASS;
import static com.brickroad.starcreator_webservice.utils.CreatorUtils.MIN_HIGH_MASS;
import static com.brickroad.starcreator_webservice.utils.CreatorUtils.getWeightedEnumIndex;

public enum BinaryType {

    WIDE(100,30000, .3, .9,90),
    INTERMEDIATE(10,100,.1,.8,70),
    CLOSE(.1,10,0,.5,40),
    CONTACT(0,.1,0,.2,5);

    private final double auMin;
    private final double auMax;
    private final double minEccentricity;
    private final double maxEccentricity;
    private final double percentChanceEccentricity;

    private static final int[] LOW_MASS_WEIGHTS = {5,5,15,1};
    private static final int[] NORMAL_WEIGHTS = {30,30,35,5};
    private static final int[] HIGH_MASS_WEIGHTS = {10,20,50,1};

    BinaryType(double auMin, double auMax, double minEccentricity, double maxEccentricity, double percentChanceEccentricity) {
        this.auMin = auMin;
        this.auMax = auMax;
        this.minEccentricity = minEccentricity;
        this.maxEccentricity = maxEccentricity;
        this.percentChanceEccentricity = percentChanceEccentricity;
    }

    public static BinaryType getRandomBinaryType(Star star) {
        if (star.getSolarMass() < MAX_LOW_MASS) {
            return BinaryType.values()[getWeightedEnumIndex(LOW_MASS_WEIGHTS)];
        } else if (star.getSolarMass() > MIN_HIGH_MASS) {
            return BinaryType.values()[getWeightedEnumIndex(HIGH_MASS_WEIGHTS)];
        } else {
            return BinaryType.values()[getWeightedEnumIndex(NORMAL_WEIGHTS)];
        }
    }

    public double getAuMin() {
        return auMin;
    }

    public double getAuMax() {
        return auMax;
    }

    public double getMinEccentricity() {
        return minEccentricity;
    }

    public double getMaxEccentricity() {
        return maxEccentricity;
    }

    public double getPercentChanceEccentricity() {
        return percentChanceEccentricity;
    }
}
