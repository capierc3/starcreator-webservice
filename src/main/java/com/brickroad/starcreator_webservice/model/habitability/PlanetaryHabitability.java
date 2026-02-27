package com.brickroad.starcreator_webservice.model.habitability;

import com.brickroad.starcreator_webservice.enums.ColonizationSuitability;
import com.brickroad.starcreator_webservice.enums.HabitabilityClass;
import com.brickroad.starcreator_webservice.enums.TerraformingPotential;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Habitability assessment for a planet or moon.
 * <p>
 * This is a transient POJO — fully regenerated from the persisted {@code climateSeed}
 * via {@link com.brickroad.starcreator_webservice.creator.HabitabilityCreator} at load
 * time. Nothing in this class is persisted to the database.
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Habitability assessment for a planet or moon, including ESI scores, radiation environment, atmospheric breathability, water potential, geological factors, and colonization suitability")
public class PlanetaryHabitability {

    // ================================================================
    // EARTH SIMILARITY INDEX
    // ================================================================
    @Schema(description = "Earth Similarity Index total score (0-1)", example = "0.82")
    private Double esiTotal;

    @Schema(description = "ESI interior subscore based on mass and radius", example = "0.85")
    private Double esiInterior;

    @Schema(description = "ESI surface subscore based on temperature and escape velocity", example = "0.79")
    private Double esiSurface;

    // ================================================================
    // RADIATION ENVIRONMENT
    // ================================================================
    @Schema(description = "UV surface flux relative to Earth (1.0 = Earth)", example = "0.95")
    private Double uvSurfaceFluxEarth;

    @Schema(description = "UV radiation hazard level", example = "LOW")
    private String uvHazardLevel;

    @Schema(description = "Ionizing radiation at surface in mSv/year", example = "2.4")
    private Double ionizingRadiationSurfaceMsvYr;

    @Schema(description = "Cosmic ray flux relative to Earth", example = "0.8")
    private Double cosmicRayFluxEarth;

    @Schema(description = "Stellar particle flux intensity")
    private Double stellarParticleFlux;

    @Schema(description = "Radiation belt dose at surface", example = "NEGLIGIBLE")
    private String radiationBeltSurfaceDose;

    // ================================================================
    // ATMOSPHERIC HABITABILITY
    // ================================================================
    @Schema(description = "Whether the atmosphere is breathable by humans")
    private Boolean isBreathable = false;

    @Schema(description = "Issues preventing breathability, if any")
    private String breathabilityIssues;

    @Schema(description = "Atmospheric oxygen percentage", example = "20.9")
    private Double oxygenPercentage;

    @Schema(description = "Primary source of atmospheric oxygen", example = "BIOLOGICAL")
    private String oxygenSource;

    @Schema(description = "Whether an ozone layer is present")
    private Boolean hasOzoneLayer = false;

    @Schema(description = "Ozone column density in Dobson units", example = "300")
    private Double ozoneColumnDobson;

    @Schema(description = "Greenhouse warming contribution in Kelvin", example = "33")
    private Double greenhouseWarningK;

    @Schema(description = "Atmosphere retention score (0-1)", example = "0.95")
    private Double atmosphericRetentionScore;

    @Schema(description = "Toxic gas hazard level", example = "NONE")
    private String toxicGasHazard;

    // ================================================================
    // LIQUID WATER POTENTIAL
    // ================================================================
    @Schema(description = "Whether surface liquid water is thermodynamically possible")
    private Boolean surfaceLiquidWaterPossible = false;

    @Schema(description = "Dominant water phase at the surface", example = "LIQUID")
    private String waterPhaseAtSurface;

    @Schema(description = "Fraction of surface within habitable temperature range (0-1)", example = "0.85")
    private Double habitableSurfaceFraction;

    @Schema(description = "Whether a subsurface ocean is possible")
    private Boolean subsurfaceOceanPossible = false;

    @Schema(description = "Likelihood of water sources", example = "HIGH")
    private String waterSourceLikelihood;

    // ================================================================
    // GEOLOGICAL HABITABILITY
    // ================================================================
    @Schema(description = "Whether a carbon-silicate cycle operates")
    private Boolean hasCarbonCycle = false;

    @Schema(description = "Strength of the carbon cycle", example = "STRONG")
    private String carbonCycleStrength;

    @Schema(description = "Geothermal heat flux in mW/m\u00b2", example = "95")
    private Double geothermalHeatFluxMwM2;

    @Schema(description = "Whether magnetic field provides adequate atmospheric protection")
    private Boolean magneticProtectionAdequate = false;

    @Schema(description = "Contribution of tidal heating to habitability", example = "NONE")
    private String tidalHeatingContribution;

    @Schema(description = "Potential for nutrient cycling processes", example = "HIGH")
    private String nutrientCyclingPotential;

    // ================================================================
    // ORBITAL & STELLAR ENVIRONMENT
    // ================================================================
    @Schema(description = "Remaining stellar main-sequence lifetime in millions of years")
    private Double stellarLifetimeRemainingMy;

    @Schema(description = "Time spent in the habitable zone in millions of years")
    private Double timeInHabitableZoneMy;

    @Schema(description = "Orbital stability score (0-1)", example = "0.95")
    private Double orbitalStabilityScore;

    @Schema(description = "Risk of tidal locking", example = "LOW")
    private String tidalLockRisk;

    @Schema(description = "Axial tilt stability classification", example = "STABLE")
    private String obliquityStability;

    @Schema(description = "Risk from stellar flare exposure", example = "LOW")
    private String flareExposureRisk;

    @Schema(description = "Day-night temperature range in Kelvin", example = "12.5")
    private Double dayNightTempRangeK;

    // ================================================================
    // BIOSIGNATURE & LIFE ASSESSMENT
    // ================================================================
    @Schema(description = "Potential for detectable biosignatures", example = "HIGH")
    private String biosignaturePotential;

    @Schema(description = "Specific biosignature indicators detected")
    private String biosignatureIndicators;

    @Schema(description = "Maximum potential life complexity", example = "COMPLEX_MULTICELLULAR")
    private String lifeComplexityPotential;

    @Schema(description = "Whether photosynthesis is viable given stellar spectrum and atmosphere")
    private Boolean photosynthesisViable = false;

    @Schema(description = "Available energy sources for life")
    private String energySourcesForLife;

    // ================================================================
    // HABITABILITY CLASSIFICATION
    // ================================================================
    @Schema(description = "Overall habitability classification", example = "EARTH_ANALOG")
    private HabitabilityClass habitabilityClass;

    @Schema(description = "Composite habitability score (0-100)", example = "87.5")
    private Double habitabilityScore;

    @Schema(description = "Terraforming feasibility assessment", example = "UNNECESSARY")
    private TerraformingPotential terraformingPotential;

    @Schema(description = "Key challenges for terraforming")
    private String terraformingChallenges;

    @Schema(description = "Colonization suitability classification", example = "SHIRT_SLEEVE")
    private ColonizationSuitability colonizationSuitability;

    @Schema(description = "Primary factor limiting habitability")
    private String limitingFactor;
}
