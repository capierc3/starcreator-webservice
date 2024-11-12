package com.brickroad.starcreator_webservice.utils;

import com.brickroad.starcreator_webservice.model.Planet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class DiceTests {

    @Test
    void roll100timesRangeTest() {
        for (int i = 0; i < 100; i++) {
            int roll = Dice.rollRange(4, 9);
            assertTrue(roll > 3);
            assertTrue(roll < 10);
        }
    }
}
