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
@Table(name = "climate_hazard", schema = "ud")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClimateHazard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "hazard_name", length = 100, nullable = false)
    private String hazardName;

    @Column(name = "hazard_type", length = 50)
    private String hazardType;

    @Column(name = "severity", length = 30)
    private String severity;

    @Column(name = "frequency", length = 30)
    private String frequency;

    @Column(name = "description", length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;
}
