package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.creator.PlanetCreator;
import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.StarSystem;
import com.brickroad.starcreator_webservice.entity.ud.Star;
import com.brickroad.starcreator_webservice.creator.StarCreator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreationService {

    private final StarCreator starCreator;
    private final SystemCreator systemCreator;
    private final PlanetCreator planetCreator;

    public CreationService(StarCreator starCreator, SystemCreator systemCreator, PlanetCreator planetCreator) {
        this.starCreator = starCreator;
        this.systemCreator = systemCreator;
        this.planetCreator = planetCreator;
    }

    public Star createStar() {
        return starCreator.generateStar();
    }

    public StarSystem createStarSystem() {
        return systemCreator.generateSystem();
    }

    public Planet createPlanet() {
        return planetCreator.generateRandomPlanet();
    }

    public List<Planet> createPlanetsForStar(Star star) {
        return planetCreator.generatePlanetarySystem(star);
    }
}
