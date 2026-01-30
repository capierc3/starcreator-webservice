-- V50__moon_geological_system.sql
-- Extends existing geological_template system to support moons
-- Uses existing tables and moon's already-calculated geologicalActivity field

-- =====================================================================
-- SECTION 1: Add Geological Columns to Moon Table
-- =====================================================================

ALTER TABLE ud.moon
    ADD COLUMN mountain_coverage_percent DOUBLE PRECISION,
    ADD COLUMN average_elevation_km DOUBLE PRECISION,
    ADD COLUMN max_elevation_km DOUBLE PRECISION,
    ADD COLUMN min_elevation_km DOUBLE PRECISION,
    ADD COLUMN terrain_roughness DOUBLE PRECISION,
    ADD COLUMN erosion_level VARCHAR(30),
    ADD COLUMN primary_erosion_agent VARCHAR(50),
    ADD COLUMN volcanism_type VARCHAR(50),
    ADD COLUMN volcanic_intensity VARCHAR(30),
    ADD COLUMN estimated_active_volcanoes INTEGER;

COMMENT ON COLUMN ud.moon.mountain_coverage_percent IS 'Percentage of surface covered by significant mountains';
COMMENT ON COLUMN ud.moon.average_elevation_km IS 'Average surface elevation in kilometers';
COMMENT ON COLUMN ud.moon.max_elevation_km IS 'Maximum elevation (highest peak) in kilometers';
COMMENT ON COLUMN ud.moon.min_elevation_km IS 'Minimum elevation (deepest depression) in kilometers';
COMMENT ON COLUMN ud.moon.terrain_roughness IS 'Terrain roughness scale (0-10, smooth to extremely rough)';
COMMENT ON COLUMN ud.moon.erosion_level IS 'Level of surface erosion (None, Minimal, Moderate, Heavy)';
COMMENT ON COLUMN ud.moon.primary_erosion_agent IS 'Primary erosion mechanism (None, Ice, Tidal, Cryovolcanic, etc.)';
COMMENT ON COLUMN ud.moon.volcanism_type IS 'Type of volcanism (Silicate, Cryovolcanic, None)';
COMMENT ON COLUMN ud.moon.volcanic_intensity IS 'Volcanic activity intensity (None, Rare, Moderate, Frequent, Continuous)';
COMMENT ON COLUMN ud.moon.estimated_active_volcanoes IS 'Estimated number of currently active volcanic features';

-- =====================================================================
-- SECTION 2: Add Moon-Specific Geological Templates
-- =====================================================================

-- Template 1: Dead Moon (geologicalActivity = "NONE")
INSERT INTO ref.geological_template
(name, description, planet_types, activity_level, min_activity_score, max_activity_score,
 min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Moon - Ancient Dead', 'Geologically dead moon with extreme cratering',
     'REGULAR_SMALL,REGULAR_MEDIUM,IRREGULAR_CAPTURED,TROJAN,COLLISION_DEBRIS',
     'Dead', 0.0, 100.0, 0.0, 1.0, 300);

-- Template 2: Low Activity Moon (geologicalActivity = "LOW")
INSERT INTO ref.geological_template
(name, description, planet_types, activity_level, min_activity_score, max_activity_score,
 min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Moon - Old Inactive', 'Inactive moon with moderate cratering',
     'REGULAR_MEDIUM,REGULAR_LARGE,COLLISION_DEBRIS',
     'Low Activity', 0.0, 100.0, 0.0, 1.0, 200);

-- Template 3: Moderately Active Rocky Moon (geologicalActivity = "MODERATE")
INSERT INTO ref.geological_template
(name, description, planet_types, activity_level, min_activity_score, max_activity_score,
 min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Moon - Active Rocky', 'Tidally heated moon with volcanic activity',
     'REGULAR_MEDIUM,REGULAR_LARGE,COLLISION_DEBRIS',
     'Moderately Active', 0.0, 100.0, 0.0, 1.0, 150);

