-- =====================================================================
-- Flyway Migration V36: Moon System
-- =====================================================================
-- Description: Creates moon entity and reference tables for moon generation
-- =====================================================================

-- =====================================================================
-- SECTION 1: Drop existing tables if they exist (for clean migration)
-- =====================================================================

DROP TABLE IF EXISTS ud.moon CASCADE;
DROP TABLE IF EXISTS ref.moon_type_ref CASCADE;

-- =====================================================================
-- SECTION 2: Create Moon Type Reference Table
-- =====================================================================

CREATE TABLE ref.moon_type_ref (
                                   id BIGSERIAL PRIMARY KEY,
                                   moon_type VARCHAR(50) NOT NULL UNIQUE,
                                   description TEXT,
                                   typical_formation VARCHAR(50),
                                   min_mass_earth_masses DOUBLE PRECISION,
                                   max_mass_earth_masses DOUBLE PRECISION,
                                   typical_composition VARCHAR(50),
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================================
-- SECTION 3: Create Moon Table
-- =====================================================================

CREATE TABLE ud.moon (
    -- Inherits from celestial_body pattern
                         id BIGINT PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         mass DOUBLE PRECISION NOT NULL,
                         radius DOUBLE PRECISION NOT NULL,
                         circumference DOUBLE PRECISION,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Parent relationship
                         planet_id BIGINT NOT NULL,

    -- Moon classification
                         moon_type VARCHAR(50) NOT NULL,
                         formation_type VARCHAR(50),

    -- Physical properties
                         earth_mass DOUBLE PRECISION,
                         earth_radius DOUBLE PRECISION,
                         density DOUBLE PRECISION,
                         composition_type VARCHAR(50),
                         albedo DOUBLE PRECISION,

    -- Orbital properties (relative to parent planet)
                         semi_major_axis_km DOUBLE PRECISION NOT NULL,
                         orbital_period_days DOUBLE PRECISION NOT NULL,
                         eccentricity DOUBLE PRECISION,
                         orbital_inclination_degrees DOUBLE PRECISION,

    -- Tidal properties
                         tidally_locked BOOLEAN NOT NULL DEFAULT TRUE,
                         tidal_heating_level VARCHAR(20),
                         tidal_heating_watt_per_m2 DOUBLE PRECISION,

    -- Rotation
                         rotation_period_hours DOUBLE PRECISION,
                         axial_tilt DOUBLE PRECISION,

    -- Surface properties
                         surface_temp DOUBLE PRECISION,
                         surface_gravity DOUBLE PRECISION,
                         escape_velocity DOUBLE PRECISION,

    -- Geological properties
                         geological_activity VARCHAR(50),
                         has_cryovolcanism BOOLEAN NOT NULL DEFAULT FALSE,
                         has_subsurface_ocean BOOLEAN NOT NULL DEFAULT FALSE,
                         ocean_depth_km DOUBLE PRECISION,
                         ice_shell_thickness_km DOUBLE PRECISION,

    -- Atmosphere
                         has_atmosphere BOOLEAN NOT NULL DEFAULT FALSE,
                         surface_pressure DOUBLE PRECISION,
                         atmosphere_composition VARCHAR(500),

    -- Surface features
                         cratering_level VARCHAR(30),
                         estimated_visible_craters INTEGER,
                         surface_features TEXT,

    -- Ring interaction
                         is_shepherd_moon BOOLEAN NOT NULL DEFAULT FALSE,
                         shepherds_ring_name VARCHAR(50),

    -- Stability constraints
                         hill_sphere_radius_km DOUBLE PRECISION,
                         roche_limit_km DOUBLE PRECISION,
                         orbit_stability VARCHAR(20),

    -- Age
                         age_my DOUBLE PRECISION,

    -- Foreign Keys
                         CONSTRAINT fk_moon_celestial_body FOREIGN KEY (id) REFERENCES ud.celestial_body(id) ON DELETE CASCADE,
                         CONSTRAINT fk_moon_planet FOREIGN KEY (planet_id) REFERENCES ud.planet(id) ON DELETE CASCADE
);

-- =====================================================================
-- SECTION 4: Create Indexes
-- =====================================================================

CREATE INDEX idx_moon_planet_id ON ud.moon(planet_id);
CREATE INDEX idx_moon_type ON ud.moon(moon_type);
CREATE INDEX idx_moon_formation_type ON ud.moon(formation_type);

-- =====================================================================
-- SECTION 5: Insert Reference Data
-- =====================================================================

INSERT INTO ref.moon_type_ref (moon_type, description, typical_formation, min_mass_earth_masses, max_mass_earth_masses, typical_composition) VALUES
                                                                                                                                                 ('REGULAR_LARGE', 'Large regular moon (e.g., Ganymede, Titan, Callisto)', 'CO_FORMED', 0.01, 0.025, 'MIXED'),
                                                                                                                                                 ('REGULAR_MEDIUM', 'Medium regular moon (e.g., Io, Europa, Triton)', 'CO_FORMED', 0.001, 0.01, 'MIXED'),
                                                                                                                                                 ('REGULAR_SMALL', 'Small regular moon (e.g., Mimas, Enceladus)', 'CO_FORMED', 0.00001, 0.001, 'ICY'),
                                                                                                                                                 ('IRREGULAR_CAPTURED', 'Captured asteroid or comet', 'CAPTURED', 0.000000001, 0.0001, 'ROCKY'),
                                                                                                                                                 ('SHEPHERD', 'Shepherd moon maintaining ring structure', 'CO_FORMED', 0.0000000001, 0.00001, 'ROCKY'),
                                                                                                                                                 ('TROJAN', 'Moon in planet''s Lagrange points', 'CAPTURED', 0.0000001, 0.0001, 'ROCKY'),
                                                                                                                                                 ('COLLISION_DEBRIS', 'Moon formed from planetary collision (e.g., Earth''s Moon)', 'COLLISION_DEBRIS', 0.001, 0.02, 'ROCKY');