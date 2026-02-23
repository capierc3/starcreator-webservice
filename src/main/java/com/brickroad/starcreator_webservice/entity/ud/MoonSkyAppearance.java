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
@Table(name = "moon_sky_appearance", schema = "ud")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MoonSkyAppearance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "moon_id")
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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;
}
