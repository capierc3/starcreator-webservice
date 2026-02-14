package com.brickroad.starcreator_webservice.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProbabilityReportRequest {

    @JsonProperty("system_count")
    private int systemCount;
}
