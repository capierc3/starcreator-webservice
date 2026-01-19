package com.brickroad.starcreator_webservice.model.planets;

import com.brickroad.starcreator_webservice.model.CelestialBody;
import jakarta.persistence.*;

@Entity
@Table(name = "planet", schema = "ud")
public class Planet extends CelestialBody {

    @Column(name = "planet_type")
    private String planetType;

    @Column(name = "earth_mass")
    private Double earthMass;

    @Column(name = "earth_radius")
    private Double earthRadius;

    @Column(name = "orbital_period_days")
    private Double orbitalPeriodDays;

    @Column(name = "semi_major_axis_au")
    private Double semiMajorAxisAU;

    @Column(name = "eccentricity")
    private Double eccentricity;

    @Column(name = "orbital_inclination_degrees")
    private Double orbitalInclinationDegrees;

    @Column(name = "surface_temp_kelvin")
    private Double surfaceTemp;

    @Column(name = "surface_pressure_atm")
    private Double surfacePressure;

    @Column(name = "escape_velocity_km_s")
    private Double escapeVelocity;

    @Column(name = "surface_gravity_g")
    private Double surfaceGravity;

    @Column(name = "rotation_period_hours")
    private Double rotationPeriodHours;

    @Column(name = "axial_tilt_degrees")
    private Double axialTilt;

    @Column(name = "magnetic_field_strength")
    private Double magneticFieldStrength;

    @Column(name = "atmosphere_composition")
    private String atmosphereComposition;

    @Column(name = "has_rings")
    private Boolean hasRings;

    @Column(name = "number_of_moons")
    private Integer numberOfMoons;

    @Column(name = "albedo")
    private Double albedo;

    @Column(name = "density_g_cm3")
    private Double density;

    @Column(name = "is_tidally_locked")
    private Boolean isTidallyLocked;

    @Column(name = "habitable_zone_position")
    private String habitableZonePosition;

    @Column(name = "water_coverage_percent")
    private Double waterCoveragePercent;

    @Column(name = "core_type")
    private String coreType;

    @Column(name = "geological_activity")
    private String geologicalActivity;

