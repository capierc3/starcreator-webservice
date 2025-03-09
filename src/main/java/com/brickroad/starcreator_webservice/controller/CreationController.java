package com.brickroad.starcreator_webservice.controller;

import com.brickroad.starcreator_webservice.model.StarSystem;
import com.brickroad.starcreator_webservice.model.planets.Planet;
import com.brickroad.starcreator_webservice.model.Star;
import com.brickroad.starcreator_webservice.request.PlanetRequest;
import com.brickroad.starcreator_webservice.request.StarRequest;
import com.brickroad.starcreator_webservice.request.SystemRequest;
import com.brickroad.starcreator_webservice.service.CreationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1")
public class CreationController {

    private final CreationService creationService;

    @Autowired
    public CreationController(CreationService creationService) {
        this.creationService = creationService;
    }

    @Operation(summary = "Welcome Text", description = "Welcome text to help direct users", tags = {"help"})
    @ApiResponse(responseCode = "200", description = "Welcome")
    @GetMapping("/")
    String home() throws IOException {
        return IOUtils.toString(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("static/welcome.txt")),StandardCharsets.UTF_8);
    }


    @Operation(summary = "Generate planet", description = "Generates a random planet based on name and type", tags = {"Planet Creation"})
    @ApiResponse(responseCode = "200", description = "Planet Generated", content = {@Content(mediaType = "application/json",schema = @Schema(implementation = Planet.class))})
    @GetMapping("/planet")
    public ResponseEntity<Planet> createPlanet(@RequestBody(required = false) PlanetRequest planetRequest) {
        return ResponseEntity.ok(creationService.createPlanet(planetRequest));
    }

    @Operation(summary = "Generate star", description = "Generates a random star based on type", tags = {"Star Creation"})
    @ApiResponse(responseCode = "200", description = "Star Generated", content = {@Content(mediaType = "application/json",schema = @Schema(implementation = Star.class))})
    @GetMapping("/star")
    public ResponseEntity<Star> createStar(@RequestBody StarRequest starRequest) {
        return ResponseEntity.ok(creationService.createStar(starRequest));
    }

    @Operation(summary = "Generate system", description = "Generates a random system based", tags = {"System Creation"})
    @ApiResponse(responseCode = "200", description = "System Generated", content = {@Content(mediaType = "application/json",schema = @Schema(implementation = StarSystem.class))})
    @GetMapping("/system")
    public ResponseEntity<StarSystem> createSystem(@RequestBody SystemRequest systemRequest) {
        return ResponseEntity.ok(creationService.createStarSystem(systemRequest));
    }






}
