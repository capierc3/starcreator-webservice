package com.brickroad.starcreator_webservice.entity.ref;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "cloud_composition_template", schema = "ref")
public class CloudCompositionTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "atmosphere_classification", length = 50, nullable = false)
    private String atmosphereClassification;

    @Column(name = "substance", length = 50, nullable = false)
    private String substance;

    @Column(name = "min_temperature_k")
    private Double minTemperatureK;

    @Column(name = "max_temperature_k")
    private Double maxTemperatureK;

    @Column(name = "typical_altitude_scale_heights")
    private Double typicalAltitudeScaleHeights;

    @Column(name = "opacity", length = 30)
    private String opacity;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "weight")
    private Integer weight = 100;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
