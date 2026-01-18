-- =====================================================================
-- Flyway Migration V10: Planet Generation System (CORRECTED)
-- =====================================================================
-- Uses proper JPA JOINED inheritance strategy with CelestialBody
-- =====================================================================

-- =====================================================================
-- SECTION 1: Create Planet Entity Table
-- =====================================================================

CREATE TABLE IF NOT EXISTS ud.planet (
    -- Primary Key (inherits from celestial_body)
    id BIGINT PRIMARY KEY REFERENCES ud.celestial_body(id) ON DELETE CASCADE,
    
    -- Planet-specific properties (not in CelestialBody)
    -- Note: name, mass, radius, circumference, system_id, distanceFromStar, 
    --       orbitalOrder, created_at, modified_at are in celestial_body table
    
    -- Planet Classification
    planet_type VARCHAR(100),
    
    -- Mass and Size (in Earth units)
    earth_mass DOUBLE PRECISION,
    earth_radius DOUBLE PRECISION,
    
    -- Orbital Parameters
    orbital_period_days DOUBLE PRECISION,
    semi_major_axis_au DOUBLE PRECISION,
    eccentricity DOUBLE PRECISION,
    orbital_inclination_degrees DOUBLE PRECISION,
    
    -- Physical Characteristics
    surface_temp_kelvin DOUBLE PRECISION,
    surface_pressure_atm DOUBLE PRECISION,
    escape_velocity_km_s DOUBLE PRECISION,
    surface_gravity_g DOUBLE PRECISION,
    density_g_cm3 DOUBLE PRECISION,
    
    -- Rotation and Orientation
    rotation_period_hours DOUBLE PRECISION,
    axial_tilt_degrees DOUBLE PRECISION,
    is_tidally_locked BOOLEAN DEFAULT FALSE,
    
    -- Magnetic and Atmospheric Properties
    magnetic_field_strength DOUBLE PRECISION,
    atmosphere_composition TEXT,
    albedo DOUBLE PRECISION,
    
    -- Satellite Systems
    has_rings BOOLEAN DEFAULT FALSE,
    number_of_moons INTEGER DEFAULT 0,
    
    -- Geological Properties
    core_type VARCHAR(100),
    geological_activity VARCHAR(100),
    water_coverage_percent DOUBLE PRECISION,
    
    -- Habitability
    habitable_zone_position VARCHAR(50),
    
    -- Age
    age_millions_years DOUBLE PRECISION,
    
    -- Parent Star Relationship (planets orbit stars specifically, not just systems)
    star_id BIGINT REFERENCES ud.star(id) ON DELETE CASCADE,
    
    -- Constraints
    CONSTRAINT chk_planet_earth_mass_positive CHECK (earth_mass IS NULL OR earth_mass > 0),
    CONSTRAINT chk_planet_earth_radius_positive CHECK (earth_radius IS NULL OR earth_radius > 0),
    CONSTRAINT chk_planet_eccentricity_range CHECK (eccentricity IS NULL OR (eccentricity >= 0 AND eccentricity < 1)),
    CONSTRAINT chk_planet_albedo_range CHECK (albedo IS NULL OR (albedo >= 0 AND albedo <= 1)),
    CONSTRAINT chk_planet_water_coverage_range CHECK (water_coverage_percent IS NULL OR (water_coverage_percent >= 0 AND water_coverage_percent <= 100)),
    CONSTRAINT chk_planet_moons_non_negative CHECK (number_of_moons >= 0)
);

-- =====================================================================
-- SECTION 2: Create Indexes for Performance
-- =====================================================================

-- Index for querying planets by parent star
CREATE INDEX IF NOT EXISTS idx_planet_star_id 
ON ud.planet(star_id);

-- Index for querying planets by type
CREATE INDEX IF NOT EXISTS idx_planet_type 
ON ud.planet(planet_type);

-- Index for habitable zone queries
CREATE INDEX IF NOT EXISTS idx_planet_habitable_zone 
ON ud.planet(habitable_zone_position);

-- Index for mass-based queries
CREATE INDEX IF NOT EXISTS idx_planet_earth_mass 
ON ud.planet(earth_mass) WHERE earth_mass IS NOT NULL;

-- =====================================================================
-- SECTION 3: Create Planet Type Reference Table
-- =====================================================================

