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

    // ── Designation (identity card) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "designation_id")
    @Schema(description = "Identity card: designation, classification, and survey history")
    private Designation designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "band_id")
    @JsonBackReference("band-asteroids")
    private OrbitalBand band;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asteroid_type_id", nullable = false)
    @Schema(description = "Asteroid type classification")
    private AsteroidTypeRef asteroidType;

    // ── Identity (legacy name column kept for DB compatibility) ──

    @Column(name = "name", nullable = false)
    @JsonIgnore
    private String nameColumn;

    // ── Physical Properties ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "physical_properties_id")
    @Schema(description = "Physical properties including mass, radius, density, and gravity")
    private PhysicalProperties physicalProperties;

    // ── Rotation ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "rotation_properties_id")
    @Schema(description = "Rotation and spin-axis properties")
    private RotationProperties rotation;

    // ── Orbital (persisted SMA — unique per asteroid, derived from parent band range) ──

    @Column(name = "semi_major_axis_au")
    @Schema(description = "Semi-major axis in AU", example = "2.77")
    private Double semiMajorAxisAu;

    // ── Terrain (flattened — asteroid-specific surface data) ──

    @Column(name = "surface_features", columnDefinition = "TEXT")
    @Schema(description = "Notable surface features")
    private String surfaceFeatures;

    @Column(name = "cratering_level", length = 20)
    @Schema(description = "Impact cratering level", example = "MODERATE")
    private String crateringLevel;

    @Column(name = "has_regolith")
    @Schema(description = "Whether surface has a regolith layer")
    private Boolean hasRegolith;

    @Column(name = "regolith_depth_m")
    @Schema(description = "Regolith depth in meters")
    private Double regolithDepthM;

    // ── Composition (flattened) ──

    @Column(name = "composition", columnDefinition = "TEXT")
    @Schema(description = "Composition breakdown", example = "Silicates 60%, Iron-Nickel 30%, Carbon 10%")
    private String composition;

    @Column(name = "is_differentiated")
    @Schema(description = "Whether the asteroid has differentiated interior layers")
    private Boolean isDifferentiated;

    @Column(name = "core_type", length = 50)
    @Schema(description = "Core composition type for differentiated asteroids", example = "Iron-Nickel")
    private String coreType;

    // ── Water/Ice ──

    @Column(name = "ice_percent")
    @Schema(description = "Ice content as percentage of mass (0-100)", example = "45.2")
    private Double icePercent;

    // ── Notable Status ──

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

    // ── Designation Convenience Getters/Setters ──

    private Designation ensureDesignation() {
        if (designation == null) designation = new Designation();
        return designation;
    }

    @JsonIgnore
    public String getName() {
        return designation != null ? designation.getLoggedName() : nameColumn;
    }

    public void setName(String name) {
        ensureDesignation().setLoggedName(name);
        this.nameColumn = name;
    }

    @JsonIgnore
    public String getDesignationCode() {
        return designation != null ? designation.getDesignationCode() : null;
    }

    public void setDesignationCode(String designationCode) {
        ensureDesignation().setDesignationCode(designationCode);
    }

    @JsonIgnore
    public Double getAgeMY() {
        return designation != null ? designation.getAgeMY() : null;
    }

    public void setAgeMY(Double ageMY) {
        ensureDesignation().setAgeMY(ageMY);
    }

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

    @JsonIgnore
    public Double getEarthRadius() {
        return physicalProperties != null ? physicalProperties.getEarthRadius() : null;
    }
    public void setEarthRadius(Double earthRadius) { ensurePhysicalProperties().setEarthRadius(earthRadius); }

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
}
