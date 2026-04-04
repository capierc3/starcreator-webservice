package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ref.TerrainCategoryRef;
import com.brickroad.starcreator_webservice.entity.ref.TerrainTypeRef;
import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.TerrainDistribution;
import com.brickroad.starcreator_webservice.entity.ud.TerrainProperties;
import com.brickroad.starcreator_webservice.entity.ud.HydrologyProperties;
import com.brickroad.starcreator_webservice.repository.TerrainCategoryRefRepository;
import com.brickroad.starcreator_webservice.repository.TerrainTypeRefRepository;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates terrain distribution and reconciles surface features with water coverage.
 * <p>
 * Runs AFTER both geology (GeologyCreator) and hydrology (HydrologyCreator) have been set on the
 * planet, so it has access to accurate liquid coverage data for the liquid/land terrain split.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Terrain distribution — picking terrain types and assigning coverage percentages</li>
 *   <li>Elevation backfill — deriving elevation from mountain terrain when templates didn't set it</li>
 *   <li>Water-world depth — setting deep min elevation for ocean-dominant worlds</li>
 *   <li>Cratering refinement — reducing visible craters hidden under water/ice</li>
 *   <li>ICE reconciliation — syncing ice terrain coverage with water ice coverage</li>
 * </ul>
 */
@Service
public class SurfaceCreator {

    @Autowired
    private TerrainTypeRefRepository terrainTypeRefRepository;

    @Autowired
    private TerrainCategoryRefRepository terrainCategoryRepository;

    private Map<String, TerrainTypeRef> cachedTerrainTypes;
    private Map<String, TerrainCategoryRef> cachedTerrainCategories;

    @PostConstruct
    public void init() {
        cachedTerrainTypes = terrainTypeRefRepository.findAll().stream()
                .collect(Collectors.toMap(TerrainTypeRef::getName, t -> t));
        cachedTerrainCategories = terrainCategoryRepository.findAll().stream()
                .collect(Collectors.toMap(TerrainCategoryRef::getCategory, c -> c));
    }

    /**
     * Generates terrain distribution and reconciles surface data for a planet.
     * Must be called after both geology and water properties are set.
     */
    public void createPlanetSurface(Planet planet) {
        if (isGasGiant(planet.getPlanetType())) {
            return;
        }

        TerrainProperties terrain = planet.getTerrain();
        if (terrain == null) {
            return;
        }

        // 1. Generate terrain distribution with real water coverage
        List<TerrainDistribution> terrainDist = generateTerrainDistribution(planet, terrain);
        terrain.setTerrainDistribution(terrainDist);

        // 2. Elevation backfill from mountain terrain coverage (when template didn't set it)
        if (!terrainDist.isEmpty() && terrain.getMaxElevationKm() == null) {
            backfillElevationFromTerrain(terrain, terrainDist);
        }

        // 3. Liquid-world min elevation — deep ocean floors for liquid-dominant worlds
        Double liquidCoverage = planet.getLiquidCoveragePercent();
        if (liquidCoverage != null && liquidCoverage > 50) {
            terrain.setMinElevationKm(-RandomUtils.rollRange(3.0, 11.0));
        }

        // 4. Refine cratering for liquid coverage — craters under liquid aren't visible
        refineCrateringForLiquidCoverage(planet, terrain);

        // 5. Reconcile ICE terrain with frozen liquid coverage
        reconcileFrozenLiquidCoverage(planet, terrain, terrainDist);
    }

    // ── Elevation Backfill ──────────────────────────────────────────────────────

