package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.model.Planet;
import com.brickroad.starcreator_webservice.model.Star;
import com.brickroad.starcreator_webservice.model.CreationRequest;
import org.springframework.stereotype.Service;

@Service
public class CreationService {

    public Star createStar(CreationRequest input) {
        return new Star(input.getName());
    }

    public Star createStar(String name) {
        return new Star(name);
    }



    public Planet createPlanet(CreationRequest input) {
        if (input != null) {
            return new Planet(input.getType(), input.getName());
        } else {
            return new Planet();
        }
    }

    public Planet createPlanet(String type, String name) {
        return new Planet(type, name);
    }

}
