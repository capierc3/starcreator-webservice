package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ring", schema = "ud")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ring {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Parent relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planet_id", nullable = false)
    @JsonBackReference
    private Planet planet;

    // Ring identification
    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "ring_type", length = 50, nullable = false)
    private String ringType; // MAIN, GOSSAMER, DIFFUSE, NARROW, SHEPHERD

    // Physical properties
    @Column(name = "inner_radius_km", nullable = false)
    private Double innerRadiusKm; // From planet center

    @Column(name = "outer_radius_km", nullable = false)
    private Double outerRadiusKm; // From planet center

    @Column(name = "thickness_km")
    private Double thicknessKm; // Vertical extent

    @Column(name = "mass_kg")
    private Double massKg; // Total ring mass

    @Column(name = "optical_depth")
    private Double opticalDepth; // 0-1+ (higher = more opaque)

    @Column(name = "particle_size_min_m")
    private Double particleSizeMinM; // Minimum particle size in meters

    @Column(name = "particle_size_max_m")
    private Double particleSizeMaxM; // Maximum particle size in meters

    // Composition
    @Column(name = "composition", length = 500)
    private String composition; // e.g., "Water Ice 93%, Silicates 5%, Organics 2%"

    @Column(name = "composition_type", length = 50)
    private String compositionType; // ICY, ROCKY, MIXED

    @Column(name = "albedo")
    private Double albedo;

    // Appearance
    @Column(name = "color", length = 100)
    private String color; // e.g., "White", "Tan", "Reddish-brown"

    @Column(name = "visibility", length = 30)
    private String visibility; // PROMINENT, MODERATE, FAINT, BARELY_VISIBLE

    @Column(name = "has_gaps", nullable = false)
    private Boolean hasGaps = false;

    @Column(name = "gap_description", length = 500)
    private String gapDescription; // Descriptions of major gaps

    // Dynamics
    @Column(name = "has_shepherd_moons", nullable = false)
    private Boolean hasShepherdMoons = false;

    @Column(name = "orbital_resonances", length = 500)
    private String orbitalResonances; // Description of resonances with moons

    @Column(name = "stability", length = 30)
    private String stability; // STABLE, SLOWLY_DISSIPATING, UNSTABLE

    @Column(name = "estimated_age_my")
    private Double estimatedAgeMY; // Age in millions of years

    // Origin
    @Column(name = "origin_type", length = 50)
    private String originType; // PRIMORDIAL, MOON_DISRUPTION, MOON_COLLISION, COMET_DEBRIS

    @Column(name = "formation_description", length = 500)
    private String formationDescription;

    // Timestamps
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        modifiedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedAt = LocalDateTime.now();
    }

    // Calculated properties
    public double getWidthKm() {
        return outerRadiusKm - innerRadiusKm;
    }

    public double getInnerRadiusPlanetRadii(double planetRadiusKm) {
        return innerRadiusKm / planetRadiusKm;
    }

    public double getOuterRadiusPlanetRadii(double planetRadiusKm) {
        return outerRadiusKm / planetRadiusKm;
    }
}
