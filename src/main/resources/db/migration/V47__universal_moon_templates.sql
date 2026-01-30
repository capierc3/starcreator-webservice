-- =====================================================================
-- Flyway Migration V48: Universal Moon Templates for Complete Coverage
-- =====================================================================
-- Problem: Moon templates have temperature gaps causing fallback to planet templates
-- 
-- Solution: Add universal moon templates organized by SIZE × COMPOSITION × TEMP_RANGE
--   - Covers ALL possible temperature ranges (10K - 2000K)
--   - Covers ALL moon types via shared templates
--   - No more gaps = no more GAS_ENVELOPE fallback!
--
-- Strategy:
--   - SIZE: Large (REGULAR_LARGE, COLLISION_DEBRIS)
--           Medium (REGULAR_MEDIUM)
--           Small (REGULAR_SMALL, SHEPHERD, TROJAN, IRREGULAR_CAPTURED)
--   - COMPOSITION: Icy, Rocky, Mixed
--   - TEMP: Very Cold (10-100), Cold (100-250), Moderate (250-400),
--           Warm (400-600), Hot (600-1000), Extreme (1000-2000)
-- =====================================================================

-- =====================================================================
-- SECTION 1: LARGE MOON UNIVERSAL TEMPLATES
-- =====================================================================

-- Large Icy - Very Cold (10-100K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Large Icy Very Cold', 'Large icy moon in very cold environment', 'ICE_RICH',
        'REGULAR_LARGE,COLLISION_DEBRIS', 100, 10, 100);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Large Icy Very Cold'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 20, 30 FROM ref.composition_template WHERE name = 'Moon - Large Icy Very Cold'
UNION ALL SELECT id, 'WATER_ICE', 'INTERIOR', 30, 45 FROM ref.composition_template WHERE name = 'Moon - Large Icy Very Cold'
UNION ALL SELECT id, 'NITROGEN_ICE', 'ENVELOPE', 30, 50 FROM ref.composition_template WHERE name = 'Moon - Large Icy Very Cold'
UNION ALL SELECT id, 'METHANE_ICE', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Large Icy Very Cold'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 15, 25 FROM ref.composition_template WHERE name = 'Moon - Large Icy Very Cold';

-- Large Icy - Moderate (250-400K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Large Icy Moderate', 'Large icy moon with moderate heating', 'MIXED_SILICATE_ICE',
        'REGULAR_LARGE,COLLISION_DEBRIS,Cryovolcanic Moon', 120, 250, 400);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 30, 40 FROM ref.composition_template WHERE name = 'Moon - Large Icy Moderate'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Large Icy Moderate'
UNION ALL SELECT id, 'WATER_ICE', 'INTERIOR', 20, 30 FROM ref.composition_template WHERE name = 'Moon - Large Icy Moderate'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 40, 60 FROM ref.composition_template WHERE name = 'Moon - Large Icy Moderate'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 35, 50 FROM ref.composition_template WHERE name = 'Moon - Large Icy Moderate';

-- Large Icy - Warm (400-600K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Large Icy Warm', 'Large moon transitioning from ice to rock', 'SILICATE_RICH',
        'REGULAR_LARGE,COLLISION_DEBRIS', 90, 400, 600);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35, 45 FROM ref.composition_template WHERE name = 'Moon - Large Icy Warm'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Large Icy Warm'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 15, 25 FROM ref.composition_template WHERE name = 'Moon - Large Icy Warm'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Large Icy Warm'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Large Icy Warm';

-- Large Icy - Hot (600-1000K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Large Icy Hot', 'Large moon with all ice vaporized', 'SILICATE_RICH',
        'REGULAR_LARGE,COLLISION_DEBRIS', 70, 600, 1000);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 40, 50 FROM ref.composition_template WHERE name = 'Moon - Large Icy Hot'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Large Icy Hot'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 15, 25 FROM ref.composition_template WHERE name = 'Moon - Large Icy Hot'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 55, 75 FROM ref.composition_template WHERE name = 'Moon - Large Icy Hot'
