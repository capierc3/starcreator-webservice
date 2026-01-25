package com.brickroad.starcreator_webservice.entity.ref;

import com.brickroad.starcreator_webservice.enums.AtmosphereClassification;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "atmosphere_template", schema = "ref")
public class AtmosphereTemplateRef {

    // Getters and Setters
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
