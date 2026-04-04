package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.entity.ud.StarSystem;
import com.brickroad.starcreator_webservice.worldBuilder.TestEnums.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
public class SystemTests extends AbstractCreatorTest {

    @Autowired
    private SystemCreator systemCreator;

    @Test
    public void findSystem() throws JsonProcessingException {

        StarSystem system = SystemFinder.using(systemCreator)
                .starCount(1)
                .planetType(PlanetType.OCEAN)
                .atmosphereClassification(Atmosphere.EARTH_LIKE)
                .planetPredicate(planet -> !planet.getTidallyLocked())
                .find();

        assertNotNull(system);
        saveJson(listToJsonString(Map.of("system", system)), "system");
    }
}
