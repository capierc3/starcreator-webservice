package com.brickroad.starcreator_webservice.model.climate;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * One quarter of a planet's orbit, centered on a solstice or equinox.
 * <p>
 * Transient POJO — regenerated from climateSeed as part of climate generation.
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "One quarter of the orbit — solstice or equinox division")
public class OrbitalQuadrant {

    @Schema(description = "Quadrant index (0-3)", example = "0")
    private Integer quadrantIndex;

    @Schema(description = "Quadrant name", example = "NORTH_POLE_SUNWARD")
    private String quadrantName;

    @Schema(description = "Human-readable label", example = "North Pole Facing Star")
    private String label;

    @Schema(description = "Orbital day this quadrant starts")
    private Double orbitalDayStart;

    @Schema(description = "Orbital day this quadrant ends")
    private Double orbitalDayEnd;

    @Schema(description = "Duration of this quadrant in Earth days")
    private Double durationDays;

    @Schema(description = "Sun's declination at the midpoint of this quadrant in degrees")
    private Double solarDeclinationAtMidpointDeg;

    @Schema(description = "Narrative description of conditions during this quadrant")
    private String description;

    @Schema(description = "Daylight data at sampled latitudes, ordered 90°N to 90°S")
    private List<LatitudeDaylightSnapshot> daylightByLatitude = new ArrayList<>();
}
