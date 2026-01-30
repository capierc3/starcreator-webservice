package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.enums.CompositionClassification;
import com.brickroad.starcreator_webservice.enums.CompositionMineral;
import com.brickroad.starcreator_webservice.entity.ref.CompositionTemplateComponentRef;
import com.brickroad.starcreator_webservice.entity.ref.CompositionTemplateRef;
import com.brickroad.starcreator_webservice.utils.planets.PlanetaryComposition;
import com.brickroad.starcreator_webservice.repository.CompositionTemplateRefRepository;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for generating realistic planetary compositions.
 * Uses database templates to create varied compositions based on planet type and distance.
 */
@Service
public class CompositionCreator {

    @Autowired
    private CompositionTemplateRefRepository templateRepository;

    private List<CompositionTemplateRef> cachedTemplates;

    @PostConstruct
    public void init() {
        cachedTemplates = templateRepository.findAllOrderedByRarity();
    }

    public PlanetaryComposition generateComposition(String planetType, double surfaceTemp, String coreType) {
        
        List<CompositionTemplateRef> matchingTemplates = findMatchingTemplates(planetType, surfaceTemp);

        if (matchingTemplates.isEmpty()) {
            return createDefaultComposition(coreType, planetType);
        }
        return generateFromTemplate(selectTemplateByWeight(matchingTemplates));
    }

    private List<CompositionTemplateRef> findMatchingTemplates(String planetType, double surfaceTemp) {

        List<CompositionTemplateRef> matches = cachedTemplates.stream()
                .filter(template -> template.matches(planetType, surfaceTemp))
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            matches = templateRepository.findMatchingTempTemplates(surfaceTemp);
        }

