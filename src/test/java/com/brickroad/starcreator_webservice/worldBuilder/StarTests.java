package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.model.Star;
import com.brickroad.starcreator_webservice.model.enums.StarType;
import com.brickroad.starcreator_webservice.service.StarCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StarTests {

    private static final String NAME = "Omicron Persei";
    private static final String MAIN_SEQ_G_DESCRIPTION = "Sun-like";

    @Test
    void testStarCreationName() {
        Star star = StarCreator.createStar(NAME);
        assertNotNull(star, "Star should have been created");
        assertEquals(NAME, star.getName(), "Name should match");
        assertNotNull(star.getStarType(), "Star type should have been created");
        assertNotNull(star.getDescription(), "Description should have been created");
        assertStarData(star);
    }

    @Test
    void testStarCreationTypeAndName() {
        Star star = StarCreator.createStar(StarType.MAIN_SEQ_G, NAME);
        assertNotNull(star, "Star should have been created");
        assertEquals(NAME, star.getName(), "Name should match");
        assertEquals(StarType.MAIN_SEQ_G, star.getStarType(), "Star type should be MAIN_SEQ_G");
        assertEquals(MAIN_SEQ_G_DESCRIPTION, star.getDescription(), "Description should be Sun-like");
        assertStarData(star);
    }

    @Test
    void testStarCreationType() {
        Star star = StarCreator.createStar(StarType.MAIN_SEQ_G);
        assertNotNull(star, "Star should have been created");
        assertNotNull(star.getName(), "Name should have been created");
        assertEquals(StarType.MAIN_SEQ_G, star.getStarType(), "Star type should be MAIN_SEQ_G");
        assertEquals(MAIN_SEQ_G_DESCRIPTION, star.getDescription(), "Description should be Sun-like");
        assertStarData(star);
    }

    @Test
    void testStarCreationFullRandom() {
        Star star = StarCreator.createStar();
        assertNotNull(star, "Star should have been created");
        assertNotNull(star.getName(), "Name should have been created");
        assertNotNull(star.getStarType(), "Star type should have been created");
        assertNotNull(star.getDescription(), "Description should have been created");
        assertStarData(star);
    }

    private void assertStarData(Star star) {
        assertTrue(star.getSolarMass() >= star.getStarType().getMass()[0], "Mass should be greater than the star type min");
        assertTrue(star.getSolarMass() <= star.getStarType().getMass()[1], "Mass should be less than the star type max");
        assertTrue(star.getSolarRadius() >= star.getStarType().getRadius()[0], "Radius should be greater than the star type min");
        assertTrue(star.getSolarRadius() <= star.getStarType().getRadius()[1], "Radius should be less than the star type max");
        assertTrue((star.getTemperature() / 1000)  >= star.getStarType().getTemperature()[0], "Temperature should be greater than the star type min");
        assertTrue((star.getTemperature() / 1000) <= star.getStarType().getTemperature()[1], "Temperature should be less than the star type max");
    }


}
