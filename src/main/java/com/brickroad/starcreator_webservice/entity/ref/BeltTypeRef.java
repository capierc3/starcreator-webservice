package com.brickroad.starcreator_webservice.entity.ref;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "belt_type", schema = "ref")
public class BeltTypeRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "typical_composition_type")
    private String typicalCompositionType;

    @Column(name = "min_eccentricity")
    private Double minEccentricity;

    @Column(name = "max_eccentricity")
    private Double maxEccentricity;

    @Column(name = "min_inclination_degrees")
    private Double minInclinationDegrees;

    @Column(name = "max_inclination_degrees")
    private Double maxInclinationDegrees;

    @Column(name = "typical_mass_earth_masses_min")
    private Double typicalMassEarthMassesMin;

    @Column(name = "typical_mass_earth_masses_max")
    private Double typicalMassEarthMassesMax;

    @Column(name = "notable_object_chance")
    private Double notableObjectChance;

    @Column(name = "max_notable_objects")
    private Integer maxNotableObjects;

    @Column(name = "requires_giant_planet")
    private Boolean requiresGiantPlanet;

    @Column(name = "min_system_age_my")
    private Double minSystemAgeMy;

    @Column(name = "location_description")
    private String locationDescription;

    @Column(name = "formation_description")
    private String formationDescription;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public BeltTypeRef() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
