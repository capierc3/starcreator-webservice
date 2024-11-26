package com.brickroad.starcreator_webservice.model.enums;

import com.brickroad.starcreator_webservice.utils.RandomUtils;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TerrainType {

    AQUATIC_DEEP("Aquatic - Deep"),
    AQUATIC_SHALLOW("Aquatic – Shallow"),
    ARCTIC_FROZEN("Arctic/Frozen"),
    CANYON("Canyon"),
    DESERT("Desert"),
    EXOTIC_OTHER("Exotic/”impossible”/Other"),
    FOREST("Forest"),
    GLACIER("Glacier"),
    GRASSLAND("Grassland"),
    HILLS("Hills"),
    ISLAND("Islands"),
    JUNGLE("Jungle"),
    MOUNTAINS("Mountains/Valleys"),
    PLAINS("Plains"),
    RIVERS_LAKES("Rivers/Lakes"),
    SCRUB_LAND("Scrubland"),
    SWAMP("Swamp/Bog"),
    TUNDRA("Tundra"),
    ARTIFICIAL("Unnatural/Artificial"),
    WASTE_LANDS("Wasteland/Devastation");

    private final String name;

    TerrainType(String name) {
        this.name = name;
    }

    public static TerrainType getRandom() {
        return TerrainType.values()[RandomUtils.rollDice(1,TerrainType.values().length) - 1];
    }

    @JsonValue
    public String getName() {
        return name;
    }
}
