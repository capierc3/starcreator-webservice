package com.brickroad.starcreator_webservice.model.factions;

import java.util.List;

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

    public Faction getFaction() {
        return faction;
    }

    public void setFaction(Faction faction) {
        this.faction = faction;
    }

    public List<Faction> getFactions() {
        return factions;
    }

    public void setFactions(List<Faction> factions) {
        this.factions = factions;
    }

    public GovernmentType getGovernmentType() {
        return governmentType;
    }

    public void setGovernmentType(GovernmentType governmentType) {
        this.governmentType = governmentType;
    }
}
