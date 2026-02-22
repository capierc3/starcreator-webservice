-- ============================================================================
-- V99: Add PhysicalProperties to Asteroid
-- ============================================================================
-- Reuses the existing physical_properties table (shared with Planet, Moon, Star).
-- Adds the asteroid-specific dimensions_km column to physical_properties.
-- Moves 10 flat physical fields from asteroid into physical_properties.
-- ============================================================================

-- ── Add asteroid-specific column to physical_properties ──

ALTER TABLE ud.physical_properties ADD COLUMN IF NOT EXISTS dimensions_km VARCHAR(100);

-- ── Add FK column on asteroid ──

ALTER TABLE ud.asteroid ADD COLUMN physical_properties_id BIGINT
    REFERENCES ud.physical_properties(id) ON DELETE SET NULL;
CREATE INDEX idx_asteroid_physical_properties ON ud.asteroid(physical_properties_id);

-- ── Drop old physical columns from asteroid ──

ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS mass;
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS earth_mass;
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS radius;
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS circumference;
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS dimensions_km;
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS density;
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS albedo;
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS surface_temp;
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS surface_gravity;
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS escape_velocity;
