-- =====================================================================
-- Flyway Migration V64: Moon Water, Magnetic Field & Habitability
-- =====================================================================
-- Description: Three-part migration:
--   1. Adds water inventory columns to ud.moon (matching planet water system)
--   2. Adds moon_id FK to planetary_magnetic_field (moons can have fields)
--   3. Adds moon_id FK to planetary_habitability (moons get assessed)
--
-- Scientific basis:
--   - Ganymede has an intrinsic dipole field from a liquid iron core dynamo
--   - Europa/Callisto have induced fields from Jupiter's magnetosphere
--   - Earth's Moon has remnant crustal magnetism
--   - Europa, Enceladus, Titan all have significant water inventories
--   - Titan has surface liquid (methane), Europa has subsurface ocean
--
-- Author: Chase
-- Date: 2026-02-07
-- =====================================================================

-- =====================================================================
-- PART 1: WATER SYSTEM FOR MOONS
-- =====================================================================
-- Moons already have has_subsurface_ocean and ocean_depth_km, but they
-- lack the richer water inventory system that planets now have.
-- Adding the same columns allows WaterCreator to work on moons.
-- =====================================================================

ALTER TABLE ud.moon ADD COLUMN IF NOT EXISTS water_inventory VARCHAR(30);
COMMENT ON COLUMN ud.moon.water_inventory IS
    'Total water inventory: NONE, TRACE, SCARCE, MODERATE, ABUNDANT, OCEAN_WORLD. Independent of surface state.';

ALTER TABLE ud.moon ADD COLUMN IF NOT EXISTS liquid_water_coverage_percent DOUBLE PRECISION;
COMMENT ON COLUMN ud.moon.liquid_water_coverage_percent IS
    'Percentage of surface covered by liquid water or other liquid (e.g., methane on Titan).';

ALTER TABLE ud.moon ADD COLUMN IF NOT EXISTS ice_coverage_percent DOUBLE PRECISION;
COMMENT ON COLUMN ud.moon.ice_coverage_percent IS
    'Percentage of surface covered by water ice or other frozen volatiles.';

ALTER TABLE ud.moon ADD COLUMN IF NOT EXISTS water_coverage_percent DOUBLE PRECISION;
COMMENT ON COLUMN ud.moon.water_coverage_percent IS
    'Total surface water coverage (liquid + ice) as percentage.';

ALTER TABLE ud.moon ADD COLUMN IF NOT EXISTS has_subsurface_water BOOLEAN DEFAULT FALSE;
COMMENT ON COLUMN ud.moon.has_subsurface_water IS
    'Whether liquid water exists below the surface. Complements existing has_subsurface_ocean with broader scope.';

ALTER TABLE ud.moon ADD COLUMN IF NOT EXISTS subsurface_water_depth_km DOUBLE PRECISION;
COMMENT ON COLUMN ud.moon.subsurface_water_depth_km IS
    'Estimated depth to subsurface water in km. May differ from ice_shell_thickness_km for partial ocean moons.';

-- Index for water queries on moons
CREATE INDEX IF NOT EXISTS idx_moon_water_inventory ON ud.moon(water_inventory);
CREATE INDEX IF NOT EXISTS idx_moon_has_subsurface_water ON ud.moon(has_subsurface_water) WHERE has_subsurface_water = TRUE;

-- =====================================================================
-- PART 2: MAGNETIC FIELD SUPPORT FOR MOONS
-- =====================================================================
-- The planetary_magnetic_field table currently only has planet_id.
-- Adding moon_id allows the same rich magnetic field model for moons.
-- Only one of planet_id or moon_id should be set per row.
-- =====================================================================

ALTER TABLE ud.planetary_magnetic_field ADD COLUMN IF NOT EXISTS moon_id BIGINT;
COMMENT ON COLUMN ud.planetary_magnetic_field.moon_id IS
    'FK to moon. Mutually exclusive with planet_id - a magnetic field belongs to either a planet or a moon.';

ALTER TABLE ud.planetary_magnetic_field
    ADD CONSTRAINT fk_magnetic_field_moon
        FOREIGN KEY (moon_id) REFERENCES ud.moon(id) ON DELETE CASCADE;

-- Drop the unique constraint on planet_id if it exists (it may be a unique column)
-- and replace with a partial unique index so both planet_id and moon_id can be unique independently
-- Note: planet_id was declared as unique in V63, so we need to handle this carefully
ALTER TABLE ud.planetary_magnetic_field DROP CONSTRAINT IF EXISTS planetary_magnetic_field_planet_id_key;
CREATE UNIQUE INDEX IF NOT EXISTS idx_magnetic_field_planet_id_unique
    ON ud.planetary_magnetic_field(planet_id) WHERE planet_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_magnetic_field_moon_id_unique
    ON ud.planetary_magnetic_field(moon_id) WHERE moon_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_magnetic_field_moon_id ON ud.planetary_magnetic_field(moon_id);

-- =====================================================================
-- PART 3: HABITABILITY SUPPORT FOR MOONS
-- =====================================================================
-- Same approach: add moon_id to planetary_habitability table.
-- =====================================================================

ALTER TABLE ud.planetary_habitability ADD COLUMN IF NOT EXISTS moon_id BIGINT;
COMMENT ON COLUMN ud.planetary_habitability.moon_id IS
    'FK to moon. Mutually exclusive with planet_id - a habitability assessment belongs to either a planet or a moon.';

ALTER TABLE ud.planetary_habitability
    ADD CONSTRAINT fk_habitability_moon
        FOREIGN KEY (moon_id) REFERENCES ud.moon(id) ON DELETE CASCADE;

-- Same unique index pattern
ALTER TABLE ud.planetary_habitability DROP CONSTRAINT IF EXISTS planetary_habitability_planet_id_key;
CREATE UNIQUE INDEX IF NOT EXISTS idx_habitability_planet_id_unique
    ON ud.planetary_habitability(planet_id) WHERE planet_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_habitability_moon_id_unique
    ON ud.planetary_habitability(moon_id) WHERE moon_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_habitability_moon_id ON ud.planetary_habitability(moon_id);

-- =====================================================================
-- PART 4: ADD MOON FK REFERENCE TO MOON TABLE FOR MAGNETIC FIELD & HAB
-- =====================================================================

ALTER TABLE ud.moon ADD COLUMN IF NOT EXISTS atmosphere_id BIGINT;

-- Migration complete
