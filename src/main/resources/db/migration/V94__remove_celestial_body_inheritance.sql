-- ============================================================================
-- V94: Remove CelestialBody JOINED inheritance
-- ============================================================================
-- Planet, Moon, and Star become standalone entities. The celestial_body base
-- table is dropped with CASCADE, which automatically removes all FK constraints
-- that reference it. Each subclass then gets its own id generation.
-- ============================================================================

-- ── Step 1: Add standalone columns to planet, moon, star ──
-- These were previously inherited from celestial_body via JOINED strategy.

ALTER TABLE ud.planet ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();
ALTER TABLE ud.planet ADD COLUMN IF NOT EXISTS modified_at TIMESTAMP DEFAULT NOW();

ALTER TABLE ud.moon ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();
ALTER TABLE ud.moon ADD COLUMN IF NOT EXISTS modified_at TIMESTAMP DEFAULT NOW();

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();
ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS modified_at TIMESTAMP DEFAULT NOW();

-- Star needs its own system_id column (was inherited from celestial_body)
ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS system_id BIGINT
    REFERENCES ud.star_system(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_star_system ON ud.star(system_id);

-- ── Step 2: Drop celestial_body table with CASCADE ──
-- CASCADE automatically removes all FK constraints from star, planet, moon,
-- orbital_band, etc. that reference celestial_body(id).

DROP TABLE IF EXISTS ud.celestial_body CASCADE;

-- ── Step 3: Re-link FKs that were broken by CASCADE ──
-- Note: planet_pkey, moon_pkey, star_pkey are NOT removed by CASCADE because
-- they are primary keys on their own tables, not FKs. Only the FK constraints
-- from subclass.id -> celestial_body.id are removed. However, any FK from
-- OTHER tables that referenced celestial_body(id) will also be lost.

-- Re-create moon -> planet FK (CASCADE may have removed it if it was defined
-- as referencing celestial_body rather than planet directly)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_moon_planet'
        AND conrelid = 'ud.moon'::regclass
    ) THEN
        ALTER TABLE ud.moon ADD CONSTRAINT fk_moon_planet
            FOREIGN KEY (planet_id) REFERENCES ud.planet(id) ON DELETE CASCADE;
    END IF;
EXCEPTION WHEN duplicate_object THEN
    NULL;
END $$;

-- Re-create orbital_band -> planet FK if needed
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'ud' AND table_name = 'orbital_band' AND column_name = 'planet_id'
    ) THEN
        ALTER TABLE ud.orbital_band ADD CONSTRAINT fk_orbital_band_planet
            FOREIGN KEY (planet_id) REFERENCES ud.planet(id) ON DELETE CASCADE;
    END IF;
EXCEPTION WHEN duplicate_object THEN
    NULL;
END $$;

-- Re-create orbital_band -> star_system FK if needed
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'ud' AND table_name = 'orbital_band' AND column_name = 'system_id'
    ) THEN
        -- Only add if not already present
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'fk_orbital_band_system'
            AND conrelid = 'ud.orbital_band'::regclass
        ) THEN
            ALTER TABLE ud.orbital_band ADD CONSTRAINT fk_orbital_band_system
                FOREIGN KEY (system_id) REFERENCES ud.star_system(id) ON DELETE CASCADE;
        END IF;
    END IF;
EXCEPTION WHEN duplicate_object THEN
    NULL;
END $$;

-- ── Step 4: Make subclass PKs auto-generated ──
-- They were previously sharing celestial_body's sequence.
-- Create new sequences starting from current max id.

CREATE SEQUENCE IF NOT EXISTS ud.planet_id_seq OWNED BY ud.planet.id;
SELECT setval('ud.planet_id_seq', COALESCE((SELECT MAX(id) FROM ud.planet), 1));
ALTER TABLE ud.planet ALTER COLUMN id SET DEFAULT nextval('ud.planet_id_seq');

CREATE SEQUENCE IF NOT EXISTS ud.moon_id_seq OWNED BY ud.moon.id;
SELECT setval('ud.moon_id_seq', COALESCE((SELECT MAX(id) FROM ud.moon), 1));
ALTER TABLE ud.moon ALTER COLUMN id SET DEFAULT nextval('ud.moon_id_seq');

CREATE SEQUENCE IF NOT EXISTS ud.star_id_seq OWNED BY ud.star.id;
SELECT setval('ud.star_id_seq', COALESCE((SELECT MAX(id) FROM ud.star), 1));
ALTER TABLE ud.star ALTER COLUMN id SET DEFAULT nextval('ud.star_id_seq');
