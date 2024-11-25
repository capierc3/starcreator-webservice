package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.model.Planet;
import com.brickroad.starcreator_webservice.model.enums.AtmosphereType;
import com.brickroad.starcreator_webservice.model.enums.PlanetType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.brickroad.starcreator_webservice.model.constants.PlanetConstants.*;
import static org.junit.jupiter.api.Assertions.*;

public class PlanetTests {

    private static Stream<Arguments> testPrams() {
        return Stream.of(
                Arguments.of(PlanetType.GAS.getName(), "Gas Test"),
                Arguments.of(PlanetType.TERRESTRIAL.getName(), "Terrestrial Test"),
                Arguments.of(PlanetType.DWARF.getName(), "Dwarf Test")
        );
    }

    @ParameterizedTest
    @MethodSource("testPrams")
    void planetCreationTest(String type, String name) {
        Planet planet = new Planet(type,name);
        assertNotNull(planet, "Planet should have been created");
        assertEquals(planet.getName(), name, "Name should match");
        assertEquals(planet.getPlanetType(), PlanetType.getEnum(type));
        assertPlanetRandomValues(planet);
    }

    @Test
    void randomPlanetTest() {
        int runs = 1000;
        double gasAvgDR = 0;
        double gasAvgSTE = 0;
        double gasAvgGrav = 0;
        int gasAMT = 0;
        double terraAvgDR = 0;
        double terraAvgSTE = 0;
        double terraAvgGrav = 0;
        int terraAMT = 0;
        double dwarfAvgDR = 0;
        double dwarfAvgSTE = 0;
        double dwarfAvgGrav = 0;
        int dwarfAMT = 0;
        for (int i = 0; i < runs; i++) {
            Planet planet = new Planet();
            assertNotNull(planet.getType(), "Planet type should not be null");
            assertPlanetRandomValues(planet);

            if (planet.getPlanetType().equals(PlanetType.GAS)) {
                gasAMT++;
                gasAvgDR += planet.getDensityRating();
                gasAvgSTE += planet.getMagneticField().getComparedToEarthStrength();
                gasAvgGrav += planet.getGravity();
            } else if (planet.getPlanetType().equals(PlanetType.DWARF)) {
                dwarfAMT++;
                dwarfAvgDR += planet.getDensityRating();
                dwarfAvgSTE += planet.getMagneticField().getComparedToEarthStrength();
                dwarfAvgGrav += planet.getGravity();
            } else {
                terraAMT++;
                terraAvgDR += planet.getDensityRating();
                terraAvgSTE += planet.getMagneticField().getComparedToEarthStrength();
                terraAvgGrav += planet.getGravity();
            }

        }
        System.out.println("\tGas: " + gasAMT);
        System.out.println("\t\tAvg Dr: " + gasAvgDR/gasAMT);
        System.out.println("\t\tAvg STE: " + gasAvgSTE/gasAMT);
        System.out.println("\t\tAvg Grav: " + gasAvgGrav/gasAMT);
        System.out.println("\tTerrestrial: " + terraAMT);
        System.out.println("\t\tAvg Dr: " + terraAvgDR/terraAMT);
        System.out.println("\t\tAvg STE: " + terraAvgSTE/terraAMT);
        System.out.println("\t\tAvg Grav: " + terraAvgGrav/terraAMT);
        System.out.println("\tDwarf: " + dwarfAMT);
        System.out.println("\t\tAvg Dr: " + dwarfAvgDR/dwarfAMT);
        System.out.println("\t\tAvg STE: " + dwarfAvgSTE/dwarfAMT);
        System.out.println("\t\tAvg Grav: " + dwarfAvgGrav/dwarfAMT);
    }

    @Test
    void planetTypeTest() {
        Arrays.stream(PlanetType.values()).sequential().forEach( planetType -> {
            PlanetType planetTypeNew = PlanetType.getEnum(planetType.getName());
            assertEquals(planetType, planetTypeNew, "Enum should match the found enum");
        });
    }

    @Test
    void randomTest() {
//        for (int i = 0; i < 11; i++) {
//
//        }
    }

