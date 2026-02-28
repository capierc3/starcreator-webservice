-- ============================================================================
-- V93: Physical Properties extraction + Orbital Elements expansion
-- ============================================================================
-- Phase 1: Create physical_properties table and FK columns
-- Phase 2: Expand orbital_elements with distance_from_parent and orbital_order
-- Phase 3: Add orbital_elements_id to star for multi-star orbital data
-- ============================================================================

-- ── Phase 1: Physical Properties ──

CREATE TABLE ud.physical_properties (
    id                BIGSERIAL PRIMARY KEY,
    mass              DOUBLE PRECISION,
    radius            DOUBLE PRECISION,
    circumference     DOUBLE PRECISION,
    earth_mass        DOUBLE PRECISION,
    earth_radius      DOUBLE PRECISION,
    density_g_cm3     DOUBLE PRECISION,
    surface_gravity   DOUBLE PRECISION,
    escape_velocity   DOUBLE PRECISION,
    albedo            DOUBLE PRECISION,
    solar_mass        DOUBLE PRECISION,
    solar_radius      DOUBLE PRECISION,
    solar_luminosity  DOUBLE PRECISION,
    surface_temp      DOUBLE PRECISION,
    created_at        TIMESTAMP DEFAULT NOW(),
    modified_at       TIMESTAMP DEFAULT NOW()
);

COMMENT ON TABLE ud.physical_properties IS
'Physical properties for any celestial body (planet, moon, or star). Body-type-specific fields are nullable.';

-- Add FK columns on planet, moon, star
ALTER TABLE ud.planet ADD COLUMN physical_properties_id BIGINT
    REFERENCES ud.physical_properties(id) ON DELETE SET NULL;
ALTER TABLE ud.moon ADD COLUMN physical_properties_id BIGINT
    REFERENCES ud.physical_properties(id) ON DELETE SET NULL;
ALTER TABLE ud.star ADD COLUMN physical_properties_id BIGINT
    REFERENCES ud.physical_properties(id) ON DELETE SET NULL;

CREATE INDEX idx_planet_physical_properties ON ud.planet(physical_properties_id);
CREATE INDEX idx_moon_physical_properties ON ud.moon(physical_properties_id);
CREATE INDEX idx_star_physical_properties ON ud.star(physical_properties_id);

-- Drop old physical property columns from planet
ALTER TABLE ud.planet DROP COLUMN IF EXISTS earth_mass;
ALTER TABLE ud.planet DROP COLUMN IF EXISTS earth_radius;
ALTER TABLE ud.planet DROP COLUMN IF EXISTS density_g_cm3;
ALTER TABLE ud.planet DROP COLUMN IF EXISTS surface_gravity_g;
ALTER TABLE ud.planet DROP COLUMN IF EXISTS escape_velocity_km_s;
ALTER TABLE ud.planet DROP COLUMN IF EXISTS albedo;
ALTER TABLE ud.planet DROP COLUMN IF EXISTS surface_temp_kelvin;

-- Drop old physical property columns from moon
ALTER TABLE ud.moon DROP COLUMN IF EXISTS earth_mass;
ALTER TABLE ud.moon DROP COLUMN IF EXISTS earth_radius;
ALTER TABLE ud.moon DROP COLUMN IF EXISTS density;
ALTER TABLE ud.moon DROP COLUMN IF EXISTS albedo;
ALTER TABLE ud.moon DROP COLUMN IF EXISTS surface_gravity;
ALTER TABLE ud.moon DROP COLUMN IF EXISTS escape_velocity;
ALTER TABLE ud.moon DROP COLUMN IF EXISTS surface_temp;

-- Drop old physical property columns from star
ALTER TABLE ud.star DROP COLUMN IF EXISTS solar_mass;
ALTER TABLE ud.star DROP COLUMN IF EXISTS solar_radius;
ALTER TABLE ud.star DROP COLUMN IF EXISTS solar_luminosity;
ALTER TABLE ud.star DROP COLUMN IF EXISTS surface_temp;

-- ── Phase 2: Expand orbital_elements ──

ALTER TABLE ud.orbital_elements ADD COLUMN IF NOT EXISTS distance_from_parent DOUBLE PRECISION;
ALTER TABLE ud.orbital_elements ADD COLUMN IF NOT EXISTS orbital_order INTEGER;

COMMENT ON COLUMN ud.orbital_elements.distance_from_parent IS
'Distance from parent body (planet→star in AU, moon→planet in km, star→barycenter in AU)';
COMMENT ON COLUMN ud.orbital_elements.orbital_order IS
'Position in orbital sequence (1 = innermost)';

-- ── Phase 3: Star orbital elements ──

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS orbital_elements_id BIGINT
    REFERENCES ud.orbital_elements(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_star_orbital_elements ON ud.star(orbital_elements_id);
