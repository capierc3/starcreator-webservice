package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ud.OrbitalElements;
import com.brickroad.starcreator_webservice.enums.DistanceUnit;
import com.brickroad.starcreator_webservice.utils.ConversionFormulas;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import org.springframework.stereotype.Service;

/**
 * Centralized orbital mechanics service.
 * <p>
 * All Kepler's third law calculations, orbital element generation, and
 * stability assessments are routed through this class. Creator classes
 * (PlanetCreator, MoonCreator, BeltCreator, RingCreator) delegate here
 * for orbital math, keeping domain-specific placement logic in their own classes.
 */
@Service
public class OrbitalCreator {

    // ═══════════════════════════════════════════════════════════════
    //  Kepler's Third Law — Orbital Period Calculations
    // ═══════════════════════════════════════════════════════════════

    /**
     * Orbital period for a body orbiting a star.
     * Uses simplified Kepler: T(years) = sqrt(a³ / M_star)
     *
     * @param semiMajorAxisAU semi-major axis in AU
     * @param starMassSolar   central star mass in solar masses
     * @return orbital period in Earth days
     */
    public double calculateOrbitalPeriodAU(double semiMajorAxisAU, double starMassSolar) {
        double periodYears = Math.sqrt(Math.pow(semiMajorAxisAU, 3) / starMassSolar);
        return periodYears * 365.25;
    }

