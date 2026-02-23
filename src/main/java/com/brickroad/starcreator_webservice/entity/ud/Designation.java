package com.brickroad.starcreator_webservice.entity.ud;

import com.brickroad.starcreator_webservice.enums.BodyType;
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
@Table(name = "designation", schema = "ud")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Identity card for a celestial object: system, star, planet, moon, belt, ring, or asteroid")
public class Designation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    // ── Universal Fields ──

    @Enumerated(EnumType.STRING)
    @Column(name = "body_type", nullable = false, length = 20)
    @Schema(description = "Type of celestial body", example = "PLANET")
    private BodyType bodyType;

    @Column(name = "logged_name", nullable = false)
    @Schema(description = "Systematic designation", example = "SCS-C42-79I A b")
    private String loggedName;

    @Column(name = "display_name")
    @Schema(description = "Human-readable name, assigned later if noteworthy", example = "Terra Nova")
    private String displayName;

    @Column(name = "age_my")
    @Schema(description = "Age in millions of years", example = "6335.65")
    private Double ageMY;

    @Column(name = "survey_history", length = 500)
    @Schema(description = "Comma-separated list of past survey codes", example = "XBO,ELW")
    private String surveyHistory;

    // ── Classification (shared field, meaning varies by bodyType) ──

    @Column(name = "object_type", length = 100)
    @Schema(description = "Classification type: planet type, moon type, star type, band type, or asteroid type", example = "Super-Earth")
    private String objectType;

    // ── Planet Fields ──

    @Column(name = "habitable_zone_position", length = 50)
    @Schema(description = "Position relative to the habitable zone (planet-only)", example = "habitable")
    private String habitableZonePosition;

    @Column(name = "parent_body_name")
    @Schema(description = "Name of the parent body (parent star for planets, parent planet for moons)", example = "SCS-C42-79I A")
    private String parentBodyName;

    // ── Star Fields ──

    @Column(name = "spectral_type", length = 20)
    @Schema(description = "Spectral class (star-only)", example = "M")
    private String spectralType;

    @Column(name = "star_role", length = 20)
    @Schema(description = "Role in multi-star system (star-only)", example = "PRIMARY")
    private String starRole;

    @Column(name = "color_index", length = 50)
    @Schema(description = "Visible color based on surface temperature (star-only)", example = "Red")
    private String colorIndex;

    @Column(name = "evolutionary_stage", length = 50)
    @Schema(description = "Current evolutionary stage (star-only)", example = "EARLY_MAIN_SEQUENCE")
    private String evolutionaryStage;

    // ── Moon Fields ──

    @Column(name = "formation_type", length = 50)
    @Schema(description = "How the moon formed (moon-only)", example = "CO_FORMED")
    private String formationType;

    // ── OrbitalBand Fields ──

    @Column(name = "band_category", length = 20)
    @Schema(description = "High-level band category: BELT or RING (band-only)", example = "BELT")
    private String bandCategory;

    // ── Asteroid Fields ──

    @Column(name = "designation_code", length = 100)
    @Schema(description = "Discovery designation code (asteroid-only)", example = "2435 WR1")
    private String designationCode;

    // ── Timestamps ──

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    @JsonIgnore
    private LocalDateTime modifiedAt;
}
