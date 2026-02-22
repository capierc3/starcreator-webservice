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
@Table(name = "precipitation_type", schema = "ud")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrecipitationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;
}
