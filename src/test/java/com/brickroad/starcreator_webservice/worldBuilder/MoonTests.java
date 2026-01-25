package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.entity.ud.CelestialBody;
import com.brickroad.starcreator_webservice.entity.ud.Moon;
import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.StarSystem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class MoonTests {

    @Autowired
    private SystemCreator systemCreator;

    @Test
    public void findPlanetByType() throws JsonProcessingException {

        StarSystem system = systemCreator.generateSystem();

        assertNotNull(system, "Failed to generate matching system");

        System.out.println("System Created... Getting Moons");
        System.out.println("-------------------------------");
        Planet biggestMoonSystem = null;
        for (CelestialBody planet : system.getPlanets()) {
            if (biggestMoonSystem == null || biggestMoonSystem.getMoons().size() < ((Planet) planet).getMoons().size()) {
                biggestMoonSystem = (Planet) planet;
            }
        }

        if (biggestMoonSystem != null && !biggestMoonSystem.getMoons().isEmpty()) {
            System.out.println("Star metallicity: " + biggestMoonSystem.getParentStar().getMetallicity());
            System.out.println(biggestMoonSystem.getName() +
                    " : " + biggestMoonSystem.getPlanetType() +
                    " with mass " + biggestMoonSystem.getEarthMass() +
                    "\nMoons: " + biggestMoonSystem.getMoons().size());
            printJSON(biggestMoonSystem.getMoons());
        }
    }

    private void printJSON(List<Moon> moons) throws JsonProcessingException {

        Map<String, Object> output = new HashMap<>();
        output.put("moons", moons);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String json = mapper.writeValueAsString(output);
        System.out.println(json);
        System.out.println("=================================\n");
    }

}