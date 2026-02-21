package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.creator.PlanetCreator;
import com.brickroad.starcreator_webservice.creator.StarCreator;
import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.entity.ud.CelestialBody;
import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.StarSystem;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class PlanetTests extends AbstractCreatorTest {

    @Autowired
    private PlanetCreator planetCreator;

    @Autowired
    private StarCreator starCreator;

    @Autowired
    private SystemCreator systemCreator;

    //@Test
    public void findPlanetByType() throws JsonProcessingException {
        String targetType = "Lava Planet";
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
                    System.out.println("Searched " + (i + 1) + " systems...");
                    break;
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
        testResults.put("star", system.getStars());
        testResults.put("planet", foundPlanet);
        String json = listToJsonString(testResults);
        //printJSON(json);
        saveJson(json, "planet");


    }

    //@Test
    public void findMultiplePlanetsOfType() throws JsonProcessingException {
        String targetType = "Terrestrial Planet";
        int examplesNeeded = 10;
        int maxAttempts = 20000;

        List<Planet> foundPlanets = new java.util.ArrayList<>();
        int systemsGenerated = 0;

        System.out.println("Searching for " + examplesNeeded + " examples of: " + targetType);
        System.out.println("---");

        for (int i = 0; i < maxAttempts && foundPlanets.size() < examplesNeeded; i++) {
            systemsGenerated++;
            StarSystem system = systemCreator.generateSystem();
            for (CelestialBody planet : system.getPlanets()) {
                if (targetType.equalsIgnoreCase(((Planet) planet).getPlanetType())
                && "habitable".equalsIgnoreCase(((Planet) planet).getHabitableZonePosition())) {
                    foundPlanets.add(((Planet) planet));
                    System.out.println("Found #" + foundPlanets.size() + " after " +
                            systemsGenerated + " systems");

                    if (foundPlanets.size() >= examplesNeeded) {
                        break;
                    }
                }
            }
            if (systemsGenerated % 200 == 0) {
                System.out.println("Searched " + systemsGenerated + " systems...");
            }
        }

        Map<String, Object> output = new HashMap<>();
        for (int i = 0; i < foundPlanets.size(); i++) {
            output.put("Planet" + i, foundPlanets.get(i));
        }

        String title = "\n✅ Found " + foundPlanets.size() + " examples!";
        System.out.println(title);
        for (int i = 0; i < foundPlanets.size(); i++) {
            System.out.println("\nPlanet #" + (i + 1) + ": " + foundPlanets.get(i).getName());
            System.out.println("dist: " + foundPlanets.get(i).getSemiMajorAxisAU());
            System.out.println("surface Temp: " + foundPlanets.get(i).getSurfaceTemp());
            System.out.println("mas: " + foundPlanets.get(i).getEarthMass());
            System.out.println("habitable: " + foundPlanets.get(i).getHabitableZonePosition());
            System.out.println("comp type: " + foundPlanets.get(i).getCompositionClassification());
            System.out.println("atmosphere: " + foundPlanets.get(i).getAtmosphereComposition());
            System.out.println("atmosphere class: " + foundPlanets.get(i).getAtmosphereClassification());
            System.out.println("interior: " + foundPlanets.get(i).getInteriorComposition());
            System.out.println("envelope: " + foundPlanets.get(i).getEnvelopeComposition());
            System.out.println("system size: " + foundPlanets.get(i).getParentStar().getSystem().getSizeAu());
        }
        saveJson(listToJsonString(output), "planet_examples");
        //printJSON(output, title);
    }

    //@Test
    public void planetProbabilityTest() throws JsonProcessingException {
        int systemsAmount = 1000;
        List<Planet> capturedPlanets = new ArrayList<>();
        for (int i = 0; i < systemsAmount; i++) {
            StarSystem system = systemCreator.generateSystem();
            for (CelestialBody planet : system.getPlanets()) {
                Planet capturedPlanet = (Planet) planet;
                if (!capturedPlanet.getBands().isEmpty()) capturedPlanets.add(capturedPlanet);
            }
            if (i % 100 == 0) System.out.println("Systems created " + (i));
        }
        System.out.println("------------------------------------------------------------");
        System.out.println("Found " + capturedPlanets.size() + " planets with rings");
        System.out.println("Probability: " + (capturedPlanets.size() * 100.0) / systemsAmount + "%");
        System.out.println("------------------------------------------------------------");
        Map<Integer, Integer> ringCounts = new HashMap<>();
        for (Planet planet : capturedPlanets) {
            ringCounts.put(planet.getBands().size(), ringCounts.getOrDefault(planet.getBands().size(), 0) + 1);
        }
        for (int ringCount : ringCounts.keySet()) {
            System.out.println("Rings " + ringCount + ": " + ringCounts.get(ringCount) + " (" + (ringCounts.get(ringCount) * 100.0) / capturedPlanets.size() + "%)");
        }
    }

}