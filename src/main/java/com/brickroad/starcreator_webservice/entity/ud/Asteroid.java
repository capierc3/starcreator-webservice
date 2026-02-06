package com.brickroad.starcreator_webservice.entity.ud;

import com.brickroad.starcreator_webservice.entity.ref.AsteroidTypeRef;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "asteroid", schema = "ud")
public class Asteroid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "belt_id")
    @JsonBackReference
    private Belt belt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asteroid_type_id", nullable = false)
    private AsteroidTypeRef asteroidType;

    // Identity
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "designation_code")
    private String designationCode;

    // Physical properties
    @Column(name = "mass")
    private Double mass;

    @Column(name = "radius")
    private Double radius;

    @Column(name = "dimensions_km")
    private String dimensionsKm;

    @Column(name = "circumference")
    private Double circumference;

    @Column(name = "earth_mass")
    private Double earthMass;

    @Column(name = "density")
    private Double density;

    @Column(name = "albedo")
    private Double albedo;

    // Orbital elements
    @Column(name = "semi_major_axis_au", nullable = false)
    private Double semiMajorAxisAu;

    @Column(name = "eccentricity")
    private Double eccentricity;

    @Column(name = "inclination_degrees")
    private Double inclinationDegrees;

    @Column(name = "orbital_period_days")
    private Double orbitalPeriodDays;

    // Rotation
    @Column(name = "rotation_period_hours")
    private Double rotationPeriodHours;

    @Column(name = "axial_tilt")
    private Double axialTilt;

    // Surface
    @Column(name = "surface_temp")
    private Double surfaceTemp;

    @Column(name = "surface_gravity")
    private Double surfaceGravity;

    @Column(name = "escape_velocity")
    private Double escapeVelocity;

    @Column(name = "surface_features")
    private String surfaceFeatures;

    @Column(name = "cratering_level")
    private String crateringLevel;

    @Column(name = "has_regolith")
    private Boolean hasRegolith;

    @Column(name = "regolith_depth_m")
    private Double regolithDepthM;

    // Composition
    @Column(name = "composition")
    private String composition;

    @Column(name = "is_differentiated")
    private Boolean isDifferentiated;

    @Column(name = "core_type")
    private String coreType;

    // Moons
    @Column(name = "has_moon")
    private Boolean hasMoon;

    @Column(name = "moon_count")
    private Integer moonCount;

    @Column(name = "moon_description")
    private String moonDescription;

    // Notable status
    @Column(name = "is_notable")
    private Boolean isNotable;

    @Column(name = "notable_reason")
    private String notableReason;

    // Metadata
    @Column(name = "age_my")
    private Double ageMY;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    public Asteroid() {}
}
