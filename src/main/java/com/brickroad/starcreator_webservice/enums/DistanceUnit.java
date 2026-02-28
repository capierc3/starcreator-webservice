package com.brickroad.starcreator_webservice.enums;

/**
 * Distance unit for orbital semi-major axis values.
 * <p>
 * Bodies orbiting stars (planets, asteroids, belt edges) use AU.
 * Bodies orbiting planets (moons, ring edges, stations) use KM.
 */
public enum DistanceUnit {

    AU("Astronomical Units — star-orbiting bodies"),
    KM("Kilometers — planet-orbiting bodies");

    private final String description;

    DistanceUnit(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
