package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ref.PlanetTypeRef;
import com.brickroad.starcreator_webservice.entity.ref.StarTypeRef;
import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.repository.StarTypeRefRepository;
import com.brickroad.starcreator_webservice.utils.planets.OrbitalStabilityAnalyzer;
import com.brickroad.starcreator_webservice.enums.BinaryConfiguration;
import com.brickroad.starcreator_webservice.repository.PlanetTypeRefRepository;
import com.brickroad.starcreator_webservice.utils.ConversionFormulas;
import com.brickroad.starcreator_webservice.utils.PhysicsFormulas;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import com.brickroad.starcreator_webservice.utils.TemperatureCalculator;
import com.brickroad.starcreator_webservice.utils.planets.PlanetaryComposition;
import com.brickroad.starcreator_webservice.utils.planets.StellarEnvironment;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlanetCreator {

    @Autowired
    private PlanetTypeRefRepository planetTypeRefRepository;

    @Autowired
    private AtmosphereCreator atmosphereCreator;

    @Autowired
    private CompositionCreator compositionCreator;

    @Autowired
    private GeologyCreator geologyCreator;

    @Autowired
    private MagneticFieldCreator magneticFieldCreator;

    @Autowired
    private MoonCreator moonCreator;

    @Autowired
    private WaterCreator waterCreator;

    @Autowired
    private HabitabilityCreator habitabilityCreator;

    @Autowired
    private ClimateCreator climateCreator;

    @Autowired
    private StarTypeRefRepository starTypeRefRepository;

    @Autowired
    private OrbitalCreator orbitalCreator;

    private List<StarTypeRef> cachedStarTypes;

    private List<PlanetTypeRef> cachedPlanetTypes;
    private static final double VARIANCE = 0.15;
    private static final double MIN_VIABLE_PLANET_TEMP_K = 10.0;

    // Physical constants — delegates to PhysicsFormulas (single source of truth)
    private static final double EARTH_MASS_KG = PhysicsFormulas.EARTH_MASS_KG;
    private static final double EARTH_RADIUS_KM = PhysicsFormulas.EARTH_RADIUS_KM;

    @PostConstruct
    public void init() {
        cachedPlanetTypes = planetTypeRefRepository.findAllPlanetTypes();
        cachedStarTypes = starTypeRefRepository.findAllStarTypes();
    }

    public Planet generateRandomPlanet() {
        PlanetTypeRef type = selectPlanetTypeByRarity();
        return generatePlanetByType(type, null, 1, 1.0);
    }

    public Planet generatePlanetByType(PlanetTypeRef type, Star parentStar,
                                       int orbitalPosition, double distanceAU) {
        return generatePlanetByType(type, parentStar, orbitalPosition, distanceAU, null);
    }

    public Planet generatePlanetByType(PlanetTypeRef type, Star parentStar,
                                       int orbitalPosition, double distanceAU,
                                       Planet previousPlanet) {
        Planet planet = new Planet();

        double earthMass = RandomUtils.rollRange(type.getMinMassEarth(), type.getMaxMassEarth());
        double earthRadius = calculateRadius(earthMass, type);
        earthRadius = addVariance(earthRadius);

        // Set mass early so populateOrbitalParameters can use it for Hill radius
        planet.setEarthMass(earthMass);

        if (parentStar != null) {
            populateOrbitalParameters(planet, parentStar, distanceAU, orbitalPosition, previousPlanet);
        } else {
            planet.setOrbit(orbitalCreator.createPlanetOrbit(distanceAU, 1.0, 0.0, 0.0));
        }

        populatePlanet(planet, type, earthMass, earthRadius, parentStar);

        return planet;
    }

    /**
     * Look up a PlanetTypeRef by name from the cached types.
     * @param name exact name match (case-insensitive), e.g. "Dwarf Planet", "Ocean World"
     * @return the matching PlanetTypeRef, or null if not found
     */
    public PlanetTypeRef findPlanetTypeByName(String name) {
        if (name == null) return null;
        return cachedPlanetTypes.stream()
                .filter(t -> t.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Create a fully populated planet of the specified type at a given location.
     * This is the reusable building block for placing specific planet types at
     * specific distances — used by belt-born dwarf planet creation, and designed
     * for future "add planet X at Y AU" features.
     *
     * @param typeName    planet type name (e.g. "Dwarf Planet", "Ocean World")
     * @param parentStar  the host star
     * @param distanceAU  semi-major axis in AU
     * @param massEarth   explicit mass in Earth masses, or null to roll from type range
     * @return fully populated Planet entity, or null if type not found
     */
    public Planet createPlanetAtLocation(String typeName, Star parentStar,
                                         double distanceAU, Double massEarth) {
        PlanetTypeRef type = findPlanetTypeByName(typeName);
        if (type == null) return null;

        Planet planet = new Planet();

        double earthMass = (massEarth != null) ? massEarth
                : RandomUtils.rollRange(type.getMinMassEarth(), type.getMaxMassEarth());
        double earthRadius = calculateRadius(earthMass, type);
        earthRadius = addVariance(earthRadius);

        planet.setEarthMass(earthMass);
        planet.setParentStar(parentStar);

        if (parentStar != null) {
            // Simplified orbital parameters — caller can override ecc/inc after creation
            double eccentricity = RandomUtils.rollRange(0.0, 0.1);
            double inclination = RandomUtils.rollRange(0.0, 10.0);
            double stellarMass = parentStar.getSolarMass() > 0 ? parentStar.getSolarMass() : 1.0;
            planet.setOrbit(orbitalCreator.createPlanetOrbit(distanceAU, stellarMass,
                    eccentricity, inclination));
        } else {
            planet.setOrbit(orbitalCreator.createPlanetOrbit(distanceAU, 1.0, 0.0, 0.0));
        }

        // Set AFTER orbit creation — setOrbit replaces the OrbitalElements object
        planet.setOrbitalPosition(-1); // belt-born: not in normal planet sequence

        populatePlanet(planet, type, earthMass, earthRadius, parentStar);

        return planet;
    }

    public List<Planet> generatePlanetarySystem(Star parentStar) {

        List<Planet> planets = new ArrayList<>();
        int numPlanets = determineNumberOfPlanets(parentStar);

        double frostLine = calculateFrostLine(parentStar);
        double maxSystemDistance = getMaxSystemDistance(parentStar);
        double starMassSolar = parentStar.getSolarMass();

        HabitableZone hz;
        double currentDistance;
        if (parentStar.getSystem().getBinaryConfiguration() == BinaryConfiguration.P_TYPE) {
            hz = new HabitableZone(parentStar.getSystem().getHabitableLow(), parentStar.getSystem().getHabitableHigh());
            double minStableDistanceAU = parentStar.getSystem().getBinarySeparationAu() * 4.0;
            currentDistance = minStableDistanceAU * RandomUtils.rollRange(1.0, 1.2);
        } else {
            hz = new HabitableZone(parentStar.getHabitableZoneInnerAU(), parentStar.getHabitableZoneOuterAU());
            double minFormation = 0.1;
            StarTypeRef starTypeRef = findStarTypeRef(parentStar);
            if (starTypeRef != null && starTypeRef.getMinPlanetFormationAu() != null) {
                minFormation = starTypeRef.getMinPlanetFormationAu();
            }

            double maxFormation = 50.0;
            if (starTypeRef != null && starTypeRef.getMaxPlanetFormationAu() != null) {
                maxFormation = starTypeRef.getMaxPlanetFormationAu();
            }

            double startCeiling = maxFormation * RandomUtils.rollRange(0.008, 0.04);
            startCeiling = Math.max(startCeiling, minFormation * 2.0);
            currentDistance = RandomUtils.rollRange(minFormation, startCeiling);
        }

        Planet previousPlanet = null;

        for (int i = 0; i < numPlanets; i++) {
            double estimatedTempK = TemperatureCalculator.calculatePlanetTemperature(parentStar, currentDistance, 0.3);
            if (estimatedTempK < MIN_VIABLE_PLANET_TEMP_K) {
                break;
            }
            PlanetTypeRef type = selectPlanetTypeByTemp(currentDistance, frostLine, hz, parentStar, estimatedTempK);

            if (type == null) {
                break;
            }

            Planet planet = generatePlanetByType(type, parentStar, i + 1, currentDistance, previousPlanet);
            planets.add(planet);
            previousPlanet = planet;

            currentDistance = calculateNextOrbitDistance(currentDistance, i, numPlanets,
                    maxSystemDistance, planet, starMassSolar);
            if (currentDistance >= maxSystemDistance * 0.95) {
                break;
            }
        }

        // ── Post-generation stability analysis ──
        analyzeSystemStability(planets, parentStar);

        return planets;
    }

    private double getMaxSystemDistance(Star parentStar) {
        double maxSystemDistance = 50.0;
        StarTypeRef starTypeRef = findStarTypeRef(parentStar);
        if (starTypeRef != null && starTypeRef.getMaxPlanetFormationAu() != null) {
            maxSystemDistance = starTypeRef.getMaxPlanetFormationAu();
        }

        if (parentStar.getSystem() != null && parentStar.getSystem().getSizeAu() != null) {
            maxSystemDistance = Math.min(maxSystemDistance, parentStar.getSystem().getSizeAu());

            BinaryConfiguration config = parentStar.getSystem().getBinaryConfiguration();
            if (config == BinaryConfiguration.S_TYPE_WIDE) {
                Double binarySep = parentStar.getSystem().getBinarySeparationAu();
                if (binarySep != null) {
                    maxSystemDistance = Math.min(maxSystemDistance, binarySep * 0.3);
                }
            }
        }
        return maxSystemDistance;
    }

    private void populatePlanet(Planet planet, PlanetTypeRef type, double earthMass, double earthRadius, Star parentStar) {

        planet.setPlanetType(type.getName());

        if (parentStar != null) {
            double starAge = parentStar.getAgeMY();

            if ("PRE_MAIN_SEQUENCE".equals(parentStar.getEvolutionaryStage())) {
                planet.setAgeMY(RandomUtils.rollRange(0.1, Math.min(10.0, starAge)));
            } else {
                double maxDelay = Math.min(100.0, starAge * 0.1);
                double formationDelay = RandomUtils.rollRange(Math.min(10.0, maxDelay), maxDelay);
                planet.setAgeMY(starAge - formationDelay);
            }
        } else {
            planet.setAgeMY(RandomUtils.rollRange(100.0, 10000));
        }

        planet.setEarthMass(earthMass);
        planet.setEarthRadius(earthRadius);

        planet.setMass(earthMass * EARTH_MASS_KG);
        planet.setRadius(earthRadius * EARTH_RADIUS_KM);
        planet.setCircumference(ConversionFormulas.radiusToCircumference(planet.getRadius()));

        planet.setDensity(calculateDensity(planet.getMass(), planet.getRadius()));
        planet.setSurfaceGravity(calculateSurfaceGravity(planet.getMass(), planet.getRadius()));
        planet.setEscapeVelocity(calculateEscapeVelocity(planet.getMass(), planet.getRadius()));

        if (planet.getOrbit() != null) {
            if (planet.getEccentricity() == null) {
                planet.getOrbit().setEccentricity(RandomUtils.rollRange(0.0, 0.1));
            }
            if (planet.getOrbitalInclinationDegrees() == null) {
                planet.getOrbit().setInclinationDegrees(RandomUtils.rollRange(0.0, 15.0));
            }
        }

        populateRotationProperties(planet, type, parentStar);

        populateAlbedo(planet, type);

        if (parentStar != null && planet.getSemiMajorAxisAU() != null) {
            planet.setSurfaceTemp(TemperatureCalculator.calculatePlanetTemperature(parentStar, planet.getSemiMajorAxisAU(), planet.getAlbedo()));
        } else {
            planet.setSurfaceTemp(RandomUtils.rollRange(100.0, 400));
        }

        populateAtmosphereProperties(planet, type);
        if (parentStar != null && planet.getSemiMajorAxisAU() != null) {
            planet.setSurfaceTemp(TemperatureCalculator.calculatePlanetTemperature(
                    parentStar, planet.getSemiMajorAxisAU(), planet.getAlbedo()));
        }
        applyGreenhouseWarming(planet);

        if (parentStar != null) {
            HabitableZone hz = new HabitableZone(parentStar.getHabitableZoneInnerAU(), parentStar.getHabitableZoneOuterAU());
            planet.setHabitableZonePosition(determineHabitableZonePosition(
                    planet.getSemiMajorAxisAU(), hz,
                    calculateFrostLine(parentStar), parentStar
            ));
        }

        planet.setCoreType(type.getTypicalCoreType());
        populateCompositionProperties(planet);
        TerrainProperties terrain = geologyCreator.createPlanetTerrain(planet);
        planet.setTerrain(terrain);

        PlanetaryMagneticField magneticField = magneticFieldCreator.generateMagneticField(planet, parentStar);
        planet.setMagneticField(magneticField);

        WaterProperties water = waterCreator.createPlanetWaterProperties(planet, parentStar);
        planet.setWater(water);

        List<Moon> moons = moonCreator.createMoons(planet, parentStar, type);
        planet.setMoons(moons);

        // Capture a deterministic seed for climate/habitability regeneration on load
        long climateSeed = System.nanoTime() ^ (planet.getOrbitalPosition() * 7919L);
        planet.setClimateSeed(climateSeed);

        RandomUtils.seed(climateSeed);
        try {
            planet.setHabitability(habitabilityCreator.assess(planet, parentStar));
        } finally {
            RandomUtils.unseed();
        }

        // Climate generation (after habitability, magnetic field, and moons are populated)
        StarSystem system = parentStar != null ? parentStar.getSystem() : null;
        RandomUtils.seed(climateSeed ^ 0xDEADBEEFL);
        try {
            planet.setClimate(climateCreator.generateClimate(planet, parentStar, system));
        } finally {
            RandomUtils.unseed();
        }

        planet.setCreatedAt(LocalDateTime.now());
        planet.setModifiedAt(LocalDateTime.now());
    }

    private void populateOrbitalParameters(Planet planet, Star star, double distanceAU,
                                           int position, Planet previousPlanet) {
        planet.setParentStar(star);
        planet.setOrbitalPosition(position);

        // --- Eccentricity: constrained by neighbor clearance ---
        double maxEcc = 0.15; // universal ceiling

        if (previousPlanet != null && previousPlanet.getSemiMajorAxisAU() != null
                && previousPlanet.getEccentricity() != null) {
            // Ensure our perihelion stays clear of previous planet's aphelion
            // plus a safety margin of 1 mutual Hill radius
            double rH = OrbitalStabilityAnalyzer.mutualHillRadius(
                    previousPlanet.getSemiMajorAxisAU(),
                    previousPlanet.getEarthMass() != null ? previousPlanet.getEarthMass() : 1.0,
                    distanceAU,
                    planet.getEarthMass() != null ? planet.getEarthMass() : 1.0,
                    star.getSolarMass());

            double neighborCeiling = OrbitalStabilityAnalyzer.maxEccentricityForClearance(
                    previousPlanet.getSemiMajorAxisAU(),
                    previousPlanet.getEccentricity(),
                    distanceAU,
                    rH); // 1 mutual Hill radius safety margin

            maxEcc = Math.min(maxEcc, neighborCeiling);
        }

        double effectiveDistance = distanceAU;
        if (star.getSolarLuminosity() > 0) {
            effectiveDistance = distanceAU / Math.sqrt(star.getSolarLuminosity());
        }
        double baseLow = 0.0;
        double baseHigh;
        if (effectiveDistance < 0.3) {
            baseHigh = 0.02;
        } else if (effectiveDistance < 1.0) {
            baseHigh = 0.05;
        } else if (effectiveDistance < 5.0) {
            baseHigh = 0.08;
        } else {
            baseHigh = 0.12;
        }

        // Cap by neighbor constraint
        baseHigh = Math.min(baseHigh, maxEcc);
        baseHigh = Math.max(baseHigh, 0.001); // never negative

        if (star.getSystem() != null
                && star.getSystem().getBinaryConfiguration() == BinaryConfiguration.P_TYPE) {
            baseHigh = Math.min(baseHigh, 0.04);
        }

        double eccentricity = RandomUtils.rollRange(baseLow, baseHigh);
        double inclination = RandomUtils.rollRange(0.0, 10.0);

        OrbitalElements orbit = orbitalCreator.createPlanetOrbit(distanceAU, star.getSolarMass(), eccentricity, inclination);
        orbit.setDistanceFromParent(distanceAU);
        orbit.setOrbitalOrder(position);
        planet.setOrbit(orbit);
    }

    private void populateRotationProperties(Planet planet, PlanetTypeRef type, Star parentStar) {
        double rotationHours;

        double tidalLockThreshold = 0.1; // default
        if (parentStar != null) {
            tidalLockThreshold = StellarEnvironment.tidalLockingThresholdAU(parentStar);
        }
        if (parentStar != null && planet.getSemiMajorAxisAU() != null &&
                planet.getSemiMajorAxisAU() < tidalLockThreshold) {
            planet.setTidallyLocked(true);
            rotationHours = planet.getOrbitalPeriodDays() * 24;
        } else {
            planet.setTidallyLocked(false);

            double baseRotation = getBaseRotation(planet, type);

            double tidalBrakingFactor = 1.0;
            if (parentStar != null && planet.getSemiMajorAxisAU() != null && planet.getAgeMY() != null) {
                double distanceFactor = Math.pow(planet.getSemiMajorAxisAU(), 2);
                double ageFactor = 1.0 + (planet.getAgeMY() / 5000.0);
                tidalBrakingFactor = Math.min(5.0, ageFactor / distanceFactor);
            }

            rotationHours = baseRotation * tidalBrakingFactor;
            rotationHours *= RandomUtils.rollRange(0.8, 1.2);

            double minRotation = getMinimumRotationPeriod(planet, type);
            rotationHours = Math.max(rotationHours, minRotation);

            rotationHours = Math.min(rotationHours, 2000.0);
        }

        planet.setRotationPeriodHours(rotationHours);

        if (type.getName().toLowerCase().contains("gas giant")) {
            planet.setAxialTilt(RandomUtils.rollRange(0, 30.0));
        } else {
            double tilt = RandomUtils.rollRange(0, 45);
            if (Math.random() < 0.05) {
                tilt = RandomUtils.rollRange(45, 170);
            }
            planet.setAxialTilt(tilt);
        }

        if (planet.getAxialTilt() > 90.0) {
            planet.setRotationPeriodHours(-Math.abs(planet.getRotationPeriodHours()));
        }
    }

    private double getMinimumRotationPeriod(Planet planet, PlanetTypeRef type) {
        String planetType = type.getName().toLowerCase();
        double earthMass = planet.getEarthMass();

        if (planetType.contains("gas giant") || planetType.contains("hot jupiter") ||
                planetType.contains("super-jupiter")) {
            return 9.0;
        }
        if (planetType.contains("ice giant") || planetType.contains("mini-neptune") ||
                planetType.contains("sub-neptune")) {
            return 10.0;
        }
        if (planetType.contains("terrestrial") || planetType.contains("super-earth") ||
                planetType.contains("ocean world") || planetType.contains("desert")) {
            if (earthMass > 2.0) {
                return 3.0;
            }
            return 4.0;
        }
        if (planetType.contains("dwarf") || earthMass < 0.3) {
            if (earthMass < 0.01) return 24.0;
            else if (earthMass < 0.05) return 12.0;
            else if (earthMass < 0.15) return 6.0;
            else return 3.0;
        }
        if (planetType.contains("ice world")) {
            return 3.0;
        }
        if (planetType.contains("lava") || planetType.contains("hot rocky")) {
            return 6.0;
        }
        return 2.0;
    }

    private static double getBaseRotation(Planet planet, PlanetTypeRef type) {
        double baseRotation;
        String typeName = type.getName().toLowerCase();

        if (typeName.contains("gas giant") || typeName.contains("ice giant")) {
            baseRotation = 8.0 + (planet.getEarthMass() / 100.0) * 5.0; // 8-13 hours typical
        } else if (typeName.contains("super-earth")) {
            baseRotation = 15.0 + (planet.getEarthMass() * 3.0); // 15-35 hours
        } else {
            baseRotation = 24.0 * Math.pow(planet.getEarthMass(), -0.25);
        }
        return baseRotation;
    }

    private void populateAtmosphereProperties(Planet planet, PlanetTypeRef type) {
        Atmosphere atmosphere = atmosphereCreator.createPlanetAtmosphere(planet, type, planet.getParentStar());
        planet.setAtmosphere(atmosphere);

        // Albedo adjustment based on atmosphere classification
        String classification = planet.getAtmosphereClassification();
        if ("NONE".equals(classification)) {
            planet.setAlbedo(type.getTypicalAlbedo());
        } else {
            double baseAlbedo = switch (classification) {
                case "EARTH_LIKE" -> 0.3;
                case "VENUS_LIKE" -> 0.75;
                case "JOVIAN", "ICE_GIANT" -> 0.5;
                default -> type.getTypicalAlbedo();
            };
            planet.setAlbedo(addVariance(baseAlbedo));
        }
    }

    private void populateAlbedo(Planet planet, PlanetTypeRef type) {
        if (!type.getCanHaveAtmosphere()) {
            planet.setAlbedo(type.getTypicalAlbedo());
            return;
        }
        double baseAlbedo = type.getTypicalAlbedo();
        planet.setAlbedo(addVariance(baseAlbedo));
    }

    private double calculateRadius(double mass, PlanetTypeRef type) {
        String typeName = type.getName().toLowerCase();
        double radius;

        if (typeName.contains("gas giant") || typeName.contains("puffy")) {
            if (mass < 100) {
                radius = Math.pow(mass / 100.0, 0.55) * type.getMinRadiusEarth();
            } else if (mass < 500) {
                radius = type.getMinRadiusEarth() + (mass - 100) / 400.0 *
                        (type.getMaxRadiusEarth() - type.getMinRadiusEarth());
            } else {
                radius = type.getMaxRadiusEarth() * (1.0 - (mass - 500) / 10000.0);
            }
        } else if (typeName.contains("ice")) {
            radius = Math.pow(mass, 0.27);
        } else if (typeName.contains("terrestrial") || typeName.contains("rocky") ||
                typeName.contains("super-earth")) {
            radius = Math.pow(mass, 0.27);
        } else {
            double minRadius = type.getMinRadiusEarth();
            double maxRadius = type.getMaxRadiusEarth();
            double massPosition = (mass - type.getMinMassEarth()) /
                    (type.getMaxMassEarth() - type.getMinMassEarth());
            radius = minRadius + Math.pow(massPosition, 0.27) * (maxRadius - minRadius);
        }

        radius = Math.max(type.getMinRadiusEarth(),
                Math.min(type.getMaxRadiusEarth(), radius));
        return addVariance(radius);
    }

    private double calculateDensity(double massKg, double radiusKm) {
        return PhysicsFormulas.density(massKg, radiusKm);
    }

    private double calculateSurfaceGravity(double massKg, double radiusKm) {
        return PhysicsFormulas.surfaceGravityG(massKg, radiusKm);
    }

    private double calculateEscapeVelocity(double massKg, double radiusKm) {
        return PhysicsFormulas.escapeVelocityKmS(massKg, radiusKm);
    }

    private double calculateFrostLine(Star star) {
        if (star.getSystem() != null &&
                star.getSystem().getBinaryConfiguration() == BinaryConfiguration.P_TYPE) {
            double totalLuminosity = star.getSystem().getStars().stream()
                    .mapToDouble(Star::getSolarLuminosity)
                    .sum();
            return 2.7 * Math.sqrt(totalLuminosity);
        }
        return 2.7 * Math.sqrt(star.getSolarLuminosity());
    }

    private int determineNumberOfPlanets(Star star) {
        double mass = star.getSolarMass();
        double age = star.getAgeMY();
        double metallicity = star.getMetallicity();
        String evoStage = star.getEvolutionaryStage();

        // === SPECIAL STAR TYPES WITH HARD CAPS ===

        // Neutron Stars: supernova destroys nearly everything
        // Only ~6 pulsar planets known total; <0.5% of pulsars host planets
        // Max 3 (PSR B1257+12 has 3), but most should have 0
        if ("NEUTRON_STAR".equals(evoStage)) {
            double roll = Math.random();
            if (roll < 0.45) return 0;       // 45% — no surviving/reformed planets
            if (roll < 0.80) return RandomUtils.rollRange(1, 2);  // 35% — 1-2 planets
            return RandomUtils.rollRange(2, 3);  // 20% — 2-3 (fallback disk formation)
        }

        // Protostars: still forming, no finished planets
        // At most protoplanetary embryos in the disk
        if ("PRE_MAIN_SEQUENCE".equals(evoStage) && age < 1.0) {
            // Protostars < 1 MY: essentially no planets yet
            double roll = Math.random();
            if (roll < 0.50) return 0;       // 50% — just a disk
            return 1;                         // 50% — one embryo forming
        }

        // T Tauri / young pre-MS: planets forming but not finished
        // ALMA shows gaps in disks suggesting forming planets at 1-10 MY
        if ("PRE_MAIN_SEQUENCE".equals(evoStage)) {
            double roll = Math.random();
            if (roll < 0.20) return 0;       // 20% — disk but no significant bodies yet
            if (roll < 0.65) return RandomUtils.rollRange(1, 2);  // 45% — 1-2 forming planets
            if (roll < 0.90) return RandomUtils.rollRange(2, 3);  // 25% — 2-3
            return RandomUtils.rollRange(3, 4);  // 10% — active formation, multiple embryos
        }

        // White Dwarfs: post-red-giant remnant, most planets consumed/ejected
        // Only outer planets survive; WD 1856+534 b is only confirmed transiting planet
        if ("WHITE_DWARF_COOLING".equals(evoStage)) {
            double roll = Math.random();
            if (roll < 0.12) return 0;       // 12% — all planets lost
            if (roll < 0.70) return RandomUtils.rollRange(1, 2);  // 58% — 1-2 survivors
            if (roll < 0.92) return RandomUtils.rollRange(2, 3);  // 22% — 2-3
            return RandomUtils.rollRange(3, 4);  // 8% — lucky system, max 4
        }

        // Brown Dwarfs: tiny disks, limited material for planet formation
        // Y-dwarfs especially: disk mass ~0.0001-0.0004 M☉
        if ("BROWN_DWARF_COOLING".equals(evoStage)) {
            if (mass < 0.02) {
                // Y-dwarfs: smallest disks
                double roll = Math.random();
                if (roll < 0.15) return 0;
                if (roll < 0.65) return RandomUtils.rollRange(1, 2);  // 50%
                if (roll < 0.92) return RandomUtils.rollRange(2, 3);  // 27%
                return 4;                     // 8% — max for Y-dwarf
            } else if (mass < 0.05) {
                // T-dwarfs: small disks
                double roll = Math.random();
                if (roll < 0.08) return 0;
                if (roll < 0.45) return RandomUtils.rollRange(1, 2);
                if (roll < 0.80) return RandomUtils.rollRange(3, 4);
                return RandomUtils.rollRange(4, 5);  // 20% — max 5
            } else {
                // L-dwarfs: modest disks, can form small systems
                double roll = Math.random();
                if (roll < 0.05) return 0;
                if (roll < 0.35) return RandomUtils.rollRange(1, 2);
                if (roll < 0.70) return RandomUtils.rollRange(3, 4);
                if (roll < 0.90) return RandomUtils.rollRange(4, 5);
                return RandomUtils.rollRange(5, 6);  // 10% — max 6
            }
        }

        // === STANDARD MAIN SEQUENCE LOGIC (unchanged) ===
        int basePlanets;
        if (mass > 2.0) {
            basePlanets = RandomUtils.rollRange(1, 4);
        } else if (mass > 1.0) {
            basePlanets = RandomUtils.rollRange(2, 8);
        } else {
            basePlanets = RandomUtils.rollRange(3, 10);
        }

        double metallicityFactor = getMetallicityFactor(metallicity);
        basePlanets = (int) Math.round(basePlanets * metallicityFactor);
        basePlanets = Math.max(1, basePlanets);

        if (age > 8000) {
            basePlanets = Math.max(1, basePlanets - RandomUtils.rollRange(0, 2));
        }

        // Evolutionary stage: evolved stars may have consumed or ejected planets
        double evoFactor = StellarEnvironment.evolutionaryPlanetFactor(star);
        basePlanets = (int) Math.round(basePlanets * evoFactor);
        basePlanets = Math.max(1, basePlanets);

        return basePlanets;
    }

    private static double getMetallicityFactor(double metallicity) {
        double metallicityFactor;
        if (metallicity < -1.0) {
            metallicityFactor = 0.3;
        } else if (metallicity < -0.5) {
            metallicityFactor = 0.5;
        } else if (metallicity < 0.0) {
            metallicityFactor = 0.75;
        } else if (metallicity < 0.3) {
            metallicityFactor = 1.0;
        } else {
            metallicityFactor = 1.3;
        }
        return metallicityFactor;
    }

    private PlanetTypeRef selectPlanetTypeByTemp(double distanceAU, double frostLine, HabitableZone hz, Star star, double estimatedTempK) {
        String zone = determineHabitableZonePosition(distanceAU, hz, frostLine, star);

        List<PlanetTypeRef> tempFilteredTypes = cachedPlanetTypes.stream()
                .filter(type -> {
                    if (type.getMinFormationTempK() == null ||
                            type.getMaxFormationTempK() == null) {
                        return true;
                    }
                    return estimatedTempK >= type.getMinFormationTempK() &&
                            estimatedTempK <= type.getMaxFormationTempK();
                })
                .collect(Collectors.toList());

        List<PlanetTypeRef> zoneFilteredTypes = tempFilteredTypes.stream()
                .filter(type -> type.getFormationZone() == null ||
                        type.getFormationZone().equals(zone))
                .toList();

        if (!zoneFilteredTypes.isEmpty()) {
            return selectFromList(zoneFilteredTypes);
        }
        // Zone fallback: only allow zone-agnostic types (formation_zone=NULL),
        // not types explicitly assigned to a different zone
        List<PlanetTypeRef> zoneAgnosticTypes = tempFilteredTypes.stream()
                .filter(type -> type.getFormationZone() == null)
                .collect(Collectors.toList());
        if (!zoneAgnosticTypes.isEmpty()) {
            return selectFromList(zoneAgnosticTypes);
        }
        // Last resort: full temp list to avoid returning null
        if (!tempFilteredTypes.isEmpty()) {
            return selectFromList(tempFilteredTypes);
        }
        return null;
    }

    private double calculateNextOrbitDistance(double currentDistance, int planetIndex,
                                              int totalPlanets, double maxSystemDistance,
                                              Planet currentPlanet, double starMassSolar) {

        double remainingSpace = maxSystemDistance - currentDistance;
        int remainingPlanets = totalPlanets - planetIndex - 1;

        if (remainingPlanets <= 0) {
            return currentDistance * 1.5;
        }

        double targetSpacing = Math.pow(remainingSpace / currentDistance, 1.0 / (remainingPlanets + 1));

        // Wider spacing allowed in outer system (Titius-Bode pattern)
        // Inner system: 1.3-2.0x, Outer system: 1.5-3.0x
        double progressFraction = currentDistance / maxSystemDistance;
        double minSpacing = 1.3 + (progressFraction * 0.2);  // 1.3 → 1.5
        double maxSpacing = 2.0 + progressFraction;  // 2.0 → 3.0

        targetSpacing = Math.max(minSpacing, Math.min(maxSpacing, targetSpacing));
        double spacing = targetSpacing * RandomUtils.rollRange(0.85, 1.15);

        double nextDistance = currentDistance * spacing;

        // Use current planet's mass + estimate next planet as ~Earth mass (conservative)
        double currentMass = (currentPlanet != null && currentPlanet.getEarthMass() != null)
                ? currentPlanet.getEarthMass() : 1.0;
        // assume similar mass as conservative estimate
        double minSafeGap = OrbitalStabilityAnalyzer.minimumSafeSpacingAU(
                currentDistance, currentMass, currentMass, starMassSolar);

        // Also ensure aphelion clearance: account for current planet's eccentricity
        double currentEcc = (currentPlanet != null && currentPlanet.getEccentricity() != null)
                ? currentPlanet.getEccentricity() : 0.05;
        double aphelionCurrent = currentDistance * (1.0 + currentEcc);
        double minFromAphelion = aphelionCurrent + minSafeGap;

        double minDistance = Math.max(currentDistance + minSafeGap, minFromAphelion);
        if (currentPlanet != null && currentPlanet.getParentStar() != null
                && currentPlanet.getParentStar().getSystem() != null
                && currentPlanet.getParentStar().getSystem().getBinaryConfiguration()
                == BinaryConfiguration.P_TYPE) {
            minDistance *= 1.5;
        }
        nextDistance = Math.max(nextDistance, minDistance);

        if (minDistance > maxSystemDistance * 0.90) {
            return maxSystemDistance; // triggers break in generatePlanetarySystem
        }
        return nextDistance;
    }

    private String determineHabitableZonePosition(double distanceAU, HabitableZone hz, double frostLine, Star star) {
        HabitableZone effectiveHZ = hz;
        if (star.getSystem() != null &&
                star.getSystem().getBinaryConfiguration() == BinaryConfiguration.P_TYPE) {
            effectiveHZ = new HabitableZone(
                    star.getSystem().getHabitableLow(),
                    star.getSystem().getHabitableHigh()
            );
        }

        String zone;
        if (distanceAU < effectiveHZ.innerEdge) {
            zone = "inner";
        } else if (distanceAU >= effectiveHZ.innerEdge && distanceAU <= effectiveHZ.outerEdge) {
            zone = "habitable";
        } else if (distanceAU > frostLine) {
            zone = "outer";
        } else {
            zone = "frost_line";
        }
        return zone;
    }

    private PlanetTypeRef selectPlanetTypeByRarity() {
        return selectFromList(cachedPlanetTypes);
    }

    private PlanetTypeRef selectFromList(List<PlanetTypeRef> types) {
        int totalWeight = types.stream()
                .mapToInt(PlanetTypeRef::getRarityWeight)
                .sum();

        int random = RandomUtils.rollRange(0, totalWeight);
        int currentWeight = 0;

        for (PlanetTypeRef type : types) {
            currentWeight += type.getRarityWeight();
            if (random < currentWeight) {
                return type;
            }
        }

        return types.getFirst();
    }

    private double addVariance(double value) {
        double factor = 1.0 + ((Math.random() - 0.5) * 2 * VARIANCE);
        return value * factor;
    }

    private void populateCompositionProperties(Planet planet) {
        PlanetaryComposition composition = compositionCreator.generateComposition(
                planet.getPlanetType(),
                planet.getSurfaceTemp(),
                planet.getCoreType()
        );
        planet.setInteriorComposition(composition.toInteriorString());
        planet.setEnvelopeComposition(composition.toEnvelopeString());
        planet.setCompositionClassification(composition.getClassification().name());
    }

    private void applyGreenhouseWarming(Planet planet) {
        if (planet.getSurfaceTemp() == null || planet.getAtmosphereClassification() == null
                || "NONE".equals(planet.getAtmosphereClassification())) {
            return;
        }

        String atmClass = planet.getAtmosphereClassification();
        if ("JOVIAN".equals(atmClass) || "ICE_GIANT".equals(atmClass)) {
            return;
        }

        double pressure = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 0;
        String composition = planet.getAtmosphereComposition() != null ? planet.getAtmosphereComposition() : "";
        double greenhouse = TemperatureCalculator.estimateGreenhouseWarming(
                planet.getAtmosphereClassification(), pressure, composition);
        if (greenhouse > 0) {
            planet.setSurfaceTemp(planet.getSurfaceTemp() + greenhouse);
        }
    }

    private void analyzeSystemStability(List<Planet> planets, Star parentStar) {
        if (planets.size() < 2 || parentStar == null) {
            // Single planet or orphan — always stable
            if (planets.size() == 1) {
                orbitalCreator.setStability(planets.getFirst().getOrbit(), "STABLE", null, null);
            }
            return;
        }

        double starMassSolar = parentStar.getSolarMass();
        double systemAgeMy = parentStar.getAgeMY();

        // For each planet, track the worst stability result from either neighbor
        for (int i = 0; i < planets.size(); i++) {
            Planet planet = planets.get(i);
            String worstClassification = "STABLE";
            Double worstTimescale = null;
            String worstNeighbor = null;

            // Check inner neighbor
            if (i > 0) {
                Planet inner = planets.get(i - 1);
                OrbitalStabilityAnalyzer.StabilityResult sr = OrbitalStabilityAnalyzer.analyzeStability(
                        safe(inner.getSemiMajorAxisAU(), 1.0), safe(inner.getEccentricity(), 0.0),
                        safe(inner.getEarthMass(), 1.0),
                        safe(planet.getSemiMajorAxisAU(), 1.0), safe(planet.getEccentricity(), 0.0),
                        safe(planet.getEarthMass(), 1.0),
                        starMassSolar, systemAgeMy);

                if (isWorse(sr.classification, worstClassification)) {
                    worstClassification = sr.classification;
                    worstTimescale = Double.isInfinite(sr.timescaleMy) ? null : sr.timescaleMy;
                    worstNeighbor = inner.getName();
                }
            }

            // Check outer neighbor
            if (i < planets.size() - 1) {
                Planet outer = planets.get(i + 1);
                OrbitalStabilityAnalyzer.StabilityResult sr = OrbitalStabilityAnalyzer.analyzeStability(
                        safe(planet.getSemiMajorAxisAU(), 1.0), safe(planet.getEccentricity(), 0.0),
                        safe(planet.getEarthMass(), 1.0),
                        safe(outer.getSemiMajorAxisAU(), 1.0), safe(outer.getEccentricity(), 0.0),
                        safe(outer.getEarthMass(), 1.0),
                        starMassSolar, systemAgeMy);

                if (isWorse(sr.classification, worstClassification)) {
                    worstClassification = sr.classification;
                    worstTimescale = Double.isInfinite(sr.timescaleMy) ? null : sr.timescaleMy;
                    worstNeighbor = outer.getName();
                }
            }

            orbitalCreator.setStability(planet.getOrbit(), worstClassification, worstTimescale,
                    "STABLE".equals(worstClassification) ? null : worstNeighbor);
        }
    }

    private static int stabilitySeverity(String classification) {
        return switch (classification) {
            case "UNSTABLE" -> 3;
            case "CROSSING" -> 2;
            case "MARGINAL" -> 1;
            default -> 0; // STABLE
        };
    }

    private static boolean isWorse(String candidate, String current) {
        return stabilitySeverity(candidate) > stabilitySeverity(current);
    }

    private static double safe(Double value, double fallback) {
        return value != null ? value : fallback;
    }

    private static class HabitableZone {
        double innerEdge;
        double outerEdge;

        HabitableZone(double inner, double outer) {
            this.innerEdge = inner;
            this.outerEdge = outer;
        }
    }

    private StarTypeRef findStarTypeRef(Star star) {
        return cachedStarTypes.stream()
                .filter(st -> st.getName().equals(star.getType()))
                .findFirst()
                .orElse(null);
    }

}
