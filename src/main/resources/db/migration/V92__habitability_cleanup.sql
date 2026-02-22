-- ============================================================
-- V92: Habitability @OneToOne cleanup — parent-side FK
-- ============================================================

-- 1. Add habitability_id FK to planet and moon tables
ALTER TABLE ud.planet
    ADD COLUMN habitability_id BIGINT REFERENCES ud.planetary_habitability(id) ON DELETE SET NULL;
ALTER TABLE ud.moon
    ADD COLUMN habitability_id BIGINT REFERENCES ud.planetary_habitability(id) ON DELETE SET NULL;

-- 2. Migrate existing data from child-side to parent-side FK
UPDATE ud.planet p SET habitability_id = ph.id
FROM ud.planetary_habitability ph WHERE ph.planet_id = p.id;

UPDATE ud.moon m SET habitability_id = ph.id
FROM ud.planetary_habitability ph WHERE ph.moon_id = m.id;

-- 3. Drop old child-side FK columns from planetary_habitability
ALTER TABLE ud.planetary_habitability DROP COLUMN IF EXISTS planet_id;
ALTER TABLE ud.planetary_habitability DROP COLUMN IF EXISTS moon_id;

-- 4. Add indexes on parent-side FK
CREATE INDEX idx_planet_habitability_id ON ud.planet(habitability_id);
CREATE INDEX idx_moon_habitability_id ON ud.moon(habitability_id);
