-- Add timestamps (should already exist from CelestialBody, but ensuring they're there)
ALTER TABLE ud.celestial_body
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW(),
ADD COLUMN IF NOT EXISTS modified_at TIMESTAMP DEFAULT NOW();

-- Add new star-specific properties
ALTER TABLE ud.star
ADD COLUMN IF NOT EXISTS age_millions_years DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS metallicity DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS rotation_days DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS color_index VARCHAR(50),
ADD COLUMN IF NOT EXISTS is_variable BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS variability_period_days DOUBLE PRECISION;

-- Add indexes for common queries
CREATE INDEX IF NOT EXISTS idx_star_age ON ud.star(age_millions_years);
CREATE INDEX IF NOT EXISTS idx_star_is_variable ON ud.star(is_variable);