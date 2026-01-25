package com.brickroad.starcreator_webservice.entity.ref;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "terrain_category_ref", schema = "ref")
public class TerrainCategoryRef {

    // Getters and Setters
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

}