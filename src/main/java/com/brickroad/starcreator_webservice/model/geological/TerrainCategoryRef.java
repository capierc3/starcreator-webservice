package com.brickroad.starcreator_webservice.model.geological;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "terrain_category_ref", schema = "ref")
public class TerrainCategoryRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String category;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_weight", nullable = false)
    private Integer baseWeight = 100;

    @Column(name = "typical_min_coverage")
    private Double typicalMinCoverage = 0.0;

    @Column(name = "typical_max_coverage")
    private Double typicalMaxCoverage = 100.0;

    @Column(name = "is_major_terrain")
    private Boolean isMajorTerrain = true;

    @Column(name = "is_rare")
    private Boolean isRare = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getBaseWeight() { return baseWeight; }
    public void setBaseWeight(Integer baseWeight) { this.baseWeight = baseWeight; }

    public Double getTypicalMinCoverage() { return typicalMinCoverage; }
    public void setTypicalMinCoverage(Double typicalMinCoverage) { this.typicalMinCoverage = typicalMinCoverage; }

    public Double getTypicalMaxCoverage() { return typicalMaxCoverage; }
    public void setTypicalMaxCoverage(Double typicalMaxCoverage) { this.typicalMaxCoverage = typicalMaxCoverage; }

    public Boolean getIsMajorTerrain() { return isMajorTerrain; }
    public void setIsMajorTerrain(Boolean isMajorTerrain) { this.isMajorTerrain = isMajorTerrain; }

    public Boolean getIsRare() { return isRare; }
    public void setIsRare(Boolean isRare) { this.isRare = isRare; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}