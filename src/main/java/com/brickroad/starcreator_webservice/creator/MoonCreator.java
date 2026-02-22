package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ref.PlanetTypeRef;
import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.enums.AtmosphereClassification;
import com.brickroad.starcreator_webservice.enums.BinaryConfiguration;
import com.brickroad.starcreator_webservice.utils.ConversionFormulas;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import com.brickroad.starcreator_webservice.utils.planets.PlanetaryAtmosphere;
import com.brickroad.starcreator_webservice.utils.planets.PlanetaryComposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.random.RandomGenerator;

@Service
public class MoonCreator {

    @Autowired
    private AtmosphereCreator atmosphereCreator;

    @Autowired
    private CompositionCreator compositionCreator;

    @Autowired
    private GeologyCreator geologyCreator;

    @Autowired
    private RingCreator ringCreator;

    @Autowired
    private OrbitalCreator orbitalCreator;

    @Autowired
    private MagneticFieldCreator magneticFieldCreator;

    @Autowired
    private WaterCreator waterCreator;

    @Autowired
    private HabitabilityCreator habitabilityCreator;

    @Autowired
    private WeatherCreator weatherCreator;

    private static final double EARTH_MASS_KG = 5.972e24;
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double GRAVITATIONAL_CONSTANT_SI = 6.674e-11; // m³/(kg·s²)
    private static final double MIN_TRACKED_MOON_MASS = 1e-6;

    // ═══════════════════════════════════════════════════════════════
    //  Public entry point
    // ═══════════════════════════════════════════════════════════════

