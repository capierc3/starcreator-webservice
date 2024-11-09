package com.brickroad.starcreator_webservice.controller;

import com.brickroad.starcreator_webservice.WorldBuilder.Planet;
import com.brickroad.starcreator_webservice.WorldBuilder.Star;
import com.brickroad.starcreator_webservice.request.CreationRequest;
import com.brickroad.starcreator_webservice.service.CreationService;
import org.apache.commons.io.FileUtils;
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

    @GetMapping("/")
    String home() throws IOException {
        return IOUtils.toString(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("static/welcome.txt")),StandardCharsets.UTF_8);
    }

    @RequestMapping("/star")
    @ResponseBody
    public ResponseEntity<Star> createStar(@RequestBody CreationRequest input) {
        return ResponseEntity.ok(creationService.createStar(input));
    }

    @RequestMapping("/planet")
    @ResponseBody
    public ResponseEntity<Planet> createPlanet(@RequestBody CreationRequest input) {
        return ResponseEntity.ok(creationService.createPlanet(input));
    }


}
