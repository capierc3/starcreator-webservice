package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.model.Planet;
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
        for (int i = 0; i < 1000; i++) {
            Planet planet = new Planet();
            assertNotNull(planet.getType(), "Planet type should not be null");
            assertPlanetRandomValues(planet);
        }
    }

    @Test
    void planetTypeTest() {
        Arrays.stream(PlanetType.values()).sequential().forEach( planetType -> {
            PlanetType planetTypeNew = PlanetType.getEnum(planetType.getName());
            assertEquals(planetType, planetTypeNew, "Enum should match the found enum");
        });
    }

    private void assertPlanetRandomValues(Planet planet) {
        assertPlanetSize(planet);
        assertPlanetGravity(planet);
        assertAtmosphere(planet);
        assertTiltAndRotation(planet);
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
        planet.getAtmosphere().values().forEach(atmoPercent -> {
            percent.set(percent.get() + atmoPercent);
        });
        assertEquals(100, percent.intValue(), "Total atmosphere should be 100%");
        assertNotNull(planet.getAtmosphereThickness(), "Atmospheric description should be populated");
        if (planet.getAtmosphereThickness().equalsIgnoreCase("Ultra Dense")) {
            assertTrue(planet.getAtmosphericPressure() >= 430);
        } else {
            assertTrue(planet.getAtmosphericPressure() >= ATMOSPHERIC_PRESSURE.get(planet.getAtmosphereThickness())[0]);
            assertTrue(planet.getAtmosphericPressure() <= ATMOSPHERIC_PRESSURE.get(planet.getAtmosphereThickness())[1]);
        }
    }

    private void assertTiltAndRotation(Planet planet) {
        assertNotNull(planet.getAxisTilt(), "Axis tilt should not be null");
        assertTrue(planet.getTiltDegree() >= TILTS.get(planet.getAxisTilt())[0]);
        assertTrue(planet.getTiltDegree() <= TILTS.get(planet.getAxisTilt())[1]);
        assertTrue(planet.getRotation() >= 0);
    }
}
