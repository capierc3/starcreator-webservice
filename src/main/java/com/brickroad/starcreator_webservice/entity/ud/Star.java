package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "star", schema = "ud")
public class Star extends CelestialBody {

    private String type;
    private double solarMass;
    private double solarRadius;
    private String spectralType;
    private double solarLuminosity;
    private double surfaceTemp;
    @Column(name = "age_millions_years")
    private Double ageMY;
    private Double metallicity;
    private Double rotationDays;
    private String colorIndex;
    private Boolean isVariable;
    @Column(name = "variability_period_days")
    private Double variabilityPeriod;

    private Double habitableZoneInnerAU;
    private Double habitableZoneOuterAU;

    @Enumerated(EnumType.STRING)
    @Column(name = "star_role")
    private StarRole starRole;

    public enum StarRole {
        PRIMARY,      // Main star
        SECONDARY,    // Companion star
        TERTIARY      // Third star in trinary
    }

    private Double activityCycleYears;
    private Double activityCyclePhase;
    private String activityLevel;

    private Boolean inGrandMinimum;
    private Double grandMinimumDurationYears;
    private Double grandMinimumDepth;

    private Double flareFrequencyPerDay;
    @Column(name = "max_flare_energy_ergs")
    private Double maxFlareEnergyErgs;
    private String flareClass;
    private Boolean superflareCapable;

    private Double starspotCoveragePercent;
    @Column(name = "starspot_temp_contrast_k")
    private Double starspotTempContrastK;
    private Boolean hasPolarSpots;

    private Double stellarWindMassLossRate;
    @Column(name = "stellar_wind_velocity_km_s")
    private Double stellarWindVelocityKmS;
    @Column(name = "stellar_wind_density_at_1au")
    private Double stellarWindDensityAt1AU;

    @Column(name = "coronal_temp_mk")
    private Double coronalTempMK;
    private String xrayLuminosityClass;
    private Boolean hasCorona;

    private Double mainSequenceFraction;
    private String evolutionaryStage;
    @Column(name = "estimated_remaining_ms_my")
    private Double estimatedRemainingMsMy;

    private Double logRPrimeHk;
    private Double rossbyNumber;

    public Star() {}

    public Boolean isVariable() {
        return isVariable;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
    public Star getCompanionStar() {
        if (starRole == StarRole.SECONDARY) {
            return this.getSystem().getStars().stream()
                    .filter(star -> star.getStarRole() == StarRole.PRIMARY)
                    .findFirst().orElse(null);
        } else {
            return this.getSystem().getStars().stream()
                    .filter(star -> star.getStarRole() == StarRole.SECONDARY)
                    .findFirst().orElse(null);
        }
    }
}
