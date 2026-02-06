-- V45__fill_geological_template_gaps.sql
-- Fills all gaps in geological template coverage for planet types

-- ============================================================================
-- UPDATE EXISTING TEMPLATES TO COVER MORE PLANET TYPES
-- ============================================================================

-- Template 1 (Dead Rocky World) - Add more dead world types
UPDATE ref.geological_template
SET planet_types = 'Hot Rocky Planet,Terrestrial Planet,Desert Planet,Ocean Planet,Super-Earth,Ice World,Iron Planet,Carbon Planet,Dwarf Planet,Rogue Planet'
WHERE id = 1;

-- Template 2 (Stagnant Lid World) - Add more low activity types
UPDATE ref.geological_template
SET planet_types = 'Terrestrial Planet,Super-Earth,Desert Planet,Hot Rocky Planet,Ocean Planet,Ice World,Iron Planet,Carbon Planet,Dwarf Planet,Rogue Planet'
WHERE id = 2;

-- Template 3 (Active Rocky World) - Add more moderate activity types
UPDATE ref.geological_template
SET planet_types = 'Terrestrial Planet,Super-Earth,Ocean Planet,Desert Planet,Hot Rocky Planet,Ice World,Iron Planet,Carbon Planet'
WHERE id = 3;

-- Template 4 (Hyperactive World) - Add more high activity types
UPDATE ref.geological_template
SET planet_types = 'Terrestrial Planet,Super-Earth,Lava Planet,Desert Planet,Hot Rocky Planet,Ocean Planet,Iron Planet,Carbon Planet'
WHERE id = 4;

-- Template 6 (Cryovolcanic Ice World) - Expand to cover low activity ice worlds
UPDATE ref.geological_template
SET min_activity_score = 0.1
WHERE id = 6;

-- Template 7 (Calm Gas Giant) - Add more gas types
UPDATE ref.geological_template
SET planet_types = 'Mini-Neptune,Sub-Neptune,Gas Giant,Ice Giant,Hot Jupiter,Super-Jupiter,Puffy Planet'
WHERE id = 7;

-- Template 8 (Active Gas Giant) - Add more gas types
UPDATE ref.geological_template
SET planet_types = 'Gas Giant,Ice Giant,Sub-Neptune,Mini-Neptune,Hot Jupiter,Super-Jupiter,Puffy Planet'
WHERE id = 8;

-- Template 9 (Turbulent Super-Jupiter) - Add more high activity gas types
UPDATE ref.geological_template
SET planet_types = 'Gas Giant,Super-Jupiter,Hot Jupiter,Ice Giant,Sub-Neptune,Mini-Neptune,Puffy Planet'
WHERE id = 9;

-- ============================================================================
-- ADD NEW SPECIALIZED TEMPLATES FOR EDGE CASES
-- ============================================================================

-- Dead Ice World (for frozen dead worlds)
INSERT INTO ref.geological_template
(name, description, planet_types, activity_level, min_activity_score, max_activity_score, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Dead Ice World', 'Ancient frozen world with no geological activity',
     'Ice World,Rogue Planet,Dwarf Planet',
     'Geologically Dead', 0, 0.1, 0.001, 1.0, 100);

-- Get the ID of the newly inserted template
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'TECTONICS', 'None', NULL, NULL FROM ref.geological_template WHERE name = 'Dead Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'VOLCANISM_TYPE', 'None', NULL, NULL FROM ref.geological_template WHERE name = 'Dead Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'VOLCANIC_ACTIVITY', 'false', NULL, NULL FROM ref.geological_template WHERE name = 'Dead Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'VOLCANIC_INTENSITY', 'None', NULL, NULL FROM ref.geological_template WHERE name = 'Dead Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'ACTIVE_VOLCANOES', 'numeric', 0, 0 FROM ref.geological_template WHERE name = 'Dead Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'MOUNTAIN_COVERAGE', 'numeric', 1.0, 5.0 FROM ref.geological_template WHERE name = 'Dead Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'MAX_ELEVATION', 'numeric', 0.5, 2.0 FROM ref.geological_template WHERE name = 'Dead Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'TERRAIN_ROUGHNESS', 'numeric', 1.0, 2.0 FROM ref.geological_template WHERE name = 'Dead Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'CRATERING_LEVEL', 'Saturated', NULL, NULL FROM ref.geological_template WHERE name = 'Dead Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'VISIBLE_CRATERS', 'numeric', 50000, 500000 FROM ref.geological_template WHERE name = 'Dead Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'EROSION_LEVEL', 'None', NULL, NULL FROM ref.geological_template WHERE name = 'Dead Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'EROSION_AGENT', 'None', NULL, NULL FROM ref.geological_template WHERE name = 'Dead Ice World';

-- Hyperactive Ice World (for tidally heated ice moons turned planets)
INSERT INTO ref.geological_template
(name, description, planet_types, activity_level, min_activity_score, max_activity_score, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Hyperactive Ice World', 'Ice world with intense cryovolcanism and tectonic activity',
     'Ice World,Ocean Planet',
     'Highly Active', 2.0, 100.0, 0.3, 3.0, 50);

INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'TECTONICS', 'Active', NULL, NULL FROM ref.geological_template WHERE name = 'Hyperactive Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'PLATE_TECTONICS', 'true', NULL, NULL FROM ref.geological_template WHERE name = 'Hyperactive Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'TECTONIC_PLATES', 'numeric', 5, 12 FROM ref.geological_template WHERE name = 'Hyperactive Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'VOLCANISM_TYPE', 'Cryovolcanic', NULL, NULL FROM ref.geological_template WHERE name = 'Hyperactive Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'VOLCANIC_ACTIVITY', 'true', NULL, NULL FROM ref.geological_template WHERE name = 'Hyperactive Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'VOLCANIC_INTENSITY', 'Continuous', NULL, NULL FROM ref.geological_template WHERE name = 'Hyperactive Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'ACTIVE_VOLCANOES', 'numeric', 50, 300 FROM ref.geological_template WHERE name = 'Hyperactive Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'MOUNTAIN_COVERAGE', 'numeric', 15.0, 30.0 FROM ref.geological_template WHERE name = 'Hyperactive Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'MAX_ELEVATION', 'numeric', 5.0, 12.0 FROM ref.geological_template WHERE name = 'Hyperactive Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'TERRAIN_ROUGHNESS', 'numeric', 5.0, 8.0 FROM ref.geological_template WHERE name = 'Hyperactive Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'CRATERING_LEVEL', 'Pristine', NULL, NULL FROM ref.geological_template WHERE name = 'Hyperactive Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'VISIBLE_CRATERS', 'numeric', 10, 100 FROM ref.geological_template WHERE name = 'Hyperactive Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'EROSION_LEVEL', 'Heavy', NULL, NULL FROM ref.geological_template WHERE name = 'Hyperactive Ice World';
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'EROSION_AGENT', 'Cryovolcanic', NULL, NULL FROM ref.geological_template WHERE name = 'Hyperactive Ice World';