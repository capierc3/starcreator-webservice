package com.brickroad.starcreator_webservice.service;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.utils.CelestialBodyUtils;
import com.brickroad.starcreator_webservice.utils.ConversionFormulas;
import com.brickroad.starcreator_webservice.utils.PhysicsFormulas;
import com.brickroad.starcreator_webservice.utils.TemperatureCalculator;
import com.brickroad.starcreator_webservice.utils.planets.OrbitalStabilityAnalyzer;
import com.brickroad.starcreator_webservice.utils.planets.StellarEnvironment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Recalculates {@code @Transient} derived fields after loading entities from the database.
 * <p>
 * These fields are pure deterministic functions of persisted seed data and were removed
 * from the DB schema to reduce storage (Tier 1 optimization). They must be recomputed
 * before the entity graph is serialized to JSON.
 * <p>
 * This class is a <b>thin orchestrator</b> — it walks the entity graph and delegates
 * every formula to {@link PhysicsFormulas} (the single source of truth shared with the
 * Creator classes). No physics formulas live here.
 */
@Component
public class DerivedFieldCalculator {

    /**
     * Recomputes all {@code @Transient} derived fields on a fully-loaded StarSystem.
     * Must be called within the same transaction that loaded the entity graph
     * (or after all lazy collections have been initialized).
     */
    public void recalculate(StarSystem system) {
        // Stars first — other calculations depend on star properties
        system.getStars().forEach(star -> {
            recalculateStarFields(star);

            star.getPlanets().forEach(planet -> {
                recalculatePhysicalProps(planet.getPhysicalProperties(), false);
                recalculateOrbitalPeriodAU(planet.getOrbit(), star);
                recalculateAtmosphere(planet.getAtmosphere(), planet);
                recalculateMagneticField(planet.getMagneticField(), planet.getPhysicalProperties(),
                        star, planet.getSemiMajorAxisAU());

                // Planet's moons
                planet.getMoons().forEach(moon -> {
                    recalculatePhysicalProps(moon.getPhysicalProperties(), false);
                    recalculateOrbitalPeriodKM(moon.getOrbit(), planet);
                    recalculateHillAndRoche(moon.getOrbit(), planet, star);
                    recalculateAtmosphere(moon.getAtmosphere(), moon.getPhysicalProperties());
                    recalculateMagneticField(moon.getMagneticField(), moon.getPhysicalProperties(),
                            star, planet.getSemiMajorAxisAU());
                });

                // Planet's rings
                planet.getBands().forEach(ring -> recalculateBand(ring, star));
            });
        });

        // System-level belts
        Star primaryStar = system.getStars().stream().findFirst().orElse(null);
        system.getBands().forEach(belt -> recalculateBand(belt, primaryStar));

        // Orbital stability (needs all planets to be ready)
        system.getStars().forEach(this::recalculateOrbitalStability);
    }

    // ══════════════════════════════════════════════════════════════════
    // STAR
    // ══════════════════════════════════════════════════════════════════

