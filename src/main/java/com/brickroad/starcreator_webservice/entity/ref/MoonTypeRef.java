package com.brickroad.starcreator_webservice.entity.ref;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "moon_type_ref", schema = "ref")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoonTypeRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "moon_type", length = 50, nullable = false, unique = true)
    private String moonType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "typical_formation", length = 50)
    private String typicalFormation;

    @Column(name = "min_mass_earth_masses")
    private double minMassEarthMasses;

    @Column(name = "max_mass_earth_masses")
    private double maxMassEarthMasses;

    @Column(name = "typical_composition", length = 50)
    private String typicalComposition;

    @Column(name = "mass_distribution_priority")
    private Integer massDistributionPriority = 5;

    @Column(name = "created_at", nullable = false, updatable = false)
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
}