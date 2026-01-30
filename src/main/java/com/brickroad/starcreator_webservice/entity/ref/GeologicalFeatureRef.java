package com.brickroad.starcreator_webservice.entity.ref;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "geological_template_feature", schema = "ref")
public class GeologicalFeatureRef {

    // Getters and Setters
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

}
