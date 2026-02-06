-- V48__fix_terrain_composition_requirements.sql
-- Fixes terrain types that can appear on scientifically inappropriate worlds

-- ============================================================================
-- SECTION 1: ICE Category Terrains - Require ice-bearing compositions
-- ============================================================================

-- Penitentes are water ice formations - require ice or ocean worlds
UPDATE ref.terrain_type_ref
SET required_composition_classes = ARRAY['ICE_RICH', 'OCEAN_WORLD', 'MIXED_SILICATE_ICE']
WHERE name = 'PENITENTES';

-- Polar ice caps require water/ice
UPDATE ref.terrain_type_ref
SET required_composition_classes = ARRAY['ICE_RICH', 'OCEAN_WORLD', 'MIXED_SILICATE_ICE']
WHERE name = 'POLAR_ICE';

-- Glaciers require water/ice
UPDATE ref.terrain_type_ref
SET required_composition_classes = ARRAY['ICE_RICH', 'OCEAN_WORLD', 'MIXED_SILICATE_ICE']
WHERE name = 'GLACIER';

-- Ice sheets require water/ice
UPDATE ref.terrain_type_ref
SET required_composition_classes = ARRAY['ICE_RICH', 'OCEAN_WORLD', 'MIXED_SILICATE_ICE']
WHERE name = 'ICE_SHEET';

-- Permafrost requires water/ice (frozen ground water)
UPDATE ref.terrain_type_ref
SET required_composition_classes = ARRAY['ICE_RICH', 'OCEAN_WORLD', 'MIXED_SILICATE_ICE']
WHERE name = 'PERMAFROST';

-- Frozen ocean obviously requires ocean/ice
UPDATE ref.terrain_type_ref
SET required_composition_classes = ARRAY['ICE_RICH', 'OCEAN_WORLD']
WHERE name = 'FROZEN_OCEAN';

-- Icy craters - on ice worlds
UPDATE ref.terrain_type_ref
SET required_composition_classes = ARRAY['ICE_RICH', 'OCEAN_WORLD', 'MIXED_SILICATE_ICE']
WHERE name = 'ICY_CRATERS';

-- ============================================================================
-- SECTION 2: Carbon Planet Terrains - Require CARBON_RICH composition
-- ============================================================================

-- Diamond deposits only on carbon planets
UPDATE ref.terrain_type_ref
SET required_composition_classes = ARRAY['CARBON_RICH']
WHERE name = 'DIAMOND_DEPOSITS';

-- Graphite fields only on carbon planets
UPDATE ref.terrain_type_ref
SET required_composition_classes = ARRAY['CARBON_RICH']
WHERE name = 'GRAPHITE_FIELDS';

-- Carbon flats (if exists) only on carbon planets
UPDATE ref.terrain_type_ref
SET required_composition_classes = ARRAY['CARBON_RICH']
WHERE name = 'CARBON_FLATS';

-- ============================================================================
-- SECTION 3: Verify no ICE terrain on silicate-only hot worlds
-- ============================================================================

-- Add SILICATE_RICH to exclusions for pure ice terrains where it makes sense
-- (Penitentes, glaciers etc. shouldn't form on hot silicate worlds without water)
-- Note: MIXED_SILICATE_ICE is allowed because it HAS ice

-- Actually, the required_composition approach above is better -
-- if required is set, anything NOT in that list is automatically excluded

-- ============================================================================
-- VERIFICATION
-- ============================================================================
SELECT name, category, required_composition_classes, excluded_composition_classes
FROM ref.terrain_type_ref
WHERE category = 'ICE' OR name IN ('DIAMOND_DEPOSITS', 'GRAPHITE_FIELDS', 'CARBON_FLATS')
ORDER BY category, name;