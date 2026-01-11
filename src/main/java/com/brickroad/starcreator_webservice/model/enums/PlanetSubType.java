package com.brickroad.starcreator_webservice.model.enums;

public enum PlanetSubType {

    IRON_RICH(7.5),
    EARTH_LIKE(5),
    WATER_RICH(2);

    private final double densityMin;

    PlanetSubType(double densityMin) {
        this.densityMin = densityMin;
    }

    public double getDensityMin() {
        return densityMin;
    }

    public static PlanetSubType getPlanetTypeByDensity(double densityMin) {
        for (PlanetSubType planetSubType : PlanetSubType.values()) {
            if (planetSubType.getDensityMin() <= densityMin) {
                return planetSubType;
            }
        }
        return null;
    }

}
