package com.brickroad.starcreator_webservice.utils.systems;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.enums.*;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SystemClassifier {

    // ================================================================
    // PUBLIC ENTRY POINT
    // ================================================================

    public SystemClassification classify(StarSystem system) {
        SystemClassification c = new SystemClassification();

        // Gather raw inventory
        SystemInventory inv = buildInventory(system);

        // Analyze each dimension
        assessEconomicProfile(c, inv);
        assessStrategicProfile(c, inv, system);
        assessDangerProfile(c, inv);
        assessInterestRatings(c, inv);
        determineArchetypes(c, inv, system);
        collectNotableFeatures(c, inv, system);

        // Generate narratives last (they use everything above)
        c.setSystemSummary(generateSystemSummary(c, inv, system));
        c.setScoutReport(generateScoutReport(c, inv, system));

        return c;
    }

    // ================================================================
    // INVENTORY - Collect all the data we need in one pass
    // ================================================================

    private SystemInventory buildInventory(StarSystem system) {
        SystemInventory inv = new SystemInventory();

        // Stars
        for (Star star : system.getStars()) {
            inv.starTypes.add(star.getType() != null ? star.getType() : "Unknown");
            inv.starSpectralTypes.add(star.getSpectralType() != null ? star.getSpectralType() : "?");

            if (star.getStarRole() == Star.StarRole.PRIMARY) {
                inv.primaryStar = star;
            }

            String activity = star.getActivityLevel();
            if ("VERY_ACTIVE".equals(activity) || "HYPERACTIVE".equals(activity)) {
                inv.hasHighActivity = true;
            } else if ("ACTIVE".equals(activity) || "MODERATE".equals(activity)) {
                inv.hasModerateActivity = true;
            }
            if (Boolean.TRUE.equals(star.getSuperflareCapable())) {
                inv.hasSuperflareRisk = true;
            }
            String evo = star.getEvolutionaryStage();
            if (evo != null) inv.evolutionaryStages.add(evo);

            String type = star.getType() != null ? star.getType().toLowerCase() : "";
            if (type.contains("proto") || type.contains("t tauri")) inv.hasProtostar = true;
            if (type.contains("red giant") || type.contains("super giant") || type.contains("asymptotic")) inv.hasDyingStar = true;
            if (type.contains("neutron")) inv.hasNeutronStar = true;
            if (type.contains("white dwarf")) inv.hasWhiteDwarf = true;
            if (type.contains("brown dwarf")) inv.hasBrownDwarf = true;
        }

        if (inv.primaryStar == null && !system.getStars().isEmpty()) {
            inv.primaryStar = system.getStars().iterator().next();
        }

        inv.starCount = system.getStars().size();
        inv.isMultiStar = inv.starCount > 1;

        // Planets
        for (CelestialBody body : system.getPlanets()) {
            if (!(body instanceof Planet planet)) continue;

            inv.totalPlanets++;
            String pType = planet.getPlanetType() != null ? planet.getPlanetType() : "";
            inv.planetTypes.add(pType);

            // Categorize
            String lower = pType.toLowerCase();
            if (lower.contains("gas giant") || lower.contains("hot jupiter") || lower.contains("super-jupiter")
                    || lower.contains("puffy")) {
                inv.gasGiantCount++;
            }
            if (lower.contains("ice giant") || lower.contains("sub-neptune") || lower.contains("neptune")) {
                inv.iceGiantCount++;
            }
            if (lower.contains("lava")) inv.lavaWorldCount++;
            if (lower.contains("ice world")) inv.iceWorldCount++;
            if (lower.contains("ocean")) inv.oceanWorldCount++;
            if (lower.contains("desert")) inv.desertWorldCount++;
            if (lower.contains("carbon")) inv.carbonWorldCount++;
            if (lower.contains("iron")) inv.ironWorldCount++;

            // Composition
            String comp = planet.getCompositionClassification();
            if (comp != null) {
                inv.compositionClasses.add(comp);
                if ("IRON_RICH".equals(comp) || "CARBON_RICH".equals(comp)) inv.metalRichCount++;
            }

            // Water
            String water = planet.getWaterInventory();
            if ("ABUNDANT".equals(water) || "OCEAN_WORLD".equals(water)) inv.waterRichCount++;
            if (planet.getLiquidWaterCoveragePercent() != null && planet.getLiquidWaterCoveragePercent() > 0) inv.liquidWaterBodies++;
            if (Boolean.TRUE.equals(planet.getHasSubsurfaceWater())) inv.subsurfaceWaterCount++;

            // Habitability
            PlanetaryHabitability hab = planet.getHabitability();
            if (hab != null) {
                HabitabilityClass hc = hab.getHabitabilityClass();
                if (hc == HabitabilityClass.EARTH_ANALOG || hc == HabitabilityClass.HABITABLE_MARGINAL) {
                    inv.habitableCount++;
                }
                if (hc == HabitabilityClass.TERRAFORMABLE_EASY || hc == HabitabilityClass.TERRAFORMABLE_HARD) {
                    inv.terraformableCount++;
                }
                if (hc == HabitabilityClass.SUBSURFACE_HABITABLE) {
                    inv.subsurfaceHabitableCount++;
                }
                if (hc == HabitabilityClass.RESOURCE_WORLD) inv.resourceWorldCount++;

                String bio = hab.getBiosignaturePotential();
                if ("HIGH".equals(bio) || "MODERATE".equals(bio)) inv.biosignatureCount++;

                String life = hab.getLifeComplexityPotential();
                if (life != null && !"NONE".equals(life)) inv.lifePotentialBodies++;

                ColonizationSuitability cs = hab.getColonizationSuitability();
                if (cs == ColonizationSuitability.SHIRT_SLEEVE) inv.shirtSleeveCount++;
                if (cs == ColonizationSuitability.ASSISTED) inv.assistedCount++;
                if (cs == ColonizationSuitability.DOME_ONLY) inv.domeOnlyCount++;
            }

            // Moons
            if (planet.getMoons() != null) {
                for (Moon moon : planet.getMoons()) {
                    inv.totalMoons++;
                    String moonComp = moon.getCompositionType();
                    String moonCompClass = moon.getCompositionClassification();
                    if ("ICY".equals(moonComp) || "ICE_RICH".equals(moonComp)
                            || "ICE_RICH".equals(moonCompClass) || "MIXED_SILICATE_ICE".equals(moonCompClass)) {
                        inv.icyMoonCount++;
                    }
                    if (Boolean.TRUE.equals(moon.getHasSubsurfaceOcean())) inv.subsurfaceOceanMoons++;

                    PlanetaryHabitability moonHab = moon.getHabitability();
                    if (moonHab != null) {
                        if (moonHab.getHabitabilityClass() == HabitabilityClass.SUBSURFACE_HABITABLE) {
                            inv.subsurfaceHabitableCount++;
                        }
                        String moonBio = moonHab.getBiosignaturePotential();
                        if ("HIGH".equals(moonBio) || "MODERATE".equals(moonBio)) inv.biosignatureCount++;
                    }
                }
            }

            // Rings
            if (planet.getBands() != null && !planet.getBands().isEmpty()) {
                inv.ringedPlanetCount++;
            }

            // Geological activity
            String geo = planet.getGeologicalActivity();
            if ("HIGH".equals(geo) || "EXTREME".equals(geo)) inv.geologicallyActiveCount++;
        }

        // Belts
        if (system.getBands() != null) {
            for (OrbitalBand belt : system.getBands()) {
                inv.beltCount++;
                String beltType = belt.getBeltType() != null ? belt.getBeltType().getName() : "";
                String beltTypeLower = beltType.toLowerCase();
                inv.beltTypes.add(beltType);

                if (beltTypeLower.contains("kuiper") || beltTypeLower.contains("scattered")) {
                    inv.outerBeltCount++;
                }
                if (beltTypeLower.contains("inner") || beltTypeLower.contains("rocky")) {
                    inv.innerBeltCount++;
                }

                if (belt.getTotalMassEarthMasses() != null) {
                    inv.totalBeltMass += belt.getTotalMassEarthMasses();
                }
                if (belt.getEstimatedObjectCount() != null) {
                    inv.totalBeltObjects += belt.getEstimatedObjectCount();
                }
            }
        }

        // System age from primary star
        if (inv.primaryStar != null && inv.primaryStar.getAgeMY() != null) {
            inv.systemAgeMY = inv.primaryStar.getAgeMY();
        }

        return inv;
    }

    // ================================================================
    // ECONOMIC ASSESSMENT
    // ================================================================

    private void assessEconomicProfile(SystemClassification c, SystemInventory inv) {
        // Mineral richness
        int mineralScore = inv.metalRichCount * 3 + inv.resourceWorldCount * 2 + inv.innerBeltCount * 2
                + inv.ironWorldCount * 3 + inv.carbonWorldCount * 2;
        if (mineralScore >= 10) c.setMineralRichness("EXCEPTIONAL");
        else if (mineralScore >= 6) c.setMineralRichness("RICH");
        else if (mineralScore >= 3) c.setMineralRichness("MODERATE");
        else if (mineralScore >= 1) c.setMineralRichness("POOR");
        else c.setMineralRichness("BARREN");

        // Fuel availability (gas giants = hydrogen fuel, ice giants = lesser fuel)
        int fuelBodies = inv.gasGiantCount * 2 + inv.iceGiantCount;
        // Ice worlds with methane/volatile ices can provide processed fuel (less efficient)
        if (inv.iceWorldCount > 0 && "RICH".equals(c.getVolatileSupply())) fuelBodies += 1;
        if (fuelBodies >= 6) c.setFuelAvailability("ABUNDANT");
        else if (fuelBodies >= 3) c.setFuelAvailability("AVAILABLE");
        else if (fuelBodies >= 1) c.setFuelAvailability("SCARCE");
        else c.setFuelAvailability("NONE");

        // Water accessibility
        int surfaceWaterScore = inv.waterRichCount * 3 + inv.liquidWaterBodies * 2 + inv.oceanWorldCount * 3;
        int subsurfaceWaterScore = inv.subsurfaceWaterCount + inv.subsurfaceOceanMoons * 2;
        int iceWaterScore = inv.outerBeltCount;
        if (inv.iceWorldCount > 0) iceWaterScore += 1;
        int waterScore = surfaceWaterScore + subsurfaceWaterScore + iceWaterScore;

        if (surfaceWaterScore >= 6) c.setWaterAccessibility("OCEAN_WORLDS");
        else if (waterScore >= 10 || surfaceWaterScore >= 3) c.setWaterAccessibility("ABUNDANT");
        else if (waterScore >= 5) c.setWaterAccessibility("AVAILABLE");
        else if (waterScore >= 2) c.setWaterAccessibility("TRACE");
        else if (waterScore == 1) c.setWaterAccessibility("ICE_LOCKED");
        else c.setWaterAccessibility("NONE");

        // Volatile supply
        int volatileScore = inv.outerBeltCount * 2 + inv.iceWorldCount;
        if (inv.icyMoonCount >= 5) volatileScore += 2;
        else if (inv.icyMoonCount >= 2) volatileScore += 1;
        if (volatileScore >= 8) c.setVolatileSupply("RICH");
        else if (volatileScore >= 4) c.setVolatileSupply("MODERATE");
        else if (volatileScore >= 1) c.setVolatileSupply("SCARCE");
        else c.setVolatileSupply("NONE");

        // Industrial potential (composite)
        int indScore = mineralScore + inv.gasGiantCount + inv.beltCount + inv.totalPlanets;
        if (indScore >= 15) c.setIndustrialPotential("EXCEPTIONAL");
        else if (indScore >= 10) c.setIndustrialPotential("HIGH");
        else if (indScore >= 5) c.setIndustrialPotential("MODERATE");
        else if (indScore >= 2) c.setIndustrialPotential("LOW");
        else c.setIndustrialPotential("NEGLIGIBLE");
    }

    // ================================================================
    // STRATEGIC ASSESSMENT
    // ================================================================

    private void assessStrategicProfile(SystemClassification c, SystemInventory inv, StarSystem system) {
        c.setHabitableWorldCount(inv.habitableCount);
        c.setTerraformableWorldCount(inv.terraformableCount);
        c.setTotalPlanetCount(inv.totalPlanets);
        c.setTotalMoonCount(inv.totalMoons);
        c.setBeltCount(inv.beltCount);
        c.setGasGiantCount(inv.gasGiantCount);
        c.setHasDefensiveBelts(inv.innerBeltCount > 0 && inv.totalBeltObjects > 10000);

        // Population capacity
        if (inv.shirtSleeveCount >= 2) c.setPopulationCapacity("METROPOLIS");
        else if (inv.shirtSleeveCount >= 1 || inv.assistedCount >= 2) c.setPopulationCapacity("SETTLEMENT");
        else if (inv.assistedCount >= 1 || inv.domeOnlyCount >= 3) c.setPopulationCapacity("COLONY");
        else if (inv.domeOnlyCount >= 1) c.setPopulationCapacity("OUTPOST");
        else c.setPopulationCapacity("NONE");
    }

    // ================================================================
    // DANGER ASSESSMENT
    // ================================================================

    private void assessDangerProfile(SystemClassification c, SystemInventory inv) {
        List<String> dangers = new ArrayList<>();
        int dangerScore = 0;

        // Stellar threats
        if (inv.hasSuperflareRisk) {
            dangers.add("Superflare-capable star — periodic sterilization events");
            dangerScore += 3;
        }
        if (inv.hasHighActivity) {
            dangers.add("High stellar activity — elevated radiation and particle flux");
            dangerScore += 2;
        }
        if (inv.hasModerateActivity && !inv.hasHighActivity) {
            dangers.add("Active star — elevated flare frequency and particle flux");
            dangerScore += 1;
        }
        if (inv.hasNeutronStar) {
            dangers.add("Neutron star — extreme radiation environment");
            dangerScore += 4;
        }
        if (inv.hasProtostar) {
            dangers.add("Protostellar system — unstable, still forming");
            dangerScore += 3;
        }
        if (inv.hasDyingStar) {
            dangers.add("Evolved giant star — expanding envelope, mass loss events");
            dangerScore += 2;
        }

        // Orbital complexity
        if (inv.isMultiStar && inv.starCount >= 3) {
            dangers.add("Trinary system — complex gravitational environment");
            dangerScore += 2;
        } else if (inv.isMultiStar) {
            dangers.add("Binary system — variable radiation and gravitational perturbations");
            dangerScore += 1;
        }

        // Lava worlds close in
        if (inv.lavaWorldCount >= 2) {
            dangers.add("Multiple lava worlds — intense inner-system thermal environment");
            dangerScore += 1;
        }

        // Environmental hazards — extreme cold with no shelter
        if (inv.habitableCount == 0 && inv.totalPlanets > 0) {
            boolean allExtremeCold = inv.primaryStar != null
                    && inv.primaryStar.getSurfaceTemp() != 0
                    && inv.primaryStar.getSurfaceTemp() < 1000;
            if (allExtremeCold && inv.hasBrownDwarf) {
                dangers.add("Brown dwarf system — near-absolute-zero conditions, no viable energy source");
                dangerScore += 1;
            }
        }

        c.setDangerSources(dangers);

        if (dangerScore >= 7) c.setDangerRating("EXTREME");
        else if (dangerScore >= 5) c.setDangerRating("DEADLY");
        else if (dangerScore >= 3) c.setDangerRating("HAZARDOUS");
        else if (dangerScore >= 1) c.setDangerRating("CAUTION");
        else c.setDangerRating("SAFE");

        // Radiation environment
        if (inv.hasNeutronStar || inv.hasSuperflareRisk) c.setRadiationEnvironment("LETHAL");
        else if (inv.hasHighActivity || inv.hasProtostar) c.setRadiationEnvironment("HARSH");
        else if (inv.isMultiStar) c.setRadiationEnvironment("MODERATE");
        else c.setRadiationEnvironment("BENIGN");

        // Orbital stability
        if (inv.starCount >= 3) c.setOrbitalStability("CHAOTIC");
        else if (inv.isMultiStar) c.setOrbitalStability("MOSTLY_STABLE");
        else c.setOrbitalStability("STABLE");
    }

    // ================================================================
    // INTEREST RATINGS (1-10 by faction perspective)
    // ================================================================

    private void assessInterestRatings(SystemClassification c, SystemInventory inv) {
        // Mining corporations
        int mining = 1;
        if ("EXCEPTIONAL".equals(c.getMineralRichness())) mining = 10;
        else if ("RICH".equals(c.getMineralRichness())) mining = 8;
        else if ("MODERATE".equals(c.getMineralRichness())) mining = 5;
        else if ("POOR".equals(c.getMineralRichness())) mining = 3;
        mining += inv.beltCount; // Belts are mining gold
        mining = Math.min(10, mining);
        // Penalty for extreme danger
        if ("EXTREME".equals(c.getDangerRating()) || "DEADLY".equals(c.getDangerRating())) mining = Math.max(1, mining - 2);
        c.setMiningInterest(mining);

        // Colonization interest
        int colony = 1;
        if (inv.habitableCount >= 2) colony = 10;
        else if (inv.habitableCount == 1) colony = 8;
        else if (inv.terraformableCount >= 2) colony = 6;
        else if (inv.terraformableCount == 1) colony = 5;
        else if (inv.domeOnlyCount >= 3) colony = 4;
        else if (inv.domeOnlyCount >= 1) colony = 3;
        // Bonus for water and fuel
        if ("ABUNDANT".equals(c.getWaterAccessibility()) || "OCEAN_WORLDS".equals(c.getWaterAccessibility())) colony += 1;
        if (!"NONE".equals(c.getFuelAvailability())) colony += 1;

        // Penalty: no habitable worlds + no energy source = not colonizable
        if (inv.habitableCount == 0 && inv.terraformableCount == 0) {
            // Brown dwarfs / ultra-dim stars — no meaningful solar energy
            if (inv.hasBrownDwarf && !inv.isMultiStar) colony = Math.max(1, colony - 2);
            // No atmosphere anywhere = dome-only at best, penalize further if extreme cold
            if (inv.primaryStar != null && inv.primaryStar.getSolarLuminosity() != 0
                    && inv.primaryStar.getSolarLuminosity() < 0.001) {
                colony = Math.max(1, colony - 1);
            }
        }

        colony = Math.min(10, colony);
        if ("EXTREME".equals(c.getDangerRating())) colony = Math.max(1, colony - 3);
        if ("DEADLY".equals(c.getDangerRating())) colony = Math.max(1, colony - 2);
        c.setColonizationInterest(colony);

        // Scientific interest
        int science = 2; // Every system has some science value
        if (inv.biosignatureCount > 0) science += 3;
        if (inv.lifePotentialBodies > 0) science += 2;
        if (inv.subsurfaceOceanMoons > 0) science += 2;
        if (inv.hasNeutronStar || inv.hasWhiteDwarf) science += 2;
        if (inv.hasProtostar) science += 3;
        if (inv.isMultiStar) science += 1;
        if (inv.carbonWorldCount > 0) science += 1;
        if (inv.geologicallyActiveCount > 0) science += 1;
        science = Math.min(10, science);
        c.setScientificInterest(science);

        // Military interest
        int military = 1;
        if (c.isHasDefensiveBelts()) military += 2;
        if (inv.innerBeltCount > 0) military += 1;
        if (inv.totalPlanets >= 5) military += 1; // More bodies = more places to hide
        if ("STABLE".equals(c.getOrbitalStability())) military += 1;
        if (!"NONE".equals(c.getFuelAvailability())) military += 1;
        // Strategic chokepoints
        if (inv.beltCount >= 2) military += 1;
        military = Math.min(10, military);
        c.setMilitaryInterest(military);

        // Exploration interest
        int explore = 3;
        if (inv.totalPlanets >= 6) explore += 2;
        else if (inv.totalPlanets >= 3) explore += 1;
        if (inv.totalMoons >= 20) explore += 2;
        else if (inv.totalMoons >= 10) explore += 1;
        if (inv.ringedPlanetCount > 0) explore += 1;
        if (inv.isMultiStar) explore += 1;
        if (inv.oceanWorldCount > 0) explore += 1;
        if (inv.beltCount > 0) explore += 1;
        explore = Math.min(10, explore);
        c.setExplorationInterest(explore);

        // Xenobiology interest
        int xeno = 1;
        if (inv.biosignatureCount > 0) xeno += 4;
        if (inv.lifePotentialBodies > 0) xeno += 3;
        if (inv.subsurfaceOceanMoons >= 3) xeno += 3;
        else if (inv.subsurfaceOceanMoons == 2) xeno += 2;
        else if (inv.subsurfaceOceanMoons == 1) xeno += 1;
        if (inv.subsurfaceHabitableCount > 0) xeno += 1;
        if (inv.habitableCount > 0) xeno += 2;
        if (inv.oceanWorldCount > 0) xeno += 1;
        if (inv.subsurfaceHabitableCount > 0) xeno += 2;
        xeno = Math.min(10, xeno);
        c.setXenobiologyInterest(xeno);
    }

    // ================================================================
    // ARCHETYPE DETERMINATION
    // ================================================================

    private void determineArchetypes(SystemClassification c, SystemInventory inv, StarSystem system) {
        List<ScoredArchetype> candidates = new ArrayList<>();

        // Life & Habitability
        if (inv.habitableCount >= 2) {
            candidates.add(new ScoredArchetype(SystemArchetype.GARDEN_OF_EDEN, 100));
        } else if (inv.habitableCount == 1) {
            candidates.add(new ScoredArchetype(SystemArchetype.HABITABLE_OASIS, 90));
        }

        if (inv.subsurfaceOceanMoons >= 2) {
            candidates.add(new ScoredArchetype(SystemArchetype.SUBSURFACE_OCEAN_NETWORK, 75));
        } else if (inv.subsurfaceOceanMoons == 1 && inv.subsurfaceHabitableCount >= 2) {
            candidates.add(new ScoredArchetype(SystemArchetype.SUBSURFACE_OCEAN_NETWORK, 55));
        }

        if (inv.oceanWorldCount >= 2) {
            candidates.add(new ScoredArchetype(SystemArchetype.OCEAN_WORLD_SYSTEM, 80));
        } else if (inv.oceanWorldCount == 1) {
            candidates.add(new ScoredArchetype(SystemArchetype.OCEAN_WORLD_SYSTEM, 45));
        }
        if (inv.biosignatureCount >= 2) {
            candidates.add(new ScoredArchetype(SystemArchetype.BIOSIGNATURE_CANDIDATE, 80));
        } else if (inv.biosignatureCount == 1) {
            candidates.add(new ScoredArchetype(SystemArchetype.BIOSIGNATURE_CANDIDATE, 50));
        }

        // Resource & Industry
        if ("EXCEPTIONAL".equals(c.getMineralRichness()) || "RICH".equals(c.getMineralRichness())) {
            int score = "EXCEPTIONAL".equals(c.getMineralRichness()) ? 85 : 65;
            candidates.add(new ScoredArchetype(SystemArchetype.MINING_BONANZA, score));
        }
        if (inv.gasGiantCount >= 3) {
            candidates.add(new ScoredArchetype(SystemArchetype.GAS_GIANT_DOMINION, 70));
        } else if (inv.gasGiantCount >= 2 && inv.gasGiantCount >= inv.totalPlanets / 2) {
            candidates.add(new ScoredArchetype(SystemArchetype.GAS_GIANT_DOMINION, 50));
        }
        if (inv.iceWorldCount >= 2 || inv.iceGiantCount >= 2
                || (inv.outerBeltCount >= 1 && inv.icyMoonCount >= 3)
                || (inv.iceWorldCount + inv.iceGiantCount >= 2 && "RICH".equals(c.getVolatileSupply()))) {
            int iceScore = 50 + (inv.iceWorldCount * 5) + (inv.iceGiantCount * 5) + (inv.outerBeltCount * 5);
            candidates.add(new ScoredArchetype(SystemArchetype.ICE_FRONTIER, Math.min(75, iceScore)));
        }
        if (inv.ironWorldCount >= 2 || (inv.metalRichCount >= 3)) {
            candidates.add(new ScoredArchetype(SystemArchetype.METAL_CORE_GRAVEYARD, 65));
        }

        // Stellar Drama
        if (inv.isMultiStar && inv.starCount >= 3) {
            candidates.add(new ScoredArchetype(SystemArchetype.BINARY_CHAOS, 70));
        } else if (inv.isMultiStar) {
            candidates.add(new ScoredArchetype(SystemArchetype.BINARY_CHAOS, 40));
        }
        if (inv.hasDyingStar) {
            candidates.add(new ScoredArchetype(SystemArchetype.DYING_GIANT_SYSTEM, 75));
        }
        if (inv.hasHighActivity || inv.hasSuperflareRisk || inv.hasProtostar) {
            int score = inv.hasProtostar ? 80 : (inv.hasSuperflareRisk ? 70 : 55);
            if (inv.hasProtostar) {
                candidates.add(new ScoredArchetype(SystemArchetype.YOUNG_PROTOPLANETARY, score));
            } else {
                candidates.add(new ScoredArchetype(SystemArchetype.STELLAR_FURNACE, score));
            }
        }
        if (inv.hasNeutronStar) {
            candidates.add(new ScoredArchetype(SystemArchetype.NEUTRON_STAR_RELIC, 85));
        }
        if (inv.hasBrownDwarf && !inv.hasBrownDwarf) {
            // Brown dwarf as primary (all stars are brown dwarfs)
            boolean allBrownDwarf = system.getStars().stream()
                    .allMatch(s -> s.getType() != null && s.getType().toLowerCase().contains("brown dwarf"));
            if (allBrownDwarf) {
                candidates.add(new ScoredArchetype(SystemArchetype.BROWN_DWARF_GLOOM, 70));
            }
        }
        // Fix: check if all stars are brown dwarfs properly
        if (inv.hasBrownDwarf) {
            boolean allBrownDwarf = system.getStars().stream()
                    .allMatch(s -> s.getType() != null && s.getType().toLowerCase().contains("brown dwarf"));
            if (allBrownDwarf) {
                candidates.add(new ScoredArchetype(SystemArchetype.BROWN_DWARF_GLOOM, 70));
            }
        }

        // Structure
        if (inv.totalPlanets == 0) {
            candidates.add(new ScoredArchetype(SystemArchetype.DEAD_SYSTEM, 95));
        } else if (inv.totalPlanets <= 2) {
            candidates.add(new ScoredArchetype(SystemArchetype.SPARSE_SYSTEM, 40));
        }

        if (inv.systemAgeMY > 8000 && "STABLE".equals(c.getOrbitalStability())) {
            candidates.add(new ScoredArchetype(SystemArchetype.GRAND_ARCHIVE, 45));
        }

        if (inv.lavaWorldCount >= 2) {
            candidates.add(new ScoredArchetype(SystemArchetype.LAVA_HELL, 60));
        }

        if (inv.terraformableCount >= 2 && inv.habitableCount == 0) {
            candidates.add(new ScoredArchetype(SystemArchetype.TERRAFORMER_PROSPECT, 55));
        }

        if (inv.habitableCount == 0 && inv.gasGiantCount >= 1 && inv.beltCount >= 1
                && "NONE".equals(c.getPopulationCapacity()) == false) {
            candidates.add(new ScoredArchetype(SystemArchetype.WAYSTATION, 35));
        }

        if (c.isHasDefensiveBelts() && inv.beltCount >= 2) {
            candidates.add(new ScoredArchetype(SystemArchetype.FORTRESS_SYSTEM, 50));
        }

        // Sort by score
        candidates.sort(Comparator.comparingInt(ScoredArchetype::score).reversed());

        // Remove duplicate brown dwarf entries
        List<ScoredArchetype> deduped = new ArrayList<>();
        Set<SystemArchetype> seen = new HashSet<>();
        for (ScoredArchetype sa : candidates) {
            if (seen.add(sa.archetype())) {
                deduped.add(sa);
            }
        }

        if (deduped.isEmpty()) {
            c.setPrimaryArchetype(SystemArchetype.ANOMALOUS);
        } else {
            c.setPrimaryArchetype(deduped.get(0).archetype());
            for (int i = 1; i < Math.min(3, deduped.size()); i++) {
                c.getSecondaryArchetypes().add(deduped.get(i).archetype());
            }
        }
    }

    // ================================================================
    // NOTABLE FEATURES
    // ================================================================

    private void collectNotableFeatures(SystemClassification c, SystemInventory inv, StarSystem system) {
        List<String> features = c.getNotableFeatures();

        if (inv.habitableCount > 0) {
            features.add(inv.habitableCount + " habitable world" + (inv.habitableCount > 1 ? "s" : ""));
        }
        if (inv.terraformableCount > 0) {
            features.add(inv.terraformableCount + " terraformable world" + (inv.terraformableCount > 1 ? "s" : ""));
        }
        if (inv.subsurfaceOceanMoons > 0) {
            features.add(inv.subsurfaceOceanMoons + " moon" + (inv.subsurfaceOceanMoons > 1 ? "s" : "") + " with subsurface oceans");
        }
        if (inv.biosignatureCount > 0) {
            features.add("Biosignature candidates detected");
        }
        if (inv.ringedPlanetCount > 0) {
            features.add(inv.ringedPlanetCount + " ringed planet" + (inv.ringedPlanetCount > 1 ? "s" : ""));
        }
        if (inv.totalMoons >= 30) {
            features.add("Extensive moon systems (" + inv.totalMoons + " total)");
        }
        if (inv.gasGiantCount >= 3) {
            features.add("Gas giant cluster (" + inv.gasGiantCount + " giants)");
        }
        if (inv.hasSuperflareRisk) {
            features.add("Superflare risk — intermittent sterilization events");
        }
        if (inv.hasNeutronStar) {
            features.add("Neutron star remnant");
        }
        if (inv.hasDyingStar) {
            features.add("Evolved stellar remnant — system in late phase");
        }
        if (inv.hasProtostar) {
            features.add("Protostellar system — still forming");
        }
        if (inv.beltCount >= 2) {
            features.add("Multiple asteroid/Kuiper belts");
        }
        if (inv.oceanWorldCount > 0) {
            features.add(inv.oceanWorldCount + " ocean world" + (inv.oceanWorldCount > 1 ? "s" : ""));
        }
        if (inv.lavaWorldCount > 0) {
            features.add(inv.lavaWorldCount + " lava world" + (inv.lavaWorldCount > 1 ? "s" : ""));
        }
    }

    // ================================================================
    // NARRATIVE GENERATION - System Summary (one-liner)
    // ================================================================

    private String generateSystemSummary(SystemClassification c, SystemInventory inv, StarSystem system) {
        SystemArchetype primary = c.getPrimaryArchetype();
        String starDesc = describeStarBrief(inv, system);

        return switch (primary) {
            case DEAD_SYSTEM -> starDesc + " with no planetary bodies — empty void.";
            case HABITABLE_OASIS -> starDesc + " harboring " + inv.habitableCount + " habitable world"
                    + (inv.habitableCount > 1 ? "s" : "") + " — a rare find.";
            case GARDEN_OF_EDEN -> starDesc + " with multiple habitable worlds — a colonist's paradise.";
            case MINING_BONANZA -> starDesc + " rich in extractable metals and mineral resources.";
            case GAS_GIANT_DOMINION -> starDesc + " dominated by " + inv.gasGiantCount + " gas giants — fuel and atmospheric resources.";
            case ICE_FRONTIER -> starDesc + " cloaked in ice and volatiles — frozen wealth.";
            case BINARY_CHAOS -> "A " + inv.starCount + "-star system with complex orbital dynamics and " + inv.totalPlanets + " worlds.";
            case DYING_GIANT_SYSTEM -> "A dying star system — the primary has entered its giant phase.";
            case STELLAR_FURNACE -> starDesc + " with violent stellar activity — radiation-drenched worlds.";
            case YOUNG_PROTOPLANETARY -> "A newborn system still condensing from its protoplanetary disk.";
            case NEUTRON_STAR_RELIC -> "A neutron star system — the aftermath of a stellar death.";
            case BROWN_DWARF_GLOOM -> "A dim brown dwarf system — perpetual twilight.";
            case SUBSURFACE_OCEAN_NETWORK -> starDesc + " with " + inv.subsurfaceOceanMoons + " moon"
                    + (inv.subsurfaceOceanMoons > 1 ? "s" : "") + " hiding subsurface oceans.";
            case BIOSIGNATURE_CANDIDATE -> starDesc + " — chemical signatures suggest possible biological activity.";
            case METAL_CORE_GRAVEYARD -> starDesc + " littered with iron-rich cores — heavy industry bonanza.";
            case FORTRESS_SYSTEM -> starDesc + " with dense belt networks — a natural defensive stronghold.";
            case WAYSTATION -> starDesc + " — useful for refueling but nothing to call home.";
            case TERRAFORMER_PROSPECT -> starDesc + " with worlds that could be made habitable.";
            case LAVA_HELL -> starDesc + " — molten surfaces and scorching heat dominate.";
            case OCEAN_WORLD_SYSTEM -> starDesc + " with " + inv.oceanWorldCount + " ocean world"
                    + (inv.oceanWorldCount > 1 ? "s" : "") + " — deep global seas and water to spare.";
            case GRAND_ARCHIVE -> starDesc + " — an ancient, stable system with billions of years of history.";
            case SPARSE_SYSTEM -> starDesc + " with only " + inv.totalPlanets + " planet" + (inv.totalPlanets > 1 ? "s" : "") + " — a lonely corner of space.";
            case PACKED_INNER_SYSTEM -> starDesc + " with tightly-packed inner worlds.";
            case ANOMALOUS -> starDesc + " — unusual characteristics defy easy classification.";
        };
    }

    // ================================================================
    // NARRATIVE GENERATION - Full Scout Report
    // ================================================================

    private String generateScoutReport(SystemClassification c, SystemInventory inv, StarSystem system) {
        StringBuilder report = new StringBuilder();

        // Opening
        report.append(generateOpening(c, inv, system));
        report.append("\n\n");

        // Stellar assessment
        report.append(generateStellarParagraph(inv, system));
        report.append("\n\n");

        // Planetary highlights
        if (inv.totalPlanets > 0) {
            report.append(generatePlanetaryParagraph(c, inv, system));
            report.append("\n\n");
        }

        // Resource & economic assessment
        report.append(generateEconomicParagraph(c, inv));
        report.append("\n\n");

        // Hazards
        if (!c.getDangerSources().isEmpty()) {
            report.append(generateHazardParagraph(c, inv));
            report.append("\n\n");
        }

        // Recommendation
        report.append(generateRecommendation(c, inv));

        return report.toString().trim();
    }

    private String generateOpening(SystemClassification c, SystemInventory inv, StarSystem system) {
        String name = system.getName() != null ? system.getName() : "Unknown";
        SystemArchetype archetype = c.getPrimaryArchetype();

        String[] openings = switch (archetype) {
            case HABITABLE_OASIS, GARDEN_OF_EDEN -> new String[]{
                    "The " + name + " system is the kind of discovery that changes careers.",
                    "Survey teams have flagged " + name + " as a priority colonial prospect.",
                    name + " — mark this one. It's the real thing."
            };
            case MINING_BONANZA, METAL_CORE_GRAVEYARD -> new String[]{
                    "Corporate prospectors will want to see the " + name + " assay results.",
                    "The " + name + " system reads like a mining company's wish list.",
                    name + " is dense with extractable resources. Someone's going to get rich here."
            };
            case DEAD_SYSTEM -> new String[]{
                    name + " is empty. A lone star, no companions, no prospects.",
                    "Survey of " + name + " complete. There's nothing here.",
                    "The " + name + " system: a star, some dust, and silence."
            };
            case BINARY_CHAOS -> new String[]{
                    name + " is a gravitational puzzle — " + inv.starCount + " stars in an intricate dance.",
                    "Navigation through " + name + " requires careful plotting. Multiple stellar bodies complicate everything.",
                    "The " + name + " system is beautiful to observe and treacherous to traverse."
            };
            case DYING_GIANT_SYSTEM -> new String[]{
                    name + " is a system on borrowed time. The primary star has entered its death throes.",
                    "Survey of " + name + " reveals a system in twilight — the star is dying.",
                    name + " — watch for expanding stellar envelope. This system won't last forever."
            };
            case STELLAR_FURNACE -> new String[]{
                    name + " is hot, angry, and dangerous. The star won't stop throwing punches.",
                    "Radiation levels in " + name + " exceed safe thresholds across most of the system.",
                    "The " + name + " star is violently active. Approach with hardened shielding only."
            };
            case GAS_GIANT_DOMINION -> new String[]{
                    name + " is a gas giant's kingdom — massive worlds dominate every orbital lane.",
                    "The " + name + " system is ruled by its gas giants. Fuel-skimming operations would thrive here.",
                    name + " — the giants run the show. " + inv.gasGiantCount + " behemoths and their retinue of moons."
            };
            case ICE_FRONTIER -> new String[]{
                    name + " is a frozen frontier — ice and volatiles as far as the sensors can reach.",
                    "The " + name + " system glitters with ice. Cold, yes, but rich in water and cryogenic resources.",
                    name + " — pack your thermal gear. Everything out here is frozen solid."
            };
            case YOUNG_PROTOPLANETARY -> new String[]{
                    name + " is still forming. Protoplanetary material swirls around a young stellar core.",
                    "The " + name + " system is a nursery — give it a few million years.",
                    name + " — astronomically young. Planets are still condensing from the disk."
            };
            case NEUTRON_STAR_RELIC -> new String[]{
                    name + " is a graveyard. A neutron star is all that remains of whatever was here before.",
                    "The " + name + " system is dominated by a neutron star — extreme physics territory.",
                    name + " — the star that was here died violently. What's left is fascinating and lethal."
            };
            case SUBSURFACE_OCEAN_NETWORK -> new String[]{
                    name + " hides its secrets beneath the ice. Multiple moons harbor subsurface oceans.",
                    "Xenobiologists will want priority access to " + name + " — ocean moons everywhere.",
                    name + " — the surface is dead ice, but underneath? Liquid water. Life?"
            };
            case OCEAN_WORLD_SYSTEM -> new String[]{
                    name + " is a water world system — deep oceans wrap entire planets here.",
                    "The " + name + " system is drenched. " + inv.oceanWorldCount + " worlds are ocean-covered from pole to pole.",
                    name + " — bring a submarine, not a rover. The oceans here run deeper than anything on Earth."
            };
            default -> new String[]{
                    "Initial survey of the " + name + " system complete. Here's what we found.",
                    name + " — a " + inv.totalPlanets + "-planet system in Sector " +
                            (system.getSector() != null ? system.getSector().getName() : "Unknown") + ".",
                    "Survey report for " + name + ". " + inv.starCount + " star" +
                            (inv.starCount > 1 ? "s" : "") + ", " + inv.totalPlanets + " planets."
            };
        };

        return openings[RandomUtils.rollRange(0, openings.length - 1)];
    }

    private String generateStellarParagraph(SystemInventory inv, StarSystem system) {
        StringBuilder sb = new StringBuilder();
        Star primary = inv.primaryStar;

        if (primary == null) return "No stellar data available.";

        String typeDesc = primary.getType() != null ? primary.getType() : "Unknown type";
        sb.append("The primary is a ").append(typeDesc);

        if (primary.getAgeMY() != null) {
            double ageBY = primary.getAgeMY() / 1000.0;
            if (ageBY >= 1.0) {
                sb.append(String.format(", roughly %.1f billion years old", ageBY));
            } else {
                sb.append(String.format(", approximately %.0f million years old", primary.getAgeMY()));
            }
        }

        String evo = primary.getEvolutionaryStage();
        if (evo != null) {
            String evoDesc = switch (evo) {
                case "EARLY_MAIN_SEQUENCE" -> {
                    Double ageMY = primary.getAgeMY();
                    if (ageMY != null && ageMY > 5000) {
                        yield ", young relative to its immense lifespan";
                    } else {
                        yield ", still in its youth";
                    }
                }
                case "MID_MAIN_SEQUENCE" -> {
                    Double msFraction = primary.getMainSequenceFraction();
                    if (msFraction != null && msFraction > 0.7) {
                        yield ", mature and well past its midlife";
                    } else if (msFraction != null && msFraction > 0.4) {
                        yield ", in the prime of its life";
                    } else {
                        yield ", settled into a long stable burn";
                    }
                }
                case "LATE_MAIN_SEQUENCE" -> ", approaching the end of its main sequence";
                case "RED_GIANT_BRANCH" -> ", swollen into a red giant";
                case "PRE_MAIN_SEQUENCE" -> ", still contracting toward the main sequence";
                case "WHITE_DWARF_COOLING" -> ", cooling as a white dwarf remnant";
                case "NEUTRON_STAR" -> ", collapsed into an impossibly dense remnant";
                case "BROWN_DWARF_COOLING" -> ", never quite massive enough to sustain fusion";
                default -> "";
            };
            sb.append(evoDesc);
        }
        sb.append(".");

        // Activity
        String activity = primary.getActivityLevel();
        if (activity != null && !"INACTIVE".equals(activity) && !"LOW".equals(activity)) {
            sb.append(" Stellar activity is ");
            sb.append(switch (activity) {
                case "MODERATE" -> "moderate — occasional flares and elevated UV";
                case "ACTIVE" -> "elevated — regular flare events and significant particle flux";
                case "VERY_ACTIVE" -> "high — frequent flaring, strong stellar winds, hazardous radiation";
                case "HYPERACTIVE" -> "extreme — near-continuous flaring, massive coronal ejections";
                default -> activity.toLowerCase();
            });
            sb.append(".");
        }

        // Multi-star
        if (inv.isMultiStar) {
            BinaryConfiguration config = system.getBinaryConfiguration();
            if (config != null) {
                sb.append(" This is a ").append(inv.starCount).append("-star system");
                if (system.getBinarySeparationAu() != null) {
                    sb.append(String.format(" with %.1f AU separation", system.getBinarySeparationAu()));
                }
                sb.append(".");
            }
        }

        // Remaining lifetime
        if (primary.getEstimatedRemainingMsMy() != null && primary.getEstimatedRemainingMsMy() > 0) {
            double remainBY = primary.getEstimatedRemainingMsMy() / 1000.0;
            if (remainBY < 1.0) {
                sb.append(String.format(" Estimated main sequence lifetime remaining: %.0f million years — the clock is ticking.",
                        primary.getEstimatedRemainingMsMy()));
            } else if (remainBY < 5.0) {
                sb.append(String.format(" About %.1f billion years of stable burning remain.", remainBY));
            }
            // If > 5BY remaining, don't bother mentioning — it's plenty
        }

        return sb.toString();
    }

    private String generatePlanetaryParagraph(SystemClassification c, SystemInventory inv, StarSystem system) {
        StringBuilder sb = new StringBuilder();

        sb.append("The system contains ").append(inv.totalPlanets).append(" planet");
        if (inv.totalPlanets != 1) sb.append("s");

        if (inv.totalMoons > 0) {
            sb.append(" and ").append(inv.totalMoons).append(" catalogued moon");
            if (inv.totalMoons != 1) sb.append("s");
        }
        sb.append(".");

        // Highlight composition
        List<String> highlights = new ArrayList<>();
        if (inv.gasGiantCount > 0) highlights.add(inv.gasGiantCount + " gas giant" + (inv.gasGiantCount > 1 ? "s" : ""));
        if (inv.iceGiantCount > 0) highlights.add(inv.iceGiantCount + " ice giant" + (inv.iceGiantCount > 1 ? "s" : ""));
        if (inv.iceWorldCount > 0) highlights.add(inv.iceWorldCount + " ice world" + (inv.iceWorldCount > 1 ? "s" : ""));
        if (inv.oceanWorldCount > 0) highlights.add(inv.oceanWorldCount + " ocean world" + (inv.oceanWorldCount > 1 ? "s" : ""));
        if (inv.lavaWorldCount > 0) highlights.add(inv.lavaWorldCount + " lava world" + (inv.lavaWorldCount > 1 ? "s" : ""));
        if (inv.desertWorldCount > 0) highlights.add(inv.desertWorldCount + " desert world" + (inv.desertWorldCount > 1 ? "s" : ""));

        if (!highlights.isEmpty()) {
            sb.append(" The roster includes ").append(String.join(", ", highlights)).append(".");
        }

        // Habitable worlds — the money paragraph
        if (inv.habitableCount > 0) {
            sb.append(" Crucially, ");
            if (inv.habitableCount == 1) {
                sb.append("one world sits in the habitable zone with conditions supporting open-air colonization");
            } else {
                sb.append(inv.habitableCount).append(" worlds support habitable conditions");
            }
            sb.append(".");
            if (inv.shirtSleeveCount > 0) {
                sb.append(" Breathable atmosphere confirmed — shirt-sleeve environment.");
            }
        } else if (inv.terraformableCount > 0) {
            sb.append(" No worlds are immediately habitable, but ").append(inv.terraformableCount);
            sb.append(" show").append(inv.terraformableCount == 1 ? "s" : "");
            sb.append(" terraforming potential.");
        }

        // Subsurface oceans
        if (inv.subsurfaceOceanMoons > 0) {
            sb.append(" Notably, ").append(inv.subsurfaceOceanMoons).append(" moon");
            if (inv.subsurfaceOceanMoons != 1) sb.append("s");
            sb.append(" show").append(inv.subsurfaceOceanMoons == 1 ? "s" : "");
            sb.append(" evidence of subsurface liquid water oceans beneath their ice shells.");
        }

        // Belts
        if (inv.beltCount > 0) {
            sb.append(" ").append(inv.beltCount).append(" asteroid/Kuiper belt");
            if (inv.beltCount != 1) sb.append("s");
            sb.append(" provide").append(inv.beltCount == 1 ? "s" : "");
            sb.append(" additional resource extraction opportunities.");
        }

        return sb.toString();
    }

    private String generateEconomicParagraph(SystemClassification c, SystemInventory inv) {
        StringBuilder sb = new StringBuilder();
        sb.append("Economic assessment: ");

        List<String> econ = new ArrayList<>();
        econ.add("mineral richness is " + c.getMineralRichness().toLowerCase().replace('_', ' '));
        econ.add("fuel availability " + c.getFuelAvailability().toLowerCase().replace('_', ' '));
        econ.add("water accessibility " + c.getWaterAccessibility().toLowerCase().replace('_', ' '));

        sb.append(String.join(", ", econ)).append(".");

        if ("HIGH".equals(c.getIndustrialPotential()) || "EXCEPTIONAL".equals(c.getIndustrialPotential())) {
            sb.append(" Overall industrial potential is ").append(c.getIndustrialPotential().toLowerCase());
            sb.append(" — this system could support sustained extraction and manufacturing operations.");
        } else if ("MODERATE".equals(c.getIndustrialPotential())) {
            sb.append(" Industrial potential is moderate — worthwhile for targeted operations.");
        } else {
            sb.append(" Industrial potential is limited.");
        }

        return sb.toString();
    }

    private String generateHazardParagraph(SystemClassification c, SystemInventory inv) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hazard assessment [").append(c.getDangerRating()).append("]: ");

        sb.append(String.join(". ", c.getDangerSources())).append(".");

        sb.append(" Radiation environment: ").append(c.getRadiationEnvironment().toLowerCase());
        sb.append(". Orbital stability: ").append(c.getOrbitalStability().toLowerCase().replace('_', ' '));
        sb.append(".");

        return sb.toString();
    }

    private String generateRecommendation(SystemClassification c, SystemInventory inv) {
        StringBuilder sb = new StringBuilder();
        sb.append("Recommendation: ");

        // Find the highest interest rating
        int maxInterest = 0;
        String topFaction = "general exploration";

        if (c.getColonizationInterest() > maxInterest) { maxInterest = c.getColonizationInterest(); topFaction = "colonial settlement"; }
        if (c.getMiningInterest() > maxInterest) { maxInterest = c.getMiningInterest(); topFaction = "mining and extraction"; }
        if (c.getScientificInterest() > maxInterest) { maxInterest = c.getScientificInterest(); topFaction = "scientific research"; }
        if (c.getXenobiologyInterest() > maxInterest) { maxInterest = c.getXenobiologyInterest(); topFaction = "xenobiological study"; }
        if (c.getMilitaryInterest() > maxInterest) { maxInterest = c.getMilitaryInterest(); topFaction = "military staging"; }
        if (c.getExplorationInterest() > maxInterest) { maxInterest = c.getExplorationInterest(); topFaction = "deep exploration"; }

        if (maxInterest >= 8) {
            sb.append("High-priority target for ").append(topFaction).append(".");
        } else if (maxInterest >= 5) {
            sb.append("Moderate interest for ").append(topFaction).append(".");
        } else if (maxInterest >= 3) {
            sb.append("Low-priority. ").append(topFaction.substring(0, 1).toUpperCase())
                    .append(topFaction.substring(1)).append(" teams may find limited value.");
        } else {
            sb.append("Negligible value. Log and move on.");
        }

        // Secondary recommendations
        List<String> alsoGood = new ArrayList<>();
        if (c.getMiningInterest() >= 6 && !topFaction.equals("mining and extraction")) alsoGood.add("mining");
        if (c.getColonizationInterest() >= 6 && !topFaction.equals("colonial settlement")) alsoGood.add("colonization");
        if (c.getScientificInterest() >= 6 && !topFaction.equals("scientific research")) alsoGood.add("research");
        if (c.getXenobiologyInterest() >= 6 && !topFaction.equals("xenobiological study")) alsoGood.add("xenobiology");
        if (c.getMilitaryInterest() >= 6 && !topFaction.equals("military staging")) alsoGood.add("military use");

        if (!alsoGood.isEmpty()) {
            sb.append(" Also of interest for: ").append(String.join(", ", alsoGood)).append(".");
        }

        return sb.toString();
    }

    // ================================================================
    // HELPER METHODS
    // ================================================================

    private String describeStarBrief(SystemInventory inv, StarSystem system) {
        Star primary = inv.primaryStar;
        if (primary == null) return "An uncharacterized star";

        String spectral = primary.getSpectralType() != null ? primary.getSpectralType() : "?";
        String type = primary.getType() != null ? primary.getType() : "Unknown";

        if (inv.isMultiStar) {
            return "A " + inv.starCount + "-star system (primary: " + type + ")";
        }
        return "A " + type + " (" + spectral + "-class) system";
    }

    // ================================================================
    // INNER CLASSES
    // ================================================================

    private record ScoredArchetype(SystemArchetype archetype, int score) {}

    /**
     * Temporary holder for all the raw counts and flags we need.
     */
    private static class SystemInventory {
        Star primaryStar;
        int starCount;
        boolean isMultiStar;
        List<String> starTypes = new ArrayList<>();
        List<String> starSpectralTypes = new ArrayList<>();
        List<String> evolutionaryStages = new ArrayList<>();
        boolean hasHighActivity;
        boolean hasSuperflareRisk;
        boolean hasProtostar;
        boolean hasDyingStar;
        boolean hasNeutronStar;
        boolean hasWhiteDwarf;
        boolean hasBrownDwarf;
        boolean hasModerateActivity;

        int totalPlanets;
        int gasGiantCount;
        int iceGiantCount;
        int lavaWorldCount;
        int iceWorldCount;
        int oceanWorldCount;
        int desertWorldCount;
        int carbonWorldCount;
        int ironWorldCount;
        int metalRichCount;
        int waterRichCount;
        int liquidWaterBodies;
        int subsurfaceWaterCount;
        int ringedPlanetCount;
        int geologicallyActiveCount;
        List<String> planetTypes = new ArrayList<>();
        List<String> compositionClasses = new ArrayList<>();

        int habitableCount;
        int terraformableCount;
        int subsurfaceHabitableCount;
        int resourceWorldCount;
        int biosignatureCount;
        int lifePotentialBodies;
        int shirtSleeveCount;
        int assistedCount;
        int domeOnlyCount;

        int totalMoons;
        int icyMoonCount;
        int subsurfaceOceanMoons;

        int beltCount;
        int innerBeltCount;
        int outerBeltCount;
        double totalBeltMass;
        long totalBeltObjects;
        List<String> beltTypes = new ArrayList<>();

        double systemAgeMY;
    }
}