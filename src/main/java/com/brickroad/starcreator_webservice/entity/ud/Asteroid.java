package com.brickroad.starcreator_webservice.entity.ud;

import com.brickroad.starcreator_webservice.entity.ref.AsteroidTypeRef;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "asteroid", schema = "ud")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "A notable asteroid within an orbital band")
public class Asteroid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "band_id")
    @JsonBackReference("band-asteroids")
    private OrbitalBand band;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asteroid_type_id", nullable = false)
    @Schema(description = "Asteroid type classification")
    private AsteroidTypeRef asteroidType;

    // ── Identity ──

    @Column(name = "name", nullable = false)
    @Schema(description = "Asteroid designation", example = "SCS-V01-8RQ KB-01 AST-0001")
    private String name;

    @Column(name = "designation_code")
    @Schema(description = "Discovery designation code", example = "2435 WR1")
    private String designationCode;

    @Column(name = "age_my")
    @Schema(description = "Age in millions of years")
    private Double ageMY;

    // ── Physical Properties ──

    @Column(name = "mass")
    @Schema(description = "Mass in kg")
    private Double mass;

    @Column(name = "earth_mass")
    @Schema(description = "Mass in Earth masses")
    private Double earthMass;

    @Column(name = "radius")
    @Schema(description = "Mean radius in km")
    private Double radius;

    @Column(name = "circumference")
    @JsonIgnore
    private Double circumference;

    @Column(name = "dimensions_km")
    @Schema(description = "Approximate dimensions for irregularly shaped bodies", example = "965 x 961 x 891")
    private String dimensionsKm;

    @Column(name = "density")
    @Schema(description = "Bulk density in g/cm\u00b3", example = "2.16")
    private Double density;

    @Column(name = "albedo")
    @Schema(description = "Surface albedo", example = "0.09")
    private Double albedo;

    // ── Orbital Properties ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "orbital_elements_id")
    @Schema(description = "Keplerian orbital elements for this asteroid's orbit")
    private OrbitalElements orbit;

    // ── Rotation ──

    @Column(name = "rotation_period_hours")
    @Schema(description = "Rotation period in hours", example = "9.07")
    private Double rotationPeriodHours;

    @Column(name = "axial_tilt")
    @Schema(description = "Axial tilt in degrees")
    private Double axialTilt;

    // ── Surface ──

    @Column(name = "surface_temp")
    @Schema(description = "Surface temperature in Kelvin", example = "167")
    private Double surfaceTemp;

    @Column(name = "surface_gravity")
    @Schema(description = "Surface gravity in m/s\u00b2", example = "0.27")
    private Double surfaceGravity;

    @Column(name = "escape_velocity")
    @Schema(description = "Escape velocity in km/s", example = "0.51")
    private Double escapeVelocity;

    // ── Terrain (extracted to TerrainProperties) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "terrain_id")
    @Schema(description = "Terrain and surface morphology data")
    private TerrainProperties terrain;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "water_id")
    @Schema(description = "Water and hydrological properties")
    private WaterProperties water;

    // ── Composition ──

    @Column(name = "composition")
    @Schema(description = "Composition breakdown", example = "Silicates 60%, Iron-Nickel 30%, Carbon 10%")
    private String composition;

    @Column(name = "is_differentiated")
    @JsonIgnore
    private Boolean isDifferentiated;

    @Column(name = "core_type")
    @Schema(description = "Core composition type, if differentiated")
    private String coreType;

    // ── Moons ──

    @Column(name = "has_moon")
    @Schema(description = "Whether this asteroid has its own moon(s)")
    private Boolean hasMoon;

    @Column(name = "moon_count")
    @Schema(description = "Number of moons")
    private Integer moonCount;

    @Column(name = "moon_description")
    @Schema(description = "Description of the asteroid's moon(s)")
    private String moonDescription;

    // ── Notable Status ──

    @Column(name = "is_notable")
    @JsonIgnore
    private Boolean isNotable;

    @Column(name = "notable_reason")
    @Schema(description = "Reason this asteroid is notable", example = "Largest object in the belt")
    private String notableReason;

    // ── Timestamps ──

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    // ── Orbital Convenience Getters (delegate to orbit object) ──

    @JsonIgnore
    public Double getSemiMajorAxisAu() {
        return orbit != null ? orbit.getSemiMajorAxis() : null;
    }

    @JsonIgnore
    public Double getEccentricity() {
        return orbit != null ? orbit.getEccentricity() : null;
    }

    @JsonIgnore
    public Double getInclinationDegrees() {
        return orbit != null ? orbit.getInclinationDegrees() : null;
    }

    @JsonIgnore
    public Double getOrbitalPeriodDays() {
        return orbit != null ? orbit.getOrbitalPeriodDays() : null;
    }

    // ── Terrain Convenience Getters (delegate to terrain object) ──

    @JsonIgnore
    public String getSurfaceFeatures() {
        return terrain != null ? terrain.getSurfaceFeatures() : null;
    }

    @JsonIgnore
    public String getCrateringLevel() {
        return terrain != null ? terrain.getCrateringLevel() : null;
    }

    @JsonIgnore
    public Boolean getHasRegolith() {
        return terrain != null ? terrain.getHasRegolith() : null;
    }

    @JsonIgnore
    public Double getRegolithDepthM() {
        return terrain != null ? terrain.getRegolithDepthM() : null;
    }
}
