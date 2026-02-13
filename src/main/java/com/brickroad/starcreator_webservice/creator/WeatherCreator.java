package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.utils.planets.AtmosphericStructureCalculator;
import com.brickroad.starcreator_webservice.utils.planets.TemperatureClimateCalculator;
import com.brickroad.starcreator_webservice.utils.planets.WindCirculationCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WeatherCreator {

    @Autowired
    private AtmosphericStructureCalculator atmosphericStructureCalculator;

    @Autowired
    private TemperatureClimateCalculator temperatureClimateCalculator;

    @Autowired
    private WindCirculationCalculator windCirculationCalculator;

    public PlanetaryWeather generateWeather(Planet planet, Star parentStar, StarSystem system) {

        String atmClass = planet.getAtmosphereClassification();
        if (atmClass == null || "NONE".equals(atmClass)) {
            return null;
        }

        PlanetaryWeather weather = new PlanetaryWeather();
        weather.setPlanet(planet);

        // Phase 2: Atmospheric Structure
        atmosphericStructureCalculator.calculate(weather, planet, parentStar);

        // Phase 3: Temperature & Climate
        temperatureClimateCalculator.calculate(weather, planet, parentStar, system);

        // Phase 4: Wind & Circulation
        windCirculationCalculator.calculate(weather, planet);

        // Phase 5: Clouds & Precipitation
        // calculateCloudsPrecipitation(weather, planet, parentStar);

        // Phase 6: Storms
        // calculateStorms(weather, planet);

        // Phase 7: Tidal & Visual
        // calculateTidalEffects(weather, planet);
        // calculateSkyAppearance(weather, planet, parentStar, system);

        // Phase 8: Narrative
        // generateNarratives(weather, planet, parentStar);

        // Phase 9: Gas Giant Specialization (if applicable)
        // if (isGasGiant(atmClass)) {
        //     calculateGasGiantFeatures(weather, planet, parentStar);
        // }

        return weather;
    }

    public PlanetaryWeather generateMoonWeather(Moon moon, Planet planet, Star parentStar, StarSystem system) {
        if (!Boolean.TRUE.equals(moon.getHasAtmosphere())) {
            return null;
        }

        PlanetaryWeather weather = new PlanetaryWeather();
        weather.setMoon(moon);

        // Same phases as planet weather, adapted for moon context
        // Implementation in later phases

        return weather;
    }

}
