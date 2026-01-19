package com.brickroad.starcreator_webservice.model.compositions;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Database entity representing a composition template.
 * Each template defines the typical composition for certain planet types/conditions.
 */
@Entity
@Table(name = "composition_template", schema = "ref")
public class CompositionTemplateRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CompositionClassification classification;

    @Column(name = "planet_types", length = 500)
    private String planetTypes; // Comma-separated: "Terrestrial Planet,Super-Earth,Desert Planet"

    @Column(name = "min_distance_au")
    private Double minDistanceAu;

    @Column(name = "max_distance_au")
    private Double maxDistanceAu;

    @Column(name = "rarity_weight")
    private Integer rarityWeight = 100;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<CompositionTemplateComponentRef> components = new ArrayList<>();

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public CompositionClassification getClassification() { return classification; }
    public void setClassification(CompositionClassification classification) { 
        this.classification = classification; 
    }
    
    public String getPlanetTypes() { return planetTypes; }
    public void setPlanetTypes(String planetTypes) { this.planetTypes = planetTypes; }
    
    public Double getMinDistanceAu() { return minDistanceAu; }
    public void setMinDistanceAu(Double minDistanceAu) { this.minDistanceAu = minDistanceAu; }
    
    public Double getMaxDistanceAu() { return maxDistanceAu; }
    public void setMaxDistanceAu(Double maxDistanceAu) { this.maxDistanceAu = maxDistanceAu; }
    
    public Integer getRarityWeight() { return rarityWeight; }
    public void setRarityWeight(Integer rarityWeight) { this.rarityWeight = rarityWeight; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<CompositionTemplateComponentRef> getComponents() { return components; }
    public void setComponents(List<CompositionTemplateComponentRef> components) { 
        this.components = components; 
    }

    /**
     * Check if this template matches the given planet type and distance
     */
    public boolean matches(String planetType, double distanceAU) {
        boolean typeMatch = planetTypes == null || 
                           planetTypes.toLowerCase().contains(planetType.toLowerCase());
        
        boolean distanceMatch = (minDistanceAu == null || distanceAU >= minDistanceAu) &&
                                (maxDistanceAu == null || distanceAU <= maxDistanceAu);
        
        return typeMatch && distanceMatch;
    }

    public List<CompositionTemplateComponentRef> getComponentsForLayer(String layerType) {
        return components.stream()
                .filter(c -> layerType.equals(c.getLayerType()))
                .toList();
    }
}
