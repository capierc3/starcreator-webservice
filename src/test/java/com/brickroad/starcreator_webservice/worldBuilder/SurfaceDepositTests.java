package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.StarSystem;
import com.brickroad.starcreator_webservice.model.climate.SurfaceDeposit;
import com.brickroad.starcreator_webservice.worldBuilder.TestEnums.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
public class SurfaceDepositTests extends AbstractCreatorTest {

    @Autowired
    private SystemCreator systemCreator;

    @Test
    public void findTitanLike() throws JsonProcessingException {
        StarSystem system = SystemFinder.using(systemCreator)
                .starCount(1)
                .atmosphereClassification(Atmosphere.TITAN_LIKE)
                .find();

        assertNotNull(system);
        printDepositsForMatchingPlanets(system, "TITAN_LIKE atmo",
                p -> "TITAN_LIKE".equalsIgnoreCase(p.getAtmosphereClassification()));
        saveJson(listToJsonString(Map.of("system", system)), "surface_deposits_titan");
    }

    @Test
    public void findIceWorld() throws JsonProcessingException {
        StarSystem system = SystemFinder.using(systemCreator)
                .starCount(1)
                .planetType(PlanetType.ICE_WORLD)
                .find();

        assertNotNull(system);
        printDepositsForMatchingPlanets(system, "Ice World",
                p -> "Ice World".equalsIgnoreCase(p.getPlanetType()));
        saveJson(listToJsonString(Map.of("system", system)), "surface_deposits_ice");
    }

    @Test
    public void findBareIceWorld() throws JsonProcessingException {
        StarSystem system = SystemFinder.using(systemCreator)
                .starCount(1)
                .planetType(PlanetType.ICE_WORLD)
                .atmosphereClassification(Atmosphere.NONE)
                .compositionClassification(Composition.ICE_RICH)
                .find();

        assertNotNull(system);
        printDepositsForMatchingPlanets(system, "Bare Ice World (no atmo, ICE_RICH)",
                p -> "Ice World".equalsIgnoreCase(p.getPlanetType())
                        && "NONE".equalsIgnoreCase(p.getAtmosphereClassification()));
        saveJson(listToJsonString(Map.of("system", system)), "surface_deposits_bare_ice");
    }

    @Test
    public void findEarthLikeAroundMDwarf() throws JsonProcessingException {
        StarSystem system = SystemFinder.using(systemCreator)
                .starCount(1)
                .starType(StarType.MAIN_SEQUENCE_M)
                .atmosphereClassification(Atmosphere.EARTH_LIKE)
                .lifeComplexity(LifeComplexity.COMPLEX_MULTICELLULAR)
                .planetPredicate(p -> !"Ocean Planet".equals(p.getPlanetType()))
                .find();

        assertNotNull(system);
        printDepositsForMatchingPlanets(system, "Earth-like around M-dwarf with complex life",
                p -> "EARTH_LIKE".equalsIgnoreCase(p.getAtmosphereClassification()));
        saveJson(listToJsonString(Map.of("system", system)), "surface_deposits_earthlike_mdwarf");
    }

    @Test
    public void findEarthLikeAroundGStar() throws JsonProcessingException {
        StarSystem system = SystemFinder.using(systemCreator)
                .starCount(1)
                .starType(StarType.MAIN_SEQUENCE_G)
                .atmosphereClassification(Atmosphere.EARTH_LIKE)
                .lifeComplexity(LifeComplexity.COMPLEX_MULTICELLULAR)
                .find();

        assertNotNull(system);
        printDepositsForMatchingPlanets(system, "Earth-like around G-star with complex life",
                p -> "EARTH_LIKE".equalsIgnoreCase(p.getAtmosphereClassification()));
        saveJson(listToJsonString(Map.of("system", system)), "surface_deposits_earthlike_gstar");
    }

