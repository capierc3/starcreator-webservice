package com.brickroad.starcreator_webservice.controller;

import com.brickroad.starcreator_webservice.model.Planet;
import com.brickroad.starcreator_webservice.model.Star;
import com.brickroad.starcreator_webservice.request.PlanetRequest;
import com.brickroad.starcreator_webservice.request.StarRequest;
import com.brickroad.starcreator_webservice.service.CreationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

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

    @Operation(summary = "Welcome Text")
    @ApiResponse(responseCode = "200", description = "Welcome")
    @GetMapping("/")
    String home() throws IOException {
        return IOUtils.toString(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("static/welcome.txt")),StandardCharsets.UTF_8);
    }


    @Operation(summary = "Generate a random planet")
    @ApiResponse(responseCode = "200", description = "Planet Generated", content = {@Content(mediaType = "application/json",schema = @Schema(implementation = Planet.class))})
    @GetMapping("/planet")
    public ResponseEntity<Planet> createPlanet(@RequestBody(required = false) PlanetRequest input) {
        return ResponseEntity.ok(creationService.createPlanet(input));
    }

    @Operation(summary = "Generate a random star")
    @ApiResponse(responseCode = "200", description = "Star Generated", content = {@Content(mediaType = "application/json",schema = @Schema(implementation = Star.class))})
    @GetMapping("/star")
    public ResponseEntity<Star> createStar(@RequestBody StarRequest input) {
        return ResponseEntity.ok(creationService.createStar(input));
    }




}
