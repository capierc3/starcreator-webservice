package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.entity.ud.CelestialBody;
import com.brickroad.starcreator_webservice.entity.ud.Moon;
import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.StarSystem;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class MoonTests extends AbstractCreatorTest {

    @Autowired
    private SystemCreator systemCreator;

    private static final int SYSTEM_COUNT = 1000;

    //@Test
    public void findPlanetByType() throws JsonProcessingException {
        int moonSystemSize = 40;
        boolean foundLargeEnoughMoonSystem = false;
        while (!foundLargeEnoughMoonSystem) {
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

            if (biggestMoonSystem != null && !biggestMoonSystem.getMoons().isEmpty() && biggestMoonSystem.getMoons().size() > moonSystemSize) {
                System.out.println("Moons found: " + biggestMoonSystem.getMoons().size());
                Map<String, Object> testResults = new HashMap<>();
                testResults.put("star", system.getStars());
                testResults.put("planet", biggestMoonSystem);
                String json = listToJsonString(testResults);
                //printJSON(json);
                saveJson(json, "moons_system");
                foundLargeEnoughMoonSystem = true;
            }
        }
    }

    //@Test
    public void getMoonData() throws JsonProcessingException {

        int moonCount = 0;
        int lowCount = 0;
        int moderateCount = 0;
        int highCount = 0;
        int noneCount = 0;
//        int volcanoesMatch = 0;
//        int volcanoesNoMatch = 0;
//        int icyVolcanicAtmoCount = 0;
//        int rockyVolcanicAtmoCount = 0;
//        int mixedVolcanicAtmoCount = 0;
//        int icyHighVolcanicCount = 0;
//        int icyModerateVolcanicCount = 0;
//        int icyTitanCount = 0;
//        int icyAmmoniaCount = 0;
        Map<String, Integer> moonTypes = new HashMap<>();
        Map<String, List<Moon>> moonTypeToMoonMap = new HashMap<>();
        Map<String, Integer> compositionTypes = new HashMap<>();

        for (int i = 0; i < SYSTEM_COUNT; i++) {
            if (i % 1000 == 0) System.out.println("Systems created " + (i + 1));

            StarSystem system = systemCreator.generateSystem();
            assertNotNull(system, "Failed to generate matching system");

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
                    moonTypes.put(moon.getMoonType(), moonTypes.getOrDefault(moon.getMoonType(), 0) + 1);
                    if (!moonTypeToMoonMap.containsKey(moon.getMoonType())) {
                        moonTypeToMoonMap.put(moon.getMoonType(), new ArrayList<>());
                        moonTypeToMoonMap.get(moon.getMoonType()).add(moon);
                    } else {
                        moonTypeToMoonMap.get(moon.getMoonType()).add(moon);
                    }
                    compositionTypes.put(moon.getCompositionClassification(), compositionTypes.getOrDefault(moon.getCompositionClassification(), 0) + 1);
                }
            }
        }
        System.out.println("-------------------------------------------------------");
        System.out.println("Moon Count: " + moonCount);
        System.out.println("-------------------------------------------------------");
        System.out.println("Active Moons:");
        System.out.println("Low: " + lowCount);
        System.out.println("Moderate: " + moderateCount);
        System.out.println("High: " + highCount);
        System.out.println("None: " + noneCount);
        double lowPercentage = (noneCount * 100.0) / moonCount;
        System.out.println("active percentage: " + (100 - lowPercentage) + "%");
        System.out.println("-------------------------------------------------------");
        System.out.println("Moon Types:");
        List<Moon> gasEnvelopes = new ArrayList<>();
        for (Map.Entry<String, List<Moon>> entry : moonTypeToMoonMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue().size());
            Map<String, Integer> compositionTypeCount = new HashMap<>();
            for (Moon moon : entry.getValue()) {
                compositionTypeCount.put(moon.getCompositionClassification(), compositionTypeCount.getOrDefault(moon.getCompositionClassification(), 0) + 1);
                if (moon.getCompositionClassification().equalsIgnoreCase("GAS_ENVELOPE")) {
                    gasEnvelopes.add(moon);
                }
            }
            for (Map.Entry<String, Integer> compositionEntry : compositionTypeCount.entrySet()) {
                System.out.println("\t" + compositionEntry.getKey() + ": " + compositionEntry.getValue());
            }
        }
        System.out.println("-------------------------------------------------------");
        if (!gasEnvelopes.isEmpty()) {
            double tempHigh = 0;
            double tempLow = 10000;
            for (Moon moon : gasEnvelopes) {
                if (moon.getSurfaceTemp() > tempHigh) {
                    tempHigh = moon.getSurfaceTemp();
                }
                if (moon.getSurfaceTemp() < tempLow) {
                    tempLow = moon.getSurfaceTemp();
                }
            }
            System.out.println("Highest Gas Envelope Moon Temp: " + tempHigh);
            System.out.println("Lowest Gas Envelope Moon Temp: " + tempLow);
            System.out.println("Random Gas Envelope Moon");
            Map<String, Object> testResults = new HashMap<>();
            testResults.put("moon", gasEnvelopes.get(RandomUtils.rollRange(0, gasEnvelopes.size() - 1)));
            printJSON(listToJsonString(testResults));
        }


    }
}