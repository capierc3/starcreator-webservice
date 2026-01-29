package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.entity.ud.CelestialBody;
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
public class MoonTests extends AbstractCreatorTest {

    @Autowired
    private SystemCreator systemCreator;

    @Test
    public void findPlanetByType() throws JsonProcessingException {
        int moonSystemSize = 40;
        boolean foundLargeEnoughMoonSystem = false;
        while (!foundLargeEnoughMoonSystem) {
            StarSystem system = systemCreator.generateSystem();

            assertNotNull(system, "Failed to generate matching system");

            System.out.println("System Created... Getting Moons");
            System.out.println("-------------------------------");
            Planet biggestMoonSystem = null;
            for (CelestialBody planet : system.getPlanets()) {
                if (biggestMoonSystem == null || biggestMoonSystem.getMoons().size() < ((Planet) planet).getMoons().size()) {
                    biggestMoonSystem = (Planet) planet;
                }
            }

            if (biggestMoonSystem != null && !biggestMoonSystem.getMoons().isEmpty() && biggestMoonSystem.getMoons().size() > moonSystemSize) {
                System.out.println("Moons found: " + biggestMoonSystem.getMoons().size());
                Map<String, Object> testResults = new HashMap<>();
                testResults.put("star", system.getStars());
                testResults.put("planet", biggestMoonSystem);
                String json = listToJsonString(testResults);
                //printJSON(json);
                saveJson(json, "moons_system");
                foundLargeEnoughMoonSystem = true;
            }
        }
    }

}