package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ref.AsteroidTypeRef;
import com.brickroad.starcreator_webservice.entity.ref.BeltTypeRef;
import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.enums.BandCategory;
import com.brickroad.starcreator_webservice.enums.BinaryConfiguration;
import com.brickroad.starcreator_webservice.enums.DistanceUnit;
import com.brickroad.starcreator_webservice.repository.AsteroidTypeRefRepository;
import com.brickroad.starcreator_webservice.repository.BeltTypeRefRepository;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import com.brickroad.starcreator_webservice.utils.TemperatureCalculator;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BeltCreator {

    @Autowired
    private BeltTypeRefRepository beltTypeRefRepository;

    @Autowired
    private AsteroidTypeRefRepository asteroidTypeRefRepository;

    @Autowired
    private OrbitalCreator orbitalCreator;

    private List<BeltTypeRef> cachedBeltTypes;
    private List<AsteroidTypeRef> cachedAsteroidTypes;

    private static final double EARTH_MASS_KG = 5.972e24;
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double GRAVITATIONAL_CONSTANT = 6.674e-11;
    private static final double AU_IN_KM = 1.496e8;

    // Giant planet threshold in Earth masses
    private static final double GIANT_PLANET_MASS_THRESHOLD = 15.0;

    // Minimum gap ratio for inner belt formation
    private static final double MIN_GAP_RATIO = 1.5;

    @PostConstruct
    public void init() {
        cachedBeltTypes = beltTypeRefRepository.findAllBeltTypes();
        cachedAsteroidTypes = asteroidTypeRefRepository.findAllAsteroidTypes();
    }

    public List<OrbitalBand> createBelts(StarSystem system, Star primaryStar) {
        List<OrbitalBand> belts = new ArrayList<>();

        List<Planet> planets = system.getPlanets().stream()
                .map(b -> (Planet) b)
                .sorted(Comparator.comparingDouble(p -> p.getSemiMajorAxisAU() != null ? p.getSemiMajorAxisAU() : 0.0))
                .collect(Collectors.toList());

        if (planets.isEmpty()) {
            return belts;
        }

        SystemArchitecture arch = analyzeSystem(planets, primaryStar);

        OrbitalBand innerBelt = tryCreateInnerRockyBelt(system, arch, primaryStar);
        if (innerBelt != null) {
            belts.add(innerBelt);
        }

        // Try to create Kuiper belt
        OrbitalBand kuiperBelt = tryCreateKuiperBelt(system, arch, primaryStar);
        if (kuiperBelt != null) {
            belts.add(kuiperBelt);

            // Try to create scattered disk (depends on Kuiper belt existing)
            OrbitalBand scatteredDisk = tryCreateScatteredDisk(system, arch, kuiperBelt, primaryStar);
            if (scatteredDisk != null) {
                belts.add(scatteredDisk);
            }
        }
        return belts;
    }

    private SystemArchitecture analyzeSystem(List<Planet> planets, Star primaryStar) {
        SystemArchitecture arch = new SystemArchitecture();

        double luminosity = primaryStar.getSolarLuminosity();
        double totalStellarMass = primaryStar.getSolarMass();

        StarSystem system = primaryStar.getSystem();
        if (system != null && system.getBinaryConfiguration() != null) {
            BinaryConfiguration config = system.getBinaryConfiguration();
            if (config == BinaryConfiguration.P_TYPE ||
                    config == BinaryConfiguration.HIERARCHICAL_BINARY_THIRD ||
                    config == BinaryConfiguration.HIERARCHICAL_TRIPLE) {
                luminosity = system.getStars().stream()
                        .mapToDouble(Star::getSolarLuminosity)
                        .sum();
                totalStellarMass = system.getStars().stream()
                        .mapToDouble(Star::getSolarMass)
                        .sum();
            }
        }

        arch.frostLineAu = 4.85 * Math.sqrt(luminosity);
        arch.totalStellarMass = totalStellarMass;

        // Get system age
        arch.systemAgeMY = primaryStar.getAgeMY() != null ? primaryStar.getAgeMY() : 4600.0;

        // Get metallicity (average for binaries, but primary is fine)
        arch.starMetallicity = primaryStar.getMetallicity() != null ? primaryStar.getMetallicity() : 0.0;

        // Categorize planets
        for (Planet planet : planets) {
            if (planet.getSemiMajorAxisAU() == null) continue;

            double mass = planet.getEarthMass() != null ? planet.getEarthMass() : 1.0;
            String type = planet.getPlanetType() != null ? planet.getPlanetType().toLowerCase() : "";

            boolean isGiant = mass > GIANT_PLANET_MASS_THRESHOLD ||
                    type.contains("gas") ||
                    type.contains("ice giant") ||
                    type.contains("neptune") ||
                    type.contains("jupiter");

            boolean isDwarf = type.contains("dwarf");

            if (isDwarf) {
                arch.dwarfPlanets.add(planet);
            } else if (isGiant) {
                arch.giantPlanets.add(planet);
            } else {
                arch.rockyPlanets.add(planet);
            }
        }

        // Find key positions
        if (!arch.rockyPlanets.isEmpty()) {
            arch.outermostRockyAu = arch.rockyPlanets.stream()
                    .mapToDouble(Planet::getSemiMajorAxisAU)
                    .max()
                    .orElse(0.0);
        }

        // Track outermost non-dwarf planet (any giant or rocky)
        double maxNonDwarf = 0;
        for (Planet p : arch.giantPlanets) {
            if (p.getSemiMajorAxisAU() != null && p.getSemiMajorAxisAU() > maxNonDwarf) {
                maxNonDwarf = p.getSemiMajorAxisAU();
            }
        }
        for (Planet p : arch.rockyPlanets) {
            if (p.getSemiMajorAxisAU() != null && p.getSemiMajorAxisAU() > maxNonDwarf) {
                maxNonDwarf = p.getSemiMajorAxisAU();
            }
        }
        arch.outermostNonDwarfAu = maxNonDwarf;

        if (!arch.giantPlanets.isEmpty()) {
            arch.innermostGiantAu = arch.giantPlanets.stream()
                    .mapToDouble(Planet::getSemiMajorAxisAU)
                    .min()
                    .orElse(0.0);

            arch.outermostGiantAu = arch.giantPlanets.stream()
                    .mapToDouble(Planet::getSemiMajorAxisAU)
                    .max()
                    .orElse(0.0);

            arch.largestGiantMass = arch.giantPlanets.stream()
                    .mapToDouble(p -> p.getEarthMass() != null ? p.getEarthMass() : 0.0)
                    .max()
                    .orElse(0.0);
        }

        return arch;
    }

    private OrbitalBand tryCreateInnerRockyBelt(StarSystem system, SystemArchitecture arch, Star primaryStar) {
        if (arch.giantPlanets.isEmpty()) {
            return null;
        }

        if (arch.outermostRockyAu <= 0 || arch.innermostGiantAu <= 0) {
            if (arch.innermostGiantAu > 1.0) {
                arch.outermostRockyAu = arch.innermostGiantAu * 0.3;
            } else {
                return null;
            }
        }

        double gapRatio = arch.innermostGiantAu / arch.outermostRockyAu;
        if (gapRatio < MIN_GAP_RATIO) {
            return null;
        }

        BeltTypeRef beltType = findBeltTypeByCode("INNER_ROCKY");
        if (beltType == null) {
            return null;
        }

        if (arch.systemAgeMY < beltType.getMinSystemAgeMy()) {
            return null;
        }

        OrbitalBand belt = new OrbitalBand();
        belt.setStarSystem(system);
        belt.setBeltType(beltType);
        belt.setBandCategory(BandCategory.BELT);
        belt.setBandType(beltType.getCode());
        belt.setAgeMY(arch.systemAgeMY);
        belt.setCompositionType(beltType.getTypicalCompositionType());

        double innerEdge = arch.outermostRockyAu * 1.3;
        double outerEdge = arch.innermostGiantAu * 0.6;

        if (outerEdge - innerEdge < 0.3) {
            outerEdge = innerEdge + 0.5;
        }
        if (outerEdge > arch.innermostGiantAu * 0.8) {
            outerEdge = arch.innermostGiantAu * 0.8;
        }

        double baseMass = RandomUtils.rollRange(
                beltType.getTypicalMassEarthMassesMin(),
                beltType.getTypicalMassEarthMassesMax()
        );

        double metallicityFactor = 1.0 + (arch.starMetallicity * 2.0);
        metallicityFactor = Math.max(0.5, Math.min(2.0, metallicityFactor));

        double ageFactor = 1.0 - (arch.systemAgeMY / 10000.0) * 0.3;
        ageFactor = Math.max(0.5, ageFactor);

        double giantFactor = 1.0 - (arch.largestGiantMass / 1000.0) * 0.2;
        giantFactor = Math.max(0.5, giantFactor);

        double totalMassEarth = baseMass * metallicityFactor * ageFactor * giantFactor;
        belt.setTotalMassEarthMasses(totalMassEarth);
        belt.setTotalMassKg(totalMassEarth * EARTH_MASS_KG);

        belt.setEstimatedObjectCount((long) (totalMassEarth * 1e12 * RandomUtils.rollRange(0.5, 2.0)));

        // Compute eccentricity and inclination BEFORE creating orbital elements
        belt.setAverageEccentricity(RandomUtils.rollRange(
                beltType.getMinEccentricity(),
                (beltType.getMinEccentricity() + beltType.getMaxEccentricity()) / 2.0
        ));
        belt.setMaxEccentricity(RandomUtils.rollRange(
                belt.getAverageEccentricity(),
                beltType.getMaxEccentricity()
        ));
        belt.setAverageInclinationDeg(RandomUtils.rollRange(
                beltType.getMinInclinationDegrees(),
                (beltType.getMinInclinationDegrees() + beltType.getMaxInclinationDegrees()) / 2.0
        ));
        belt.setMaxInclinationDeg(RandomUtils.rollRange(
                belt.getAverageInclinationDeg(),
                beltType.getMaxInclinationDegrees()
        ));

        // Create orbital elements for inner and outer edges
        double avgEcc = belt.getAverageEccentricity() != null ? belt.getAverageEccentricity() : 0.05;
        double avgInc = belt.getAverageInclinationDeg() != null ? belt.getAverageInclinationDeg() : 5.0;
        belt.setInnerOrbit(orbitalCreator.createBandEdgeOrbit(innerEdge, DistanceUnit.AU, arch.totalStellarMass, avgEcc, avgInc, "Inner rocky belt inner edge"));
        belt.setOuterOrbit(orbitalCreator.createBandEdgeOrbit(outerEdge, DistanceUnit.AU, arch.totalStellarMass, avgEcc, avgInc, "Inner rocky belt outer edge"));
        belt.setPeakDensityDistance((innerEdge + outerEdge) / 2.0);

        generateBeltComposition(belt, arch.frostLineAu);

        generateResonanceGaps(belt, arch.giantPlanets.getFirst());

        if (RandomUtils.rollRange(0.0, 1.0) < 0.6) {
            belt.setHasCollisionalFamilies(true);
            belt.setFamilyCount(RandomUtils.rollRange(3, 12));
        } else {
            belt.setHasCollisionalFamilies(false);
        }

        belt.setDescription(generateBeltDescription(belt, beltType));

        generateNotableAsteroids(belt, beltType, primaryStar, arch);

        return belt;
    }

    private OrbitalBand tryCreateKuiperBelt(StarSystem system, SystemArchitecture arch, Star primaryStar) {
        // Requires giant planet
        if (arch.giantPlanets.isEmpty()) {
            return null;
        }

        // Need outer giant beyond frost line
        if (arch.outermostGiantAu < arch.frostLineAu) {
            return null;
        }

        BeltTypeRef beltType = findBeltTypeByCode("KUIPER");
        if (beltType == null) {
            return null;
        }

        // Check system age
        if (arch.systemAgeMY < beltType.getMinSystemAgeMy()) {
            return null;
        }

        OrbitalBand belt = new OrbitalBand();
        belt.setStarSystem(system);
        belt.setBeltType(beltType);
        belt.setBandCategory(BandCategory.BELT);
        belt.setBandType(beltType.getCode());
        belt.setAgeMY(arch.systemAgeMY);
        belt.setCompositionType(beltType.getTypicalCompositionType());

        // Calculate bounds (3:2 resonance with outermost giant)
        double innerEdge = arch.outermostGiantAu * 1.5;
        // But push outward if non-dwarf planets exist beyond the outermost giant
        if (arch.outermostNonDwarfAu > arch.outermostGiantAu) {
            double minInnerEdge = arch.outermostNonDwarfAu * 1.3;
            innerEdge = Math.max(innerEdge, minInnerEdge);
        }
        double outerEdge = innerEdge * RandomUtils.rollRange(1.8, 2.5);

        // Calculate mass (Kuiper belt is more massive than asteroid belt)
        double baseMass = RandomUtils.rollRange(
                beltType.getTypicalMassEarthMassesMin(),
                beltType.getTypicalMassEarthMassesMax()
        );

        double metallicityFactor = 1.0 + (arch.starMetallicity * 1.5);
        metallicityFactor = Math.max(0.5, Math.min(2.0, metallicityFactor));

        double totalMassEarth = baseMass * metallicityFactor;
        belt.setTotalMassEarthMasses(totalMassEarth);
        belt.setTotalMassKg(totalMassEarth * EARTH_MASS_KG);

        belt.setEstimatedObjectCount((long) (totalMassEarth * 1e10 * RandomUtils.rollRange(0.5, 2.0)));

        // Compute eccentricity and inclination BEFORE creating orbital elements
        belt.setAverageEccentricity(RandomUtils.rollRange(
                beltType.getMinEccentricity(),
                (beltType.getMinEccentricity() + beltType.getMaxEccentricity()) / 2.0
        ));
        belt.setMaxEccentricity(RandomUtils.rollRange(
                belt.getAverageEccentricity(),
                beltType.getMaxEccentricity()
        ));
        belt.setAverageInclinationDeg(RandomUtils.rollRange(
                beltType.getMinInclinationDegrees(),
                10.0
        ));
        belt.setMaxInclinationDeg(RandomUtils.rollRange(
                belt.getAverageInclinationDeg(),
                beltType.getMaxInclinationDegrees()
        ));

        // Create orbital elements for inner and outer edges
        double avgEcc = belt.getAverageEccentricity() != null ? belt.getAverageEccentricity() : 0.05;
        double avgInc = belt.getAverageInclinationDeg() != null ? belt.getAverageInclinationDeg() : 5.0;
        belt.setInnerOrbit(orbitalCreator.createBandEdgeOrbit(innerEdge, DistanceUnit.AU, arch.totalStellarMass, avgEcc, avgInc, "Kuiper belt inner edge"));
        belt.setOuterOrbit(orbitalCreator.createBandEdgeOrbit(outerEdge, DistanceUnit.AU, arch.totalStellarMass, avgEcc, avgInc, "Kuiper belt outer edge"));
        belt.setPeakDensityDistance(innerEdge + (outerEdge - innerEdge) * 0.3);

        // Icy composition
        belt.setPrimaryComposition("Water Ice 50%, Methane Ice 15%, Ammonia Ice 10%, Rock 20%, Organics 5%");

        // No Kirkwood-style gaps, but resonance structures
        belt.setHasResonanceGaps(false);
        belt.setHasCollisionalFamilies(RandomUtils.rollRange(0.0, 1.0) < 0.3);
        if (Boolean.TRUE.equals(belt.getHasCollisionalFamilies())) {
            belt.setFamilyCount(RandomUtils.rollRange(1, 5));
        }

        belt.setDescription(generateBeltDescription(belt, beltType));

        // Link any dwarf planets that fall within the belt
        linkDwarfPlanets(belt, arch.dwarfPlanets);

        // Generate notable KBOs (fewer if we already have dwarf planets)
        int dwarfCount = belt.getDwarfPlanets().size();
        generateNotableAsteroids(belt, beltType, primaryStar, arch, dwarfCount);

        return belt;
    }

    private OrbitalBand tryCreateScatteredDisk(StarSystem system, SystemArchitecture arch, OrbitalBand kuiperBelt, Star primaryStar) {
        BeltTypeRef beltType = findBeltTypeByCode("SCATTERED_DISK");
        if (beltType == null) {
            return null;
        }

        // Check system age (needs time for scattering)
        if (arch.systemAgeMY < beltType.getMinSystemAgeMy()) {
            return null;
        }

        // Only ~30% chance of significant scattered disk
        if (RandomUtils.rollRange(0.0, 1.0) > 0.30) {
            return null;
        }

        OrbitalBand belt = new OrbitalBand();
        belt.setStarSystem(system);
        belt.setBeltType(beltType);
        belt.setBandCategory(BandCategory.BELT);
        belt.setBandType(beltType.getCode());
        belt.setAgeMY(arch.systemAgeMY);
        belt.setCompositionType(beltType.getTypicalCompositionType());

        // Starts where Kuiper belt ends
        double innerEdge = kuiperBelt.getOuterOrbit().getSemiMajorAxis();
        double outerEdge = innerEdge * RandomUtils.rollRange(3.0, 5.0);

        // Mass (less than Kuiper)
        double baseMass = RandomUtils.rollRange(
                beltType.getTypicalMassEarthMassesMin(),
                beltType.getTypicalMassEarthMassesMax()
        );
        belt.setTotalMassEarthMasses(baseMass);
        belt.setTotalMassKg(baseMass * EARTH_MASS_KG);

        belt.setEstimatedObjectCount((long) (baseMass * 1e9 * RandomUtils.rollRange(0.5, 2.0)));

        // Compute eccentricity and inclination BEFORE creating orbital elements
        belt.setAverageEccentricity(RandomUtils.rollRange(0.3, 0.5));
        belt.setMaxEccentricity(RandomUtils.rollRange(0.5, 0.85));
        belt.setAverageInclinationDeg(RandomUtils.rollRange(15.0, 30.0));
        belt.setMaxInclinationDeg(RandomUtils.rollRange(30.0, 60.0));

        // Create orbital elements for inner and outer edges
        double avgEcc = belt.getAverageEccentricity() != null ? belt.getAverageEccentricity() : 0.05;
        double avgInc = belt.getAverageInclinationDeg() != null ? belt.getAverageInclinationDeg() : 5.0;
        belt.setInnerOrbit(orbitalCreator.createBandEdgeOrbit(innerEdge, DistanceUnit.AU, arch.totalStellarMass, avgEcc, avgInc, "Scattered disk inner edge"));
        belt.setOuterOrbit(orbitalCreator.createBandEdgeOrbit(outerEdge, DistanceUnit.AU, arch.totalStellarMass, avgEcc, avgInc, "Scattered disk outer edge"));
        belt.setPeakDensityDistance(innerEdge * 1.5);

        belt.setPrimaryComposition("Water Ice 55%, Methane Ice 10%, Ammonia Ice 10%, Rock 20%, Organics 5%");
        belt.setHasResonanceGaps(false);
        belt.setHasCollisionalFamilies(false);

        belt.setDescription(generateBeltDescription(belt, beltType));

        // Generate 0-2 notable objects
        generateNotableAsteroids(belt, beltType, primaryStar, arch);

        return belt;
    }

    private void linkDwarfPlanets(OrbitalBand belt, List<Planet> dwarfPlanets) {
        double innerBound = belt.getInnerOrbit().getSemiMajorAxis() * 0.8; // Allow some margin
        double outerBound = belt.getOuterOrbit().getSemiMajorAxis() * 1.5;

        Planet largestInBelt = null;
        double largestMass = 0;

        for (Planet dwarf : dwarfPlanets) {
            if (dwarf.getSemiMajorAxisAU() == null) continue;

            double sma = dwarf.getSemiMajorAxisAU();
            if (sma >= innerBound && sma <= outerBound) {
                belt.addDwarfPlanet(dwarf);

                double mass = dwarf.getEarthMass() != null ? dwarf.getEarthMass() : 0;
                if (mass > largestMass) {
                    largestMass = mass;
                    largestInBelt = dwarf;
                }
            }
        }

        // Note: We can't easily mark "is_largest_in_belt" without a custom entity
        // for the junction table. For now, the largest is implicitly the first
        // when ordered by mass. This could be enhanced later.
    }

    private void generateNotableAsteroids(OrbitalBand belt, BeltTypeRef beltType, Star primaryStar, SystemArchitecture arch) {
        generateNotableAsteroids(belt, beltType, primaryStar, arch, 0);
    }

    private void generateNotableAsteroids(OrbitalBand belt, BeltTypeRef beltType, Star primaryStar, SystemArchitecture arch, int existingNotableCount) {
        if (RandomUtils.rollRange(0.0, 1.0) > beltType.getNotableObjectChance()) {
            return;
        }

        int maxObjects = beltType.getMaxNotableObjects() != null ? beltType.getMaxNotableObjects() : 5;
        maxObjects = Math.max(0, maxObjects - existingNotableCount);

        if (maxObjects == 0) {
            return;
        }

        int count = RandomUtils.rollRange(1, maxObjects);

        List<AsteroidTypeRef> eligibleTypes = getEligibleAsteroidTypes(beltType.getCode());
        if (eligibleTypes.isEmpty()) {
            return;
        }

        double[] sizeDistribution = generateSizeDistribution(count);

        for (int i = 0; i < count; i++) {
            Asteroid asteroid = createNotableAsteroid(
                    belt, eligibleTypes, primaryStar, arch,
                    sizeDistribution[i], i + 1
            );
            belt.addNotableAsteroid(asteroid);
        }
    }

    private Asteroid createNotableAsteroid(OrbitalBand belt, List<AsteroidTypeRef> eligibleTypes,
                                           Star primaryStar, SystemArchitecture arch,
                                           double sizeFraction, int rank) {
        Asteroid asteroid = new Asteroid();

        AsteroidTypeRef type = selectAsteroidType(eligibleTypes);
        asteroid.setAsteroidType(type);

        // Calculate size based on belt type and fraction
        double diameterKm = calculateAsteroidDiameter(belt, sizeFraction);
        double radiusKm = diameterKm / 2.0;

        asteroid.setRadius(radiusKm);
        asteroid.setCircumference(2.0 * Math.PI * radiusKm);

        // Irregular shape for smaller asteroids
        if (diameterKm < 400) {
            double a = diameterKm * RandomUtils.rollRange(0.9, 1.1);
            double b = diameterKm * RandomUtils.rollRange(0.7, 0.95);
            double c = diameterKm * RandomUtils.rollRange(0.5, 0.85);
            asteroid.setDimensionsKm(String.format("%.0f x %.0f x %.0f", a, b, c));
        }

        // Density from type
        double density = RandomUtils.rollRange(type.getDensityMin(), type.getDensityMax());
        asteroid.setDensity(density);

        // Mass
        double volumeM3 = (4.0 / 3.0) * Math.PI * Math.pow(radiusKm * 1000, 3);
        double massKg = volumeM3 * density * 1000; // density in g/cm3 = 1000 kg/m3
        asteroid.setMass(massKg);
        asteroid.setEarthMass(massKg / EARTH_MASS_KG);

        // Albedo
        asteroid.setAlbedo(RandomUtils.rollRange(type.getAlbedoMin(), type.getAlbedoMax()));

        // Orbital elements via OrbitalCreator
        double sma = RandomUtils.rollRange(belt.getInnerOrbit().getSemiMajorAxis(), belt.getOuterOrbit().getSemiMajorAxis());
        // Weight toward peak density
        if (belt.getPeakDensityDistance() != null && RandomUtils.rollRange(0.0, 1.0) < 0.6) {
            double spread = belt.getWidth() * 0.3;
            sma = belt.getPeakDensityDistance() + RandomUtils.rollRange(-spread, spread);
            sma = Math.max(belt.getInnerOrbit().getSemiMajorAxis(), Math.min(belt.getOuterOrbit().getSemiMajorAxis(), sma));
        }

        double ecc = RandomUtils.rollRange(belt.getAverageEccentricity() * 0.5, belt.getMaxEccentricity());
        double inc = RandomUtils.rollRange(0.0, belt.getMaxInclinationDeg());
        asteroid.setOrbit(orbitalCreator.createAsteroidOrbit(sma, arch.totalStellarMass, ecc, inc));

        asteroid.setRotationPeriodHours(RandomUtils.rollRange(2.0, 20.0));
        asteroid.setAxialTilt(RandomUtils.rollRange(0.0, 180.0));

        double temp = TemperatureCalculator.calculatePlanetTemperature(
                primaryStar, sma, asteroid.getAlbedo()
        );
        asteroid.setSurfaceTemp(temp);

        double surfaceGravity = (GRAVITATIONAL_CONSTANT * massKg) / Math.pow(radiusKm * 1000, 2);
        asteroid.setSurfaceGravity(surfaceGravity);

        double escapeVelocity = Math.sqrt(2 * GRAVITATIONAL_CONSTANT * massKg / (radiusKm * 1000)) / 1000.0;
        asteroid.setEscapeVelocity(escapeVelocity);

        asteroid.setSurfaceFeatures(type.getTypicalSurfaceFeatures());
        asteroid.setCrateringLevel(selectCrateringLevel());
        asteroid.setHasRegolith(diameterKm > 1.0);
        if (Boolean.TRUE.equals(asteroid.getHasRegolith())) {
            asteroid.setRegolithDepthM(RandomUtils.rollRange(0.1, Math.min(100, diameterKm * 0.1)));
        }

        asteroid.setComposition(type.getPrimaryComposition());

        if (diameterKm > 400 && Boolean.TRUE.equals(type.getCanBeDifferentiated())) {
            if (RandomUtils.rollRange(0.0, 1.0) < 0.4) {
                asteroid.setIsDifferentiated(true);
                asteroid.setCoreType(determineCoreType(type));
            } else {
                asteroid.setIsDifferentiated(false);
            }
        } else {
            asteroid.setIsDifferentiated(false);
        }

        generateAsteroidMoons(asteroid, diameterKm);

        asteroid.setIsNotable(true);
        asteroid.setNotableReason(generateNotableReason(asteroid, rank, belt));
        asteroid.setDesignationCode(generateDesignationCode(rank));

        asteroid.setAgeMY(belt.getAgeMY());

        return asteroid;
    }

    private void generateResonanceGaps(OrbitalBand belt, Planet giant) {
        if (giant == null || giant.getSemiMajorAxisAU() == null) {
            belt.setHasResonanceGaps(false);
            return;
        }

        double giantSma = giant.getSemiMajorAxisAU();
        List<String> gaps = new ArrayList<>();

        double[][] resonances = {
                {4.0, 1.0, 0.48},
                {3.0, 1.0, 0.48},
                {5.0, 2.0, 0.57},
                {2.0, 1.0, 0.63}
        };

        for (double[] res : resonances) {
            double ratio = Math.pow(res[0] / res[1], -2.0 / 3.0);
            double gapLocation = giantSma * ratio;

            if (gapLocation > belt.getInnerOrbit().getSemiMajorAxis() && gapLocation < belt.getOuterOrbit().getSemiMajorAxis()) {
                gaps.add(String.format("%.0f:%.0f resonance at %.2f AU", res[0], res[1], gapLocation));
            }
        }

        if (!gaps.isEmpty()) {
            belt.setHasResonanceGaps(true);
            belt.setGapDescription(String.join("; ", gaps));
        } else {
            belt.setHasResonanceGaps(false);
        }
    }

    private void generateBeltComposition(OrbitalBand belt, double frostLineAu) {
        double midpoint = (belt.getInnerOrbit().getSemiMajorAxis() + belt.getOuterOrbit().getSemiMajorAxis()) / 2.0;

        if (midpoint < frostLineAu * 0.7) {
            belt.setPrimaryComposition("Silicates 55%, Iron-Nickel 25%, Carbonaceous 20%");
            belt.setCompositionType("ROCKY");
        } else if (midpoint < frostLineAu) {
            belt.setPrimaryComposition("Silicates 40%, Carbonaceous 35%, Hydrated Minerals 15%, Ice 10%");
            belt.setCompositionType("MIXED");
        } else {
            belt.setPrimaryComposition("Water Ice 35%, Carbonaceous 30%, Silicates 25%, Organics 10%");
            belt.setCompositionType("ICY");
        }
    }

    private List<AsteroidTypeRef> getEligibleAsteroidTypes(String beltCode) {
        List<String> affinities = new ArrayList<>();

        switch (beltCode) {
            case "INNER_ROCKY":
                affinities.add("INNER_ROCKY");
                affinities.add("OUTER_ROCKY"); // Some P/D types in outer main belt
                break;
            case "OUTER_ROCKY":
                affinities.add("OUTER_ROCKY");
                affinities.add("INNER_ROCKY");
                break;
            case "KUIPER":
            case "SCATTERED_DISK":
                affinities.add("KUIPER");
                affinities.add("SCATTERED_DISK");
                break;
            default:
                return cachedAsteroidTypes;
        }

        return cachedAsteroidTypes.stream()
                .filter(t -> affinities.contains(t.getBeltAffinity()))
                .collect(Collectors.toList());
    }

    private AsteroidTypeRef selectAsteroidType(List<AsteroidTypeRef> types) {
        double totalWeight = types.stream()
                .mapToDouble(t -> t.getRelativeAbundance() != null ? t.getRelativeAbundance() : 0.1)
                .sum();

        double roll = RandomUtils.rollRange(0.0, totalWeight);
        double cumulative = 0;

        for (AsteroidTypeRef type : types) {
            cumulative += type.getRelativeAbundance() != null ? type.getRelativeAbundance() : 0.1;
            if (roll <= cumulative) {
                return type;
            }
        }

        return types.getFirst();
    }

    private double[] generateSizeDistribution(int count) {
        double[] distribution = new double[count];

        distribution[0] = RandomUtils.rollRange(0.20, 0.35);

        double remaining = 1.0 - distribution[0];

        for (int i = 1; i < count; i++) {
            double fraction = remaining * RandomUtils.rollRange(0.2, 0.5);
            distribution[i] = fraction;
            remaining -= fraction;
        }

        double total = 0;
        for (double d : distribution) total += d;
        for (int i = 0; i < distribution.length; i++) {
            distribution[i] /= total;
        }

        return distribution;
    }

    private double calculateAsteroidDiameter(OrbitalBand belt, double sizeFraction) {
        String beltCode = belt.getBeltType().getCode();

        double maxDiameter;
        double minDiameter = switch (beltCode) {
            case "KUIPER" -> {
                maxDiameter = 800;
                yield 100;
            }
            case "SCATTERED_DISK" -> {
                maxDiameter = 600;
                yield 100;
            }
            default -> {
                maxDiameter = 950;
                yield 100;
            }
        };

        double scaledFraction = Math.pow(sizeFraction / 0.35, 0.5);
        scaledFraction = Math.min(1.0, scaledFraction);

        double diameter = minDiameter + (maxDiameter - minDiameter) * scaledFraction;

        diameter *= RandomUtils.rollRange(0.85, 1.15);

        return Math.max(minDiameter, Math.min(maxDiameter, diameter));
    }

    private void generateAsteroidMoons(Asteroid asteroid, double diameterKm) {
        double moonChance = 0.0;

        if (diameterKm > 500) {
            moonChance = 0.25;
        } else if (diameterKm > 200) {
            moonChance = 0.15;
        } else if (diameterKm > 100) {
            moonChance = 0.05;
        }

        if (RandomUtils.rollRange(0.0, 1.0) < moonChance) {
            asteroid.setHasMoon(true);
            asteroid.setMoonCount(RandomUtils.rollRange(0.0, 1.0) < 0.9 ? 1 : 2);

            double moonSize = diameterKm * RandomUtils.rollRange(0.02, 0.15);
            double moonDistance = diameterKm * RandomUtils.rollRange(2, 10);
            asteroid.setMoonDescription(String.format(
                    "%d small satellite%s (largest ~%.0f km diameter at %.0f km distance)",
                    asteroid.getMoonCount(),
                    asteroid.getMoonCount() > 1 ? "s" : "",
                    moonSize,
                    moonDistance
            ));
        } else {
            asteroid.setHasMoon(false);
            asteroid.setMoonCount(0);
        }
    }

    private String generateNotableReason(Asteroid asteroid, int rank, OrbitalBand belt) {
        List<String> reasons = new ArrayList<>();

        if (rank == 1) {
            reasons.add("Largest object in the " + belt.getName());
        }

        if (Boolean.TRUE.equals(asteroid.getIsDifferentiated())) {
            reasons.add("Differentiated interior with " + asteroid.getCoreType() + " core");
        }

        if (Boolean.TRUE.equals(asteroid.getHasMoon())) {
            if (asteroid.getMoonCount() > 1) {
                reasons.add("Binary/multiple system with " + asteroid.getMoonCount() + " satellites");
            } else {
                reasons.add("Has a small satellite moon");
            }
        }

        // High albedo outlier
        double avgAlbedo = (asteroid.getAsteroidType().getAlbedoMin() +
                           asteroid.getAsteroidType().getAlbedoMax()) / 2.0;
        if (asteroid.getAlbedo() > avgAlbedo * 1.5) {
            reasons.add("Unusually bright surface");
        }

        // High density
        if (asteroid.getDensity() > 5.0) {
            reasons.add("High density suggests metallic composition");
        }

        if (reasons.isEmpty()) {
            reasons.add("Significant size and mass");
        }

        return String.join("; ", reasons);
    }

    private String generateDesignationCode(int rank) {
        int year = 2400 + RandomUtils.rollRange(0, 100);
        char letter1 = (char) ('A' + RandomUtils.rollRange(0, 25));
        char letter2 = (char) ('A' + RandomUtils.rollRange(0, 25));
        return String.format("%d %c%c%d", year, letter1, letter2, rank);
    }

    private String selectCrateringLevel() {
        double roll = RandomUtils.rollRange(0.0, 1.0);
        if (roll < 0.1) return "MINIMAL";
        if (roll < 0.3) return "LIGHT";
        if (roll < 0.6) return "MODERATE";
        if (roll < 0.85) return "HEAVY";
        return "EXTREME";
    }

    private String determineCoreType(AsteroidTypeRef type) {
        String code = type.getCode();
        return switch (code) {
            case "M" -> "Iron-Nickel";
            case "S", "V" -> "Iron-Nickel with silicate mantle";
            case "E" -> "Iron-Nickel with enstatite mantle";
            default -> "Partially differentiated";
        };
    }

    private String generateBeltDescription(OrbitalBand belt, BeltTypeRef beltType) {
        StringBuilder sb = new StringBuilder();
        sb.append(beltType.getDescription()).append(" ");
        sb.append(String.format("Spanning %.2f to %.2f AU with an estimated %,d objects. ",
                belt.getInnerOrbit().getSemiMajorAxis(), belt.getOuterOrbit().getSemiMajorAxis(), belt.getEstimatedObjectCount()));

        if (Boolean.TRUE.equals(belt.getHasResonanceGaps())) {
            sb.append("Features orbital resonance gaps. ");
        }
        if (Boolean.TRUE.equals(belt.getHasCollisionalFamilies())) {
            sb.append(String.format("Contains %d identified collisional families. ", belt.getFamilyCount()));
        }

        return sb.toString().trim();
    }

    private BeltTypeRef findBeltTypeByCode(String code) {
        return cachedBeltTypes.stream()
                .filter(bt -> bt.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    private static class SystemArchitecture {
        List<Planet> rockyPlanets = new ArrayList<>();
        List<Planet> giantPlanets = new ArrayList<>();
        List<Planet> dwarfPlanets = new ArrayList<>();

        double outermostRockyAu = 0;
        double innermostGiantAu = 0;
        double outermostGiantAu = 0;
        double outermostNonDwarfAu = 0;
        double largestGiantMass = 0;

        double frostLineAu = 0;
        double systemAgeMY = 0;
        double starMetallicity = 0;
        double totalStellarMass = 1.0;
    }
}
