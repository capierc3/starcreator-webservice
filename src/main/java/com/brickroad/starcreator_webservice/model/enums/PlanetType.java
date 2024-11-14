package com.brickroad.starcreator_webservice.model.enums;

import com.brickroad.starcreator_webservice.utils.RandomUtils;

public enum PlanetType {

    DWARF("Dwarf Planet", 1,2),
    GAS("Gas Planet",6,10),
    TERRESTRIAL("Terrestrial Planet",1,9);

    private final String name;
    private final int sizeMin;
    private final int sizeMax;

    PlanetType(String value, int sizeMin, int sizeMax) {
        this.name = value;
        this.sizeMin = sizeMin;
        this.sizeMax = sizeMax;
    }

    public static PlanetType getEnum(String value) {
        for(PlanetType v : values())
            if(v.getName().equalsIgnoreCase(value)) return v;
        throw new IllegalArgumentException();
    }

    public static PlanetType getRandom() {
        return PlanetType.values()[RandomUtils.Roller(1,PlanetType.values().length) - 1];
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



}
