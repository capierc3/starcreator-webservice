package com.brickroad.starcreator_webservice.model.planets;

import com.brickroad.starcreator_webservice.model.geological.TerrainTypeRef;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "planet_terrain_distribution", schema = "ud")
public class PlanetaryTerrainDistribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planet_id", nullable = false)
    @JsonBackReference
    private Planet planet;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "terrain_type_id", nullable = false)
    private TerrainTypeRef terrainType;

    @Column(name = "coverage_percent", nullable = false)
    private Double coveragePercent;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

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

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Planet getPlanet() { return planet; }
    public void setPlanet(Planet planet) { this.planet = planet; }

    public TerrainTypeRef getTerrainType() { return terrainType; }
    public void setTerrainType(TerrainTypeRef terrainType) { this.terrainType = terrainType; }

    public Double getCoveragePercent() { return coveragePercent; }
    public void setCoveragePercent(Double coveragePercent) { this.coveragePercent = coveragePercent; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}