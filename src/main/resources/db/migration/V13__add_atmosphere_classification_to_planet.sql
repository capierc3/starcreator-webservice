-- V13__add_atmosphere_classification_to_planet.sql

-- Add atmosphere classification column to planet table
ALTER TABLE ud.planet 
ADD COLUMN IF NOT EXISTS atmosphere_classification VARCHAR(50);

-- Create index for querying planets by atmosphere type
CREATE INDEX IF NOT EXISTS idx_planet_atmosphere_classification 
ON ud.planet(atmosphere_classification);

COMMENT ON COLUMN ud.planet.atmosphere_classification IS 'Atmosphere type classification (EARTH_LIKE, VENUS_LIKE, JOVIAN, etc.)';

-- Example query to find breathable planets:
-- SELECT * FROM ud.planet WHERE atmosphere_classification = 'EARTH_LIKE';
