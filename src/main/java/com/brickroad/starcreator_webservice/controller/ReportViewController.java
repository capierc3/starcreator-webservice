package com.brickroad.starcreator_webservice.controller;

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
            description = "Returns the most recently generated probability report as HTML.",
            tags = {"Probability Report"})
    @ApiResponse(responseCode = "200", description = "Report HTML returned successfully")
    @ApiResponse(responseCode = "404", description = "No report has been generated yet")
    @GetMapping(value = "/report", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> viewReport() {
        File latestReport = getLatestHtmlReport();
        if (latestReport == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(new FileSystemResource(latestReport));
    }

    @Operation(summary = "Get Report Header Image",
            description = "Returns the header image used by the probability report.",
            tags = {"Probability Report"})
    @ApiResponse(responseCode = "200", description = "Image returned successfully")
    @ApiResponse(responseCode = "404", description = "Image not found")
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

    private File getLatestHtmlReport() {
        if (!REPORT_DIR.exists()) return null;
        File[] htmlFiles = REPORT_DIR.listFiles((dir, name) -> name.endsWith(".html"));
        if (htmlFiles == null || htmlFiles.length == 0) return null;
        return Arrays.stream(htmlFiles)
                .max(Comparator.comparingLong(File::lastModified))
                .orElse(null);
    }
}
