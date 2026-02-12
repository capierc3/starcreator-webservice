-- =====================================================================
-- Flyway Migration V66: Close Planet Type Temperature Gap Below 30K
-- =====================================================================
-- Problem: Ice World and Dwarf Planet both have min_formation_temp_k = 30,
--          but MIN_VIABLE_PLANET_TEMP_K = 10. Planets generated between
--          10-30K match NO type, causing fallback to random rarity-based
--          selection which produces impossible results like Ocean Planets
--          at 16K.
--
-- Fix: Lower Ice World and Dwarf Planet minimums to 10K to cover the gap.
-- =====================================================================

UPDATE ref.planet_type_ref
SET min_formation_temp_k = 10
WHERE name = 'Ice World';

UPDATE ref.planet_type_ref
SET min_formation_temp_k = 10
WHERE name = 'Dwarf Planet';
