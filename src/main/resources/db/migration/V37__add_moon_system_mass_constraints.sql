-- =====================================================================
-- Flyway Migration V37: Add Moon System Mass Constraints
-- =====================================================================
-- Description: Adds moon system mass budget ranges to planet types and
--              ordering priority to moon types for realistic mass distribution
-- Author: Chase
-- Date: 2026-01-24
-- =====================================================================

-- =====================================================================
-- SECTION 1: Add Moon System Mass Budget to Planet Types
-- =====================================================================

ALTER TABLE ref.planet_type_ref
    ADD COLUMN min_moon_system_mass_ratio DOUBLE PRECISION,
    ADD COLUMN max_moon_system_mass_ratio DOUBLE PRECISION;

COMMENT ON COLUMN ref.planet_type_ref.min_moon_system_mass_ratio IS
    'Minimum ratio of total moon system mass to planet mass (e.g., 0.0001 = 0.01% of planet mass)';

COMMENT ON COLUMN ref.planet_type_ref.max_moon_system_mass_ratio IS
    'Maximum ratio of total moon system mass to planet mass (e.g., 0.0005 = 0.05% of planet mass)';

-- =====================================================================
-- SECTION 2: Add Mass Distribution Priority to Moon Types
-- =====================================================================

ALTER TABLE ref.moon_type_ref
    ADD COLUMN mass_distribution_priority INTEGER DEFAULT 5;

COMMENT ON COLUMN ref.moon_type_ref.mass_distribution_priority IS
    'Priority order for mass distribution (lower = gets mass first, 0 = highest priority)';

-- =====================================================================
-- SECTION 3: Update Planet Type Moon Mass Ratios
-- =====================================================================
-- Based on real solar system data and formation physics

-- Gas Giants (Jupiter-like, Saturn-like)
UPDATE ref.planet_type_ref
SET 
    min_moon_system_mass_ratio = 0.00005,
    max_moon_system_mass_ratio = 0.0003
WHERE name IN ('Gas Giant', 'Hot Jupiter');

-- Ice Giants (Uranus-like, Neptune-like)
UPDATE ref.planet_type_ref
SET 
    min_moon_system_mass_ratio = 0.00008,
    max_moon_system_mass_ratio = 0.0005
WHERE name IN ('Ice Giant', 'Sub-Neptune');

-- Super-Earths and Ocean Worlds (can have impact moons)
UPDATE ref.planet_type_ref
SET 
    min_moon_system_mass_ratio = 0.0001,
    max_moon_system_mass_ratio = 0.01
WHERE name IN ('Super-Earth', 'Ocean Planet');

-- Terrestrial Planets (Earth-like with potential giant impact moon)
UPDATE ref.planet_type_ref
SET 
    min_moon_system_mass_ratio = 0.0001,
    max_moon_system_mass_ratio = 0.015
WHERE name = 'Terrestrial Planet';

-- Rocky Planets (Mars-like, smaller)
UPDATE ref.planet_type_ref
SET 
    min_moon_system_mass_ratio = 0.00005,
    max_moon_system_mass_ratio = 0.002
WHERE name IN ('Rocky Planet', 'Desert Planet');

-- Hot/Exotic Planets (minimal moons due to stellar proximity or composition)
UPDATE ref.planet_type_ref
SET 
    min_moon_system_mass_ratio = 0.00001,
    max_moon_system_mass_ratio = 0.0001
WHERE name IN ('Hot Neptune', 'Lava Planet', 'Iron Planet', 'Carbon Planet');

-- Ice Worlds and Dwarf Planets
UPDATE ref.planet_type_ref
SET 
    min_moon_system_mass_ratio = 0.00005,
    max_moon_system_mass_ratio = 0.001
WHERE name IN ('Ice World', 'Dwarf Planet');

-- Rogue Planets (no additional constraints)
UPDATE ref.planet_type_ref
SET 
    min_moon_system_mass_ratio = 0.00005,
    max_moon_system_mass_ratio = 0.0005
WHERE name = 'Rogue Planet';

-- =====================================================================
-- SECTION 4: Update Moon Type Mass Distribution Priorities
-- =====================================================================
-- Lower priority = gets mass allocated first

UPDATE ref.moon_type_ref
SET mass_distribution_priority = 0
WHERE moon_type = 'COLLISION_DEBRIS';  -- Highest priority (formed from giant impact)

UPDATE ref.moon_type_ref
SET mass_distribution_priority = 1
WHERE moon_type = 'REGULAR_LARGE';  -- Second priority (major moons)

UPDATE ref.moon_type_ref
SET mass_distribution_priority = 2
WHERE moon_type = 'REGULAR_MEDIUM';  -- Third priority (medium moons)

UPDATE ref.moon_type_ref
SET mass_distribution_priority = 3
WHERE moon_type = 'REGULAR_SMALL';  -- Fourth priority (small moons)

UPDATE ref.moon_type_ref
SET mass_distribution_priority = 4
WHERE moon_type IN ('SHEPHERD', 'TROJAN');  -- Fifth priority (specialized moons)

UPDATE ref.moon_type_ref
SET mass_distribution_priority = 5
WHERE moon_type = 'IRREGULAR_CAPTURED';  -- Lowest priority (captured objects)

-- =====================================================================
-- SECTION 5: Add Constraints
-- =====================================================================

ALTER TABLE ref.planet_type_ref
    ADD CONSTRAINT chk_moon_mass_ratio_range 
    CHECK (min_moon_system_mass_ratio IS NULL OR max_moon_system_mass_ratio IS NULL OR 
           min_moon_system_mass_ratio <= max_moon_system_mass_ratio);

ALTER TABLE ref.planet_type_ref
    ADD CONSTRAINT chk_moon_mass_ratio_positive
    CHECK (min_moon_system_mass_ratio IS NULL OR min_moon_system_mass_ratio >= 0);

ALTER TABLE ref.moon_type_ref
    ADD CONSTRAINT chk_mass_priority_non_negative
    CHECK (mass_distribution_priority >= 0);

-- =====================================================================
-- SECTION 6: Migration Verification Query
-- =====================================================================

-- Uncomment to verify migration results:
-- SELECT 
--     name,
--     min_moon_system_mass_ratio,
--     max_moon_system_mass_ratio,
--     CONCAT(ROUND(min_moon_system_mass_ratio * 100, 4), '% - ', ROUND(max_moon_system_mass_ratio * 100, 4), '%') as mass_ratio_percent
-- FROM ref.planet_type_ref
-- WHERE min_moon_system_mass_ratio IS NOT NULL
-- ORDER BY max_moon_system_mass_ratio DESC;

-- SELECT 
--     moon_type,
--     mass_distribution_priority,
--     min_mass_earth_masses,
--     max_mass_earth_masses
-- FROM ref.moon_type_ref
-- ORDER BY mass_distribution_priority;
