package com.brickroad.starcreator_webservice.utils.planets;

import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.Star;
import com.brickroad.starcreator_webservice.enums.HabitabilityClass;
import com.brickroad.starcreator_webservice.model.climate.PlanetaryClimate;
import com.brickroad.starcreator_webservice.model.climate.PrecipitationType;
import com.brickroad.starcreator_webservice.model.climate.SurfaceDeposit;
import com.brickroad.starcreator_webservice.model.habitability.PlanetaryHabitability;
import com.brickroad.starcreator_webservice.utils.RandomUtils;

import java.util.*;

/**
 * Computes surface deposits — materials that accumulate on the ground from
 * precipitation, volcanic activity, photochemical processes, or biological activity.
 * <p>
 * Provides the renderer with pre-computed data about what coats the surface,
 * eliminating the need to reverse-engineer surface appearance from atmosphere,
 * cloud, and hydrology data.
 * <p>
 * Key factors affecting deposits:
 * <ul>
 *   <li>Precipitation type, frequency, and reachesSurface flag</li>
 *   <li>Atmosphere classification (photochemical haze on Titan-like/Reducing worlds)</li>
 *   <li>Geological activity (dead = deposits accumulate, active = resurfacing)</li>
 *   <li>Cryovolcanism (pushes fresh ice to surface)</li>
 *   <li>Habitability + star type (vegetation color depends on stellar spectrum)</li>
 *   <li>Volatile type + temperature (determines what rain chemistry does to soil)</li>
 * </ul>
 */
public final class SurfaceDepositCalculator {

    private SurfaceDepositCalculator() {}

    // ── Deposit type constants ──
    private static final String WATER_ICE = "WATER_ICE";
    private static final String WATER_FROST = "WATER_FROST";
    private static final String METHANE_ICE = "METHANE_ICE";
    private static final String AMMONIA_ICE = "AMMONIA_ICE";
    private static final String CO2_ICE = "CO2_ICE";
    private static final String HYDROCARBON_THOLIN = "HYDROCARBON_THOLIN";
    private static final String SULFUR = "SULFUR";
    private static final String VOLCANIC_ASH = "VOLCANIC_ASH";
    private static final String FRESH_VOLATILE_ICE = "FRESH_VOLATILE_ICE";
    private static final String IRON_OXIDE_DUST = "IRON_OXIDE_DUST";
    private static final String WEATHERED_SOIL = "WEATHERED_SOIL";
    private static final String VEGETATION = "VEGETATION";
    private static final String AMMONIA_BIOME = "AMMONIA_BIOME";

    // ── Source constants ──
    private static final String SRC_PRECIPITATION = "PRECIPITATION";
    private static final String SRC_PHOTOCHEMICAL = "PHOTOCHEMICAL";
    private static final String SRC_CRYOVOLCANISM = "CRYOVOLCANISM";
    private static final String SRC_VOLCANIC = "VOLCANIC";
    private static final String SRC_BIOGENIC = "BIOGENIC";

    // ── Melting points for temperature checks (K) ──
    private static final double WATER_ICE_MELT = 273.16;
    private static final double METHANE_ICE_MELT = 90.7;
    private static final double AMMONIA_ICE_MELT = 195.4;
    private static final double CO2_ICE_MELT = 194.65;

