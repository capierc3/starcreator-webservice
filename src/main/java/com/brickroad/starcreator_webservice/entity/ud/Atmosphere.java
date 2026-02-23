package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Atmospheric properties for a celestial body")
public class Atmosphere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "classification", nullable = false, length = 50)
    @Schema(description = "Atmosphere classification type", example = "EARTH_LIKE")
    private String classification;

    @Column(name = "surface_pressure_bar")
    @Schema(description = "Surface atmospheric pressure in bar (1 bar ≈ Earth sea level)", example = "1.01")
    private Double surfacePressureBar;

    @Column(name = "composition_summary", columnDefinition = "TEXT")
    @Schema(description = "Human-readable atmospheric composition summary", example = "N2 78%, O2 21%, Ar 0.93%")
    private String compositionSummary;

    // Atmospheric properties (derived on load from pressure/temp/gravity, not persisted)
    @Transient
    @Schema(description = "Atmospheric scale height in km")
    private Double scaleHeightKm;

    @Transient
    @Schema(description = "Temperature increase due to greenhouse effect in Kelvin")
    private Double greenhouseEffectK;

    // Stripping metadata (internal generation data)
    @Column(name = "is_stripped", nullable = false)
    @JsonIgnore
    private Boolean isStripped = false;

    @Column(name = "stripped_reason", length = 100)
    @JsonIgnore
    private String strippedReason;

    // Component relationship - individual gas components
    @OneToMany(mappedBy = "atmosphere", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @Schema(description = "Individual gas components of the atmosphere")
    private List<AtmosphereComponent> components = new ArrayList<>();

    // ── Storm Properties (gas giant only — null for other planet types) ──

    @Column(name = "has_great_storm")
    @Schema(description = "Whether the planet has a persistent great storm (like Jupiter's Red Spot)")
    private Boolean hasGreatStorm;

    @Column(name = "number_of_major_storms")
    @Schema(description = "Number of persistent major storm systems")
    private Integer numberOfMajorStorms;

    @Column(name = "atmospheric_convection_level", length = 50)
    @Schema(description = "Atmospheric convection intensity", example = "VIGOROUS")
    private String atmosphericConvectionLevel;

    @Column(name = "created_at")
    @JsonIgnore
    private java.time.LocalDateTime createdAt;

    @Column(name = "modified_at")
    @JsonIgnore
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