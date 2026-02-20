-- V50__orbital_elements.sql (or whatever your next migration number is)

-- =====================================================================
-- Add Keplerian orbital elements for 3D visualization
-- =====================================================================

-- Planets
ALTER TABLE ud.planet
    ADD COLUMN IF NOT EXISTS longitude_of_ascending_node_degrees DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS argument_of_periapsis_degrees DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS mean_anomaly_degrees DOUBLE PRECISION;

COMMENT ON COLUMN ud.planet.longitude_of_ascending_node_degrees IS
    'Longitude of ascending node (Ω) - direction of orbital tilt, 0-360 degrees';
COMMENT ON COLUMN ud.planet.argument_of_periapsis_degrees IS
    'Argument of periapsis (ω) - rotation of ellipse within orbital plane, 0-360 degrees';
COMMENT ON COLUMN ud.planet.mean_anomaly_degrees IS
    'Mean anomaly at epoch - initial orbital position, 0-360 degrees';

-- Moons
ALTER TABLE ud.moon
    ADD COLUMN IF NOT EXISTS longitude_of_ascending_node_degrees DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS argument_of_periapsis_degrees DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS mean_anomaly_degrees DOUBLE PRECISION;

COMMENT ON COLUMN ud.moon.longitude_of_ascending_node_degrees IS
    'Longitude of ascending node (Ω) relative to parent planet equatorial plane, 0-360 degrees';
COMMENT ON COLUMN ud.moon.argument_of_periapsis_degrees IS
    'Argument of periapsis (ω) within moon orbital plane, 0-360 degrees';
COMMENT ON COLUMN ud.moon.mean_anomaly_degrees IS
    'Mean anomaly at epoch - initial orbital position, 0-360 degrees';