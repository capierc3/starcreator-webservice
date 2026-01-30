-- =====================================================================
-- Flyway Migration V49: Fix SHEPHERD Moon Coverage Gaps
-- =====================================================================
-- Problem: 3 SHEPHERD moons still getting GAS_ENVELOPE at 306-322K
--
-- Root Causes:
--   1. "Small Icy Moderate" template missing SHEPHERD from planet_types
--   2. No "Small Mixed Moderate" template (250-400K gap for MIXED)
--   3. SHEPHERD not included in some Small templates
--
-- Solution: Add SHEPHERD to all Small templates and create missing templates
-- =====================================================================

-- =====================================================================
-- SECTION 1: Add SHEPHERD to existing Small templates
-- =====================================================================

-- Add SHEPHERD to Small Icy Very Cold
UPDATE ref.composition_template
SET planet_types = 'REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED'
WHERE name = 'Moon - Small Icy Very Cold';

-- Add SHEPHERD to Small Icy Moderate  
UPDATE ref.composition_template
SET planet_types = 'REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED'
WHERE name = 'Moon - Small Icy Moderate';

-- Add SHEPHERD to Small Icy Warm
UPDATE ref.composition_template
SET planet_types = 'REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED'
WHERE name = 'Moon - Small Icy Warm';

-- Add SHEPHERD to Small Mixed Very Cold
UPDATE ref.composition_template
SET planet_types = 'REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED'
WHERE name = 'Moon - Small Mixed Very Cold';

-- Add SHEPHERD to Small Mixed Cold
UPDATE ref.composition_template
SET planet_types = 'REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED'
WHERE name = 'Moon - Small Mixed Cold';

-- Add SHEPHERD to Small Mixed Warm
UPDATE ref.composition_template
SET planet_types = 'REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED'
WHERE name = 'Moon - Small Mixed Warm';

-- =====================================================================
-- SECTION 2: Add missing Small Mixed Moderate template (250-400K)
-- =====================================================================
-- This is the gap causing SHEPHERD moons at 306-322K to fall back to planets

INSERT INTO ref.composition_template 
(name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES 
('Moon - Small Mixed Moderate', 'Small mixed moon with moderate heating', 'MIXED_SILICATE_ICE',
 'REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED', 100, 250, 400);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 28, 38 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Moderate'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 22, 32 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Moderate'
UNION ALL SELECT id, 'WATER_ICE', 'INTERIOR', 20, 30 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Moderate'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 35, 55 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Moderate'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 40, 55 FROM ref.composition_template WHERE name = 'Moon - Small Mixed Moderate';

-- =====================================================================
-- SECTION 3: Add Small Rocky Moderate (250-400K) for completeness
-- =====================================================================

INSERT INTO ref.composition_template 
(name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES 
('Moon - Small Rocky Moderate', 'Small rocky moon with moderate heating', 'SILICATE_RICH',
 'REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED', 110, 250, 400);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 40, 55 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Moderate'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Moderate'
UNION ALL SELECT id, 'IRON', 'INTERIOR', 8, 18 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Moderate'
UNION ALL SELECT id, 'REGOLITH', 'ENVELOPE', 45, 65 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Moderate'
UNION ALL SELECT id, 'BASALT', 'ENVELOPE', 30, 45 FROM ref.composition_template WHERE name = 'Moon - Small Rocky Moderate';

-- =====================================================================
-- SECTION 4: Fix SHEPHERD formation type in code
-- =====================================================================
-- NOTE: This is a reminder that setFormationType() in MoonCreator.java
-- currently sets SHEPHERD as "CAPTURED" but it should be "CO_FORMED"
-- 
-- Current code:
--   case "IRREGULAR_CAPTURED", "SHEPHERD", "TROJAN" -> moon.setFormationType("CAPTURED");
--
-- Should be:
--   case "IRREGULAR_CAPTURED" -> moon.setFormationType("CAPTURED");
--   case "SHEPHERD", "TROJAN" -> moon.setFormationType("CO_FORMED");
--
-- This migration adds a comment marker for tracking

COMMENT ON TABLE ref.composition_template IS 
'After V49: All Small moon templates include SHEPHERD. Note: SHEPHERD moons should have formationType=CO_FORMED not CAPTURED (code fix needed in MoonCreator.java setFormationType method)';

-- =====================================================================
-- SECTION 5: Validation
-- =====================================================================

DO $$
DECLARE
    shepherd_template_count INTEGER;
    small_moderate_count INTEGER;
BEGIN
    -- Count templates that include SHEPHERD
    SELECT COUNT(*) INTO shepherd_template_count
    FROM ref.composition_template
    WHERE planet_types LIKE '%SHEPHERD%';
    
    -- Count Small Moderate templates
    SELECT COUNT(*) INTO small_moderate_count
    FROM ref.composition_template
    WHERE name LIKE 'Moon - Small%Moderate';
    
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Migration V49 Completed';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Templates including SHEPHERD: %', shepherd_template_count;
    RAISE NOTICE 'Small Moderate templates: % (should be 3)', small_moderate_count;
    RAISE NOTICE '';
    RAISE NOTICE 'Fixed Coverage:';
    RAISE NOTICE '  Small Mixed Moderate: 250-400K (SHEPHERD included) ← Closes the gap!';
    RAISE NOTICE '  Small Rocky Moderate: 250-400K (SHEPHERD included)';
    RAISE NOTICE '';
    RAISE NOTICE 'CODE FIX NEEDED:';
    RAISE NOTICE '  MoonCreator.setFormationType() should set SHEPHERD as CO_FORMED not CAPTURED';
    
    IF small_moderate_count != 3 THEN
        RAISE WARNING 'Expected 3 Small Moderate templates, found %', small_moderate_count;
    END IF;
END $$;

-- =====================================================================
-- END OF MIGRATION V49
-- =====================================================================
