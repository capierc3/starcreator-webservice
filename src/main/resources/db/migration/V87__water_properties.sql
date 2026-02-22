-- =====================================================================
-- V87: Water Properties extraction
-- =====================================================================
-- Extracts water, ocean, and ice fields from planet and moon into a
-- shared water_properties table. Promotes moon-only subsurface ocean
-- fields (has_subsurface_ocean, ocean_depth_km, ice_shell_thickness_km)
-- to the shared table so any body type can have them.
-- =====================================================================

-- ─── 1. Create water_properties table ──────────────────────────────

CREATE TABLE ud.water_properties (
    id                              BIGSERIAL PRIMARY KEY,

    -- Surface water
    water_inventory                 VARCHAR(30),
    water_coverage_percent          DOUBLE PRECISION,
    liquid_water_coverage_percent   DOUBLE PRECISION,
    ice_coverage_percent            DOUBLE PRECISION,

    -- Subsurface water
    has_subsurface_water            BOOLEAN,
    subsurface_water_depth_km       DOUBLE PRECISION,

    -- Subsurface ocean (promoted from moon)
    has_subsurface_ocean            BOOLEAN,
    ocean_depth_km                  DOUBLE PRECISION,
    ice_shell_thickness_km          DOUBLE PRECISION,

    -- Metadata
    label                           VARCHAR(100),
    created_at                      TIMESTAMP DEFAULT NOW(),
    modified_at                     TIMESTAMP DEFAULT NOW()
);

-- ─── 2. Add water_id FK to planet, moon, asteroid ──────────────────

ALTER TABLE ud.planet
    ADD COLUMN water_id BIGINT REFERENCES ud.water_properties(id);

ALTER TABLE ud.moon
    ADD COLUMN water_id BIGINT REFERENCES ud.water_properties(id);

ALTER TABLE ud.asteroid
    ADD COLUMN water_id BIGINT REFERENCES ud.water_properties(id);

-- ─── 3. Drop old water columns from planet ─────────────────────────

ALTER TABLE ud.planet
    DROP COLUMN IF EXISTS water_inventory,
    DROP COLUMN IF EXISTS water_coverage_percent,
    DROP COLUMN IF EXISTS liquid_water_coverage_percent,
    DROP COLUMN IF EXISTS ice_coverage_percent,
    DROP COLUMN IF EXISTS has_subsurface_water,
    DROP COLUMN IF EXISTS subsurface_water_depth_km;

-- ─── 4. Drop old water columns from moon ───────────────────────────

ALTER TABLE ud.moon
    DROP COLUMN IF EXISTS has_subsurface_ocean,
    DROP COLUMN IF EXISTS ocean_depth_km,
    DROP COLUMN IF EXISTS ice_shell_thickness_km,
    DROP COLUMN IF EXISTS water_inventory,
    DROP COLUMN IF EXISTS water_coverage_percent,
    DROP COLUMN IF EXISTS liquid_water_coverage_percent,
    DROP COLUMN IF EXISTS ice_coverage_percent,
    DROP COLUMN IF EXISTS has_subsurface_water,
    DROP COLUMN IF EXISTS subsurface_water_depth_km;
