package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "planet", schema = "ud")
public class Planet extends CelestialBody {

    @Column(name = "planet_type")
    private String planetType;

    @Column(name = "earth_mass")
    private Double earthMass;

    @Column(name = "earth_radius")
    private Double earthRadius;

    @Column(name = "orbital_period_days")
    private Double orbitalPeriodDays;

    @Column(name = "semi_major_axis_au")
    private Double semiMajorAxisAU;

    @Column(name = "eccentricity")
    private Double eccentricity;

    @Column(name = "orbital_inclination_degrees")
    private Double orbitalInclinationDegrees;

    @Column(name = "surface_temp_kelvin")
    private Double surfaceTemp;

    @Column(name = "surface_pressure_atm")
    private Double surfacePressure;

    @Column(name = "escape_velocity_km_s")
    private Double escapeVelocity;

    @Column(name = "surface_gravity_g")
    private Double surfaceGravity;

    @Column(name = "rotation_period_hours")
    private Double rotationPeriodHours;

    @Column(name = "axial_tilt_degrees")
    private Double axialTilt;

    @Column(name = "magnetic_field_strength")
    private Double magneticFieldStrength;

    @Column(name = "atmosphere_composition")
    private String atmosphereComposition;

    @Column(name = "has_rings")
    private Boolean hasRings;

    @Column(name = "number_of_moons")
    private Integer numberOfMoons;

    @Column(name = "albedo")
    private Double albedo;

    @Column(name = "density_g_cm3")
    private Double density;

    @Column(name = "is_tidally_locked")
    private Boolean isTidallyLocked;

    @Column(name = "habitable_zone_position")
    private String habitableZonePosition;

    @Column(name = "orbit_stability", length = 20)
    private String orbitStability;

    @Column(name = "orbit_stability_timescale_my")
    private Double orbitStabilityTimescaleMy;

    @Column(name = "orbit_crossing_neighbor")
    private String orbitCrossingNeighbor;

    @Column(name = "water_coverage_percent")
    private Double waterCoveragePercent;

    @Column(name = "water_inventory", length = 30)
    private String waterInventory;

    @Column(name = "liquid_water_coverage_percent")
    private Double liquidWaterCoveragePercent;

    @Column(name = "ice_coverage_percent")
    private Double iceCoveragePercent;

    @Column(name = "has_subsurface_water")
    private Boolean hasSubsurfaceWater;

    @Column(name = "subsurface_water_depth_km")
    private Double subsurfaceWaterDepthKm;

    @Column(name = "core_type")
    private String coreType;

    @Column(name = "geological_activity")
    private String geologicalActivity;

    @Column(name = "activity_score")
    private Double activityScore;

    @Column(name = "has_plate_tectonics")
    private Boolean hasPlateTectonics;

    @Column(name = "number_of_tectonic_plates")
    private Integer numberOfTectonicPlates;

    @Column(name = "tectonic_activity_level", length = 50)
    private String tectonicActivityLevel;

    @Column(name = "has_volcanic_activity")
    private Boolean hasVolcanicActivity;

    @Column(name = "volcanism_type", length = 50)
    private String volcanismType;

    @Column(name = "estimated_active_volcanoes")
    private Integer estimatedActiveVolcanoes;

    @Column(name = "volcanic_intensity", length = 50)
    private String volcanicIntensity;

    @Column(name = "mountain_coverage_percent")
    private Double mountainCoveragePercent;

    @Column(name = "average_elevation_km")
    private Double averageElevationKm;

    @Column(name = "max_elevation_km")
    private Double maxElevationKm;

    @Column(name = "min_elevation_km")
    private Double minElevationKm;

    @Column(name = "terrain_roughness")
    private Double terrainRoughness;

    @Column(name = "cratering_level", length = 50)
    private String crateringLevel;

    @Column(name = "estimated_visible_craters")
    private Integer estimatedVisibleCraters;

    @Column(name = "erosion_level", length = 50)
    private String erosionLevel;

    @Column(name = "primary_erosion_agent", length = 50)
    private String primaryErosionAgent;

    @Column(name = "has_great_storm")
    private Boolean hasGreatStorm;

    @Column(name = "number_of_major_storms")
    private Integer numberOfMajorStorms;

    @Column(name = "atmospheric_convection_level", length = 50)
    private String atmosphericConvectionLevel;

    @OneToMany(mappedBy = "planet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<PlanetaryTerrainDistribution> terrainDistribution = new ArrayList<>();

    @Column(name = "age_millions_years")
    private Double ageMY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "star_id")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
    @JsonIdentityReference(alwaysAsId = true)
    private Star parentStar;

    @Column(name = "atmosphere_classification", length = 50)
    private String atmosphereClassification;

    @Column(name = "interior_composition", length = 500)
    private String interiorComposition;

    @Column(name = "envelope_composition", length = 500)
    private String envelopeComposition;

    @Column(name = "composition_classification", length = 50)
    private String compositionClassification;

    @Transient
    @JsonProperty("magneticField")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PlanetaryMagneticField magneticField;

    @Transient
    @JsonProperty("habitability")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PlanetaryHabitability habitability;

    @Transient
    @JsonProperty("weather")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PlanetaryWeather weather;

    @OneToMany(mappedBy = "planet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Moon> moons = new ArrayList<>();

    @Column(name = "additional_moonlets")
    private Integer additionalMoonlets;

    @OneToMany(mappedBy = "planet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Ring> rings = new ArrayList<>();



    public Planet() {}

    // Getters and Setters

    public Boolean getTidallyLocked() {
        return isTidallyLocked;
    }

    public void setTidallyLocked(Boolean tidallyLocked) {
        isTidallyLocked = tidallyLocked;
    }

    public Integer getOrbitalPosition() {
        return getOrbitalOrder();
    }

    public void setOrbitalPosition(Integer position) {
        setOrbitalOrder(position);
    }
}
