package com.brickroad.starcreator_webservice.openapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class OpenApiYamlGeneratorTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    //@Test
    public void generateOpenApiYaml() throws IOException {
        String url = "http://localhost:" + port + "/api-docs.yaml";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(200, response.getStatusCode().value(), "Failed to fetch OpenAPI YAML");
        assertNotNull(response.getBody(), "OpenAPI YAML body is null");

        File targetDir = new File("target");
        assertTrue(targetDir.exists(), "target directory does not exist");

        File yamlFile = new File(targetDir, "galaxy-creator-api.yaml");
        try (FileWriter writer = new FileWriter(yamlFile)) {
            writer.write(response.getBody());
        }

        System.out.println("OpenAPI YAML saved to: " + yamlFile.getAbsolutePath());
        assertTrue(yamlFile.exists(), "YAML file was not created");
        assertTrue(yamlFile.length() > 0, "YAML file is empty");
    }
}
