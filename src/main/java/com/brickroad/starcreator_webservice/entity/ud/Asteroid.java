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
    @JsonIgnore
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

    // ── Physical Properties (extracted to PhysicalProperties entity) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "physical_properties_id")
    @Schema(description = "Physical properties including mass, radius, density, and gravity")
    private PhysicalProperties physicalProperties;

    // ── Orbital Properties ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "orbital_elements_id")
    @Schema(description = "Keplerian orbital elements for this asteroid's orbit")
    private OrbitalElements orbit;

    // ── Rotation (extracted to RotationProperties entity) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "rotation_properties_id")
    @Schema(description = "Rotation and spin-axis properties")
    private RotationProperties rotation;

    // ── Terrain (extracted to TerrainProperties) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "terrain_id")
    @Schema(description = "Terrain and surface morphology data")
    private TerrainProperties terrain;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "water_id")
    @Schema(description = "Water and hydrological properties")
    private WaterProperties water;

    // ── Composition (extracted to CompositionProperties entity) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "composition_properties_id")
    @Schema(description = "Composition properties")
    private CompositionProperties compositionProperties;

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
    @JsonIgnore
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    @JsonIgnore
    private LocalDateTime modifiedAt;

    // ── Physical Properties Convenience Getters/Setters ──

    private PhysicalProperties ensurePhysicalProperties() {
        if (physicalProperties == null) physicalProperties = new PhysicalProperties();
        return physicalProperties;
    }

    @JsonIgnore
    public Double getMass() {
        return physicalProperties != null ? physicalProperties.getMass() : null;
    }
    public void setMass(Double mass) { ensurePhysicalProperties().setMass(mass); }

    @JsonIgnore
    public Double getEarthMass() {
        return physicalProperties != null ? physicalProperties.getEarthMass() : null;
    }
    public void setEarthMass(Double earthMass) { ensurePhysicalProperties().setEarthMass(earthMass); }

    @JsonIgnore
    public Double getRadius() {
        return physicalProperties != null ? physicalProperties.getRadius() : null;
    }
    public void setRadius(Double radius) { ensurePhysicalProperties().setRadius(radius); }

    @JsonIgnore
    public Double getCircumference() {
        return physicalProperties != null ? physicalProperties.getCircumference() : null;
    }
    public void setCircumference(Double circumference) { ensurePhysicalProperties().setCircumference(circumference); }

    @JsonIgnore
    public String getDimensionsKm() {
        return physicalProperties != null ? physicalProperties.getDimensionsKm() : null;
    }
    public void setDimensionsKm(String dimensionsKm) { ensurePhysicalProperties().setDimensionsKm(dimensionsKm); }

    @JsonIgnore
    public Double getDensity() {
        return physicalProperties != null ? physicalProperties.getDensity() : null;
    }
    public void setDensity(Double density) { ensurePhysicalProperties().setDensity(density); }

    @JsonIgnore
    public Double getAlbedo() {
        return physicalProperties != null ? physicalProperties.getAlbedo() : null;
    }
    public void setAlbedo(Double albedo) { ensurePhysicalProperties().setAlbedo(albedo); }

    @JsonIgnore
    public Double getSurfaceTemp() {
        return physicalProperties != null ? physicalProperties.getSurfaceTemp() : null;
    }
    public void setSurfaceTemp(Double surfaceTemp) { ensurePhysicalProperties().setSurfaceTemp(surfaceTemp); }

    @JsonIgnore
    public Double getSurfaceGravity() {
        return physicalProperties != null ? physicalProperties.getSurfaceGravity() : null;
    }
    public void setSurfaceGravity(Double surfaceGravity) { ensurePhysicalProperties().setSurfaceGravity(surfaceGravity); }

    @JsonIgnore
    public Double getEscapeVelocity() {
        return physicalProperties != null ? physicalProperties.getEscapeVelocity() : null;
    }
    public void setEscapeVelocity(Double escapeVelocity) { ensurePhysicalProperties().setEscapeVelocity(escapeVelocity); }

    // ── Rotation Convenience Getters/Setters ──

    private RotationProperties ensureRotation() {
        if (rotation == null) rotation = new RotationProperties();
        return rotation;
    }

    @JsonIgnore
    public Double getRotationPeriodHours() {
        return rotation != null ? rotation.getRotationPeriodHours() : null;
    }
    public void setRotationPeriodHours(Double rotationPeriodHours) { ensureRotation().setRotationPeriodHours(rotationPeriodHours); }

    @JsonIgnore
    public Double getAxialTilt() {
        return rotation != null ? rotation.getAxialTilt() : null;
    }
    public void setAxialTilt(Double axialTilt) { ensureRotation().setAxialTilt(axialTilt); }

    // ── Composition Convenience Getters/Setters ──

    private CompositionProperties ensureCompositionProperties() {
        if (compositionProperties == null) compositionProperties = new CompositionProperties();
        return compositionProperties;
    }

    @JsonIgnore
    public String getComposition() {
        return compositionProperties != null ? compositionProperties.getComposition() : null;
    }
    public void setComposition(String composition) { ensureCompositionProperties().setComposition(composition); }

    @JsonIgnore
    public Boolean getIsDifferentiated() {
        return compositionProperties != null ? compositionProperties.getIsDifferentiated() : null;
    }
    public void setIsDifferentiated(Boolean isDifferentiated) { ensureCompositionProperties().setIsDifferentiated(isDifferentiated); }

    @JsonIgnore
    public String getCoreType() {
        return compositionProperties != null ? compositionProperties.getCoreType() : null;
    }
    public void setCoreType(String coreType) { ensureCompositionProperties().setCoreType(coreType); }

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