    /**
     * Compute surface deposits for a planet or moon proxy.
     *
     * @param climate    fully populated climate (must have precipitation data)
     * @param planet     planet or moon proxy (terrain, hydrology, habitability, atmosphere)
     * @param parentStar parent star (for vegetation color derivation)
     * @return list of surface deposits, sorted by dominance
     */
    public static List<SurfaceDeposit> calculate(PlanetaryClimate climate, Planet planet, Star parentStar) {
        if (climate == null || planet == null) return Collections.emptyList();

        double surfaceTemp = planet.getSurfaceTemp() != null ? planet.getSurfaceTemp() : 288.0;
        double liquidCoverage = planet.getLiquidSurfaceCoveragePercent() != null
                ? planet.getLiquidSurfaceCoveragePercent() : 0.0;
        String geoActivity = planet.getGeologicalActivity();
        String atmClass = planet.getAtmosphereClassification();
        String volatileType = planet.getVolatileType();

        // Accumulate deposits in a map (depositType → deposit) to merge duplicates
        Map<String, SurfaceDeposit> deposits = new LinkedHashMap<>();

        // ── Step 1: Frozen/solid deposits from precipitation ──
        addPrecipitationDeposits(deposits, climate, surfaceTemp);

        // ── Step 1b: Photochemical deposits from atmosphere classification ──
        addPhotochemicalDeposits(deposits, atmClass, planet);

        // ── Step 1c: Intrinsic surface ice for ice-rich/mixed-ice compositions ──
        addCompositionIce(deposits, planet, surfaceTemp);

        // ── Step 2: Volcanic/cryovolcanic deposits ──
        addVolcanicDeposits(deposits, planet, volatileType);

        // ── Step 3: Liquid rain effects — weathering and vegetation ──
        addLiquidRainEffects(deposits, climate, planet, parentStar);

        // ── Step 4: Geological activity modifier ──
        applyGeologicalModifier(deposits, geoActivity);

        // ── Step 5: Wind redistribution — loose deposits spread by wind ──
        applyWindRedistribution(deposits, climate);

        // ── Step 6: Temperature check ──
        applyTemperatureCheck(deposits, surfaceTemp);

        // ── Step 7: Normalize, rank, and filter ──
        return normalizeAndRank(deposits, liquidCoverage);
    }

    // =========================================================================
    // Step 1: Precipitation deposits
    // =========================================================================

    private static void addPrecipitationDeposits(Map<String, SurfaceDeposit> deposits,
                                                  PlanetaryClimate climate, double surfaceTemp) {
        if (climate.getPrecipitationTypes() == null) return;

        for (PrecipitationType precip : climate.getPrecipitationTypes()) {
            if (!Boolean.TRUE.equals(precip.getReachesSurface())) continue;

            String substance = precip.getSubstance();
            String phase = precip.getPhase();
            if (substance == null || phase == null) continue;

            // Only frozen/solid precipitation accumulates as deposits
            // Liquid rain is handled separately (weathering, vegetation)
            if (isSolidPhase(phase)) {
                String depositType = mapSubstanceToDeposit(substance, phase);
                if (depositType != null) {
                    double baseCoverage = frequencyToCoverage(precip.getFrequency())
                            * intensityMultiplier(precip.getIntensity());
                    String color = depositColor(depositType);
                    String desc = describeDeposit(depositType, substance, precip.getFrequency());

                    mergeDeposit(deposits, depositType, SRC_PRECIPITATION, color, baseCoverage, desc);
                }
            }
        }
    }

    private static boolean isSolidPhase(String phase) {
        return "SNOW".equalsIgnoreCase(phase) || "HAIL".equalsIgnoreCase(phase)
                || "SLEET".equalsIgnoreCase(phase);
    }

    private static String mapSubstanceToDeposit(String substance, String phase) {
        if (substance == null) return null;
        return switch (substance.toUpperCase()) {
            case "H2O", "H2O_ICE" -> WATER_ICE;
            case "CH4" -> METHANE_ICE;
            case "NH3" -> AMMONIA_ICE;
            case "CO2_ICE", "CO2" -> CO2_ICE;
            case "HYDROCARBON_HAZE" -> HYDROCARBON_THOLIN;
            case "SO2" -> SULFUR;
            case "SULFUR" -> SULFUR;
            default -> null; // Iron rain, diamond rain, etc. — too exotic or don't accumulate
        };
    }

    // =========================================================================
    // Step 1b: Photochemical deposits (atmosphere-driven)
    // =========================================================================

    private static void addPhotochemicalDeposits(Map<String, SurfaceDeposit> deposits,
                                                  String atmClass, Planet planet) {
        if (atmClass == null) return;

        // Titan-like or Reducing atmospheres produce photochemical haze
        // that settles continuously regardless of explicit precipitation templates
        if ("TITAN_LIKE".equals(atmClass) || "REDUCING".equals(atmClass)) {
            double pressure = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 1.0;
            // Denser atmosphere = more haze production and fallout
            double pressureFactor = Math.min(2.0, Math.max(0.3, pressure / 1.5));
            double baseCoverage = (30.0 + RandomUtils.rollRange(0.0, 1.0) * 30.0) * pressureFactor;
            baseCoverage = Math.min(80.0, baseCoverage);

            mergeDeposit(deposits, HYDROCARBON_THOLIN, SRC_PHOTOCHEMICAL, "#CC8833",
                    baseCoverage, "Organic tholin deposits from photochemical haze fallout");
        }

        // Mars-like thin atmosphere with iron-bearing surface → iron oxide dust
        if ("MARS_LIKE".equals(atmClass)) {
            String compClass = planet.getCompositionClassification();
            if ("SILICATE_RICH".equals(compClass) || "IRON_RICH".equals(compClass)) {
                double coverage = 15.0 + RandomUtils.rollRange(0.0, 1.0) * 25.0;
                mergeDeposit(deposits, IRON_OXIDE_DUST, SRC_PHOTOCHEMICAL, "#CC6633",
                        coverage, "Iron oxide dust from surface oxidation");
            }
        }
    }

