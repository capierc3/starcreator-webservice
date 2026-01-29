package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ref.AtmosphereTemplateComponentRef;
import com.brickroad.starcreator_webservice.entity.ref.AtmosphereTemplateRef;
import com.brickroad.starcreator_webservice.enums.AtmosphereClassification;
import com.brickroad.starcreator_webservice.enums.AtmosphereGas;
import com.brickroad.starcreator_webservice.utils.planets.PlanetaryAtmosphere;
import com.brickroad.starcreator_webservice.repository.AtmosphereTemplateRefRepository;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AtmosphereCreator {

    @Autowired
    private AtmosphereTemplateRefRepository templateRepository;

    private List<AtmosphereTemplateRef> cachedTemplates;

    @PostConstruct
    public void init() {
        cachedTemplates = templateRepository.findAllOrderedByRarity();
    }

    public record AtmosphereResult(PlanetaryAtmosphere atmosphere, AtmosphereTemplateRef template) {}

    public AtmosphereResult generateAtmosphereWithTemplate(String planetType, double surfaceTemp, double earthMass, double distanceAU) {

        if (surfaceTemp > 2000) {
            return new AtmosphereResult(createNoneAtmosphere(), null);
        }
        if (shouldLoseAtmosphere(earthMass, distanceAU, surfaceTemp)) {
            return new AtmosphereResult(createNoneAtmosphere(), null);
        }
        List<AtmosphereTemplateRef> matchingTemplates = findMatchingTemplates(planetType, surfaceTemp, earthMass);
        if (matchingTemplates.isEmpty()) {
            return new AtmosphereResult(createDefaultAtmosphere(), null);
        }
        AtmosphereTemplateRef selectedTemplate = selectTemplateByWeight(matchingTemplates);
        return new AtmosphereResult(generateFromTemplate(selectedTemplate, distanceAU), selectedTemplate);
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
        List<AtmosphereTemplateRef> matches = templateRepository.findMatchingTemplates(planetType, temp, mass);

        List<AtmosphereTemplateRef> realAtmospheres = matches.stream()
                .filter(t -> t.getClassification() != AtmosphereClassification.NONE)
                .toList();
        if (!realAtmospheres.isEmpty()) {
            return realAtmospheres;  // Return only real atmospheres
        }

        if (matches.isEmpty()) {
            matches = templateRepository.findMatchingTemplates(planetType, temp);
        }
        if (matches.isEmpty()) {
            matches = templateRepository.findMatchingTemplates(planetType);
        }
        if (matches.isEmpty()) {
            matches = cachedTemplates;
        }
        return matches;
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

    public double calculateSurfacePressure(double earthMass, double surfaceTemp, AtmosphereTemplateRef template) {

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