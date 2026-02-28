package com.brickroad.starcreator_webservice.model.climate;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * An atmospheric cloud layer.
 * Transient POJO — regenerated from climateSeed (table dropped in V108).
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CloudLayer {

    private Integer layerOrder;
    private String composition;
    private Double altitudeKm;
    private Double thicknessKm;
    private Double temperatureK;
    private String opacity;
    private String color;
    private String description;
}
