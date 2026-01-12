package com.brickroad.starcreator_webservice.database;

import com.brickroad.starcreator_webservice.model.factions.Faction;
import com.brickroad.starcreator_webservice.service.FactionService;
import com.brickroad.starcreator_webservice.model.factions.FactionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DatabaseTests {

    @Autowired
    private FactionService factionService;

    //@Test
    void testCreateAndFindUser() {
        factionService.createFaction("United Stellar Coalition","Galactic Empires & Governments", "", FactionUtils.getRandomAlignment());

        Faction found = factionService.getFactionByName("United Stellar Coalition");
        assertNotNull(found);
        assertEquals("United Stellar Coalition", found.getName());
    }

    //@Test
    void testRandomFaction() {
        Faction faction = factionService.getRandomFaction();
        assertNotNull(faction);

        System.out.println("Faction Type: " + faction.getType());
        System.out.println("Faction ID: " + faction.getId());
        System.out.println("Faction Name: " + faction.getName());
        System.out.println("Faction Description: " + faction.getDescription());
        System.out.println("Faction Alignment: " + faction.getAlignment());
    }

}
