package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ud.*;
import com.brickroad.starcreator_webservice.enums.LiquidInventory;
import com.brickroad.starcreator_webservice.enums.VolatileType;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import com.brickroad.starcreator_webservice.utils.planets.TemperatureClimateCalculator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HydrologyCreator {

    // ── Water phase constants ──
    private static final double WATER_TRIPLE_POINT_TEMP_K = 273.16;
    private static final double WATER_TRIPLE_POINT_PRESSURE_ATM = 0.00604;
    private static final double WATER_BOILING_100C_K = 373.15;
    private static final double WATER_CRITICAL_TEMP_K = 647.0;
    private static final double ICE_SUBLIMATION_LIMIT_K = 170.0;

    // ── Ammonia phase constants ──
    private static final double AMMONIA_FREEZING_K = 195.4;
    private static final double AMMONIA_BOILING_K = 239.8;
    private static final double AMMONIA_WATER_EUTECTIC_K = 195.0;  // ~33% NH3

    // ── Methane phase constants ──
    private static final double METHANE_FREEZING_K = 90.7;
    private static final double METHANE_BOILING_K = 111.7;
    private static final double ETHANE_BOILING_K = 184.6;

    // ═══════════════════════════════════════════════════════════════
    //  Planet Hydrology
    // ═══════════════════════════════════════════════════════════════

    public HydrologyProperties createPlanetHydrology(Planet planet, Star parentStar) {
        String planetType = planet.getPlanetType() != null ? planet.getPlanetType().toLowerCase() : "";

        if (isGaseousBody(planetType)) {
            return null;
        }

        HydrologyProperties hp = new HydrologyProperties();
        hp.setLabel("Planet hydrology");

        LiquidInventory inventory = determineWaterInventory(planet, parentStar);
        hp.setLiquidInventory(inventory.name());

        // Always determine volatile type — it tells us WHAT substance is present,
        // even if inventory says there's very little of it. An ICE_RICH dwarf planet
        // with NONE inventory still has water ice as its dominant volatile.
        VolatileType liquidType = determineVolatileType(planet);
        hp.setVolatileType(liquidType.name());

        // If truly no volatile identified, ensure inventory is also NONE
        if (liquidType == VolatileType.NONE) {
            hp.setLiquidInventory(LiquidInventory.NONE.name());
            hp.setLiquidCoveragePercent(0.0);
            hp.setLiquidSurfaceCoveragePercent(0.0);
            hp.setFrozenLiquidCoveragePercent(0.0);
            hp.setWaterIceCoveragePercent(0.0);
            hp.setHasSubsurfaceLiquid(false);
            return hp;
        }

        // If volatile exists but inventory is NONE, still set zero coverage and return
        if (inventory == LiquidInventory.NONE) {
            hp.setLiquidCoveragePercent(0.0);
            hp.setLiquidSurfaceCoveragePercent(0.0);
            hp.setFrozenLiquidCoveragePercent(0.0);
            hp.setWaterIceCoveragePercent(0.0);
            hp.setHasSubsurfaceLiquid(false);
            return hp;
        }

        // Calculate phase points adjusted for pressure
        double pressure = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 0.0;
        double nh3Fraction = getGasPercentage(planet.getAtmosphere(), "NH3") / 100.0;
        double[] phasePoints = calculatePhasePoints(liquidType, pressure, nh3Fraction);
        hp.setFreezingPointK(phasePoints[0]);
        hp.setBoilingPointK(phasePoints[1]);

        // Distribute liquid using dynamic phase points
        distributeLiquid(hp, planet, inventory, liquidType, phasePoints[0], phasePoints[1]);

        // Set water ice coverage
        calculateWaterIceCoverage(hp, planet, inventory, liquidType);

        // Subsurface assessment
        assessSubsurfaceLiquid(hp, planet, inventory);

        // Ocean depth for surface ocean worlds
        calculateSurfaceOceanDepth(hp, planet, inventory);

        // Post-distribution inventory correction
        inventory = correctInventoryForActualState(hp, inventory);
        hp.setLiquidInventory(inventory.name());

        // Total coverage
        double liquid = hp.getLiquidSurfaceCoveragePercent() != null ? hp.getLiquidSurfaceCoveragePercent() : 0.0;
        double frozen = hp.getFrozenLiquidCoveragePercent() != null ? hp.getFrozenLiquidCoveragePercent() : 0.0;
        hp.setLiquidCoveragePercent(Math.min(100.0, liquid + frozen));

        // Composition string and colors
        hp.setLiquidComposition(buildCompositionString(liquidType, nh3Fraction));
        deriveLiquidColors(hp, liquidType);

        return hp;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Moon Hydrology
    // ═══════════════════════════════════════════════════════════════

    public HydrologyProperties createMoonHydrology(Moon moon) {
        HydrologyProperties hp = new HydrologyProperties();
        hp.setLabel("Moon hydrology");

        // Step 1: Determine subsurface ocean
        determineSubsurfaceOcean(hp, moon);

        double moonMass = moon.getEarthMass() != null ? moon.getEarthMass() : 0;

        // Step 2: Tiny moonlets get simplified defaults
        if (moonMass < 0.0005) {
            double tinyTemp = moon.getSurfaceTemp() != null ? moon.getSurfaceTemp() : 100.0;
            double tinyPressure = moon.getSurfacePressure() != null ? moon.getSurfacePressure() : 0.0;
            boolean tinySurfaceIcePossible = (tinyPressure >= WATER_TRIPLE_POINT_PRESSURE_ATM)
                    ? tinyTemp < WATER_TRIPLE_POINT_TEMP_K
                    : tinyTemp < ICE_SUBLIMATION_LIMIT_K;

            // Even tiny moonlets should identify their volatile type
            VolatileType tinyVolatile = determineMoonVolatileType(moon);

            if (Boolean.TRUE.equals(hp.getHasSubsurfaceOcean())) {
                hp.setLiquidInventory("MODERATE");
                hp.setVolatileType(tinyVolatile != VolatileType.NONE ? tinyVolatile.name() : VolatileType.WATER.name());
                hp.setLiquidSurfaceCoveragePercent(0.0);
                double icePercent = tinySurfaceIcePossible ? RandomUtils.rollRange(40.0, 90.0) : 0.0;
                hp.setFrozenLiquidCoveragePercent(icePercent);
                hp.setWaterIceCoveragePercent(icePercent);
                hp.setLiquidCoveragePercent(icePercent);
                hp.setHasSubsurfaceLiquid(true);
                if (hp.getSubsurfaceLiquidDepthKm() == null && hp.getIceShellThicknessKm() != null) {
                    hp.setSubsurfaceLiquidDepthKm(hp.getIceShellThicknessKm());
                }
                hp.setFreezingPointK(WATER_TRIPLE_POINT_TEMP_K);
                hp.setBoilingPointK(WATER_BOILING_100C_K);
                hp.setLiquidComposition("H2O");
                deriveLiquidColors(hp, tinyVolatile != VolatileType.NONE ? tinyVolatile : VolatileType.WATER);
            } else {
                hp.setVolatileType(tinyVolatile.name());
                hp.setLiquidInventory("NONE");
                hp.setLiquidSurfaceCoveragePercent(0.0);
                hp.setFrozenLiquidCoveragePercent(0.0);
                hp.setWaterIceCoveragePercent(0.0);
                hp.setLiquidCoveragePercent(0.0);
                hp.setHasSubsurfaceLiquid(false);
            }
            return hp;
        }

        // Step 3: Full generation for larger moons
        LiquidInventory inventory = determineMoonWaterInventory(moon, hp);
        hp.setLiquidInventory(inventory.name());

        // Always determine volatile type — it tells us WHAT substance is present
        // regardless of inventory level (HOW MUCH)
        VolatileType liquidType = determineMoonVolatileType(moon);
        hp.setVolatileType(liquidType.name());

        // If truly no volatile identified, ensure inventory is also NONE
        if (liquidType == VolatileType.NONE) {
            hp.setLiquidInventory(LiquidInventory.NONE.name());
            hp.setLiquidCoveragePercent(0.0);
            hp.setLiquidSurfaceCoveragePercent(0.0);
            hp.setFrozenLiquidCoveragePercent(0.0);
            hp.setWaterIceCoveragePercent(0.0);
            hp.setHasSubsurfaceLiquid(Boolean.TRUE.equals(hp.getHasSubsurfaceOcean()));
            if (hp.getHasSubsurfaceLiquid() && hp.getSubsurfaceLiquidDepthKm() == null && hp.getIceShellThicknessKm() != null) {
                hp.setSubsurfaceLiquidDepthKm(hp.getIceShellThicknessKm());
            }
            return hp;
        }

        // If volatile exists but inventory is NONE, still keep volatile type label
        // but set zero coverage
        if (inventory == LiquidInventory.NONE) {
            hp.setLiquidCoveragePercent(0.0);
            hp.setLiquidSurfaceCoveragePercent(0.0);
            hp.setFrozenLiquidCoveragePercent(0.0);
            hp.setWaterIceCoveragePercent(0.0);
            hp.setHasSubsurfaceLiquid(Boolean.TRUE.equals(hp.getHasSubsurfaceOcean()));
            if (hp.getHasSubsurfaceLiquid() && hp.getSubsurfaceLiquidDepthKm() == null && hp.getIceShellThicknessKm() != null) {
                hp.setSubsurfaceLiquidDepthKm(hp.getIceShellThicknessKm());
            }
            return hp;
        }

        double pressure = moon.getSurfacePressure() != null ? moon.getSurfacePressure() : 0.0;
        double nh3Fraction = getGasPercentage(moon.getAtmosphere(), "NH3") / 100.0;
        double[] phasePoints = calculatePhasePoints(liquidType, pressure, nh3Fraction);
        hp.setFreezingPointK(phasePoints[0]);
        hp.setBoilingPointK(phasePoints[1]);

        distributeMoonLiquid(hp, moon, inventory, liquidType, phasePoints[0], phasePoints[1]);
        calculateWaterIceCoverageMoon(hp, moon, inventory, liquidType);
        assessMoonSubsurfaceLiquid(hp, moon, inventory);

        // Post-distribution inventory correction: if the volatile can't exist in any form
        // on the surface AND there's no subsurface liquid, the inventory label is misleading.
        // The volatile has been lost to outgassing/thermal escape at these temperatures.
        inventory = correctInventoryForActualState(hp, inventory);
        hp.setLiquidInventory(inventory.name());

        double liquid = hp.getLiquidSurfaceCoveragePercent() != null ? hp.getLiquidSurfaceCoveragePercent() : 0.0;
        double frozen = hp.getFrozenLiquidCoveragePercent() != null ? hp.getFrozenLiquidCoveragePercent() : 0.0;
        hp.setLiquidCoveragePercent(Math.min(100.0, liquid + frozen));

        hp.setLiquidComposition(buildCompositionString(liquidType, nh3Fraction));
        deriveLiquidColors(hp, liquidType);

        return hp;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Liquid Type Determination
    // ═══════════════════════════════════════════════════════════════

    private VolatileType determineVolatileType(Planet planet) {
        Double surfaceTemp = planet.getSurfaceTemp();
        if (surfaceTemp == null) return VolatileType.WATER;

        double temp = surfaceTemp;
        double pressure = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 0.0;
        String composition = planet.getCompositionClassification() != null ? planet.getCompositionClassification() : "";
        double ch4Pct = getGasPercentage(planet.getAtmosphere(), "CH4");
        double nh3Pct = getGasPercentage(planet.getAtmosphere(), "NH3");
        double h2oPct = getGasPercentage(planet.getAtmosphere(), "H2O");

        return classifyVolatileType(temp, pressure, composition, ch4Pct, nh3Pct, h2oPct);
    }

    private VolatileType determineMoonVolatileType(Moon moon) {
        Double surfaceTemp = moon.getSurfaceTemp();
        if (surfaceTemp == null) return VolatileType.WATER;

        double temp = surfaceTemp;
        double pressure = moon.getSurfacePressure() != null ? moon.getSurfacePressure() : 0.0;
        String composition = moon.getCompositionClassification() != null ? moon.getCompositionClassification() : "";
        double ch4Pct = getGasPercentage(moon.getAtmosphere(), "CH4");
        double nh3Pct = getGasPercentage(moon.getAtmosphere(), "NH3");
        double h2oPct = getGasPercentage(moon.getAtmosphere(), "H2O");

        return classifyVolatileType(temp, pressure, composition, ch4Pct, nh3Pct, h2oPct);
    }

    private VolatileType classifyVolatileType(double temp, double pressure, String composition,
                                           double ch4Pct, double nh3Pct, double h2oPct) {
        // ── Step 1: Identify dominant volatile from atmosphere and composition ──
        // The liquid type describes WHAT the volatile is, not whether it's currently
        // in liquid phase. Temperature determines phase (liquid/frozen/vapor) later.

        boolean hasWaterVolatile = h2oPct > 0.1
                || "ICE_RICH".equals(composition)
                || "MIXED_SILICATE_ICE".equals(composition)
                || "OCEAN_WORLD".equals(composition);

        boolean hasMethaneVolatile = ch4Pct > 0.5
                || "CARBON_RICH".equals(composition);

        boolean hasAmmoniaVolatile = nh3Pct > 0.1;

        // ── Step 2: When multiple volatiles are present, determine the dominant one ──
        // Priority considers both concentration and temperature regime.

        // Ammonia-dominated worlds: when NH3 is the primary volatile (>> CH4),
        // ammonia chemistry wins even if some methane is present.
        // On real worlds, ammonia-water eutectic mixtures (~195K) are far more
        // common than pure ammonia lakes. Pure ammonia only when too cold for
        // any water to participate.
        if (hasAmmoniaVolatile && nh3Pct > ch4Pct) {
            // Pure ammonia: very cold, below the eutectic point where water
            // is completely frozen out and only pure NH3 liquid/ice exists
            if (nh3Pct > 5.0 && temp < AMMONIA_WATER_EUTECTIC_K && temp >= AMMONIA_FREEZING_K * 0.5) {
                return VolatileType.AMMONIA;
            }

            // Ammonia-water eutectic mix: NH3 depresses water's freezing point,
            // enabling liquid/ice mixtures from the eutectic (~195K) up to water
            // freezing (~273K). This is the most common ammonia volatile regime.
            if (nh3Pct > 0.5 && temp >= AMMONIA_WATER_EUTECTIC_K * 0.5 && temp < WATER_TRIPLE_POINT_TEMP_K) {
                return VolatileType.AMMONIA_WATER;
            }

            // Warm ammonia worlds above water freezing: ammonia acts as antifreeze
            // in liquid water — still an ammonia-water system
            if (nh3Pct > 5.0 && temp >= WATER_TRIPLE_POINT_TEMP_K && temp <= AMMONIA_BOILING_K * 1.5) {
                return VolatileType.AMMONIA_WATER;
            }
        }

        // Methane-dominated: cold worlds where CH4 is the primary volatile
        // and ammonia is not dominant
        if (hasMethaneVolatile && temp <= ETHANE_BOILING_K) {
            if (ch4Pct > 1.0 && temp <= METHANE_BOILING_K * 1.1) {
                return VolatileType.METHANE;
            }
            // Methane-ethane mix for slightly warmer hydrocarbon worlds
            if (temp > METHANE_BOILING_K || ch4Pct <= 1.0) {
                return VolatileType.METHANE_ETHANE;
            }
            return VolatileType.METHANE;
        }

        // Water: the most common volatile — atmosphere H2O, ice-rich composition,
        // or sufficient pressure/temp for water cycle
        if (hasWaterVolatile) {
            return VolatileType.WATER;
        }

        // Pressure + temperature suggests water is viable even without atmospheric detection
        if (pressure >= WATER_TRIPLE_POINT_PRESSURE_ATM && temp >= WATER_TRIPLE_POINT_TEMP_K) {
            return VolatileType.WATER;
        }

        // High-pressure water above normal boiling
        if (pressure > 1.0 && temp > WATER_BOILING_100C_K) {
            double maxLiquidTemp = WATER_BOILING_100C_K + (pressure - 1.0) * 20.0;
            maxLiquidTemp = Math.min(maxLiquidTemp, WATER_CRITICAL_TEMP_K);
            if (temp <= maxLiquidTemp) {
                return VolatileType.WATER;
            }
        }

        return VolatileType.NONE;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Phase Point Calculation
    // ═══════════════════════════════════════════════════════════════

    double[] calculatePhasePoints(VolatileType liquidType, double pressure, double nh3Fraction) {
        double freezeK;
        double boilK;

        switch (liquidType) {
            case WATER -> {
                freezeK = WATER_TRIPLE_POINT_TEMP_K;
                boilK = WATER_BOILING_100C_K;
                // Pressure-adjusted boiling (existing Clausius-Clapeyron approximation)
                if (pressure > 1.0) {
                    boilK = WATER_BOILING_100C_K + (pressure - 1.0) * 20.0;
                    boilK = Math.min(boilK, WATER_CRITICAL_TEMP_K);
                }
            }
            case AMMONIA_WATER -> {
                // Freezing point depression from NH3: eutectic at ~33% NH3 → 195K
                double clampedFraction = Math.min(nh3Fraction, 0.33);
                freezeK = WATER_TRIPLE_POINT_TEMP_K - (WATER_TRIPLE_POINT_TEMP_K - AMMONIA_WATER_EUTECTIC_K)
                        * (clampedFraction / 0.33);
                // Boiling scales between ammonia boiling and water boiling
                boilK = AMMONIA_BOILING_K + (WATER_BOILING_100C_K - AMMONIA_BOILING_K)
                        * (1.0 - clampedFraction / 0.33);
                if (pressure > 1.0) {
                    boilK += (pressure - 1.0) * 15.0;
                    boilK = Math.min(boilK, WATER_CRITICAL_TEMP_K);
                }
            }
            case AMMONIA -> {
                freezeK = AMMONIA_FREEZING_K;
                boilK = AMMONIA_BOILING_K;
                if (pressure > 1.0) {
                    boilK += (pressure - 1.0) * 12.0;
                    boilK = Math.min(boilK, 405.0); // Ammonia critical temp ~405K
                }
            }
            case METHANE -> {
                freezeK = METHANE_FREEZING_K;
                boilK = METHANE_BOILING_K;
                if (pressure > 1.0) {
                    boilK += (pressure - 1.0) * 8.0;
                    boilK = Math.min(boilK, 190.6); // Methane critical temp ~190.6K
                }
            }
            case METHANE_ETHANE -> {
                freezeK = METHANE_FREEZING_K;
                boilK = ETHANE_BOILING_K;
                if (pressure > 1.0) {
                    boilK += (pressure - 1.0) * 10.0;
                    boilK = Math.min(boilK, 305.3); // Ethane critical temp ~305.3K
                }
            }
            default -> {
                freezeK = 0;
                boilK = 0;
            }
        }

        return new double[]{freezeK, boilK};
    }

    // ═══════════════════════════════════════════════════════════════
    //  Planet Liquid Distribution (dynamic phase points)
    // ═══════════════════════════════════════════════════════════════

    private void distributeLiquid(HydrologyProperties hp, Planet planet,
                                   LiquidInventory inventory, VolatileType liquidType,
                                   double freezeK, double boilK) {
        Double surfaceTemp = planet.getSurfaceTemp();
        double pressure = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 0.0;

        if (surfaceTemp == null || liquidType == VolatileType.NONE) {
            double frozenPercent = getBaseIcePercent(inventory) * 0.5;
            hp.setLiquidSurfaceCoveragePercent(0.0);
            hp.setFrozenLiquidCoveragePercent(frozenPercent);
            return;
        }

        double temp = surfaceTemp;

        // Tidally locked planets: use zone temperatures instead of global average.
        // The antistellar hemisphere can be below freezing even when surfaceTemp is above.
        if (Boolean.TRUE.equals(planet.getTidallyLocked())) {
            distributeTidallyLockedLiquid(hp, planet, inventory, liquidType, temp, pressure, freezeK, boilK);
            return;
        }

        // Check if liquid phase is possible at this temp/pressure
        boolean liquidPossible = temp >= freezeK && temp <= boilK
                && pressure >= getMinPressureForLiquid(liquidType);

        if (liquidPossible) {
            distributeLiquidDominated(hp, planet, inventory, temp, freezeK, boilK);
        } else if (temp < freezeK) {
            distributeFrozenDominated(hp, inventory, temp, pressure, freezeK, liquidType);
        } else {
            // Too hot — liquid has boiled off
            hp.setLiquidSurfaceCoveragePercent(0.0);
            hp.setFrozenLiquidCoveragePercent(0.0);
        }
    }

    /**
     * For tidally locked planets, compute ice/liquid distribution using hemisphere zone
     * temperatures instead of the single global average. The substellar, terminator, and
     * antistellar zones each get their own freeze/liquid assessment, weighted by coverage.
     */
    private void distributeTidallyLockedLiquid(HydrologyProperties hp, Planet planet,
                                                LiquidInventory inventory, VolatileType liquidType,
                                                double surfaceTemp, double pressure,
                                                double freezeK, double boilK) {
        double redistribution = TemperatureClimateCalculator.tidalRedistributionFactor(pressure);
        double substellarTemp = surfaceTemp * (1.0 + 0.3 * (1.0 - redistribution));
        double antistellarTemp = surfaceTemp * (1.0 - 0.4 * (1.0 - redistribution));
        double terminatorTemp = (substellarTemp + antistellarTemp) / 2.0;

        // Zone coverages (same as TemperatureClimateCalculator)
        double substellarCov = 0.30;
        double terminatorCov = 0.20;
        double antistellarCov = 0.50;

        boolean hasPressure = pressure >= getMinPressureForLiquid(liquidType);

        // Classify each zone
        double frozenFraction = 0;
        double liquidFraction = 0;

        double[][] zones = {
            {substellarTemp, substellarCov},
            {terminatorTemp, terminatorCov},
            {antistellarTemp, antistellarCov}
        };

        for (double[] zone : zones) {
            double zoneTemp = zone[0];
            double zoneCov = zone[1];

            if (zoneTemp < freezeK) {
                // Below freezing — this zone is frozen
                frozenFraction += zoneCov;
            } else if (zoneTemp <= boilK && hasPressure) {
                // Liquid possible in this zone
                liquidFraction += zoneCov;
                // Near-freezing transition: part of the zone ices over at boundaries
                if (zoneTemp < freezeK + 15) {
                    double transitionIce = zoneCov * 0.3;
                    frozenFraction += transitionIce;
                    liquidFraction -= transitionIce;
                }
            }
            // else: above boiling — volatile lost, contributes neither
        }

        // Apply zone fractions to base coverage from inventory
        double baseLiquid = getBaseLiquidPercent(inventory);
        double baseIce = getBaseIcePercent(inventory);

        double liquidPercent;
        double frozenPercent;

        if (frozenFraction > 0 && liquidFraction > 0) {
            // Mixed world: liquid on warm side, ice on cold side
            liquidPercent = baseLiquid * liquidFraction * RandomUtils.rollRange(0.8, 1.2);
            frozenPercent = baseIce * (frozenFraction / (frozenFraction + liquidFraction))
                    + baseLiquid * frozenFraction * 0.5;
            frozenPercent *= RandomUtils.rollRange(0.8, 1.2);
        } else if (frozenFraction > 0) {
            // Entirely frozen world
            liquidPercent = 0;
            frozenPercent = baseIce * RandomUtils.rollRange(0.8, 1.2);
            // Deep freeze reduction
            if (antistellarTemp < freezeK * 0.5) frozenPercent *= 0.6;
        } else if (liquidFraction > 0) {
            // Entirely liquid (warm enough everywhere)
            liquidPercent = baseLiquid * RandomUtils.rollRange(0.8, 1.2);
            frozenPercent = 0;
        } else {
            // All zones above boiling — volatile lost
            liquidPercent = 0;
            frozenPercent = 0;
        }

        hp.setLiquidSurfaceCoveragePercent(Math.max(0, Math.min(100, liquidPercent)));
        hp.setFrozenLiquidCoveragePercent(Math.max(0, Math.min(100, frozenPercent)));
    }

    private void distributeLiquidDominated(HydrologyProperties hp, Planet planet,
                                            LiquidInventory inventory, double temp,
                                            double freezeK, double boilK) {
        double baseLiquid = getBaseLiquidPercent(inventory);
        double baseFrozen = 0;

        // Near freezing point: some freezes at poles
        double freezeMargin = (boilK - freezeK) * 0.15;
        if (temp < freezeK + freezeMargin) {
            double freezeFraction = (freezeK + freezeMargin - temp) / freezeMargin;
            baseFrozen = baseLiquid * freezeFraction * 0.4;
            baseLiquid -= baseFrozen;
        }

        // Moderate temps: polar caps possible with axial tilt
        double midRange = freezeK + (boilK - freezeK) * 0.5;
        if (temp > freezeK + freezeMargin && temp < midRange) {
            Double axialTilt = planet.getAxialTilt();
            if (axialTilt != null && axialTilt > 10.0) {
                baseFrozen = RandomUtils.rollRange(1.0, 10.0) * (1.0 - axialTilt / 90.0);
            }
        }

        // Near boiling: reduced liquid coverage
        double boilMargin = (boilK - freezeK) * 0.2;
        if (temp > boilK - boilMargin) {
            baseLiquid *= 0.7;
            baseFrozen = 0;
        }

        double liquidPercent = baseLiquid * RandomUtils.rollRange(0.8, 1.2);
        double frozenPercent = baseFrozen * RandomUtils.rollRange(0.7, 1.3);

        hp.setLiquidSurfaceCoveragePercent(Math.max(0, Math.min(100, liquidPercent)));
        hp.setFrozenLiquidCoveragePercent(Math.max(0, Math.min(100, frozenPercent)));
    }

    private void distributeFrozenDominated(HydrologyProperties hp, LiquidInventory inventory,
                                            double temp, double pressure, double freezeK,
                                            VolatileType liquidType) {
        double baseFrozen = getBaseIcePercent(inventory);
        double liquidPercent = 0;

        // Very cold relative to freeze point: less surface coverage
        double deepFreezeThreshold = freezeK * 0.5;
        if (temp < deepFreezeThreshold) {
            baseFrozen *= 0.6;
        } else if (temp < freezeK * 0.75) {
            baseFrozen *= 0.8;
        }

        // Near freezing point with some pressure: small amount of liquid
        if (temp > freezeK * 0.93 && pressure >= getMinPressureForLiquid(liquidType)) {
            liquidPercent = RandomUtils.rollRange(0.1, 2.0);
            if (inventory == LiquidInventory.ABUNDANT || inventory == LiquidInventory.OCEAN_WORLD) {
                liquidPercent = RandomUtils.rollRange(1.0, 5.0);
            }
        }

        double frozenPercent = baseFrozen * RandomUtils.rollRange(0.8, 1.2);

        hp.setLiquidSurfaceCoveragePercent(Math.max(0, Math.min(100, liquidPercent)));
        hp.setFrozenLiquidCoveragePercent(Math.max(0, Math.min(100, frozenPercent)));
    }

    // ═══════════════════════════════════════════════════════════════
    //  Water Ice Coverage (separate from frozen liquid)
    // ═══════════════════════════════════════════════════════════════

    private void calculateWaterIceCoverage(HydrologyProperties hp, Planet planet,
                                            LiquidInventory inventory, VolatileType liquidType) {
        if (liquidType == VolatileType.WATER) {
            // For water worlds, water ice = frozen liquid
            hp.setWaterIceCoveragePercent(hp.getFrozenLiquidCoveragePercent());
            return;
        }

        // For non-water worlds: water ice depends on composition and temperature
        double temp = planet.getSurfaceTemp() != null ? planet.getSurfaceTemp() : 100.0;
        String composition = planet.getCompositionClassification() != null ? planet.getCompositionClassification() : "";

        if (temp < WATER_TRIPLE_POINT_TEMP_K) {
            // Cold enough for water ice — how much depends on composition
            double waterIcePercent = 0;
            if ("ICE_RICH".equals(composition) || "MIXED_SILICATE_ICE".equals(composition) || "OCEAN_WORLD".equals(composition)) {
                // Significant water ice as bedrock/surface layer
                waterIcePercent = RandomUtils.rollRange(20.0, 80.0);
                // On methane worlds, water ice is often the "ground" between methane lakes
                if (liquidType == VolatileType.METHANE || liquidType == VolatileType.METHANE_ETHANE) {
                    waterIcePercent = RandomUtils.rollRange(40.0, 90.0);
                }
            }
            hp.setWaterIceCoveragePercent(waterIcePercent);
        } else {
            hp.setWaterIceCoveragePercent(0.0);
        }
    }

    private void calculateWaterIceCoverageMoon(HydrologyProperties hp, Moon moon,
                                                LiquidInventory inventory, VolatileType liquidType) {
        if (liquidType == VolatileType.WATER) {
            hp.setWaterIceCoveragePercent(hp.getFrozenLiquidCoveragePercent());
            return;
        }

        double temp = moon.getSurfaceTemp() != null ? moon.getSurfaceTemp() : 100.0;
        String compType = moon.getCompositionType() != null ? moon.getCompositionType() : "";

        if (temp < WATER_TRIPLE_POINT_TEMP_K && ("ICY".equals(compType) || "MIXED".equals(compType))) {
            double waterIcePercent = RandomUtils.rollRange(30.0, 90.0);
            hp.setWaterIceCoveragePercent(waterIcePercent);
        } else {
            hp.setWaterIceCoveragePercent(0.0);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Subsurface Ocean (moved from MoonCreator)
    // ═══════════════════════════════════════════════════════════════

    private void determineSubsurfaceOcean(HydrologyProperties hp, Moon moon) {
        String compositionType = moon.getCompositionType();
        if (!"ICY".equals(compositionType) && !"MIXED".equals(compositionType)) {
            hp.setHasSubsurfaceOcean(false);
            return;
        }

        double tidalHeatingWattPerM2 = moon.getTidalHeatingWattPerM2() != null ?
                moon.getTidalHeatingWattPerM2() : 0.0;
        double surfaceTemp = moon.getSurfaceTemp() != null ? moon.getSurfaceTemp() : 50.0;
        double earthMass = moon.getEarthMass() != null ? moon.getEarthMass() : 0.0;

        double oceanChance = 0.0;

        if (tidalHeatingWattPerM2 > 2.0) oceanChance += 0.6;
        else if (tidalHeatingWattPerM2 > 0.5) oceanChance += 0.45;
        else if (tidalHeatingWattPerM2 > 0.1) oceanChance += 0.2;
        else if (tidalHeatingWattPerM2 > 0.01) oceanChance += 0.05;

        if (earthMass > 0.01) oceanChance += 0.15;
        else if (earthMass > 0.001) oceanChance += 0.08;

        if (surfaceTemp < 200 && surfaceTemp > 50) oceanChance += 0.1;

        if (Boolean.TRUE.equals(moon.getHasCryovolcanism())) oceanChance += 0.3;

        oceanChance = Math.min(0.85, oceanChance);

        boolean hasOcean = RandomUtils.rollRange(0.0, 1.0) < oceanChance;
        hp.setHasSubsurfaceOcean(hasOcean);

        if (hasOcean) {
            populateSubsurfaceOceanDetails(hp, moon, tidalHeatingWattPerM2, surfaceTemp);
        }
    }

    private void populateSubsurfaceOceanDetails(HydrologyProperties hp, Moon moon,
                                                  double tidalHeatingWattPerM2, double surfaceTemp) {
        double baseOceanDepth = 20.0;
        baseOceanDepth += moon.getEarthMass() * 500;
        baseOceanDepth += Math.min(tidalHeatingWattPerM2 * 10, 50);

        double oceanDepth = RandomUtils.rollRange(baseOceanDepth * 0.5, baseOceanDepth * 1.5);
        hp.setOceanDepthKm(oceanDepth);

        double baseShellThickness = 50.0;
        baseShellThickness -= Math.min(tidalHeatingWattPerM2 * 15, 40);

        if (surfaceTemp < 50) baseShellThickness += 30.0;
        else if (surfaceTemp < 80) baseShellThickness += 15.0;
        else if (surfaceTemp > 120) baseShellThickness -= 15.0;

        if (Boolean.TRUE.equals(moon.getHasCryovolcanism())) baseShellThickness *= 0.6;

        baseShellThickness = Math.max(3.0, baseShellThickness);

        double iceShellThickness = RandomUtils.rollRange(baseShellThickness * 0.7, baseShellThickness * 1.3);
        hp.setIceShellThicknessKm(iceShellThickness);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Planet Liquid Inventory
    // ═══════════════════════════════════════════════════════════════

    private LiquidInventory determineWaterInventory(Planet planet, Star parentStar) {
        String planetType = planet.getPlanetType() != null ? planet.getPlanetType().toLowerCase() : "";
        String composition = planet.getCompositionClassification() != null ? planet.getCompositionClassification() : "";
        String atmosphereClass = planet.getAtmosphereClassification() != null ? planet.getAtmosphereClassification() : "";

        if (planetType.contains("ocean")) return LiquidInventory.OCEAN_WORLD;

        if (planetType.contains("ice world")) {
            double mass = planet.getEarthMass() != null ? planet.getEarthMass() : 0.5;
            double pressure = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 0.0;
            String atmoClass = planet.getAtmosphereClassification() != null ? planet.getAtmosphereClassification() : "";

            int score = 0;
            if (mass > 3.0) score += 35;
            else if (mass > 1.5) score += 25;
            else if (mass > 0.8) score += 15;
            else if (mass > 0.3) score += 8;

            if (pressure > 1.0) score += 25;
            else if (pressure > 0.1) score += 15;
            else if (pressure > 0.01) score += 8;
            else if (pressure > 0.001) score += 3;

            if ("TITAN_LIKE".equals(atmoClass) || "AMMONIA".equals(atmoClass)) score += 10;
            else if ("NONE".equals(atmoClass)) score -= 10;

            Double magStrength = planet.getMagneticFieldStrength();
            if (magStrength != null && magStrength > 0.5) score += 5;

            if (score >= 55) return LiquidInventory.ABUNDANT;
            if (score >= 35) return LiquidInventory.MODERATE;
            if (score >= 15) return LiquidInventory.SCARCE;
            return LiquidInventory.TRACE;
        }

        if (planetType.contains("lava") || planetType.contains("hot rocky")) return LiquidInventory.NONE;
        if (planetType.contains("iron planet")) return LiquidInventory.NONE;

        if (planetType.contains("carbon")) {
            return RandomUtils.rollRange(0, 100) < 30 ? LiquidInventory.TRACE : LiquidInventory.NONE;
        }

        if (planetType.contains("dwarf")) {
            double mass = planet.getEarthMass() != null ? planet.getEarthMass() : 0.01;
            String geoActivity = planet.getGeologicalActivity() != null ? planet.getGeologicalActivity() : "";
            boolean isDead = geoActivity.toLowerCase().contains("dead");

            int score = 0;
            if (mass > 0.05) score += 15;
            else if (mass > 0.02) score += 8;
            else if (mass > 0.005) score += 3;

            if ("ICE_RICH".equals(composition)) score += 15;
            else if ("MIXED_SILICATE_ICE".equals(composition)) score += 10;

            if (isDead) score -= 10;
            else score += 5;

            Double magStrength = planet.getMagneticFieldStrength();
            if (magStrength != null && magStrength > 0.1) score += 5;

            if (score >= 30) return LiquidInventory.MODERATE;
            if (score >= 18) return LiquidInventory.SCARCE;
            if (score >= 8) return LiquidInventory.TRACE;
            return LiquidInventory.NONE;
        }

        if ("NONE".equals(atmosphereClass) || atmosphereClass.isEmpty()) {
            double mass = planet.getEarthMass() != null ? planet.getEarthMass() : 0;
            if (mass > 0.5 && ("MIXED_SILICATE_ICE".equals(composition) || "ICE_RICH".equals(composition))) {
                return LiquidInventory.TRACE;
            }
            return LiquidInventory.NONE;
        }

        if (planetType.contains("desert")) {
            return RandomUtils.rollRange(0, 100) < 70 ? LiquidInventory.TRACE : LiquidInventory.SCARCE;
        }

        if (planetType.contains("terrestrial") || planetType.contains("super-earth")) {
            return assessRockyPlanetLiquid(planet, parentStar, composition, atmosphereClass);
        }

        return LiquidInventory.TRACE;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Moon Liquid Inventory
    // ═══════════════════════════════════════════════════════════════

    private LiquidInventory determineMoonWaterInventory(Moon moon, HydrologyProperties hp) {
        String composition = moon.getCompositionType() != null ? moon.getCompositionType() : "";
        String compClass = moon.getCompositionClassification() != null ? moon.getCompositionClassification() : "";
        double mass = moon.getEarthMass() != null ? moon.getEarthMass() : 0;
        boolean hasOcean = Boolean.TRUE.equals(hp.getHasSubsurfaceOcean());

        if ("ICY".equals(composition) || "ICE_RICH".equals(compClass) || "MIXED_SILICATE_ICE".equals(compClass)) {
            if (hasOcean) {
                if (mass > 0.01) return LiquidInventory.OCEAN_WORLD;
                return LiquidInventory.ABUNDANT;
            }
            if (mass > 0.005) return LiquidInventory.ABUNDANT;
            if (mass > 0.001) return LiquidInventory.MODERATE;
            return LiquidInventory.SCARCE;
        }

        if ("MIXED".equals(composition)) {
            if (hasOcean) return LiquidInventory.MODERATE;
            if (mass > 0.005) return LiquidInventory.SCARCE;
            return LiquidInventory.TRACE;
        }

        if ("ROCKY".equals(composition)) {
            if ("HIGH".equals(moon.getGeologicalActivity())) return LiquidInventory.TRACE;
            if (mass < 0.001) return LiquidInventory.NONE;
            return RandomUtils.rollRange(0, 100) < 40 ? LiquidInventory.TRACE : LiquidInventory.NONE;
        }

        return LiquidInventory.NONE;
    }

    /**
     * After distribution and subsurface assessment, correct the inventory label if
     * the volatile can't actually exist anywhere. A hot moon with ICY composition
     * may have had its volatiles thermally driven off — labeling it ABUNDANT is misleading.
     */
    private LiquidInventory correctInventoryForActualState(HydrologyProperties hp, LiquidInventory inventory) {
        if (inventory == LiquidInventory.NONE || inventory == LiquidInventory.TRACE) {
            return inventory; // already low, no correction needed
        }

        double liquid = hp.getLiquidSurfaceCoveragePercent() != null ? hp.getLiquidSurfaceCoveragePercent() : 0.0;
        double frozen = hp.getFrozenLiquidCoveragePercent() != null ? hp.getFrozenLiquidCoveragePercent() : 0.0;
        double waterIce = hp.getWaterIceCoveragePercent() != null ? hp.getWaterIceCoveragePercent() : 0.0;
        boolean hasSubsurface = Boolean.TRUE.equals(hp.getHasSubsurfaceLiquid());
        boolean hasOcean = Boolean.TRUE.equals(hp.getHasSubsurfaceOcean());

        boolean noSurfacePresence = liquid <= 0 && frozen <= 0 && waterIce <= 0;

        if (noSurfacePresence && !hasSubsurface && !hasOcean) {
            // Volatile exists compositionally but is entirely absent — thermal escape
            return LiquidInventory.TRACE;
        }

        if (noSurfacePresence && (hasSubsurface || hasOcean)) {
            // No surface presence but subsurface reservoir exists — cap at MODERATE
            if (inventory.ordinal() > LiquidInventory.MODERATE.ordinal()) {
                return LiquidInventory.MODERATE;
            }
        }

        return inventory;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Rocky Planet Liquid Assessment
    // ═══════════════════════════════════════════════════════════════

    private LiquidInventory assessRockyPlanetLiquid(Planet planet, Star parentStar,
                                                     String composition, String atmosphereClass) {
        double score = 0;

        if ("MIXED_SILICATE_ICE".equals(composition)) score += 40;
        else if ("OCEAN_WORLD".equals(composition)) score += 60;
        else if ("SILICATE_RICH".equals(composition)) score += 15;
        else if ("IRON_RICH".equals(composition)) score += 5;

        double pressure = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 0;
        if (pressure >= 1.0) score += 20;
        else if (pressure >= 0.1) score += 10;
        else if (pressure >= 0.01) score += 3;

        if ("EARTH_LIKE".equals(atmosphereClass)) score += 25;
        else if ("DENSE_CO2".equals(atmosphereClass) || "THICK".equals(atmosphereClass)) score += 10;

        double mass = planet.getEarthMass() != null ? planet.getEarthMass() : 1.0;
        if (mass >= 2.0) score += 10;
        else if (mass >= 0.8) score += 5;
        else if (mass < 0.3) score -= 10;

        Double magStrength = planet.getMagneticFieldStrength();
        if (magStrength != null && magStrength > 0.5) score += 10;
        else if (magStrength != null && magStrength < 0.01) score -= 10;

        if (parentStar != null && parentStar.getMetallicity() != null) {
            double metallicity = parentStar.getMetallicity();
            if (metallicity > 0.1) score += 5;
            else if (metallicity < -0.3) score -= 5;
        }

        if (Boolean.TRUE.equals(planet.getHasPlateTectonics())) score += 5;

        if (score >= 70) return LiquidInventory.ABUNDANT;
        if (score >= 45) return LiquidInventory.MODERATE;
        if (score >= 25) return LiquidInventory.SCARCE;
        if (score >= 10) return LiquidInventory.TRACE;
        return LiquidInventory.NONE;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Moon Liquid Distribution
    // ═══════════════════════════════════════════════════════════════

    private void distributeMoonLiquid(HydrologyProperties hp, Moon moon, LiquidInventory inventory,
                                       VolatileType liquidType, double freezeK, double boilK) {
        double temp = moon.getSurfaceTemp() != null ? moon.getSurfaceTemp() : 100.0;
        double pressure = moon.getSurfacePressure() != null ? moon.getSurfacePressure() : 0.0;

        boolean liquidPossible = temp >= freezeK && temp <= boilK
                && pressure >= getMinPressureForLiquid(liquidType);

        if (liquidPossible) {
            double liquidPercent = getBaseLiquidPercent(inventory) * RandomUtils.rollRange(0.3, 0.7);
            double frozenPercent = getBaseIcePercent(inventory) * RandomUtils.rollRange(0.3, 0.6);

            hp.setLiquidSurfaceCoveragePercent(Math.max(0, Math.min(100, liquidPercent)));
            hp.setFrozenLiquidCoveragePercent(Math.max(0, Math.min(100, frozenPercent)));
            return;
        }

        // No liquid possible — check if frozen form can persist on surface
        boolean frozenPossible;
        if (liquidType == VolatileType.WATER) {
            frozenPossible = (pressure < WATER_TRIPLE_POINT_PRESSURE_ATM)
                    ? temp < ICE_SUBLIMATION_LIMIT_K
                    : temp < WATER_TRIPLE_POINT_TEMP_K;
        } else {
            frozenPossible = temp < freezeK;
        }

        if (frozenPossible) {
            double frozenPercent = getBaseIcePercent(inventory) * RandomUtils.rollRange(0.7, 1.3);
            if (temp < freezeK * 0.5) frozenPercent *= 0.5;
            hp.setLiquidSurfaceCoveragePercent(0.0);
            hp.setFrozenLiquidCoveragePercent(Math.max(0, Math.min(100, frozenPercent)));
            return;
        }

        hp.setLiquidSurfaceCoveragePercent(0.0);
        hp.setFrozenLiquidCoveragePercent(0.0);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Subsurface Liquid Assessment
    // ═══════════════════════════════════════════════════════════════

    private void assessSubsurfaceLiquid(HydrologyProperties hp, Planet planet, LiquidInventory inventory) {
        if (inventory == LiquidInventory.NONE || inventory == LiquidInventory.TRACE) {
            if (inventory == LiquidInventory.TRACE) {
                Double activityScore = planet.getActivityScore();
                if (activityScore != null && activityScore > 3.0) {
                    hp.setHasSubsurfaceLiquid(true);
                    hp.setSubsurfaceLiquidDepthKm(RandomUtils.rollRange(5.0, 50.0));
                    return;
                }
            }
            hp.setHasSubsurfaceLiquid(false);
            return;
        }

        Double activityScore = planet.getActivityScore();
        Double surfaceTemp = planet.getSurfaceTemp();
        double freezeK = hp.getFreezingPointK() != null ? hp.getFreezingPointK() : WATER_TRIPLE_POINT_TEMP_K;

        boolean hasGeothermal = activityScore != null && activityScore > 1.5;
        boolean surfaceFrozen = surfaceTemp != null && surfaceTemp < freezeK;

        if (hasGeothermal) {
            hp.setHasSubsurfaceLiquid(true);
            double baseDepth;
            if (surfaceFrozen) {
                double tempDeficit = freezeK - surfaceTemp;
                double gradient = 10.0 + (activityScore * 5.0);
                baseDepth = tempDeficit / gradient;
                baseDepth = Math.max(0.5, Math.min(200.0, baseDepth));
            } else {
                baseDepth = RandomUtils.rollRange(0.1, 5.0);
            }
            hp.setSubsurfaceLiquidDepthKm(baseDepth * RandomUtils.rollRange(0.8, 1.2));
        } else if (surfaceFrozen && (inventory == LiquidInventory.ABUNDANT || inventory == LiquidInventory.OCEAN_WORLD)) {
            hp.setHasSubsurfaceLiquid(true);
            hp.setSubsurfaceLiquidDepthKm(RandomUtils.rollRange(20.0, 100.0));
        } else if (inventory == LiquidInventory.ABUNDANT || inventory == LiquidInventory.OCEAN_WORLD) {
            hp.setHasSubsurfaceLiquid(true);
            hp.setSubsurfaceLiquidDepthKm(RandomUtils.rollRange(2.0, 30.0));
        } else {
            hp.setHasSubsurfaceLiquid(false);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Ocean Depth (for any planet with meaningful liquid coverage)
    // ═══════════════════════════════════════════════════════════════

    private void calculateSurfaceOceanDepth(HydrologyProperties hp, Planet planet, LiquidInventory inventory) {
        // Skip planets with negligible liquid — NONE, TRACE, and SCARCE don't form real oceans
        if (inventory == LiquidInventory.NONE || inventory == LiquidInventory.TRACE
                || inventory == LiquidInventory.SCARCE) {
            return;
        }

        // Use total liquid coverage (liquid + frozen) — a frozen ocean is still an ocean
        double liquidSurface = hp.getLiquidSurfaceCoveragePercent() != null ? hp.getLiquidSurfaceCoveragePercent() : 0.0;
        double frozenSurface = hp.getFrozenLiquidCoveragePercent() != null ? hp.getFrozenLiquidCoveragePercent() : 0.0;
        double totalCoverage = liquidSurface + frozenSurface;
        if (totalCoverage < 5.0) {
            return;
        }

        double gravity = planet.getSurfaceGravity() != null ? planet.getSurfaceGravity() : 1.0;
        double earthMass = planet.getEarthMass() != null ? planet.getEarthMass() : 1.0;
        double coverage = totalCoverage / 100.0;

        // Base depth scales with mass and coverage; Earth (1M⊕, 1g, 71%) ≈ 5.6 km base
        double baseDepth = 2.0 + (earthMass * 1.5) + (coverage * 3.0);
        baseDepth *= Math.sqrt(gravity);

        boolean surfaceFrozen = frozenSurface > liquidSurface;

        if (surfaceFrozen) {
            // Frozen oceans can be very deep (water locked as ice mantles)
            baseDepth = Math.max(5.0, Math.min(150.0, baseDepth * 2.0));
        } else if (inventory == LiquidInventory.OCEAN_WORLD) {
            // Ocean worlds: deep oceans
            baseDepth = Math.max(1.5, Math.min(30.0, baseDepth));
        } else {
            // MODERATE/ABUNDANT terrestrial planets: shallower oceans
            // Earth's mean ocean depth is ~3.7 km — scale down for lower coverage
            baseDepth = Math.max(0.5, Math.min(12.0, baseDepth * 0.6));
        }

        double oceanDepth = RandomUtils.rollRange(baseDepth * 0.7, baseDepth * 1.3);
        hp.setOceanDepthKm(Math.round(oceanDepth * 100.0) / 100.0);

        // High-pressure ice layer (Ice VI/VII) at ocean floor for deep oceans
        calculateHighPressureIceLayer(hp, gravity);
    }

    /**
     * Calculates the high-pressure ice layer thickness for deep oceans.
     * At ~0.6 GPa (~15 km depth at 1g), water transitions to Ice VI/VII.
     * This is a deterministic function of ocean depth and gravity — also
     * recalculated on load by {@link com.brickroad.starcreator_webservice.service.DerivedFieldCalculator}.
     */
    public static void calculateHighPressureIceLayer(HydrologyProperties hp, double surfaceGravity) {
        Double oceanDepth = hp.getOceanDepthKm();
        if (oceanDepth == null) {
            return;
        }
        double threshold = 15.0 / Math.max(0.1, surfaceGravity);
        if (oceanDepth > threshold) {
            hp.setHighPressureIceLayerKm(Math.round((oceanDepth - threshold) * 100.0) / 100.0);
        }
    }

    private void assessMoonSubsurfaceLiquid(HydrologyProperties hp, Moon moon, LiquidInventory inventory) {
        if (Boolean.TRUE.equals(hp.getHasSubsurfaceOcean())) {
            hp.setHasSubsurfaceLiquid(true);
            if (hp.getSubsurfaceLiquidDepthKm() == null) {
                if (hp.getIceShellThicknessKm() != null) {
                    hp.setSubsurfaceLiquidDepthKm(hp.getIceShellThicknessKm());
                } else {
                    hp.setSubsurfaceLiquidDepthKm(RandomUtils.rollRange(5.0, 100.0));
                }
            }
            return;
        }

        if (inventory.ordinal() >= LiquidInventory.MODERATE.ordinal()) {
            String tidalLevel = moon.getTidalHeatingLevel() != null ? moon.getTidalHeatingLevel() : "NONE";
            double chance = 20;

            switch (tidalLevel) {
                case "HIGH", "EXTREME" -> chance += 40;
                case "MODERATE" -> chance += 20;
                case "LOW" -> chance += 10;
            }

            if (RandomUtils.rollRange(0, 100) < chance) {
                hp.setHasSubsurfaceLiquid(true);
                hp.setSubsurfaceLiquidDepthKm(RandomUtils.rollRange(10.0, 200.0));
            } else {
                hp.setHasSubsurfaceLiquid(false);
            }
        } else {
            hp.setHasSubsurfaceLiquid(false);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Liquid Colors
    // ═══════════════════════════════════════════════════════════════

    private void deriveLiquidColors(HydrologyProperties hp, VolatileType liquidType) {
        if (liquidType == VolatileType.NONE) return;

        hp.setLiquidColorPrimary(liquidType.getDefaultColorPrimary());
        hp.setLiquidColorSecondary(liquidType.getDefaultColorSecondary());
    }

    // ═══════════════════════════════════════════════════════════════
    //  Composition String
    // ═══════════════════════════════════════════════════════════════

    private String buildCompositionString(VolatileType liquidType, double nh3Fraction) {
        return switch (liquidType) {
            case WATER -> "H2O";
            case AMMONIA_WATER -> {
                int nh3Pct = (int) Math.round(nh3Fraction * 100);
                nh3Pct = Math.max(1, Math.min(33, nh3Pct));
                yield (100 - nh3Pct) + "% H2O, " + nh3Pct + "% NH3";
            }
            case AMMONIA -> "NH3";
            case METHANE -> "CH4";
            case METHANE_ETHANE -> "CH4/C2H6";
            case NONE -> null;
        };
    }

    // ═══════════════════════════════════════════════════════════════
    //  Utility Methods
    // ═══════════════════════════════════════════════════════════════

    private double getGasPercentage(Atmosphere atmosphere, String formula) {
        if (atmosphere == null || atmosphere.getComponents() == null) return 0.0;
        return atmosphere.getComponents().stream()
                .filter(c -> formula.equals(c.getGasFormula()))
                .mapToDouble(AtmosphereComponent::getPercentage)
                .sum();
    }

    private double getMinPressureForLiquid(VolatileType liquidType) {
        return switch (liquidType) {
            case WATER -> WATER_TRIPLE_POINT_PRESSURE_ATM;
            case AMMONIA, AMMONIA_WATER -> 0.001; // Ammonia has lower triple point pressure
            case METHANE, METHANE_ETHANE -> 0.001; // Methane/ethane require minimal pressure
            case NONE -> Double.MAX_VALUE;
        };
    }

    private boolean isGaseousBody(String planetType) {
        return planetType.contains("gas giant") || planetType.contains("ice giant")
                || planetType.contains("hot jupiter") || planetType.contains("super-jupiter")
                || planetType.contains("mini-neptune") || planetType.contains("sub-neptune")
                || planetType.contains("warm neptune") || planetType.contains("hot neptune")
                || planetType.contains("puffy");
    }

    private double getBaseLiquidPercent(LiquidInventory inventory) {
        return switch (inventory) {
            case TRACE -> RandomUtils.rollRange(0.5, 3.0);
            case SCARCE -> RandomUtils.rollRange(3.0, 15.0);
            case MODERATE -> RandomUtils.rollRange(15.0, 50.0);
            case ABUNDANT -> RandomUtils.rollRange(40.0, 80.0);
            case OCEAN_WORLD -> RandomUtils.rollRange(70.0, 98.0);
            default -> 0.0;
        };
    }

    private double getBaseIcePercent(LiquidInventory inventory) {
        return switch (inventory) {
            case TRACE -> RandomUtils.rollRange(1.0, 5.0);
            case SCARCE -> RandomUtils.rollRange(5.0, 20.0);
            case MODERATE -> RandomUtils.rollRange(15.0, 45.0);
            case ABUNDANT -> RandomUtils.rollRange(30.0, 70.0);
            case OCEAN_WORLD -> RandomUtils.rollRange(60.0, 95.0);
            default -> 0.0;
        };
    }
}
