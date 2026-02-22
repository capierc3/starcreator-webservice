package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "planet", schema = "ud")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "planetType", "habitableZonePosition",
    "ageMY", "parentStar",
    "physicalProperties", "orbit", "rotation",
    "atmosphere",
    "compositionProperties",
    "water", "terrain",
    "additionalMoonlets", "moons", "bands",
    "magneticField", "habitability", "climate",
    "createdAt", "modifiedAt"
})
@Schema(description = "A planet orbiting a star within a star system")
public class Planet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    // ── Name (transient — deferred to a future naming update) ──

    @Transient
    @JsonIgnore
    private String name;

    // ── Identity & Classification ──

    @Column(name = "planet_type")
    @Schema(description = "Planet classification type", example = "Ocean Planet")
    private String planetType;

    @Column(name = "age_millions_years")
    @Schema(description = "Age in millions of years", example = "4500")
    private Double ageMY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "star_id")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
    @JsonIdentityReference(alwaysAsId = true)
    @Schema(description = "Name of the parent star this planet orbits", example = "SCS-V01-8RQ A")
    private Star parentStar;

    @Column(name = "habitable_zone_position")
    @Schema(description = "Position relative to the habitable zone", example = "habitable")
    private String habitableZonePosition;

    // ── Physical Properties (extracted to PhysicalProperties entity) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "physical_properties_id")
    @Schema(description = "Physical properties including mass, radius, density, and gravity")
    private PhysicalProperties physicalProperties;

    // ── Orbital Properties ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "orbital_elements_id")
    @Schema(description = "Keplerian orbital elements for this planet's orbit around its star")
    private OrbitalElements orbit;

    // ── Rotation (extracted to RotationProperties entity) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "rotation_properties_id")
    @Schema(description = "Rotation and spin-axis properties")
    private RotationProperties rotation;

    // ── Atmosphere (extracted to Atmosphere entity) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "atmosphere_id")
    @Schema(description = "Atmospheric properties")
    private Atmosphere atmosphere;

    // ── Composition (extracted to CompositionProperties entity) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "composition_properties_id")
    @Schema(description = "Composition properties including core, interior, and envelope breakdown")
    private CompositionProperties compositionProperties;

    // ── Water (extracted to WaterProperties) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "water_id")
    @Schema(description = "Water and hydrological properties")
    private WaterProperties water;

    // ── Terrain (extracted to TerrainProperties) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "terrain_id")
    @Schema(description = "Terrain, geology, and surface morphology data")
    private TerrainProperties terrain;

    // ── Moons & Bands ──

    @Column(name = "additional_moonlets")
    @Schema(description = "Number of additional small moonlets not individually generated")
    private Integer additionalMoonlets;

    @OneToMany(mappedBy = "planet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Schema(description = "Moons orbiting this planet")
    private List<Moon> moons = new ArrayList<>();

    @OneToMany(mappedBy = "planet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("planet-bands")
    @Schema(description = "Orbital bands (rings) around this planet")
    private List<OrbitalBand> bands = new ArrayList<>();

    // ── Magnetic Field (extracted to PlanetaryMagneticField) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "magnetic_field_id")
    @Schema(description = "Magnetic field properties")
    private PlanetaryMagneticField magneticField;

    // ── Habitability & Climate ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "habitability_id")
    @Schema(description = "Habitability assessment including ESI score and risk factors")
    private PlanetaryHabitability habitability;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "climate_id")
    @Schema(description = "Climate and atmospheric conditions")
    private PlanetaryClimate climate;

    // ── Metadata ──

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @Schema(description = "Timestamp when this planet was generated")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    @Schema(description = "Timestamp when this planet was last modified")
    private LocalDateTime modifiedAt;

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

    // ── System Convenience (navigate through parentStar) ──

    @JsonIgnore
    public StarSystem getSystem() {
        return parentStar != null ? parentStar.getSystem() : null;
    }

    // ── Physical Properties Convenience Getters/Setters ──
    // Delegate to physicalProperties, auto-creating if needed for setters.

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
    public Double getAlbedo() {
        return physicalProperties != null ? physicalProperties.getAlbedo() : null;
    }
    public void setAlbedo(Double albedo) { ensurePhysicalProperties().setAlbedo(albedo); }

    @JsonIgnore
    public Double getSurfaceTemp() {
        return physicalProperties != null ? physicalProperties.getSurfaceTemp() : null;
    }
    public void setSurfaceTemp(Double surfaceTemp) { ensurePhysicalProperties().setSurfaceTemp(surfaceTemp); }

    // ── Orbital Convenience Getters (delegate to orbit object) ──

    @JsonIgnore
    public Integer getOrbitalPosition() {
        return orbit != null ? orbit.getOrbitalOrder() : null;
    }

    public void setOrbitalPosition(Integer position) {
        if (orbit == null) orbit = new OrbitalElements();
        orbit.setOrbitalOrder(position);
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
    public Double getDistanceFromStar() {
        return orbit != null ? orbit.getDistanceFromParent() : null;
    }

    public void setDistanceFromStar(Double distance) {
        if (orbit == null) orbit = new OrbitalElements();
        orbit.setDistanceFromParent(distance);
    }

    @JsonIgnore
    public Double getSemiMajorAxisAU() {
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
    public Double getOrbitStabilityTimescaleMy() {
        return orbit != null ? orbit.getOrbitStabilityTimescaleMy() : null;
    }

    @JsonIgnore
    public String getOrbitCrossingNeighbor() {
        return orbit != null ? orbit.getOrbitCrossingNeighbor() : null;
    }

    // ── Terrain Convenience Getters (delegate to terrain object) ──

    @JsonIgnore
    public String getGeologicalActivity() {
        return terrain != null ? terrain.getGeologicalActivity() : null;
    }

    @JsonIgnore
    public Double getActivityScore() {
        return terrain != null ? terrain.getActivityScore() : null;
    }

    @JsonIgnore
    public Boolean getHasPlateTectonics() {
        return terrain != null ? terrain.getHasPlateTectonics() : null;
    }

    @JsonIgnore
    public Integer getNumberOfTectonicPlates() {
        return terrain != null ? terrain.getNumberOfTectonicPlates() : null;
    }

    @JsonIgnore
    public String getTectonicActivityLevel() {
        return terrain != null ? terrain.getTectonicActivityLevel() : null;
    }

    @JsonIgnore
    public Boolean getHasVolcanicActivity() {
        return terrain != null ? terrain.getHasVolcanicActivity() : null;
    }

    @JsonIgnore
    public String getVolcanismType() {
        return terrain != null ? terrain.getVolcanismType() : null;
    }

    @JsonIgnore
    public Integer getEstimatedActiveVolcanoes() {
        return terrain != null ? terrain.getEstimatedActiveVolcanoes() : null;
    }

    @JsonIgnore
    public String getVolcanicIntensity() {
        return terrain != null ? terrain.getVolcanicIntensity() : null;
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
    public String getCrateringLevel() {
        return terrain != null ? terrain.getCrateringLevel() : null;
    }

    @JsonIgnore
    public Integer getEstimatedVisibleCraters() {
        return terrain != null ? terrain.getEstimatedVisibleCraters() : null;
    }

    @JsonIgnore
    public String getErosionLevel() {
        return terrain != null ? terrain.getErosionLevel() : null;
    }

    @JsonIgnore
    public String getPrimaryErosionAgent() {
        return terrain != null ? terrain.getPrimaryErosionAgent() : null;
    }

    @JsonIgnore
    public List<TerrainDistribution> getTerrainDistribution() {
        return terrain != null ? terrain.getTerrainDistribution() : new ArrayList<>();
    }

    // ── Storm Convenience Getters (delegate to atmosphere object) ──

    private Atmosphere ensureAtmosphere() {
        if (atmosphere == null) atmosphere = new Atmosphere();
        return atmosphere;
    }

    @JsonIgnore
    public Boolean getHasGreatStorm() {
        return atmosphere != null ? atmosphere.getHasGreatStorm() : null;
    }
    public void setHasGreatStorm(Boolean hasGreatStorm) { ensureAtmosphere().setHasGreatStorm(hasGreatStorm); }

    @JsonIgnore
    public Integer getNumberOfMajorStorms() {
        return atmosphere != null ? atmosphere.getNumberOfMajorStorms() : null;
    }
    public void setNumberOfMajorStorms(Integer numberOfMajorStorms) { ensureAtmosphere().setNumberOfMajorStorms(numberOfMajorStorms); }

    @JsonIgnore
    public String getAtmosphericConvectionLevel() {
        return atmosphere != null ? atmosphere.getAtmosphericConvectionLevel() : null;
    }
    public void setAtmosphericConvectionLevel(String atmosphericConvectionLevel) { ensureAtmosphere().setAtmosphericConvectionLevel(atmosphericConvectionLevel); }

    // ── Composition Convenience Getters/Setters ──

    private CompositionProperties ensureCompositionProperties() {
        if (compositionProperties == null) compositionProperties = new CompositionProperties();
        return compositionProperties;
    }

    @JsonIgnore
    public String getCoreType() {
        return compositionProperties != null ? compositionProperties.getCoreType() : null;
    }
    public void setCoreType(String coreType) { ensureCompositionProperties().setCoreType(coreType); }

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

    // ── Water Convenience Getters (delegate to water object) ──

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
    public String getAtmosphereClassification() {
        return atmosphere != null ? atmosphere.getClassification() : null;
    }

    @JsonIgnore
    public String getAtmosphereComposition() {
        return atmosphere != null ? atmosphere.getCompositionSummary() : null;
    }

    @JsonIgnore
    public Double getSurfacePressure() {
        return atmosphere != null ? atmosphere.getSurfacePressureBar() : null;
    }

    // ── Magnetic Field Convenience Getter ──

    @JsonIgnore
    public Double getMagneticFieldStrength() {
        return magneticField != null ? magneticField.getStrengthComparedToEarth() : null;
    }

    // ── Moons Convenience Getter ──

    @JsonIgnore
    public Integer getNumberOfMoons() {
        int count = (moons != null ? moons.size() : 0);
        int moonlets = (additionalMoonlets != null ? additionalMoonlets : 0);
        return count + moonlets;
    }

    // ── Rings Convenience Getter ──

    @JsonIgnore
    public Boolean getHasRings() {
        return bands != null && !bands.isEmpty();
    }
}