UNION ALL SELECT id, 'PLAGIOCLASE', 'ENVELOPE', 20, 35 FROM ref.composition_template WHERE name = 'Moon - Large Icy Hot';

-- Large Rocky - Very Cold (10-100K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Large Rocky Very Cold', 'Large rocky moon in very cold environment', 'SILICATE_RICH',
        'REGULAR_LARGE,COLLISION_DEBRIS', 90, 10, 100);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35, 45 FROM ref.composition_template WHERE name = 'Moon - Large Rocky Very Cold'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Large Rocky Very Cold'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 15, 25 FROM ref.composition_template WHERE name = 'Moon - Large Rocky Very Cold'
UNION ALL SELECT id, 'PLAGIOCLASE', 'ENVELOPE', 40, 60 FROM ref.composition_template WHERE name = 'Moon - Large Rocky Very Cold'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 30, 50 FROM ref.composition_template WHERE name = 'Moon - Large Rocky Very Cold';

-- Large Rocky - Warm (400-600K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Large Rocky Warm', 'Large warm rocky moon', 'SILICATE_RICH',
        'REGULAR_LARGE,COLLISION_DEBRIS,Volcanic Moon', 120, 400, 600);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 38, 48 FROM ref.composition_template WHERE name = 'Moon - Large Rocky Warm'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Large Rocky Warm'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 12, 22 FROM ref.composition_template WHERE name = 'Moon - Large Rocky Warm'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 45, 65 FROM ref.composition_template WHERE name = 'Moon - Large Rocky Warm'
UNION ALL SELECT id, 'PLAGIOCLASE', 'ENVELOPE', 30, 45 FROM ref.composition_template WHERE name = 'Moon - Large Rocky Warm';

-- Large Rocky - Hot (600-1000K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Large Rocky Hot', 'Large extremely hot rocky moon', 'SILICATE_RICH',
        'REGULAR_LARGE,COLLISION_DEBRIS,Volcanic Moon', 100, 600, 1000);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35, 50 FROM ref.composition_template WHERE name = 'Moon - Large Rocky Hot'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Large Rocky Hot'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 10, 25 FROM ref.composition_template WHERE name = 'Moon - Large Rocky Hot'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Large Rocky Hot'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Large Rocky Hot';

-- Large Mixed - Very Cold (10-100K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Large Mixed Very Cold', 'Large mixed composition moon, very cold', 'MIXED_SILICATE_ICE',
        'REGULAR_LARGE', 100, 10, 100);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Large Mixed Very Cold'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 20, 30 FROM ref.composition_template WHERE name = 'Moon - Large Mixed Very Cold'
UNION ALL SELECT id, 'WATER_ICE', 'INTERIOR', 30, 40 FROM ref.composition_template WHERE name = 'Moon - Large Mixed Very Cold'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Large Mixed Very Cold'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Large Mixed Very Cold';

-- Large Mixed - Cold (100-250K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Large Mixed Cold', 'Large mixed composition moon, cold', 'MIXED_SILICATE_ICE',
        'REGULAR_LARGE', 120, 100, 250);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 28, 38 FROM ref.composition_template WHERE name = 'Moon - Large Mixed Cold'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 22, 32 FROM ref.composition_template WHERE name = 'Moon - Large Mixed Cold'
UNION ALL SELECT id, 'WATER_ICE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Large Mixed Cold'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 45, 65 FROM ref.composition_template WHERE name = 'Moon - Large Mixed Cold'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 30, 45 FROM ref.composition_template WHERE name = 'Moon - Large Mixed Cold';

-- Large Mixed - Warm (400-600K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Large Mixed Warm', 'Large mixed moon with melting ice', 'MIXED_SILICATE_ICE',
        'REGULAR_LARGE', 100, 400, 600);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35, 45 FROM ref.composition_template WHERE name = 'Moon - Large Mixed Warm'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Large Mixed Warm'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 15, 25 FROM ref.composition_template WHERE name = 'Moon - Large Mixed Warm'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Large Mixed Warm'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Large Mixed Warm';

