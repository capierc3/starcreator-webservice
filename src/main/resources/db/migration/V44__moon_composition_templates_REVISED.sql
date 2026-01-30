-- V39__moon_composition_templates.sql
-- Adds composition templates specifically for moons
-- Extends the existing composition system to support moon-specific templates
--
-- TEMPLATE SELECTION LOGIC:
-- 1. Temperature-based filtering (uses moon.getSurfaceTemp() from calculateTidalEffects)
-- 2. Type-based filtering using actual moon.getMoonType() values from moon_type_ref
-- 3. Activity-based filtering:
--    - "Volcanic Moon" type only for HIGH/MODERATE geological activity + VOLCANIC atmosphere
--    - "Cryovolcanic Moon" type for icy moons with cryovolcanism
--    - Moon type strings from DB: REGULAR_LARGE, REGULAR_MEDIUM, REGULAR_SMALL, COLLISION_DEBRIS, SHEPHERD, TROJAN, IRREGULAR_CAPTURED
--
-- This ensures each moon type from the DB gets appropriate composition templates
--

-- =====================================================================
-- SECTION 1: Add moon composition columns to moon table
-- =====================================================================

ALTER TABLE ud.moon
    ADD COLUMN interior_composition VARCHAR(500),
    ADD COLUMN envelope_composition VARCHAR(500),
    ADD COLUMN composition_classification VARCHAR(50);

COMMENT ON COLUMN ud.moon.interior_composition IS 'Interior/core composition (similar to planets)';
COMMENT ON COLUMN ud.moon.envelope_composition IS 'Surface/crust composition';
COMMENT ON COLUMN ud.moon.composition_classification IS 'Broad compositional category';

-- =====================================================================
-- SECTION 2: Add moon-specific composition templates
-- =====================================================================

-- REGULAR_LARGE Moons (Ganymede, Titan, Callisto size - 0.01 to 0.025 Earth masses)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES 
    ('Moon - Large Regular Rocky', 'Large regular moon with rocky composition', 'SILICATE_RICH', 'REGULAR_LARGE', 140, 100, 400),
    ('Moon - Large Regular Icy', 'Large regular moon with thick ice layers', 'ICE_RICH', 'REGULAR_LARGE', 180, 30, 150);

-- REGULAR_MEDIUM Moons (Io, Europa, Triton size - 0.001 to 0.01 Earth masses)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES 
    ('Moon - Medium Rocky', 'Medium-sized rocky moon', 'SILICATE_RICH', 'REGULAR_MEDIUM', 150, 80, 400),
    ('Moon - Medium Icy', 'Medium-sized icy moon', 'ICE_RICH', 'REGULAR_MEDIUM', 170, 30, 150);

-- REGULAR_SMALL Moons (Mimas, Enceladus size - 0.00001 to 0.001 Earth masses)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES 
    ('Moon - Small Rocky', 'Small regular rocky moon', 'SILICATE_RICH', 'REGULAR_SMALL', 130, 80, 400),
    ('Moon - Small Icy', 'Small icy moon', 'ICE_RICH', 'REGULAR_SMALL', 200, 30, 150);

