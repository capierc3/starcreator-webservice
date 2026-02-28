package com.brickroad.starcreator_webservice.model.climate;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * A climate-related hazard (radiation storms, acid rain, etc.).
 * Transient POJO — regenerated from climateSeed (table dropped in V108).
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClimateHazard {

    private String hazardName;
    private String hazardType;
    private String severity;
    private String frequency;
    private String description;
}
