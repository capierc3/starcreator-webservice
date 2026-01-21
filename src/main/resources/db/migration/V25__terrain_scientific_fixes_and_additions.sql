-- V25__terrain_scientific_fixes_and_additions.sql
-- Scientific accuracy fixes and new terrain types

-- ============================================================================
-- PART 1: TEMPERATURE CONSTRAINT FIXES
-- ============================================================================

-- PLAINS: Requires some geological processing, not ultra-cold
UPDATE ref.terrain_type_ref SET min_temperature_k = 100 WHERE name = 'PLAINS';

-- MUD_FLATS: Requires liquid water to form, needs warmer temps
UPDATE ref.terrain_type_ref SET min_temperature_k = 273 WHERE name = 'MUD_FLATS';

-- WETLANDS: Requires liquid water
UPDATE ref.terrain_type_ref SET min_temperature_k = 273 WHERE name = 'WETLANDS';

-- HILLS: Can exist anywhere with solid surface, remove atmosphere requirement
UPDATE ref.terrain_type_ref SET requires_atmosphere = false, min_temperature_k = NULL, max_temperature_k = NULL WHERE name = 'HILLS';

-- KARST: Requires water erosion of limestone, needs liquid water temps
UPDATE ref.terrain_type_ref SET min_temperature_k = 273, requires_water = true WHERE name = 'KARST';

-- SALT_FLATS: Form from evaporated water, need temps where water can exist
UPDATE ref.terrain_type_ref SET min_temperature_k = 273, requires_water = true WHERE name = 'SALT_FLATS';

-- DUNES: Can exist on cold worlds (Mars has dunes), but need atmosphere for wind
UPDATE ref.terrain_type_ref SET requires_atmosphere = true, min_temperature_k = 50 WHERE name = 'DUNES';

-- DESERT_SAND: Needs atmosphere for wind erosion to create sand
UPDATE ref.terrain_type_ref SET requires_atmosphere = true, min_temperature_k = 150 WHERE name = 'DESERT_SAND';

-- DESERT_ROCK: Can exist without atmosphere (just exposed rock)
UPDATE ref.terrain_type_ref SET requires_atmosphere = false, min_temperature_k = 100 WHERE name = 'DESERT_ROCK';

-- STEPPE: Requires vegetation, needs atmosphere and moderate temps
UPDATE ref.terrain_type_ref SET requires_atmosphere = true, min_temperature_k = 260 WHERE name = 'STEPPE';

-- ============================================================================
-- PART 2: AQUATIC TERRAIN FIXES
-- ============================================================================

UPDATE ref.terrain_type_ref SET min_temperature_k = 273, max_temperature_k = 373 WHERE name = 'OCEAN_DEEP';
UPDATE ref.terrain_type_ref SET min_temperature_k = 273, max_temperature_k = 373 WHERE name = 'OCEAN_SHALLOW';
UPDATE ref.terrain_type_ref SET min_temperature_k = 273, max_temperature_k = 373 WHERE name = 'OCEAN_ABYSSAL';
UPDATE ref.terrain_type_ref SET min_temperature_k = 273, max_temperature_k = 323 WHERE name = 'COASTAL';
UPDATE ref.terrain_type_ref SET min_temperature_k = 273, max_temperature_k = 323 WHERE name = 'LAKES';
UPDATE ref.terrain_type_ref SET min_temperature_k = 273, max_temperature_k = 323 WHERE name = 'RIVERS';

-- ============================================================================
-- PART 3: ICE TERRAIN FIXES
-- ============================================================================

UPDATE ref.terrain_type_ref SET requires_water = false WHERE name IN ('POLAR_ICE', 'GLACIER', 'ICE_SHEET', 'TUNDRA');

-- ============================================================================
-- PART 4: MOUNTAIN TERRAIN - Remove temp constraints
-- ============================================================================

UPDATE ref.terrain_type_ref SET min_temperature_k = NULL, max_temperature_k = NULL WHERE name IN ('MOUNTAINS_HIGH', 'MOUNTAINS_MID', 'PLATEAU', 'CANYON');

-- ============================================================================
-- PART 5: VOLCANIC TERRAIN FIXES
-- ============================================================================

UPDATE ref.terrain_type_ref SET min_temperature_k = 200 WHERE name = 'GEOTHERMAL';
UPDATE ref.terrain_type_ref SET category = 'VOLCANIC', volcanic_weight_boost = 100 WHERE name = 'SULFUR_PLAINS';

-- ============================================================================
-- PART 6: NEW BARREN TERRAIN TYPES
-- ============================================================================

INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, is_frozen, rarity_weight, typical_coverage_min, typical_coverage_max, cratering_weight_boost, volcanic_weight_boost)
VALUES
    ('LUNAR_MARE', 'Lunar Mare', 'Dark basaltic plains from ancient volcanic flooding', 'BARREN', false, false, 0, 1500, false, 100, 10, 50, 50, 0),
    ('RILLES', 'Rilles and Channels', 'Collapsed lava tube channels and sinuous valleys', 'BARREN', false, false, 0, 1000, false, 60, 1, 10, 30, 0),
    ('EJECTA_FIELDS', 'Ejecta Fields', 'Debris fields surrounding major impact craters', 'BARREN', false, false, 0, 1500, false, 80, 2, 20, 120, 0),
    ('PEDESTAL_CRATERS', 'Pedestal Craters', 'Elevated impact craters from differential erosion', 'BARREN', false, true, 150, 400, false, 40, 1, 10, 80, 0);

-- ============================================================================
-- PART 7: NEW EXOTIC TERRAIN TYPES
-- ============================================================================

INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, is_frozen, rarity_weight, typical_coverage_min, typical_coverage_max, cratering_weight_boost, volcanic_weight_boost)
VALUES
    ('AMMONIA_SEAS', 'Ammonia Seas', 'Liquid ammonia bodies on cold worlds', 'EXOTIC', false, false, 195, 240, false, 25, 5, 40, 0, 0),
    ('TAR_PITS', 'Tar Pits and Hydrocarbon Bogs', 'Thick hydrocarbon deposits and tar lakes', 'EXOTIC', false, true, 90, 150, false, 20, 1, 15, 0, 0),
    ('BANDED_TERRAIN', 'Banded Terrain', 'Linear ridges and grooves from tidal flexing', 'EXOTIC', false, false, 50, 200, true, 35, 5, 30, 0, 0),
    ('CHAOTIC_TERRAIN', 'Chaotic Terrain', 'Disrupted and jumbled surface blocks from subsurface activity', 'EXOTIC', false, false, 50, 250, true, 40, 3, 20, 0, 0),
    ('METAL_FROST', 'Metal Frost', 'Condensed metallic deposits on cooler regions', 'EXOTIC', false, false, 400, 800, false, 20, 1, 15, 0, 0),
    ('TESSERA', 'Tessera Terrain', 'Highly deformed and folded crustal terrain', 'EXOTIC', false, true, 400, 800, false, 35, 5, 25, 0, 0);

-- ============================================================================
-- PART 8: NEW VOLCANIC TERRAIN TYPES
-- ============================================================================

INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, is_volcanic, rarity_weight, typical_coverage_min, typical_coverage_max, cratering_weight_boost, volcanic_weight_boost)
VALUES
    ('OBSIDIAN_FLOWS', 'Obsidian Flows', 'Volcanic glass formations from rapid lava cooling', 'VOLCANIC', false, false, 200, 800, true, 40, 1, 12, 0, 70),
    ('FUMAROLE_FIELDS', 'Fumarole Fields', 'Dense concentrations of volcanic gas vents', 'VOLCANIC', false, false, 150, 600, true, 50, 0.5, 8, 0, 90),
    ('LAVA_LAKES', 'Lava Lakes', 'Persistent pools of molten rock', 'VOLCANIC', false, false, 800, 1500, true, 30, 0.5, 10, 0, 150),
    ('MAGMA_OCEAN', 'Magma Ocean', 'Planet-spanning molten rock surface', 'VOLCANIC', false, false, 1200, 3000, true, 15, 20, 90, 0, 200);

-- ============================================================================
-- PART 9: NEW ICE TERRAIN TYPE
-- ============================================================================

INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, is_frozen, rarity_weight, typical_coverage_min, typical_coverage_max, cratering_weight_boost, volcanic_weight_boost)
VALUES
    ('PENITENTES', 'Penitentes Fields', 'Tall blade-like ice formations from sublimation', 'ICE', false, false, 150, 273, true, 30, 1, 10, 0, 0);

-- ============================================================================
-- PART 10: NEW ARID TERRAIN TYPES
-- ============================================================================

INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, is_frozen, rarity_weight, typical_coverage_min, typical_coverage_max, cratering_weight_boost, volcanic_weight_boost)
VALUES
    ('YARDANGS', 'Yardang Fields', 'Streamlined wind-eroded rock formations', 'ARID', false, true, 150, 400, false, 50, 2, 15, 0, 0),
    ('BADLANDS', 'Badlands', 'Heavily eroded terrain with dramatic formations', 'ARID', false, true, 240, 330, false, 80, 3, 20, 0, 0),
    ('OASIS', 'Oases', 'Isolated water sources in arid regions', 'ARID', true, true, 280, 330, false, 30, 0.1, 3, 0, 0);

-- ============================================================================
-- PART 11: NEW MOUNTAIN TERRAIN TYPE
-- ============================================================================

INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, is_frozen, rarity_weight, typical_coverage_min, typical_coverage_max, cratering_weight_boost, volcanic_weight_boost)
VALUES
    ('MESAS', 'Mesas and Buttes', 'Flat-topped erosional remnants', 'MOUNTAIN', false, false, 200, 350, false, 70, 2, 15, 0, 0);

-- ============================================================================
-- PART 12: NEW TEMPERATE TERRAIN TYPES
-- ============================================================================

INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, is_frozen, rarity_weight, typical_coverage_min, typical_coverage_max, cratering_weight_boost, volcanic_weight_boost)
VALUES
    ('TIDAL_FORESTS', 'Tidal Forests', 'Coastal forests adapted to tidal flooding', 'TEMPERATE', true, true, 285, 310, false, 40, 1, 10, 0, 0),
    ('ALPINE_MEADOW', 'Alpine Meadows', 'High-altitude grasslands above treeline', 'TEMPERATE', false, true, 260, 290, false, 70, 2, 15, 0, 0);

-- ============================================================================
-- PART 13: NEW AQUATIC TERRAIN TYPES
-- ============================================================================

INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, is_aquatic, rarity_weight, typical_coverage_min, typical_coverage_max, cratering_weight_boost, volcanic_weight_boost)
VALUES
    ('CORAL_REEFS', 'Reef Systems', 'Shallow water carbonate structures', 'AQUATIC', true, false, 285, 305, true, 60, 0.5, 8, 0, 0),
    ('RIVER_DELTA', 'River Deltas', 'Sediment deposits at river mouths', 'AQUATIC', true, true, 273, 320, true, 70, 0.5, 5, 0, 0),
    ('FJORDS', 'Fjords', 'Steep-walled glacially carved coastal inlets', 'AQUATIC', true, true, 260, 290, true, 50, 1, 8, 0, 0);

-- ============================================================================
-- VERIFICATION
-- ============================================================================

SELECT category, COUNT(*) as count
FROM ref.terrain_type_ref
GROUP BY category
ORDER BY count DESC;