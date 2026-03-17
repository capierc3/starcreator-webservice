-- V119: Hydrology System
-- Renames water_properties to hydrology_properties, renames water-centric
-- columns to liquid-centric, adds liquid type determination fields,
-- and adds liquid-type-specific terrain types.
--
-- Written to be idempotent (safe to re-run after partial failure).

-- ═══════════════════════════════════════════════════════════════════
--  1. Rename table (skip if already renamed)
-- ═══════════════════════════════════════════════════════════════════

DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables
               WHERE table_schema = 'ud' AND table_name = 'water_properties') THEN
        ALTER TABLE ud.water_properties RENAME TO hydrology_properties;
    END IF;
END $$;

-- ═══════════════════════════════════════════════════════════════════
--  2. Rename existing columns (skip if already renamed)
-- ═══════════════════════════════════════════════════════════════════

DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'ud' AND table_name = 'hydrology_properties'
               AND column_name = 'water_inventory') THEN
        ALTER TABLE ud.hydrology_properties RENAME COLUMN water_inventory TO liquid_inventory;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'ud' AND table_name = 'hydrology_properties'
               AND column_name = 'water_coverage_percent') THEN
        ALTER TABLE ud.hydrology_properties RENAME COLUMN water_coverage_percent TO liquid_coverage_percent;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'ud' AND table_name = 'hydrology_properties'
               AND column_name = 'liquid_water_coverage_percent') THEN
        ALTER TABLE ud.hydrology_properties RENAME COLUMN liquid_water_coverage_percent TO liquid_surface_coverage_percent;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'ud' AND table_name = 'hydrology_properties'
               AND column_name = 'has_subsurface_water') THEN
        ALTER TABLE ud.hydrology_properties RENAME COLUMN has_subsurface_water TO has_subsurface_liquid;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'ud' AND table_name = 'hydrology_properties'
               AND column_name = 'subsurface_water_depth_km') THEN
        ALTER TABLE ud.hydrology_properties RENAME COLUMN subsurface_water_depth_km TO subsurface_liquid_depth_km;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'ud' AND table_name = 'hydrology_properties'
               AND column_name = 'ice_coverage_percent') THEN
        ALTER TABLE ud.hydrology_properties RENAME COLUMN ice_coverage_percent TO water_ice_coverage_percent;
    END IF;
END $$;

-- ═══════════════════════════════════════════════════════════════════
--  3. Add new columns (IF NOT EXISTS)
-- ═══════════════════════════════════════════════════════════════════

ALTER TABLE ud.hydrology_properties ADD COLUMN IF NOT EXISTS liquid_type VARCHAR(30);
ALTER TABLE ud.hydrology_properties ADD COLUMN IF NOT EXISTS liquid_composition VARCHAR(100);
ALTER TABLE ud.hydrology_properties ADD COLUMN IF NOT EXISTS freezing_point_k DOUBLE PRECISION;
ALTER TABLE ud.hydrology_properties ADD COLUMN IF NOT EXISTS boiling_point_k DOUBLE PRECISION;
ALTER TABLE ud.hydrology_properties ADD COLUMN IF NOT EXISTS frozen_liquid_coverage_percent DOUBLE PRECISION;
ALTER TABLE ud.hydrology_properties ADD COLUMN IF NOT EXISTS liquid_color_primary VARCHAR(7);
ALTER TABLE ud.hydrology_properties ADD COLUMN IF NOT EXISTS liquid_color_secondary VARCHAR(7);

-- Backfill: existing ice data is all H2O, copy to frozen_liquid (same substance for water worlds)
UPDATE ud.hydrology_properties SET frozen_liquid_coverage_percent = water_ice_coverage_percent
WHERE frozen_liquid_coverage_percent IS NULL;

-- ═══════════════════════════════════════════════════════════════════
--  4. Rename FK columns on planet, moon
-- ═══════════════════════════════════════════════════════════════════

DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'ud' AND table_name = 'planet'
               AND column_name = 'water_id') THEN
        ALTER TABLE ud.planet RENAME COLUMN water_id TO hydrology_id;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'ud' AND table_name = 'moon'
               AND column_name = 'water_id') THEN
        ALTER TABLE ud.moon RENAME COLUMN water_id TO hydrology_id;
    END IF;
END $$;

-- ═══════════════════════════════════════════════════════════════════
--  5. Terrain type ref: rename requires_water, add liquid_type
-- ═══════════════════════════════════════════════════════════════════

DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'ref' AND table_name = 'terrain_type_ref'
               AND column_name = 'requires_water') THEN
        ALTER TABLE ref.terrain_type_ref RENAME COLUMN requires_water TO requires_liquid;
    END IF;
END $$;

ALTER TABLE ref.terrain_type_ref ADD COLUMN IF NOT EXISTS liquid_type VARCHAR(30);

-- Tag existing liquid-specific exotic terrains
UPDATE ref.terrain_type_ref SET liquid_type = 'METHANE' WHERE name = 'METHANE_LAKES' AND liquid_type IS NULL;
UPDATE ref.terrain_type_ref SET liquid_type = 'METHANE_ETHANE' WHERE name = 'HYDROCARBON_SEAS' AND liquid_type IS NULL;
UPDATE ref.terrain_type_ref SET liquid_type = 'AMMONIA' WHERE name = 'AMMONIA_SEAS' AND liquid_type IS NULL;

-- Tag existing AQUATIC terrains as WATER-specific
UPDATE ref.terrain_type_ref SET liquid_type = 'WATER'
WHERE category = 'AQUATIC' AND liquid_type IS NULL;

-- ═══════════════════════════════════════════════════════════════════
--  6. Add new liquid-type-specific AQUATIC terrain types
-- ═══════════════════════════════════════════════════════════════════

INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_liquid, requires_atmosphere,
 min_temperature_k, max_temperature_k, is_aquatic, rarity_weight,
 typical_coverage_min, typical_coverage_max, cratering_weight_boost, volcanic_weight_boost, liquid_type)
SELECT *
FROM (VALUES
    ('METHANE_SEA', 'Methane Sea', 'Vast methane-ethane sea covering large surface areas', 'AQUATIC',
     true, false, 70::integer, 112::integer, true, 80::integer, 10::integer, 60::integer, 0::integer, 0::integer, 'METHANE'),
    ('METHANE_COAST', 'Methane Coastline', 'Shoreline between methane seas and solid terrain', 'AQUATIC',
     true, false, 70, 112, true, 50, 2, 15, 0, 0, 'METHANE'),
    ('METHANE_RIVER', 'Methane River', 'Flowing methane channels carving through icy terrain', 'AQUATIC',
     true, true, 80, 112, true, 40, 1, 8, 0, 0, 'METHANE'),
    ('AMMONIA_OCEAN', 'Ammonia Ocean', 'Deep ammonia or ammonia-water ocean basin', 'AQUATIC',
     true, false, 195, 280, true, 70, 15, 70, 0, 0, 'AMMONIA_WATER'),
    ('AMMONIA_LAKE', 'Ammonia Lake', 'Ammonia-water lakes pooled in low-lying terrain', 'AQUATIC',
     true, false, 195, 280, true, 50, 2, 20, 0, 0, 'AMMONIA_WATER'),
    ('AMMONIA_WETLAND', 'Ammonia Wetland', 'Saturated terrain with ammonia-water seepage and frost', 'AQUATIC',
     true, true, 195, 280, true, 35, 1, 10, 0, 0, 'AMMONIA_WATER')
) AS v(name, display_name, description, category, requires_liquid, requires_atmosphere,
       min_temperature_k, max_temperature_k, is_aquatic, rarity_weight,
       typical_coverage_min, typical_coverage_max, cratering_weight_boost, volcanic_weight_boost, liquid_type)
WHERE NOT EXISTS (SELECT 1 FROM ref.terrain_type_ref t WHERE t.name = v.name);
