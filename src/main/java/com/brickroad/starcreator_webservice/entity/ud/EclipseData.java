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
@Table(name = "eclipse_data", schema = "ud")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EclipseData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;
}
