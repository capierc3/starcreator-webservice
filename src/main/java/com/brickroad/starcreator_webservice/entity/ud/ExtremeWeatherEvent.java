package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "extreme_weather_event", schema = "ud")
public class ExtremeWeatherEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "weather_id", nullable = false)
    private Long weatherId;

    @Transient
    @JsonIgnore
    private PlanetaryWeather weather;

    @Column(name = "event_name", length = 100, nullable = false)
    private String eventName;

    @Column(name = "event_type", length = 50)
    private String eventType;

    @Column(name = "severity", length = 30)
    private String severity;

    @Column(name = "frequency", length = 30)
    private String frequency;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ExtremeWeatherEvent() {
        this.createdAt = LocalDateTime.now();
    }

    public void preparePersistence() {
        if (this.weather != null && this.weather.getId() != null) {
            this.weatherId = this.weather.getId();
        }
    }
}
