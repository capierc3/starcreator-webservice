-- V16__fix_gas_ice_planet_composition_distances.sql
-- Remove absolute AU distance constraints from gas/ice planet templates
-- These planets can form at different distances around different mass stars

-- Gas Giants (can be at any distance - Hot Jupiters to cold Jupiters)
UPDATE ref.composition_template 
SET min_distance_au = NULL,
    max_distance_au = NULL
WHERE name IN ('Gas Giant - Jupiter-like', 'Hot Jupiter - Inflated Gas', 'Super-Jupiter - Massive Gas', 'Puffy - Low Density Gas');

-- Ice planets (frost line varies by star - can be close for M-dwarfs, far for hot stars)
UPDATE ref.composition_template 
SET min_distance_au = NULL,
    max_distance_au = NULL
WHERE name IN ('Ice Giant - Neptune-like', 'Sub-Neptune - Ice Giant', 'Mini-Neptune - Ice-Rock Core', 'Ice World - Frozen Volatiles');
