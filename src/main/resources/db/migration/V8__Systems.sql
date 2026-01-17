-- Add timestamp columns to star_system
ALTER TABLE ud.star_system
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS modified_at TIMESTAMP DEFAULT NOW();

-- Update existing rows to have timestamps
UPDATE ud.star_system
SET created_at = NOW(), modified_at = NOW()
WHERE created_at IS NULL;

-- Add binary configuration to star_system
ALTER TABLE ud.star_system
    ADD COLUMN IF NOT EXISTS binary_configuration VARCHAR(50),
    ADD COLUMN IF NOT EXISTS primary_star_id BIGINT,
    ADD COLUMN IF NOT EXISTS binary_separation_au DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS binary_orbital_period_days DOUBLE PRECISION;

-- Add star role to star table
ALTER TABLE ud.star
    ADD COLUMN IF NOT EXISTS star_role VARCHAR(20);

CREATE INDEX idx_star_system_config ON ud.star_system(binary_configuration);
CREATE INDEX idx_star_role ON ud.star(star_role);