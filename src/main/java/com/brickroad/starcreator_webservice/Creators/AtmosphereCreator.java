package com.brickroad.starcreator_webservice.Creators;

import com.brickroad.starcreator_webservice.model.atmospheres.*;
import com.brickroad.starcreator_webservice.repos.AtmosphereTemplateRefRepository;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates realistic planetary atmospheres using database templates
 * Hybrid approach: Gas properties in enums, compositions in database
 */
@Service
public class AtmosphereCreator {

    @Autowired
    private AtmosphereTemplateRefRepository templateRepository;

    private List<AtmosphereTemplateRef> cachedTemplates;

    @PostConstruct
    public void init() {
        // Cache templates on startup for performance
        cachedTemplates = templateRepository.findAllOrderedByRarity();
    }

    public PlanetaryAtmosphere generateAtmosphere(String planetType, double surfaceTemp, double earthMass, double distanceAU) {

        if (earthMass < 0.1 || surfaceTemp > 2000) {
            return createNoneAtmosphere();
        }
        if (shouldLoseAtmosphere(earthMass, distanceAU, surfaceTemp)) {
            return createNoneAtmosphere();
        }
        List<AtmosphereTemplateRef> matchingTemplates = findMatchingTemplates(planetType, surfaceTemp, earthMass);
        if (matchingTemplates.isEmpty()) {
            return createDefaultAtmosphere();
        }
        return generateFromTemplate(selectTemplateByWeight(matchingTemplates), distanceAU);
    }

    private boolean shouldLoseAtmosphere(double earthMass, double distanceAU, double surfaceTemp) {
            boolean result = false;
            if (distanceAU < 0.1 && earthMass < 50.0 && surfaceTemp > 1000) {
                result = Math.random() < 0.3;
            } else if (distanceAU < 0.5 && earthMass < 0.5 && surfaceTemp > 400) {
                result = Math.random() < 0.5;
            } else if (distanceAU < 1.0 && earthMass < 0.3) {
                result = Math.random() < 0.2;
            }
            return result;
        }

    private List<AtmosphereTemplateRef> findMatchingTemplates(String planetType, double temp, double mass) {
        List<AtmosphereTemplateRef> matches = filterByPlanetType(planetType, cachedTemplates);
        matches = matches.stream()
                .filter(template -> template.matches(temp, mass))
                .collect(Collectors.toList());
        if (matches.isEmpty()) {
            matches = templateRepository.findMatchingTemplates(temp, mass);
        }
        if (matches.isEmpty()) {
            matches = filterByPlanetType(planetType, cachedTemplates);
        }
        if (matches.isEmpty()) {
            matches = cachedTemplates;
        }
        return matches;
    }

    private List<AtmosphereTemplateRef> filterByPlanetType(String planetType, List<AtmosphereTemplateRef> templates) {
        String type = planetType.toLowerCase();

        if (type.contains("gas giant") || type.contains("hot jupiter") || type.contains("super-jupiter")) {
            return templates.stream()
                    .filter(t -> t.getClassification() == AtmosphereClassification.JOVIAN)
                    .collect(Collectors.toList());
        }
        if (type.contains("ice giant") || type.contains("mini-neptune") || type.contains("sub-neptune")) {
            return templates.stream()
                    .filter(t -> t.getClassification() == AtmosphereClassification.ICE_GIANT)
                    .collect(Collectors.toList());
        }
        if (type.contains("terrestrial") || type.contains("super-earth") || type.contains("ocean world")) {
            return templates.stream()
                    .filter(t -> t.getClassification() == AtmosphereClassification.EARTH_LIKE ||
                            t.getClassification() == AtmosphereClassification.VENUS_LIKE ||
                            t.getClassification() == AtmosphereClassification.MARS_LIKE ||
                            t.getClassification() == AtmosphereClassification.REDUCING)
                    .collect(Collectors.toList());
        }
        if (type.contains("lava") || type.contains("hot rocky")) {
            return templates.stream()
                    .filter(t -> t.getClassification() == AtmosphereClassification.VOLCANIC ||
                            t.getClassification() == AtmosphereClassification.EXOTIC ||
                            t.getClassification() == AtmosphereClassification.CORROSIVE)
                    .collect(Collectors.toList());
        }
        if (type.contains("ice world") || type.contains("ice planet")) {
            return templates.stream()
                    .filter(t -> t.getClassification() == AtmosphereClassification.TITAN_LIKE ||
                            t.getClassification() == AtmosphereClassification.AMMONIA ||
                            t.getClassification() == AtmosphereClassification.MARS_LIKE)
                    .collect(Collectors.toList());
        }
        if (type.contains("desert")) {
            return templates.stream()
                    .filter(t -> t.getClassification() == AtmosphereClassification.MARS_LIKE ||
                            t.getClassification() == AtmosphereClassification.VENUS_LIKE)
                    .collect(Collectors.toList());
        }
        if (type.contains("carbon") || type.contains("iron")) {
            return templates.stream()
                    .filter(t -> t.getClassification() == AtmosphereClassification.EXOTIC ||
                            t.getClassification() == AtmosphereClassification.CORROSIVE ||
                            t.getClassification() == AtmosphereClassification.REDUCING)
                    .collect(Collectors.toList());
        }
        if (type.contains("dwarf") || type.contains("rogue")) {
            return templates.stream()
                    .filter(t -> t.getClassification() == AtmosphereClassification.NONE ||
                            t.getClassification() == AtmosphereClassification.TITAN_LIKE ||
                            t.getClassification() == AtmosphereClassification.MARS_LIKE)
                    .collect(Collectors.toList());
        }
        if (type.contains("puffy")) {
            return templates.stream()
                    .filter(t -> t.getClassification() == AtmosphereClassification.JOVIAN ||
                            t.getClassification() == AtmosphereClassification.ICE_GIANT)
                    .collect(Collectors.toList());
        }
        return templates;
    }