    // =========================================================================
    // Step 1c: Intrinsic surface ice from composition
    // =========================================================================

    /**
     * Ice worlds and mixed-ice bodies have surface ice as an intrinsic part of
     * their composition — it doesn't need to fall from precipitation.
     * Only applies when the surface is cold enough to keep ice frozen.
     */
    private static void addCompositionIce(Map<String, SurfaceDeposit> deposits,
                                           Planet planet, double surfaceTemp) {
        String compClass = planet.getCompositionClassification();
        if (compClass == null) return;

        String volatileType = planet.getVolatileType();
        double liquidCoverage = planet.getLiquidSurfaceCoveragePercent() != null
                ? planet.getLiquidSurfaceCoveragePercent() : 0.0;

        boolean isIceRich = "ICE_RICH".equals(compClass);
        boolean isMixedIce = "MIXED_SILICATE_ICE".equals(compClass);
        if (!isIceRich && !isMixedIce) return;

        // Base coverage: ice-rich bodies are mostly ice, mixed have significant ice
        double baseCoverage = isIceRich
                ? 50.0 + RandomUtils.rollRange(0.0, 1.0) * 30.0
                : 20.0 + RandomUtils.rollRange(0.0, 1.0) * 20.0;

        // Reduce by liquid coverage (liquid = melted ice, not available as deposit)
        baseCoverage = Math.max(0, baseCoverage - liquidCoverage);

        if (baseCoverage < 1.0) return;

        // Determine ice type from volatile type
        String depositType;
        String color;
        String desc;
        if ("METHANE".equals(volatileType) || "METHANE_ETHANE".equals(volatileType)) {
            depositType = METHANE_ICE;
            color = depositColor(METHANE_ICE);
            desc = "Exposed methane ice from surface composition";
        } else if ("AMMONIA".equals(volatileType)) {
            depositType = AMMONIA_ICE;
            color = depositColor(AMMONIA_ICE);
            desc = "Exposed ammonia ice from surface composition";
        } else {
            // Default to water ice (most common volatile)
            depositType = WATER_ICE;
            color = depositColor(WATER_ICE);
            desc = "Exposed water ice from surface composition";
        }

        mergeDeposit(deposits, depositType, "COMPOSITION", color, baseCoverage, desc);
    }

    // =========================================================================
    // Step 2: Volcanic/cryovolcanic deposits
    // =========================================================================

    private static void addVolcanicDeposits(Map<String, SurfaceDeposit> deposits,
                                             Planet planet, String volatileType) {
        // Cryovolcanism → fresh volatile ice
        boolean hasCryo = planet.getTerrain() != null
                && Boolean.TRUE.equals(planet.getTerrain().getHasCryovolcanism());
        if (hasCryo) {
            String intensity = planet.getVolcanicIntensity();
            double coverage = intensityToCoverage(intensity);
            String color = freshIceColor(volatileType);
            mergeDeposit(deposits, FRESH_VOLATILE_ICE, SRC_CRYOVOLCANISM, color,
                    coverage, "Fresh " + volatileDisplayName(volatileType)
                            + " ice from cryovolcanic eruptions");
        }

        // Silicate volcanism → ash
        if (Boolean.TRUE.equals(planet.getHasVolcanicActivity())) {
            String volcType = planet.getVolcanismType();
            String intensity = planet.getVolcanicIntensity();
            if (volcType != null && !volcType.contains("Cryo")) {
                if ("Continuous".equals(intensity) || "Moderate".equals(intensity)) {
                    double coverage = "Continuous".equals(intensity)
                            ? 10.0 + RandomUtils.rollRange(0.0, 1.0) * 10.0
                            : 3.0 + RandomUtils.rollRange(0.0, 1.0) * 7.0;

                    if (volcType.contains("Sulfur") || volcType.contains("sulfur")) {
                        mergeDeposit(deposits, SULFUR, SRC_VOLCANIC, "#CCAA22",
                                coverage, "Sulfur deposits from volcanic outgassing");
                    } else {
                        mergeDeposit(deposits, VOLCANIC_ASH, SRC_VOLCANIC, "#555555",
                                coverage, "Volcanic ash fallout from active eruptions");
                    }
                }
            }
        }
    }

