package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.entity.ref.RingTemplateRef;
import com.brickroad.starcreator_webservice.enums.BandCategory;
import com.brickroad.starcreator_webservice.enums.DistanceUnit;
import com.brickroad.starcreator_webservice.repository.RingTemplateRefRepository;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RingCreator {

    @Autowired
    private RingTemplateRefRepository ringTemplateRefRepository;

    @Autowired
    private OrbitalCreator orbitalCreator;

    private static final double EARTH_MASS_KG = 5.972e24;

    public static class RingSystemData {
        public boolean shouldHaveRings;
        public double ringMassBudget; // In Earth masses
        public RingTemplateRef selectedTemplate;
        public int suggestedShepherdMoons;

        public RingSystemData(boolean shouldHaveRings, double ringMassBudget,
                              RingTemplateRef template, int shepherdMoons) {
            this.shouldHaveRings = shouldHaveRings;
            this.ringMassBudget = ringMassBudget;
            this.selectedTemplate = template;
            this.suggestedShepherdMoons = shepherdMoons;
        }
    }

    public RingSystemData planRingSystem(Planet planet, double totalSatelliteMass) {
        if (!shouldHaveRings(planet)) {
            return new RingSystemData(false, 0.0, null, 0);
        }

        List<RingTemplateRef> matchingTemplates = findMatchingTemplates(planet);
        if (matchingTemplates.isEmpty()) {
            return new RingSystemData(false, 0.0, null, 0);
        }

        RingTemplateRef selectedTemplate = selectTemplateByWeight(matchingTemplates);

        double ringMassFraction = RandomUtils.rollRange(0.00001, 0.0001);
        double ringMassBudget = totalSatelliteMass * ringMassFraction;

        int shepherdMoons = determineShepherdMoonCount(selectedTemplate);

        return new RingSystemData(true, ringMassBudget, selectedTemplate, shepherdMoons);
    }

    public List<OrbitalBand> createRings(Planet planet, RingSystemData ringPlan,
                                          double assignedRingMass) {
        List<OrbitalBand> rings = new ArrayList<>();
        if (!ringPlan.shouldHaveRings || ringPlan.selectedTemplate == null) {
            return rings;
        }

        double rocheLimit = calculateRocheLimit(planet);
        rings = generateRingsFromTemplate(planet, ringPlan.selectedTemplate, rocheLimit, assignedRingMass);

        return rings;
    }

    private int determineShepherdMoonCount(RingTemplateRef template) {
        if (template.getHasShepherdMoonsProbability() < 0.3) {
            return 0;
        }
        return switch (template.getRingType()) {
            case "MAIN" -> RandomUtils.rollRange(0, 3) + 2;
            case "NARROW" -> {
                int numRings = RandomUtils.rollRange(0, 3) + 2;
                yield numRings * 2;
            }
            case "GOSSAMER" -> RandomUtils.rollRange(0, 2) + 1;
            default -> RandomUtils.rollRange(0, 2);
        };
    }

    private boolean shouldHaveRings(Planet planet) {
        double planetMassEarth = planet.getEarthMass();
        double distanceAU = planet.getSemiMajorAxisAU();
        String planetType = planet.getPlanetType();

        if (planetMassEarth < 0.5 || distanceAU < 0.1) {
            return false;
        }

        if (planetType != null && (planetType.contains("Rocky") ||
                planetType.contains("Terrestrial") || planetType.contains("Desert"))) {
            double systemAge = planet.getAgeMY();
            if (systemAge > 100) {
                return false;
            }
            return RandomUtils.rollRange(0.0,1.0) < 0.05; // 5% chance
        }

        if (planetType != null && (planetType.contains("Gas Giant") ||
                planetType.contains("Ice Giant"))) {

            if (distanceAU < 0.5) {
                return RandomUtils.rollRange(0.0,1.0) < 0.3;
            } else if (distanceAU > 50) {
                return RandomUtils.rollRange(0.0,1.0) < 0.4;
            } else {
                return RandomUtils.rollRange(0.0,1.0) < 0.75;
            }
        }

        if (planetMassEarth > 2.0 && planetMassEarth < 15.0) {
            double systemAge = planet.getAgeMY();
            if (systemAge > 5000) {
                double retentionChance = 1.0 - ((systemAge - 5000) / 5000.0);
                retentionChance = Math.max(0.1, retentionChance);
                return RandomUtils.rollRange(0.0,1.0) < retentionChance;
            }
            return RandomUtils.rollRange(0.0,1.0) < 0.5;
        }

        return RandomUtils.rollRange(0.0,1.0) < 0.15;
    }

    private List<RingTemplateRef> findMatchingTemplates(Planet planet) {
        List<RingTemplateRef> allTemplates = ringTemplateRefRepository.findAll();

        return allTemplates.stream()
                .filter(template -> template.matches(
                        planet.getPlanetType(),
                        planet.getEarthMass(),
                        planet.getSemiMajorAxisAU()))
                .collect(Collectors.toList());
    }

    private RingTemplateRef selectTemplateByWeight(List<RingTemplateRef> templates) {
        int totalWeight = templates.stream()
                .mapToInt(RingTemplateRef::getRarityWeight)
                .sum();

        int randomValue = RandomUtils.rollRange(0, totalWeight);
        int currentWeight = 0;

        for (RingTemplateRef template : templates) {
            currentWeight += template.getRarityWeight();
            if (randomValue < currentWeight) {
                return template;
            }
        }

        return templates.getFirst();
    }

    private double calculateRocheLimit(Planet planet) {
        double planetRadius = planet.getRadius();
        double planetDensity = planet.getDensity();
        double particleDensity = 1.0;

        double densityRatio = Math.pow(planetDensity / particleDensity, 1.0 / 3.0);
        return 2.46 * planetRadius * densityRatio;
    }

    private List<OrbitalBand> generateRingsFromTemplate(Planet planet, RingTemplateRef template,
                                                         double rocheLimit, double totalRingMassEarth) {
        List<OrbitalBand> rings = new ArrayList<>();

        int numRings = determineNumberOfRings(template);
        double totalRingMassKg = totalRingMassEarth * EARTH_MASS_KG;
        List<Double> ringMasses = distributeRingMass(totalRingMassKg, numRings);
        double planetMassKg = planet.getEarthMass() * EARTH_MASS_KG;

        for (int i = 0; i < numRings; i++) {
            OrbitalBand ring = new OrbitalBand();
            ring.setPlanet(planet);
            ring.setBandCategory(BandCategory.RING);
            ring.setBandType(template.getRingType());

            double planetRadiusKm = planet.getRadius();
            double[] radii = calculateRingRadii(template, planetRadiusKm, rocheLimit, i, numRings);
            double innerEdge = radii[0];
            double outerEdge = radii[1];

            ring.setInnerOrbit(orbitalCreator.createBandEdgeOrbit(
                    innerEdge, DistanceUnit.KM, planetMassKg, 0.001, 0.5, "Ring inner edge"));
            ring.setOuterOrbit(orbitalCreator.createBandEdgeOrbit(
                    outerEdge, DistanceUnit.KM, planetMassKg, 0.001, 0.5, "Ring outer edge"));

            ring.setThicknessKm(RandomUtils.rollRange(
                    template.getThicknessMinKm(),
                    template.getThicknessMaxKm()
            ));

            ring.setOpticalDepth(RandomUtils.rollRange(
                    template.getOpticalDepthMin(),
                    template.getOpticalDepthMax()
            ));

            ring.setParticleSizeMinM(template.getParticleSizeMinM());
            ring.setParticleSizeMaxM(template.getParticleSizeMaxM());

            ring.setCompositionType(template.getCompositionType());
            ring.setPrimaryComposition(template.getCompositionDescription());
            ring.setColor(template.getColor());

            ring.setAlbedo(RandomUtils.rollRange(
                    template.getAlbedoMin(),
                    template.getAlbedoMax()
            ));

            ring.setVisibility(template.getVisibility());

            ring.setHasGaps(RandomUtils.rollRange(0.0, 1.0) < template.getHasGapsProbability());
            if (ring.getHasGaps()) {
                ring.setGapDescription(generateGapDescription());
            }

            ring.setHasShepherdMoons(RandomUtils.rollRange(0.0, 1.0) < template.getHasShepherdMoonsProbability());
            ring.setStability(template.getStability());
            ring.setOriginType(template.getOriginType());

            ring.setEstimatedAgeMY(calculateRingAge(planet, template));
            ring.setTotalMassKg(ringMasses.get(i));

            ring.setFormationDescription(generateFormationDescription(template));

            rings.add(ring);
        }

        return rings;
    }

    private List<Double> distributeRingMass(double totalMassKg, int numRings) {
        List<Double> masses = new ArrayList<>();

        if (numRings == 1) {
            masses.add(totalMassKg);
            return masses;
        }

        List<Double> weights = new ArrayList<>();
        double totalWeight = 0.0;

        for (int i = 0; i < numRings; i++) {
            double weight = Math.pow(numRings - i, 2.0); // Quadratic falloff
            weights.add(weight);
            totalWeight += weight;
        }

        for (int i = 0; i < numRings; i++) {
            double fraction = weights.get(i) / totalWeight;
            masses.add(totalMassKg * fraction);
        }

        return masses;
    }

    public void linkShepherdMoons(Planet planet) {
        List<OrbitalBand> rings = planet.getBands().stream()
                .filter(b -> b.getBandCategory() == BandCategory.RING)
                .collect(Collectors.toList());
        List<Moon> moons = planet.getMoons();

        if (rings.isEmpty() || moons == null || moons.isEmpty()) {
            return;
        }

        for (OrbitalBand ring : rings) {
            if (!ring.getHasShepherdMoons()) {
                continue;
            }

            double ringInner = ring.getInnerOrbit().getSemiMajorAxis();
            double ringOuter = ring.getOuterOrbit().getSemiMajorAxis();
            double ringWidth = ringOuter - ringInner;

            double searchMargin = Math.max(
                    ringWidth * 0.5,
                    planet.getRadius() * 0.2
            );

            List<Moon> sortedMoons = new ArrayList<>(moons);
            sortedMoons.sort(Comparator.comparingDouble(Moon::getSemiMajorAxisKm));

            for (Moon moon : sortedMoons) {
                if (Boolean.TRUE.equals(moon.getIsShepherdMoon())) {
                    continue;
                }

                double moonOrbit = moon.getSemiMajorAxisKm();

                boolean nearInner = Math.abs(moonOrbit - ringInner) < searchMargin;
                boolean nearOuter = Math.abs(moonOrbit - ringOuter) < searchMargin;

                if (nearInner || nearOuter) {
                    moon.setIsShepherdMoon(true);
                    moon.setShepherdsRingName(ring.getName());

                    String resonanceDesc = ring.getOrbitalResonances();
                    if (resonanceDesc == null || resonanceDesc.isEmpty()) {
                        resonanceDesc = "Shepherded by " + moon.getName();
                    } else {
                        resonanceDesc += ", " + moon.getName();
                    }
                    ring.setOrbitalResonances(resonanceDesc);
                }
            }
        }
    }

    private int determineNumberOfRings(RingTemplateRef template) {
        return switch (template.getRingType()) {
            case "MAIN" -> RandomUtils.rollRange(0,3) + 1;
            case "GOSSAMER" -> RandomUtils.rollRange(0,2) + 1;
            case "NARROW" -> RandomUtils.rollRange(0,5) + 2;
            default -> 1;
        };
    }

    private double[] calculateRingRadii(RingTemplateRef template, double planetRadiusKm,
                                         double rocheLimit, int ringIndex, int totalRings) {
        // Calculate available space for rings
        double minRadius = Math.max(
                template.getInnerRadiusMinPlanetRadii() * planetRadiusKm,
                rocheLimit * 1.1 // 10% margin above Roche limit
        );
        double maxRadius = template.getOuterRadiusMaxPlanetRadii() * planetRadiusKm;

        double innerEdge;
        double outerEdge;

        // Distribute rings across available space
        if (totalRings == 1) {
            // Single ring - use full range
            innerEdge = RandomUtils.rollRange(minRadius, minRadius + (maxRadius - minRadius) * 0.3);
            outerEdge = RandomUtils.rollRange(
                    innerEdge + planetRadiusKm * 0.5,  // Ensure outer > inner
                    maxRadius
            );
        } else {
            // Multiple rings - divide space
            // Rings are indexed from 0 (innermost) to totalRings-1 (outermost)
            double segmentSize = (maxRadius - minRadius) / totalRings;
            double segmentStart = minRadius + (segmentSize * ringIndex);
            double segmentEnd = segmentStart + segmentSize;

            // Generate ring boundaries within this segment
            // Inner edge: in first 30% of segment
            innerEdge = RandomUtils.rollRange(segmentStart, segmentStart + segmentSize * 0.3);

            // Outer edge: between inner edge and segment end
            // Ensure minimum width of 10% of segment
            double minOuterEdge = innerEdge + segmentSize * 0.1;
            outerEdge = RandomUtils.rollRange(
                    minOuterEdge,
                    segmentEnd
            );

            if (outerEdge <= innerEdge) {
                outerEdge = innerEdge + segmentSize * 0.2;  // Force 20% segment width
            }

            //TODO Remove this if statement! outer should never be smaller than inner
            if (outerEdge <= innerEdge) {
                double temp = innerEdge;
                innerEdge = outerEdge;
                outerEdge = temp;
            }
        }

        return new double[]{innerEdge, outerEdge};
    }

    private String generateGapDescription() {
        int numGaps = RandomUtils.rollRange(0,3) + 1;
        List<String> gaps = new ArrayList<>();

        String[] gapTypes = {
                "Cassini-like division",
                "Shepherd moon gap",
                "Resonance gap",
                "Narrow clearing",
                "Wave-edge gap"
        };

        for (int i = 0; i < numGaps; i++) {
            gaps.add(gapTypes[RandomUtils.rollRange(0,(gapTypes.length-1))]);
        }

        return String.join(", ", gaps);
    }

    private double calculateRingAge(Planet planet, RingTemplateRef template) {
        String origin = template.getOriginType();
        double planetAge = planet.getAgeMY();

        return switch (origin) {
            case "PRIMORDIAL" -> planetAge;
            case "MOON_DISRUPTION", "MOON_COLLISION" ->
                    RandomUtils.rollRange(10, planetAge * 0.5);
            case "COMET_DEBRIS" -> RandomUtils.rollRange(1, planetAge * 0.1);
            default -> planetAge * 0.5;
        };
    }

    private String generateFormationDescription(RingTemplateRef template) {
        return switch (template.getOriginType()) {
            case "PRIMORDIAL" ->
                    "Formed from leftover material during planet formation, persisting since the early solar system.";
            case "MOON_DISRUPTION" ->
                    "Created when a moon passed within the Roche limit and was torn apart by tidal forces.";
            case "MOON_COLLISION" -> "Debris from a catastrophic collision between two moons in the distant past.";
            case "COMET_DEBRIS" -> "Formed from the breakup of a captured comet or series of cometary impacts.";
            default -> "Origin uncertain, possibly from multiple contributing sources.";
        };
    }
}
