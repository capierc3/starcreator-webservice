package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.model.planets.Planet;
import com.brickroad.starcreator_webservice.model.Star;
import com.brickroad.starcreator_webservice.request.PlanetRequest;
import com.brickroad.starcreator_webservice.request.StarRequest;
import org.springframework.stereotype.Service;

@Service
public class CreationService {

    public Star createStar(StarRequest input) {
        return new Star(input.getName());
    }

    public Planet createPlanet(PlanetRequest planetRequest) {
        if (planetRequest != null) {
            return PlanetCreator.generateRandomPlanet(planetRequest.getType().getName(), planetRequest.getName());
        } else {
            return PlanetCreator.generateRandomPlanet("","");
        }
    }

}