    private AtmosphereTemplateRef selectTemplateByWeight(List<AtmosphereTemplateRef> templates) {
        int totalWeight = templates.stream()
                .mapToInt(AtmosphereTemplateRef::getRarityWeight)
                .sum();

        int random = RandomUtils.rollRange(0, totalWeight);
        int currentWeight = 0;

        for (AtmosphereTemplateRef template : templates) {
            currentWeight += template.getRarityWeight();
            if (random < currentWeight) {
                return template;
            }
        }

        return templates.getFirst();
    }

    private PlanetaryAtmosphere generateFromTemplate(AtmosphereTemplateRef template, double distanceAU) {
        PlanetaryAtmosphere.Builder builder = new PlanetaryAtmosphere.Builder()
                .classification(template.getClassification());

        for (AtmosphereTemplateComponentRef component : template.getComponents()) {
            AtmosphereGas gas = component.getGas();
            if (gas != null) {
                double percentage = RandomUtils.rollRange(
                        component.getMinPercentage(),
                        component.getMaxPercentage()
                );
                percentage = adjustGasPercentageByDistance(gas, percentage, distanceAU);
                builder.addGas(gas, percentage);
            }
        }
        return builder.build();
    }

    private double adjustGasPercentageByDistance(AtmosphereGas gas, double percentage, double distanceAU) {
        if (distanceAU < 0.5) {
            return switch (gas) {
                case WATER_VAPOR -> percentage * 0.3;
                case METHANE, AMMONIA -> percentage * 0.1;
                case CARBON_DIOXIDE -> percentage * 0.7;
                default -> percentage;
            };
        }

        if (distanceAU > 5.0) {
            return switch (gas) {
                case WATER_VAPOR, METHANE, AMMONIA -> percentage * 1.3;
                case CARBON_MONOXIDE -> percentage * 1.5;
                default -> percentage;
            };
        }
        return percentage;
    }

    private PlanetaryAtmosphere createDefaultAtmosphere() {
        return new PlanetaryAtmosphere.Builder()
                .addGas(AtmosphereGas.NITROGEN, RandomUtils.rollRange(60.0, 90.0))
                .addGas(AtmosphereGas.CARBON_DIOXIDE, RandomUtils.rollRange(5.0, 30.0))
                .addGas(AtmosphereGas.ARGON, RandomUtils.rollRange(0.5, 5.0))
                .classification(AtmosphereClassification.CUSTOM)
                .build();
    }

    private PlanetaryAtmosphere createNoneAtmosphere() {
        return new PlanetaryAtmosphere.Builder()
                .classification(AtmosphereClassification.NONE)
                .build();
    }

    public PlanetaryAtmosphere generateByClassification(AtmosphereClassification classification, double distanceAU) {
        AtmosphereTemplateRef template = templateRepository.findByClassification(classification).orElse(null);
        if (template == null) {
            return createDefaultAtmosphere();
        }
        return generateFromTemplate(template, distanceAU);
    }

    public AtmosphereTemplateRef getTemplateByClassification(AtmosphereClassification classification) {
        return templateRepository.findByClassification(classification).orElse(null);
    }

    public double calculateSurfacePressure(double earthMass, double surfaceTemp, PlanetaryAtmosphere atmosphere) {

        double basePressure;
        if (earthMass < 0.5) {
            basePressure = RandomUtils.rollRange(0.001, 0.1);
        } else if (earthMass < 2.0) {
            basePressure = RandomUtils.rollRange(0.3, 5.0);
        } else if (earthMass < 10.0) {
            basePressure = RandomUtils.rollRange(1.0, 50.0);
        } else {
            basePressure = RandomUtils.rollRange(10.0, 10000.0);
        }

        AtmosphereTemplateRef template = getTemplateByClassification(atmosphere.getClassification());

        if (template != null && template.getTypicalPressureBar() != null) {
            double templatePressure = template.getTypicalPressureBar();

            double tempFactor = 288.0 / surfaceTemp;
            tempFactor = Math.max(0.5, Math.min(2.0, tempFactor));

            return templatePressure * tempFactor * RandomUtils.rollRange(0.8, 1.2);
        }

        double tempFactor = 288.0 / surfaceTemp;
        tempFactor = Math.max(0.5, Math.min(2.0, tempFactor));
        return basePressure * tempFactor;
    }
}