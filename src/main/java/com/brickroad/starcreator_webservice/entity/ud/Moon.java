package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "moon", schema = "ud")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "A natural satellite orbiting a planet")
public class Moon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    // ── Name (transient — deferred to a future naming update) ──

    @Transient
    @JsonIgnore
    private String name;

    // ── Parent Relationship ──

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planet_id", nullable = false)
    @JsonBackReference
    private Planet planet;

    // ── Classification ──

    @Column(name = "moon_type", length = 50, nullable = false)
    @Schema(description = "Moon classification type", example = "REGULAR_LARGE")
    private String moonType;

    @Column(name = "formation_type", length = 50)
    @Schema(description = "How the moon formed", example = "CO_FORMED")
    private String formationType;

    @Column(name = "age_my")
    @Schema(description = "Age in millions of years")
    private Double ageMY;

    // ── Physical Properties (extracted to PhysicalProperties entity) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "physical_properties_id")
    @Schema(description = "Physical properties including mass, radius, density, and gravity")
    private PhysicalProperties physicalProperties;

    @Column(name = "composition_type", length = 50)
    @Schema(description = "Primary composition type", example = "ROCKY")
    private String compositionType;

    // ── Orbital Properties ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "orbital_elements_id")
    @Schema(description = "Keplerian orbital elements for this moon's orbit around its planet")
    private OrbitalElements orbit;

    @Column(name = "hill_sphere_radius_km")
    @Schema(description = "Hill sphere radius in km")
    private Double hillSphereRadiusKm;

    @Column(name = "roche_limit_km")
    @Schema(description = "Roche limit distance in km")
    private Double rocheLimitKm;

    // ── Rotation & Tidal ──

    @Column(name = "tidally_locked", nullable = false)
    @JsonIgnore
    private Boolean tidallyLocked = true;

    @Column(name = "rotation_period_hours")
    @Schema(description = "Rotation period in hours", example = "655.7")
    private Double rotationPeriodHours;

    @Column(name = "axial_tilt")
    @Schema(description = "Axial tilt in degrees", example = "6.7")
    private Double axialTilt;

    @Column(name = "tidal_heating_level", length = 20)
    @Schema(description = "Tidal heating intensity", example = "MODERATE")
    private String tidalHeatingLevel;

    @Column(name = "tidal_heating_watt_per_m2")
    @Schema(description = "Tidal heating flux in W/m²")
    private Double tidalHeatingWattPerM2;

    // ── Terrain (extracted to TerrainProperties) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "terrain_id")
    @Schema(description = "Terrain, geology, and surface morphology data")
    private TerrainProperties terrain;

    // ── Water (extracted to WaterProperties) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "water_id")
    @Schema(description = "Water and hydrological properties")
    private WaterProperties water;

    // ── Atmosphere ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "atmosphere_id")
    @Schema(description = "Atmospheric properties")
    private Atmosphere atmosphere;

    // ── Composition ──

    @Column(name = "interior_composition", length = 500)
    @Schema(description = "Interior composition breakdown")
    private String interiorComposition;

    @Column(name = "envelope_composition", length = 500)
    @Schema(description = "Surface/envelope composition breakdown")
    private String envelopeComposition;

    @Column(name = "composition_classification", length = 50)
    @Schema(description = "Composition classification")
    private String compositionClassification;

    // ── Ring Interaction ──

    @Column(name = "is_shepherd_moon", nullable = false)
    @JsonIgnore
    private Boolean isShepherdMoon = false;

    @Column(name = "shepherds_ring_name", length = 50)
    @Schema(description = "Name of the ring this moon shepherds, if applicable")
    private String shepherdsRingName;

    // ── Magnetic Field (extracted to PlanetaryMagneticField) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "magnetic_field_id")
    @Schema(description = "Magnetic field properties")
    private PlanetaryMagneticField magneticField;

    // ── Habitability & Climate ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "habitability_id")
    @Schema(description = "Habitability assessment")
    private PlanetaryHabitability habitability;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "climate_id")
    @Schema(description = "Climate and atmospheric conditions")
    private PlanetaryClimate climate;

    // ── Metadata ──

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @Schema(description = "Timestamp when this moon was generated")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    @Schema(description = "Timestamp when this moon was last modified")
    private LocalDateTime modifiedAt;

    // ── JSON Accessors ──

    @JsonProperty("tidallyLocked")
    @Schema(description = "Whether the moon is tidally locked to its planet", example = "true")
    public Boolean getTidallyLocked() {
        return tidallyLocked;
    }

    public void setTidallyLocked(Boolean tidallyLocked) {
        this.tidallyLocked = tidallyLocked;
    }

    @JsonProperty("shepherdMoon")
    @Schema(description = "Whether this moon acts as a shepherd for a ring")
    public Boolean getShepherdMoon() {
        return isShepherdMoon;
    }

    // ── Physical Properties Convenience Getters/Setters ──

    private PhysicalProperties ensurePhysicalProperties() {
        if (physicalProperties == null) physicalProperties = new PhysicalProperties();
        return physicalProperties;
    }

    @JsonIgnore
    public double getMass() {
        return physicalProperties != null ? physicalProperties.getMass() : 0;
    }
    public void setMass(double mass) { ensurePhysicalProperties().setMass(mass); }

    @JsonIgnore
    public double getRadius() {
        return physicalProperties != null ? physicalProperties.getRadius() : 0;
    }
    public void setRadius(double radius) { ensurePhysicalProperties().setRadius(radius); }

    @JsonIgnore
    public double getCircumference() {
        return physicalProperties != null ? physicalProperties.getCircumference() : 0;
    }
    public void setCircumference(double circumference) { ensurePhysicalProperties().setCircumference(circumference); }

    @JsonIgnore
    public Double getEarthMass() {
        return physicalProperties != null ? physicalProperties.getEarthMass() : null;
    }
    public void setEarthMass(Double earthMass) { ensurePhysicalProperties().setEarthMass(earthMass); }

    @JsonIgnore
    public Double getEarthRadius() {
        return physicalProperties != null ? physicalProperties.getEarthRadius() : null;
    }
    public void setEarthRadius(Double earthRadius) { ensurePhysicalProperties().setEarthRadius(earthRadius); }

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
    public Double getSurfaceTemp() {
        return physicalProperties != null ? physicalProperties.getSurfaceTemp() : null;
    }
    public void setSurfaceTemp(Double surfaceTemp) { ensurePhysicalProperties().setSurfaceTemp(surfaceTemp); }

    // ── Orbital Convenience Getters ──

    @JsonIgnore
    public Double getDistanceFromStar() {
        return orbit != null ? orbit.getDistanceFromParent() : null;
    }

    public void setDistanceFromStar(Double distance) {
        if (orbit == null) orbit = new OrbitalElements();
        orbit.setDistanceFromParent(distance);
    }

    @JsonIgnore
    public Integer getOrbitalOrder() {
        return orbit != null ? orbit.getOrbitalOrder() : null;
    }

    public void setOrbitalOrder(Integer orbitalOrder) {
        if (orbit == null) orbit = new OrbitalElements();
        orbit.setOrbitalOrder(orbitalOrder);
    }

    @JsonIgnore
    public Double getSemiMajorAxisKm() {
        return orbit != null ? orbit.getSemiMajorAxis() : null;
    }

    @JsonIgnore
    public Double getOrbitalPeriodDays() {
        return orbit != null ? orbit.getOrbitalPeriodDays() : null;
    }

    @JsonIgnore
    public Double getEccentricity() {
        return orbit != null ? orbit.getEccentricity() : null;
    }

    @JsonIgnore
    public Double getOrbitalInclinationDegrees() {
        return orbit != null ? orbit.getInclinationDegrees() : null;
    }

    @JsonIgnore
    public String getOrbitStability() {
        return orbit != null ? orbit.getOrbitStability() : null;
    }

    // ── Terrain Convenience Getters (delegate to terrain object) ──

    @JsonIgnore
    public String getCrateringLevel() {
        return terrain != null ? terrain.getCrateringLevel() : null;
    }

    @JsonIgnore
    public Integer getEstimatedVisibleCraters() {
        return terrain != null ? terrain.getEstimatedVisibleCraters() : null;
    }

    @JsonIgnore
    public String getSurfaceFeatures() {
        return terrain != null ? terrain.getSurfaceFeatures() : null;
    }

    @JsonIgnore
    public String getGeologicalActivity() {
        return terrain != null ? terrain.getGeologicalActivity() : null;
    }

    @JsonIgnore
    public Boolean getHasCryovolcanism() {
        return terrain != null ? terrain.getHasCryovolcanism() : null;
    }

    @JsonIgnore
    public String getVolcanismType() {
        return terrain != null ? terrain.getVolcanismType() : null;
    }

    @JsonIgnore
    public String getVolcanicIntensity() {
        return terrain != null ? terrain.getVolcanicIntensity() : null;
    }

    @JsonIgnore
    public Integer getEstimatedActiveVolcanoes() {
        return terrain != null ? terrain.getEstimatedActiveVolcanoes() : null;
    }

    @JsonIgnore
    public Double getMountainCoveragePercent() {
        return terrain != null ? terrain.getMountainCoveragePercent() : null;
    }

    @JsonIgnore
    public Double getAverageElevationKm() {
        return terrain != null ? terrain.getAverageElevationKm() : null;
    }

    @JsonIgnore
    public Double getMaxElevationKm() {
        return terrain != null ? terrain.getMaxElevationKm() : null;
    }

    @JsonIgnore
    public Double getMinElevationKm() {
        return terrain != null ? terrain.getMinElevationKm() : null;
    }

    @JsonIgnore
    public Double getTerrainRoughness() {
        return terrain != null ? terrain.getTerrainRoughness() : null;
    }

    @JsonIgnore
    public String getErosionLevel() {
        return terrain != null ? terrain.getErosionLevel() : null;
    }

    @JsonIgnore
    public String getPrimaryErosionAgent() {
        return terrain != null ? terrain.getPrimaryErosionAgent() : null;
    }

    // ── Water Convenience Getters (delegate to water object) ──

    @JsonIgnore
    public Boolean getHasSubsurfaceOcean() {
        return water != null ? water.getHasSubsurfaceOcean() : false;
    }

    @JsonIgnore
    public Double getOceanDepthKm() {
        return water != null ? water.getOceanDepthKm() : null;
    }

    @JsonIgnore
    public Double getIceShellThicknessKm() {
        return water != null ? water.getIceShellThicknessKm() : null;
    }

    @JsonIgnore
    public String getWaterInventory() {
        return water != null ? water.getWaterInventory() : null;
    }

    @JsonIgnore
    public Double getWaterCoveragePercent() {
        return water != null ? water.getWaterCoveragePercent() : null;
    }

    @JsonIgnore
    public Double getLiquidWaterCoveragePercent() {
        return water != null ? water.getLiquidWaterCoveragePercent() : null;
    }

    @JsonIgnore
    public Double getIceCoveragePercent() {
        return water != null ? water.getIceCoveragePercent() : null;
    }

    @JsonIgnore
    public Boolean getHasSubsurfaceWater() {
        return water != null ? water.getHasSubsurfaceWater() : false;
    }

    @JsonIgnore
    public Double getSubsurfaceWaterDepthKm() {
        return water != null ? water.getSubsurfaceWaterDepthKm() : null;
    }

    // ── Atmosphere Convenience Getters (delegate to atmosphere object) ──

    @JsonIgnore
    public Boolean getHasAtmosphere() {
        return atmosphere != null
                && atmosphere.getClassification() != null
                && !"NONE".equals(atmosphere.getClassification());
    }

    @JsonIgnore
    public Double getSurfacePressure() {
        return atmosphere != null ? atmosphere.getSurfacePressureBar() : null;
    }

    @JsonIgnore
    public String getAtmosphereComposition() {
        return atmosphere != null ? atmosphere.getCompositionSummary() : null;
    }
}
