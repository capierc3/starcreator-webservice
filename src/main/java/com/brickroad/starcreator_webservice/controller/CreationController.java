package com.brickroad.starcreator_webservice.controller;

import com.brickroad.starcreator_webservice.entity.ud.StarSystem;
import com.brickroad.starcreator_webservice.service.CreationService;
import com.brickroad.starcreator_webservice.utils.visualization.OrbitalAnalysisHtmlGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Generation", description = "Endpoints for generating random celestial bodies and star systems")
public class CreationController {

    private final CreationService creationService;
    private final ObjectMapper objectMapper;

    public CreationController(CreationService creationService, ObjectMapper objectMapper) {
        this.creationService = creationService;
        this.objectMapper = objectMapper;
    }

    @Operation(
        summary = "Generate a random star system",
        description = "Generates a complete star system including stars, planets with moons and rings, asteroid belts, "
            + "and a system classification with scout report. Systems can be single, binary, or trinary star configurations."
    )
    @ApiResponse(responseCode = "200", description = "Star system generated successfully",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = StarSystem.class)))
    @GetMapping("/solarsystem")
    public ResponseEntity<StarSystem> createSolarSystem() {
        return ResponseEntity.ok(creationService.createStarSystem());
    }

    @Operation(
        summary = "Generate a star system with orbital analysis visualization",
        description = "Generates a random star system and returns an HTML page with an interactive orbital simulation. "
            + "Also saves the system JSON and HTML to the target/orbital-analysis/ folder."
    )
    @ApiResponse(responseCode = "200", description = "HTML orbital analysis generated",
        content = @Content(mediaType = "text/html"))
    @GetMapping(value = "/solarsystem/orbital-analysis", produces = "text/html")
    public ResponseEntity<String> createSolarSystemWithOrbitalAnalysis() {

        StarSystem system = creationService.createStarSystem();
        String html = OrbitalAnalysisHtmlGenerator.generate(system);

        // Save both files to target folder for local development use
        try {
            File targetFolder = new File("target/orbital-analysis/");
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }
            objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(targetFolder, "system.json"), system);
            try (FileWriter writer = new FileWriter(new File(targetFolder, "system_orbital.html"))) {
                writer.write(html);
            }
        } catch (IOException e) {
            System.err.println("Failed to save orbital analysis files: " + e.getMessage());
        }

        return ResponseEntity.ok(html);
    }
}
