package com.brickroad.starcreator_webservice.enums;

import lombok.Getter;

/**
 * Practical assessment of whether humans can live on this world.
 */
@Getter
public enum ColonizationSuitability {
    SHIRT_SLEEVE("Shirt-Sleeve", "Walk outside with no equipment"),
    ASSISTED("Assisted", "Need supplemental O2 or light pressure suit"),
    DOME_ONLY("Dome Only", "Must live in enclosed habitats"),
    UNINHABITABLE("Uninhabitable", "Cannot sustain permanent human presence");

    private final String displayName;
    private final String description;

    ColonizationSuitability(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
