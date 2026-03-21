-- =====================================================================
-- V121: Ocean World Terrain Types & Continental Biome Exclusions
-- =====================================================================
-- Makes ocean world land feel distinctly volcanic/barren rather than
-- continental. Adds ocean-world-specific terrain types and excludes
-- temperate/plains biomes from OCEAN_WORLD composition.
-- =====================================================================

-- =====================================================================
-- SECTION 1: New Ocean-World Terrain Types
-- =====================================================================

INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_liquid, requires_atmosphere,
 min_temperature_k, max_temperature_k, is_volcanic, is_aquatic, rarity_weight,
 typical_coverage_min, typical_coverage_max, cratering_weight_boost, volcanic_weight_boost)
SELECT v.*
FROM (VALUES
    ('VOLCANIC_ISLANDS'::varchar, 'Volcanic Islands'::varchar,
     'Shield volcano islands rising from ocean floor with active or recent eruptions'::text,
     'VOLCANIC'::varchar, true, false, 270::double precision, 373::double precision,
     true, false, 80, 2.0, 20.0, 0, 100),

    ('BASALT_PLATEAU'::varchar, 'Basalt Plateau'::varchar,
     'Elevated basaltic platforms formed by flood volcanism exposed above ocean surface'::text,
     'MOUNTAIN'::varchar, false, false, 100::double precision, 500::double precision,
     false, false, 90, 3.0, 25.0, 0, 50),

    ('BLACK_SAND_SHORES'::varchar, 'Black Sand Shores'::varchar,
     'Dark volcanic sand beaches and littoral zones surrounding volcanic land masses'::text,
     'AQUATIC'::varchar, true, false, 270::double precision, 373::double precision,
     false, true, 60, 1.0, 10.0, 0, 0),

    ('HYDROTHERMAL_VENTS'::varchar, 'Hydrothermal Vent Fields'::varchar,
     'Submarine volcanic vents releasing mineral-rich superheated fluid'::text,
     'VOLCANIC'::varchar, true, false, 270::double precision, 500::double precision,
     true, false, 50, 0.5, 8.0, 0, 120),

    ('SUBMARINE_RIDGE'::varchar, 'Submarine Ridge'::varchar,
     'Mid-ocean volcanic ridges and seamount chains partially breaching the surface'::text,
     'AQUATIC'::varchar, true, false, 100::double precision, 400::double precision,
     false, true, 70, 2.0, 15.0, 0, 60),

    ('VOLCANIC_ARCHIPELAGO'::varchar, 'Volcanic Archipelago'::varchar,
     'Chains of volcanic islands formed by hotspot or subduction activity'::text,
     'VOLCANIC'::varchar, true, true, 270::double precision, 373::double precision,
     true, false, 60, 3.0, 20.0, 0, 80),

    ('BARE_ROCK_COAST'::varchar, 'Bare Rock Coast'::varchar,
     'Exposed rock surfaces and cliff faces battered by waves devoid of soil'::text,
     'BARREN'::varchar, false, false, 100::double precision, 500::double precision,
     false, false, 80, 1.0, 15.0, 0, 0)
) AS v(name, display_name, description, category, requires_liquid, requires_atmosphere,
       min_temperature_k, max_temperature_k, is_volcanic, is_aquatic, rarity_weight,
       typical_coverage_min, typical_coverage_max, cratering_weight_boost, volcanic_weight_boost)
WHERE NOT EXISTS (SELECT 1 FROM ref.terrain_type_ref t WHERE t.name = v.name);

-- =====================================================================
-- SECTION 2: Composition Filtering for New Terrain Types
-- =====================================================================

-- Ocean-world-exclusive terrain types
UPDATE ref.terrain_type_ref
SET required_composition_classes = ARRAY['OCEAN_WORLD']
WHERE name IN ('VOLCANIC_ISLANDS', 'VOLCANIC_ARCHIPELAGO', 'BLACK_SAND_SHORES',
               'HYDROTHERMAL_VENTS', 'SUBMARINE_RIDGE');

-- Basalt plateau and bare rock coast: not ocean-exclusive but exclude
-- incompatible compositions
UPDATE ref.terrain_type_ref
SET excluded_composition_classes = ARRAY['ICE_RICH', 'IRON_RICH', 'GAS_ENVELOPE']
WHERE name IN ('BASALT_PLATEAU', 'BARE_ROCK_COAST');

-- Set volatile type for water-specific aquatic terrains
UPDATE ref.terrain_type_ref
SET volatile_type = 'WATER'
WHERE name IN ('BLACK_SAND_SHORES', 'SUBMARINE_RIDGE')
  AND volatile_type IS NULL;

-- =====================================================================
-- SECTION 3: Exclude Continental Biomes from Ocean Worlds
-- =====================================================================
-- Forests, grasslands, and plains are continental features that shouldn't
-- appear on volcanic ocean-world islands.

-- Exclude TEMPERATE terrains from OCEAN_WORLD composition
UPDATE ref.terrain_type_ref
SET excluded_composition_classes = COALESCE(excluded_composition_classes, ARRAY[]::text[]) || ARRAY['OCEAN_WORLD']
WHERE category = 'TEMPERATE'
  AND NOT ('OCEAN_WORLD' = ANY(COALESCE(excluded_composition_classes, ARRAY[]::text[])));

-- Exclude PLAINS terrains from OCEAN_WORLD composition
UPDATE ref.terrain_type_ref
SET excluded_composition_classes = COALESCE(excluded_composition_classes, ARRAY[]::text[]) || ARRAY['OCEAN_WORLD']
WHERE category = 'PLAINS'
  AND NOT ('OCEAN_WORLD' = ANY(COALESCE(excluded_composition_classes, ARRAY[]::text[])));

-- =====================================================================
-- SECTION 4: Update Existing ISLANDS Terrain
-- =====================================================================
-- Widen temperature range and remove atmosphere requirement for broader
-- applicability across different ocean world types.

UPDATE ref.terrain_type_ref
SET min_temperature_k = 70,
    max_temperature_k = 373,
    requires_atmosphere = false
WHERE name = 'ISLANDS';

-- =====================================================================
-- SECTION 5: Verification
-- =====================================================================

SELECT name, category, display_name, required_composition_classes, excluded_composition_classes
FROM ref.terrain_type_ref
WHERE 'OCEAN_WORLD' = ANY(COALESCE(required_composition_classes, ARRAY[]::text[]))
   OR 'OCEAN_WORLD' = ANY(COALESCE(excluded_composition_classes, ARRAY[]::text[]))
ORDER BY category, name;
