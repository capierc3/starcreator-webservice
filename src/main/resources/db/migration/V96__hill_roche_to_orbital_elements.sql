-- ============================================================================
-- V96: Move Hill Sphere & Roche Limit from Moon to OrbitalElements
-- ============================================================================
-- These gravitational boundary metrics are orbital-derived values that belong
-- with the orbital data. They are null for non-moon orbits and hidden by
-- @JsonInclude(NON_NULL).
-- ============================================================================

-- ── Add columns to orbital_elements ──

ALTER TABLE ud.orbital_elements ADD COLUMN IF NOT EXISTS hill_sphere_radius_km DOUBLE PRECISION;
ALTER TABLE ud.orbital_elements ADD COLUMN IF NOT EXISTS roche_limit_km DOUBLE PRECISION;

-- ── Drop old columns from moon ──

ALTER TABLE ud.moon DROP COLUMN IF EXISTS hill_sphere_radius_km;
ALTER TABLE ud.moon DROP COLUMN IF EXISTS roche_limit_km;
