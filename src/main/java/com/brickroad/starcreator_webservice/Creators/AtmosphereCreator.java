package com.brickroad.starcreator_webservice.Creators;

import com.brickroad.starcreator_webservice.model.atmospheres.AtmosphereClassification;
import com.brickroad.starcreator_webservice.model.atmospheres.AtmosphereGas;
import com.brickroad.starcreator_webservice.model.atmospheres.PlanetaryAtmosphere;
import com.brickroad.starcreator_webservice.utils.RandomUtils;

public class AtmosphereCreator {

    public static PlanetaryAtmosphere generateAtmosphere(String planetType, double surfaceTemp, double earthMass, double distanceAU) {

        if (earthMass < 0.1 || surfaceTemp > 2000) {
            return createNoneAtmosphere();
        }

        return switch (planetType.toLowerCase()) {
            case "terrestrial planet", "super-earth" -> createTerrestrialAtmosphere(surfaceTemp, earthMass, distanceAU);
            case "ocean world" -> createOceanWorldAtmosphere(surfaceTemp, earthMass);
            case "desert planet" -> createDesertAtmosphere(surfaceTemp);
            case "lava planet", "hot rocky planet" -> createVolcanicAtmosphere(surfaceTemp);
            case "ice world" -> createIceWorldAtmosphere(surfaceTemp);
            case "mini-neptune", "sub-neptune" -> createMiniNeptuneAtmosphere(surfaceTemp, earthMass);
            case "gas giant", "hot jupiter", "super-jupiter" -> createGasGiantAtmosphere(surfaceTemp);
            case "ice giant", "puffy planet" -> createIceGiantAtmosphere(surfaceTemp);
            case "carbon planet" -> createCarbonAtmosphere();
            case "iron planet" -> createIronAtmosphere();
            case "dwarf planet", "rogue planet" -> createDwarfAtmosphere(surfaceTemp);
            default -> createCustomAtmosphere(surfaceTemp, earthMass);
        };
    }
    
    private static PlanetaryAtmosphere createTerrestrialAtmosphere(double temp, double mass, double distanceAU) {
        if (temp >= 250 && temp <= 320 && mass >= 0.5 && mass <= 2.0 && distanceAU >= 0.8 && distanceAU <= 1.5) {
            return createEarthLikeAtmosphere();
        }
        if (temp > 400) {
            return createVenusLikeAtmosphere();
        }
        if (mass < 0.3 || temp < 250) {
            return createMarsLikeAtmosphere();
        }
        return createModerateAtmosphere(temp, mass);
    }
    
    private static PlanetaryAtmosphere createEarthLikeAtmosphere() {
        return new PlanetaryAtmosphere.Builder()
                .addGas(AtmosphereGas.NITROGEN, 78.08)
                .addGas(AtmosphereGas.OXYGEN, 20.95)
                .addGas(AtmosphereGas.ARGON, 0.93)
                .addGas(AtmosphereGas.CARBON_DIOXIDE, 0.04)
                .addGas(AtmosphereGas.WATER_VAPOR, RandomUtils.rollRange(0.1, 4.0))
                .classification(AtmosphereClassification.EARTH_LIKE)
                .build();
    }
    
    private static PlanetaryAtmosphere createVenusLikeAtmosphere() {
        return new PlanetaryAtmosphere.Builder()
                .addGas(AtmosphereGas.CARBON_DIOXIDE, 96.5)
                .addGas(AtmosphereGas.NITROGEN, 3.5)
                .addGas(AtmosphereGas.SULFUR_DIOXIDE, RandomUtils.rollRange(0.01, 0.2))
                .classification(AtmosphereClassification.VENUS_LIKE)
                .build();
    }
    
    private static PlanetaryAtmosphere createMarsLikeAtmosphere() {
        return new PlanetaryAtmosphere.Builder()
                .addGas(AtmosphereGas.CARBON_DIOXIDE, 95.3)
                .addGas(AtmosphereGas.NITROGEN, 2.7)
                .addGas(AtmosphereGas.ARGON, 1.6)
                .addGas(AtmosphereGas.OXYGEN, 0.13)
                .addGas(AtmosphereGas.CARBON_MONOXIDE, 0.08)
                .classification(AtmosphereClassification.MARS_LIKE)
                .build();
    }
    
