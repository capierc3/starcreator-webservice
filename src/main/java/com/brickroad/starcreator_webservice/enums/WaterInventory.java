package com.brickroad.starcreator_webservice.enums;

import lombok.Getter;

/**
 * How much total water a planet possesses, independent of its surface state.
 * A frozen planet can have ABUNDANT water locked as ice.
 */
@Getter
public enum WaterInventory {
    NONE("None", "No detectable water in any form"),
    TRACE("Trace", "Minimal water - thin frost deposits, bound in minerals"),
    SCARCE("Scarce", "Limited water - small ice caps, thin subsurface deposits"),
    MODERATE("Moderate", "Significant water - substantial ice caps or moderate oceans"),
    ABUNDANT("Abundant", "Large water reserves - major oceans or deep ice mantles"),
    OCEAN_WORLD("Ocean World", "Water-dominated surface - global or near-global ocean");

    private final String displayName;
    private final String description;

    WaterInventory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
