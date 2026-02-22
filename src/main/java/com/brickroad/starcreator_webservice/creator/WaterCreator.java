package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ud.Moon;
import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.Star;
import com.brickroad.starcreator_webservice.entity.ud.WaterProperties;
import com.brickroad.starcreator_webservice.enums.WaterInventory;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import org.springframework.stereotype.Service;

@Service
public class WaterCreator {

    private static final double WATER_TRIPLE_POINT_TEMP_K = 273.16;
    private static final double WATER_TRIPLE_POINT_PRESSURE_ATM = 0.00604;
    private static final double WATER_BOILING_100C_K = 373.15;

    /**
     * Creates water properties for a planet. Returns null for gaseous bodies
     * (gas giants, ice giants, etc.) so they have no "water" key in JSON.
     */
    public WaterProperties createPlanetWaterProperties(Planet planet, Star parentStar) {
        String planetType = planet.getPlanetType() != null ? planet.getPlanetType().toLowerCase() : "";

        if (isGaseousBody(planetType)) {
            return null;
        }

        WaterProperties wp = new WaterProperties();
        wp.setLabel("Planet water");

        WaterInventory inventory = determineWaterInventory(planet, parentStar);
        wp.setWaterInventory(inventory.name());

        if (inventory == WaterInventory.NONE) {
            wp.setWaterCoveragePercent(0.0);
            wp.setLiquidWaterCoveragePercent(0.0);
            wp.setIceCoveragePercent(0.0);
            wp.setHasSubsurfaceWater(false);
            return wp;
        }

        distributeWater(wp, planet, inventory);
        assessSubsurfaceWater(wp, planet, inventory);

        double liquid = wp.getLiquidWaterCoveragePercent() != null ? wp.getLiquidWaterCoveragePercent() : 0.0;
        double ice = wp.getIceCoveragePercent() != null ? wp.getIceCoveragePercent() : 0.0;
        wp.setWaterCoveragePercent(Math.min(100.0, liquid + ice));

        return wp;
    }

