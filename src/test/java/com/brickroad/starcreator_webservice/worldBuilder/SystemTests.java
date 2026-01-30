package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.entity.ud.CelestialBody;
import com.brickroad.starcreator_webservice.entity.ud.Moon;
import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.StarSystem;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class SystemTests extends AbstractCreatorTest {

    @Autowired
    private SystemCreator systemCreator;

    //@Test
    public void findSystem() throws JsonProcessingException {

        String targetType = "Ocean Planet";
        int maxAttempts = 1000;

        Planet foundPlanet = null;
        StarSystem system = null;

        System.out.println("Searching for planet type: " + targetType);
        System.out.println("Max attempts: " + maxAttempts);
        System.out.println("---");

        for (int i = 0; i < maxAttempts; i++) {

            system = systemCreator.generateSystem();
            for (CelestialBody planet : system.getPlanets()) {
                if (targetType.equalsIgnoreCase(((Planet) planet).getPlanetType())) {
                    foundPlanet = (Planet) planet;
                }
            }
            if (foundPlanet != null) {
                break;
            }
            if (i % 100 == 0 && i > 0) {
                System.out.println("Searched " + (i + 1) + " systems...");
            }
        }

        assertNotNull(system, "Failed to generate matching system after " + maxAttempts + " attempts");

        Map<String, Object> testResults = new HashMap<>();
        testResults.put("system", system);
        String json = listToJsonString(testResults);
        saveJson(json, "system");
    }

}