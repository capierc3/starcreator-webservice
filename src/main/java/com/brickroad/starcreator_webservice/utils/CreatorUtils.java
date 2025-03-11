package com.brickroad.starcreator_webservice.utils;

import static com.brickroad.starcreator_webservice.utils.RandomUtils.getRandomStringFromTxt;

public class CreatorUtils {

    public static final String SYSTEM_NAMES_PREFIX_TXT = "static/SystemNamesPrefix.txt";
    public static final String SYSTEM_NAMES_SUFFIX_TXT = "static/SystemNamesSuffix.txt";
    public static final double MAX_LOW_MASS = .5;
    public static final int MIN_HIGH_MASS = 5;

    public static String generateSystemName(){
        return generateSectorName() + " " +
                getRandomStringFromTxt(SYSTEM_NAMES_SUFFIX_TXT);
    }

    public static String generateSystemName(String sectorName){
        return sectorName + " " +
                getRandomStringFromTxt(SYSTEM_NAMES_SUFFIX_TXT);
    }

    public static String generateSectorName(){
        return getRandomStringFromTxt(SYSTEM_NAMES_PREFIX_TXT);
    }

    public static int getWeightedIndex(int[] weights){
        int totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }
        int random = RandomUtils.rollRange(1, totalWeight);
        int cumulativeWeight = 0;
        for (int i = 0; i < weights.length; i++) {
            cumulativeWeight += weights[i];
            if (random <= cumulativeWeight) {
                return i;
            }
        }
        throw new IllegalStateException("Failed to select a weighted enum index.");
    }

}
