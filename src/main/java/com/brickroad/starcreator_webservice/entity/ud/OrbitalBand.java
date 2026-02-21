package com.brickroad.starcreator_webservice.entity.ud;

import com.brickroad.starcreator_webservice.entity.ref.BeltTypeRef;
import com.brickroad.starcreator_webservice.enums.BandCategory;
import com.brickroad.starcreator_webservice.enums.DistanceUnit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * An annular band of material orbiting a star (belt) or planet (ring).
 * <p>
 * Unifies the concepts of "asteroid belt" and "planetary ring" into a single model.
 * Each band has two {@link OrbitalElements} references defining its inner and outer
 * orbital boundaries. The inner edge orbits faster than the outer edge per Kepler's laws.
 * <p>
 * A belt orbits a star (star_system_id is set, planet_id is null).
 * A ring orbits a planet (planet_id is set, star_system_id is null).
 */
@Entity
@Table(name = "orbital_band", schema = "ud")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "An annular band of material orbiting a star (belt) or planet (ring)")
public class OrbitalBand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier")
    private Long id;

    // ── Parent (exactly one is non-null) ──

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "star_system_id")
    @JsonBackReference("system-bands")
    private StarSystem starSystem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planet_id")
    @JsonBackReference("planet-bands")
    private Planet planet;

    // ── Type Discrimination ──

    @Enumerated(EnumType.STRING)
    @Column(name = "band_category", nullable = false, length = 20)
    @Schema(description = "High-level category: BELT (orbits a star) or RING (orbits a planet)", example = "BELT")
    private BandCategory bandCategory;

    @Column(name = "band_type", nullable = false, length = 50)
    @Schema(description = "Specific band type classification", example = "INNER_ROCKY")
    private String bandType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "belt_type_id")
    @Schema(description = "Belt type reference data (belts only)")
    private BeltTypeRef beltType;

    // ── Orbital Boundaries ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "inner_orbit_id", nullable = false)
    @Schema(description = "Orbital elements for the inner edge of the band")
    private OrbitalElements innerOrbit;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "outer_orbit_id", nullable = false)
    @Schema(description = "Orbital elements for the outer edge of the band")
    private OrbitalElements outerOrbit;

    // ── Identity ──

    @Column(name = "name", length = 100)
    @Schema(description = "Band designation", example = "SCS-V01-8RQ KB-01")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    @Schema(description = "Description of the band")
    private String description;

    @Column(name = "age_my")
    @Schema(description = "Age in millions of years")
    private Double ageMY;

    // ── Physical Properties (shared) ──

    @Column(name = "total_mass_kg")
    @Schema(description = "Total band mass in kg")
    private Double totalMassKg;

    @Column(name = "total_mass_earth_masses")
    @Schema(description = "Total band mass in Earth masses", example = "0.049")
    private Double totalMassEarthMasses;

    @Column(name = "composition_type", length = 50)
    @Schema(description = "Primary composition type", example = "ICY")
    private String compositionType;

    @Column(name = "primary_composition", columnDefinition = "TEXT")
    @Schema(description = "Detailed composition breakdown", example = "Water Ice 50%, Rock 30%, Organics 20%")
    private String primaryComposition;

    @Column(name = "albedo")
    @Schema(description = "Particle/surface albedo", example = "0.5")
    private Double albedo;

    // ── Aggregate Orbital Stats (for belts with many objects) ──

    @Column(name = "peak_density_distance")
    @Schema(description = "Distance of peak object density, in same unit as orbital boundaries", example = "3.1")
    private Double peakDensityDistance;

    @Column(name = "average_eccentricity")
    @Schema(description = "Average orbital eccentricity of objects within the band")
    private Double averageEccentricity;

    @Column(name = "max_eccentricity")
    @Schema(description = "Maximum eccentricity among band objects")
    private Double maxEccentricity;

    @Column(name = "average_inclination_deg")
    @Schema(description = "Average orbital inclination of objects within the band in degrees")
    private Double averageInclinationDeg;

    @Column(name = "max_inclination_deg")
    @Schema(description = "Maximum inclination among band objects in degrees")
    private Double maxInclinationDeg;

    @Column(name = "estimated_object_count")
    @Schema(description = "Estimated number of objects in the band")
    private Long estimatedObjectCount;

    // ── Structure (shared) ──

    @Column(name = "has_gaps")
    @Schema(description = "Whether the band has visible gaps")
    private Boolean hasGaps;

    @Column(name = "gap_description", columnDefinition = "TEXT")
    @Schema(description = "Description of gaps within the band")
    private String gapDescription;

    // ── Belt-Specific ──

    @Column(name = "has_resonance_gaps")
    @Schema(description = "Whether the belt has Kirkwood-style resonance gaps (belts only)")
    private Boolean hasResonanceGaps;

    @Column(name = "has_collisional_families")
    @Schema(description = "Whether the belt contains collisional asteroid families (belts only)")
    private Boolean hasCollisionalFamilies;

    @Column(name = "family_count")
    @Schema(description = "Number of identified collisional families (belts only)")
    private Integer familyCount;

    // ── Ring-Specific ──

    @Column(name = "thickness_km")
    @Schema(description = "Vertical thickness of the ring in km (rings only)", example = "0.01")
    private Double thicknessKm;

    @Column(name = "optical_depth")
    @Schema(description = "Optical depth: 0 = transparent, >1 = opaque (rings only)", example = "0.5")
    private Double opticalDepth;

    @Column(name = "particle_size_min_m")
    @Schema(description = "Minimum particle size in meters (rings only)")
    private Double particleSizeMinM;

    @Column(name = "particle_size_max_m")
    @Schema(description = "Maximum particle size in meters (rings only)")
    private Double particleSizeMaxM;

    @Column(name = "color", length = 100)
    @Schema(description = "Visual color of the ring (rings only)", example = "White")
    private String color;

    @Column(name = "visibility", length = 30)
    @Schema(description = "Visual prominence (rings only)", example = "PROMINENT")
    private String visibility;

    @Column(name = "has_shepherd_moons")
    @Schema(description = "Whether shepherd moons confine this ring (rings only)")
    private Boolean hasShepherdMoons;

    @Column(name = "orbital_resonances", columnDefinition = "TEXT")
    @Schema(description = "Description of resonances with moons (rings only)")
    private String orbitalResonances;

    @Column(name = "stability", length = 30)
    @Schema(description = "Stability assessment (rings only)", example = "STABLE")
    private String stability;

    @Column(name = "origin_type", length = 50)
    @Schema(description = "Formation origin (rings only)", example = "PRIMORDIAL")
    private String originType;

    @Column(name = "formation_description", columnDefinition = "TEXT")
    @Schema(description = "Description of how the ring formed (rings only)")
    private String formationDescription;

    @Column(name = "estimated_age_my")
    @Schema(description = "Estimated age in millions of years (rings only)")
    private Double estimatedAgeMY;

    // ── Notable Objects (belt-specific children) ──

    @OneToMany(mappedBy = "band", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("band-asteroids")
    @OrderBy("mass DESC")
    @Schema(description = "Notable asteroids within this band (belts only)")
    private List<Asteroid> notableAsteroids = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "band_dwarf_planet",
        schema = "ud",
        joinColumns = @JoinColumn(name = "band_id"),
        inverseJoinColumns = @JoinColumn(name = "planet_id")
    )
    @Schema(description = "Dwarf planets residing within this band (belts only)")
    private List<Planet> dwarfPlanets = new ArrayList<>();

    // ── Timestamps ──

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    @JsonIgnore
    private LocalDateTime modifiedAt;

    // ── Computed Properties ──

    @Schema(description = "Width of the band in its native distance unit")
    public Double getWidth() {
        if (innerOrbit == null || outerOrbit == null) return null;
        return outerOrbit.getSemiMajorAxis() - innerOrbit.getSemiMajorAxis();
    }

    @Schema(description = "Distance unit used by this band (AU for belts, KM for rings)")
    public DistanceUnit getDistanceUnit() {
        if (innerOrbit != null) return innerOrbit.getSemiMajorAxisUnit();
        return null;
    }

    // ── Convenience Methods ──

    public void addNotableAsteroid(Asteroid asteroid) {
        notableAsteroids.add(asteroid);
        asteroid.setBand(this);
    }

    public void addDwarfPlanet(Planet dwarfPlanet) {
        if (!dwarfPlanets.contains(dwarfPlanet)) {
            dwarfPlanets.add(dwarfPlanet);
        }
    }
}