    private void backfillElevationFromTerrain(TerrainProperties terrain, List<TerrainDistribution> terrainDist) {
        double mountainCoverage = calculateMountainCoverage(terrainDist);
        terrain.setMountainCoveragePercent(mountainCoverage);

        if (mountainCoverage > 15) {
            terrain.setMaxElevationKm(RandomUtils.rollRange(4.0, 8.0));
            terrain.setMinElevationKm(RandomUtils.rollRange(-3.0, -1.0));
            terrain.setAverageElevationKm(RandomUtils.rollRange(0.0, 2.0));
            terrain.setTerrainRoughness(RandomUtils.rollRange(3.0, 6.0));
        } else if (mountainCoverage > 5) {
            terrain.setMaxElevationKm(RandomUtils.rollRange(2.0, 5.0));
            terrain.setMinElevationKm(RandomUtils.rollRange(-2.0, -0.5));
            terrain.setAverageElevationKm(RandomUtils.rollRange(-0.5, 1.0));
            terrain.setTerrainRoughness(RandomUtils.rollRange(2.0, 4.0));
        } else {
            terrain.setMaxElevationKm(RandomUtils.rollRange(0.5, 2.0));
            terrain.setMinElevationKm(RandomUtils.rollRange(-1.0, -0.2));
            terrain.setAverageElevationKm(RandomUtils.rollRange(-0.2, 0.5));
            terrain.setTerrainRoughness(RandomUtils.rollRange(1.0, 2.5));
        }
    }

    // ── Cratering Refinement ────────────────────────────────────────────────────

    /**
     * Reduces visible crater count for liquid coverage — craters under oceans or
     * frozen surfaces aren't visible from orbit.
     */
    private void refineCrateringForLiquidCoverage(Planet planet, TerrainProperties terrain) {
        Integer craters = terrain.getEstimatedVisibleCraters();
        if (craters == null || craters <= 0) return;

        Double liquidCoverage = planet.getLiquidCoveragePercent();
        if (liquidCoverage == null || liquidCoverage <= 0) return;

        double landFraction = Math.max(0.1, 1.0 - (liquidCoverage / 100.0));
        int adjusted = Math.max(0, (int) Math.round(craters * landFraction));

        terrain.setEstimatedVisibleCraters(adjusted);

        String level;
        if (adjusted < 50)           level = "Pristine";
        else if (adjusted < 500)     level = "Light";
        else if (adjusted < 5_000)   level = "Moderate";
        else if (adjusted < 50_000)  level = "Heavy";
        else if (adjusted < 500_000) level = "Extreme";
        else                         level = "Saturated";
        terrain.setCrateringLevel(level);
    }

    // ── ICE Reconciliation ──────────────────────────────────────────────────────

    /**
     * If terrain placed ICE-category features (glaciers, ice sheets, etc.) but hydrology
     * reports less frozen coverage, update hydrology to match. This prevents contradictions
     * like 40% glacier terrain with 0% frozen liquid in hydrology properties.
     */
    private void reconcileFrozenLiquidCoverage(Planet planet, TerrainProperties terrain, List<TerrainDistribution> terrainDist) {
        HydrologyProperties hydrology = planet.getHydrology();
        if (hydrology == null) return;

        double iceTerrainPct = terrainDist.stream()
                .filter(t -> "ICE".equals(t.getTerrainType().getCategory()))
                .mapToDouble(TerrainDistribution::getCoveragePercent)
                .sum();

        double currentFrozen = hydrology.getFrozenLiquidCoveragePercent() != null ? hydrology.getFrozenLiquidCoveragePercent() : 0.0;

        if (iceTerrainPct > currentFrozen) {
            hydrology.setFrozenLiquidCoveragePercent(iceTerrainPct);
            // For water worlds, also update water ice
            if ("WATER".equals(hydrology.getVolatileType())) {
                hydrology.setWaterIceCoveragePercent(iceTerrainPct);
            }
            double liquidPct = hydrology.getLiquidSurfaceCoveragePercent() != null ? hydrology.getLiquidSurfaceCoveragePercent() : 0.0;
            hydrology.setLiquidCoveragePercent(Math.min(100.0, liquidPct + iceTerrainPct));

            // Reconcile inventory label to match the actual coverage.
            // Terrain placed more ice than hydrology predicted — upgrade the label.
            reconcileInventoryForCoverage(hydrology, iceTerrainPct);
        }
    }

