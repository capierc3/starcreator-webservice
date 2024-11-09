package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.WorldBuilder.Planet;
import com.brickroad.starcreator_webservice.WorldBuilder.Star;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StarTests {

    private static Stream<Arguments> testPrams() {
        return Stream.of(
                Arguments.of("Test Name")
        );
    }

    @ParameterizedTest
    @MethodSource("testPrams")
    void starCreationTest(String name) {
        Star star = new Star(name);
        assertNotNull(star, "Star should have been created");
        assertEquals(star.getName(), name, "Name should match");
    }


}
