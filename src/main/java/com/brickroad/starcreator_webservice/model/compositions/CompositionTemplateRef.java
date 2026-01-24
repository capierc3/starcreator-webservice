package com.brickroad.starcreator_webservice.model.compositions;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private String planetTypes;

    @Column(name = "min_surface_temp_k")
    private Double minSurfaceTempK;

    @Column(name = "max_surface_temp_k")
    private Double maxSurfaceTempK;

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
    
    public Integer getRarityWeight() { return rarityWeight; }
    public void setRarityWeight(Integer rarityWeight) { this.rarityWeight = rarityWeight; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<CompositionTemplateComponentRef> getComponents() { return components; }
    public void setComponents(List<CompositionTemplateComponentRef> components) { 
        this.components = components; 
    }

    public Double getMinSurfaceTempK() {
        return minSurfaceTempK;
    }

    public void setMinSurfaceTempK(Double minSurfaceTempK) {
        this.minSurfaceTempK = minSurfaceTempK;
    }

    public Double getMaxSurfaceTempK() {
        return maxSurfaceTempK;
    }

    public void setMaxSurfaceTempK(Double maxSurfaceTempK) {
        this.maxSurfaceTempK = maxSurfaceTempK;
    }

    public boolean matches(String planetType, double surfaceTempK) {
        double tempToleranceK = 5.0;
        boolean typeMatch = planetTypes == null ||
                planetTypes.toLowerCase().contains(planetType.toLowerCase());

        boolean tempMatch = (minSurfaceTempK == null || surfaceTempK >= (minSurfaceTempK - tempToleranceK)) &&
                (maxSurfaceTempK == null || surfaceTempK <= (maxSurfaceTempK + tempToleranceK));

        return typeMatch && tempMatch;
    }

    public List<CompositionTemplateComponentRef> getComponentsForLayer(String layerType) {
        return components.stream()
                .filter(c -> layerType.equals(c.getLayerType()))
                .toList();
    }
}
