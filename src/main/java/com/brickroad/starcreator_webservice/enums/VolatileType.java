package com.brickroad.starcreator_webservice.enums;

import lombok.Getter;

/**
 * The dominant volatile substance on a celestial body's surface or subsurface.
 * Determines the liquid/ice cycle — temperature and pressure decide the phase.
 * Each type stores its base phase-change temperatures at 1 bar
 * and default surface colors for rendering.
 */
@Getter
public enum VolatileType {
    WATER("Water", 273.16, 373.15, "#3388aa", "#4499bb"),
    AMMONIA_WATER("Ammonia-Water", 195.0, 373.15, "#445588", "#556699"),
    AMMONIA("Ammonia", 195.4, 239.8, "#4455aa", "#5566bb"),
    METHANE("Methane", 90.7, 111.7, "#886633", "#997744"),
    METHANE_ETHANE("Methane-Ethane", 90.7, 184.0, "#775522", "#886633"),
    NONE("None", 0, 0, null, null);

    private final String displayName;
    private final double baseFreezeK;
    private final double baseBoilK;
    private final String defaultColorPrimary;
    private final String defaultColorSecondary;

    VolatileType(String displayName, double baseFreezeK, double baseBoilK,
                 String defaultColorPrimary, String defaultColorSecondary) {
        this.displayName = displayName;
        this.baseFreezeK = baseFreezeK;
        this.baseBoilK = baseBoilK;
        this.defaultColorPrimary = defaultColorPrimary;
        this.defaultColorSecondary = defaultColorSecondary;
    }
}
