-- =====================================================================
-- Flyway Migration V27: Diversify Plains/Grassland Terrain Types (REVISED)
-- =====================================================================
-- Description: Updates existing GRASSLAND and adds new specialized variants
--              Works with existing STEPPE, TUNDRA, and SALT_FLATS terrains
-- Author: Chase
-- Date: 2026-01-21
-- =====================================================================

-- =====================================================================
-- SECTION 1: Update Existing Terrains
-- =====================================================================

-- Update existing GRASSLAND to be stricter and more Earth-like
UPDATE ref.terrain_type_ref
SET display_name = 'Verdant Grasslands',
    description = 'Lush prairies, savannas, and meadows with rich seasonal vegetation',
    min_temperature_k = 275.0,
    max_temperature_k = 310.0,
    requires_water = true,
    requires_atmosphere = true,
    min_pressure_atm = 0.5,
    rarity_weight = 150
WHERE name = 'GRASSLAND';

-- Update existing STEPPE to have clearer requirements
UPDATE ref.terrain_type_ref
SET min_pressure_atm = 0.1,
    min_temperature_k = 250.0,
    max_temperature_k = 320.0,
    description = 'Semi-arid grasslands with sparse vegetation and seasonal grasses'
WHERE name = 'STEPPE';

-- Update existing TUNDRA to have pressure requirement
UPDATE ref.terrain_type_ref
SET min_pressure_atm = 0.15,
    description = 'Cold plains with permafrost, moss, lichen, and low-lying hardy vegetation'
WHERE name = 'TUNDRA';

-- Update existing SALT_FLATS
UPDATE ref.terrain_type_ref
SET requires_water = false,
    requires_atmosphere = false,
    description = 'Vast evaporite plains and dry lake beds with crystalline salt deposits'
WHERE name = 'SALT_FLATS';

-- =====================================================================
-- SECTION 2: Add New Specialized Plains Types
-- =====================================================================

DO $$
    BEGIN
        -- ================================================================
        -- COLD CLIMATE PLAINS
        -- ================================================================

        -- Frozen barren plains (Mars-like, cold moons)
        INSERT INTO ref.terrain_type_ref
        (name, display_name, description, category, requires_water, requires_atmosphere,
         min_temperature_k, max_temperature_k, rarity_weight, typical_coverage_min, typical_coverage_max,
         cratering_weight_boost, volcanic_weight_boost)
        VALUES
            ('CRYOPLAINS', 'Cryogenic Plains',
             'Frozen barren lowlands with frost-covered rocky surfaces and ice deposits',
             'ICE', false, false, 50.0, 200.0, 110, 10.0, 50.0, 0, 0);

        -- Cold but above freezing (transitional)
        INSERT INTO ref.terrain_type_ref
        (name, display_name, description, category, requires_water, requires_atmosphere,
         min_temperature_k, max_temperature_k, min_pressure_atm, rarity_weight, typical_coverage_min, typical_coverage_max,
         cratering_weight_boost, volcanic_weight_boost)
        VALUES
            ('BOREAL_PLAINS', 'Boreal Plains',
             'Cold grassy plains transitioning between tundra and temperate zones',
             'TEMPERATE', false, true, 260.0, 280.0, 0.2, 90, 10.0, 40.0, 0, 0);

        -- ================================================================
        -- BARREN/AIRLESS PLAINS
        -- ================================================================

        -- Regolith-covered plains (Moon/Mercury-like)
        INSERT INTO ref.terrain_type_ref
        (name, display_name, description, category, requires_water, requires_atmosphere,
         min_temperature_k, max_temperature_k, rarity_weight, typical_coverage_min, typical_coverage_max,
         cratering_weight_boost, volcanic_weight_boost)
        VALUES
            ('REGOLITH_FLATS', 'Regolith Flats',
             'Pulverized rocky plains blanketed in fine dust and impact debris',
             'BARREN', false, false, 0.0, 800.0, 140, 10.0, 60.0, 50, 0);

        -- ================================================================
        -- VOLCANIC PLAINS
        -- ================================================================

        -- Basaltic lava plains
        INSERT INTO ref.terrain_type_ref
        (name, display_name, description, category, requires_water, requires_atmosphere,
         min_temperature_k, max_temperature_k, is_volcanic, rarity_weight, typical_coverage_min, typical_coverage_max,
         cratering_weight_boost, volcanic_weight_boost)
        VALUES
            ('LAVA_PLAINS', 'Lava Plains',
             'Vast basaltic plains formed by ancient volcanic flood basalts',
             'VOLCANIC', false, false, 200.0, 600.0, false, 80, 10.0, 50.0, 0, 60);

        -- ================================================================
        -- EXOTIC PLAINS
        -- ================================================================

        -- Carbon-rich plains
        INSERT INTO ref.terrain_type_ref
        (name, display_name, description, category, requires_water, requires_atmosphere,
         min_temperature_k, max_temperature_k, rarity_weight, typical_coverage_min, typical_coverage_max,
         cratering_weight_boost, volcanic_weight_boost)
        VALUES
            ('CARBON_FLATS', 'Carbon Flats',
             'Graphite and silicon carbide plains characteristic of carbon-rich worlds',
             'EXOTIC', false, false, 200.0, 700.0, 40, 5.0, 30.0, 0, 0);

        RAISE NOTICE 'Added 5 new specialized plains terrain types';
        RAISE NOTICE 'Updated 4 existing terrain types (GRASSLAND, STEPPE, TUNDRA, SALT_FLATS)';
    END $$;

