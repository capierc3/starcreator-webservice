package com.brickroad.starcreator_webservice.entity.ud;

import com.brickroad.starcreator_webservice.enums.BinaryConfiguration;
import com.brickroad.starcreator_webservice.utils.systems.SystemClassification;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "A star system containing one or more stars, planets, belts, and associated celestial bodies")
public class StarSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the star system", example = "42")
    private Long id;

    // ── Designation (identity card) ──

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "designation_id")
    @Schema(description = "Identity card: designation, classification, and survey history")
    private Designation designation;

    // ── Legacy name column (kept for DB compatibility, delegates to designation) ──

    @Column(name = "name")
    @JsonIgnore
    private String nameColumn;

    @Transient
    @JsonProperty("classification")
    @Schema(description = "Computed classification of the system including archetypes, resource ratings, and scout report")
    private SystemClassification classification;

    @ManyToOne
    @JoinColumn(name = "sector_id")
    @Schema(description = "The galactic sector this system belongs to")
    private Sector sector;

    @OneToMany(mappedBy = "system", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Schema(description = "Stars in this system")
    private Set<Star> stars = new HashSet<>();

    @Transient
    @JsonProperty("planets")
    @Schema(description = "Planets orbiting within this system")
    private List<Planet> planets = new ArrayList<>();

    @OneToMany(mappedBy = "starSystem", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("system-bands")
    @Schema(description = "Orbital bands (asteroid belts, debris disks) in this system")
    private List<OrbitalBand> bands = new ArrayList<>();

    @OneToMany(mappedBy = "system", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<FactionPresence> factionPresences = new HashSet<>();

    @Schema(description = "Total system extent in Astronomical Units", example = "143.0")
    private Double sizeAu;

    @Schema(description = "Inner edge of the habitable zone in AU", example = "0.95")
    private Double habitableLow;

    @Schema(description = "Outer edge of the habitable zone in AU", example = "1.37")
    private Double habitableHigh;

    @Enumerated(EnumType.STRING)
    @Column(name = "binary_configuration")
    @Schema(description = "Orbital configuration for multi-star systems", example = "SINGLE")
    private BinaryConfiguration binaryConfiguration;

    @Column(name = "primary_star_id")
    @Schema(description = "ID of the primary star in the system")
    private Long primaryStarId;

    @Column(name = "binary_separation_au")
    @Schema(description = "Separation between stars in a multi-star system (AU)")
    private Double binarySeparationAu;

    @Column(name = "binary_orbital_period_days")
    @Schema(description = "Orbital period of the stellar binary pair (days)")
    private Double binaryOrbitalPeriodDays;

    @Column(length = 5000)
    @Schema(description = "Generated scout report describing the system")
    private String description;

    @Column(nullable = false)
    @Schema(description = "Galactic X coordinate", example = "14")
    private int x;

    @Column(nullable = false)
    @Schema(description = "Galactic Y coordinate", example = "-26")
    private int y;

    @Column(nullable = false)
    @Schema(description = "Galactic Z coordinate", example = "99")
    private int z;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @Schema(description = "Timestamp when this system was generated")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    @Schema(description = "Timestamp when this system was last modified")
    private LocalDateTime modifiedAt;

    // ── Derived JSON properties ──

    @JsonProperty("factions")
    @Schema(description = "Factions present in this system")
    public Set<Faction> getFactions() {
        if (factionPresences == null || factionPresences.isEmpty()) {
            return Collections.emptySet();
        }
        return factionPresences.stream()
                .map(FactionPresence::getFaction)
                .collect(Collectors.toSet());
    }

    @JsonProperty("controllingFaction")
    @Schema(description = "The faction currently controlling this system, if any")
    public Faction getControllingFaction() {
        if (factionPresences == null) {
            return null;
        }
        return factionPresences.stream()
                .filter(FactionPresence::getIsControlling)
                .map(FactionPresence::getFaction)
                .findFirst()
                .orElse(null);
    }

    // ── Designation Convenience Getters/Setters ──

    private Designation ensureDesignation() {
        if (designation == null) designation = new Designation();
        return designation;
    }

    @JsonIgnore
    public String getName() {
        return designation != null ? designation.getLoggedName() : nameColumn;
    }

    public void setName(String name) {
        ensureDesignation().setLoggedName(name);
        this.nameColumn = name;
    }

    // ── Convenience methods ──

    public void setPlanets(List<Planet> planets) {
        this.planets = planets;
    }

    public void setBands(List<OrbitalBand> bands) {
        this.bands = bands;
        for (OrbitalBand band : bands) {
            band.setStarSystem(this);
        }
    }

    public void addBand(OrbitalBand band) {
        bands.add(band);
        band.setStarSystem(this);
    }
}
