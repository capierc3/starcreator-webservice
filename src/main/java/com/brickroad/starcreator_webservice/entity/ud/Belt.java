package com.brickroad.starcreator_webservice.entity.ud;

import com.brickroad.starcreator_webservice.entity.ref.BeltTypeRef;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "belt", schema = "ud")
public class Belt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "star_system_id", nullable = false)
    @JsonBackReference
    private StarSystem starSystem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "belt_type_id", nullable = false)
    private BeltTypeRef beltType;

    @Column(name = "name")
    private String name;

    // Orbital bounds
    @Column(name = "inner_radius_au", nullable = false)
    private Double innerRadiusAu;

    @Column(name = "outer_radius_au", nullable = false)
    private Double outerRadiusAu;

    @Column(name = "peak_density_au")
    private Double peakDensityAu;

    // Physical properties
    @Column(name = "total_mass_kg")
    private Double totalMassKg;

    @Column(name = "total_mass_earth_masses")
    private Double totalMassEarthMasses;

    @Column(name = "estimated_object_count")
    private Long estimatedObjectCount;

    // Orbital characteristics
    @Column(name = "average_eccentricity")
    private Double averageEccentricity;

    @Column(name = "max_eccentricity")
    private Double maxEccentricity;

    @Column(name = "average_inclination_degrees")
    private Double averageInclinationDegrees;

    @Column(name = "max_inclination_degrees")
    private Double maxInclinationDegrees;

    // Composition
    @Column(name = "composition_type")
    private String compositionType;

    @Column(name = "primary_composition")
    private String primaryComposition;

    // Structure
    @Column(name = "has_resonance_gaps")
    private Boolean hasResonanceGaps;

    @Column(name = "gap_description")
    private String gapDescription;

    @Column(name = "has_collisional_families")
    private Boolean hasCollisionalFamilies;

    @Column(name = "family_count")
    private Integer familyCount;

    // Notable asteroids (owned by belt)
    @OneToMany(mappedBy = "belt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @OrderBy("mass DESC")
    private List<Asteroid> notableAsteroids = new ArrayList<>();

    // Dwarf planets in this belt (referenced, not owned)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "belt_dwarf_planet",
        schema = "ud",
        joinColumns = @JoinColumn(name = "belt_id"),
        inverseJoinColumns = @JoinColumn(name = "planet_id")
    )
    private List<Planet> dwarfPlanets = new ArrayList<>();

    @Column(name = "age_my")
    private Double ageMY;

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    public Belt() {}

    // Convenience methods
    public void addNotableAsteroid(Asteroid asteroid) {
        notableAsteroids.add(asteroid);
        asteroid.setBelt(this);
    }

    public void addDwarfPlanet(Planet dwarfPlanet) {
        if (!dwarfPlanets.contains(dwarfPlanet)) {
            dwarfPlanets.add(dwarfPlanet);
        }
    }

    public double getWidthAu() {
        return outerRadiusAu - innerRadiusAu;
    }
}
