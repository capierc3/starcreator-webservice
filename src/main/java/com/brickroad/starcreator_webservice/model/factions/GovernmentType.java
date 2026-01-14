package com.brickroad.starcreator_webservice.model.factions;

import jakarta.persistence.*;

@Entity
@Table(name = "government_type", schema = "ref")
public class GovernmentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String faction_type;
    private String description;

    public GovernmentType() {}

    public GovernmentType(String name, String faction_type, String description) {
        this.name = name;
        this.faction_type = faction_type;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFaction_type() {
        return faction_type;
    }

    public void setFaction_type(String faction_type) {
        this.faction_type = faction_type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