    /**
     * After terrain bumps frozen coverage, upgrade the inventory label if the
     * coverage is inconsistent with the current label.
     */
    private void reconcileInventoryForCoverage(HydrologyProperties hydrology, double frozenPct) {
        String currentInventory = hydrology.getLiquidInventory();
        if (currentInventory == null) return;

        // Determine minimum inventory that matches actual coverage
        String requiredInventory;
        if (frozenPct >= 60) {
            requiredInventory = "OCEAN_WORLD";
        } else if (frozenPct >= 30) {
            requiredInventory = "ABUNDANT";
        } else if (frozenPct >= 15) {
            requiredInventory = "MODERATE";
        } else if (frozenPct >= 5) {
            requiredInventory = "SCARCE";
        } else {
            return; // coverage is low enough, no upgrade needed
        }

        // Only upgrade, never downgrade
        int currentOrd = inventoryOrdinal(currentInventory);
        int requiredOrd = inventoryOrdinal(requiredInventory);
        if (requiredOrd > currentOrd) {
            hydrology.setLiquidInventory(requiredInventory);
        }
    }

    private int inventoryOrdinal(String inventory) {
        return switch (inventory) {
            case "NONE" -> 0;
            case "TRACE" -> 1;
            case "SCARCE" -> 2;
            case "MODERATE" -> 3;
            case "ABUNDANT" -> 4;
            case "OCEAN_WORLD" -> 5;
            default -> 0;
        };
    }

    // ── Terrain Distribution ────────────────────────────────────────────────────

    private List<TerrainDistribution> generateTerrainDistribution(Planet planet, TerrainProperties terrain) {
        List<TerrainDistribution> terrains = new ArrayList<>();
        String planetType = planet.getPlanetType();

        double liquidCoverage = planet.getLiquidCoveragePercent() != null ?
                planet.getLiquidCoveragePercent() : 0;
        double landPercent = 100.0 - liquidCoverage;
        Double surfaceTemp = planet.getSurfaceTemp();
        String volatileType = planet.getVolatileType();

        if (isGasGiant(planetType)) {
            return terrains;
        }

        List<TerrainTypeRef> viableTerrains = getViableTerrains(planet, liquidCoverage);

        if (liquidCoverage > 1.0) {
            Double freezeK = planet.getFreezingPointK();
            boolean liquidIsFrozen = surfaceTemp != null && freezeK != null && surfaceTemp < freezeK;

            if (liquidIsFrozen) {
                List<TerrainTypeRef> iceTerrains = filterByCategory(viableTerrains, "ICE");
                if (!iceTerrains.isEmpty()) {
                    distributePercentageAcrossCategories(terrains, planet, terrain, iceTerrains, liquidCoverage);
                }
            } else {
                // Filter AQUATIC terrains by liquid type compatibility
                List<TerrainTypeRef> aquaticTerrains = filterByCategory(viableTerrains, "AQUATIC").stream()
                        .filter(t -> t.isCompatibleWithVolatileType(volatileType))
                        .collect(Collectors.toList());

                // Also include matching EXOTIC liquid terrains (e.g. METHANE_LAKES, AMMONIA_SEAS)
                if (volatileType != null && !"WATER".equals(volatileType) && !"NONE".equals(volatileType)) {
                    List<TerrainTypeRef> exoticLiquidTerrains = filterByCategory(viableTerrains, "EXOTIC").stream()
                            .filter(t -> volatileType.equals(t.getVolatileType()))
                            .collect(Collectors.toList());
                    aquaticTerrains.addAll(exoticLiquidTerrains);
                }

                if (!aquaticTerrains.isEmpty()) {
                    distributePercentageAcrossCategories(terrains, planet, terrain, aquaticTerrains, liquidCoverage);
                }
            }
        }

        if (landPercent > 1.0) {
            List<TerrainTypeRef> landTerrains = viableTerrains.stream()
                    .filter(t -> !t.getCategory().equals("AQUATIC"))
                    .collect(Collectors.toList());

            if (!landTerrains.isEmpty()) {
                distributePercentageAcrossCategories(terrains, planet, terrain, landTerrains, landPercent);
            }
        }

        double totalCoverage = terrains.stream()
                .mapToDouble(TerrainDistribution::getCoveragePercent)
                .sum();

        if (totalCoverage < 95.0 && !viableTerrains.isEmpty()) {
            double missingPercent = 100.0 - totalCoverage;
            List<TerrainTypeRef> fillTerrains = viableTerrains.stream()
                    .filter(t -> !t.getCategory().equals("ARTIFICIAL"))
                    .filter(t -> !t.getCategory().equals("AQUATIC"))
                    .collect(Collectors.toList());

            if (!fillTerrains.isEmpty()) {
                distributePercentageAcrossCategories(terrains, planet, terrain, fillTerrains, missingPercent);
            }
        }

        return consolidateDuplicateTerrains(terrains);
    }