    private static double intensityToCoverage(String intensity) {
        if (intensity == null) return 5.0;
        return switch (intensity) {
            case "Continuous" -> 25.0 + RandomUtils.rollRange(0.0, 1.0) * 15.0;
            case "Moderate" -> 10.0 + RandomUtils.rollRange(0.0, 1.0) * 15.0;
            case "Rare" -> 3.0 + RandomUtils.rollRange(0.0, 1.0) * 7.0;
            default -> 5.0 + RandomUtils.rollRange(0.0, 1.0) * 5.0;
        };
    }

    // =========================================================================
    // Step 3: Liquid rain effects — weathering and vegetation
    // =========================================================================

    private static void addLiquidRainEffects(Map<String, SurfaceDeposit> deposits,
                                              PlanetaryClimate climate, Planet planet,
                                              Star parentStar) {
        if (climate.getPrecipitationTypes() == null) return;

        // Find liquid precipitation that reaches surface
        PrecipitationType liquidPrecip = null;
        for (PrecipitationType p : climate.getPrecipitationTypes()) {
            if (Boolean.TRUE.equals(p.getReachesSurface()) && isLiquidPhase(p.getPhase())) {
                liquidPrecip = p;
                break;
            }
        }
        if (liquidPrecip == null) return;

        String substance = liquidPrecip.getSubstance();
        String frequency = liquidPrecip.getFrequency();
        double rainCoverage = frequencyToRainCoverage(frequency)
                * intensityMultiplier(liquidPrecip.getIntensity());

        // ── Weathered soil from liquid rain ──
        addWeatheredSoil(deposits, substance, planet, rainCoverage);

        // ── Vegetation (biogenic) ──
        addVegetation(deposits, planet, parentStar, substance, frequency, rainCoverage);
    }

    private static boolean isLiquidPhase(String phase) {
        return "RAIN".equalsIgnoreCase(phase) || "DRIZZLE".equalsIgnoreCase(phase);
    }

    private static void addWeatheredSoil(Map<String, SurfaceDeposit> deposits,
                                          String substance, Planet planet, double rainCoverage) {
        if (substance == null) return;
        String sub = substance.toUpperCase();

        // Methane rain doesn't chemically weather — non-polar solvent
        if ("CH4".equals(sub)) return;

        String compClass = planet.getCompositionClassification();
        String color;
        String desc;

        if ("NH3".equals(sub)) {
            // Ammonia rain: aggressive base weathering → grey-blue ammonium minerals
            color = "#8899AA";
            desc = "Grey-blue ammonium silicate soil from ammonia rain weathering";
        } else if ("H2SO4".equals(sub)) {
            // Sulfuric acid rain (Venus-like that reaches surface)
            color = "#AAAA66";
            desc = "Sulfate mineral deposits from acid rain weathering";
        } else if ("IRON_RICH".equals(compClass) || "SILICATE_RICH".equals(compClass)) {
            // Water rain on iron-bearing rock → iron oxide (rust)
            color = "#CC6633";
            desc = "Iron oxide laterite from water rain weathering";
        } else {
            // Water rain on other compositions → brown clay
            color = "#887755";
            desc = "Weathered clay soil from water rain erosion";
        }

        mergeDeposit(deposits, WEATHERED_SOIL, SRC_PRECIPITATION, color,
                rainCoverage * 0.5, desc);
    }

