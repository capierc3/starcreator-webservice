-- Tier 2C: Store climate seed, regenerate climate/habitability from seed on load
-- Add seed columns
ALTER TABLE ud.planet ADD COLUMN climate_seed BIGINT;
ALTER TABLE ud.moon ADD COLUMN climate_seed BIGINT;

-- Drop FK columns from planet/moon
ALTER TABLE ud.planet DROP COLUMN IF EXISTS climate_id;
ALTER TABLE ud.planet DROP COLUMN IF EXISTS habitability_id;
ALTER TABLE ud.moon DROP COLUMN IF EXISTS climate_id;
ALTER TABLE ud.moon DROP COLUMN IF EXISTS habitability_id;

-- Drop child tables first (FK dependencies)
DROP TABLE IF EXISTS ud.climate_hazard CASCADE;
DROP TABLE IF EXISTS ud.eclipse_data CASCADE;
DROP TABLE IF EXISTS ud.moon_sky_appearance CASCADE;
DROP TABLE IF EXISTS ud.extreme_climate_event CASCADE;
DROP TABLE IF EXISTS ud.precipitation_type CASCADE;
DROP TABLE IF EXISTS ud.cloud_layer CASCADE;
DROP TABLE IF EXISTS ud.climate_zone CASCADE;

-- Drop parent tables
DROP TABLE IF EXISTS ud.planetary_climate CASCADE;
DROP TABLE IF EXISTS ud.planetary_habitability CASCADE;
