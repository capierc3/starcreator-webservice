-- =====================================================================
-- V85: Orbital Elements table + Orbital Band (Belt/Ring unification)
-- =====================================================================
-- Creates a reusable orbital_elements table for Keplerian orbital params
-- and a unified orbital_band table replacing both belt and ring tables.
-- =====================================================================

-- ─── 1. Create orbital_elements table ─────────────────────────────

CREATE TABLE ud.orbital_elements (
    id                              BIGSERIAL PRIMARY KEY,

    -- Core Keplerian elements
    semi_major_axis                 DOUBLE PRECISION NOT NULL,
    semi_major_axis_unit            VARCHAR(5) NOT NULL DEFAULT 'AU',
    orbital_period_days             DOUBLE PRECISION,
    eccentricity                    DOUBLE PRECISION,
    inclination_degrees             DOUBLE PRECISION,
    longitude_of_ascending_node_deg DOUBLE PRECISION,
    argument_of_periapsis_deg       DOUBLE PRECISION,
    mean_anomaly_deg                DOUBLE PRECISION,

    -- Stability assessment
    orbit_stability                 VARCHAR(20),
    orbit_stability_timescale_my    DOUBLE PRECISION,
    orbit_crossing_neighbor         VARCHAR(100),

    -- Metadata
    label                           VARCHAR(100),

    created_at                      TIMESTAMP DEFAULT NOW(),
    modified_at                     TIMESTAMP DEFAULT NOW()
);

COMMENT ON TABLE ud.orbital_elements IS 'Reusable Keplerian orbital parameters for any orbiting object or orbital boundary';
COMMENT ON COLUMN ud.orbital_elements.semi_major_axis IS 'Semi-major axis value in the unit specified by semi_major_axis_unit';
COMMENT ON COLUMN ud.orbital_elements.semi_major_axis_unit IS 'Distance unit: AU for star-orbiting bodies, KM for planet-orbiting bodies';
COMMENT ON COLUMN ud.orbital_elements.label IS 'Human-readable label for debugging (e.g. Planet orbit, Belt inner edge)';

-- ─── 2. Create orbital_band table (replaces belt + ring) ─────────

CREATE TABLE ud.orbital_band (
    id                      BIGSERIAL PRIMARY KEY,

    -- Parent body (exactly one should be non-null)
    star_system_id          BIGINT REFERENCES ud.star_system(id) ON DELETE CASCADE,
    planet_id               BIGINT REFERENCES ud.celestial_body(id) ON DELETE CASCADE,

    -- Type discrimination
    band_category           VARCHAR(20) NOT NULL,
    band_type               VARCHAR(50) NOT NULL,
    belt_type_id            INTEGER REFERENCES ref.belt_type(id),

    -- Orbital boundaries
    inner_orbit_id          BIGINT NOT NULL REFERENCES ud.orbital_elements(id) ON DELETE CASCADE,
    outer_orbit_id          BIGINT NOT NULL REFERENCES ud.orbital_elements(id) ON DELETE CASCADE,

    -- Identity
    name                    VARCHAR(100),
    description             TEXT,
    age_my                  DOUBLE PRECISION,

    -- Physical properties (shared)
    total_mass_kg           DOUBLE PRECISION,
    total_mass_earth_masses DOUBLE PRECISION,
    composition_type        VARCHAR(50),
    primary_composition     TEXT,
    albedo                  DOUBLE PRECISION,

    -- Aggregate orbital stats (belt-like bands with many objects)
    peak_density_distance   DOUBLE PRECISION,
    average_eccentricity    DOUBLE PRECISION,
    max_eccentricity        DOUBLE PRECISION,
    average_inclination_deg DOUBLE PRECISION,
    max_inclination_deg     DOUBLE PRECISION,
    estimated_object_count  BIGINT,

    -- Structure (shared)
    has_gaps                BOOLEAN DEFAULT FALSE,
    gap_description         TEXT,

    -- Belt-specific
    has_resonance_gaps      BOOLEAN DEFAULT FALSE,
    has_collisional_families BOOLEAN DEFAULT FALSE,
    family_count            INTEGER,

    -- Ring-specific
    thickness_km            DOUBLE PRECISION,
    optical_depth           DOUBLE PRECISION,
    particle_size_min_m     DOUBLE PRECISION,
    particle_size_max_m     DOUBLE PRECISION,
    color                   VARCHAR(100),
    visibility              VARCHAR(30),
    has_shepherd_moons      BOOLEAN DEFAULT FALSE,
    orbital_resonances      TEXT,
    stability               VARCHAR(30),
    origin_type             VARCHAR(50),
    formation_description   TEXT,
    estimated_age_my        DOUBLE PRECISION,

    -- Timestamps
    created_at              TIMESTAMP DEFAULT NOW(),
    modified_at             TIMESTAMP DEFAULT NOW(),

    -- Ensure band belongs to exactly one parent type
    CONSTRAINT chk_band_parent CHECK (
        (star_system_id IS NOT NULL AND planet_id IS NULL) OR
        (star_system_id IS NULL AND planet_id IS NOT NULL)
    )
);

