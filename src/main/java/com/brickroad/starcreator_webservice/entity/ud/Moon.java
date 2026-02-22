package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "moon", schema = "ud")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "A natural satellite orbiting a planet")
public class Moon extends CelestialBody {

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

    // ── Physical Properties ──

    @Column(name = "earth_mass")
    @Schema(description = "Mass in Earth masses", example = "0.012")
    private Double earthMass;

    @Column(name = "earth_radius")
    @Schema(description = "Radius in Earth radii", example = "0.27")
    private Double earthRadius;

    @Column(name = "density")
    @Schema(description = "Bulk density in g/cm\u00b3", example = "3.34")
    private Double density;

    @Column(name = "albedo")
    @Schema(description = "Surface albedo", example = "0.12")
    private Double albedo;

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
    @Schema(description = "Tidal heating flux in W/m\u00b2")
    private Double tidalHeatingWattPerM2;

    // ── Surface ──

    @Column(name = "surface_temp")
    @Schema(description = "Surface temperature in Kelvin", example = "110")
    private Double surfaceTemp;

    @Column(name = "surface_gravity")
    @Schema(description = "Surface gravity in m/s\u00b2", example = "1.62")
    private Double surfaceGravity;

    @Column(name = "escape_velocity")
    @Schema(description = "Escape velocity in km/s", example = "2.38")
    private Double escapeVelocity;

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

    @Column(name = "has_atmosphere", nullable = false)
    @Schema(description = "Whether the moon has a significant atmosphere")
    private Boolean hasAtmosphere = false;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "atmosphere_id")
    @Schema(description = "Detailed atmosphere data")
    private Atmosphere atmosphere;

    @Column(name = "surface_pressure")
    @Schema(description = "Surface atmospheric pressure in atm")
    private Double surfacePressure;

    @Column(name = "atmosphere_composition", length = 500)
    @Schema(description = "Atmosphere composition summary")
    private String atmosphereComposition;

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

    // ── Computed Properties (not persisted) ──

    @Transient
    @JsonProperty("magneticField")
    @Schema(description = "Magnetic field properties")
    private PlanetaryMagneticField magneticField;

    @Transient
    @JsonProperty("habitability")
    @Schema(description = "Habitability assessment")
    private PlanetaryHabitability habitability;

    @Transient
    @JsonProperty("weather")
    @Schema(description = "Weather and climate data")
    private PlanetaryWeather weather;

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

    // ── Orbital Convenience Getters (delegate to orbit object) ──

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
}
