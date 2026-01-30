package com.brickroad.starcreator_webservice.entity.ref;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "geological_template", schema = "ref")
public class GeologicalTemplateRef {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "planet_types", length = 500)
    private String planetTypes; // Comma-separated

    @Column(name = "activity_level", nullable = false, length = 50)
    private String activityLevel; // "Highly Active", "Moderately Active", etc.

    @Column(name = "min_activity_score")
    private Double minActivityScore;

    @Column(name = "max_activity_score")
    private Double maxActivityScore;

    @Column(name = "min_planet_mass_earth")
    private Double minPlanetMassEarth;

    @Column(name = "max_planet_mass_earth")
    private Double maxPlanetMassEarth;

    @Column(name = "rarity_weight")
    private Integer rarityWeight = 100;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<GeologicalFeatureRef> features = new ArrayList<>();

}
