package com.brickroad.starcreator_webservice.baseline;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.model.climate.*;
import com.brickroad.starcreator_webservice.model.habitability.*;
import com.brickroad.starcreator_webservice.service.CreationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Locks down the "field contract" for climate and habitability data.
 * <p>
 * Generates multiple systems and verifies that all expected fields are populated
 * on the appropriate planet/moon types. After the Tier 2C migration (seeded
 * regeneration), these same assertions must still pass — ensuring no fields
 * or child collections are accidentally dropped.
 * <p>
 * Uses soft assertions: accumulates all violations and reports them together.
 */
@SpringBootTest
@ActiveProfiles("test")
class ClimateHabitabilityBaselineTest {

    private static final int SYSTEM_COUNT = 10;

    @Autowired
    private CreationService creationService;

    // ══════════════════════════════════════════════════════════════════
    //  Climate Field Completeness
    // ══════════════════════════════════════════════════════════════════

    //@Test
    void testClimateFieldCompleteness() {
        List<String> violations = new ArrayList<>();
        int rockyWithAtmo = 0;

        for (int i = 0; i < SYSTEM_COUNT; i++) {
            StarSystem system = creationService.createStarSystem();

            for (Planet planet : system.getPlanets()) {
                if (isGaseousBody(planet)) continue;
                String atmoClass = planet.getAtmosphereClassification();
                if (atmoClass == null || "NONE".equals(atmoClass)) continue;

                rockyWithAtmo++;
                String prefix = "system[" + i + "]." + planetLabel(planet);
                PlanetaryClimate c = planet.getClimate();

                assertNotNull(violations, prefix + ".climate", c);
                if (c == null) continue;

                // Atmospheric structure
                assertNotNull(violations, prefix + ".scaleHeightKm", c.getScaleHeightKm());
                assertNotNull(violations, prefix + ".tropopauseAltitudeKm", c.getTropopauseAltitudeKm());
                assertNotNull(violations, prefix + ".totalAtmosphereHeightKm", c.getTotalAtmosphereHeightKm());
                assertNotNull(violations, prefix + ".skyColor", c.getSkyColor());

                // Temperature
                assertNotNull(violations, prefix + ".dayNightTempRangeK", c.getDayNightTempRangeK());
                assertNotNull(violations, prefix + ".seasonalTempAmplitudeK", c.getSeasonalTempAmplitudeK());
                assertNotNull(violations, prefix + ".thermalInertiaClass", c.getThermalInertiaClass());
                assertNotNull(violations, prefix + ".meanEquatorialTempK", c.getMeanEquatorialTempK());
                assertNotNull(violations, prefix + ".meanPolarTempK", c.getMeanPolarTempK());

                // Wind
                assertNotNull(violations, prefix + ".circulationPattern", c.getCirculationPattern());
                assertNotNull(violations, prefix + ".meanSurfaceWindSpeedMs", c.getMeanSurfaceWindSpeedMs());
                assertNotNull(violations, prefix + ".windIntensity", c.getWindIntensity());

                // Clouds
                assertNotNull(violations, prefix + ".cloudCoveragePercent", c.getCloudCoveragePercent());
                assertNotNull(violations, prefix + ".cloudCoverageClass", c.getCloudCoverageClass());

                // Weather summary
                assertNotNull(violations, prefix + ".weatherSummary", c.getWeatherSummary());
                assertNotNull(violations, prefix + ".weatherSeverity", c.getWeatherSeverity());
                assertNotNull(violations, prefix + ".outdoorExposureRating", c.getOutdoorExposureRating());
                assertNotNull(violations, prefix + ".survivalTimeDescription", c.getSurvivalTimeDescription());

                // Collections
                assertNotNull(violations, prefix + ".climateZones", c.getClimateZones());
                if (c.getClimateZones() != null && c.getClimateZones().isEmpty()) {
                    violations.add(prefix + ".climateZones is empty (expected >= 1 zone)");
                }
                assertNotNull(violations, prefix + ".cloudLayers", c.getCloudLayers());
            }
        }

        assertTrue(rockyWithAtmo > 0,
                "Should have found at least one rocky planet with atmosphere across " + SYSTEM_COUNT + " systems");
        assertTrue(violations.isEmpty(),
                "Climate field contract violations (" + violations.size() + "):\n" + String.join("\n", violations));
    }

