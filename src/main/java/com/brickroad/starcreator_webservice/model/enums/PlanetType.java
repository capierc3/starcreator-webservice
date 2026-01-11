package com.brickroad.starcreator_webservice.model.enums;

import com.brickroad.starcreator_webservice.utils.RandomUtils;

public enum PlanetType {

    TERRESTRIAL("Terrestrial Planet",1,9, .05,2, .3,1.5),
    SUPER_EARTH("Super Earth Planet",1,9,2,10,1.5,2),
    WATER_WORLDS("Water World Planet",1,9,2,10,1.5,2),
    ICE_GIANT("Ice Giant Planet",1,9,10,30,4,6),
    GAS_GIANT("Gas Planet",6,10,30,300,6,15),
    SUPER_JOVIAN("Super Jovian Planet",1,9,300,1300,15,50),
    DWARF("Dwarf Planet", 1,2,.00016,.0028,.1,.7);

    private final String name;
    private final int sizeMin;
    private final int sizeMax;
    private final double minMass;
    private final double maxMass;
    private final double minRadius;
    private final double maxRadius;

    PlanetType(String value, int sizeMin, int sizeMax, double minMass, double maxMass, double minRadius, double maxRadius) {
        this.name = value;
        this.sizeMin = sizeMin;
        this.sizeMax = sizeMax;
        this.minMass = minMass;
        this.maxMass = maxMass;
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
    }

    public static PlanetType getEnum(String value) {
        for(PlanetType v : values())
            if(v.getName().equalsIgnoreCase(value)) return v;
        throw new IllegalArgumentException();
    }

    public static PlanetType getRandom() {
        return PlanetType.values()[RandomUtils.rollRange(0,6)];
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public int getSizeMin() {
        return sizeMin;
    }

    public int getSizeMax() {
        return sizeMax;
    }

    public double getMinMass() {
        return minMass;
    }

    public double getMaxMass() {
        return maxMass;
    }

    public double getMinRadius() {
        return minRadius;
    }

    public double getMaxRadius() {
        return maxRadius;
    }
}
