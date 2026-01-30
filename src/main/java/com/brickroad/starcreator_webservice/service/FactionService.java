package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.repository.GovernmentTypeRepo;
import com.brickroad.starcreator_webservice.entity.ud.Faction;
import com.brickroad.starcreator_webservice.repository.FactionRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FactionService {

    private final FactionRepo factionRepo;
    private final GovernmentTypeRepo governmentTypeRepo;

    public FactionService(FactionRepo factionRepo, GovernmentTypeRepo governmentTypeRepo) {
     this.factionRepo = factionRepo;
     this.governmentTypeRepo = governmentTypeRepo;
    }

    public void createFaction(String name, String type, String description, String alignment) {
        Faction faction = new Faction(name, type, description, alignment);
        factionRepo.save(faction);
    }

    public Faction getFactionByName(String name) {
        Faction faction = factionRepo.findByName(name);
        if(faction == null) {
            faction = new Faction("Faction Not Found", "", "", "");
        }
        return faction;
    }

    public List<Faction> getFactionByTypeAndAlignment(String type, String alignment, int count) {

        if (count <= 0) {
            count = 100;
        }

        List<Faction> factions;
        if (StringUtils.isEmpty(type)) {
            factions = factionRepo.findAllByAlignment(alignment, count);
        } else if (StringUtils.isEmpty(alignment)) {
            factions = factionRepo.findAllByType(type, count);
        } else {
            factions = factionRepo.findByTypeAndAlignment(type, alignment, count);
        }
        return factions;
    }

    public Faction getRandomFaction() {
        return factionRepo.getRandomFaction();
    }

}
