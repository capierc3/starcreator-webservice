package com.brickroad.starcreator_webservice.entity.ref;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "planet_type_ref", schema = "ref")
public class PlanetTypeRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "min_mass_earth", nullable = false)
    private Double minMassEarth;

    @Column(name = "max_mass_earth", nullable = false)
    private Double maxMassEarth;

    @Column(name = "min_radius_earth", nullable = false)
    private Double minRadiusEarth;

    @Column(name = "max_radius_earth", nullable = false)
    private Double maxRadiusEarth;

    @Column(name = "typical_density_g_cm3")
    private Double typicalDensity;

    @Column(name = "min_formation_distance_au")
    private Double minFormationDistanceAU;

    @Column(name = "max_formation_distance_au")
    private Double maxFormationDistanceAU;

    @Column(name = "can_have_atmosphere")
    private Boolean canHaveAtmosphere;

    @Column(name = "typical_atmosphere")
    private String typicalAtmosphere;

    @Column(name = "can_have_rings")
    private Boolean canHaveRings;

    @Column(name = "ring_probability")
    private Double ringProbability;

    @Column(name = "min_moons")
    private Integer minMoons;

    @Column(name = "max_moons")
    private Integer maxMoons;

    @Column(name = "habitable")
    private Boolean habitable;

    @Column(name = "typical_albedo")
    private Double typicalAlbedo;

    @Column(name = "typical_core_type")
    private String typicalCoreType;

    @Column(name = "rarity_weight", nullable = false)
    private Integer rarityWeight;

    @Column(name = "formation_zone")
    private String formationZone;

    @Column(name = "min_formation_temp_k")
    private Double minFormationTempK;

    @Column(name = "max_formation_temp_k")
    private Double maxFormationTempK;

    @Column(name = "min_moon_system_mass_ratio")
    private Double minMoonSystemMassRatio;

    @Column(name = "max_moon_system_mass_ratio")
    private Double maxMoonSystemMassRatio;

    public PlanetTypeRef() {}

}
