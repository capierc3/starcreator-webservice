package com.brickroad.starcreator_webservice.model.starSystems;

import com.brickroad.starcreator_webservice.model.CelestialBody;
import com.brickroad.starcreator_webservice.model.enums.BinaryConfiguration;
import com.brickroad.starcreator_webservice.model.factions.Faction;
import com.brickroad.starcreator_webservice.model.factions.FactionPresence;
import com.brickroad.starcreator_webservice.model.planets.Planet;
import com.brickroad.starcreator_webservice.model.sectors.Sector;
import com.brickroad.starcreator_webservice.model.stars.Star;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private Set<CelestialBody> bodies = new HashSet<>();

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

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
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

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public Double getSizeAu() {
        return sizeAu;
    }

    public void setSizeAu(Double sizeAu) {
        this.sizeAu = sizeAu;
    }

    public Double getHabitableLow() {
        return habitableLow;
    }

    public void setHabitableLow(Double habitableLow) {
        this.habitableLow = habitableLow;
    }

    public Double getHabitableHigh() {
        return habitableHigh;
    }

    public void setHabitableHigh(Double habitableHigh) {
        this.habitableHigh = habitableHigh;
    }

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

    public Set<Planet> getPlanets() {
        return bodies.stream()
                .filter(body -> body instanceof Planet)
                .map(body -> (Planet) body)
                .collect(Collectors.toSet());
    }

    public List<CelestialBody> getOrderedBodies() {
        return bodies.stream()
                .sorted(Comparator.comparing(CelestialBody::getDistanceFromStar,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    public Set<Star> getStars() {
        return stars;
    }

    public void setStars(Set<Star> stars) {
        this.stars = stars;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public BinaryConfiguration getBinaryConfiguration() {
        return binaryConfiguration;
    }

    public void setBinaryConfiguration(BinaryConfiguration binaryConfiguration) {
        this.binaryConfiguration = binaryConfiguration;
    }

    public Long getPrimaryStarId() {
        return primaryStarId;
    }

    public void setPrimaryStarId(Long primaryStarId) {
        this.primaryStarId = primaryStarId;
    }

    public Double getBinarySeparationAu() {
        return binarySeparationAu;
    }

    public void setBinarySeparationAu(Double binarySeparationAu) {
        this.binarySeparationAu = binarySeparationAu;
    }

    public Double getBinaryOrbitalPeriodDays() {
        return binaryOrbitalPeriodDays;
    }

    public void setBinaryOrbitalPeriodDays(Double binaryOrbitalPeriodDays) {
        this.binaryOrbitalPeriodDays = binaryOrbitalPeriodDays;
    }
}
