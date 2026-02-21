package com.brickroad.starcreator_webservice.probabilityreport;

import com.brickroad.starcreator_webservice.entity.ud.Asteroid;
import com.brickroad.starcreator_webservice.entity.ud.OrbitalBand;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class BeltDataCollector {

    private final Map<String, Integer> beltTypes = new HashMap<>();
    private final Map<String, Integer> asteroidTypes = new HashMap<>();

    public void analyzeData(OrbitalBand band, ProbabilityCounts counts) {
        String typeCode = band.getBeltType() != null ? band.getBeltType().getCode() : band.getBandType();
        beltTypes.merge(typeCode, 1, Integer::sum);
        counts.incrementAsteroidCount(band.getNotableAsteroids().size());
        for (Asteroid asteroid : band.getNotableAsteroids()) {
            asteroidTypes.merge(asteroid.getAsteroidType().getCode(), 1, Integer::sum);
        }
        counts.incrementTempCount(band.getDwarfPlanets().size());
    }
}
