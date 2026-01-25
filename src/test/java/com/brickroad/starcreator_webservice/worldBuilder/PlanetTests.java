package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.creator.PlanetCreator;
import com.brickroad.starcreator_webservice.creator.StarCreator;
import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.entity.ud.CelestialBody;
import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.StarSystem;
import com.brickroad.starcreator_webservice.entity.ud.Star;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String targetType = "Gas Giant";
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
                    if (((Planet) planet).getMoons().stream()
                            .anyMatch(moon -> moon.getAtmosphereComposition() != null &&
                                    !moon.getAtmosphereComposition().contains("None"))) {
                        foundPlanet = (Planet) planet;
                        System.out.println("Searched " + (i + 1) + " systems...");
                        break;
                    }
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

        Map<String, Object> output = new HashMap<>();
        output.put("planet", foundPlanet);

        String title = "\n✅ FOUND ";
        printJSON(output, title);
    }

    //@Test
    public void findMultiplePlanetsOfType() throws JsonProcessingException {
        String targetType = "Desert Planet";
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
                if (targetType.equalsIgnoreCase(((Planet) planet).getPlanetType())) {
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
        //printJSON(output, title);
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
        saveJson(json);
        System.out.println(title);
        System.out.println("\n========== JSON OUTPUT ==========");
        System.out.println(json);
        System.out.println("=================================\n");
    }


    private void saveJson(String jsonString) {

        File targetFolder = new File("target/systemJSONs/");

        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        File jsonFile = new File(targetFolder, "System.json");

        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write(jsonString); // Write the JSON content to the file
            System.out.println("JSON file saved successfully to: " + jsonFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing JSON to file: " + e.getMessage());
        }
    }

}