package com.brickroad.starcreator_webservice.model.geological;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "geological_template", schema = "ref")
public class GeologicalTemplateRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "planet_types", length = 500)
    private String planetTypes; // Comma-separated

    @Column(name = "activity_level", nullable = false, length = 50)
    private String activityLevel; // "Highly Active", "Moderately Active", etc.

    @Column(name = "min_activity_score")
    private Double minActivityScore;

    @Column(name = "max_activity_score")
    private Double maxActivityScore;

    @Column(name = "min_planet_mass_earth")
    private Double minPlanetMassEarth;

    @Column(name = "max_planet_mass_earth")
    private Double maxPlanetMassEarth;

    @Column(name = "rarity_weight")
    private Integer rarityWeight = 100;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<GeologicalFeatureRef> features = new ArrayList<>();

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPlanetTypes() { return planetTypes; }
    public void setPlanetTypes(String planetTypes) { this.planetTypes = planetTypes; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public Double getMinActivityScore() { return minActivityScore; }
    public void setMinActivityScore(Double minActivityScore) { this.minActivityScore = minActivityScore; }

    public Double getMaxActivityScore() { return maxActivityScore; }
    public void setMaxActivityScore(Double maxActivityScore) { this.maxActivityScore = maxActivityScore; }

    public Double getMinPlanetMassEarth() { return minPlanetMassEarth; }
    public void setMinPlanetMassEarth(Double minPlanetMassEarth) { this.minPlanetMassEarth = minPlanetMassEarth; }

    public Double getMaxPlanetMassEarth() { return maxPlanetMassEarth; }
    public void setMaxPlanetMassEarth(Double maxPlanetMassEarth) { this.maxPlanetMassEarth = maxPlanetMassEarth; }

    public Integer getRarityWeight() { return rarityWeight; }
    public void setRarityWeight(Integer rarityWeight) { this.rarityWeight = rarityWeight; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<GeologicalFeatureRef> getFeatures() { return features; }
    public void setFeatures(List<GeologicalFeatureRef> features) { this.features = features; }
}
