package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.entity.ud.CelestialBody;
import com.brickroad.starcreator_webservice.entity.ud.Moon;
import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.StarSystem;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class SystemTests extends AbstractCreatorTest {

    @Autowired
    private SystemCreator systemCreator;

    //@Test
    public void findSystem() throws JsonProcessingException {

        int moonCount = 0;
        int lowCount = 0;
        int moderateCount = 0;
        int highCount = 0;
        int noneCount = 0;
        int volcanoesMatch = 0;
        int volcanoesNoMatch = 0;
        int icyVolcanicAtmoCount = 0;
        int rockyVolcanicAtmoCount = 0;
        int mixedVolcanicAtmoCount = 0;
        int icyHighVolcanicCount = 0;
        int icyModerateVolcanicCount = 0;
        int icyTitanCount = 0;
        int icyAmmoniaCount = 0;

        for (int i = 0; i < 10000; i++) {
            if (i % 100 == 0) System.out.println("Systems created " + (i + 1));

            StarSystem system = systemCreator.generateSystem();
            assertNotNull(system, "Failed to generate matching system");

            //Map<String, Object> testResults = new HashMap<>();
            //testResults.put("system", system);
            //String json = listToJsonString(testResults);
            //saveJson(json, "system");
            for (CelestialBody body : system.getPlanets()) {
                moonCount += ((Planet) body).getMoons().size();
                for (Moon moon : ((Planet) body).getMoons()) {
                    switch (moon.getGeologicalActivity()) {
                        case "LOW":
                            lowCount++;
                            break;
                        case "MODERATE":
                            moderateCount++;
                            break;
                        case "HIGH":
                            highCount++;
                            break;
                        case "NONE":
                            noneCount++;
                            break;
                    }
                    if (moon.getHasAtmosphere() && moon.getAtmosphere().getClassification().equalsIgnoreCase("VOLCANIC")) {
                        switch (moon.getCompositionType()) {
                            case "ICE":
                                icyVolcanicAtmoCount++;
                                if (moon.getHasCryovolcanism() != null && moon.getHasCryovolcanism()) {
                                    volcanoesMatch++;
                                } else {
                                    volcanoesNoMatch++;
                                }
                                break;
                            case "ROCKY":
                                rockyVolcanicAtmoCount++;
                                break;
                            case "MIXED":
                                mixedVolcanicAtmoCount++;
                                break;
                        }
                    }
                    if ("ICY".equalsIgnoreCase(moon.getCompositionType()) && "HIGH".equalsIgnoreCase(moon.getGeologicalActivity())) {
                        if (moon.getHasCryovolcanism() != null && moon.getHasCryovolcanism()) {
                            icyHighVolcanicCount++;
                        }
                    } else if ("ICY".equalsIgnoreCase(moon.getCompositionType()) && "MODERATE".equalsIgnoreCase(moon.getGeologicalActivity())) {
                        if (moon.getHasCryovolcanism() != null && moon.getHasCryovolcanism()) {
                            icyModerateVolcanicCount++;
                        }
                    }
                    if ("ICY".equalsIgnoreCase(moon.getCompositionType()) && moon.getHasAtmosphere()) {
                        if ("TITAN_LIKE".equalsIgnoreCase(moon.getAtmosphere().getClassification())) {
                            icyTitanCount++;
                        } else if ("AMMONIA".equalsIgnoreCase(moon.getAtmosphere().getClassification())) {
                            icyAmmoniaCount++;
                        }
                    }
                }
            }
        }
        System.out.println("-------------------------------------------------------");
        System.out.println("Moon Count: " + moonCount);
        System.out.println("-------------------------------------------------------");
        System.out.println("Low: " + lowCount);
        System.out.println("Moderate: " + moderateCount);
        System.out.println("High: " + highCount);
        System.out.println("None: " + noneCount);
        double lowPercentage = (noneCount * 100.0) / moonCount;
        System.out.println("active percentage: " + (100 - lowPercentage) + "%");
        System.out.println("-------------------------------------------------------");
        System.out.println("Rocky Volcanic Atmo Count: " + rockyVolcanicAtmoCount);
        System.out.println("Mixed Volcanic Atmo Count: " + mixedVolcanicAtmoCount);
        System.out.println("-------------------------------------------------------");
        System.out.println("Icy High Volcanic Count: " + icyHighVolcanicCount);
        System.out.println("Icy Moderate Volcanic Count: " + icyModerateVolcanicCount);
        System.out.println("Icy Volcanic Atmo Count: " + icyVolcanicAtmoCount);
        System.out.println("Icy Volcanic Atmosphere with volcanoes: " + volcanoesMatch);
        System.out.println("Icy Volcanic Atmosphere with no volcanoes: " + volcanoesNoMatch);
        System.out.println("-------------------------------------------------------");
        System.out.println("Icy Titan Count: " + icyTitanCount);
        System.out.println("Icy Ammonia Count: " + icyAmmoniaCount);
        System.out.println("-------------------------------------------------------");
    }

}