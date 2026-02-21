package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "star", schema = "ud")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "A star within a star system")
public class Star extends CelestialBody {

    // ── Identity & Classification ──

    @Schema(description = "Star type from reference data", example = "Main Sequence M")
    private String type;

    @Schema(description = "Spectral class", example = "M")
    private String spectralType;

    @Enumerated(EnumType.STRING)
    @Column(name = "star_role")
    @Schema(description = "Role in a multi-star system", example = "PRIMARY")
    private StarRole starRole;

    @Schema(description = "Visible color based on surface temperature", example = "Red")
    private String colorIndex;

    public enum StarRole {
        PRIMARY,
        SECONDARY,
        TERTIARY
    }

    // ── Physical Properties ──

    @Schema(description = "Mass in solar masses (1.0 = Sun)", example = "0.37")
    private double solarMass;

    @Schema(description = "Radius in solar radii (1.0 = Sun)", example = "0.51")
    private double solarRadius;

    @Schema(description = "Luminosity in solar luminosities (1.0 = Sun)", example = "0.031")
    private double solarLuminosity;

    @Schema(description = "Surface temperature in Kelvin", example = "2931")
    private double surfaceTemp;

    @Schema(description = "Metallicity [Fe/H] relative to Sun (0.0 = solar)", example = "-0.44")
    private Double metallicity;

    @Schema(description = "Rotation period in days", example = "36.1")
    private Double rotationDays;

    // ── Age & Evolution ──

    @Column(name = "age_millions_years")
    @Schema(description = "Age in millions of years", example = "280.1")
    private Double ageMY;

    @Schema(description = "Current evolutionary stage", example = "EARLY_MAIN_SEQUENCE")
    private String evolutionaryStage;

    @Schema(description = "Fraction of main sequence lifespan elapsed (0.0-1.0)", example = "0.002")
    private Double mainSequenceFraction;

    @Column(name = "estimated_remaining_ms_my")
    @Schema(description = "Estimated remaining main sequence lifetime in millions of years", example = "118740")
    private Double estimatedRemainingMsMy;

    // ── Habitable Zone ──

    @Schema(description = "Inner edge of the habitable zone in AU", example = "0.167")
    private Double habitableZoneInnerAU;

    @Schema(description = "Outer edge of the habitable zone in AU", example = "0.241")
    private Double habitableZoneOuterAU;

    // ── Variability ──

    @Schema(description = "Whether this star is variable", example = "false")
    private Boolean isVariable;

    @Column(name = "variability_period_days")
    @Schema(description = "Variability period in days (if variable)")
    private Double variabilityPeriod;

    // ── Chromospheric Activity ──

    @Schema(description = "Overall activity level", example = "VERY_ACTIVE")
    private String activityLevel;

    @Schema(description = "Log R'HK chromospheric activity index", example = "-4.50")
    private Double logRPrimeHk;

    @Schema(description = "Rossby number (rotation period / convective turnover time)", example = "0.40")
    private Double rossbyNumber;

    // ── Activity Cycle ──

    @Schema(description = "Activity cycle period in years (like the Sun's ~11 year cycle)", example = "10.8")
    private Double activityCycleYears;

    @Schema(description = "Current phase in the activity cycle (0.0-1.0)", example = "0.30")
    private Double activityCyclePhase;

    // ── Grand Minimum ──

    @Schema(description = "Whether the star is currently in a grand activity minimum (like the Maunder Minimum)")
    private Boolean inGrandMinimum;

    @Schema(description = "Duration of the grand minimum in years")
    private Double grandMinimumDurationYears;

    @Schema(description = "Depth of the grand minimum (0.0-1.0)")
    private Double grandMinimumDepth;

    // ── Flare Activity ──

    @Schema(description = "Average flare frequency per day", example = "1.67")
    private Double flareFrequencyPerDay;

    @Column(name = "max_flare_energy_ergs")
    @Schema(description = "Log of maximum flare energy in ergs", example = "30.0")
    private Double maxFlareEnergyErgs;

    @Schema(description = "Flare classification", example = "C_CLASS")
    private String flareClass;

    @Schema(description = "Whether the star is capable of producing superflares")
    private Boolean superflareCapable;

    // ── Starspots ──

    @Schema(description = "Percentage of stellar surface covered by starspots", example = "3.83")
    private Double starspotCoveragePercent;

    @Column(name = "starspot_temp_contrast_k")
    @Schema(description = "Temperature contrast of starspots vs photosphere in Kelvin", example = "336")
    private Double starspotTempContrastK;

    @Schema(description = "Whether the star has polar starspot concentrations")
    private Boolean hasPolarSpots;

    // ── Stellar Wind ──

    @Schema(description = "Mass loss rate in solar masses per year", example = "1.07e-14")
    private Double stellarWindMassLossRate;

    @Column(name = "stellar_wind_velocity_km_s")
    @Schema(description = "Stellar wind velocity in km/s", example = "268")
    private Double stellarWindVelocityKmS;

    @Column(name = "stellar_wind_density_at_1au")
    @Schema(description = "Stellar wind particle density at 1 AU (relative to solar)", example = "4.36")
    private Double stellarWindDensityAt1AU;

    // ── Corona ──

    @Schema(description = "Whether the star has a corona")
    private Boolean hasCorona;

    @Column(name = "coronal_temp_mk")
    @Schema(description = "Coronal temperature in millions of Kelvin", example = "3.54")
    private Double coronalTempMK;

    @Schema(description = "X-ray luminosity classification", example = "MODERATE")
    private String xrayLuminosityClass;

    // ── Derived Properties (not persisted) ──

    /**
     * Returns the companion star in a multi-star system.
     * For a SECONDARY star, returns the PRIMARY; for PRIMARY/TERTIARY, returns the SECONDARY.
     * Returns null for single-star systems.
     */
    @JsonIgnore
    public Star getCompanionStar() {
        if (getSystem() == null || getSystem().getStars() == null) {
            return null;
        }
        if (starRole == StarRole.SECONDARY) {
            return getSystem().getStars().stream()
                    .filter(star -> star.getStarRole() == StarRole.PRIMARY)
                    .findFirst().orElse(null);
        } else {
            return getSystem().getStars().stream()
                    .filter(star -> star.getStarRole() == StarRole.SECONDARY)
                    .findFirst().orElse(null);
        }
    }
}
