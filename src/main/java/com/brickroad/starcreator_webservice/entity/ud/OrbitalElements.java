package com.brickroad.starcreator_webservice.entity.ud;

import com.brickroad.starcreator_webservice.enums.DistanceUnit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Reusable Keplerian orbital parameters for any orbiting object or orbital boundary.
 * <p>
 * Used by planets, moons, asteroids, and orbital band edges (belt/ring boundaries).
 * The {@code semiMajorAxisUnit} field distinguishes between AU (star-orbiting) and KM (planet-orbiting).
 */
@Entity
@Table(name = "orbital_elements", schema = "ud")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Keplerian orbital parameters for any orbiting object or orbital boundary")
public class OrbitalElements {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    // ── Core Keplerian Elements ──

    @Column(name = "semi_major_axis", nullable = false)
    @Schema(description = "Semi-major axis in the specified distance unit", example = "1.0")
    private Double semiMajorAxis;

    @Enumerated(EnumType.STRING)
    @Column(name = "semi_major_axis_unit", nullable = false, length = 5)
    @Schema(description = "Distance unit for semi-major axis: AU for star-orbiting bodies, KM for planet-orbiting bodies", example = "AU")
    private DistanceUnit semiMajorAxisUnit;

    @Column(name = "orbital_period_days")
    @Schema(description = "Orbital period in Earth days", example = "365.25")
    private Double orbitalPeriodDays;

    @Column(name = "eccentricity")
    @Schema(description = "Orbital eccentricity (0 = circular, <1 = elliptical)", example = "0.017")
    private Double eccentricity;

    @Column(name = "inclination_degrees")
    @Schema(description = "Orbital inclination in degrees relative to reference plane", example = "1.6")
    private Double inclinationDegrees;

    @Column(name = "longitude_of_ascending_node_deg")
    @Schema(description = "Longitude of ascending node (\u03A9) in degrees — direction of orbital tilt", example = "174.9")
    private Double longitudeOfAscendingNodeDeg;

    @Column(name = "argument_of_periapsis_deg")
    @Schema(description = "Argument of periapsis (\u03C9) in degrees — rotation of ellipse within orbital plane", example = "288.1")
    private Double argumentOfPeriapsisDeg;

    @Column(name = "mean_anomaly_deg")
    @Schema(description = "Mean anomaly at epoch in degrees — initial orbital position", example = "357.5")
    private Double meanAnomalyDeg;

    // ── Stability Assessment ──

    @Column(name = "orbit_stability", length = 20)
    @Schema(description = "Stability classification: STABLE, MARGINAL, CROSSING, or UNSTABLE", example = "STABLE")
    private String orbitStability;

    @Column(name = "orbit_stability_timescale_my")
    @Schema(description = "Estimated timescale to instability in millions of years")
    private Double orbitStabilityTimescaleMy;

    @Column(name = "orbit_crossing_neighbor", length = 100)
    @Schema(description = "Name of the neighboring body involved in orbital instability, if any")
    private String orbitCrossingNeighbor;

    // ── Parent Distance & Order ──

    @Column(name = "distance_from_parent")
    @Schema(description = "Distance from parent body (planet→star in AU, moon→planet in km, star→barycenter in AU)")
    private Double distanceFromParent;

    @Column(name = "orbital_order")
    @Schema(description = "Position in orbital sequence (1 = innermost)")
    private Integer orbitalOrder;

    // ── Gravitational Boundaries (moon orbits only — null for others) ──

    @Column(name = "hill_sphere_radius_km")
    @Schema(description = "Hill sphere radius in km — gravitational sphere of influence")
    private Double hillSphereRadiusKm;

    @Column(name = "roche_limit_km")
    @Schema(description = "Roche limit distance in km — tidal disruption boundary")
    private Double rocheLimitKm;

    // ── Metadata ──

    @Column(name = "label", length = 100)
    @JsonIgnore
    @Schema(hidden = true, description = "Human-readable label for debugging (e.g. 'Planet orbit', 'Belt inner edge')")
    private String label;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    @JsonIgnore
    private LocalDateTime modifiedAt;
}
