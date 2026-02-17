-- =====================================================================
-- Flyway Migration V82: Remove Planet Formation Distance Columns
-- =====================================================================
-- Bug Tracker #18 Resolution: Lava Planets at >1 AU
--
-- Analysis showed that fixed min/max formation distance values per
-- planet type are fundamentally flawed — formation distance depends
-- on the parent star's luminosity, not the planet type alone.
--
-- Example: Lava Planets (1200-3000K) had formation distance 0.01-0.15 AU,
-- which is only correct for Sun-like stars. Around an O-star (L~100,000 L☉),
-- the 1200K isotherm is at ~10 AU. Around M-dwarfs, it's at ~0.01 AU.
--
-- The temperature-based selection (min/max_formation_temp_k) already
-- handles this correctly by computing equilibrium temperature from the
-- star's actual luminosity and the orbital distance. Formation distance
-- is redundant and misleading.
--
-- Composition templates went through this same cleanup in V30 for
-- the same reason.
--
-- Impact: Only one code path uses these columns —
--         PlanetCreator.selectPlanetTypeByTemp() distance filter layer.
--         That filter layer is also removed in the corresponding code change.
-- =====================================================================

ALTER TABLE ref.planet_type_ref
    DROP COLUMN IF EXISTS min_formation_distance_au,
    DROP COLUMN IF EXISTS max_formation_distance_au;
