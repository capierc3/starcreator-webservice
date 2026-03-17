-- V117__fix_ocean_planet_generation.sql
-- BUG-001: Ocean Planets never generate (0 out of 490,459 in 100K report)
--
-- Root cause: The formation temp range (273-373K) assumes surface liquid-water
-- temps, but the selection-time estimate is a pre-atmosphere equilibrium temp
-- (albedo=0.3, no greenhouse). A Sun-like star at 1 AU yields ~255K equilibrium,
-- already below the 273K minimum. Ocean planets also have significant H2O vapor
-- greenhouse warming (+30-60K), so viable formation starts at lower equilibrium
-- temps.
--
-- Fix: Widen Ocean temp range to 220-370K. Rebalance all three habitable-zone
-- type weights so the effective split lands near SE ~44%, T ~33%, O ~22% after
-- accounting for Super-Earth's wider temp-range advantage.

-- Ocean Planet: widen temp range, boost weight
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 220,
    max_formation_temp_k = 370,
    rarity_weight = 200
WHERE name = 'Ocean Planet';

-- Super-Earth: reduce weight to offset its wide temp-range advantage (150-500K)
UPDATE ref.planet_type_ref
SET rarity_weight = 280
WHERE name = 'Super-Earth';

-- Terrestrial Planet: boost weight to compete in the overlap band
UPDATE ref.planet_type_ref
SET rarity_weight = 300
WHERE name = 'Terrestrial Planet';