-- =====================================================================
-- SECTION 3: Update Filtering Rules for All Plains Types
-- =====================================================================

-- GRASSLAND (now Verdant Grasslands) - STRICT Earth-like requirements
UPDATE ref.terrain_type_ref
SET excluded_planet_types = ARRAY['Iron Planet', 'Hot Rocky Planet', 'Lava Planet', 'Ice World', 'Desert Planet'],
    excluded_composition_classes = ARRAY['IRON_RICH', 'MOLTEN_SURFACE', 'GAS_ENVELOPE', 'ICE_RICH']
WHERE name = 'GRASSLAND';

-- STEPPE - Dry grasslands, needs atmosphere
UPDATE ref.terrain_type_ref
SET excluded_planet_types = ARRAY['Iron Planet', 'Hot Rocky Planet', 'Lava Planet', 'Ice World'],
    excluded_composition_classes = ARRAY['IRON_RICH', 'MOLTEN_SURFACE', 'GAS_ENVELOPE']
WHERE name = 'STEPPE';

-- TUNDRA - Cold climate plains
UPDATE ref.terrain_type_ref
SET excluded_planet_types = ARRAY['Hot Rocky Planet', 'Lava Planet'],
    excluded_composition_classes = ARRAY['MOLTEN_SURFACE']
WHERE name = 'TUNDRA';

-- SALT_FLATS - Evaporite deposits
UPDATE ref.terrain_type_ref
SET excluded_planet_types = ARRAY['Ice World', 'Ice Giant', 'Gas Giant']
WHERE name = 'SALT_FLATS';

-- CRYOPLAINS - Ultra-cold plains
UPDATE ref.terrain_type_ref
SET excluded_planet_types = ARRAY['Hot Rocky Planet', 'Lava Planet'],
    excluded_composition_classes = ARRAY['MOLTEN_SURFACE']
WHERE name = 'CRYOPLAINS';

-- BOREAL_PLAINS - Cool transition zone
UPDATE ref.terrain_type_ref
SET excluded_planet_types = ARRAY['Hot Rocky Planet', 'Lava Planet', 'Ice World'],
    excluded_composition_classes = ARRAY['MOLTEN_SURFACE', 'IRON_RICH']
WHERE name = 'BOREAL_PLAINS';

-- REGOLITH_FLATS - Airless/barren
UPDATE ref.terrain_type_ref
SET excluded_composition_classes = ARRAY['GAS_ENVELOPE']
WHERE name = 'REGOLITH_FLATS';

-- LAVA_PLAINS - Volcanic plains
UPDATE ref.terrain_type_ref
SET excluded_composition_classes = ARRAY['ICE_RICH', 'GAS_ENVELOPE'],
    excluded_planet_types = ARRAY['Ice World']
WHERE name = 'LAVA_PLAINS';

-- CARBON_FLATS - Exotic carbon worlds
UPDATE ref.terrain_type_ref
SET required_composition_classes = ARRAY['CARBON_RICH']
WHERE name = 'CARBON_FLATS';

-- =====================================================================
-- SECTION 4: Create Summary View for Testing
-- =====================================================================

CREATE OR REPLACE VIEW ref.v_plains_terrain_summary AS
SELECT
    name,
    display_name,
    category,
    min_temperature_k,
    max_temperature_k,
    requires_water,
    requires_atmosphere,
    min_pressure_atm,
    excluded_planet_types,
    required_composition_classes,
    excluded_composition_classes,
    rarity_weight
FROM ref.terrain_type_ref
WHERE name IN ('CRYOPLAINS', 'TUNDRA', 'BOREAL_PLAINS', 'GRASSLAND',
               'STEPPE', 'SALT_FLATS', 'REGOLITH_FLATS', 'LAVA_PLAINS', 'CARBON_FLATS')
ORDER BY min_temperature_k NULLS LAST, name;

COMMENT ON VIEW ref.v_plains_terrain_summary IS
    'Summary of all plains/grassland terrain variants for easy comparison';

-- =====================================================================
-- SECTION 5: Verification and Summary
-- =====================================================================

