-- V24__cryovolcanic_terrain_types.sql
-- Adds volcanic terrain types for cold worlds (cryovolcanism)

-- ============================================================================
-- CRYOVOLCANIC TERRAIN TYPES
-- ============================================================================
INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, is_volcanic, is_frozen, rarity_weight, typical_coverage_min, typical_coverage_max, volcanic_weight_boost)
VALUES
-- Cryovolcanism - ice volcanoes on cold moons/planets (like Enceladus, Triton)
('CRYOVOLCANIC_FIELDS', 'Cryovolcanic Fields', 'Ice volcanoes erupting water, ammonia, or methane slurries', 'VOLCANIC', false, false, 30, 200, true, true, 70, 2, 20, 120),

-- Cryolava flows - frozen "lava" flows of ice/ammonia
('CRYOLAVA_PLAINS', 'Cryolava Plains', 'Smooth plains formed by cryolava flows', 'VOLCANIC', false, false, 30, 180, true, true, 60, 5, 25, 100),

-- Ice geysers (like on Enceladus/Triton)
('ICE_GEYSER_FIELDS', 'Ice Geyser Fields', 'Active regions of ice and vapor geysers', 'VOLCANIC', false, false, 30, 150, true, true, 50, 1, 10, 100),

-- Volcanic vents that work across a wide temp range
('VOLCANIC_VENTS', 'Volcanic Vents', 'Localized volcanic vents and fumaroles', 'VOLCANIC', false, false, 0, 500, true, false, 80, 1, 12, 80),

-- Warm spots on otherwise cold worlds (subsurface heating)
('THERMAL_ANOMALIES', 'Thermal Anomaly Zones', 'Warm regions from subsurface volcanic heating on cold worlds', 'VOLCANIC', false, false, 40, 250, true, false, 50, 0.5, 8, 90);

-- ============================================================================
-- VERIFICATION
-- ============================================================================
SELECT name, display_name, min_temperature_k, max_temperature_k, volcanic_weight_boost
FROM ref.terrain_type_ref
WHERE category = 'VOLCANIC'
ORDER BY min_temperature_k;