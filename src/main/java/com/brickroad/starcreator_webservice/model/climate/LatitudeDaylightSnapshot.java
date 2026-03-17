package com.brickroad.starcreator_webservice.model.climate;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Daylight conditions at a single latitude during a single orbital quadrant.
 * <p>
 * Transient POJO — regenerated from climateSeed as part of climate generation.
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Daylight snapshot at one latitude during one orbital quadrant")
public class LatitudeDaylightSnapshot {

    @Schema(description = "Latitude in degrees (90 = north pole, 0 = equator, -90 = south pole)")
    private Double latitudeDeg;

    @Schema(description = "Human-readable latitude label", example = "45°N")
    private String latitudeLabel;

    @Schema(description = "Hours of daylight per solar day (null if perpetual day or night)")
    private Double daylightHours;

    @Schema(description = "True if the sun never sets at this latitude during this quadrant")
    private Boolean perpetualDaylight;

    @Schema(description = "True if the sun never rises at this latitude during this quadrant")
    private Boolean perpetualDarkness;

    @Schema(description = "Maximum solar elevation above the horizon in degrees")
    private Double maxSolarElevationDeg;

    @Schema(description = "Estimated mean temperature during daylight hours in Kelvin (null if perpetual darkness)")
    private Double meanDaytimeTempK;

    @Schema(description = "Estimated mean temperature during darkness hours in Kelvin (null if perpetual daylight)")
    private Double meanNighttimeTempK;

    @Schema(description = "Human-readable description of conditions")
    private String description;
}
