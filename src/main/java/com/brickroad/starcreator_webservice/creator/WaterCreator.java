package com.brickroad.starcreator_webservice.creator;

import com.brickroad.starcreator_webservice.entity.ud.Moon;
import com.brickroad.starcreator_webservice.entity.ud.Planet;
import com.brickroad.starcreator_webservice.entity.ud.Star;
import com.brickroad.starcreator_webservice.enums.WaterInventory;
import com.brickroad.starcreator_webservice.utils.RandomUtils;
import org.springframework.stereotype.Service;

@Service
public class WaterCreator {

    private static final double WATER_TRIPLE_POINT_TEMP_K = 273.16;
    private static final double WATER_TRIPLE_POINT_PRESSURE_ATM = 0.00604;
    private static final double WATER_BOILING_100C_K = 373.15;

    public void populateWaterProperties(Planet planet, Star parentStar) {
        String planetType = planet.getPlanetType() != null ? planet.getPlanetType().toLowerCase() : "";

        if (isGaseousBody(planetType)) {
            planet.setWaterInventory(WaterInventory.NONE.name());
            planet.setWaterCoveragePercent(null);
            planet.setLiquidWaterCoveragePercent(null);
            planet.setIceCoveragePercent(null);
            planet.setHasSubsurfaceWater(false);
            planet.setSubsurfaceWaterDepthKm(null);
            return;
        }

        WaterInventory inventory = determineWaterInventory(planet, parentStar);
        planet.setWaterInventory(inventory.name());

        if (inventory == WaterInventory.NONE) {
            planet.setWaterCoveragePercent(0.0);
            planet.setLiquidWaterCoveragePercent(0.0);
            planet.setIceCoveragePercent(0.0);
            planet.setHasSubsurfaceWater(false);
            planet.setSubsurfaceWaterDepthKm(null);
            return;
        }

        distributeWater(planet, inventory);

        assessSubsurfaceWater(planet, inventory);

        double liquid = planet.getLiquidWaterCoveragePercent() != null ? planet.getLiquidWaterCoveragePercent() : 0.0;
        double ice = planet.getIceCoveragePercent() != null ? planet.getIceCoveragePercent() : 0.0;
        planet.setWaterCoveragePercent(Math.min(100.0, liquid + ice));
    }

    public void populateMoonWaterProperties(Moon moon) {
        String compositionType = moon.getCompositionType() != null ? moon.getCompositionType() : "";

        // Gas-composition moons don't exist in our model, but safety check
        WaterInventory inventory = determineMoonWaterInventory(moon);
        moon.setWaterInventory(inventory.name());

        if (inventory == WaterInventory.NONE) {
            moon.setWaterCoveragePercent(0.0);
            moon.setLiquidWaterCoveragePercent(0.0);
            moon.setIceCoveragePercent(0.0);
            moon.setHasSubsurfaceWater(false);
            moon.setSubsurfaceWaterDepthKm(null);
            return;
        }

        distributeMoonWater(moon, inventory);
        assessMoonSubsurfaceWater(moon, inventory);

        double liquid = moon.getLiquidWaterCoveragePercent() != null ? moon.getLiquidWaterCoveragePercent() : 0.0;
        double ice = moon.getIceCoveragePercent() != null ? moon.getIceCoveragePercent() : 0.0;
        moon.setWaterCoveragePercent(Math.min(100.0, liquid + ice));
    }

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

            // Mass contribution (Ice Worlds range ~0.1 to 5+ Earth masses)
            if (mass > 3.0) score += 35;
            else if (mass > 1.5) score += 25;
            else if (mass > 0.8) score += 15;
            else if (mass > 0.3) score += 8;
            // else: tiny ice bodies get 0

            // Atmosphere retention helps preserve volatile inventory
            if (pressure > 1.0) score += 25;
            else if (pressure > 0.1) score += 15;
            else if (pressure > 0.01) score += 8;
            else if (pressure > 0.001) score += 3;
            // stripped atmosphere = no bonus

            // Atmosphere type matters
            if ("TITAN_LIKE".equals(atmoClass) || "AMMONIA".equals(atmoClass)) score += 10;
            else if ("NONE".equals(atmoClass)) score -= 10;

            // Magnetic protection helps retain volatiles
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

