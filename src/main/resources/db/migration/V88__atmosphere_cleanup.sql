-- =====================================================================
-- Flyway Migration V88: Atmosphere Cleanup
-- =====================================================================
-- Description: Drop denormalized atmosphere columns from planet and moon
--              tables. Planet and Moon now use @OneToOne relationship to
--              the ud.atmosphere table (atmosphere_id column already exists
--              from V38 migration).
-- =====================================================================

-- Drop denormalized atmosphere columns from planet
-- (atmosphere_id FK already exists from V38)
ALTER TABLE ud.planet DROP COLUMN IF EXISTS atmosphere_classification;
ALTER TABLE ud.planet DROP COLUMN IF EXISTS atmosphere_composition;
ALTER TABLE ud.planet DROP COLUMN IF EXISTS surface_pressure_atm;

-- Drop denormalized atmosphere columns from moon
-- (atmosphere_id FK already exists from V38)
ALTER TABLE ud.moon DROP COLUMN IF EXISTS has_atmosphere;
ALTER TABLE ud.moon DROP COLUMN IF EXISTS surface_pressure;
ALTER TABLE ud.moon DROP COLUMN IF EXISTS atmosphere_composition;
