package com.brickroad.starcreator_webservice.junit.Enums;

import com.brickroad.starcreator_webservice.model.Star;
import com.brickroad.starcreator_webservice.model.enums.SystemType;
import com.brickroad.starcreator_webservice.service.StarCreator;
import org.junit.jupiter.api.RepeatedTest;

import static com.brickroad.starcreator_webservice.model.enums.StarType.MAIN_SEQ_M;
import static com.brickroad.starcreator_webservice.model.enums.SystemType.getRandomSystemType;
import static org.junit.jupiter.api.Assertions.*;

public class SystemTypeTests {

    @RepeatedTest(1000)
    public void testGetRandomSystemType(){
        Star star = StarCreator.createStar(MAIN_SEQ_M,null,false);
        assertNotEquals(SystemType.TRINARY, getRandomSystemType(star), "Low mass stars should return TRINARY");
    }

}
