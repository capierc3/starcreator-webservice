package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ud.Sector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SectorCreator {

    @Autowired
    private SystemCreator systemCreator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Sector generateSector() {
        Sector sector = new Sector();

        return sector;
    }

}
