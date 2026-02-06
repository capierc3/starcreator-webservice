package com.brickroad.starcreator_webservice.entity.ref;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "asteroid_type", schema = "ref")
public class AsteroidTypeRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "spectral_class")
    private String spectralClass;

    @Column(name = "description")
    private String description;

    @Column(name = "primary_composition")
    private String primaryComposition;

    @Column(name = "albedo_min")
    private Double albedoMin;

    @Column(name = "albedo_max")
    private Double albedoMax;

    @Column(name = "density_min")
    private Double densityMin;

    @Column(name = "density_max")
    private Double densityMax;

    @Column(name = "belt_affinity")
    private String beltAffinity;

    @Column(name = "relative_abundance")
    private Double relativeAbundance;

    @Column(name = "can_be_differentiated")
    private Boolean canBeDifferentiated;

    @Column(name = "typical_surface_features")
    private String typicalSurfaceFeatures;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public AsteroidTypeRef() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
