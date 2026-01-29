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
