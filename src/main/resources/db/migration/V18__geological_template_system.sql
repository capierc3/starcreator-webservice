-- V18__geological_template_system.sql
-- Creates geological activity template system similar to atmosphere and composition templates

-- ============================================================================
-- GEOLOGICAL TEMPLATE TABLE
-- ============================================================================
CREATE TABLE ref.geological_template (
                                         id SERIAL PRIMARY KEY,
                                         name VARCHAR(100) UNIQUE NOT NULL,
                                         description TEXT,
                                         planet_types VARCHAR(500),
                                         activity_level VARCHAR(50) NOT NULL,
                                         min_activity_score DOUBLE PRECISION,
                                         max_activity_score DOUBLE PRECISION,
                                         min_planet_mass_earth DOUBLE PRECISION DEFAULT 0.0,
                                         max_planet_mass_earth DOUBLE PRECISION DEFAULT 10000.0,
                                         rarity_weight INTEGER DEFAULT 100,
                                         created_at TIMESTAMP DEFAULT NOW()
);

COMMENT ON TABLE ref.geological_template IS 'Pre-defined geological activity templates';
COMMENT ON COLUMN ref.geological_template.planet_types IS 'Comma-separated list of applicable planet types';
COMMENT ON COLUMN ref.geological_template.activity_level IS 'Descriptive activity level (Highly Active, Moderately Active, etc.)';
COMMENT ON COLUMN ref.geological_template.min_activity_score IS 'Minimum activity score (mass/age ratio)';
COMMENT ON COLUMN ref.geological_template.max_activity_score IS 'Maximum activity score (mass/age ratio)';

-- ============================================================================
-- GEOLOGICAL FEATURE TABLE
-- ============================================================================
CREATE TABLE ref.geological_template_feature (
                                                 id SERIAL PRIMARY KEY,
                                                 template_id INTEGER NOT NULL REFERENCES ref.geological_template(id) ON DELETE CASCADE,
                                                 feature_type VARCHAR(50) NOT NULL,
                                                 feature_value VARCHAR(100) NOT NULL,
                                                 min_value DOUBLE PRECISION,
                                                 max_value DOUBLE PRECISION,
                                                 description TEXT
);

COMMENT ON TABLE ref.geological_template_feature IS 'Features associated with each geological template';
COMMENT ON COLUMN ref.geological_template_feature.feature_type IS 'Type of feature (VOLCANISM, TECTONICS, TERRAIN, EROSION, etc.)';
COMMENT ON COLUMN ref.geological_template_feature.feature_value IS 'String value for categorical features';
COMMENT ON COLUMN ref.geological_template_feature.min_value IS 'Minimum value for numeric features';
COMMENT ON COLUMN ref.geological_template_feature.max_value IS 'Maximum value for numeric features';

CREATE INDEX idx_geological_template_activity ON ref.geological_template(activity_level);
CREATE INDEX idx_geological_template_feature ON ref.geological_template_feature(template_id);

-- ============================================================================
-- UPDATE PLANET TABLE
-- ============================================================================
ALTER TABLE ud.planet
    ADD COLUMN geological_activity_json TEXT;

COMMENT ON COLUMN ud.planet.geological_activity_json IS 'JSON representation of PlanetaryGeology object';

-- Keep existing geological_activity column for backwards compatibility
-- It will now be populated from the template's activity_level

-- ============================================================================
-- SEED DATA: TERRESTRIAL PLANET TEMPLATES
-- ============================================================================

-- 1. GEOLOGICALLY DEAD (Score: 0.0 - 0.1)
INSERT INTO ref.geological_template
(name, description, planet_types, activity_level, min_activity_score, max_activity_score, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Dead Rocky World', 'Ancient, geologically dead world with heavily cratered surface',
     'Hot Rocky Planet,Terrestrial Planet,Desert Planet',
     'Geologically Dead', 0.0, 0.1, 0.1, 2.0, 100);

INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value) VALUES
                                                                                                                 (1, 'TECTONICS', 'None', NULL, NULL),
                                                                                                                 (1, 'VOLCANISM_TYPE', 'None', NULL, NULL),
                                                                                                                 (1, 'VOLCANIC_ACTIVITY', 'false', NULL, NULL),
                                                                                                                 (1, 'VOLCANIC_INTENSITY', 'None', NULL, NULL),
                                                                                                                 (1, 'ACTIVE_VOLCANOES', 'numeric', 0, 0),
                                                                                                                 (1, 'MOUNTAIN_COVERAGE', 'numeric', 2.0, 8.0),
                                                                                                                 (1, 'MAX_ELEVATION', 'numeric', 0.5, 3.0),
                                                                                                                 (1, 'TERRAIN_ROUGHNESS', 'numeric', 1.0, 2.5),
                                                                                                                 (1, 'CRATERING_LEVEL', 'Saturated', NULL, NULL),
                                                                                                                 (1, 'VISIBLE_CRATERS', 'numeric', 100000, 1000000),
                                                                                                                 (1, 'EROSION_LEVEL', 'None', NULL, NULL),
                                                                                                                 (1, 'EROSION_AGENT', 'None', NULL, NULL);

-- 2. LOW ACTIVITY (Score: 0.1 - 0.5)
INSERT INTO ref.geological_template
(name, description, planet_types, activity_level, min_activity_score, max_activity_score, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Stagnant Lid World', 'Minimal geological activity, occasional volcanism',
     'Terrestrial Planet,Super-Earth,Desert Planet',
     'Low Activity', 0.1, 0.5, 0.3, 3.0, 120);

INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value) VALUES
                                                                                                                 (2, 'TECTONICS', 'Stagnant Lid', NULL, NULL),
                                                                                                                 (2, 'VOLCANISM_TYPE', 'Silicate', NULL, NULL),
                                                                                                                 (2, 'VOLCANIC_ACTIVITY', 'true', NULL, NULL),
                                                                                                                 (2, 'VOLCANIC_INTENSITY', 'Rare', NULL, NULL),
                                                                                                                 (2, 'ACTIVE_VOLCANOES', 'numeric', 1, 10),
                                                                                                                 (2, 'MOUNTAIN_COVERAGE', 'numeric', 5.0, 12.0),
                                                                                                                 (2, 'MAX_ELEVATION', 'numeric', 2.0, 6.0),
                                                                                                                 (2, 'TERRAIN_ROUGHNESS', 'numeric', 1.5, 3.5),
                                                                                                                 (2, 'CRATERING_LEVEL', 'Heavy', NULL, NULL),
                                                                                                                 (2, 'VISIBLE_CRATERS', 'numeric', 10000, 100000),
                                                                                                                 (2, 'EROSION_LEVEL', 'Minimal', NULL, NULL),
                                                                                                                 (2, 'EROSION_AGENT', 'Wind', NULL, NULL);

-- 3. MODERATELY ACTIVE (Score: 0.5 - 2.0)
INSERT INTO ref.geological_template
(name, description, planet_types, activity_level, min_activity_score, max_activity_score, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Active Rocky World', 'Moderate geological activity with plate tectonics and volcanism',
     'Terrestrial Planet,Super-Earth,Ocean Planet',
     'Moderately Active', 0.5, 2.0, 0.5, 3.0, 150);

INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value) VALUES
                                                                                                                 (3, 'TECTONICS', 'Active', NULL, NULL),
                                                                                                                 (3, 'PLATE_TECTONICS', 'true', NULL, NULL),
                                                                                                                 (3, 'TECTONIC_PLATES', 'numeric', 5, 15),
                                                                                                                 (3, 'VOLCANISM_TYPE', 'Silicate', NULL, NULL),
                                                                                                                 (3, 'VOLCANIC_ACTIVITY', 'true', NULL, NULL),
                                                                                                                 (3, 'VOLCANIC_INTENSITY', 'Moderate', NULL, NULL),
                                                                                                                 (3, 'ACTIVE_VOLCANOES', 'numeric', 10, 50),
                                                                                                                 (3, 'MOUNTAIN_COVERAGE', 'numeric', 12.0, 25.0),
                                                                                                                 (3, 'MAX_ELEVATION', 'numeric', 6.0, 10.0),
                                                                                                                 (3, 'TERRAIN_ROUGHNESS', 'numeric', 4.0, 6.5),
                                                                                                                 (3, 'CRATERING_LEVEL', 'Moderate', NULL, NULL),
                                                                                                                 (3, 'VISIBLE_CRATERS', 'numeric', 1000, 10000),
                                                                                                                 (3, 'EROSION_LEVEL', 'Moderate', NULL, NULL),
                                                                                                                 (3, 'EROSION_AGENT', 'Water', NULL, NULL);

