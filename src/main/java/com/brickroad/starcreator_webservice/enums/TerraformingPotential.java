package com.brickroad.starcreator_webservice.enums;

import lombok.Getter;

/**
 * Assessment of how feasible terraforming would be.
 */
@Getter
public enum TerraformingPotential {
    UNNECESSARY("Unnecessary", "Already habitable - no terraforming needed"),
    FEASIBLE("Feasible", "Centuries timescale - add atmosphere, warm/cool planet"),
    MODERATE("Moderate", "Major engineering - import water, build magnetic shield"),
    DIFFICULT("Difficult", "Extreme challenges - no magnetic field, wrong mass"),
    EXTREMELY_DIFFICULT("Extremely Difficult", "Theoretically possible, practically impossible"),
    NONE("None", "Cannot be made habitable by any known means");

    private final String displayName;
    private final String description;

    TerraformingPotential(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
