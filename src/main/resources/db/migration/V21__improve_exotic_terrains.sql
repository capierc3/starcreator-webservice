-- V21__improve_exotic_terrains.sql

-- Remove the generic EXOTIC_OTHER
DELETE FROM ref.terrain_type_ref WHERE name = 'EXOTIC_OTHER';

-- Add specific exotic terrain types
INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_water, requires_atmosphere, min_temperature_k, max_temperature_k, min_pressure_atm, is_volcanic, is_frozen, is_aquatic, is_artificial, rarity_weight, typical_coverage_min, typical_coverage_max)
VALUES
-- Actual exotic geological features
('GLASS_FIELDS', 'Glass Fields', 'Vast fields of natural glass from ancient impacts or volcanic activity', 'EXOTIC', false, false, 200.0, 800.0, null, false, false, false, false, 40, 1.0, 15.0),
('METAL_PLAINS', 'Metal Plains', 'Exposed metallic minerals creating reflective plains', 'EXOTIC', false, false, 100.0, 600.0, null, false, false, false, false, 30, 2.0, 20.0),
('DIAMOND_DEPOSITS', 'Diamond Deposits', 'Carbon-rich deposits with exposed diamonds (carbon planets)', 'EXOTIC', false, false, 200.0, 1000.0, null, false, false, false, false, 20, 0.5, 10.0),
('GRAPHITE_FIELDS', 'Graphite Fields', 'Vast graphite deposits (carbon planets)', 'EXOTIC', false, false, 200.0, 800.0, null, false, false, false, false, 35, 5.0, 30.0),
('SUBLIMATION_ZONES', 'Sublimation Zones', 'Areas where ices sublimate directly to gas creating unique landscapes', 'EXOTIC', false, false, 70.0, 150.0, null, false, true, false, false, 45, 2.0, 20.0),
('RADIATION_SCORCHED', 'Radiation Scorched', 'Terrain heavily altered by stellar radiation', 'EXOTIC', false, false, 0.0, 1500.0, null, false, false, false, false, 35, 1.0, 25.0),
('NITROGEN_GEYSERS', 'Nitrogen Geyser Fields', 'Active nitrogen geysers (Triton-like)', 'EXOTIC', false, false, 35.0, 60.0, null, false, true, false, false, 25, 0.5, 10.0),
('HYDROCARBON_SEAS', 'Hydrocarbon Seas', 'Ethane/propane lakes and seas', 'EXOTIC', false, false, 70.0, 110.0, null, false, false, false, false, 30, 5.0, 40.0);

-- Update CRYSTAL_FORMATIONS to be more specific to silicate/quartz
UPDATE ref.terrain_type_ref
SET description = 'Exposed quartz and silicate crystal formations',
    typical_coverage_max = 10.0
WHERE name = 'CRYSTAL_FORMATIONS';

-- Adjust some existing exotic terrain weights to be more balanced
UPDATE ref.terrain_type_ref
SET rarity_weight = 35,
    typical_coverage_max = 25.0
WHERE name = 'METHANE_LAKES';

UPDATE ref.terrain_type_ref
SET rarity_weight = 30,
    typical_coverage_max = 40.0
WHERE name = 'SULFUR_PLAINS';

COMMENT ON TABLE ref.terrain_type_ref IS 'Reference data for terrain types with environmental requirements and rarity weights';