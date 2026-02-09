package com.brickroad.starcreator_webservice.entity.ud;

import com.brickroad.starcreator_webservice.enums.ColonizationSuitability;
import com.brickroad.starcreator_webservice.enums.HabitabilityClass;
import com.brickroad.starcreator_webservice.enums.TerraformingPotential;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "planetary_habitability", schema = "ud")
public class PlanetaryHabitability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "planet_id", unique = true)
    private Long planetId;

    @Transient
    @JsonIgnore
    private Planet planet;

    @Column(name = "moon_id")
    private Long moonId;

    @Transient
    @JsonIgnore
    private Moon moon;

    // ================================================================
    // EARTH SIMILARITY INDEX
    // ================================================================
    @Column(name = "esi_total")
    private Double esiTotal;

    @Column(name = "esi_interior")
    private Double esiInterior;

    @Column(name = "esi_surface")
    private Double esiSurface;

    // ================================================================
    // RADIATION ENVIRONMENT
    // ================================================================
    @Column(name = "uv_surface_flux_earth")
    private Double uvSurfaceFluxEarth;

    @Column(name = "uv_hazard_level", length = 30)
    private String uvHazardLevel;

    @Column(name = "ionizing_radiation_surface_msv_yr")
    private Double ionizingRadiationSurfaceMsvYr;

    @Column(name = "cosmic_ray_flux_earth")
    private Double cosmicRayFluxEarth;

    @Column(name = "stellar_particle_flux")
    private Double stellarParticleFlux;

    @Column(name = "radiation_belt_surface_dose", length = 30)
    private String radiationBeltSurfaceDose;

    // ================================================================
    // ATMOSPHERIC HABITABILITY
    // ================================================================
    @Column(name = "is_breathable")
    private Boolean isBreathable = false;

    @Column(name = "breathability_issues", length = 500)
    private String breathabilityIssues;

    @Column(name = "oxygen_percentage")
    private Double oxygenPercentage;

    @Column(name = "oxygen_source", length = 30)
    private String oxygenSource;

    @Column(name = "has_ozone_layer")
    private Boolean hasOzoneLayer = false;

    @Column(name = "ozone_column_dobson")
    private Double ozoneColumnDobson;

    @Column(name = "greenhouse_warming_k")
    private Double greenhouseWarningK;

    @Column(name = "atmospheric_retention_score")
    private Double atmosphericRetentionScore;

    @Column(name = "toxic_gas_hazard", length = 30)
    private String toxicGasHazard;

    // ================================================================
    // LIQUID WATER POTENTIAL
    // ================================================================
    @Column(name = "surface_liquid_water_possible")
    private Boolean surfaceLiquidWaterPossible = false;

    @Column(name = "water_phase_at_surface", length = 30)
    private String waterPhaseAtSurface;

    @Column(name = "habitable_surface_fraction")
    private Double habitableSurfaceFraction;

    @Column(name = "subsurface_ocean_possible")
    private Boolean subsurfaceOceanPossible = false;

    @Column(name = "water_source_likelihood", length = 30)
    private String waterSourceLikelihood;

    @Column(name = "mean_surface_temp_habitable")
    private Boolean meanSurfaceTempHabitable = false;

    // ================================================================
    // GEOLOGICAL HABITABILITY
    // ================================================================
    @Column(name = "has_carbon_cycle")
    private Boolean hasCarbonCycle = false;

    @Column(name = "carbon_cycle_strength", length = 30)
    private String carbonCycleStrength;

    @Column(name = "geothermal_heat_flux_mw_m2")
    private Double geothermalHeatFluxMwM2;

    @Column(name = "magnetic_protection_adequate")
    private Boolean magneticProtectionAdequate = false;

    @Column(name = "tidal_heating_contribution", length = 30)
    private String tidalHeatingContribution;

    @Column(name = "nutrient_cycling_potential", length = 30)
    private String nutrientCyclingPotential;

    // ================================================================
    // ORBITAL & STELLAR ENVIRONMENT
    // ================================================================
    @Column(name = "stellar_lifetime_remaining_my")
    private Double stellarLifetimeRemainingMy;

    @Column(name = "time_in_habitable_zone_my")
    private Double timeInHabitableZoneMy;

    @Column(name = "orbital_stability_score")
    private Double orbitalStabilityScore;

    @Column(name = "tidal_lock_risk", length = 30)
    private String tidalLockRisk;

    @Column(name = "obliquity_stability", length = 30)
    private String obliquityStability;

    @Column(name = "flare_exposure_risk", length = 30)
    private String flareExposureRisk;

    @Column(name = "day_night_temp_range_k")
    private Double dayNightTempRangeK;

    // ================================================================
    // BIOSIGNATURE & LIFE ASSESSMENT
    // ================================================================
    @Column(name = "biosignature_potential", length = 30)
    private String biosignaturePotential;

    @Column(name = "biosignature_indicators", length = 500)
    private String biosignatureIndicators;

    @Column(name = "life_complexity_potential", length = 50)
    private String lifeComplexityPotential;

    @Column(name = "photosynthesis_viable")
    private Boolean photosynthesisViable = false;

    @Column(name = "energy_sources_for_life", length = 500)
    private String energySourcesForLife;

    // ================================================================
    // HABITABILITY CLASSIFICATION
    // ================================================================
    @Enumerated(EnumType.STRING)
    @Column(name = "habitability_class", length = 50, nullable = false)
    private HabitabilityClass habitabilityClass;

    @Column(name = "habitability_score")
    private Double habitabilityScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "terraforming_potential", length = 30)
    private TerraformingPotential terraformingPotential;

    @Column(name = "terraforming_challenges", length = 500)
    private String terraformingChallenges;

    @Enumerated(EnumType.STRING)
    @Column(name = "colonization_suitability", length = 30)
    private ColonizationSuitability colonizationSuitability;

    @Column(name = "limiting_factor", length = 500)
    private String limitingFactor;

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
    public PlanetaryHabitability() {
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }

    // ================================================================
    // PERSISTENCE HELPER
    // ================================================================
    public void preparePersistence() {
        if (this.planet != null && this.planet.getId() != null) {
            this.planetId = this.planet.getId();
        }
    }
}
