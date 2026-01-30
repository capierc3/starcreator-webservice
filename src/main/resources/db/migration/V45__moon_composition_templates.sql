-- =====================================================================
-- Flyway Migration V45: Moon Composition System Overhaul
-- =====================================================================
-- Description: 
--   1. Removes incomplete moon composition templates from V39 if they exist
--   2. Adds new moon-specific composition templates for all moon types
--   3. Adds moon composition columns if they don't exist
--
-- New Minerals Required in CompositionMineral enum:
--   - PLAGIOCLASE (lunar highlands feldspar)
--   - SILICATE (generic mixed silicates)
--   - REGOLITH (surface debris)
--   - ORGANIC (organic compounds for comets)
--   - THOLINS (complex organic polymers)
-- =====================================================================

-- =====================================================================
-- SECTION 1: Clean up any existing moon composition templates from V39
-- =====================================================================

-- Remove components first (foreign key constraint)
DELETE FROM ref.composition_template_component
WHERE template_id IN (
    SELECT id FROM ref.composition_template 
    WHERE planet_types LIKE '%Moon%'
       OR planet_types LIKE '%REGULAR_%'
       OR planet_types LIKE '%COLLISION_DEBRIS%'
       OR planet_types LIKE '%SHEPHERD%'
       OR planet_types LIKE '%TROJAN%'
       OR planet_types LIKE '%IRREGULAR_CAPTURED%'
       OR planet_types LIKE '%Volcanic Moon%'
       OR planet_types LIKE '%Cryovolcanic Moon%'
       OR planet_types LIKE '%Metal Rich Moon%'
);

-- Remove templates
DELETE FROM ref.composition_template 
WHERE planet_types LIKE '%Moon%'
   OR planet_types LIKE '%REGULAR_%'
   OR planet_types LIKE '%COLLISION_DEBRIS%'
   OR planet_types LIKE '%SHEPHERD%'
   OR planet_types LIKE '%TROJAN%'
   OR planet_types LIKE '%IRREGULAR_CAPTURED%'
   OR planet_types LIKE '%Volcanic Moon%'
   OR planet_types LIKE '%Cryovolcanic Moon%'
   OR planet_types LIKE '%Metal Rich Moon%';

-- =====================================================================
-- SECTION 2: Add moon composition columns if they don't exist
-- =====================================================================

DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'ud' 
                   AND table_name = 'moon' 
                   AND column_name = 'interior_composition') THEN
        ALTER TABLE ud.moon ADD COLUMN interior_composition VARCHAR(500);
        COMMENT ON COLUMN ud.moon.interior_composition IS 'Interior/core composition';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'ud' 
                   AND table_name = 'moon' 
                   AND column_name = 'envelope_composition') THEN
        ALTER TABLE ud.moon ADD COLUMN envelope_composition VARCHAR(500);
        COMMENT ON COLUMN ud.moon.envelope_composition IS 'Surface/crust composition';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'ud' 
                   AND table_name = 'moon' 
                   AND column_name = 'composition_classification') THEN
        ALTER TABLE ud.moon ADD COLUMN composition_classification VARCHAR(50);
        COMMENT ON COLUMN ud.moon.composition_classification IS 'Broad compositional category';
    END IF;
END $$;

-- =====================================================================
-- SECTION 3: Add moon-specific composition templates
-- =====================================================================
-- Templates match moon_type_ref values: REGULAR_LARGE, REGULAR_MEDIUM,
-- REGULAR_SMALL, COLLISION_DEBRIS, SHEPHERD, TROJAN, IRREGULAR_CAPTURED
-- Plus special types: Volcanic Moon, Cryovolcanic Moon, Metal Rich Moon
-- =====================================================================

