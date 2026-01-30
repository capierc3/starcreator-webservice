package com.brickroad.starcreator_webservice.entity.ud;

import com.brickroad.starcreator_webservice.entity.ref.TerrainTypeRef;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "planet_terrain_distribution", schema = "ud")
public class PlanetaryTerrainDistribution {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planet_id", nullable = false)
    @JsonBackReference
    private Planet planet;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "terrain_type_id", nullable = false)
    @JsonIgnore
    private TerrainTypeRef terrainType;

    @Column(name = "coverage_percent", nullable = false)
    private Double coveragePercent;

    @Column(name = "description", columnDefinition = "TEXT")
    @JsonIgnore
    private String description;

    @JsonProperty("description")
    public String getTerrainDescription() {
        if (terrainType == null) {
            return description;
        }
        return terrainType.getDisplayName() + ": " + terrainType.getDescription();
    }

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public PlanetaryTerrainDistribution() {
        this.createdAt = LocalDateTime.now();
    }

    public PlanetaryTerrainDistribution(TerrainTypeRef terrainType, Double coveragePercent) {
        this.terrainType = terrainType;
        this.coveragePercent = coveragePercent;
        this.createdAt = LocalDateTime.now();
    }

}