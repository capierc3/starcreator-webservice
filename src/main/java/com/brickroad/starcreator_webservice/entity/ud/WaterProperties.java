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
 * Water and hydrological properties for any celestial body.
 * <p>
 * Used by planets, moons, and (in the future) asteroids. Body-type-specific fields
 * (e.g. subsurface ocean details for icy moons) are nullable and only populated for
 * the appropriate body type.
 */
@Entity
@Table(name = "water_properties", schema = "ud")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Water and hydrological properties for any celestial body")
public class WaterProperties {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    // ── Surface Water ──

    @Column(name = "water_inventory", length = 30)
    @Schema(description = "Relative water inventory classification", example = "MODERATE")
    private String waterInventory;

    @Column(name = "water_coverage_percent")
    @Schema(description = "Total water coverage as percentage of surface", example = "71.0")
    private Double waterCoveragePercent;

    @Column(name = "liquid_water_coverage_percent")
    @Schema(description = "Liquid water coverage as percentage of surface", example = "65.0")
    private Double liquidWaterCoveragePercent;

    @Column(name = "ice_coverage_percent")
    @Schema(description = "Ice coverage as percentage of surface", example = "6.0")
    private Double iceCoveragePercent;

    // ── Subsurface Water ──

    @Column(name = "has_subsurface_water")
    @Schema(description = "Whether subsurface liquid water is present")
    private Boolean hasSubsurfaceWater;

    @Column(name = "subsurface_water_depth_km")
    @Schema(description = "Depth to subsurface water layer in km")
    private Double subsurfaceWaterDepthKm;

    // ── Subsurface Ocean (promoted from Moon, applicable to any icy body) ──

    @Column(name = "has_subsurface_ocean")
    @Schema(description = "Whether the body has a subsurface liquid water ocean")
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
