package com.brickroad.starcreator_webservice.Creators;

import com.brickroad.starcreator_webservice.model.enums.BinaryConfiguration;
import com.brickroad.starcreator_webservice.model.starSystems.StarSystem;
import com.brickroad.starcreator_webservice.model.stars.Star;
import com.brickroad.starcreator_webservice.repos.SectorRepository;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class SystemCreator {

    @Autowired
    private StarCreator starCreator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SectorRepository sectorRepository;

    public StarSystem generateSystem() {
        StarSystem system = new StarSystem();
        system.setName(generateRandomSystemName());

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

        system.setSizeAu(calculateSystemSize(config, stars));

        calculateHabitableZone(system, stars, config);

        system.setDescription(generateDescription(system));

        return system;
    }

    private void calculateBinarySeparation(StarSystem system, BinaryConfiguration config) {
        double separation;

        if (config == BinaryConfiguration.S_TYPE_CLOSE || config == BinaryConfiguration.P_TYPE) {
            // Close binary: 0.1 to 5 AU
            separation = RandomUtils.rollRange(0.1, 5.0);
        } else if (config == BinaryConfiguration.S_TYPE_WIDE) {
            // Wide binary: 10 to 100 AU
            separation = RandomUtils.rollRange(10, 100);
        } else { // Hierarchical
            // Wide separation for stability
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

    private String generateRandomSystemName() {
        return jdbcTemplate.queryForObject("SELECT suffix FROM ref.name_suffix ORDER BY RANDOM() LIMIT 1",String.class);
    }

    private int generateStarCount() {
        double roll = Math.random();
        if (roll < 0.70) return 1;  // 70% single star
        if (roll < 0.95) return 2;  // 25% binary
        return 3;                    // 5% trinary
    }

    private double calculateSystemSize(BinaryConfiguration config, Set<Star> stars) {
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
            calculateSingleStarHabitableZone(system, primary);
        }
    }

    private void calculateSingleStarHabitableZone(StarSystem system, Star star) {
        double luminosity = star.getSolarLuminosity();
        system.setHabitableLow(Math.sqrt(luminosity / 1.1));
        system.setHabitableHigh(Math.sqrt(luminosity / 0.53));
    }

    private void calculateCircumbinaryHabitableZone(StarSystem system, Set<Star> stars) {
        double totalLuminosity = stars.stream()
                .mapToDouble(Star::getSolarLuminosity)
                .sum();

        // Calculate theoretical habitable zone
        double theoreticalInner = Math.sqrt(totalLuminosity / 1.1);
        double theoreticalOuter = Math.sqrt(totalLuminosity / 0.53);

        // Minimum stable distance for circumbinary planets (2.5x separation)
        double minStableDistance = system.getBinarySeparationAu() * 2.5;

        // Actual habitable zone starts at the stable distance
        double innerEdge = Math.max(theoreticalInner, minStableDistance);
        double outerEdge = Math.max(theoreticalOuter, minStableDistance * 1.2);

        system.setHabitableLow(innerEdge);
        system.setHabitableHigh(outerEdge);
    }


    private String generateDescription(StarSystem system) {
        BinaryConfiguration config = system.getBinaryConfiguration();
        StringBuilder desc = new StringBuilder();

        desc.append(config.getDescription()).append(". ");

        if (config != BinaryConfiguration.SINGLE) {
            desc.append(formatBinaryOrbitalInfo(system));
        }

        desc.append(formatHabitableZoneInfo(system, config));

        return desc.toString();
    }

    private String formatBinaryOrbitalInfo(StarSystem system) {
        BinaryConfiguration config = system.getBinaryConfiguration();

        if (config == BinaryConfiguration.S_TYPE_CLOSE || config == BinaryConfiguration.P_TYPE) {
            return String.format("Binary separation: %.2f AU (stars orbit common center of mass), orbital period: %.1f days. ",
                    system.getBinarySeparationAu(),
                    system.getBinaryOrbitalPeriodDays());
        } else {
            return String.format("Binary separation: %.2f AU (secondary orbits primary), orbital period: %.1f days. ",
                    system.getBinarySeparationAu(),
                    system.getBinaryOrbitalPeriodDays());
        }
    }

    private String formatHabitableZoneInfo(StarSystem system, BinaryConfiguration config) {
        if (config == BinaryConfiguration.S_TYPE_WIDE) {
            return formatWideBinaryHabitableZones(system);
        } else {
            return formatStandardHabitableZone(system);
        }
    }

    private String formatWideBinaryHabitableZones(StarSystem system) {
        StringBuilder zones = new StringBuilder();
        zones.append("Both stars can host independent planetary systems. ");
        zones.append(String.format("Primary habitable zone: %.2f to %.2f AU. ",
                system.getHabitableLow(),
                system.getHabitableHigh()));

        // Calculate and show secondary's zone
        Star secondary = system.getStars().stream()
                .filter(s -> s.getStarRole() == Star.StarRole.SECONDARY)
                .findFirst()
                .orElse(null);

        if (secondary != null) {
            double secLuminosity = secondary.getSolarLuminosity();
            double secInner = Math.sqrt(secLuminosity / 1.1);
            double secOuter = Math.sqrt(secLuminosity / 0.53);
            zones.append(String.format("Secondary habitable zone: %.2f to %.2f AU.",
                    secInner, secOuter));
        }

        return zones.toString();
    }

    private String formatStandardHabitableZone(StarSystem system) {
        return String.format("Habitable zone: %.2f to %.2f AU.",
                system.getHabitableLow(),
                system.getHabitableHigh());
    }

}
