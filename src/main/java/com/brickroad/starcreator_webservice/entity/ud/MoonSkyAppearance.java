package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "moon_sky_appearance", schema = "ud")
public class MoonSkyAppearance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "weather_id", nullable = false)
    private Long weatherId;

    @Transient
    @JsonIgnore
    private PlanetaryWeather weather;

    @Column(name = "moon_id", nullable = false)
    private Long moonId;

    @Column(name = "angular_diameter_degrees")
    private Double angularDiameterDegrees;

    @Column(name = "phase_cycle_days")
    private Double phaseCycleDays;

    @Column(name = "brightness_relative_to_full_moon")
    private Double brightnessRelativeToFullMoon;

    @Column(name = "is_visible_in_daytime")
    private Boolean isVisibleInDaytime = false;

    @Column(name = "apparent_color", length = 50)
    private String apparentColor;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public MoonSkyAppearance() {
        this.createdAt = LocalDateTime.now();
    }

    public void preparePersistence() {
        if (this.weather != null && this.weather.getId() != null) {
            this.weatherId = this.weather.getId();
        }
    }
}
