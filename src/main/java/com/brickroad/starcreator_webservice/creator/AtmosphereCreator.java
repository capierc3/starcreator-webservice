package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ref.AtmosphereTemplateComponentRef;
import com.brickroad.starcreator_webservice.entity.ref.AtmosphereTemplateRef;
import com.brickroad.starcreator_webservice.entity.ud.Star;
import com.brickroad.starcreator_webservice.enums.AtmosphereClassification;
import com.brickroad.starcreator_webservice.enums.AtmosphereGas;
import com.brickroad.starcreator_webservice.utils.planets.PlanetaryAtmosphere;
import com.brickroad.starcreator_webservice.repository.AtmosphereTemplateRefRepository;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import com.brickroad.starcreator_webservice.utils.planets.StellarEnvironment;
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

    public AtmosphereResult generateAtmosphereWithTemplate(String planetType, double surfaceTemp,
                                                           double earthMass, double distanceAU) {
        return generateAtmosphereWithTemplate(planetType, surfaceTemp, earthMass, distanceAU, null);
    }

    public AtmosphereResult generateAtmosphereWithTemplate(String planetType, double surfaceTemp,
                                                           double earthMass, double distanceAU,
                                                           Star parentStar) {
        if (surfaceTemp > 2000) {
            return new AtmosphereResult(createNoneAtmosphere(), null);
        }
        if (shouldLoseAtmosphere(earthMass, distanceAU, surfaceTemp, parentStar)) {
            return new AtmosphereResult(createNoneAtmosphere(), null);
        }
        List<AtmosphereTemplateRef> matchingTemplates = findMatchingTemplates(planetType, surfaceTemp, earthMass);
        if (matchingTemplates.isEmpty()) {
            return new AtmosphereResult(createDefaultAtmosphere(), null);
        }
        AtmosphereTemplateRef selectedTemplate = selectTemplateByWeight(matchingTemplates);
        return new AtmosphereResult(generateFromTemplate(selectedTemplate, distanceAU, parentStar), selectedTemplate);
    }

    private boolean shouldLoseAtmosphere(double earthMass, double distanceAU,
                                         double surfaceTemp, Star parentStar) {
        if (earthMass >= 50.0) {
            return false;
        }

        if (parentStar == null) {
            if (distanceAU < 0.1 && earthMass < 50.0 && surfaceTemp > 1000) {
                return Math.random() < 0.3;
            } else if (distanceAU < 0.5 && earthMass < 0.5 && surfaceTemp > 400) {
                return Math.random() < 0.5;
            } else if (distanceAU < 1.0 && earthMass < 0.3) {
                return Math.random() < 0.2;
            }
            return false;
        }

        double strippingFactor = StellarEnvironment.atmosphericStrippingFactor(parentStar, distanceAU);


        double gravityResistance = Math.pow(earthMass, 0.6);

        double thermalEscapeFactor = 1.0;
        if (surfaceTemp > 1000) {
            thermalEscapeFactor = 1.0 + (surfaceTemp - 1000) / 2000.0;
        }

        double exposureFactor = 1.0;
        if (parentStar.getEvolutionaryStage() != null) {
            exposureFactor = switch (parentStar.getEvolutionaryStage()) {
                case "PRE_MAIN_SEQUENCE" -> 2.0;
                case "EARLY_MAIN_SEQUENCE" -> 0.8;
                case "LATE_MAIN_SEQUENCE" -> 1.3;
                default -> 1.0;
            };
        }

        double strippingScore = (strippingFactor * thermalEscapeFactor * exposureFactor)
                / Math.max(0.01, gravityResistance);

        if (strippingScore < 1.0) {
            return false;
        } else if (strippingScore < 5.0) {
            return Math.random() < (strippingScore - 1.0) / 8.0;
        } else if (strippingScore < 20.0) {
            return Math.random() < 0.5 + (strippingScore - 5.0) / 30.0;
        } else {
            return true;
        }
    }

    private List<AtmosphereTemplateRef> findMatchingTemplates(String planetType, double temp, double mass) {
        List<AtmosphereTemplateRef> matches = templateRepository.findMatchingTemplates(planetType, temp, mass);

        List<AtmosphereTemplateRef> realAtmospheres = matches.stream()
                .filter(t -> t.getClassification() != AtmosphereClassification.NONE)
                .toList();
        if (!realAtmospheres.isEmpty()) {
            return realAtmospheres;
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

    private PlanetaryAtmosphere generateFromTemplate(AtmosphereTemplateRef template, double distanceAU,
                                                     Star parentStar) {
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
                percentage = adjustGasPercentageByRadiation(gas, percentage, parentStar, distanceAU);
                builder.addGas(gas, percentage);
            }
        }
        return builder.build();
    }

    private PlanetaryAtmosphere generateFromTemplate(AtmosphereTemplateRef template, double distanceAU) {
        return generateFromTemplate(template, distanceAU, null);
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

    public double calculateSurfacePressure(double earthMass, double surfaceTemp,
                                           AtmosphereTemplateRef template) {
        return calculateSurfacePressure(earthMass, surfaceTemp, template, null, 0.0);
    }

    public double calculateSurfacePressure(double earthMass, double surfaceTemp,
                                           AtmosphereTemplateRef template,
                                           Star parentStar, double distanceAU) {
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

        double pressure;
        if (template != null && template.getTypicalPressureBar() != null) {
            double templatePressure = template.getTypicalPressureBar();
            double tempFactor = 288.0 / surfaceTemp;
            tempFactor = Math.max(0.5, Math.min(2.0, tempFactor));
            pressure = templatePressure * tempFactor * RandomUtils.rollRange(0.8, 1.2);
        } else {
            double tempFactor = 288.0 / surfaceTemp;
            tempFactor = Math.max(0.5, Math.min(2.0, tempFactor));
            pressure = basePressure * tempFactor;
        }

        // Apply stellar wind erosion if star data available
        if (parentStar != null && distanceAU > 0) {
            double strippingFactor = StellarEnvironment.atmosphericStrippingFactor(parentStar, distanceAU);
            double gravityResistance = Math.pow(earthMass, 0.5);

            // Erosion ratio: how much pressure is reduced over geological time
            // strippingFactor/gravityResistance = 1 for Sun/Earth → no reduction
            // Higher ratios → more erosion → lower pressure
            double erosionRatio = strippingFactor / Math.max(0.1, gravityResistance);

            if (erosionRatio > 1.0) {
                // Logarithmic reduction: 10x erosion → pressure halved, 100x → quartered
                double erosionMultiplier = 1.0 / (1.0 + 0.5 * Math.log10(erosionRatio));
                erosionMultiplier = Math.max(0.01, erosionMultiplier); // Never fully zero (template said it has atmosphere)
                pressure *= erosionMultiplier;
            }
        }

        return pressure;
    }

    private double adjustGasPercentageByRadiation(AtmosphereGas gas, double percentage,
                                                  Star parentStar, double distanceAU) {
        if (parentStar == null || percentage <= 0) {
            return percentage;
        }

        double xrayFactor = StellarEnvironment.xrayRadiationFactor(parentStar)
                / (distanceAU * distanceAU);

        if (xrayFactor <= 1.5) {
            return percentage;
        }

        double reductionFactor = 1.0 / (1.0 + 0.3 * Math.log10(xrayFactor));

        return switch (gas) {
            case WATER_VAPOR -> percentage * reductionFactor;
            case METHANE -> percentage * reductionFactor * 0.9;
            case AMMONIA -> percentage * reductionFactor * 0.85;
            case CARBON_DIOXIDE -> percentage * (1.0 + 0.1 * Math.log10(xrayFactor));
            case CARBON_MONOXIDE -> percentage * (1.0 + 0.05 * Math.log10(xrayFactor));
            default -> percentage;
        };
    }
}