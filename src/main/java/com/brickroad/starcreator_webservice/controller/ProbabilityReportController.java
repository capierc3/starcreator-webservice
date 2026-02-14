package com.brickroad.starcreator_webservice.controller;

import com.brickroad.starcreator_webservice.request.ProbabilityReportRequest;
import com.brickroad.starcreator_webservice.response.ProbabilityReportResponse;
import com.brickroad.starcreator_webservice.service.ProbabilityReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ProbabilityReportController {

    private final ProbabilityReportService probabilityReportService;

    @Value("${probability-report.api-key}")
    private String apiKey;

    @Autowired
    public ProbabilityReportController(ProbabilityReportService probabilityReportService) {
        this.probabilityReportService = probabilityReportService;
    }

    @Operation(summary = "Run System Probability Test",
            description = "Generates probability reports for N star systems. Returns immediately, reports are generated asynchronously.",
            tags = {"Probability Report"})
    @ApiResponse(responseCode = "200", description = "Report generation started")
    @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing API key")
    @PostMapping("/system-probability-test")
    public ResponseEntity<ProbabilityReportResponse> runProbabilityTest(
            @RequestHeader(value = "X-Api-Key", required = false) String providedKey,
            @RequestBody ProbabilityReportRequest request) {

        if (providedKey == null || !providedKey.equals(apiKey)) {
            return ResponseEntity.status(401)
                    .body(new ProbabilityReportResponse("error", 0, "Unauthorized"));
        }

        int systemCount = request.getSystemCount();
        if (systemCount <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ProbabilityReportResponse("error", 0, "system_count must be greater than 0"));
        }

        probabilityReportService.generateReportAsync(systemCount);

        return ResponseEntity.ok(new ProbabilityReportResponse(
                "started", systemCount,
                "Probability report generation started for " + systemCount + " systems"));
    }
}