CREATE TABLE IF NOT EXISTS ref.planet_type_ref (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,
    
    -- Identification
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    
    -- Mass Range (in Earth masses)
    min_mass_earth DOUBLE PRECISION NOT NULL,
    max_mass_earth DOUBLE PRECISION NOT NULL,
    
    -- Radius Range (in Earth radii)
    min_radius_earth DOUBLE PRECISION NOT NULL,
    max_radius_earth DOUBLE PRECISION NOT NULL,
    
    -- Physical Properties
    typical_density_g_cm3 DOUBLE PRECISION,
    typical_albedo DOUBLE PRECISION DEFAULT 0.3,
    typical_core_type VARCHAR(100),
    
    -- Formation Parameters
    min_formation_distance_au DOUBLE PRECISION,
    max_formation_distance_au DOUBLE PRECISION,
    formation_zone VARCHAR(50), -- 'inner', 'habitable', 'outer', 'frost_line'
    
    -- Atmospheric Properties
    can_have_atmosphere BOOLEAN DEFAULT TRUE,
    typical_atmosphere TEXT,
    
    -- Satellite Systems
    can_have_rings BOOLEAN DEFAULT FALSE,
    ring_probability DOUBLE PRECISION DEFAULT 0.0,
    min_moons INTEGER DEFAULT 0,
    max_moons INTEGER DEFAULT 0,
    
    -- Habitability
    habitable BOOLEAN DEFAULT FALSE,
    
    -- Generation Parameters
    rarity_weight INTEGER NOT NULL DEFAULT 100,
    
    -- Constraints
    CONSTRAINT chk_planet_type_mass_range CHECK (max_mass_earth >= min_mass_earth),
    CONSTRAINT chk_planet_type_radius_range CHECK (max_radius_earth >= min_radius_earth),
    CONSTRAINT chk_planet_type_density_positive CHECK (typical_density_g_cm3 IS NULL OR typical_density_g_cm3 > 0),
    CONSTRAINT chk_planet_type_albedo_range CHECK (typical_albedo >= 0 AND typical_albedo <= 1),
    CONSTRAINT chk_planet_type_ring_probability CHECK (ring_probability >= 0 AND ring_probability <= 1),
    CONSTRAINT chk_planet_type_moons_range CHECK (max_moons >= min_moons),
    CONSTRAINT chk_planet_type_rarity_positive CHECK (rarity_weight > 0)
);

-- Index for formation zone queries
CREATE INDEX IF NOT EXISTS idx_planet_type_formation_zone 
ON ref.planet_type_ref(formation_zone);

-- Index for habitable type queries
CREATE INDEX IF NOT EXISTS idx_planet_type_habitable 
ON ref.planet_type_ref(habitable) WHERE habitable = TRUE;

-- =====================================================================
-- SECTION 4: Insert Planet Type Reference Data
-- =====================================================================

-- TERRESTRIAL PLANETS (Rocky Worlds)

INSERT INTO ref.planet_type_ref (
    name, description, 
    min_mass_earth, max_mass_earth, 
    min_radius_earth, max_radius_earth,
    typical_density_g_cm3, 
    min_formation_distance_au, max_formation_distance_au,
    can_have_atmosphere, typical_atmosphere, 
    can_have_rings, ring_probability,
    min_moons, max_moons, 
    habitable, typical_albedo, typical_core_type, 
    rarity_weight, formation_zone
) VALUES 
-- Hot Rocky Planet (Mercury-like)
('Hot Rocky Planet', 'Small rocky planet very close to its star, extremely hot with little to no atmosphere', 
 0.05, 0.4, 0.3, 0.6, 5.4, 0.05, 0.4, 
 false, 'None', false, 0.0, 0, 0, 
 false, 0.12, 'Iron-Nickel', 150, 'inner'),

-- Earth-like
('Terrestrial Planet', 'Rocky planet with potential for atmosphere and liquid water', 
 0.5, 2.0, 0.7, 1.3, 5.5, 0.7, 1.5, 
 true, 'N2, O2, trace gases', false, 0.0, 0, 2, 
 true, 0.3, 'Iron-Nickel', 200, 'habitable'),

-- Super-Earth
('Super-Earth', 'Large rocky planet with high gravity, can retain thick atmosphere', 
 2.0, 10.0, 1.3, 2.0, 5.0, 0.5, 2.0, 
 true, 'N2, CO2, H2O vapor', false, 0.0, 0, 4, 
 true, 0.35, 'Iron-Silicate', 180, 'habitable'),

-- Dry/Desert World
('Desert Planet', 'Rocky world with minimal water, thin atmosphere', 
 0.4, 1.5, 0.6, 1.2, 5.2, 0.8, 2.0, 
 true, 'CO2, N2, trace H2O', false, 0.0, 0, 2, 
 false, 0.25, 'Iron-Nickel', 120, 'inner'),

