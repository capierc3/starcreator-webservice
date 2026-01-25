package com.brickroad.starcreator_webservice.entity.ud;

import com.brickroad.starcreator_webservice.enums.BinaryConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "star_system", schema = "ud")
public class StarSystem {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @OneToMany(mappedBy = "system")
    @JsonManagedReference
    private Set<Star> stars = new HashSet<>();

    @OneToMany(mappedBy = "system")
    @JsonManagedReference
    private List<CelestialBody> bodies = new ArrayList<>();

    @OneToMany(mappedBy = "system", cascade = CascadeType.ALL)
    private Set<FactionPresence> factionPresences = new HashSet<>();

    private Double sizeAu;  // Size in Astronomical Units

    private Double habitableLow;

    private Double habitableHigh;

    @Enumerated(EnumType.STRING)
    @Column(name = "binary_configuration")
    private BinaryConfiguration binaryConfiguration;

    @Column(name = "primary_star_id")
    private Long primaryStarId;  // Which star is the "main" one

    @Column(name = "binary_separation_au")
    private Double binarySeparationAu;  // Distance between stars

    @Column(name = "binary_orbital_period_days")
    private Double binaryOrbitalPeriodDays;

    private String description;

    @Column(nullable = false)
    private int x;
    @Column(nullable = false)
    private int y;
    @Column(nullable = false)
    private int z;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    public Set<Faction> getFactions() {
        return factionPresences.stream()
                .map(FactionPresence::getFaction)
                .collect(Collectors.toSet());
    }

    public Optional<Faction> getControllingFaction() {
        return factionPresences.stream()
                .filter(FactionPresence::getIsControlling)
                .map(FactionPresence::getFaction)
                .findFirst();
    }

    @JsonIgnore
    public Set<CelestialBody> getPlanets() {
        return bodies.stream()
                .filter(body -> body instanceof Planet)
                .map(body -> (Planet) body)
                .collect(Collectors.toSet());
    }

    public void setPlanets(List<CelestialBody> bodies) {
        this.bodies = bodies;
    }


}
