-- Create user-defined sectors (larger galactic regions)
CREATE TABLE IF NOT EXISTS ud.sector (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    z INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    modified_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(x, y, z)
);

CREATE INDEX idx_sector_coordinates ON ud.sector(x, y, z);

-- Create star systems table (within sectors)
CREATE TABLE IF NOT EXISTS ud.star_system (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    sector_id BIGINT,
    size_au DOUBLE PRECISION,
    habitable_low DOUBLE PRECISION,
    habitable_high DOUBLE PRECISION,
    description TEXT,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    z INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    modified_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT star_system_sector_fk FOREIGN KEY (sector_id) REFERENCES ud.sector(id) ON DELETE CASCADE
);

CREATE INDEX idx_star_system_sector ON ud.star_system(sector_id);
CREATE INDEX idx_star_system_coordinates ON ud.star_system(x, y, z);

-- Create faction presence table
CREATE TABLE IF NOT EXISTS ud.faction_presence (
    id BIGSERIAL PRIMARY KEY,
    faction_id BIGINT NOT NULL,
    system_id BIGINT NOT NULL,
    influence_level INTEGER DEFAULT 0,
    is_controlling BOOLEAN DEFAULT FALSE,
    since_date TIMESTAMP DEFAULT NOW(),
    UNIQUE(faction_id, system_id),
    CONSTRAINT fp_faction_fk FOREIGN KEY (faction_id) REFERENCES ud.factions(id) ON DELETE CASCADE,
    CONSTRAINT fp_system_fk FOREIGN KEY (system_id) REFERENCES ud.star_system(id) ON DELETE CASCADE
);

-- Add system reference to celestial bodies
ALTER TABLE ud.celestial_body
    ADD COLUMN IF NOT EXISTS system_id BIGINT,
    ADD COLUMN IF NOT EXISTS distance_from_star_au DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS orbital_order INTEGER;

ALTER TABLE ud.celestial_body
    ADD CONSTRAINT celestial_body_system_fk
        FOREIGN KEY (system_id) REFERENCES ud.star_system(id) ON DELETE CASCADE;

CREATE INDEX idx_celestial_body_system ON ud.celestial_body(system_id);

-- Update sector_id in celestial_body to reference ud.sector
-- Drop old constraint if it exists (from earlier setup)
ALTER TABLE ud.celestial_body
    DROP CONSTRAINT IF EXISTS celestial_body_sector_fk;

CREATE INDEX idx_faction_presence_faction ON ud.faction_presence(faction_id);
CREATE INDEX idx_faction_presence_system ON ud.faction_presence(system_id);