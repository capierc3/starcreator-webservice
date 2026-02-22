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
@Table(name = "climate_zone", schema = "ud")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClimateZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;
}