        if (matches.isEmpty()) {
            matches = cachedTemplates.stream()
                    .filter(template -> template.getPlanetTypes() != null && 
                                      template.getPlanetTypes().toLowerCase().contains(planetType.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (matches.isEmpty()) {
            matches = cachedTemplates;
        }
        
        return matches;
    }

    private CompositionTemplateRef selectTemplateByWeight(List<CompositionTemplateRef> templates) {
        int totalWeight = templates.stream()
                .mapToInt(CompositionTemplateRef::getRarityWeight)
                .sum();

        int random = RandomUtils.rollRange(0, totalWeight);
        int currentWeight = 0;

        for (CompositionTemplateRef template : templates) {
            currentWeight += template.getRarityWeight();
            if (random < currentWeight) {
                return template;
            }
        }

        return templates.getFirst();
    }

    private PlanetaryComposition generateFromTemplate(CompositionTemplateRef template) {
            PlanetaryComposition.Builder builder = new PlanetaryComposition.Builder()
                    .classification(template.getClassification());

        for (CompositionTemplateComponentRef component : template.getComponents()) {
            if ("INTERIOR".equals(component.getLayerType())) {
                double percentage = RandomUtils.rollRange(
                        component.getMinPercentage(),
                        component.getMaxPercentage()
                );
                builder.addInteriorMineral(component.getMineral(), percentage);
            }
        }

        for (CompositionTemplateComponentRef component : template.getComponents()) {
            if ("ENVELOPE".equals(component.getLayerType())) {
                double percentage = RandomUtils.rollRange(
                        component.getMinPercentage(),
                        component.getMaxPercentage()
                );
                builder.addEnvelopeMineral(component.getMineral(), percentage);
            }
        }

        return builder.build();
    }

    private PlanetaryComposition createDefaultComposition(String coreType, String planetType) {
        PlanetaryComposition.Builder builder = new PlanetaryComposition.Builder();
        
        String lowerType = planetType.toLowerCase();
        String lowerCore = coreType != null ? coreType.toLowerCase() : "";

        if(isMoon(lowerType)) {
            return createDefaultMoonComposition(lowerType, lowerCore, builder);
        }

        if (lowerType.contains("gas") || lowerType.contains("jupiter") || lowerType.contains("neptune")) {
            builder.classification(CompositionClassification.GAS_ENVELOPE);
            builder.addInteriorMineral(CompositionMineral.HYDROGEN_HELIUM_MIX, RandomUtils.rollRange(85.0, 95.0));
            builder.addInteriorMineral(CompositionMineral.WATER_ICE, RandomUtils.rollRange(3.0, 10.0));
            builder.addEnvelopeMineral(CompositionMineral.HYDROGEN_HELIUM_MIX, 100.0);
            
        } else if (lowerType.contains("ice") || lowerCore.contains("ice")) {
            builder.classification(CompositionClassification.ICE_RICH);
            builder.addInteriorMineral(CompositionMineral.WATER_ICE, RandomUtils.rollRange(40.0, 60.0));
            builder.addInteriorMineral(CompositionMineral.METHANE_ICE, RandomUtils.rollRange(15.0, 30.0));
            builder.addInteriorMineral(CompositionMineral.AMMONIA_ICE, RandomUtils.rollRange(10.0, 25.0));
            builder.addEnvelopeMineral(CompositionMineral.WATER_ICE, RandomUtils.rollRange(70.0, 90.0));
            builder.addEnvelopeMineral(CompositionMineral.METHANE_ICE, RandomUtils.rollRange(5.0, 20.0));
            
        } else if (lowerType.contains("ocean")) {
            builder.classification(CompositionClassification.OCEAN_WORLD);
            builder.addInteriorMineral(CompositionMineral.OLIVINE, RandomUtils.rollRange(40.0, 50.0));
            builder.addInteriorMineral(CompositionMineral.PYROXENE, RandomUtils.rollRange(30.0, 40.0));
            builder.addInteriorMineral(CompositionMineral.IRON, RandomUtils.rollRange(10.0, 20.0));
            builder.addEnvelopeMineral(CompositionMineral.LIQUID_WATER, RandomUtils.rollRange(80.0, 100.0));
            
        } else if (lowerType.contains("carbon") || lowerCore.contains("carbon")) {
            builder.classification(CompositionClassification.CARBON_RICH);
            builder.addInteriorMineral(CompositionMineral.GRAPHITE, RandomUtils.rollRange(40.0, 50.0));
            builder.addInteriorMineral(CompositionMineral.SILICON_CARBIDE, RandomUtils.rollRange(25.0, 35.0));
            builder.addInteriorMineral(CompositionMineral.DIAMOND, RandomUtils.rollRange(10.0, 20.0));
            builder.addEnvelopeMineral(CompositionMineral.GRAPHITE, RandomUtils.rollRange(50.0, 70.0));
            builder.addEnvelopeMineral(CompositionMineral.DIAMOND, RandomUtils.rollRange(15.0, 30.0));
            
        } else if (lowerType.contains("iron") || lowerCore.contains("iron")) {
            builder.classification(CompositionClassification.IRON_RICH);
            builder.addInteriorMineral(CompositionMineral.IRON, RandomUtils.rollRange(70.0, 85.0));
            builder.addInteriorMineral(CompositionMineral.NICKEL, RandomUtils.rollRange(10.0, 20.0));
            builder.addInteriorMineral(CompositionMineral.OLIVINE, RandomUtils.rollRange(5.0, 15.0));
            builder.addEnvelopeMineral(CompositionMineral.IRON, RandomUtils.rollRange(80.0, 95.0));
            builder.addEnvelopeMineral(CompositionMineral.NICKEL, RandomUtils.rollRange(5.0, 15.0));
            
        } else if (lowerType.contains("lava")) {
            builder.classification(CompositionClassification.MOLTEN_SURFACE);
            builder.addInteriorMineral(CompositionMineral.OLIVINE, RandomUtils.rollRange(45.0, 55.0));
            builder.addInteriorMineral(CompositionMineral.PYROXENE, RandomUtils.rollRange(30.0, 40.0));
            builder.addInteriorMineral(CompositionMineral.IRON, RandomUtils.rollRange(10.0, 15.0));
            builder.addEnvelopeMineral(CompositionMineral.MOLTEN_ROCK, RandomUtils.rollRange(70.0, 90.0));
            builder.addEnvelopeMineral(CompositionMineral.BASALT, RandomUtils.rollRange(10.0, 30.0));
            
        } else {
            // Default silicate-rich (terrestrial, super-earth, desert, etc.)
            builder.classification(CompositionClassification.SILICATE_RICH);
            builder.addInteriorMineral(CompositionMineral.OLIVINE, RandomUtils.rollRange(40.0, 55.0));
            builder.addInteriorMineral(CompositionMineral.PYROXENE, RandomUtils.rollRange(25.0, 40.0));
            builder.addInteriorMineral(CompositionMineral.IRON, RandomUtils.rollRange(10.0, 20.0));
            builder.addEnvelopeMineral(CompositionMineral.FELDSPAR, RandomUtils.rollRange(40.0, 60.0));
            builder.addEnvelopeMineral(CompositionMineral.QUARTZ, RandomUtils.rollRange(20.0, 30.0));
            builder.addEnvelopeMineral(CompositionMineral.PYROXENE, RandomUtils.rollRange(10.0, 25.0));
        }
        
        return builder.build();
    }

    private boolean isMoon(String lowerType) {
        return (lowerType.contains("moon") ||
                lowerType.equals("regular_large") ||
                lowerType.equals("regular_medium") ||
                lowerType.equals("regular_small") ||
                lowerType.equals("collision_debris") ||
                lowerType.equals("shepherd") ||
                lowerType.equals("trojan") ||
                lowerType.equals("irregular_captured") ||
                lowerType.equals("volcanic moon") ||
                lowerType.equals("cryovolcanic moon") ||
                lowerType.equals("metal rich moon"));
    }

    private PlanetaryComposition createDefaultMoonComposition(String lowerType, String lowerCore, PlanetaryComposition.Builder builder) {
        System.out.println("DEBUG: Moon fallback triggered - Type: " + lowerType +
                ", Core: " + lowerCore);

        // Determine composition based on coreType
        // Handle ICY, MIXED, ROCKY, IRON explicitly
        if (lowerCore.contains("icy") || lowerCore.equals("ice")) {
            // Pure icy moon
            builder.classification(CompositionClassification.ICE_RICH);
            builder.addInteriorMineral(CompositionMineral.WATER_ICE, RandomUtils.rollRange(40.0, 60.0));
            builder.addInteriorMineral(CompositionMineral.OLIVINE, RandomUtils.rollRange(20.0, 30.0));
            builder.addInteriorMineral(CompositionMineral.PYROXENE, RandomUtils.rollRange(10.0, 20.0));
            builder.addEnvelopeMineral(CompositionMineral.WATER_ICE, RandomUtils.rollRange(70.0, 90.0));
            builder.addEnvelopeMineral(CompositionMineral.SILICATE, RandomUtils.rollRange(5.0, 15.0));

        } else if (lowerCore.equals("mixed")) {
            // Mixed ice/rock moon (common for medium-distance moons)
            builder.classification(CompositionClassification.MIXED_SILICATE_ICE);
            builder.addInteriorMineral(CompositionMineral.OLIVINE, RandomUtils.rollRange(30.0, 40.0));
            builder.addInteriorMineral(CompositionMineral.PYROXENE, RandomUtils.rollRange(20.0, 30.0));
            builder.addInteriorMineral(CompositionMineral.WATER_ICE, RandomUtils.rollRange(25.0, 35.0));
            builder.addInteriorMineral(CompositionMineral.IRON, RandomUtils.rollRange(10.0, 15.0));
            builder.addEnvelopeMineral(CompositionMineral.WATER_ICE, RandomUtils.rollRange(40.0, 60.0));
            builder.addEnvelopeMineral(CompositionMineral.SILICATE, RandomUtils.rollRange(30.0, 50.0));

        } else if (lowerCore.contains("iron") || lowerCore.contains("metal")) {
            // Metal-rich moon
            builder.classification(CompositionClassification.IRON_RICH);
            builder.addInteriorMineral(CompositionMineral.IRON, RandomUtils.rollRange(60.0, 80.0));
            builder.addInteriorMineral(CompositionMineral.NICKEL, RandomUtils.rollRange(10.0, 20.0));
            builder.addInteriorMineral(CompositionMineral.OLIVINE, RandomUtils.rollRange(5.0, 15.0));
            builder.addEnvelopeMineral(CompositionMineral.IRON, RandomUtils.rollRange(40.0, 60.0));
            builder.addEnvelopeMineral(CompositionMineral.BASALT, RandomUtils.rollRange(30.0, 50.0));

        } else {
            // Default rocky moon (ROCKY or unknown)
            builder.classification(CompositionClassification.SILICATE_RICH);
            builder.addInteriorMineral(CompositionMineral.OLIVINE, RandomUtils.rollRange(40.0, 55.0));
            builder.addInteriorMineral(CompositionMineral.PYROXENE, RandomUtils.rollRange(25.0, 40.0));
            builder.addInteriorMineral(CompositionMineral.IRON, RandomUtils.rollRange(10.0, 20.0));
            builder.addEnvelopeMineral(CompositionMineral.PLAGIOCLASE, RandomUtils.rollRange(40.0, 60.0));
            builder.addEnvelopeMineral(CompositionMineral.BASALT, RandomUtils.rollRange(30.0, 50.0));
        }

        CompositionClassification finalClass = builder.build().getClassification();
        System.out.println("DEBUG: Moon default → " + finalClass + " (coreType was: " + lowerCore + ")");

        return builder.build();
    }
}
