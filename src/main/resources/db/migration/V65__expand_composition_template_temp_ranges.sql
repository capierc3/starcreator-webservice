-- V69__expand_composition_template_temp_ranges.sql
-- Fix: Planet types with narrow temp ranges fall through to wrong templates
-- causing impossible compositions (e.g. IRON_RICH Mini-Neptunes)

-- ============================================================================
-- EXPAND EXISTING TEMPLATE TEMP RANGES
-- ============================================================================

-- Mini-Neptune: expand from 50-150K to 20-300K
-- Below 50K: still ice-rock core, just colder. Above 150K: ice starts sublimating
-- but core composition stays mixed silicate-ice up to ~300K
UPDATE ref.composition_template
SET min_surface_temp_k = 20, max_surface_temp_k = 300
WHERE name = 'Mini-Neptune - Ice-Rock Core';

-- Sub-Neptune: expand from 40-120K to 20-200K
-- Same ice-rich composition holds across wider temp range
UPDATE ref.composition_template
SET min_surface_temp_k = 20, max_surface_temp_k = 200
WHERE name = 'Sub-Neptune - Ice Giant';

-- Ice Giant: expand from 40-100K to 20-200K
UPDATE ref.composition_template
SET min_surface_temp_k = 20, max_surface_temp_k = 200
WHERE name = 'Ice Giant - Neptune-like';

-- Ice World: expand from 30-150K to 10-250K
-- Ice worlds can exist warmer (sublimating surface) or colder
UPDATE ref.composition_template
SET min_surface_temp_k = 10, max_surface_temp_k = 250
WHERE name = 'Ice World - Frozen Volatiles';

-- Dwarf Planet: expand from 30-150K to 10-250K
UPDATE ref.composition_template
SET min_surface_temp_k = 10, max_surface_temp_k = 250
WHERE name = 'Dwarf - Ice-Rock Mix';

-- ============================================================================
-- ADD WARM MINI-NEPTUNE VARIANT (300-600K)
-- ============================================================================
-- Close-in Mini-Neptunes get heated — ice sublimates, H/He envelope thins,
-- leaving a volatile-depleted but still gaseous composition

INSERT INTO ref.composition_template
(name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES
    ('Mini-Neptune - Warm Volatile-Poor', 'Close-in Mini-Neptune with partially evaporated ices',
     'GAS_ENVELOPE', 'Mini-Neptune', 150, 300, 800);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35, 50 FROM ref.composition_template WHERE name = 'Mini-Neptune - Warm Volatile-Poor'
UNION ALL
SELECT id, 'PYROXENE', 'INTERIOR', 20, 30 FROM ref.composition_template WHERE name = 'Mini-Neptune - Warm Volatile-Poor'
UNION ALL
SELECT id, 'IRON', 'INTERIOR', 15, 25 FROM ref.composition_template WHERE name = 'Mini-Neptune - Warm Volatile-Poor'
UNION ALL
SELECT id, 'HYDROGEN_HELIUM_MIX', 'ENVELOPE', 70, 85 FROM ref.composition_template WHERE name = 'Mini-Neptune - Warm Volatile-Poor'
UNION ALL
SELECT id, 'WATER_ICE', 'ENVELOPE', 10, 25 FROM ref.composition_template WHERE name = 'Mini-Neptune - Warm Volatile-Poor';

-- ============================================================================
-- ADD WARM SUB-NEPTUNE VARIANT (200-600K)
-- ============================================================================

INSERT INTO ref.composition_template
(name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES
    ('Sub-Neptune - Warm Transitional', 'Warm Sub-Neptune with reduced ice fraction',
     'MIXED_SILICATE_ICE', 'Sub-Neptune', 120, 200, 600);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'WATER_ICE', 'INTERIOR', 25, 40 FROM ref.composition_template WHERE name = 'Sub-Neptune - Warm Transitional'
UNION ALL
SELECT id, 'OLIVINE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Sub-Neptune - Warm Transitional'
UNION ALL
SELECT id, 'PYROXENE', 'INTERIOR', 15, 25 FROM ref.composition_template WHERE name = 'Sub-Neptune - Warm Transitional'
UNION ALL
SELECT id, 'HYDROGEN_HELIUM_MIX', 'ENVELOPE', 80, 95 FROM ref.composition_template WHERE name = 'Sub-Neptune - Warm Transitional'
UNION ALL
SELECT id, 'WATER_ICE', 'ENVELOPE', 5, 15 FROM ref.composition_template WHERE name = 'Sub-Neptune - Warm Transitional';