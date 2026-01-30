package com.brickroad.starcreator_webservice.response;

import com.brickroad.starcreator_webservice.entity.ud.Faction;
import com.brickroad.starcreator_webservice.entity.ref.GovernmentType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class FactionResponse {

    private Faction faction;
    private GovernmentType governmentType;
    private List<Faction> factions;

    public FactionResponse(Faction faction, GovernmentType governmentType, List<Faction> factions) {
        this.faction = faction;
        this.governmentType = governmentType;
        this.factions = factions;
    }

    public FactionResponse() {

    }

    public FactionResponse(Faction faction) {
        this.faction = faction;
    }

    public FactionResponse(List<Faction> factions) {
        this.factions = factions;
    }

}
