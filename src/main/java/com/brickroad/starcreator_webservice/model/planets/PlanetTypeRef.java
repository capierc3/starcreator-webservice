package com.brickroad.starcreator_webservice.model.planets;

import jakarta.persistence.*;

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
    private String formationZone; // "inner", "habitable", "outer", "frost_line"

    public PlanetTypeRef() {}

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getMinMassEarth() {
        return minMassEarth;
    }

    public void setMinMassEarth(Double minMassEarth) {
        this.minMassEarth = minMassEarth;
    }

    public Double getMaxMassEarth() {
        return maxMassEarth;
    }

    public void setMaxMassEarth(Double maxMassEarth) {
        this.maxMassEarth = maxMassEarth;
    }

    public Double getMinRadiusEarth() {
        return minRadiusEarth;
    }

    public void setMinRadiusEarth(Double minRadiusEarth) {
        this.minRadiusEarth = minRadiusEarth;
    }

    public Double getMaxRadiusEarth() {
        return maxRadiusEarth;
    }

    public void setMaxRadiusEarth(Double maxRadiusEarth) {
        this.maxRadiusEarth = maxRadiusEarth;
    }

    public Double getTypicalDensity() {
        return typicalDensity;
    }

    public void setTypicalDensity(Double typicalDensity) {
        this.typicalDensity = typicalDensity;
    }

    public Double getMinFormationDistanceAU() {
        return minFormationDistanceAU;
    }

    public void setMinFormationDistanceAU(Double minFormationDistanceAU) {
        this.minFormationDistanceAU = minFormationDistanceAU;
    }

    public Double getMaxFormationDistanceAU() {
        return maxFormationDistanceAU;
    }

    public void setMaxFormationDistanceAU(Double maxFormationDistanceAU) {
        this.maxFormationDistanceAU = maxFormationDistanceAU;
    }

    public Boolean getCanHaveAtmosphere() {
        return canHaveAtmosphere;
    }

    public void setCanHaveAtmosphere(Boolean canHaveAtmosphere) {
        this.canHaveAtmosphere = canHaveAtmosphere;
    }

    public String getTypicalAtmosphere() {
        return typicalAtmosphere;
    }

    public void setTypicalAtmosphere(String typicalAtmosphere) {
        this.typicalAtmosphere = typicalAtmosphere;
    }

    public Boolean getCanHaveRings() {
        return canHaveRings;
    }

    public void setCanHaveRings(Boolean canHaveRings) {
        this.canHaveRings = canHaveRings;
    }

    public Double getRingProbability() {
        return ringProbability;
    }

    public void setRingProbability(Double ringProbability) {
        this.ringProbability = ringProbability;
    }

    public Integer getMinMoons() {
        return minMoons;
    }

    public void setMinMoons(Integer minMoons) {
        this.minMoons = minMoons;
    }

    public Integer getMaxMoons() {
        return maxMoons;
    }

    public void setMaxMoons(Integer maxMoons) {
        this.maxMoons = maxMoons;
    }

    public Boolean getHabitable() {
        return habitable;
    }

    public void setHabitable(Boolean habitable) {
        this.habitable = habitable;
    }

    public Double getTypicalAlbedo() {
        return typicalAlbedo;
    }

    public void setTypicalAlbedo(Double typicalAlbedo) {
        this.typicalAlbedo = typicalAlbedo;
    }

    public String getTypicalCoreType() {
        return typicalCoreType;
    }

    public void setTypicalCoreType(String typicalCoreType) {
        this.typicalCoreType = typicalCoreType;
    }

    public Integer getRarityWeight() {
        return rarityWeight;
    }

    public void setRarityWeight(Integer rarityWeight) {
        this.rarityWeight = rarityWeight;
    }

    public String getFormationZone() {
        return formationZone;
    }

    public void setFormationZone(String formationZone) {
        this.formationZone = formationZone;
    }
}
