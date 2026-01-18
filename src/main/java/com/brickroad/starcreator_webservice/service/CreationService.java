package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.Creators.PlanetCreator;
import com.brickroad.starcreator_webservice.Creators.SystemCreator;
import com.brickroad.starcreator_webservice.model.planets.Planet;
import com.brickroad.starcreator_webservice.model.starSystems.StarSystem;
import com.brickroad.starcreator_webservice.model.stars.Star;
import com.brickroad.starcreator_webservice.Creators.StarCreator;
import com.brickroad.starcreator_webservice.request.PlanetRequest;
import com.brickroad.starcreator_webservice.request.StarRequest;
import com.brickroad.starcreator_webservice.request.StarSystemRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreationService {

    @Autowired
    private StarCreator starCreator;

    @Autowired
    private SystemCreator systemCreator;

    @Autowired
    private PlanetCreator planetCreator;

    public Star createStar(StarRequest input) {
        return starCreator.generateStar();
    }

    public StarSystem createStarSystem(StarSystemRequest systemRequest) {
        return systemCreator.generateSystem();
    }

    public Planet createPlanet() {
        return planetCreator.generateRandomPlanet();
    }

    public List<Planet> createPlanetsForStar(Star star) {
        return planetCreator.generatePlanetarySystem(star);
    }

}