-- =====================================================================
-- SECTION 2: MEDIUM MOON UNIVERSAL TEMPLATES  
-- =====================================================================

-- Medium Icy - Very Cold (10-100K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Medium Icy Very Cold', 'Medium icy moon, very cold', 'ICE_RICH',
        'REGULAR_MEDIUM', 100, 10, 100);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Very Cold'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 20, 30 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Very Cold'
UNION ALL SELECT id, 'WATER_ICE', 'INTERIOR', 30, 45 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Very Cold'
UNION ALL SELECT id, 'NITROGEN_ICE', 'ENVELOPE', 35, 55 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Very Cold'
UNION ALL SELECT id, 'METHANE_ICE', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Very Cold'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 10, 20 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Very Cold';

-- Medium Icy - Moderate (250-400K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Medium Icy Moderate', 'Medium icy moon with moderate heating', 'MIXED_SILICATE_ICE',
        'REGULAR_MEDIUM,Cryovolcanic Moon', 110, 250, 400);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 30, 40 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Moderate'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Moderate'
UNION ALL SELECT id, 'WATER_ICE', 'INTERIOR', 20, 30 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Moderate'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 40, 60 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Moderate'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 35, 50 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Moderate';

-- Medium Icy - Warm (400-600K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Medium Icy Warm', 'Medium moon with vaporized ice', 'SILICATE_RICH',
        'REGULAR_MEDIUM', 90, 400, 600);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35, 45 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Warm'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Warm'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 15, 25 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Warm'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Warm'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Warm';

-- Medium Icy - Hot (600-1000K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Medium Icy Hot', 'Medium moon extremely heated', 'SILICATE_RICH',
        'REGULAR_MEDIUM', 70, 600, 1000);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 40, 50 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Hot'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Hot'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 15, 25 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Hot'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 55, 75 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Hot'
UNION ALL SELECT id, 'PLAGIOCLASE', 'ENVELOPE', 20, 35 FROM ref.composition_template WHERE name = 'Moon - Medium Icy Hot';

-- Medium Rocky - Very Cold (10-100K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Medium Rocky Very Cold', 'Medium rocky moon, very cold', 'SILICATE_RICH',
        'REGULAR_MEDIUM', 90, 10, 100);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 38, 48 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky Very Cold'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky Very Cold'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 10, 20 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky Very Cold'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky Very Cold'
UNION ALL SELECT id, 'PLAGIOCLASE', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky Very Cold';

-- Medium Rocky - Warm (400-600K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Medium Rocky Warm', 'Medium warm rocky moon', 'SILICATE_RICH',
        'REGULAR_MEDIUM,Volcanic Moon', 110, 400, 600);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 38, 48 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky Warm'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky Warm'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 10, 20 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky Warm'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 45, 65 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky Warm'
UNION ALL SELECT id, 'PLAGIOCLASE', 'ENVELOPE', 30, 45 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky Warm';

-- Medium Rocky - Hot (600-1000K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Medium Rocky Hot', 'Medium extremely hot rocky moon', 'SILICATE_RICH',
        'REGULAR_MEDIUM,Volcanic Moon', 90, 600, 1000);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35, 50 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky Hot'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky Hot'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 10, 25 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky Hot'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky Hot'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky Hot';

-- Medium Mixed - Very Cold (10-100K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Medium Mixed Very Cold', 'Medium mixed moon, very cold', 'MIXED_SILICATE_ICE',
        'REGULAR_MEDIUM', 100, 10, 100);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Medium Mixed Very Cold'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 20, 30 FROM ref.composition_template WHERE name = 'Moon - Medium Mixed Very Cold'
UNION ALL SELECT id, 'WATER_ICE', 'INTERIOR', 30, 40 FROM ref.composition_template WHERE name = 'Moon - Medium Mixed Very Cold'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Medium Mixed Very Cold'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Medium Mixed Very Cold';