    private static void addVegetation(Map<String, SurfaceDeposit> deposits,
                                       Planet planet, Star parentStar,
                                       String substance, String frequency, double rainCoverage) {
        PlanetaryHabitability hab = planet.getHabitability();
        if (hab == null) return;

        HabitabilityClass habClass = hab.getHabitabilityClass();
        String lifePotential = hab.getLifeComplexityPotential();

        // Check for ammonia biome (separate from water-based vegetation)
        String volatileType = planet.getVolatileType();
        if (("AMMONIA".equals(volatileType) || "AMMONIA_WATER".equals(volatileType))
                && isLifeCapable(lifePotential, "MICROBIAL_LIKELY")) {
            double coverage = 5.0 + RandomUtils.rollRange(0.0, 1.0) * 20.0;
            coverage *= frequencyMultiplier(frequency) * habitabilityMultiplier(habClass);
            coverage = Math.min(25.0, coverage);
            if (coverage >= 1.0) {
                mergeDeposit(deposits, AMMONIA_BIOME, SRC_BIOGENIC, "#8899AA",
                        coverage, "Hypothetical ammonia-based surface biome in temperate zones");
            }
        }

        // Water-based vegetation requires multicellular life potential
        if (!isLifeCapable(lifePotential, "SIMPLE_MULTICELLULAR")) return;
        if (habClass == null) return;
        if (habClass != HabitabilityClass.EARTH_ANALOG
                && habClass != HabitabilityClass.HABITABLE_MARGINAL
                && habClass != HabitabilityClass.BIOSPHERE_POSSIBLE) return;

        // Only water/ammonia-water rain supports conventional vegetation
        if (substance == null) return;
        String sub = substance.toUpperCase();
        if (!"H2O".equals(sub) && !"H2O_ICE".equals(sub) && !"NH3".equals(sub)) return;

        double baseCoverage = rainCoverage * frequencyMultiplier(frequency) * habitabilityMultiplier(habClass);
        baseCoverage = Math.min(65.0, baseCoverage);
        if (baseCoverage < 1.0) return;

        String vegColor = vegetationColorForStar(parentStar);
        String starDesc = starTypeDescription(parentStar);

        mergeDeposit(deposits, VEGETATION, SRC_BIOGENIC, vegColor,
                baseCoverage, starDesc + " photosynthetic ground cover");
    }

    /**
     * Vegetation color based on parent star spectral type.
     * From real astrobiology research (Kiang et al. 2007):
     * photosynthetic pigments evolve to capture the star's peak spectral output.
     */
    private static String vegetationColorForStar(Star star) {
        if (star == null) return "#447744"; // Default green (G-type)

        String type = star.getType();
        if (type == null) return "#447744";

        if (type.contains(" M") || type.contains("Brown Dwarf")) {
            // M-dwarf / Brown Dwarf: absorbs ALL visible → black/very dark red
            return "#2D1F1F";
        }
        if (type.contains(" K")) {
            // K-dwarf: absorbs blue-green → dark red/maroon
            return "#6B2D2D";
        }
        if (type.contains(" G")) {
            // G-dwarf (Sun-like): absorbs red+blue → green (Earth baseline)
            return "#447744";
        }
        if (type.contains(" F")) {
            // F-dwarf: absorbs UV/blue → yellow-orange
            return "#AA8833";
        }
        if (type.contains(" A")) {
            // A-dwarf: deep UV pigments → blue-purple
            return "#554488";
        }
        if (type.contains(" B") || type.contains(" O")) {
            // Very hot stars: extreme UV → deep purple
            return "#443366";
        }

        return "#447744"; // Fallback green
    }

    private static String starTypeDescription(Star star) {
        if (star == null) return "Green";
        String type = star.getType();
        if (type == null) return "Green";
        if (type.contains(" M") || type.contains("Brown Dwarf")) return "Dark";
        if (type.contains(" K")) return "Maroon";
        if (type.contains(" G")) return "Green";
        if (type.contains(" F")) return "Golden";
        if (type.contains(" A")) return "Blue-tinted";
        if (type.contains(" B") || type.contains(" O")) return "Purple-tinted";
        return "Green";
    }

    private static boolean isLifeCapable(String lifePotential, String minimumLevel) {
        if (lifePotential == null) return false;
        int level = lifeComplexityRank(lifePotential);
        int required = lifeComplexityRank(minimumLevel);
        return level >= required;
    }

    private static int lifeComplexityRank(String level) {
        if (level == null) return 0;
        return switch (level) {
            case "NONE" -> 0;
            case "MICROBIAL_ONLY" -> 1;
            case "MICROBIAL_LIKELY" -> 2;
            case "COMPLEX_POSSIBLE" -> 3;
            case "SIMPLE_MULTICELLULAR" -> 4;
            case "COMPLEX_MULTICELLULAR" -> 5;
            default -> 0;
        };
    }

