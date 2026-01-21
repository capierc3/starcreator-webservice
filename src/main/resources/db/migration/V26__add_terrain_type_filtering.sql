-- =====================================================================
-- Flyway Migration V22: Add Terrain Type Filtering Columns (CORRECTED)
-- =====================================================================
-- Description: Adds planet type and composition-based filtering to
--              terrain types to prevent geologically impossible terrain
--              assignments (e.g., glaciers on iron planets)
-- Author: Chase
-- Date: 2026-01-21
-- =====================================================================

-- =====================================================================
-- SECTION 1: Add New Filtering Columns
-- =====================================================================

ALTER TABLE ref.terrain_type_ref
    ADD COLUMN excluded_planet_types TEXT[],
    ADD COLUMN required_composition_classes TEXT[],
    ADD COLUMN excluded_composition_classes TEXT[];

-- =====================================================================
-- SECTION 2: Add Column Comments
-- =====================================================================

COMMENT ON COLUMN ref.terrain_type_ref.excluded_planet_types IS
    'Array of planet type names that CANNOT have this terrain (e.g., Iron Planet cannot have glaciers). Note: Gas giants are excluded via code, not database.';

COMMENT ON COLUMN ref.terrain_type_ref.required_composition_classes IS
    'Array of composition classification names required for this terrain (e.g., ICE_RICH for cryovolcanic features). If specified, planet MUST have one of these compositions.';

COMMENT ON COLUMN ref.terrain_type_ref.excluded_composition_classes IS
    'Array of composition classification names that exclude this terrain (e.g., IRON_RICH excludes ice features). More flexible than required_composition_classes.';

-- =====================================================================
-- SECTION 3: Update ICE Category Terrains
-- =====================================================================

-- Regular ice terrains (not cryovolcanic) excluded from metal/molten planets
UPDATE ref.terrain_type_ref
SET excluded_planet_types = ARRAY['Iron Planet', 'Hot Rocky Planet', 'Lava Planet'],
    excluded_composition_classes = ARRAY['IRON_RICH', 'MOLTEN_SURFACE', 'GAS_ENVELOPE']
WHERE category = 'ICE'
  AND name NOT IN ('CRYOVOLCANIC_FIELDS', 'CRYOLAVA_PLAINS', 'ICE_GEYSER_FIELDS');

-- =====================================================================
-- SECTION 4: Update CRYOVOLCANIC Features
-- =====================================================================

-- Cryovolcanic features REQUIRE ice-rich worlds
UPDATE ref.terrain_type_ref
SET required_composition_classes = ARRAY['ICE_RICH', 'OCEAN_WORLD'],
    excluded_planet_types = ARRAY['Hot Rocky Planet', 'Lava Planet', 'Iron Planet']
WHERE name IN ('CRYOVOLCANIC_FIELDS', 'CRYOLAVA_PLAINS', 'ICE_GEYSER_FIELDS');

-- =====================================================================
-- SECTION 5: Update AQUATIC Category Terrains
-- =====================================================================

-- Aquatic features use exclusions (more flexible than requirements)
-- This allows terrestrial planets, super-earths, and ocean worlds to have water
UPDATE ref.terrain_type_ref
SET excluded_composition_classes = ARRAY['IRON_RICH', 'GAS_ENVELOPE', 'MOLTEN_SURFACE'],
    excluded_planet_types = ARRAY['Iron Planet', 'Hot Rocky Planet', 'Lava Planet']
WHERE category = 'AQUATIC';

-- =====================================================================
-- SECTION 6: Update VOLCANIC Category Terrains (Silicate Volcanism)
-- =====================================================================

-- Regular volcanic features (not cryovolcanic) excluded from ice-rich worlds
-- Note: Gas giants already excluded via isGasGiant() code check
UPDATE ref.terrain_type_ref
SET excluded_composition_classes = ARRAY['ICE_RICH'],
    excluded_planet_types = ARRAY['Ice World']
WHERE category = 'VOLCANIC'
  AND is_volcanic = true
  AND name NOT IN ('CRYOVOLCANIC_FIELDS', 'CRYOLAVA_PLAINS', 'ICE_GEYSER_FIELDS');

-- =====================================================================
-- SECTION 7: Update Specific Extreme Terrain Types
-- =====================================================================

