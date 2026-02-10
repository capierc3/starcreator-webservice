package com.brickroad.starcreator_webservice.enums;

import lombok.Getter;

/**
 * High-level system classification archetypes.
 * A system can have multiple archetypes (primary + secondary).
 */
@Getter
public enum SystemArchetype {

    // --- Life & Habitability ---
    HABITABLE_OASIS("Habitable Oasis",
            "A rare jewel — at least one world where humans could walk under open sky"),
    GARDEN_OF_EDEN("Garden of Eden",
            "Multiple habitable or near-habitable worlds; a colonist's dream"),
    SUBSURFACE_OCEAN_NETWORK("Subsurface Ocean Network",
            "Ice-capped moons hiding vast liquid oceans — prime targets for xenobiologists"),
    BIOSIGNATURE_CANDIDATE("Biosignature Candidate",
            "Worlds showing chemical fingerprints that hint at biological activity"),

    // --- Resource & Industry ---
    MINING_BONANZA("Mining Bonanza",
            "Metal-rich worlds and dense asteroid belts — a corporate prospector's paradise"),
    GAS_GIANT_DOMINION("Gas Giant Dominion",
            "Dominated by massive gas worlds; fuel-skimming and atmospheric mining opportunities abound"),
    ICE_FRONTIER("Ice Frontier",
            "A frozen expanse of ice worlds and cryogenic belts — water and volatiles in abundance"),
    METAL_CORE_GRAVEYARD("Metal Core Graveyard",
            "Iron-rich stripped cores and metallic worlds — industrial-grade ore deposits"),

    // --- Stellar Drama ---
    BINARY_CHAOS("Binary Chaos",
            "Multiple stars in complex orbital dance; unpredictable gravitational tides reshape everything"),
    DYING_GIANT_SYSTEM("Dying Giant System",
            "A bloated red giant consuming its inner worlds — a system in its twilight"),
    STELLAR_FURNACE("Stellar Furnace",
            "A young, violently active star bathing its worlds in flares and radiation"),
    NEUTRON_STAR_RELIC("Neutron Star Relic",
            "The collapsed remnant of a dead star — extreme physics, extreme danger"),
    BROWN_DWARF_GLOOM("Brown Dwarf Gloom",
            "A failed star barely glowing — perpetual twilight across all worlds"),

    // --- Composition & Structure ---
    YOUNG_PROTOPLANETARY("Young Protoplanetary",
            "A newborn system still condensing from its primordial disk"),
    DEAD_SYSTEM("Dead System",
            "No planets, no belts — just a lonely star in the void"),
    SPARSE_SYSTEM("Sparse System",
            "A handful of worlds scattered across a vast, empty expanse"),
    PACKED_INNER_SYSTEM("Packed Inner System",
            "Worlds crowded close to their star — tight orbits, short years"),
    GRAND_ARCHIVE("Grand Archive",
            "An ancient, stable system — billions of years of geological and potential biological history"),

    // --- Strategic & Narrative ---
    FORTRESS_SYSTEM("Fortress System",
            "Dense asteroid belts and chokepoint orbits — a natural defensive position"),
    WAYSTATION("Waystation",
            "A convenient refueling stop — gas giants for fuel, belts for materials, but nothing to settle"),
    TERRAFORMER_PROSPECT("Terraformer's Prospect",
            "Worlds that are close to habitable — with enough engineering, they could bloom"),
    LAVA_HELL("Lava Hell",
            "Worlds of molten rock and scorching atmosphere — valuable minerals if you can survive the heat"),
    OCEAN_WORLD_SYSTEM("Ocean World System",
            "Worlds wrapped in deep global oceans — water for centuries and possible alien seas"),
    ANOMALOUS("Anomalous",
            "Something here doesn't fit standard models — unusual compositions, unexpected orbits, or strange chemistry");

    private final String displayName;
    private final String description;

    SystemArchetype(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}