package com.brickroad.starcreator_webservice.entity.ref;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "ring_template", schema = "ref")
public class RingTemplateRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "ring_type", length = 50, nullable = false)
    private String ringType; // MAIN, GOSSAMER, DIFFUSE, NARROW, SHEPHERD

    // Applicability constraints
    @Column(name = "planet_types", length = 500)
    private String planetTypes; // e.g., "Gas Giant, Ice Giant"

    @Column(name = "min_planet_mass_earth")
    private Double minPlanetMassEarth;

    @Column(name = "max_planet_mass_earth")
    private Double maxPlanetMassEarth;

    @Column(name = "min_distance_au")
    private Double minDistanceAU; // Minimum stellar distance

    @Column(name = "max_distance_au")
    private Double maxDistanceAU; // Maximum stellar distance

    // Ring properties (as ranges for generation)
    @Column(name = "inner_radius_min_planet_radii")
    private Double innerRadiusMinPlanetRadii;

    @Column(name = "inner_radius_max_planet_radii")
    private Double innerRadiusMaxPlanetRadii;

    @Column(name = "outer_radius_min_planet_radii")
    private Double outerRadiusMinPlanetRadii;

    @Column(name = "outer_radius_max_planet_radii")
    private Double outerRadiusMaxPlanetRadii;

    @Column(name = "thickness_min_km")
    private Double thicknessMinKm;

    @Column(name = "thickness_max_km")
    private Double thicknessMaxKm;

    @Column(name = "optical_depth_min")
    private Double opticalDepthMin;

    @Column(name = "optical_depth_max")
    private Double opticalDepthMax;

    @Column(name = "particle_size_min_m")
    private Double particleSizeMinM;

    @Column(name = "particle_size_max_m")
    private Double particleSizeMaxM;

    // Composition
    @Column(name = "composition_type", length = 50)
    private String compositionType; // ICY, ROCKY, MIXED

    @Column(name = "composition_description", length = 500)
    private String compositionDescription; // Template for composition string

    @Column(name = "albedo_min")
    private Double albedoMin;

    @Column(name = "albedo_max")
    private Double albedoMax;

    @Column(name = "color", length = 100)
    private String color;

    @Column(name = "visibility", length = 30)
    private String visibility; // PROMINENT, MODERATE, FAINT

    // Dynamics
    @Column(name = "has_gaps_probability")
    private Double hasGapsProbability; // 0.0 to 1.0

    @Column(name = "has_shepherd_moons_probability")
    private Double hasShepherdMoonsProbability; // 0.0 to 1.0

    @Column(name = "stability", length = 30)
    private String stability;

    @Column(name = "origin_type", length = 50)
    private String originType;

    // Selection weight
    @Column(name = "rarity_weight")
    private Integer rarityWeight = 100; // Higher = more common

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Check if this template matches the planet's characteristics
     */
    public boolean matches(String planetType, double planetMassEarth, double distanceAU) {
        // Check planet type
        boolean typeMatch = planetTypes == null ||
                planetTypes.toLowerCase().contains(planetType.toLowerCase());

        // Check mass range
        boolean massMatch = (minPlanetMassEarth == null || planetMassEarth >= minPlanetMassEarth) &&
                (maxPlanetMassEarth == null || planetMassEarth <= maxPlanetMassEarth);

        // Check distance range
        boolean distanceMatch = (minDistanceAU == null || distanceAU >= minDistanceAU) &&
                (maxDistanceAU == null || distanceAU <= maxDistanceAU);

        return typeMatch && massMatch && distanceMatch;
    }
}