-- Template 4: Highly Active Volcanic Moon (geologicalActivity = "HIGH")
INSERT INTO ref.geological_template
(name, description, planet_types, activity_level, min_activity_score, max_activity_score,
 min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Moon - Hyperactive Volcanic', 'Extremely active volcanic moon (Io-like)',
     'REGULAR_MEDIUM,REGULAR_LARGE',
     'Highly Active', 0.0, 100.0, 0.0, 1.0, 50);

-- Template 5: Dead Icy Moon (geologicalActivity = "NONE")
INSERT INTO ref.geological_template
(name, description, planet_types, activity_level, min_activity_score, max_activity_score,
 min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Moon - Inactive Icy', 'Cold icy moon with no cryovolcanic activity',
     'REGULAR_SMALL,REGULAR_MEDIUM,IRREGULAR_CAPTURED,TROJAN',
     'Dead', 0.0, 100.0, 0.0, 1.0, 250);

-- Template 6: Moderately Active Cryovolcanic Moon (geologicalActivity = "MODERATE")
INSERT INTO ref.geological_template
(name, description, planet_types, activity_level, min_activity_score, max_activity_score,
 min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Moon - Cryovolcanic', 'Active icy moon with cryovolcanic features (Europa/Enceladus-like)',
     'REGULAR_MEDIUM,REGULAR_LARGE',
     'Moderately Active', 0.0, 100.0, 0.0, 1.0, 100);

-- Template 7: Highly Active Cryovolcanic Moon (geologicalActivity = "HIGH")
INSERT INTO ref.geological_template
(name, description, planet_types, activity_level, min_activity_score, max_activity_score,
 min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Moon - Hyperactive Cryovolcanic', 'Extremely active icy moon with powerful geysers',
     'REGULAR_MEDIUM,REGULAR_LARGE',
     'Highly Active', 0.0, 100.0, 0.0, 1.0, 40);

-- =====================================================================
-- SECTION 3: Add Features for Moon Templates
-- =====================================================================

-- Features for 'Moon - Ancient Dead'
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'VOLCANISM_TYPE', 'None', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Ancient Dead'
UNION ALL SELECT id, 'VOLCANIC_INTENSITY', 'None', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Ancient Dead'
UNION ALL SELECT id, 'ACTIVE_VOLCANOES', 'numeric', 0::double precision, 0::double precision FROM ref.geological_template WHERE name = 'Moon - Ancient Dead'
UNION ALL SELECT id, 'MOUNTAIN_COVERAGE', 'numeric', 1::double precision, 5::double precision FROM ref.geological_template WHERE name = 'Moon - Ancient Dead'
UNION ALL SELECT id, 'MAX_ELEVATION', 'numeric', 0.3, 2.0 FROM ref.geological_template WHERE name = 'Moon - Ancient Dead'
UNION ALL SELECT id, 'TERRAIN_ROUGHNESS', 'numeric', 0.5, 2.0 FROM ref.geological_template WHERE name = 'Moon - Ancient Dead'
UNION ALL SELECT id, 'EROSION_LEVEL', 'None', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Ancient Dead'
UNION ALL SELECT id, 'EROSION_AGENT', 'None', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Ancient Dead';

-- Features for 'Moon - Old Inactive'
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'VOLCANISM_TYPE', 'None', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Old Inactive'
UNION ALL SELECT id, 'VOLCANIC_INTENSITY', 'None', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Old Inactive'
UNION ALL SELECT id, 'ACTIVE_VOLCANOES', 'numeric', 0::double precision, 0::double precision FROM ref.geological_template WHERE name = 'Moon - Old Inactive'
UNION ALL SELECT id, 'MOUNTAIN_COVERAGE', 'numeric', 3::double precision, 10::double precision FROM ref.geological_template WHERE name = 'Moon - Old Inactive'
UNION ALL SELECT id, 'MAX_ELEVATION', 'numeric', 1.0, 4.0 FROM ref.geological_template WHERE name = 'Moon - Old Inactive'
UNION ALL SELECT id, 'TERRAIN_ROUGHNESS', 'numeric', 1.5, 3.0 FROM ref.geological_template WHERE name = 'Moon - Old Inactive'
UNION ALL SELECT id, 'EROSION_LEVEL', 'Minimal', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Old Inactive'
UNION ALL SELECT id, 'EROSION_AGENT', 'Meteorite', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Old Inactive';

