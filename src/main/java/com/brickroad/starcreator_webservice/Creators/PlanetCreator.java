package com.brickroad.starcreator_webservice.Creators;


import com.brickroad.starcreator_webservice.model.planets.*;
import com.brickroad.starcreator_webservice.model.enums.BinaryConfiguration;
import com.brickroad.starcreator_webservice.model.stars.Star;
import com.brickroad.starcreator_webservice.repos.PlanetTypeRefRepository;
import com.brickroad.starcreator_webservice.utils.ConversionFormulas;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import com.brickroad.starcreator_webservice.utils.TemperatureCalculator;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlanetCreator {

    @Autowired
    private PlanetTypeRefRepository planetTypeRefRepository;

    @Autowired
    private AtmosphereCreator atmosphereCreator;

    @Autowired
    private CompositionCreator compositionCreator;

    @Autowired
    private GeologyCreator geologyCreator;

    private List<PlanetTypeRef> cachedPlanetTypes;
    private static final double VARIANCE = 0.15;

    private static final double EARTH_MASS_KG = 5.972e24;
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double GRAVITATIONAL_CONSTANT = 6.674e-11;

    @PostConstruct
    public void init() {
        cachedPlanetTypes = planetTypeRefRepository.findAllPlanetTypes();
    }

    public Planet generateRandomPlanet() {
        PlanetTypeRef type = selectPlanetTypeByRarity();
        return generatePlanetByType(type, null, 1, 1.0);
    }

    public Planet generatePlanetByType(PlanetTypeRef type, Star parentStar, int orbitalPosition, double distanceAU) {
        Planet planet = new Planet();

        double earthMass = RandomUtils.rollRange(type.getMinMassEarth(), type.getMaxMassEarth());
        double earthRadius = calculateRadius(earthMass, type);
        earthRadius = addVariance(earthRadius);

        if (parentStar != null) {
            populateOrbitalParameters(planet, parentStar, distanceAU, orbitalPosition);
        } else {
            planet.setSemiMajorAxisAU(distanceAU);
            planet.setOrbitalPeriodDays(calculateOrbitalPeriod(distanceAU, 1.0)); // Assume 1 solar mass
        }

        populatePlanet(planet, type, earthMass, earthRadius, parentStar);

        return planet;
    }

    public List<Planet> generatePlanetarySystem(Star parentStar) {
        List<Planet> planets = new ArrayList<>();
        HabitableZone hz = new HabitableZone(parentStar.getHabitableZoneInnerAU(), parentStar.getHabitableZoneOuterAU());

        int numPlanets = determineNumberOfPlanets(parentStar);
        double frostLine = calculateFrostLine(parentStar);

        double maxSystemDistance = getMaxSystemDistance(parentStar);
        double currentDistance = RandomUtils.rollRange(0.1, 0.5);
        for (int i = 0; i < numPlanets; i++) {
            PlanetTypeRef type = selectPlanetTypeByDistance(currentDistance, frostLine, hz);

            Planet planet = generatePlanetByType(type, parentStar, i + 1, currentDistance);
            planets.add(planet);

            currentDistance = calculateNextOrbitDistance(currentDistance, i, numPlanets, maxSystemDistance);
        }

        return planets;
    }

    private static double getMaxSystemDistance(Star parentStar) {
        double maxSystemDistance = 50.0;
        if (parentStar.getSystem() != null && parentStar.getSystem().getSizeAu() != null) {
            maxSystemDistance = parentStar.getSystem().getSizeAu();

            BinaryConfiguration config = parentStar.getSystem().getBinaryConfiguration();
            if (config == BinaryConfiguration.S_TYPE_WIDE) {
                Double binarySep = parentStar.getSystem().getBinarySeparationAu();
                if (binarySep != null) {
                    maxSystemDistance = Math.min(maxSystemDistance, binarySep * 0.3);
                }
            }
        }
        return maxSystemDistance;
    }

    private void populatePlanet(Planet planet, PlanetTypeRef type, double earthMass, double earthRadius, Star parentStar) {

        planet.setPlanetType(type.getName());

        if (parentStar != null) {
            planet.setAgeMY(parentStar.getAgeMY() + RandomUtils.rollRange(-100, 100));
        } else {
            planet.setAgeMY(RandomUtils.rollRange(100.0, 10000));
        }

        planet.setEarthMass(earthMass);
        planet.setEarthRadius(earthRadius);

        planet.setMass(earthMass * EARTH_MASS_KG);
        planet.setRadius(earthRadius * EARTH_RADIUS_KM);
        planet.setCircumference(ConversionFormulas.radiusToCircumference(planet.getRadius()));

        planet.setDensity(calculateDensity(planet.getMass(), planet.getRadius()));
        planet.setSurfaceGravity(calculateSurfaceGravity(planet.getMass(), planet.getRadius()));
        planet.setEscapeVelocity(calculateEscapeVelocity(planet.getMass(), planet.getRadius()));

        if (planet.getEccentricity() == null) {
            planet.setEccentricity(RandomUtils.rollRange(0.0, 0.2));
        }
        if (planet.getOrbitalInclinationDegrees() == null) {
            planet.setOrbitalInclinationDegrees(RandomUtils.rollRange(0.0, 15.0));
        }

        populateRotationProperties(planet, type, parentStar);

        populateAlbedo(planet, type);

        if (parentStar != null && planet.getSemiMajorAxisAU() != null) {
            planet.setSurfaceTemp(TemperatureCalculator.calculatePlanetTemperature(parentStar, planet.getSemiMajorAxisAU(), planet.getAlbedo()));
        } else {
            planet.setSurfaceTemp(RandomUtils.rollRange(100.0, 400));
        }

        populateAtmosphereProperties(planet, type, parentStar);

        planet.setCoreType(type.getTypicalCoreType());
        populateCompositionProperties(planet);
        populateGeologicalProperties(planet);

        planet.setHasRings(type.getCanHaveRings() && Math.random() < type.getRingProbability());
        planet.setNumberOfMoons(calculateMoonAmount(type, parentStar));

        planet.setMagneticFieldStrength(calculateMagneticField(planet));

        if (parentStar != null) {
            HabitableZone hz = new HabitableZone(parentStar.getHabitableZoneInnerAU(), parentStar.getHabitableZoneOuterAU());
            planet.setHabitableZonePosition(determineHabitableZonePosition(
                    planet.getSemiMajorAxisAU(), hz
            ));
        }

        if (type.getHabitable() && "habitable_zone".equals(planet.getHabitableZonePosition())) {
            planet.setWaterCoveragePercent(RandomUtils.rollRange(10, 90.0));
        }

        planet.setCreatedAt(LocalDateTime.now());
        planet.setModifiedAt(LocalDateTime.now());
    }

    private int calculateMoonAmount(PlanetTypeRef type, Star parentStar) {
        int baseMoons = RandomUtils.rollRange(type.getMinMoons(), type.getMaxMoons());
        if (parentStar != null) {
            double metallicity = parentStar.getMetallicity();
            double moonFactor;
            if (metallicity < -0.5) {
                moonFactor = 0.5; // Metal-poor: fewer moons
            } else if (metallicity < 0.0) {
                moonFactor = 0.75; // Below solar
            } else if (metallicity < 0.3) {
                moonFactor = 1.0; // Solar to enriched
            } else {
                moonFactor = 1.2; // Metal-rich: more moons
            }
            baseMoons = (int) Math.round(baseMoons * moonFactor);
            baseMoons = Math.max(type.getMinMoons(), baseMoons); // At least the minimum
        }
        return baseMoons;
    }

    private void populateOrbitalParameters(Planet planet, Star star, double distanceAU, int position) {
        planet.setParentStar(star);
        planet.setOrbitalPosition(position);
        planet.setSemiMajorAxisAU(distanceAU);

        // Calculate orbital period using Kepler's third law
        double orbitalPeriod = calculateOrbitalPeriod(distanceAU, star.getSolarMass());
        planet.setOrbitalPeriodDays(orbitalPeriod);

        // Eccentricity (most orbits are fairly circular)
        planet.setEccentricity(RandomUtils.rollRange(0.0, 0.2));

        // Inclination (most planets in same plane)
        planet.setOrbitalInclinationDegrees(RandomUtils.rollRange(0.0, 10.0));
    }

    private void populateRotationProperties(Planet planet, PlanetTypeRef type, Star parentStar) {
        double rotationHours;

        if (parentStar != null && planet.getSemiMajorAxisAU() != null &&
                planet.getSemiMajorAxisAU() < 0.1) {
            planet.setTidallyLocked(true);
            rotationHours = planet.getOrbitalPeriodDays() * 24;
        } else {
            planet.setTidallyLocked(false);

            double baseRotation = getBaseRotation(planet, type);

            double tidalBrakingFactor = 1.0;
            if (parentStar != null && planet.getSemiMajorAxisAU() != null && planet.getAgeMY() != null) {
                double distanceFactor = Math.pow(planet.getSemiMajorAxisAU(), 2);
                double ageFactor = 1.0 + (planet.getAgeMY() / 5000.0);
                tidalBrakingFactor = Math.min(5.0, ageFactor / distanceFactor);
            }

            rotationHours = baseRotation * tidalBrakingFactor;
            rotationHours *= RandomUtils.rollRange(0.8, 1.2);

            double minRotation = getMinimumRotationPeriod(planet, type);
            rotationHours = Math.max(rotationHours, minRotation);

            rotationHours = Math.min(rotationHours, 2000.0);
        }

        planet.setRotationPeriodHours(rotationHours);

        if (type.getName().toLowerCase().contains("gas giant")) {
            planet.setAxialTilt(RandomUtils.rollRange(0, 30.0));
        } else {
            double tilt = RandomUtils.rollRange(0, 45);
            if (Math.random() < 0.05) {
                tilt = RandomUtils.rollRange(45, 120);
            }
            planet.setAxialTilt(tilt);
        }
    }

    private double getMinimumRotationPeriod(Planet planet, PlanetTypeRef type) {
        String planetType = type.getName().toLowerCase();
        double earthMass = planet.getEarthMass();

        if (planetType.contains("gas giant") || planetType.contains("hot jupiter") ||
                planetType.contains("super-jupiter")) {
            return 9.0;
        }
        if (planetType.contains("ice giant") || planetType.contains("mini-neptune") ||
                planetType.contains("sub-neptune")) {
            return 10.0;
        }
        if (planetType.contains("terrestrial") || planetType.contains("super-earth") ||
                planetType.contains("ocean world") || planetType.contains("desert")) {
            if (earthMass > 2.0) {
                return 3.0;
            }
            return 4.0;
        }
        if (planetType.contains("dwarf") || earthMass < 0.3) {
            return 1.0;
        }
        if (planetType.contains("ice world")) {
            return 2.0;
        }
        if (planetType.contains("lava") || planetType.contains("hot rocky")) {
            return 1.0;
        }
        return 2.0;
    }

    private static double getBaseRotation(Planet planet, PlanetTypeRef type) {
        double baseRotation;
        String typeName = type.getName().toLowerCase();

        if (typeName.contains("gas giant") || typeName.contains("ice giant")) {
            baseRotation = 8.0 + (planet.getEarthMass() / 100.0) * 5.0; // 8-13 hours typical
        } else if (typeName.contains("super-earth")) {
            baseRotation = 15.0 + (planet.getEarthMass() * 3.0); // 15-35 hours
        } else {
            baseRotation = 24.0 * Math.pow(planet.getEarthMass(), -0.25);
        }
        return baseRotation;
    }

    private void populateAtmosphereProperties(Planet planet, PlanetTypeRef type, Star parentStar) {
        if (!type.getCanHaveAtmosphere() || planet.getEarthMass() < 0.1) {
            planet.setAtmosphereComposition("None");
            planet.setAtmosphereClassification("NONE");
            planet.setSurfacePressure(0.0);
            planet.setAlbedo(type.getTypicalAlbedo());
            return;
        }

        PlanetaryAtmosphere atmosphere = atmosphereCreator.generateAtmosphere(
                planet.getPlanetType(),
                planet.getSurfaceTemp(),
                planet.getEarthMass(),
                planet.getSemiMajorAxisAU()
        );
        planet.setAtmosphereComposition(atmosphere.toCompactString());
        planet.setAtmosphereClassification(atmosphere.getClassification().name());

        double pressure = atmosphereCreator.calculateSurfacePressure(
                planet.getEarthMass(),
                planet.getSurfaceTemp(),
                atmosphere
        );
        planet.setSurfacePressure(pressure);

        double baseAlbedo = type.getTypicalAlbedo();
        baseAlbedo = switch (atmosphere.getClassification()) {
            case EARTH_LIKE -> 0.3;
            case VENUS_LIKE -> 0.75;
            case JOVIAN, ICE_GIANT -> 0.5;
            case NONE -> 0.1;
            default -> baseAlbedo;
        };

        planet.setAlbedo(addVariance(baseAlbedo));
    }

    private void populateAlbedo(Planet planet, PlanetTypeRef type) {
        if (!type.getCanHaveAtmosphere()) {
            planet.setAlbedo(type.getTypicalAlbedo());
            return;
        }
        double baseAlbedo = type.getTypicalAlbedo();
        planet.setAlbedo(addVariance(baseAlbedo));
    }

    private double calculateRadius(double mass, PlanetTypeRef type) {
        String typeName = type.getName().toLowerCase();
        double radius;

        if (typeName.contains("gas giant") || typeName.contains("puffy")) {
            if (mass < 100) {
                radius = Math.pow(mass / 100.0, 0.55) * type.getMinRadiusEarth();
            } else if (mass < 500) {
                radius = type.getMinRadiusEarth() + (mass - 100) / 400.0 *
                        (type.getMaxRadiusEarth() - type.getMinRadiusEarth());
            } else {
                radius = type.getMaxRadiusEarth() * (1.0 - (mass - 500) / 10000.0);
            }
        } else if (typeName.contains("ice")) {
            radius = Math.pow(mass, 0.27);
        } else if (typeName.contains("terrestrial") || typeName.contains("rocky") ||
                typeName.contains("super-earth")) {
            radius = Math.pow(mass, 0.27);
        } else {
            double minRadius = type.getMinRadiusEarth();
            double maxRadius = type.getMaxRadiusEarth();
            double massPosition = (mass - type.getMinMassEarth()) /
                    (type.getMaxMassEarth() - type.getMinMassEarth());
            radius = minRadius + Math.pow(massPosition, 0.27) * (maxRadius - minRadius);
        }

        radius = Math.max(type.getMinRadiusEarth(),
                Math.min(type.getMaxRadiusEarth(), radius));
        return addVariance(radius);
    }

    private double calculateDensity(double massKg, double RadiusKm) {
        double radiusM = RadiusKm* 1000;
        double volumeM3 = (4.0/3.0) * Math.PI * Math.pow(radiusM, 3);
        double densityKgM3 = massKg / volumeM3;
        return densityKgM3 / 1000.0;
    }

    private double calculateSurfaceGravity(double massKg, double radiusKm) {
        double radiusM = radiusKm * 1000;
        double gravityMS2 = (GRAVITATIONAL_CONSTANT * massKg) / (radiusM * radiusM);
        return gravityMS2 / 9.81;
    }

    private double calculateEscapeVelocity(double massKg, double radiusKm) {
        double radiusM = radiusKm * 1000;
        double velocityMS = Math.sqrt((2 * GRAVITATIONAL_CONSTANT * massKg) / radiusM);
        return velocityMS / 1000.0;
    }

    private double calculateOrbitalPeriod(double semiMajorAxisAU, double starMassSolar) {
        // Kepler's Third Law: T^2 = (4π^2 / GM) * a^3
        // Simplified for solar masses and AU: T (years) = sqrt(a^3 / M)

        double periodYears = Math.sqrt(Math.pow(semiMajorAxisAU, 3) / starMassSolar);
        return periodYears * 365.25; // Convert to days
    }

    private double calculateFrostLine(Star star) {
        return 2.7 * Math.sqrt(star.getSolarLuminosity());
    }

    private int determineNumberOfPlanets(Star star) {
        double mass = star.getSolarMass();
        double age = star.getAgeMY();
        double metallicity = star.getMetallicity();

        int basePlanets;
        if (mass > 2.0) {
            basePlanets = RandomUtils.rollRange(1, 4);
        } else if (mass > 1.0) {
            basePlanets = RandomUtils.rollRange(2, 8);
        } else {
            basePlanets = RandomUtils.rollRange(3, 10);
        }

        double metallicityFactor = getMetallicityFactor(metallicity);
        basePlanets = (int) Math.round(basePlanets * metallicityFactor);
        basePlanets = Math.max(1, basePlanets);

        if (age > 8000) {
            basePlanets = Math.max(1, basePlanets - RandomUtils.rollRange(0, 2));
        }

        return basePlanets;
    }

    private static double getMetallicityFactor(double metallicity) {
        double metallicityFactor;
        if (metallicity < -1.0) {
            metallicityFactor = 0.3;
        } else if (metallicity < -0.5) {
            metallicityFactor = 0.5;
        } else if (metallicity < 0.0) {
            metallicityFactor = 0.75;
        } else if (metallicity < 0.3) {
            metallicityFactor = 1.0;
        } else {
            metallicityFactor = 1.3;
        }
        return metallicityFactor;
    }

    private PlanetTypeRef selectPlanetTypeByDistance(double distanceAU, double frostLine, HabitableZone hz) {
        String zone;

        if (distanceAU < hz.innerEdge) {
            zone = "inner";
        } else if (distanceAU >= hz.innerEdge && distanceAU <= hz.outerEdge) {
            zone = "habitable";
        } else if (distanceAU > frostLine) {
            zone = "outer";
        } else {
            zone = "frost_line";
        }

        List<PlanetTypeRef> suitableTypes = cachedPlanetTypes.stream()
                .filter(type -> type.getFormationZone() == null ||
                        type.getFormationZone().equals(zone))
                .collect(Collectors.toList());

        List<PlanetTypeRef> distanceFilteredTypes = suitableTypes.stream()
                .filter(type -> {
                    if (type.getMinFormationDistanceAU() == null ||
                            type.getMaxFormationDistanceAU() == null) {
                        return true;
                    }

                    // Check if current distance is within allowed range
                    return distanceAU >= type.getMinFormationDistanceAU() &&
                            distanceAU <= type.getMaxFormationDistanceAU();
                })
                .collect(Collectors.toList());


        if (!distanceFilteredTypes.isEmpty()) {
            return selectFromList(distanceFilteredTypes);
        }
        if (!suitableTypes.isEmpty()) {
            return selectFromList(suitableTypes);
        }
        return selectPlanetTypeByRarity();
    }

    private double calculateNextOrbitDistance(double currentDistance, int planetIndex, int totalPlanets, double maxSystemDistance) {

        double remainingSpace = maxSystemDistance - currentDistance;
        int remainingPlanets = totalPlanets - planetIndex - 1;

        if (remainingPlanets <= 0) {
            return currentDistance * 1.5;
        }

        double targetSpacing = Math.pow(remainingSpace / currentDistance, 1.0 / (remainingPlanets + 1));
        targetSpacing = Math.max(1.3, Math.min(2.0, targetSpacing));
        double spacing = targetSpacing * RandomUtils.rollRange(0.85, 1.15);

        double nextDistance = currentDistance * spacing;
        if (nextDistance > maxSystemDistance * 0.9) {
            nextDistance = maxSystemDistance * RandomUtils.rollRange(0.85, 0.95);
        }
        return nextDistance;
    }

    private String determineHabitableZonePosition(double distanceAU, HabitableZone hz) {
        if (distanceAU < hz.innerEdge * 0.5) {
            return "too_hot";
        } else if (distanceAU < hz.innerEdge) {
            return "inner_edge";
        } else if (distanceAU >= hz.innerEdge && distanceAU <= hz.outerEdge) {
            return "habitable_zone";
        } else if (distanceAU <= hz.outerEdge * 1.5) {
            return "outer_edge";
        } else {
            return "too_cold";
        }
    }




    private double calculateMagneticField(Planet planet) {

        if (planet.getCoreType() == null || planet.getCoreType().contains("Ice")) {
            return 0.0;
        }

        double rotationFactor = 24.0 / (planet.getRotationPeriodHours() + 1);
        double massFactor = planet.getEarthMass();
        double fieldStrength = rotationFactor * massFactor * RandomUtils.rollRange(0.5, 2.0);

        return Math.max(0, fieldStrength);
    }

    private PlanetTypeRef selectPlanetTypeByRarity() {
        return selectFromList(cachedPlanetTypes);
    }

    private PlanetTypeRef selectFromList(List<PlanetTypeRef> types) {
        int totalWeight = types.stream()
                .mapToInt(PlanetTypeRef::getRarityWeight)
                .sum();

        int random = RandomUtils.rollRange(0, totalWeight);
        int currentWeight = 0;

        for (PlanetTypeRef type : types) {
            currentWeight += type.getRarityWeight();
            if (random < currentWeight) {
                return type;
            }
        }

        return types.getFirst();
    }

    private double addVariance(double value) {
        double factor = 1.0 + ((Math.random() - 0.5) * 2 * VARIANCE);
        return value * factor;
    }

    private void populateCompositionProperties(Planet planet) {
        PlanetaryComposition composition = compositionCreator.generateComposition(
                planet.getPlanetType(),
                planet.getSemiMajorAxisAU(),
                planet.getCoreType()
        );
        planet.setInteriorComposition(composition.toInteriorString());
        planet.setEnvelopeComposition(composition.toEnvelopeString());
        planet.setCompositionClassification(composition.getClassification().name());
    }

    private void populateGeologicalProperties(Planet planet) {
        PlanetaryGeology geology = geologyCreator.generateGeology(planet);
        planet.setGeologicalActivity(geology.toString());
    }

    private static class HabitableZone {
        double innerEdge;
        double outerEdge;

        HabitableZone(double inner, double outer) {
            this.innerEdge = inner;
            this.outerEdge = outer;
        }
    }

}
