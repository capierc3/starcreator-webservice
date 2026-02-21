package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "planet", schema = "ud")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "A planet orbiting a star within a star system")
public class Planet extends CelestialBody {

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

    // ── Physical Properties ──

    @Column(name = "earth_mass")
    @Schema(description = "Mass in Earth masses (1.0 = Earth)", example = "1.2")
    private Double earthMass;

    @Column(name = "earth_radius")
    @Schema(description = "Radius in Earth radii (1.0 = Earth)", example = "1.1")
    private Double earthRadius;

    @Column(name = "density_g_cm3")
    @Schema(description = "Bulk density in g/cm\u00b3", example = "5.51")
    private Double density;

    @Column(name = "surface_gravity_g")
    @Schema(description = "Surface gravity in g (1.0 = Earth)", example = "0.98")
    private Double surfaceGravity;

    @Column(name = "escape_velocity_km_s")
    @Schema(description = "Escape velocity in km/s", example = "11.2")
    private Double escapeVelocity;

    @Column(name = "albedo")
    @Schema(description = "Bond albedo (fraction of energy reflected)", example = "0.30")
    private Double albedo;

    // ── Orbital Properties ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "orbital_elements_id")
    @Schema(description = "Keplerian orbital elements for this planet's orbit around its star")
    private OrbitalElements orbit;

    // ── Rotation ──

    @Column(name = "rotation_period_hours")
    @Schema(description = "Sidereal rotation period in hours (negative = retrograde)", example = "24.0")
    private Double rotationPeriodHours;

    @Column(name = "axial_tilt_degrees")
    @Schema(description = "Axial tilt in degrees", example = "23.4")
    private Double axialTilt;

    @Column(name = "is_tidally_locked")
    @JsonIgnore
    private Boolean isTidallyLocked;

    // ── Surface & Temperature ──

    @Column(name = "surface_temp_kelvin")
    @Schema(description = "Equilibrium surface temperature in Kelvin", example = "288")
    private Double surfaceTemp;

    @Column(name = "surface_pressure_atm")
    @Schema(description = "Surface atmospheric pressure in atm (1.0 = Earth sea level)", example = "1.0")
    private Double surfacePressure;

    // ── Atmosphere ──

    @Column(name = "atmosphere_composition")
    @Schema(description = "Atmospheric composition summary", example = "N2 78%, O2 21%, Ar 0.93%, CO2 (trace)")
    private String atmosphereComposition;

    @Column(name = "atmosphere_classification", length = 50)
    @Schema(description = "Atmosphere classification", example = "EARTH_LIKE")
    private String atmosphereClassification;

    // ── Composition ──

    @Column(name = "core_type")
    @Schema(description = "Core composition type", example = "Iron-Nickel")
    private String coreType;

    @Column(name = "interior_composition", length = 500)
    @Schema(description = "Interior composition breakdown")
    private String interiorComposition;

    @Column(name = "envelope_composition", length = 500)
    @Schema(description = "Envelope/crust composition breakdown")
    private String envelopeComposition;

    @Column(name = "composition_classification", length = 50)
    @Schema(description = "Overall composition classification", example = "SILICATE")
    private String compositionClassification;

    // ── Water ──

    @Column(name = "water_coverage_percent")
    @Schema(description = "Total water coverage as percentage of surface", example = "71.0")
    private Double waterCoveragePercent;

    @Column(name = "water_inventory", length = 30)
    @Schema(description = "Relative water inventory classification", example = "EARTH_LIKE")
    private String waterInventory;

    @Column(name = "liquid_water_coverage_percent")
    @Schema(description = "Liquid water coverage as percentage of surface", example = "65.0")
    private Double liquidWaterCoveragePercent;

    @Column(name = "ice_coverage_percent")
    @Schema(description = "Ice coverage as percentage of surface", example = "6.0")
    private Double iceCoveragePercent;

    @Column(name = "has_subsurface_water")
    @Schema(description = "Whether subsurface liquid water is present")
    private Boolean hasSubsurfaceWater;

    @Column(name = "subsurface_water_depth_km")
    @Schema(description = "Depth to subsurface water layer in km")
    private Double subsurfaceWaterDepthKm;

    // ── Geology ──

    @Column(name = "geological_activity")
    @Schema(description = "Overall geological activity level", example = "ACTIVE")
    private String geologicalActivity;

    @Column(name = "activity_score")
    @Schema(description = "Numerical geological activity score")
    private Double activityScore;

    @Column(name = "has_plate_tectonics")
    @Schema(description = "Whether the planet has active plate tectonics")
    private Boolean hasPlateTectonics;

