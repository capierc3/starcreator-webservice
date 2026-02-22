package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Rotation and spin-axis properties for any celestial body (planet, moon, or asteroid).
 * <p>
 * The {@code tidallyLocked} field is specific to planets and moons — it is null for
 * asteroids and hidden by {@code @JsonInclude(NON_NULL)}.
 */
@Entity
@Table(name = "rotation_properties", schema = "ud")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Rotation and spin-axis properties for a celestial body")
public class RotationProperties {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "rotation_period_hours")
    @Schema(description = "Sidereal rotation period in hours (negative = retrograde)", example = "24.0")
    private Double rotationPeriodHours;

    @Column(name = "axial_tilt")
    @Schema(description = "Axial tilt in degrees", example = "23.4")
    private Double axialTilt;

    @Column(name = "tidally_locked")
    @JsonProperty("tidallyLocked")
    @Schema(description = "Whether the body is tidally locked to its parent")
    private Boolean tidallyLocked;

    // ── Metadata ──

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    @JsonIgnore
    private LocalDateTime modifiedAt;
}