-- Ocean World
('Ocean Planet', 'Water-rich world with deep global oceans', 
 0.8, 3.0, 0.9, 1.5, 4.0, 0.9, 1.8, 
 true, 'H2O vapor, N2, O2', false, 0.0, 0, 3, 
 true, 0.4, 'Silicate-Water', 100, 'habitable'),

-- Lava World
('Lava Planet', 'Extremely hot world with molten surface due to tidal heating or proximity to star', 
 0.5, 2.0, 0.7, 1.2, 5.5, 0.01, 0.15, 
 true, 'Vaporized rock', false, 0.0, 0, 0, 
 false, 0.1, 'Molten Iron', 80, 'inner')
ON CONFLICT (name) DO NOTHING;

-- GAS DWARFS AND SUB-NEPTUNES

INSERT INTO ref.planet_type_ref (
    name, description, 
    min_mass_earth, max_mass_earth, 
    min_radius_earth, max_radius_earth,
    typical_density_g_cm3, 
    min_formation_distance_au, max_formation_distance_au,
    can_have_atmosphere, typical_atmosphere, 
    can_have_rings, ring_probability,
    min_moons, max_moons, 
    habitable, typical_albedo, typical_core_type, 
    rarity_weight, formation_zone
) VALUES 
-- Mini-Neptune
('Mini-Neptune', 'Small gas planet with thick hydrogen-helium atmosphere over rocky core', 
 2.0, 10.0, 1.5, 3.0, 2.5, 0.5, 5.0, 
 true, 'H2, He, CH4, NH3', true, 0.2, 0, 5, 
 false, 0.5, 'Rocky-Ice', 250, 'frost_line'),

-- Sub-Neptune
('Sub-Neptune', 'Neptune-sized planet with substantial atmosphere', 
 10.0, 20.0, 2.5, 4.0, 1.8, 1.0, 10.0, 
 true, 'H2, He, CH4, H2O', true, 0.3, 2, 15, 
 false, 0.55, 'Ice-Rock', 200, 'outer')
ON CONFLICT (name) DO NOTHING;

-- GAS GIANTS

INSERT INTO ref.planet_type_ref (
    name, description, 
    min_mass_earth, max_mass_earth, 
    min_radius_earth, max_radius_earth,
    typical_density_g_cm3, 
    min_formation_distance_au, max_formation_distance_au,
    can_have_atmosphere, typical_atmosphere, 
    can_have_rings, ring_probability,
    min_moons, max_moons, 
    habitable, typical_albedo, typical_core_type, 
    rarity_weight, formation_zone
) VALUES 
-- Hot Jupiter
('Hot Jupiter', 'Massive gas giant orbiting very close to its star', 
 50.0, 500.0, 8.0, 15.0, 0.7, 0.01, 0.1, 
 true, 'H2, He, hot metals', true, 0.1, 0, 2, 
 false, 0.15, 'Diffuse Gas', 100, 'inner'),

-- Jupiter-like
('Gas Giant', 'Large planet composed primarily of hydrogen and helium', 
 100.0, 400.0, 9.0, 12.0, 1.3, 3.0, 10.0, 
 true, 'H2, He, CH4, NH3', true, 0.4, 10, 80, 
 false, 0.5, 'Diffuse Gas-Metallic H', 300, 'outer'),

-- Super-Jupiter
('Super-Jupiter', 'Extremely massive gas giant', 
 400.0, 3000.0, 12.0, 20.0, 1.8, 5.0, 30.0, 
 true, 'H2, He, exotic compounds', true, 0.5, 20, 100, 
 false, 0.45, 'Metallic Hydrogen', 80, 'outer')
ON CONFLICT (name) DO NOTHING;

-- ICE GIANTS

INSERT INTO ref.planet_type_ref (
    name, description, 
    min_mass_earth, max_mass_earth, 
    min_radius_earth, max_radius_earth,
    typical_density_g_cm3, 
    min_formation_distance_au, max_formation_distance_au,
    can_have_atmosphere, typical_atmosphere, 
    can_have_rings, ring_probability,
    min_moons, max_moons, 
    habitable, typical_albedo, typical_core_type, 
    rarity_weight, formation_zone
) VALUES 
-- Ice Giant
('Ice Giant', 'Planet composed of water, methane, and ammonia ices with small rocky core', 
 10.0, 25.0, 3.5, 5.0, 1.6, 8.0, 30.0, 
 true, 'H2, He, CH4, NH3, H2O', true, 0.4, 5, 30, 
 false, 0.6, 'Ice-Rock', 220, 'outer'),

-- Puffy Planet
('Puffy Planet', 'Low-density gas giant with inflated atmosphere due to stellar heating', 
 50.0, 300.0, 10.0, 18.0, 0.4, 0.02, 0.15, 
 true, 'H2, He, hot volatiles', true, 0.15, 0, 3, 
 false, 0.2, 'Diffuse Gas', 70, 'inner')
