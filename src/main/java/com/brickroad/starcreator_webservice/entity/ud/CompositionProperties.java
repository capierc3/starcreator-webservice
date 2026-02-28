package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Composition properties for any celestial body (planet, moon, or asteroid).
 * <p>
 * Body-type-specific fields are nullable and only populated for the appropriate
 * body type. {@code @JsonInclude(NON_NULL)} ensures only relevant fields appear.
 * <ul>
 *   <li>Planet: coreType, interiorComposition, envelopeComposition, compositionClassification</li>
 *   <li>Moon: compositionType, interiorComposition, envelopeComposition, compositionClassification</li>
 *   <li>Asteroid: composition (simple text), isDifferentiated, coreType</li>
 * </ul>
 */
@Entity
@Table(name = "composition_properties", schema = "ud")
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Composition properties for a celestial body")
public class CompositionProperties {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    // ── Shared Fields ──

    @Column(name = "core_type")
    @Schema(description = "Core composition type", example = "Iron-Nickel")
    private String coreType;

    // ── Planet / Moon Fields ──

    @Column(name = "interior_composition", length = 500)
    @Schema(description = "Interior composition breakdown")
    private String interiorComposition;

    @Column(name = "envelope_composition", length = 500)
    @Schema(description = "Envelope/crust composition breakdown")
    private String envelopeComposition;

    @Column(name = "composition_classification", length = 50)
    @Schema(description = "Overall composition classification", example = "SILICATE_RICH")
    private String compositionClassification;

    // ── Moon-only Fields ──

    @Column(name = "composition_type", length = 50)
    @Schema(description = "Primary composition type (moon classification)", example = "ROCKY")
    private String compositionType;

    // ── Asteroid-only Fields ──

    @Column(name = "composition")
    @Schema(description = "Composition breakdown", example = "Silicates 60%, Iron-Nickel 30%, Carbon 10%")
    private String composition;

    @Column(name = "is_differentiated")
    @JsonIgnore
    private Boolean isDifferentiated;

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
