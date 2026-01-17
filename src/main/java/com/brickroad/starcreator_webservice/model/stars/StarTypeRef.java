package com.brickroad.starcreator_webservice.model.stars;

import jakarta.persistence.*;

@Entity
@Table(name = "star_type", schema = "ref")
public class StarTypeRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String spectralClass;
    private Double minMass;
    private Double maxMass;
    private Double massRadiusExponent;
    private Double radiusMultiplierMin;
    private Double radiusMultiplierMax;
    private Integer rarityWeight;
    private String description;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpectralClass() {
        return spectralClass;
    }

    public void setSpectralClass(String spectralClass) {
        this.spectralClass = spectralClass;
    }

    public Double getMinMass() {
        return minMass;
    }

    public void setMinMass(Double minMass) {
        this.minMass = minMass;
    }

    public Double getMaxMass() {
        return maxMass;
    }

    public void setMaxMass(Double maxMass) {
        this.maxMass = maxMass;
    }

    public Double getMassRadiusExponent() {
        return massRadiusExponent;
    }

    public void setMassRadiusExponent(Double massRadiusExponent) {
        this.massRadiusExponent = massRadiusExponent;
    }

    public Double getRadiusMultiplierMin() {
        return radiusMultiplierMin;
    }

    public void setRadiusMultiplierMin(Double radiusMultiplierMin) {
        this.radiusMultiplierMin = radiusMultiplierMin;
    }

    public Double getRadiusMultiplierMax() {
        return radiusMultiplierMax;
    }

    public void setRadiusMultiplierMax(Double radiusMultiplierMax) {
        this.radiusMultiplierMax = radiusMultiplierMax;
    }

    public Integer getRarityWeight() {
        return rarityWeight;
    }

    public void setRarityWeight(Integer rarityWeight) {
        this.rarityWeight = rarityWeight;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