    private static PlanetaryAtmosphere createModerateAtmosphere(double temp, double mass) {
        double n2 = RandomUtils.rollRange(60.0, 85.0);
        double co2 = RandomUtils.rollRange(5.0, 30.0);
        double remainder = 100.0 - n2 - co2;
        
        var builder = new PlanetaryAtmosphere.Builder()
                .addGas(AtmosphereGas.NITROGEN, n2)
                .addGas(AtmosphereGas.CARBON_DIOXIDE, co2);
        
        // Add trace gases
        if (temp < 300 && mass > 0.8) {
            builder.addGas(AtmosphereGas.OXYGEN, RandomUtils.rollRange(0.1, remainder * 0.5));
        }
        
        builder.classification(AtmosphereClassification.CUSTOM);
        return builder.build();
    }
    
    private static PlanetaryAtmosphere createOceanWorldAtmosphere(double temp, double mass) {
        return new PlanetaryAtmosphere.Builder()
                .addGas(AtmosphereGas.NITROGEN, RandomUtils.rollRange(70.0, 80.0))
                .addGas(AtmosphereGas.OXYGEN, RandomUtils.rollRange(15.0, 25.0))
                .addGas(AtmosphereGas.WATER_VAPOR, RandomUtils.rollRange(3.0, 10.0))
                .addGas(AtmosphereGas.CARBON_DIOXIDE, RandomUtils.rollRange(0.01, 1.0))
                .classification(AtmosphereClassification.EARTH_LIKE)
                .build();
    }
    
    private static PlanetaryAtmosphere createDesertAtmosphere(double temp) {
        return new PlanetaryAtmosphere.Builder()
                .addGas(AtmosphereGas.CARBON_DIOXIDE, RandomUtils.rollRange(85.0, 95.0))
                .addGas(AtmosphereGas.NITROGEN, RandomUtils.rollRange(3.0, 10.0))
                .addGas(AtmosphereGas.ARGON, RandomUtils.rollRange(1.0, 3.0))
                .classification(AtmosphereClassification.MARS_LIKE)
                .build();
    }
    
    private static PlanetaryAtmosphere createVolcanicAtmosphere(double temp) {
        return new PlanetaryAtmosphere.Builder()
                .addGas(AtmosphereGas.SULFUR_DIOXIDE, RandomUtils.rollRange(40.0, 70.0))
                .addGas(AtmosphereGas.CARBON_DIOXIDE, RandomUtils.rollRange(20.0, 50.0))
                .addGas(AtmosphereGas.HYDROGEN_SULFIDE, RandomUtils.rollRange(1.0, 10.0))
                .classification(AtmosphereClassification.VOLCANIC)
                .build();
    }
    
    private static PlanetaryAtmosphere createIceWorldAtmosphere(double temp) {
        if (temp < 100) {
            // Very cold - nitrogen and methane
            return new PlanetaryAtmosphere.Builder()
                    .addGas(AtmosphereGas.NITROGEN, RandomUtils.rollRange(60.0, 90.0))
                    .addGas(AtmosphereGas.METHANE, RandomUtils.rollRange(5.0, 30.0))
                    .addGas(AtmosphereGas.CARBON_MONOXIDE, RandomUtils.rollRange(1.0, 10.0))
                    .classification(AtmosphereClassification.TITAN_LIKE)
                    .build();
        } else {
            // Moderate cold
            return new PlanetaryAtmosphere.Builder()
                    .addGas(AtmosphereGas.NITROGEN, RandomUtils.rollRange(70.0, 95.0))
                    .addGas(AtmosphereGas.CARBON_DIOXIDE, RandomUtils.rollRange(2.0, 20.0))
                    .addGas(AtmosphereGas.ARGON, RandomUtils.rollRange(1.0, 5.0))
                    .classification(AtmosphereClassification.CUSTOM)
                    .build();
        }
    }
    
    private static PlanetaryAtmosphere createGasGiantAtmosphere(double temp) {
        double h2 = RandomUtils.rollRange(85.0, 90.0);
        double he = RandomUtils.rollRange(10.0, 15.0);
        double remainder = 100.0 - h2 - he;
        
        return new PlanetaryAtmosphere.Builder()
                .addGas(AtmosphereGas.HYDROGEN, h2)
                .addGas(AtmosphereGas.HELIUM, he)
                .addGas(AtmosphereGas.METHANE, remainder * 0.5)
                .addGas(AtmosphereGas.AMMONIA, remainder * 0.3)
                .addGas(AtmosphereGas.WATER_VAPOR, remainder * 0.2)
                .classification(AtmosphereClassification.JOVIAN)
                .build();
    }
    
