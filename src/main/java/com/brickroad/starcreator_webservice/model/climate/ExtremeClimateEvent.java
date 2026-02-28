package com.brickroad.starcreator_webservice.model.climate;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * An extreme climate event (storms, eruptions, etc.).
 * Transient POJO — regenerated from climateSeed (table dropped in V108).
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtremeClimateEvent {

    private String eventName;
    private String eventType;
    private String severity;
    private String frequency;
    private String description;
}
