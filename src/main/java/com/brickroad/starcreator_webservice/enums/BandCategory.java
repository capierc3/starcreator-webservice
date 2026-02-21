package com.brickroad.starcreator_webservice.enums;

/**
 * High-level discriminator for orbital bands.
 * <p>
 * A BELT orbits a star (e.g., asteroid belt, Kuiper belt, scattered disk).
 * A RING orbits a planet (e.g., Saturn's rings, planetary debris ring).
 */
public enum BandCategory {

    BELT("Annular band of material orbiting a star"),
    RING("Annular band of material orbiting a planet");

    private final String description;

    BandCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
