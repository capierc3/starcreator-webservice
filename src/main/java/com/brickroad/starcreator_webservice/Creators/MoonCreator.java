package com.brickroad.starcreator_webservice.Creators;

import com.brickroad.starcreator_webservice.model.moons.Moon;
import com.brickroad.starcreator_webservice.model.moons.MoonTypeRef;
import com.brickroad.starcreator_webservice.model.planets.Planet;
import com.brickroad.starcreator_webservice.model.planets.PlanetTypeRef;
import com.brickroad.starcreator_webservice.model.stars.Star;
import com.brickroad.starcreator_webservice.repos.MoonTypeRefRepository;
import com.brickroad.starcreator_webservice.utils.ConversionFormulas;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MoonCreator {

    private final MoonTypeRefRepository moonTypeRefRepository;


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

        double stellarContribution = planet.getSurfaceTemp() * 0.3; // Some fraction from parent planet
        double distanceFactor = 1.0 / Math.sqrt(moon.getSemiMajorAxisKm() / 100000.0);
        distanceFactor = Math.min(1.0, distanceFactor);

        double albedoFactor = 1.0 - (moon.getAlbedo() * 0.3);
        double baseTemp = stellarContribution * distanceFactor * albedoFactor;

        moon.setSurfaceTemp(baseTemp);
    }

    private void calculateTidalEffects(Moon moon, Planet planet) {

        double proximityFactor = Math.pow(300000.0 / moon.getSemiMajorAxisKm(), 3);
        double eccentricityFactor = moon.getEccentricity() * moon.getEccentricity() * 100;
        double sizeFactor = moon.getRadius() / 1000.0;

        double tidalHeatingWattPerM2 = proximityFactor * eccentricityFactor * sizeFactor * 0.1;

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
    }

    private void determineGeologicalActivity(Moon moon, Planet planet) {
        String tidalHeating = moon.getTidalHeatingLevel();
        double mass = moon.getEarthMass();
        double age = moon.getAgeMY();
        double planetMass = planet.getEarthMass();

        double planetTidalFactor = Math.min(2.0, planetMass / 100.0);
        double activityScore = (mass * 1000 * planetTidalFactor) / (age + 100);

        if ("EXTREME".equals(tidalHeating) && activityScore > 0.1) {
            moon.setGeologicalActivity("HIGH");
            moon.setHasCryovolcanism("ICY".equals(moon.getCompositionType()));
        } else if ("HIGH".equals(tidalHeating) && activityScore > 0.05) {
            moon.setGeologicalActivity("MODERATE");
            moon.setHasCryovolcanism("ICY".equals(moon.getCompositionType()) && RandomUtils.rollRange(0.0, 1.0) < 0.5);
        } else if (mass > 0.005 || "MODERATE".equals(tidalHeating)) {
            moon.setGeologicalActivity("LOW");
        } else {
            moon.setGeologicalActivity("NONE");
        }

        if ("ICY".equals(moon.getCompositionType()) &&
                (tidalHeating.equals("MODERATE") || tidalHeating.equals("HIGH") || tidalHeating.equals("EXTREME"))) {
            moon.setHasSubsurfaceOcean(RandomUtils.rollRange(0.0, 1.0) < 0.7);
            if (moon.getHasSubsurfaceOcean()) {
                moon.setOceanDepthKm(RandomUtils.rollRange(50.0, 200));
                moon.setIceShellThicknessKm(RandomUtils.rollRange(5.0, 50));
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
        if (moon.getEarthMass() > 0.01 ||
                "HIGH".equals(moon.getGeologicalActivity())) {
            if (RandomUtils.rollRange(0.0, 1.0) < 0.2) {
                moon.setHasAtmosphere(true);
                moon.setSurfacePressure(RandomUtils.rollRange(0.0001, 0.01));

                if (moon.getHasCryovolcanism()) {
                    moon.setAtmosphereComposition("N2 60%, CH4 30%, CO 10%");
                } else {
                    moon.setAtmosphereComposition("N2 80%, O2 15%, trace gases");
                }
            }
        }

        if (!moon.getHasAtmosphere()) {
            moon.setSurfacePressure(0.0);
            moon.setAtmosphereComposition("None");
        }
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