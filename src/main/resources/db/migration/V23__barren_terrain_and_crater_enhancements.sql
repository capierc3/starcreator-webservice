-- V23__barren_terrain_and_crater_enhancements.sql
-- Adds BARREN category for cold/airless worlds and enhances crater-based terrain selection

-- ============================================================================
-- 1. ADD BARREN CATEGORY FOR DEAD/COLD WORLDS
-- ============================================================================
INSERT INTO ref.terrain_category_ref
(category, display_name, description, base_weight, typical_min_coverage, typical_max_coverage, is_major_terrain, is_rare)
VALUES
    ('BARREN', 'Barren Terrain', 'Lifeless rocky/icy terrain for cold or airless worlds', 180, 20.0, 100.0, true, false);

-- ============================================================================
-- 2. MOVE IMPACT_CRATERS AND WASTELAND TO BARREN (make them major terrains)
-- ============================================================================
UPDATE ref.terrain_type_ref
SET category = 'BARREN',
    rarity_weight = 150,
    typical_coverage_min = 5,
    typical_coverage_max = 60
WHERE name = 'IMPACT_CRATERS';

UPDATE ref.terrain_type_ref
SET category = 'BARREN',
    rarity_weight = 180
WHERE name = 'WASTELAND';

-- ============================================================================
-- 3. ADD NEW BARREN TERRAIN TYPES
-- ============================================================================
INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, min_pressure_atm, is_volcanic, is_frozen, is_aquatic, is_artificial, rarity_weight, typical_coverage_min, typical_coverage_max)
VALUES
-- General barren terrains (work at any temp, no water/atmo needed)
('REGOLITH_PLAINS', 'Regolith Plains', 'Flat expanses of loose rocky debris and dust', 'BARREN', false, false, 0, 1500, NULL, false, false, false, false, 200, 20, 70),
('CRATERED_HIGHLANDS', 'Cratered Highlands', 'Elevated terrain heavily pocked with impact craters', 'BARREN', false, false, 0, 1500, NULL, false, false, false, false, 140, 10, 40),
('ROCKY_BADLANDS', 'Rocky Badlands', 'Rugged, eroded rocky terrain', 'BARREN', false, false, 0, 1000, NULL, false, false, false, false, 120, 5, 35),

-- Frozen barren (for very cold bodies like your dwarf planet at 21K)
('FROZEN_REGOLITH', 'Frozen Regolith', 'Ice-bound rocky debris plains on extremely cold bodies', 'BARREN', false, false, 0, 120, NULL, false, true, false, false, 160, 15, 60),
('ICY_CRATERS', 'Icy Impact Basins', 'Large impact craters with frozen volatiles', 'BARREN', false, false, 0, 150, NULL, false, true, false, false, 100, 5, 30),
('NITROGEN_ICE_FIELDS', 'Nitrogen Ice Fields', 'Expanses of frozen nitrogen (Pluto-like)', 'BARREN', false, false, 20, 60, NULL, false, true, false, false, 80, 10, 40),

-- Hot barren (for Mercury-like worlds)
('SCORCHED_PLAINS', 'Scorched Plains', 'Heat-blasted flat terrain', 'BARREN', false, false, 400, 1500, NULL, false, false, false, false, 100, 10, 50),
('THERMAL_FRACTURES', 'Thermal Fracture Zones', 'Terrain cracked by extreme temperature cycling', 'BARREN', false, false, 100, 800, NULL, false, false, false, false, 80, 5, 20);

-- ============================================================================
-- 4. REMOVE TEMPERATURE CONSTRAINTS FROM GENERIC PLAINS/MOUNTAINS
-- (These should be able to appear on any rocky body)
-- ============================================================================
UPDATE ref.terrain_type_ref
SET min_temperature_k = NULL, max_temperature_k = NULL
WHERE name = 'PLAINS';

UPDATE ref.terrain_type_ref
SET min_temperature_k = NULL, max_temperature_k = NULL
WHERE name IN ('MOUNTAINS_HIGH', 'MOUNTAINS_MID', 'PLATEAU', 'CANYON', 'HILLS');

-- ============================================================================
-- 5. FIX ICE TERRAINS - Remove water requirement for some
-- (Frozen bodies can have ice without liquid water)
-- ============================================================================
UPDATE ref.terrain_type_ref
SET requires_water = false
WHERE name IN ('PERMAFROST', 'ICE_SHEET');

-- ============================================================================
-- 6. ADD CRATERING-WEIGHT FIELD TO terrain_type_ref FOR CRATER TERRAIN BOOST
-- ============================================================================
ALTER TABLE ref.terrain_type_ref
    ADD COLUMN IF NOT EXISTS cratering_weight_boost INTEGER DEFAULT 0;

COMMENT ON COLUMN ref.terrain_type_ref.cratering_weight_boost IS
    'Additional weight when planet has heavy cratering (added to rarity_weight)';

-- Set boost for crater-related terrains
UPDATE ref.terrain_type_ref SET cratering_weight_boost = 100 WHERE name = 'IMPACT_CRATERS';
UPDATE ref.terrain_type_ref SET cratering_weight_boost = 80 WHERE name = 'CRATERED_HIGHLANDS';
UPDATE ref.terrain_type_ref SET cratering_weight_boost = 50 WHERE name = 'ICY_CRATERS';
UPDATE ref.terrain_type_ref SET cratering_weight_boost = 30 WHERE name = 'REGOLITH_PLAINS';

-- ============================================================================
-- 7. ADD VOLCANISM-WEIGHT FIELD FOR VOLCANIC TERRAIN BOOST
-- ============================================================================
ALTER TABLE ref.terrain_type_ref
    ADD COLUMN IF NOT EXISTS volcanic_weight_boost INTEGER DEFAULT 0;

COMMENT ON COLUMN ref.terrain_type_ref.volcanic_weight_boost IS
    'Additional weight when planet has volcanic activity (added to rarity_weight)';

UPDATE ref.terrain_type_ref SET volcanic_weight_boost = 150 WHERE name = 'VOLCANIC_ACTIVE';
UPDATE ref.terrain_type_ref SET volcanic_weight_boost = 100 WHERE name = 'VOLCANIC_DORMANT';
UPDATE ref.terrain_type_ref SET volcanic_weight_boost = 80 WHERE name = 'GEOTHERMAL';
UPDATE ref.terrain_type_ref SET volcanic_weight_boost = 60 WHERE name = 'LAVA_TUBES';
UPDATE ref.terrain_type_ref SET volcanic_weight_boost = 50 WHERE name = 'SULFUR_PLAINS';

-- ============================================================================
-- VERIFICATION
-- ============================================================================
SELECT category, COUNT(*) as count FROM ref.terrain_type_ref GROUP BY category ORDER BY category;
SELECT 'V23 Migration Complete - Barren terrain and crater enhancements added' as status;