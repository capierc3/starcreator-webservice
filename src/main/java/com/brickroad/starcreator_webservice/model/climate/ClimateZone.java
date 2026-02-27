package com.brickroad.starcreator_webservice.model.climate;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * A climate zone within a planet or moon's climate system.
 * Transient POJO — regenerated from climateSeed (table dropped in V108).
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClimateZone {

    private String zoneName;
    private Double latitudeStartDegrees;
    private Double latitudeEndDegrees;
    private Double coveragePercent;
    private Double meanTempK;
    private String description;
}
