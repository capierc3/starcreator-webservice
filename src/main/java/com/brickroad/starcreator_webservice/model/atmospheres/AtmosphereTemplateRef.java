package com.brickroad.starcreator_webservice.model.atmospheres;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "atmosphere_template", schema = "ref")
public class AtmosphereTemplateRef {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AtmosphereClassification classification;
    
    @Column(name = "is_breathable")
    private Boolean isBreathable;
    
    @Column(name = "typical_pressure_bar")
    private Double typicalPressureBar;
    
    @Column(name = "min_temperature_k")
    private Double minTemperatureK;
    
    @Column(name = "max_temperature_k")
    private Double maxTemperatureK;
    
    @Column(name = "min_planet_mass_earth")
    private Double minPlanetMassEarth;
    
    @Column(name = "max_planet_mass_earth")
    private Double maxPlanetMassEarth;
    
    @Column(name = "rarity_weight")
    private Integer rarityWeight;
    
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<AtmosphereTemplateComponentRef> components = new ArrayList<>();
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public AtmosphereTemplateRef() {
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public AtmosphereClassification getClassification() {
        return classification;
    }
    
    public void setClassification(AtmosphereClassification classification) {
        this.classification = classification;
    }
    
    public Boolean getIsBreathable() {
        return isBreathable;
    }
    
    public void setIsBreathable(Boolean breathable) {
        isBreathable = breathable;
    }
    
    public Double getTypicalPressureBar() {
        return typicalPressureBar;
    }
    
    public void setTypicalPressureBar(Double typicalPressureBar) {
        this.typicalPressureBar = typicalPressureBar;
    }
    
    public Double getMinTemperatureK() {
        return minTemperatureK;
    }
    
    public void setMinTemperatureK(Double minTemperatureK) {
        this.minTemperatureK = minTemperatureK;
    }
    
    public Double getMaxTemperatureK() {
        return maxTemperatureK;
    }
    
    public void setMaxTemperatureK(Double maxTemperatureK) {
        this.maxTemperatureK = maxTemperatureK;
    }
    
    public Double getMinPlanetMassEarth() {
        return minPlanetMassEarth;
    }
    
    public void setMinPlanetMassEarth(Double minPlanetMassEarth) {
        this.minPlanetMassEarth = minPlanetMassEarth;
    }
    
    public Double getMaxPlanetMassEarth() {
        return maxPlanetMassEarth;
    }
    
    public void setMaxPlanetMassEarth(Double maxPlanetMassEarth) {
        this.maxPlanetMassEarth = maxPlanetMassEarth;
    }
    
    public Integer getRarityWeight() {
        return rarityWeight;
    }
    
    public void setRarityWeight(Integer rarityWeight) {
        this.rarityWeight = rarityWeight;
    }
    
    public List<AtmosphereTemplateComponentRef> getComponents() {
        return components;
    }
    
    public void setComponents(List<AtmosphereTemplateComponentRef> components) {
        this.components = components;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Check if this template matches the given planet conditions
     */
    public boolean matches(double temperatureK, double planetMassEarth) {
        boolean tempMatch = (minTemperatureK == null || temperatureK >= minTemperatureK) &&
                           (maxTemperatureK == null || temperatureK <= maxTemperatureK);
        boolean massMatch = (minPlanetMassEarth == null || planetMassEarth >= minPlanetMassEarth) &&
                           (maxPlanetMassEarth == null || planetMassEarth <= maxPlanetMassEarth);
        return tempMatch && massMatch;
    }
}