    // ── Terrain Viability ───────────────────────────────────────────────────────

    private List<TerrainTypeRef> getViableTerrains(Planet planet, double liquidCoverage) {
        boolean hasAtmosphere = planet.getAtmosphereComposition() != null &&
                !planet.getAtmosphereComposition().isEmpty() &&
                !planet.getAtmosphereComposition().equals("None");
        boolean hasHeavyCratering = "Heavy".equals(planet.getCrateringLevel()) ||
                "Extreme".equals(planet.getCrateringLevel()) ||
                (planet.getEstimatedVisibleCraters() != null && planet.getEstimatedVisibleCraters() > 10000);
        boolean hasVolcanism = Boolean.TRUE.equals(planet.getHasVolcanicActivity());

        List<TerrainTypeRef> viableTerrains = terrainTypeRefRepository.findViableTerrainTypes(
                planet.getSurfaceTemp(),
                liquidCoverage > 0,
                hasAtmosphere,
                planet.getSurfacePressure()
        );

        if (viableTerrains.isEmpty()) {
            Double surfaceTemp = planet.getSurfaceTemp();
            viableTerrains = cachedTerrainTypes.values().stream()
                    .filter(t -> "BARREN".equals(t.getCategory()) || "EXOTIC".equals(t.getCategory()))
                    .filter(t -> surfaceTemp == null || t.getMinTemperatureK() == null || surfaceTemp >= t.getMinTemperatureK())
                    .filter(t -> surfaceTemp == null || t.getMaxTemperatureK() == null || surfaceTemp <= t.getMaxTemperatureK())
                    .collect(Collectors.toList());
        }


        if (viableTerrains.isEmpty()) {
            Double surfaceTemp = planet.getSurfaceTemp();
            viableTerrains = cachedTerrainTypes.values().stream()
                    .filter(t -> "BARREN".equals(t.getCategory()))
                    .filter(t -> surfaceTemp == null || t.getMinTemperatureK() == null || surfaceTemp >= t.getMinTemperatureK())
                    .filter(t -> surfaceTemp == null || t.getMaxTemperatureK() == null || surfaceTemp <= t.getMaxTemperatureK())
                    .collect(Collectors.toList());
        }

        if (hasHeavyCratering || hasVolcanism) {
            viableTerrains = applyGeologicalWeightBoosts(viableTerrains, hasHeavyCratering, hasVolcanism);
        }

        if (planet.getHasVolcanicActivity() == null) {
            viableTerrains = viableTerrains.stream()
                    .filter(t -> !t.getCategory().equals("VOLCANIC"))
                    .collect(Collectors.toList());
        }

        viableTerrains = viableTerrains.stream()
                .filter(t -> !isExcludedByPlanetType(t, planet))
                .filter(t -> meetsCompositionRequirement(t, planet))
                .collect(Collectors.toList());

        // Ultimate Question of Life, the Universe, and Everything...
        if (RandomUtils.rollRange(0, 1000000) != 42) {
            viableTerrains = viableTerrains.stream()
                    .filter(t -> !t.getCategory().equals("ARTIFICIAL"))
                    .collect(Collectors.toList());
        }

        return viableTerrains;
    }

    // ── Filtering & Composition ─────────────────────────────────────────────────

    private boolean isExcludedByPlanetType(TerrainTypeRef terrain, Planet planet) {
        if (terrain.getExcludedPlanetTypes() == null ||
                terrain.getExcludedPlanetTypes().length == 0) {
            return false;
        }

        String planetType = planet.getPlanetType();
        if (planetType == null) {
            return false;
        }

        return Arrays.asList(terrain.getExcludedPlanetTypes()).contains(planetType);
    }