-- Scorched plains only on hot, barren worlds
-- Note: Gas giants already excluded via code
UPDATE ref.terrain_type_ref
SET excluded_planet_types = ARRAY['Ice World', 'Ocean Planet'],
    excluded_composition_classes = ARRAY['ICE_RICH', 'OCEAN_WORLD']
WHERE name = 'SCORCHED_PLAINS';

-- Frozen ocean requires water-bearing worlds with cold temperatures
UPDATE ref.terrain_type_ref
SET required_composition_classes = ARRAY['OCEAN_WORLD', 'ICE_RICH'],
    excluded_planet_types = ARRAY['Iron Planet', 'Hot Rocky Planet', 'Lava Planet']
WHERE name = 'FROZEN_OCEAN';

-- Methane/ammonia lakes require ice giant composition
UPDATE ref.terrain_type_ref
SET required_composition_classes = ARRAY['ICE_RICH']
WHERE name IN ('METHANE_LAKES', 'AMMONIA_SEAS');

-- Lava flows/lakes require molten or high-temperature volcanic worlds
UPDATE ref.terrain_type_ref
SET excluded_composition_classes = ARRAY['ICE_RICH'],
    excluded_planet_types = ARRAY['Ice World']
WHERE name IN ('LAVA_FLOWS', 'LAVA_LAKES');

-- =====================================================================
-- SECTION 8: Update ARID Category Logic
-- =====================================================================

-- Arid terrains shouldn't appear on ice-dominated or water-dominated worlds
UPDATE ref.terrain_type_ref
SET excluded_planet_types = ARRAY['Ocean Planet', 'Ice World']
WHERE category = 'ARID'
  AND name IN ('DESERT_SAND', 'DESERT_ROCK');

-- However, allow arid features on cold, dry worlds with minimal water
-- (Mars-like planets can be both cold AND arid)
UPDATE ref.terrain_type_ref
SET excluded_composition_classes = ARRAY['OCEAN_WORLD']
WHERE category = 'ARID'
  AND name IN ('DESERT_SAND', 'DESERT_ROCK');

-- =====================================================================
-- SECTION 9: Verification Queries
-- =====================================================================

-- Display summary of filtering rules
DO $$
    DECLARE
        ice_excluded_count INTEGER;
        aquatic_excluded_count INTEGER;
        cryovolcanic_required_count INTEGER;
        total_filtered INTEGER;
    BEGIN
        SELECT COUNT(*) INTO ice_excluded_count
        FROM ref.terrain_type_ref
        WHERE 'Iron Planet' = ANY(excluded_planet_types)
          AND category = 'ICE';

        SELECT COUNT(*) INTO aquatic_excluded_count
        FROM ref.terrain_type_ref
        WHERE 'IRON_RICH' = ANY(excluded_composition_classes)
          AND category = 'AQUATIC';

        SELECT COUNT(*) INTO cryovolcanic_required_count
        FROM ref.terrain_type_ref
        WHERE 'ICE_RICH' = ANY(required_composition_classes);

        SELECT COUNT(*) INTO total_filtered
        FROM ref.terrain_type_ref
        WHERE excluded_planet_types IS NOT NULL
           OR required_composition_classes IS NOT NULL
           OR excluded_composition_classes IS NOT NULL;

        RAISE NOTICE 'Migration V22 Summary:';
        RAISE NOTICE '  - ICE terrains with Iron Planet exclusion: %', ice_excluded_count;
        RAISE NOTICE '  - AQUATIC terrains with IRON_RICH exclusion: %', aquatic_excluded_count;
        RAISE NOTICE '  - Cryovolcanic terrains requiring ICE_RICH: %', cryovolcanic_required_count;
        RAISE NOTICE '  - Total terrain types with filtering rules: %', total_filtered;
    END $$;

-- Show examples of the filtering rules
SELECT
    name,
    category,
    excluded_planet_types,
    required_composition_classes,
    excluded_composition_classes
FROM ref.terrain_type_ref
WHERE excluded_planet_types IS NOT NULL
   OR required_composition_classes IS NOT NULL
   OR excluded_composition_classes IS NOT NULL
ORDER BY category, name;

-- =====================================================================
-- END OF MIGRATION V22
-- =====================================================================