ON CONFLICT (name) DO NOTHING;

-- EXOTIC TYPES

INSERT INTO ref.planet_type_ref (
    name, description, 
    min_mass_earth, max_mass_earth, 
    min_radius_earth, max_radius_earth,
    typical_density_g_cm3, 
    min_formation_distance_au, max_formation_distance_au,
    can_have_atmosphere, typical_atmosphere, 
    can_have_rings, ring_probability,
    min_moons, max_moons, 
    habitable, typical_albedo, typical_core_type, 
    rarity_weight, formation_zone
) VALUES 
-- Carbon Planet
('Carbon Planet', 'Rocky planet rich in carbon compounds, graphite, and possibly diamonds', 
 0.5, 5.0, 0.7, 1.8, 6.0, 0.5, 3.0, 
 true, 'CO, CO2, CH4', false, 0.0, 0, 2, 
 false, 0.2, 'Carbon-Iron', 60, 'inner'),

-- Iron Planet
('Iron Planet', 'Stripped rocky core, primarily metallic composition', 
 0.1, 1.0, 0.4, 0.8, 8.0, 0.1, 0.5, 
 false, 'None', false, 0.0, 0, 0, 
 false, 0.1, 'Pure Iron', 50, 'inner'),

-- Ice World
('Ice World', 'Frozen planet beyond the frost line, covered in water and methane ice', 
 0.1, 5.0, 0.5, 2.0, 2.0, 5.0, 50.0, 
 true, 'N2, CH4, CO', false, 0.0, 0, 10, 
 false, 0.7, 'Ice-Rock', 150, 'outer'),

-- Dwarf Planet
('Dwarf Planet', 'Small planetary body, often icy, in outer system', 
 0.001, 0.1, 0.1, 0.5, 2.5, 10.0, 100.0, 
 true, 'N2, CH4, CO (trace)', false, 0.0, 0, 5, 
 false, 0.6, 'Ice-Rock', 100, 'outer'),

-- Rogue Planet
('Rogue Planet', 'Planet ejected from star system or formed independently', 
 0.5, 10.0, 0.8, 3.0, 4.0, 0.0, 0.0, 
 true, 'H2, He, frozen volatiles', false, 0.0, 0, 5, 
 false, 0.05, 'Rocky-Ice', 30, NULL)
ON CONFLICT (name) DO NOTHING;

-- =====================================================================
-- SECTION 5: Add Table Comments
-- =====================================================================

COMMENT ON TABLE ud.planet IS 
'Stores planet data - inherits from celestial_body via JPA JOINED strategy';

COMMENT ON TABLE ref.planet_type_ref IS 
'Reference data for planet types defining characteristics and generation parameters';

COMMENT ON COLUMN ud.planet.star_id IS 
'Foreign key to parent star - planets orbit specific stars';

COMMENT ON COLUMN ud.planet.semi_major_axis_au IS 
'Average orbital distance from parent star in Astronomical Units';

-- =====================================================================
-- SECTION 6: Important Notes on CelestialBody Inheritance
-- =====================================================================

-- IMPORTANT: The planet table uses JPA JOINED inheritance:
-- 
-- 1. Common fields are in celestial_body table:
--    - id, name, mass, radius, circumference
--    - system_id (references star_system)
--    - distanceFromStar (redundant with semi_major_axis_au but used by parent)
--    - orbitalOrder (use this instead of separate orbital_position column)
--    - created_at, modified_at
--
-- 2. Planet-specific fields are in planet table
--
-- 3. Queries will automatically JOIN these tables in JPA
--
-- 4. Use orbitalOrder from celestial_body instead of adding orbital_position

-- =====================================================================
-- SECTION 7: Verify Migration
-- =====================================================================

DO $$
DECLARE
    type_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO type_count FROM ref.planet_type_ref;
    IF type_count < 18 THEN
        RAISE EXCEPTION 'Expected 18 planet types, found %', type_count;
    END IF;
    RAISE NOTICE 'Successfully loaded % planet types', type_count;
END $$;

-- Display summary
SELECT 
    'MIGRATION V10 COMPLETE' as status,
    COUNT(*) as total_planet_types,
    COUNT(*) FILTER (WHERE habitable = TRUE) as habitable_types,
    COUNT(*) FILTER (WHERE can_have_rings = TRUE) as types_with_rings,
    SUM(rarity_weight) as total_weight
FROM ref.planet_type_ref;

-- =====================================================================
-- END OF MIGRATION V10
-- =====================================================================