    private boolean meetsCompositionRequirement(TerrainTypeRef terrain, Planet planet) {
        String planetComposition = planet.getCompositionClassification();

        if (planetComposition == null) {
            return true;
        }

        if (terrain.getRequiredCompositionClasses() != null &&
                terrain.getRequiredCompositionClasses().length > 0) {
            return Arrays.asList(terrain.getRequiredCompositionClasses())
                    .contains(planetComposition);
        }

        if (terrain.getExcludedCompositionClasses() != null &&
                terrain.getExcludedCompositionClasses().length > 0) {
            return !Arrays.asList(terrain.getExcludedCompositionClasses())
                    .contains(planetComposition);
        }

        return true;
    }

    private List<TerrainTypeRef> applyGeologicalWeightBoosts(List<TerrainTypeRef> terrains, boolean hasHeavyCratering, boolean hasVolcanism) {
        List<TerrainTypeRef> weighted = new ArrayList<>();
        for (TerrainTypeRef terrain : terrains) {
            int effectiveWeight = terrain.getEffectiveWeight(hasHeavyCratering, hasVolcanism);
            int baseWeight = terrain.getRarityWeight() != null ? terrain.getRarityWeight() : 100;

            int copies = 1 + (effectiveWeight - baseWeight) / 50;
            for (int i = 0; i < copies; i++) {
                weighted.add(terrain);
            }
        }
        return weighted.isEmpty() ? terrains : weighted;
    }

    // ── Distribution ────────────────────────────────────────────────────────────

    private void distributePercentageAcrossCategories(List<TerrainDistribution> terrains, Planet planet, TerrainProperties terrain, List<TerrainTypeRef> viableTerrains, double totalPercent) {
        if (viableTerrains.isEmpty() || totalPercent < 0.5) {
            return;
        }

        Map<String, List<TerrainTypeRef>> terrainsByCategory = viableTerrains.stream()
                .collect(Collectors.groupingBy(TerrainTypeRef::getCategory));

        Map<String, Integer> categoryWeights = new HashMap<>();
        for (String category : terrainsByCategory.keySet()) {
            TerrainCategoryRef categoryRef = cachedTerrainCategories.get(category);
            if (categoryRef != null) {
                categoryWeights.put(category, categoryRef.getBaseWeight());
            } else {
                categoryWeights.put(category, 50);
            }
        }

        int totalWeight = categoryWeights.values().stream().mapToInt(Integer::intValue).sum();

        if (totalWeight == 0) {
            distributePercentageEvenly(terrains, planet, terrain, viableTerrains, totalPercent);
            return;
        }

        List<String> majorCategories = new ArrayList<>();
        List<String> rareCategories = new ArrayList<>();

        for (String category : terrainsByCategory.keySet()) {
            TerrainCategoryRef categoryRef = cachedTerrainCategories.get(category);
            if (categoryRef != null && categoryRef.getIsRare()) {
                rareCategories.add(category);
            } else {
                majorCategories.add(category);
            }
        }

        double majorPercent = totalPercent * RandomUtils.rollRange(0.90, 0.95);
        double rarePercent = totalPercent - majorPercent;

        int numMajor = Math.min(majorCategories.size(), RandomUtils.rollRange(2, 4));
        List<String> selectedMajor = selectWeightedRandomCategories(majorCategories, categoryWeights, numMajor);

        distributeAmongCategories(terrains, planet, terrain, terrainsByCategory, categoryWeights,
                cachedTerrainCategories, selectedMajor, majorPercent);

        if (!rareCategories.isEmpty() && rarePercent > 0.5) {
            int numRare = Math.min(rareCategories.size(), RandomUtils.rollRange(1, 2));
            List<String> selectedRare = selectWeightedRandomCategories(rareCategories, categoryWeights, numRare);
            distributeAmongCategories(terrains, planet, terrain, terrainsByCategory, categoryWeights,
                    cachedTerrainCategories, selectedRare, rarePercent);
        }
    }

