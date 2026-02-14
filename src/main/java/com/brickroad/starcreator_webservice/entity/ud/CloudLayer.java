package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "cloud_layer", schema = "ud")
public class CloudLayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "weather_id", nullable = false)
    private Long weatherId;

    @Transient
    @JsonIgnore
    private PlanetaryWeather weather;

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public CloudLayer() {
        this.createdAt = LocalDateTime.now();
    }

    public void preparePersistence() {
        if (this.weather != null && this.weather.getId() != null) {
            this.weatherId = this.weather.getId();
        }
    }
}
