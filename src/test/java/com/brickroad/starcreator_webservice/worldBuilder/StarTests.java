package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.entity.ud.Star;
import com.brickroad.starcreator_webservice.creator.StarCreator;
import com.brickroad.starcreator_webservice.entity.ref.StarTypeRef;
import com.brickroad.starcreator_webservice.entity.ud.StarSystem;
import com.brickroad.starcreator_webservice.repository.StarTypeRefRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class StarTests extends AbstractCreatorTest {

    @Autowired
    private SystemCreator systemCreator;

    private static final int SYSTEM_COUNT = 100;

    //@Test
    void getStars() throws JsonProcessingException {
        List<Star> stars = new ArrayList<>();
        for (int i = 0; i < SYSTEM_COUNT; i++) {
            StarSystem system = systemCreator.generateSystem();
            stars.addAll(system.getStars());
        }
        Map<String, Object> testResults = new HashMap<>();
        testResults.put("stars", stars);
        String json = listToJsonString(testResults);
        saveJson(json, "stars");
    }


}