    private static double habitabilityMultiplier(HabitabilityClass habClass) {
        if (habClass == null) return 0.3;
        return switch (habClass) {
            case EARTH_ANALOG -> 1.0;
            case HABITABLE_MARGINAL -> 0.7;
            case BIOSPHERE_POSSIBLE -> 0.4;
            default -> 0.2;
        };
    }

    private static double frequencyMultiplier(String frequency) {
        if (frequency == null) return 0.5;
        return switch (frequency.toUpperCase()) {
            case "CONTINUOUS" -> 1.0;
            case "FREQUENT" -> 0.75;
            case "OCCASIONAL" -> 0.4;
            case "RARE" -> 0.15;
            default -> 0.3;
        };
    }

    /**
     * How much precipitation volume per event affects deposit accumulation.
     * LIGHT drizzle deposits less per cycle than HEAVY downpours.
     */
    private static double intensityMultiplier(String intensity) {
        if (intensity == null) return 1.0;
        return switch (intensity.toUpperCase()) {
            case "LIGHT" -> 0.6;
            case "MODERATE" -> 1.0;
            case "HEAVY" -> 1.5;
            case "EXTREME" -> 2.0;
            default -> 1.0;
        };
    }

    // =========================================================================
    // Step 4: Geological activity modifier
    // =========================================================================

    private static void applyGeologicalModifier(Map<String, SurfaceDeposit> deposits, String geoActivity) {
        if (geoActivity == null) return;

        double modifier = switch (geoActivity) {
            case "Geologically Dead" -> 1.3;
            case "Low Activity" -> 1.1;
            case "Moderately Active" -> 0.85;
            case "Highly Active" -> 0.6;
            default -> 1.0;
        };

        // Inverse modifier for volcanic/cryovolcanic output
        double volcModifier = switch (geoActivity) {
            case "Geologically Dead" -> 0.5;
            case "Low Activity" -> 0.6;
            case "Moderately Active" -> 1.0;
            case "Highly Active" -> 1.4;
            default -> 1.0;
        };

        for (SurfaceDeposit d : deposits.values()) {
            if (FRESH_VOLATILE_ICE.equals(d.getDepositType())
                    || VOLCANIC_ASH.equals(d.getDepositType())
                    || SULFUR.equals(d.getDepositType())) {
                d.setCoveragePercent(d.getCoveragePercent() * volcModifier);
            } else {
                d.setCoveragePercent(d.getCoveragePercent() * modifier);
            }
        }
    }

    // =========================================================================
    // Step 5: Wind redistribution
    // =========================================================================

    /**
     * Wind spreads loose, fine-grained deposits (dust, ash, tholin) over wider areas.
     * Stronger winds = more redistribution = higher coverage but thinner layers.
     * Anchored deposits (ice, vegetation, weathered soil) are not affected.
     * <p>
     * Dust storms are a special case — global dust storms (Mars-like) can push
     * loose deposit coverage toward near-total surface coverage.
     */
    private static void applyWindRedistribution(Map<String, SurfaceDeposit> deposits,
                                                  PlanetaryClimate climate) {
        if (climate == null) return;

        String windIntensity = climate.getWindIntensity();
        boolean hasDustStorms = Boolean.TRUE.equals(climate.getHasDustStorms());
        boolean globalDustStorms = Boolean.TRUE.equals(climate.getDustStormsCanBeGlobal());

        // Wind spread factor for loose materials
        double windFactor = windSpreadFactor(windIntensity);

        for (SurfaceDeposit d : deposits.values()) {
            if (!isWindMobile(d.getDepositType())) continue;

            // Wind spreads loose deposits: higher wind = wider but thinner coverage
            d.setCoveragePercent(d.getCoveragePercent() * windFactor);

            // Dust storms massively boost loose dust/ash coverage
            if (hasDustStorms) {
                if (IRON_OXIDE_DUST.equals(d.getDepositType())
                        || VOLCANIC_ASH.equals(d.getDepositType())) {
                    double stormBoost = globalDustStorms ? 2.0 : 1.4;
                    d.setCoveragePercent(d.getCoveragePercent() * stormBoost);
                }
            }
        }
    }

    /**
     * Whether a deposit type can be picked up and redistributed by wind.
     * Fine-grained, loose materials are mobile; ice, vegetation, and soil are anchored.
     */
    private static boolean isWindMobile(String depositType) {
        if (depositType == null) return false;
        return switch (depositType) {
            case IRON_OXIDE_DUST, VOLCANIC_ASH, HYDROCARBON_THOLIN, SULFUR -> true;
            default -> false;
        };
    }