    private WaterInventory determineMoonWaterInventory(Moon moon) {
        String composition = moon.getCompositionType() != null ? moon.getCompositionType() : "";
        String compClass = moon.getCompositionClassification() != null ? moon.getCompositionClassification() : "";
        double mass = moon.getEarthMass() != null ? moon.getEarthMass() : 0;
        boolean hasOcean = Boolean.TRUE.equals(moon.getHasSubsurfaceOcean());

        // ICY moons are inherently water-rich
        if ("ICY".equals(composition) || "ICE_RICH".equals(compClass) || "MIXED_SILICATE_ICE".equals(compClass)) {
            if (hasOcean) {
                if (mass > 0.01) return WaterInventory.OCEAN_WORLD;
                return WaterInventory.ABUNDANT;
            }
            if (mass > 0.005) return WaterInventory.ABUNDANT;
            if (mass > 0.001) return WaterInventory.MODERATE;
            return WaterInventory.SCARCE;
        }

        // MIXED composition moons
        if ("MIXED".equals(composition)) {
            if (hasOcean) return WaterInventory.MODERATE;
            if (mass > 0.005) return WaterInventory.SCARCE;
            return WaterInventory.TRACE;
        }

        // ROCKY moons — generally dry but can have trace water
        if ("ROCKY".equals(composition)) {
            // Volcanic activity can release water vapor
            if ("HIGH".equals(moon.getGeologicalActivity())) {
                return WaterInventory.TRACE;
            }
            // Very small rocky moons are bone dry
            if (mass < 0.001) return WaterInventory.NONE;
            // Larger rocky moons may have trace water bound in minerals
            return RandomUtils.rollRange(0, 100) < 40 ? WaterInventory.TRACE : WaterInventory.NONE;
        }

        return WaterInventory.NONE;
    }

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

    private void distributeWater(Planet planet, WaterInventory inventory) {
        Double surfaceTemp = planet.getSurfaceTemp();
        Double surfacePressure = planet.getSurfacePressure();

        if (surfaceTemp == null) {
            double icePercent = getBaseIcePercent(inventory) * 0.5;
            planet.setLiquidWaterCoveragePercent(0.0);
            planet.setIceCoveragePercent(icePercent);
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
            distributeLiquidDominated(planet, inventory, temp);
        } else if (temp < WATER_TRIPLE_POINT_TEMP_K) {
            distributeIceDominated(planet, inventory, temp, pressure);
        } else {
            planet.setLiquidWaterCoveragePercent(0.0);
            planet.setIceCoveragePercent(0.0);
        }
    }

    private void distributeMoonWater(Moon moon, WaterInventory inventory) {
        double temp = moon.getSurfaceTemp() != null ? moon.getSurfaceTemp() : 100.0;
        double pressure = moon.getSurfacePressure() != null ? moon.getSurfacePressure() : 0.0;

        // Most moons are cold and airless → nearly all water is ice
        if (temp < WATER_TRIPLE_POINT_TEMP_K || pressure < WATER_TRIPLE_POINT_PRESSURE_ATM) {
            // Below triple point: no stable liquid water on surface
            double icePercent = getBaseIcePercent(inventory) * RandomUtils.rollRange(0.7, 1.3);

            // Very cold moons might have less visible surface ice (buried under regolith)
            if (temp < 80) {
                icePercent *= 0.5;
            }

            moon.setLiquidWaterCoveragePercent(0.0);
            moon.setIceCoveragePercent(Math.max(0, Math.min(100, icePercent)));
            return;
        }

        // Rare case: moon with atmosphere and temps allowing liquid water
        // (like a large Titan-like moon in habitable zone)
        if (temp >= WATER_TRIPLE_POINT_TEMP_K && temp <= WATER_BOILING_100C_K
                && pressure >= WATER_TRIPLE_POINT_PRESSURE_ATM) {
            double liquidPercent = getBaseLiquidPercent(inventory) * RandomUtils.rollRange(0.3, 0.7);
            double icePercent = getBaseIcePercent(inventory) * RandomUtils.rollRange(0.3, 0.6);

            moon.setLiquidWaterCoveragePercent(Math.max(0, Math.min(100, liquidPercent)));
            moon.setIceCoveragePercent(Math.max(0, Math.min(100, icePercent)));
            return;
        }

        // Hot moon (volcanic) — water exists as vapor, minimal surface ice
        if (temp > WATER_BOILING_100C_K) {
            moon.setLiquidWaterCoveragePercent(0.0);
            moon.setIceCoveragePercent(0.0);
            return;
        }

        // Default: ice-dominated
        moon.setLiquidWaterCoveragePercent(0.0);
        moon.setIceCoveragePercent(getBaseIcePercent(inventory) * RandomUtils.rollRange(0.5, 1.0));
    }

