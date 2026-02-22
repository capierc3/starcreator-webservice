-- ============================================================================
-- V97: Create RotationProperties entity
-- ============================================================================
-- Consolidates rotation/spin-axis fields from Planet, Moon, and Asteroid
-- into a shared RotationProperties table. The tidallyLocked field is null
-- for asteroids (hidden by @JsonInclude(NON_NULL)).
-- ============================================================================

-- ── Create rotation_properties table ──

CREATE TABLE ud.rotation_properties (
    id                    BIGSERIAL PRIMARY KEY,
    rotation_period_hours DOUBLE PRECISION,
    axial_tilt            DOUBLE PRECISION,
    tidally_locked        BOOLEAN,
    created_at            TIMESTAMP DEFAULT NOW(),
    modified_at           TIMESTAMP DEFAULT NOW()
);

COMMENT ON TABLE ud.rotation_properties IS
'Rotation and spin-axis properties for any celestial body (planet, moon, or asteroid)';

-- ── Add FK columns ──

ALTER TABLE ud.planet ADD COLUMN rotation_properties_id BIGINT
    REFERENCES ud.rotation_properties(id) ON DELETE SET NULL;
CREATE INDEX idx_planet_rotation_properties ON ud.planet(rotation_properties_id);

ALTER TABLE ud.moon ADD COLUMN rotation_properties_id BIGINT
    REFERENCES ud.rotation_properties(id) ON DELETE SET NULL;
CREATE INDEX idx_moon_rotation_properties ON ud.moon(rotation_properties_id);

ALTER TABLE ud.asteroid ADD COLUMN rotation_properties_id BIGINT
    REFERENCES ud.rotation_properties(id) ON DELETE SET NULL;
CREATE INDEX idx_asteroid_rotation_properties ON ud.asteroid(rotation_properties_id);

-- ── Drop old rotation columns ──

ALTER TABLE ud.planet DROP COLUMN IF EXISTS rotation_period_hours;
ALTER TABLE ud.planet DROP COLUMN IF EXISTS axial_tilt_degrees;
ALTER TABLE ud.planet DROP COLUMN IF EXISTS is_tidally_locked;

ALTER TABLE ud.moon DROP COLUMN IF EXISTS rotation_period_hours;
ALTER TABLE ud.moon DROP COLUMN IF EXISTS axial_tilt;
ALTER TABLE ud.moon DROP COLUMN IF EXISTS tidally_locked;

ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS rotation_period_hours;
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS axial_tilt;