    private void distributeAmongCategories(List<TerrainDistribution> terrains, Planet planet, TerrainProperties terrain, Map<String, List<TerrainTypeRef>> terrainsByCategory,
                                           Map<String, Integer> categoryWeights, Map<String, TerrainCategoryRef> categoryRefs, List<String> categories, double totalPercent) {
        double remainingPercent = totalPercent;

        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            List<TerrainTypeRef> categoryTerrains = terrainsByCategory.get(category);
            TerrainCategoryRef categoryRef = categoryRefs.get(category);

            double categoryPercent;
            if (i == categories.size() - 1) {
                categoryPercent = remainingPercent;
            } else {
                int categoryWeight = categoryWeights.get(category);
                int selectedWeight = categories.stream().mapToInt(categoryWeights::get).sum();

                double basePercent = (totalPercent * categoryWeight) / (double) selectedWeight;
                double variance = RandomUtils.rollRange(-0.3, 0.3);
                categoryPercent = basePercent * (1.0 + variance);

                if (categoryRef != null) {
                    categoryPercent = Math.max(categoryPercent, categoryRef.getTypicalMinCoverage());
                    categoryPercent = Math.min(categoryPercent, categoryRef.getTypicalMaxCoverage());
                }

                double reserveForOthers = (categories.size() - i - 1) * 1.0;
                categoryPercent = Math.min(categoryPercent, remainingPercent - reserveForOthers);
            }

            if (categoryPercent >= 0.5) {
                distributePercentageWithinCategory(terrains, planet, terrain, categoryTerrains, categoryPercent);
                remainingPercent -= categoryPercent;
            }
        }
    }

    private List<String> selectWeightedRandomCategories(List<String> categories, Map<String, Integer> weights, int count) {
        List<String> selected = new ArrayList<>();
        List<String> available = new ArrayList<>(categories);

        count = Math.min(count, available.size());

        for (int i = 0; i < count; i++) {
            if (available.isEmpty()) break;

            int totalWeight = available.stream()
                    .mapToInt(weights::get)
                    .sum();

            if (totalWeight == 0) {
                selected.add(available.remove(RandomUtils.rollRange(0, available.size() - 1)));
                continue;
            }

            int randomWeight = RandomUtils.rollRange(1, totalWeight);
            int currentWeight = 0;

            for (String category : available) {
                currentWeight += weights.get(category);
                if (randomWeight <= currentWeight) {
                    selected.add(category);
                    available.remove(category);
                    break;
                }
            }
        }

        return selected;
    }

    private void distributePercentageWithinCategory(List<TerrainDistribution> terrains, Planet planet, TerrainProperties terrainProps, List<TerrainTypeRef> categoryTerrains, double categoryPercent) {
        if (categoryTerrains.isEmpty()) {
            return;
        }

        boolean heavyCratering = "Heavy".equals(planet.getCrateringLevel()) ||
                "Extreme".equals(planet.getCrateringLevel()) ||
                (planet.getEstimatedVisibleCraters() != null && planet.getEstimatedVisibleCraters() > 10000);
        boolean volcanism = Boolean.TRUE.equals(planet.getHasVolcanicActivity());

        List<TerrainTypeRef> shuffled = new ArrayList<>(categoryTerrains);
        Collections.shuffle(shuffled);
        int numTerrains = Math.min(shuffled.size(), RandomUtils.rollRange(1, 3));
        List<TerrainTypeRef> selectedTerrains = shuffled.subList(0, numTerrains);

        int totalWeight = selectedTerrains.stream()
                .mapToInt(t -> t.getEffectiveWeight(heavyCratering, volcanism))
                .sum();

        double remainingPercent = categoryPercent;

        for (int i = 0; i < selectedTerrains.size(); i++) {
            TerrainTypeRef terrainType = selectedTerrains.get(i);
            double percent;

            if (i == selectedTerrains.size() - 1) {
                percent = remainingPercent;
            } else {
                if (totalWeight > 0) {
                    double basePercent = (categoryPercent * terrainType.getEffectiveWeight(heavyCratering, volcanism)) / (double) totalWeight;
                    double variance = RandomUtils.rollRange(-0.2, 0.2);
                    percent = basePercent * (1.0 + variance);
                } else {
                    percent = categoryPercent / selectedTerrains.size();
                }

                percent = Math.max(percent, terrainType.getTypicalCoverageMin());
                percent = Math.min(percent, terrainType.getTypicalCoverageMax());

                double reserveForOthers = (selectedTerrains.size() - i - 1) * 0.5;
                percent = Math.min(percent, remainingPercent - reserveForOthers);
            }

            if (percent >= 0.5) {
                addTerrainDirect(terrains, terrainProps, terrainType, percent);
                remainingPercent -= percent;
            }
        }
    }

    private void distributePercentageEvenly(List<TerrainDistribution> terrains, Planet planet, TerrainProperties terrainProps, List<TerrainTypeRef> terrainTypes, double totalPercent) {
        if (terrainTypes.isEmpty() || totalPercent < 0.5) {
            return;
        }

        List<TerrainTypeRef> selectedTerrains = new ArrayList<>(terrainTypes);
        Collections.shuffle(selectedTerrains);
        int numTerrains = Math.min(selectedTerrains.size(), RandomUtils.rollRange(2, 4));
        selectedTerrains = selectedTerrains.subList(0, numTerrains);

        double remainingPercent = totalPercent;

        for (int i = 0; i < selectedTerrains.size(); i++) {
            TerrainTypeRef terrainType = selectedTerrains.get(i);
            double percent;

            if (i == selectedTerrains.size() - 1) {
                percent = remainingPercent;
            } else {
                double basePercent = totalPercent / selectedTerrains.size();
                double variance = RandomUtils.rollRange(-0.3, 0.3);
                percent = basePercent * (1.0 + variance);

                percent = Math.max(percent, terrainType.getTypicalCoverageMin());
                percent = Math.min(percent, terrainType.getTypicalCoverageMax());

                double reserveForOthers = (selectedTerrains.size() - i - 1) * 1.0;
                percent = Math.min(percent, remainingPercent - reserveForOthers);
            }

            if (percent >= 0.5) {
                addTerrainDirect(terrains, terrainProps, terrainType, percent);
                remainingPercent -= percent;
            }

            if (remainingPercent < 0) {
                remainingPercent = 0;
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private void addTerrainDirect(List<TerrainDistribution> terrains, TerrainProperties terrainProps, TerrainTypeRef terrainType, double percent) {
        if (percent < 0.5) {
            return;
        }

        TerrainDistribution distribution = new TerrainDistribution(
                terrainType,
                Math.round(percent * 100.0) / 100.0
        );
        distribution.setTerrain(terrainProps);
        terrains.add(distribution);
    }

    private List<TerrainTypeRef> filterByCategory(List<TerrainTypeRef> terrains, String category) {
        return terrains.stream()
                .filter(t -> t.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    private double calculateMountainCoverage(List<TerrainDistribution> terrains) {
        return terrains.stream()
                .filter(t -> t.getTerrainType().getCategory().equals("MOUNTAIN"))
                .mapToDouble(TerrainDistribution::getCoveragePercent)
                .sum();
    }

    private List<TerrainDistribution> consolidateDuplicateTerrains(List<TerrainDistribution> terrains) {
        Map<Integer, TerrainDistribution> consolidated = new LinkedHashMap<>();

        for (TerrainDistribution dist : terrains) {
            Integer terrainId = dist.getTerrainType().getId();

            if (consolidated.containsKey(terrainId)) {
                TerrainDistribution existing = consolidated.get(terrainId);
                double newCoverage = existing.getCoveragePercent() + dist.getCoveragePercent();
                existing.setCoveragePercent(Math.round(newCoverage * 100.0) / 100.0);
            } else {
                consolidated.put(terrainId, dist);
            }
        }

        return new ArrayList<>(consolidated.values());
    }

    private boolean isGasGiant(String planetType) {
        return planetType != null && (
                planetType.contains("Gas Giant") ||
                        planetType.contains("Jupiter") ||
                        planetType.contains("Ice Giant") ||
                        planetType.contains("Neptune") ||
                        planetType.contains("Mini-Neptune") ||
                        planetType.contains("Sub-Neptune")
        );
    }
}
