package com.brickroad.starcreator_webservice.controller;

import com.brickroad.starcreator_webservice.model.Planet;
import com.brickroad.starcreator_webservice.model.Star;
import com.brickroad.starcreator_webservice.model.CreationRequest;
import com.brickroad.starcreator_webservice.service.CreationService;
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
    public ResponseEntity<Planet> createPlanet(@RequestBody(required = false) CreationRequest input) {
        return ResponseEntity.ok(creationService.createPlanet(input));
    }

    @GetMapping("/star/{name}")
    public ResponseEntity<Star> createStar(@PathVariable String name) {
        return ResponseEntity.ok(creationService.createStar(name));
    }

    @GetMapping("/planet/{name}/{type}")
    public ResponseEntity<Planet> createPlanet(@PathVariable String type, @PathVariable String name) {
        return ResponseEntity.ok(creationService.createPlanet(type, name));
    }



}
