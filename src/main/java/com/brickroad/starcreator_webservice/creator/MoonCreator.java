package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.entity.ref.MoonTypeRef;
import com.brickroad.starcreator_webservice.entity.ref.PlanetTypeRef;
import com.brickroad.starcreator_webservice.enums.AtmosphereClassification;
import com.brickroad.starcreator_webservice.enums.BinaryConfiguration;
import com.brickroad.starcreator_webservice.repository.MoonTypeRefRepository;
import com.brickroad.starcreator_webservice.utils.ConversionFormulas;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import com.brickroad.starcreator_webservice.utils.planets.PlanetaryAtmosphere;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MoonCreator {

    @Autowired
    private MoonTypeRefRepository moonTypeRefRepository;

    @Autowired
    private AtmosphereCreator atmosphereCreator;

    private static final double EARTH_MASS_KG = 5.972e24;
    private static final double EARTH_RADIUS_KM = 6371.0;

    public List<Moon> createMoons(Planet planet, Star primaryStar, PlanetTypeRef planetType) {
        List<Moon> moons = new ArrayList<>();

        double totalMassBudget = calculateTotalMoonMassBudget(planet, planetType, primaryStar);
        int numMoons = determineNumberOfMoons(planet, primaryStar, totalMassBudget, planetType);
        if (numMoons == 0) {
            return moons;
        }

        double hillSphereKm = calculateHillSphere(planet, primaryStar);
        double innerRocheLimit = calculateRocheLimit(planet, 3.3);
        double outerRocheLimit = calculateRocheLimit(planet, 1.0);

        List<MoonGenerationData> moonDataList = distributeMoonMassesAndTypes(numMoons, totalMassBudget, planet);

        for (int i = 0; i < numMoons; i++) {
            MoonGenerationData moonData = moonDataList.get(i);
            Moon moon = createMoon(planet, primaryStar, i + 1, hillSphereKm,
                    innerRocheLimit, outerRocheLimit,
                    moonData.moonType, moonData.massEarthMasses);
            moons.add(moon);
        }

        return moons;
    }

    private Moon createMoon(Planet planet, Star primaryStar, int moonNumber,
                            double hillSphereKm, double innerRocheLimit,
                            double outerRocheLimit, String predeterminedMoonType,
                            double moonMassEarth) {
        Moon moon = new Moon();

        moon.setPlanet(planet);
        moon.setAgeMY(planet.getAgeMY());

        moon.setMoonType(predeterminedMoonType);
        setFormationType(moon, predeterminedMoonType); // New helper method
        moon.setCompositionType(planet.getSurfaceTemp() < 150 ? "ICY" : "MIXED");

        generatePhysicalProperties(moon, moonMassEarth);
        generateOrbitalProperties(moon, planet, hillSphereKm, innerRocheLimit, outerRocheLimit);
        calculateDerivedProperties(moon, planet, primaryStar);
        calculateTidalEffects(moon, planet);
        determineGeologicalActivity(moon, planet);
        generateSurfaceFeatures(moon);
        generateAtmosphere(moon);

        return moon;
    }

    private void setFormationType(Moon moon, String moonType) {
        switch (moonType) {
            case "COLLISION_DEBRIS" -> moon.setFormationType("COLLISION_DEBRIS");
            case "IRREGULAR_CAPTURED", "SHEPHERD", "TROJAN" -> moon.setFormationType("CAPTURED");
            default -> moon.setFormationType("CO_FORMED");
        }
    }

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

    private List<MoonGenerationData> distributeMoonMassesAndTypes(int numMoons, double totalMassBudget, Planet planet) {
        List<MoonGenerationData> moonData = new ArrayList<>();

        List<String> moonTypes = new ArrayList<>();
        for (int i = 0; i < numMoons; i++) {
            moonTypes.add(determineMoonTypeString(planet));
        }

        Map<String, MoonTypeRef> typeRefMap = new HashMap<>();
        List<String> sortedTypes = new ArrayList<>(moonTypes);

        for (String type : moonTypes) {
            if (!typeRefMap.containsKey(type)) {
                moonTypeRefRepository.findByMoonType(type).ifPresent(ref ->
                        typeRefMap.put(type, ref));
            }
        }

        sortedTypes.sort((a, b) -> {
            MoonTypeRef refA = typeRefMap.get(a);
            MoonTypeRef refB = typeRefMap.get(b);
            if (refA == null || refB == null) return 0;
            return Integer.compare(refA.getMassDistributionPriority(),
                    refB.getMassDistributionPriority());
        });

        double[] weights = new double[numMoons];
        double totalWeight = 0;

        for (int i = 0; i < numMoons; i++) {
            weights[i] = 1.0 / Math.pow(i + 1, 1.5);
            totalWeight += weights[i];
        }

        for (int i = 0; i < numMoons; i++) {
            String moonType = sortedTypes.get(i);
            double massShare = (weights[i] / totalWeight) * totalMassBudget;
            moonData.add(new MoonGenerationData(moonType, massShare));
        }

        return moonData;
    }

    private String determineMoonTypeString(Planet planet) {
        double rand = RandomUtils.rollRange(0.0, 1.0);

        if (planet.getPlanetType().contains("Gas Giant") ||
                planet.getPlanetType().contains("Ice Giant") ||
                planet.getPlanetType().contains("Jupiter")) {

            if (rand < 0.5) return "REGULAR_LARGE";
            else if (rand < 0.75) return "REGULAR_MEDIUM";
            else if (rand < 0.9) return "REGULAR_SMALL";
            else return "IRREGULAR_CAPTURED";

        } else {
            if (rand < 0.4 && planet.getEarthMass() > 0.5) return "COLLISION_DEBRIS";
            else if (rand < 0.7) return "REGULAR_SMALL";
            else return "IRREGULAR_CAPTURED";
        }
    }

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

        double budgetFactor = totalMassBudget / (planet.getEarthMass() * 0.001); // Ratio to 0.1% planet mass

        int targetMoons;
        if (budgetFactor < 0.1) {
            targetMoons = RandomUtils.rollRange(minMoons, minMoons + (effectiveMax - minMoons) / 3);
        } else if (budgetFactor < 0.5) {
            targetMoons = RandomUtils.rollRange(minMoons + (effectiveMax - minMoons) / 3,
                    minMoons + 2 * (effectiveMax - minMoons) / 3);
        } else {
            targetMoons = RandomUtils.rollRange(minMoons + (effectiveMax - minMoons) / 2, effectiveMax);
        }

        double metallicity = primaryStar.getMetallicity();
        if (metallicity > 0.3 && RandomUtils.rollRange(0.0, 1.0) < 0.3) {
            targetMoons = Math.min(effectiveMax, targetMoons + 1);
        } else if (metallicity < -0.3 && RandomUtils.rollRange(0.0, 1.0) < 0.3) {
            targetMoons = Math.max(minMoons, targetMoons - 1);
        }

        return targetMoons;
    }

    private void generatePhysicalProperties(Moon moon, double moonMassEarth) {
        moon.setEarthMass(moonMassEarth);
        moon.setMass(moonMassEarth * EARTH_MASS_KG);

        double density = "ICY".equals(moon.getCompositionType()) ?
                RandomUtils.rollRange(0.9, 1.8) :
                RandomUtils.rollRange(2.5, 3.5);
        moon.setDensity(density);

        double radiusKm = Math.cbrt((3 * moonMassEarth * EARTH_MASS_KG * 1000) /
                (4 * Math.PI * density * 1000)) / 1000;
        moon.setEarthRadius(radiusKm / EARTH_RADIUS_KM);
        moon.setRadius(radiusKm);
        moon.setCircumference(2 * Math.PI * radiusKm);

        double albedo = "ICY".equals(moon.getCompositionType()) ?
                RandomUtils.rollRange(0.5, 0.9) :
                RandomUtils.rollRange(0.1, 0.3);
        moon.setAlbedo(albedo);
    }

    private void generateOrbitalProperties(Moon moon, Planet planet, double hillSphereKm, double innerRocheLimit, double outerRocheLimit) {

        double rocheLimit = "ICY".equals(moon.getCompositionType()) ?
                outerRocheLimit : innerRocheLimit;

        double minOrbitKm = rocheLimit * 1.5;
        double maxOrbitKm = hillSphereKm * 0.5;

        double semiMajorAxisKm;
        if ("IRREGULAR_CAPTURED".equals(moon.getMoonType())) {
            semiMajorAxisKm = RandomUtils.rollRange(maxOrbitKm * 0.3, maxOrbitKm);
        } else {
            semiMajorAxisKm = RandomUtils.rollRange(minOrbitKm, maxOrbitKm * 0.3);
        }

        moon.setSemiMajorAxisKm(semiMajorAxisKm);

        double eccentricity = "IRREGULAR_CAPTURED".equals(moon.getMoonType()) ?
                RandomUtils.rollRange(0.1, 0.5) :
                RandomUtils.rollRange(0.0, 0.1);
        moon.setEccentricity(eccentricity);

        double inclination = "IRREGULAR_CAPTURED".equals(moon.getMoonType()) ?
                RandomUtils.rollRange(10, 60) :
                RandomUtils.rollRange(0, 5);
        moon.setOrbitalInclinationDegrees(inclination);

        double periodSeconds = 2 * Math.PI * Math.sqrt(
                Math.pow(semiMajorAxisKm * 1000, 3) /
                        (ConversionFormulas.GRAVITATIONAL_CONSTANT * planet.getMass())
        );
        double periodDays = periodSeconds / (24 * 3600);
        moon.setOrbitalPeriodDays(periodDays);

        moon.setTidallyLocked(!("IRREGULAR_CAPTURED".equals(moon.getMoonType()) && RandomUtils.rollRange(0.0, 1.0) < 0.7));

        if (moon.getTidallyLocked()) {
            moon.setRotationPeriodHours(periodDays * 24);
        } else {
            moon.setRotationPeriodHours(RandomUtils.rollRange(10.0, 100));
        }

        moon.setAxialTilt(RandomUtils.rollRange(0.0, 25));
        moon.setHillSphereRadiusKm(hillSphereKm);
        moon.setRocheLimitKm(rocheLimit);

        if (semiMajorAxisKm < rocheLimit * 1.2) {
            moon.setOrbitStability("UNSTABLE");
        } else if (semiMajorAxisKm > hillSphereKm * 0.4) {
            moon.setOrbitStability("MARGINALLY_STABLE");
        } else {
            moon.setOrbitStability("STABLE");
        }
    }

    private void calculateDerivedProperties(Moon moon, Planet planet, Star primaryStar) {
        double radiusKm = moon.getRadius();
        double massKg = moon.getMass();

        double surfaceGravity = (ConversionFormulas.GRAVITATIONAL_CONSTANT * massKg) /
                Math.pow(radiusKm * 1000, 2);
        moon.setSurfaceGravity(surfaceGravity);

        double escapeVelocity = Math.sqrt((2 * ConversionFormulas.GRAVITATIONAL_CONSTANT * massKg) /
                (radiusKm * 1000)) / 1000;
        moon.setEscapeVelocity(escapeVelocity);

        double distanceFromStarAU = planet.getSemiMajorAxisAU();
        double stellarLuminosity = primaryStar.getSolarLuminosity();
        if (primaryStar.getSystem() != null && primaryStar.getSystem().getBinaryConfiguration() == BinaryConfiguration.P_TYPE) {
            stellarLuminosity = primaryStar.getSystem().getStars().stream()
                    .mapToDouble(Star::getSolarLuminosity)
                    .sum();
        }
        double albedo = moon.getAlbedo();

        double baseTemp = 278.5 * Math.pow(stellarLuminosity, 0.25)
                / Math.sqrt(distanceFromStarAU)
                * Math.pow(1.0 - albedo, 0.25);

        moon.setSurfaceTemp(Math.max(baseTemp, 10.0));
    }

    private void calculateTidalEffects(Moon moon, Planet planet) {

        double gravityFactor = (planet.getEarthMass() / 1.0) * Math.pow(100000.0 / moon.getSemiMajorAxisKm(), 3);
        double eccentricityFactor = moon.getEccentricity() * moon.getEccentricity() * 100;
        double sizeFactor = Math.pow(moon.getRadius() / 1000.0, 2);

        double tidalHeatingWattPerM2 = gravityFactor * eccentricityFactor * sizeFactor * 0.01;

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
            double tidalTempBoost = Math.sqrt(tidalHeatingWattPerM2) * 30.0; // Rough approximation
            moon.setSurfaceTemp(moon.getSurfaceTemp() + tidalTempBoost);
        }
    }

    private void determineGeologicalActivity(Moon moon, Planet planet) {
        String tidalHeating = moon.getTidalHeatingLevel();
        Double tidalHeatingWattPerM2 = moon.getTidalHeatingWattPerM2();

        boolean hasSignificantTidalHeating = tidalHeatingWattPerM2 != null && tidalHeatingWattPerM2 > 0.5;

        double ageModifier = hasSignificantTidalHeating ? 1.0 : (1.0 / (1.0 + moon.getAgeMY() / 5000.0));
        double activityScore = moon.getEarthMass() * 100 * ageModifier;

        if (hasSignificantTidalHeating) {
            activityScore += tidalHeatingWattPerM2 * 10;
        }

        if (activityScore > 10 || "EXTREME".equals(tidalHeating)) {
            moon.setGeologicalActivity("HIGH");
            moon.setHasCryovolcanism("ICY".equals(moon.getCompositionType()));
        } else if (activityScore > 5 || "HIGH".equals(tidalHeating)) {
            moon.setGeologicalActivity("MODERATE");
            moon.setHasCryovolcanism("ICY".equals(moon.getCompositionType()) && RandomUtils.rollRange(0.0, 1.0) < 0.6);
        } else if (activityScore > 2 || "MODERATE".equals(tidalHeating)) {
            moon.setGeologicalActivity("LOW");
            if ("ICY".equals(moon.getCompositionType()) && tidalHeatingWattPerM2 != null && tidalHeatingWattPerM2 > 0.8) {
                moon.setHasCryovolcanism(RandomUtils.rollRange(0.0, 1.0) < 0.3);
            }
        } else {
            moon.setGeologicalActivity("NONE");
        }

        if ("ICY".equals(moon.getCompositionType()) && hasSignificantTidalHeating) {
            double oceanChance = tidalHeatingWattPerM2 > 1.0 ? 0.9 : 0.6;
            moon.setHasSubsurfaceOcean(RandomUtils.rollRange(0.0, 1.0) < oceanChance);

            if (moon.getHasSubsurfaceOcean()) {
                double baseDepth = 50.0 + (tidalHeatingWattPerM2 * 50);
                moon.setOceanDepthKm(RandomUtils.rollRange(baseDepth, baseDepth + 150));

                double maxShellThickness = Math.max(10.0, 80.0 - (tidalHeatingWattPerM2 * 20));
                moon.setIceShellThicknessKm(RandomUtils.rollRange(5.0, maxShellThickness));
            }
        }
    }

    private void generateSurfaceFeatures(Moon moon) {
        double age = moon.getAgeMY();
        String activity = moon.getGeologicalActivity();

        if ("NONE".equals(activity)) {
            if (age > 3000) {
                moon.setCrateringLevel("EXTREME");
                moon.setEstimatedVisibleCraters(RandomUtils.rollRange(100000, 500000));
            } else {
                moon.setCrateringLevel("HEAVY");
                moon.setEstimatedVisibleCraters(RandomUtils.rollRange(50000, 150000));
            }
        } else if ("LOW".equals(activity)) {
            moon.setCrateringLevel("MODERATE");
            moon.setEstimatedVisibleCraters(RandomUtils.rollRange(10000, 50000));
        } else {
            moon.setCrateringLevel("LIGHT");
            moon.setEstimatedVisibleCraters(RandomUtils.rollRange(1000, 10000));
        }

        List<String> features = getFeatures(moon);
        moon.setSurfaceFeatures(String.join(", ", features));
    }

    private static List<String> getFeatures(Moon moon) {
        List<String> features = new ArrayList<>();

        if (moon.getHasCryovolcanism()) {
            features.add("Cryovolcanic plumes");
            features.add("Ice geysers");
        }

        if (moon.getHasSubsurfaceOcean()) {
            features.add("Subsurface ocean");
            features.add("Tectonic stress patterns");
        }

        if ("EXTREME".equals(moon.getCrateringLevel())) {
            features.add("Ancient impact basins");
        }

        if ("ICY".equals(moon.getCompositionType())) {
            features.add("Water ice plains");
        }
        return features;
    }

    private void generateAtmosphere(Moon moon) {
        Planet planet = moon.getPlanet();

        if (moon.getEscapeVelocity() < 0.3) {
            moon.setHasAtmosphere(false);
            moon.setSurfacePressure(0.0);
            moon.setAtmosphereComposition("None");
            moon.setAtmosphere(null);
            return;
        }

        boolean strippedByMagneticField = checkMagneticFieldStripping(moon, planet);

        if (strippedByMagneticField) {
            Atmosphere strippedAtmosphere = createStrippedAtmosphere(
            );
            moon.setAtmosphere(strippedAtmosphere);
            moon.setHasAtmosphere(false);
            moon.setSurfacePressure(0.0);
            moon.setAtmosphereComposition("None (stripped by magnetic field)");
            return;
        }

        boolean shouldHaveAtmosphere = determineAtmospherePresence(moon);

        if (!shouldHaveAtmosphere) {
            moon.setHasAtmosphere(false);
            moon.setSurfacePressure(0.0);
            moon.setAtmosphereComposition("None");
            return;
        }

        // Generate atmosphere using AtmosphereCreator
        String moonType = determineMoonTypeForAtmosphere(moon);
        double surfaceTemp = moon.getSurfaceTemp();
        double earthMass = moon.getEarthMass();
        double distanceFromStarAU = planet.getSemiMajorAxisAU(); // Use planet's distance

        AtmosphereCreator.AtmosphereResult result = atmosphereCreator.generateAtmosphereWithTemplate(
                moonType,
                surfaceTemp,
                earthMass,
                distanceFromStarAU
        );

        PlanetaryAtmosphere generatedAtmosphere = result.atmosphere();
        if (generatedAtmosphere.getClassification() == AtmosphereClassification.NONE) {
            moon.setHasAtmosphere(false);
            moon.setSurfacePressure(0.0);
            moon.setAtmosphereComposition("None");
            moon.setAtmosphere(null);
            return;
        }

        double surfacePressure = atmosphereCreator.calculateSurfacePressure(
                earthMass,
                surfaceTemp,
                result.template()
        );

        // Convert PlanetaryAtmosphere to Atmosphere entity
        Atmosphere atmosphereEntity = convertToAtmosphereEntity(generatedAtmosphere, surfacePressure);

        moon.setAtmosphere(atmosphereEntity);
        moon.setHasAtmosphere(true);
        moon.setSurfacePressure(surfacePressure);
        moon.setAtmosphereComposition(generatedAtmosphere.toCompactString());
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

    private Atmosphere createStrippedAtmosphere() {
        return Atmosphere.builder()
                .classification("NONE")
                .surfacePressureBar(0.0)
                .compositionSummary("None")
                .isStripped(true)
                .strippedReason("Atmosphere stripped by parent planet's magnetosphere")
                .components(new java.util.ArrayList<>())
                .build();
    }

    private Atmosphere convertToAtmosphereEntity(PlanetaryAtmosphere planetaryAtm, double pressureBar) {
        Atmosphere atmosphere = Atmosphere.builder()
                .classification(planetaryAtm.getClassification().name())
                .surfacePressureBar(pressureBar)
                .compositionSummary(planetaryAtm.toCompactString())
                .isStripped(false)
                .components(new java.util.ArrayList<>())
                .build();

        for (AtmosphereComponent component : planetaryAtm.components()) {
            component.setAtmosphere(atmosphere);
            atmosphere.getComponents().add(component);
        }

        return atmosphere;
    }

    private double calculateHillSphere(Planet planet, Star primaryStar) {
        double semiMajorAxisKm = planet.getSemiMajorAxisAU() * ConversionFormulas.AU_TO_KM;
        return semiMajorAxisKm * Math.cbrt(planet.getMass() / (3 * primaryStar.getMass()));
    }

    private double calculateRocheLimit(Planet planet, double moonDensity) {
        return  2.46 * planet.getRadius() * Math.cbrt(planet.getDensity() / moonDensity);
    }

    private record MoonGenerationData(String moonType, double massEarthMasses) {
    }
}