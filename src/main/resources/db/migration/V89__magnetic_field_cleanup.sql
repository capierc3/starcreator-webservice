-- =====================================================================
-- Flyway Migration V89: Magnetic Field @OneToOne Conversion
-- =====================================================================
-- Description: Converts PlanetaryMagneticField from child-side FK
--              (planet_id, moon_id on planetary_magnetic_field) to
--              parent-side FK (magnetic_field_id on planet and moon).
--              Also drops deprecated magnetic_field_strength from planet.
-- =====================================================================

-- =====================================================================
-- SECTION 1: Add magnetic_field_id FK to planet table
-- =====================================================================

ALTER TABLE ud.planet
    ADD COLUMN magnetic_field_id BIGINT REFERENCES ud.planetary_magnetic_field(id) ON DELETE SET NULL;

CREATE INDEX idx_planet_magnetic_field_id ON ud.planet(magnetic_field_id);

-- =====================================================================
-- SECTION 2: Add magnetic_field_id FK to moon table
-- =====================================================================

ALTER TABLE ud.moon
    ADD COLUMN magnetic_field_id BIGINT REFERENCES ud.planetary_magnetic_field(id) ON DELETE SET NULL;

CREATE INDEX idx_moon_magnetic_field_id ON ud.moon(magnetic_field_id);

-- =====================================================================
-- SECTION 3: Migrate existing data (populate parent-side FK)
-- =====================================================================

UPDATE ud.planet p
SET magnetic_field_id = pmf.id
FROM ud.planetary_magnetic_field pmf
WHERE pmf.planet_id = p.id;

UPDATE ud.moon m
SET magnetic_field_id = pmf.id
FROM ud.planetary_magnetic_field pmf
WHERE pmf.moon_id = m.id;

-- =====================================================================
-- SECTION 4: Drop old child-side FK columns from planetary_magnetic_field
-- =====================================================================

ALTER TABLE ud.planetary_magnetic_field DROP CONSTRAINT IF EXISTS fk_planet;
ALTER TABLE ud.planetary_magnetic_field DROP COLUMN IF EXISTS planet_id;
ALTER TABLE ud.planetary_magnetic_field DROP COLUMN IF EXISTS moon_id;

-- =====================================================================
-- SECTION 5: Drop deprecated magnetic_field_strength from planet
-- =====================================================================

ALTER TABLE ud.planet DROP COLUMN IF EXISTS magnetic_field_strength;

-- =====================================================================
-- SECTION 6: Drop old indexes that referenced dropped columns
-- =====================================================================

DROP INDEX IF EXISTS ud.idx_mag_field_planet_id;

-- =====================================================================
-- SECTION 7: Verify Migration
-- =====================================================================

DO $$
    BEGIN
        RAISE NOTICE 'Migration V89 completed successfully';
        RAISE NOTICE 'Added magnetic_field_id to: ud.planet, ud.moon';
        RAISE NOTICE 'Dropped planet_id, moon_id from: ud.planetary_magnetic_field';
        RAISE NOTICE 'Dropped magnetic_field_strength from: ud.planet';
    END $$;

-- =====================================================================
-- END OF MIGRATION V89
-- =====================================================================
