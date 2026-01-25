package com.brickroad.starcreator_webservice.enums;

import lombok.Getter;


@Getter
public enum AtmosphereGas {
    
    // Major atmospheric components
    NITROGEN("N2", "Nitrogen", "Inert", 28.0, false),
    OXYGEN("O2", "Oxygen", "Breathable", 32.0, true),
    CARBON_DIOXIDE("CO2", "Carbon Dioxide", "Greenhouse", 44.0, false),
    WATER_VAPOR("H2O", "Water Vapor", "Greenhouse", 18.0, false),
    
    // Noble gases
    ARGON("Ar", "Argon", "Inert", 40.0, false),
    HELIUM("He", "Helium", "Inert", 4.0, false),
    NEON("Ne", "Neon", "Inert", 20.2, false),
    KRYPTON("Kr", "Krypton", "Inert", 83.8, false),
    XENON("Xe", "Xenon", "Inert", 131.3, false),
    
    // Hydrogen and compounds
    HYDROGEN("H2", "Hydrogen", "Combustive", 2.0, false),
    METHANE("CH4", "Methane", "Toxic", 16.0, false),
    AMMONIA("NH3", "Ammonia", "Toxic", 17.0, false),
    
    // Volcanic/toxic gases
    SULFUR_DIOXIDE("SO2", "Sulfur Dioxide", "Toxic", 64.1, false),
    HYDROGEN_SULFIDE("H2S", "Hydrogen Sulfide", "Toxic", 34.1, false),
    CHLORINE("Cl2", "Chlorine", "Corrosive", 71.0, false),
    FLUORINE("F2", "Fluorine", "Corrosive", 38.0, false),
    
    // Other compounds
    CARBON_MONOXIDE("CO", "Carbon Monoxide", "Toxic", 28.0, false),
    NITROUS_OXIDE("N2O", "Nitrous Oxide", "Greenhouse", 44.0, false),
    OZONE("O3", "Ozone", "Protective", 48.0, false),
    PHOSPHINE("PH3", "Phosphine", "Toxic", 34.0, false),
    SILANE("SiH4", "Silane", "Exotic", 32.1, false),
    
    // Exotic atmospheres
    SODIUM("Na", "Sodium Vapor", "Exotic", 23.0, false),
    POTASSIUM("K", "Potassium Vapor", "Exotic", 39.1, false),
    IRON("Fe", "Iron Vapor", "Exotic", 55.8, false);
    
    private final String formula;
    private final String name;
    private final String effect;
    private final double molecularWeight;
    private final boolean breathable;
    
    AtmosphereGas(String formula, String name, String effect, double molecularWeight, boolean breathable) {
        this.formula = formula;
        this.name = name;
        this.effect = effect;
        this.molecularWeight = molecularWeight;
        this.breathable = breathable;
    }

}
