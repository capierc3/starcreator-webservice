-- ============================================================
-- V91: Weather → Climate rename + @OneToOne conversion
-- ============================================================

-- 1. Rename main table
ALTER TABLE ud.planetary_weather RENAME TO planetary_climate;

-- 2. Rename child tables with "weather" in name
ALTER TABLE ud.weather_hazard RENAME TO climate_hazard;
ALTER TABLE ud.extreme_weather_event RENAME TO extreme_climate_event;

-- 3. Add climate_id FK to planet and moon tables (parent-side ownership)
ALTER TABLE ud.planet
    ADD COLUMN climate_id BIGINT REFERENCES ud.planetary_climate(id) ON DELETE SET NULL;
ALTER TABLE ud.moon
    ADD COLUMN climate_id BIGINT REFERENCES ud.planetary_climate(id) ON DELETE SET NULL;

-- 4. Migrate existing FK data from child-side to parent-side
UPDATE ud.planet p SET climate_id = pc.id
FROM ud.planetary_climate pc WHERE pc.planet_id = p.id;

UPDATE ud.moon m SET climate_id = pc.id
FROM ud.planetary_climate pc WHERE pc.moon_id = m.id;

-- 5. Rename weather_id FK columns in child tables to climate_id
ALTER TABLE ud.climate_zone RENAME COLUMN weather_id TO climate_id;
ALTER TABLE ud.cloud_layer RENAME COLUMN weather_id TO climate_id;
ALTER TABLE ud.precipitation_type RENAME COLUMN weather_id TO climate_id;
ALTER TABLE ud.extreme_climate_event RENAME COLUMN weather_id TO climate_id;
ALTER TABLE ud.moon_sky_appearance RENAME COLUMN weather_id TO climate_id;
ALTER TABLE ud.eclipse_data RENAME COLUMN weather_id TO climate_id;
ALTER TABLE ud.climate_hazard RENAME COLUMN weather_id TO climate_id;

-- 6. Drop old child-side FK columns from planetary_climate
ALTER TABLE ud.planetary_climate DROP COLUMN IF EXISTS planet_id;
ALTER TABLE ud.planetary_climate DROP COLUMN IF EXISTS moon_id;

-- 7. Add indexes on parent-side FK
CREATE INDEX idx_planet_climate_id ON ud.planet(climate_id);
CREATE INDEX idx_moon_climate_id ON ud.moon(climate_id);
