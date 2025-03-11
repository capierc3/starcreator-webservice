package com.brickroad.starcreator_webservice.model.enums;

import com.brickroad.starcreator_webservice.utils.RandomUtils;

import static com.brickroad.starcreator_webservice.model.enums.HabitableZone.*;
import static com.brickroad.starcreator_webservice.utils.CreatorUtils.getWeightedEnumIndex;

public enum StarType {
    PROTO("Collapsing gas cloud", .08, 200, 1, 5, 3, 100, NOT_VIABLE),
    T_TAURI("Pre-main sequence", .08, 3, 3, 6, 1, 5, NOT_VIABLE),
    MAIN_SEQ_O("Massive Blue", 16, 32, 300, 500, 6, 15, NOT_VIABLE),
    MAIN_SEQ_B("Massive Blue-White", 2.1, 16, 100, 300, 2, 6, NOT_VIABLE),
    MAIN_SEQ_A("White Star", 1.4, 2.1, 75, 100, 1.4, 2, POOR),
    MAIN_SEQ_F("Yellow-White Star", 1.04, 1.4, 60, 75, 1.1, 1.3, POSSIBLE),
    MAIN_SEQ_G("Sun-like", .8, 1.04, 52, 60, .9, 1.1, GOOD),
    MAIN_SEQ_K("Orange Dwarf", .45, .8, 37, 52, .7, .9, EXCELLENT),
    MAIN_SEQ_M("Red Dwarf", .08, .45, 25, 37, .1, .6, MIXED),
    WHITE_DWARF("White Dwarf", .17, 1, 4, 150, .008, .02, NOT_VIABLE),
    NEUTRON("Neutron", 1.1, 2.3, 100, 100000, 0.000014, 0.00002, NOT_VIABLE),
    SUPER_GIANT("Super Giant", 8, 100, 3, 50, 30, 1500, NOT_VIABLE);

    private final String description;
    private final double minMass;
    private final double maxMass;
    private final double minTemperature;
    private final double maxTemperature;
    private final double minRadius;
    private final double maxRadius;
    private final HabitableZone habitableZone;

    private final static int[] WEIGHTS = {1000,100,1,1000,6000,30000,70000,120000,750000,60000,1000,1};
    private final static int[] HABITABLE_WEIGHTS = {0,0,0,0,6,30,70,120,750,0,0,0};

    StarType(String description, double minMass, double maxMass, double minTemperature, double maxTemperature, double minRadius, double maxRadius,  HabitableZone habitableZone) {
        this.description = description;
        this.minMass = minMass;
        this.maxMass = maxMass;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        this.habitableZone = habitableZone;
    }

    public String getDescription() {
        return description;
    }

    public static StarType getWeightedRandom() {
        return StarType.values()[getWeightedEnumIndex(WEIGHTS)];
    }

    public static StarType getHabitableRandom() {
        return StarType.values()[getWeightedEnumIndex(HABITABLE_WEIGHTS)];
    }

    public static StarType getStarTypeBelow(StarType starType) {
        return StarType.values()[Math.floorMod((getStarTypeIndex(starType) - 1),StarType.values().length)];
    }

    public static StarType getStarTypeAbove(StarType starType) {
        return StarType.values()[(getStarTypeIndex(starType) + 1) % StarType.values().length];
    }

    public double getMinMass() {
        return minMass;
    }

    public double getMaxMass() {
        return maxMass;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public double getMinRadius() {
        return minRadius;
    }

    public double getMaxRadius() {
        return maxRadius;
    }

    public HabitableZone getHabitableZone() {
        return habitableZone;
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
