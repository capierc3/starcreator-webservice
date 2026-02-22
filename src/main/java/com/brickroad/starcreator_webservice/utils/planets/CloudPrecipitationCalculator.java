package com.brickroad.starcreator_webservice.utils.planets;

import com.brickroad.starcreator_webservice.entity.ref.CloudCompositionTemplate;
import com.brickroad.starcreator_webservice.entity.ref.PrecipitationTemplate;
import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.repository.CloudCompositionTemplateRepository;
import com.brickroad.starcreator_webservice.repository.PrecipitationTemplateRepository;
import com.brickroad.starcreator_webservice.utils.CelestialBodyUtils;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CloudPrecipitationCalculator {

    @Autowired
    private CloudCompositionTemplateRepository cloudTemplateRepo;

    @Autowired
    private PrecipitationTemplateRepository precipTemplateRepo;

    // ================================================================
    // MAIN ENTRY POINT
    // ================================================================

    public void calculate(PlanetaryClimate weather, Planet planet) {
        String atmClass = planet.getAtmosphereClassification();
        double surfaceTemp = planet.getSurfaceTemp() != null ? planet.getSurfaceTemp() : 250.0;
        double pressureAtm = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 1.0;
        double waterPercent = planet.getWaterCoveragePercent() != null ? planet.getWaterCoveragePercent() : 0.0;
        double liquidWaterPercent = planet.getLiquidWaterCoveragePercent() != null ? planet.getLiquidWaterCoveragePercent() : 0.0;
        double icePercent = planet.getIceCoveragePercent() != null ? planet.getIceCoveragePercent() : 0.0;
        double scaleHeightKm = weather.getScaleHeightKm() != null ? weather.getScaleHeightKm() : 8.5;
        boolean tidallyLocked = Boolean.TRUE.equals(planet.getTidallyLocked());

        // Wind data from Phase 4 (affects cloud formation)
        double meanWindMs = weather.getMeanSurfaceWindSpeedMs() != null ? weather.getMeanSurfaceWindSpeedMs() : 7.0;

        // 1. Generate cloud layers from templates
        List<CloudLayer> cloudLayers = generateCloudLayers(atmClass, surfaceTemp, pressureAtm, scaleHeightKm);
        weather.setCloudLayers(cloudLayers);

        // 2. Calculate cloud coverage
        calculateCloudCoverage(weather, atmClass, surfaceTemp, pressureAtm,
                waterPercent, liquidWaterPercent, icePercent, tidallyLocked, meanWindMs);

        // 3. Set primary cloud composition from the dominant layer
        setPrimaryCloudComposition(weather, cloudLayers);

        // 4. Generate precipitation types
        List<PrecipitationType> precipTypes = generatePrecipitationTypes(cloudLayers, surfaceTemp, atmClass);
        weather.setPrecipitationTypes(precipTypes);

        // 5. Set summary precipitation fields
        calculatePrecipitationSummary(weather, precipTypes, atmClass, pressureAtm,
                liquidWaterPercent, waterPercent, surfaceTemp);
    }

    // ================================================================
    // CLOUD LAYER GENERATION
    // ================================================================

    private List<CloudLayer> generateCloudLayers(String atmClass, double surfaceTemp,
                                                  double pressureAtm, double scaleHeightKm) {
        List<CloudLayer> layers = new ArrayList<>();

        // Query reference templates for this atmosphere classification and temperature
        List<CloudCompositionTemplate> templates = cloudTemplateRepo
                .findByClassificationAndTemperature(atmClass, surfaceTemp);

        // Fallback: get all templates for this classification if temp-specific query returns empty
        if (templates.isEmpty()) {
            templates = cloudTemplateRepo.findByAtmosphereClassification(atmClass);
        }

        if (templates.isEmpty()) {
            return layers;
        }

        int layerOrder = 1;
        for (CloudCompositionTemplate template : templates) {
            // Weight-based inclusion: higher weight → more likely to appear
            // Weight 200 = always present, 150 = 75%, 120 = 60%, 100 = 50%, 80 = 40%
            if (RandomUtils.rollD100() > (template.getWeight() / 2)) {
                continue;
            }

            CloudLayer layer = new CloudLayer();
            layer.setLayerOrder(layerOrder++);
            layer.setComposition(template.getSubstance());

            // Altitude: template gives scale-height multiplier
            double altitudeMultiplier = template.getTypicalAltitudeScaleHeights() != null
                    ? template.getTypicalAltitudeScaleHeights() : 1.0;
            double altitudeKm = scaleHeightKm * altitudeMultiplier;
            // Add some variance (±20%)
            altitudeKm *= RandomUtils.rollRange(0.8, 1.2);
            layer.setAltitudeKm(round2(altitudeKm));

            // Thickness: related to scale height, thicker for dense atmospheres
            double thicknessKm = scaleHeightKm * RandomUtils.rollRange(0.2, 0.8);
            if (pressureAtm > 10) thicknessKm *= 2.0;    // Dense atmosphere → thicker layers
            if (pressureAtm < 0.01) thicknessKm *= 0.3;  // Thin atmosphere → wispy layers
            layer.setThicknessKm(round2(thicknessKm));

            // Temperature at cloud altitude: lapse rate approximation
            // T_cloud ≈ surfaceTemp - (altitude / scaleHeight) × lapse_factor
            double lapseFactor = 30.0; // rough K per scale height
            double cloudTemp = surfaceTemp - (altitudeMultiplier * lapseFactor);
            cloudTemp = Math.max(40, cloudTemp); // Floor at 40K
            layer.setTemperatureK(round2(cloudTemp));

            layer.setOpacity(template.getOpacity());
            layer.setColor(template.getColor());
            layer.setDescription(template.getDescription());

            layers.add(layer);
        }

        // Ensure at least one cloud layer for atmospheres that should have clouds
        if (layers.isEmpty() && pressureAtm > 0.001) {
            layers.add(createFallbackCloudLayer(atmClass, surfaceTemp, scaleHeightKm));
        }

        return layers;
    }

    private CloudLayer createFallbackCloudLayer(String atmClass, double surfaceTemp, double scaleHeightKm) {
        CloudLayer layer = new CloudLayer();
        layer.setLayerOrder(1);

        if (CelestialBodyUtils.isGasGiantAtmosphere(atmClass)) {
            layer.setComposition("NH3");
            layer.setOpacity("THICK");
            layer.setColor("White");
            layer.setDescription("Ammonia ice cloud deck");
        } else if (surfaceTemp > 1500) {
            layer.setComposition("SILICATE");
            layer.setOpacity("THICK");
            layer.setColor("Red-Orange");
            layer.setDescription("Silicate vapor clouds on ultrahot world");
        } else if (surfaceTemp < 100) {
            layer.setComposition("CH4");
            layer.setOpacity("THIN");
            layer.setColor("Pale Blue");
            layer.setDescription("Methane ice haze");
        } else {
            layer.setComposition("H2O");
            layer.setOpacity("MODERATE");
            layer.setColor("White");
            layer.setDescription("Water vapor clouds");
        }

        layer.setAltitudeKm(round2(scaleHeightKm * RandomUtils.rollRange(0.8, 1.2)));
        layer.setThicknessKm(round2(scaleHeightKm * RandomUtils.rollRange(0.2, 0.5)));
        layer.setTemperatureK(round2(Math.max(40, surfaceTemp - 30)));
        return layer;
    }

    // ================================================================
    // CLOUD COVERAGE
    // ================================================================

    private void calculateCloudCoverage(PlanetaryClimate weather, String atmClass,
                                         double surfaceTemp, double pressureAtm,
                                         double waterPercent, double liquidWaterPercent,
                                         double icePercent, boolean tidallyLocked, double meanWindMs) {
        double coverage;

        switch (atmClass) {
            case "VENUS_LIKE":
                // Venus: 100% opaque cloud deck
                coverage = RandomUtils.rollRange(95.0, 100.0);
                break;

            case "JOVIAN":
            case "ICE_GIANT":
                // Gas giants: 100% cloud coverage (multiple cloud layers)
                coverage = 100.0;
                break;

            case "MARS_LIKE":
                // Mars: sparse clouds, mostly clear skies. Mars averages ~2% cloud cover.
                // Thinner atmospheres → clearer skies
                if (pressureAtm < 0.01) {
                    // Very thin (Mars-like, 0.006 atm): mostly clear
                    coverage = RandomUtils.rollRange(1.0, 12.0);
                } else if (pressureAtm < 0.1) {
                    // Thin but not ultra-thin
                    coverage = RandomUtils.rollRange(3.0, 20.0);
                } else {
                    // Thicker Mars-like (approaching Earth thin)
                    coverage = RandomUtils.rollRange(8.0, 30.0);
                }
                // Dust haze can add apparent coverage, but only in warmer/dustier conditions
                if (surfaceTemp > 250 && pressureAtm > 0.005 && RandomUtils.rollD100() <= 30) {
                    coverage += RandomUtils.rollRange(3.0, 10.0);
                }
                break;

            case "TITAN_LIKE":
                // Titan: moderate cloud coverage with extensive haze
                coverage = RandomUtils.rollRange(40.0, 75.0);
                break;

            case "VOLCANIC":
                // Volcanic: variable — driven by eruption level and SO2
                coverage = RandomUtils.rollRange(20.0, 70.0);
                break;

            case "EXOTIC":
                // Ultrahot worlds: silicate/iron cloud decks can be extensive
                if (surfaceTemp > 2000) {
                    coverage = RandomUtils.rollRange(60.0, 100.0);
                } else {
                    coverage = RandomUtils.rollRange(30.0, 70.0);
                }
                break;

            case "EARTH_LIKE":
            default:
                // Earth-like and others: driven by water availability and temperature
                coverage = calculateEarthLikeCloudCoverage(surfaceTemp, pressureAtm,
                        waterPercent, liquidWaterPercent, icePercent, tidallyLocked, meanWindMs);
                break;
        }

        coverage = Math.max(0.0, Math.min(100.0, coverage));
        weather.setCloudCoveragePercent(round2(coverage));
        weather.setCloudCoverageClass(classifyCloudCoverage(coverage));
    }

    private double calculateEarthLikeCloudCoverage(double surfaceTemp, double pressureAtm,
                                                    double waterPercent, double liquidWaterPercent,
                                                    double icePercent, boolean tidallyLocked, double meanWindMs) {
        // Earth baseline: ~67% cloud coverage
        double coverage = 30.0; // Base minimum

        // Water availability is the primary driver
        // Liquid water evaporates → clouds. Ice sublimates slowly → fewer clouds.
        if (liquidWaterPercent > 0) {
            // More ocean → more evaporation → more clouds
            coverage += liquidWaterPercent * 0.6; // 100% ocean → +60%
        } else if (icePercent > 0) {
            // Ice worlds: some sublimation, less cloud formation
            coverage += icePercent * 0.15;
        } else if (waterPercent > 0) {
            coverage += waterPercent * 0.3; // Mixed water coverage
        }

        // Temperature effect: warmer → more evaporation → more clouds (up to a point)
        if (surfaceTemp > 373) {
            // Above boiling: steam atmosphere, very high cloud coverage
            coverage += 20;
        } else if (surfaceTemp > 300) {
            coverage += (surfaceTemp - 300) * 0.2; // Warm tropics effect
        } else if (surfaceTemp < 200) {
            // Very cold: less water vapor available
            coverage *= 0.6;
        }

        // Pressure effect: denser atmosphere holds more moisture
        if (pressureAtm > 2.0) {
            coverage *= 1.1;
        } else if (pressureAtm < 0.5) {
            coverage *= 0.7;
        }

        // Wind effect: stronger winds → more convective cloud formation
        if (meanWindMs > 15) {
            coverage += 5;
        }

        // Tidally locked: cloud pattern is asymmetric, but overall coverage can be high
        // Substellar convection → clouds concentrate at substellar point
        if (tidallyLocked && liquidWaterPercent > 20) {
            coverage += 10; // Strong substellar convection
        }

        // Add natural variance
        coverage *= RandomUtils.rollRange(0.85, 1.15);

        return coverage;
    }

    private String classifyCloudCoverage(double coveragePercent) {
        if (coveragePercent < 5) return "CLEAR";
        if (coveragePercent < 30) return "SCATTERED";
        if (coveragePercent < 60) return "BROKEN";
        if (coveragePercent < 90) return "OVERCAST";
        return "OPAQUE_DECK";
    }

    // ================================================================
    // PRIMARY CLOUD COMPOSITION
    // ================================================================

    private void setPrimaryCloudComposition(PlanetaryClimate weather, List<CloudLayer> cloudLayers) {
        if (cloudLayers.isEmpty()) {
            weather.setPrimaryCloudComposition("NONE");
            return;
        }

        // Primary composition = the most opaque / lowest-altitude layer (most visible)
        // Priority: OPAQUE > THICK > MODERATE > THIN > TRANSPARENT
        CloudLayer primary = cloudLayers.get(0);
        int bestOpacityRank = opacityRank(primary.getOpacity());

        for (CloudLayer layer : cloudLayers) {
            int rank = opacityRank(layer.getOpacity());
            if (rank > bestOpacityRank) {
                primary = layer;
                bestOpacityRank = rank;
            }
        }

        weather.setPrimaryCloudComposition(primary.getComposition());
    }

    private int opacityRank(String opacity) {
        if (opacity == null) return 0;
        switch (opacity) {
            case "OPAQUE": return 5;
            case "THICK": return 4;
            case "MODERATE": return 3;
            case "THIN": return 2;
            case "TRANSPARENT": return 1;
            default: return 0;
        }
    }

    // ================================================================
    // PRECIPITATION GENERATION
    // ================================================================

    private List<PrecipitationType> generatePrecipitationTypes(List<CloudLayer> cloudLayers,
                                                                double surfaceTemp, String atmClass) {
        List<PrecipitationType> precipTypes = new ArrayList<>();

        for (CloudLayer cloud : cloudLayers) {
            // Look up precipitation templates for this cloud substance at this surface temp
            List<PrecipitationTemplate> templates = precipTemplateRepo
                    .findBySubstanceAndSurfaceTemp(cloud.getComposition(), surfaceTemp);

            if (templates.isEmpty()) {
                // Fallback: get all templates for this substance regardless of temp
                templates = precipTemplateRepo.findByCloudSubstance(cloud.getComposition());
            }

            for (PrecipitationTemplate template : templates) {
                // Avoid duplicate substances+phases
                boolean alreadyExists = precipTypes.stream()
                        .anyMatch(p -> p.getSubstance().equals(template.getCloudSubstance())
                                && p.getPhase().equals(template.getPhase()));
                if (alreadyExists) continue;

                PrecipitationType precip = new PrecipitationType();
                precip.setSubstance(template.getCloudSubstance());
                precip.setPhase(template.getPhase());
                precip.setReachesSurface(template.getReachesSurface());
                precip.setDescription(template.getDescription());

                // Determine frequency based on cloud opacity and atmosphere type
                precip.setFrequency(determinePrecipFrequency(cloud, atmClass, surfaceTemp));

                // Determine intensity based on cloud thickness and convection
                precip.setIntensity(determinePrecipIntensity(cloud, atmClass));

                precipTypes.add(precip);
            }
        }

        // Special case: diamond rain for ice giants at extreme internal pressures
        if ("ICE_GIANT".equals(atmClass)) {
            addDiamondRain(precipTypes);
        }

        return precipTypes;
    }

    private String determinePrecipFrequency(CloudLayer cloud, String atmClass, double surfaceTemp) {
        String opacity = cloud.getOpacity() != null ? cloud.getOpacity() : "MODERATE";

        switch (atmClass) {
            case "VENUS_LIKE":
                // Venus: continuous virga (never reaches surface)
                return "CONTINUOUS";
            case "JOVIAN":
            case "ICE_GIANT":
                // Gas giants: continuous precipitation within cloud layers
                return "CONTINUOUS";
            case "MARS_LIKE":
                // Mars: rare — mostly CO2 frost and occasional ice crystal precipitation
                return "RARE";
            case "TITAN_LIKE":
                // Titan: occasional methane rain (monsoon-like pattern)
                return "OCCASIONAL";
            case "EXOTIC":
                // Ultrahot worlds: frequent molten rain on nightside
                return surfaceTemp > 2000 ? "FREQUENT" : "OCCASIONAL";
            default:
                // Earth-like and others: based on cloud opacity
                switch (opacity) {
                    case "OPAQUE":
                    case "THICK":
                        return "FREQUENT";
                    case "MODERATE":
                        return "OCCASIONAL";
                    case "THIN":
                        return "RARE";
                    default:
                        return "RARE";
                }
        }
    }

    private String determinePrecipIntensity(CloudLayer cloud, String atmClass) {
        String opacity = cloud.getOpacity() != null ? cloud.getOpacity() : "MODERATE";

        if ("JOVIAN".equals(atmClass) || "ICE_GIANT".equals(atmClass)) {
            return "HEAVY"; // Massive convective systems
        }
        if ("VENUS_LIKE".equals(atmClass)) {
            return "LIGHT"; // Drizzle/virga
        }
        if ("MARS_LIKE".equals(atmClass)) {
            return "LIGHT"; // Thin atmosphere can't sustain heavy precip
        }

        switch (opacity) {
            case "OPAQUE":
            case "THICK":
                return "HEAVY";
            case "MODERATE":
                return "MODERATE";
            default:
                return "LIGHT";
        }
    }

    private void addDiamondRain(List<PrecipitationType> precipTypes) {
        PrecipitationType diamond = new PrecipitationType();
        diamond.setSubstance("DIAMOND");
        diamond.setPhase("RAIN");
        diamond.setFrequency("CONTINUOUS");
        diamond.setIntensity("MODERATE");
        diamond.setReachesSurface(false); // Occurs deep in interior, not at cloud tops
        diamond.setDescription("Carbon compressed into diamond crystals at extreme interior pressures");
        precipTypes.add(diamond);
    }

    // ================================================================
    // PRECIPITATION SUMMARY
    // ================================================================

    private void calculatePrecipitationSummary(PlanetaryClimate weather, List<PrecipitationType> precipTypes,
                                                String atmClass, double pressureAtm,
                                                double liquidWaterPercent, double waterPercent,
                                                double surfaceTemp) {
        if (precipTypes.isEmpty()) {
            weather.setHasPrecipitation(false);
            weather.setPrimaryPrecipitationType("NONE");
            weather.setPrecipitationFrequency("NEVER");
            weather.setPrecipitationReachesSurface(false);
            return;
        }

        weather.setHasPrecipitation(true);

        // Primary type: the first one that reaches the surface, or the first one overall
        PrecipitationType primary = precipTypes.stream()
                .filter(p -> Boolean.TRUE.equals(p.getReachesSurface()))
                .findFirst()
                .orElse(precipTypes.get(0));

        weather.setPrimaryPrecipitationType(primary.getSubstance() + "_" + primary.getPhase());

        // Overall frequency: use the most frequent type
        weather.setPrecipitationFrequency(getMostFrequent(precipTypes));

        // Does any precipitation reach the surface?
        boolean anyReachesSurface = precipTypes.stream()
                .anyMatch(p -> Boolean.TRUE.equals(p.getReachesSurface()));
        weather.setPrecipitationReachesSurface(anyReachesSurface);
    }

    private String getMostFrequent(List<PrecipitationType> precipTypes) {
        // CONTINUOUS > FREQUENT > OCCASIONAL > RARE > NEVER
        int maxRank = 0;
        for (PrecipitationType p : precipTypes) {
            int rank = frequencyRank(p.getFrequency());
            if (rank > maxRank) maxRank = rank;
        }
        switch (maxRank) {
            case 4: return "CONTINUOUS";
            case 3: return "FREQUENT";
            case 2: return "OCCASIONAL";
            case 1: return "RARE";
            default: return "NEVER";
        }
    }

    private int frequencyRank(String frequency) {
        if (frequency == null) return 0;
        switch (frequency) {
            case "CONTINUOUS": return 4;
            case "FREQUENT": return 3;
            case "OCCASIONAL": return 2;
            case "RARE": return 1;
            default: return 0;
        }
    }

    // ================================================================
    // UTILITY
    // ================================================================

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