    /**
     * Wind spread factor — how much wind increases coverage of loose deposits.
     * CALM winds barely move material; HURRICANE-force winds spread it everywhere.
     */
    private static double windSpreadFactor(String windIntensity) {
        if (windIntensity == null) return 1.0;
        return switch (windIntensity) {
            case "CALM" -> 0.8;        // Barely moves, deposits stay where they fall
            case "LIGHT" -> 1.0;       // Baseline — gentle redistribution
            case "MODERATE" -> 1.15;   // Noticeable spread
            case "STRONG" -> 1.35;     // Significant redistribution
            case "EXTREME" -> 1.6;     // Deposits blown across large areas
            case "HURRICANE" -> 1.9;   // Near-global redistribution of fine particles
            default -> 1.0;
        };
    }

    // =========================================================================
    // Step 6: Temperature check
    // =========================================================================

    private static void applyTemperatureCheck(Map<String, SurfaceDeposit> deposits, double surfaceTemp) {
        Iterator<Map.Entry<String, SurfaceDeposit>> it = deposits.entrySet().iterator();
        while (it.hasNext()) {
            SurfaceDeposit d = it.next().getValue();
            Double meltPoint = meltingPoint(d.getDepositType());
            if (meltPoint == null) continue; // Non-frozen deposits (vegetation, soil) — skip

            if (surfaceTemp > meltPoint) {
                it.remove(); // Above melting → substance is liquid, not a deposit
            } else if (surfaceTemp > meltPoint - 20.0) {
                d.setCoveragePercent(d.getCoveragePercent() * 0.5); // Near melting → partial melt
            }
        }
    }

    private static Double meltingPoint(String depositType) {
        if (depositType == null) return null;
        return switch (depositType) {
            case WATER_ICE, WATER_FROST, FRESH_VOLATILE_ICE -> WATER_ICE_MELT;
            case METHANE_ICE -> METHANE_ICE_MELT;
            case AMMONIA_ICE -> AMMONIA_ICE_MELT;
            case CO2_ICE -> CO2_ICE_MELT;
            default -> null; // Tholins, ash, vegetation, soil — no melting check
        };
    }

    // =========================================================================
    // Step 7: Normalize, rank, and filter
    // =========================================================================

    private static List<SurfaceDeposit> normalizeAndRank(Map<String, SurfaceDeposit> deposits,
                                                          double liquidCoverage) {
        if (deposits.isEmpty()) return Collections.emptyList();

        double availableSurface = Math.max(0, 100.0 - liquidCoverage);

        // Cap total deposit coverage to available surface
        double totalCoverage = deposits.values().stream()
                .mapToDouble(d -> d.getCoveragePercent() != null ? d.getCoveragePercent() : 0)
                .sum();
        if (totalCoverage > availableSurface && totalCoverage > 0) {
            double scale = availableSurface / totalCoverage;
            deposits.values().forEach(d -> d.setCoveragePercent(d.getCoveragePercent() * scale));
        }

        // Filter out tiny deposits
        List<SurfaceDeposit> result = new ArrayList<>();
        for (SurfaceDeposit d : deposits.values()) {
            if (d.getCoveragePercent() != null && d.getCoveragePercent() >= 0.5) {
                d.setCoveragePercent(Math.round(d.getCoveragePercent() * 10.0) / 10.0);
                d.setThickness(coverageToThickness(d.getCoveragePercent()));
                result.add(d);
            }
        }

        // Sort by coverage descending, assign rank
        result.sort((a, b) -> Double.compare(b.getCoveragePercent(), a.getCoveragePercent()));
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setDominanceRank(i + 1);
        }

