package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.WorldBuilder.Planet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PlanetTests {

    private static Stream<Arguments> testPrams() {
        return Stream.of(
                Arguments.of("Terrestrial Planet", "Test Name")
        );
    }

    @ParameterizedTest
    @MethodSource("testPrams")
    void planetCreationTest(String type, String name) {
        Planet planet = new Planet(type,name);
        assertNotNull(planet, "Planet should have been created");
        assertEquals(planet.getName(), name, "Name should match");
    }


}
