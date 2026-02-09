package com.brickroad.starcreator_webservice.ProbabilityReport;

import com.brickroad.starcreator_webservice.entity.ud.Asteroid;
import com.brickroad.starcreator_webservice.entity.ud.Belt;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class BeltData {

    private static final Map<String, Integer> BELT_TYPES = new HashMap<>();
    private static final Map<String, Integer> ASTEROID_TYPES = new HashMap<>();

    static void analyzeData(Belt belt, ProbabilityCounts counts) {
        BELT_TYPES.put(belt.getBeltType().getCode(), BELT_TYPES.getOrDefault(belt.getBeltType().getCode(), 0) + 1);
        counts.incrementAsteroidCount(belt.getNotableAsteroids().size());
        for (Asteroid asteroid : belt.getNotableAsteroids()) {
            ASTEROID_TYPES.put(asteroid.getAsteroidType().getCode(), ASTEROID_TYPES.getOrDefault(asteroid.getAsteroidType().getCode(), 0) + 1);
        }
        counts.incrementTempCount(belt.getDwarfPlanets().size());
    }

    static void printData(PrintWriter writer, ProbabilityCounts counts) {
        writer.println("---");
        writer.println("## Belt Types");
        BELT_TYPES.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> writer.println("* " + entry.getKey() + ": " + entry.getValue() + " (" + (entry.getValue() * 100.0) / counts.getBeltCount() + "%)"));

        writer.println("---");
        writer.println("## Asteroid Types");
        ASTEROID_TYPES.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> writer.println("* " + entry.getKey() + ": " + entry.getValue() + " (" + (entry.getValue() * 100.0) / counts.getAsteroidCount() + "%)"));
        writer.println("");
        writer.println("Dwarf Planets in Belts: " + counts.getTempCount());
        writer.println("");
        writer.println("---");
    }

}