    @Column(name = "age_millions_years")
    private Double ageMY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "star_id")
    private com.brickroad.starcreator_webservice.model.stars.Star parentStar;

    @Column(name = "atmosphere_classification", length = 50)
    private String atmosphereClassification;

    @Column(name = "interior_composition", length = 500)
    private String interiorComposition;

    @Column(name = "envelope_composition", length = 500)
    private String envelopeComposition;

    @Column(name = "composition_classification", length = 50)
    private String compositionClassification;

    public Planet() {}

    // Getters and Setters

    public String getPlanetType() {
        return planetType;
    }

    public void setPlanetType(String planetType) {
        this.planetType = planetType;
    }

    public Double getEarthMass() {
        return earthMass;
    }

    public void setEarthMass(Double earthMass) {
        this.earthMass = earthMass;
    }

    public Double getEarthRadius() {
        return earthRadius;
    }

    public void setEarthRadius(Double earthRadius) {
        this.earthRadius = earthRadius;
    }

    public Double getOrbitalPeriodDays() {
        return orbitalPeriodDays;
    }

    public void setOrbitalPeriodDays(Double orbitalPeriodDays) {
        this.orbitalPeriodDays = orbitalPeriodDays;
    }

    public Double getSemiMajorAxisAU() {
        return semiMajorAxisAU;
    }

    public void setSemiMajorAxisAU(Double semiMajorAxisAU) {
        this.semiMajorAxisAU = semiMajorAxisAU;
    }

    public Double getEccentricity() {
        return eccentricity;
    }

    public void setEccentricity(Double eccentricity) {
        this.eccentricity = eccentricity;
    }

    public Double getOrbitalInclinationDegrees() {
        return orbitalInclinationDegrees;
    }

    public void setOrbitalInclinationDegrees(Double orbitalInclinationDegrees) {
        this.orbitalInclinationDegrees = orbitalInclinationDegrees;
    }

    public Double getSurfaceTemp() {
        return surfaceTemp;
    }

    public void setSurfaceTemp(Double surfaceTemp) {
        this.surfaceTemp = surfaceTemp;
    }

    public Double getSurfacePressure() {
        return surfacePressure;
    }

    public void setSurfacePressure(Double surfacePressure) {
        this.surfacePressure = surfacePressure;
    }

    public Double getEscapeVelocity() {
        return escapeVelocity;
    }

    public void setEscapeVelocity(Double escapeVelocity) {
        this.escapeVelocity = escapeVelocity;
    }

    public Double getSurfaceGravity() {
        return surfaceGravity;
    }

    public void setSurfaceGravity(Double surfaceGravity) {
        this.surfaceGravity = surfaceGravity;
    }

    public Double getRotationPeriodHours() {
        return rotationPeriodHours;
    }

    public void setRotationPeriodHours(Double rotationPeriodHours) {
        this.rotationPeriodHours = rotationPeriodHours;
    }

    public Double getAxialTilt() {
        return axialTilt;
    }

    public void setAxialTilt(Double axialTilt) {
        this.axialTilt = axialTilt;
    }

    public Double getMagneticFieldStrength() {
        return magneticFieldStrength;
    }

    public void setMagneticFieldStrength(Double magneticFieldStrength) {
        this.magneticFieldStrength = magneticFieldStrength;
    }

    public String getAtmosphereComposition() {
        return atmosphereComposition;
    }

    public void setAtmosphereComposition(String atmosphereComposition) {
        this.atmosphereComposition = atmosphereComposition;
    }

    public Boolean getHasRings() {
        return hasRings;
    }

    public void setHasRings(Boolean hasRings) {
        this.hasRings = hasRings;
    }

    public Integer getNumberOfMoons() {
        return numberOfMoons;
    }

    public void setNumberOfMoons(Integer numberOfMoons) {
        this.numberOfMoons = numberOfMoons;
    }

    public Double getAlbedo() {
        return albedo;
    }

    public void setAlbedo(Double albedo) {
        this.albedo = albedo;
    }

    public Double getDensity() {
        return density;
    }

    public void setDensity(Double density) {
        this.density = density;
    }

    public Boolean getTidallyLocked() {
        return isTidallyLocked;
    }

    public void setTidallyLocked(Boolean tidallyLocked) {
        isTidallyLocked = tidallyLocked;
    }

    public String getHabitableZonePosition() {
        return habitableZonePosition;
    }

    public void setHabitableZonePosition(String habitableZonePosition) {
        this.habitableZonePosition = habitableZonePosition;
    }

    public Double getWaterCoveragePercent() {
        return waterCoveragePercent;
    }

    public void setWaterCoveragePercent(Double waterCoveragePercent) {
        this.waterCoveragePercent = waterCoveragePercent;
    }

    public String getCoreType() {
        return coreType;
    }

    public void setCoreType(String coreType) {
        this.coreType = coreType;
    }

    public String getGeologicalActivity() {
        return geologicalActivity;
    }

    public void setGeologicalActivity(String geologicalActivity) {
        this.geologicalActivity = geologicalActivity;
    }

    public Double getAgeMY() {
        return ageMY;
    }

    public void setAgeMY(Double ageMY) {
        this.ageMY = ageMY;
    }

    public com.brickroad.starcreator_webservice.model.stars.Star getParentStar() {
        return parentStar;
    }

    public void setParentStar(com.brickroad.starcreator_webservice.model.stars.Star parentStar) {
        this.parentStar = parentStar;
    }

    public Integer getOrbitalPosition() {
        return getOrbitalOrder();
    }

    public void setOrbitalPosition(Integer position) {
        setOrbitalOrder(position);
    }

    public String getAtmosphereClassification() {
        return atmosphereClassification;
    }

    public void setAtmosphereClassification(String atmosphereClassification) {
        this.atmosphereClassification = atmosphereClassification;
    }

    public String getInteriorComposition() { return interiorComposition; }

    public void setInteriorComposition(String interiorComposition) {
        this.interiorComposition = interiorComposition;
    }
    public String getEnvelopeComposition() { return envelopeComposition; }

    public void setEnvelopeComposition(String envelopeComposition) {
        this.envelopeComposition = envelopeComposition;
    }

    public String getCompositionClassification() {
        return compositionClassification;
    }

    public void setCompositionClassification(String compositionClassification) {
        this.compositionClassification = compositionClassification;
    }
}
