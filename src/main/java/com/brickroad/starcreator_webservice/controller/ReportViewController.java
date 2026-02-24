package com.brickroad.starcreator_webservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;

@RestController
public class ReportViewController {

    private static final File REPORT_DIR = new File("target/probability_reports/");

    @Operation(summary = "View Probability Report",
            description = "Returns the most recently generated probability report as an interactive SPA. "
                    + "Falls back to the legacy HTML report if no SPA report exists.",
            tags = {"Probability Report"})
    @ApiResponse(responseCode = "200", description = "Report HTML returned successfully")
    @ApiResponse(responseCode = "404", description = "No report has been generated yet")
    @GetMapping(value = "/report", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> viewReport() {
        // Prefer SPA report; fall back to legacy HTML
        File spaReport = getLatestFileMatching("system_spa_report_");
        if (spaReport != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(new FileSystemResource(spaReport));
        }

        File legacyReport = getLatestFileMatching("system_report_");
        if (legacyReport != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(new FileSystemResource(legacyReport));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "View Legacy Probability Report",
            description = "Returns the legacy (non-SPA) HTML probability report.",
            tags = {"Probability Report"})
    @ApiResponse(responseCode = "200", description = "Legacy report HTML returned successfully")
    @ApiResponse(responseCode = "404", description = "No legacy report has been generated yet")
    @GetMapping(value = "/report/legacy", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> viewLegacyReport() {
        File legacyReport = getLatestFileMatching("system_report_");
        if (legacyReport == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(new FileSystemResource(legacyReport));
    }

    @Operation(summary = "Get Report JSON Data",
            description = "Returns the raw JSON data from the most recent probability report.",
            tags = {"Probability Report"})
    @ApiResponse(responseCode = "200", description = "Report JSON returned successfully")
    @ApiResponse(responseCode = "404", description = "No report has been generated yet")
    @GetMapping(value = "/api/v1/report/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource> getReportData() {
        if (!REPORT_DIR.exists()) return ResponseEntity.notFound().build();
        File[] jsonFiles = REPORT_DIR.listFiles((dir, name) -> name.endsWith(".json"));
        if (jsonFiles == null || jsonFiles.length == 0) return ResponseEntity.notFound().build();
        File latest = Arrays.stream(jsonFiles)
                .max(Comparator.comparingLong(File::lastModified))
                .orElse(null);
        if (latest == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new FileSystemResource(latest));
    }

    @Hidden
    @GetMapping(value = "/report/headerImg.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getHeaderImage() {
        InputStream is = getClass().getResourceAsStream("/static/report/headerImg.png");
        if (is == null) {
            return ResponseEntity.notFound().build();
        }
        try (is) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(is.readAllBytes());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private File getLatestFileMatching(String prefix) {
        if (!REPORT_DIR.exists()) return null;
        File[] files = REPORT_DIR.listFiles((dir, name) ->
                name.startsWith(prefix) && name.endsWith(".html"));
        if (files == null || files.length == 0) return null;
        return Arrays.stream(files)
                .max(Comparator.comparingLong(File::lastModified))
                .orElse(null);
    }
}