    @Column(name = "number_of_tectonic_plates")
    @Schema(description = "Number of major tectonic plates")
    private Integer numberOfTectonicPlates;

    @Column(name = "tectonic_activity_level", length = 50)
    @Schema(description = "Tectonic activity classification", example = "MODERATE")
    private String tectonicActivityLevel;

    @Column(name = "has_volcanic_activity")
    @Schema(description = "Whether the planet has active volcanism")
    private Boolean hasVolcanicActivity;

    @Column(name = "volcanism_type", length = 50)
    @Schema(description = "Primary type of volcanism", example = "SILICATE")
    private String volcanismType;

    @Column(name = "estimated_active_volcanoes")
    @Schema(description = "Estimated number of active volcanoes")
    private Integer estimatedActiveVolcanoes;

    @Column(name = "volcanic_intensity", length = 50)
    @Schema(description = "Volcanic activity intensity", example = "MODERATE")
    private String volcanicIntensity;

    // ── Terrain ──

    @Column(name = "mountain_coverage_percent")
    @Schema(description = "Percentage of surface covered by mountains")
    private Double mountainCoveragePercent;

    @Column(name = "average_elevation_km")
    @Schema(description = "Average surface elevation in km")
    private Double averageElevationKm;

    @Column(name = "max_elevation_km")
    @Schema(description = "Maximum surface elevation in km")
    private Double maxElevationKm;

    @Column(name = "min_elevation_km")
    @Schema(description = "Minimum surface elevation (deepest point) in km")
    private Double minElevationKm;

    @Column(name = "terrain_roughness")
    @Schema(description = "Terrain roughness index")
    private Double terrainRoughness;

    @Column(name = "cratering_level", length = 50)
    @Schema(description = "Impact cratering level", example = "MODERATE")
    private String crateringLevel;

    @Column(name = "estimated_visible_craters")
    @Schema(description = "Estimated number of major visible impact craters")
    private Integer estimatedVisibleCraters;

    @Column(name = "erosion_level", length = 50)
    @Schema(description = "Surface erosion level", example = "HIGH")
    private String erosionLevel;

    @Column(name = "primary_erosion_agent", length = 50)
    @Schema(description = "Primary agent of surface erosion", example = "WATER")
    private String primaryErosionAgent;

    @OneToMany(mappedBy = "planet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    @Schema(description = "Terrain type distribution across the surface")
    private List<PlanetaryTerrainDistribution> terrainDistribution = new ArrayList<>();

    // ── Storms ──

    @Column(name = "has_great_storm")
    @Schema(description = "Whether the planet has a persistent great storm (like Jupiter's Red Spot)")
    private Boolean hasGreatStorm;

    @Column(name = "number_of_major_storms")
    @Schema(description = "Number of persistent major storm systems")
    private Integer numberOfMajorStorms;

    @Column(name = "atmospheric_convection_level", length = 50)
    @Schema(description = "Atmospheric convection intensity", example = "VIGOROUS")
    private String atmosphericConvectionLevel;

    // ── Magnetic Field ──

    @Column(name = "magnetic_field_strength")
    @Schema(description = "Magnetic field strength relative to Earth (1.0 = Earth)", example = "0.60")
    private Double magneticFieldStrength;

    // ── Moons & Bands ──

    @Column(name = "has_rings")
    @Schema(description = "Whether this planet has a ring system")
    private Boolean hasRings;

    @Column(name = "number_of_moons")
    @Schema(description = "Total number of moons", example = "4")
    private Integer numberOfMoons;

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

    // ── Computed Properties (not persisted) ──

    @Transient
    @JsonProperty("magneticField")
    @Schema(description = "Detailed magnetic field properties")
    private PlanetaryMagneticField magneticField;

    @Transient
    @JsonProperty("habitability")
    @Schema(description = "Habitability assessment including ESI score and risk factors")
    private PlanetaryHabitability habitability;

    @Transient
    @JsonProperty("weather")
    @Schema(description = "Weather and climate simulation data")
    private PlanetaryWeather weather;

    // ── JSON Accessors ──

    @JsonProperty("tidallyLocked")
    @Schema(description = "Whether the planet is tidally locked to its star", example = "false")
    public Boolean getTidallyLocked() {
        return isTidallyLocked;
    }

    public void setTidallyLocked(Boolean tidallyLocked) {
        isTidallyLocked = tidallyLocked;
    }

    @JsonIgnore
    public Integer getOrbitalPosition() {
        return getOrbitalOrder();
    }

    public void setOrbitalPosition(Integer position) {
        setOrbitalOrder(position);
    }

    // ── Orbital Convenience Getters (delegate to orbit object) ──
    // These reduce churn in existing code that reads orbital properties directly.

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
}
