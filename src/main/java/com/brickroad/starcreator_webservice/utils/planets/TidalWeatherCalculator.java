package com.brickroad.starcreator_webservice.utils.planets;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.utils.CelestialBodyUtils;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TidalWeatherCalculator {

    // Earth reference values
    private static final double EARTH_MASS_KG = 5.972e24;
    private static final double EARTH_RADIUS_KM = CelestialBodyUtils.EARTH_RADIUS_KM;
    private static final double MOON_MASS_KG = 7.342e22;
    private static final double MOON_DISTANCE_KM = 384400.0;
    private static final double EARTH_TIDAL_RANGE_M = 1.0; // Average open-ocean tidal range

    // ================================================================
    // MAIN ENTRY POINT
    // ================================================================

    public void calculate(PlanetaryClimate weather, Planet planet, Star parentStar, StarSystem system) {
        String atmClass = planet.getAtmosphereClassification();
        double planetMassKg = (planet.getEarthMass() != null ? planet.getEarthMass() : 1.0) * EARTH_MASS_KG;
        double planetRadiusKm = (planet.getEarthRadius() != null ? planet.getEarthRadius() : 1.0) * EARTH_RADIUS_KM;
        double liquidWaterPercent = planet.getLiquidWaterCoveragePercent() != null ? planet.getLiquidWaterCoveragePercent() : 0.0;

        // Skip tidal calculations for gas giants (no surface) or atmosphereless worlds
        if (CelestialBodyUtils.isGasGiantAtmosphere(atmClass)) {
            weather.setAtmosphericTidalEffect("Gas giant — internal tidal forces drive deep circulation patterns");
            return;
        }

        // Calculate tidal effects from moons
        List<Moon> moons = planet.getMoons();
        if (moons != null && !moons.isEmpty() && liquidWaterPercent > 0) {
            calculateMoonTides(weather, moons, planetMassKg, planetRadiusKm);
        }

        // Atmospheric tidal effects
        calculateAtmosphericTides(weather, planet, moons, parentStar);

        // Binary star heating variation
        calculateBinaryHeating(weather, planet, parentStar, system);
    }

    // ================================================================
    // MOON TIDES
    // ================================================================

    private void calculateMoonTides(PlanetaryClimate weather, List<Moon> moons,
                                     double planetMassKg, double planetRadiusKm) {
        // Tidal force ∝ M_moon × R_planet / d³
        // Compare to Earth-Moon system to get relative tidal range
        double totalTidalForce = 0.0;
        double dominantPeriodHours = Double.MAX_VALUE;
        double dominantForce = 0.0;

        for (Moon moon : moons) {
            double moonMassKg = (moon.getEarthMass() != null ? moon.getEarthMass() : 0.01) * EARTH_MASS_KG;
            double moonDistanceKm = moon.getSemiMajorAxisKm() != null ? moon.getSemiMajorAxisKm() : 400000.0;
            double moonPeriodDays = moon.getOrbitalPeriodDays() != null ? moon.getOrbitalPeriodDays() : 27.3;

            if (moonDistanceKm <= 0) continue;

            // Tidal force relative to Earth-Moon:
            // F_tidal ∝ M × R / d³
            // Ratio = (M_moon/M_luna) × (R_planet/R_earth) × (d_luna/d_moon)³
            double forceRatio = (moonMassKg / MOON_MASS_KG)
                    * (planetRadiusKm / EARTH_RADIUS_KM)
                    * Math.pow(MOON_DISTANCE_KM / moonDistanceKm, 3.0);

            totalTidalForce += forceRatio;

            // Track the dominant moon (strongest tidal force)
            if (forceRatio > dominantForce) {
                dominantForce = forceRatio;
                dominantPeriodHours = moonPeriodDays * 24.0 / 2.0; // Two tides per orbit
            }
        }

        // Tidal range scales with total tidal force relative to Earth
        double tidalRange = EARTH_TIDAL_RANGE_M * totalTidalForce;

        // Add some natural variance
        tidalRange *= RandomUtils.rollRange(0.7, 1.4);

        // Coastal amplification can increase this significantly
        // But we report open-ocean baseline
        tidalRange = Math.max(0.01, Math.min(500.0, tidalRange));

        weather.setTidalRangeMeters(round2(tidalRange));

        if (dominantPeriodHours < Double.MAX_VALUE) {
            weather.setDominantTidalPeriodHours(round2(dominantPeriodHours));
        }
    }

    // ================================================================
    // ATMOSPHERIC TIDES
    // ================================================================

    private void calculateAtmosphericTides(PlanetaryClimate weather, Planet planet,
                                            List<Moon> moons, Star parentStar) {
        double pressureAtm = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 1.0;
        boolean tidallyLocked = Boolean.TRUE.equals(planet.getTidallyLocked());

        StringBuilder tidalEffect = new StringBuilder();

        // Stellar atmospheric tides (thermal tides)
        if (pressureAtm > 0.5) {
            double semiMajorAU = planet.getSemiMajorAxisAU() != null ? planet.getSemiMajorAxisAU() : 1.0;
            double luminosity = parentStar.getSolarLuminosity() != 0 ? parentStar.getSolarLuminosity() : 1.0;
            double stellarFlux = luminosity / (semiMajorAU * semiMajorAU);

            if (tidallyLocked) {
                tidalEffect.append("Permanent stellar heating drives a fixed thermal tide pattern " +
                        "with atmospheric bulge at the substellar point");
            } else if (stellarFlux > 2.0 && pressureAtm > 2.0) {
                tidalEffect.append("Strong solar thermal tides interact with gravitational tides, " +
                        "creating semi-diurnal atmospheric pressure oscillations");
            } else if (pressureAtm > 1.0) {
                tidalEffect.append("Mild solar thermal tides cause subtle semi-diurnal pressure variations");
            }
        }

        // Lunar atmospheric tides
        if (moons != null && !moons.isEmpty() && pressureAtm > 0.5) {
            boolean hasMassiveMoon = moons.stream()
                    .anyMatch(m -> m.getEarthMass() != null && m.getEarthMass() > 0.005);

            if (hasMassiveMoon) {
                if (!tidalEffect.isEmpty()) tidalEffect.append(". ");
                tidalEffect.append("Massive moon(s) generate detectable gravitational atmospheric tides");
            }
        }

        if (tidalEffect.isEmpty()) {
            tidalEffect.append("Negligible atmospheric tidal effects");
        }

        weather.setAtmosphericTidalEffect(tidalEffect.toString());
    }

    // ================================================================
    // BINARY STAR HEATING
    // ================================================================

    private void calculateBinaryHeating(PlanetaryClimate weather, Planet planet,
                                         Star parentStar, StarSystem system) {
        if (system == null) return;

        Star companion = parentStar.getCompanionStar();
        if (companion == null) return;

        Double binarySepAU = system.getBinarySeparationAu();
        if (binarySepAU == null || binarySepAU <= 0) return;

        double companionLuminosity = companion.getSolarLuminosity() != 0 ? companion.getSolarLuminosity() : 0.01;
        double planetDistAU = planet.getSemiMajorAxisAU() != null ? planet.getSemiMajorAxisAU() : 1.0;

        // Companion heating varies with binary orbital geometry
        // At closest approach: distance ≈ binarySep - planetDist
        // At farthest: distance ≈ binarySep + planetDist
        double closestDist = Math.abs(binarySepAU - planetDistAU);
        double farthestDist = binarySepAU + planetDistAU;

        // Avoid division by zero for very close approaches
        closestDist = Math.max(0.01, closestDist);

        double fluxAtClosest = companionLuminosity / (closestDist * closestDist);
        double fluxAtFarthest = companionLuminosity / (farthestDist * farthestDist);

        // Primary star flux
        double primaryLuminosity = parentStar.getSolarLuminosity() != 0 ? parentStar.getSolarLuminosity() : 1.0;
        double primaryFlux = primaryLuminosity / (planetDistAU * planetDistAU);

        // Variation as percentage of primary flux
        double variationPercent = ((fluxAtClosest - fluxAtFarthest) / primaryFlux) * 100.0;

        if (variationPercent > 0.1) {
            String existing = weather.getAtmosphericTidalEffect() != null ? weather.getAtmosphericTidalEffect() : "";
            String binaryNote = String.format(
                    ". Binary companion contributes variable heating (%.1f%% flux variation), " +
                    "creating a super-seasonal climate cycle", variationPercent);
            weather.setAtmosphericTidalEffect(existing + binaryNote);
        }
    }

    // ================================================================
    // MOON-PERSPECTIVE TIDAL EFFECTS
    // ================================================================

    public void calculateForMoon(PlanetaryClimate weather, Moon moon, Planet parentPlanet,
                                  Star parentStar, List<Moon> siblingMoons) {
        String atmClass = moon.getAtmosphere() != null ? moon.getAtmosphere().getClassification() : null;
        if (atmClass == null || "NONE".equals(atmClass)) return;

        double moonRadiusKm = (moon.getEarthRadius() != null ? moon.getEarthRadius() : 0.1) * EARTH_RADIUS_KM;
        double liquidWaterPercent = moon.getLiquidWaterCoveragePercent() != null ? moon.getLiquidWaterCoveragePercent() : 0.0;

        // Parent planet tidal effect on the moon (the dominant force)
        if (parentPlanet != null && liquidWaterPercent > 0) {
            calculateParentPlanetTides(weather, moon, parentPlanet, moonRadiusKm);
        }

        // Sibling moon tidal perturbations
        if (siblingMoons != null && siblingMoons.size() > 1 && liquidWaterPercent > 0) {
            addSiblingMoonTides(weather, moon, siblingMoons, moonRadiusKm);
        }

        // Atmospheric tidal effects on the moon
        calculateMoonAtmosphericTides(weather, moon, parentPlanet, parentStar);
    }

    private void calculateParentPlanetTides(PlanetaryClimate weather, Moon moon,
                                            Planet parentPlanet, double moonRadiusKm) {
        double planetMassKg = (parentPlanet.getEarthMass() != null ? parentPlanet.getEarthMass() : 1.0) * EARTH_MASS_KG;
        double moonMassKg = (moon.getEarthMass() != null ? moon.getEarthMass() : 0.001) * EARTH_MASS_KG;
        double moonOrbitKm = moon.getSemiMajorAxisKm() != null ? moon.getSemiMajorAxisKm() : 400000.0;

        if (moonOrbitKm <= 0 || moonMassKg <= 0) return;

        // Equilibrium tidal bulge height: h = 1.25 × (M_raiser/M_body) × (R/d)³ × R
        // This gives the theoretical max for a perfectly fluid body.
        // Real oceans are reduced by:
        //   - Love number h2 (~0.6 for ocean response on rocky body)
        //   - Partial ocean coverage (water can't flow freely around the body)
        //   - Basin geometry constraints

        double equilibriumHeightKm = 1.25 * (planetMassKg / moonMassKg)
                * Math.pow(moonRadiusKm / moonOrbitKm, 3.0)
                * moonRadiusKm;

        double equilibriumHeightM = equilibriumHeightKm * 1000.0;

        // Apply ocean response factor:
        // h2 Love number for ocean on rocky body ≈ 0.6
        // Partial coverage reduction: small seas don't get full equilibrium tide
        double liquidWaterPercent = moon.getLiquidWaterCoveragePercent() != null
                ? moon.getLiquidWaterCoveragePercent() : 1.0;
        double coverageFactor = Math.min(1.0, liquidWaterPercent / 50.0); // Full response at 50%+ coverage
        double loveFactor = 0.6;

        // Basin geometry: small bodies have more constrained basins
        // Earth's actual tides are ~0.44m equilibrium but coastal amplification gives 1-10m
        // Small moons with patchy seas: much less amplification
        double geometryFactor = Math.min(1.0, moonRadiusKm / EARTH_RADIUS_KM);

        double tidalRange = equilibriumHeightM * loveFactor * coverageFactor * geometryFactor;

        tidalRange *= RandomUtils.rollRange(0.7, 1.4);
        tidalRange = Math.max(0.01, Math.min(500.0, tidalRange));

        weather.setTidalRangeMeters(round2(tidalRange));

        // Tidal period
        if (Boolean.TRUE.equals(moon.getTidallyLocked())) {
            double orbitalPeriodDays = moon.getOrbitalPeriodDays() != null ? moon.getOrbitalPeriodDays() : 27.3;
            weather.setDominantTidalPeriodHours(round2(orbitalPeriodDays * 24.0));
        } else {
            double orbitalPeriodDays = moon.getOrbitalPeriodDays() != null ? moon.getOrbitalPeriodDays() : 27.3;
            weather.setDominantTidalPeriodHours(round2(orbitalPeriodDays * 24.0 / 2.0));
        }
    }

    private void addSiblingMoonTides(PlanetaryClimate weather, Moon targetMoon,
                                     List<Moon> siblingMoons, double targetMoonRadiusKm) {
        double existingRange = weather.getTidalRangeMeters() != null ? weather.getTidalRangeMeters() : 0.0;
        double targetMoonMassKg = (targetMoon.getEarthMass() != null ? targetMoon.getEarthMass() : 0.001) * EARTH_MASS_KG;
        double targetOrbitKm = targetMoon.getSemiMajorAxisKm() != null ? targetMoon.getSemiMajorAxisKm() : 400000.0;

        double liquidWaterPercent = targetMoon.getLiquidWaterCoveragePercent() != null
                ? targetMoon.getLiquidWaterCoveragePercent() : 1.0;
        double coverageFactor = Math.min(1.0, liquidWaterPercent / 50.0);
        double loveFactor = 0.6;
        double geometryFactor = Math.min(1.0, targetMoonRadiusKm / EARTH_RADIUS_KM);

        double siblingTidalTotal = 0.0;

        for (Moon sibling : siblingMoons) {
            if (sibling == targetMoon) continue;

            double siblingMassKg = (sibling.getEarthMass() != null ? sibling.getEarthMass() : 0.001) * EARTH_MASS_KG;
            double siblingOrbitKm = sibling.getSemiMajorAxisKm() != null ? sibling.getSemiMajorAxisKm() : 400000.0;

            double interMoonDistKm = Math.abs(siblingOrbitKm - targetOrbitKm);
            interMoonDistKm = Math.max(interMoonDistKm, 1000.0);

            double tidalHeightKm = 1.25 * (siblingMassKg / targetMoonMassKg)
                    * Math.pow(targetMoonRadiusKm / interMoonDistKm, 3.0)
                    * targetMoonRadiusKm;

            siblingTidalTotal += tidalHeightKm * 1000.0; // to meters
        }

        siblingTidalTotal *= loveFactor * coverageFactor * geometryFactor;

        if (siblingTidalTotal > 0.001) {
            double newRange = existingRange + siblingTidalTotal * RandomUtils.rollRange(0.5, 1.0);
            weather.setTidalRangeMeters(round2(Math.min(500.0, newRange)));
        }
    }

    private void calculateMoonAtmosphericTides(PlanetaryClimate weather, Moon moon,
                                                Planet parentPlanet, Star parentStar) {
        double pressureAtm = moon.getSurfacePressure() != null ? moon.getSurfacePressure() : 0.0;
        boolean tidallyLocked = Boolean.TRUE.equals(moon.getTidallyLocked());

        StringBuilder tidalEffect = new StringBuilder();

        // Parent planet drives strong gravitational atmospheric tides on the moon
        if (pressureAtm > 0.1 && parentPlanet != null) {
            double planetMass = parentPlanet.getEarthMass() != null ? parentPlanet.getEarthMass() : 1.0;
            if (tidallyLocked) {
                tidalEffect.append("Tidally locked to parent planet — permanent atmospheric bulge " +
                        "faces the planet, driving fixed circulation cells between sub-planetary and anti-planetary hemispheres");
            } else if (planetMass > 50) {
                tidalEffect.append("Massive parent planet drives powerful gravitational atmospheric tides");
            } else {
                tidalEffect.append("Parent planet generates moderate gravitational atmospheric tides");
            }
        }

        // Stellar thermal tides (weaker due to distance but still present)
        if (pressureAtm > 0.5 && parentStar != null && parentPlanet != null) {
            double semiMajorAU = parentPlanet.getSemiMajorAxisAU() != null ? parentPlanet.getSemiMajorAxisAU() : 1.0;
            double luminosity = parentStar.getSolarLuminosity() != 0 ? parentStar.getSolarLuminosity() : 1.0;
            double stellarFlux = luminosity / (semiMajorAU * semiMajorAU);

            if (stellarFlux > 2.0 && pressureAtm > 1.0) {
                if (!tidalEffect.isEmpty()) tidalEffect.append(". ");
                tidalEffect.append("Strong stellar heating also drives thermal atmospheric tides");
            }
        }

        if (tidalEffect.isEmpty()) {
            tidalEffect.append("Negligible atmospheric tidal effects");
        }

        weather.setAtmosphericTidalEffect(tidalEffect.toString());
    }

    // ================================================================
    // UTILITY
    // ================================================================

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