        return result;
    }

    private static String coverageToThickness(double coverage) {
        if (coverage >= 40) return "THICK";
        if (coverage >= 15) return "MODERATE";
        if (coverage >= 5) return "THIN_LAYER";
        return "DUSTING";
    }

    // =========================================================================
    // Helper methods
    // =========================================================================

    private static double frequencyToCoverage(String frequency) {
        if (frequency == null) return 10.0;
        return switch (frequency.toUpperCase()) {
            case "CONTINUOUS" -> 50.0 + RandomUtils.rollRange(0.0, 1.0) * 30.0;
            case "FREQUENT" -> 25.0 + RandomUtils.rollRange(0.0, 1.0) * 25.0;
            case "OCCASIONAL" -> 5.0 + RandomUtils.rollRange(0.0, 1.0) * 20.0;
            case "RARE" -> 1.0 + RandomUtils.rollRange(0.0, 1.0) * 4.0;
            default -> 5.0 + RandomUtils.rollRange(0.0, 1.0) * 10.0;
        };
    }

    private static double frequencyToRainCoverage(String frequency) {
        if (frequency == null) return 20.0;
        return switch (frequency.toUpperCase()) {
            case "CONTINUOUS" -> 60.0 + RandomUtils.rollRange(0.0, 1.0) * 20.0;
            case "FREQUENT" -> 35.0 + RandomUtils.rollRange(0.0, 1.0) * 25.0;
            case "OCCASIONAL" -> 10.0 + RandomUtils.rollRange(0.0, 1.0) * 20.0;
            case "RARE" -> 2.0 + RandomUtils.rollRange(0.0, 1.0) * 8.0;
            default -> 15.0 + RandomUtils.rollRange(0.0, 1.0) * 15.0;
        };
    }

    private static String depositColor(String depositType) {
        if (depositType == null) return "#888888";
        return switch (depositType) {
            case WATER_ICE -> "#DDEEFF";
            case WATER_FROST -> "#CCDDEE";
            case METHANE_ICE -> "#AA8855";
            case AMMONIA_ICE -> "#BBBBCC";
            case CO2_ICE -> "#EEEEFF";
            case HYDROCARBON_THOLIN -> "#CC8833";
            case SULFUR -> "#CCAA22";
            case VOLCANIC_ASH -> "#555555";
            case FRESH_VOLATILE_ICE -> "#BBDDEE";
            case IRON_OXIDE_DUST -> "#CC6633";
            default -> "#888888";
        };
    }

    private static String freshIceColor(String volatileType) {
        if (volatileType == null) return "#BBDDEE";
        return switch (volatileType) {
            case "WATER", "AMMONIA_WATER" -> "#BBDDEE";
            case "AMMONIA" -> "#AABBCC";
            case "METHANE", "METHANE_ETHANE" -> "#BBAA88";
            default -> "#BBDDEE";
        };
    }

    private static String volatileDisplayName(String volatileType) {
        if (volatileType == null) return "volatile";
        return switch (volatileType) {
            case "WATER" -> "water";
            case "AMMONIA_WATER" -> "ammonia-water";
            case "AMMONIA" -> "ammonia";
            case "METHANE" -> "methane";
            case "METHANE_ETHANE" -> "methane-ethane";
            default -> "volatile";
        };
    }

    private static String describeDeposit(String depositType, String substance, String frequency) {
        String freq = frequency != null ? frequency.toLowerCase() : "periodic";
        return switch (depositType) {
            case WATER_ICE -> "Water ice from " + freq + " snowfall";
            case METHANE_ICE -> "Methane ice from " + freq + " methane snow";
            case AMMONIA_ICE -> "Ammonia ice from " + freq + " ammonia precipitation";
            case CO2_ICE -> "Dry ice deposits from " + freq + " CO2 snow";
            case HYDROCARBON_THOLIN -> "Hydrocarbon deposits from " + freq + " organic drizzle";
            case SULFUR -> "Sulfur deposits from " + freq + " sulfurous precipitation";
            default -> substance + " deposits";
        };
    }

    /**
     * Merge a deposit into the map. If a deposit of the same type already exists,
     * boost its coverage rather than creating a duplicate.
     */
    private static void mergeDeposit(Map<String, SurfaceDeposit> deposits,
                                      String type, String source, String color,
                                      double coverage, String description) {
        SurfaceDeposit existing = deposits.get(type);
        if (existing != null) {
            // Merge: take the higher coverage + a bonus for having multiple sources
            double merged = Math.max(existing.getCoveragePercent(), coverage)
                    + Math.min(existing.getCoveragePercent(), coverage) * 0.3;
            existing.setCoveragePercent(merged);
            // Keep the more informative description
            if (description.length() > existing.getDescription().length()) {
                existing.setDescription(description);
            }
        } else {
            deposits.put(type, new SurfaceDeposit(type, source, color, coverage, description));
        }
    }
}
