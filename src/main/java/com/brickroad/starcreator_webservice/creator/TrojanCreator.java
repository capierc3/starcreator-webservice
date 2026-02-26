package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ref.AsteroidTypeRef;
import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.enums.BandCategory;
import com.brickroad.starcreator_webservice.enums.DistanceUnit;
import com.brickroad.starcreator_webservice.repository.AsteroidTypeRefRepository;
import com.brickroad.starcreator_webservice.utils.ConversionFormulas;
import com.brickroad.starcreator_webservice.utils.PhysicsFormulas;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import com.brickroad.starcreator_webservice.utils.TemperatureCalculator;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates Trojan asteroid/moon swarms at the L4 and L5 Lagrange points of planets.
 * <p>
 * Trojans are planet-parented bands (like rings) but belt-like in content:
 * they contain asteroids, and large swarms can include Trojan moons.
 * <p>
 * Output is tiered by swarm mass:
 * <ul>
 *   <li><b>Tiny</b> (&lt; 1e-9 M⊕): object count only, no stored asteroids</li>
 *   <li><b>Medium</b> (≥ 1e-9 M⊕): 1–3 notable asteroids (D/P/C-type)</li>
 *   <li><b>Large</b> (≥ 1e-6 M⊕, gas/ice giant): Trojan moon(s)</li>
 * </ul>
 */
@Service
public class TrojanCreator {

    @Autowired
    private OrbitalCreator orbitalCreator;

    @Autowired
    private AsteroidTypeRefRepository asteroidTypeRefRepository;

    @Autowired
    private MoonCreator moonCreator;

    private List<AsteroidTypeRef> cachedAsteroidTypes;

    private static final double EARTH_MASS_KG = PhysicsFormulas.EARTH_MASS_KG;
    private static final double EARTH_RADIUS_KM = PhysicsFormulas.EARTH_RADIUS_KM;
    private static final double AU_IN_KM = ConversionFormulas.AU_TO_KM;

    // ── Mass thresholds for tiered content ──
    private static final double MEDIUM_SWARM_THRESHOLD = 1e-9;   // M⊕ — notable asteroids
    private static final double LARGE_SWARM_THRESHOLD = 1e-6;    // M⊕ — Trojan moon(s)

    // ── Jupiter benchmark for object count scaling ──
    // Jupiter has ~1 million Trojans at ~0.00002 M⊕ total swarm mass
    private static final double JUPITER_TROJAN_COUNT = 1_000_000.0;
    private static final double JUPITER_TROJAN_MASS = 0.00002;   // M⊕

