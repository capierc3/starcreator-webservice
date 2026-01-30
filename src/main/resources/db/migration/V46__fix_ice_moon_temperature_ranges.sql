-- =====================================================================
-- Flyway Migration V46: Fix Ice Moon Template Temperature Ranges
-- =====================================================================
-- Issue: Tidally heated ice moons (150K-250K) don't match any templates
--        and fall back to GAS_ENVELOPE default composition
--
-- Examples found:
--   - Cryovolcanic moons with tidal heating: 150-250K
--   - Large icy moons near gas giants: 150-200K
--
-- Fix: Extend max temperature for ice moon templates to 250K
-- =====================================================================

-- =====================================================================
-- SECTION 1: Extend Ice Moon Template Temperature Ranges
-- =====================================================================

-- Large Regular Icy: Extend from 150K to 250K
UPDATE ref.composition_template
SET max_surface_temp_k = 250
WHERE name = 'Moon - Large Regular Icy';

-- Medium Icy: Extend from 150K to 250K
UPDATE ref.composition_template
SET max_surface_temp_k = 250
WHERE name = 'Moon - Medium Icy';

-- Small Icy: Extend from 150K to 220K (smaller moons heat less)
UPDATE ref.composition_template
SET max_surface_temp_k = 220
WHERE name = 'Moon - Small Icy';

-- Ice Shell Ocean (Cryovolcanic): Extend from 150K to 300K
-- These can be very warm from tidal heating (Europa-like)
UPDATE ref.composition_template
SET max_surface_temp_k = 300
WHERE name = 'Moon - Ice Shell Ocean';

-- Nitrogen Ice Cryo: Extend from 80K to 150K
-- These stay colder due to volatile ices
UPDATE ref.composition_template
SET max_surface_temp_k = 150
WHERE name = 'Moon - Nitrogen Ice Cryo';

-- Trojan Icy: Extend from 150K to 200K
UPDATE ref.composition_template
SET max_surface_temp_k = 200
WHERE name = 'Moon - Trojan Icy';

-- Captured Comet: Extend from 150K to 200K
UPDATE ref.composition_template
SET max_surface_temp_k = 200
WHERE name = 'Moon - Captured Comet';

-- =====================================================================
-- SECTION 2: Add Warm Transition Template for Very Hot Ice Moons
-- =====================================================================
-- For edge cases where ice moons get extremely hot (>250K) from tidal heating
-- These transition to mixed compositions

INSERT INTO ref.composition_template 
(name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES 
('Moon - Warm Ice Transition', 'Tidally superheated ice moon transitioning to mixed', 'MIXED_SILICATE_ICE', 
 'REGULAR_LARGE,REGULAR_MEDIUM,Cryovolcanic Moon', 100, 250, 400);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35, 45 FROM ref.composition_template WHERE name = 'Moon - Warm Ice Transition'
UNION ALL SELECT id, 'PYROXENE', 'INTERIOR', 25, 35 FROM ref.composition_template WHERE name = 'Moon - Warm Ice Transition'
UNION ALL SELECT id, 'WATER_ICE', 'INTERIOR', 15, 25 FROM ref.composition_template WHERE name = 'Moon - Warm Ice Transition'
UNION ALL SELECT id, 'WATER_ICE', 'ENVELOPE', 40, 60 FROM ref.composition_template WHERE name = 'Moon - Warm Ice Transition'
UNION ALL SELECT id, 'SILICATE', 'ENVELOPE', 30, 50 FROM ref.composition_template WHERE name = 'Moon - Warm Ice Transition';

-- =====================================================================
-- SECTION 3: Validation
-- =====================================================================

DO $$
DECLARE
    updated_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO updated_count
    FROM ref.composition_template
    WHERE (name LIKE 'Moon -%' AND name LIKE '%Icy%' AND max_surface_temp_k >= 200)
       OR (name = 'Moon - Ice Shell Ocean' AND max_surface_temp_k >= 300);
    
    RAISE NOTICE 'Migration V46 completed successfully';
    RAISE NOTICE 'Extended temperature ranges for % ice moon templates', updated_count;
    RAISE NOTICE 'Added 1 new warm transition template';
    RAISE NOTICE '';
    RAISE NOTICE 'Temperature coverage for ice moons:';
    RAISE NOTICE '  Regular Large Icy: 30-250K (was 30-150K)';
    RAISE NOTICE '  Regular Medium Icy: 30-250K (was 30-150K)';
    RAISE NOTICE '  Regular Small Icy: 30-220K (was 30-150K)';
    RAISE NOTICE '  Ice Shell Ocean: 30-300K (was 30-150K)';
    RAISE NOTICE '  Nitrogen Ice Cryo: 30-150K (was 30-80K)';
    RAISE NOTICE '  Warm Ice Transition: 250-400K (NEW)';
END $$;

-- =====================================================================
-- END OF MIGRATION V46
-- =====================================================================
