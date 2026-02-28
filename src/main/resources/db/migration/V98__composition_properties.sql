-- ============================================================================
-- V98: Create CompositionProperties entity
-- ============================================================================
-- Consolidates composition fields from Planet, Moon, and Asteroid into a
-- shared CompositionProperties table. Body-type-specific fields are nullable
-- and hidden by @JsonInclude(NON_NULL).
-- ============================================================================

-- ── Create composition_properties table ──

CREATE TABLE ud.composition_properties (
    id                         BIGSERIAL PRIMARY KEY,
    core_type                  VARCHAR(100),
    interior_composition       VARCHAR(500),
    envelope_composition       VARCHAR(500),
    composition_classification VARCHAR(50),
    composition_type           VARCHAR(50),
    composition                TEXT,
    is_differentiated          BOOLEAN,
    created_at                 TIMESTAMP DEFAULT NOW(),
    modified_at                TIMESTAMP DEFAULT NOW()
);

COMMENT ON TABLE ud.composition_properties IS
'Composition properties for any celestial body (planet, moon, or asteroid)';

-- ── Add FK columns ──

ALTER TABLE ud.planet ADD COLUMN composition_properties_id BIGINT
    REFERENCES ud.composition_properties(id) ON DELETE SET NULL;
CREATE INDEX idx_planet_composition_properties ON ud.planet(composition_properties_id);

ALTER TABLE ud.moon ADD COLUMN composition_properties_id BIGINT
    REFERENCES ud.composition_properties(id) ON DELETE SET NULL;
CREATE INDEX idx_moon_composition_properties ON ud.moon(composition_properties_id);

ALTER TABLE ud.asteroid ADD COLUMN composition_properties_id BIGINT
    REFERENCES ud.composition_properties(id) ON DELETE SET NULL;
CREATE INDEX idx_asteroid_composition_properties ON ud.asteroid(composition_properties_id);

-- ── Drop old composition columns ──

ALTER TABLE ud.planet DROP COLUMN IF EXISTS core_type;
ALTER TABLE ud.planet DROP COLUMN IF EXISTS interior_composition;
ALTER TABLE ud.planet DROP COLUMN IF EXISTS envelope_composition;
ALTER TABLE ud.planet DROP COLUMN IF EXISTS composition_classification;

ALTER TABLE ud.moon DROP COLUMN IF EXISTS composition_type;
ALTER TABLE ud.moon DROP COLUMN IF EXISTS interior_composition;
ALTER TABLE ud.moon DROP COLUMN IF EXISTS envelope_composition;
ALTER TABLE ud.moon DROP COLUMN IF EXISTS composition_classification;

ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS composition;
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS is_differentiated;
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS core_type;
