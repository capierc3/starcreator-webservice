package com.brickroad.starcreator_webservice.model.climate;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * A precipitation type (rain, snow, exotic substances, etc.).
 * Transient POJO — regenerated from climateSeed (table dropped in V108).
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrecipitationType {

    private String substance;
    private String phase;
    private String frequency;
    private String intensity;
    private Boolean reachesSurface = true;
    private String description;
}
