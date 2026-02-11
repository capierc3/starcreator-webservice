-- =====================================================================
-- Flyway Migration V69: Rebalance Mini-Neptune and Sub-Neptune Generation
-- =====================================================================
-- Problem: 10K system report shows Mini-Neptune at only 0.2% (121 planets)
-- while Sub-Neptune dominates at 23.9% (12,805 planets). Kepler data shows
-- mini-Neptunes are among the most abundant exoplanet types, so 0.2% is
-- far too low.
--
-- Root Cause Analysis:
--   1. Mini-Neptune formation_zone = 'frost_line' is too restrictive.
--      Most orbital slots at 50-120K are in the 'outer' zone, so the
--      zone filter in selectPlanetTypeByTemp() excludes Mini-Neptune
--      in favor of Sub-Neptune (zone = 'outer').
--   2. Mini-Neptune temp range (50-150K) nearly identical to Sub-Neptune
--      (40-120K), so in the overlap zone Sub-Neptune always wins on
--      zone match.
--   3. Real mini-Neptunes exist across a wide range of temperatures
--      and orbital distances — they're defined by mass (2-10 M⊕) and
--      having a modest H/He envelope, not by location.
--
-- Fix:
--   - Set Mini-Neptune formation_zone to NULL so it passes the zone
--     filter unconditionally and competes on temp + distance + weight.
--   - Widen temp range to 40-400K to cover habitable and inner zones.
--   - Extend distance down to 0.3 AU for warm close-in mini-Neptunes.
--   - Bump rarity weight from 200 to 250 for realistic abundance.
--   - Widen Sub-Neptune max temp from 120K to 200K so it properly
--     covers the sub-freezing range where Gas Giant also competes.
-- =====================================================================

-- Mini-Neptune: remove zone restriction, widen temp and distance
UPDATE ref.planet_type_ref
SET formation_zone = NULL,
    min_formation_temp_k = 40,
    max_formation_temp_k = 400,
    min_formation_distance_au = 0.3,
    max_formation_distance_au = 10.0,
    rarity_weight = 250
WHERE name = 'Mini-Neptune';

-- Sub-Neptune: widen upper temp limit for better coverage
UPDATE ref.planet_type_ref
SET max_formation_temp_k = 200
WHERE name = 'Sub-Neptune';