CREATE INDEX idx_orbital_band_system ON ud.orbital_band(star_system_id) WHERE star_system_id IS NOT NULL;
CREATE INDEX idx_orbital_band_planet ON ud.orbital_band(planet_id) WHERE planet_id IS NOT NULL;

COMMENT ON TABLE ud.orbital_band IS 'Unified annular band — replaces both belt (star-orbiting) and ring (planet-orbiting)';
COMMENT ON COLUMN ud.orbital_band.band_category IS 'BELT for star-orbiting bands, RING for planet-orbiting bands';
COMMENT ON COLUMN ud.orbital_band.inner_orbit_id IS 'Orbital elements defining the inner edge of the band';
COMMENT ON COLUMN ud.orbital_band.outer_orbit_id IS 'Orbital elements defining the outer edge of the band';

-- ─── 3. Create band_dwarf_planet junction table ──────────────────

CREATE TABLE ud.band_dwarf_planet (
    band_id     BIGINT NOT NULL REFERENCES ud.orbital_band(id) ON DELETE CASCADE,
    planet_id   BIGINT NOT NULL REFERENCES ud.celestial_body(id) ON DELETE CASCADE,
    PRIMARY KEY (band_id, planet_id)
);

-- ─── 4. Add orbital_elements_id FK to planet ─────────────────────

ALTER TABLE ud.planet
    ADD COLUMN IF NOT EXISTS orbital_elements_id BIGINT REFERENCES ud.orbital_elements(id) ON DELETE SET NULL;

-- ─── 5. Add orbital_elements_id FK to moon ───────────────────────

ALTER TABLE ud.moon
    ADD COLUMN IF NOT EXISTS orbital_elements_id BIGINT REFERENCES ud.orbital_elements(id) ON DELETE SET NULL;

-- ─── 6. Add orbital_elements_id FK to asteroid ───────────────────
-- Also change belt_id → band_id reference

ALTER TABLE ud.asteroid
    ADD COLUMN IF NOT EXISTS orbital_elements_id BIGINT REFERENCES ud.orbital_elements(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS band_id BIGINT REFERENCES ud.orbital_band(id) ON DELETE SET NULL;

-- ─── 7. Drop old orbital columns from planet ─────────────────────

ALTER TABLE ud.planet
    DROP COLUMN IF EXISTS semi_major_axis_au,
    DROP COLUMN IF EXISTS orbital_period_days,
    DROP COLUMN IF EXISTS eccentricity,
    DROP COLUMN IF EXISTS orbital_inclination_degrees,
    DROP COLUMN IF EXISTS longitude_of_ascending_node_degrees,
    DROP COLUMN IF EXISTS argument_of_periapsis_degrees,
    DROP COLUMN IF EXISTS mean_anomaly_degrees,
    DROP COLUMN IF EXISTS orbit_stability,
    DROP COLUMN IF EXISTS orbit_stability_timescale_my,
    DROP COLUMN IF EXISTS orbit_crossing_neighbor;

-- ─── 8. Drop old orbital columns from moon ───────────────────────

ALTER TABLE ud.moon
    DROP COLUMN IF EXISTS semi_major_axis_km,
    DROP COLUMN IF EXISTS orbital_period_days,
    DROP COLUMN IF EXISTS eccentricity,
    DROP COLUMN IF EXISTS orbital_inclination_degrees,
    DROP COLUMN IF EXISTS longitude_of_ascending_node_degrees,
    DROP COLUMN IF EXISTS argument_of_periapsis_degrees,
    DROP COLUMN IF EXISTS mean_anomaly_degrees,
    DROP COLUMN IF EXISTS orbit_stability;

-- ─── 9. Drop old orbital columns from asteroid ──────────────────

ALTER TABLE ud.asteroid
    DROP COLUMN IF EXISTS semi_major_axis_au,
    DROP COLUMN IF EXISTS eccentricity,
    DROP COLUMN IF EXISTS inclination_degrees,
    DROP COLUMN IF EXISTS orbital_period_days,
    DROP COLUMN IF EXISTS belt_id;

-- ─── 10. Drop old belt and ring tables ───────────────────────────

DROP TABLE IF EXISTS ud.belt_dwarf_planet CASCADE;
DROP TABLE IF EXISTS ud.ring CASCADE;
DROP TABLE IF EXISTS ud.belt CASCADE;
