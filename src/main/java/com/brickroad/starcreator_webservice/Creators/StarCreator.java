package com.brickroad.starcreator_webservice.Creators;

import com.brickroad.starcreator_webservice.model.stars.Star;
import com.brickroad.starcreator_webservice.model.stars.StarTypeRef;
import com.brickroad.starcreator_webservice.repos.StarTypeRefRepository;
import com.brickroad.starcreator_webservice.utils.ConversionFormulas;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StarCreator {

    @Autowired
    private StarTypeRefRepository starTypeRefRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final double VARIANCE = 0.15;
    private List<StarTypeRef> cachedStarTypes;

    @PostConstruct
    public void init() {
        cachedStarTypes = starTypeRefRepository.findAllStarTypes();
    }

    public Star generateStar() {
        StarTypeRef type = selectStarTypeByRarity();
        return generateStarByType(type);
    }

    public Star generateStarByType(StarTypeRef type) {
        Star star = new Star();

        // Generate base properties
        double solarMass = RandomUtils.rollRange(type.getMinMass(), type.getMaxMass());
        double solarRadius = calculateRadius(solarMass, type);
        solarRadius = addVariance(solarRadius);

        // Populate star with all properties
        populateStar(star, type, solarMass, solarRadius);

        return star;
    }

    public Star generateCompanionStar(Star primary) {
        List<StarTypeRef> compatibleTypes = getCompatibleStarTypes(primary);
        StarTypeRef companionType = selectFromList(compatibleTypes);

        Star companion = generateStarByType(companionType);

        // Match ages and metallicity (binary stars form together)
        companion.setAgeMY(primary.getAgeMY() + RandomUtils.rollRange(-500, 500));
        companion.setMetallicity(primary.getMetallicity() + RandomUtils.rollRange(-0.1, 0.1));

        return companion;
    }

    private void populateStar(Star star, StarTypeRef type, double solarMass, double solarRadius) {
        star.setType(type.getName());
        star.setSpectralType(type.getSpectralClass());

        star.setSolarMass(solarMass);
        star.setSolarRadius(solarRadius);

        star.setMass(ConversionFormulas.solarMassToKG(solarMass));
        star.setRadius(ConversionFormulas.solarRadiusToKM(solarRadius));
        star.setCircumference(ConversionFormulas.radiusToCircumference(star.getRadius()));

        star.setSolarLuminosity(calculateLuminosity(solarMass, type));
        star.setHabitableZoneInnerAU(Math.sqrt(star.getSolarLuminosity() / 1.1));
        star.setHabitableZoneOuterAU(Math.sqrt(star.getSolarLuminosity() / 0.53));
        star.setSurfaceTemp(calculateSurfaceTemp(type, solarMass));
        star.setColorIndex(determineColor(star.getSurfaceTemp()));

        star.setAgeMY(calculateStarAge(solarMass, type));
        star.setMetallicity(RandomUtils.rollRange(-0.5, 0.3));

        star.setRotationDays(calculateRotationPeriod(solarMass, star.getAgeMY()));

        star.setIsVariable(isStarVariable(type));
        if (star.isVariable()) {
            star.setVariabilityPeriod(RandomUtils.rollRange(0.1, 100));
        }

        star.setCreatedAt(LocalDateTime.now());
        star.setModifiedAt(LocalDateTime.now());
    }

    private StarTypeRef selectStarTypeByRarity() {

        int totalWeight = cachedStarTypes.stream()
                .mapToInt(StarTypeRef::getRarityWeight)
                .sum();

        int random = RandomUtils.rollRange(0, totalWeight);
        int currentWeight = 0;
        for (StarTypeRef type : cachedStarTypes) {
            currentWeight += type.getRarityWeight();
            if (random < currentWeight) {
                return type;
            }
        }
        return cachedStarTypes.getFirst();
    }

    private double calculateRadius(double mass, StarTypeRef type) {
        if (type.getMassRadiusExponent() != null) {
            double baseRadius = Math.pow(mass, type.getMassRadiusExponent());
            if (type.getRadiusMultiplierMin() != null) {
                double multiplier = RandomUtils.rollRange(type.getRadiusMultiplierMin(), type.getRadiusMultiplierMax());
                return (baseRadius * multiplier);
            }
            return baseRadius;
        }
        if (type.getRadiusMultiplierMin() != null) {
            return RandomUtils.rollRange(type.getRadiusMultiplierMin(), type.getRadiusMultiplierMax());
        }
        return 1.0;
    }

    private double addVariance(double value) {
        double factor = 1.0 + ((Math.random() - 0.5) * 2 * VARIANCE);
        return (value * factor);
    }

    private double calculateLuminosity(double mass, StarTypeRef type) {
        String name = type.getName().toLowerCase();

        if (name.contains("main sequence")) {
            // Main sequence: L ≈ M^3.5
            return Math.pow(mass, 3.5);
        } else if (name.contains("giant")) {
            // Giants are much more luminous
            return Math.pow(mass, 3.5) * RandomUtils.rollRange(50, 500);
        } else if (name.contains("white dwarf")) {
            // White dwarfs are dim
            return 0.0001 * mass;
        } else if (name.contains("neutron")) {
            // Neutron stars don't shine like normal stars
            return 0.00001;
        }

        return Math.pow(mass, 3.5); // Default
    }

    private double calculateSurfaceTemp(StarTypeRef type, double mass) {
        String spectral = type.getSpectralClass();

        if (spectral == null) {
            String name = type.getName().toLowerCase();
            if (name.contains("white dwarf")) return RandomUtils.rollRange(8000, 40000);
            if (name.contains("neutron")) return RandomUtils.rollRange(600000, 1000000);
            if (name.contains("giant")) return RandomUtils.rollRange(3000, 5000);
            return 5778; // Default to Sun-like
        }

        // Main sequence temperatures by spectral class
        return switch (spectral) {
            case "O" -> RandomUtils.rollRange(30000, 50000);
            case "B" -> RandomUtils.rollRange(10000, 30000);
            case "A" -> RandomUtils.rollRange(7500, 10000);
            case "F" -> RandomUtils.rollRange(6000, 7500);
            case "G" -> RandomUtils.rollRange(5200, 6000);
            case "K" -> RandomUtils.rollRange(3700, 5200);
            case "M" -> RandomUtils.rollRange(2400, 3700);
            default -> 5778;
        };
    }

    private double calculateStarAge(double solarMass, StarTypeRef type) {
        String typeName = type.getName().toLowerCase();

        if (typeName.contains("proto") || typeName.contains("t tauri")) {
            return RandomUtils.rollRange(0.1, 10);
        } else if (typeName.contains("main sequence")) {
            double maxLifespan = 10000 / Math.pow(solarMass, 2.5);
            // Cap at universe age (13,800 MY) and use 80% of star's potential lifespan
            double maxAge = Math.min(maxLifespan * 0.8, 13800);
            return RandomUtils.rollRange(100, maxAge); // At least 100 MY old
        } else if (typeName.contains("giant")) {
            return RandomUtils.rollRange(5000, 13000);
        } else if (typeName.contains("white dwarf")) {
            return RandomUtils.rollRange(1000, 13000);
        }

        return RandomUtils.rollRange(1000, 10000);
    }

    private double calculateRotationPeriod(double solarMass, double age) {
        double baseRotation = 25.0 / Math.pow(solarMass, 0.5);
        double ageFactor = 1.0 + (age / 5000.0);
        return baseRotation * ageFactor * RandomUtils.rollRange(0.8, 1.2);
    }

    private String determineColor(double surfaceTemp) {
        if (surfaceTemp > 30000) return "Blue";
        if (surfaceTemp > 10000) return "Blue-White";
        if (surfaceTemp > 7500) return "White";
        if (surfaceTemp > 6000) return "Yellow-White";
        if (surfaceTemp > 5200) return "Yellow";
        if (surfaceTemp > 3700) return "Orange";
        return "Red";
    }

    private boolean isStarVariable(StarTypeRef type) {
        String typeName = type.getName().toLowerCase();
        // Giants and young stars are more variable
        if (typeName.contains("giant")) return Math.random() < 0.3;
        if (typeName.contains("proto") || typeName.contains("t tauri")) return Math.random() < 0.5;
        return Math.random() < 0.05; // 5% of main sequence stars are variable
    }

    private List<StarTypeRef> getCompatibleStarTypes(Star primary) {
        String primaryTypeName = primary.getType().toLowerCase();

        // Filter out incompatible combinations
        return cachedStarTypes.stream()
                .filter(type -> {
                    String typeName = type.getName().toLowerCase();

                    // Proto-stars and T Tauri only with each other
                    if (primaryTypeName.contains("proto") || primaryTypeName.contains("t tauri")) {
                        return typeName.contains("proto") || typeName.contains("t tauri");
                    }

                    // White dwarfs and neutron stars only with main sequence or each other
                    if (primaryTypeName.contains("white dwarf") || primaryTypeName.contains("neutron")) {
                        return typeName.contains("main sequence") ||
                                typeName.contains("white dwarf") ||
                                typeName.contains("neutron");
                    }

                    // Giants only with main sequence or other giants
                    if (primaryTypeName.contains("giant")) {
                        return typeName.contains("main sequence") || typeName.contains("giant");
                    }

                    // Main sequence can pair with anything except proto/T Tauri
                    return !typeName.contains("proto") && !typeName.contains("t tauri");
                })
                .collect(Collectors.toList());
    }

    private StarTypeRef selectFromList(List<StarTypeRef> types) {
        int totalWeight = types.stream()
                .mapToInt(StarTypeRef::getRarityWeight)
                .sum();

        int random = (int) (Math.random() * totalWeight);

        int currentWeight = 0;
        for (StarTypeRef type : types) {
            currentWeight += type.getRarityWeight();
            if (random < currentWeight) {
                return type;
            }
        }

        return types.getFirst();
    }
}
