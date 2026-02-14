package com.brickroad.starcreator_webservice.probabilityreport;

import com.brickroad.starcreator_webservice.entity.ud.Asteroid;
import com.brickroad.starcreator_webservice.entity.ud.Belt;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class BeltDataCollector {

    private final Map<String, Integer> beltTypes = new HashMap<>();
    private final Map<String, Integer> asteroidTypes = new HashMap<>();

    public void analyzeData(Belt belt, ProbabilityCounts counts) {
        beltTypes.merge(belt.getBeltType().getCode(), 1, Integer::sum);
        counts.incrementAsteroidCount(belt.getNotableAsteroids().size());
        for (Asteroid asteroid : belt.getNotableAsteroids()) {
            asteroidTypes.merge(asteroid.getAsteroidType().getCode(), 1, Integer::sum);
        }
        counts.incrementTempCount(belt.getDwarfPlanets().size());
    }
}
