package com.brickroad.starcreator_webservice.Creators;

import com.brickroad.starcreator_webservice.model.sectors.Sector;
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
