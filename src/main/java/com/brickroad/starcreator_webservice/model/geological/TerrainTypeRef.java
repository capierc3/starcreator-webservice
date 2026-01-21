package com.brickroad.starcreator_webservice.model.geological;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "terrain_type_ref", schema = "ref")
public class TerrainTypeRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String category;

    // Environmental requirements
    @Column(name = "requires_water")
    private Boolean requiresWater = false;

    @Column(name = "requires_atmosphere")
    private Boolean requiresAtmosphere = false;

    @Column(name = "min_temperature_k")
    private Double minTemperatureK;

    @Column(name = "max_temperature_k")
    private Double maxTemperatureK;

    @Column(name = "min_pressure_atm")
    private Double minPressureAtm;

    // Characteristics
    @Column(name = "is_volcanic")
    private Boolean isVolcanic = false;

    @Column(name = "is_frozen")
    private Boolean isFrozen = false;

    @Column(name = "is_aquatic")
    private Boolean isAquatic = false;

    @Column(name = "is_artificial")
    private Boolean isArtificial = false;

    // Generation parameters
    @Column(name = "rarity_weight")
    private Integer rarityWeight = 100;

    @Column(name = "typical_coverage_min")
    private Double typicalCoverageMin = 0.0;

    @Column(name = "typical_coverage_max")
    private Double typicalCoverageMax = 100.0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "cratering_weight_boost")
    private Integer crateringWeightBoost = 0;

    @Column(name = "volcanic_weight_boost")
    private Integer volcanicWeightBoost = 0;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Boolean getRequiresWater() { return requiresWater; }
    public void setRequiresWater(Boolean requiresWater) { this.requiresWater = requiresWater; }

    public Boolean getRequiresAtmosphere() { return requiresAtmosphere; }
    public void setRequiresAtmosphere(Boolean requiresAtmosphere) { this.requiresAtmosphere = requiresAtmosphere; }

    public Double getMinTemperatureK() { return minTemperatureK; }
    public void setMinTemperatureK(Double minTemperatureK) { this.minTemperatureK = minTemperatureK; }

    public Double getMaxTemperatureK() { return maxTemperatureK; }
    public void setMaxTemperatureK(Double maxTemperatureK) { this.maxTemperatureK = maxTemperatureK; }

    public Double getMinPressureAtm() { return minPressureAtm; }
    public void setMinPressureAtm(Double minPressureAtm) { this.minPressureAtm = minPressureAtm; }

    public Boolean getIsVolcanic() { return isVolcanic; }
    public void setIsVolcanic(Boolean isVolcanic) { this.isVolcanic = isVolcanic; }

    public Boolean getIsFrozen() { return isFrozen; }
    public void setIsFrozen(Boolean isFrozen) { this.isFrozen = isFrozen; }

    public Boolean getIsAquatic() { return isAquatic; }
    public void setIsAquatic(Boolean isAquatic) { this.isAquatic = isAquatic; }

    public Boolean getIsArtificial() { return isArtificial; }
    public void setIsArtificial(Boolean isArtificial) { this.isArtificial = isArtificial; }

    public Integer getRarityWeight() { return rarityWeight; }
    public void setRarityWeight(Integer rarityWeight) { this.rarityWeight = rarityWeight; }

    public Double getTypicalCoverageMin() { return typicalCoverageMin; }
    public void setTypicalCoverageMin(Double typicalCoverageMin) { this.typicalCoverageMin = typicalCoverageMin; }

    public Double getTypicalCoverageMax() { return typicalCoverageMax; }
    public void setTypicalCoverageMax(Double typicalCoverageMax) { this.typicalCoverageMax = typicalCoverageMax; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getCrateringWeightBoost() { return crateringWeightBoost != null ? crateringWeightBoost : 0; }
    public void setCrateringWeightBoost(Integer crateringWeightBoost) { this.crateringWeightBoost = crateringWeightBoost; }

    public Integer getVolcanicWeightBoost() { return volcanicWeightBoost != null ? volcanicWeightBoost : 0; }
    public void setVolcanicWeightBoost(Integer volcanicWeightBoost) { this.volcanicWeightBoost = volcanicWeightBoost; }

    public int getEffectiveWeight(boolean hasHeavyCratering, boolean hasVolcanism) {
        int weight = rarityWeight != null ? rarityWeight : 100;
        if (hasHeavyCratering) {
            weight += getCrateringWeightBoost();
        }
        if (hasVolcanism) {
            weight += getVolcanicWeightBoost();
        }
        return weight;
    }

    public boolean isViableFor(Double surfaceTempK, Double pressureAtm, Boolean hasWater, Boolean hasAtmosphere) {
        if (requiresWater && (hasWater == null || !hasWater)) {
            return false;
        }
        if (requiresAtmosphere && (hasAtmosphere == null || !hasAtmosphere)) {
            return false;
        }
        if (minTemperatureK != null && surfaceTempK != null && surfaceTempK < minTemperatureK) {
            return false;
        }
        if (maxTemperatureK != null && surfaceTempK != null && surfaceTempK > maxTemperatureK) {
            return false;
        }
        if (minPressureAtm != null && pressureAtm != null && pressureAtm < minPressureAtm) {
            return false;
        }
        return true;
    }
}