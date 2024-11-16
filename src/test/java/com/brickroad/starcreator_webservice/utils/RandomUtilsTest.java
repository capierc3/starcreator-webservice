package com.brickroad.starcreator_webservice.utils;

import org.junit.jupiter.api.Test;

import static com.brickroad.starcreator_webservice.model.planets.MagneticField.MAGNETIC_FIELD;
import static org.junit.jupiter.api.Assertions.*;

public class RandomUtilsTest {

    @Test
    void roll100timesRangeTest() {
        for (int i = 0; i < 1000; i++) {
            int roll = RandomUtils.rollRange(4, 9);
            assertTrue(roll >= 3);
            assertTrue(roll <= 9);
        }
        int low = 0;
        int high = 0;
        for (int i = 0; i <1000 ; i++) {
            int roll = RandomUtils.rollRange(-4,4);
            if (roll < low) low = roll;
            if (roll > high) high = roll;
        }
        System.out.println(low);
        System.out.println(high);
    }

    @Test
    void diceRollTests() {
        for (int i = 0; i < 1000; i++) {
            assertTrue(RandomUtils.rollD100() > 0);
            assertTrue(RandomUtils.rollD100() < 101);

            assertTrue(RandomUtils.rollD20() > 0);
            assertTrue(RandomUtils.rollD20() < 21);

            assertTrue(RandomUtils.rollD12() > 0);
            assertTrue(RandomUtils.rollD12() < 13);

            assertTrue(RandomUtils.rollD10() > 0);
            assertTrue(RandomUtils.rollD10() < 11);

            assertTrue(RandomUtils.rollD8() > 0);
            assertTrue(RandomUtils.rollD8() < 9);

            assertTrue(RandomUtils.rollD6() > 0);
            assertTrue(RandomUtils.rollD6() < 7);

            assertTrue(RandomUtils.rollD4() > 0);
            assertTrue(RandomUtils.rollD4() < 5);

            assertTrue(RandomUtils.flipCoin() > 0);
            assertTrue(RandomUtils.flipCoin() < 3);
        }
    }

    @Test
    void stringFromArrayTest() {
        for (int i = 0; i < 1000; i++) {
            assertNotEquals("Error", RandomUtils.getStringFromArray(MAGNETIC_FIELD), "Should not return the error string");
        }
    }
}
