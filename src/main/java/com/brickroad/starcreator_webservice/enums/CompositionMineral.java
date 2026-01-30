package com.brickroad.starcreator_webservice.enums;

import lombok.Getter;

@Getter
public enum CompositionMineral {
    // Silicates (Rocky/Terrestrial planets)
    OLIVINE("Olivine", "Silicate", 3.2, 3.4, "(Mg,Fe)₂SiO₄"),
    PYROXENE("Pyroxene", "Silicate", 3.2, 3.6, "(Ca,Mg,Fe)SiO₃"),
    FELDSPAR("Feldspar", "Silicate", 2.5, 2.8, "(K,Na,Ca)(Al,Si)₄O₈"),
    PLAGIOCLASE("Plagioclase", "Silicate", 2.6, 2.8, "(Na,Ca)(Al,Si)AlSi₂O₈"),
    QUARTZ("Quartz", "Silicate", 2.65, 2.65, "SiO₂"),
    SERPENTINE("Serpentine", "Silicate", 2.5, 2.6, "(Mg,Fe)₃Si₂O₅(OH)₄"),
    SILICATE("Mixed Silicates", "Silicate", 2.5, 3.5, "General silicate minerals"),

    // Iron/Metals (Core materials)
    IRON("Iron", "Metal", 7.87, 7.87, "Fe"),
    NICKEL("Nickel", "Metal", 8.9, 8.9, "Ni"),
    IRON_NICKEL_ALLOY("Iron-Nickel Alloy", "Metal", 7.8, 8.5, "Fe-Ni"),
    METALLIC_HYDROGEN("Metallic Hydrogen", "Metal", 0.7, 0.8, "H (metallic)"),

    // Carbon compounds (Carbon planets)
    GRAPHITE("Graphite", "Carbon", 2.1, 2.3, "C (graphite)"),
    DIAMOND("Diamond", "Carbon", 3.5, 3.53, "C (diamond)"),
    SILICON_CARBIDE("Silicon Carbide", "Carbide", 3.2, 3.2, "SiC"),
    TITANIUM_CARBIDE("Titanium Carbide", "Carbide", 4.93, 4.93, "TiC"),
    IRON_CARBIDE("Iron Carbide", "Carbide", 7.4, 7.7, "Fe₃C"),
    AMORPHOUS_CARBON("Amorphous Carbon", "Carbon", 1.8, 2.1, "C (amorphous)"),

    // Ices (Ice giants, ice worlds, ocean worlds)
    WATER_ICE("Water Ice", "Ice", 0.92, 0.92, "H₂O (ice)"),
    METHANE_ICE("Methane Ice", "Ice", 0.47, 0.52, "CH₄ (ice)"),
    AMMONIA_ICE("Ammonia Ice", "Ice", 0.82, 0.82, "NH₃ (ice)"),
    CARBON_DIOXIDE_ICE("CO₂ Ice", "Ice", 1.56, 1.56, "CO₂ (ice)"),
    NITROGEN_ICE("Nitrogen Ice", "Ice", 1.03, 1.03, "N₂ (ice)"),
    METHANE_CLATHRATE("Methane Clathrate", "Ice", 0.9, 0.9, "CH₄·H₂O"),

    // Oxides (Rocky mantles)
    MAGNESIUM_OXIDE("Magnesium Oxide", "Oxide", 3.58, 3.58, "MgO"),
    CALCIUM_OXIDE("Calcium Oxide", "Oxide", 3.34, 3.34, "CaO"),
    ALUMINUM_OXIDE("Aluminum Oxide", "Oxide", 3.95, 4.1, "Al₂O₃"),
    SILICON_DIOXIDE("Silicon Dioxide", "Oxide", 2.65, 2.65, "SiO₂"),

    // Volatiles (Lava planets, volcanic)
    SULFUR("Sulfur", "Non-metal", 2.0, 2.1, "S"),
    MOLTEN_ROCK("Molten Rock", "Magma", 2.5, 2.8, "Silicate Melt"),
    BASALT("Basalt", "Volcanic Rock", 2.8, 3.0, "Mafic Rock"),

    // Gas giant materials (diffuse)
    HYDROGEN_HELIUM_MIX("Hydrogen-Helium", "Gas", 0.1, 0.2, "H₂-He mix"),

    // Ocean world materials
    LIQUID_WATER("Liquid Water", "Liquid", 1.0, 1.0, "H₂O (liquid)"),
    HIGH_PRESSURE_ICE("High-Pressure Ice", "Ice", 1.3, 1.5, "Ice VII/X"),

    // Surface Materials (Regolith, debris)
    REGOLITH("Regolith", "Surface Material", 1.5, 2.5, "Mixed fine debris"),

    // Organic Materials (Comets, outer solar system)
    ORGANIC("Organic Compounds", "Organic", 1.0, 1.5, "C-H-N-O compounds"),
    THOLINS("Tholins", "Organic", 1.2, 1.4, "Complex organic polymers");

    private final String displayName;
    private final String category;
    private final double minDensity;
    private final double maxDensity;
    private final String formula;

    CompositionMineral(String displayName, String category, double minDensity, double maxDensity, String formula) {
        this.displayName = displayName;
        this.category = category;
        this.minDensity = minDensity;
        this.maxDensity = maxDensity;
        this.formula = formula;
    }

}
