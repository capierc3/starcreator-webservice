package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ref.AsteroidTypeRef;
import com.brickroad.starcreator_webservice.entity.ref.BeltTypeRef;
import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.enums.BandCategory;
import com.brickroad.starcreator_webservice.enums.BinaryConfiguration;
import com.brickroad.starcreator_webservice.enums.DistanceUnit;
import com.brickroad.starcreator_webservice.repository.AsteroidTypeRefRepository;
import com.brickroad.starcreator_webservice.repository.BeltTypeRefRepository;
import com.brickroad.starcreator_webservice.utils.BinaryStabilityLimits;
import com.brickroad.starcreator_webservice.utils.ConversionFormulas;
import com.brickroad.starcreator_webservice.utils.PhysicsFormulas;
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

    @Autowired
    private PlanetCreator planetCreator;

    private List<BeltTypeRef> cachedBeltTypes;
    private List<AsteroidTypeRef> cachedAsteroidTypes;

    // Physical constants — delegates to PhysicsFormulas (single source of truth)
    private static final double EARTH_MASS_KG = PhysicsFormulas.EARTH_MASS_KG;
    private static final double EARTH_RADIUS_KM = PhysicsFormulas.EARTH_RADIUS_KM;
    private static final double AU_IN_KM = 1.496e8;

    // Giant planet threshold in Earth masses
    private static final double GIANT_PLANET_MASS_THRESHOLD = 15.0;

    // Minimum gap ratio for inner belt formation
    private static final double MIN_GAP_RATIO = 1.5;

    // Minimum belt widths (AU) — below these the belt is too narrow to be meaningful
    private static final double MIN_KUIPER_WIDTH_AU = 0.5;
    private static final double MIN_SCATTERED_DISK_WIDTH_AU = 1.0;

    @PostConstruct
    public void init() {
        cachedBeltTypes = beltTypeRefRepository.findAllBeltTypes();
        cachedAsteroidTypes = asteroidTypeRefRepository.findAllAsteroidTypes();
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Public Entry Point — dispatches per binary configuration
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Creates all belts for a star system. Dispatches by binary configuration
     * to ensure each star gets its own belt set (S_TYPE_WIDE) or the system
     * gets circumbinary belts (P_TYPE / hierarchical).
     * <p>
     * Belts are added directly to their parent Star via {@link Star#addBand}.
     */
    public void createBeltsForSystem(StarSystem system) {
        BinaryConfiguration config = system.getBinaryConfiguration();
        if (config == null) config = BinaryConfiguration.SINGLE;

        switch (config) {
            case SINGLE -> {
                Star primary = findStarByRole(system, Star.StarRole.PRIMARY);
                createCircumstellarBelts(primary, primary.getSolarLuminosity(), primary.getSolarMass(), null);
            }
            case S_TYPE_CLOSE -> {
                Star primary = findStarByRole(system, Star.StarRole.PRIMARY);
                Star secondary = findStarByRole(system, Star.StarRole.SECONDARY);
                double eBin = getBinaryEccentricity(secondary);
                double outerCap = BinaryStabilityLimits.sTypeCriticalSMA(
                        system.getBinarySeparationAu(), primary.getSolarMass(),
                        secondary != null ? secondary.getSolarMass() : 0, eBin);
                createCircumstellarBelts(primary, primary.getSolarLuminosity(), primary.getSolarMass(), outerCap);
            }
            case S_TYPE_WIDE -> {
                Star primary = findStarByRole(system, Star.StarRole.PRIMARY);
                Star secondary = findStarByRole(system, Star.StarRole.SECONDARY);
                double eBin = getBinaryEccentricity(secondary);

                // Yelverton et al. (2019) dead zone — medium-separation binaries suppress belt formation
                double beltChance = BinaryStabilityLimits.beltFormationProbability(system.getBinarySeparationAu());

                // Holman-Wiegert S-type outer stability limits (with eccentricity)
                double outerCapPrimary = BinaryStabilityLimits.sTypeCriticalSMA(
                        system.getBinarySeparationAu(), primary.getSolarMass(), secondary.getSolarMass(), eBin);
                double outerCapSecondary = BinaryStabilityLimits.sTypeCriticalSMA(
                        system.getBinarySeparationAu(), secondary.getSolarMass(), primary.getSolarMass(), eBin);

                if (RandomUtils.rollRange(0.0, 1.0) < beltChance) {
                    createCircumstellarBelts(primary, primary.getSolarLuminosity(), primary.getSolarMass(), outerCapPrimary);
                }
                if (secondary != null && RandomUtils.rollRange(0.0, 1.0) < beltChance) {
                    createCircumstellarBelts(secondary, secondary.getSolarLuminosity(), secondary.getSolarMass(), outerCapSecondary);
                }
            }
            case P_TYPE -> {
                // Circumbinary — belts parented to primary star (same convention as planets)
                Star primary = findStarByRole(system, Star.StarRole.PRIMARY);
                Star secondary = findStarByRole(system, Star.StarRole.SECONDARY);
                double eBin = getBinaryEccentricity(secondary);
                double totalLuminosity = sumLuminosity(system);
                double totalMass = sumMass(system);
                double innerCavity = BinaryStabilityLimits.pTypeCriticalSMA(
                        system.getBinarySeparationAu(), primary.getSolarMass(),
                        secondary != null ? secondary.getSolarMass() : 0, eBin);
                createCircumbinaryBelts(primary, totalLuminosity, totalMass, innerCavity);
            }
            case HIERARCHICAL_BINARY_THIRD, HIERARCHICAL_TRIPLE -> {
                // Close pair gets circumbinary belts
                Star primary = findStarByRole(system, Star.StarRole.PRIMARY);
                Star secondary = findStarByRole(system, Star.StarRole.SECONDARY);
                double innerEBin = getBinaryEccentricity(secondary);
                double pairLuminosity = primary.getSolarLuminosity()
                        + (secondary != null ? secondary.getSolarLuminosity() : 0);
                double pairMass = primary.getSolarMass()
                        + (secondary != null ? secondary.getSolarMass() : 0);
                double innerCavity = BinaryStabilityLimits.pTypeCriticalSMA(
                        system.getBinarySeparationAu(), primary.getSolarMass(),
                        secondary != null ? secondary.getSolarMass() : 0, innerEBin);

                // Outer cap for the close pair's circumbinary belts: tertiary's gravitational influence
                Star tertiary = findStarByRole(system, Star.StarRole.TERTIARY);
                Double pairOuterCap = null;
                if (tertiary != null) {
                    double tertiarySep = system.getBinarySeparationAu() * 3.0;
                    double outerEBin = getBinaryEccentricity(tertiary);
                    pairOuterCap = BinaryStabilityLimits.sTypeCriticalSMA(
                            tertiarySep, pairMass, tertiary.getSolarMass(), outerEBin);
                }
                createCircumbinaryBelts(primary, pairLuminosity, pairMass, innerCavity, pairOuterCap);

                // Tertiary gets its own circumstellar belts, capped by the outer binary
                if (tertiary != null) {
                    double tertiarySep = system.getBinarySeparationAu() * 3.0;
                    double outerEBin = getBinaryEccentricity(tertiary);
                    double outerCapTertiary = BinaryStabilityLimits.sTypeCriticalSMA(
                            tertiarySep, tertiary.getSolarMass(), pairMass, outerEBin);
                    createCircumstellarBelts(tertiary, tertiary.getSolarLuminosity(),
                            tertiary.getSolarMass(), outerCapTertiary);
                }
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Circumstellar Belts — one star, its own planets
    // ═════════════════════════════════════════════════════════════════════

    private void createCircumstellarBelts(Star star, double luminosity, double stellarMass, Double outerCapAU) {
        // CRITICAL: Only use planets from THIS star — no cross-star mixing
        List<Planet> planets = star.getPlanets().stream()
                .sorted(Comparator.comparingDouble(p -> p.getSemiMajorAxisAU() != null ? p.getSemiMajorAxisAU() : 0.0))
                .collect(Collectors.toList());

        if (planets.isEmpty()) return;

        SystemArchitecture arch = analyzeSystem(planets, star, luminosity, stellarMass);
        if (outerCapAU != null) {
            arch.outerStabilityCapAU = outerCapAU;
        }

        OrbitalBand innerBelt = tryCreateInnerRockyBelt(arch, star);
        if (innerBelt != null) {
            star.addBand(innerBelt);
        }

        OrbitalBand kuiperBelt = tryCreateKuiperBelt(arch, star);
        if (kuiperBelt != null) {
            star.addBand(kuiperBelt);

            OrbitalBand scatteredDisk = tryCreateScatteredDisk(arch, kuiperBelt, star);
            if (scatteredDisk != null) {
                star.addBand(scatteredDisk);
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Circumbinary Belts — P-TYPE and Hierarchical close pair
    // ═════════════════════════════════════════════════════════════════════

    private void createCircumbinaryBelts(Star primaryStar, double totalLuminosity, double totalMass, double innerCavityAU) {
        createCircumbinaryBelts(primaryStar, totalLuminosity, totalMass, innerCavityAU, null);
    }

    private void createCircumbinaryBelts(Star primaryStar, double totalLuminosity, double totalMass, double innerCavityAU, Double outerCapAU) {
        // Circumbinary planets are parented to the primary star
        List<Planet> planets = primaryStar.getPlanets().stream()
                .sorted(Comparator.comparingDouble(p -> p.getSemiMajorAxisAU() != null ? p.getSemiMajorAxisAU() : 0.0))
                .collect(Collectors.toList());

        if (planets.isEmpty()) return;

        SystemArchitecture arch = analyzeSystem(planets, primaryStar, totalLuminosity, totalMass);
        arch.innerCavityAU = innerCavityAU;
        if (outerCapAU != null) {
            arch.outerStabilityCapAU = outerCapAU;
        }

        OrbitalBand innerBelt = tryCreateInnerRockyBelt(arch, primaryStar);
        if (innerBelt != null) {
            primaryStar.addBand(innerBelt);
        }

        OrbitalBand kuiperBelt = tryCreateKuiperBelt(arch, primaryStar);
        if (kuiperBelt != null) {
            primaryStar.addBand(kuiperBelt);

            OrbitalBand scatteredDisk = tryCreateScatteredDisk(arch, kuiperBelt, primaryStar);
            if (scatteredDisk != null) {
                primaryStar.addBand(scatteredDisk);
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  System Analysis — builds architecture from a specific planet list
    // ═════════════════════════════════════════════════════════════════════

    private SystemArchitecture analyzeSystem(List<Planet> planets, Star star, double luminosity, double stellarMass) {
        SystemArchitecture arch = new SystemArchitecture();
        arch.star = star;
        arch.frostLineAu = 4.85 * Math.sqrt(luminosity);
        arch.totalStellarMass = stellarMass;
        arch.systemAgeMY = star.getAgeMY() != null ? star.getAgeMY() : 4600.0;
        arch.starMetallicity = star.getMetallicity() != null ? star.getMetallicity() : 0.0;

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

    // ═════════════════════════════════════════════════════════════════════
    //  Belt Creation Methods
    // ═════════════════════════════════════════════════════════════════════

    private OrbitalBand tryCreateInnerRockyBelt(SystemArchitecture arch, Star star) {
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
        belt.setBeltType(beltType);
        belt.setBandCategory(BandCategory.BELT);
        belt.setBandType(beltType.getCode());
        belt.setAgeMY(arch.systemAgeMY);
        belt.setCompositionType(beltType.getTypicalCompositionType());

        double innerEdge = arch.outermostRockyAu * 1.3;
        double outerEdge = arch.innermostGiantAu * 0.6;

        // For circumbinary: inner edge must be outside the cavity
        if (arch.innerCavityAU > 0) {
            innerEdge = Math.max(innerEdge, arch.innerCavityAU);
        }

        if (outerEdge - innerEdge < 0.3) {
            outerEdge = innerEdge + 0.5;
        }
        if (outerEdge > arch.innermostGiantAu * 0.8) {
            outerEdge = arch.innermostGiantAu * 0.8;
        }

        // Enforce stability cap
        if (arch.outerStabilityCapAU < Double.MAX_VALUE) {
            outerEdge = Math.min(outerEdge, arch.outerStabilityCapAU);
            if (outerEdge <= innerEdge) return null;
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

        generateNotableAsteroids(belt, beltType, star, arch);

        // Attempt to spawn belt-born dwarf planets (e.g. Ceres-like bodies)
        trySpawnBeltDwarfPlanets(belt, arch, star);

        return belt;
    }

    private OrbitalBand tryCreateKuiperBelt(SystemArchitecture arch, Star star) {
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

        // Enforce stability cap (Holman-Wiegert limit)
        if (arch.outerStabilityCapAU < Double.MAX_VALUE) {
            outerEdge = Math.min(outerEdge, arch.outerStabilityCapAU);
            // If inner edge already exceeds cap, no room for a belt
            if (innerEdge >= arch.outerStabilityCapAU) return null;
            if (outerEdge - innerEdge < MIN_KUIPER_WIDTH_AU) return null;
        }

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
        belt.setHasGaps(false);
        belt.setHasCollisionalFamilies(RandomUtils.rollRange(0.0, 1.0) < 0.3);
        if (Boolean.TRUE.equals(belt.getHasCollisionalFamilies())) {
            belt.setFamilyCount(RandomUtils.rollRange(1, 5));
        }

        belt.setDescription(generateBeltDescription(belt, beltType));

        // Link any dwarf planets that fall within the belt
        linkDwarfPlanets(belt, arch.dwarfPlanets);

        // Attempt to spawn belt-born dwarf planets (skips if already has linked dwarfs)
        trySpawnBeltDwarfPlanets(belt, arch, star);

        // Generate notable KBOs (fewer if we already have dwarf planets)
        int dwarfCount = belt.getDwarfPlanets().size();
        generateNotableAsteroids(belt, beltType, star, arch, dwarfCount);

        return belt;
    }

    private OrbitalBand tryCreateScatteredDisk(SystemArchitecture arch, OrbitalBand kuiperBelt, Star star) {
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
        belt.setBeltType(beltType);
        belt.setBandCategory(BandCategory.BELT);
        belt.setBandType(beltType.getCode());
        belt.setAgeMY(arch.systemAgeMY);
        belt.setCompositionType(beltType.getTypicalCompositionType());

        // Starts where Kuiper belt ends
        double innerEdge = kuiperBelt.getOuterOrbit().getSemiMajorAxis();
        double outerEdge = innerEdge * RandomUtils.rollRange(3.0, 5.0);

        // Enforce stability cap (Holman-Wiegert limit)
        if (arch.outerStabilityCapAU < Double.MAX_VALUE) {
            outerEdge = Math.min(outerEdge, arch.outerStabilityCapAU);
            if (innerEdge >= arch.outerStabilityCapAU) return null;
            if (outerEdge - innerEdge < MIN_SCATTERED_DISK_WIDTH_AU) return null;
        }

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
        belt.setHasGaps(false);
        belt.setHasCollisionalFamilies(false);

        belt.setDescription(generateBeltDescription(belt, beltType));

        // Generate 0-2 notable objects
        generateNotableAsteroids(belt, beltType, star, arch);

        // Attempt to spawn belt-born dwarf planets
        trySpawnBeltDwarfPlanets(belt, arch, star);

        return belt;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Belt-Born Dwarf Planet Formation
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Attempts to spawn dwarf planets from a belt's own mass budget.
     * Models in-situ formation where belt material concentrates into one
     * or more bodies (like Ceres in the asteroid belt, Pluto in the Kuiper belt).
     *
     * @param belt the belt to spawn dwarfs in
     * @param arch system architecture for modifiers
     * @param star parent star for the dwarf planet
     * @return list of newly created dwarf planets (may be empty)
     */
    private List<Planet> trySpawnBeltDwarfPlanets(OrbitalBand belt, SystemArchitecture arch, Star star) {
        List<Planet> spawnedDwarfs = new ArrayList<>();

        String beltCode = belt.getBeltType() != null ? belt.getBeltType().getCode() : "";

        // ── Formation probability ──
        double baseChance;
        double minMassEarth, maxMassEarth;
        int maxDwarfs;

        switch (beltCode) {
            case "INNER_ROCKY" -> {
                baseChance = 0.15; // Ceres-like; giant planet prevents runaway accretion
                minMassEarth = 0.0001;
                maxMassEarth = 0.002; // Ceres = 0.00016 M⊕
                maxDwarfs = 1;        // Ceres is unique in our belt — rarely 2
            }
            case "KUIPER" -> {
                // Skip if belt already has linked dwarfs from planet generation
                if (!belt.getDwarfPlanets().isEmpty()) return spawnedDwarfs;
                baseChance = 0.25;    // ~1 in 4 Kuiper belts form a Pluto-class body
                minMassEarth = 0.001;
                maxMassEarth = 0.004; // Pluto = 0.0022 M⊕
                maxDwarfs = 2;
            }
            case "SCATTERED_DISK" -> {
                baseChance = 0.08; // Highly disrupted, rarely concentrates mass
                minMassEarth = 0.001;
                maxMassEarth = 0.003;
                maxDwarfs = 1;
            }
            default -> {
                return spawnedDwarfs; // Unknown belt type — no dwarfs
            }
        }

        // ── Modifiers (gentle — keep rates near baseChance) ──
        // Mass modifier: more massive belts → slightly higher formation chance
        double typicalMass = switch (beltCode) {
            case "INNER_ROCKY" -> 0.0005; // typical asteroid belt mass
            case "KUIPER" -> 0.03;         // typical Kuiper belt mass
            case "SCATTERED_DISK" -> 0.01;
            default -> 0.01;
        };
        double beltMass = belt.getTotalMassEarthMasses() != null ? belt.getTotalMassEarthMasses() : typicalMass;
        double massMod = Math.max(0.7, Math.min(1.5, beltMass / typicalMass));

        // Age modifier: older systems have had more time for accretion
        double ageMod = Math.max(0.9, Math.min(1.2, arch.systemAgeMY / 4000.0));

        double formationChance = Math.min(0.60, baseChance * massMod * ageMod);

        if (RandomUtils.rollRange(0.0, 1.0) > formationChance) {
            return spawnedDwarfs; // No formation this time
        }

        // ── Determine count ──
        int count = 1;
        if (maxDwarfs > 1 && "KUIPER".equals(beltCode)) {
            // Kuiper: 1, occasionally 2 (20% chance)
            count = RandomUtils.rollRange(0.0, 1.0) < 0.20 ? 2 : 1;
        }

        // ── Generate dwarfs with mass conservation ──
        double remainingBeltMass = beltMass;
        double maxTotalDwarfMass = beltMass * 0.50; // cap at 50% of belt mass
        double dwarfMassUsed = 0;

        for (int i = 0; i < count; i++) {
            if (dwarfMassUsed >= maxTotalDwarfMass) break;

            double dwarfMass;
            if (i == 0) {
                // Largest dwarf: 15-35% of belt mass, clamped to type range
                dwarfMass = beltMass * RandomUtils.rollRange(0.15, 0.35);
                dwarfMass = Math.max(minMassEarth, Math.min(maxMassEarth, dwarfMass));
            } else {
                // Subsequent dwarfs: 20-60% of the largest
                double largestMass = spawnedDwarfs.isEmpty() ? minMassEarth
                        : spawnedDwarfs.get(0).getEarthMass();
                dwarfMass = largestMass * RandomUtils.rollRange(0.20, 0.60);
                dwarfMass = Math.max(minMassEarth, Math.min(maxMassEarth, dwarfMass));
            }

            // Check we don't exceed cap
            if (dwarfMassUsed + dwarfMass > maxTotalDwarfMass) {
                dwarfMass = maxTotalDwarfMass - dwarfMassUsed;
                if (dwarfMass < minMassEarth) break;
            }

            // ── Placement: weighted toward peak density ──
            double innerEdge = belt.getInnerOrbit().getSemiMajorAxis();
            double outerEdge = belt.getOuterOrbit().getSemiMajorAxis();
            double peakDensity = belt.getPeakDensityDistance() != null
                    ? belt.getPeakDensityDistance()
                    : (innerEdge + outerEdge) / 2.0;

            double sma;
            if (RandomUtils.rollRange(0.0, 1.0) < 0.70) {
                // 70% chance: within ±30% of peak density
                double spread = (outerEdge - innerEdge) * 0.30;
                sma = peakDensity + RandomUtils.rollRange(-spread, spread);
            } else {
                // 30% chance: anywhere in the belt
                sma = RandomUtils.rollRange(innerEdge, outerEdge);
            }
            sma = Math.max(innerEdge, Math.min(outerEdge, sma));

            // ── Create the dwarf planet via reusable method ──
            // Override eccentricity/inclination to match belt properties
            Planet dwarf = planetCreator.createPlanetAtLocation("Dwarf Planet", star, sma, dwarfMass);
            if (dwarf == null) break; // Type not found — shouldn't happen, but be safe

            // Override orbit with belt-appropriate eccentricity and inclination
            double beltEcc = belt.getAverageEccentricity() != null ? belt.getAverageEccentricity() : 0.05;
            double beltInc = belt.getAverageInclinationDeg() != null ? belt.getAverageInclinationDeg() : 5.0;
            double ecc = beltEcc * RandomUtils.rollRange(0.5, 1.5);
            double inc = beltInc * RandomUtils.rollRange(0.5, 1.5);
            double stellarMass = star.getSolarMass() > 0 ? star.getSolarMass() : 1.0;
            dwarf.setOrbit(orbitalCreator.createPlanetOrbit(sma, stellarMass, ecc, inc));
            dwarf.setOrbitalPosition(-1); // Re-set after orbit override (setOrbit replaces OrbitalElements)

            // Link to belt and star
            belt.addDwarfPlanet(dwarf);
            star.addPlanet(dwarf);
            spawnedDwarfs.add(dwarf);
            dwarfMassUsed += dwarfMass;
        }

        // ── Post-processing: subtract dwarf mass from belt ──
        if (dwarfMassUsed > 0 && beltMass > dwarfMassUsed) {
            double newBeltMass = beltMass - dwarfMassUsed;
            belt.setTotalMassEarthMasses(newBeltMass);
            belt.setTotalMassKg(newBeltMass * EARTH_MASS_KG);

            // Reduce object count proportionally
            if (belt.getEstimatedObjectCount() != null && belt.getEstimatedObjectCount() > 0) {
                double massFraction = newBeltMass / beltMass;
                belt.setEstimatedObjectCount((long) (belt.getEstimatedObjectCount() * massFraction));
            }
        }

        return spawnedDwarfs;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Helper Methods
    // ═════════════════════════════════════════════════════════════════════

    private Star findStarByRole(StarSystem system, Star.StarRole role) {
        return system.getStars().stream()
                .filter(s -> s.getStarRole() == role)
                .findFirst()
                .orElse(system.getStars().iterator().next());
    }

    private double sumLuminosity(StarSystem system) {
        return system.getStars().stream().mapToDouble(Star::getSolarLuminosity).sum();
    }

    private double sumMass(StarSystem system) {
        return system.getStars().stream().mapToDouble(Star::getSolarMass).sum();
    }

    private double getBinaryEccentricity(Star star) {
        if (star != null && star.getOrbit() != null
                && star.getOrbit().getEccentricity() != null) {
            return star.getOrbit().getEccentricity();
        }
        return 0.0;
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
    }

    private void generateNotableAsteroids(OrbitalBand belt, BeltTypeRef beltType, Star star, SystemArchitecture arch) {
        generateNotableAsteroids(belt, beltType, star, arch, 0);
    }

    private void generateNotableAsteroids(OrbitalBand belt, BeltTypeRef beltType, Star star, SystemArchitecture arch, int existingNotableCount) {
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
                    belt, eligibleTypes, star, arch,
                    sizeDistribution[i], i + 1
            );
            belt.addNotableAsteroid(asteroid);
        }
    }

    private Asteroid createNotableAsteroid(OrbitalBand belt, List<AsteroidTypeRef> eligibleTypes,
                                           Star star, SystemArchitecture arch,
                                           double sizeFraction, int rank) {
        Asteroid asteroid = new Asteroid();

        AsteroidTypeRef type = selectAsteroidType(eligibleTypes);
        asteroid.setAsteroidType(type);

        // Calculate size based on belt type and fraction
        double diameterKm = calculateAsteroidDiameter(belt, sizeFraction);
        double radiusKm = diameterKm / 2.0;

        asteroid.setRadius(radiusKm);
        asteroid.setCircumference(2.0 * Math.PI * radiusKm);

        // Persist earthRadius (radius is @Transient and lost on DB reload)
        asteroid.setEarthRadius(radiusKm / EARTH_RADIUS_KM);

        // Dimensions — irregular shape for smaller bodies, spheroid label for large ones
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

        asteroid.setSemiMajorAxisAu(sma);

        asteroid.setRotationPeriodHours(RandomUtils.rollRange(2.0, 20.0));
        asteroid.setAxialTilt(RandomUtils.rollRange(0.0, 180.0));

        double temp = TemperatureCalculator.calculatePlanetTemperature(
                star, sma, asteroid.getAlbedo()
        );
        asteroid.setSurfaceTemp(temp);

        // Asteroids store gravity in raw m/s² (not g-multiples like planets/moons)
        double radiusM = radiusKm * 1000;
        double surfaceGravity = (ConversionFormulas.GRAVITATIONAL_CONSTANT * massKg) / (radiusM * radiusM);
        asteroid.setSurfaceGravity(surfaceGravity);

        asteroid.setEscapeVelocity(PhysicsFormulas.escapeVelocityKmS(massKg, radiusKm));

        asteroid.setSurfaceFeatures(type.getTypicalSurfaceFeatures());
        asteroid.setCrateringLevel(selectCrateringLevel());
        boolean hasRegolith = diameterKm > 1.0;
        asteroid.setHasRegolith(hasRegolith);
        asteroid.setRegolithDepthM(hasRegolith
                ? RandomUtils.rollRange(0.1, Math.min(100, diameterKm * 0.1))
                : null);

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

        // Ice content based on belt location and asteroid type
        String beltCode = belt.getBandType();
        String typeCode = type.getCode();
        if ("KUIPER".equals(beltCode) || "SCATTERED_DISK".equals(beltCode)) {
            asteroid.setIcePercent(RandomUtils.rollRange(30.0, 80.0));
        } else if ("C".equals(typeCode) || "B".equals(typeCode)) {
            asteroid.setIcePercent(RandomUtils.rollRange(1.0, 15.0));
        }
        // S, M, V, E types in inner belt: effectively dry, leave null

        asteroid.setNotableReason(generateNotableReason(asteroid, rank, belt));
        asteroid.setDesignationCode(generateDesignationCode(rank));

        asteroid.setAgeMY(belt.getAgeMY());

        return asteroid;
    }

    private void generateResonanceGaps(OrbitalBand belt, Planet giant) {
        if (giant == null || giant.getSemiMajorAxisAU() == null) {
            belt.setHasGaps(false);
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
            belt.setHasGaps(true);
            belt.setGapDescription(String.join("; ", gaps));
        } else {
            belt.setHasGaps(false);
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
                affinities.add("OUTER_ROCKY");
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

    private String generateNotableReason(Asteroid asteroid, int rank, OrbitalBand belt) {
        List<String> reasons = new ArrayList<>();

        if (rank == 1) {
            // belt.getName() may be null at creation time (naming runs later in SystemCreator)
            String beltLabel = belt.getName();
            if (beltLabel == null) {
                // Fall back to a readable belt-type label
                String code = belt.getBandType();
                beltLabel = switch (code != null ? code : "") {
                    case "INNER_ROCKY" -> "inner asteroid belt";
                    case "OUTER_ROCKY" -> "outer asteroid belt";
                    case "KUIPER" -> "Kuiper belt";
                    case "SCATTERED_DISK" -> "scattered disk";
                    default -> "belt";
                };
            }
            reasons.add("Largest object in the " + beltLabel);
        }

        if (Boolean.TRUE.equals(asteroid.getIsDifferentiated())) {
            reasons.add("Differentiated interior with " + asteroid.getCoreType() + " core");
        }

        // High ice content
        if (asteroid.getIcePercent() != null && asteroid.getIcePercent() > 50) {
            reasons.add(String.format("Ice-rich body (%.0f%% ice by mass)", asteroid.getIcePercent()));
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

        if (Boolean.TRUE.equals(belt.getHasGaps())) {
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

    // ═════════════════════════════════════════════════════════════════════
    //  System Architecture — internal analysis state
    // ═════════════════════════════════════════════════════════════════════

    private static class SystemArchitecture {
        Star star;

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

        /** Holman-Wiegert S-type outer stability limit, or MAX_VALUE if unconstrained. */
        double outerStabilityCapAU = Double.MAX_VALUE;

        /** P-type inner cavity (circumbinary minimum stable orbit), 0 for circumstellar. */
        double innerCavityAU = 0.0;
    }
}