INSERT INTO ref.composition_template (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES 
    -- REGULAR_LARGE (Ganymede, Titan, Callisto size - 0.01 to 0.025 Earth masses)
    ('Moon - Large Regular Rocky', 'Large regular moon with rocky composition', 'SILICATE_RICH', 'REGULAR_LARGE', 140, 100, 400),
    ('Moon - Large Regular Icy', 'Large regular moon with thick ice layers', 'ICE_RICH', 'REGULAR_LARGE', 180, 30, 150),
    
    -- REGULAR_MEDIUM (Io, Europa, Triton size - 0.001 to 0.01 Earth masses)
    ('Moon - Medium Rocky', 'Medium-sized rocky moon', 'SILICATE_RICH', 'REGULAR_MEDIUM', 150, 80, 400),
    ('Moon - Medium Icy', 'Medium-sized icy moon', 'ICE_RICH', 'REGULAR_MEDIUM', 170, 30, 150),
    
    -- REGULAR_SMALL (Mimas, Enceladus size - 0.00001 to 0.001 Earth masses)
    ('Moon - Small Rocky', 'Small regular rocky moon', 'SILICATE_RICH', 'REGULAR_SMALL', 130, 80, 400),
    ('Moon - Small Icy', 'Small icy moon', 'ICE_RICH', 'REGULAR_SMALL', 200, 30, 150),
    
    -- COLLISION_DEBRIS (Earth's Moon type - 0.001 to 0.02 Earth masses)
    ('Moon - Collision Debris', 'Moon formed from giant impact', 'SILICATE_RICH', 'COLLISION_DEBRIS', 200, 50, 400),
    
    -- Special type: Volcanic Moon (HIGH/MODERATE geological activity + VOLCANIC atmosphere)
    ('Moon - Volcanic Active', 'Volcanically active moon like Io', 'SILICATE_RICH', 'Volcanic Moon', 200, 200, 600),
    
    -- Special type: Cryovolcanic Moon (ICY + hasCryovolcanism = true)
    ('Moon - Ice Shell Ocean', 'Subsurface ocean beneath ice shell (Europa/Enceladus)', 'OCEAN_WORLD', 'Cryovolcanic Moon', 180, 30, 150),
    ('Moon - Nitrogen Ice Cryo', 'Cold cryovolcanic moon (Triton-like)', 'ICE_RICH', 'Cryovolcanic Moon', 120, 30, 80),
    
    -- SHEPHERD (tiny ring shepherds - 0.0000000001 to 0.00001 Earth masses)
    ('Moon - Shepherd Tiny', 'Tiny shepherd moon maintaining ring structure', 'SILICATE_RICH', 'SHEPHERD', 200, 50, 300),
    
    -- TROJAN (Lagrange point - 0.0000001 to 0.0001 Earth masses)
    ('Moon - Trojan Rocky', 'Rocky moon at Lagrange point', 'SILICATE_RICH', 'TROJAN', 150, 50, 300),
    ('Moon - Trojan Icy', 'Icy moon at Lagrange point', 'ICE_RICH', 'TROJAN', 100, 30, 150),
    
    -- IRREGULAR_CAPTURED (0.000000001 to 0.0001 Earth masses)
    ('Moon - Captured Asteroid', 'Irregular captured asteroid', 'SILICATE_RICH', 'IRREGULAR_CAPTURED', 180, 50, 300),
    ('Moon - Captured Comet', 'Icy captured object', 'ICE_RICH', 'IRREGULAR_CAPTURED', 120, 30, 150),
    
    -- Special type: Metal Rich Moon (density > 4.5 g/cm³)
    ('Moon - Metal Rich', 'High metal content moon', 'IRON_RICH', 'Metal Rich Moon', 150, 50, 500);

-- =====================================================================
-- SECTION 4: Add mineral components for each template
-- =====================================================================

-- Large Regular Rocky (PLAGIOCLASE for lunar highlands)
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35, 45 FROM ref.composition_template WHERE name = 'Moon - Large Regular Rocky'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Large Regular Rocky'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 15, 25 FROM ref.composition_template WHERE name = 'Moon - Large Regular Rocky'
UNION ALL SELECT id, 'PLAGIOCLASE', 'ENVELOPE', 40, 60 FROM ref.composition_template WHERE name = 'Moon - Large Regular Rocky'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 30, 50 FROM ref.composition_template WHERE name = 'Moon - Large Regular Rocky';

-- Large Regular Icy (SILICATE for ice moon dust)
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 30, 40 FROM ref.composition_template WHERE name = 'Moon - Large Regular Icy'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Large Regular Icy'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 15, 25 FROM ref.composition_template WHERE name = 'Moon - Large Regular Icy'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 80, 95 FROM ref.composition_template WHERE name = 'Moon - Large Regular Icy'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 3, 10 FROM ref.composition_template WHERE name = 'Moon - Large Regular Icy';

-- Medium Rocky
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 40, 50 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 10, 20 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky'
UNION ALL SELECT id, 'PLAGIOCLASE', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Medium Rocky';

-- Medium Icy
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35, 45 FROM ref.composition_template WHERE name = 'Moon - Medium Icy'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Medium Icy'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 15, 25 FROM ref.composition_template WHERE name = 'Moon - Medium Icy'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 75, 90 FROM ref.composition_template WHERE name = 'Moon - Medium Icy'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 5, 15 FROM ref.composition_template WHERE name = 'Moon - Medium Icy';

-- Small Rocky (REGOLITH for surface debris)
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 40, 55 FROM ref.composition_template WHERE name = 'Moon - Small Rocky'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Small Rocky'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 8, 18 FROM ref.composition_template WHERE name = 'Moon - Small Rocky'
UNION ALL SELECT id, 'REGOLITH', 'ENVELOPE', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Small Rocky'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Small Rocky';

-- Small Icy
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 30, 40 FROM ref.composition_template WHERE name = 'Moon - Small Icy'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 20, 30 FROM ref.composition_template WHERE name = 'Moon - Small Icy'
UNION ALL SELECT id, 'WATER_ICE', 'INTERIOR', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Small Icy'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 85, 95 FROM ref.composition_template WHERE name = 'Moon - Small Icy'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 3, 10 FROM ref.composition_template WHERE name = 'Moon - Small Icy';

-- Collision Debris (Earth's Moon - PLAGIOCLASE)
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35, 45 FROM ref.composition_template WHERE name = 'Moon - Collision Debris'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Collision Debris'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 10, 20 FROM ref.composition_template WHERE name = 'Moon - Collision Debris'
UNION ALL SELECT id, 'PLAGIOCLASE', 'ENVELOPE', 40, 60 FROM ref.composition_template WHERE name = 'Moon - Collision Debris'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 30, 50 FROM ref.composition_template WHERE name = 'Moon - Collision Debris';

-- Volcanic Active (Io-like with sulfur)
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 40, 50 FROM ref.composition_template WHERE name = 'Moon - Volcanic Active'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 30, 40 FROM ref.composition_template WHERE name = 'Moon - Volcanic Active'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 5, 15 FROM ref.composition_template WHERE name = 'Moon - Volcanic Active'
UNION ALL SELECT id, 'SULFUR', 'ENVELOPE', 30, 50 FROM ref.composition_template WHERE name = 'Moon - Volcanic Active'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 40, 60 FROM ref.composition_template WHERE name = 'Moon - Volcanic Active';

-- Ice Shell Ocean (Europa/Enceladus)
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 40, 50 FROM ref.composition_template WHERE name = 'Moon - Ice Shell Ocean'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Ice Shell Ocean'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 15, 25 FROM ref.composition_template WHERE name = 'Moon - Ice Shell Ocean'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 70, 90 FROM ref.composition_template WHERE name = 'Moon - Ice Shell Ocean'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 5, 15 FROM ref.composition_template WHERE name = 'Moon - Ice Shell Ocean';

-- Nitrogen Ice Cryo (Triton-like with THOLINS)
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 30, 40 FROM ref.composition_template WHERE name = 'Moon - Nitrogen Ice Cryo'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Nitrogen Ice Cryo'
UNION ALL SELECT id, 'WATER_ICE', 'INTERIOR', 20, 30 FROM ref.composition_template WHERE name = 'Moon - Nitrogen Ice Cryo'
UNION ALL SELECT id, 'NITROGEN_ICE', 'ENVELOPE', 40, 60 FROM ref.composition_template WHERE name = 'Moon - Nitrogen Ice Cryo'
UNION ALL SELECT id, 'METHANE_ICE', 'ENVELOPE', 20, 40 FROM ref.composition_template WHERE name = 'Moon - Nitrogen Ice Cryo'
UNION ALL SELECT id, 'THOLINS', 'ENVELOPE', 5, 15 FROM ref.composition_template WHERE name = 'Moon - Nitrogen Ice Cryo';

-- Shepherd Tiny (REGOLITH)
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35, 50 FROM ref.composition_template WHERE name = 'Moon - Shepherd Tiny'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 40 FROM ref.composition_template WHERE name = 'Moon - Shepherd Tiny'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 10, 25 FROM ref.composition_template WHERE name = 'Moon - Shepherd Tiny'
UNION ALL SELECT id, 'REGOLITH', 'ENVELOPE', 60, 80 FROM ref.composition_template WHERE name = 'Moon - Shepherd Tiny'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 15, 30 FROM ref.composition_template WHERE name = 'Moon - Shepherd Tiny';

-- Trojan Rocky (REGOLITH)
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35, 50 FROM ref.composition_template WHERE name = 'Moon - Trojan Rocky'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 20, 35 FROM ref.composition_template WHERE name = 'Moon - Trojan Rocky'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 15, 30 FROM ref.composition_template WHERE name = 'Moon - Trojan Rocky'
UNION ALL SELECT id, 'REGOLITH', 'ENVELOPE', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Trojan Rocky'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 20, 40 FROM ref.composition_template WHERE name = 'Moon - Trojan Rocky';

-- Trojan Icy (ORGANIC, SILICATE)
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'WATER_ICE', 'INTERIOR', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Trojan Icy'
UNION ALL SELECT id, 'SILICATE', 'INTERIOR', 20, 35 FROM ref.composition_template WHERE name = 'Moon - Trojan Icy'
UNION ALL SELECT id, 'ORGANIC', 'INTERIOR', 5, 15 FROM ref.composition_template WHERE name = 'Moon - Trojan Icy'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 70, 90 FROM ref.composition_template WHERE name = 'Moon - Trojan Icy'
UNION ALL SELECT id, 'METHANE_ICE', 'ENVELOPE', 5, 20 FROM ref.composition_template WHERE name = 'Moon - Trojan Icy';

-- Captured Asteroid (REGOLITH)
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35, 50 FROM ref.composition_template WHERE name = 'Moon - Captured Asteroid'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 20, 35 FROM ref.composition_template WHERE name = 'Moon - Captured Asteroid'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 15, 30 FROM ref.composition_template WHERE name = 'Moon - Captured Asteroid'
UNION ALL SELECT id, 'REGOLITH', 'ENVELOPE', 60, 80 FROM ref.composition_template WHERE name = 'Moon - Captured Asteroid'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 15, 30 FROM ref.composition_template WHERE name = 'Moon - Captured Asteroid';

-- Captured Comet (ORGANIC, SILICATE)
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'WATER_ICE', 'INTERIOR', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Captured Comet'
UNION ALL SELECT id, 'SILICATE', 'INTERIOR', 20, 35 FROM ref.composition_template WHERE name = 'Moon - Captured Comet'
UNION ALL SELECT id, 'ORGANIC', 'INTERIOR', 5, 15 FROM ref.composition_template WHERE name = 'Moon - Captured Comet'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 60, 80 FROM ref.composition_template WHERE name = 'Moon - Captured Comet'
UNION ALL SELECT id, 'METHANE_ICE', 'ENVELOPE', 10, 25 FROM ref.composition_template WHERE name = 'Moon - Captured Comet';

-- Metal Rich
INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'IRON', 'INTERIOR', 50, 70 FROM ref.composition_template WHERE name = 'Moon - Metal Rich'
UNION ALL SELECT id, 'NICKEL', 'INTERIOR', 10, 20 FROM ref.composition_template WHERE name = 'Moon - Metal Rich'
UNION ALL SELECT id, 'OLIVINE', 'INTERIOR', 10, 20 FROM ref.composition_template WHERE name = 'Moon - Metal Rich'
UNION ALL SELECT id, 'IRON', 'ENVELOPE', 30, 50 FROM ref.composition_template WHERE name = 'Moon - Metal Rich'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 20, 40 FROM ref.composition_template WHERE name = 'Moon - Metal Rich';

-- =====================================================================
-- SECTION 5: Create index for moon composition queries
-- =====================================================================

CREATE INDEX IF NOT EXISTS idx_moon_composition_classification ON ud.moon(composition_classification);

-- =====================================================================
-- SECTION 6: Validation
-- =====================================================================

DO $$
DECLARE
    template_count INTEGER;
    component_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO template_count 
    FROM ref.composition_template 
    WHERE planet_types LIKE '%REGULAR_%' 
       OR planet_types LIKE '%COLLISION_DEBRIS%'
       OR planet_types LIKE '%SHEPHERD%'
       OR planet_types LIKE '%TROJAN%'
       OR planet_types LIKE '%IRREGULAR_CAPTURED%'
       OR planet_types LIKE '%Volcanic Moon%'
       OR planet_types LIKE '%Cryovolcanic Moon%'
       OR planet_types LIKE '%Metal Rich Moon%';
    
    SELECT COUNT(*) INTO component_count
    FROM ref.composition_template_component
    WHERE template_id IN (
        SELECT id FROM ref.composition_template 
        WHERE planet_types LIKE '%REGULAR_%' 
           OR planet_types LIKE '%COLLISION_DEBRIS%'
           OR planet_types LIKE '%SHEPHERD%'
           OR planet_types LIKE '%TROJAN%'
           OR planet_types LIKE '%IRREGULAR_CAPTURED%'
           OR planet_types LIKE '%Volcanic Moon%'
           OR planet_types LIKE '%Cryovolcanic Moon%'
           OR planet_types LIKE '%Metal Rich Moon%'
    );
    
    RAISE NOTICE 'Migration V45 completed successfully';
    RAISE NOTICE 'Created % moon composition templates', template_count;
    RAISE NOTICE 'Created % mineral components', component_count;
    RAISE NOTICE 'Expected: 16 templates, 80 components';
    
    IF template_count != 16 THEN
        RAISE WARNING 'Template count is %, expected 16', template_count;
    END IF;
    
    IF component_count != 80 THEN
        RAISE WARNING 'Component count is %, expected 80', component_count;
    END IF;
END $$;

-- =====================================================================
-- END OF MIGRATION V45
-- =====================================================================
