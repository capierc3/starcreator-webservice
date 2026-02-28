-- =====================================================================
-- V86: Terrain Properties extraction
-- =====================================================================
-- Extracts terrain, geology, and surface morphology fields from planet,
-- moon, and asteroid into a shared terrain_properties table.
-- Renames planet_terrain_distribution → terrain_distribution and
-- re-parents it from planet to terrain_properties.
-- =====================================================================

-- ─── 1. Create terrain_properties table ──────────────────────────────

CREATE TABLE ud.terrain_properties (
    id                              BIGSERIAL PRIMARY KEY,

    -- Surface features
    surface_features                TEXT,

    -- Geological activity
    geological_activity             VARCHAR(50),
    activity_score                  DOUBLE PRECISION,

    -- Volcanism
    has_volcanic_activity           BOOLEAN,
    volcanism_type                  VARCHAR(50),
    estimated_active_volcanoes      INTEGER,
    volcanic_intensity              VARCHAR(50),

    -- Terrain morphology
    mountain_coverage_percent       DOUBLE PRECISION,
    average_elevation_km            DOUBLE PRECISION,
    max_elevation_km                DOUBLE PRECISION,
    min_elevation_km                DOUBLE PRECISION,
    terrain_roughness               DOUBLE PRECISION,

    -- Impact history
    cratering_level                 VARCHAR(50),
    estimated_visible_craters       INTEGER,

    -- Erosion
    erosion_level                   VARCHAR(50),
    primary_erosion_agent           VARCHAR(50),

    -- Planet-only: tectonics
    has_plate_tectonics             BOOLEAN,
    number_of_tectonic_plates       INTEGER,
    tectonic_activity_level         VARCHAR(50),

    -- Moon-only: cryovolcanism
    has_cryovolcanism               BOOLEAN,

    -- Asteroid-only: regolith
    has_regolith                    BOOLEAN,
    regolith_depth_m                DOUBLE PRECISION,

    -- Metadata
    label                           VARCHAR(100),

    created_at                      TIMESTAMP DEFAULT NOW(),
    modified_at                     TIMESTAMP DEFAULT NOW()
);

COMMENT ON TABLE ud.terrain_properties IS 'Terrain, geology, and surface morphology data for any celestial body with a surface';
COMMENT ON COLUMN ud.terrain_properties.label IS 'Human-readable label for debugging (e.g. Planet terrain, Moon terrain)';

-- ─── 2. Add terrain FK to parent entities ────────────────────────────

ALTER TABLE ud.planet ADD COLUMN terrain_id BIGINT REFERENCES ud.terrain_properties(id) ON DELETE SET NULL;
ALTER TABLE ud.moon ADD COLUMN terrain_id BIGINT REFERENCES ud.terrain_properties(id) ON DELETE SET NULL;
ALTER TABLE ud.asteroid ADD COLUMN terrain_id BIGINT REFERENCES ud.terrain_properties(id) ON DELETE SET NULL;

-- ─── 3. Rename and re-parent terrain distribution ────────────────────

ALTER TABLE ud.planet_terrain_distribution RENAME TO terrain_distribution;

ALTER TABLE ud.terrain_distribution ADD COLUMN terrain_properties_id BIGINT;

-- For any existing distribution rows, we'd migrate via the planet's terrain_id.
-- Since we don't have persistent user data yet, existing rows will be orphaned
-- and recreated by the next generation run.

ALTER TABLE ud.terrain_distribution
    DROP CONSTRAINT IF EXISTS planet_terrain_distribution_planet_id_fkey,
    DROP COLUMN IF EXISTS planet_id;

ALTER TABLE ud.terrain_distribution ADD CONSTRAINT fk_terrain_dist_terrain
    FOREIGN KEY (terrain_properties_id) REFERENCES ud.terrain_properties(id) ON DELETE CASCADE;

-- ─── 4. Drop old terrain/geology columns from planet ─────────────────

ALTER TABLE ud.planet
    DROP COLUMN IF EXISTS geological_activity,
    DROP COLUMN IF EXISTS activity_score,
    DROP COLUMN IF EXISTS has_plate_tectonics,
    DROP COLUMN IF EXISTS number_of_tectonic_plates,
    DROP COLUMN IF EXISTS tectonic_activity_level,
    DROP COLUMN IF EXISTS has_volcanic_activity,
    DROP COLUMN IF EXISTS volcanism_type,
    DROP COLUMN IF EXISTS estimated_active_volcanoes,
    DROP COLUMN IF EXISTS volcanic_intensity,
    DROP COLUMN IF EXISTS mountain_coverage_percent,
    DROP COLUMN IF EXISTS average_elevation_km,
    DROP COLUMN IF EXISTS max_elevation_km,
    DROP COLUMN IF EXISTS min_elevation_km,
    DROP COLUMN IF EXISTS terrain_roughness,
    DROP COLUMN IF EXISTS cratering_level,
    DROP COLUMN IF EXISTS estimated_visible_craters,
    DROP COLUMN IF EXISTS erosion_level,
    DROP COLUMN IF EXISTS primary_erosion_agent;

-- NOTE: Storm fields (has_great_storm, number_of_major_storms, atmospheric_convection_level) STAY on planet

-- ─── 5. Drop old terrain/geology columns from moon ───────────────────

ALTER TABLE ud.moon
    DROP COLUMN IF EXISTS geological_activity,
    DROP COLUMN IF EXISTS has_cryovolcanism,
    DROP COLUMN IF EXISTS volcanism_type,
    DROP COLUMN IF EXISTS volcanic_intensity,
    DROP COLUMN IF EXISTS estimated_active_volcanoes,
    DROP COLUMN IF EXISTS cratering_level,
    DROP COLUMN IF EXISTS estimated_visible_craters,
    DROP COLUMN IF EXISTS surface_features,
    DROP COLUMN IF EXISTS mountain_coverage_percent,
    DROP COLUMN IF EXISTS average_elevation_km,
    DROP COLUMN IF EXISTS max_elevation_km,
    DROP COLUMN IF EXISTS min_elevation_km,
    DROP COLUMN IF EXISTS terrain_roughness,
    DROP COLUMN IF EXISTS erosion_level,
    DROP COLUMN IF EXISTS primary_erosion_agent;

-- ─── 6. Drop old surface columns from asteroid ──────────────────────

ALTER TABLE ud.asteroid
    DROP COLUMN IF EXISTS surface_features,
    DROP COLUMN IF EXISTS cratering_level,
    DROP COLUMN IF EXISTS has_regolith,
    DROP COLUMN IF EXISTS regolith_depth_m;