    /**
     * Orbital period for a body orbiting a planet (moon, ring edge, station).
     * Uses Newton's form: T = 2π * sqrt(a³ / (G * M_central))
     *
     * @param semiMajorAxisKm semi-major axis in kilometers
     * @param centralMassKg   central body mass in kilograms
     * @return orbital period in Earth days
     */
    public double calculateOrbitalPeriodKM(double semiMajorAxisKm, double centralMassKg) {
        double semiMajorAxisMeters = semiMajorAxisKm * 1000.0;
        double periodSeconds = 2 * Math.PI * Math.sqrt(
                Math.pow(semiMajorAxisMeters, 3) /
                        (ConversionFormulas.GRAVITATIONAL_CONSTANT * centralMassKg)
        );
        return periodSeconds / (24.0 * 3600.0);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Orbital Element Builders
    // ═══════════════════════════════════════════════════════════════

    /**
     * Create orbital elements for a planet orbiting a star.
     *
     * @param distanceAU    semi-major axis in AU
     * @param starMassSolar star mass in solar masses
     * @param eccentricity  orbital eccentricity
     * @param inclination   orbital inclination in degrees
     * @return fully populated OrbitalElements with unit = AU
     */
    public OrbitalElements createPlanetOrbit(double distanceAU, double starMassSolar,
                                              double eccentricity, double inclination) {
        OrbitalElements orbit = new OrbitalElements();
        orbit.setSemiMajorAxis(distanceAU);
        orbit.setSemiMajorAxisUnit(DistanceUnit.AU);
        orbit.setOrbitalPeriodDays(calculateOrbitalPeriodAU(distanceAU, starMassSolar));
        orbit.setEccentricity(eccentricity);
        orbit.setInclinationDegrees(inclination);
        randomizeAngularElements(orbit);
        orbit.setLabel("Planet orbit");
        return orbit;
    }

    /**
     * Create orbital elements for a moon orbiting a planet.
     *
     * @param distanceKm    semi-major axis in kilometers
     * @param planetMassKg  planet mass in kilograms
     * @param eccentricity  orbital eccentricity
     * @param inclination   orbital inclination in degrees
     * @return fully populated OrbitalElements with unit = KM
     */
    public OrbitalElements createMoonOrbit(double distanceKm, double planetMassKg,
                                            double eccentricity, double inclination) {
        OrbitalElements orbit = new OrbitalElements();
        orbit.setSemiMajorAxis(distanceKm);
        orbit.setSemiMajorAxisUnit(DistanceUnit.KM);
        orbit.setOrbitalPeriodDays(calculateOrbitalPeriodKM(distanceKm, planetMassKg));
        orbit.setEccentricity(eccentricity);
        orbit.setInclinationDegrees(inclination);
        randomizeAngularElements(orbit);
        orbit.setLabel("Moon orbit");
        return orbit;
    }

    /**
     * Create orbital elements for an asteroid within a belt.
     *
     * @param smaAU           semi-major axis in AU
     * @param starMassSolar   total stellar mass in solar masses
     * @param eccentricity    orbital eccentricity
     * @param inclination     orbital inclination in degrees
     * @return OrbitalElements with unit = AU (no angular elements for asteroids)
     */
    public OrbitalElements createAsteroidOrbit(double smaAU, double starMassSolar,
                                                double eccentricity, double inclination) {
        OrbitalElements orbit = new OrbitalElements();
        orbit.setSemiMajorAxis(smaAU);
        orbit.setSemiMajorAxisUnit(DistanceUnit.AU);
        orbit.setOrbitalPeriodDays(calculateOrbitalPeriodAU(smaAU, starMassSolar));
        orbit.setEccentricity(eccentricity);
        orbit.setInclinationDegrees(inclination);
        // Asteroids don't get LAN/AoP/MA in the current model
        orbit.setLabel("Asteroid orbit");
        return orbit;
    }

    /**
     * Create orbital elements for a belt or ring boundary edge.
     *
     * @param distance       distance value (AU or KM depending on unit)
     * @param unit           distance unit
     * @param centralMass    central body mass (solar masses for AU, kg for KM)
     * @param eccentricity   eccentricity at this boundary
     * @param inclination    inclination at this boundary in degrees
     * @param label          human-readable label (e.g. "Belt inner edge")
     * @return OrbitalElements for the boundary
     */
    public OrbitalElements createBandEdgeOrbit(double distance, DistanceUnit unit,
                                                double centralMass, double eccentricity,
                                                double inclination, String label) {
        OrbitalElements orbit = new OrbitalElements();
        orbit.setSemiMajorAxis(distance);
        orbit.setSemiMajorAxisUnit(unit);
        orbit.setEccentricity(eccentricity);
        orbit.setInclinationDegrees(inclination);

        if (unit == DistanceUnit.AU) {
            orbit.setOrbitalPeriodDays(calculateOrbitalPeriodAU(distance, centralMass));
        } else {
            orbit.setOrbitalPeriodDays(calculateOrbitalPeriodKM(distance, centralMass));
        }

        orbit.setLabel(label);
        return orbit;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Angular Element Randomization
    // ═══════════════════════════════════════════════════════════════

    /**
     * Randomize the three angular elements: longitude of ascending node,
     * argument of periapsis, and mean anomaly. All uniformly distributed 0-360°.
     */
    public void randomizeAngularElements(OrbitalElements orbit) {
        orbit.setLongitudeOfAscendingNodeDeg(RandomUtils.rollRange(0.0, 360.0));
        orbit.setArgumentOfPeriapsisDeg(RandomUtils.rollRange(0.0, 360.0));
        orbit.setMeanAnomalyDeg(RandomUtils.rollRange(0.0, 360.0));
    }

    // ═══════════════════════════════════════════════════════════════
    //  Stability Setters
    // ═══════════════════════════════════════════════════════════════

    /**
     * Set stability classification on a planet's orbit.
     */
    public void setStability(OrbitalElements orbit, String classification,
                              Double timescaleMy, String crossingNeighbor) {
        if (orbit == null) return;
        orbit.setOrbitStability(classification);
        orbit.setOrbitStabilityTimescaleMy(timescaleMy);
        orbit.setOrbitCrossingNeighbor(crossingNeighbor);
    }

    /**
     * Classify moon orbit stability based on Roche limit and Hill sphere.
     */
    public void classifyMoonStability(OrbitalElements orbit, double smaKm,
                                       double rocheLimitKm, double hillSphereRadiusKm) {
        if (orbit == null) return;
        if (smaKm < rocheLimitKm * 1.2) {
            orbit.setOrbitStability("UNSTABLE");
        } else if (smaKm > hillSphereRadiusKm * 0.4) {
            orbit.setOrbitStability("MARGINALLY_STABLE");
        } else {
            orbit.setOrbitStability("STABLE");
        }
    }
}
