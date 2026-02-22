package com.brickroad.starcreator_webservice.entity.ud;

import com.brickroad.starcreator_webservice.enums.ColonizationSuitability;
import com.brickroad.starcreator_webservice.enums.HabitabilityClass;
import com.brickroad.starcreator_webservice.enums.TerraformingPotential;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "planetary_habitability", schema = "ud")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Habitability assessment for a planet or moon, including ESI scores, radiation environment, atmospheric breathability, water potential, geological factors, and colonization suitability")
public class PlanetaryHabitability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    // ================================================================
    // EARTH SIMILARITY INDEX
    // ================================================================
    @Column(name = "esi_total")
    @Schema(description = "Earth Similarity Index total score (0-1)", example = "0.82")
    private Double esiTotal;

    @Column(name = "esi_interior")
    @Schema(description = "ESI interior subscore based on mass and radius", example = "0.85")
    private Double esiInterior;

    @Column(name = "esi_surface")
    @Schema(description = "ESI surface subscore based on temperature and escape velocity", example = "0.79")
    private Double esiSurface;

    // ================================================================
    // RADIATION ENVIRONMENT
    // ================================================================
    @Column(name = "uv_surface_flux_earth")
    @Schema(description = "UV surface flux relative to Earth (1.0 = Earth)", example = "0.95")
    private Double uvSurfaceFluxEarth;

    @Column(name = "uv_hazard_level", length = 30)
    @Schema(description = "UV radiation hazard level", example = "LOW")
    private String uvHazardLevel;

    @Column(name = "ionizing_radiation_surface_msv_yr")
    @Schema(description = "Ionizing radiation at surface in mSv/year", example = "2.4")
    private Double ionizingRadiationSurfaceMsvYr;

    @Column(name = "cosmic_ray_flux_earth")
    @Schema(description = "Cosmic ray flux relative to Earth", example = "0.8")
    private Double cosmicRayFluxEarth;

    @Column(name = "stellar_particle_flux")
    @Schema(description = "Stellar particle flux intensity")
    private Double stellarParticleFlux;

    @Column(name = "radiation_belt_surface_dose", length = 30)
    @Schema(description = "Radiation belt dose at surface", example = "NEGLIGIBLE")
    private String radiationBeltSurfaceDose;

    // ================================================================
    // ATMOSPHERIC HABITABILITY
    // ================================================================
    @Column(name = "is_breathable")
    @Schema(description = "Whether the atmosphere is breathable by humans")
    private Boolean isBreathable = false;

    @Column(name = "breathability_issues", length = 500)
    @Schema(description = "Issues preventing breathability, if any")
    private String breathabilityIssues;

    @Column(name = "oxygen_percentage")
    @Schema(description = "Atmospheric oxygen percentage", example = "20.9")
    private Double oxygenPercentage;

    @Column(name = "oxygen_source", length = 30)
    @Schema(description = "Primary source of atmospheric oxygen", example = "BIOLOGICAL")
    private String oxygenSource;

    @Column(name = "has_ozone_layer")
    @Schema(description = "Whether an ozone layer is present")
    private Boolean hasOzoneLayer = false;

    @Column(name = "ozone_column_dobson")
    @Schema(description = "Ozone column density in Dobson units", example = "300")
    private Double ozoneColumnDobson;

    @Column(name = "greenhouse_warming_k")
    @Schema(description = "Greenhouse warming contribution in Kelvin", example = "33")
    private Double greenhouseWarningK;

    @Column(name = "atmospheric_retention_score")
    @Schema(description = "Atmosphere retention score (0-1)", example = "0.95")
    private Double atmosphericRetentionScore;

    @Column(name = "toxic_gas_hazard", length = 30)
    @Schema(description = "Toxic gas hazard level", example = "NONE")
    private String toxicGasHazard;

    // ================================================================
    // LIQUID WATER POTENTIAL
    // ================================================================
    @Column(name = "surface_liquid_water_possible")
    @Schema(description = "Whether surface liquid water is thermodynamically possible")
    private Boolean surfaceLiquidWaterPossible = false;

    @Column(name = "water_phase_at_surface", length = 30)
    @Schema(description = "Dominant water phase at the surface", example = "LIQUID")
    private String waterPhaseAtSurface;

    @Column(name = "habitable_surface_fraction")
    @Schema(description = "Fraction of surface within habitable temperature range (0-1)", example = "0.85")
    private Double habitableSurfaceFraction;

    @Column(name = "subsurface_ocean_possible")
    @Schema(description = "Whether a subsurface ocean is possible")
    private Boolean subsurfaceOceanPossible = false;

    @Column(name = "water_source_likelihood", length = 30)
    @Schema(description = "Likelihood of water sources", example = "HIGH")
    private String waterSourceLikelihood;

    @Column(name = "mean_surface_temp_habitable")
    @Schema(description = "Whether the mean surface temperature falls within habitable range")
    private Boolean meanSurfaceTempHabitable = false;

    // ================================================================
    // GEOLOGICAL HABITABILITY
    // ================================================================
    @Column(name = "has_carbon_cycle")
    @Schema(description = "Whether a carbon-silicate cycle operates")
    private Boolean hasCarbonCycle = false;

    @Column(name = "carbon_cycle_strength", length = 30)
    @Schema(description = "Strength of the carbon cycle", example = "STRONG")
    private String carbonCycleStrength;

    @Column(name = "geothermal_heat_flux_mw_m2")
    @Schema(description = "Geothermal heat flux in mW/m\u00b2", example = "95")
    private Double geothermalHeatFluxMwM2;

    @Column(name = "magnetic_protection_adequate")
    @Schema(description = "Whether magnetic field provides adequate atmospheric protection")
    private Boolean magneticProtectionAdequate = false;

    @Column(name = "tidal_heating_contribution", length = 30)
    @Schema(description = "Contribution of tidal heating to habitability", example = "NONE")
    private String tidalHeatingContribution;

    @Column(name = "nutrient_cycling_potential", length = 30)
    @Schema(description = "Potential for nutrient cycling processes", example = "HIGH")
    private String nutrientCyclingPotential;

    // ================================================================
    // ORBITAL & STELLAR ENVIRONMENT
    // ================================================================
    @Column(name = "stellar_lifetime_remaining_my")
    @Schema(description = "Remaining stellar main-sequence lifetime in millions of years")
    private Double stellarLifetimeRemainingMy;

    @Column(name = "time_in_habitable_zone_my")
    @Schema(description = "Time spent in the habitable zone in millions of years")
    private Double timeInHabitableZoneMy;

    @Column(name = "orbital_stability_score")
    @Schema(description = "Orbital stability score (0-1)", example = "0.95")
    private Double orbitalStabilityScore;

    @Column(name = "tidal_lock_risk", length = 30)
    @Schema(description = "Risk of tidal locking", example = "LOW")
    private String tidalLockRisk;

    @Column(name = "obliquity_stability", length = 30)
    @Schema(description = "Axial tilt stability classification", example = "STABLE")
    private String obliquityStability;

    @Column(name = "flare_exposure_risk", length = 30)
    @Schema(description = "Risk from stellar flare exposure", example = "LOW")
    private String flareExposureRisk;

    @Column(name = "day_night_temp_range_k")
    @Schema(description = "Day-night temperature range in Kelvin", example = "12.5")
    private Double dayNightTempRangeK;

    // ================================================================
    // BIOSIGNATURE & LIFE ASSESSMENT
    // ================================================================
    @Column(name = "biosignature_potential", length = 30)
    @Schema(description = "Potential for detectable biosignatures", example = "HIGH")
    private String biosignaturePotential;

    @Column(name = "biosignature_indicators", length = 500)
    @Schema(description = "Specific biosignature indicators detected")
    private String biosignatureIndicators;

    @Column(name = "life_complexity_potential", length = 50)
    @Schema(description = "Maximum potential life complexity", example = "COMPLEX_MULTICELLULAR")
    private String lifeComplexityPotential;

    @Column(name = "photosynthesis_viable")
    @Schema(description = "Whether photosynthesis is viable given stellar spectrum and atmosphere")
    private Boolean photosynthesisViable = false;

    @Column(name = "energy_sources_for_life", length = 500)
    @Schema(description = "Available energy sources for life")
    private String energySourcesForLife;

    // ================================================================
    // HABITABILITY CLASSIFICATION
    // ================================================================
    @Enumerated(EnumType.STRING)
    @Column(name = "habitability_class", length = 50, nullable = false)
    @Schema(description = "Overall habitability classification", example = "EARTH_ANALOG")
    private HabitabilityClass habitabilityClass;

    @Column(name = "habitability_score")
    @Schema(description = "Composite habitability score (0-100)", example = "87.5")
    private Double habitabilityScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "terraforming_potential", length = 30)
    @Schema(description = "Terraforming feasibility assessment", example = "UNNECESSARY")
    private TerraformingPotential terraformingPotential;

    @Column(name = "terraforming_challenges", length = 500)
    @Schema(description = "Key challenges for terraforming")
    private String terraformingChallenges;

    @Enumerated(EnumType.STRING)
    @Column(name = "colonization_suitability", length = 30)
    @Schema(description = "Colonization suitability classification", example = "SHIRT_SLEEVE")
    private ColonizationSuitability colonizationSuitability;

    @Column(name = "limiting_factor", length = 500)
    @Schema(description = "Primary factor limiting habitability")
    private String limitingFactor;

    // ================================================================
    // METADATA
    // ================================================================
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    @JsonIgnore
    private LocalDateTime modifiedAt;
}
