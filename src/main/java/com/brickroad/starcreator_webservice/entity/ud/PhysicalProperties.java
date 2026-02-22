package com.brickroad.starcreator_webservice.entity.ud;

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
 * Physical properties for any celestial body (planet, moon, star, or asteroid).
 * <p>
 * Body-type-specific fields are nullable and only populated for the appropriate
 * body type. Planet/moon fields (earthMass, earthRadius, density, etc.) are null
 * for stars; star fields (solarMass, solarRadius, solarLuminosity) are null for
 * planets and moons; {@code dimensionsKm} is asteroid-only.
 * {@code @JsonInclude(NON_NULL)} ensures only relevant fields appear in the JSON output.
 */
@Entity
@Table(name = "physical_properties", schema = "ud")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Physical properties for any celestial body including mass, radius, density, and gravity")
public class PhysicalProperties {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    // ── Internal Computation Fields (hidden from JSON) ──

    @Column(name = "mass")
    @JsonIgnore
    @Schema(hidden = true)
    private double mass;

    @Column(name = "radius")
    @JsonIgnore
    @Schema(hidden = true)
    private double radius;

    @Column(name = "circumference")
    @JsonIgnore
    @Schema(hidden = true)
    private double circumference;

    // ── Planet / Moon Fields ──

    @Column(name = "earth_mass")
    @Schema(description = "Mass in Earth masses (1.0 = Earth)", example = "1.2")
    private Double earthMass;

    @Column(name = "earth_radius")
    @Schema(description = "Radius in Earth radii (1.0 = Earth)", example = "1.1")
    private Double earthRadius;

    @Column(name = "density_g_cm3")
    @Schema(description = "Bulk density in g/cm³", example = "5.51")
    private Double density;

    @Column(name = "surface_gravity")
    @Schema(description = "Surface gravity (in g for planets, m/s² for moons)", example = "0.98")
    private Double surfaceGravity;

    @Column(name = "escape_velocity")
    @Schema(description = "Escape velocity in km/s", example = "11.2")
    private Double escapeVelocity;

    @Column(name = "albedo")
    @Schema(description = "Bond albedo (fraction of energy reflected)", example = "0.30")
    private Double albedo;

    // ── Asteroid Fields ──

    @Column(name = "dimensions_km")
    @Schema(description = "Approximate dimensions for irregularly shaped bodies", example = "965 x 961 x 891")
    private String dimensionsKm;

    // ── Star Fields ──

    @Column(name = "solar_mass")
    @Schema(description = "Mass in solar masses (1.0 = Sun)", example = "0.37")
    private Double solarMass;

    @Column(name = "solar_radius")
    @Schema(description = "Radius in solar radii (1.0 = Sun)", example = "0.51")
    private Double solarRadius;

    @Column(name = "solar_luminosity")
    @Schema(description = "Luminosity in solar luminosities (1.0 = Sun)", example = "0.031")
    private Double solarLuminosity;

    // ── Shared Fields ──

    @Column(name = "surface_temp")
    @Schema(description = "Surface temperature in Kelvin", example = "288")
    private Double surfaceTemp;

    // ── Metadata ──

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    @JsonIgnore
    private LocalDateTime modifiedAt;
}
