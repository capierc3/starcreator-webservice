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

    // ── Designation (identity card) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "designation_id")
    @Schema(description = "Identity card: designation, classification, and survey history")
    private Designation designation;

    // ── Parent Relationship ──

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planet_id", nullable = false)
    @JsonBackReference
    private Planet planet;

    // ── Physical Properties (extracted to PhysicalProperties entity) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "physical_properties_id")
    @Schema(description = "Physical properties including mass, radius, density, and gravity")
    private PhysicalProperties physicalProperties;

    // ── Orbital Properties ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "orbital_elements_id")
    @Schema(description = "Keplerian orbital elements for this moon's orbit around its planet")
    private OrbitalElements orbit;

    // ── Rotation (extracted to RotationProperties entity) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "rotation_properties_id")
    @Schema(description = "Rotation and spin-axis properties")
    private RotationProperties rotation;

    // ── Tidal Heating (moon-specific) ──

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

    // ── Composition (extracted to CompositionProperties entity) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "composition_properties_id")
    @Schema(description = "Composition properties")
    private CompositionProperties compositionProperties;

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

    // ── Designation Convenience Getters/Setters ──

    private Designation ensureDesignation() {
        if (designation == null) designation = new Designation();
        return designation;
    }

    @JsonIgnore
    public String getName() {
        return designation != null ? designation.getLoggedName() : null;
    }
    public void setName(String name) { ensureDesignation().setLoggedName(name); }

    @JsonIgnore
    public String getMoonType() {
        return designation != null ? designation.getObjectType() : null;
    }
    public void setMoonType(String moonType) { ensureDesignation().setObjectType(moonType); }

    @JsonIgnore
    public String getFormationType() {
        return designation != null ? designation.getFormationType() : null;
    }
    public void setFormationType(String formationType) { ensureDesignation().setFormationType(formationType); }

    @JsonIgnore
    public Double getAgeMY() {
        return designation != null ? designation.getAgeMY() : null;
    }
    public void setAgeMY(Double ageMY) { ensureDesignation().setAgeMY(ageMY); }

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

    @JsonIgnore
    public Boolean getTidallyLocked() {
        return rotation != null ? rotation.getTidallyLocked() : null;
    }
    public void setTidallyLocked(Boolean tidallyLocked) { ensureRotation().setTidallyLocked(tidallyLocked); }

    // ── JSON Accessors ──

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

    // ── Composition Convenience Getters/Setters ──

    private CompositionProperties ensureCompositionProperties() {
        if (compositionProperties == null) compositionProperties = new CompositionProperties();
        return compositionProperties;
    }

    @JsonIgnore
    public String getCompositionType() {
        return compositionProperties != null ? compositionProperties.getCompositionType() : null;
    }
    public void setCompositionType(String compositionType) { ensureCompositionProperties().setCompositionType(compositionType); }

    @JsonIgnore
    public String getInteriorComposition() {
        return compositionProperties != null ? compositionProperties.getInteriorComposition() : null;
    }
    public void setInteriorComposition(String interiorComposition) { ensureCompositionProperties().setInteriorComposition(interiorComposition); }

    @JsonIgnore
    public String getEnvelopeComposition() {
        return compositionProperties != null ? compositionProperties.getEnvelopeComposition() : null;
    }
    public void setEnvelopeComposition(String envelopeComposition) { ensureCompositionProperties().setEnvelopeComposition(envelopeComposition); }

    @JsonIgnore
    public String getCompositionClassification() {
        return compositionProperties != null ? compositionProperties.getCompositionClassification() : null;
    }
    public void setCompositionClassification(String compositionClassification) { ensureCompositionProperties().setCompositionClassification(compositionClassification); }

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

    @JsonIgnore
    public Double getHillSphereRadiusKm() {
        return orbit != null ? orbit.getHillSphereRadiusKm() : null;
    }
    public void setHillSphereRadiusKm(Double hillSphereRadiusKm) {
        if (orbit == null) orbit = new OrbitalElements();
        orbit.setHillSphereRadiusKm(hillSphereRadiusKm);
    }

    @JsonIgnore
    public Double getRocheLimitKm() {
        return orbit != null ? orbit.getRocheLimitKm() : null;
    }
    public void setRocheLimitKm(Double rocheLimitKm) {
        if (orbit == null) orbit = new OrbitalElements();
        orbit.setRocheLimitKm(rocheLimitKm);
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
