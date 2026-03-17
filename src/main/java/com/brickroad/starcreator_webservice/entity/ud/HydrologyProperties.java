package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Hydrology properties for any celestial body.
 * <p>
 * Covers surface and subsurface liquid of any type (water, ammonia, methane, etc.),
 * frozen liquid coverage, and water ice coverage. Body-type-specific fields
 * (e.g. subsurface ocean details for icy moons) are nullable and only populated for
 * the appropriate body type.
 */
@Entity
@Table(name = "hydrology_properties", schema = "ud")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Hydrology and liquid properties for any celestial body")
public class HydrologyProperties {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    // ── Volatile Type ──

    @Column(name = "volatile_type", length = 30)
    @Schema(description = "Dominant volatile substance on the surface", example = "WATER")
    private String volatileType;

    @Column(name = "liquid_composition", length = 100)
    @Schema(description = "Descriptive composition of the liquid", example = "95% H2O, 5% NH3")
    private String liquidComposition;

    @Column(name = "freezing_point_k")
    @Schema(description = "Freezing point of the dominant liquid in Kelvin (pressure-adjusted)", example = "273.16")
    private Double freezingPointK;

    @Column(name = "boiling_point_k")
    @Schema(description = "Boiling point of the dominant liquid in Kelvin (pressure-adjusted)", example = "373.15")
    private Double boilingPointK;

    // ── Surface Liquid ──

    @Column(name = "liquid_inventory", length = 30)
    @Schema(description = "Relative liquid inventory classification", example = "MODERATE")
    private String liquidInventory;

    @Column(name = "liquid_coverage_percent")
    @Schema(description = "Total liquid coverage as percentage of surface (liquid + frozen liquid)", example = "71.0")
    private Double liquidCoveragePercent;

    @Column(name = "liquid_surface_coverage_percent")
    @Schema(description = "Liquid-phase coverage as percentage of surface", example = "65.0")
    private Double liquidSurfaceCoveragePercent;

    @Column(name = "frozen_liquid_coverage_percent")
    @Schema(description = "Frozen form of the dominant liquid as percentage of surface", example = "6.0")
    private Double frozenLiquidCoveragePercent;

    @Column(name = "water_ice_coverage_percent")
    @Schema(description = "Water ice (H2O) coverage as percentage of surface", example = "6.0")
    private Double waterIceCoveragePercent;

    // ── Liquid Colors ──

    @Column(name = "liquid_color_primary", length = 7)
    @Schema(description = "Primary hex color of the liquid surface", example = "#3388aa")
    private String liquidColorPrimary;

    @Column(name = "liquid_color_secondary", length = 7)
    @Schema(description = "Secondary hex color of the liquid surface", example = "#4499bb")
    private String liquidColorSecondary;

    // ── Subsurface Liquid ──

    @Column(name = "has_subsurface_liquid")
    @Schema(description = "Whether subsurface liquid is present")
    private Boolean hasSubsurfaceLiquid;

    @Column(name = "subsurface_liquid_depth_km")
    @Schema(description = "Depth to subsurface liquid layer in km")
    private Double subsurfaceLiquidDepthKm;

    // ── Subsurface Ocean (applicable to any icy body) ──

    @Column(name = "has_subsurface_ocean")
    @Schema(description = "Whether the body has a subsurface liquid ocean")
    private Boolean hasSubsurfaceOcean;

    @Column(name = "ocean_depth_km")
    @Schema(description = "Depth of subsurface ocean in km")
    private Double oceanDepthKm;

    @Column(name = "ice_shell_thickness_km")
    @Schema(description = "Thickness of ice shell above subsurface ocean in km")
    private Double iceShellThicknessKm;

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