    /**
     * Creates water properties for a moon. Handles subsurface ocean determination,
     * surface water distribution, and tiny moonlet defaults — all self-contained.
     */
    public WaterProperties createMoonWaterProperties(Moon moon) {
        WaterProperties wp = new WaterProperties();
        wp.setLabel("Moon water");

        // Step 1: Determine subsurface ocean (moved from MoonCreator)
        determineSubsurfaceOcean(wp, moon);

        double moonMass = moon.getEarthMass() != null ? moon.getEarthMass() : 0;

        // Step 2: Tiny moonlets get simplified defaults
        if (moonMass < 0.0005) {
            if (Boolean.TRUE.equals(wp.getHasSubsurfaceOcean())) {
                wp.setWaterInventory("MODERATE");
                wp.setLiquidWaterCoveragePercent(0.0);
                wp.setIceCoveragePercent(RandomUtils.rollRange(40.0, 90.0));
                wp.setWaterCoveragePercent(wp.getIceCoveragePercent());
                wp.setHasSubsurfaceWater(true);
                if (wp.getSubsurfaceWaterDepthKm() == null && wp.getIceShellThicknessKm() != null) {
                    wp.setSubsurfaceWaterDepthKm(wp.getIceShellThicknessKm());
                }
            } else {
                wp.setWaterInventory("NONE");
                wp.setLiquidWaterCoveragePercent(0.0);
                wp.setIceCoveragePercent(0.0);
                wp.setWaterCoveragePercent(0.0);
                wp.setHasSubsurfaceWater(false);
            }
            return wp;
        }

        // Step 3: Full water generation for larger moons
        WaterInventory inventory = determineMoonWaterInventory(moon, wp);
        wp.setWaterInventory(inventory.name());

        if (inventory == WaterInventory.NONE) {
            wp.setWaterCoveragePercent(0.0);
            wp.setLiquidWaterCoveragePercent(0.0);
            wp.setIceCoveragePercent(0.0);
            wp.setHasSubsurfaceWater(false);
            return wp;
        }

        distributeMoonWater(wp, moon, inventory);
        assessMoonSubsurfaceWater(wp, moon, inventory);

        double liquid = wp.getLiquidWaterCoveragePercent() != null ? wp.getLiquidWaterCoveragePercent() : 0.0;
        double ice = wp.getIceCoveragePercent() != null ? wp.getIceCoveragePercent() : 0.0;
        wp.setWaterCoveragePercent(Math.min(100.0, liquid + ice));

        return wp;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Subsurface Ocean (moved from MoonCreator)
    // ═══════════════════════════════════════════════════════════════

    private void determineSubsurfaceOcean(WaterProperties wp, Moon moon) {
        String compositionType = moon.getCompositionType();
        if (!"ICY".equals(compositionType) && !"MIXED".equals(compositionType)) {
            wp.setHasSubsurfaceOcean(false);
            return;
        }

        double tidalHeatingWattPerM2 = moon.getTidalHeatingWattPerM2() != null ?
                moon.getTidalHeatingWattPerM2() : 0.0;
        double surfaceTemp = moon.getSurfaceTemp() != null ?
                moon.getSurfaceTemp() : 50.0;
        double earthMass = moon.getEarthMass() != null ?
                moon.getEarthMass() : 0.0;

        double oceanChance = 0.0;

        if (tidalHeatingWattPerM2 > 2.0) {
            oceanChance += 0.6;
        } else if (tidalHeatingWattPerM2 > 0.5) {
            oceanChance += 0.45;
        } else if (tidalHeatingWattPerM2 > 0.1) {
            oceanChance += 0.2;
        } else if (tidalHeatingWattPerM2 > 0.01) {
            oceanChance += 0.05;
        }

        if (earthMass > 0.01) {
            oceanChance += 0.15;
        } else if (earthMass > 0.001) {
            oceanChance += 0.08;
        }

        if (surfaceTemp < 200 && surfaceTemp > 50) {
            oceanChance += 0.1;
        }

        if (Boolean.TRUE.equals(moon.getHasCryovolcanism())) {
            oceanChance += 0.3;
        }

        oceanChance = Math.min(0.85, oceanChance);

        boolean hasOcean = RandomUtils.rollRange(0.0, 1.0) < oceanChance;
        wp.setHasSubsurfaceOcean(hasOcean);

        if (hasOcean) {
            populateSubsurfaceOceanDetails(wp, moon, tidalHeatingWattPerM2, surfaceTemp);
        }
    }

    private void populateSubsurfaceOceanDetails(WaterProperties wp, Moon moon,
                                                  double tidalHeatingWattPerM2, double surfaceTemp) {
        double baseOceanDepth = 20.0;
        baseOceanDepth += moon.getEarthMass() * 500;
        baseOceanDepth += Math.min(tidalHeatingWattPerM2 * 10, 50);

        double oceanDepth = RandomUtils.rollRange(baseOceanDepth * 0.5, baseOceanDepth * 1.5);
        wp.setOceanDepthKm(oceanDepth);

        double baseShellThickness = 50.0;
        baseShellThickness -= Math.min(tidalHeatingWattPerM2 * 15, 40);

        if (surfaceTemp < 50) {
            baseShellThickness += 30.0;
        } else if (surfaceTemp < 80) {
            baseShellThickness += 15.0;
        } else if (surfaceTemp > 120) {
            baseShellThickness -= 15.0;
        }

        if (Boolean.TRUE.equals(moon.getHasCryovolcanism())) {
            baseShellThickness *= 0.6;
        }

        baseShellThickness = Math.max(3.0, baseShellThickness);

        double iceShellThickness = RandomUtils.rollRange(
                baseShellThickness * 0.7,
                baseShellThickness * 1.3
        );
        wp.setIceShellThicknessKm(iceShellThickness);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Planet Water Inventory
    // ═══════════════════════════════════════════════════════════════

    private WaterInventory determineWaterInventory(Planet planet, Star parentStar) {
        String planetType = planet.getPlanetType() != null ? planet.getPlanetType().toLowerCase() : "";
        String composition = planet.getCompositionClassification() != null ? planet.getCompositionClassification() : "";
        String atmosphereClass = planet.getAtmosphereClassification() != null ? planet.getAtmosphereClassification() : "";

        if (planetType.contains("ocean")) {
            return WaterInventory.OCEAN_WORLD;
        }

        if (planetType.contains("ice world")) {
            double mass = planet.getEarthMass() != null ? planet.getEarthMass() : 0.5;
            double pressure = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 0.0;
            String atmoClass = planet.getAtmosphereClassification() != null
                    ? planet.getAtmosphereClassification() : "";

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

            if (score >= 55) return WaterInventory.ABUNDANT;
            if (score >= 35) return WaterInventory.MODERATE;
            if (score >= 15) return WaterInventory.SCARCE;
            return WaterInventory.TRACE;
        }

        if (planetType.contains("lava") || planetType.contains("hot rocky")) {
            return WaterInventory.NONE;
        }

        if (planetType.contains("iron planet")) {
            return WaterInventory.NONE;
        }

        if (planetType.contains("carbon")) {
            return RandomUtils.rollRange(0, 100) < 30 ? WaterInventory.TRACE : WaterInventory.NONE;
        }

        if (planetType.contains("dwarf")) {
            double mass = planet.getEarthMass() != null ? planet.getEarthMass() : 0.01;
            String geoActivity = planet.getGeologicalActivity() != null
                    ? planet.getGeologicalActivity() : "";
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

            if (score >= 30) return WaterInventory.MODERATE;
            if (score >= 18) return WaterInventory.SCARCE;
            if (score >= 8) return WaterInventory.TRACE;
            return WaterInventory.NONE;
        }

        if ("NONE".equals(atmosphereClass) || atmosphereClass.isEmpty()) {
            double mass = planet.getEarthMass() != null ? planet.getEarthMass() : 0;
            if (mass > 0.5 && ("MIXED_SILICATE_ICE".equals(composition) || "ICE_RICH".equals(composition))) {
                return WaterInventory.TRACE;
            }
            return WaterInventory.NONE;
        }

        if (planetType.contains("desert")) {
            return RandomUtils.rollRange(0, 100) < 70 ? WaterInventory.TRACE : WaterInventory.SCARCE;
        }

        if (planetType.contains("terrestrial") || planetType.contains("super-earth")) {
            return assessRockyPlanetWater(planet, parentStar, composition, atmosphereClass);
        }

        return WaterInventory.TRACE;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Moon Water Inventory
    // ═══════════════════════════════════════════════════════════════

    private WaterInventory determineMoonWaterInventory(Moon moon, WaterProperties wp) {
        String composition = moon.getCompositionType() != null ? moon.getCompositionType() : "";
        String compClass = moon.getCompositionClassification() != null ? moon.getCompositionClassification() : "";
        double mass = moon.getEarthMass() != null ? moon.getEarthMass() : 0;
        boolean hasOcean = Boolean.TRUE.equals(wp.getHasSubsurfaceOcean());

        if ("ICY".equals(composition) || "ICE_RICH".equals(compClass) || "MIXED_SILICATE_ICE".equals(compClass)) {
            if (hasOcean) {
                if (mass > 0.01) return WaterInventory.OCEAN_WORLD;
                return WaterInventory.ABUNDANT;
            }
            if (mass > 0.005) return WaterInventory.ABUNDANT;
            if (mass > 0.001) return WaterInventory.MODERATE;
            return WaterInventory.SCARCE;
        }

        if ("MIXED".equals(composition)) {
            if (hasOcean) return WaterInventory.MODERATE;
            if (mass > 0.005) return WaterInventory.SCARCE;
            return WaterInventory.TRACE;
        }

        if ("ROCKY".equals(composition)) {
            if ("HIGH".equals(moon.getGeologicalActivity())) {
                return WaterInventory.TRACE;
            }
            if (mass < 0.001) return WaterInventory.NONE;
            return RandomUtils.rollRange(0, 100) < 40 ? WaterInventory.TRACE : WaterInventory.NONE;
        }

        return WaterInventory.NONE;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Planet Water Distribution
    // ═══════════════════════════════════════════════════════════════

    private WaterInventory assessRockyPlanetWater(Planet planet, Star parentStar,
                                                   String composition, String atmosphereClass) {
        double score = 0;

        if ("MIXED_SILICATE_ICE".equals(composition)) {
            score += 40;
        } else if ("OCEAN_WORLD".equals(composition)) {
            score += 60;
        } else if ("SILICATE_RICH".equals(composition)) {
            score += 15;
        } else if ("IRON_RICH".equals(composition)) {
            score += 5;
        }

        double pressure = planet.getSurfacePressure() != null ? planet.getSurfacePressure() : 0;
        if (pressure >= 1.0) score += 20;
        else if (pressure >= 0.1) score += 10;
        else if (pressure >= 0.01) score += 3;

        if ("EARTH_LIKE".equals(atmosphereClass)) {
            score += 25;
        } else if ("DENSE_CO2".equals(atmosphereClass) || "THICK".equals(atmosphereClass)) {
            score += 10;
        }

        double mass = planet.getEarthMass() != null ? planet.getEarthMass() : 1.0;
        if (mass >= 2.0) score += 10;
        else if (mass >= 0.8) score += 5;
        else if (mass < 0.3) score -= 10;

        Double magStrength = planet.getMagneticFieldStrength();
        if (magStrength != null && magStrength > 0.5) {
            score += 10;
        } else if (magStrength != null && magStrength < 0.01) {
            score -= 10;
        }

        if (parentStar != null && parentStar.getMetallicity() != null) {
            double metallicity = parentStar.getMetallicity();
            if (metallicity > 0.1) score += 5;
            else if (metallicity < -0.3) score -= 5;
        }

        if (Boolean.TRUE.equals(planet.getHasPlateTectonics())) {
            score += 5;
        }

        if (score >= 70) return WaterInventory.ABUNDANT;
        if (score >= 45) return WaterInventory.MODERATE;
        if (score >= 25) return WaterInventory.SCARCE;
        if (score >= 10) return WaterInventory.TRACE;
        return WaterInventory.NONE;
    }

    private void distributeWater(WaterProperties wp, Planet planet, WaterInventory inventory) {
        Double surfaceTemp = planet.getSurfaceTemp();
        Double surfacePressure = planet.getSurfacePressure();

        if (surfaceTemp == null) {
            double icePercent = getBaseIcePercent(inventory) * 0.5;
            wp.setLiquidWaterCoveragePercent(0.0);
            wp.setIceCoveragePercent(icePercent);
            return;
        }

        double temp = surfaceTemp;
        double pressure = surfacePressure != null ? surfacePressure : 0.0;

        boolean liquidPossible = temp >= WATER_TRIPLE_POINT_TEMP_K
                && temp <= WATER_BOILING_100C_K
                && pressure >= WATER_TRIPLE_POINT_PRESSURE_ATM;

        if (pressure > 1.0 && temp > WATER_BOILING_100C_K) {
            double maxLiquidTemp = WATER_BOILING_100C_K + (pressure - 1.0) * 20.0;
            maxLiquidTemp = Math.min(maxLiquidTemp, 647.0);
            liquidPossible = temp <= maxLiquidTemp;
        }

        if (liquidPossible) {
            distributeLiquidDominated(wp, planet, inventory, temp);
        } else if (temp < WATER_TRIPLE_POINT_TEMP_K) {
            distributeIceDominated(wp, inventory, temp, pressure);
        } else {
            wp.setLiquidWaterCoveragePercent(0.0);
            wp.setIceCoveragePercent(0.0);
        }
    }

    private void distributeLiquidDominated(WaterProperties wp, Planet planet,
                                            WaterInventory inventory, double temp) {
        double baseLiquid = getBaseLiquidPercent(inventory);
        double baseIce = 0;

        if (temp < 283.0) {
            double freezeFraction = (283.0 - temp) / 10.0;
            baseIce = baseLiquid * freezeFraction * 0.4;
            baseLiquid -= baseIce;
        }

        if (temp > 283.0 && temp < 323.0) {
            Double axialTilt = planet.getAxialTilt();
            if (axialTilt != null && axialTilt > 10.0) {
                baseIce = RandomUtils.rollRange(1.0, 10.0) * (1.0 - axialTilt / 90.0);
            }
        }

        if (temp > 323.0) {
            baseLiquid *= 0.7;
            baseIce = 0;
        }

        double liquidPercent = baseLiquid * RandomUtils.rollRange(0.8, 1.2);
        double icePercent = baseIce * RandomUtils.rollRange(0.7, 1.3);

        wp.setLiquidWaterCoveragePercent(Math.max(0, Math.min(100, liquidPercent)));
        wp.setIceCoveragePercent(Math.max(0, Math.min(100, icePercent)));
    }

    private void distributeIceDominated(WaterProperties wp, WaterInventory inventory,
                                         double temp, double pressure) {
        double baseIce = getBaseIcePercent(inventory);
        double liquidPercent = 0;

        if (temp < 100) {
            baseIce *= 0.6;
        } else if (temp < 200) {
            baseIce *= 0.8;
        }

        if (temp > 253.0 && pressure >= 0.001) {
            liquidPercent = RandomUtils.rollRange(0.1, 2.0);
            if (inventory == WaterInventory.ABUNDANT || inventory == WaterInventory.OCEAN_WORLD) {
                liquidPercent = RandomUtils.rollRange(1.0, 5.0);
            }
        }

        double icePercent = baseIce * RandomUtils.rollRange(0.8, 1.2);

        wp.setLiquidWaterCoveragePercent(Math.max(0, Math.min(100, liquidPercent)));
        wp.setIceCoveragePercent(Math.max(0, Math.min(100, icePercent)));
    }

    // ═══════════════════════════════════════════════════════════════
    //  Subsurface Water Assessment
    // ═══════════════════════════════════════════════════════════════

    private void assessSubsurfaceWater(WaterProperties wp, Planet planet, WaterInventory inventory) {
        if (inventory == WaterInventory.NONE || inventory == WaterInventory.TRACE) {
            if (inventory == WaterInventory.TRACE) {
                Double activityScore = planet.getActivityScore();
                if (activityScore != null && activityScore > 3.0) {
                    wp.setHasSubsurfaceWater(true);
                    wp.setSubsurfaceWaterDepthKm(RandomUtils.rollRange(5.0, 50.0));
                    return;
                }
            }
            wp.setHasSubsurfaceWater(false);
            return;
        }

        Double activityScore = planet.getActivityScore();
        Double surfaceTemp = planet.getSurfaceTemp();

        boolean hasGeothermal = activityScore != null && activityScore > 1.5;
        boolean surfaceFrozen = surfaceTemp != null && surfaceTemp < WATER_TRIPLE_POINT_TEMP_K;

        if (hasGeothermal) {
            wp.setHasSubsurfaceWater(true);

            double baseDepth;
            if (surfaceFrozen) {
                double tempDeficit = WATER_TRIPLE_POINT_TEMP_K - surfaceTemp;
                double gradient = 10.0 + (activityScore * 5.0);
                baseDepth = tempDeficit / gradient;
                baseDepth = Math.max(0.5, Math.min(200.0, baseDepth));
            } else {
                baseDepth = RandomUtils.rollRange(0.1, 5.0);
            }
            wp.setSubsurfaceWaterDepthKm(baseDepth * RandomUtils.rollRange(0.8, 1.2));

        } else if (surfaceFrozen && (inventory == WaterInventory.ABUNDANT || inventory == WaterInventory.OCEAN_WORLD)) {
            wp.setHasSubsurfaceWater(true);
            wp.setSubsurfaceWaterDepthKm(RandomUtils.rollRange(20.0, 100.0));
        } else if (inventory == WaterInventory.ABUNDANT || inventory == WaterInventory.OCEAN_WORLD) {
            wp.setHasSubsurfaceWater(true);
            wp.setSubsurfaceWaterDepthKm(RandomUtils.rollRange(2.0, 30.0));
        } else {
            wp.setHasSubsurfaceWater(false);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Moon Water Distribution
    // ═══════════════════════════════════════════════════════════════

    private void distributeMoonWater(WaterProperties wp, Moon moon, WaterInventory inventory) {
        double temp = moon.getSurfaceTemp() != null ? moon.getSurfaceTemp() : 100.0;
        double pressure = moon.getSurfacePressure() != null ? moon.getSurfacePressure() : 0.0;

        if (temp < WATER_TRIPLE_POINT_TEMP_K || pressure < WATER_TRIPLE_POINT_PRESSURE_ATM) {
            double icePercent = getBaseIcePercent(inventory) * RandomUtils.rollRange(0.7, 1.3);

            if (temp < 80) {
                icePercent *= 0.5;
            }

            wp.setLiquidWaterCoveragePercent(0.0);
            wp.setIceCoveragePercent(Math.max(0, Math.min(100, icePercent)));
            return;
        }

        if (temp >= WATER_TRIPLE_POINT_TEMP_K && temp <= WATER_BOILING_100C_K
                && pressure >= WATER_TRIPLE_POINT_PRESSURE_ATM) {
            double liquidPercent = getBaseLiquidPercent(inventory) * RandomUtils.rollRange(0.3, 0.7);
            double icePercent = getBaseIcePercent(inventory) * RandomUtils.rollRange(0.3, 0.6);

            wp.setLiquidWaterCoveragePercent(Math.max(0, Math.min(100, liquidPercent)));
            wp.setIceCoveragePercent(Math.max(0, Math.min(100, icePercent)));
            return;
        }

        if (temp > WATER_BOILING_100C_K) {
            wp.setLiquidWaterCoveragePercent(0.0);
            wp.setIceCoveragePercent(0.0);
            return;
        }

        wp.setLiquidWaterCoveragePercent(0.0);
        wp.setIceCoveragePercent(getBaseIcePercent(inventory) * RandomUtils.rollRange(0.5, 1.0));
    }

    private void assessMoonSubsurfaceWater(WaterProperties wp, Moon moon, WaterInventory inventory) {
        if (Boolean.TRUE.equals(wp.getHasSubsurfaceOcean())) {
            wp.setHasSubsurfaceWater(true);
            if (wp.getSubsurfaceWaterDepthKm() == null) {
                if (wp.getIceShellThicknessKm() != null) {
                    wp.setSubsurfaceWaterDepthKm(wp.getIceShellThicknessKm());
                } else {
                    wp.setSubsurfaceWaterDepthKm(RandomUtils.rollRange(5.0, 100.0));
                }
            }
            return;
        }

        if (inventory.ordinal() >= WaterInventory.MODERATE.ordinal()) {
            String tidalLevel = moon.getTidalHeatingLevel() != null ? moon.getTidalHeatingLevel() : "NONE";
            double chance = 20;

            switch (tidalLevel) {
                case "HIGH", "EXTREME" -> chance += 40;
                case "MODERATE" -> chance += 20;
                case "LOW" -> chance += 10;
            }

            if (RandomUtils.rollRange(0, 100) < chance) {
                wp.setHasSubsurfaceWater(true);
                wp.setSubsurfaceWaterDepthKm(RandomUtils.rollRange(10.0, 200.0));
            } else {
                wp.setHasSubsurfaceWater(false);
            }
        } else {
            wp.setHasSubsurfaceWater(false);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Utility Methods
    // ═══════════════════════════════════════════════════════════════

    private boolean isGaseousBody(String planetType) {
        return planetType.contains("gas giant") || planetType.contains("ice giant")
                || planetType.contains("hot jupiter") || planetType.contains("super-jupiter")
                || planetType.contains("mini-neptune") || planetType.contains("sub-neptune")
                || planetType.contains("warm neptune") || planetType.contains("hot neptune")
                || planetType.contains("puffy");
    }

    private double getBaseLiquidPercent(WaterInventory inventory) {
        return switch (inventory) {
            case TRACE -> RandomUtils.rollRange(0.5, 3.0);
            case SCARCE -> RandomUtils.rollRange(3.0, 15.0);
            case MODERATE -> RandomUtils.rollRange(15.0, 50.0);
            case ABUNDANT -> RandomUtils.rollRange(40.0, 80.0);
            case OCEAN_WORLD -> RandomUtils.rollRange(70.0, 98.0);
            default -> 0.0;
        };
    }

    private double getBaseIcePercent(WaterInventory inventory) {
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