    public List<Moon> createMoons(Planet planet, Star primaryStar, PlanetTypeRef planetType) {
        List<Moon> moons = new ArrayList<>();

        double totalSatelliteMassBudget = calculateTotalMoonMassBudget(planet, planetType, primaryStar);

        RingCreator.RingSystemData ringPlan = ringCreator.planRingSystem(planet, totalSatelliteMassBudget);

        double moonMassBudget = totalSatelliteMassBudget - ringPlan.ringMassBudget;
        int numRegularMoons = determineNumberOfMoons(planet, primaryStar, moonMassBudget, planetType);

        if (numRegularMoons == 0 && ringPlan.suggestedShepherdMoons == 0) {
            if (ringPlan.shouldHaveRings) {
                createAndAttachRings(planet, ringPlan, ringPlan.ringMassBudget);
            }
            return moons;
        }

        double hillSphereKm = calculateHillSphere(planet, primaryStar);
        double innerRocheLimit = calculateRocheLimit(planet, 3.3);
        double outerRocheLimit = calculateRocheLimit(planet, 1.0);

        MoonDistributionResult distributionResult = distributeMoonMasses(
                numRegularMoons, moonMassBudget, planet, primaryStar, ringPlan);

        if (ringPlan.shouldHaveRings) {
            createAndAttachRings(planet, ringPlan, ringPlan.ringMassBudget);
        }

        for (int i = 0; i < distributionResult.moonData.size(); i++) {
            MoonGenerationData moonData = distributionResult.moonData.get(i);
            Moon previousMoon = moons.isEmpty() ? null : moons.getLast();
            Moon moon = createMoon(planet, primaryStar, i + 1, hillSphereKm,
                    innerRocheLimit, outerRocheLimit,
                    moonData.moonType, moonData.massEarthMasses, previousMoon);
            moons.add(moon);
        }

        // Generate weather for all moons AFTER all moons are created,
        // so sibling moons are available for tidal and sky appearance calculations.
        generateMoonWeather(moons, planet, primaryStar);

        int moonlets = calculateAdditionalMoonlets(planet) + distributionResult.redirectedToMoonlets;
        planet.setAdditionalMoonlets(moonlets);
        planet.setNumberOfMoons(moons.size() + moonlets);

        return moons;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Moon creation
    // ═══════════════════════════════════════════════════════════════

    private Moon createMoon(Planet planet, Star primaryStar, int moonNumber,
                            double hillSphereKm, double innerRocheLimit,
                            double outerRocheLimit, String predeterminedMoonType,
                            double moonMassEarth, Moon previousMoon) {
        Moon moon = new Moon();

        moon.setPlanet(planet);
        moon.setAgeMY(planet.getAgeMY());

        moon.setMoonType(predeterminedMoonType);
        setFormationType(moon, predeterminedMoonType);

        double estimatedTemp = estimateMoonTemperature(planet, primaryStar);
        if (estimatedTemp < 150) {
            moon.setCompositionType("ICY");
        } else if (estimatedTemp < 250) {
            moon.setCompositionType("MIXED");
        } else {
            moon.setCompositionType("ROCKY");
        }

        generatePhysicalProperties(moon, moonMassEarth);
        generateOrbitalProperties(moon, planet, hillSphereKm, innerRocheLimit, outerRocheLimit, previousMoon);
        calculateDerivedProperties(moon, planet, primaryStar);
        calculateTidalEffects(moon, planet);
        Object[] geoResult = determineGeologicalActivity(moon);
        String geologicalActivity = (String) geoResult[0];
        Boolean hasCryovolcanism = (Boolean) geoResult[1];

        TerrainProperties terrain = geologyCreator.createMoonTerrain(moon, geologicalActivity, hasCryovolcanism);
        moon.setTerrain(terrain);

        generateAtmosphere(moon);

        PlanetaryComposition composition = generateMoonComposition(moon);
        if (composition != null) {
            moon.setInteriorComposition(composition.toInteriorString());
            moon.setEnvelopeComposition(composition.toEnvelopeString());
            moon.setCompositionClassification(composition.getClassification().name());
        }

        // Water system — self-contained: handles subsurface ocean, surface water, tiny moonlet defaults
        WaterProperties water = waterCreator.createMoonWaterProperties(moon);
        moon.setWater(water);

        double moonMass = moon.getEarthMass() != null ? moon.getEarthMass() : 0;
        if (moonMass >= 0.0005) {
            // Magnetic field (Ganymede-style dynamos, Europa-style induced, or remnant)
            PlanetaryMagneticField moonMagField = magneticFieldCreator.generateMoonMagneticField(
                    moon, planet);
            moon.setMagneticField(moonMagField);

            // Habitability assessment (must be LAST — reads all other moon data)
            PlanetaryHabitability moonHab = habitabilityCreator.assessMoon(
                    moon, planet, primaryStar);
            moon.setHabitability(moonHab);
        }

        // Weather generation is deferred to after all moons are created,
        // so sibling moons are available for tidal and sky calculations.

        return moon;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Weather (post-creation pass)
    // ═══════════════════════════════════════════════════════════════

    private void generateMoonWeather(List<Moon> moons, Planet planet, Star primaryStar) {
        if (moons == null || moons.isEmpty()) return;

        StarSystem system = primaryStar != null ? primaryStar.getSystem() : null;

        for (Moon moon : moons) {
            if (Boolean.TRUE.equals(moon.getHasAtmosphere())) {
                PlanetaryWeather moonWeather = weatherCreator.generateMoonWeather(
                        moon, planet, primaryStar, system, moons);
                moon.setWeather(moonWeather);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Mass budget and distribution (MASS-FIRST approach)
    // ═══════════════════════════════════════════════════════════════

    private double calculateTotalMoonMassBudget(Planet planet, PlanetTypeRef planetType, Star primaryStar) {
        double planetMass = planet.getEarthMass();
        double metallicity = primaryStar.getMetallicity();

        Double minRatio = planetType.getMinMoonSystemMassRatio();
        Double maxRatio = planetType.getMaxMoonSystemMassRatio();

        if (minRatio == null || maxRatio == null) {
            minRatio = 0.00005;
            maxRatio = 0.0005;
        }

        double baseMassRatio = RandomUtils.rollRange(minRatio, maxRatio);

        double metallicityModifier = 1.0 + (metallicity * 0.3);
        baseMassRatio *= metallicityModifier;

        return planetMass * baseMassRatio;
    }

    private MoonDistributionResult distributeMoonMasses(int numMoons, double totalMassBudget,
                                                        Planet planet, Star primaryStar,
                                                        RingCreator.RingSystemData ringPlan) {
        List<MoonGenerationData> moonData = new ArrayList<>();

        int totalMoonCount = numMoons;
        if (ringPlan != null && ringPlan.suggestedShepherdMoons > 0) {
            totalMoonCount += ringPlan.suggestedShepherdMoons;
        }

        // Power-law mass distribution: 1/(i+1)^1.5
        double[] weights = new double[totalMoonCount];
        double totalWeight = 0;
        for (int i = 0; i < totalMoonCount; i++) {
            weights[i] = 1.0 / Math.pow(i + 1, 1.5);
            totalWeight += weights[i];
        }

        boolean isGasIceGiant = isGasIceGiant(planet);
        int redirectedToMoonlets = 0;

        // Distribute mass to regular moons, determine type from mass
        for (int i = 0; i < numMoons; i++) {
            double massShare = (weights[i] / totalWeight) * totalMassBudget;

            if (massShare < MIN_TRACKED_MOON_MASS) {
                // Too small for meaningful entity generation — count as moonlet
                redirectedToMoonlets++;
                continue;
            }

            String moonType = determineMoonTypeFromMass(massShare, planet, moonData.isEmpty(), isGasIceGiant);
            moonData.add(new MoonGenerationData(moonType, massShare));
        }

        // Shepherd moons (ring-associated, always tiny — but still tracked for ring linkage)
        if (ringPlan != null && ringPlan.suggestedShepherdMoons > 0) {
            int shepherdCount = ringPlan.suggestedShepherdMoons;
            for (int i = 0; i < shepherdCount; i++) {
                int weightIndex = numMoons + i;
                double massShare = (weights[weightIndex] / totalWeight) * totalMassBudget;
                double shepherdMass = Math.max(1e-10, Math.min(massShare, 1e-5));
                moonData.add(new MoonGenerationData("SHEPHERD", shepherdMass));
            }
        }

        return new MoonDistributionResult(moonData, redirectedToMoonlets);
    }

    private String determineMoonTypeFromMass(double massEarth, Planet planet,
                                             boolean isPrimaryMoon, boolean isGasIceGiant) {

        // Ganymede/Titan/Callisto class
        if (massEarth >= 0.01) {
            return "REGULAR_LARGE";
        }

        // Io/Europa/Triton/Earth's-Moon class
        if (massEarth >= 0.001) {
            // Rocky/terrestrial planets can produce collision-origin moons at this mass
            // The Earth-Moon system formed from a giant impact; this is the only known
            // mechanism for producing large moons around sub-giant planets.
            if (!isGasIceGiant && planet.getEarthMass() > 0.5) {
                if (isPrimaryMoon && RandomUtils.rollRange(0.0, 1.0) < 0.40) {
                    return "COLLISION_DEBRIS";
                }
                if (!isPrimaryMoon && RandomUtils.rollRange(0.0, 1.0) < 0.15) {
                    return "COLLISION_DEBRIS";
                }
            }
            return "REGULAR_MEDIUM";
        }

        // Mimas/Enceladus/Miranda class
        if (massEarth >= 0.00001) {
            return "REGULAR_SMALL";
        }

        // Marginal tracked moons (1e-6 to 1e-5 M⊕, ~40-100km)
        // Gas/ice giants can have Trojan moons at Lagrange points
        if (isGasIceGiant && RandomUtils.rollRange(0.0, 1.0) < 0.20) {
            return "TROJAN";
        }
        return "IRREGULAR_CAPTURED";
    }

    // ═══════════════════════════════════════════════════════════════
    //  Moon count determination
    // ═══════════════════════════════════════════════════════════════

    private int determineNumberOfMoons(Planet planet, Star primaryStar, double totalMassBudget, PlanetTypeRef planetType) {

        int minMoons = planetType.getMinMoons() != null ? planetType.getMinMoons() : 0;
        int maxMoons = planetType.getMaxMoons() != null ? planetType.getMaxMoons() : 0;

        if (minMoons == 0 && maxMoons == 0) {
            return 0;
        }

        double minPossibleMoonMass = 0.0000001;
        int maxPossibleFromBudget = (int) Math.floor(totalMassBudget / minPossibleMoonMass);

        int effectiveMax = Math.min(maxMoons, maxPossibleFromBudget);

        if (effectiveMax < minMoons) {
            if (totalMassBudget > minPossibleMoonMass) {
                return minMoons;
            } else {
                return 0;
            }
        }

        int targetMoons;
        int range = effectiveMax - minMoons;

        if (range <= 3) {
            // Small range: uniform distribution is fine
            targetMoons = RandomUtils.rollRange(minMoons, effectiveMax);
        } else {
            // Gaussian distribution across full range for natural spread
            // Budget gently nudges the mean but doesn't lock into a narrow band
            double budgetFactor = totalMassBudget / (planet.getEarthMass() * 0.001);
            double budgetInfluence = Math.min(1.0, budgetFactor);

            // Mean at 25-50% of range (lower moons slightly more common, budget shifts up)
            double meanFraction = 0.25 + 0.25 * budgetInfluence;
            double mean = minMoons + meanFraction * range;

            // Sigma = 28% of range gives good spread with natural tails
            double sigma = Math.max(range * 0.28, 2.0);

            double rawCount = RandomGenerator.getDefault().nextGaussian(mean, sigma);
            targetMoons = (int) Math.round(Math.max(minMoons, Math.min(effectiveMax, rawCount)));
        }

        // Metallicity adjustment
        double metallicity = primaryStar.getMetallicity();
        if (metallicity > 0.3 && RandomUtils.rollRange(0.0, 1.0) < 0.3) {
            targetMoons = Math.min(effectiveMax, targetMoons + 1);
        } else if (metallicity < -0.3 && RandomUtils.rollRange(0.0, 1.0) < 0.3) {
            targetMoons = Math.max(minMoons, targetMoons - 1);
        }

        return targetMoons;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Physical properties
    // ═══════════════════════════════════════════════════════════════

    private void generatePhysicalProperties(Moon moon, double moonMassEarth) {
        moon.setEarthMass(moonMassEarth);
        moon.setMass(moonMassEarth * EARTH_MASS_KG);

        double density = "ICY".equals(moon.getCompositionType()) ?
                RandomUtils.rollRange(0.9, 1.8) :
                RandomUtils.rollRange(2.5, 3.5);
        moon.setDensity(density);

        double radiusM = Math.cbrt((3 * moonMassEarth * EARTH_MASS_KG) /
                (4 * Math.PI * density * 1000));
        double radiusKm = radiusM / 1000;
        moon.setEarthRadius(radiusKm / EARTH_RADIUS_KM);
        moon.setRadius(radiusKm);
        moon.setCircumference(2 * Math.PI * radiusKm);

        double albedo = "ICY".equals(moon.getCompositionType()) ?
                RandomUtils.rollRange(0.5, 0.9) :
                RandomUtils.rollRange(0.1, 0.3);
        moon.setAlbedo(albedo);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Orbital properties
    // ═══════════════════════════════════════════════════════════════

    private void generateOrbitalProperties(Moon moon, Planet planet, double hillSphereKm,
                                           double innerRocheLimit, double outerRocheLimit, Moon previousMoon) {

        if ("SHEPHERD".equals(moon.getMoonType())) {
            generateShepherdMoonOrbit(moon, planet, innerRocheLimit, outerRocheLimit);
            return;
        }

        double rocheLimit = "ICY".equals(moon.getCompositionType()) ?
                outerRocheLimit : innerRocheLimit;
        double minOrbitKm = rocheLimit * 1.5;

        boolean isGiantPlanet = isGasIceGiant(planet);

        double maxOrbitKm;
        if (isGiantPlanet) {
            // Gas/ice giants: real major moons orbit at 3-60 planet radii
            // (Io at 6, Callisto at 26, Iapetus at 61 Rp)
            // Cap at 60 Rp or 0.15 Hill, whichever is smaller.
            double maxOrbitByRadii = planet.getRadius() * 60.0;
            double maxOrbitByHill = hillSphereKm * 0.15;
            maxOrbitKm = Math.min(maxOrbitByRadii, maxOrbitByHill);
        } else {
            // Rocky/terrestrial planets: moons can orbit at wide Rp ranges
            // (Earth's Moon at 60 Rp, Charon at 17 Rp of Pluto)
            // Use Hill sphere fraction — the actual gravitational stability limit.
            maxOrbitKm = hillSphereKm * 0.3;
        }

        // Ensure max > min (can happen for very small planets with large Roche limits)
        maxOrbitKm = Math.max(maxOrbitKm, minOrbitKm * 2.0);

        double semiMajorAxisKm;
        if ("IRREGULAR_CAPTURED".equals(moon.getMoonType())) {
            double irregularMin = Math.max(maxOrbitKm, hillSphereKm * 0.15);
            double irregularMax = hillSphereKm * 0.5;
            semiMajorAxisKm = RandomUtils.rollRange(irregularMin, irregularMax);
        } else if (isGiantPlanet) {
            double logMin = Math.log(minOrbitKm);
            double logMax = Math.log(maxOrbitKm);
            semiMajorAxisKm = Math.exp(RandomUtils.rollRange(logMin, logMax));
        } else {
            semiMajorAxisKm = RandomUtils.rollRange(minOrbitKm, maxOrbitKm);
        }

        if (previousMoon != null && previousMoon.getSemiMajorAxisKm() != null
                && previousMoon.getEarthMass() != null && moon.getEarthMass() != null) {
            double prevSmaKm = previousMoon.getSemiMajorAxisKm();
            double planetMassKg = planet.getMass();
            double prevMassKg = previousMoon.getEarthMass() * EARTH_MASS_KG;
            double moonMassKg = moon.getEarthMass() * EARTH_MASS_KG;

            // Mutual Hill radius in km
            double mu = (prevMassKg + moonMassKg) / (3.0 * planetMassKg);
            double mutualHillKm = Math.cbrt(mu) * (prevSmaKm + semiMajorAxisKm) / 2.0;

            // Require 3.46 * 2.0 (Gladman * safety) mutual Hill radii separation
            double minSeparationKm = 3.46 * 2.0 * mutualHillKm;

            // Also ensure no orbit crossing
            double prevApoapsis = prevSmaKm * (1.0 + (previousMoon.getEccentricity() != null ? previousMoon.getEccentricity() : 0.0));
            double minFromCrossing = prevApoapsis + mutualHillKm;

            double minimumSmaKm = Math.max(prevSmaKm + minSeparationKm, minFromCrossing);

            if (semiMajorAxisKm < minimumSmaKm) {
                semiMajorAxisKm = minimumSmaKm * RandomUtils.rollRange(1.0, 1.15);
            }
        }

        // Avoid ring gaps
        for (OrbitalBand band : planet.getBands()) {
            double ringInner = band.getInnerOrbit().getSemiMajorAxis();
            double ringOuter = band.getOuterOrbit().getSemiMajorAxis();
            double ringMargin = (ringOuter - ringInner) * 0.1;

            if (semiMajorAxisKm >= (ringInner - ringMargin) &&
                    semiMajorAxisKm <= (ringOuter + ringMargin)) {
                if (semiMajorAxisKm < ringInner) {
                    semiMajorAxisKm = ringInner - ringMargin - RandomUtils.rollRange(1000, 5000);
                } else {
                    semiMajorAxisKm = ringOuter + ringMargin + RandomUtils.rollRange(1000, 5000);
                }
            }
        }

        double eccentricity;
        if ("IRREGULAR_CAPTURED".equals(moon.getMoonType())) {
            eccentricity = RandomUtils.rollRange(0.1, 0.5);
        } else {
            // Normalize orbit fraction to the actual regular moon zone
            // (not Hill sphere, which is vastly larger and makes fractions tiny)
            double orbitRange = Math.max(1.0, maxOrbitKm - minOrbitKm);
            double orbitFraction = Math.max(0.0, Math.min(1.0,
                    (semiMajorAxisKm - minOrbitKm) / orbitRange));

            // Base eccentricity ramps from ~0.0005 (close-in, tidally circularized)
            // to ~0.02 (outer edge of regular zone, retains primordial eccentricity)
            double baseEcc = 0.0003 + 0.008 * Math.pow(orbitFraction, 0.8);

            // Tidal circularization is stronger around massive planets:
            // timescale ∝ 1/Mp^(5/2), so Jupiter circularizes ~60x faster than Earth.
            // Reduce eccentricity for close-in moons around massive planets.
            double planetMassEarth = planet.getEarthMass();
            if (planetMassEarth > 50 && orbitFraction < 0.3) {
                double circularizationFactor = 1.0 - 0.5 * Math.min(1.0, planetMassEarth / 500.0)
                        * (1.0 - orbitFraction / 0.3);
                baseEcc *= circularizationFactor;
            }

            // Resonance kick: ~15% chance, adds 0.003-0.008
            // This is the ONLY mechanism that maintains eccentricity against tidal damping
            // at close orbits (e.g., Io's Laplace resonance)
            boolean isResonant = RandomUtils.rollRange(0.0, 1.0) < 0.15;
            if (isResonant) {
                baseEcc += RandomUtils.rollRange(0.003, 0.008);
            }

            // Random scatter ±30%
            double scatter = RandomUtils.rollRange(0.7, 1.3);
            eccentricity = Math.max(0.0001, baseEcc * scatter);
        }
        double inclination = "IRREGULAR_CAPTURED".equals(moon.getMoonType()) ?
                RandomUtils.rollRange(10, 60) :
                RandomUtils.rollRange(0, 5);

        OrbitalElements orbit = orbitalCreator.createMoonOrbit(semiMajorAxisKm, planet.getMass(), eccentricity, inclination);
        moon.setOrbit(orbit);

        double periodDays = orbit.getOrbitalPeriodDays();

        moon.setTidallyLocked(!("IRREGULAR_CAPTURED".equals(moon.getMoonType()) && RandomUtils.rollRange(0.0, 1.0) < 0.7));

        if (moon.getTidallyLocked()) {
            moon.setRotationPeriodHours(periodDays * 24);
        } else {
            moon.setRotationPeriodHours(RandomUtils.rollRange(10.0, 100));
        }
        if ("CAPTURED".equals(moon.getFormationType()) && Math.random() < 0.5) {
            moon.setRotationPeriodHours(-Math.abs(moon.getRotationPeriodHours()));
        }

        moon.setAxialTilt(RandomUtils.rollRange(0.0, 25));

        // Per-moon Hill sphere
        double moonMassKg = moon.getMass();
        double planetMassKg = planet.getMass();
        double moonHillSphere = semiMajorAxisKm * Math.cbrt(moonMassKg / (3 * planetMassKg));
        moon.setHillSphereRadiusKm(moonHillSphere);

        // Per-moon Roche limit
        double moonDensity = moon.getDensity();
        double moonRadiusKm = moon.getRadius();
        double debrisDensity = 2.5;
        double moonRocheLimit = 2.46 * moonRadiusKm * Math.cbrt(moonDensity / debrisDensity);
        moon.setRocheLimitKm(moonRocheLimit);

        orbitalCreator.classifyMoonStability(moon.getOrbit(), semiMajorAxisKm, rocheLimit, hillSphereKm);
    }

    private void generateShepherdMoonOrbit(Moon moon, Planet planet,
                                           double innerRocheLimit, double outerRocheLimit) {
        double planetRadiusKm = planet.getRadius();
        double minOrbit = Math.max(outerRocheLimit * 1.1, planetRadiusKm * 1.5);
        double maxOrbit = planetRadiusKm * 5.0;

        double semiMajorAxisKm;
        if (planet.getBands() != null && !planet.getBands().isEmpty()) {
            OrbitalBand targetBand = planet.getBands().getFirst();
            for (OrbitalBand band : planet.getBands()) {
                if (band.getHasShepherdMoons() != null && band.getHasShepherdMoons()) {
                    targetBand = band;
                    break;
                }
            }

            double innerEdge = targetBand.getInnerOrbit().getSemiMajorAxis();
            double outerEdge = targetBand.getOuterOrbit().getSemiMajorAxis();
            double ringWidth = outerEdge - innerEdge;

            if (RandomUtils.rollRange(0.0, 1.0) < 0.5) {
                semiMajorAxisKm = innerEdge - (ringWidth * RandomUtils.rollRange(0.05, 0.10));
            } else {
                semiMajorAxisKm = outerEdge + (ringWidth * RandomUtils.rollRange(0.05, 0.10));
            }

            semiMajorAxisKm = Math.max(minOrbit, Math.min(semiMajorAxisKm, maxOrbit));
        } else {
            semiMajorAxisKm = RandomUtils.rollRange(minOrbit, maxOrbit);
        }

        double eccentricity = RandomUtils.rollRange(0.0001, 0.01);
        double inclination = RandomUtils.rollRange(0.0, 2.0);

        OrbitalElements orbit = orbitalCreator.createMoonOrbit(semiMajorAxisKm, planet.getMass(), eccentricity, inclination);
        moon.setOrbit(orbit);

        double periodDays = orbit.getOrbitalPeriodDays();

        moon.setTidallyLocked(true);
        moon.setRotationPeriodHours(periodDays * 24);

        moon.setAxialTilt(RandomUtils.rollRange(0.0, 10.0));

        double moonHillSphere = moon.getSemiMajorAxisKm() * Math.cbrt(moon.getMass() / (3 * planet.getMass()));
        moon.setHillSphereRadiusKm(moonHillSphere);

        double debrisDensity = 2.5;
        double moonRocheLimit = 2.46 * moon.getRadius() * Math.cbrt(moon.getDensity() / debrisDensity);
        moon.setRocheLimitKm(moonRocheLimit);

        double planetRocheLimit = "ICY".equals(moon.getCompositionType()) ?
                outerRocheLimit : innerRocheLimit;

        orbitalCreator.classifyMoonStability(moon.getOrbit(), semiMajorAxisKm, planetRocheLimit, Double.MAX_VALUE);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Derived properties (gravity, escape velocity, temperature)
    // ═══════════════════════════════════════════════════════════════

    private void calculateDerivedProperties(Moon moon, Planet planet, Star primaryStar) {
        double radiusKm = moon.getRadius();
        double massKg = moon.getMass();

        double surfaceGravityMS2 = (ConversionFormulas.GRAVITATIONAL_CONSTANT * massKg) /
                Math.pow(radiusKm * 1000, 2);
        moon.setSurfaceGravity(surfaceGravityMS2 / 9.81);

        double escapeVelocity = Math.sqrt((2 * ConversionFormulas.GRAVITATIONAL_CONSTANT * massKg) /
                (radiusKm * 1000));
        moon.setEscapeVelocity(escapeVelocity / 1000.0);

        moon.setSurfaceTemp(calculateSurfaceTemp(moon, planet, primaryStar));
    }

    private double calculateSurfaceTemp(Moon moon, Planet planet, Star primaryStar) {

        double distanceFromStarAU = planet.getSemiMajorAxisAU();
        double stellarLuminosity = calculateEffectiveLuminosity(primaryStar, distanceFromStarAU);
        double moonAlbedo = moon.getAlbedo();

        double solarConstant = 1361.0;
        double stellarFlux = solarConstant * stellarLuminosity / (distanceFromStarAU * distanceFromStarAU);

        double planetRadiusKm = planet.getRadius();
        double moonOrbitKm = moon.getSemiMajorAxisKm();

        double shadowFraction = Math.min(planetRadiusKm / moonOrbitKm, 0.2); // Cap at 20%
        double directFlux = stellarFlux * (1.0 - moonAlbedo) * (1.0 - shadowFraction * 0.5);

        double planetCrossSectionM2 = Math.PI * Math.pow(planetRadiusKm * 1000, 2);
        double moonOrbitM = moonOrbitKm * 1000;
        double reflectedFlux = stellarFlux * planet.getAlbedo() * planetCrossSectionM2 /
                (4 * Math.PI * Math.pow(moonOrbitM, 2));

        double sigma = 5.67e-8;
        double planetEmission = sigma * Math.pow(planet.getSurfaceTemp(), 4);
        double thermalFlux = planetEmission * planetCrossSectionM2 /
                (4 * Math.PI * Math.pow(moonOrbitM, 2));

        double totalFlux = directFlux + reflectedFlux * (1.0 - moonAlbedo) + thermalFlux;

        double baseTemp = Math.pow(totalFlux / sigma, 0.25);

        return (Math.max(baseTemp, 10.0));
    }

    private double estimateMoonTemperature(Planet planet, Star primaryStar) {
        double distanceAU = planet.getSemiMajorAxisAU();
        double stellarLuminosity = calculateEffectiveLuminosity(primaryStar, distanceAU);

        double typicalAlbedo = 0.12;
        double baseTemp = 278.0 * Math.pow(stellarLuminosity * (1 - typicalAlbedo), 0.25)
                / Math.sqrt(distanceAU);

        double tidalEstimate = 0;
        if (planet.getEarthMass() > 10) {
            tidalEstimate = 20;
        } else if (planet.getEarthMass() > 1) {
            tidalEstimate = 5;
        }

        return baseTemp + tidalEstimate;
    }

    private double calculateEffectiveLuminosity(Star primaryStar, double distanceFromStarAU) {
        double stellarLuminosity = primaryStar.getSolarLuminosity();

        if (primaryStar.getSystem() != null) {
            BinaryConfiguration config = primaryStar.getSystem().getBinaryConfiguration();

            if (config == BinaryConfiguration.P_TYPE) {
                stellarLuminosity = primaryStar.getSystem().getStars().stream()
                        .mapToDouble(Star::getSolarLuminosity)
                        .sum();

            } else if (config == BinaryConfiguration.S_TYPE_CLOSE) {
                Star companionStar = primaryStar.getCompanionStar();
                if (companionStar != null) {
                    double companionLuminosity = companionStar.getSolarLuminosity();
                    double binarySeparation = primaryStar.getSystem().getBinarySeparationAu();
                    double companionDistance = Math.sqrt(
                            distanceFromStarAU * distanceFromStarAU +
                                    binarySeparation * binarySeparation
                    );
                    double companionEffectiveLuminosity = companionLuminosity *
                            Math.pow(distanceFromStarAU / companionDistance, 2);
                    stellarLuminosity += companionEffectiveLuminosity;
                }
            }
        }

        return stellarLuminosity;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Tidal effects and geological activity
    // ═══════════════════════════════════════════════════════════════

    private void calculateTidalEffects(Moon moon, Planet planet) {
        double planetMassKg = planet.getMass();
        double moonRadiusM = moon.getRadius() * 1000;
        double semiMajorAxisM = moon.getSemiMajorAxisKm() * 1000;
        double eccentricity = moon.getEccentricity();

        // Composition-dependent tidal dissipation factor (k₂/Q)
        double k2OverQ = calculateTidalK2Q(moon);

        // Orbital mean motion: n = sqrt(G * Mp / a³)
        double meanMotion = Math.sqrt(GRAVITATIONAL_CONSTANT_SI * planetMassKg /
                Math.pow(semiMajorAxisM, 3));

        // Tidal dissipation power (Peale, Cassen & Reynolds 1979):
        //   dE/dt = (21/2) * (k₂/Q) * G * Mp² * Rm⁵ * n * e² / a⁶
        double tidalPower = (21.0 / 2.0) * k2OverQ * GRAVITATIONAL_CONSTANT_SI
                * Math.pow(planetMassKg, 2) * Math.pow(moonRadiusM, 5)
                * meanMotion * Math.pow(eccentricity, 2)
                / Math.pow(semiMajorAxisM, 6);

        // Convert to surface flux (W/m²)
        double surfaceArea = 4.0 * Math.PI * Math.pow(moonRadiusM, 2);
        double tidalHeatingWattPerM2 = tidalPower / surfaceArea;

        moon.setTidalHeatingWattPerM2(tidalHeatingWattPerM2);

        if (tidalHeatingWattPerM2 < 0.01) {
            moon.setTidalHeatingLevel("NONE");
        } else if (tidalHeatingWattPerM2 < 0.1) {
            moon.setTidalHeatingLevel("LOW");
        } else if (tidalHeatingWattPerM2 < 1.0) {
            moon.setTidalHeatingLevel("MODERATE");
        } else if (tidalHeatingWattPerM2 < 10.0) {
            moon.setTidalHeatingLevel("HIGH");
        } else {
            moon.setTidalHeatingLevel("EXTREME");
        }

        if (tidalHeatingWattPerM2 > 0.01) {
            double tidalTempBoost = Math.sqrt(tidalHeatingWattPerM2) * 30.0;
            moon.setSurfaceTemp(moon.getSurfaceTemp() + tidalTempBoost);
        }
    }

    private double calculateTidalK2Q(Moon moon) {
        String composition = moon.getCompositionType();

        double baseK2Q = switch (composition) {
            case "ICY" -> 0.01;      // Ice is more deformable than rock
            case "MIXED" -> 0.006;   // Intermediate
            case "ROCKY" -> 0.003;   // Rigid rock, low dissipation
            default -> 0.006;
        };

        // Larger moons are more deformable (higher k₂) due to
        // self-gravity and pressure-dependent rheology
        double massEarth = moon.getEarthMass() != null ? moon.getEarthMass() : 0;
        if (massEarth > 0.01) {
            baseK2Q *= 1.5;  // Ganymede/Titan class: more self-gravitating
        } else if (massEarth < 0.0001) {
            baseK2Q *= 0.5;  // Very small: more rigid, less dissipation
        }

        return baseK2Q;
    }

    /**
     * Determines geological activity level and cryovolcanism, returning them as a two-element array.
     * [0] = geologicalActivity (String), [1] = hasCryovolcanism (Boolean)
     */
    private Object[] determineGeologicalActivity(Moon moon) {
        Double tidalHeatingWattPerM2 = moon.getTidalHeatingWattPerM2();

        boolean hasSignificantTidalHeating = tidalHeatingWattPerM2 != null && tidalHeatingWattPerM2 > 0.5;

        double ageModifier = hasSignificantTidalHeating ?
                1.0 : (1.0 / (1.0 + moon.getAgeMY() / 5000.0));
        double activityScore = moon.getEarthMass() * 100 * ageModifier;

        if (tidalHeatingWattPerM2 != null && tidalHeatingWattPerM2 > 0.001) {
            activityScore += (Math.log10(tidalHeatingWattPerM2) + 3) * 3;
        }

        String geologicalActivity;
        Boolean hasCryovolcanism = false;

        if (activityScore > 10) {
            geologicalActivity = "HIGH";
            hasCryovolcanism = "ICY".equals(moon.getCompositionType());
        } else if (activityScore > 7.5) {
            geologicalActivity = "MODERATE";
            hasCryovolcanism = "ICY".equals(moon.getCompositionType()) && RandomUtils.rollRange(0.0, 1.0) < 0.6;
        } else if (activityScore > 3) {
            geologicalActivity = "LOW";
            if ("ICY".equals(moon.getCompositionType()) && tidalHeatingWattPerM2 != null && tidalHeatingWattPerM2 > 0.8) {
                hasCryovolcanism = RandomUtils.rollRange(0.0, 1.0) < 0.3;
            }
        } else {
            geologicalActivity = "NONE";
        }

        return new Object[]{ geologicalActivity, hasCryovolcanism };
    }

    // ═══════════════════════════════════════════════════════════════
    //  Atmosphere generation
    // ═══════════════════════════════════════════════════════════════

    private void generateAtmosphere(Moon moon) {
        Planet planet = moon.getPlanet();

        if (moon.getEscapeVelocity() < 0.3) {
            moon.setAtmosphere(atmosphereCreator.createNoAtmosphereEntity());
            return;
        }

        boolean strippedByMagneticField = checkMagneticFieldStripping(moon, planet);
        if (strippedByMagneticField) {
            moon.setAtmosphere(atmosphereCreator.createStrippedAtmosphereEntity(
                    "Atmosphere stripped by parent planet's magnetosphere"));
            return;
        }

        boolean shouldHaveAtmosphere = determineAtmospherePresence(moon);
        if (!shouldHaveAtmosphere) {
            moon.setAtmosphere(atmosphereCreator.createNoAtmosphereEntity());
            return;
        }

        String moonType = determineMoonTypeForAtmosphere(moon);

        AtmosphereCreator.AtmosphereResult result = atmosphereCreator.generateAtmosphereWithTemplate(
                moonType,
                moon.getSurfaceTemp(),
                moon.getEarthMass(),
                planet.getSemiMajorAxisAU(),
                planet.getParentStar()
        );

        PlanetaryAtmosphere generatedAtmosphere = result.atmosphere();
        if (generatedAtmosphere.getClassification() == AtmosphereClassification.NONE) {
            moon.setAtmosphere(atmosphereCreator.createNoAtmosphereEntity());
            return;
        }

        double surfacePressure = atmosphereCreator.calculateSurfacePressure(
                moon.getEarthMass(),
                moon.getSurfaceTemp(),
                result.template()
        );

        Atmosphere atmosphereEntity = atmosphereCreator.toAtmosphereEntity(generatedAtmosphere, surfacePressure);
        moon.setAtmosphere(atmosphereEntity);
    }

    private boolean checkMagneticFieldStripping(Moon moon, Planet planet) {
        Double planetMagneticField = planet.getMagneticFieldStrength();

        if (planetMagneticField == null || planetMagneticField < 0.1) {
            return false;
        }

        double moonOrbitKm = moon.getSemiMajorAxisKm();
        double magnetosphereRadiusKm = calculateMagnetosphereRadius(planet, planetMagneticField);

        if (moonOrbitKm < magnetosphereRadiusKm) {
            double strippingProbability = calculateStrippingProbability(
                    moon.getEarthMass(),
                    planetMagneticField,
                    moonOrbitKm,
                    magnetosphereRadiusKm
            );

            return RandomUtils.rollRange(0.0, 1.0) < strippingProbability;
        }

        if (planet.getMagneticField() != null && planet.getMagneticField().getMagnetosphereExists()
                && planet.getMagneticField().getMagnetopauseDistancePlanetRadii() != null
                && moon.getSemiMajorAxisKm() != null && planet.getRadius() > 0) {

            double moonDistanceRadii = moon.getSemiMajorAxisKm() / planet.getRadius();
            double magnetopauseRadii = planet.getMagneticField().getMagnetopauseDistancePlanetRadii();

            if (moonDistanceRadii < magnetopauseRadii * 0.8) {
                return false;
            }
        }

        return false;
    }

    private double calculateMagnetosphereRadius(Planet planet, double magneticFieldStrength) {
        double planetRadiusKm = planet.getRadius();
        double baseRadiusMultiplier = 10.0;
        double fieldFactor = Math.sqrt(magneticFieldStrength);

        return planetRadiusKm * baseRadiusMultiplier * fieldFactor;
    }

    private double calculateStrippingProbability(double moonMass, double magneticField,
                                                 double orbitKm, double magnetosphereKm) {

        if (orbitKm > magnetosphereKm * 0.5) {
            return 0.0;
        }
        double distanceFactor = 1.0 - (orbitKm / (magnetosphereKm * 0.5));
        distanceFactor = Math.max(0.0, distanceFactor);

        double massFactor;
        if (moonMass < 0.005) {
            massFactor = 0.7;
        } else if (moonMass < 0.01) {
            massFactor = 0.4;
        } else if (moonMass < 0.05) {
            massFactor = 0.2;
        } else if (moonMass < 0.1) {
            massFactor = 0.1;
        } else {
            massFactor = 0.05;
        }

        double fieldFactor = Math.min(1.0, magneticField / 10.0);

        return distanceFactor * massFactor * fieldFactor * 0.5;
    }

    private boolean determineAtmospherePresence(Moon moon) {
        double atmosphereChance = 0.0;

        if (moon.getEarthMass() > 0.02) {
            atmosphereChance += 0.5;
        } else if (moon.getEarthMass() > 0.01) {
            atmosphereChance += 0.35;
        } else if (moon.getEarthMass() > 0.001) {
            atmosphereChance += 0.15;
        }

        if ("HIGH".equals(moon.getGeologicalActivity())) {
            atmosphereChance += 0.4;
        } else if ("MODERATE".equals(moon.getGeologicalActivity())) {
            atmosphereChance += 0.25;
        } else if ("LOW".equals(moon.getGeologicalActivity())) {
            atmosphereChance += 0.1;
        }

        if (moon.getHasCryovolcanism()) {
            atmosphereChance += 0.3;
        }

        atmosphereChance = Math.min(0.95, atmosphereChance);

        return RandomUtils.rollRange(0.0, 1.0) < atmosphereChance;
    }

    private String determineMoonTypeForAtmosphere(Moon moon) {
        String compositionType = moon.getCompositionType();
        String geologicalActivity = moon.getGeologicalActivity();
        double earthMass = moon.getEarthMass();
        boolean hasCryovolcanism = moon.getHasCryovolcanism() != null && moon.getHasCryovolcanism();
        String moonType = moon.getMoonType();

        if ("IRREGULAR_CAPTURED".equals(moonType) || "SHEPHERD".equals(moonType) || "TROJAN".equals(moonType)) {
            return "Captured Body";
        }

        if ("ICY".equals(compositionType)) {
            if (hasCryovolcanism) {
                return "Cryovolcanic Moon";
            }
            if (earthMass > 0.01) {
                return "Icy Moon Large";
            }
            return "Icy Moon";
        }

        if ("ROCKY".equals(compositionType) || "MIXED".equals(compositionType)) {
            if ("HIGH".equals(geologicalActivity) || "MODERATE".equals(geologicalActivity)) {
                if (moon.getSurfaceTemp() > 150) {
                    return "Volcanic Moon";
                }
            }
            if (earthMass > 0.01) {
                return "Rocky Moon Large";
            }
            return "Rocky Moon";
        }

        if (earthMass > 0.01) {
            return "Rocky Moon Large";
        }
        return "Rocky Moon";
    }

    // ═══════════════════════════════════════════════════════════════
    //  Composition
    // ═══════════════════════════════════════════════════════════════

    private PlanetaryComposition generateMoonComposition(Moon moon) {
        double surfaceTemp = moon.getSurfaceTemp() != null ? moon.getSurfaceTemp() : 100.0;

        String moonTypeForTemplate = mapMoonTypeToTemplateType(moon);

        return compositionCreator.generateComposition(
                moonTypeForTemplate,
                surfaceTemp,
                moon.getCompositionType()
        );
    }

    private String mapMoonTypeToTemplateType(Moon moon) {
        String moonType = moon.getMoonType();
        String compositionType = moon.getCompositionType();
        String geologicalActivity = moon.getGeologicalActivity();
        Boolean hasCryovolcanism = moon.getHasCryovolcanism();

        if ("ROCKY".equalsIgnoreCase(compositionType) &&
                ("HIGH".equals(geologicalActivity) || "MODERATE".equals(geologicalActivity)) &&
                moon.getSurfaceTemp() > 150) {
            if (moon.getAtmosphere() != null &&
                    "VOLCANIC".equals(moon.getAtmosphere().getClassification())) {
                return "Volcanic Moon";
            }
        }

        if ("ICY".equalsIgnoreCase(compositionType) && Boolean.TRUE.equals(hasCryovolcanism)) {
            return "Cryovolcanic Moon";
        }

        if (moon.getDensity() != null && moon.getDensity() > 4.5) {
            return "Metal Rich Moon";
        }

        return switch (moonType) {
            case "REGULAR_LARGE" -> "REGULAR_LARGE";
            case "REGULAR_MEDIUM" -> "REGULAR_MEDIUM";
            case "REGULAR_SMALL" -> "REGULAR_SMALL";
            case "COLLISION_DEBRIS" -> "COLLISION_DEBRIS";
            case "SHEPHERD" -> "SHEPHERD";
            case "TROJAN" -> "TROJAN";
            case "IRREGULAR_CAPTURED" -> "IRREGULAR_CAPTURED";
            default -> {
                System.out.println("Warning: Unexpected moon type '" + moonType + "', using REGULAR_SMALL as fallback");
                yield "REGULAR_SMALL";
            }
        };
    }

    // ═══════════════════════════════════════════════════════════════
    //  Subsurface ocean
    // ═══════════════════════════════════════════════════════════════

    // ═══════════════════════════════════════════════════════════════
    //  Ring integration
    // ═══════════════════════════════════════════════════════════════

    private void createAndAttachRings(Planet planet, RingCreator.RingSystemData ringPlan, double ringMassEarth) {
        List<OrbitalBand> rings = ringCreator.createRings(planet, ringPlan, ringMassEarth);

        if (!rings.isEmpty()) {
            planet.setBands(rings);
            planet.setHasRings(true);

            ringCreator.linkShepherdMoons(planet);
        } else {
            planet.setHasRings(false);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Moonlet calculation
    // ═══════════════════════════════════════════════════════════════

    private int calculateAdditionalMoonlets(Planet planet) {
        String planetType = planet.getPlanetType();
        double mass = planet.getEarthMass() != null ? planet.getEarthMass() : 0;

        // Inner rocky worlds and close-in giants: tidal forces clear small bodies
        if (planetType == null) return 0;
        if (planetType.contains("Hot") || planetType.contains("Lava")
                || planetType.contains("Iron") || planetType.contains("Puffy")) {
            return 0;
        }

        // Rocky/icy small bodies: rarely have moonlet swarms
        if (planetType.contains("Terrestrial") || planetType.contains("Desert")
                || planetType.contains("Carbon") || planetType.contains("Ocean")) {
            return 0;
        }
        if (planetType.contains("Dwarf") || planetType.contains("Ice World")) {
            if (RandomUtils.rollRange(0.0, 1.0) < 0.15) {
                return RandomUtils.rollRange(1, 3);
            }
            return 0;
        }
        if (planetType.contains("Super-Earth")) {
            if (RandomUtils.rollRange(0.0, 1.0) < 0.2) {
                return RandomUtils.rollRange(1, 5);
            }
            return 0;
        }

        // Gas/ice worlds: mass-dependent swarms of captured irregulars
        double base;
        double sigma;
        int cap;

        if (planetType.contains("Mini-Neptune") || planetType.contains("Warm Neptune")) {
            base = mass * 0.5;
            sigma = Math.max(1, base * 0.5);
            cap = 20;
        } else if (planetType.contains("Sub-Neptune")) {
            base = mass * 0.5;
            sigma = Math.max(1, base * 0.5);
            cap = 20;
        } else if (planetType.contains("Ice Giant")) {
            base = mass * 1.2;
            sigma = Math.max(3, base * 0.5);
            cap = 40;
        } else if (planetType.contains("Super-Jupiter")) {
            base = Math.sqrt(mass) * 4;
            sigma = Math.max(10, base * 0.5);
            cap = 300;
        } else if (planetType.contains("Gas Giant")) {
            base = Math.sqrt(mass) * 5;
            sigma = Math.max(5, base * 0.5);
            cap = 200;
        } else {
            return 0;
        }

        double raw = RandomGenerator.getDefault().nextGaussian(base, sigma);
        return (int) Math.max(0, Math.min(cap, Math.round(raw)));
    }

    // ═══════════════════════════════════════════════════════════════
    //  Helper methods
    // ═══════════════════════════════════════════════════════════════

    private void setFormationType(Moon moon, String moonType) {
        switch (moonType) {
            case "COLLISION_DEBRIS" -> moon.setFormationType("COLLISION_DEBRIS");
            case "IRREGULAR_CAPTURED", "TROJAN" -> moon.setFormationType("CAPTURED");
            default -> moon.setFormationType("CO_FORMED");
        }
    }

    private double calculateHillSphere(Planet planet, Star primaryStar) {
        double semiMajorAxisKm = planet.getSemiMajorAxisAU() * ConversionFormulas.AU_TO_KM;
        return semiMajorAxisKm * Math.cbrt(planet.getMass() / (3 * primaryStar.getMass()));
    }

    private double calculateRocheLimit(Planet planet, double moonDensity) {
        return 2.46 * planet.getRadius() * Math.cbrt(planet.getDensity() / moonDensity);
    }

    private boolean isGasIceGiant(Planet planet) {
        String type = planet.getPlanetType();
        return type != null && (type.contains("Gas Giant") || type.contains("Ice Giant")
                || type.contains("Jupiter") || type.contains("Sub-Neptune"));
    }

    // ═══════════════════════════════════════════════════════════════
    //  Internal data structures
    // ═══════════════════════════════════════════════════════════════

    private record MoonGenerationData(String moonType, double massEarthMasses) {
    }

    private record MoonDistributionResult(
            List<MoonGenerationData> moonData,
            int redirectedToMoonlets
    ) {
    }
}