package com.brickroad.starcreator_webservice.entity.ref;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "precipitation_template", schema = "ref")
public class PrecipitationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cloud_substance", length = 50, nullable = false)
    private String cloudSubstance;

    @Column(name = "surface_temp_min_k")
    private Double surfaceTempMinK;

    @Column(name = "surface_temp_max_k")
    private Double surfaceTempMaxK;

    @Column(name = "phase", length = 30, nullable = false)
    private String phase;

    @Column(name = "reaches_surface")
    private Boolean reachesSurface = true;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