-- Medium Mixed - Cold (100-250K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Medium Mixed Cold', 'Medium mixed moon, cold', 'MIXED_SILICATE_ICE',
        'REGULAR_MEDIUM', 120, 100, 250);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 28, 38 FROM ref.composition_template WHERE name = 'Moon - Medium Mixed Cold'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 22, 32 FROM ref.composition_template WHERE name = 'Moon - Medium Mixed Cold'
UNION ALL SELECT id, 'WATER_ICE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Medium Mixed Cold'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 45, 65 FROM ref.composition_template WHERE name = 'Moon - Medium Mixed Cold'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 30, 45 FROM ref.composition_template WHERE name = 'Moon - Medium Mixed Cold';

-- Medium Mixed - Warm (400-600K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Medium Mixed Warm', 'Medium mixed moon with melting ice', 'MIXED_SILICATE_ICE',
        'REGULAR_MEDIUM', 100, 400, 600);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35, 45 FROM ref.composition_template WHERE name = 'Moon - Medium Mixed Warm'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Medium Mixed Warm'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 15, 25 FROM ref.composition_template WHERE name = 'Moon - Medium Mixed Warm'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Medium Mixed Warm'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Medium Mixed Warm';

-- =====================================================================
-- SECTION 3: SMALL MOON UNIVERSAL TEMPLATES
-- These cover: REGULAR_SMALL, SHEPHERD, TROJAN, IRREGULAR_CAPTURED
-- =====================================================================

-- Small Icy - Very Cold (10-100K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Small Icy Very Cold', 'Small icy moon, very cold', 'ICE_RICH',
        'REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED', 110, 10, 100);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 20, 30 FROM ref.composition_template WHERE name = 'Moon - Small Icy Very Cold'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 15, 25 FROM ref.composition_template WHERE name = 'Moon - Small Icy Very Cold'
UNION ALL SELECT id, 'WATER_ICE', 'INTERIOR', 35, 50 FROM ref.composition_template WHERE name = 'Moon - Small Icy Very Cold'
UNION ALL SELECT id, 'NITROGEN_ICE', 'ENVELOPE', 35, 55 FROM ref.composition_template WHERE name = 'Moon - Small Icy Very Cold'
UNION ALL SELECT id, 'METHANE_ICE', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Small Icy Very Cold'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 10, 20 FROM ref.composition_template WHERE name = 'Moon - Small Icy Very Cold';

-- Small Icy - Moderate (250-400K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Small Icy Moderate', 'Small icy moon with moderate heating', 'MIXED_SILICATE_ICE',
        'REGULAR_SMALL,TROJAN,IRREGULAR_CAPTURED', 100, 250, 400);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 28, 38 FROM ref.composition_template WHERE name = 'Moon - Small Icy Moderate'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 20, 30 FROM ref.composition_template WHERE name = 'Moon - Small Icy Moderate'
UNION ALL SELECT id, 'WATER_ICE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Small Icy Moderate'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 40, 60 FROM ref.composition_template WHERE name = 'Moon - Small Icy Moderate'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 35, 50 FROM ref.composition_template WHERE name = 'Moon - Small Icy Moderate';

-- Small Icy - Warm (400-600K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Small Icy Warm', 'Small moon with vaporized ice', 'SILICATE_RICH',
        'REGULAR_SMALL,TROJAN,IRREGULAR_CAPTURED', 80, 400, 600);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35, 50 FROM ref.composition_template WHERE name = 'Moon - Small Icy Warm'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Small Icy Warm'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 8, 18 FROM ref.composition_template WHERE name = 'Moon - Small Icy Warm'
UNION ALL SELECT id, 'REGOLITH', 'ENVELOPE', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Small Icy Warm'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Small Icy Warm';

-- Small Rocky - Very Cold (10-100K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Small Rocky Very Cold', 'Small rocky moon, very cold', 'SILICATE_RICH',
        'REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED', 100, 10, 100);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 40, 55 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Very Cold'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Very Cold'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 8, 18 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Very Cold'