    private void assertPlanetRandomValues(Planet planet) {
        assertPlanetSize(planet);
        assertPlanetGravity(planet);
        assertAtmosphere(planet);
        assertTiltAndRotation(planet);
        if (!planet.getPlanetType().equals(PlanetType.GAS)) {
            assertLiquidAmtAndType(planet);
            assertTerrain(planet);
        }
        assertMagnetField(planet);
    }

    private void assertPlanetSize(Planet planet) {
        if (planet.getPlanetType().equals(PlanetType.GAS)) {
            assertTrue(planet.getCircumference() > 3999, "Gas Planet to small");
            assertTrue(planet.getCircumference() < 500001, "Dwarf Planet to big");
        } else if (planet.getPlanetType().equals(PlanetType.DWARF)) {
            assertTrue(planet.getCircumference() > 1499, "Dwarf Planet to small");
            assertTrue(planet.getCircumference() < 20001, "Dwarf Planet to big");
        } else if (planet.getPlanetType().equals(PlanetType.TERRESTRIAL)) {
            assertTrue(planet.getCircumference() > 1499, "Terrestrial Planet to small");
            assertTrue(planet.getCircumference() < 400001, "Terrestrial Planet to small");
        } else {
            fail();
        }
        assertEquals(planet.getRadius(), (planet.getCircumference() / (2 * Math.PI))
                ,"Radius equation matches");
    }

    private void assertPlanetGravity(Planet planet) {
        assertNotNull(planet.getDensity(), "Density value should be set");
        assertNotNull(DENSITY_RATINGS.get(planet.getDensity()), "Density range should be found");
        assertTrue(planet.getGravity() >= DENSITY_RATINGS.get(planet.getDensity())[0]
                ,"Gravity should be equal to or more then the min value");
        if (!planet.getDensity().equalsIgnoreCase("Extreme")) {
            assertTrue(planet.getGravity() <= DENSITY_RATINGS.get(planet.getDensity())[1]
                    ,"Gravity should be equal to or less than the max value");
        }
    }

    private void assertAtmosphere(Planet planet) {
        AtomicInteger percent = new AtomicInteger();
        planet.getAtmosphere().getAtmosphericComposite().forEach(ac -> percent.set(percent.get() + ac.getPercent()));
        assertEquals(100, percent.intValue(), "Total atmosphere should be 100%");
        assertNotNull(planet.getAtmosphere().getAtmosphereThickness(), "Atmospheric description should be populated");
        if (planet.getAtmosphere().getAtmosphereThickness().equalsIgnoreCase("Ultra Dense")) {
            assertTrue(planet.getAtmosphere().getAtmosphericPressure() >= 430);
        } else {
            assertTrue(planet.getAtmosphere().getAtmosphericPressure() >= ATMOSPHERIC_PRESSURE.get(planet.getAtmosphere().getAtmosphereThickness())[0]);
            assertTrue(planet.getAtmosphere().getAtmosphericPressure() <= ATMOSPHERIC_PRESSURE.get(planet.getAtmosphere().getAtmosphereThickness())[1]);
        }
    }

    private void assertTiltAndRotation(Planet planet) {
        assertNotNull(planet.getAxisTilt(), "Axis tilt should not be null");
        assertTrue(planet.getTiltDegree() >= TILTS.get(planet.getAxisTilt())[0]);
        assertTrue(planet.getTiltDegree() <= TILTS.get(planet.getAxisTilt())[1]);
        assertTrue(planet.getRotation() >= 0);
        assertNotNull(planet.getRotationDir());
    }

    private void assertLiquidAmtAndType(Planet planet) {
        if (planet.getAtmosphere().compositeContainsType(AtmosphereType.EARTH_LIKE)) {
            assertTrue(planet.getLiquidType().equalsIgnoreCase("H2O"));
        }
        assertNotNull(planet.getLiquidType());
        assertTrue(planet.getLiquidAmt() >= 0);
        assertTrue(planet.getLiquidAmt() <= 100);
    }

    private void assertMagnetField(Planet planet) {
        assertNotNull(planet.getMagneticField().getVariation());
    }

    private void assertTerrain(Planet planet) {
        AtomicInteger percent = new AtomicInteger();
        planet.getTerrain().values().forEach(atmoPercent -> {
            percent.set(percent.get() + atmoPercent);
        });
        assertEquals(100, percent.intValue(), "Total terrain should be 100%");
    }

}
