-- ============================================================================
-- V102: Create Designation entity ("ID card" for every celestial object)
-- ============================================================================
-- Consolidates identity/classification fields from all celestial objects
-- into a shared Designation table. Each object has a one-to-one relationship
-- with its designation, which holds its logged name, display name, type
-- classification, age, and survey history.
-- ============================================================================

-- ── Create designation table ──

CREATE TABLE ud.designation (
    id                       BIGSERIAL PRIMARY KEY,
    body_type                VARCHAR(20)  NOT NULL,
    logged_name              VARCHAR(255) NOT NULL,
    display_name             VARCHAR(255),
    age_my                   DOUBLE PRECISION,
    survey_history           VARCHAR(500),
    object_type              VARCHAR(100),
    habitable_zone_position  VARCHAR(50),
    parent_body_name         VARCHAR(255),
    spectral_type            VARCHAR(20),
    star_role                VARCHAR(20),
    color_index              VARCHAR(50),
    evolutionary_stage       VARCHAR(50),
    formation_type           VARCHAR(50),
    band_category            VARCHAR(20),
    designation_code         VARCHAR(100),
    created_at               TIMESTAMP DEFAULT NOW(),
    modified_at              TIMESTAMP DEFAULT NOW()
);

COMMENT ON TABLE ud.designation IS
'Identity card for any celestial object: system, star, planet, moon, belt, ring, or asteroid';

-- ── Add FK columns on all parent entities ──

ALTER TABLE ud.star_system ADD COLUMN designation_id BIGINT
    REFERENCES ud.designation(id) ON DELETE SET NULL;
CREATE INDEX idx_star_system_designation ON ud.star_system(designation_id);

ALTER TABLE ud.star ADD COLUMN designation_id BIGINT
    REFERENCES ud.designation(id) ON DELETE SET NULL;
CREATE INDEX idx_star_designation ON ud.star(designation_id);

ALTER TABLE ud.planet ADD COLUMN designation_id BIGINT
    REFERENCES ud.designation(id) ON DELETE SET NULL;
CREATE INDEX idx_planet_designation ON ud.planet(designation_id);

ALTER TABLE ud.moon ADD COLUMN designation_id BIGINT
    REFERENCES ud.designation(id) ON DELETE SET NULL;
CREATE INDEX idx_moon_designation ON ud.moon(designation_id);

ALTER TABLE ud.orbital_band ADD COLUMN designation_id BIGINT
    REFERENCES ud.designation(id) ON DELETE SET NULL;
CREATE INDEX idx_orbital_band_designation ON ud.orbital_band(designation_id);

ALTER TABLE ud.asteroid ADD COLUMN designation_id BIGINT
    REFERENCES ud.designation(id) ON DELETE SET NULL;
CREATE INDEX idx_asteroid_designation ON ud.asteroid(designation_id);

-- ── Drop moved columns from parent entities ──
-- Note: enum columns used in JPQL queries (star_role, band_category) are KEPT
-- Note: name columns are KEPT as legacy references

-- Planet: drop ageMY, planetType, habitableZonePosition
ALTER TABLE ud.planet DROP COLUMN IF EXISTS age_millions_years;
ALTER TABLE ud.planet DROP COLUMN IF EXISTS planet_type;
ALTER TABLE ud.planet DROP COLUMN IF EXISTS habitable_zone_position;

-- Star: drop ageMY, type, spectral_type, color_index, evolutionary_stage
-- Keep star_role (used in JPQL) and name (legacy reference)
ALTER TABLE ud.star DROP COLUMN IF EXISTS age_millions_years;
ALTER TABLE ud.star DROP COLUMN IF EXISTS type;
ALTER TABLE ud.star DROP COLUMN IF EXISTS spectral_type;
ALTER TABLE ud.star DROP COLUMN IF EXISTS color_index;
ALTER TABLE ud.star DROP COLUMN IF EXISTS evolutionary_stage;

-- Moon: drop ageMY, moonType, formationType
ALTER TABLE ud.moon DROP COLUMN IF EXISTS age_my;
ALTER TABLE ud.moon DROP COLUMN IF EXISTS moon_type;
ALTER TABLE ud.moon DROP COLUMN IF EXISTS formation_type;

-- OrbitalBand: drop ageMY, bandType
-- Keep band_category (used in JPQL) and name (legacy reference)
ALTER TABLE ud.orbital_band DROP COLUMN IF EXISTS age_my;
ALTER TABLE ud.orbital_band DROP COLUMN IF EXISTS band_type;

-- Asteroid: drop ageMY, designationCode
-- Keep name (legacy reference)
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS age_my;
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS designation_code;
