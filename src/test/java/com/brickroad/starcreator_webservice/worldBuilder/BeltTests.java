package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.creator.BeltCreator;
import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.enums.BandCategory;
import com.brickroad.starcreator_webservice.enums.BinaryConfiguration;
import com.brickroad.starcreator_webservice.utils.BinaryStabilityLimits;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
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

            List<OrbitalBand> bands = system.getBands();
            if (bands != null && !bands.isEmpty()) {
                systemsWithBelts++;

                for (OrbitalBand band : bands) {
                    assertNotNull(band.getBeltType(), "Belt type should not be null");
                    assertNotNull(band.getInnerOrbit(), "Inner orbit should not be null");
                    assertNotNull(band.getOuterOrbit(), "Outer orbit should not be null");
                    assertTrue(band.getOuterOrbit().getSemiMajorAxis() > band.getInnerOrbit().getSemiMajorAxis(),
                            "Outer radius should be greater than inner radius");

                    String code = band.getBeltType().getCode();
                    switch (code) {
                        case "INNER_ROCKY":
                            totalInnerBelts++;
                            break;
                        case "KUIPER":
                            totalKuiperBelts++;
                            totalDwarfPlanetsInBelts += band.getDwarfPlanets().size();
                            break;
                        case "SCATTERED_DISK":
                            totalScatteredDisks++;
                            break;
                    }

                    totalAsteroids += band.getNotableAsteroids().size();

                    // Validate asteroids
                    for (Asteroid asteroid : band.getNotableAsteroids()) {
                        assertNotNull(asteroid.getName(), "Asteroid name should not be null");
                        assertNotNull(asteroid.getAsteroidType(), "Asteroid type should not be null");
                        assertNotNull(asteroid.getSemiMajorAxisAu(), "Asteroid SMA should not be null");
                        assertTrue(asteroid.getSemiMajorAxisAu() >= band.getInnerOrbit().getSemiMajorAxis() * 0.8,
                                "Asteroid should be within belt bounds (inner)");
                        assertTrue(asteroid.getSemiMajorAxisAu() <= band.getOuterOrbit().getSemiMajorAxis() * 1.2,
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

            if (system.getBands() != null && !system.getBands().isEmpty()) {
                // Look for a system with both inner and Kuiper belt
                boolean hasInner = system.getBands().stream()
                        .anyMatch(b -> b.getBeltType() != null && "INNER_ROCKY".equals(b.getBeltType().getCode()));
                boolean hasKuiper = system.getBands().stream()
                        .anyMatch(b -> b.getBeltType() != null && "KUIPER".equals(b.getBeltType().getCode()));

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

    @Test
    public void validateBeltPhysics() throws JsonProcessingException {
        for (int i = 0; i < 50; i++) {
            StarSystem system = systemCreator.generateSystem();

            if (system.getBands() == null) continue;

            for (OrbitalBand band : system.getBands()) {
                // Belt should have reasonable mass
                if (band.getTotalMassEarthMasses() != null) {
                    assertTrue(band.getTotalMassEarthMasses() > 0,
                            "Belt mass should be positive");
                    assertTrue(band.getTotalMassEarthMasses() < 1.0,
                            "Belt mass should be less than 1 Earth mass");
                }

                // Eccentricity should be reasonable
                if (band.getAverageEccentricity() != null) {
                    assertTrue(band.getAverageEccentricity() >= 0 &&
                               band.getAverageEccentricity() < 1,
                            "Eccentricity should be between 0 and 1");
                }

                // Check asteroids don't overlap with planets
                for (Asteroid asteroid : band.getNotableAsteroids()) {
                    for (Planet planet : system.getPlanets()) {
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

    @Test
    public void testAsteroidTypeDistribution() {
        Map<String, Integer> typeCount = new HashMap<>();
        var ref = new Object() {
            int totalAsteroids = 0;
        };

        for (int i = 0; i < 1000; i++) {
            StarSystem system = systemCreator.generateSystem();

            if (system.getBands() == null) continue;

            for (OrbitalBand band : system.getBands()) {
                for (Asteroid asteroid : band.getNotableAsteroids()) {
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

    // ═════════════════════════════════════════════════════════════════════
    //  Multi-Star Belt Tests
    // ═════════════════════════════════════════════════════════════════════

    @Test
    public void testWideBinaryBelts() {
        int systemsTested = 0;
        int systemsWithPrimaryBelts = 0;
        int systemsWithSecondaryBelts = 0;
        int stabilityViolations = 0;

        for (int i = 0; i < 500 && systemsTested < 30; i++) {
            StarSystem system = systemCreator.generateSystem();
            if (system.getBinaryConfiguration() != BinaryConfiguration.S_TYPE_WIDE) continue;
            systemsTested++;

            Star primary = system.getStars().stream()
                    .filter(s -> s.getStarRole() == Star.StarRole.PRIMARY)
                    .findFirst().orElse(null);
            Star secondary = system.getStars().stream()
                    .filter(s -> s.getStarRole() == Star.StarRole.SECONDARY)
                    .findFirst().orElse(null);
            assertNotNull(primary, "Wide binary should have a primary star");
            assertNotNull(secondary, "Wide binary should have a secondary star");

            double binarySep = system.getBinarySeparationAu();
            double outerCapPrimary = BinaryStabilityLimits.sTypeCriticalSMA(
                    binarySep, primary.getSolarMass(), secondary.getSolarMass());
            double outerCapSecondary = BinaryStabilityLimits.sTypeCriticalSMA(
                    binarySep, secondary.getSolarMass(), primary.getSolarMass());

            // Check primary star belts
            if (!primary.getBands().isEmpty()) {
                systemsWithPrimaryBelts++;
                for (OrbitalBand band : primary.getBands()) {
                    assertNotNull(band.getStar(), "Belt should have a parent star");
                    assertEquals(primary, band.getStar(), "Primary belt should be parented to primary star");
                    assertNotNull(band.getInnerOrbit(), "Belt should have inner orbit");
                    assertNotNull(band.getOuterOrbit(), "Belt should have outer orbit");

                    double outerEdge = band.getOuterOrbit().getSemiMajorAxis();
                    if (outerEdge > outerCapPrimary * 1.01) { // 1% tolerance for floating point
                        stabilityViolations++;
                        System.out.printf("VIOLATION: Primary belt %s outer edge %.2f AU > cap %.2f AU (sep=%.1f AU)%n",
                                band.getBandType(), outerEdge, outerCapPrimary, binarySep);
                    }
                }
            }

            // Check secondary star belts
            if (!secondary.getBands().isEmpty()) {
                systemsWithSecondaryBelts++;
                for (OrbitalBand band : secondary.getBands()) {
                    assertNotNull(band.getStar(), "Belt should have a parent star");
                    assertEquals(secondary, band.getStar(), "Secondary belt should be parented to secondary star");

                    double outerEdge = band.getOuterOrbit().getSemiMajorAxis();
                    if (outerEdge > outerCapSecondary * 1.01) {
                        stabilityViolations++;
                        System.out.printf("VIOLATION: Secondary belt %s outer edge %.2f AU > cap %.2f AU (sep=%.1f AU)%n",
                                band.getBandType(), outerEdge, outerCapSecondary, binarySep);
                    }
                }
            }

            // Verify no belt appears on both stars (each belt has exactly one parent)
            for (OrbitalBand band : system.getBands()) {
                assertNotNull(band.getStar(), "Every belt must have a parent star set");
            }
        }

        System.out.println("=== Wide Binary Belt Statistics ===");
        System.out.println("S_TYPE_WIDE systems tested: " + systemsTested);
        System.out.println("With primary belts: " + systemsWithPrimaryBelts);
        System.out.println("With secondary belts: " + systemsWithSecondaryBelts);
        System.out.println("Stability violations: " + stabilityViolations);

        assertTrue(systemsTested > 0, "Should find at least one S_TYPE_WIDE system in 500 attempts");
        assertEquals(0, stabilityViolations, "No belt should exceed Holman-Wiegert stability limit");
    }

    @Test
    public void testCircumbinaryBelts() {
        int systemsTested = 0;
        int systemsWithBelts = 0;
        int cavityViolations = 0;

        for (int i = 0; i < 500 && systemsTested < 30; i++) {
            StarSystem system = systemCreator.generateSystem();
            if (system.getBinaryConfiguration() != BinaryConfiguration.P_TYPE) continue;
            systemsTested++;

            Star primary = system.getStars().stream()
                    .filter(s -> s.getStarRole() == Star.StarRole.PRIMARY)
                    .findFirst().orElse(null);
            Star secondary = system.getStars().stream()
                    .filter(s -> s.getStarRole() == Star.StarRole.SECONDARY)
                    .findFirst().orElse(null);
            assertNotNull(primary, "P_TYPE should have a primary star");

            double binarySep = system.getBinarySeparationAu();
            double innerCavity = BinaryStabilityLimits.pTypeCriticalSMA(
                    binarySep, primary.getSolarMass(),
                    secondary != null ? secondary.getSolarMass() : 0);

            // ALL belts should be parented to the primary star (circumbinary convention)
            List<OrbitalBand> allBelts = system.getBands();
            if (!allBelts.isEmpty()) {
                systemsWithBelts++;

                for (OrbitalBand band : allBelts) {
                    assertNotNull(band.getStar(), "Circumbinary belt must have a parent star");
                    assertEquals(primary, band.getStar(),
                            "All circumbinary belts should be parented to the primary star");

                    // Inner edge should respect the P-type cavity
                    double innerEdge = band.getInnerOrbit().getSemiMajorAxis();
                    if (innerEdge < innerCavity * 0.99) { // 1% tolerance
                        cavityViolations++;
                        System.out.printf("VIOLATION: Belt %s inner edge %.2f AU < cavity %.2f AU (sep=%.3f AU)%n",
                                band.getBandType(), innerEdge, innerCavity, binarySep);
                    }
                }
            }

            // Secondary star should have NO belts
            if (secondary != null) {
                assertTrue(secondary.getBands().isEmpty(),
                        "Secondary star in P_TYPE should have no belts (all are circumbinary on primary)");
            }
        }

        System.out.println("=== Circumbinary Belt Statistics ===");
        System.out.println("P_TYPE systems tested: " + systemsTested);
        System.out.println("With belts: " + systemsWithBelts);
        System.out.println("Cavity violations: " + cavityViolations);

        assertTrue(systemsTested > 0, "Should find at least one P_TYPE system in 500 attempts");
        assertEquals(0, cavityViolations, "No circumbinary belt should encroach on the inner cavity");
    }

    @Test
    public void testHierarchicalBelts() {
        int systemsTested = 0;
        int systemsWithPairBelts = 0;
        int systemsWithTertiaryBelts = 0;

        for (int i = 0; i < 1000 && systemsTested < 15; i++) {
            StarSystem system = systemCreator.generateSystem();
            BinaryConfiguration config = system.getBinaryConfiguration();
            if (config != BinaryConfiguration.HIERARCHICAL_BINARY_THIRD
                    && config != BinaryConfiguration.HIERARCHICAL_TRIPLE) continue;
            systemsTested++;

            Star primary = system.getStars().stream()
                    .filter(s -> s.getStarRole() == Star.StarRole.PRIMARY)
                    .findFirst().orElse(null);
            Star tertiary = system.getStars().stream()
                    .filter(s -> s.getStarRole() == Star.StarRole.TERTIARY)
                    .findFirst().orElse(null);
            assertNotNull(primary, "Hierarchical should have a primary star");

            // Primary star hosts circumbinary belts for the close pair
            if (!primary.getBands().isEmpty()) {
                systemsWithPairBelts++;
                for (OrbitalBand band : primary.getBands()) {
                    assertEquals(primary, band.getStar(),
                            "Close pair belts should be parented to primary");
                }
            }

            // Tertiary star (if present) may have its own circumstellar belts
            if (tertiary != null && !tertiary.getBands().isEmpty()) {
                systemsWithTertiaryBelts++;
                for (OrbitalBand band : tertiary.getBands()) {
                    assertEquals(tertiary, band.getStar(),
                            "Tertiary belts should be parented to tertiary star");

                    // Tertiary belts should be stability-capped
                    double binarySep = system.getBinarySeparationAu();
                    double tertiarySep = binarySep * 3.0;
                    Star secondary = system.getStars().stream()
                            .filter(s -> s.getStarRole() == Star.StarRole.SECONDARY)
                            .findFirst().orElse(null);
                    double pairMass = primary.getSolarMass()
                            + (secondary != null ? secondary.getSolarMass() : 0);
                    double outerCap = BinaryStabilityLimits.sTypeCriticalSMA(
                            tertiarySep, tertiary.getSolarMass(), pairMass);

                    double outerEdge = band.getOuterOrbit().getSemiMajorAxis();
                    assertTrue(outerEdge <= outerCap * 1.01,
                            String.format("Tertiary belt outer edge %.2f AU should not exceed cap %.2f AU",
                                    outerEdge, outerCap));
                }
            }
        }

        System.out.println("=== Hierarchical Belt Statistics ===");
        System.out.println("Hierarchical systems tested: " + systemsTested);
        System.out.println("With close pair belts: " + systemsWithPairBelts);
        System.out.println("With tertiary belts: " + systemsWithTertiaryBelts);

        // Hierarchical systems are rare, so just make sure we tested some
        if (systemsTested == 0) {
            System.out.println("WARNING: No hierarchical systems found in 1000 attempts — skip assertions");
        }
    }

    @Test
    public void testStabilityLimits() {
        // ── S-type: equal-mass binary at 30 AU ──
        // mu = 0.5, a_crit = (0.464 - 0.380*0.5) * 30 = 0.274 * 30 = 8.22 AU
        // With 10% safety margin: 8.22 * 0.90 = 7.398 AU
        double sType = BinaryStabilityLimits.sTypeCriticalSMA(30.0, 1.0, 1.0);
        assertEquals(7.398, sType, 0.01,
                "S-type limit for equal 1 Msol stars at 30 AU should be ~7.4 AU");

        // ── S-type: asymmetric binary ──
        // mu = 0.2/(1.0+0.2) = 0.167, a_crit = (0.464 - 0.380*0.167) * 50 = 0.4006 * 50 = 20.03
        // With margin: 20.03 * 0.90 = 18.027
        double sTypeAsym = BinaryStabilityLimits.sTypeCriticalSMA(50.0, 1.0, 0.2);
        assertEquals(18.03, sTypeAsym, 0.1,
                "S-type limit for 1.0 + 0.2 Msol at 50 AU");

        // ── P-type: equal-mass binary at 1 AU ──
        // mu = min(1,1)/2 = 0.5, a_crit = (1.60 + 4.12*0.5) * 1.0 = 3.66
        // With margin outward: 3.66 / 0.90 = 4.067
        double pType = BinaryStabilityLimits.pTypeCriticalSMA(1.0, 1.0, 1.0);
        assertEquals(4.067, pType, 0.01,
                "P-type limit for equal 1 Msol stars at 1 AU should be ~4.07 AU");

        // ── P-type: close binary ──
        // mu = 0.3/1.3 = 0.231, a_crit = (1.60 + 4.12*0.231) * 0.5 = (1.60 + 0.951) * 0.5 = 1.276
        // With margin: 1.276 / 0.90 = 1.418
        double pTypeClose = BinaryStabilityLimits.pTypeCriticalSMA(0.5, 1.0, 0.3);
        assertEquals(1.418, pTypeClose, 0.01,
                "P-type limit for 1.0 + 0.3 Msol at 0.5 AU");

        // ── Dead zone: center (80 AU) should have low probability ──
        double deadCenter = BinaryStabilityLimits.beltFormationProbability(80.0);
        assertTrue(deadCenter <= 0.2,
                "Dead zone center (80 AU) should have very low belt probability, got " + deadCenter);

        // ── Dead zone: outside range should be 1.0 ──
        assertEquals(1.0, BinaryStabilityLimits.beltFormationProbability(10.0), 0.001,
                "Below 25 AU should have full belt probability");
        assertEquals(1.0, BinaryStabilityLimits.beltFormationProbability(200.0), 0.001,
                "Above 135 AU should have full belt probability");

        // ── Dead zone: edge should be higher than center ──
        double deadEdge = BinaryStabilityLimits.beltFormationProbability(30.0);
        assertTrue(deadEdge > deadCenter,
                "Dead zone edge (30 AU) should have higher probability than center (80 AU)");

        System.out.println("=== Stability Limit Tests ===");
        System.out.printf("S-type (30 AU, equal mass): %.3f AU%n", sType);
        System.out.printf("S-type (50 AU, 1.0+0.2): %.3f AU%n", sTypeAsym);
        System.out.printf("P-type (1 AU, equal mass): %.3f AU%n", pType);
        System.out.printf("P-type (0.5 AU, 1.0+0.3): %.3f AU%n", pTypeClose);
        System.out.printf("Dead zone center prob: %.3f%n", deadCenter);
        System.out.printf("Dead zone edge prob: %.3f%n", deadEdge);
    }

    @Test
    public void testBeltParentStarIntegrity() {
        int systemsTested = 0;
        int beltsChecked = 0;

        for (int i = 0; i < 200; i++) {
            StarSystem system = systemCreator.generateSystem();
            systemsTested++;

            for (OrbitalBand band : system.getBands()) {
                beltsChecked++;

                // Every belt must have a parent star
                assertNotNull(band.getStar(), "Every belt must have a parent star");

                // The parent star must be in the system's star set
                assertTrue(system.getStars().contains(band.getStar()),
                        "Belt's parent star must be in the system's star set");

                // The belt must also be in its parent star's bands list
                assertTrue(band.getStar().getBands().contains(band),
                        "Belt must be in its parent star's bands collection");

                // Belt category should be BELT (not RING — rings are on planets)
                assertEquals(BandCategory.BELT, band.getBandCategory(),
                        "System-level bands should be BELT category");

                // Outer orbit > inner orbit
                assertTrue(band.getOuterOrbit().getSemiMajorAxis() > band.getInnerOrbit().getSemiMajorAxis(),
                        "Outer edge must be beyond inner edge");
            }
        }

        System.out.println("=== Belt Parent Star Integrity ===");
        System.out.println("Systems tested: " + systemsTested);
        System.out.println("Belts checked: " + beltsChecked);
    }
}
