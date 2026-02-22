-- ============================================================================
-- V95: Move storm fields from Planet to Atmosphere
-- ============================================================================
-- Storm data (hasGreatStorm, numberOfMajorStorms, atmosphericConvectionLevel)
-- are gas-giant-only atmospheric phenomena. They now live on the Atmosphere
-- entity where they belong.
-- ============================================================================

-- ── Add storm columns to atmosphere ──

ALTER TABLE ud.atmosphere ADD COLUMN IF NOT EXISTS has_great_storm BOOLEAN;
ALTER TABLE ud.atmosphere ADD COLUMN IF NOT EXISTS number_of_major_storms INTEGER;
ALTER TABLE ud.atmosphere ADD COLUMN IF NOT EXISTS atmospheric_convection_level VARCHAR(50);

-- ── Drop old storm columns from planet ──

ALTER TABLE ud.planet DROP COLUMN IF EXISTS has_great_storm;
ALTER TABLE ud.planet DROP COLUMN IF EXISTS number_of_major_storms;
ALTER TABLE ud.planet DROP COLUMN IF EXISTS atmospheric_convection_level;
