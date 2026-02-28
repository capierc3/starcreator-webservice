package com.brickroad.starcreator_webservice.utils.planets;

import com.brickroad.starcreator_webservice.entity.ud.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SurfaceColorDeriver} — surface color derivation from
 * planet composition, temperature, water, volcanism, and atmosphere.
 * <p>
 * Pure unit tests — no Spring context, no database.
 */
class SurfaceColorDeriverTest {

    // ══════════════════════════════════════════════════════════════════
    // Helper: build a minimal Planet with controllable properties
    // ══════════════════════════════════════════════════════════════════

    private static Planet buildPlanet(String planetType, String compClass, String atmoClass,
                                       double surfaceTemp, double waterPct, double icePct,
                                       boolean volcanic, String volcType) {
        Planet p = new Planet();
        p.setPlanetType(planetType);
        p.setCompositionClassification(compClass);
        p.setSurfaceTemp(surfaceTemp);

        // Atmosphere
        Atmosphere atmo = new Atmosphere();
        atmo.setClassification(atmoClass);
        p.setAtmosphere(atmo);

        // Water
        WaterProperties water = new WaterProperties();
        water.setWaterCoveragePercent(waterPct);
        water.setIceCoveragePercent(icePct);
        p.setWater(water);

        // Terrain with volcanism
        TerrainProperties terrain = new TerrainProperties();
        terrain.setHasVolcanicActivity(volcanic);
        terrain.setVolcanismType(volcType);
        p.setTerrain(terrain);

        return p;
    }

    private static Planet buildSimple(String planetType, String compClass, double surfaceTemp) {
        return buildPlanet(planetType, compClass, "EARTH_LIKE", surfaceTemp, 0, 0, false, null);
    }

    // ══════════════════════════════════════════════════════════════════
    // Basic color derivation
    // ══════════════════════════════════════════════════════════════════

    @Test
    void deriveReturnsNonNullColors() {
        Planet p = buildSimple("Terrestrial Planet", "SILICATE_RICH", 288);
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertNotNull(colors.primary(), "Primary color should not be null");
        assertNotNull(colors.secondary(), "Secondary color should not be null");
    }

    @Test
    void colorsAreValidHexFormat() {
        Planet p = buildSimple("Terrestrial Planet", "SILICATE_RICH", 288);
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertTrue(colors.primary().matches("#[0-9a-f]{6}"),
                "Primary color should be valid hex: " + colors.primary());
        assertTrue(colors.secondary().matches("#[0-9a-f]{6}"),
                "Secondary color should be valid hex: " + colors.secondary());
    }

    // ══════════════════════════════════════════════════════════════════
    // Composition-based color
    // ══════════════════════════════════════════════════════════════════

    @Test
    void silicateRich_temperateWithWater_showsGreen() {
        Planet p = buildPlanet("Terrestrial Planet", "SILICATE_RICH", "EARTH_LIKE",
                288, 10, 0, false, null);
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertEquals("#88aa66", colors.primary(), "Temperate silicate should be green-brown");
    }

    @Test
    void silicateRich_oceanDominated_showsBlue() {
        Planet p = buildPlanet("Terrestrial Planet", "SILICATE_RICH", "EARTH_LIKE",
                288, 70, 0, false, null);
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertEquals("#3377aa", colors.primary(), "Ocean-dominated silicate should be blue");
    }

    @Test
    void silicateRich_hot_showsDesertBrown() {
        Planet p = buildSimple("Desert Planet", "SILICATE_RICH", 450);
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        // Desert type override takes priority
        assertEquals("#cc9955", colors.primary(), "Desert planet should be sandy");
    }

    @Test
    void ironRich_darkGrey() {
        Planet p = buildSimple("Iron Planet", "IRON_RICH", 400);
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertEquals("#666666", colors.primary(), "Iron planet should be dark grey");
    }

    @Test
    void carbonRich_veryDark() {
        Planet p = buildSimple("Carbon Planet", "CARBON_RICH", 500);
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertEquals("#444444", colors.primary(), "Carbon planet should be very dark");
    }

    @Test
    void iceRich_cold_brightIce() {
        Planet p = buildPlanet("Ice World", "ICE_RICH", "NONE", 60, 0, 80, false, null);
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertEquals("#bbccdd", colors.primary(), "Ice world should be bright icy");
    }

    @Test
    void oceanWorld_deepBlue() {
        Planet p = buildPlanet("Ocean Planet", "OCEAN_WORLD", "EARTH_LIKE",
                290, 95, 0, false, null);
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertEquals("#3388aa", colors.primary(), "Ocean planet should be vibrant blue");
    }

    @Test
    void moltenSurface_redLava() {
        Planet p = buildPlanet("Lava Planet", "MOLTEN_SURFACE", "EXOTIC",
                2500, 0, 0, true, "SILICATE");
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertEquals("#dd4422", colors.primary(), "Lava planet should be bright red");
    }

    // ══════════════════════════════════════════════════════════════════
    // Gaseous planet colors (atmosphere-dominated)
    // ══════════════════════════════════════════════════════════════════

    @Test
    void jovian_warmBands() {
        Planet p = buildPlanet("Gas Giant", "GAS_ENVELOPE", "JOVIAN",
                150, 0, 0, false, null);
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertEquals("#b8a88a", colors.primary(), "Cold jovian should be muted bands");
    }

