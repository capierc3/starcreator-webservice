package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "eclipse_data", schema = "ud")
public class EclipseData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "weather_id", nullable = false)
    private Long weatherId;

    @Transient
    @JsonIgnore
    private PlanetaryWeather weather;

    @Column(name = "eclipse_source", length = 50)
    private String eclipseSource;

    @Column(name = "source_body_name", length = 100)
    private String sourceBodyName;

    @Column(name = "eclipse_type", length = 30)
    private String eclipseType;

    @Column(name = "frequency_per_year")
    private Double frequencyPerYear;

    @Column(name = "typical_duration_minutes")
    private Double typicalDurationMinutes;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public EclipseData() {
        this.createdAt = LocalDateTime.now();
    }

    public void preparePersistence() {
        if (this.weather != null && this.weather.getId() != null) {
            this.weatherId = this.weather.getId();
        }
    }
}