    //@Test
    void testGasGiantClimateFields() {
        List<String> violations = new ArrayList<>();
        int gasGiantCount = 0;

        for (int i = 0; i < SYSTEM_COUNT; i++) {
            StarSystem system = creationService.createStarSystem();

            for (Planet planet : system.getPlanets()) {
                if (!isGaseousBody(planet)) continue;

                gasGiantCount++;
                String prefix = "system[" + i + "]." + planetLabel(planet);
                PlanetaryClimate c = planet.getClimate();

                assertNotNull(violations, prefix + ".climate", c);
                if (c == null) continue;

                // Gas giant specific — only JOVIAN/ICE_GIANT atmosphere classes
                // get band structure and internal heat from GasGiantFeatureCalculator.
                // Smaller Neptune subtypes (Mini-Neptune, Warm Neptune, Hot Neptune)
                // may have different atmosphere classifications and skip these fields.
                String atmClass = planet.getAtmosphereClassification();
                if ("JOVIAN".equals(atmClass) || "ICE_GIANT".equals(atmClass)) {
                    assertNotNull(violations, prefix + ".numberOfBands", c.getNumberOfBands());
                    assertNotNull(violations, prefix + ".internalHeatFluxWm2", c.getInternalHeatFluxWm2());
                }

                // Core weather fields should still be populated
                assertNotNull(violations, prefix + ".meanSurfaceWindSpeedMs", c.getMeanSurfaceWindSpeedMs());
                assertNotNull(violations, prefix + ".windIntensity", c.getWindIntensity());
                assertNotNull(violations, prefix + ".weatherSummary", c.getWeatherSummary());
                assertNotNull(violations, prefix + ".weatherSeverity", c.getWeatherSeverity());
            }
        }

        assertTrue(gasGiantCount > 0,
                "Should have found at least one gas giant across " + SYSTEM_COUNT + " systems");
        assertTrue(violations.isEmpty(),
                "Gas giant climate violations (" + violations.size() + "):\n" + String.join("\n", violations));
    }

    // ══════════════════════════════════════════════════════════════════
    //  Habitability Field Completeness
    // ══════════════════════════════════════════════════════════════════

    //@Test
    void testHabitabilityFieldCompleteness() {
        List<String> violations = new ArrayList<>();
        int rockyCount = 0;

        for (int i = 0; i < SYSTEM_COUNT; i++) {
            StarSystem system = creationService.createStarSystem();

            for (Planet planet : system.getPlanets()) {
                if (isGaseousBody(planet)) continue;

                rockyCount++;
                String prefix = "system[" + i + "]." + planetLabel(planet);
                PlanetaryHabitability h = planet.getHabitability();

                assertNotNull(violations, prefix + ".habitability", h);
                if (h == null) continue;

                // ESI
                assertNotNull(violations, prefix + ".esiTotal", h.getEsiTotal());
                assertNotNull(violations, prefix + ".esiInterior", h.getEsiInterior());
                assertNotNull(violations, prefix + ".esiSurface", h.getEsiSurface());

                // Radiation
                assertNotNull(violations, prefix + ".uvHazardLevel", h.getUvHazardLevel());
                assertNotNull(violations, prefix + ".cosmicRayFluxEarth", h.getCosmicRayFluxEarth());
                assertNotNull(violations, prefix + ".radiationBeltSurfaceDose", h.getRadiationBeltSurfaceDose());
                assertNotNull(violations, prefix + ".flareExposureRisk", h.getFlareExposureRisk());

                // Atmosphere
                assertNotNull(violations, prefix + ".isBreathable", h.getIsBreathable());
                assertNotNull(violations, prefix + ".oxygenSource", h.getOxygenSource());
                assertNotNull(violations, prefix + ".toxicGasHazard", h.getToxicGasHazard());
                assertNotNull(violations, prefix + ".atmosphericRetentionScore", h.getAtmosphericRetentionScore());

                // Water
                assertNotNull(violations, prefix + ".surfaceLiquidWaterPossible", h.getSurfaceLiquidWaterPossible());
                assertNotNull(violations, prefix + ".waterPhaseAtSurface", h.getWaterPhaseAtSurface());

                // Geological
                assertNotNull(violations, prefix + ".hasGeochemicalCycle", h.getHasGeochemicalCycle());
                assertNotNull(violations, prefix + ".nutrientCyclingPotential", h.getNutrientCyclingPotential());
                assertNotNull(violations, prefix + ".magneticProtectionAdequate", h.getMagneticProtectionAdequate());

                // Biosignatures
                assertNotNull(violations, prefix + ".biosignaturePotential", h.getBiosignaturePotential());
                assertNotNull(violations, prefix + ".lifeComplexityPotential", h.getLifeComplexityPotential());
                assertNotNull(violations, prefix + ".photosynthesisViable", h.getPhotosynthesisViable());

                // Classification (most critical — must always be set)
                assertNotNull(violations, prefix + ".habitabilityClass", h.getHabitabilityClass());
                assertNotNull(violations, prefix + ".habitabilityScore", h.getHabitabilityScore());
                assertNotNull(violations, prefix + ".terraformingPotential", h.getTerraformingPotential());
                assertNotNull(violations, prefix + ".colonizationSuitability", h.getColonizationSuitability());
            }
        }

        assertTrue(rockyCount > 0,
                "Should have found at least one rocky planet across " + SYSTEM_COUNT + " systems");
        assertTrue(violations.isEmpty(),
                "Habitability field contract violations (" + violations.size() + "):\n" + String.join("\n", violations));
    }

