package com.brickroad.starcreator_webservice.enums;

import lombok.Getter;

/**
 * How much total liquid a celestial body possesses, independent of its surface state.
 * A frozen planet can have ABUNDANT liquid reserves locked as ice.
 */
@Getter
public enum LiquidInventory {
    NONE("None", "No detectable liquid in any form"),
    TRACE("Trace", "Minimal liquid - thin frost deposits, bound in minerals"),
    SCARCE("Scarce", "Limited liquid - small ice caps, thin subsurface deposits"),
    MODERATE("Moderate", "Significant liquid - substantial ice caps or moderate surface coverage"),
    ABUNDANT("Abundant", "Large liquid reserves - major surface coverage or deep ice mantles"),
    OCEAN_WORLD("Ocean World", "Liquid-dominated surface - global or near-global ocean");

    private final String displayName;
    private final String description;

    LiquidInventory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
