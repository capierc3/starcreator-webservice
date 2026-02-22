package com.brickroad.starcreator_webservice.entity.ud;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name = "star", schema = "ud")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "A star within a star system")
public class Star {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Schema(description = "Designation of this star", example = "SCS-V01-8RQ A")
    private String name;

    // ── System Relationship ──

    @ManyToOne
    @JoinColumn(name = "system_id")
    @JsonBackReference
    private StarSystem system;

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

    // ── Physical Properties (extracted to PhysicalProperties entity) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "physical_properties_id")
    @Schema(description = "Physical properties including mass, radius, luminosity, and temperature")
    private PhysicalProperties physicalProperties;

    // ── Orbital Elements (for multi-star systems) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "orbital_elements_id")
    @Schema(description = "Orbital elements for this star in a multi-star system")
    private OrbitalElements orbit;

    // ── Composition ──

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

    // ── Metadata ──

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @Schema(description = "Timestamp when this star was generated")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    @Schema(description = "Timestamp when this star was last modified")
    private LocalDateTime modifiedAt;

    // ── Physical Properties Convenience Getters/Setters ──

    private PhysicalProperties ensurePhysicalProperties() {
        if (physicalProperties == null) physicalProperties = new PhysicalProperties();
        return physicalProperties;
    }

    @JsonIgnore
    public double getMass() {
        return physicalProperties != null ? physicalProperties.getMass() : 0;
    }
    public void setMass(double mass) { ensurePhysicalProperties().setMass(mass); }

    @JsonIgnore
    public double getRadius() {
        return physicalProperties != null ? physicalProperties.getRadius() : 0;
    }
    public void setRadius(double radius) { ensurePhysicalProperties().setRadius(radius); }

    @JsonIgnore
    public double getCircumference() {
        return physicalProperties != null ? physicalProperties.getCircumference() : 0;
    }
    public void setCircumference(double circumference) { ensurePhysicalProperties().setCircumference(circumference); }

    @JsonIgnore
    public double getSolarMass() {
        return physicalProperties != null && physicalProperties.getSolarMass() != null ? physicalProperties.getSolarMass() : 0;
    }
    public void setSolarMass(double solarMass) { ensurePhysicalProperties().setSolarMass(solarMass); }

    @JsonIgnore
    public double getSolarRadius() {
        return physicalProperties != null && physicalProperties.getSolarRadius() != null ? physicalProperties.getSolarRadius() : 0;
    }
    public void setSolarRadius(double solarRadius) { ensurePhysicalProperties().setSolarRadius(solarRadius); }

    @JsonIgnore
    public double getSolarLuminosity() {
        return physicalProperties != null && physicalProperties.getSolarLuminosity() != null ? physicalProperties.getSolarLuminosity() : 0;
    }
    public void setSolarLuminosity(double solarLuminosity) { ensurePhysicalProperties().setSolarLuminosity(solarLuminosity); }

    @JsonIgnore
    public double getSurfaceTemp() {
        return physicalProperties != null && physicalProperties.getSurfaceTemp() != null ? physicalProperties.getSurfaceTemp() : 0;
    }
    public void setSurfaceTemp(double surfaceTemp) { ensurePhysicalProperties().setSurfaceTemp(surfaceTemp); }

    // ── Orbital Convenience Getters/Setters ──

    @JsonIgnore
    public Double getDistanceFromStar() {
        return orbit != null ? orbit.getDistanceFromParent() : null;
    }

    public void setDistanceFromStar(Double distance) {
        if (orbit == null) orbit = new OrbitalElements();
        orbit.setDistanceFromParent(distance);
    }

    @JsonIgnore
    public Integer getOrbitalOrder() {
        return orbit != null ? orbit.getOrbitalOrder() : null;
    }

    public void setOrbitalOrder(Integer orbitalOrder) {
        if (orbit == null) orbit = new OrbitalElements();
        orbit.setOrbitalOrder(orbitalOrder);
    }

    // ── Derived Properties (not persisted) ──

    /**
     * Returns the companion star in a multi-star system.
     * For a SECONDARY star, returns the PRIMARY; for PRIMARY/TERTIARY, returns the SECONDARY.
     * Returns null for single-star systems.
     */
    @JsonIgnore
    public Star getCompanionStar() {
        if (system == null || system.getStars() == null) {
            return null;
        }
        if (starRole == StarRole.SECONDARY) {
            return system.getStars().stream()
                    .filter(star -> star.getStarRole() == StarRole.PRIMARY)
                    .findFirst().orElse(null);
        } else {
            return system.getStars().stream()
                    .filter(star -> star.getStarRole() == StarRole.SECONDARY)
                    .findFirst().orElse(null);
        }
    }
}
