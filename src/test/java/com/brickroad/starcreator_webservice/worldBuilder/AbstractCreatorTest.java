package com.brickroad.starcreator_webservice.worldBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public abstract class AbstractCreatorTest {

    protected void saveJson(String jsonString, String fileName) {

        File targetFolder = new File("target/systemJSONs/");

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

}
