package com.brickroad.starcreator_webservice.model.atmospheres;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.Arrays;

@Entity
@Table(name = "atmosphere_template_component", schema = "ref")
public class AtmosphereTemplateComponentRef {
    
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
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public AtmosphereTemplateRef getTemplate() {
        return template;
    }
    
    public void setTemplate(AtmosphereTemplateRef template) {
        this.template = template;
    }
    
    public String getGasFormula() {
        return gasFormula;
    }
    
    public void setGasFormula(String gasFormula) {
        this.gasFormula = gasFormula;
    }
    
    public Double getMinPercentage() {
        return minPercentage;
    }
    
    public void setMinPercentage(Double minPercentage) {
        this.minPercentage = minPercentage;
    }
    
    public Double getMaxPercentage() {
        return maxPercentage;
    }
    
    public void setMaxPercentage(Double maxPercentage) {
        this.maxPercentage = maxPercentage;
    }
    
    public Boolean getIsTrace() {
        return isTrace;
    }
    
    public void setIsTrace(Boolean trace) {
        isTrace = trace;
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
