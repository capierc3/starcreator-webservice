package com.brickroad.starcreator_webservice.worldBuilder;

/**
 * String-constant enums for use in tests. Prevents typos when searching
 * for specific planet types, star types, belt types, etc. via SystemFinder.
 *
 * These mirror the database reference data but live in test scope so they
 * can evolve independently of the main enums.
 */
public final class TestEnums {

    private TestEnums() {}

    public enum PlanetType {
        HOT_ROCKY("Hot Rocky Planet"),
        TERRESTRIAL("Terrestrial Planet"),
        SUPER_EARTH("Super-Earth"),
        DESERT("Desert Planet"),
        OCEAN("Ocean Planet"),
        LAVA("Lava Planet"),
        MINI_NEPTUNE("Mini-Neptune"),
        SUB_NEPTUNE("Sub-Neptune"),
        HOT_JUPITER("Hot Jupiter"),
        GAS_GIANT("Gas Giant"),
        SUPER_JUPITER("Super-Jupiter"),
        ICE_GIANT("Ice Giant"),
        PUFFY("Puffy Planet"),
        CARBON("Carbon Planet"),
        IRON("Iron Planet"),
        ICE_WORLD("Ice World"),
        DWARF("Dwarf Planet"),
        ROGUE("Rogue Planet");

        private final String value;

        PlanetType(String value) { this.value = value; }

        @Override public String toString() { return value; }
    }

    public enum StarType {
        MAIN_SEQUENCE_O("Main Sequence O"),
        MAIN_SEQUENCE_B("Main Sequence B"),
        MAIN_SEQUENCE_A("Main Sequence A"),
        MAIN_SEQUENCE_F("Main Sequence F"),
        MAIN_SEQUENCE_G("Main Sequence G"),
        MAIN_SEQUENCE_K("Main Sequence K"),
        MAIN_SEQUENCE_M("Main Sequence M"),
        RED_GIANT("Red Giant"),
        SUPER_GIANT("Super Giant"),
        WHITE_DWARF("White Dwarf"),
        NEUTRON_STAR("Neutron Star"),
        BROWN_DWARF_L("Brown Dwarf L"),
        BROWN_DWARF_T("Brown Dwarf T"),
        BROWN_DWARF_Y("Brown Dwarf Y"),
        PROTO("Proto"),
        T_TAURI("T Tauri");

        private final String value;

        StarType(String value) { this.value = value; }

        @Override public String toString() { return value; }
    }

    public enum BeltType {
        INNER_ROCKY("INNER_ROCKY"),
        OUTER_ROCKY("OUTER_ROCKY"),
        KUIPER("KUIPER"),
        SCATTERED_DISK("SCATTERED_DISK");

        private final String value;

        BeltType(String value) { this.value = value; }

        @Override public String toString() { return value; }
    }

    public enum Composition {
        SILICATE_RICH("SILICATE_RICH"),
        IRON_RICH("IRON_RICH"),
        CARBON_RICH("CARBON_RICH"),
        ICE_RICH("ICE_RICH"),
        MIXED_SILICATE_ICE("MIXED_SILICATE_ICE"),
        OCEAN_WORLD("OCEAN_WORLD"),
        GAS_ENVELOPE("GAS_ENVELOPE"),
        MOLTEN_SURFACE("MOLTEN_SURFACE"),
        EXOTIC("EXOTIC");

        private final String value;

        Composition(String value) { this.value = value; }

        @Override public String toString() { return value; }
    }

    public enum Atmosphere {
        EARTH_LIKE("EARTH_LIKE"),
        VENUS_LIKE("VENUS_LIKE"),
        MARS_LIKE("MARS_LIKE"),
        TITAN_LIKE("TITAN_LIKE"),
        JOVIAN("JOVIAN"),
        ICE_GIANT("ICE_GIANT"),
        AMMONIA("AMMONIA"),
        METHANE("METHANE"),
        VOLCANIC("VOLCANIC"),
        REDUCING("REDUCING"),
        OXIDIZING("OXIDIZING"),
        CORROSIVE("CORROSIVE"),
        EXOTIC("EXOTIC"),
        NONE("NONE"),
        CUSTOM("CUSTOM");

        private final String value;

        Atmosphere(String value) { this.value = value; }

        @Override public String toString() { return value; }
    }

    public enum LifeComplexity {
        NONE("NONE"),
        MICROBIAL_ONLY("MICROBIAL_ONLY"),
        MICROBIAL_LIKELY("MICROBIAL_LIKELY"),
        SIMPLE_MULTICELLULAR("SIMPLE_MULTICELLULAR"),
        COMPLEX_MULTICELLULAR("COMPLEX_MULTICELLULAR"),
        COMPLEX_POSSIBLE("COMPLEX_POSSIBLE");

        private final String value;

        LifeComplexity(String value) { this.value = value; }

        @Override public String toString() { return value; }
    }

    public enum HabitableZone {
        INNER("inner"),
        HABITABLE("habitable"),
        FROST_LINE("frost_line"),
        OUTER("outer");

        private final String value;

        HabitableZone(String value) { this.value = value; }

        @Override public String toString() { return value; }
    }
}
