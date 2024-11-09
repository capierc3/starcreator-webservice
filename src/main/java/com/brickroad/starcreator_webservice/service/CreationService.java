package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.WorldBuilder.Planet;
import com.brickroad.starcreator_webservice.WorldBuilder.Star;
import com.brickroad.starcreator_webservice.request.CreationRequest;
import org.springframework.stereotype.Service;

@Service
public class CreationService {

    public Star createStar(CreationRequest input) {
        return new Star(input.getName());
    }

    public Planet createPlanet(CreationRequest input) {
        return new Planet(input.getType(), input.getName());
    }

}
