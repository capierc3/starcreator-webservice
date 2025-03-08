package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.model.Star;
import com.brickroad.starcreator_webservice.model.enums.HabitableZone;
import com.brickroad.starcreator_webservice.model.enums.StarType;
import com.brickroad.starcreator_webservice.service.StarCreator;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class StarTests {

    private static final String NAME = "Omicron Persei";
    private static final String MAIN_SEQ_G_DESCRIPTION = "Sun-like";

    @RepeatedTest(value = 100)
    void testStarCreationName() {
        Star star = StarCreator.createStar(null, NAME, false);
        assertNotNull(star, "Star should have been created");
        assertEquals(NAME, star.getName(), "Name should match");
        assertNotNull(star.getStarType(), "Star type should have been created");
        assertNotNull(star.getDescription(), "Description should have been created");
        assertStarData(star);
    }

    @RepeatedTest(value = 100)
    void testStarCreationTypeAndName() {
        Star star = StarCreator.createStar(StarType.MAIN_SEQ_G, NAME, false);
        assertNotNull(star, "Star should have been created");
        assertEquals(NAME, star.getName(), "Name should match");
        assertEquals(StarType.MAIN_SEQ_G, star.getStarType(), "Star type should be MAIN_SEQ_G");
        assertEquals(MAIN_SEQ_G_DESCRIPTION, star.getDescription(), "Description should be Sun-like");
        assertEquals(HabitableZone.GOOD, star.getLifeSupporting(), "Life support should be GOOD");
        assertEquals(HabitableZone.GOOD.getMinHabitableZone(), star.getMinHabitableZone(), "MinHabitableZone should be .95");
        assertEquals(HabitableZone.GOOD.getMaxHabitableZone(), star.getMaxHabitableZone(), "MaxHabitableZone should be 1.4");
        assertStarData(star);
    }

    @RepeatedTest(value = 100)
    void testStarCreationNameAndHabitable() {
        Star star = StarCreator.createStar(null, NAME, true);
        assertNotNull(star, "Star should have been created");
        assertEquals(NAME, star.getName(), "Name should match");
        assertNotNull(star.getStarType(), "Star type should have been created");
        assertNotNull(star.getDescription(), "Description should have been created");
        assertTrue(Arrays.stream(Arrays.copyOfRange(StarType.values(), 4, 9))
                .anyMatch(starType -> starType.equals(star.getStarType())));
        assertStarData(star);
    }

    @RepeatedTest(value = 100)
    void testStarCreationType() {
        Star star = StarCreator.createStar(StarType.MAIN_SEQ_G, null, false);
        assertNotNull(star, "Star should have been created");
        assertNotNull(star.getName(), "Name should have been created");
        assertEquals(StarType.MAIN_SEQ_G, star.getStarType(), "Star type should be MAIN_SEQ_G");
        assertEquals(MAIN_SEQ_G_DESCRIPTION, star.getDescription(), "Description should be Sun-like");
        assertStarData(star);
    }

    @RepeatedTest(value = 100)
    void testStarCreationFullRandom() {
        Star star = StarCreator.createStar(null, null, false);
        assertNotNull(star, "Star should have been created");
        assertNotNull(star.getName(), "Name should have been created");
        assertNotNull(star.getStarType(), "Star type should have been created");
        assertNotNull(star.getDescription(), "Description should have been created");
        assertStarData(star);
    }

    @Test
    public void testStarCreationTypeAndHabitable() {
        Star star = StarCreator.createStar(StarType.WHITE_DWARF, null, true);
        assertNotNull(star, "Star should have been created");
        assertEquals(StarType.WHITE_DWARF, star.getStarType(), "Star type should be from input not random");
    }

    @Test
    public void testStarTypeEnums() {
        assertEquals(StarType.MAIN_SEQ_A, StarType.getStarTypeBelow(StarType.MAIN_SEQ_F),  "Star type should be MAIN_SEQ_A");
        assertEquals(StarType.MAIN_SEQ_G, StarType.getStarTypeAbove(StarType.MAIN_SEQ_F),  "Star type should be MAIN_SEQ_G");
        assertEquals(StarType.SUPER_GIANT, StarType.getStarTypeBelow(StarType.PROTO),  "Star type should be PROTO");
        assertEquals(StarType.PROTO, StarType.getStarTypeAbove(StarType.SUPER_GIANT),  "Star type should be SUPER_GIANT");
    }

    private void assertStarData(Star star) {
        assertTrue(star.getSolarMass() >= star.getStarType().getMinMass(), "Mass should be greater than the star type min");
        assertTrue(star.getSolarMass() <= star.getStarType().getMaxMass(), "Mass should be less than the star type max");
        assertTrue(star.getSolarRadius() >= star.getStarType().getMinRadius(), "Radius should be greater than the star type min");
        assertTrue(star.getSolarRadius() <= star.getStarType().getMaxRadius(), "Radius should be less than the star type max");
        assertTrue((star.getTemperature() / 1000)  >= star.getStarType().getMinTemperature(), "Temperature should be greater than the star type min");
        assertTrue((star.getTemperature() / 1000) <= star.getStarType().getMaxTemperature(), "Temperature should be less than the star type max");
    }

}