    private void recalculateStarFields(Star star) {
        // Internal mass/radius fields on PhysicalProperties
        PhysicalProperties pp = star.getPhysicalProperties();
        if (pp != null) {
            if (pp.getSolarMass() != null) {
                pp.setMass(ConversionFormulas.solarMassToKG(pp.getSolarMass()));
            }
            if (pp.getSolarRadius() != null) {
                pp.setRadius(ConversionFormulas.solarRadiusToKM(pp.getSolarRadius()));
                pp.setCircumference(ConversionFormulas.radiusToCircumference(pp.getRadius()));
            }
        }

        // Habitable zone
        double effectiveLum = StellarEnvironment.effectiveLuminosity(star);
        star.setHabitableZoneInnerAU(PhysicsFormulas.habitableZoneInnerAU(effectiveLum));
        star.setHabitableZoneOuterAU(PhysicsFormulas.habitableZoneOuterAU(effectiveLum));

        // Estimated remaining MS lifetime
        if (pp != null && pp.getSolarMass() != null && star.getAgeMY() != null) {
            star.setEstimatedRemainingMsMy(
                    PhysicsFormulas.estimatedRemainingMsMy(pp.getSolarMass(), star.getAgeMY()));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // PHYSICAL PROPERTIES (planet/moon)
    // ══════════════════════════════════════════════════════════════════

    private void recalculatePhysicalProps(PhysicalProperties pp, boolean isStar) {
        if (pp == null) return;

        if (!isStar && pp.getEarthMass() != null && pp.getEarthRadius() != null) {
            double massKg = PhysicsFormulas.earthMassToKg(pp.getEarthMass());
            double radiusKm = PhysicsFormulas.earthRadiusToKm(pp.getEarthRadius());

            // Internal fields
            pp.setMass(massKg);
            pp.setRadius(radiusKm);
            pp.setCircumference(ConversionFormulas.radiusToCircumference(radiusKm));

            // Derived bulk properties
            pp.setDensity(PhysicsFormulas.density(massKg, radiusKm));
            pp.setSurfaceGravity(PhysicsFormulas.surfaceGravityG(massKg, radiusKm));
            pp.setEscapeVelocity(PhysicsFormulas.escapeVelocityKmS(massKg, radiusKm));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // ORBITAL PERIOD
    // ══════════════════════════════════════════════════════════════════

    /** Planet orbiting a star — Kepler's 3rd law (simplified). */
    private void recalculateOrbitalPeriodAU(OrbitalElements orbit, Star star) {
        if (orbit == null || orbit.getSemiMajorAxis() == null) return;
        if (star == null || star.getPhysicalProperties() == null
                || star.getPhysicalProperties().getSolarMass() == null) return;

        orbit.setOrbitalPeriodDays(
                PhysicsFormulas.orbitalPeriodDaysAU(
                        orbit.getSemiMajorAxis(),
                        star.getPhysicalProperties().getSolarMass()));
    }

    /** Moon orbiting a planet — Newton's generalized form. */
    private void recalculateOrbitalPeriodKM(OrbitalElements orbit, Planet planet) {
        if (orbit == null || orbit.getSemiMajorAxis() == null) return;
        if (planet == null || planet.getPhysicalProperties() == null
                || planet.getPhysicalProperties().getEarthMass() == null) return;

        double centralMassKg = PhysicsFormulas.earthMassToKg(
                planet.getPhysicalProperties().getEarthMass());

        orbit.setOrbitalPeriodDays(
                PhysicsFormulas.orbitalPeriodDaysKM(orbit.getSemiMajorAxis(), centralMassKg));
    }

    // ══════════════════════════════════════════════════════════════════
    // HILL SPHERE & ROCHE LIMIT (moon orbits)
    // ══════════════════════════════════════════════════════════════════

    private void recalculateHillAndRoche(OrbitalElements moonOrbit, Planet planet, Star star) {
        if (moonOrbit == null || planet == null || star == null) return;
        if (planet.getOrbit() == null || planet.getOrbit().getSemiMajorAxis() == null) return;
        if (planet.getPhysicalProperties() == null || star.getPhysicalProperties() == null) return;

        double planetSmaKm = planet.getOrbit().getSemiMajorAxis() * ConversionFormulas.AU_TO_KM;
        double planetMass = planet.getPhysicalProperties().getMass();
        double starMass = star.getPhysicalProperties().getMass();

        if (starMass > 0) {
            moonOrbit.setHillSphereRadiusKm(
                    PhysicsFormulas.hillSphereRadiusKm(planetSmaKm, planetMass, starMass));
        }

        // Roche limit: use ρ_moon = 3.3 g/cm³ (rocky default)
        double planetRadius = planet.getPhysicalProperties().getRadius();
        Double planetDensity = planet.getPhysicalProperties().getDensity();
        if (planetRadius > 0 && planetDensity != null && planetDensity > 0) {
            moonOrbit.setRocheLimitKm(
                    PhysicsFormulas.rocheLimitKm(planetRadius, planetDensity, 3.3));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // ORBITAL STABILITY (planet pairs)
    // ══════════════════════════════════════════════════════════════════

    private void recalculateOrbitalStability(Star star) {
        List<Planet> planets = star.getPlanets();
        if (planets == null || planets.size() < 2) return;
        if (star.getPhysicalProperties() == null || star.getPhysicalProperties().getSolarMass() == null) return;

        double starMassSolar = star.getPhysicalProperties().getSolarMass();
        double systemAgeMy = star.getAgeMY() != null ? star.getAgeMY() : 5000.0;

        // Sort planets by semi-major axis
        List<Planet> sorted = planets.stream()
                .filter(p -> p.getOrbit() != null && p.getOrbit().getSemiMajorAxis() != null)
                .sorted((a, b) -> Double.compare(
                        a.getOrbit().getSemiMajorAxis(),
                        b.getOrbit().getSemiMajorAxis()))
                .toList();

        for (int i = 0; i < sorted.size() - 1; i++) {
            Planet inner = sorted.get(i);
            Planet outer = sorted.get(i + 1);

            OrbitalElements innerOrbit = inner.getOrbit();
            OrbitalElements outerOrbit = outer.getOrbit();

            double sma1 = innerOrbit.getSemiMajorAxis();
            double ecc1 = innerOrbit.getEccentricity() != null ? innerOrbit.getEccentricity() : 0;
            double mass1 = inner.getPhysicalProperties() != null && inner.getPhysicalProperties().getEarthMass() != null
                    ? inner.getPhysicalProperties().getEarthMass() : 1.0;

            double sma2 = outerOrbit.getSemiMajorAxis();
            double ecc2 = outerOrbit.getEccentricity() != null ? outerOrbit.getEccentricity() : 0;
            double mass2 = outer.getPhysicalProperties() != null && outer.getPhysicalProperties().getEarthMass() != null
                    ? outer.getPhysicalProperties().getEarthMass() : 1.0;

            OrbitalStabilityAnalyzer.StabilityResult result =
                    OrbitalStabilityAnalyzer.analyzeStability(
                            sma1, ecc1, mass1, sma2, ecc2, mass2, starMassSolar, systemAgeMy);

            // Apply to the outer planet (same convention as creation code)
            outerOrbit.setOrbitStability(result.classification);
            outerOrbit.setOrbitStabilityTimescaleMy(result.timescaleMy);
            if (result.orbitsCrossing) {
                String innerName = inner.getDesignation() != null ? inner.getDesignation().getLoggedName() : "inner";
                outerOrbit.setOrbitCrossingNeighbor(innerName);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // ATMOSPHERE
    // ══════════════════════════════════════════════════════════════════

    /** Planet atmosphere. */
    private void recalculateAtmosphere(Atmosphere atmo, Planet planet) {
        if (atmo == null || planet == null) return;
        recalculateAtmosphereFields(atmo, planet.getPhysicalProperties());
    }

    /** Moon atmosphere. */
    private void recalculateAtmosphere(Atmosphere atmo, PhysicalProperties pp) {
        if (atmo == null) return;
        recalculateAtmosphereFields(atmo, pp);
    }

    private void recalculateAtmosphereFields(Atmosphere atmo, PhysicalProperties pp) {
        if (pp == null) return;

        double surfaceTemp = pp.getSurfaceTemp() != null ? pp.getSurfaceTemp() : 288.0;
        double surfaceGravity = pp.getSurfaceGravity() != null ? pp.getSurfaceGravity() : 1.0;
        double surfaceGravityMs2 = surfaceGravity * PhysicsFormulas.EARTH_GRAVITY_MS2;
        double pressureBar = atmo.getSurfacePressureBar() != null ? atmo.getSurfacePressureBar() : 1.0;

        // Scale height
        double meanMolWeight = CelestialBodyUtils.calculateMeanMolecularWeightFromString(atmo.getCompositionSummary());
        if (surfaceGravityMs2 > 0 && meanMolWeight > 0) {
            atmo.setScaleHeightKm(
                    PhysicsFormulas.scaleHeightKm(surfaceTemp, surfaceGravityMs2, meanMolWeight));
        }

        // Greenhouse effect
        double greenhouse = TemperatureCalculator.estimateGreenhouseWarming(
                atmo.getClassification(), pressureBar, atmo.getCompositionSummary());
        atmo.setGreenhouseEffectK(greenhouse);
    }

    // ══════════════════════════════════════════════════════════════════
    // MAGNETIC FIELD
    // ══════════════════════════════════════════════════════════════════

    /**
     * Recalculate all magnetic field derived properties.
     *
     * @param field          the magnetic field entity
     * @param pp             physical properties of the body (planet or moon)
     * @param star           parent star (for stellar wind and protection calculations)
     * @param distanceAU     distance from the star in AU (planet's SMA, or parent planet's SMA for moons)
     */
    private void recalculateMagneticField(PlanetaryMagneticField field, PhysicalProperties pp,
                                           Star star, Double distanceAU) {
        if (field == null || field.getStrengthComparedToEarth() == null) return;

        double strength = field.getStrengthComparedToEarth();

        // Surface field: avg / min / max
        double avgField = PhysicsFormulas.surfaceFieldAvgMicrotesla(strength);
        field.setSurfaceFieldMicroteslasAvg(avgField);
        field.setSurfaceFieldMicroteslasMin(PhysicsFormulas.surfaceFieldMinMicrotesla(avgField));
        field.setSurfaceFieldMicroteslasMax(PhysicsFormulas.surfaceFieldMaxMicrotesla(avgField));

        // Magnetic moment
        if (pp != null && pp.getEarthRadius() != null) {
            double radiusKm = PhysicsFormulas.earthRadiusToKm(pp.getEarthRadius());
            field.setMagneticMoment(PhysicsFormulas.magneticMoment(strength, radiusKm));
        }

        // Magnetosphere geometry (deterministic core — no random variance on reload)
        if (Boolean.TRUE.equals(field.getMagnetosphereExists()) && pp != null && star != null) {
            recalculateMagnetosphere(field, pp, star, distanceAU);
        }

        // Protection level (uses stellar threat factor — fixes the simplified-thresholds bug)
        recalculateProtection(field, star, distanceAU);
    }

    private void recalculateMagnetosphere(PlanetaryMagneticField field, PhysicalProperties pp,
                                           Star star, Double distanceAU) {
        double strength = field.getStrengthComparedToEarth();
        double radiusKm = PhysicsFormulas.earthRadiusToKm(
                pp.getEarthRadius() != null ? pp.getEarthRadius() : 1.0);
        double planetRadiusM = radiusKm * 1000.0;

        // Magnetic moment
        double moment = PhysicsFormulas.magneticMoment(strength, radiusKm);

        // Ram pressure — use StellarEnvironment for proper distance-based calculation
        double effectiveDistanceAU = distanceAU != null ? distanceAU : 1.0;
        double ramPressure = StellarEnvironment.windRamPressureAtDistance(star, effectiveDistanceAU);

        // Magnetopause standoff (deterministic, no random variance)
        double magnetopauseRadii = PhysicsFormulas.magnetopauseStandoffRadii(
                moment, ramPressure, planetRadiusM);
        field.setMagnetopauseDistancePlanetRadii(magnetopauseRadii);

        // Bow shock
        double windSpeed = star.getStellarWindVelocityKmS() != null
                ? star.getStellarWindVelocityKmS() : 400.0;
        field.setBowShockDistancePlanetRadii(
                magnetopauseRadii * PhysicsFormulas.bowShockMultiplier(windSpeed));

        // Magnetotail
        field.setMagnetotailLengthPlanetRadii(
                magnetopauseRadii * PhysicsFormulas.magnetotailMultiplier(windSpeed));

        // Surface power flux
        if (pp.getEarthRadius() != null) {
            field.setSurfacePowerFluxWattsPerM2(
                    PhysicsFormulas.surfacePowerFluxWm2(moment, planetRadiusM));
        }
    }

    private void recalculateProtection(PlanetaryMagneticField field, Star star, Double distanceAU) {
        if (field.getStrengthComparedToEarth() == null) return;

        double strength = field.getStrengthComparedToEarth();

        // Calculate stellar threat factor (same as MagneticFieldCreator)
        double threatFactor = 1.0;
        if (star != null && distanceAU != null) {
            threatFactor = StellarEnvironment.atmosphericStrippingFactor(star, distanceAU);
        }

        // Shared protection classification — fixes the simplified-thresholds bug
        PhysicsFormulas.ProtectionResult result =
                PhysicsFormulas.calculateProtectionLevel(strength, threatFactor);
        field.setProtectionLevel(result.level());
        field.setShieldsFromStellarWind(result.shieldsFromStellarWind());
        field.setShieldsFromCosmicRays(result.shieldsFromCosmicRays());
    }

    // ══════════════════════════════════════════════════════════════════
    // ORBITAL BAND
    // ══════════════════════════════════════════════════════════════════

    private void recalculateBand(OrbitalBand band, Star star) {
        // totalMassEarthMasses from totalMassKg
        if (band.getTotalMassKg() != null) {
            band.setTotalMassEarthMasses(PhysicsFormulas.massKgToEarthMasses(band.getTotalMassKg()));
        }

        // Orbital periods for inner/outer edges
        if (star != null) {
            recalculateOrbitalPeriodAU(band.getInnerOrbit(), star);
            recalculateOrbitalPeriodAU(band.getOuterOrbit(), star);
        }
    }
}
