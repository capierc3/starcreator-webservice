-- =====================================================================
-- Migration V43: Fix Moon Volcanic Atmosphere & Composition Issues
-- =====================================================================
--
-- ISSUE 1: Rocky/Mixed moons without volcanic activity are getting
--          VOLCANIC atmospheres from template selection
--
-- ISSUE 2: All warm/hot moons are MIXED composition, none are ROCKY
--          (caused by simple temp < 150K check in code)
--
-- This migration addresses Issue 1 by adjusting template compatibility
-- weights. Issue 2 requires code change in MoonCreator.java
-- =====================================================================

-- =====================================================================
-- SECTION 1: Remove VOLCANIC from Non-Volcanic Rocky Moons
-- =====================================================================

-- Remove VOLCANIC compatibility from regular Rocky Moon entirely
-- Only "Volcanic Moon" type (requires HIGH/MODERATE geological activity)
-- should have VOLCANIC atmospheres
DELETE FROM ref.planet_atmosphere_compatibility
WHERE planet_type = 'Rocky Moon'
  AND atmosphere_classification = 'VOLCANIC';

COMMENT ON TABLE ref.planet_atmosphere_compatibility IS
    'After V43: Rocky Moon type removed from VOLCANIC - only Volcanic Moon type gets volcanic atmospheres';

-- =====================================================================
-- SECTION 2: Significantly Reduce VOLCANIC Weight for Rocky Moon Large
-- =====================================================================

-- Reduce VOLCANIC weight for Rocky Moon Large from 120 to 20
-- Keep it available but make it very rare (only if they happen to be active)
UPDATE ref.planet_atmosphere_compatibility
SET preference_weight = 20,  -- Down from 120
    notes = 'Very rare - only if moon happens to be geologically active despite large size'
WHERE planet_type = 'Rocky Moon Large'
  AND atmosphere_classification = 'VOLCANIC';

-- =====================================================================
-- SECTION 3: Increase NONE Weight for Rocky Moon
-- =====================================================================

-- Increase NONE preference weight for Rocky Moon to make airless moons more common
-- This better reflects reality - most small rocky moons have no atmosphere
UPDATE ref.planet_atmosphere_compatibility
SET preference_weight = 250  -- Up from 150 or 120
WHERE planet_type = 'Rocky Moon'
  AND atmosphere_classification = 'NONE';

-- =====================================================================
-- SECTION 4: Increase MARS_LIKE Weight for Rocky Moon Large
-- =====================================================================

-- Increase MARS_LIKE for Rocky Moon Large to be the preferred atmosphere
-- (if they have one at all)
UPDATE ref.planet_atmosphere_compatibility
SET preference_weight = 180  -- Up from 150
WHERE planet_type = 'Rocky Moon Large'
  AND atmosphere_classification = 'MARS_LIKE';

-- =====================================================================
-- SECTION 5: Verification - Show Updated Weights
-- =====================================================================

DO $$
    DECLARE
        rocky_moon_volcanic_count INTEGER;
        rocky_moon_large_volcanic_weight INTEGER;
    BEGIN
        -- Check that Rocky Moon VOLCANIC was deleted
        SELECT COUNT(*) INTO rocky_moon_volcanic_count
        FROM ref.planet_atmosphere_compatibility
        WHERE planet_type = 'Rocky Moon'
          AND atmosphere_classification = 'VOLCANIC';

        IF rocky_moon_volcanic_count > 0 THEN
            RAISE WARNING 'Rocky Moon still has VOLCANIC compatibility!';
        ELSE
            RAISE NOTICE '✓ Rocky Moon VOLCANIC compatibility removed';
        END IF;

        -- Check Rocky Moon Large VOLCANIC weight was reduced
        SELECT preference_weight INTO rocky_moon_large_volcanic_weight
        FROM ref.planet_atmosphere_compatibility
        WHERE planet_type = 'Rocky Moon Large'
          AND atmosphere_classification = 'VOLCANIC';

        IF rocky_moon_large_volcanic_weight > 20 THEN
            RAISE WARNING 'Rocky Moon Large VOLCANIC weight is %, expected 20', rocky_moon_large_volcanic_weight;
        ELSE
            RAISE NOTICE '✓ Rocky Moon Large VOLCANIC weight reduced to %', rocky_moon_large_volcanic_weight;
        END IF;

        RAISE NOTICE 'Migration V43 completed successfully';
        RAISE NOTICE 'Expected results:';
        RAISE NOTICE '  - Fewer moons with VOLCANIC atmospheres overall';
        RAISE NOTICE '  - Only "Volcanic Moon" type gets VOLCANIC atmospheres reliably';
        RAISE NOTICE '  - More airless Rocky Moons';
        RAISE NOTICE '  - Rocky Moon Large prefers MARS_LIKE if it has atmosphere';
    END $$;

-- =====================================================================
-- SECTION 6: Summary of Expected Atmosphere Distribution After V43
-- =====================================================================

-- Before V43:
--   Rocky Moon:       VOLCANIC (60), MARS_LIKE (80), NONE (120)
--   Rocky Moon Large: VOLCANIC (120), MARS_LIKE (150), NONE (100)
--   Volcanic Moon:    VOLCANIC (250), NONE (30)
--
-- After V43:
--   Rocky Moon:       VOLCANIC (REMOVED), MARS_LIKE (80), NONE (250)
--   Rocky Moon Large: VOLCANIC (20), MARS_LIKE (180), NONE (100)
--   Volcanic Moon:    VOLCANIC (250), NONE (30) [unchanged]
--
-- Expected outcome:
--   - 64.7% VOLCANIC → should drop to ~10-15%
--   - Most small rocky moons will be airless
--   - Large rocky moons will prefer thin MARS_LIKE atmospheres
--   - Only actual "Volcanic Moon" type gets VOLCANIC atmospheres

-- =====================================================================
-- END OF MIGRATION V43
-- =====================================================================