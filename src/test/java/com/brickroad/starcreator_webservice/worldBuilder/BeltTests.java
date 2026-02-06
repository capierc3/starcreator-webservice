package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.creator.BeltCreator;
import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.enums.BinaryConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class BeltTests extends AbstractCreatorTest {

    @Autowired
    private SystemCreator systemCreator;

    @Autowired
    private BeltCreator beltCreator;

    private static final int SYSTEM_COUNT = 100;

    @Test
    public void testBeltGeneration() throws JsonProcessingException {
        int systemsWithBelts = 0;
        int totalInnerBelts = 0;
        int totalKuiperBelts = 0;
        int totalScatteredDisks = 0;
        int totalAsteroids = 0;
        int totalDwarfPlanetsInBelts = 0;

        for (int i = 0; i < SYSTEM_COUNT; i++) {
            StarSystem system = systemCreator.generateSystem();
            assertNotNull(system, "System should not be null");

            List<Belt> belts = system.getBelts();
            if (belts != null && !belts.isEmpty()) {
                systemsWithBelts++;

                for (Belt belt : belts) {
                    assertNotNull(belt.getBeltType(), "Belt type should not be null");
                    assertNotNull(belt.getInnerRadiusAu(), "Inner radius should not be null");
                    assertNotNull(belt.getOuterRadiusAu(), "Outer radius should not be null");
                    assertTrue(belt.getOuterRadiusAu() > belt.getInnerRadiusAu(),
                            "Outer radius should be greater than inner radius");

                    String code = belt.getBeltType().getCode();
                    switch (code) {
                        case "INNER_ROCKY":
                            totalInnerBelts++;
                            break;
                        case "KUIPER":
                            totalKuiperBelts++;
                            totalDwarfPlanetsInBelts += belt.getDwarfPlanets().size();
                            break;
                        case "SCATTERED_DISK":
                            totalScatteredDisks++;
                            break;
                    }

                    totalAsteroids += belt.getNotableAsteroids().size();

                    // Validate asteroids
                    for (Asteroid asteroid : belt.getNotableAsteroids()) {
                        assertNotNull(asteroid.getName(), "Asteroid name should not be null");
                        assertNotNull(asteroid.getAsteroidType(), "Asteroid type should not be null");
                        assertNotNull(asteroid.getSemiMajorAxisAu(), "Asteroid SMA should not be null");
                        assertTrue(asteroid.getSemiMajorAxisAu() >= belt.getInnerRadiusAu() * 0.8,
                                "Asteroid should be within belt bounds (inner)");
                        assertTrue(asteroid.getSemiMajorAxisAu() <= belt.getOuterRadiusAu() * 1.2,
                                "Asteroid should be within belt bounds (outer)");

                        if (asteroid.getMass() != null) {
                            assertTrue(asteroid.getMass() > 0, "Asteroid mass should be positive");
                        }
                    }
                }
            }
        }

        System.out.println("=== Belt Generation Statistics ===");
        System.out.println("Systems generated: " + SYSTEM_COUNT);
        System.out.println("Systems with belts: " + systemsWithBelts + " (" + 
                (systemsWithBelts * 100.0 / SYSTEM_COUNT) + "%)");
        System.out.println("Inner asteroid belts: " + totalInnerBelts);
        System.out.println("Kuiper belts: " + totalKuiperBelts);
        System.out.println("Scattered disks: " + totalScatteredDisks);
        System.out.println("Total notable asteroids: " + totalAsteroids);
        System.out.println("Dwarf planets in Kuiper belts: " + totalDwarfPlanetsInBelts);
        System.out.println("Average asteroids per belt: " + 
                (totalAsteroids / (double) Math.max(1, totalInnerBelts + totalKuiperBelts + totalScatteredDisks)));
    }

    @Test
    public void findSystemWithBelts() throws JsonProcessingException {
        int maxAttempts = 100;
        StarSystem foundSystem = null;

        for (int i = 0; i < maxAttempts; i++) {
            StarSystem system = systemCreator.generateSystem();

            if (system.getBelts() != null && !system.getBelts().isEmpty()) {
                // Look for a system with both inner and Kuiper belt
                boolean hasInner = system.getBelts().stream()
                        .anyMatch(b -> "INNER_ROCKY".equals(b.getBeltType().getCode()));
                boolean hasKuiper = system.getBelts().stream()
                        .anyMatch(b -> "KUIPER".equals(b.getBeltType().getCode()));

                if (hasInner && hasKuiper && system.getBinaryConfiguration().equals(BinaryConfiguration.P_TYPE)) {
                    foundSystem = system;
                    System.out.println("Found system with both belt types after " + (i + 1) + " attempts");
                    break;
                }
            }
        }

        if (foundSystem != null) {
            Map<String, Object> output = new HashMap<>();
            output.put("system", foundSystem);
            String json = listToJsonString(output);
            saveJson(json, "belt_system");
            System.out.println("System saved to belt_system.json");
        } else {
            System.out.println("No system with both belt types found in " + maxAttempts + " attempts");
        }
    }

    //@Test
    public void validateBeltPhysics() throws JsonProcessingException {
        for (int i = 0; i < 50; i++) {
            StarSystem system = systemCreator.generateSystem();

            if (system.getBelts() == null) continue;

            for (Belt belt : system.getBelts()) {
                // Belt should have reasonable mass
                if (belt.getTotalMassEarthMasses() != null) {
                    assertTrue(belt.getTotalMassEarthMasses() > 0, 
                            "Belt mass should be positive");
                    assertTrue(belt.getTotalMassEarthMasses() < 1.0, 
                            "Belt mass should be less than 1 Earth mass");
                }

                // Eccentricity should be reasonable
                if (belt.getAverageEccentricity() != null) {
                    assertTrue(belt.getAverageEccentricity() >= 0 && 
                               belt.getAverageEccentricity() < 1,
                            "Eccentricity should be between 0 and 1");
                }

                // Check asteroids don't overlap with planets
                for (Asteroid asteroid : belt.getNotableAsteroids()) {
                    for (CelestialBody body : system.getBodies()) {
                        if (body instanceof Planet) {
                            Planet planet = (Planet) body;
                            if (planet.getSemiMajorAxisAU() != null && 
                                asteroid.getSemiMajorAxisAu() != null) {
                                // Asteroids shouldn't be at exact same orbit as planets
                                double diff = Math.abs(planet.getSemiMajorAxisAU() - 
                                                      asteroid.getSemiMajorAxisAu());
                                assertTrue(diff > 0.01 || 
                                          "Dwarf Planet".equals(planet.getPlanetType()),
                                        "Asteroid shouldn't share exact orbit with major planet");
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testAsteroidTypeDistribution() {
        Map<String, Integer> typeCount = new HashMap<>();
        var ref = new Object() {
            int totalAsteroids = 0;
        };

        for (int i = 0; i < 1000; i++) {
            StarSystem system = systemCreator.generateSystem();

            if (system.getBelts() == null) continue;

            for (Belt belt : system.getBelts()) {
                for (Asteroid asteroid : belt.getNotableAsteroids()) {
                    String typeName = asteroid.getAsteroidType().getName();
                    typeCount.merge(typeName, 1, Integer::sum);
                    ref.totalAsteroids++;
                }
            }
        }

        System.out.println("=== Asteroid Type Distribution ===");
        System.out.println("Total asteroids: " + ref.totalAsteroids);
        typeCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .forEach(e -> System.out.printf("%s: %d (%.1f%%)\n", 
                        e.getKey(), e.getValue(), e.getValue() * 100.0 / ref.totalAsteroids));
    }
}
