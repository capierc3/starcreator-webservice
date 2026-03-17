package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.entity.ud.Atmosphere;
import com.brickroad.starcreator_webservice.entity.ud.OrbitalBand;
import com.brickroad.starcreator_webservice.entity.ud.Sector;
import com.brickroad.starcreator_webservice.entity.ud.StarSystem;
import com.brickroad.starcreator_webservice.repository.SectorRepository;
import com.brickroad.starcreator_webservice.repository.StarSystemRepository;
import com.brickroad.starcreator_webservice.utils.systems.SystemClassification;
import com.brickroad.starcreator_webservice.utils.systems.SystemClassifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SystemPersistenceService {

    private final StarSystemRepository starSystemRepository;
    private final SectorRepository sectorRepository;
    private final SystemClassifier systemClassifier;
    private final DerivedFieldCalculator derivedFieldCalculator;

    public SystemPersistenceService(StarSystemRepository starSystemRepository,
                                     SectorRepository sectorRepository,
                                     SystemClassifier systemClassifier,
                                     DerivedFieldCalculator derivedFieldCalculator) {
        this.starSystemRepository = starSystemRepository;
        this.sectorRepository = sectorRepository;
        this.systemClassifier = systemClassifier;
        this.derivedFieldCalculator = derivedFieldCalculator;
    }

    /**
     * Saves a StarSystem and all its cascaded children to the database.
     * The Sector must be saved first since it's a @ManyToOne without cascade.
     *
     * Cascade chain:
     * StarSystem -> Designation, Stars, FactionPresences
     * Star -> Designation, PhysicalProperties, OrbitalElements, Planets, OrbitalBands(Belts)
     * Planet -> Designation, PhysicalProperties, OrbitalElements, RotationProperties,
     *           Atmosphere, CompositionProperties, HydrologyProperties, TerrainProperties,
     *           MagneticField, Moons, Bands(Rings)
     * Moon -> (same component pattern as Planet)
     * OrbitalBand -> Designation, InnerOrbit, OuterOrbit, NotableAsteroids
     * Asteroid -> Designation, PhysicalProperties, OrbitalElements, RotationProperties,
     *             TerrainProperties, HydrologyProperties, CompositionProperties
     */
    @Transactional
    public StarSystem saveSystem(StarSystem system) {
        // Save the sector first if it's new (no cascade from StarSystem to Sector)
        Sector sector = system.getSector();
        if (sector != null && sector.getId() == null) {
            sectorRepository.save(sector);
        }

        // Save the system — cascades to everything else
        return starSystemRepository.save(system);
    }

    /**
     * Loads a StarSystem by ID with all cascaded children.
     * Recomputes the @Transient classification since it's not persisted.
     */
    @Transactional(readOnly = true)
    public Optional<StarSystem> loadSystem(Long id) {
        Optional<StarSystem> optSystem = starSystemRepository.findById(id);

        // Recompute @Transient fields that are not persisted
        optSystem.ifPresent(system -> {
            // Force-initialize ALL lazy collections to avoid LazyInitializationException.
            // Every @OneToMany(LAZY) or @ManyToMany(LAZY) collection reachable from
            // JSON serialization must be touched within this transaction.

            // StarSystem-level lazy collections
            system.getFactionPresences().size();  // needed by getFactions() / getControllingFaction()

            // Star -> Belts / Planet -> Moon/Ring tree
            system.getStars().forEach(star -> {
                // Star-level belts (cascade-managed by Star, like planets)
                star.getBands().forEach(this::initBand);

                star.getPlanets().forEach(planet -> {
                    initCelestialBody(planet.getAtmosphere());

                    // Planet's moons
                    planet.getMoons().forEach(moon -> initCelestialBody(moon.getAtmosphere()));

                    // Planet's rings
                    planet.getBands().forEach(ring -> initBand(ring));
                });
            });

            // Recompute all @Transient derived fields (Tier 1 optimization)
            derivedFieldCalculator.recalculate(system);

            // Recompute classification (it's @Transient, so not persisted)
            SystemClassification classification = systemClassifier.classify(system);
            system.setClassification(classification);
        });

        return optSystem;
    }

    /**
     * Deletes a StarSystem and all cascaded children.
     * The Sector is NOT deleted (shared by multiple systems).
     */
    @Transactional
    public void deleteSystem(Long id) {
        starSystemRepository.deleteById(id);
    }

    // ── Lazy-Init Helpers ──

    /** Initialize all lazy collections on an OrbitalBand (belt or ring). */
    private void initBand(OrbitalBand band) {
        band.getNotableAsteroids().size();
        band.getDwarfPlanets().size();
    }

    /** Initialize Atmosphere.components (LAZY) if the atmosphere exists. */
    private void initCelestialBody(Atmosphere atmosphere) {
        if (atmosphere != null) {
            atmosphere.getComponents().size();
        }
    }
}
