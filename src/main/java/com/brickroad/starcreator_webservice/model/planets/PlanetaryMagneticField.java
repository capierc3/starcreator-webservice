package com.brickroad.starcreator_webservice.model.planets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

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
        CORE_DYNAMO,        // Liquid iron/nickel core convection
        METALLIC_HYDROGEN,  // Gas giant hydrogen layer
        REMNANT,            // Frozen ancient field in crust
        INDUCED,            // External field induces currents
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlanetId() {
        return planetId;
    }

    public void setPlanetId(Long planetId) {
        this.planetId = planetId;
    }

    public Double getStrengthComparedToEarth() {
        return strengthComparedToEarth;
    }

    public void setStrengthComparedToEarth(Double strengthComparedToEarth) {
        this.strengthComparedToEarth = strengthComparedToEarth;
    }

    public Double getSurfaceFieldMicroteslasMin() {
        return surfaceFieldMicroteslasMin;
    }

    public void setSurfaceFieldMicroteslasMin(Double surfaceFieldMicroteslasMin) {
        this.surfaceFieldMicroteslasMin = surfaceFieldMicroteslasMin;
    }

    public Double getSurfaceFieldMicroteslasMax() {
        return surfaceFieldMicroteslasMax;
    }

    public void setSurfaceFieldMicroteslasMax(Double surfaceFieldMicroteslasMax) {
        this.surfaceFieldMicroteslasMax = surfaceFieldMicroteslasMax;
    }

    public Double getSurfaceFieldMicroteslasAvg() {
        return surfaceFieldMicroteslasAvg;
    }

    public void setSurfaceFieldMicroteslasAvg(Double surfaceFieldMicroteslasAvg) {
        this.surfaceFieldMicroteslasAvg = surfaceFieldMicroteslasAvg;
    }

    public DynamoType getDynamoType() {
        return dynamoType;
    }

    public void setDynamoType(DynamoType dynamoType) {
        this.dynamoType = dynamoType;
    }

    public Double getDynamoEfficiency() {
        return dynamoEfficiency;
    }

    public void setDynamoEfficiency(Double dynamoEfficiency) {
        this.dynamoEfficiency = dynamoEfficiency;
    }

    public CoreConvectionIntensity getCoreConvectionIntensity() {
        return coreConvectionIntensity;
    }

    public void setCoreConvectionIntensity(CoreConvectionIntensity coreConvectionIntensity) {
        this.coreConvectionIntensity = coreConvectionIntensity;
    }

    public FieldGeometry getFieldGeometry() {
        return fieldGeometry;
    }

    public void setFieldGeometry(FieldGeometry fieldGeometry) {
        this.fieldGeometry = fieldGeometry;
    }

    public Double getDipoleTiltDegrees() {
        return dipoleTiltDegrees;
    }

    public void setDipoleTiltDegrees(Double dipoleTiltDegrees) {
        this.dipoleTiltDegrees = dipoleTiltDegrees;
    }

    public Double getMagneticAxisOffsetKm() {
        return magneticAxisOffsetKm;
    }

    public void setMagneticAxisOffsetKm(Double magneticAxisOffsetKm) {
        this.magneticAxisOffsetKm = magneticAxisOffsetKm;
    }

    public VariationPattern getVariationPattern() {
        return variationPattern;
    }

    public void setVariationPattern(VariationPattern variationPattern) {
        this.variationPattern = variationPattern;
    }

    public Double getPoleFieldStrengthMultiplier() {
        return poleFieldStrengthMultiplier;
    }

    public void setPoleFieldStrengthMultiplier(Double poleFieldStrengthMultiplier) {
        this.poleFieldStrengthMultiplier = poleFieldStrengthMultiplier;
    }

    public Double getEquatorialFieldStrengthMultiplier() {
        return equatorialFieldStrengthMultiplier;
    }

    public void setEquatorialFieldStrengthMultiplier(Double equatorialFieldStrengthMultiplier) {
        this.equatorialFieldStrengthMultiplier = equatorialFieldStrengthMultiplier;
    }

    public TemporalStability getTemporalStability() {
        return temporalStability;
    }

    public void setTemporalStability(TemporalStability temporalStability) {
        this.temporalStability = temporalStability;
    }

    public Integer getFluxPeriodHours() {
        return fluxPeriodHours;
    }

    public void setFluxPeriodHours(Integer fluxPeriodHours) {
        this.fluxPeriodHours = fluxPeriodHours;
    }

    public Double getFluxPeakMicroteslas() {
        return fluxPeakMicroteslas;
    }

    public void setFluxPeakMicroteslas(Double fluxPeakMicroteslas) {
        this.fluxPeakMicroteslas = fluxPeakMicroteslas;
    }

    public Double getFluxLowMicroteslas() {
        return fluxLowMicroteslas;
    }

    public void setFluxLowMicroteslas(Double fluxLowMicroteslas) {
        this.fluxLowMicroteslas = fluxLowMicroteslas;
    }

    public Double getFluxAmplitudePercent() {
        return fluxAmplitudePercent;
    }

    public void setFluxAmplitudePercent(Double fluxAmplitudePercent) {
        this.fluxAmplitudePercent = fluxAmplitudePercent;
    }

    public InstabilityFrequency getInstabilityFrequency() {
        return instabilityFrequency;
    }

    public void setInstabilityFrequency(InstabilityFrequency instabilityFrequency) {
        this.instabilityFrequency = instabilityFrequency;
    }

    public Double getRandomFluctuationPercent() {
        return randomFluctuationPercent;
    }

    public void setRandomFluctuationPercent(Double randomFluctuationPercent) {
        this.randomFluctuationPercent = randomFluctuationPercent;
    }

    public Boolean getHasReversals() {
        return hasReversals;
    }

    public void setHasReversals(Boolean hasReversals) {
        this.hasReversals = hasReversals;
    }

    public Double getReversalPeriodMillionYears() {
        return reversalPeriodMillionYears;
    }

    public void setReversalPeriodMillionYears(Double reversalPeriodMillionYears) {
        this.reversalPeriodMillionYears = reversalPeriodMillionYears;
    }

    public Double getTimeSinceLastReversalMillionYears() {
        return timeSinceLastReversalMillionYears;
    }

    public void setTimeSinceLastReversalMillionYears(Double timeSinceLastReversalMillionYears) {
        this.timeSinceLastReversalMillionYears = timeSinceLastReversalMillionYears;
    }

    public Integer getReversalTransitionDurationYears() {
        return reversalTransitionDurationYears;
    }

    public void setReversalTransitionDurationYears(Integer reversalTransitionDurationYears) {
        this.reversalTransitionDurationYears = reversalTransitionDurationYears;
    }

    public ReversalState getCurrentReversalState() {
        return currentReversalState;
    }

    public void setCurrentReversalState(ReversalState currentReversalState) {
        this.currentReversalState = currentReversalState;
    }

    public Boolean getMagnetosphereExists() {
        return magnetosphereExists;
    }

    public void setMagnetosphereExists(Boolean magnetosphereExists) {
        this.magnetosphereExists = magnetosphereExists;
    }

    public Double getMagnetopauseDistancePlanetRadii() {
        return magnetopauseDistancePlanetRadii;
    }

    public void setMagnetopauseDistancePlanetRadii(Double magnetopauseDistancePlanetRadii) {
        this.magnetopauseDistancePlanetRadii = magnetopauseDistancePlanetRadii;
    }

    public Double getMagnetotailLengthPlanetRadii() {
        return magnetotailLengthPlanetRadii;
    }

    public void setMagnetotailLengthPlanetRadii(Double magnetotailLengthPlanetRadii) {
        this.magnetotailLengthPlanetRadii = magnetotailLengthPlanetRadii;
    }

    public Double getBowShockDistancePlanetRadii() {
        return bowShockDistancePlanetRadii;
    }

    public void setBowShockDistancePlanetRadii(Double bowShockDistancePlanetRadii) {
        this.bowShockDistancePlanetRadii = bowShockDistancePlanetRadii;
    }

    public Boolean getHasRadiationBelts() {
        return hasRadiationBelts;
    }

    public void setHasRadiationBelts(Boolean hasRadiationBelts) {
        this.hasRadiationBelts = hasRadiationBelts;
    }

    public BeltIntensity getInnerBeltIntensity() {
        return innerBeltIntensity;
    }

    public void setInnerBeltIntensity(BeltIntensity innerBeltIntensity) {
        this.innerBeltIntensity = innerBeltIntensity;
    }

    public BeltIntensity getOuterBeltIntensity() {
        return outerBeltIntensity;
    }

    public void setOuterBeltIntensity(BeltIntensity outerBeltIntensity) {
        this.outerBeltIntensity = outerBeltIntensity;
    }

    public Boolean getHasAuroras() {
        return hasAuroras;
    }

    public void setHasAuroras(Boolean hasAuroras) {
        this.hasAuroras = hasAuroras;
    }

    public Double getAuroralZoneLatitudeDegrees() {
        return auroralZoneLatitudeDegrees;
    }

    public void setAuroralZoneLatitudeDegrees(Double auroralZoneLatitudeDegrees) {
        this.auroralZoneLatitudeDegrees = auroralZoneLatitudeDegrees;
    }

    public AuroralFrequency getAuroralFrequency() {
        return auroralFrequency;
    }

    public void setAuroralFrequency(AuroralFrequency auroralFrequency) {
        this.auroralFrequency = auroralFrequency;
    }

    public AuroralIntensity getAuroralIntensity() {
        return auroralIntensity;
    }

    public void setAuroralIntensity(AuroralIntensity auroralIntensity) {
        this.auroralIntensity = auroralIntensity;
    }

    public Boolean getShieldsFromStellarWind() {
        return shieldsFromStellarWind;
    }

    public void setShieldsFromStellarWind(Boolean shieldsFromStellarWind) {
        this.shieldsFromStellarWind = shieldsFromStellarWind;
    }

    public Boolean getShieldsFromCosmicRays() {
        return shieldsFromCosmicRays;
    }

    public void setShieldsFromCosmicRays(Boolean shieldsFromCosmicRays) {
        this.shieldsFromCosmicRays = shieldsFromCosmicRays;
    }

    public ProtectionLevel getProtectionLevel() {
        return protectionLevel;
    }

    public void setProtectionLevel(ProtectionLevel protectionLevel) {
        this.protectionLevel = protectionLevel;
    }

    public Double getAtmosphericLossRateFactor() {
        return atmosphericLossRateFactor;
    }

    public void setAtmosphericLossRateFactor(Double atmosphericLossRateFactor) {
        this.atmosphericLossRateFactor = atmosphericLossRateFactor;
    }

    public Double getMagneticMoment() {
        return magneticMoment;
    }

    public void setMagneticMoment(Double magneticMoment) {
        this.magneticMoment = magneticMoment;
    }

    public Double getSurfacePowerFluxWattsPerM2() {
        return surfacePowerFluxWattsPerM2;
    }

    public void setSurfacePowerFluxWattsPerM2(Double surfacePowerFluxWattsPerM2) {
        this.surfacePowerFluxWattsPerM2 = surfacePowerFluxWattsPerM2;
    }

    public Boolean getHasPaleomagneticRecord() {
        return hasPaleomagneticRecord;
    }

    public void setHasPaleomagneticRecord(Boolean hasPaleomagneticRecord) {
        this.hasPaleomagneticRecord = hasPaleomagneticRecord;
    }

    public Double getOldestMagneticRocksMillionYears() {
        return oldestMagneticRocksMillionYears;
    }

    public void setOldestMagneticRocksMillionYears(Double oldestMagneticRocksMillionYears) {
        this.oldestMagneticRocksMillionYears = oldestMagneticRocksMillionYears;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public Planet getPlanet() {
        return planet;
    }

    public void setPlanet(Planet planet) {
        this.planet = planet;
    }

    /**
     * Prepare for database persistence by setting planetId from the planet object
     * Call this after the planet has been saved and has an ID
     */
    public void preparePersistence() {
        if (this.planet != null && this.planet.getId() != null) {
            this.planetId = this.planet.getId();
        }
    }
}