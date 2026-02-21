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

    @Column(name = "cratering_level", length = 30)
    @Schema(description = "Impact cratering level", example = "HEAVY")
    private String crateringLevel;

    @Column(name = "estimated_visible_craters")
    @Schema(description = "Estimated number of major visible craters")
    private Integer estimatedVisibleCraters;

    @Column(name = "surface_features", columnDefinition = "TEXT")
    @Schema(description = "Notable surface features")
    private String surfaceFeatures;

    // ── Geology ──

    @Column(name = "geological_activity", length = 50)
    @Schema(description = "Geological activity level", example = "LOW")
    private String geologicalActivity;

    @Column(name = "has_cryovolcanism", nullable = false)
    @Schema(description = "Whether the moon has cryovolcanic activity")
    private Boolean hasCryovolcanism = false;

    @Column(name = "volcanism_type", length = 50)
    @Schema(description = "Primary volcanism type")
    private String volcanismType;

    @Column(name = "volcanic_intensity", length = 30)
    @Schema(description = "Volcanic activity intensity")
    private String volcanicIntensity;

    @Column(name = "estimated_active_volcanoes")
    @Schema(description = "Estimated number of active volcanoes")
    private Integer estimatedActiveVolcanoes;

    // ── Terrain ──

    @Column(name = "mountain_coverage_percent")
    @Schema(description = "Mountain coverage percentage")
    private Double mountainCoveragePercent;

    @Column(name = "average_elevation_km")
    @Schema(description = "Average surface elevation in km")
    private Double averageElevationKm;

    @Column(name = "max_elevation_km")
    @Schema(description = "Maximum elevation in km")
    private Double maxElevationKm;

    @Column(name = "min_elevation_km")
    @Schema(description = "Minimum elevation in km")
    private Double minElevationKm;

    @Column(name = "terrain_roughness")
    @Schema(description = "Terrain roughness index")
    private Double terrainRoughness;

    @Column(name = "erosion_level", length = 30)
    @Schema(description = "Surface erosion level")
    private String erosionLevel;

    @Column(name = "primary_erosion_agent", length = 50)
    @Schema(description = "Primary erosion agent")
    private String primaryErosionAgent;

    // ── Water & Ocean ──

    @Column(name = "has_subsurface_ocean", nullable = false)
    @Schema(description = "Whether the moon has a subsurface liquid water ocean")
    private Boolean hasSubsurfaceOcean = false;

    @Column(name = "ocean_depth_km")
    @Schema(description = "Depth of subsurface ocean in km")
    private Double oceanDepthKm;

    @Column(name = "ice_shell_thickness_km")
    @Schema(description = "Thickness of ice shell above subsurface ocean in km")
    private Double iceShellThicknessKm;

    @Column(name = "water_inventory", length = 30)
    @Schema(description = "Relative water inventory")
    private String waterInventory;

    @Column(name = "water_coverage_percent")
    @Schema(description = "Water coverage percentage")
    private Double waterCoveragePercent;

    @Column(name = "liquid_water_coverage_percent")
    @Schema(description = "Liquid water coverage percentage")
    private Double liquidWaterCoveragePercent;

    @Column(name = "ice_coverage_percent")
    @Schema(description = "Ice coverage percentage")
    private Double iceCoveragePercent;

    @Column(name = "has_subsurface_water")
    private Boolean hasSubsurfaceWater = false;

    @Column(name = "subsurface_water_depth_km")
    private Double subsurfaceWaterDepthKm;

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
}
