package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.model.Star;
import com.brickroad.starcreator_webservice.model.StarSystem;
import com.brickroad.starcreator_webservice.request.SystemRequest;
import com.brickroad.starcreator_webservice.service.SystemCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SystemTests {

    @Test
    void systemFullRandomCreationTest() {
        StarSystem system = SystemCreator.createStarSystem(new SystemRequest());
        assertNotNull(system, "System should have been created");
        assertNotNull(system.getName(), "System name should have been created");
        assertNotNull(system.getPopulation(), "System population should have been created");
        assertNotNull(system.getStars(), "System stars should have been created");
        assertTrue(system.getStars().length > 0, "System should have at least one star");
    }

}
