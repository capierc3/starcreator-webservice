package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "precipitation_type", schema = "ud")
public class PrecipitationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "weather_id", nullable = false)
    private Long weatherId;

    @Transient
    @JsonIgnore
    private PlanetaryWeather weather;

    @Column(name = "substance", length = 50, nullable = false)
    private String substance;

    @Column(name = "phase", length = 30, nullable = false)
    private String phase;

    @Column(name = "frequency", length = 30)
    private String frequency;

    @Column(name = "intensity", length = 30)
    private String intensity;

    @Column(name = "reaches_surface")
    private Boolean reachesSurface = true;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public PrecipitationType() {
        this.createdAt = LocalDateTime.now();
    }

    public void preparePersistence() {
        if (this.weather != null && this.weather.getId() != null) {
            this.weatherId = this.weather.getId();
        }
    }
}