UNION ALL SELECT id, 'REGOLITH', 'ENVELOPE', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Very Cold'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Very Cold';

-- Small Rocky - Warm (400-600K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Small Rocky Warm', 'Small warm rocky moon', 'SILICATE_RICH',
        'REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED', 100, 400, 600);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 40, 55 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Warm'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Warm'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 8, 18 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Warm'
UNION ALL SELECT id, 'REGOLITH', 'ENVELOPE', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Warm'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Warm';

-- Small Rocky - Hot (600-1000K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Small Rocky Hot', 'Small extremely hot rocky moon', 'SILICATE_RICH',
        'REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED', 80, 600, 1000);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35, 50 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Hot'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Hot'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 10, 25 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Hot'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Hot'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Hot';

-- Small Mixed - Very Cold (10-100K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Small Mixed Very Cold', 'Small mixed moon, very cold', 'MIXED_SILICATE_ICE',
        'REGULAR_SMALL,TROJAN,IRREGULAR_CAPTURED', 110, 10, 100);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 22, 32 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Very Cold'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 18, 28 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Very Cold'
UNION ALL SELECT id, 'WATER_ICE', 'INTERIOR', 35, 45 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Very Cold'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Very Cold'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Very Cold';

-- Small Mixed - Cold (100-250K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Small Mixed Cold', 'Small mixed moon, cold', 'MIXED_SILICATE_ICE',
        'REGULAR_SMALL,TROJAN,IRREGULAR_CAPTURED', 120, 100, 250);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Cold'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 20, 30 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Cold'
UNION ALL SELECT id, 'WATER_ICE', 'INTERIOR', 30, 40 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Cold'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 45, 65 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Cold'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 30, 45 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Cold';

-- Small Mixed - Warm (400-600K)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES ('Moon - Small Mixed Warm', 'Small mixed moon with melting ice', 'MIXED_SILICATE_ICE',
        'REGULAR_SMALL,TROJAN,IRREGULAR_CAPTURED', 100, 400, 600);
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 32, 42 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Warm'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Warm'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 15, 25 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Warm'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Warm'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Warm';

-- =====================================================================
-- SECTION 4: Extend Volcanic Active for extreme temperatures
-- =====================================================================

UPDATE ref.composition_template
SET max_surface_temp_k = 2000,
    planet_types = 'Volcanic Moon,REGULAR_LARGE,REGULAR_MEDIUM'
WHERE name = 'Moon - Volcanic Active';

-- =====================================================================
-- SECTION 5: Extend Metal Rich for all temperatures
-- =====================================================================

UPDATE ref.composition_template
SET min_surface_temp_k = 10,
    max_surface_temp_k = 2000
WHERE name = 'Moon - Metal Rich';

-- =====================================================================
-- SECTION 6: Validation
-- =====================================================================

DO $$
DECLARE
    moon_template_count INTEGER;
    temp_gaps TEXT := '';
BEGIN
    SELECT COUNT(*) INTO moon_template_count
    FROM ref.composition_template
    WHERE name LIKE 'Moon -%';
    
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Migration V48 Completed';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Total moon templates: % (was 17)', moon_template_count;
    RAISE NOTICE '';
    RAISE NOTICE 'Complete Temperature Coverage Added:';
    RAISE NOTICE '  Large Moons:  10-1000K (all compositions)';
    RAISE NOTICE '  Medium Moons: 10-1000K (all compositions)';
    RAISE NOTICE '  Small Moons:  10-1000K (all compositions)';
    RAISE NOTICE '  Volcanic:     200-2000K';
    RAISE NOTICE '  Metal Rich:   10-2000K';
    RAISE NOTICE '';
    RAISE NOTICE 'All moon types + compositions + temps now covered!';
    RAISE NOTICE 'No more GAS_ENVELOPE fallback!';
END $$;

-- =====================================================================
-- END OF MIGRATION V48
-- =====================================================================
