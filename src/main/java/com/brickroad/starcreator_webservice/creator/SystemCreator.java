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
    private SectorCreator sectorCreator;

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

        List<OrbitalBand> bands = beltCreator.createBelts(system, primary);
        system.setBands(bands);

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
            Star parentStar = planet.getParentStar();

            if (parentStar != null && parentStar.getName() != null) {
                Integer pos = planet.getOrbitalPosition();
                String planetName = pos != null ? planetPOSString(pos) : "x";
                planet.setName(parentStar.getName() + " " + planetName);
                for (int i = 0; i < planet.getMoons().size(); i++) {
                    planet.getMoons().get(i).setName(planet.getName() + " " + numberToRoman((i + 1)));
                }
                for (int i = 0; i < planet.getBands().size(); i++) {
                    planet.getBands().get(i).setName(planet.getName() + " Ring " + (char) ('A' + i));
                }
            } else {
                planet.setName("Rogue-" + RandomUtils.rollRange(1000, 9999));
            }
        }
    }

    private void assignBandNames(List<OrbitalBand> bands, String systemName) {
        for (OrbitalBand band : bands) {
            if (band.getBeltType() != null) {
                band.setName(switch (band.getBeltType().getCode()) {
                    case "INNER_ROCKY" -> systemName + " IB-01";
                    case "OUTER_ROCKY" -> systemName + " OB-01";
                    case "KUIPER" -> systemName + " KB-01";
                    case "SCATTERED_DISK" -> systemName + " SD-01";
                    default -> "UB-01";
                });
            }
            generateAsteroidNames(band);
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
                single.setDistanceFromStar(0.0);
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

                if (config == BinaryConfiguration.S_TYPE_CLOSE || config == BinaryConfiguration.P_TYPE) {
                    setCloseBinaryDistances(primary, secondary, system.getBinarySeparationAu());
                } else {
                    primary.setDistanceFromStar(0.0);
                    secondary.setDistanceFromStar(system.getBinarySeparationAu());
                }
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

                // Generate distant third star (can be more different)
                Star tertiary = starCreator.generateStar();
                tertiary.setSystem(system);
                tertiary.setStarRole(Star.StarRole.TERTIARY);
                stars.add(tertiary);

                setCloseBinaryDistances(primary3, secondary3, system.getBinarySeparationAu());
                tertiary.setDistanceFromStar(system.getBinarySeparationAu() * 3);
                break;
        }

        return stars;
    }

    private void setCloseBinaryDistances(Star primary, Star secondary, double separation) {

        double m1 = primary.getSolarMass();
        double m2 = secondary.getSolarMass();
        double totalMass = m1 + m2;

        double primaryDistance = separation * (m2 / totalMass);
        double secondaryDistance = separation * (m1 / totalMass);

        primary.setDistanceFromStar(primaryDistance);
        secondary.setDistanceFromStar(secondaryDistance);
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

            // Planet ring designations
            for (OrbitalBand ring : planet.getBands()) {
                Designation rd = ring.getDesignation();
                rd.setBodyType(BodyType.RING);
                rd.setBandCategory(ring.getBandCategory() != null ? ring.getBandCategory().name() : null);
            }
        }

        // System-level belt designations
        for (OrbitalBand band : system.getBands()) {
            Designation bd = band.getDesignation();
            bd.setBodyType(BodyType.BELT);
            bd.setBandCategory(band.getBandCategory() != null ? band.getBandCategory().name() : null);

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
