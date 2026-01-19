package com.brickroad.starcreator_webservice.model.atmospheres;

/**
 * Classification of planetary atmospheres by type
 */
public enum AtmosphereClassification {
    
    EARTH_LIKE("Earth-like", "Nitrogen-oxygen atmosphere, breathable by humans"),
    VENUS_LIKE("Venus-like", "Dense CO2 atmosphere with extreme greenhouse effect"),
    MARS_LIKE("Mars-like", "Thin CO2 atmosphere, minimal surface pressure"),
    TITAN_LIKE("Titan-like", "Dense nitrogen atmosphere with methane"),
    JOVIAN("Jovian", "Hydrogen-helium atmosphere, gas giant"),
    ICE_GIANT("Ice Giant", "Hydrogen-helium with methane and ammonia"),
    AMMONIA("Ammonia", "Ammonia-dominated atmosphere, extremely toxic"),
    METHANE("Methane", "Methane-dominated atmosphere"),
    VOLCANIC("Volcanic", "Sulfur dioxide and volcanic gases"),
    REDUCING("Reducing", "Hydrogen-rich, oxygen-poor"),
    OXIDIZING("Oxidizing", "Oxygen-rich, rare in universe"),
    CORROSIVE("Corrosive", "Chlorine or fluorine-based"),
    EXOTIC("Exotic", "Unusual composition (metallic vapors, etc.)"),
    NONE("None", "No atmosphere or extremely thin"),
    CUSTOM("Custom", "Non-standard composition");
    
    private final String name;
    private final String description;
    
    AtmosphereClassification(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
}
