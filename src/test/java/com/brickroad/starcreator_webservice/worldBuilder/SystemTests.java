package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.entity.ref.TerrainTypeRef;
import com.brickroad.starcreator_webservice.entity.ud.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@ActiveProfiles("test")
public class SystemTests extends AbstractCreatorTest {

    @Autowired
    private SystemCreator systemCreator;

    private static final Map<String, Integer> STAR_TYPES = new HashMap<>();
    private static final Map<Integer, Integer> STAR_AMOUNTS = new HashMap<>();
    private static final Map<String, Integer> PLANET_TYPES = new HashMap<>();
    private static final Map<String, Integer> MOON_TYPES = new HashMap<>();
    private static final Map<String, Integer> RING_TYPES = new HashMap<>();
    private static final Map<String, Integer> BELT_TYPES = new HashMap<>();
    private static final Map<String, Integer> ASTEROID_TYPES = new HashMap<>();
    private static final ProbabilityCounts COUNTS = new ProbabilityCounts();
    private static final PerformanceTimer TIMER = new PerformanceTimer();

    private static final int SYSTEM_AMOUNT = 100_000;

    //@Test
    public void findSystem() throws JsonProcessingException {

        String targetType = "Terrestrial Planet";
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

    //@Test
    public void SystemProbabilityTest() {

        COUNTS.setSystemCount(SYSTEM_AMOUNT);
        TIMER.start();

        for (int i = 0; i < COUNTS.getSystemCount(); i++) {

            if (i % 1000 == 0 && i != 0) {
                printETA(TIMER.averageLap(),COUNTS.getSystemCount(),i);
            }

            StarSystem system = systemCreator.generateSystem();

            COUNTS.incrementStarCount(system.getStars().size());
            STAR_AMOUNTS.put(system.getStars().size(), STAR_AMOUNTS.getOrDefault(system.getStars().size(), 0) + 1);
            for (Star star : system.getStars()) {
                STAR_TYPES.put(star.getType(), STAR_TYPES.getOrDefault(star.getType(), 0) + 1);
            }

            COUNTS.incrementPlanetCount(system.getPlanets().size());
            for (CelestialBody planet : system.getPlanets()) {
                planetTestData((Planet) planet);
            }

            COUNTS.incrementBeltCount(system.getBelts().size());
            for (Belt belt : system.getBelts()) {
                beltTestData(belt);
            }
            TIMER.lap();
        }
        TIMER.stop();

        saveProbabilityResults();
    }

    private void saveProbabilityResults() {

        File targetFolder = new File("target/probability_reports/");
        if (!targetFolder.exists()) targetFolder.mkdirs();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMdd_HHmm");
        String timestamp = LocalDateTime.now().format(formatter);
        File file = new File(targetFolder, "system_report_" + timestamp + ".md");
        formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("# System Probability Report");
            writer.println(LocalDateTime.now().format(formatter));
            writer.println("");
            writer.println("---");
            writer.println("Created " + COUNTS.getSystemCount() + " systems in " + TIMER.getFinalTime());
            writer.println("- Stars Created: " + COUNTS.getStarCount());
            writer.println("- Planets Created: " + COUNTS.getPlanetCount());
            writer.println("- Moons Created: " + COUNTS.getMoonCount());
            writer.println("- Rings Created: " + COUNTS.getRingCount());
            writer.println("- Belts Created: " + COUNTS.getBeltCount());
            writer.println("- Asteroids Created: " + COUNTS.getAsteroidCount());
            writer.println("");
            writer.println("Average time to create one system: " + TIMER.averageLap() + "ms");
            writer.println("");
            writer.println("---");
            writer.println("## Star Amounts");
            STAR_AMOUNTS.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(entry -> writer.println("* Stars " + entry.getKey() + ": " + entry.getValue() + " (" + (entry.getValue() * 100.0) / COUNTS.getSystemCount() + "%)"));
            writer.println("---");
            writer.println("## Star Types");
            STAR_TYPES.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(entry -> writer.println("* " + entry.getKey() + ": " + entry.getValue() + " (" + (entry.getValue() * 100.0) / COUNTS.getStarCount() + "%)"));

            writer.println("---");
            writer.println("## Planet Types");
            writer.println("");
            PLANET_TYPES.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(entry -> writer.println("* " + entry.getKey() + ": " + entry.getValue() + " (" + (entry.getValue() * 100.0) / COUNTS.getPlanetCount() + "%)"));

            writer.println("---");
            writer.println("## Moon Types");
            MOON_TYPES.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(entry -> writer.println("* " + entry.getKey() + ": " + entry.getValue() + " (" + (entry.getValue() * 100.0) / COUNTS.getMoonCount() + "%)"));

            writer.println("---");
            writer.println("## Ring Types");
            RING_TYPES.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(entry -> writer.println("* " + entry.getKey() + ": " + entry.getValue() + " (" + (entry.getValue() * 100.0) / COUNTS.getRingCount() + "%)"));

            writer.println("---");
            writer.println("## Belt Types");
            BELT_TYPES.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(entry -> writer.println("* " + entry.getKey() + ": " + entry.getValue() + " (" + (entry.getValue() * 100.0) / COUNTS.getBeltCount() + "%)"));

            writer.println("---");
            writer.println("## Asteroid Types");
            ASTEROID_TYPES.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(entry -> writer.println("* " + entry.getKey() + ": " + entry.getValue() + " (" + (entry.getValue() * 100.0) / COUNTS.getAsteroidCount() + "%)"));
            writer.println("");
            writer.println("Dwarf Planets in Belts: " + COUNTS.getTempCount());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void planetTestData(Planet planet) {
        PLANET_TYPES.put(planet.getPlanetType(), PLANET_TYPES.getOrDefault(planet.getPlanetType(), 0) + 1);

        COUNTS.incrementMoonCount(planet.getMoons().size());
        for (Moon moon : planet.getMoons()) {
            moonTestData(moon);
        }

        COUNTS.incrementRingCount(planet.getRings().size());
        for (Ring ring : planet.getRings()) {
            ringTestData(ring);
        }

    }

    private void moonTestData(Moon moon) {
        MOON_TYPES.put(moon.getMoonType(), MOON_TYPES.getOrDefault(moon.getMoonType(), 0) + 1);
    }

    private void ringTestData(Ring ring) {
        RING_TYPES.put(ring.getRingType(), RING_TYPES.getOrDefault(ring.getRingType(), 0) + 1);
    }

    private void beltTestData(Belt belt) {
        BELT_TYPES.put(belt.getBeltType().getCode(), BELT_TYPES.getOrDefault(belt.getBeltType().getCode(), 0) + 1);
        COUNTS.incrementAsteroidCount(belt.getNotableAsteroids().size());
        for (Asteroid asteroid : belt.getNotableAsteroids()) {
            ASTEROID_TYPES.put(asteroid.getAsteroidType().getCode(), ASTEROID_TYPES.getOrDefault(asteroid.getAsteroidType().getCode(), 0) + 1);
        }
        COUNTS.incrementTempCount(belt.getDwarfPlanets().size());
    }
}