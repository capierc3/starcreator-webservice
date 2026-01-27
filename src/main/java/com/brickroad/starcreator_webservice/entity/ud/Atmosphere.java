package com.brickroad.starcreator_webservice.entity.ud;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "atmosphere", schema = "ud")
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // Private - only for Builder
@Builder
public class Atmosphere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "classification", nullable = false, length = 50)
    private String classification;

    @Column(name = "surface_pressure_bar")
    private Double surfacePressureBar;

    @Column(name = "composition_summary", columnDefinition = "TEXT")
    private String compositionSummary;

    // Atmospheric properties
    @Column(name = "scale_height_km")
    private Double scaleHeightKm;

    @Column(name = "greenhouse_effect_k")
    private Double greenhouseEffectK;

    // Stripping metadata
    @Column(name = "is_stripped", nullable = false)
    private Boolean isStripped = false;

    @Column(name = "stripped_reason", length = 100)
    private String strippedReason;

    // Component relationship - individual gas components
    @OneToMany(mappedBy = "atmosphere", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AtmosphereComponent> components = new ArrayList<>();

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "modified_at")
    private java.time.LocalDateTime modifiedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        modifiedAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedAt = java.time.LocalDateTime.now();
    }

    // Convenience method to add a component
    public void addComponent(AtmosphereComponent component) {
        components.add(component);
        component.setAtmosphere(this);
    }

    // Convenience method to add a component by gas formula and percentage
    public void addComponent(String gasFormula, double percentage, boolean isTrace) {
        AtmosphereComponent component = new AtmosphereComponent();
        component.setGasFormula(gasFormula);
        component.setPercentage(percentage);
        component.setIsTrace(isTrace);
        addComponent(component);
    }
}