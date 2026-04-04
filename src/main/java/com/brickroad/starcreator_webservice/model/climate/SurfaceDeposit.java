package com.brickroad.starcreator_webservice.model.climate;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * A surface deposit — material that accumulates on the ground from precipitation,
 * volcanic activity, photochemical processes, or biological activity.
 * <p>
 * Provides the renderer with pre-computed data about what's coating the surface:
 * type, color, coverage, and visual thickness. Eliminates the need to reverse-engineer
 * surface appearance from atmosphere, cloud, and hydrology data.
 * <p>
 * Transient POJO — regenerated from climateSeed as part of climate generation.
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Surface deposit — material accumulating on the ground")
public class SurfaceDeposit {

    @Schema(description = "Deposit material type", example = "HYDROCARBON_THOLIN")
    private String depositType;

    @Schema(description = "How this deposit forms", example = "PHOTOCHEMICAL")
    private String source;

    @Schema(description = "Hex color hint for rendering", example = "#CC8833")
    private String colorHint;

    @Schema(description = "Surface coverage percentage (0-100)", example = "55.0")
    private Double coveragePercent;

    @Schema(description = "Visual thickness classification", example = "THICK")
    private String thickness;

    @Schema(description = "Visual dominance rank (1 = most dominant)", example = "1")
    private Integer dominanceRank;

    @Schema(description = "Human-readable description")
    private String description;

    public SurfaceDeposit() {}

    public SurfaceDeposit(String depositType, String source, String colorHint,
                          double coveragePercent, String description) {
        this.depositType = depositType;
        this.source = source;
        this.colorHint = colorHint;
        this.coveragePercent = coveragePercent;
        this.description = description;
    }
}
