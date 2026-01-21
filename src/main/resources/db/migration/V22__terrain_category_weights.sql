-- V22__terrain_category_weights.sql

-- Create terrain category reference table
CREATE TABLE ref.terrain_category_ref (
                                          id SERIAL PRIMARY KEY,
                                          category VARCHAR(50) UNIQUE NOT NULL,
                                          display_name VARCHAR(100) NOT NULL,
                                          description TEXT,

    -- Weight controls how much of planet surface this category should occupy
                                          base_weight INTEGER NOT NULL DEFAULT 100,

    -- Typical coverage ranges for this entire category
                                          typical_min_coverage DOUBLE PRECISION DEFAULT 0.0,
                                          typical_max_coverage DOUBLE PRECISION DEFAULT 100.0,

    -- Flags
                                          is_major_terrain BOOLEAN DEFAULT TRUE,  -- Major terrains can dominate landscapes
                                          is_rare BOOLEAN DEFAULT FALSE,          -- Rare terrains appear in small amounts

                                          created_at TIMESTAMP DEFAULT NOW()
);

COMMENT ON TABLE ref.terrain_category_ref IS 'Controls how terrain categories are distributed across planet surfaces';
COMMENT ON COLUMN ref.terrain_category_ref.base_weight IS 'Higher weight = more likely to be selected and allocated more surface area';
COMMENT ON COLUMN ref.terrain_category_ref.is_major_terrain IS 'Major terrains (plains, mountains) can cover large percentages';
COMMENT ON COLUMN ref.terrain_category_ref.is_rare IS 'Rare terrains (exotic, artificial) appear in small amounts';

-- Seed category data
INSERT INTO ref.terrain_category_ref
(category, display_name, description, base_weight, typical_min_coverage, typical_max_coverage, is_major_terrain, is_rare)
VALUES
-- Major terrain categories - can dominate landscapes
('PLAINS', 'Plains and Lowlands', 'Flat or gently rolling terrain - common on most worlds', 200, 10.0, 60.0, true, false),
('MOUNTAIN', 'Mountains and Highlands', 'Elevated terrain and mountain ranges - common on geologically active worlds', 150, 5.0, 40.0, true, false),
('AQUATIC', 'Aquatic Features', 'Oceans, lakes, and water bodies - depends on water coverage', 180, 0.0, 80.0, true, false),
('ICE', 'Ice and Frozen', 'Glaciers, ice sheets, and frozen terrain - common on cold worlds', 140, 5.0, 80.0, true, false),
('ARID', 'Arid and Desert', 'Deserts and dry regions - common on low-water worlds', 130, 10.0, 70.0, true, false),
('TEMPERATE', 'Temperate Biomes', 'Forests, grasslands, and moderate climates - requires specific conditions', 120, 10.0, 60.0, true, false),

-- Secondary terrain categories - moderate coverage
('VOLCANIC', 'Volcanic Features', 'Lava flows, volcanic fields - appears on geologically active worlds', 80, 1.0, 30.0, false, false),

-- Rare terrain categories - small coverage
('EXOTIC', 'Exotic Features', 'Unusual geological features - rare and unique', 40, 0.5, 20.0, false, true),
('ARTIFICIAL', 'Artificial Features', 'Signs of civilization - extremely rare', 5, 0.1, 5.0, false, true);

-- Create index for faster queries
CREATE INDEX idx_terrain_category_major ON ref.terrain_category_ref(is_major_terrain);
CREATE INDEX idx_terrain_category_rare ON ref.terrain_category_ref(is_rare);

-- Show summary
SELECT
    category,
    display_name,
    base_weight,
    is_major_terrain,
    is_rare,
    typical_min_coverage || '% - ' || typical_max_coverage || '%' as coverage_range
FROM ref.terrain_category_ref
ORDER BY base_weight DESC;