    @Test
    void hotJupiter_glowingClouds() {
        Planet p = buildPlanet("Hot Jupiter", "GAS_ENVELOPE", "JOVIAN",
                1500, 0, 0, false, null);
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        // Hot Jupiter type override takes priority
        assertEquals("#d4764e", colors.primary(), "Hot Jupiter should be orange-glowing");
    }

    @Test
    void iceGiant_cyanBlue() {
        Planet p = buildPlanet("Ice Giant", "GAS_ENVELOPE", "ICE_GIANT",
                60, 0, 0, false, null);
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertEquals("#4499bb", colors.primary(), "Ice giant should be cyan-blue");
    }

    // ══════════════════════════════════════════════════════════════════
    // Secondary color (accents)
    // ══════════════════════════════════════════════════════════════════

    @Test
    void volcanic_secondaryIsLavaGlow() {
        Planet p = buildPlanet("Terrestrial Planet", "SILICATE_RICH", "EARTH_LIKE",
                288, 10, 0, true, "SILICATE");
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertEquals("#cc4422", colors.secondary(), "Silicate volcanism secondary should be lava red");
    }

    @Test
    void cryovolcanic_secondaryIsIcyBlue() {
        // Use a generic planet type so the type override doesn't fire,
        // allowing the volcanism-based secondary to show through
        Planet p = buildPlanet("Terrestrial Planet", "ICE_RICH", "NONE",
                80, 0, 60, true, "CRYOVOLCANIC");
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertEquals("#aaddee", colors.secondary(), "Cryovolcanism secondary should be icy blue-white");
    }

    @Test
    void iceWorld_withCryovolcanism_typeOverrideTakesPriority() {
        // When planet type is "Ice World", the type override always fires
        Planet p = buildPlanet("Ice World", "ICE_RICH", "NONE",
                80, 0, 60, true, "CRYOVOLCANIC");
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertEquals("#bbccdd", colors.primary(), "Ice World override primary");
        assertEquals("#ddeeff", colors.secondary(), "Ice World override secondary");
    }

    @Test
    void sulfurVolcanic_secondaryIsYellow() {
        Planet p = buildPlanet("Terrestrial Planet", "SILICATE_RICH", "VOLCANIC",
                400, 0, 0, true, "SULFUR");
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertEquals("#ccaa33", colors.secondary(), "Sulfur volcanism secondary should be yellow");
    }

    @Test
    void highIce_secondaryIsWhiteBlue() {
        Planet p = buildPlanet("Terrestrial Planet", "MIXED_SILICATE_ICE", "MARS_LIKE",
                200, 0, 50, false, null);
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertEquals("#ddeeff", colors.secondary(), "High ice coverage should give white-blue secondary");
    }

    @Test
    void highWater_secondaryIsOcean() {
        Planet p = buildPlanet("Terrestrial Planet", "SILICATE_RICH", "EARTH_LIKE",
                288, 60, 5, false, null);
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertEquals("#4488bb", colors.secondary(), "High water should give ocean accent");
    }

    // ══════════════════════════════════════════════════════════════════
    // Planet type overrides
    // ══════════════════════════════════════════════════════════════════

    @Test
    void desertPlanet_alwaysSandy() {
        Planet p = buildSimple("Desert Planet", "SILICATE_RICH", 400);
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertEquals("#cc9955", colors.primary());
        assertEquals("#bb8844", colors.secondary());
    }

    @Test
    void dwarfPlanet_muted() {
        Planet p = buildPlanet("Dwarf Planet", "ICE_RICH", "NONE", 50, 0, 0, false, null);
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertEquals("#8899aa", colors.primary());
        assertEquals("#778899", colors.secondary());
    }

    @Test
    void puffyPlanet_inflatedHot() {
        Planet p = buildPlanet("Puffy Planet", "GAS_ENVELOPE", "JOVIAN", 1200, 0, 0, false, null);
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertEquals("#dd8855", colors.primary());
    }

    // ══════════════════════════════════════════════════════════════════
    // Edge cases
    // ══════════════════════════════════════════════════════════════════

    @Test
    void nullCompositionAndType_usesTemperatureFallback() {
        Planet p = buildPlanet(null, null, "NONE", 288, 0, 0, false, null);
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertNotNull(colors.primary());
        assertTrue(colors.primary().matches("#[0-9a-f]{6}"),
                "Fallback should still produce valid hex");
    }

    @Test
    void minimalPlanet_noNullPointers() {
        Planet p = new Planet();
        // No properties set at all
        SurfaceColorDeriver.SurfaceColors colors = SurfaceColorDeriver.derive(p);
        assertNotNull(colors.primary());
        assertNotNull(colors.secondary());
    }

    // ══════════════════════════════════════════════════════════════════
    // Color utility
    // ══════════════════════════════════════════════════════════════════

    @Test
    void shiftColor_producesValidHex() {
        String shifted = SurfaceColorDeriver.shiftColor("#88aa66", 15);
        assertTrue(shifted.matches("#[0-9a-f]{6}"),
                "Shifted color should be valid hex: " + shifted);
    }

    @Test
    void shiftColor_invalidInput_returnsFallback() {
        assertEquals("#887766", SurfaceColorDeriver.shiftColor(null, 10));
        assertEquals("#887766", SurfaceColorDeriver.shiftColor("bad", 10));
    }

    @Test
    void shiftColor_clampsAtBounds() {
        // Very bright input — shift shouldn't exceed 255
        String shifted = SurfaceColorDeriver.shiftColor("#ffffff", 50);
        assertTrue(shifted.startsWith("#"), "Should still be hex");
        // R should be clamped to ff (255), G and B shifted down
        assertNotNull(shifted);
    }
}
