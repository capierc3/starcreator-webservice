package com.brickroad.starcreator_webservice.model.moons;

import com.brickroad.starcreator_webservice.model.CelestialBody;
import com.brickroad.starcreator_webservice.model.planets.Planet;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "moon", schema = "ud")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Moon extends CelestialBody {

    // Parent relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planet_id", nullable = false)
    @JsonBackReference
    private Planet planet;

    // Moon classification
    @Column(name = "moon_type", length = 50, nullable = false)
    private String moonType; // Regular, Irregular, Captured, Shepherd, etc.

    @Column(name = "formation_type", length = 50)
    private String formationType; // CO_FORMED, CAPTURED, COLLISION_DEBRIS

    // Physical properties
    @Column(name = "earth_mass")
    private Double earthMass;

    @Column(name = "earth_radius")
    private Double earthRadius;

    @Column(name = "density")
    private Double density; // g/cm³

    @Column(name = "composition_type", length = 50)
    private String compositionType; // ICY, ROCKY, MIXED

    @Column(name = "albedo")
    private Double albedo;

    // Orbital properties (relative to parent planet)
    @Column(name = "semi_major_axis_km")
    private Double semiMajorAxisKm; // Distance from planet center

    @Column(name = "orbital_period_days", nullable = false)
    private Double orbitalPeriodDays;

    @Column(name = "eccentricity")
    private Double eccentricity;

    @Column(name = "orbital_inclination_degrees")
    private Double orbitalInclinationDegrees; // Relative to planet's equatorial plane

    // Tidal properties
    @Column(name = "tidally_locked", nullable = false)
    private Boolean tidallyLocked = true; // Most moons are tidally locked

    @Column(name = "tidal_heating_level", length = 20)
    private String tidalHeatingLevel; // NONE, LOW, MODERATE, HIGH, EXTREME

    @Column(name = "tidal_heating_watt_per_m2")
    private Double tidalHeatingWattPerM2;

    // Rotation
    @Column(name = "rotation_period_hours")
    private Double rotationPeriodHours;

    @Column(name = "axial_tilt")
    private Double axialTilt; // Degrees

    // Surface properties
    @Column(name = "surface_temp")
    private Double surfaceTemp; // Kelvin

    @Column(name = "surface_gravity")
    private Double surfaceGravity; // m/s²

    @Column(name = "escape_velocity")
    private Double escapeVelocity; // km/s

    // Geological properties
    @Column(name = "geological_activity", length = 50)
    private String geologicalActivity; // NONE, LOW, MODERATE, HIGH

    @Column(name = "has_cryovolcanism", nullable = false)
    private Boolean hasCryovolcanism = false;

    @Column(name = "has_subsurface_ocean", nullable = false)
    private Boolean hasSubsurfaceOcean = false;

    @Column(name = "ocean_depth_km")
    private Double oceanDepthKm;

    @Column(name = "ice_shell_thickness_km")
    private Double iceShellThicknessKm;

    // Atmosphere (most moons have none or very thin)
    @Column(name = "has_atmosphere", nullable = false)
    private Boolean hasAtmosphere = false;

    @Column(name = "surface_pressure")
    private Double surfacePressure; // Earth atmospheres

    @Column(name = "atmosphere_composition", length = 500)
    private String atmosphereComposition;

    // Surface features
    @Column(name = "cratering_level", length = 30)
    private String crateringLevel; // MINIMAL, LIGHT, MODERATE, HEAVY, EXTREME

    @Column(name = "estimated_visible_craters")
    private Integer estimatedVisibleCraters;

    @Column(name = "surface_features", columnDefinition = "TEXT")
    private String surfaceFeatures; // JSON or comma-separated

    // Ring interaction (for shepherd moons)
    @Column(name = "is_shepherd_moon", nullable = false)
    private Boolean isShepherdMoon = false;

    @Column(name = "shepherds_ring_name", length = 50)
    private String shepherdsRingName;

    // Stability constraints
    @Column(name = "hill_sphere_radius_km")
    private Double hillSphereRadiusKm;

    @Column(name = "roche_limit_km")
    private Double rocheLimitKm;

    @Column(name = "orbit_stability", length = 20)
    private String orbitStability; // STABLE, MARGINALLY_STABLE, UNSTABLE

    // Age
    @Column(name = "age_my")
    private Double ageMY; // Million years
}