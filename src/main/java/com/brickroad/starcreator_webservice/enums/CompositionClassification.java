package com.brickroad.starcreator_webservice.enums;

import lombok.Getter;

/**
 * Broad classification categories for planetary composition
 */
@Getter
public enum CompositionClassification {
    SILICATE_RICH("Silicate-Rich", "Earth-like silicate mantle with iron core"),
    IRON_RICH("Iron-Rich", "Metal-dominated stripped core"),
    CARBON_RICH("Carbon-Rich", "Carbon compounds dominate (graphite, diamond, carbides)"),
    ICE_RICH("Ice-Rich", "Volatile ices dominate (water, methane, ammonia)"),
    MIXED_SILICATE_ICE("Mixed Silicate-Ice", "Silicate core with substantial ice mantle"),
    OCEAN_WORLD("Ocean World", "Deep liquid water ocean over ice/rock layers"),
    GAS_ENVELOPE("Gas Envelope", "Thick hydrogen-helium atmosphere over small core"),
    MOLTEN_SURFACE("Molten Surface", "Partially or fully molten surface due to extreme heat"),
    EXOTIC("Exotic", "Unusual or extreme composition");

    private final String displayName;
    private final String description;

    CompositionClassification(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

}