-- Features for 'Moon - Active Rocky'
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'VOLCANISM_TYPE', 'Silicate', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Active Rocky'
UNION ALL SELECT id, 'VOLCANIC_INTENSITY', 'Moderate', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Active Rocky'
UNION ALL SELECT id, 'ACTIVE_VOLCANOES', 'numeric', 5::double precision, 50::double precision FROM ref.geological_template WHERE name = 'Moon - Active Rocky'
UNION ALL SELECT id, 'MOUNTAIN_COVERAGE', 'numeric', 8::double precision, 20::double precision FROM ref.geological_template WHERE name = 'Moon - Active Rocky'
UNION ALL SELECT id, 'MAX_ELEVATION', 'numeric', 3.0, 8.0 FROM ref.geological_template WHERE name = 'Moon - Active Rocky'
UNION ALL SELECT id, 'TERRAIN_ROUGHNESS', 'numeric', 3.0, 5.5 FROM ref.geological_template WHERE name = 'Moon - Active Rocky'
UNION ALL SELECT id, 'EROSION_LEVEL', 'Moderate', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Active Rocky'
UNION ALL SELECT id, 'EROSION_AGENT', 'Volcanic', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Active Rocky';

-- Features for 'Moon - Hyperactive Volcanic'
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'VOLCANISM_TYPE', 'Silicate', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Hyperactive Volcanic'
UNION ALL SELECT id, 'VOLCANIC_INTENSITY', 'Continuous', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Hyperactive Volcanic'
UNION ALL SELECT id, 'ACTIVE_VOLCANOES', 'numeric', 100::double precision, 400::double precision FROM ref.geological_template WHERE name = 'Moon - Hyperactive Volcanic'
UNION ALL SELECT id, 'MOUNTAIN_COVERAGE', 'numeric', 15::double precision, 35::double precision FROM ref.geological_template WHERE name = 'Moon - Hyperactive Volcanic'
UNION ALL SELECT id, 'MAX_ELEVATION', 'numeric', 5.0, 17.0 FROM ref.geological_template WHERE name = 'Moon - Hyperactive Volcanic'
UNION ALL SELECT id, 'TERRAIN_ROUGHNESS', 'numeric', 6.0, 9.0 FROM ref.geological_template WHERE name = 'Moon - Hyperactive Volcanic'
UNION ALL SELECT id, 'EROSION_LEVEL', 'Heavy', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Hyperactive Volcanic'
UNION ALL SELECT id, 'EROSION_AGENT', 'Volcanic', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Hyperactive Volcanic';

-- Features for 'Moon - Inactive Icy'
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'VOLCANISM_TYPE', 'None', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Inactive Icy'
UNION ALL SELECT id, 'VOLCANIC_INTENSITY', 'None', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Inactive Icy'
UNION ALL SELECT id, 'ACTIVE_VOLCANOES', 'numeric', 0::double precision, 0::double precision FROM ref.geological_template WHERE name = 'Moon - Inactive Icy'
UNION ALL SELECT id, 'MOUNTAIN_COVERAGE', 'numeric', 0.5, 4::double precision FROM ref.geological_template WHERE name = 'Moon - Inactive Icy'
UNION ALL SELECT id, 'MAX_ELEVATION', 'numeric', 0.1, 1.5 FROM ref.geological_template WHERE name = 'Moon - Inactive Icy'
UNION ALL SELECT id, 'TERRAIN_ROUGHNESS', 'numeric', 0.3, 1.5 FROM ref.geological_template WHERE name = 'Moon - Inactive Icy'
UNION ALL SELECT id, 'EROSION_LEVEL', 'None', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Inactive Icy'
UNION ALL SELECT id, 'EROSION_AGENT', 'None', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Inactive Icy';

