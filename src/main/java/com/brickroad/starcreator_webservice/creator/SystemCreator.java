package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.enums.BandCategory;
import com.brickroad.starcreator_webservice.enums.BinaryConfiguration;
import com.brickroad.starcreator_webservice.enums.BodyType;
import com.brickroad.starcreator_webservice.repository.SurveyRepository;
import com.brickroad.starcreator_webservice.utils.systems.SystemClassification;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import com.brickroad.starcreator_webservice.utils.systems.SystemClassifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SystemCreator {

    @Autowired
    private StarCreator starCreator;

    @Autowired
    private PlanetCreator planetCreator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BeltCreator beltCreator;

    @Autowired
    private SystemClassifier systemClassifier;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private TrojanCreator trojanCreator;

    @Autowired
    private SectorCreator sectorCreator;

    @Autowired
    private OrbitalCreator orbitalCreator;

    public StarSystem generateSystem() {
        Survey survey = surveyRepository.findByCode("SCS")
                .orElseThrow(() -> new IllegalStateException("Default survey 'SCS' not found"));
        Sector sector = sectorCreator.generateSector(survey);
        return generateSystem(sector);
    }

    public StarSystem generateSystem(Sector sector) {
        StarSystem system = new StarSystem();
        system.setSector(sector);

        system.setX(RandomUtils.rollRange(-100, 100));
        system.setY(RandomUtils.rollRange(-100, 100));
        system.setZ(RandomUtils.rollRange(-100, 100));

        int starCount = generateStarCount();
        BinaryConfiguration config = determineConfiguration(starCount);
        system.setBinaryConfiguration(config);

        if (starCount > 1) {
            calculateBinarySeparation(system, config);
        }

        Set<Star> stars = generateStarsForConfiguration(config, system);
        system.setStars(stars);

        if (starCount > 1) {
            calculateBinaryOrbitalPeriod(system, stars);
        }

        Star primary = stars.stream()
                .filter(s -> s.getStarRole() == Star.StarRole.PRIMARY)
                .findFirst()
                .orElse(stars.iterator().next());
        system.setPrimaryStarId(primary.getId());

        system.setSizeAu(calculateSystemSize(config));

        calculateHabitableZone(system, stars, config);

        List<Planet> planets = generatePlanetsForSystem(system, stars, config);
        system.setPlanets(planets); // distributes planets to their parent stars

        beltCreator.createBeltsForSystem(system);
        trojanCreator.createTrojansForSystem(system);

        SystemClassification classification = systemClassifier.classify(system);
        system.setClassification(classification);
        system.setDescription(classification.getScoutReport());

        system.setName(sector.getName() + "-" + Integer.toString(RandomUtils.rollRange(0,46_655), Character.MAX_RADIX).toUpperCase());
        assignStarNames(stars, system.getName());
        assignPlanetNames(system.getPlanets());
        assignBandNames(system.getBands(), system.getName());

        initDesignationBodyTypes(system, stars, system.getPlanets());

        return system;
    }

    private void assignStarNames(Set<Star> stars, String systemName) {
        if (stars.size() == 1) {
            stars.iterator().next().setName(systemName + " A");
        } else {
            List<Star> starList = new ArrayList<>(stars);
            starList.sort(Comparator.comparing(s -> s.getStarRole().ordinal()));

            char suffix = 'A';
            for (Star star : starList) {
                star.setName(systemName + " " + suffix);
                suffix++;
            }
        }
    }

    private void assignPlanetNames(List<Planet> planets) {
        for (Planet planet : planets) {
            // Skip belt-born dwarfs — they get named by nameBeltDwarfPlanets()
            if (planet.getOrbitalPosition() != null && planet.getOrbitalPosition() < 0) continue;

            Star parentStar = planet.getParentStar();

            if (parentStar != null && parentStar.getName() != null) {
                Integer pos = planet.getOrbitalPosition();
                String planetName = pos != null ? planetPOSString(pos) : "x";
                planet.setName(parentStar.getName() + " " + planetName);
                for (int i = 0; i < planet.getMoons().size(); i++) {
                    planet.getMoons().get(i).setName(planet.getName() + " " + numberToRoman((i + 1)));
                }
                int ringIndex = 0;
                for (OrbitalBand band : planet.getBands()) {
                    if (band.getBandCategory() == BandCategory.TROJAN) {
                        // Trojan swarms: "{PlanetName} TJ-L4" / "{PlanetName} TJ-L5"
                        String lp = band.getLagrangePoint() != null ? band.getLagrangePoint() : "L4";
                        band.setName(planet.getName() + " TJ-" + lp);
                        // Name notable asteroids in the swarm
                        for (int a = 0; a < band.getNotableAsteroids().size(); a++) {
                            band.getNotableAsteroids().get(a).setName(
                                    band.getName() + " AST-" + String.format("%04d", a + 1));
                        }
                    } else {
                        // Rings: "{PlanetName} Ring A/B/C"
                        band.setName(planet.getName() + " Ring " + (char) ('A' + ringIndex));
                        ringIndex++;
                    }
                }
            } else {
                planet.setName("Rogue-" + RandomUtils.rollRange(1000, 9999));
            }
        }
    }

    private void assignBandNames(List<OrbitalBand> bands, String systemName) {
        for (OrbitalBand band : bands) {
            if (band.getBeltType() != null) {
                // Use the parent star's name as prefix (e.g. "SYS-A2F A" for star A)
                String prefix = (band.getStar() != null && band.getStar().getName() != null)
                        ? band.getStar().getName()
                        : systemName;
                band.setName(switch (band.getBeltType().getCode()) {
                    case "INNER_ROCKY" -> prefix + " IB-01";
                    case "OUTER_ROCKY" -> prefix + " OB-01";
                    case "KUIPER" -> prefix + " KB-01";
                    case "SCATTERED_DISK" -> prefix + " SD-01";
                    default -> prefix + " UB-01";
                });
            }
            generateAsteroidNames(band);
            nameBeltDwarfPlanets(band);
        }
    }

    /**
     * Names dwarf planets associated with a belt using the belt's name as prefix.
     * Applies to both belt-born dwarfs (orbitalPosition = -1) and existing dwarfs
     * linked via linkDwarfPlanets(). Format: "{BeltName} DWF-{2DigitIndex}"
     * e.g. "SCS-A2F A KB-01 DWF-01", "SCS-A2F A IB-01 DWF-02"
     */
    private void nameBeltDwarfPlanets(OrbitalBand band) {
        String baseName = band.getName();
        if (baseName == null) return;
        List<Planet> dwarfs = band.getDwarfPlanets();
        for (int i = 0; i < dwarfs.size(); i++) {
            Planet dwarf = dwarfs.get(i);
            dwarf.setName(baseName + " DWF-" + String.format("%02d", i + 1));
            // Also rename any moons the dwarf has
            for (int m = 0; m < dwarf.getMoons().size(); m++) {
                dwarf.getMoons().get(m).setName(dwarf.getName() + " " + numberToRoman(m + 1));
            }
        }
    }

    private void generateAsteroidNames(OrbitalBand band) {
        String baseName = band.getName();
        for (int i = 0; i < band.getNotableAsteroids().size(); i++) {
            band.getNotableAsteroids().get(i).setName(baseName + " AST-" + String.format("%04d", i + 1));
        }
    }

    private void calculateBinarySeparation(StarSystem system, BinaryConfiguration config) {
        double separation;

        if (config == BinaryConfiguration.S_TYPE_CLOSE || config == BinaryConfiguration.P_TYPE) {
            separation = RandomUtils.rollRange(0.1, 5.0);
        } else if (config == BinaryConfiguration.S_TYPE_WIDE) {
            separation = RandomUtils.rollRange(10, 100);
        } else {
            separation = RandomUtils.rollRange(50, 500);
        }
        system.setBinarySeparationAu(separation);
    }

    private void calculateBinaryOrbitalPeriod(StarSystem system, Set<Star> stars) {
        double separation = system.getBinarySeparationAu();

        // Calculate using Kepler's Third Law: P^2 = a^3 / (M1 + M2)
        double totalMass = stars.stream()
                .mapToDouble(Star::getSolarMass)
                .sum();

        double periodYears = Math.sqrt(Math.pow(separation, 3) / totalMass);
        system.setBinaryOrbitalPeriodDays(periodYears * 365.25);
    }

    private Set<Star> generateStarsForConfiguration(BinaryConfiguration config, StarSystem system) {
        Set<Star> stars = new HashSet<>();

        switch (config) {
            case SINGLE:
                Star single = starCreator.generateStar();
                single.setSystem(system);
                single.setStarRole(Star.StarRole.PRIMARY);
                single.setOrbit(orbitalCreator.createStarOrbit(0.0, 0.0, 0.0, 0.0));
                stars.add(single);
                break;

            case S_TYPE_CLOSE:
            case S_TYPE_WIDE:
            case P_TYPE:
                Star primary = starCreator.generateStar();
                primary.setSystem(system);
                primary.setStarRole(Star.StarRole.PRIMARY);
                stars.add(primary);

                Star secondary = starCreator.generateCompanionStar(primary);
                secondary.setSystem(system);
                secondary.setStarRole(Star.StarRole.SECONDARY);
                stars.add(secondary);

                assignBinaryOrbits(primary, secondary, system.getBinarySeparationAu(), config);
                break;

            case HIERARCHICAL_BINARY_THIRD:
            case HIERARCHICAL_TRIPLE:
                // Generate close binary pair
                Star primary3 = starCreator.generateStar();
                primary3.setSystem(system);
                primary3.setStarRole(Star.StarRole.PRIMARY);
                stars.add(primary3);

                Star secondary3 = starCreator.generateCompanionStar(primary3);
                secondary3.setSystem(system);
                secondary3.setStarRole(Star.StarRole.SECONDARY);
                stars.add(secondary3);

                // Inner pair: close binary orbit
                assignBinaryOrbits(primary3, secondary3, system.getBinarySeparationAu(), BinaryConfiguration.S_TYPE_CLOSE);

                // Generate distant third star
                Star tertiary = starCreator.generateStar();
                tertiary.setSystem(system);
                tertiary.setStarRole(Star.StarRole.TERTIARY);
                stars.add(tertiary);

                // Outer orbit: tertiary orbits the (AB) barycenter
                double innerMass = primary3.getSolarMass() + secondary3.getSolarMass();
                double tertiaryMass = tertiary.getSolarMass();
                double outerTotalMass = innerMass + tertiaryMass;
                double tertiaryDistance = system.getBinarySeparationAu() * 3;

                // Tertiary SMA = distance from system barycenter (mass-weighted)
                double tertiarySMA = tertiaryDistance * (innerMass / outerTotalMass);
                double outerEcc = generateBinaryEccentricity(tertiaryDistance);
                double outerInc = generateBinaryInclination(tertiaryDistance);

                tertiary.setOrbit(orbitalCreator.createStarOrbit(
                        tertiarySMA, outerTotalMass, outerEcc, outerInc));
                break;
        }

        return stars;
    }

    /**
     * Assigns full Keplerian orbital elements to both stars in a binary pair.
     * <p>
     * For close binaries (S_TYPE_CLOSE, P_TYPE): both stars orbit the barycenter
     * with mass-weighted semi-major axes, shared orbital plane (Ω, ω), same
     * eccentricity, and mean anomalies offset by 180°.
     * <p>
     * For wide binaries (S_TYPE_WIDE): primary at origin, secondary orbits at
     * the full separation distance.
     */
    private void assignBinaryOrbits(Star primary, Star secondary, double separation,
                                     BinaryConfiguration config) {
        double m1 = primary.getSolarMass();
        double m2 = secondary.getSolarMass();
        double totalMass = m1 + m2;
        double ecc = generateBinaryEccentricity(separation);
        double inc = generateBinaryInclination(separation);

        if (config == BinaryConfiguration.S_TYPE_WIDE) {
            // Wide binary: primary at origin, secondary orbits at full separation
            primary.setOrbit(orbitalCreator.createStarOrbit(0.0, 0.0, 0.0, 0.0));
            secondary.setOrbit(orbitalCreator.createStarOrbit(separation, totalMass, ecc, inc));
        } else {
            // Close binary: both stars orbit the barycenter
            double primarySMA = separation * (m2 / totalMass);
            double secondarySMA = separation * (m1 / totalMass);

            primary.setOrbit(orbitalCreator.createStarOrbit(primarySMA, totalMass, ecc, inc));
            secondary.setOrbit(orbitalCreator.createStarOrbit(secondarySMA, totalMass, ecc, inc));

            // Both stars share the same orbital plane
            secondary.getOrbit().setLongitudeOfAscendingNodeDeg(
                    primary.getOrbit().getLongitudeOfAscendingNodeDeg());
            secondary.getOrbit().setArgumentOfPeriapsisDeg(
                    primary.getOrbit().getArgumentOfPeriapsisDeg());

            // Secondary is 180° opposite the primary in its orbit
            secondary.getOrbit().setMeanAnomalyDeg(
                    (primary.getOrbit().getMeanAnomalyDeg() + 180.0) % 360.0);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Binary Star Orbital Element Generation
    // ═══════════════════════════════════════════════════════════════

    /**
     * Generate eccentricity for a binary star orbit based on separation.
     * <p>
     * Follows the observed period-eccentricity correlation from Raghavan et al. (2010)
     * and Duchêne & Kraus (2013):
     * <ul>
     *   <li>Tight binaries (< 1 AU) — tidally circularized, nearly circular</li>
     *   <li>Close binaries (1-5 AU) — moderate eccentricity</li>
     *   <li>Wide binaries (5-50 AU) — thermal distribution peak</li>
     *   <li>Very wide binaries (> 50 AU) — dynamically hot</li>
     * </ul>
     */
    private double generateBinaryEccentricity(double separationAU) {
        if (separationAU < 1.0) {
            return RandomUtils.rollRange(0.0, 0.15);
        } else if (separationAU < 5.0) {
            return RandomUtils.rollRange(0.1, 0.4);
        } else if (separationAU <= 50.0) {
            return RandomUtils.rollRange(0.3, 0.6);
        } else {
            return RandomUtils.rollRange(0.4, 0.8);
        }
    }

    /**
     * Generate inclination for a binary star orbit based on separation.
     * <p>
     * Tight binaries are tidally aligned to near-zero inclination.
     * Wide binaries have isotropic orientations: cos(i) uniform on [-1,1],
     * so i = arccos(1 - 2*random) for proper isotropic sampling.
     */
    private double generateBinaryInclination(double separationAU) {
        if (separationAU < 1.0) {
            return RandomUtils.rollRange(0.0, 10.0);
        } else if (separationAU < 5.0) {
            return RandomUtils.rollRange(0.0, 30.0);
        } else {
            // Isotropic distribution: i = arccos(1 - 2*rand) → range [0°, 180°]
            return Math.toDegrees(Math.acos(1.0 - 2.0 * Math.random()));
        }
    }

    private BinaryConfiguration determineConfiguration(int starCount) {
        if (starCount == 1) {
            return BinaryConfiguration.SINGLE;
        } else if (starCount == 2) {
            double roll = Math.random();
            if (roll < 0.3) return BinaryConfiguration.S_TYPE_CLOSE;
            if (roll < 0.6) return BinaryConfiguration.S_TYPE_WIDE;
            return BinaryConfiguration.P_TYPE;
        } else { // trinary
            double roll = Math.random();
            if (roll < 0.7) return BinaryConfiguration.HIERARCHICAL_BINARY_THIRD;
            return BinaryConfiguration.HIERARCHICAL_TRIPLE;
        }
    }

    private int generateStarCount() {
        double roll = Math.random();
        if (roll < 0.70) return 1;  // 70% single star
        if (roll < 0.95) return 2;  // 25% binary
        return 3;                    // 5% trinary
    }

    private double calculateSystemSize(BinaryConfiguration config) {
        return switch (config) {
            case SINGLE -> RandomUtils.rollRange(50, 200);
            case S_TYPE_CLOSE, P_TYPE -> RandomUtils.rollRange(100, 300);
            case S_TYPE_WIDE, HIERARCHICAL_BINARY_THIRD, HIERARCHICAL_TRIPLE -> RandomUtils.rollRange(300, 1000);
        };
    }

    private void calculateHabitableZone(StarSystem system, Set<Star> stars, BinaryConfiguration config) {
        Star primary = stars.stream()
                .filter(s -> s.getStarRole() == Star.StarRole.PRIMARY)
                .findFirst()
                .orElse(stars.iterator().next());

        if (config == BinaryConfiguration.P_TYPE) {
            calculateCircumbinaryHabitableZone(system, stars);
        } else {
            system.setHabitableLow(primary.getHabitableZoneInnerAU());
            system.setHabitableHigh(primary.getHabitableZoneOuterAU());;
        }
    }

    private void calculateCircumbinaryHabitableZone(StarSystem system, Set<Star> stars) {
        double totalLuminosity = stars.stream()
                .mapToDouble(Star::getSolarLuminosity)
                .sum();

        double theoreticalInner = Math.sqrt(totalLuminosity / 1.1);
        double theoreticalOuter = Math.sqrt(totalLuminosity / 0.53);

        double minStableDistance = system.getBinarySeparationAu() * 2.5;

        double innerEdge = Math.max(theoreticalInner, minStableDistance);
        double outerEdge = Math.max(theoreticalOuter, minStableDistance * 1.2);

        if (innerEdge > theoreticalOuter) {
            system.setHabitableLow(-1.0);
            system.setHabitableHigh(-1.0);
        } else {
            system.setHabitableLow(innerEdge);
            system.setHabitableHigh(outerEdge);
        }
    }

    private List<Planet> generatePlanetsForSystem(StarSystem system, Set<Star> stars, BinaryConfiguration config) {
        List<Planet> allPlanets = new ArrayList<>();

        switch (config) {
            case SINGLE:
                Star single = stars.stream()
                        .filter(s -> s.getStarRole() == Star.StarRole.PRIMARY)
                        .findFirst()
                        .orElse(stars.iterator().next());

                List<Planet> singlePlanets = planetCreator.generatePlanetarySystem(single);
                allPlanets.addAll(singlePlanets);
                break;

            case S_TYPE_CLOSE:
                Star sClosePrimary = stars.stream()
                        .filter(s -> s.getStarRole() == Star.StarRole.PRIMARY)
                        .findFirst()
                        .orElseThrow();

                List<Planet> sClosePlanets = planetCreator.generatePlanetarySystem(sClosePrimary);
                allPlanets.addAll(sClosePlanets);
                break;

            case S_TYPE_WIDE:
                stars.forEach(star -> {
                    List<Planet> starPlanets = planetCreator.generatePlanetarySystem(star);
                    allPlanets.addAll(starPlanets);
                });
                break;

            case P_TYPE:
                Star pTypePrimary = stars.stream()
                        .filter(s -> s.getStarRole() == Star.StarRole.PRIMARY)
                        .findFirst()
                        .orElseThrow();

                List<Planet> circumbinaryPlanets = generateCircumbinaryPlanets(system, pTypePrimary, system.getBinarySeparationAu());
                allPlanets.addAll(circumbinaryPlanets);
                break;

            case HIERARCHICAL_BINARY_THIRD:
            case HIERARCHICAL_TRIPLE:
                Star hierarchicalPrimary = stars.stream()
                        .filter(s -> s.getStarRole() == Star.StarRole.PRIMARY)
                        .findFirst()
                        .orElseThrow();

                List<Planet> binaryPlanets = generateCircumbinaryPlanets(system, hierarchicalPrimary, system.getBinarySeparationAu());
                allPlanets.addAll(binaryPlanets);

                Star tertiary = stars.stream()
                        .filter(s -> s.getStarRole() == Star.StarRole.TERTIARY)
                        .findFirst()
                        .orElse(null);

                if (tertiary != null) {
                    List<Planet> tertiaryPlanets = planetCreator.generatePlanetarySystem(tertiary);
                    allPlanets.addAll(tertiaryPlanets);
                }
                break;
        }

        return allPlanets;
    }

    private List<Planet> generateCircumbinaryPlanets(StarSystem system, Star primary, double binarySeparation) {
        List<Planet> planets = planetCreator.generatePlanetarySystem(primary);
        return planets;
    }

    private void initDesignationBodyTypes(StarSystem system, Set<Star> stars, List<Planet> planets) {
        // System designation
        system.getDesignation().setBodyType(BodyType.SYSTEM);

        // Star designations
        for (Star star : stars) {
            Designation d = star.getDesignation();
            d.setBodyType(BodyType.STAR);
            d.setStarRole(star.getStarRole() != null ? star.getStarRole().name() : null);
        }

        // Planet designations
        for (Planet planet : planets) {
            Designation d = planet.getDesignation();
            d.setBodyType(BodyType.PLANET);
            Star parentStar = planet.getParentStar();
            d.setParentBodyName(parentStar != null ? parentStar.getName() : null);

            // Moon designations
            for (Moon moon : planet.getMoons()) {
                Designation md = moon.getDesignation();
                md.setBodyType(BodyType.MOON);
                md.setParentBodyName(planet.getName());
            }

            // Planet band designations (rings and trojans)
            for (OrbitalBand band : planet.getBands()) {
                Designation bd = band.getDesignation();
                if (band.getBandCategory() == BandCategory.TROJAN) {
                    bd.setBodyType(BodyType.TROJAN);
                } else {
                    bd.setBodyType(BodyType.RING);
                }
                bd.setBandCategory(band.getBandCategory() != null ? band.getBandCategory().name() : null);
                bd.setParentBodyName(planet.getName());

                // Notable asteroid designations within Trojan swarms
                for (Asteroid asteroid : band.getNotableAsteroids()) {
                    Designation ad = asteroid.getDesignation();
                    ad.setBodyType(BodyType.ASTEROID);
                    if (asteroid.getAsteroidType() != null) {
                        ad.setObjectType(asteroid.getAsteroidType().getName());
                    }
                }
            }
        }

        // System-level belt designations (aggregated from all stars)
        for (OrbitalBand band : system.getBands()) {
            Designation bd = band.getDesignation();
            bd.setBodyType(BodyType.BELT);
            bd.setBandCategory(band.getBandCategory() != null ? band.getBandCategory().name() : null);
            if (band.getStar() != null) {
                bd.setParentBodyName(band.getStar().getName());
            }

            // Asteroid designations
            for (Asteroid asteroid : band.getNotableAsteroids()) {
                Designation ad = asteroid.getDesignation();
                ad.setBodyType(BodyType.ASTEROID);
                if (asteroid.getAsteroidType() != null) {
                    ad.setObjectType(asteroid.getAsteroidType().getName());
                }
            }
        }
    }

    public static String numberToRoman(int number) {
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] numerals = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

        StringBuilder roman = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                roman.append(numerals[i]);
                number -= values[i];
            }
        }
        return roman.toString();
    }

    public static String planetPOSString(int n) {
        if (n < 0) throw new IllegalArgumentException("Negative numbers not supported");

        StringBuilder sb = new StringBuilder();
        int base = 25;
        do {
            int remainder = n % base;
            sb.insert(0, (char)('a' + remainder));
            n = n / base - 1;
        } while (n >= 0);

        return sb.toString();
    }


}