-- 4. HIGHLY ACTIVE (Score: > 2.0)
INSERT INTO ref.geological_template
(name, description, planet_types, activity_level, min_activity_score, max_activity_score, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Hyperactive World', 'Extremely active geology with intense volcanism and tectonics',
     'Terrestrial Planet,Super-Earth,Lava Planet',
     'Highly Active', 2.0, 100.0, 0.8, 5.0, 80);

INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value) VALUES
                                                                                                                 (4, 'TECTONICS', 'Hyperactive', NULL, NULL),
                                                                                                                 (4, 'PLATE_TECTONICS', 'true', NULL, NULL),
                                                                                                                 (4, 'TECTONIC_PLATES', 'numeric', 8, 20),
                                                                                                                 (4, 'VOLCANISM_TYPE', 'Silicate', NULL, NULL),
                                                                                                                 (4, 'VOLCANIC_ACTIVITY', 'true', NULL, NULL),
                                                                                                                 (4, 'VOLCANIC_INTENSITY', 'Continuous', NULL, NULL),
                                                                                                                 (4, 'ACTIVE_VOLCANOES', 'numeric', 200, 800),
                                                                                                                 (4, 'MOUNTAIN_COVERAGE', 'numeric', 20.0, 35.0),
                                                                                                                 (4, 'MAX_ELEVATION', 'numeric', 8.0, 15.0),
                                                                                                                 (4, 'TERRAIN_ROUGHNESS', 'numeric', 6.0, 8.5),
                                                                                                                 (4, 'CRATERING_LEVEL', 'Pristine', NULL, NULL),
                                                                                                                 (4, 'VISIBLE_CRATERS', 'numeric', 10, 100),
                                                                                                                 (4, 'EROSION_LEVEL', 'Heavy', NULL, NULL),
                                                                                                                 (4, 'EROSION_AGENT', 'Volcanic', NULL, NULL);

-- ============================================================================
-- SPECIAL TEMPLATES
-- ============================================================================

-- 5. LAVA WORLD
INSERT INTO ref.geological_template
(name, description, planet_types, activity_level, min_activity_score, max_activity_score, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Molten Lava World', 'Surface entirely molten, extreme volcanic activity',
     'Lava Planet',
     'Highly Active', 0.5, 100.0, 0.5, 2.0, 60);

INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value) VALUES
                                                                                                                 (5, 'TECTONICS', 'Molten', NULL, NULL),
                                                                                                                 (5, 'VOLCANISM_TYPE', 'Silicate', NULL, NULL),
                                                                                                                 (5, 'VOLCANIC_ACTIVITY', 'true', NULL, NULL),
                                                                                                                 (5, 'VOLCANIC_INTENSITY', 'Continuous', NULL, NULL),
                                                                                                                 (5, 'ACTIVE_VOLCANOES', 'numeric', 1000, 10000),
                                                                                                                 (5, 'MOUNTAIN_COVERAGE', 'numeric', 30.0, 50.0),
                                                                                                                 (5, 'MAX_ELEVATION', 'numeric', 5.0, 12.0),
                                                                                                                 (5, 'TERRAIN_ROUGHNESS', 'numeric', 8.0, 10.0),
                                                                                                                 (5, 'CRATERING_LEVEL', 'None', NULL, NULL),
                                                                                                                 (5, 'VISIBLE_CRATERS', 'numeric', 0, 10),
                                                                                                                 (5, 'EROSION_LEVEL', 'Extreme', NULL, NULL),
                                                                                                                 (5, 'EROSION_AGENT', 'Lava', NULL, NULL);

-- 6. ICE WORLD - CRYOVOLCANIC
INSERT INTO ref.geological_template
(name, description, planet_types, activity_level, min_activity_score, max_activity_score, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Cryovolcanic Ice World', 'Ice world with cryovolcanic activity',
     'Ice World,Ocean Planet',
     'Moderately Active', 0.3, 2.0, 0.3, 3.0, 70);

INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value) VALUES
                                                                                                                 (6, 'TECTONICS', 'Ice Shell', NULL, NULL),
                                                                                                                 (6, 'VOLCANISM_TYPE', 'Cryovolcanic', NULL, NULL),
                                                                                                                 (6, 'VOLCANIC_ACTIVITY', 'true', NULL, NULL),
                                                                                                                 (6, 'VOLCANIC_INTENSITY', 'Moderate', NULL, NULL),
                                                                                                                 (6, 'ACTIVE_VOLCANOES', 'numeric', 5, 30),
                                                                                                                 (6, 'MOUNTAIN_COVERAGE', 'numeric', 8.0, 18.0),
                                                                                                                 (6, 'MAX_ELEVATION', 'numeric', 3.0, 8.0),
                                                                                                                 (6, 'TERRAIN_ROUGHNESS', 'numeric', 3.0, 6.0),
                                                                                                                 (6, 'CRATERING_LEVEL', 'Light', NULL, NULL),
                                                                                                                 (6, 'VISIBLE_CRATERS', 'numeric', 100, 5000),
                                                                                                                 (6, 'EROSION_LEVEL', 'Moderate', NULL, NULL),
                                                                                                                 (6, 'EROSION_AGENT', 'Ice', NULL, NULL);