DO $$
    DECLARE
        plains_count INTEGER;
        total_weight INTEGER;
    BEGIN
        SELECT COUNT(*), SUM(rarity_weight)
        INTO plains_count, total_weight
        FROM ref.terrain_type_ref
        WHERE name IN ('CRYOPLAINS', 'TUNDRA', 'BOREAL_PLAINS', 'GRASSLAND',
                       'STEPPE', 'SALT_FLATS', 'REGOLITH_FLATS', 'LAVA_PLAINS', 'CARBON_FLATS');

        RAISE NOTICE 'Migration V27 Summary:';
        RAISE NOTICE '  - Total plains/grassland variants: %', plains_count;
        RAISE NOTICE '  - Combined rarity weight: %', total_weight;
        RAISE NOTICE '  - Created summary view: ref.v_plains_terrain_summary';
    END $$;

-- Display the new variants
SELECT
    display_name,
    category,
    CONCAT(COALESCE(min_temperature_k::text, '0'), '-', COALESCE(max_temperature_k::text, '∞'), 'K') as temp_range,
    CASE
        WHEN requires_water AND requires_atmosphere THEN 'Water + Atm'
        WHEN requires_atmosphere THEN 'Atm only'
        WHEN requires_water THEN 'Water only'
        ELSE 'None'
        END as requirements,
    COALESCE(min_pressure_atm::text, '-') as min_pressure,
    rarity_weight
FROM ref.terrain_type_ref
WHERE name IN ('CRYOPLAINS', 'TUNDRA', 'BOREAL_PLAINS', 'GRASSLAND',
               'STEPPE', 'SALT_FLATS', 'REGOLITH_FLATS', 'LAVA_PLAINS', 'CARBON_FLATS')
ORDER BY min_temperature_k NULLS LAST;

-- =====================================================================
-- SECTION 6: Usage Examples
-- =====================================================================

/*
TERRAIN OVERVIEW:

EXISTING (UPDATED):
- GRASSLAND → "Verdant Grasslands" (275-310K, water + 0.5 atm) - STRICT Earth-like
- STEPPE → "Steppe Grasslands" (250-320K, 0.1 atm) - Semi-arid
- TUNDRA → "Tundra" (240-280K, 0.15 atm) - Cold permafrost
- SALT_FLATS → "Salt Flats" (260-340K) - Evaporites

NEW:
- CRYOPLAINS → "Cryogenic Plains" (50-200K) - Ultra-cold
- BOREAL_PLAINS → "Boreal Plains" (260-280K, 0.2 atm) - Transition zone
- REGOLITH_FLATS → "Regolith Flats" (0-800K) - Airless dusty plains
- LAVA_PLAINS → "Lava Plains" (200-600K) - Basaltic flood plains
- CARBON_FLATS → "Carbon Flats" (200-700K, CARBON_RICH) - Exotic

EXAMPLE PLANET SCENARIOS:

1. Frozen Desert (272K, no water, 0.0073 atm) - Like cize A 3:
   - CRYOPLAINS ✗ (max 200K)
   - TUNDRA ✗ (needs 0.15 atm, too thin)
   - BOREAL_PLAINS ✗ (needs 0.2 atm, too thin)
   - GRASSLAND ✗ (needs water + 0.5 atm)
   - STEPPE ✗ (needs 0.1 atm, too thin)
   - SALT_FLATS ✓ (260-340K, no requirements)
   - REGOLITH_FLATS ✓✓ (0-800K, no requirements) - BEST FIT
   Result: Dusty regolith plains with salt deposits

2. Earth-like (288K, water, 1.0 atm):
   - GRASSLAND ✓✓ (275-310K, water + 0.5 atm) - PERFECT
   - STEPPE ✓ (250-320K, 0.1 atm)
   - BOREAL_PLAINS ✗ (max 280K, too warm)
   Result: Verdant grasslands and prairies

3. Hot Rocky (450K, no atm):
   - LAVA_PLAINS ✓✓ (200-600K) - PERFECT
   - REGOLITH_FLATS ✓ (0-800K)
   Result: Ancient lava flows and impact debris

4. Carbon Planet (350K, CARBON_RICH):
   - CARBON_FLATS ✓✓ (requires CARBON_RICH) - PERFECT
   - LAVA_PLAINS ✓ (if volcanic)
   Result: Graphite plains

5. Ice World (150K):
   - CRYOPLAINS ✓✓ (50-200K) - PERFECT
   - REGOLITH_FLATS ✓ (0-800K)
   Result: Frozen plains

6. Mars-like (240K, 0.006 atm):
   - CRYOPLAINS ✗ (max 200K, too warm)
   - TUNDRA ✗ (needs 0.15 atm, too thin)
   - STEPPE ✗ (needs 0.1 atm, too thin)
   - REGOLITH_FLATS ✓✓ (0-800K) - BEST FIT
   - SALT_FLATS ✓ (260-340K)
   Result: Dusty regolith plains (realistic!)
*/

-- =====================================================================
-- END OF MIGRATION V27
-- =====================================================================