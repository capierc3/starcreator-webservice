package com.brickroad.starcreator_webservice.entity.ud;

import com.brickroad.starcreator_webservice.entity.ref.GovernmentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
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

    public boolean isAi_created() {
        return aiCreated;
    }

    public void setAi_created(boolean ai_created) {
        this.aiCreated = ai_created;
    }

    public Set<StarSystem> getSystems() {
        return systemPresences.stream()
                .map(FactionPresence::getSystem)
                .collect(Collectors.toSet());
    }

    public Set<StarSystem> getControlledSystems() {
        return systemPresences.stream()
                .filter(FactionPresence::getIsControlling)
                .map(FactionPresence::getSystem)
                .collect(Collectors.toSet());
    }
}
