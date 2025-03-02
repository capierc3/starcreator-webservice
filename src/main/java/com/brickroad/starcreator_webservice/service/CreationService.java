package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.model.StarSystem;
import com.brickroad.starcreator_webservice.model.planets.Planet;
import com.brickroad.starcreator_webservice.model.Star;
import com.brickroad.starcreator_webservice.request.PlanetRequest;
import com.brickroad.starcreator_webservice.request.StarRequest;
import com.brickroad.starcreator_webservice.request.SystemRequest;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CreationService {

    public Star createStar(StarRequest input) {
        if (input.getName() != null && input.getType() != null) {
            return StarCreator.createStar(input.getType(), input.getName());
        } else if (input.getName() != null && input.getType() == null) {
            return StarCreator.createStar(input.getName());
        } else if (input.getName() == null && input.getType() != null) {
            return StarCreator.createStar(input.getType());
        }
        return StarCreator.createStar();
    }

    public Planet createPlanet(PlanetRequest planetRequest) {
        if (planetRequest != null) {
            return PlanetCreator.generateRandomPlanet(planetRequest.getType().getName(), planetRequest.getName());
        } else {
            return PlanetCreator.generateRandomPlanet("","");
        }
    }

    public StarSystem createStarSystem(SystemRequest systemRequest) {
        return SystemCreator.createStarSystem(Objects.requireNonNullElseGet(systemRequest, SystemRequest::new));
    }

}
