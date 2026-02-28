package com.brickroad.starcreator_webservice.persistence;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.model.climate.*;
import com.brickroad.starcreator_webservice.model.habitability.*;
import com.brickroad.starcreator_webservice.service.CreationService;
import com.brickroad.starcreator_webservice.service.SystemPersistenceService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that {@code @Transient} derived fields survive a database round-trip.
 * <p>
 * Pattern: generate → save → load → verify all transient fields are still populated.
 * <p>
 * For deterministic fields (density, gravity, orbital period, etc.) the test verifies
 * that the DFC-recalculated value matches the creator's original value exactly.
 * <p>
 * For fields where the creator uses randomness that DFC cannot reproduce (magnetosphere
 * geometry, surface power flux, etc.), the test only verifies non-null presence —
 * exact value matching for these will be verified after the Tier 2C migration when
 * seeded regeneration replaces DFC for those fields.
 * <p>
 * Uses entity designation names (not positional indices) as keys so results are stable
 * regardless of JPA collection ordering.
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
class DerivedFieldRoundTripTest {

    @Autowired
    private CreationService creationService;

    @Autowired
    private SystemPersistenceService persistenceService;

    // ══════════════════════════════════════════════════════════════════
    //  Main Round-Trip Test — Deterministic Fields
    // ══════════════════════════════════════════════════════════════════

