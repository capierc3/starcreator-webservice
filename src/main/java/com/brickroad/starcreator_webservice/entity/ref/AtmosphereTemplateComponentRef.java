package com.brickroad.starcreator_webservice.entity.ref;

import com.brickroad.starcreator_webservice.enums.AtmosphereGas;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Setter
@Getter
@Entity
@Table(name = "atmosphere_template_component", schema = "ref")
public class AtmosphereTemplateComponentRef {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    @JsonBackReference
    private AtmosphereTemplateRef template;
    
    @Column(name = "gas_formula", nullable = false, length = 10)
    private String gasFormula;
    
    @Column(name = "min_percentage", nullable = false)
    private Double minPercentage;
    
    @Column(name = "max_percentage", nullable = false)
    private Double maxPercentage;
    
    @Column(name = "is_trace")
    private Boolean isTrace;
    
    // Constructors
    public AtmosphereTemplateComponentRef() {
    }

    /**
     * Get the AtmosphereGas enum corresponding to this component's formula
     */
    public AtmosphereGas getGas() {
        return Arrays.stream(AtmosphereGas.values())
                .filter(gas -> gas.getFormula().equals(gasFormula))
                .findFirst()
                .orElse(null);
    }
}
