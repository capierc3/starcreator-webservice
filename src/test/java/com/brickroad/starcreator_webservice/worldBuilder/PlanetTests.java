package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.Creators.PlanetCreator;
import com.brickroad.starcreator_webservice.Creators.StarCreator;
import com.brickroad.starcreator_webservice.Creators.SystemCreator;
import com.brickroad.starcreator_webservice.model.CelestialBody;
import com.brickroad.starcreator_webservice.model.planets.Planet;
import com.brickroad.starcreator_webservice.model.stars.Star;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class PlanetTests {

    @Autowired
    private PlanetCreator planetCreator;

    @Autowired
    private StarCreator starCreator;

    @Autowired
    private SystemCreator systemCreator;

    @Test
    public void findPlanetByType() throws JsonProcessingException {
        String targetType = "Desert Planet";
        int maxAttempts = 1000;
        int systemsGenerated = 0;

        Planet foundPlanet = null;
        Star parentStar = null;

        System.out.println("Searching for planet type: " + targetType);
        System.out.println("Max attempts: " + maxAttempts);
        System.out.println("---");

        for (int i = 0; i < maxAttempts; i++) {
            systemsGenerated++;

            Star star = starCreator.generateStar();
            star.setName("Star #" + systemsGenerated);

            List<Planet> planets = planetCreator.generatePlanetarySystem(star);

            for (Planet planet : planets) {
                if (targetType.equalsIgnoreCase(planet.getPlanetType())) {
                    foundPlanet = planet;
                    parentStar = star;
                    break;
                }
            }

            if (foundPlanet != null) {
                break;
            }

            if (systemsGenerated % 100 == 0) {
                System.out.println("Searched " + systemsGenerated + " systems...");
            }
        }

        assertNotNull(foundPlanet,
                "Failed to find planet type '" + targetType + "' after " + maxAttempts + " attempts");

        Map<String, Object> output = new HashMap<>();
        output.put("star", parentStar);
        output.put("planet", foundPlanet);

        String title = "\n✅ FOUND " + targetType + " after " + systemsGenerated + " attempts!";
        printJSON(output, title);

    }

    @Test
    public void findMultiplePlanetsOfType() throws JsonProcessingException {
        String targetType = "Desert Planet";
        int examplesNeeded = 100;
        int maxAttempts = 2000;

        List<Planet> foundPlanets = new java.util.ArrayList<>();
        int systemsGenerated = 0;

        System.out.println("Searching for " + examplesNeeded + " examples of: " + targetType);
        System.out.println("---");

        for (int i = 0; i < maxAttempts && foundPlanets.size() < examplesNeeded; i++) {
            systemsGenerated++;

            Set<CelestialBody> planets = systemCreator.generateSystem().getPlanets();

            for (CelestialBody planet : planets) {
                if (targetType.equalsIgnoreCase(((Planet) planet).getPlanetType())) {
                    foundPlanets.add(((Planet) planet));
                    System.out.println("Found #" + foundPlanets.size() + " after " +
                            systemsGenerated + " systems");

                    if (foundPlanets.size() >= examplesNeeded) {
                        break;
                    }
                }
            }
        }


        assertEquals(examplesNeeded, foundPlanets.size(),
                "Only found " + foundPlanets.size() + " examples of '" + targetType +
                        "' out of " + examplesNeeded + " needed");


        Map<String, Object> output = new HashMap<>();
        for (int i = 0; i < foundPlanets.size(); i++) {
            output.put("Planet" + i, foundPlanets.get(i));
        }

        String title = "\n✅ Found " + foundPlanets.size() + " examples!";
        for (int i = 0; i < foundPlanets.size(); i++) {
            System.out.println("\nPlanet #" + (i + 1) + ": " + foundPlanets.get(i).getName());
            System.out.println("dist: " + foundPlanets.get(i).getSemiMajorAxisAU());
            System.out.println("comp type: " + foundPlanets.get(i).getCompositionClassification());
            System.out.println("habitable: " + foundPlanets.get(i).getHabitableZonePosition());
        }
        printJSON(output, title);
    }

    //@Test
    public void discoverAllPlanetTypes() {
        int systemsToGenerate = 500;
        java.util.Map<String, Integer> typeCount = new java.util.HashMap<>();
        java.util.Map<String, Planet> typeExamples = new java.util.HashMap<>();

        System.out.println("Generating " + systemsToGenerate + " systems to discover planet types...");
        System.out.println("---");

        for (int i = 0; i < systemsToGenerate; i++) {
            Star star = starCreator.generateStar();
            List<Planet> planets = planetCreator.generatePlanetarySystem(star);

            for (Planet planet : planets) {
                String type = planet.getPlanetType();
                typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);

                // Keep first example of each type
                if (!typeExamples.containsKey(type)) {
                    typeExamples.put(type, planet);
                }
            }

            if ((i + 1) % 100 == 0) {
                System.out.println("Processed " + (i + 1) + " systems...");
            }
        }

        System.out.println("\n✅ PLANET TYPE DISTRIBUTION");
        System.out.println("=====================================");

        typeCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .forEach(entry -> {
                    String type = entry.getKey();
                    int count = entry.getValue();
                    double percentage = (count * 100.0) / systemsToGenerate;

                    System.out.printf("%-20s : %4d planets (%.1f%% of systems)%n",
                            type, count, percentage);
                });

        System.out.println("\nTotal unique types: " + typeCount.size());

        // Assert we found some planets
        assertTrue(!typeCount.isEmpty(), "No planets found!");
    }

    //@Test
    public void findPlanetWithCharacteristics() {
        int maxAttempts = 1000;
        Planet foundPlanet = null;

        System.out.println("Searching for planet with strong magnetic field and auroras...");
        System.out.println("---");

        for (int i = 0; i < maxAttempts; i++) {
            Star star = starCreator.generateStar();
            List<Planet> planets = planetCreator.generatePlanetarySystem(star);

            for (Planet planet : planets) {
                if (planet.getMagneticField() != null &&
                        planet.getMagneticField().getStrengthComparedToEarth() > 1.0 &&
                        planet.getMagneticField().getHasAuroras() != null &&
                        planet.getMagneticField().getHasAuroras() &&
                        planet.getMagneticField().getAuroralColors() != null) {

                    foundPlanet = planet;
                    System.out.println("\n✅ FOUND after " + (i + 1) + " attempts!");
                    break;
                }
            }

            if (foundPlanet != null) break;
        }

        assertNotNull(foundPlanet, "Failed to find matching planet");

        System.out.println("=====================================");
        System.out.println("Planet: " + foundPlanet.getName());
        System.out.println("Type: " + foundPlanet.getPlanetType());
        System.out.println("Magnetic Field: " +
                foundPlanet.getMagneticField().getStrengthComparedToEarth() + "× Earth");
        System.out.println("Auroral Colors: " +
                foundPlanet.getMagneticField().getAuroralColors());
        System.out.println("Atmosphere: " + foundPlanet.getAtmosphereComposition());
    }

    private void printJSON(Map<String, Object> output, String title) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String json = mapper.writeValueAsString(output);
        System.out.println(title);
        System.out.println("\n========== JSON OUTPUT ==========");
        System.out.println(json);
        System.out.println("=================================\n");
    }
}