package com.brickroad.starcreator_webservice.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProbabilityReportResponse {

    private String status;
    private int systemCount;
    private String message;

    public ProbabilityReportResponse(String status, int systemCount, String message) {
        this.status = status;
        this.systemCount = systemCount;
        this.message = message;
    }
}
