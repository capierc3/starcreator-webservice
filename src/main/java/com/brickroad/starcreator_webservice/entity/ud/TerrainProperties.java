package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Terrain, geology, and surface morphology data for any celestial body with a surface.
 * <p>
 * Used by planets, moons, and asteroids. Body-type-specific fields (e.g. tectonics for planets,
 * cryovolcanism for moons, regolith for asteroids) are nullable and only populated for the
 * appropriate body type.
 */
@Entity
@Table(name = "terrain_properties", schema = "ud")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Terrain, geology, and surface morphology for any celestial body")
public class TerrainProperties {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    // ── Surface Features ──

    @Column(name = "surface_features", columnDefinition = "TEXT")
    @Schema(description = "Notable surface features")
    private String surfaceFeatures;

    // ── Geological Activity ──

    @Column(name = "geological_activity", length = 50)
    @Schema(description = "Overall geological activity level", example = "ACTIVE")
    private String geologicalActivity;

    @Column(name = "activity_score")
    @Schema(description = "Numerical geological activity score")
    private Double activityScore;

    // ── Volcanism ──

    @Column(name = "has_volcanic_activity")
    @Schema(description = "Whether the body has active volcanism")
    private Boolean hasVolcanicActivity;

    @Column(name = "volcanism_type", length = 50)
    @Schema(description = "Primary type of volcanism", example = "SILICATE")
    private String volcanismType;

    @Column(name = "estimated_active_volcanoes")
    @Schema(description = "Estimated number of active volcanoes")
    private Integer estimatedActiveVolcanoes;

    @Column(name = "volcanic_intensity", length = 50)
    @Schema(description = "Volcanic activity intensity", example = "MODERATE")
    private String volcanicIntensity;

    // ── Terrain Morphology ──

    @Column(name = "mountain_coverage_percent")
    @Schema(description = "Percentage of surface covered by mountains")
    private Double mountainCoveragePercent;

    @Column(name = "average_elevation_km")
    @Schema(description = "Average surface elevation in km")
    private Double averageElevationKm;

    @Column(name = "max_elevation_km")
    @Schema(description = "Maximum surface elevation in km")
    private Double maxElevationKm;

    @Column(name = "min_elevation_km")
    @Schema(description = "Minimum surface elevation (deepest point) in km")
    private Double minElevationKm;

    @Column(name = "terrain_roughness")
    @Schema(description = "Terrain roughness index")
    private Double terrainRoughness;

    // ── Impact History ──

    @Column(name = "cratering_level", length = 50)
    @Schema(description = "Impact cratering level", example = "MODERATE")
    private String crateringLevel;

    @Column(name = "estimated_visible_craters")
    @Schema(description = "Estimated number of major visible impact craters")
    private Integer estimatedVisibleCraters;

    // ── Erosion ──

    @Column(name = "erosion_level", length = 50)
    @Schema(description = "Surface erosion level", example = "HIGH")
    private String erosionLevel;

    @Column(name = "primary_erosion_agent", length = 50)
    @Schema(description = "Primary agent of surface erosion", example = "WATER")
    private String primaryErosionAgent;

    // ── Tectonics (planet-only) ──

    @Column(name = "has_plate_tectonics")
    @Schema(description = "Whether the body has active plate tectonics (planets only)")
    private Boolean hasPlateTectonics;

    @Column(name = "number_of_tectonic_plates")
    @Schema(description = "Number of major tectonic plates")
    private Integer numberOfTectonicPlates;

    @Column(name = "tectonic_activity_level", length = 50)
    @Schema(description = "Tectonic activity classification", example = "MODERATE")
    private String tectonicActivityLevel;

    // ── Cryovolcanism (moon-only) ──

    @Column(name = "has_cryovolcanism")
    @Schema(description = "Whether the body has cryovolcanic activity (icy moons)")
    private Boolean hasCryovolcanism;

    // ── Regolith (asteroid-only) ──

    @Column(name = "has_regolith")
    @Schema(description = "Whether the surface has a regolith layer (asteroids)")
    private Boolean hasRegolith;

    @Column(name = "regolith_depth_m")
    @Schema(description = "Regolith depth in meters")
    private Double regolithDepthM;

    // ── Terrain Distribution (re-parented from Planet) ──

    @OneToMany(mappedBy = "terrain", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference("terrain-distribution")
    @Schema(description = "Terrain type distribution across the surface")
    private List<TerrainDistribution> terrainDistribution = new ArrayList<>();

    // ── Metadata ──

    @Column(name = "label", length = 100)
    @JsonIgnore
    @Schema(hidden = true, description = "Human-readable label for debugging")
    private String label;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    @JsonIgnore
    private LocalDateTime modifiedAt;
}
