package com.brickroad.starcreator_webservice.controller;

import com.brickroad.starcreator_webservice.entity.Star;
import com.brickroad.starcreator_webservice.request.CreationRequest;
import com.brickroad.starcreator_webservice.service.CreationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class CreationController {

    private final CreationService creationService;

    @Autowired
    public CreationController(CreationService creationService) {
        this.creationService = creationService;
    }

    @GetMapping("/star")
    public ResponseEntity<Star> createStar() {
        return ResponseEntity.ok(creationService.createStar());
    }

    @RequestMapping("/newStar")
    @ResponseBody
    public ResponseEntity<Star> createStarFromRequest(@RequestBody CreationRequest input) {
        return ResponseEntity.ok(creationService.createStar(input));
    }

}
