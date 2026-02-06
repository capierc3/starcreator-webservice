-- =====================================================================
-- V60__belt_system.sql - Belt and Asteroid Generation System
-- =====================================================================

-- =====================================================================
-- SECTION 1: Reference Tables
-- =====================================================================

CREATE TABLE ref.belt_type (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(30) NOT NULL UNIQUE,
    description TEXT,
    typical_composition_type VARCHAR(20),
    min_eccentricity DECIMAL(5,4),
    max_eccentricity DECIMAL(5,4),
    min_inclination_degrees DECIMAL(5,2),
    max_inclination_degrees DECIMAL(5,2),
    typical_mass_earth_masses_min DECIMAL(12,10),
    typical_mass_earth_masses_max DECIMAL(12,10),
    notable_object_chance DECIMAL(3,2),
    max_notable_objects INTEGER,
    requires_giant_planet BOOLEAN DEFAULT true,
    min_system_age_my DECIMAL(10,2),
    location_description TEXT,
    formation_description TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE ref.asteroid_type (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(10) NOT NULL UNIQUE,
    spectral_class VARCHAR(10),
    description TEXT,
    primary_composition TEXT,
    albedo_min DECIMAL(4,3),
    albedo_max DECIMAL(4,3),
    density_min DECIMAL(4,2),
    density_max DECIMAL(4,2),
    belt_affinity VARCHAR(30),
    relative_abundance DECIMAL(4,3),
    can_be_differentiated BOOLEAN DEFAULT false,
    typical_surface_features TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- =====================================================================
-- SECTION 2: User Data Tables
-- =====================================================================

CREATE TABLE ud.belt (
    id BIGSERIAL PRIMARY KEY,
    star_system_id BIGINT NOT NULL REFERENCES ud.star_system(id) ON DELETE CASCADE,
    belt_type_id INTEGER NOT NULL REFERENCES ref.belt_type(id),
    name VARCHAR(100),
    
    -- Orbital bounds
    inner_radius_au DECIMAL(10,4) NOT NULL,
    outer_radius_au DECIMAL(10,4) NOT NULL,
    peak_density_au DECIMAL(10,4),
    
    -- Physical properties
    total_mass_kg DECIMAL(30,0),
    total_mass_earth_masses DECIMAL(15,12),
    estimated_object_count BIGINT,
    
    -- Orbital characteristics
    average_eccentricity DECIMAL(5,4),
    max_eccentricity DECIMAL(5,4),
    average_inclination_degrees DECIMAL(5,2),
    max_inclination_degrees DECIMAL(5,2),
    
    -- Composition
    composition_type VARCHAR(20),
    primary_composition TEXT,
    
    -- Structure
    has_resonance_gaps BOOLEAN DEFAULT false,
    gap_description TEXT,
    has_collisional_families BOOLEAN DEFAULT false,
    family_count INTEGER,
    
    -- Metadata
    age_my DECIMAL(12,4),
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    modified_at TIMESTAMP DEFAULT NOW()
);

-- Junction table for dwarf planets in belts
CREATE TABLE ud.belt_dwarf_planet (
    belt_id BIGINT NOT NULL REFERENCES ud.belt(id) ON DELETE CASCADE,
    planet_id BIGINT NOT NULL REFERENCES ud.planet(id) ON DELETE CASCADE,
    is_largest_in_belt BOOLEAN DEFAULT false,
    orbital_classification VARCHAR(50),
    PRIMARY KEY (belt_id, planet_id)
);

CREATE TABLE ud.asteroid (
    id BIGSERIAL PRIMARY KEY,
    belt_id BIGINT REFERENCES ud.belt(id) ON DELETE CASCADE,
    asteroid_type_id INTEGER NOT NULL REFERENCES ref.asteroid_type(id),
    
    -- Identity
    name VARCHAR(100) NOT NULL,
    designation_code VARCHAR(50),
    
    -- Physical properties
    mass DECIMAL(30,0),
    radius DECIMAL(15,4),
    dimensions_km VARCHAR(100),
    circumference DECIMAL(20,6),
    earth_mass DECIMAL(20,15),
    density DECIMAL(6,4),
    albedo DECIMAL(5,4),
    
    -- Orbital elements
    semi_major_axis_au DECIMAL(12,6) NOT NULL,
    eccentricity DECIMAL(8,6),
    inclination_degrees DECIMAL(8,4),
    orbital_period_days DECIMAL(15,4),
    
    -- Rotation
    rotation_period_hours DECIMAL(10,4),
    axial_tilt DECIMAL(6,3),
    
    -- Surface
    surface_temp DECIMAL(8,3),
    surface_gravity DECIMAL(12,8),
    escape_velocity DECIMAL(10,6),
    surface_features TEXT,
    cratering_level VARCHAR(20),
    has_regolith BOOLEAN DEFAULT true,
    regolith_depth_m DECIMAL(8,2),
    
    -- Composition
    composition TEXT,
    is_differentiated BOOLEAN DEFAULT false,
    core_type VARCHAR(50),
    
    -- Moons (asteroids can have them!)
    has_moon BOOLEAN DEFAULT false,
    moon_count INTEGER DEFAULT 0,
    moon_description TEXT,
    
    -- Notable status
    is_notable BOOLEAN DEFAULT true,
    notable_reason TEXT,
    
    -- Metadata
    age_my DECIMAL(12,4),
    created_at TIMESTAMP DEFAULT NOW(),
    modified_at TIMESTAMP DEFAULT NOW()
);

-- =====================================================================
-- SECTION 3: Indexes
-- =====================================================================

CREATE INDEX idx_belt_system_id ON ud.belt(star_system_id);
CREATE INDEX idx_belt_type_id ON ud.belt(belt_type_id);
CREATE INDEX idx_asteroid_belt_id ON ud.asteroid(belt_id);
CREATE INDEX idx_asteroid_type_id ON ud.asteroid(asteroid_type_id);
CREATE INDEX idx_belt_dwarf_planet_belt ON ud.belt_dwarf_planet(belt_id);
CREATE INDEX idx_belt_dwarf_planet_planet ON ud.belt_dwarf_planet(planet_id);

-- =====================================================================
-- SECTION 4: Reference Data - Belt Types
-- =====================================================================

INSERT INTO ref.belt_type (name, code, description, typical_composition_type, 
    min_eccentricity, max_eccentricity, min_inclination_degrees, max_inclination_degrees,
    typical_mass_earth_masses_min, typical_mass_earth_masses_max,
    notable_object_chance, max_notable_objects, requires_giant_planet, min_system_age_my,
    location_description, formation_description) VALUES

('Inner Asteroid Belt', 'INNER_ROCKY', 
 'Rocky asteroid belt between inner planets and gas giants, analogous to the Main Belt',
 'ROCKY', 0.05, 0.30, 0.0, 30.0, 
 0.0000001, 0.0005,
 0.80, 6, true, 100.0,
 'Located between the outermost rocky planet and innermost giant planet',
 'Formed from planetesimals that failed to accrete due to gravitational perturbation from the nearby giant planet'),

('Outer Asteroid Belt', 'OUTER_ROCKY',
 'Secondary asteroid belt between giant planets',
 'MIXED', 0.05, 0.25, 0.0, 25.0, 
 0.00000001, 0.0001,
 0.40, 3, true, 100.0,
 'Located in the gap between two giant planets',
 'Remnant material trapped between giant planet orbital resonances'),

('Kuiper Belt', 'KUIPER',
 'Trans-giant icy belt in the outer system',
 'ICY', 0.01, 0.20, 0.0, 35.0, 
 0.001, 0.1,
 0.70, 5, true, 500.0,
 'Beyond the outermost giant planet, extending to the edge of the system',
 'Remnant population of icy planetesimals from the outer protoplanetary disk'),

('Scattered Disk', 'SCATTERED_DISK',
 'Highly eccentric population scattered by giant planet migration',
 'ICY', 0.25, 0.85, 10.0, 60.0, 
 0.0001, 0.01,
 0.30, 2, true, 1000.0,
 'Overlapping and beyond the Kuiper Belt with highly eccentric orbits',
 'Objects scattered into eccentric orbits during giant planet migration in early system history');

-- =====================================================================
-- SECTION 5: Reference Data - Asteroid Types (Rocky Belt)
-- =====================================================================

INSERT INTO ref.asteroid_type (name, code, spectral_class, description, primary_composition,
    albedo_min, albedo_max, density_min, density_max, belt_affinity, relative_abundance,
    can_be_differentiated, typical_surface_features) VALUES

('Carbonaceous', 'C', 'C', 'Dark, primitive asteroids rich in carbon compounds',
 'Carbon 15%, Hydrated Silicates 60%, Organics 25%',
 0.03, 0.10, 1.1, 2.3, 'INNER_ROCKY', 0.35, false,
 'Very dark surface, primitive composition, clay minerals'),

('Silicaceous', 'S', 'S', 'Rocky asteroids with silicate minerals and metal',
 'Olivine 35%, Pyroxene 40%, Iron-Nickel 25%',
 0.10, 0.28, 2.3, 3.5, 'INNER_ROCKY', 0.30, true,
 'Rocky terrain, metallic flecks, moderate cratering'),

('Metallic', 'M', 'M', 'Metal-rich asteroids, possibly exposed planetary cores',
 'Iron 85%, Nickel 10%, Cobalt 3%, Silicates 2%',
 0.10, 0.20, 4.5, 7.5, 'INNER_ROCKY', 0.08, true,
 'Metallic luster, radar-bright, possible core fragment'),

('Basaltic', 'V', 'V', 'Igneous asteroids with basaltic crust',
 'Basalt 50%, Pyroxene 30%, Plagioclase 20%',
 0.20, 0.45, 2.8, 3.5, 'INNER_ROCKY', 0.05, true,
 'Volcanic surface, lava flow features, large impact basins'),

('Enstatite', 'E', 'E', 'High-albedo asteroids with enstatite composition',
 'Enstatite 70%, Iron-Nickel 20%, Sulfides 10%',
 0.25, 0.60, 2.8, 3.5, 'INNER_ROCKY', 0.02, true,
 'Bright surface, formed in reducing conditions'),

('Primitive Dark', 'D', 'D', 'Very dark, organic-rich outer belt asteroids',
 'Organics 40%, Carbon 30%, Anhydrous Silicates 25%, Ice 5%',
 0.02, 0.06, 0.8, 1.8, 'OUTER_ROCKY', 0.10, false,
 'Extremely dark, reddish tint, possibly cometary origin'),

('Primitive', 'P', 'P', 'Low-albedo primitive outer belt asteroids',
 'Organics 30%, Carbon 25%, Silicates 40%, Ice 5%',
 0.02, 0.07, 1.0, 2.0, 'OUTER_ROCKY', 0.07, false,
 'Dark surface, organic-rich, transitional composition'),

('Blue Carbonaceous', 'B', 'B', 'Primitive carbonaceous with blue spectral slope',
 'Hydrated Silicates 50%, Carbon 20%, Organics 20%, Volatiles 10%',
 0.04, 0.10, 1.2, 2.2, 'INNER_ROCKY', 0.03, false,
 'Dark surface, hydration features, volatile content');

-- =====================================================================
-- SECTION 6: Reference Data - Asteroid Types (Kuiper Belt / Icy)
-- =====================================================================

INSERT INTO ref.asteroid_type (name, code, spectral_class, description, primary_composition,
    albedo_min, albedo_max, density_min, density_max, belt_affinity, relative_abundance,
    can_be_differentiated, typical_surface_features) VALUES

('Cold Classical KBO', 'KC', 'IR', 'Primordial KBO with circular, low-inclination orbit',
 'Water Ice 45%, Methane Ice 15%, Ammonia Ice 10%, Rock 25%, Tholins 5%',
 0.04, 0.15, 0.8, 1.8, 'KUIPER', 0.30, false,
 'Icy surface, ultra-red coloring from tholins, pristine'),

('Hot Classical KBO', 'KH', 'IR', 'Dynamically excited KBO with higher inclination',
 'Water Ice 50%, Ammonia Ice 15%, Rock 30%, Organics 5%',
 0.04, 0.12, 1.0, 2.0, 'KUIPER', 0.20, false,
 'Icy surface, less red than cold classicals'),

('Resonant KBO', 'KR', 'IR', 'KBO locked in mean-motion resonance with giant planet',
 'Nitrogen Ice 20%, Water Ice 35%, Methane Ice 15%, Rock 25%, Organics 5%',
 0.04, 0.80, 1.4, 2.2, 'KUIPER', 0.15, true,
 'Variable surface, possible seasonal atmosphere'),

('Scattered Disk Object', 'SD', 'IR', 'Highly eccentric outer system object',
 'Water Ice 55%, Methane Ice 10%, Ammonia Ice 10%, Rock 20%, Organics 5%',
 0.03, 0.10, 0.9, 1.8, 'SCATTERED_DISK', 0.25, false,
 'Icy surface, possible cometary activity near perihelion'),

('Centaur', 'CE', 'IR', 'Transitional object crossing giant planet orbits',
 'Water Ice 40%, Carbon 20%, Silicates 30%, Organics 10%',
 0.02, 0.08, 0.8, 1.5, 'KUIPER', 0.10, false,
 'Dark icy surface, cometary outgassing, chaotic orbit');

-- =====================================================================
-- SECTION 7: Comments
-- =====================================================================

COMMENT ON TABLE ud.belt IS 'Asteroid and Kuiper belt systems generated for star systems';
COMMENT ON TABLE ud.asteroid IS 'Notable asteroids and KBOs within belt systems';
COMMENT ON TABLE ref.belt_type IS 'Reference data defining belt type characteristics';
COMMENT ON TABLE ref.asteroid_type IS 'Reference data defining asteroid spectral and compositional types';