    private void distributeLiquidDominated(Planet planet, WaterInventory inventory,
                                            double temp) {
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

        planet.setLiquidWaterCoveragePercent(Math.max(0, Math.min(100, liquidPercent)));
        planet.setIceCoveragePercent(Math.max(0, Math.min(100, icePercent)));
    }

    private void distributeIceDominated(Planet planet, WaterInventory inventory,
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

        planet.setLiquidWaterCoveragePercent(Math.max(0, Math.min(100, liquidPercent)));
        planet.setIceCoveragePercent(Math.max(0, Math.min(100, icePercent)));
    }

    private void assessSubsurfaceWater(Planet planet, WaterInventory inventory) {
        if (inventory == WaterInventory.NONE || inventory == WaterInventory.TRACE) {
            if (inventory == WaterInventory.TRACE) {
                Double activityScore = planet.getActivityScore();
                if (activityScore != null && activityScore > 3.0) {
                    planet.setHasSubsurfaceWater(true);
                    planet.setSubsurfaceWaterDepthKm(RandomUtils.rollRange(5.0, 50.0));
                    return;
                }
            }
            planet.setHasSubsurfaceWater(false);
            planet.setSubsurfaceWaterDepthKm(null);
            return;
        }

        Double activityScore = planet.getActivityScore();
        Double surfaceTemp = planet.getSurfaceTemp();

        boolean hasGeothermal = activityScore != null && activityScore > 1.5;
        boolean surfaceFrozen = surfaceTemp != null && surfaceTemp < WATER_TRIPLE_POINT_TEMP_K;

        if (hasGeothermal) {
            planet.setHasSubsurfaceWater(true);

            double baseDepth;
            if (surfaceFrozen) {
                double tempDeficit = WATER_TRIPLE_POINT_TEMP_K - surfaceTemp;
                double gradient = 10.0 + (activityScore * 5.0);
                baseDepth = tempDeficit / gradient;
                baseDepth = Math.max(0.5, Math.min(200.0, baseDepth));
            } else {
                baseDepth = RandomUtils.rollRange(0.1, 5.0);
            }
            planet.setSubsurfaceWaterDepthKm(baseDepth * RandomUtils.rollRange(0.8, 1.2));

        } else if (surfaceFrozen && (inventory == WaterInventory.ABUNDANT || inventory == WaterInventory.OCEAN_WORLD)) {
            planet.setHasSubsurfaceWater(true);
            planet.setSubsurfaceWaterDepthKm(RandomUtils.rollRange(20.0, 100.0));
        } else if (inventory == WaterInventory.ABUNDANT || inventory == WaterInventory.OCEAN_WORLD) {
            planet.setHasSubsurfaceWater(true);
            planet.setSubsurfaceWaterDepthKm(RandomUtils.rollRange(2.0, 30.0));
        } else {
            planet.setHasSubsurfaceWater(false);
            planet.setSubsurfaceWaterDepthKm(null);
        }
    }

    private void assessMoonSubsurfaceWater(Moon moon, WaterInventory inventory) {
        // Moon already has hasSubsurfaceOcean from MoonCreator — respect that
        // but enhance with the broader hasSubsurfaceWater concept

        if (Boolean.TRUE.equals(moon.getHasSubsurfaceOcean())) {
            // If there's a subsurface ocean, there's definitely subsurface water
            moon.setHasSubsurfaceWater(true);
            if (moon.getSubsurfaceWaterDepthKm() == null) {
                // Use ice shell thickness if available, otherwise estimate
                if (moon.getIceShellThicknessKm() != null) {
                    moon.setSubsurfaceWaterDepthKm(moon.getIceShellThicknessKm());
                } else {
                    moon.setSubsurfaceWaterDepthKm(RandomUtils.rollRange(5.0, 100.0));
                }
            }
            return;
        }

        // Even without a full ocean, icy/mixed moons can have subsurface water pockets
        if (inventory.ordinal() >= WaterInventory.MODERATE.ordinal()) {
            String tidalLevel = moon.getTidalHeatingLevel() != null ? moon.getTidalHeatingLevel() : "NONE";
            double chance = 20; // Base chance for moderate+ water inventory

            switch (tidalLevel) {
                case "HIGH", "EXTREME" -> chance += 40;
                case "MODERATE" -> chance += 20;
                case "LOW" -> chance += 10;
            }

            if (RandomUtils.rollRange(0, 100) < chance) {
                moon.setHasSubsurfaceWater(true);
                moon.setSubsurfaceWaterDepthKm(RandomUtils.rollRange(10.0, 200.0));
            } else {
                moon.setHasSubsurfaceWater(false);
            }
        } else {
            moon.setHasSubsurfaceWater(false);
        }
    }

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
