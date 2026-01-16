package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.model.planets.Planet;
import com.brickroad.starcreator_webservice.model.stars.Star;
import com.brickroad.starcreator_webservice.model.planets.PlanetCreator;
import com.brickroad.starcreator_webservice.model.stars.StarCreator;
import com.brickroad.starcreator_webservice.request.PlanetRequest;
import com.brickroad.starcreator_webservice.request.StarRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreationService {

    @Autowired
    private StarCreator starCreator;

    public Star createStar(StarRequest input) {
        return starCreator.generateStar();
    }

    public Planet createPlanet(PlanetRequest planetRequest) {
        if (planetRequest != null) {
            //return PlanetCreator.generateRandomPlanet(planetRequest.getType().getName(), planetRequest.getName());
        } else {
            //return PlanetCreator.generateRandomPlanet("","");
        }
        return null;
    }

}