    //@Test
    void testGasGiantHabitabilityShortPath() {
        List<String> violations = new ArrayList<>();
        int gasGiantCount = 0;

        for (int i = 0; i < SYSTEM_COUNT; i++) {
            StarSystem system = creationService.createStarSystem();

            for (Planet planet : system.getPlanets()) {
                if (!isGaseousBody(planet)) continue;

                gasGiantCount++;
                String prefix = "system[" + i + "]." + planetLabel(planet);
                PlanetaryHabitability h = planet.getHabitability();

                assertNotNull(violations, prefix + ".habitability", h);
                if (h == null) continue;

                // Gas giants should take the short path
                assertNotNull(violations, prefix + ".habitabilityClass", h.getHabitabilityClass());
                assertNotNull(violations, prefix + ".colonizationSuitability", h.getColonizationSuitability());
                assertNotNull(violations, prefix + ".habitabilityScore", h.getHabitabilityScore());

                // ESI should be 0 for gas giants
                if (h.getEsiTotal() != null && h.getEsiTotal() != 0.0) {
                    violations.add(prefix + ".esiTotal expected 0.0 for gas giant, got " + h.getEsiTotal());
                }
            }
        }

        assertTrue(gasGiantCount > 0,
                "Should have found at least one gas giant across " + SYSTEM_COUNT + " systems");
        assertTrue(violations.isEmpty(),
                "Gas giant habitability violations (" + violations.size() + "):\n" + String.join("\n", violations));
    }

    //@Test
    void testMoonClimateAndHabitabilityCompleteness() {
        List<String> violations = new ArrayList<>();
        int moonsWithAtmo = 0;
        int totalMoons = 0;

        for (int i = 0; i < SYSTEM_COUNT; i++) {
            StarSystem system = creationService.createStarSystem();

            for (Planet planet : system.getPlanets()) {
                for (Moon moon : planet.getMoons()) {
                    totalMoons++;
                    String prefix = "system[" + i + "]." + planetLabel(planet) + "." + moonLabel(moon);

                    // Only moons with mass >= 0.0005 Earth masses get habitability
                    // (matches MoonCreator threshold at line 154)
                    double moonMass = moon.getPhysicalProperties() != null
                            && moon.getPhysicalProperties().getEarthMass() != null
                            ? moon.getPhysicalProperties().getEarthMass() : 0;

                    if (moonMass >= 0.0005) {
                        PlanetaryHabitability h = moon.getHabitability();
                        assertNotNull(violations, prefix + ".habitability", h);
                        if (h != null) {
                            assertNotNull(violations, prefix + ".habitabilityClass", h.getHabitabilityClass());
                            assertNotNull(violations, prefix + ".habitabilityScore", h.getHabitabilityScore());
                            assertNotNull(violations, prefix + ".colonizationSuitability", h.getColonizationSuitability());
                            assertNotNull(violations, prefix + ".esiTotal", h.getEsiTotal());
                        }
                    }

                    // Moons with atmosphere should have climate
                    if (Boolean.TRUE.equals(moon.getHasAtmosphere())) {
                        moonsWithAtmo++;
                        PlanetaryClimate c = moon.getClimate();
                        assertNotNull(violations, prefix + ".climate", c);
                        if (c != null) {
                            assertNotNull(violations, prefix + ".weatherSummary", c.getWeatherSummary());
                            assertNotNull(violations, prefix + ".weatherSeverity", c.getWeatherSeverity());
                            assertNotNull(violations, prefix + ".meanSurfaceWindSpeedMs", c.getMeanSurfaceWindSpeedMs());
                        }
                    }
                }
            }
        }

        assertTrue(totalMoons > 0,
                "Should have found at least one moon across " + SYSTEM_COUNT + " systems");

        // It's okay if no moons have atmosphere — just log it
        if (moonsWithAtmo == 0) {
            System.out.println("Note: No moons with atmosphere found across " + SYSTEM_COUNT
                    + " systems (" + totalMoons + " total moons)");
        }

        assertTrue(violations.isEmpty(),
                "Moon baseline violations (" + violations.size() + "):\n" + String.join("\n", violations));
    }

    // ══════════════════════════════════════════════════════════════════
    //  Helpers
    // ══════════════════════════════════════════════════════════════════

    /**
     * Matches HabitabilityCreator.isGaseousBody() — these planet types
     * take the short-path habitability assessment and have gas giant climate.
     */
    private boolean isGaseousBody(Planet planet) {
        String type = planet.getPlanetType();
        if (type == null) return false;
        String lower = type.toLowerCase();
        return lower.contains("gas giant") || lower.contains("ice giant")
                || lower.contains("hot jupiter") || lower.contains("super-jupiter")
                || lower.contains("mini-neptune") || lower.contains("sub-neptune")
                || lower.contains("warm neptune") || lower.contains("hot neptune")
                || lower.contains("puffy");
    }

    private String planetLabel(Planet planet) {
        String name = planet.getDesignation() != null ? planet.getDesignation().getLoggedName() : "unnamed";
        String type = planet.getPlanetType() != null ? planet.getPlanetType() : "unknown";
        return "planet(" + name + "/" + type + ")";
    }

    private String moonLabel(Moon moon) {
        String name = moon.getDesignation() != null ? moon.getDesignation().getLoggedName() : "unnamed";
        return "moon(" + name + ")";
    }

    private void assertNotNull(List<String> violations, String path, Object value) {
        if (value == null) {
            violations.add(path + " is null");
        }
    }
}
