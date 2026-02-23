package com.brickroad.starcreator_webservice.persistence;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.service.CreationService;
import com.brickroad.starcreator_webservice.service.SystemPersistenceService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class SystemPersistenceTest {

    @Autowired
    private CreationService creationService;

    @Autowired
    private SystemPersistenceService persistenceService;

    @Test
    public void testSaveAndLoadSystem() throws IOException {
        // ── Generate ──
        StarSystem generated = creationService.createStarSystem();
        assertNotNull(generated, "Generated system should not be null");
        assertFalse(generated.getStars().isEmpty(), "System should have stars");
        assertFalse(generated.getPlanets().isEmpty(), "System should have planets");

        // Capture pre-save counts
        int starCount = generated.getStars().size();
        int planetCount = generated.getPlanets().size();
        int moonCount = generated.getPlanets().stream()
                .mapToInt(p -> p.getMoons().size()).sum();
        int bandCount = generated.getBands().size();
        int planetRingCount = generated.getPlanets().stream()
                .mapToInt(p -> p.getBands().size()).sum();
        int asteroidCount = generated.getBands().stream()
                .mapToInt(b -> b.getNotableAsteroids().size()).sum();

        String generatedName = generated.getName();
        String generatedJson = toJson(generated);

        System.out.println("=== PRE-SAVE STATS ===");
        System.out.printf("Stars: %d, Planets: %d, Moons: %d, Belts: %d, Rings: %d, Asteroids: %d%n",
                starCount, planetCount, moonCount, bandCount, planetRingCount, asteroidCount);

        // ── Save ──
        StarSystem saved = persistenceService.saveSystem(generated);
        assertNotNull(saved.getId(), "Saved system should have an ID");

        Long savedId = saved.getId();
        System.out.println("Saved system ID: " + savedId);

        // ── Load ──
        Optional<StarSystem> loaded = persistenceService.loadSystem(savedId);
        assertTrue(loaded.isPresent(), "Loaded system should be present");

        StarSystem loadedSystem = loaded.get();

        // ── Verify Core Fields ──
        assertEquals(generatedName, loadedSystem.getName(), "System name mismatch");
        assertEquals(starCount, loadedSystem.getStars().size(), "Star count mismatch");
        assertEquals(planetCount, loadedSystem.getPlanets().size(), "Planet count mismatch");
        assertEquals(bandCount, loadedSystem.getBands().size(), "Belt count mismatch");

        int loadedMoonCount = loadedSystem.getPlanets().stream()
                .mapToInt(p -> p.getMoons().size()).sum();
        assertEquals(moonCount, loadedMoonCount, "Moon count mismatch");

        int loadedPlanetRingCount = loadedSystem.getPlanets().stream()
                .mapToInt(p -> p.getBands().size()).sum();
        assertEquals(planetRingCount, loadedPlanetRingCount, "Planet ring count mismatch");

        int loadedAsteroidCount = loadedSystem.getBands().stream()
                .mapToInt(b -> b.getNotableAsteroids().size()).sum();
        assertEquals(asteroidCount, loadedAsteroidCount, "Asteroid count mismatch");

        // ── Verify Designation Fields Survived Round-Trip ──
        assertNotNull(loadedSystem.getDesignation(), "System designation should exist");
        assertEquals(generatedName, loadedSystem.getDesignation().getLoggedName(),
                "System designation name mismatch");

        for (Star star : loadedSystem.getStars()) {
            assertNotNull(star.getDesignation(), "Star designation should exist");
            assertNotNull(star.getDesignation().getLoggedName(), "Star name should be set");
            assertNotNull(star.getDesignation().getObjectType(), "Star type should be set");
            assertNotNull(star.getPhysicalProperties(), "Star physical properties should exist");
        }

        for (Planet planet : loadedSystem.getPlanets()) {
            assertNotNull(planet.getDesignation(), "Planet designation should exist");
            assertNotNull(planet.getDesignation().getLoggedName(), "Planet name should be set");
            assertNotNull(planet.getDesignation().getObjectType(), "Planet type should be set");
            assertNotNull(planet.getPhysicalProperties(), "Planet physical properties should exist");
            assertNotNull(planet.getOrbit(), "Planet orbit should exist");

            for (Moon moon : planet.getMoons()) {
                assertNotNull(moon.getDesignation(), "Moon designation should exist");
                assertNotNull(moon.getDesignation().getLoggedName(), "Moon name should be set");
                assertNotNull(moon.getPhysicalProperties(), "Moon physical properties should exist");
            }
        }

        for (OrbitalBand band : loadedSystem.getBands()) {
            assertNotNull(band.getDesignation(), "Band designation should exist");
            assertNotNull(band.getInnerOrbit(), "Band inner orbit should exist");
            assertNotNull(band.getOuterOrbit(), "Band outer orbit should exist");

            for (Asteroid asteroid : band.getNotableAsteroids()) {
                assertNotNull(asteroid.getDesignation(), "Asteroid designation should exist");
                assertNotNull(asteroid.getPhysicalProperties(), "Asteroid physical properties should exist");
            }
        }

        // ── Verify Classification Was Recomputed ──
        assertNotNull(loadedSystem.getClassification(), "Classification should be recomputed on load");

        // ── Verify Sector Survived ──
        assertNotNull(loadedSystem.getSector(), "Sector should be loaded");
        assertEquals(generated.getSector().getName(), loadedSystem.getSector().getName(),
                "Sector name mismatch");

        // ── Save JSON Output for Comparison ──
        String loadedJson = toJson(loadedSystem);

        File targetFolder = new File("target/persistence-test/");
        if (!targetFolder.exists()) targetFolder.mkdirs();

        try (FileWriter w1 = new FileWriter(new File(targetFolder, "generated.json"))) {
            w1.write(generatedJson);
        }
        try (FileWriter w2 = new FileWriter(new File(targetFolder, "loaded.json"))) {
            w2.write(loadedJson);
        }

        System.out.println("=== POST-LOAD STATS ===");
        System.out.printf("Stars: %d, Planets: %d, Moons: %d, Belts: %d, Rings: %d, Asteroids: %d%n",
                loadedSystem.getStars().size(),
                loadedSystem.getPlanets().size(),
                loadedMoonCount, loadedSystem.getBands().size(),
                loadedPlanetRingCount, loadedAsteroidCount);
        System.out.println("JSON files saved to: " + targetFolder.getAbsolutePath());
        System.out.println("=== ROUND-TRIP TEST PASSED ===");

        // ── Cleanup ──
        persistenceService.deleteSystem(savedId);
        assertTrue(persistenceService.loadSystem(savedId).isEmpty(), "System should be deleted");
    }

    private String toJson(StarSystem system) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.writeValueAsString(system);
    }
}
