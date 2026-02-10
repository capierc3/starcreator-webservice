package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ud.Star;
import com.brickroad.starcreator_webservice.entity.ref.StarTypeRef;
import com.brickroad.starcreator_webservice.repository.StarTypeRefRepository;
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

        companion.setAgeMY(primary.getAgeMY() + RandomUtils.rollRange(-500, 500));
        companion.setMetallicity(primary.getMetallicity() + RandomUtils.rollRange(-0.1, 0.1));

        if (primary.getActivityCycleYears() != null && companion.getActivityCycleYears() != null) {
            if (Math.random() < 0.4) {
                companion.setActivityCyclePhase(primary.getActivityCyclePhase());
            }
        }

        if (Boolean.TRUE.equals(primary.getInGrandMinimum()) && Math.random() < 0.3) {
            companion.setInGrandMinimum(true);
            companion.setGrandMinimumDurationYears(
                    primary.getGrandMinimumDurationYears() + RandomUtils.rollRange(-20, 20));
            companion.setGrandMinimumDepth(
                    primary.getGrandMinimumDepth() + RandomUtils.rollRange(-0.1, 0.1));
        }

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

        populateStellarActivity(star, type);

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
            return Math.pow(mass, 3.5);
        } else if (name.contains("brown dwarf")) {
            return Math.pow(mass, 2.3) * 0.001;
        } else if (name.contains("giant")) {
            return Math.pow(mass, 3.5) * RandomUtils.rollRange(50, 500);
        } else if (name.contains("white dwarf")) {
            return 0.0001 * mass;
        } else if (name.contains("neutron")) {
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
            if (name.contains("brown dwarf")) return RandomUtils.rollRange(500, 2400);
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
            case "L" -> RandomUtils.rollRange(1300, 2400);
            case "T" -> RandomUtils.rollRange(500, 1300);
            case "Y" -> RandomUtils.rollRange(250, 500);
            default -> 5778;
        };
    }

    private double calculateStarAge(double solarMass, StarTypeRef type) {
        String typeName = type.getName().toLowerCase();

        if (typeName.contains("proto") || typeName.contains("t tauri")) {
            return RandomUtils.rollRange(0.1, 10);
        } else if (typeName.contains("main sequence")) {
            double maxLifespan = 10000 / Math.pow(solarMass, 2.5);
            double maxAge = Math.min(maxLifespan * 0.8, 13800);
            return RandomUtils.rollRange(100, maxAge);
        } else if (typeName.contains("giant")) {
            return RandomUtils.rollRange(5000, 13000);
        } else if (typeName.contains("white dwarf")) {
            return RandomUtils.rollRange(1000, 13000);
        } else if (typeName.contains("brown dwarf")) {
            return RandomUtils.rollRange(100, 13000);
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
        if (surfaceTemp > 2400) return "Red";
        if (surfaceTemp > 1300) return "Deep Red";
        if (surfaceTemp > 500) return "Magenta";
        return "Infrared";
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

    private void populateStellarActivity(Star star, StarTypeRef type) {
        double mass = star.getSolarMass();
        double age = star.getAgeMY();
        double rotationDays = star.getRotationDays();

        populateEvolutionaryState(star, type, mass, age);
        populateChromosphericActivity(star, type, mass, rotationDays);
        populateActivityCycle(star, type, rotationDays);
        populateGrandMinimum(star, type);
        populateFlareActivity(star, type, mass, rotationDays);
        populateStarSpots(star, type);
        populateStellarWind(star, type, mass);
        populateCoronalProperties(star, type);
    }

    private void populateEvolutionaryState(Star star, StarTypeRef type, double mass, double ageMY) {
        String typeName = type.getName().toLowerCase();

        if (typeName.contains("proto") || typeName.contains("t tauri")) {
            star.setEvolutionaryStage("PRE_MAIN_SEQUENCE");
            star.setMainSequenceFraction(null);
            star.setEstimatedRemainingMsMy(null);
            return;
        }

        if (typeName.contains("white dwarf")) {
            star.setEvolutionaryStage("WHITE_DWARF_COOLING");
            star.setMainSequenceFraction(null);
            star.setEstimatedRemainingMsMy(null);
            return;
        }

        if (typeName.contains("neutron")) {
            star.setEvolutionaryStage("NEUTRON_STAR");
            star.setMainSequenceFraction(null);
            star.setEstimatedRemainingMsMy(null);
            return;
        }

        if (typeName.contains("brown dwarf")) {
            star.setEvolutionaryStage("BROWN_DWARF_COOLING");
            star.setMainSequenceFraction(null);
            star.setEstimatedRemainingMsMy(null);
            return;
        }

        if (typeName.contains("giant")) {
            // Determine which giant phase
            if (typeName.contains("super")) {
                star.setEvolutionaryStage("ASYMPTOTIC_GIANT");
            } else {
                star.setEvolutionaryStage(Math.random() < 0.7 ? "RED_GIANT_BRANCH" : "HORIZONTAL_BRANCH");
            }
            star.setMainSequenceFraction(null);
            star.setEstimatedRemainingMsMy(null);
            return;
        }

        double msLifespan = 10000.0 / Math.pow(mass, 2.5);
        double fraction = ageMY / msLifespan;
        fraction = Math.min(fraction, 0.99);

        star.setMainSequenceFraction(fraction);
        star.setEstimatedRemainingMsMy(Math.max(0, msLifespan - ageMY));

        if (fraction < 0.1) {
            star.setEvolutionaryStage("EARLY_MAIN_SEQUENCE");
        } else if (fraction < 0.5) {
            star.setEvolutionaryStage("MID_MAIN_SEQUENCE");
        } else if (fraction < 0.85) {
            star.setEvolutionaryStage("LATE_MAIN_SEQUENCE");
        } else {
            star.setEvolutionaryStage("SUBGIANT_TRANSITION");
        }
    }

    private void populateChromosphericActivity(Star star, StarTypeRef type, double mass, double rotationDays) {
        String typeName = type.getName().toLowerCase();

        if (!hasConvectiveEnvelope(type)) {
            star.setRossbyNumber(null);
            star.setLogRPrimeHk(null);
            star.setActivityLevel("INACTIVE");
            return;
        }

        if (typeName.contains("white dwarf") || typeName.contains("neutron") || typeName.contains("brown dwarf")) {
            star.setRossbyNumber(null);
            star.setLogRPrimeHk(null);
            star.setActivityLevel("INACTIVE");
            return;
        }

        if (typeName.contains("proto") || typeName.contains("t tauri")) {
            star.setRossbyNumber(null);
            star.setLogRPrimeHk(RandomUtils.rollRange(-4.0, -3.9));
            star.setActivityLevel("HYPERACTIVE");
            return;
        }

        double tauConv = calculateConvectiveTurnover(mass);
        double rossby = rotationDays / tauConv;
        star.setRossbyNumber(rossby);

        double logRHK;
        if (rossby < 0.1) {
            // Saturated regime — activity maxes out
            logRHK = RandomUtils.rollRange(-4.2, -4.0);
        } else if (rossby < 5.0) {
            // Power-law regime with steeper slope and lower base
            // Spreads stars from VERY_ACTIVE (Ro~0.3) through LOW (Ro~3-4)
            logRHK = -4.7 - 0.5 * Math.log10(rossby) + RandomUtils.rollRange(-0.1, 0.1);
        } else {
            // Very inactive — old slow rotators
            logRHK = RandomUtils.rollRange(-5.2, -5.0);
        }
        logRHK = Math.max(-5.2, Math.min(-3.9, logRHK));
        star.setLogRPrimeHk(logRHK);

        // Activity level classification (unchanged thresholds)
        if (logRHK > -4.2) {
            star.setActivityLevel("HYPERACTIVE");
        } else if (logRHK > -4.5) {
            star.setActivityLevel("VERY_ACTIVE");
        } else if (logRHK > -4.75) {
            star.setActivityLevel("ACTIVE");
        } else if (logRHK > -4.95) {
            star.setActivityLevel("MODERATE");
        } else if (logRHK > -5.1) {
            star.setActivityLevel("LOW");
        } else {
            star.setActivityLevel("INACTIVE");
        }
    }

    private double calculateConvectiveTurnover(double mass) {
        if (mass > 1.3) return 5.0;
        if (mass > 1.1) return 12.0;
        if (mass > 0.9) return 22.0;
        if (mass > 0.7) return 35.0;
        if (mass > 0.5) return 55.0;
        if (mass > 0.35) return 90.0;
        if (mass > 0.2) return 140.0;
        return 200.0;
    }

    private boolean hasConvectiveEnvelope(StarTypeRef type) {
        String spectral = type.getSpectralClass();
        if (spectral == null) return true;
        return !spectral.equals("O") && !spectral.equals("B");
    }

    private void populateActivityCycle(Star star, StarTypeRef type, double rotationDays) {
        String typeName = type.getName().toLowerCase();

        if (!hasConvectiveEnvelope(type) || typeName.contains("white dwarf") ||
                typeName.contains("neutron") || typeName.contains("brown dwarf")) {
            star.setActivityCycleYears(null);
            star.setActivityCyclePhase(null);
            return;
        }

        double cyclePeriod;
        if (rotationDays < 3) {
            cyclePeriod = RandomUtils.rollRange(1.0, 5.0);
        } else if (rotationDays < 10) {
            cyclePeriod = 0.72 * Math.pow(rotationDays, 0.8) * RandomUtils.rollRange(0.7, 1.3);
        } else {
            cyclePeriod = 0.72 * Math.pow(rotationDays, 0.8) * RandomUtils.rollRange(0.7, 1.3);
        }
        cyclePeriod = Math.max(2.0, Math.min(30.0, cyclePeriod));
        star.setActivityCycleYears(cyclePeriod);

        star.setActivityCyclePhase(RandomUtils.rollRange(0.0, 1.0));
    }

    private void populateGrandMinimum(Star star, StarTypeRef type) {
        String typeName = type.getName().toLowerCase();

        if (!typeName.contains("main sequence") || !hasConvectiveEnvelope(type)) {
            star.setInGrandMinimum(false);
            star.setGrandMinimumDurationYears(null);
            star.setGrandMinimumDepth(null);
            return;
        }

        double baseProb = 0.17;
        String activity = star.getActivityLevel();
        if ("VERY_ACTIVE".equals(activity) || "HYPERACTIVE".equals(activity)) {
            baseProb = 0.02;
        } else if ("ACTIVE".equals(activity)) {
            baseProb = 0.08;
        } else if ("LOW".equals(activity) || "INACTIVE".equals(activity)) {
            baseProb = 0.25;
        }

        boolean inMinimum = Math.random() < baseProb;
        star.setInGrandMinimum(inMinimum);

        if (inMinimum) {
            star.setGrandMinimumDurationYears(RandomUtils.rollRange(30.0, 200.0));
            star.setGrandMinimumDepth(RandomUtils.rollRange(0.5, 1.0));
            star.setActivityCyclePhase(RandomUtils.rollRange(0.0, 0.15));
        }
    }

    private void populateFlareActivity(Star star, StarTypeRef type, double mass, double rotationDays) {
        String typeName = type.getName().toLowerCase();

        if (typeName.contains("white dwarf") || typeName.contains("neutron")) {
            star.setFlareFrequencyPerDay(0.0);
            star.setMaxFlareEnergyErgs(null);
            star.setFlareClass("NONE");
            star.setSuperflareCapable(false);
            return;
        }

        if (!hasConvectiveEnvelope(type)) {
            star.setFlareFrequencyPerDay(0.0);
            star.setMaxFlareEnergyErgs(null);
            star.setFlareClass("NONE");
            star.setSuperflareCapable(false);
            return;
        }

        if (typeName.contains("brown dwarf")) {
            star.setFlareFrequencyPerDay(RandomUtils.rollRange(0.0, 0.05));
            star.setMaxFlareEnergyErgs(RandomUtils.rollRange(27.0, 29.0));
            star.setFlareClass("MICROFLARE");
            star.setSuperflareCapable(false);
            return;
        }

        if (typeName.contains("proto") || typeName.contains("t tauri")) {
            star.setFlareFrequencyPerDay(RandomUtils.rollRange(5.0, 30.0));
            star.setMaxFlareEnergyErgs(RandomUtils.rollRange(33.0, 36.0));
            star.setFlareClass("SUPERFLARE");
            star.setSuperflareCapable(true);
            return;
        }

        double baseRate;
        String spectral = type.getSpectralClass();

        switch (spectral) {
            case "M" -> {
                if (rotationDays < 5) {
                    baseRate = RandomUtils.rollRange(10.0, 50.0);
                } else if (rotationDays < 20) {
                    baseRate = RandomUtils.rollRange(2.0, 15.0);
                } else if (rotationDays < 60) {
                    baseRate = RandomUtils.rollRange(0.3, 3.0);
                } else {
                    baseRate = RandomUtils.rollRange(0.01, 0.5); // Old slow M dwarfs
                }
            }
            case "K" -> {
                if (rotationDays < 10) {
                    baseRate = RandomUtils.rollRange(1.0, 8.0);
                } else if (rotationDays < 30) {
                    baseRate = RandomUtils.rollRange(0.2, 2.0);
                } else {
                    baseRate = RandomUtils.rollRange(0.01, 0.5);
                }
            }
            case "G" -> {
                if (rotationDays < 15) {
                    baseRate = RandomUtils.rollRange(0.5, 3.0);
                } else if (rotationDays < 30) {
                    baseRate = RandomUtils.rollRange(0.05, 0.8);
                } else {
                    baseRate = RandomUtils.rollRange(0.005, 0.1);
                }
            }
            case "F" -> baseRate = RandomUtils.rollRange(0.005, 0.3);
            case "A" -> baseRate = RandomUtils.rollRange(0.0, 0.02);
            case null, default -> baseRate = RandomUtils.rollRange(0.01, 0.5);
        }

        double cyclePhase = star.getActivityCyclePhase() != null ? star.getActivityCyclePhase() : 0.5;
        double cycleModulation = 0.2 + 0.8 * Math.sin(Math.PI * cyclePhase);

        if (Boolean.TRUE.equals(star.getInGrandMinimum())) {
            double depth = star.getGrandMinimumDepth() != null ? star.getGrandMinimumDepth() : 0.8;
            cycleModulation *= (1.0 - depth * 0.9);
        }

        star.setFlareFrequencyPerDay(baseRate * cycleModulation);

        double maxEnergy = switch (spectral) {
            case "M" -> 30.0 + 4.0 * Math.pow(Math.random(), 2.0);
            case "K" -> 29.5 + 4.0 * Math.pow(Math.random(), 2.0);
            case "G" -> 29.0 + 3.5 * Math.pow(Math.random(), 2.2);
            case "F" -> 28.0 + 3.0 * Math.pow(Math.random(), 2.5);
            case null, default -> 27.0 + 3.0 * Math.pow(Math.random(), 2.5);
        };

        boolean superflareCapable = false;
        if (rotationDays < 10 && mass < 1.4) {
            maxEnergy += RandomUtils.rollRange(0.5, 2.5);
            superflareCapable = true;
        }
        star.setMaxFlareEnergyErgs(maxEnergy);
        star.setSuperflareCapable(superflareCapable);

        if (maxEnergy >= 34) {
            star.setFlareClass("SUPERFLARE");
        } else if (maxEnergy >= 32) {
            star.setFlareClass("X_CLASS");
        } else if (maxEnergy >= 31) {
            star.setFlareClass("M_CLASS");
        } else if (maxEnergy >= 30) {
            star.setFlareClass("C_CLASS");
        } else if (maxEnergy >= 28) {
            star.setFlareClass("MICROFLARE");
        } else {
            star.setFlareClass("NANOFLARE");
        }
    }

    private void populateStarSpots(Star star, StarTypeRef type) {
        String typeName = type.getName().toLowerCase();

        if (!hasConvectiveEnvelope(type) || typeName.contains("white dwarf") ||
                typeName.contains("neutron") || typeName.contains("brown dwarf")) {
            star.setStarspotCoveragePercent(0.0);
            star.setStarspotTempContrastK(null);
            star.setHasPolarSpots(false);
            return;
        }

        if (typeName.contains("proto") || typeName.contains("t tauri")) {
            star.setStarspotCoveragePercent(RandomUtils.rollRange(20.0, 60.0));
            star.setStarspotTempContrastK(RandomUtils.rollRange(500.0, 1500.0));
            star.setHasPolarSpots(true);
            return;
        }

        double rossby = star.getRossbyNumber() != null ? star.getRossbyNumber() : 1.0;
        String spectral = type.getSpectralClass();

        double maxCoverageForType = switch (spectral != null ? spectral : "") {
            case "A" -> 0.5;
            case "F" -> 3.0;
            case "K" -> 15.0;
            case "M" -> 12.0;
            default -> 5.0;
        };

        double baseFraction;
        if (rossby < 0.1) {
            baseFraction = RandomUtils.rollRange(0.6, 1.0); // Near ceiling
        } else if (rossby < 0.3) {
            baseFraction = RandomUtils.rollRange(0.3, 0.7);
        } else if (rossby < 0.8) {
            baseFraction = RandomUtils.rollRange(0.1, 0.4);
        } else if (rossby < 1.5) {
            baseFraction = RandomUtils.rollRange(0.02, 0.15); // Sun-like ~0.06 of max
        } else {
            baseFraction = RandomUtils.rollRange(0.005, 0.05); // Very quiet
        }

        double baseCoverage = maxCoverageForType * baseFraction;

        double phase = star.getActivityCyclePhase() != null ? star.getActivityCyclePhase() : 0.5;
        double phaseModulation = 0.1 + 0.9 * Math.sin(Math.PI * phase);

        if (Boolean.TRUE.equals(star.getInGrandMinimum())) {
            double depth = star.getGrandMinimumDepth() != null ? star.getGrandMinimumDepth() : 0.8;
            phaseModulation *= (1.0 - depth * 0.95);
        }

        double finalCoverage = baseCoverage * phaseModulation;

        finalCoverage = Math.min(finalCoverage, maxCoverageForType);

        star.setStarspotCoveragePercent(finalCoverage);

        double surfaceTemp = star.getSurfaceTemp();
        double contrast;
        if (surfaceTemp > 6000) {
            contrast = RandomUtils.rollRange(1200, 2200);
        } else if (surfaceTemp > 5000) {
            contrast = RandomUtils.rollRange(800, 1600);
        } else if (surfaceTemp > 4000) {
            contrast = RandomUtils.rollRange(400, 1000);
        } else {
            contrast = RandomUtils.rollRange(100, 500);
        }
        star.setStarspotTempContrastK(contrast);

        star.setHasPolarSpots(rossby < 0.3 && Math.random() < 0.7);
    }

    private void populateStellarWind(Star star, StarTypeRef type, double mass) {
        String typeName = type.getName().toLowerCase();

        if (typeName.contains("neutron")) {
            star.setStellarWindMassLossRate(RandomUtils.rollRange(1e-18, 1e-15));
            star.setStellarWindVelocityKmS(RandomUtils.rollRange(10000.0, 200000.0));
            star.setStellarWindDensityAt1AU(RandomUtils.rollRange(0.01, 0.5));
            return;
        }

        if (typeName.contains("white dwarf")) {
            star.setStellarWindMassLossRate(RandomUtils.rollRange(1e-17, 1e-14));
            star.setStellarWindVelocityKmS(RandomUtils.rollRange(500.0, 2000.0));
            star.setStellarWindDensityAt1AU(RandomUtils.rollRange(0.01, 1.0));
            return;
        }

        if (typeName.contains("brown dwarf")) {
            star.setStellarWindMassLossRate(RandomUtils.rollRange(1e-18, 1e-15));
            star.setStellarWindVelocityKmS(RandomUtils.rollRange(10.0, 100.0));
            star.setStellarWindDensityAt1AU(RandomUtils.rollRange(0.001, 0.1));
            return;
        }

        if (typeName.contains("giant") || typeName.contains("super")) {
            // Giants have massive, slow winds
            star.setStellarWindMassLossRate(RandomUtils.rollRange(1e-8, 1e-5));
            star.setStellarWindVelocityKmS(RandomUtils.rollRange(10.0, 50.0));
            star.setStellarWindDensityAt1AU(RandomUtils.rollRange(100.0, 10000.0));
            return;
        }

        if (typeName.contains("proto") || typeName.contains("t tauri")) {
            // Young stars have powerful winds
            star.setStellarWindMassLossRate(RandomUtils.rollRange(1e-10, 1e-7));
            star.setStellarWindVelocityKmS(RandomUtils.rollRange(200.0, 500.0));
            star.setStellarWindDensityAt1AU(RandomUtils.rollRange(50.0, 5000.0));
            return;
        }

        double activity = star.getLogRPrimeHk() != null ? star.getLogRPrimeHk() : -4.9;
        double activityScaler = Math.pow(10, activity + 5.0);

        double baseLossRate = 2e-14 * Math.pow(mass, 1.5);
        star.setStellarWindMassLossRate(baseLossRate * activityScaler * RandomUtils.rollRange(0.5, 2.0));

        double surfaceTemp = star.getSurfaceTemp();
        double baseVelocity;
        if (surfaceTemp > 7000) {
            baseVelocity = RandomUtils.rollRange(800, 2500);
        } else if (surfaceTemp > 5500) {
            baseVelocity = RandomUtils.rollRange(300, 800);
        } else if (surfaceTemp > 4000) {
            baseVelocity = RandomUtils.rollRange(200, 500);
        } else {
            baseVelocity = RandomUtils.rollRange(100, 350);
        }
        star.setStellarWindVelocityKmS(baseVelocity);

        double densityScale = (star.getStellarWindMassLossRate() / 2e-14) * (400.0 / baseVelocity);
        star.setStellarWindDensityAt1AU(6.0 * densityScale * RandomUtils.rollRange(0.6, 1.4));
    }

    private void populateCoronalProperties(Star star, StarTypeRef type) {
        String typeName = type.getName().toLowerCase();

        if (typeName.contains("white dwarf") || typeName.contains("neutron") || typeName.contains("brown dwarf")) {
            star.setHasCorona(false);
            star.setCoronalTempMK(null);
            star.setXrayLuminosityClass("DARK");
            return;
        }

        if (type.getSpectralClass() != null &&
                (type.getSpectralClass().equals("O") || type.getSpectralClass().equals("B"))) {
            star.setHasCorona(false);
            star.setCoronalTempMK(RandomUtils.rollRange(0.5, 3.0));
            star.setXrayLuminosityClass("BRIGHT");
            return;
        }

        star.setHasCorona(true);

        double activity = star.getLogRPrimeHk() != null ? star.getLogRPrimeHk() : -4.9;
        double coronalTemp;
        if (activity > -4.3) {
            coronalTemp = RandomUtils.rollRange(8.0, 30.0);
        } else if (activity > -4.6) {
            coronalTemp = RandomUtils.rollRange(3.0, 12.0);
        } else if (activity > -4.9) {
            coronalTemp = RandomUtils.rollRange(1.5, 5.0);
        } else {
            coronalTemp = RandomUtils.rollRange(0.5, 2.0);
        }
        star.setCoronalTempMK(coronalTemp);

        if (coronalTemp > 15) {
            star.setXrayLuminosityClass("INTENSE");
        } else if (coronalTemp > 5) {
            star.setXrayLuminosityClass("BRIGHT");
        } else if (coronalTemp > 2) {
            star.setXrayLuminosityClass("MODERATE");
        } else {
            star.setXrayLuminosityClass("DIM");
        }

        if (typeName.contains("proto") || typeName.contains("t tauri")) {
            star.setCoronalTempMK(RandomUtils.rollRange(10.0, 50.0));
            star.setXrayLuminosityClass("INTENSE");
        }
    }








}