    @PostConstruct
    public void init() {
        cachedAsteroidTypes = asteroidTypeRefRepository.findAllAsteroidTypes();
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Public Entry Point
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Creates Trojan swarms for all eligible planets in the system.
     * Iterates all stars → all planets, evaluating each for L4/L5 swarm formation.
     */
    public void createTrojansForSystem(StarSystem system) {
        for (Star star : system.getStars()) {
            for (Planet planet : star.getPlanets()) {
                tryCreateTrojansForPlanet(planet, star);
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Per-Planet Evaluation
    // ═════════════════════════════════════════════════════════════════════

    private void tryCreateTrojansForPlanet(Planet planet, Star star) {
        // Skip dwarf planets (orbital position < 0)
        if (planet.getOrbitalPosition() != null && planet.getOrbitalPosition() < 0) return;

        // Minimum mass for stable L4/L5 regions (dwarf planets < 0.5 M⊕ can't hold trojans)
        Double planetMass = planet.getEarthMass();
        if (planetMass != null && planetMass < 0.5) return;

        // Need a valid orbit
        Double sma = planet.getSemiMajorAxisAU();
        if (sma == null || sma <= 0) return;

        // Skip if eccentricity too high — destabilizes Lagrange points
        Double ecc = planet.getEccentricity();
        if (ecc != null && ecc > 0.3) return;

        // Skip if inclination too high
        Double inc = planet.getOrbitalInclinationDegrees();
        if (inc != null && inc > 60.0) return;

        // ── Determine formation probability ──
        double baseChance = getBaseChance(planet);
        if (baseChance <= 0) return;

        // Eccentricity penalty: higher ecc → less stable L-points
        double eccValue = ecc != null ? ecc : 0.0;
        double eccPenalty = Math.max(0.1, 1.0 - (eccValue / 0.3) * 0.8);

        double formationChance = baseChance * eccPenalty;

        if (RandomUtils.rollRange(0.0, 1.0) > formationChance) return;

        // ── Calculate total swarm mass ──
        double planetMassEarth = planet.getEarthMass() != null ? planet.getEarthMass() : 1.0;
        double stellarMetallicity = star.getMetallicity() != null ? star.getMetallicity() : 0.0;
        double totalSwarmMass = calculateSwarmMass(planetMassEarth, planet, stellarMetallicity);

        // ── Split mass between L4 and L5 ──
        // L4 gets 55-65% (observed asymmetry in Jupiter's Trojans)
        double l4Fraction = RandomUtils.rollRange(0.55, 0.65);
        double l4Mass = totalSwarmMass * l4Fraction;
        double l5Mass = totalSwarmMass * (1.0 - l4Fraction);

        // L4 always forms if we got this far
        createTrojanSwarm(planet, star, "L4", l4Mass);

        // L5 has 85% chance of forming
        if (RandomUtils.rollRange(0.0, 1.0) < 0.85) {
            createTrojanSwarm(planet, star, "L5", l5Mass);
        }
    }

    private double getBaseChance(Planet planet) {
        String type = planet.getPlanetType();
        if (type == null) return 0.0;
        String lc = type.toLowerCase();

        // Gas Giant / Super-Jupiter
        if (lc.contains("gas giant") || lc.contains("super-jupiter")) return 0.70;

        // Ice Giant
        if (lc.contains("ice giant")) return 0.55;

        // Sub-Neptune / Mini-Neptune / Warm Neptune
        if (lc.contains("neptune") || lc.contains("sub-neptune")) return 0.30;

        // Super-Earth
        if (lc.contains("super-earth")) return 0.10;

        // Terrestrial / Ocean / Desert
        if (lc.contains("terrestrial") || lc.contains("ocean") || lc.contains("desert")) return 0.05;

        // Hot Jupiter, Lava World, etc. — very close-in, tidal disruption
        if (lc.contains("hot") || lc.contains("lava") || lc.contains("puffy") || lc.contains("iron")) return 0.02;

        // Dwarf planets: insufficient mass for stable L4/L5 regions, often in MMR with giants
        if (lc.contains("dwarf")) return 0.0;

        // Ice worlds
        if (lc.contains("ice world")) return 0.02;

        // Carbon planets, other exotic types
        return 0.05;
    }

    private double calculateSwarmMass(double planetMassEarth, Planet planet, double metallicity) {
        boolean isGiant = isGasIceGiant(planet);

        double minRatio, maxRatio;
        if (isGiant) {
            minRatio = 1e-8;
            maxRatio = 5e-7;
        } else {
            minRatio = 1e-10;
            maxRatio = 1e-8;
        }

        double massRatio = RandomUtils.rollRange(minRatio, maxRatio);

        // Metallicity modifier: more metals → more building material
        double metallicityMod = 1.0 + metallicity * 1.5;
        metallicityMod = Math.max(0.5, Math.min(2.0, metallicityMod));

        return planetMassEarth * massRatio * metallicityMod;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Swarm Creation
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Creates a single Trojan swarm band at the specified Lagrange point.
     */
    private void createTrojanSwarm(Planet planet, Star star, String lagrangePoint, double swarmMassEarth) {
        OrbitalBand swarm = new OrbitalBand();
        swarm.setBandCategory(BandCategory.TROJAN);
        swarm.setBandType("TROJAN");
        swarm.setPlanet(planet);
        swarm.setLagrangePoint(lagrangePoint);
        swarm.deriveMeanLibrationOffset();

        // ── Libration amplitude (random per swarm) ──
        // Tadpole orbits require amplitudes < ~60° (beyond that → horseshoe orbits).
        // Jupiter's trojans have amplitudes up to ~35°, capped here at 55° for safety.
        // Use weighted distribution: 70% chance of moderate 10–35°, rest spans 0.5–55°.
        double rawAmplitude;
        if (RandomUtils.rollRange(0.0, 1.0) < 0.70) {
            rawAmplitude = RandomUtils.rollRange(10.0, 35.0);
        } else {
            rawAmplitude = RandomUtils.rollRange(0.5, 55.0);
        }
        swarm.setLibrationAmplitudeDeg(rawAmplitude);

        // ── Mass ──
        swarm.setTotalMassEarthMasses(swarmMassEarth);
        swarm.setTotalMassKg(swarmMassEarth * EARTH_MASS_KG);

        // ── Object count (scaled from Jupiter benchmark) ──
        double countScaling = swarmMassEarth / JUPITER_TROJAN_MASS;
        long objectCount = Math.max(1, (long) (JUPITER_TROJAN_COUNT * countScaling * RandomUtils.rollRange(0.5, 2.0)));
        swarm.setEstimatedObjectCount(objectCount);

        // ── Orbital boundaries: planet SMA ± ~1 Hill radius ──
        Double planetSma = planet.getSemiMajorAxisAU();
        double planetMassEarth = planet.getEarthMass() != null ? planet.getEarthMass() : 1.0;
        double starMassSolar = star.getSolarMass() > 0 ? star.getSolarMass() : 1.0;

        // Hill radius in AU
        double planetSmaKm = planetSma * AU_IN_KM;
        double planetMassKg = planetMassEarth * EARTH_MASS_KG;
        double starMassKg = ConversionFormulas.solarMassToKG(starMassSolar);
        double hillRadiusKm = PhysicsFormulas.hillSphereRadiusKm(planetSmaKm, planetMassKg, starMassKg);
        double hillRadiusAU = hillRadiusKm / AU_IN_KM;

        // Randomize radial extent slightly
        double innerEdge = planetSma - hillRadiusAU * RandomUtils.rollRange(0.8, 1.2);
        double outerEdge = planetSma + hillRadiusAU * RandomUtils.rollRange(0.8, 1.2);
        innerEdge = Math.max(0.01, innerEdge); // Prevent negative/zero distance

        // ── Eccentricity & inclination stats ──
        double avgEcc = RandomUtils.rollRange(0.03, 0.08);
        double maxEcc = RandomUtils.rollRange(avgEcc, 0.15);
        double avgInc = RandomUtils.rollRange(5.0, 15.0);
        double maxInc = RandomUtils.rollRange(avgInc, 40.0);

        swarm.setAverageEccentricity(avgEcc);
        swarm.setMaxEccentricity(maxEcc);
        swarm.setAverageInclinationDeg(avgInc);
        swarm.setMaxInclinationDeg(maxInc);

        // ── Orbital elements for edges (in AU, orbiting the star) ──
        double stellarMass = starMassSolar;
        swarm.setInnerOrbit(orbitalCreator.createBandEdgeOrbit(
                innerEdge, DistanceUnit.AU, stellarMass, avgEcc, avgInc,
                lagrangePoint + " Trojan swarm inner edge"));
        swarm.setOuterOrbit(orbitalCreator.createBandEdgeOrbit(
                outerEdge, DistanceUnit.AU, stellarMass, avgEcc, avgInc,
                lagrangePoint + " Trojan swarm outer edge"));

        swarm.setPeakDensityDistance(planetSma); // Peak density at the planet's SMA

        // ── Composition: dark, icy, organic-rich (D/P/C-type asteroids) ──
        swarm.setCompositionType("DARK_ORGANIC");
        swarm.setPrimaryComposition("Silicates 30%, Organics 25%, Water Ice 25%, Carbon 15%, Iron 5%");
        swarm.setAlbedo(RandomUtils.rollRange(0.04, 0.08));

        // ── No structural features ──
        swarm.setHasGaps(false);
        swarm.setHasCollisionalFamilies(false);

        // ── Description ──
        swarm.setDescription(generateSwarmDescription(swarm, planet, lagrangePoint));

        // ── Tiered content ──
        if (swarmMassEarth >= MEDIUM_SWARM_THRESHOLD) {
            generateTrojanAsteroids(swarm, planet, star, stellarMass, swarmMassEarth);
        }
        if (swarmMassEarth >= LARGE_SWARM_THRESHOLD && isGasIceGiant(planet)) {
            // Trojan moons are extremely rare — only 4 confirmed among ~15,000+ Jupiter trojans.
            // Even accounting for detection bias, maybe ~4-15% of large trojans have satellites.
            // Gas giants have deeper gravity wells → slightly more capture potential.
            double moonChance = getTrojanMoonChance(planet, swarmMassEarth);
            if (RandomUtils.rollRange(0.0, 1.0) < moonChance) {
                generateTrojanMoons(swarm, planet, star, swarmMassEarth);
            }
        }

        // ── Attach to planet ──
        planet.getBands().add(swarm);
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Notable Asteroid Generation (Medium+ swarms)
    // ═════════════════════════════════════════════════════════════════════

    private void generateTrojanAsteroids(OrbitalBand swarm, Planet planet, Star star,
                                          double stellarMass, double swarmMassEarth) {
        int count = determineTrojanAsteroidCount(planet, swarmMassEarth);
        if (count <= 0) return;

        // D, P, C types are dominant in Trojan populations.
        // Exclude Centaurs (CE) and Scattered Disk (SD) — dynamically unstable
        // populations that cannot exist in stable L4/L5 resonance.
        List<AsteroidTypeRef> eligibleTypes = cachedAsteroidTypes.stream()
                .filter(t -> {
                    String code = t.getCode();
                    if ("CE".equals(code) || "SD".equals(code)) return false;
                    return "D".equals(code) || "P".equals(code) || "C".equals(code)
                            || "KUIPER".equals(t.getBeltAffinity());
                })
                .collect(Collectors.toList());

        if (eligibleTypes.isEmpty()) {
            // Fallback: use all types
            eligibleTypes = new ArrayList<>(cachedAsteroidTypes);
        }
        if (eligibleTypes.isEmpty()) return;

        double innerSma = swarm.getInnerOrbit().getSemiMajorAxis();
        double outerSma = swarm.getOuterOrbit().getSemiMajorAxis();

        for (int i = 0; i < count; i++) {
            Asteroid asteroid = createTrojanAsteroid(
                    swarm, eligibleTypes, star, stellarMass,
                    innerSma, outerSma, i + 1, count);
            swarm.addNotableAsteroid(asteroid);
        }
    }

    /**
     * Determines how many notable asteroids a trojan swarm should generate.
     *
     * Medium swarms (≥1e-9 M⊕): 0 or 1 — coin flip weighted by planet mass.
     *   Gas/ice giants get ~70% chance of 1, rocky planets ~40%.
     *
     * Large swarms (≥1e-6 M⊕): 1 base, with a chance at 2 for massive planets.
     *   Gas giants: ~25% chance of 2. Others: 1 always.
     */
    private int determineTrojanAsteroidCount(Planet planet, double swarmMassEarth) {
        boolean isLarge = swarmMassEarth >= LARGE_SWARM_THRESHOLD;
        boolean isGiant = isGasIceGiant(planet);

        if (isLarge) {
            // Large swarms always get at least 1
            if (isGiant && RandomUtils.rollRange(0.0, 1.0) < 0.25) {
                return 2;
            }
            return 1;
        }

        // Medium swarms: 0 or 1, weighted by planet mass
        // Giants have more material and gravitational influence → more likely to have
        // a "named" body emerge from the swarm
        double chanceOfOne;
        if (isGiant) {
            chanceOfOne = 0.70;  // Gas/ice giants: 70% chance of 1 notable body
        } else {
            double planetMass = planet.getEarthMass() != null ? planet.getEarthMass() : 1.0;
            // Scale: Super-Earth (~5 M⊕) → ~50%, Terrestrial (~1 M⊕) → ~35%
            chanceOfOne = Math.min(0.55, 0.30 + planetMass * 0.04);
        }

        return RandomUtils.rollRange(0.0, 1.0) < chanceOfOne ? 1 : 0;
    }

    private Asteroid createTrojanAsteroid(OrbitalBand swarm, List<AsteroidTypeRef> eligibleTypes,
                                           Star star, double stellarMass,
                                           double innerSma, double outerSma,
                                           int rank, int totalCount) {
        Asteroid asteroid = new Asteroid();

        // Select type (weighted by abundance)
        AsteroidTypeRef type = selectAsteroidType(eligibleTypes);
        asteroid.setAsteroidType(type);

        // ── Size: largest Trojans are ~225 km (624 Hektor), most are 50-150 km ──
        double maxDiameterKm;
        if (rank == 1) {
            maxDiameterKm = RandomUtils.rollRange(100, 225);
        } else {
            maxDiameterKm = RandomUtils.rollRange(30, 120);
        }
        double diameterKm = maxDiameterKm;
        double radiusKm = diameterKm / 2.0;

        asteroid.setRadius(radiusKm);
        asteroid.setCircumference(2.0 * Math.PI * radiusKm);

        // Persist earthRadius (radius is @Transient and lost on DB reload)
        asteroid.setEarthRadius(radiusKm / EARTH_RADIUS_KM);

        // Irregular shape (Trojans are generally irregular — all < 400km)
        if (diameterKm < 400) {
            double a = diameterKm * RandomUtils.rollRange(0.9, 1.1);
            double b = diameterKm * RandomUtils.rollRange(0.7, 0.95);
            double c = diameterKm * RandomUtils.rollRange(0.5, 0.85);
            asteroid.setDimensionsKm(String.format("%.0f x %.0f x %.0f", a, b, c));
        } else {
            asteroid.setDimensionsKm(String.format("~%.0fkm spheroid", diameterKm));
        }

        // Density from type
        double density = RandomUtils.rollRange(type.getDensityMin(), type.getDensityMax());
        asteroid.setDensity(density);

        // Mass
        double volumeM3 = (4.0 / 3.0) * Math.PI * Math.pow(radiusKm * 1000, 3);
        double massKg = volumeM3 * density * 1000; // density in g/cm³ = 1000 kg/m³
        asteroid.setMass(massKg);
        asteroid.setEarthMass(massKg / EARTH_MASS_KG);

        // Albedo (Trojans are very dark)
        asteroid.setAlbedo(RandomUtils.rollRange(0.04, 0.08));

        // ── Orbital elements ──
        double sma = RandomUtils.rollRange(innerSma, outerSma);
        // Weight toward peak density (planet's SMA)
        if (swarm.getPeakDensityDistance() != null && RandomUtils.rollRange(0.0, 1.0) < 0.6) {
            double spread = (outerSma - innerSma) * 0.3;
            sma = swarm.getPeakDensityDistance() + RandomUtils.rollRange(-spread, spread);
            sma = Math.max(innerSma, Math.min(outerSma, sma));
        }

        asteroid.setSemiMajorAxisAu(sma);

        // Rotation
        asteroid.setRotationPeriodHours(RandomUtils.rollRange(4.0, 24.0));
        asteroid.setAxialTilt(RandomUtils.rollRange(0.0, 180.0));

        // Temperature
        double temp = TemperatureCalculator.calculatePlanetTemperature(
                star, sma, asteroid.getAlbedo());
        asteroid.setSurfaceTemp(temp);

        // Surface gravity (raw m/s²)
        double radiusM = radiusKm * 1000;
        double surfaceGravity = (ConversionFormulas.GRAVITATIONAL_CONSTANT * massKg) / (radiusM * radiusM);
        asteroid.setSurfaceGravity(surfaceGravity);
        asteroid.setEscapeVelocity(PhysicsFormulas.escapeVelocityKmS(massKg, radiusKm));

        // Terrain (direct fields)
        asteroid.setSurfaceFeatures(type.getTypicalSurfaceFeatures());
        asteroid.setCrateringLevel(selectCrateringLevel());
        boolean hasRegolith = diameterKm > 1.0;
        asteroid.setHasRegolith(hasRegolith);
        asteroid.setRegolithDepthM(hasRegolith
                ? RandomUtils.rollRange(0.1, Math.min(100, diameterKm * 0.1))
                : null);

        // Composition
        asteroid.setComposition(type.getPrimaryComposition());
        asteroid.setIsDifferentiated(false); // Trojans are too small

        // Ice content (trojans are organic-ice mixtures)
        asteroid.setIcePercent(RandomUtils.rollRange(10.0, 40.0));

        // Identity
        asteroid.setNotableReason(generateNotableReason(asteroid, rank, swarm));
        asteroid.setDesignationCode(generateDesignationCode(rank));
        asteroid.setAgeMY(swarm.getAgeMY());

        return asteroid;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Trojan Moon Generation (Large swarms of gas/ice giants)
    // ═════════════════════════════════════════════════════════════════════

    private void generateTrojanMoons(OrbitalBand swarm, Planet planet, Star star, double swarmMassEarth) {
        // 1–2 Trojan moons for very large swarms
        int moonCount = 1;
        if (swarmMassEarth > 1e-5 && RandomUtils.rollRange(0.0, 1.0) < 0.30) {
            moonCount = 2;
        }

        int existingMoonCount = planet.getMoons() != null ? planet.getMoons().size() : 0;

        for (int i = 0; i < moonCount; i++) {
            // Trojan moon mass: small fraction of swarm mass (like Telesto/Calypso)
            double moonMass = swarmMassEarth * RandomUtils.rollRange(0.001, 0.05);
            // Clamp to realistic Trojan moon range: ~1e-9 to ~1e-5 M⊕
            moonMass = Math.max(1e-9, Math.min(1e-5, moonMass));

            Moon trojanMoon = moonCreator.createTrojanMoon(
                    planet, star, moonMass, existingMoonCount + i + 1);
            if (trojanMoon != null) {
                planet.getMoons().add(trojanMoon);
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Helper Methods
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Determines the probability that a Large-tier trojan swarm produces a captured moon.
     * Real-world: only 4 confirmed satellites among ~15,000+ known Jupiter trojans.
     * Among the ~100 large (>50km) trojans, that's ~4% confirmed — but detection bias
     * is significant, so the true rate may be 8-15% for the largest bodies.
     *
     * Per-swarm chance is low because a planet typically gets 2 swarms (L4+L5),
     * so the per-planet rate is ~1-(1-base)^2. Target: gas giants ~12%, ice giants ~8%.
     * Gas giant: 1-(1-0.06)^2 = 11.6%  |  Ice giant: 1-(1-0.04)^2 = 7.8%
     */
    private double getTrojanMoonChance(Planet planet, double swarmMassEarth) {
        String type = planet.getPlanetType();
        double base;
        if (type != null && (type.contains("Gas Giant") || type.contains("Super-Jupiter"))) {
            base = 0.06;  // ~6% per swarm → ~11.6% per planet with 2 swarms
        } else if (type != null && type.contains("Ice Giant")) {
            base = 0.04;  // ~4% per swarm → ~7.8% per planet with 2 swarms
        } else {
            base = 0.02;  // ~2% per swarm → ~3.9% per planet with 2 swarms
        }

        // Massive swarms (≥1e-5 M⊕) get a modest boost — more bodies = more capture events
        if (swarmMassEarth >= 1e-5) {
            base *= 1.3;
        }

        return Math.min(base, 0.10); // Cap at 10% per swarm
    }

    private boolean isGasIceGiant(Planet planet) {
        String type = planet.getPlanetType();
        return type != null && (type.contains("Gas Giant") || type.contains("Ice Giant")
                || type.contains("Jupiter"));
        // Sub-Neptune excluded: 2-10 M⊕ bodies lack the mass for stable Trojan moons
    }

    private AsteroidTypeRef selectAsteroidType(List<AsteroidTypeRef> types) {
        double totalWeight = types.stream()
                .mapToDouble(t -> t.getRelativeAbundance() != null ? t.getRelativeAbundance() : 0.1)
                .sum();
        double roll = RandomUtils.rollRange(0.0, totalWeight);
        double cumulative = 0;
        for (AsteroidTypeRef type : types) {
            cumulative += type.getRelativeAbundance() != null ? type.getRelativeAbundance() : 0.1;
            if (roll <= cumulative) return type;
        }
        return types.getFirst();
    }

    private String selectCrateringLevel() {
        double roll = RandomUtils.rollRange(0.0, 1.0);
        if (roll < 0.1) return "MINIMAL";
        if (roll < 0.3) return "LIGHT";
        if (roll < 0.6) return "MODERATE";
        if (roll < 0.85) return "HEAVY";
        return "EXTREME";
    }

    private String generateNotableReason(Asteroid asteroid, int rank, OrbitalBand swarm) {
        List<String> reasons = new ArrayList<>();
        if (rank == 1) reasons.add("Largest object in the " + swarm.getLagrangePoint() + " Trojan swarm");
        if (asteroid.getRadius() > 75) reasons.add("Significant size");
        if (asteroid.getIcePercent() != null && asteroid.getIcePercent() > 30) {
            reasons.add(String.format("Ice-rich body (%.0f%% ice by mass)", asteroid.getIcePercent()));
        }
        if (reasons.isEmpty()) reasons.add("Notable Trojan asteroid");
        return String.join("; ", reasons);
    }

    private String generateDesignationCode(int rank) {
        int year = 2400 + RandomUtils.rollRange(0, 100);
        char letter1 = (char) ('A' + RandomUtils.rollRange(0, 25));
        char letter2 = (char) ('A' + RandomUtils.rollRange(0, 25));
        return String.format("%d %c%c%d", year, letter1, letter2, rank);
    }

    private String generateSwarmDescription(OrbitalBand swarm, Planet planet, String lagrangePoint) {
        String pointName = "L4".equals(lagrangePoint) ? "leading" : "trailing";
        String planetName = planet.getName() != null ? planet.getName() : "the planet";

        long count = swarm.getEstimatedObjectCount() != null ? swarm.getEstimatedObjectCount() : 0;

        return String.format(
                "Trojan swarm at the %s %s Lagrange point of %s. " +
                "Contains an estimated %,d objects with a libration amplitude of %.1f°. " +
                "Radial extent spans %.2f to %.2f AU.",
                pointName, lagrangePoint, planetName,
                count, swarm.getLibrationAmplitudeDeg(),
                swarm.getInnerOrbit().getSemiMajorAxis(),
                swarm.getOuterOrbit().getSemiMajorAxis());
    }
}
