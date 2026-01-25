package com.brickroad.starcreator_webservice.entity.ud;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "faction_presence", schema = "ud",
        uniqueConstraints = @UniqueConstraint(columnNames = {"faction_id", "system_id"})
)
public class FactionPresence {

    // Getters and setters
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

}
