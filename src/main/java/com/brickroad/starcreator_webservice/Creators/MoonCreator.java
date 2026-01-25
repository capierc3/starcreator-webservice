package com.brickroad.starcreator_webservice.Creators;

import com.brickroad.starcreator_webservice.model.moons.Moon;
import com.brickroad.starcreator_webservice.model.planets.Planet;
import com.brickroad.starcreator_webservice.model.stars.Star;
import com.brickroad.starcreator_webservice.utils.ConversionFormulas;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MoonCreator {

    private static final MathContext MC = new MathContext(34, RoundingMode.HALF_UP);

    private static final double EARTH_MASS_KG = 5.972e24;
    private static final double EARTH_RADIUS_KM = 6371.0;

    public List<Moon> createMoons(Planet planet, Star primaryStar) {
        List<Moon> moons = new ArrayList<>();

        int numMoons = determineNumberOfMoons(planet, primaryStar);
        if (numMoons == 0) {
            return moons;
        }

        double hillSphereKm = calculateHillSphere(planet, primaryStar);
        double innerRocheLimit = calculateRocheLimit(planet, 3.3);
        double outerRocheLimit = calculateRocheLimit(planet, 1.0);

        for (int i = 0; i < numMoons; i++) {
            Moon moon = createMoon(planet, primaryStar, i + 1, hillSphereKm, innerRocheLimit, outerRocheLimit);
            moons.add(moon);
        }

        return moons;
    }

    private int determineNumberOfMoons(Planet planet, Star primaryStar) {
        String planetType = planet.getPlanetType();
        double mass = planet.getEarthMass();
        double metallicity = primaryStar.getMetallicity();

        double metallicityModifier = 1.0 + (metallicity * 0.3);
        if (planetType.contains("Gas Giant") || planetType.contains("Ice Giant")) {
            int baseMoons;
            if (mass > 100) {
                baseMoons = RandomUtils.rollRange(20, 80);
            } else if (mass > 10) {
                baseMoons = RandomUtils.rollRange(10, 40);
            } else {
                baseMoons = RandomUtils.rollRange(4, 15);
            }
            return Math.max(1, (int) (baseMoons * metallicityModifier));

        } else if (planetType.contains("Super-Earth") || planetType.contains("Ocean World")) {
            double moonChance = Math.min(0.9, (mass * 0.1) + (metallicity * 0.2));

            if (RandomUtils.rollRange(0.0, 1.0) < moonChance) {
                int baseMoons = RandomUtils.rollRange(1, 4);
                if (metallicity > 0.5 && RandomUtils.rollRange(0.0, 1.0) < 0.3) {
                    baseMoons++;
                }
                return baseMoons;
            }

        } else if (planetType.contains("Terrestrial")) {
            double moonChance = Math.min(0.8, (mass * 0.12) + (metallicity * 0.15));

            if (RandomUtils.rollRange(0.0, 1.0) < moonChance) {
                int baseMoons = RandomUtils.rollRange(1, 3);
                if (metallicity > 0.4 && RandomUtils.rollRange(0.0, 1.0) < 0.25) {
                    baseMoons++;
                }
                return baseMoons;
            }

        } else if (planetType.contains("Iron") || planetType.contains("Lava")) {
            double captureChance = 0.05 + (metallicity * 0.1);
            if (RandomUtils.rollRange(0.0, 1.0) < captureChance) {
                return 1;
            }
        }

        return 0;
    }

    private Moon createMoon(Planet planet, Star primaryStar, int moonNumber,
                            double hillSphereKm, double innerRocheLimit,
                            double outerRocheLimit) {
        Moon moon = new Moon();

        moon.setPlanet(planet);
        moon.setName(planet.getName() + "-" + romanNumeral(moonNumber));
        moon.setAgeMY(planet.getAgeMY());

        determineMoonType(moon, planet);
        generatePhysicalProperties(moon, planet);
        generateOrbitalProperties(moon, planet, hillSphereKm, innerRocheLimit, outerRocheLimit);
        calculateDerivedProperties(moon, planet, primaryStar);
        calculateTidalEffects(moon, planet);
        determineGeologicalActivity(moon, planet);
        generateSurfaceFeatures(moon);
        generateAtmosphere(moon);



        return moon;
    }

    private void determineMoonType(Moon moon, Planet planet) {
        double rand = RandomUtils.rollRange(0.0, 1.0);

        if (planet.getPlanetType().contains("Gas Giant") || planet.getPlanetType().contains("Ice Giant")) {
            if (rand < 0.5) {
                moon.setMoonType("REGULAR_LARGE");
                moon.setFormationType("CO_FORMED");
            } else if (rand < 0.75) {
                moon.setMoonType("REGULAR_MEDIUM");
                moon.setFormationType("CO_FORMED");
            } else if (rand < 0.9) {
                moon.setMoonType("REGULAR_SMALL");
                moon.setFormationType("CO_FORMED");
            } else {
                moon.setMoonType("IRREGULAR_CAPTURED");
                moon.setFormationType("CAPTURED");
            }
        } else {
            if (rand < 0.4 && planet.getEarthMass() > 0.5) {
                moon.setMoonType("COLLISION_DEBRIS");
                moon.setFormationType("COLLISION_DEBRIS");
            } else if (rand < 0.7) {
                moon.setMoonType("REGULAR_SMALL");
                moon.setFormationType("CO_FORMED");
            } else {
                moon.setMoonType("IRREGULAR_CAPTURED");
                moon.setFormationType("CAPTURED");
            }
        }
        moon.setCompositionType(planet.getSurfaceTemp() < 150 ? "ICY" : "MIXED");
    }

    //TODO Does not use planet
    private void generatePhysicalProperties(Moon moon, Planet planet) {

        double massEarth = switch (moon.getMoonType()) {
            case "REGULAR_LARGE" -> RandomUtils.rollRange(0.01, 0.025);
            case "REGULAR_MEDIUM" -> RandomUtils.rollRange(0.001, 0.01);
            case "REGULAR_SMALL" -> RandomUtils.rollRange(0.00001, 0.001);
            case "COLLISION_DEBRIS" -> RandomUtils.rollRange(0.005, 0.015);
            default -> RandomUtils.rollRange(0.0000001, 0.0001);
        };

        moon.setEarthMass(massEarth);
        moon.setMass(massEarth * EARTH_MASS_KG);

        double density = "ICY".equals(moon.getCompositionType()) ?
                RandomUtils.rollRange(0.9, 1.8) :
                RandomUtils.rollRange(2.5, 3.5);
        moon.setDensity(density);

        double radiusKm = Math.cbrt((3 * massEarth * EARTH_MASS_KG * 1000) /
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

        double baseTempFromPlanet = planet.getSurfaceTemp();
        double distanceFromPlanetKm = moon.getSemiMajorAxisKm();

        double tempModifier = Math.max(0, 1 - (distanceFromPlanetKm / 100000));
        double surfaceTemp = baseTempFromPlanet * 0.7 + (tempModifier * 20);

        moon.setSurfaceTemp(surfaceTemp);
    }

    private void calculateTidalEffects(Moon moon, Planet planet) {
        double semiMajorAxis = moon.getSemiMajorAxisKm() * 1000;
        double eccentricity = moon.getEccentricity();

        double tidalHeatingFactor = (eccentricity * eccentricity *
                Math.pow(ConversionFormulas.GRAVITATIONAL_CONSTANT * planet.getMass(), 2) *
                Math.pow(moon.getRadius() * 1000, 5)) /
                Math.pow(semiMajorAxis, 6);

        double tidalHeatingWattPerM2 = tidalHeatingFactor * 1e15; // Scaling factor

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

        moon.setTidalHeatingWattPerM2(tidalHeatingWattPerM2);

        if (tidalHeatingWattPerM2 > 0.1) {
            double tempIncrease = tidalHeatingWattPerM2 * 10;
            moon.setSurfaceTemp(moon.getSurfaceTemp() + tempIncrease);
        }
    }

    //TODO Does not use planet
    private void determineGeologicalActivity(Moon moon, Planet planet) {
        String tidalHeating = moon.getTidalHeatingLevel();
        double mass = moon.getEarthMass();

        if ("EXTREME".equals(tidalHeating) || "HIGH".equals(tidalHeating)) {
            moon.setGeologicalActivity("HIGH");
            moon.setHasCryovolcanism("ICY".equals(moon.getCompositionType()));
        } else if ("MODERATE".equals(tidalHeating) && mass > 0.001) {
            moon.setGeologicalActivity("MODERATE");
            moon.setHasCryovolcanism("ICY".equals(moon.getCompositionType()) && RandomUtils.rollRange(0.0, 1.0) < 0.5);
        } else if (mass > 0.005) {
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

    private String romanNumeral(int number) {
        String[] romanNumerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X",
                "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX",
                "XXI", "XXII", "XXIII", "XXIV", "XXV", "XXVI", "XXVII", "XXVIII", "XXIX", "XXX",
                "XXXI", "XXXII", "XXXIII", "XXXIV", "XXXV", "XXXVI", "XXXVII", "XXXVIII", "XXXIX", "XL",
                "XLI", "XLII", "XLIII", "XLIV", "XLV", "XLVI", "XLVII", "XLVIII", "XLIX", "L"};

        return number <= 50 ? romanNumerals[number - 1] : String.valueOf(number);
    }
}