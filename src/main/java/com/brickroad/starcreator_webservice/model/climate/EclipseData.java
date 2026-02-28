package com.brickroad.starcreator_webservice.model.climate;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * Eclipse event data (solar, lunar, or planetary eclipses).
 * Transient POJO — regenerated from climateSeed (table dropped in V108).
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EclipseData {

    private String eclipseSource;
    private String sourceBodyName;
    private String eclipseType;
    private Double frequencyPerYear;
    private Double typicalDurationMinutes;
    private String description;
}
