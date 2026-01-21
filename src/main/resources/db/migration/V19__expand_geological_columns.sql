-- V19__expand_geological_columns.sql
-- Expands geological data storage from JSON to proper columns

-- Drop the JSON column
ALTER TABLE ud.planet DROP COLUMN IF EXISTS geological_activity_json;

-- Add detailed geological columns
ALTER TABLE ud.planet
    ADD COLUMN activity_score DOUBLE PRECISION,
    ADD COLUMN has_plate_tectonics BOOLEAN,
    ADD COLUMN number_of_tectonic_plates INTEGER,
    ADD COLUMN tectonic_activity_level VARCHAR(50),
    ADD COLUMN has_volcanic_activity BOOLEAN,
    ADD COLUMN volcanism_type VARCHAR(50),
    ADD COLUMN estimated_active_volcanoes INTEGER,
    ADD COLUMN volcanic_intensity VARCHAR(50),
    ADD COLUMN mountain_coverage_percent DOUBLE PRECISION,
    ADD COLUMN average_elevation_km DOUBLE PRECISION,
    ADD COLUMN max_elevation_km DOUBLE PRECISION,
    ADD COLUMN min_elevation_km DOUBLE PRECISION,
    ADD COLUMN terrain_roughness DOUBLE PRECISION,
    ADD COLUMN cratering_level VARCHAR(50),
    ADD COLUMN estimated_visible_craters INTEGER,
    ADD COLUMN erosion_level VARCHAR(50),
    ADD COLUMN primary_erosion_agent VARCHAR(50),
    ADD COLUMN has_great_storm BOOLEAN,
    ADD COLUMN number_of_major_storms INTEGER,
    ADD COLUMN atmospheric_convection_level VARCHAR(50);

-- Add comments
COMMENT ON COLUMN ud.planet.activity_score IS 'Geological activity score (mass/age ratio)';
COMMENT ON COLUMN ud.planet.has_plate_tectonics IS 'Whether planet has active plate tectonics';
COMMENT ON COLUMN ud.planet.number_of_tectonic_plates IS 'Number of tectonic plates (if applicable)';
COMMENT ON COLUMN ud.planet.tectonic_activity_level IS 'Level of tectonic activity (None, Stagnant Lid, Active, Hyperactive)';
COMMENT ON COLUMN ud.planet.has_volcanic_activity IS 'Whether planet has active volcanism';
COMMENT ON COLUMN ud.planet.volcanism_type IS 'Type of volcanism (Silicate, Cryovolcanic, Atmospheric, None)';
COMMENT ON COLUMN ud.planet.estimated_active_volcanoes IS 'Estimated number of currently active volcanoes';
COMMENT ON COLUMN ud.planet.volcanic_intensity IS 'Volcanic activity intensity (None, Rare, Moderate, Frequent, Continuous)';
COMMENT ON COLUMN ud.planet.mountain_coverage_percent IS 'Percentage of surface covered by significant mountains';
COMMENT ON COLUMN ud.planet.average_elevation_km IS 'Average surface elevation in kilometers';
COMMENT ON COLUMN ud.planet.max_elevation_km IS 'Maximum elevation (highest mountain) in kilometers';
COMMENT ON COLUMN ud.planet.min_elevation_km IS 'Minimum elevation (deepest trench/depression) in kilometers';
COMMENT ON COLUMN ud.planet.terrain_roughness IS 'Terrain roughness scale (0-10, smooth to extremely rough)';
COMMENT ON COLUMN ud.planet.cratering_level IS 'Impact crater coverage (Pristine, Light, Moderate, Heavy, Saturated)';
COMMENT ON COLUMN ud.planet.estimated_visible_craters IS 'Estimated number of visible impact craters';
COMMENT ON COLUMN ud.planet.erosion_level IS 'Level of surface erosion (None, Minimal, Moderate, Heavy, Extreme)';
COMMENT ON COLUMN ud.planet.primary_erosion_agent IS 'Primary erosion mechanism (None, Wind, Water, Ice, Chemical, Volcanic, Lava)';
COMMENT ON COLUMN ud.planet.has_great_storm IS 'Whether planet has a persistent great storm (gas giants)';
COMMENT ON COLUMN ud.planet.number_of_major_storms IS 'Number of major storm systems (gas giants)';
COMMENT ON COLUMN ud.planet.atmospheric_convection_level IS 'Atmospheric convection intensity (gas giants: Minimal, Moderate, Vigorous, Extreme)';

-- Add check constraints
ALTER TABLE ud.planet
    ADD CONSTRAINT chk_mountain_coverage_range CHECK (mountain_coverage_percent IS NULL OR (mountain_coverage_percent >= 0 AND mountain_coverage_percent <= 100)),
    ADD CONSTRAINT chk_terrain_roughness_range CHECK (terrain_roughness IS NULL OR (terrain_roughness >= 0 AND terrain_roughness <= 10)),
    ADD CONSTRAINT chk_tectonic_plates_positive CHECK (number_of_tectonic_plates IS NULL OR number_of_tectonic_plates > 0),
    ADD CONSTRAINT chk_active_volcanoes_non_negative CHECK (estimated_active_volcanoes IS NULL OR estimated_active_volcanoes >= 0),
    ADD CONSTRAINT chk_visible_craters_non_negative CHECK (estimated_visible_craters IS NULL OR estimated_visible_craters >= 0),
    ADD CONSTRAINT chk_major_storms_non_negative CHECK (number_of_major_storms IS NULL OR number_of_major_storms >= 0);

-- Create indexes for common queries
CREATE INDEX idx_planet_activity_score ON ud.planet(activity_score);
CREATE INDEX idx_planet_has_volcanism ON ud.planet(has_volcanic_activity);
CREATE INDEX idx_planet_has_tectonics ON ud.planet(has_plate_tectonics);