-- COLLISION_DEBRIS (Earth's Moon type - 0.001 to 0.02 Earth masses)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES 
    ('Moon - Collision Debris', 'Moon formed from giant impact (Earth Moon-like)', 'SILICATE_RICH', 'COLLISION_DEBRIS', 200, 50, 400);

-- Volcanic/Active Moons (Special type for volcanically active - requires HIGH/MODERATE activity + VOLCANIC atmosphere)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES 
    ('Moon - Volcanic Active', 'Volcanically active moon like Io', 'SILICATE_RICH', 'Volcanic Moon', 200, 200, 600);

-- Cryovolcanic Moons (Special type for icy moons with cryovolcanism - Europa, Enceladus, Triton)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES 
    ('Moon - Ice Shell Ocean', 'Subsurface ocean beneath ice shell (Europa/Enceladus)', 'OCEAN_WORLD', 'Cryovolcanic Moon', 180, 30, 150),
    ('Moon - Nitrogen Ice Cryo', 'Cold cryovolcanic moon (Triton-like)', 'ICE_RICH', 'Cryovolcanic Moon', 120, 30, 80);

-- SHEPHERD Moons (tiny ring shepherds - 0.0000000001 to 0.00001 Earth masses)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES 
    ('Moon - Shepherd Tiny', 'Tiny shepherd moon maintaining ring structure', 'SILICATE_RICH', 'SHEPHERD', 200, 50, 300);

-- TROJAN Moons (at Lagrange points - 0.0000001 to 0.0001 Earth masses)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES 
    ('Moon - Trojan Rocky', 'Rocky moon at Lagrange point', 'SILICATE_RICH', 'TROJAN', 150, 50, 300),
    ('Moon - Trojan Icy', 'Icy moon at Lagrange point', 'ICE_RICH', 'TROJAN', 100, 30, 150);

-- IRREGULAR_CAPTURED Moons (0.000000001 to 0.0001 Earth masses)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES 
    ('Moon - Captured Asteroid', 'Irregular captured asteroid', 'SILICATE_RICH', 'IRREGULAR_CAPTURED', 180, 50, 300),
    ('Moon - Captured Comet', 'Icy captured object', 'ICE_RICH', 'IRREGULAR_CAPTURED', 120, 30, 150);

-- Metal Rich (Special type for high-density moons)
INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES 
    ('Moon - Metal Rich', 'High metal content moon', 'IRON_RICH', 'Metal Rich Moon', 150, 50, 500);

-- =====================================================================
-- SECTION 3: Add components for REGULAR moon types
-- =====================================================================

-- Moon - Large Regular Rocky
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35.0, 45.0 FROM ref.composition_template WHERE name = 'Moon - Large Regular Rocky'
UNION ALL
SELECT id, 'PYROXENE', 'INTERIOR', 25.0, 35.0 FROM ref.composition_template WHERE name = 'Moon - Large Regular Rocky'
UNION ALL
SELECT id, 'IRON', 'INTERIOR', 15.0, 25.0 FROM ref.composition_template WHERE name = 'Moon - Large Regular Rocky'
UNION ALL
SELECT id, 'PLAGIOCLASE', 'ENVELOPE', 40.0, 60.0 FROM ref.composition_template WHERE name = 'Moon - Large Regular Rocky'
UNION ALL
SELECT id, 'BASALT', 'ENVELOPE', 30.0, 50.0 FROM ref.composition_template WHERE name = 'Moon - Large Regular Rocky';

-- Moon - Large Regular Icy
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 30.0, 40.0 FROM ref.composition_template WHERE name = 'Moon - Large Regular Icy'
UNION ALL
SELECT id, 'PYROXENE', 'INTERIOR', 25.0, 35.0 FROM ref.composition_template WHERE name = 'Moon - Large Regular Icy'
UNION ALL
SELECT id, 'IRON', 'INTERIOR', 15.0, 25.0 FROM ref.composition_template WHERE name = 'Moon - Large Regular Icy'
UNION ALL
SELECT id, 'WATER_ICE', 'ENVELOPE', 80.0, 95.0 FROM ref.composition_template WHERE name = 'Moon - Large Regular Icy'
UNION ALL
SELECT id, 'SILICATE', 'ENVELOPE', 3.0, 10.0 FROM ref.composition_template WHERE name = 'Moon - Large Regular Icy';

-- Moon - Medium Rocky
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 40.0, 50.0 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky'
UNION ALL
SELECT id, 'PYROXENE', 'INTERIOR', 25.0, 35.0 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky'
UNION ALL
SELECT id, 'IRON', 'INTERIOR', 10.0, 20.0 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky'
UNION ALL
SELECT id, 'BASALT', 'ENVELOPE', 50.0, 70.0 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky'
UNION ALL
SELECT id, 'PLAGIOCLASE', 'ENVELOPE', 25.0, 40.0 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky';

-- Moon - Medium Icy
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35.0, 45.0 FROM ref.composition_template WHERE name = 'Moon - Medium Icy'
UNION ALL
SELECT id, 'PYROXENE', 'INTERIOR', 25.0, 35.0 FROM ref.composition_template WHERE name = 'Moon - Medium Icy'
UNION ALL
SELECT id, 'IRON', 'INTERIOR', 15.0, 25.0 FROM ref.composition_template WHERE name = 'Moon - Medium Icy'
UNION ALL
SELECT id, 'WATER_ICE', 'ENVELOPE', 75.0, 90.0 FROM ref.composition_template WHERE name = 'Moon - Medium Icy'
UNION ALL
SELECT id, 'SILICATE', 'ENVELOPE', 5.0, 15.0 FROM ref.composition_template WHERE name = 'Moon - Medium Icy';

-- Moon - Small Rocky
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 40.0, 55.0 FROM ref.composition_template WHERE name = 'Moon - Small Rocky'
UNION ALL
SELECT id, 'PYROXENE', 'INTERIOR', 25.0, 35.0 FROM ref.composition_template WHERE name = 'Moon - Small Rocky'
UNION ALL
SELECT id, 'IRON', 'INTERIOR', 8.0, 18.0 FROM ref.composition_template WHERE name = 'Moon - Small Rocky'
UNION ALL
SELECT id, 'REGOLITH', 'ENVELOPE', 50.0, 70.0 FROM ref.composition_template WHERE name = 'Moon - Small Rocky'
UNION ALL
SELECT id, 'BASALT', 'ENVELOPE', 25.0, 40.0 FROM ref.composition_template WHERE name = 'Moon - Small Rocky';

-- Moon - Small Icy
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 30.0, 40.0 FROM ref.composition_template WHERE name = 'Moon - Small Icy'
UNION ALL
SELECT id, 'PYROXENE', 'INTERIOR', 20.0, 30.0 FROM ref.composition_template WHERE name = 'Moon - Small Icy'
UNION ALL
SELECT id, 'WATER_ICE', 'INTERIOR', 25.0, 40.0 FROM ref.composition_template WHERE name = 'Moon - Small Icy'
UNION ALL
SELECT id, 'WATER_ICE', 'ENVELOPE', 85.0, 95.0 FROM ref.composition_template WHERE name = 'Moon - Small Icy'
UNION ALL
SELECT id, 'SILICATE', 'ENVELOPE', 3.0, 10.0 FROM ref.composition_template WHERE name = 'Moon - Small Icy';

-- =====================================================================
-- SECTION 4: Add components for COLLISION_DEBRIS
-- =====================================================================

-- Moon - Collision Debris (Earth's Moon-like)
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35.0, 45.0 FROM ref.composition_template WHERE name = 'Moon - Collision Debris'
UNION ALL
SELECT id, 'PYROXENE', 'INTERIOR', 25.0, 35.0 FROM ref.composition_template WHERE name = 'Moon - Collision Debris'
UNION ALL
SELECT id, 'IRON', 'INTERIOR', 10.0, 20.0 FROM ref.composition_template WHERE name = 'Moon - Collision Debris'
UNION ALL
SELECT id, 'PLAGIOCLASE', 'ENVELOPE', 40.0, 60.0 FROM ref.composition_template WHERE name = 'Moon - Collision Debris'
UNION ALL
SELECT id, 'BASALT', 'ENVELOPE', 30.0, 50.0 FROM ref.composition_template WHERE name = 'Moon - Collision Debris';

-- =====================================================================
-- SECTION 5: Add components for volcanic and cryovolcanic moons
-- =====================================================================

-- Moon - Volcanic Active (Io-like)
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 40.0, 50.0 FROM ref.composition_template WHERE name = 'Moon - Volcanic Active'
UNION ALL
SELECT id, 'PYROXENE', 'INTERIOR', 30.0, 40.0 FROM ref.composition_template WHERE name = 'Moon - Volcanic Active'
UNION ALL
SELECT id, 'IRON', 'INTERIOR', 5.0, 15.0 FROM ref.composition_template WHERE name = 'Moon - Volcanic Active'
UNION ALL
SELECT id, 'SULFUR', 'ENVELOPE', 30.0, 50.0 FROM ref.composition_template WHERE name = 'Moon - Volcanic Active'
UNION ALL
SELECT id, 'BASALT', 'ENVELOPE', 40.0, 60.0 FROM ref.composition_template WHERE name = 'Moon - Volcanic Active';

-- Moon - Ice Shell Ocean (Europa/Enceladus-like)
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 40.0, 50.0 FROM ref.composition_template WHERE name = 'Moon - Ice Shell Ocean'
UNION ALL
SELECT id, 'PYROXENE', 'INTERIOR', 25.0, 35.0 FROM ref.composition_template WHERE name = 'Moon - Ice Shell Ocean'
UNION ALL
SELECT id, 'IRON', 'INTERIOR', 15.0, 25.0 FROM ref.composition_template WHERE name = 'Moon - Ice Shell Ocean'
UNION ALL
SELECT id, 'WATER_ICE', 'ENVELOPE', 70.0, 90.0 FROM ref.composition_template WHERE name = 'Moon - Ice Shell Ocean'
UNION ALL
SELECT id, 'SILICATE', 'ENVELOPE', 5.0, 15.0 FROM ref.composition_template WHERE name = 'Moon - Ice Shell Ocean';

-- Moon - Nitrogen Ice Cryo (Triton-like)
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 30.0, 40.0 FROM ref.composition_template WHERE name = 'Moon - Nitrogen Ice Cryo'
UNION ALL
SELECT id, 'PYROXENE', 'INTERIOR', 25.0, 35.0 FROM ref.composition_template WHERE name = 'Moon - Nitrogen Ice Cryo'
UNION ALL
SELECT id, 'WATER_ICE', 'INTERIOR', 20.0, 30.0 FROM ref.composition_template WHERE name = 'Moon - Nitrogen Ice Cryo'
UNION ALL
SELECT id, 'NITROGEN_ICE', 'ENVELOPE', 40.0, 60.0 FROM ref.composition_template WHERE name = 'Moon - Nitrogen Ice Cryo'
UNION ALL
SELECT id, 'METHANE_ICE', 'ENVELOPE', 20.0, 40.0 FROM ref.composition_template WHERE name = 'Moon - Nitrogen Ice Cryo'
UNION ALL
SELECT id, 'WATER_ICE', 'ENVELOPE', 10.0, 20.0 FROM ref.composition_template WHERE name = 'Moon - Nitrogen Ice Cryo';

-- =====================================================================
-- SECTION 6: Add components for SHEPHERD and TROJAN
-- =====================================================================

-- Moon - Shepherd Tiny
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35.0, 50.0 FROM ref.composition_template WHERE name = 'Moon - Shepherd Tiny'
UNION ALL
SELECT id, 'PYROXENE', 'INTERIOR', 25.0, 40.0 FROM ref.composition_template WHERE name = 'Moon - Shepherd Tiny'
UNION ALL
SELECT id, 'IRON', 'INTERIOR', 10.0, 25.0 FROM ref.composition_template WHERE name = 'Moon - Shepherd Tiny'
UNION ALL
SELECT id, 'REGOLITH', 'ENVELOPE', 60.0, 80.0 FROM ref.composition_template WHERE name = 'Moon - Shepherd Tiny'
UNION ALL
SELECT id, 'BASALT', 'ENVELOPE', 15.0, 30.0 FROM ref.composition_template WHERE name = 'Moon - Shepherd Tiny';

-- Moon - Trojan Rocky
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35.0, 50.0 FROM ref.composition_template WHERE name = 'Moon - Trojan Rocky'
UNION ALL
SELECT id, 'PYROXENE', 'INTERIOR', 20.0, 35.0 FROM ref.composition_template WHERE name = 'Moon - Trojan Rocky'
UNION ALL
SELECT id, 'IRON', 'INTERIOR', 15.0, 30.0 FROM ref.composition_template WHERE name = 'Moon - Trojan Rocky'
UNION ALL
SELECT id, 'REGOLITH', 'ENVELOPE', 50.0, 70.0 FROM ref.composition_template WHERE name = 'Moon - Trojan Rocky'
UNION ALL
SELECT id, 'BASALT', 'ENVELOPE', 20.0, 40.0 FROM ref.composition_template WHERE name = 'Moon - Trojan Rocky';

-- Moon - Trojan Icy
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'WATER_ICE', 'INTERIOR', 50.0, 70.0 FROM ref.composition_template WHERE name = 'Moon - Trojan Icy'
UNION ALL
SELECT id, 'SILICATE', 'INTERIOR', 20.0, 35.0 FROM ref.composition_template WHERE name = 'Moon - Trojan Icy'
UNION ALL
SELECT id, 'ORGANIC', 'INTERIOR', 5.0, 15.0 FROM ref.composition_template WHERE name = 'Moon - Trojan Icy'
UNION ALL
SELECT id, 'WATER_ICE', 'ENVELOPE', 70.0, 90.0 FROM ref.composition_template WHERE name = 'Moon - Trojan Icy'
UNION ALL
SELECT id, 'METHANE_ICE', 'ENVELOPE', 5.0, 20.0 FROM ref.composition_template WHERE name = 'Moon - Trojan Icy';

-- =====================================================================
-- SECTION 7: Add components for IRREGULAR_CAPTURED
-- =====================================================================

-- Moon - Captured Asteroid
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35.0, 50.0 FROM ref.composition_template WHERE name = 'Moon - Captured Asteroid'
UNION ALL
SELECT id, 'PYROXENE', 'INTERIOR', 20.0, 35.0 FROM ref.composition_template WHERE name = 'Moon - Captured Asteroid'
UNION ALL
SELECT id, 'IRON', 'INTERIOR', 15.0, 30.0 FROM ref.composition_template WHERE name = 'Moon - Captured Asteroid'
UNION ALL
SELECT id, 'REGOLITH', 'ENVELOPE', 60.0, 80.0 FROM ref.composition_template WHERE name = 'Moon - Captured Asteroid'
UNION ALL
SELECT id, 'BASALT', 'ENVELOPE', 15.0, 30.0 FROM ref.composition_template WHERE name = 'Moon - Captured Asteroid';

-- Moon - Captured Comet
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'WATER_ICE', 'INTERIOR', 50.0, 70.0 FROM ref.composition_template WHERE name = 'Moon - Captured Comet'
UNION ALL
SELECT id, 'SILICATE', 'INTERIOR', 20.0, 35.0 FROM ref.composition_template WHERE name = 'Moon - Captured Comet'
UNION ALL
SELECT id, 'ORGANIC', 'INTERIOR', 5.0, 15.0 FROM ref.composition_template WHERE name = 'Moon - Captured Comet'
UNION ALL
SELECT id, 'WATER_ICE', 'ENVELOPE', 60.0, 80.0 FROM ref.composition_template WHERE name = 'Moon - Captured Comet'
UNION ALL
SELECT id, 'METHANE_ICE', 'ENVELOPE', 10.0, 25.0 FROM ref.composition_template WHERE name = 'Moon - Captured Comet';

-- =====================================================================
-- SECTION 8: Add components for Metal Rich
-- =====================================================================

-- Moon - Metal Rich
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'IRON', 'INTERIOR', 50.0, 70.0 FROM ref.composition_template WHERE name = 'Moon - Metal Rich'
UNION ALL
SELECT id, 'NICKEL', 'INTERIOR', 10.0, 20.0 FROM ref.composition_template WHERE name = 'Moon - Metal Rich'
UNION ALL
SELECT id, 'OLIVINE', 'INTERIOR', 10.0, 20.0 FROM ref.composition_template WHERE name = 'Moon - Metal Rich'
UNION ALL
SELECT id, 'IRON', 'ENVELOPE', 30.0, 50.0 FROM ref.composition_template WHERE name = 'Moon - Metal Rich'
UNION ALL
SELECT id, 'BASALT', 'ENVELOPE', 20.0, 40.0 FROM ref.composition_template WHERE name = 'Moon - Metal Rich';

-- =====================================================================
-- SECTION 9: Create index for moon composition queries
-- =====================================================================

CREATE INDEX idx_moon_composition_classification ON ud.moon(composition_classification);
