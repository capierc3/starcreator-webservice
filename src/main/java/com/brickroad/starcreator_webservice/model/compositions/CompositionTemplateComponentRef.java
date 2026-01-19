package com.brickroad.starcreator_webservice.model.compositions;

import jakarta.persistence.*;

/**
 * Database entity representing a single mineral component within a composition template.
 * Links a mineral to a template with percentage ranges and layer information.
 */
@Entity
@Table(name = "composition_template_component", schema = "ref")
public class CompositionTemplateComponentRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    private CompositionTemplateRef template;

    @Enumerated(EnumType.STRING)
    @Column(name = "mineral", nullable = false, length = 50)
    private CompositionMineral mineral;

    @Column(name = "layer_type", nullable = false, length = 10)
    private String layerType; // "MANTLE" or "CRUST"

    @Column(name = "min_percentage", nullable = false)
    private Double minPercentage;

    @Column(name = "max_percentage", nullable = false)
    private Double maxPercentage;

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public CompositionTemplateRef getTemplate() { return template; }
    public void setTemplate(CompositionTemplateRef template) { this.template = template; }
    
    public CompositionMineral getMineral() { return mineral; }
    public void setMineral(CompositionMineral mineral) { this.mineral = mineral; }
    
    public String getLayerType() { return layerType; }
    public void setLayerType(String layerType) { this.layerType = layerType; }
    
    public Double getMinPercentage() { return minPercentage; }
    public void setMinPercentage(Double minPercentage) { this.minPercentage = minPercentage; }
    
    public Double getMaxPercentage() { return maxPercentage; }
    public void setMaxPercentage(Double maxPercentage) { this.maxPercentage = maxPercentage; }
}
