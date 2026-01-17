package com.brickroad.starcreator_webservice.model.factions;

import com.brickroad.starcreator_webservice.model.starSystems.StarSystem;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "faction_presence", schema = "ud",
        uniqueConstraints = @UniqueConstraint(columnNames = {"faction_id", "system_id"})
)
public class FactionPresence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "faction_id", nullable = false)
    private Faction faction;

    @ManyToOne
    @JoinColumn(name = "system_id", nullable = false)
    private StarSystem system;

    @Column(name = "influence_level")
    private Integer influenceLevel = 0;  // 0-100

    @Column(name = "is_controlling")
    private Boolean isControlling = false;

    @Column(name = "since_date")
    private LocalDateTime sinceDate;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Faction getFaction() { return faction; }
    public void setFaction(Faction faction) { this.faction = faction; }

    public StarSystem getSystem() { return system; }
    public void setSystem(StarSystem system) { this.system = system; }

    public Integer getInfluenceLevel() { return influenceLevel; }
    public void setInfluenceLevel(Integer influenceLevel) { this.influenceLevel = influenceLevel; }

    public Boolean getIsControlling() { return isControlling; }
    public void setIsControlling(Boolean isControlling) { this.isControlling = isControlling; }

    public LocalDateTime getSinceDate() { return sinceDate; }
    public void setSinceDate(LocalDateTime sinceDate) { this.sinceDate = sinceDate; }
}
