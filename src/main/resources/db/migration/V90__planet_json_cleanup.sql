-- =====================================================================
-- Flyway Migration V90: Planet JSON Cleanup
-- =====================================================================
-- Description: Removes redundant columns from planet table:
--   - number_of_moons: derivable from moons list + additional_moonlets
--   - has_rings: derivable from bands list emptiness
-- =====================================================================

ALTER TABLE ud.planet DROP COLUMN IF EXISTS number_of_moons;
ALTER TABLE ud.planet DROP COLUMN IF EXISTS has_rings;
