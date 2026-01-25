package com.brickroad.starcreator_webservice.entity.ref;

import com.brickroad.starcreator_webservice.enums.CompositionMineral;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "composition_template_component", schema = "ref")
public class CompositionTemplateComponentRef {

    // Getters and setters
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

}
