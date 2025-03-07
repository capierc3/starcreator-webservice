package com.brickroad.starcreator_webservice.model.enums;

import com.brickroad.starcreator_webservice.utils.RandomUtils;

public enum StarType {
    PROTO("Collapsing gas cloud", new double[]{.08,200}, new double[]{1,5}, new double[]{3,100}),
    T_TAURI("Pre-main sequence", new double[]{.08,3}, new double[]{3,6}, new double[]{1,5}),
    MAIN_SEQ_O("Massive Blue", new double[]{16, 32}, new double[]{300,500}, new double[]{6,15}),
    MAIN_SEQ_B("Massive Blue-White", new double[]{2.1, 16}, new double[]{100,300}, new double[]{2,6}),
    MAIN_SEQ_A("White Star", new double[]{1.4,2.1}, new double[]{75,100}, new double[]{1.4,2}),
    MAIN_SEQ_F("Yellow-White Star", new double[]{1.04,1.4}, new double[]{60,75}, new double[]{1.1,1.3}),
    MAIN_SEQ_G("Sun-like", new double[]{.8,1.04}, new double[]{52,60}, new double[]{.9,1.1}),
    MAIN_SEQ_K("Orange Dwarf", new double[]{.45,.8}, new double[]{37,52}, new double[]{.7,.9}),
    MAIN_SEQ_M("Red Dwarf", new double[]{.08,.45}, new double[]{25,37}, new double[]{.1,.6}),
    WHITE_DWARF("White Dwarf", new double[]{.17,1}, new double[]{4,150}, new double[]{.008,.02}),
    NEUTRON("Neutron", new double[]{1.1,2.3}, new double[]{100,100000}, new double[]{0.000014,0.00002}),
    SUPER_GIANT("Super Giant", new double[]{8,100}, new double[]{3,50}, new double[]{30,1500});

    private final static int[] WEIGHTS = {1000,100,1,1000,6000,30000,70000,120000,750000,60000,1000,1};
    private final String description;
    private final double[] mass;
    private final double[] temperature;
    private final double[] radius;

    StarType(String description, double[] mass, double[] temperature, double[] radius) {
        this.description = description;
        this.mass = mass;
        this.temperature = temperature;
        this.radius = radius;
    }

    public String getDescription() {
        return description;
    }

    public double[] getMass() {
        return mass;
    }

    public double[] getTemperature() {
        return temperature;
    }

    public double[] getRadius() {
        return radius;
    }

    public static StarType getRandom() {
        return StarType.values()[RandomUtils.rollDice(1,StarType.values().length) - 1];
    }

    public static StarType getWeightedRandom() {
        int totalWeight = 0;
        for (int weight : WEIGHTS) {
            totalWeight += weight;
        }
        int random = RandomUtils.rollRange(1, totalWeight);
        int cumulativeWeight = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            cumulativeWeight += WEIGHTS[i];
            if (random <= cumulativeWeight) {
                return StarType.values()[i];
            }
        }
        throw new IllegalStateException("Failed to select a weighted random Star Type.");
    }

    public static StarType getStarTypeBelow(StarType starType) {
        return StarType.values()[Math.floorMod((getStarTypeIndex(starType) - 1),StarType.values().length)];
    }

    public static StarType getStarTypeAbove(StarType starType) {
        return StarType.values()[(getStarTypeIndex(starType) + 1) % StarType.values().length];
    }

    private static int getStarTypeIndex(StarType starType) {
        for (int i = 0; i < StarType.values().length; i++) {
            if (starType.equals(StarType.values()[i])) {
                return i;
            }
        }
        return -1;
    }

}
