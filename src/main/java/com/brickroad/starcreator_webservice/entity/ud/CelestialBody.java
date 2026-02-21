package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "celestial_body", schema = "ud")
@Inheritance(strategy = InheritanceType.JOINED)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Base class for all celestial bodies (stars, planets, moons)")
public class CelestialBody {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier", example = "1")
    private Long id;

    @Schema(description = "Designation of this body", example = "SCS-V01-8RQ A b")
    private String name;

    @JsonIgnore
    @Schema(hidden = true)
    private double mass;

    @JsonIgnore
    @Schema(hidden = true)
    private double radius;

    @JsonIgnore
    @Schema(hidden = true)
    private double circumference;

    @ManyToOne
    @JoinColumn(name = "system_id")
    @JsonBackReference
    private StarSystem system;

    @Schema(description = "Distance from the parent star in AU (for stars in multi-star systems, distance from barycenter)")
    private Double distanceFromStar;

    @Schema(description = "Position in the orbital sequence (1 = innermost)")
    private Integer orbitalOrder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @Schema(description = "Timestamp when this body was generated")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    @Schema(description = "Timestamp when this body was last modified")
    private LocalDateTime modifiedAt;
}