    @Test
    public void findAmmoniaWorldWithLife() throws JsonProcessingException {
        StarSystem system = SystemFinder.using(systemCreator)
                .starCount(1)
                .atmosphereClassification(Atmosphere.AMMONIA)
                .planetPredicate(p -> p.getHabitability() != null
                        && p.getHabitability().getLifeComplexityPotential() != null
                        && !"NONE".equals(p.getHabitability().getLifeComplexityPotential()))
                .find();

        assertNotNull(system);
        printDepositsForMatchingPlanets(system, "Ammonia world with life",
                p -> "AMMONIA".equalsIgnoreCase(p.getAtmosphereClassification()));
        saveJson(listToJsonString(Map.of("system", system)), "surface_deposits_ammonia_life");
    }

    @Test
    public void findMarsLike() throws JsonProcessingException {
        StarSystem system = SystemFinder.using(systemCreator)
                .starCount(1)
                .atmosphereClassification(Atmosphere.MARS_LIKE)
                .find();

        assertNotNull(system);
        printDepositsForMatchingPlanets(system, "Mars-like",
                p -> "MARS_LIKE".equalsIgnoreCase(p.getAtmosphereClassification()));
        saveJson(listToJsonString(Map.of("system", system)), "surface_deposits_mars");
    }

    private void printDepositsForMatchingPlanets(StarSystem system, String label,
                                                  java.util.function.Predicate<Planet> filter) {
        System.out.println("\n========== SURFACE DEPOSITS: " + label + " ==========");
        for (Planet p : system.getPlanets()) {
            if (!filter.test(p)) continue;

            System.out.println("\nPlanet: " + p.getName() + " | Type: " + p.getPlanetType()
                    + " | Atmo: " + p.getAtmosphereClassification());
            System.out.println("  Star type: " + system.getStars().iterator().next().getType());
            System.out.println("  Surface colors: " + p.getSurfaceColorPrimary() + " / " + p.getSurfaceColorSecondary());

            if (p.getTerrain() != null) {
                System.out.println("  Geology: activity=" + p.getTerrain().getGeologicalActivity()
                        + " cryovolcanism=" + p.getTerrain().getHasCryovolcanism()
                        + " volcanism=" + p.getTerrain().getHasVolcanicActivity()
                        + " type=" + p.getTerrain().getVolcanismType());
            }
            if (p.getHydrology() != null) {
                System.out.println("  Hydrology: volatile=" + p.getHydrology().getVolatileType()
                        + " liquid%=" + p.getHydrology().getLiquidSurfaceCoveragePercent()
                        + " inventory=" + p.getHydrology().getLiquidInventory());
            }
            if (p.getHabitability() != null) {
                System.out.println("  Life: " + p.getHabitability().getLifeComplexityPotential()
                        + " habClass=" + p.getHabitability().getHabitabilityClass());
            }

            if (p.getClimate() != null) {
                System.out.println("  Wind: intensity=" + p.getClimate().getWindIntensity()
                        + " meanMs=" + p.getClimate().getMeanSurfaceWindSpeedMs()
                        + " dustStorms=" + p.getClimate().getHasDustStorms()
                        + " globalDust=" + p.getClimate().getDustStormsCanBeGlobal()
                        + " stormFreq=" + p.getClimate().getStormFrequency());
            }
            if (p.getClimate() != null && p.getClimate().getPrecipitationTypes() != null) {
                System.out.println("  --- Precipitation ---");
                for (var precip : p.getClimate().getPrecipitationTypes()) {
                    System.out.printf("    %s phase=%s freq=%s intensity=%s reaches=%s%n",
                            precip.getSubstance(), precip.getPhase(),
                            precip.getFrequency(), precip.getIntensity(),
                            precip.getReachesSurface());
                }
            }
            if (p.getClimate() != null && p.getClimate().getSurfaceDeposits() != null
                    && !p.getClimate().getSurfaceDeposits().isEmpty()) {
                System.out.println("  --- Deposits ---");
                for (SurfaceDeposit d : p.getClimate().getSurfaceDeposits()) {
                    System.out.printf("    #%d %-25s src=%-15s color=%-8s coverage=%5.1f%% thickness=%-10s %s%n",
                            d.getDominanceRank(), d.getDepositType(), d.getSource(),
                            d.getColorHint(), d.getCoveragePercent(), d.getThickness(),
                            d.getDescription());
                }
            } else {
                System.out.println("  --- No deposits ---");
            }
        }
        System.out.println("==================================================\n");
    }
}
