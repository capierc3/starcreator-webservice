package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ud.Sector;
import com.brickroad.starcreator_webservice.entity.ud.Survey;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import org.springframework.stereotype.Service;

@Service
public class SectorCreator {

    public Sector generateSector(Survey survey) {
        String sectorCode = Integer.toString(RandomUtils.rollRange(0, 1295), Character.MAX_RADIX).toUpperCase();
        return generateSector(survey, sectorCode);
    }

    public Sector generateSector(Survey survey, String sectorCode) {
        Sector sector = new Sector();
        sector.setSurvey(survey);
        sector.setSectorCode(sectorCode);
        sector.setX(RandomUtils.rollRange(-100, 100));
        sector.setY(RandomUtils.rollRange(-100, 100));
        sector.setZ(RandomUtils.rollRange(-100, 100));
        char quad = (char) ('A' + ((sector.getX() < 0 ? 1 : 0)
                | (sector.getY() < 0 ? 2 : 0)
                | (sector.getZ() < 0 ? 4 : 0)));
        sector.setName(survey.getCode() + "-" + quad + sectorCode);
        return sector;
    }
}
