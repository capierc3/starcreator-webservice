package com.brickroad.starcreator_webservice.model.climate;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Orbital light cycle — how daylight varies by latitude through the orbit.
 * <p>
 * Divides the orbit into four quadrants (solstice/equinox pattern) and computes
 * day length, solar elevation, and temperature at sampled latitudes for each.
 * Provides both the UI renderer and human readers with actionable data about
 * how alien (or familiar) a planet's day/night patterns are.
 * <p>
 * Transient POJO — regenerated from climateSeed as part of climate generation.
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Orbital light cycle — daylight variation by latitude through the orbit")
public class OrbitalLightCycle {

    @Schema(description = "Effective obliquity in degrees (0-90), accounting for retrograde rotation")
    private Double effectiveObliquityDeg;

    @Schema(description = "Tropic latitude — where the sun can be directly overhead (= effective obliquity)")
    private Double tropicLatitudeDeg;

    @Schema(description = "Arctic circle latitude — where perpetual day/night begins (= 90 - effective obliquity)")
    private Double arcticCircleLatitudeDeg;

    @Schema(description = "Synodic (solar) day length in hours, adjusted for orbital motion")
    private Double solarDayHours;

    @Schema(description = "Full orbital period in Earth days")
    private Double orbitalPeriodDays;

    @Schema(description = "Light cycle classification", example = "EXTREME_TILT")
    private String lightCycleClass;

    @Schema(description = "Percentage of surface that experiences perpetual daylight at some point in the orbit")
    private Double perpetualDaylightCoveragePercent;

    @Schema(description = "Human-readable narrative summarizing the overall light pattern")
    private String lightCycleSummary;

    @Schema(description = "The four orbital quadrants (solstice/equinox divisions)")
    private List<OrbitalQuadrant> quadrants = new ArrayList<>();

    @Schema(description = "Latitude-indexed annual daylight summary, ordered 90°N to 90°S")
    private List<LatitudeDaylightProfile> latitudeProfiles = new ArrayList<>();
}
