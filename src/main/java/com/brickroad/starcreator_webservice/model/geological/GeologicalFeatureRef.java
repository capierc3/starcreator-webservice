package com.brickroad.starcreator_webservice.model.geological;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "geological_template_feature", schema = "ref")
public class GeologicalFeatureRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    @JsonBackReference
    private GeologicalTemplateRef template;

    @Column(name = "feature_type", nullable = false, length = 50)
    private String featureType; // "VOLCANISM", "TECTONICS", "TERRAIN", "EROSION", etc.

    @Column(name = "feature_value", nullable = false, length = 100)
    private String featureValue; // The actual value (e.g., "Silicate", "Active", etc.)

    @Column(name = "min_value")
    private Double minValue; // For numeric features

    @Column(name = "max_value")
    private Double maxValue; // For numeric features

    @Column(columnDefinition = "TEXT")
    private String description;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public GeologicalTemplateRef getTemplate() { return template; }
    public void setTemplate(GeologicalTemplateRef template) { this.template = template; }

    public String getFeatureType() { return featureType; }
    public void setFeatureType(String featureType) { this.featureType = featureType; }

    public String getFeatureValue() { return featureValue; }
    public void setFeatureValue(String featureValue) { this.featureValue = featureValue; }

    public Double getMinValue() { return minValue; }
    public void setMinValue(Double minValue) { this.minValue = minValue; }

    public Double getMaxValue() { return maxValue; }
    public void setMaxValue(Double maxValue) { this.maxValue = maxValue; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
