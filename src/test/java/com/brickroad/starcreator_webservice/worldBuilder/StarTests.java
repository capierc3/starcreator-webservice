package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.model.stars.Star;
import com.brickroad.starcreator_webservice.Creators.StarCreator;
import com.brickroad.starcreator_webservice.model.stars.StarTypeRef;
import com.brickroad.starcreator_webservice.repos.StarTypeRefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StarTests {

    @Mock
    private StarTypeRefRepository starTypeRefRepository;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @InjectMocks
    private StarCreator starCreator;
    private static final double ZERO = 0.0;

    @BeforeEach
    void setUp() {
        StarTypeRef mType = new StarTypeRef();
        mType.setId(1);
        mType.setName("Main Sequence M");
        mType.setSpectralClass("M");
        mType.setMinMass(0.08);
        mType.setMaxMass(0.45);
        mType.setMassRadiusExponent(0.8);
        mType.setRarityWeight(7645);

        StarTypeRef gType = new StarTypeRef();
        gType.setId(2);
        gType.setName("Main Sequence G");
        gType.setSpectralClass("G");
        gType.setMinMass(0.8);
        gType.setMaxMass(1.04);
        gType.setMassRadiusExponent(0.57);
        gType.setRarityWeight(760);

        List<StarTypeRef> mockStarTypes = Arrays.asList(mType, gType);

        // Mock repository response
        when(starTypeRefRepository.findAllStarTypes()).thenReturn(mockStarTypes);

        // Initialize the generator (simulates @PostConstruct)
        starCreator.init();
    }

    @Test
    void starCreationTest() {
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class)))
                .thenReturn("Test");

        Star star = starCreator.generateStar();
        assertNotNull(star, "Star should have been created");
        assertEquals("Test - Test", star.getName(), "Star should have correct name");
        assertNotEquals(ZERO, star.getMass(), "Star mass should not be null");
        assertNotEquals(ZERO, star.getRadius(), "Star radius should not be null");
        assertNotEquals(ZERO, star.getCircumference(), "Star circumference should not be 0.0");
        assertNotNull(star.getType(), "Star type should not be null");
        assertNotEquals(ZERO, star.getSolarMass(), "Star solar mass should not be 0.0");
        assertNotEquals(ZERO, star.getSolarRadius(), "Star solar radius should not be 0.0");
        assertNotNull(star.getSpectralType(), "Star spectral type should not be null");
        assertNotEquals(ZERO, star.getSolarLuminosity(), "Star solar luminosity should not be 0.0");
        assertNotEquals(ZERO, star.getSurfaceTemp(), "Star surface temperature should not be 0.0");


    }


}
