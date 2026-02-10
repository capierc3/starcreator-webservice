package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.entity.ud.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class SystemTests extends AbstractCreatorTest {

    @Autowired
    private SystemCreator systemCreator;

    @Test
    public void findSystem() throws JsonProcessingException {

        int starCount = 1;
        String starType = "Main Sequence M";
        boolean foundStar = false;

        String targetType = "ANY";
        String tempTarget = "habitable";
        boolean foundPlanet = false;

        int maxAttempts = 1000;
        StarSystem system = null;

        System.out.println("Searching for planet type: " + targetType);
        System.out.println("Max attempts: " + maxAttempts);
        System.out.println("---");

        for (int i = 0; i < maxAttempts; i++) {

            system = systemCreator.generateSystem();
            for (CelestialBody planet : system.getPlanets()) {
                if (targetType.equalsIgnoreCase("ANY")) {
                    foundPlanet = true;
                } else if (targetType.equalsIgnoreCase(((Planet) planet).getPlanetType())
                && tempTarget.equalsIgnoreCase(((Planet) planet).getHabitableZonePosition())) {
                    foundPlanet = true;
                }
            }
            foundStar = system.getStars().size() == starCount &&
                    system.getStars().stream()
                            .anyMatch(star -> star.getType().equalsIgnoreCase(starType));

            if (foundPlanet && foundStar) {
                break;
            }
            if (i % 100 == 0 && i > 0) {
                System.out.println("Searched " + (i + 1) + " systems...");
            }
        }

        assertNotNull(system, "Failed to generate matching system after " + maxAttempts + " attempts");

        Map<String, Object> testResults = new HashMap<>();
        testResults.put("system", system);
        String json = listToJsonString(testResults);
        saveJson(json, "system");
    }
}