    private static PlanetaryAtmosphere createIceGiantAtmosphere(double temp) {
        return new PlanetaryAtmosphere.Builder()
                .addGas(AtmosphereGas.HYDROGEN, RandomUtils.rollRange(80.0, 85.0))
                .addGas(AtmosphereGas.HELIUM, RandomUtils.rollRange(10.0, 15.0))
                .addGas(AtmosphereGas.METHANE, RandomUtils.rollRange(2.0, 4.0))
                .addGas(AtmosphereGas.AMMONIA, RandomUtils.rollRange(0.5, 2.0))
                .addGas(AtmosphereGas.WATER_VAPOR, RandomUtils.rollRange(0.5, 1.5))
                .classification(AtmosphereClassification.ICE_GIANT)
                .build();
    }
    
    private static PlanetaryAtmosphere createMiniNeptuneAtmosphere(double temp, double mass) {
        if (mass > 5.0) {
            // Large mini-neptune - more hydrogen
            return createGasGiantAtmosphere(temp);
        } else {
            // Small mini-neptune - transitional
            return new PlanetaryAtmosphere.Builder()
                    .addGas(AtmosphereGas.HYDROGEN, RandomUtils.rollRange(60.0, 80.0))
                    .addGas(AtmosphereGas.HELIUM, RandomUtils.rollRange(15.0, 25.0))
                    .addGas(AtmosphereGas.METHANE, RandomUtils.rollRange(2.0, 10.0))
                    .addGas(AtmosphereGas.WATER_VAPOR, RandomUtils.rollRange(1.0, 5.0))
                    .classification(AtmosphereClassification.ICE_GIANT)
                    .build();
        }
    }
    
    private static PlanetaryAtmosphere createCarbonAtmosphere() {
        return new PlanetaryAtmosphere.Builder()
                .addGas(AtmosphereGas.CARBON_MONOXIDE, RandomUtils.rollRange(50.0, 70.0))
                .addGas(AtmosphereGas.CARBON_DIOXIDE, RandomUtils.rollRange(20.0, 40.0))
                .addGas(AtmosphereGas.METHANE, RandomUtils.rollRange(5.0, 15.0))
                .classification(AtmosphereClassification.EXOTIC)
                .build();
    }
    
    private static PlanetaryAtmosphere createIronAtmosphere() {
        // Very hot iron planet - metallic vapors
        return new PlanetaryAtmosphere.Builder()
                .addGas(AtmosphereGas.IRON, RandomUtils.rollRange(40.0, 60.0))
                .addGas(AtmosphereGas.SODIUM, RandomUtils.rollRange(20.0, 40.0))
                .addGas(AtmosphereGas.POTASSIUM, RandomUtils.rollRange(10.0, 20.0))
                .classification(AtmosphereClassification.EXOTIC)
                .build();
    }
    
    private static PlanetaryAtmosphere createDwarfAtmosphere(double temp) {
        if (temp < 50) {
            // Very cold - minimal atmosphere
            return new PlanetaryAtmosphere.Builder()
                    .addGas(AtmosphereGas.NITROGEN, RandomUtils.rollRange(60.0, 80.0))
                    .addGas(AtmosphereGas.METHANE, RandomUtils.rollRange(10.0, 30.0))
                    .addGas(AtmosphereGas.CARBON_MONOXIDE, RandomUtils.rollRange(5.0, 15.0))
                    .classification(AtmosphereClassification.CUSTOM)
                    .build();
        } else {
            return createNoneAtmosphere();
        }
    }
    
    private static PlanetaryAtmosphere createNoneAtmosphere() {
        return new PlanetaryAtmosphere.Builder()
                .classification(AtmosphereClassification.NONE)
                .build();
    }
    
    private static PlanetaryAtmosphere createCustomAtmosphere(double temp, double mass) {
        // Generic fallback
        return new PlanetaryAtmosphere.Builder()
                .addGas(AtmosphereGas.NITROGEN, RandomUtils.rollRange(50.0, 90.0))
                .addGas(AtmosphereGas.CARBON_DIOXIDE, RandomUtils.rollRange(5.0, 40.0))
                .addGas(AtmosphereGas.ARGON, RandomUtils.rollRange(0.5, 5.0))
                .classification(AtmosphereClassification.CUSTOM)
                .build();
    }
}
