package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "climate_zone", schema = "ud")
public class ClimateZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "weather_id", nullable = false)
    private Long weatherId;

    @Transient
    @JsonIgnore
    private PlanetaryWeather weather;

    @Column(name = "zone_name", length = 50, nullable = false)
    private String zoneName;

    @Column(name = "latitude_start_degrees")
    private Double latitudeStartDegrees;

    @Column(name = "latitude_end_degrees")
    private Double latitudeEndDegrees;

    @Column(name = "coverage_percent")
    private Double coveragePercent;

    @Column(name = "mean_temp_k")
    private Double meanTempK;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ClimateZone() {
        this.createdAt = LocalDateTime.now();
    }

    public void preparePersistence() {
        if (this.weather != null && this.weather.getId() != null) {
            this.weatherId = this.weather.getId();
        }
    }
}
