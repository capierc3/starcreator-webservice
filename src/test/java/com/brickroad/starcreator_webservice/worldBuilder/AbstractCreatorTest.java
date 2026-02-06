package com.brickroad.starcreator_webservice.worldBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public abstract class AbstractCreatorTest {

    protected void saveJson(String jsonString, String fileName) {

        File targetFolder = new File("target/creation-jsons/");

        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        File jsonFile = new File(targetFolder, fileName+".json");

        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write(jsonString); // Write the JSON content to the file
            System.out.println("JSON file saved successfully to: " + jsonFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing JSON to file: " + e.getMessage());
        }
    }

    protected String listToJsonString(Map<String, Object> jsonMap) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.writeValueAsString(jsonMap);
    }

    protected void printJSON(String json) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        System.out.println("\n========== JSON OUTPUT ==========");
        System.out.println(json);
        System.out.println("=================================\n");
    }

    protected void printETA(double averageTimePerSystem, int systemsAmount, int i) {
        int remainingSystems = systemsAmount - i;
        double estimatedRemainingMs = averageTimePerSystem * remainingSystems;

        int minutes = (int) (estimatedRemainingMs / 60000);
        int seconds = (int) ((estimatedRemainingMs % 60000) / 1000);

        System.out.printf("%d created, Estimated time remaining: %d min %d sec%n",
                i, minutes, seconds);
    }

    @Setter
    @Getter
    protected static class ProbabilityCounts {
        private int systemCount = 0;
        private int starCount = 0;
        private int planetCount = 0;
        private int moonCount = 0;
        private int ringCount = 0;
        private int tempCount = 0;

        public void incrementSystemCount() {systemCount++;}
        public void incrementSystemCount(int count) {systemCount += count;}
        public void incrementStarCount() {
            starCount++;
        }
        public void incrementStarCount(int count) {starCount += count;}
        public void incrementPlanetCount() {
            planetCount++;
        }
        public void incrementPlanetCount(int count) {planetCount += count;}
        public void incrementMoonCount() {
            moonCount++;
        }
        public void incrementMoonCount(int count) {moonCount += count;}
        public void incrementRingCount() {
            ringCount++;
        }
        public void incrementRingCount(int count) {ringCount += count;}
        public void incrementTempCount() {
            tempCount++;
        }
        public void incrementTempCount(int count) {tempCount += count;}

    }

    @Setter
    @Getter
    protected static class PerformanceTimer {

        private StopWatch stopWatch;
        private int lapCounter;
        private long lap;
        private long lapDuration;
        private long totalTime;
        private String finalTime;

        public PerformanceTimer() {
            stopWatch = StopWatch.create();
            totalTime = 0;
            lapCounter = 0;
        }

        public void start() {
            stopWatch.start();
        }

        public void stop() {
            finalTime = stopWatch.formatTime();
            stopWatch.stop();
        }

        public void lap() {
            lapDuration = stopWatch.getTime() - lap;
            totalTime += lapDuration;
            lapCounter++;
            lap = stopWatch.getTime();
        }

        public double averageLap() {
            return (double) totalTime / lapCounter;
        }
    }

}
