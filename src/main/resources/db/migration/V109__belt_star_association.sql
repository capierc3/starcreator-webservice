-- ═══════════════════════════════════════════════════════════════════
-- V109: Move belt parent from star_system to star
--
-- Belts now belong to a specific Star (like planets do), not to
-- the StarSystem directly. This enables per-star belt generation
-- in multi-star systems and gives the UI a concrete star reference
-- for positioning.
--
-- For circumbinary belts (P_TYPE), the PRIMARY star is the parent
-- (same convention used for circumbinary planets).
-- ═══════════════════════════════════════════════════════════════════

-- 1. Add star_id FK
ALTER TABLE ud.orbital_band
    ADD COLUMN star_id BIGINT REFERENCES ud.star(id) ON DELETE CASCADE;

-- 2. Migrate existing belt data: star_system_id -> primary star id
UPDATE ud.orbital_band ob
SET star_id = (
    SELECT s.id FROM ud.star s
    WHERE s.system_id = ob.star_system_id
      AND s.star_role = 'PRIMARY'
    LIMIT 1
)
WHERE ob.star_system_id IS NOT NULL;

-- 3. Drop old constraint and column
ALTER TABLE ud.orbital_band DROP CONSTRAINT chk_band_parent;
ALTER TABLE ud.orbital_band DROP COLUMN star_system_id;

-- 4. New constraint: exactly one of star_id or planet_id must be set
ALTER TABLE ud.orbital_band ADD CONSTRAINT chk_band_parent CHECK (
    (star_id IS NOT NULL AND planet_id IS NULL) OR
    (star_id IS NULL AND planet_id IS NOT NULL)
);

-- 5. Indexes
DROP INDEX IF EXISTS ud.idx_orbital_band_system;
CREATE INDEX idx_orbital_band_star ON ud.orbital_band(star_id) WHERE star_id IS NOT NULL;