    @Test
    void testDeterministicFieldsSurviveRoundTrip() {
        StarSystem generated = creationService.createStarSystem();
        assertNotNull(generated);

        // Snapshot deterministic transient fields pre-save
        Map<String, Object> preSnapshot = captureDeterministicSnapshot(generated);

        StarSystem saved = persistenceService.saveSystem(generated);
        Long savedId = saved.getId();
        assertNotNull(savedId);

        try {
            Optional<StarSystem> loaded = persistenceService.loadSystem(savedId);
            assertTrue(loaded.isPresent());

            Map<String, Object> postSnapshot = captureDeterministicSnapshot(loaded.get());

            List<String> mismatches = compareSnapshots(preSnapshot, postSnapshot);
            assertTrue(mismatches.isEmpty(),
                    "Deterministic field round-trip mismatches (" + mismatches.size() + "):\n"
                            + String.join("\n", mismatches));
        } finally {
            persistenceService.deleteSystem(savedId);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Presence Test — All Transient Fields Non-Null After Load
    // ══════════════════════════════════════════════════════════════════

    @Test
    void testAllTransientFieldsPopulatedAfterRoundTrip() {
        StarSystem generated = creationService.createStarSystem();

        // Capture which fields were non-null before save
        Map<String, Boolean> prePresence = capturePresenceSnapshot(generated);

        StarSystem saved = persistenceService.saveSystem(generated);
        Long savedId = saved.getId();

        try {
            Optional<StarSystem> loaded = persistenceService.loadSystem(savedId);
            assertTrue(loaded.isPresent());

            Map<String, Boolean> postPresence = capturePresenceSnapshot(loaded.get());

            // Every field that was non-null before save must still be non-null after load
            List<String> lost = new ArrayList<>();
            for (Map.Entry<String, Boolean> entry : prePresence.entrySet()) {
                if (Boolean.TRUE.equals(entry.getValue())) {
                    Boolean postVal = postPresence.get(entry.getKey());
                    if (!Boolean.TRUE.equals(postVal)) {
                        lost.add(entry.getKey() + " was present before save but null after load");
                    }
                }
            }

            assertTrue(lost.isEmpty(),
                    "Transient fields lost during round-trip (" + lost.size() + "):\n"
                            + String.join("\n", lost));
        } finally {
            persistenceService.deleteSystem(savedId);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Climate & Habitability Presence After Round-Trip
    // ══════════════════════════════════════════════════════════════════

    @Test
    void testClimateAndHabitabilityPresentAfterRoundTrip() {
        StarSystem generated = creationService.createStarSystem();

        int planetsWithClimate = 0;
        int planetsWithHabitability = 0;
        for (Planet planet : generated.getPlanets()) {
            if (planet.getClimate() != null) planetsWithClimate++;
            if (planet.getHabitability() != null) planetsWithHabitability++;
        }

        StarSystem saved = persistenceService.saveSystem(generated);
        Long savedId = saved.getId();

        try {
            Optional<StarSystem> loaded = persistenceService.loadSystem(savedId);
            assertTrue(loaded.isPresent());
            StarSystem loadedSystem = loaded.get();

            int loadedWithClimate = 0;
            int loadedWithHabitability = 0;
            for (Planet planet : loadedSystem.getPlanets()) {
                if (planet.getClimate() != null) loadedWithClimate++;
                if (planet.getHabitability() != null) loadedWithHabitability++;
            }

            assertEquals(planetsWithClimate, loadedWithClimate,
                    "Climate count mismatch after round-trip");
            assertEquals(planetsWithHabitability, loadedWithHabitability,
                    "Habitability count mismatch after round-trip");

            List<String> violations = new ArrayList<>();
            for (Planet planet : loadedSystem.getPlanets()) {
                String prefix = entityName(planet.getDesignation());

                if (planet.getHabitability() != null) {
                    PlanetaryHabitability h = planet.getHabitability();
                    if (h.getHabitabilityScore() == null)
                        violations.add(prefix + ".habitability.habitabilityScore is null");
                    if (h.getHabitabilityClass() == null)
                        violations.add(prefix + ".habitability.habitabilityClass is null");
                }

                if (planet.getClimate() != null) {
                    PlanetaryClimate c = planet.getClimate();
                    if (c.getWeatherSeverity() == null)
                        violations.add(prefix + ".climate.weatherSeverity is null");
                }
            }

            assertTrue(violations.isEmpty(),
                    "Post-load field violations (" + violations.size() + "):\n"
                            + String.join("\n", violations));
        } finally {
            persistenceService.deleteSystem(savedId);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Deterministic Snapshot — fields that must match exactly
    // ══════════════════════════════════════════════════════════════════

    /**
     * Captures only the deterministic @Transient fields that DFC computes
     * from the same formulas as the creators. These must match exactly.
     */
    private Map<String, Object> captureDeterministicSnapshot(StarSystem system) {
        Map<String, Object> snap = new LinkedHashMap<>();

        snap.put("system.classificationPresent", system.getClassification() != null);

        for (Star star : sorted(system.getStars())) {
            String sp = entityName(star.getDesignation());

            // Star transient fields — deterministic
            snap.put(sp + ".habitableZoneInnerAU", star.getHabitableZoneInnerAU());
            snap.put(sp + ".habitableZoneOuterAU", star.getHabitableZoneOuterAU());
            snap.put(sp + ".estimatedRemainingMsMy", star.getEstimatedRemainingMsMy());

            // Star physical properties — conversion-based, deterministic
            capturePP(snap, sp + ".pp", star.getPhysicalProperties(), true);

            for (Planet planet : sorted(star.getPlanets())) {
                String pp = sp + "." + entityName(planet.getDesignation());

                // Planet physical properties — deterministic from earthMass + earthRadius
                capturePP(snap, pp + ".pp", planet.getPhysicalProperties(), false);

                // Orbital period — deterministic Kepler's law
                if (planet.getOrbit() != null) {
                    snap.put(pp + ".orbit.orbitalPeriodDays",
                            planet.getOrbit().getOrbitalPeriodDays());
                }

                // Atmosphere scale height & greenhouse — deterministic
                captureAtmo(snap, pp + ".atmo", planet.getAtmosphere());

                // Magnetic field — only the deterministic surface field chain
                captureDeterministicMag(snap, pp + ".mag", planet.getMagneticField());

                for (Moon moon : sorted(planet.getMoons())) {
                    String mp = pp + "." + entityName(moon.getDesignation());

                    capturePP(snap, mp + ".pp", moon.getPhysicalProperties(), false);

                    if (moon.getOrbit() != null) {
                        snap.put(mp + ".orbit.orbitalPeriodDays",
                                moon.getOrbit().getOrbitalPeriodDays());
                        // Hill sphere and Roche limit — deterministic from moon/planet masses
                        snap.put(mp + ".orbit.hillSphereRadiusKm",
                                moon.getOrbit().getHillSphereRadiusKm());
                        snap.put(mp + ".orbit.rocheLimitKm",
                                moon.getOrbit().getRocheLimitKm());
                    }

                    captureAtmo(snap, mp + ".atmo", moon.getAtmosphere());
                }

                // Planet rings
                for (OrbitalBand ring : sorted(planet.getBands())) {
                    String rp = pp + "." + entityName(ring.getDesignation());
                    captureBand(snap, rp, ring);
                }
            }
        }

        // System-level belts
        for (OrbitalBand belt : sorted(system.getBands())) {
            captureBand(snap, "belt." + entityName(belt.getDesignation()), belt);
        }

        return snap;
    }

    private void capturePP(Map<String, Object> snap, String prefix,
                            PhysicalProperties pp, boolean isStar) {
        if (pp == null) return;
        snap.put(prefix + ".mass", pp.getMass());
        snap.put(prefix + ".radius", pp.getRadius());
        snap.put(prefix + ".circumference", pp.getCircumference());
        if (!isStar) {
            snap.put(prefix + ".density", pp.getDensity());
            snap.put(prefix + ".surfaceGravity", pp.getSurfaceGravity());
            snap.put(prefix + ".escapeVelocity", pp.getEscapeVelocity());
        }
    }

    private void captureAtmo(Map<String, Object> snap, String prefix, Atmosphere atmo) {
        if (atmo == null) return;
        snap.put(prefix + ".scaleHeightKm", atmo.getScaleHeightKm());
        snap.put(prefix + ".greenhouseEffectK", atmo.getGreenhouseEffectK());
    }

    /**
     * Only the avg surface field and magnetic moment are deterministic in DFC
     * for planet-level dipole fields. Min/max use different multipliers per
     * field type in the creator, and surfacePowerFlux uses random in the creator.
     */
    private void captureDeterministicMag(Map<String, Object> snap, String prefix,
                                          PlanetaryMagneticField field) {
        if (field == null) return;
        snap.put(prefix + ".surfaceFieldAvg", field.getSurfaceFieldMicroteslasAvg());
        snap.put(prefix + ".magneticMoment", field.getMagneticMoment());
        snap.put(prefix + ".protectionLevel", field.getProtectionLevel());
        snap.put(prefix + ".shieldsFromStellarWind", field.getShieldsFromStellarWind());
        snap.put(prefix + ".shieldsFromCosmicRays", field.getShieldsFromCosmicRays());
    }

    private void captureBand(Map<String, Object> snap, String prefix, OrbitalBand band) {
        if (band == null) return;
        snap.put(prefix + ".totalMassEarthMasses", band.getTotalMassEarthMasses());
        if (band.getInnerOrbit() != null) {
            snap.put(prefix + ".innerOrbit.orbitalPeriodDays",
                    band.getInnerOrbit().getOrbitalPeriodDays());
        }
        if (band.getOuterOrbit() != null) {
            snap.put(prefix + ".outerOrbit.orbitalPeriodDays",
                    band.getOuterOrbit().getOrbitalPeriodDays());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Presence Snapshot — all transient fields, just non-null check
    // ══════════════════════════════════════════════════════════════════

    private Map<String, Boolean> capturePresenceSnapshot(StarSystem system) {
        Map<String, Boolean> snap = new LinkedHashMap<>();

        snap.put("system.classification", system.getClassification() != null);

        for (Star star : sorted(system.getStars())) {
            String sp = entityName(star.getDesignation());

            snap.put(sp + ".habitableZoneInnerAU", star.getHabitableZoneInnerAU() != null);
            snap.put(sp + ".habitableZoneOuterAU", star.getHabitableZoneOuterAU() != null);
            snap.put(sp + ".estimatedRemainingMsMy", star.getEstimatedRemainingMsMy() != null);

            capturePPPresence(snap, sp + ".pp", star.getPhysicalProperties(), true);

            for (Planet planet : sorted(star.getPlanets())) {
                String pp = sp + "." + entityName(planet.getDesignation());

                capturePPPresence(snap, pp + ".pp", planet.getPhysicalProperties(), false);

                if (planet.getOrbit() != null) {
                    snap.put(pp + ".orbit.orbitalPeriodDays",
                            planet.getOrbit().getOrbitalPeriodDays() != null);
                }

                captureAtmoPresence(snap, pp + ".atmo", planet.getAtmosphere());
                captureMagPresence(snap, pp + ".mag", planet.getMagneticField());

                for (Moon moon : sorted(planet.getMoons())) {
                    String mp = pp + "." + entityName(moon.getDesignation());
                    capturePPPresence(snap, mp + ".pp", moon.getPhysicalProperties(), false);
                    if (moon.getOrbit() != null) {
                        snap.put(mp + ".orbit.orbitalPeriodDays",
                                moon.getOrbit().getOrbitalPeriodDays() != null);
                        snap.put(mp + ".orbit.hillSphereRadiusKm",
                                moon.getOrbit().getHillSphereRadiusKm() != null);
                        snap.put(mp + ".orbit.rocheLimitKm",
                                moon.getOrbit().getRocheLimitKm() != null);
                    }
                    captureAtmoPresence(snap, mp + ".atmo", moon.getAtmosphere());
                    captureMagPresence(snap, mp + ".mag", moon.getMagneticField());
                }

                for (OrbitalBand ring : sorted(planet.getBands())) {
                    captureBandPresence(snap, pp + "." + entityName(ring.getDesignation()), ring);
                }
            }
        }

        for (OrbitalBand belt : sorted(system.getBands())) {
            captureBandPresence(snap, "belt." + entityName(belt.getDesignation()), belt);
        }

        return snap;
    }

    private void capturePPPresence(Map<String, Boolean> snap, String prefix,
                                    PhysicalProperties pp, boolean isStar) {
        if (pp == null) return;
        // mass, radius, circumference are primitive double — always present
        snap.put(prefix + ".mass", pp.getMass() != 0.0);
        snap.put(prefix + ".radius", pp.getRadius() != 0.0);
        snap.put(prefix + ".circumference", pp.getCircumference() != 0.0);
        if (!isStar) {
            snap.put(prefix + ".density", pp.getDensity() != null);
            snap.put(prefix + ".surfaceGravity", pp.getSurfaceGravity() != null);
            snap.put(prefix + ".escapeVelocity", pp.getEscapeVelocity() != null);
        }
    }

    private void captureAtmoPresence(Map<String, Boolean> snap, String prefix, Atmosphere atmo) {
        if (atmo == null) return;
        snap.put(prefix + ".scaleHeightKm", atmo.getScaleHeightKm() != null);
        snap.put(prefix + ".greenhouseEffectK", atmo.getGreenhouseEffectK() != null);
    }

    private void captureMagPresence(Map<String, Boolean> snap, String prefix,
                                     PlanetaryMagneticField field) {
        if (field == null) return;
        snap.put(prefix + ".surfaceFieldAvg", field.getSurfaceFieldMicroteslasAvg() != null);
        snap.put(prefix + ".surfaceFieldMin", field.getSurfaceFieldMicroteslasMin() != null);
        snap.put(prefix + ".surfaceFieldMax", field.getSurfaceFieldMicroteslasMax() != null);
        snap.put(prefix + ".magneticMoment", field.getMagneticMoment() != null);
        snap.put(prefix + ".protectionLevel", field.getProtectionLevel() != null);
        snap.put(prefix + ".shieldsFromStellarWind", field.getShieldsFromStellarWind() != null);
        snap.put(prefix + ".shieldsFromCosmicRays", field.getShieldsFromCosmicRays() != null);
        // surfacePowerFlux, magnetopause, bowShock, magnetotail — only if magnetosphere exists
        if (Boolean.TRUE.equals(field.getMagnetosphereExists())) {
            snap.put(prefix + ".magnetopause", field.getMagnetopauseDistancePlanetRadii() != null);
            snap.put(prefix + ".bowShock", field.getBowShockDistancePlanetRadii() != null);
            snap.put(prefix + ".magnetotail", field.getMagnetotailLengthPlanetRadii() != null);
            snap.put(prefix + ".surfacePowerFlux", field.getSurfacePowerFluxWattsPerM2() != null);
        }
    }

    private void captureBandPresence(Map<String, Boolean> snap, String prefix, OrbitalBand band) {
        if (band == null) return;
        snap.put(prefix + ".totalMassEarthMasses", band.getTotalMassEarthMasses() != null);
        if (band.getInnerOrbit() != null) {
            snap.put(prefix + ".innerOrbit.orbitalPeriodDays",
                    band.getInnerOrbit().getOrbitalPeriodDays() != null);
        }
        if (band.getOuterOrbit() != null) {
            snap.put(prefix + ".outerOrbit.orbitalPeriodDays",
                    band.getOuterOrbit().getOrbitalPeriodDays() != null);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Snapshot Comparison
    // ══════════════════════════════════════════════════════════════════

    /**
     * Fields with known creator/DFC differences that should use wider tolerance.
     * estimatedRemainingMsMy: in binary systems, the companion star's age is overridden
     * after the creator computes this field, but DFC correctly uses the persisted age.
     */
    private static final Set<String> WIDE_TOLERANCE_SUFFIXES = Set.of(
            ".estimatedRemainingMsMy"
    );

    private List<String> compareSnapshots(Map<String, Object> pre, Map<String, Object> post) {
        List<String> mismatches = new ArrayList<>();

        for (Map.Entry<String, Object> entry : pre.entrySet()) {
            String key = entry.getKey();
            Object preVal = entry.getValue();
            Object postVal = post.get(key);

            // Pre was null — DFC may or may not compute; skip
            if (preVal == null) continue;

            // Key missing from post entirely
            if (!post.containsKey(key)) {
                mismatches.add(key + ": key missing in post-load snapshot");
                continue;
            }

            // Pre non-null, post null
            if (postVal == null) {
                mismatches.add(key + ": was " + preVal + " before save, null after load");
                continue;
            }

            // Doubles
            if (preVal instanceof Double preD && postVal instanceof Double postD) {
                double tolerance = isWideTolerance(key) ? 0.05 : 1e-6;
                if (!withinTolerance(preD, postD, tolerance)) {
                    mismatches.add(key + ": pre=" + preD + ", post=" + postD);
                }
                continue;
            }

            // Everything else: exact match
            if (!preVal.equals(postVal)) {
                mismatches.add(key + ": pre=" + preVal + ", post=" + postVal);
            }
        }

        return mismatches;
    }

    private boolean isWideTolerance(String key) {
        for (String suffix : WIDE_TOLERANCE_SUFFIXES) {
            if (key.endsWith(suffix)) return true;
        }
        return false;
    }

    /**
     * @param tolerance for tight fields (1e-6): absolute tolerance, plus 1e-9 relative
     *                  for wide fields (0.05): 5% relative tolerance
     */
    private boolean withinTolerance(double a, double b, double tolerance) {
        if (Double.isNaN(a) && Double.isNaN(b)) return true;
        if (Double.isInfinite(a) && Double.isInfinite(b)) return a == b;
        if (Math.abs(a - b) <= tolerance) return true;
        double denom = Math.max(Math.abs(a), Math.abs(b));
        if (denom == 0.0) return false;
        double relTol = tolerance >= 0.01 ? tolerance : 1e-9;
        return Math.abs(a - b) / denom <= relTol;
    }

    // ══════════════════════════════════════════════════════════════════
    //  Helpers
    // ══════════════════════════════════════════════════════════════════

    private String entityName(Designation d) {
        if (d == null) return "unnamed";
        return d.getLoggedName() != null ? d.getLoggedName() : "unnamed";
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> sorted(Collection<T> items) {
        return items.stream()
                .sorted(Comparator.comparing(item -> {
                    if (item instanceof Star s) return entityName(s.getDesignation());
                    if (item instanceof Planet p) return entityName(p.getDesignation());
                    if (item instanceof Moon m) return entityName(m.getDesignation());
                    if (item instanceof OrbitalBand b) return entityName(b.getDesignation());
                    return "";
                }))
                .toList();
    }
}
