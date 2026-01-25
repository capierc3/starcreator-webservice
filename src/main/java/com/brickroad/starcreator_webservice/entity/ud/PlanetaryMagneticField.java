package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "planetary_magnetic_field", schema = "ud")
public class PlanetaryMagneticField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "planet_id", unique = true)
    private Long planetId;  // Nullable until planet is saved

    // Transient relationship for in-memory linking before save
    @Transient
    @JsonIgnore
    private Planet planet;

    // ================================================================
    // FIELD STRENGTH
    // ================================================================
    @Column(name = "strength_compared_to_earth", nullable = false)
    private Double strengthComparedToEarth;

    @Column(name = "surface_field_microteslas_min", nullable = false)
    private Double surfaceFieldMicroteslasMin;

    @Column(name = "surface_field_microteslas_max", nullable = false)
    private Double surfaceFieldMicroteslasMax;

    @Column(name = "surface_field_microteslas_avg", nullable = false)
    private Double surfaceFieldMicroteslasAvg;

    // ================================================================
    // DYNAMO CHARACTERISTICS
    // ================================================================
    public enum DynamoType {
        CORE_DYNAMO,        // Liquid iron/nickel core convection (rocky planets)
        METALLIC_HYDROGEN,  // Gas giant metallic hydrogen layer (Jupiter, Saturn)
        IONIC_FLUID,        // Ice giant superionic ice/ammonia layer (Uranus, Neptune)
        REMNANT,            // Frozen ancient field in crust (Mars)
        INDUCED,            // External field induces currents (Venus)
        NONE                // No magnetic field generation
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "dynamo_type", length = 50)
    private DynamoType dynamoType;

    @Column(name = "dynamo_efficiency")
    private Double dynamoEfficiency; // 0.0 to 1.0

    public enum CoreConvectionIntensity {
        NONE, WEAK, MODERATE, STRONG, EXTREME
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "core_convection_intensity", length = 50)
    private CoreConvectionIntensity coreConvectionIntensity;

    // ================================================================
    // FIELD GEOMETRY
    // ================================================================
    public enum FieldGeometry {
        DIPOLE,      // Two poles, like Earth
        QUADRUPOLE,  // Four poles
        MULTIPOLE,   // Many poles, complex
        CHAOTIC,     // Disorganized
        COMPRESSED,  // Squashed by stellar wind
        NONE         // No field structure
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "field_geometry", length = 50)
    private FieldGeometry fieldGeometry;

    @Column(name = "dipole_tilt_degrees")
    private Double dipoleTiltDegrees; // Angle from rotation axis

    @Column(name = "magnetic_axis_offset_km")
    private Double magneticAxisOffsetKm; // Offset from planet center

    // ================================================================
    // SPATIAL VARIATION
    // ================================================================
    public enum VariationPattern {
        NO_REGIONAL_VARIANCE,
        HIGHER_AT_NORTH_POLE,
        HIGHER_AT_SOUTH_POLE,
        HIGHER_AT_BOTH_POLES,
        HIGHER_AT_EQUATOR,
        HIGHER_IN_RANDOM_SPOTS,
        BANDED_ANOMALIES,      // Stripes of strong/weak field
        CRUSTAL_ANOMALIES      // Localized crustal magnetization
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "variation_pattern", nullable = false, length = 100)
    private VariationPattern variationPattern;

    @Column(name = "pole_field_strength_multiplier")
    private Double poleFieldStrengthMultiplier;

    @Column(name = "equatorial_field_strength_multiplier")
    private Double equatorialFieldStrengthMultiplier;

    // ================================================================
    // TEMPORAL VARIATION
    // ================================================================
    public enum TemporalStability {
        STABLE,     // Constant field
        FLUXING,    // Regular cyclical changes
        UNSTABLE,   // Random fluctuations
        REVERSING,  // Pole reversals occurring
        DECAYING    // Permanent weakening
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "temporal_stability", length = 50)
    private TemporalStability temporalStability;

    // For FLUXING fields
    @Column(name = "flux_period_hours")
    private Integer fluxPeriodHours;

    @Column(name = "flux_peak_microteslas")
    private Double fluxPeakMicroteslas;

    @Column(name = "flux_low_microteslas")
    private Double fluxLowMicroteslas;

    @Column(name = "flux_amplitude_percent")
    private Double fluxAmplitudePercent;

    // For UNSTABLE fields
    public enum InstabilityFrequency {
        OCCASIONAL, FREQUENT, CONSTANT
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "instability_frequency", length = 50)
    private InstabilityFrequency instabilityFrequency;

    @Column(name = "random_fluctuation_percent")
    private Double randomFluctuationPercent;

    // ================================================================
    // MAGNETIC REVERSALS
    // ================================================================
    @Column(name = "has_reversals")
    private Boolean hasReversals = false;

    @Column(name = "reversal_period_million_years")
    private Double reversalPeriodMillionYears;

    @Column(name = "time_since_last_reversal_million_years")
    private Double timeSinceLastReversalMillionYears;

    @Column(name = "reversal_transition_duration_years")
    private Integer reversalTransitionDurationYears;

    public enum ReversalState {
        NORMAL, IN_TRANSITION, REVERSED
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "current_reversal_state", length = 50)
    private ReversalState currentReversalState;

    // ================================================================
    // MAGNETOSPHERE PROPERTIES
    // ================================================================
    @Column(name = "magnetosphere_exists")
    private Boolean magnetosphereExists = false;

    @Column(name = "magnetopause_distance_planet_radii")
    private Double magnetopauseDistancePlanetRadii;

    @Column(name = "magnetotail_length_planet_radii")
    private Double magnetotailLengthPlanetRadii;

    @Column(name = "bow_shock_distance_planet_radii")
    private Double bowShockDistancePlanetRadii;

    // Van Allen Belt analogs
    @Column(name = "has_radiation_belts")
    private Boolean hasRadiationBelts = false;

    public enum BeltIntensity {
        NONE, LOW, MODERATE, HIGH, EXTREME
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "inner_belt_intensity", length = 50)
    private BeltIntensity innerBeltIntensity;

    @Enumerated(EnumType.STRING)
    @Column(name = "outer_belt_intensity", length = 50)
    private BeltIntensity outerBeltIntensity;

    // ================================================================
    // AURORAL ACTIVITY
    // ================================================================
    @Column(name = "has_auroras")
    private Boolean hasAuroras = false;

    @Column(name = "auroral_colors", length = 200)
    private String auroralColors;

    @Column(name = "auroral_zone_latitude_degrees")
    private Double auroralZoneLatitudeDegrees;

    public enum AuroralFrequency {
        RARE, OCCASIONAL, FREQUENT, CONSTANT
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "auroral_frequency", length = 50)
    private AuroralFrequency auroralFrequency;

    public enum AuroralIntensity {
        FAINT, MODERATE, BRIGHT, SPECTACULAR
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "auroral_intensity", length = 50)
    private AuroralIntensity auroralIntensity;

    // ================================================================
    // FIELD INTERACTIONS
    // ================================================================
    @Column(name = "shields_from_stellar_wind")
    private Boolean shieldsFromStellarWind = false;

    @Column(name = "shields_from_cosmic_rays")
    private Boolean shieldsFromCosmicRays = false;

    public enum ProtectionLevel {
        NONE,           // 0% protection
        MINIMAL,        // <25%
        MODERATE,       // 25-50%
        STRONG,         // 50-80%
        EXCEPTIONAL     // >80%
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "protection_level", length = 50)
    private ProtectionLevel protectionLevel;

    @Column(name = "atmospheric_loss_rate_factor")
    private Double atmosphericLossRateFactor; // Multiplier: 1.0 = normal

    // ================================================================
    // SCIENTIFIC PROPERTIES
    // ================================================================
    @Column(name = "magnetic_moment")
    private Double magneticMoment; // A·m² (Earth: 7.91 × 10^22)

    @Column(name = "surface_power_flux_watts_per_m2")
    private Double surfacePowerFluxWattsPerM2;

    // Paleomagnetism
    @Column(name = "has_paleomagnetic_record")
    private Boolean hasPaleomagneticRecord = false;

    @Column(name = "oldest_magnetic_rocks_million_years")
    private Double oldestMagneticRocksMillionYears;

    // ================================================================
    // METADATA
    // ================================================================
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    // ================================================================
    // CONSTRUCTORS
    // ================================================================
    public PlanetaryMagneticField() {
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }

    // ================================================================
    // GETTERS AND SETTERS
    // ================================================================

    public void preparePersistence() {
        if (this.planet != null && this.planet.getId() != null) {
            this.planetId = this.planet.getId();
        }
    }
}