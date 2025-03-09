package com.brickroad.starcreator_webservice.junit.service;

import com.brickroad.starcreator_webservice.model.StarSystem;
import com.brickroad.starcreator_webservice.model.enums.Population;
import com.brickroad.starcreator_webservice.model.enums.StarType;
import com.brickroad.starcreator_webservice.model.enums.SystemType;
import com.brickroad.starcreator_webservice.request.SystemRequest;
import com.brickroad.starcreator_webservice.service.SystemCreator;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SystemCreatorTests {

    private static final String SYSTEM_NAME = "Vega";
    private static final int STAR_COUNT = 2;

    @RepeatedTest(100)
    void systemFullRandomCreationTest() {
        StarSystem system = SystemCreator.createStarSystem(new SystemRequest());
        assertNotNull(system, "System should have been created");
        assertNotNull(system.getName(), "System name should have been created");
        assertNotNull(system.getPopulation(), "System population should have been created");
        assertNotNull(system.getStars(), "System stars should have been created");
        assertFalse(system.getStars().isEmpty(), "System should have at least one star");
    }

    @RepeatedTest(100)
    void systemAllInputsCreationTest() {
        SystemRequest request = new SystemRequest();
        request.setName(SYSTEM_NAME);
        request.setSystemType(SystemType.BINARY);
        request.setPopulation(Population.MODERATELY_POPULATED);
        StarSystem system = SystemCreator.createStarSystem(request);
        assertNotNull(system, "System should have been created");
        assertEquals(SYSTEM_NAME, system.getName(), "System name should have been created");
        assertEquals(Population.MODERATELY_POPULATED, system.getPopulation(), "System population should have been created");
        assertNotNull(system.getStars(), "System stars should have been created");
        assertEquals(STAR_COUNT, system.getStars().size(), "Two Stars should have been created");
        system.getStars().forEach(star -> assertNotNull(star, "System star should have been created"));
    }

    @Test
    void dumbSystemCreationTest() {
        assertEquals(StarType.MAIN_SEQ_G,StarType.values()[6]);
    }

}
