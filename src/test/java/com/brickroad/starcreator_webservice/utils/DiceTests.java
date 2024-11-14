package com.brickroad.starcreator_webservice.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DiceTests {

    @Test
    void roll100timesRangeTest() {
        for (int i = 0; i < 100; i++) {
            int roll = RandomUtils.rollRange(4, 9);
            assertTrue(roll > 3);
            assertTrue(roll < 10);
        }
    }
}
