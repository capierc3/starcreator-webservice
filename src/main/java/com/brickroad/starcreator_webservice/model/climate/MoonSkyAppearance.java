package com.brickroad.starcreator_webservice.model.climate;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * Visual appearance of a moon in the sky of a planet or sibling moon.
 * Transient POJO — regenerated from climateSeed (table dropped in V108).
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MoonSkyAppearance {

    private Long moonId;
    private Double angularDiameterDegrees;
    private Double phaseCycleDays;
    private Double brightnessRelativeToFullMoon;
    private Boolean isVisibleInDaytime = false;
    private String apparentColor;
    private String description;
}
