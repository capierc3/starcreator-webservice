-- V20__terrain_type_reference_system.sql
-- Creates terrain type reference system and planet terrain distribution table

-- ============================================================================
-- TERRAIN TYPE REFERENCE TABLE
-- ============================================================================
CREATE TABLE ref.terrain_type_ref (
                                      id SERIAL PRIMARY KEY,
                                      name VARCHAR(100) UNIQUE NOT NULL,
                                      display_name VARCHAR(100) NOT NULL,
                                      description TEXT,
                                      category VARCHAR(50) NOT NULL,

    -- Environmental requirements
                                      requires_water BOOLEAN DEFAULT FALSE,
                                      requires_atmosphere BOOLEAN DEFAULT FALSE,
                                      min_temperature_k DOUBLE PRECISION,
                                      max_temperature_k DOUBLE PRECISION,
                                      min_pressure_atm DOUBLE PRECISION,

    -- Characteristics
                                      is_volcanic BOOLEAN DEFAULT FALSE,
                                      is_frozen BOOLEAN DEFAULT FALSE,
                                      is_aquatic BOOLEAN DEFAULT FALSE,
                                      is_artificial BOOLEAN DEFAULT FALSE,

    -- For procedural generation
                                      rarity_weight INTEGER DEFAULT 100,
                                      typical_coverage_min DOUBLE PRECISION DEFAULT 0.0,
                                      typical_coverage_max DOUBLE PRECISION DEFAULT 100.0,

                                      created_at TIMESTAMP DEFAULT NOW()
);

COMMENT ON TABLE ref.terrain_type_ref IS 'Reference data for terrain types with environmental requirements';
COMMENT ON COLUMN ref.terrain_type_ref.category IS 'Terrain category (AQUATIC, ICE, TEMPERATE, ARID, VOLCANIC, MOUNTAIN, EXOTIC, ARTIFICIAL)';
COMMENT ON COLUMN ref.terrain_type_ref.requires_water IS 'Whether this terrain requires liquid water';
COMMENT ON COLUMN ref.terrain_type_ref.requires_atmosphere IS 'Whether this terrain requires an atmosphere';
COMMENT ON COLUMN ref.terrain_type_ref.rarity_weight IS 'Weight for random selection (higher = more common)';

-- ============================================================================
-- PLANET TERRAIN DISTRIBUTION TABLE
-- ============================================================================
CREATE TABLE ud.planet_terrain_distribution (
                                                id BIGSERIAL PRIMARY KEY,
                                                planet_id BIGINT NOT NULL REFERENCES ud.planet(id) ON DELETE CASCADE,
                                                terrain_type_id INTEGER NOT NULL REFERENCES ref.terrain_type_ref(id),
                                                coverage_percent DOUBLE PRECISION NOT NULL,
                                                description TEXT,
                                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                CONSTRAINT chk_terrain_coverage_range CHECK (coverage_percent >= 0 AND coverage_percent <= 100)
);

COMMENT ON TABLE ud.planet_terrain_distribution IS 'Stores terrain type distribution for planets';
COMMENT ON COLUMN ud.planet_terrain_distribution.coverage_percent IS 'Percentage of planet surface covered by this terrain type';

CREATE INDEX idx_terrain_distribution_planet ON ud.planet_terrain_distribution(planet_id);
CREATE INDEX idx_terrain_distribution_type ON ud.planet_terrain_distribution(terrain_type_id);
CREATE INDEX idx_terrain_type_category ON ref.terrain_type_ref(category);

-- ============================================================================
-- SEED DATA: TERRAIN TYPES
-- ============================================================================

-- AQUATIC TERRAINS
INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, is_aquatic, rarity_weight, typical_coverage_min, typical_coverage_max)
VALUES
    ('OCEAN_DEEP', 'Deep Ocean', 'Ocean deeper than 200m, abyssal zones', 'AQUATIC', true, false, 270, 310, true, 200, 0, 70),
    ('OCEAN_SHALLOW', 'Shallow Ocean', 'Continental shelf, depths less than 200m', 'AQUATIC', true, false, 270, 310, true, 150, 0, 30),
    ('OCEAN_ABYSSAL', 'Abyssal Depths', 'Extremely deep ocean trenches (>4000m)', 'AQUATIC', true, false, 270, 310, true, 80, 0, 10),
    ('COASTAL', 'Coastal Zone', 'Shoreline and littoral zones', 'AQUATIC', true, true, 270, 320, true, 100, 1, 15),
    ('LAKES', 'Lakes', 'Inland freshwater bodies', 'AQUATIC', true, true, 270, 320, true, 120, 1, 10),
    ('RIVERS', 'Rivers and Waterways', 'Flowing freshwater systems', 'AQUATIC', true, true, 270, 320, true, 100, 0.5, 5),
    ('WETLANDS', 'Wetlands and Marshes', 'Swamps, bogs, marshes, and wetlands', 'AQUATIC', true, true, 270, 320, true, 80, 1, 15);

