package com.brickroad.starcreator_webservice.model.factions;

import com.brickroad.starcreator_webservice.model.starSystems.StarSystem;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "factions", schema = "ud")
public class Faction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private String type;
    private String description;
    private String alignment;
    private int influence;
    private boolean aiCreated;

    @OneToMany(mappedBy = "faction", cascade = CascadeType.ALL)
    private Set<FactionPresence> systemPresences = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "government_type")
    private GovernmentType governmentType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_on")
    private LocalDateTime modifiedOn;

    public Faction() {}

    public Faction(String name, String type, String description, String alignment) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.alignment = alignment;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public int getInfluence() {
        return influence;
    }

    public void setInfluence(int influence) {
        this.influence = influence;
    }

    public GovernmentType getGovernmentType() {
        return governmentType;
    }

    public boolean isAi_created() {
        return aiCreated;
    }

    public void setAi_created(boolean ai_created) {
        this.aiCreated = ai_created;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(LocalDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public Set<StarSystem> getSystems() {
        return systemPresences.stream()
                .map(FactionPresence::getSystem)
                .collect(Collectors.toSet());
    }

    // Helper to get controlled systems
    public Set<StarSystem> getControlledSystems() {
        return systemPresences.stream()
                .filter(FactionPresence::getIsControlling)
                .map(FactionPresence::getSystem)
                .collect(Collectors.toSet());
    }
}
