package com.brickroad.starcreator_webservice.entity.ref;

import jakarta.persistence.*;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Arrays;

@Setter
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

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "excluded_planet_types", columnDefinition = "text[]")
    private String[] excludedPlanetTypes;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "required_composition_classes", columnDefinition = "text[]")
    private String[] requiredCompositionClasses;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "excluded_composition_classes", columnDefinition = "text[]")
    private String[] excludedCompositionClasses;

    // Getters and Setters
    public Integer getId() { return id; }

    public String getName() { return name; }

    public String getDisplayName() { return displayName; }

    public String getDescription() { return description; }

    public String getCategory() { return category; }

    public Boolean getRequiresWater() { return requiresWater; }

    public Boolean getRequiresAtmosphere() { return requiresAtmosphere; }

    public Double getMinTemperatureK() { return minTemperatureK; }

    public Double getMaxTemperatureK() { return maxTemperatureK; }

    public Double getMinPressureAtm() { return minPressureAtm; }

    public Boolean getIsVolcanic() { return isVolcanic; }

    public Boolean getIsFrozen() { return isFrozen; }

    public Boolean getIsAquatic() { return isAquatic; }

    public Boolean getIsArtificial() { return isArtificial; }

    public Integer getRarityWeight() { return rarityWeight; }

    public Double getTypicalCoverageMin() { return typicalCoverageMin; }

    public Double getTypicalCoverageMax() { return typicalCoverageMax; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Integer getCrateringWeightBoost() { return crateringWeightBoost != null ? crateringWeightBoost : 0; }

    public Integer getVolcanicWeightBoost() { return volcanicWeightBoost != null ? volcanicWeightBoost : 0; }

    public String[] getExcludedPlanetTypes() {
        return excludedPlanetTypes;
    }

    public String[] getRequiredCompositionClasses() {
        return requiredCompositionClasses;
    }

    public String[] getExcludedCompositionClasses() {
        return excludedCompositionClasses;
    }

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
        return minPressureAtm == null || pressureAtm == null || pressureAtm >= minPressureAtm;
    }

    public boolean isExcludedForPlanetType(String planetType) {
        return excludedPlanetTypes != null &&
                Arrays.asList(excludedPlanetTypes).contains(planetType);
    }

    public boolean isCompatibleWithComposition(String compositionClass) {
        // If required classes specified, composition must match one
        if (requiredCompositionClasses != null && requiredCompositionClasses.length > 0) {
            return Arrays.asList(requiredCompositionClasses).contains(compositionClass);
        }

        // Check if composition is explicitly excluded
        if (excludedCompositionClasses != null) {
            return !Arrays.asList(excludedCompositionClasses).contains(compositionClass);
        }

        return true;
    }
}