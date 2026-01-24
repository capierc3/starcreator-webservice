package com.brickroad.starcreator_webservice.model.stars;

import com.brickroad.starcreator_webservice.model.CelestialBody;
import jakarta.persistence.*;

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

    public double getSolarMass() {
        return solarMass;
    }

    public void setSolarMass(double solarMass) {
        this.solarMass = solarMass;
    }

    public double getSolarRadius() {
        return solarRadius;
    }

    public void setSolarRadius(double solarRadius) {
        this.solarRadius = solarRadius;
    }

    public String getSpectralType() {
        return spectralType;
    }

    public void setSpectralType(String spectralType) {
        this.spectralType = spectralType;
    }

    public double getSolarLuminosity() {
        return solarLuminosity;
    }

    public void setSolarLuminosity(double solarLuminosity) {
        this.solarLuminosity = solarLuminosity;
    }

    public double getSurfaceTemp() {
        return surfaceTemp;
    }

    public void setSurfaceTemp(double surfaceTemp) {
        this.surfaceTemp = surfaceTemp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getAgeMY() {
        return ageMY;
    }

    public void setAgeMY(Double ageMY) {
        this.ageMY = ageMY;
    }

    public Double getMetallicity() {
        return metallicity;
    }

    public void setMetallicity(Double metallicity) {
        this.metallicity = metallicity;
    }

    public Double getRotationDays() {
        return rotationDays;
    }

    public void setRotationDays(Double rotationDays) {
        this.rotationDays = rotationDays;
    }

    public String getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(String colorIndex) {
        this.colorIndex = colorIndex;
    }

    public Boolean isVariable() {
        return isVariable;
    }

    public void setIsVariable(Boolean variable) {
        isVariable = variable;
    }

    public Double getVariabilityPeriod() {
        return variabilityPeriod;
    }

    public void setVariabilityPeriod(Double variabilityPeriod) {
        this.variabilityPeriod = variabilityPeriod;
    }

    public StarRole getStarRole() {
        return starRole;
    }

    public void setStarRole(StarRole starRole) {
        this.starRole = starRole;
    }

    public Double getHabitableZoneInnerAU() {
        return habitableZoneInnerAU;
    }

    public void setHabitableZoneInnerAU(Double habitableZoneInnerAU) {
        this.habitableZoneInnerAU = habitableZoneInnerAU;
    }

    public Double getHabitableZoneOuterAU() {
        return habitableZoneOuterAU;
    }

    public void setHabitableZoneOuterAU(Double habitableZoneOuterAU) {
        this.habitableZoneOuterAU = habitableZoneOuterAU;
    }

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