-- Features for 'Moon - Cryovolcanic'
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'VOLCANISM_TYPE', 'Cryovolcanic', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Cryovolcanic'
UNION ALL SELECT id, 'VOLCANIC_INTENSITY', 'Moderate', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Cryovolcanic'
UNION ALL SELECT id, 'ACTIVE_VOLCANOES', 'numeric', 3::double precision, 30::double precision FROM ref.geological_template WHERE name = 'Moon - Cryovolcanic'
UNION ALL SELECT id, 'MOUNTAIN_COVERAGE', 'numeric', 5::double precision, 15::double precision FROM ref.geological_template WHERE name = 'Moon - Cryovolcanic'
UNION ALL SELECT id, 'MAX_ELEVATION', 'numeric', 1.0, 5.0 FROM ref.geological_template WHERE name = 'Moon - Cryovolcanic'
UNION ALL SELECT id, 'TERRAIN_ROUGHNESS', 'numeric', 2.0, 4.5 FROM ref.geological_template WHERE name = 'Moon - Cryovolcanic'
UNION ALL SELECT id, 'EROSION_LEVEL', 'Moderate', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Cryovolcanic'
UNION ALL SELECT id, 'EROSION_AGENT', 'Cryovolcanic', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Cryovolcanic';

-- Features for 'Moon - Hyperactive Cryovolcanic'
INSERT INTO ref.geological_template_feature (template_id, feature_type, feature_value, min_value, max_value)
SELECT id, 'VOLCANISM_TYPE', 'Cryovolcanic', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Hyperactive Cryovolcanic'
UNION ALL SELECT id, 'VOLCANIC_INTENSITY', 'Continuous', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Hyperactive Cryovolcanic'
UNION ALL SELECT id, 'ACTIVE_VOLCANOES', 'numeric', 50::double precision, 200::double precision FROM ref.geological_template WHERE name = 'Moon - Hyperactive Cryovolcanic'
UNION ALL SELECT id, 'MOUNTAIN_COVERAGE', 'numeric', 10::double precision, 25::double precision FROM ref.geological_template WHERE name = 'Moon - Hyperactive Cryovolcanic'
UNION ALL SELECT id, 'MAX_ELEVATION', 'numeric', 2.0, 8.0 FROM ref.geological_template WHERE name = 'Moon - Hyperactive Cryovolcanic'
UNION ALL SELECT id, 'TERRAIN_ROUGHNESS', 'numeric', 4.0, 7.5 FROM ref.geological_template WHERE name = 'Moon - Hyperactive Cryovolcanic'
UNION ALL SELECT id, 'EROSION_LEVEL', 'Heavy', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Hyperactive Cryovolcanic'
UNION ALL SELECT id, 'EROSION_AGENT', 'Cryovolcanic', NULL::double precision, NULL::double precision FROM ref.geological_template WHERE name = 'Moon - Hyperactive Cryovolcanic';

-- =====================================================================
-- SECTION 4: Update Comments
-- =====================================================================

COMMENT ON TABLE ref.geological_template IS 'Pre-defined geological activity templates for planets and moons';
COMMENT ON COLUMN ref.geological_template.planet_types IS 'Comma-separated list of planet types OR moon types (REGULAR_LARGE, COLLISION_DEBRIS, etc.)';
COMMENT ON COLUMN ref.geological_template.activity_level IS 'Activity level - for planets: calculated from mass/age, for moons: maps from geologicalActivity field (NONE→Dead, LOW→Low Activity, MODERATE→Moderately Active, HIGH→Highly Active)';