-- ============================================================================
-- GAS GIANT TEMPLATES
-- ============================================================================

-- 7. GAS GIANT - LOW ACTIVITY
INSERT INTO ref.geological_template
(name, description, planet_types, activity_level, min_activity_score, max_activity_score, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Calm Gas Giant', 'Gas giant with minimal atmospheric disturbances',
     'Mini-Neptune,Sub-Neptune,Gas Giant,Ice Giant',
     'Low Activity', 0.0, 0.5, 10.0, 100.0, 80);

INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value) VALUES
                                                                                                                 (7, 'TECTONICS', 'N/A', NULL, NULL),
                                                                                                                 (7, 'VOLCANISM_TYPE', 'Atmospheric', NULL, NULL),
                                                                                                                 (7, 'ATMOSPHERIC_CONVECTION', 'Minimal', NULL, NULL),
                                                                                                                 (7, 'GREAT_STORM', 'false', NULL, NULL),
                                                                                                                 (7, 'MAJOR_STORMS', 'numeric', 0, 2),
                                                                                                                 (7, 'TERRAIN_ROUGHNESS', 'numeric', 3.0, 5.0);

-- 8. GAS GIANT - MODERATE ACTIVITY
INSERT INTO ref.geological_template
(name, description, planet_types, activity_level, min_activity_score, max_activity_score, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Active Gas Giant', 'Gas giant with moderate storms and atmospheric activity',
     'Gas Giant,Ice Giant,Sub-Neptune',
     'Moderately Active', 0.5, 2.0, 80.0, 400.0, 120);

INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value) VALUES
                                                                                                                 (8, 'TECTONICS', 'N/A', NULL, NULL),
                                                                                                                 (8, 'VOLCANISM_TYPE', 'Atmospheric', NULL, NULL),
                                                                                                                 (8, 'ATMOSPHERIC_CONVECTION', 'Moderate', NULL, NULL),
                                                                                                                 (8, 'GREAT_STORM', 'random_50', NULL, NULL),
                                                                                                                 (8, 'MAJOR_STORMS', 'numeric', 2, 10),
                                                                                                                 (8, 'TERRAIN_ROUGHNESS', 'numeric', 6.0, 8.0);

-- 9. GAS GIANT - HIGH ACTIVITY
INSERT INTO ref.geological_template
(name, description, planet_types, activity_level, min_activity_score, max_activity_score, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Turbulent Super-Jupiter', 'Massive gas giant with extreme storms and atmospheric chaos',
     'Gas Giant,Super-Jupiter,Hot Jupiter',
     'Highly Active', 2.0, 100.0, 300.0, 3000.0, 60);

INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value) VALUES
                                                                                                                 (9, 'TECTONICS', 'N/A', NULL, NULL),
                                                                                                                 (9, 'VOLCANISM_TYPE', 'Atmospheric', NULL, NULL),
                                                                                                                 (9, 'ATMOSPHERIC_CONVECTION', 'Extreme', NULL, NULL),
                                                                                                                 (9, 'GREAT_STORM', 'true', NULL, NULL),
                                                                                                                 (9, 'MAJOR_STORMS', 'numeric', 5, 20),
                                                                                                                 (9, 'TERRAIN_ROUGHNESS', 'numeric', 9.0, 10.0);

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Count templates
SELECT COUNT(*) as template_count FROM ref.geological_template;
-- Expected: 9

-- Count features
SELECT COUNT(*) as feature_count FROM ref.geological_template_feature;
-- Expected: 80+

-- Show all templates with feature counts
SELECT
    t.id,
    t.name,
    t.activity_level,
    COUNT(f.id) as num_features
FROM ref.geological_template t
         LEFT JOIN ref.geological_template_feature f ON t.id = f.template_id
GROUP BY t.id, t.name, t.activity_level
ORDER BY t.id;