-- ICE/FROZEN TERRAINS
INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, is_frozen, rarity_weight, typical_coverage_min, typical_coverage_max)
VALUES
    ('POLAR_ICE', 'Polar Ice Caps', 'Permanent polar ice coverage', 'ICE', true, false, 0, 273, true, 150, 5, 50),
    ('GLACIER', 'Glaciers', 'Massive ice rivers and sheets', 'ICE', true, false, 0, 273, true, 120, 2, 40),
    ('ICE_SHEET', 'Continental Ice Sheets', 'Continent-spanning ice coverage', 'ICE', true, false, 0, 250, true, 100, 10, 80),
    ('PERMAFROST', 'Permafrost', 'Permanently frozen ground', 'ICE', false, true, 0, 273, true, 110, 5, 60),
    ('TUNDRA', 'Tundra', 'Cold, treeless plains with frozen subsoil', 'ICE', false, true, 240, 280, true, 100, 5, 30),
    ('TAIGA', 'Taiga/Boreal Forest', 'Northern coniferous forests', 'TEMPERATE', false, true, 250, 290, false, 90, 5, 25);

-- TEMPERATE/VEGETATION TERRAINS
INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, rarity_weight, typical_coverage_min, typical_coverage_max)
VALUES
    ('FOREST_TEMPERATE', 'Temperate Forest', 'Deciduous and mixed forests', 'TEMPERATE', true, true, 270, 300, 120, 5, 40),
    ('FOREST_TROPICAL', 'Tropical Rainforest', 'Dense tropical and subtropical forests', 'TEMPERATE', true, true, 290, 320, 100, 5, 35),
    ('GRASSLAND', 'Grassland and Savanna', 'Open grasslands and savannas', 'TEMPERATE', false, true, 270, 310, 150, 10, 50),
    ('SHRUBLAND', 'Shrubland', 'Mediterranean scrub and chaparral', 'TEMPERATE', false, true, 280, 310, 100, 5, 30),
    ('STEPPE', 'Steppe', 'Dry grassland with sparse vegetation', 'ARID', false, true, 260, 310, 90, 5, 35);

-- ARID TERRAINS
INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, rarity_weight, typical_coverage_min, typical_coverage_max)
VALUES
    ('DESERT_SAND', 'Sand Desert', 'Sandy deserts and ergs', 'ARID', false, false, 250, 350, 120, 10, 70),
    ('DESERT_ROCK', 'Rock Desert', 'Rocky badlands and hammada', 'ARID', false, false, 250, 350, 110, 10, 60),
    ('DESERT_ICE', 'Polar Desert', 'Dry polar regions with minimal precipitation', 'ARID', false, true, 200, 270, 80, 5, 40),
    ('SALT_FLATS', 'Salt Flats', 'Dried salt lakebeds and playas', 'ARID', false, true, 260, 340, 70, 1, 15),
    ('DUNES', 'Dune Fields', 'Active sand dune systems', 'ARID', false, false, 250, 350, 90, 2, 30);

-- MOUNTAIN/HIGHLAND TERRAINS
INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, rarity_weight, typical_coverage_min, typical_coverage_max)
VALUES
    ('MOUNTAINS_HIGH', 'High Alpine Mountains', 'High elevation mountain peaks and ranges', 'MOUNTAIN', false, false, 240, 290, 100, 5, 25),
    ('MOUNTAINS_MID', 'Mid-elevation Mountains', 'Moderate elevation mountains and foothills', 'MOUNTAIN', false, false, 260, 300, 120, 10, 35),
    ('HILLS', 'Hills and Highlands', 'Rolling hills and low highlands', 'MOUNTAIN', false, true, 270, 310, 140, 10, 40),
    ('PLATEAU', 'Plateaus', 'Elevated flat tablelands', 'MOUNTAIN', false, false, 250, 310, 90, 5, 25),
    ('CANYON', 'Canyons and Gorges', 'Deep valleys and canyon systems', 'MOUNTAIN', false, false, 250, 330, 80, 1, 15),
    ('KARST', 'Karst Terrain', 'Limestone terrain with caves and sinkholes', 'MOUNTAIN', false, true, 270, 310, 60, 1, 10);

