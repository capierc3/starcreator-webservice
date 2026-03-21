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
                .planetPredicate(p -> p.getAxialTilt() != null && p.getAxialTilt() > 95.0)
                //.planetPredicate(p -> p.getAxialTilt() != null && p.getAxialTilt() < 140.0)
                .planetPredicate(p -> !p.getTidallyLocked())
                .planetPredicate(p -> p.getLiquidSurfaceCoveragePercent() != null
                        && p.getLiquidSurfaceCoveragePercent() > 5.0)
                .atmosphereClassification(Atmosphere.EARTH_LIKE)
                .find();

        assertNotNull(system);
        saveJson(listToJsonString(Map.of("system", system)), "system");
    }
}
