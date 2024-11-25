package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.model.Planet;
import com.brickroad.starcreator_webservice.model.Star;
import com.brickroad.starcreator_webservice.request.PlanetRequest;
import com.brickroad.starcreator_webservice.request.StarRequest;
import org.springframework.stereotype.Service;

@Service
public class CreationService {

    public Star createStar(StarRequest input) {
        return new Star(input.getName());
    }

    public Planet createPlanet(PlanetRequest input) {
        if (input != null) {
            return new Planet(input.getType().getName(), input.getName());
        } else {
            return new Planet();
        }
    }

}