-- PLAINS/LOWLAND TERRAINS
INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, rarity_weight, typical_coverage_min, typical_coverage_max)
VALUES
    ('PLAINS', 'Plains', 'Flat or gently rolling lowlands', 'PLAINS', false, false, 260, 320, 150, 10, 50),
    ('MUD_FLATS', 'Mud Flats', 'Tidal flats and sediment plains', 'PLAINS', true, true, 270, 310, 60, 0.5, 8);

-- VOLCANIC TERRAINS
INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, is_volcanic, rarity_weight, typical_coverage_min, typical_coverage_max)
VALUES
    ('VOLCANIC_ACTIVE', 'Active Volcanic Fields', 'Currently active lava flows and volcanic zones', 'VOLCANIC', false, false, 300, 1500, true, 50, 1, 20),
    ('VOLCANIC_DORMANT', 'Dormant Volcanic Terrain', 'Inactive volcanic features', 'VOLCANIC', false, false, 250, 350, true, 80, 2, 25),
    ('LAVA_TUBES', 'Lava Tubes and Caves', 'Volcanic cave systems', 'VOLCANIC', false, false, 250, 400, true, 40, 0.1, 5),
    ('GEOTHERMAL', 'Geothermal Fields', 'Hot springs, geysers, and thermal features', 'VOLCANIC', false, false, 270, 380, true, 60, 0.5, 10);

-- EXOTIC TERRAINS
INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, rarity_weight, typical_coverage_min, typical_coverage_max)
VALUES
    ('IMPACT_CRATERS', 'Impact Crater Fields', 'Heavy impact cratering', 'EXOTIC', false, false, 0, 1000, 70, 1, 40),
    ('WASTELAND', 'Barren Wasteland', 'Lifeless, barren terrain', 'EXOTIC', false, false, 0, 800, 90, 5, 80),
    ('CRYSTAL_FORMATIONS', 'Crystal Formations', 'Exposed mineral crystal fields', 'EXOTIC', false, false, 200, 600, 30, 1, 15),
    ('METHANE_LAKES', 'Methane Lakes', 'Liquid methane/ethane bodies (Titan-like)', 'EXOTIC', false, false, 70, 100, 20, 5, 40),
    ('SULFUR_PLAINS', 'Sulfur Plains', 'Sulfurous volcanic plains (Io-like)', 'EXOTIC', false, false, 110, 400, 25, 10, 70),
    ('EXOTIC_OTHER', 'Exotic/Unusual Terrain', 'Truly unusual or impossible terrain types', 'EXOTIC', false, false, 0, 2000, 10, 1, 50);

-- ARTIFICIAL TERRAINS
INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, is_artificial, rarity_weight, typical_coverage_min, typical_coverage_max)
VALUES
    ('ARTIFICIAL_TERRAFORMED', 'Terraformed', 'Artificially modified terrain', 'ARTIFICIAL', false, false, true, 5, 1, 100),
    ('ARTIFICIAL_MEGASTRUCTURE', 'Megastructures', 'Cities, arcologies, megastructures', 'ARTIFICIAL', false, false, true, 3, 0.1, 30),
    ('ARTIFICIAL_RUINS', 'Ancient Ruins', 'Archaeological sites and ruins', 'ARTIFICIAL', false, false, true, 8, 0.1, 10);

-- ============================================================================
-- CREATE ISLANDS REFERENCE (SPECIAL CASE - COMBINATION TYPE)
-- ============================================================================
INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, is_aquatic, rarity_weight, typical_coverage_min, typical_coverage_max)
VALUES
    ('ISLANDS', 'Islands', 'Land masses surrounded by water', 'AQUATIC', true, true, 270, 320, true, 90, 1, 20);

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Count terrain types by category
SELECT
    category,
    COUNT(*) as terrain_count
FROM ref.terrain_type_ref
GROUP BY category
ORDER BY category;

-- Total terrain types
SELECT COUNT(*) as total_terrain_types FROM ref.terrain_type_ref;
-- Expected: 47 terrain types

-- Verify migration
SELECT 'V20 Migration Complete - Terrain Reference System Created' as status;