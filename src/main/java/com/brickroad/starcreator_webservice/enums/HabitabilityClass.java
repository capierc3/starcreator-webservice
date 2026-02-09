package com.brickroad.starcreator_webservice.enums;

import lombok.Getter;

/**
 * Top-level habitability classification for planets and moons.
 */
@Getter
public enum HabitabilityClass {
    EARTH_ANALOG("Earth Analog", "ESI>0.9, breathable atmosphere, liquid water, magnetic protection"),
    HABITABLE_MARGINAL("Marginally Habitable", "ESI>0.7, habitable but challenging conditions"),
    BIOSPHERE_POSSIBLE("Biosphere Possible", "Microbial life possible, not human-breathable"),
    SUBSURFACE_HABITABLE("Subsurface Habitable", "Surface hostile, subsurface ocean possible"),
    TERRAFORMABLE_EASY("Easily Terraformable", "Right size and zone, needs atmosphere work"),
    TERRAFORMABLE_HARD("Difficult to Terraform", "Major obstacles but physically possible"),
    EXTREME_ENVIRONMENT("Extreme Environment", "Too hot, cold, or toxic for terraforming"),
    RESOURCE_WORLD("Resource World", "Useful for mining or industry only"),
    INHOSPITABLE("Inhospitable", "Gas giants, lava worlds, no practical use for habitation"),
    STERILIZED("Sterilized", "In habitable zone but stellar environment prevents habitability");

    private final String displayName;
    private final String description;

    HabitabilityClass(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
