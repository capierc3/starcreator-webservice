package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "cloud_layer", schema = "ud")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CloudLayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "layer_order", nullable = false)
    private Integer layerOrder;

    @Column(name = "composition", length = 100, nullable = false)
    private String composition;

    @Column(name = "altitude_km")
    private Double altitudeKm;

    @Column(name = "thickness_km")
    private Double thicknessKm;

    @Column(name = "temperature_k")
    private Double temperatureK;

    @Column(name = "opacity", length = 30)
    private String opacity;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "description", length = 200)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;
}
