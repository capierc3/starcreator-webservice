package com.brickroad.starcreator_webservice.entity.ref;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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

}
