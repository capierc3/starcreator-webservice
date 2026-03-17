package com.brickroad.starcreator_webservice.worldBuilder;

import com.brickroad.starcreator_webservice.creator.SystemCreator;
import com.brickroad.starcreator_webservice.entity.ud.OrbitalBand;
import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.StarSystem;
import com.brickroad.starcreator_webservice.enums.ColonizationSuitability;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Fluent builder for generating star systems that match specific criteria.
 * Generates systems in a loop until one matches all specified predicates.
 *
 * Usage:
 *   StarSystem system = SystemFinder.using(systemCreator)
 *       .starCount(1)
 *       .planetType(PlanetType.DWARF)
 *       .compositionClassification(Composition.SILICATE_RICH)
 *       .inBeltType(BeltType.INNER_ROCKY)
 *       .find();
 *
 * All methods accept either String or TestEnums values.
 */
public class SystemFinder {

    private final SystemCreator systemCreator;
    private final List<Predicate<StarSystem>> systemPredicates = new ArrayList<>();
    private final List<Predicate<Planet>> planetPredicates = new ArrayList<>();
    private String requiredBeltType;
    private int maxAttempts = 10_000;
    private boolean verbose = true;

    private SystemFinder(SystemCreator systemCreator) {
        this.systemCreator = systemCreator;
    }

    public static SystemFinder using(SystemCreator systemCreator) {
        return new SystemFinder(systemCreator);
    }

    // ── Star criteria ──

    public SystemFinder starCount(int count) {
        systemPredicates.add(sys -> sys.getStars().size() == count);
        return this;
    }

    public SystemFinder starType(String type) {
        systemPredicates.add(sys -> sys.getStars().stream()
                .anyMatch(star -> type.equalsIgnoreCase(star.getType())));
        return this;
    }

    public SystemFinder starType(TestEnums.StarType type) { return starType(type.toString()); }

    // ── Planet criteria ──

    public SystemFinder planetType(String type) {
        planetPredicates.add(p -> type.equalsIgnoreCase(p.getPlanetType()));
        return this;
    }

    public SystemFinder planetType(TestEnums.PlanetType type) { return planetType(type.toString()); }

    public SystemFinder compositionClassification(String classification) {
        planetPredicates.add(p -> classification.equalsIgnoreCase(p.getCompositionClassification()));
        return this;
    }

    public SystemFinder compositionClassification(TestEnums.Composition comp) { return compositionClassification(comp.toString()); }

    public SystemFinder colonizationSuitability(ColonizationSuitability suitability) {
        planetPredicates.add(p -> p.getHabitability() != null
                && suitability.equals(p.getHabitability().getColonizationSuitability()));
        return this;
    }

    public SystemFinder lifeComplexity(String complexity) {
        planetPredicates.add(p -> p.getHabitability() != null
                && complexity.equalsIgnoreCase(p.getHabitability().getLifeComplexityPotential()));
        return this;
    }

    public SystemFinder lifeComplexity(TestEnums.LifeComplexity complexity) { return lifeComplexity(complexity.toString()); }

    public SystemFinder hasLiquidWater() {
        planetPredicates.add(p -> p.getWater() != null
                && p.getWater().getLiquidWaterCoveragePercent() != null
                && p.getWater().getLiquidWaterCoveragePercent() > 0);
        return this;
    }

    public SystemFinder atmosphereClassification(String classification) {
        planetPredicates.add(p -> classification.equalsIgnoreCase(p.getAtmosphereClassification()));
        return this;
    }

    public SystemFinder atmosphereClassification(TestEnums.Atmosphere atmo) { return atmosphereClassification(atmo.toString()); }

    public SystemFinder habitableZonePosition(String position) {
        planetPredicates.add(p -> position.equalsIgnoreCase(p.getHabitableZonePosition()));
        return this;
    }

    public SystemFinder habitableZonePosition(TestEnums.HabitableZone zone) { return habitableZonePosition(zone.toString()); }

    public SystemFinder planetPredicate(Predicate<Planet> predicate) {
        planetPredicates.add(predicate);
        return this;
    }

    // ── Belt criteria ──

    public SystemFinder inBeltType(String beltType) {
        this.requiredBeltType = beltType;
        return this;
    }

    public SystemFinder inBeltType(TestEnums.BeltType beltType) { return inBeltType(beltType.toString()); }

    // ── Control ──

    public SystemFinder maxAttempts(int max) {
        this.maxAttempts = max;
        return this;
    }

    public SystemFinder verbose() {
        this.verbose = true;
        return this;
    }

    public SystemFinder quiet() {
        this.verbose = false;
        return this;
    }

    // ── Terminal ──

    public StarSystem find() {
        printCriteria();

        for (int i = 0; i < maxAttempts; i++) {
            StarSystem system = systemCreator.generateSystem();

            if (matches(system)) {
                if (verbose) System.out.println("Found match on attempt " + (i + 1));
                return system;
            }

            if (verbose && i > 0 && i % 100 == 0) {
                System.out.println("Searched " + i + " systems...");
            }
        }

        assertNotNull(null, "Failed to find matching system after " + maxAttempts + " attempts");
        return null; // unreachable
    }

    private boolean matches(StarSystem system) {
        // Check system-level predicates
        for (Predicate<StarSystem> pred : systemPredicates) {
            if (!pred.test(system)) return false;
        }

        // If no planet criteria, system-level match is enough
        if (planetPredicates.isEmpty() && requiredBeltType == null) return true;

        // Check planet-level: any planet must satisfy ALL planet predicates
        for (Planet planet : system.getPlanets()) {
            if (matchesPlanet(planet) && matchesBelt(system, planet)) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesPlanet(Planet planet) {
        for (Predicate<Planet> pred : planetPredicates) {
            if (!pred.test(planet)) return false;
        }
        return true;
    }

    private boolean matchesBelt(StarSystem system, Planet planet) {
        if (requiredBeltType == null) return true;

        for (OrbitalBand band : system.getBands()) {
            if (requiredBeltType.equalsIgnoreCase(band.getBandType())
                    && band.getDwarfPlanets().contains(planet)) {
                return true;
            }
        }
        return false;
    }

    private void printCriteria() {
        if (!verbose) return;
        System.out.println("=== SystemFinder ===");
        System.out.println("Max attempts: " + maxAttempts);
        if (!systemPredicates.isEmpty()) System.out.println("System predicates: " + systemPredicates.size());
        if (!planetPredicates.isEmpty()) System.out.println("Planet predicates: " + planetPredicates.size());
        if (requiredBeltType != null) System.out.println("Belt type: " + requiredBeltType);
        System.out.println("---");
    }
}
