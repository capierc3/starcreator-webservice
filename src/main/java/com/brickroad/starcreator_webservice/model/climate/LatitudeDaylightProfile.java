package com.brickroad.starcreator_webservice.model.climate;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Annual daylight summary at a single latitude — how daylight changes across all four quadrants.
 * <p>
 * Transient POJO — regenerated from climateSeed as part of climate generation.
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Annual daylight profile at one latitude across all orbital quadrants")
public class LatitudeDaylightProfile {

    @Schema(description = "Latitude in degrees (90 = north pole, 0 = equator, -90 = south pole)")
    private Double latitudeDeg;

    @Schema(description = "Human-readable latitude label", example = "45°N")
    private String latitudeLabel;

    @Schema(description = "Minimum daylight hours across all quadrants (null if perpetual darkness occurs)")
    private Double minDaylightHours;

    @Schema(description = "Maximum daylight hours across all quadrants (null if perpetual daylight occurs)")
    private Double maxDaylightHours;

    @Schema(description = "True if this latitude experiences perpetual daylight in at least one quadrant")
    private Boolean experiencesPerpetualDaylight;

    @Schema(description = "True if this latitude experiences perpetual darkness in at least one quadrant")
    private Boolean experiencesPerpetualDarkness;

    @Schema(description = "Human-readable annual description")
    private String annualDescription;
}
