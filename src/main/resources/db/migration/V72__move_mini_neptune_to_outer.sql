-- =====================================================================
-- Flyway Migration V72: Move Mini-Neptune to Outer Zone
-- =====================================================================
-- Problem: Mini-Neptune at frost_line zone produces only 0.2% of planets.
-- The frost_line zone (HZ outer edge to frost line distance) is
-- physically too narrow — roughly 1 AU for Sun-like stars, even less
-- for M-dwarfs which are 64% of generated stars. Almost no orbital
-- slots land there.
--
-- Fix: Move Mini-Neptune to outer zone with weight 90. This adds ~9%
-- to the outer pool without heavily diluting existing types:
--   Gas Giant 250, Sub-Neptune 220, Ice Giant 220, Ice World 100,
--   Mini-Neptune 90, Dwarf Planet 60, Super-Jupiter 60 = total ~1000
--
-- frost_line zone will have 0 types. The selection fallback in
-- selectPlanetTypeByTemp gracefully handles this — when zoneFilteredTypes
-- is empty it falls back to tempFilteredTypes (all temp-eligible types).
-- This is fine since the frost_line band is a narrow transitional region.
-- =====================================================================

UPDATE ref.planet_type_ref
SET formation_zone = 'outer',
    min_formation_temp_k = 40,
    max_formation_temp_k = 150,
    min_formation_distance_au = 1.0,
    max_formation_distance_au = 15.0,
    rarity_weight = 90
WHERE name = 'Mini-Neptune';
