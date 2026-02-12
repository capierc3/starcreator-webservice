-- V67__add_star_planet_formation_ranges.sql
-- Adds min/max planet formation distance columns to star_type_ref
-- so that planetary system generation uses per-star-type orbital boundaries
-- instead of hardcoded values.
--
-- min_planet_formation_au: closest a planet can form (inner disk edge)
-- max_planet_formation_au: outer boundary of planetary system
--
-- Values based on observed exoplanet systems, protoplanetary disk physics,
-- and known systems like TRAPPIST-1 (M-dwarf), Solar System (G-type), etc.

-- ============================================================================
-- SECTION 1: Add Columns
-- ============================================================================

ALTER TABLE ref.star_type
    ADD COLUMN min_planet_formation_au DOUBLE PRECISION,
    ADD COLUMN max_planet_formation_au DOUBLE PRECISION;

COMMENT ON COLUMN ref.star_type.min_planet_formation_au IS
    'Minimum orbital distance in AU where planets can form (inner edge of protoplanetary disk)';

COMMENT ON COLUMN ref.star_type.max_planet_formation_au IS
    'Maximum orbital distance in AU for planetary system extent (outer disk boundary)';

-- ============================================================================
-- SECTION 2: Populate Values
-- ============================================================================

-- Main Sequence O: massive disks, radiation clears inner region
UPDATE ref.star_type
SET min_planet_formation_au = 1.0, max_planet_formation_au = 200.0
WHERE name = 'Main Sequence O';

-- Main Sequence B: large disks, less extreme than O
UPDATE ref.star_type
SET min_planet_formation_au = 0.5, max_planet_formation_au = 150.0
WHERE name = 'Main Sequence B';

-- Main Sequence A: Vega-like disks extend to ~100 AU
UPDATE ref.star_type
SET min_planet_formation_au = 0.3, max_planet_formation_au = 100.0
WHERE name = 'Main Sequence A';

-- Main Sequence F: slightly larger than solar
UPDATE ref.star_type
SET min_planet_formation_au = 0.15, max_planet_formation_au = 80.0
WHERE name = 'Main Sequence F';

-- Main Sequence G: Solar system reference (Mercury 0.39 AU, Kuiper ~50 AU)
UPDATE ref.star_type
SET min_planet_formation_au = 0.1, max_planet_formation_au = 60.0
WHERE name = 'Main Sequence G';

-- Main Sequence K: smaller disks than solar
UPDATE ref.star_type
SET min_planet_formation_au = 0.05, max_planet_formation_au = 40.0
WHERE name = 'Main Sequence K';

-- Main Sequence M: TRAPPIST-1 innermost planet at 0.011 AU
UPDATE ref.star_type
SET min_planet_formation_au = 0.01, max_planet_formation_au = 20.0
WHERE name = 'Main Sequence M';

-- Brown Dwarf L: very compact systems
UPDATE ref.star_type
SET min_planet_formation_au = 0.005, max_planet_formation_au = 10.0
WHERE name = 'Brown Dwarf L';

-- Brown Dwarf T: tiny disks
UPDATE ref.star_type
SET min_planet_formation_au = 0.003, max_planet_formation_au = 5.0
WHERE name = 'Brown Dwarf T';

-- Brown Dwarf Y: minimal disk material
UPDATE ref.star_type
SET min_planet_formation_au = 0.002, max_planet_formation_au = 3.0
WHERE name = 'Brown Dwarf Y';

-- T Tauri: still forming, active disk present
UPDATE ref.star_type
SET min_planet_formation_au = 0.05, max_planet_formation_au = 50.0
WHERE name = 'T Tauri';

-- Proto: very young, disk still condensing
UPDATE ref.star_type
SET min_planet_formation_au = 0.02, max_planet_formation_au = 20.0
WHERE name = 'Proto';

-- White Dwarf: second-generation planets, inner region cleared by giant phase
UPDATE ref.star_type
SET min_planet_formation_au = 0.5, max_planet_formation_au = 30.0
WHERE name = 'White Dwarf';

-- Red Giant: inner planets consumed by stellar expansion
UPDATE ref.star_type
SET min_planet_formation_au = 2.0, max_planet_formation_au = 100.0
WHERE name = 'Red Giant';

-- Super Giant: massive star, inner region cleared
UPDATE ref.star_type
SET min_planet_formation_au = 5.0, max_planet_formation_au = 200.0
WHERE name = 'Super Giant';

-- ============================================================================
-- SECTION 3: Add Constraints
-- ============================================================================

ALTER TABLE ref.star_type
    ADD CONSTRAINT chk_planet_formation_range CHECK (
        min_planet_formation_au IS NULL OR
        max_planet_formation_au IS NULL OR
        min_planet_formation_au <= max_planet_formation_au
    );

ALTER TABLE ref.star_type
    ADD CONSTRAINT chk_planet_formation_positive CHECK (
        (min_planet_formation_au IS NULL OR min_planet_formation_au > 0) AND
        (max_planet_formation_au IS NULL OR max_planet_formation_au > 0)
    );

-- ============================================================================
-- SECTION 4: Verify
-- ============================================================================

DO $$
DECLARE
    missing INTEGER;
BEGIN
    SELECT COUNT(*) INTO missing
    FROM ref.star_type
    WHERE min_planet_formation_au IS NULL OR max_planet_formation_au IS NULL;

    IF missing > 0 THEN
        RAISE WARNING '% star types missing planet formation ranges', missing;
    ELSE
        RAISE NOTICE 'All star types have planet formation ranges set';
    END IF;
END $$;
