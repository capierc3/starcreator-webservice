package com.brickroad.starcreator_webservice.entity.ud;

import com.brickroad.starcreator_webservice.enums.AtmosphereGas;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.Arrays;

@Entity
@Table(name = "atmosphere_component", schema = "ud")
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AtmosphereComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atmosphere_id")
    @JsonBackReference
    private Atmosphere atmosphere;

    @Transient
    private AtmosphereGas gas;

    @Column(name = "gas_formula", length = 10)
    private String gasFormula;

    @Column(name = "percentage")
    private Double percentage;

    @Column(name = "is_trace")
    private Boolean isTrace = false;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    // ===================================================================
    // LIFECYCLE HOOKS
    // ===================================================================

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();

        // Sync gas enum to formula before persisting
        if (gas != null && gasFormula == null) {
            gasFormula = gas.getFormula();
        }
    }

    @PostLoad
    protected void onLoad() {
        // Sync formula to gas enum after loading from database
        if (gasFormula != null && gas == null) {
            gas = getGasFromFormula(gasFormula);
        }
    }

    // ===================================================================
    // ACCESSOR METHODS (works for both transient and persistent)
    // ===================================================================

    /**
     * Get the AtmosphereGas enum.
     * If gas is null (loaded from DB), derive it from gasFormula.
     */
    public AtmosphereGas gas() {
        if (gas == null && gasFormula != null) {
            gas = getGasFromFormula(gasFormula);
        }
        return gas;
    }

    /**
     * Get percentage value.
     */
    public double percentage() {
        return percentage != null ? percentage : 0.0;
    }

    /**
     * Get the formula (works whether from enum or database).
     */
    public String getFormula() {
        if (gasFormula != null) {
            return gasFormula;
        }
        return gas != null ? gas.getFormula() : null;
    }

    /**
     * Get the effect of this gas (e.g., "Breathable", "Toxic", "Greenhouse")
     */
    public String getEffect() {
        AtmosphereGas gasEnum = gas();
        return gasEnum != null ? gasEnum.getEffect() : "Unknown";
    }

    /**
     * Get the name of this gas (e.g., "Nitrogen", "Oxygen")
     */
    public String getName() {
        AtmosphereGas gasEnum = gas();
        return gasEnum != null ? gasEnum.getName() : gasFormula;
    }

    /**
     * Get the molecular weight of this gas
     */
    public Double getMolecularWeight() {
        AtmosphereGas gasEnum = gas();
        return gasEnum != null ? gasEnum.getMolecularWeight() : null;
    }

    /**
     * Check if this gas is breathable
     */
    public Boolean isBreathable() {
        AtmosphereGas gasEnum = gas();
        return gasEnum != null ? gasEnum.isBreathable() : false;
    }

    /**
     * Get formatted string for display (e.g., "N2 78.0%" or "CO2 (trace)")
     */
    public String getFormattedString() {
        String formula = getFormula();
        if (isTrace != null && isTrace) {
            return String.format("%s (trace)", formula);
        } else if (percentage >= 1.0) {
            return String.format("%s %.1f%%", formula, percentage);
        } else if (percentage >= 0.1) {
            return String.format("%s %.2f%%", formula, percentage);
        } else {
            return String.format("%s (trace)", formula);
        }
    }

    // ===================================================================
    // HELPER METHODS
    // ===================================================================

    /**
     * Convert formula string to AtmosphereGas enum
     */
    private AtmosphereGas getGasFromFormula(String formula) {
        return Arrays.stream(AtmosphereGas.values())
                .filter(g -> g.getFormula().equals(formula))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return getFormattedString();
    }

    // ===================================================================
    // EQUALS AND HASHCODE (important for collections)
    // ===================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AtmosphereComponent that)) return false;

        // For transient objects (not yet persisted), compare by gas formula
        if (id == null && that.id == null) {
            return getFormula().equals(that.getFormula());
        }

        // For persisted objects, compare by ID
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        // Use formula for hash code (works for both transient and persistent)
        return getFormula() != null ? getFormula().hashCode() : 0;
    }
}