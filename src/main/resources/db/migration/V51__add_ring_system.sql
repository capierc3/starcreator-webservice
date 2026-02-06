-- Create ring table in ud schema
CREATE TABLE IF NOT EXISTS ud.ring (
    id BIGSERIAL PRIMARY KEY,
    planet_id BIGINT NOT NULL,
    name VARCHAR(100),
    ring_type VARCHAR(50) NOT NULL,
    inner_radius_km DOUBLE PRECISION NOT NULL,
    outer_radius_km DOUBLE PRECISION NOT NULL,
    thickness_km DOUBLE PRECISION,
    mass_kg DOUBLE PRECISION,
    optical_depth DOUBLE PRECISION,
    particle_size_min_m DOUBLE PRECISION,
    particle_size_max_m DOUBLE PRECISION,
    composition VARCHAR(500),
    composition_type VARCHAR(50),
    albedo DOUBLE PRECISION,
    color VARCHAR(100),
    visibility VARCHAR(30),
    has_gaps BOOLEAN NOT NULL DEFAULT FALSE,
    gap_description VARCHAR(500),
    has_shepherd_moons BOOLEAN NOT NULL DEFAULT FALSE,
    orbital_resonances VARCHAR(500),
    stability VARCHAR(30),
    estimated_age_my DOUBLE PRECISION,
    origin_type VARCHAR(50),
    formation_description VARCHAR(500),
    created_at TIMESTAMP,
    modified_at TIMESTAMP,
    CONSTRAINT fk_ring_planet FOREIGN KEY (planet_id) REFERENCES ud.planet(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_ring_planet_id ON ud.ring(planet_id);
CREATE INDEX idx_ring_type ON ud.ring(ring_type);

-- Create ring_template table in ref schema
CREATE TABLE IF NOT EXISTS ref.ring_template (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    ring_type VARCHAR(50) NOT NULL,
    planet_types VARCHAR(500),
    min_planet_mass_earth DOUBLE PRECISION,
    max_planet_mass_earth DOUBLE PRECISION,
    min_distance_au DOUBLE PRECISION,
    max_distance_au DOUBLE PRECISION,
    inner_radius_min_planet_radii DOUBLE PRECISION,
    inner_radius_max_planet_radii DOUBLE PRECISION,
    outer_radius_min_planet_radii DOUBLE PRECISION,
    outer_radius_max_planet_radii DOUBLE PRECISION,
    thickness_min_km DOUBLE PRECISION,
    thickness_max_km DOUBLE PRECISION,
    optical_depth_min DOUBLE PRECISION,
    optical_depth_max DOUBLE PRECISION,
    particle_size_min_m DOUBLE PRECISION,
    particle_size_max_m DOUBLE PRECISION,
    composition_type VARCHAR(50),
    composition_description VARCHAR(500),
    albedo_min DOUBLE PRECISION,
    albedo_max DOUBLE PRECISION,
    color VARCHAR(100),
    visibility VARCHAR(30),
    has_gaps_probability DOUBLE PRECISION,
    has_shepherd_moons_probability DOUBLE PRECISION,
    stability VARCHAR(30),
    origin_type VARCHAR(50),
    rarity_weight INTEGER DEFAULT 100,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert Saturn-like main ring system (most common for gas giants)
INSERT INTO ref.ring_template (
    name, description, ring_type, planet_types,
    min_planet_mass_earth, max_planet_mass_earth,
    min_distance_au, max_distance_au,
    inner_radius_min_planet_radii, inner_radius_max_planet_radii,
    outer_radius_min_planet_radii, outer_radius_max_planet_radii,
    thickness_min_km, thickness_max_km,
    optical_depth_min, optical_depth_max,
    particle_size_min_m, particle_size_max_m,
    composition_type, composition_description,
    albedo_min, albedo_max, color, visibility,
    has_gaps_probability, has_shepherd_moons_probability,
    stability, origin_type, rarity_weight
) VALUES (
    'Saturn-Type Main Rings', 
    'Prominent, icy main ring system with multiple bands and divisions',
    'MAIN',
    'Gas Giant, Ice Giant',
    15.0, NULL, -- Gas giants only
    2.0, 15.0, -- Mid to outer system
    1.2, 1.5, -- Inner edge
    2.0, 2.8, -- Outer edge
    10, 100, -- Thickness
    0.4, 1.2, -- Optical depth (opaque)
    0.01, 10.0, -- Particle sizes (cm to meters)
    'ICY',
    'Water Ice 93%, Silicates 5%, Organics 2%',
    0.5, 0.8, -- High albedo
    'White to pale yellow',
    'PROMINENT',
    0.7, -- 70% chance of gaps
    0.8, -- 80% chance of shepherd moons
    'STABLE',
    'PRIMORDIAL',
    100 -- Common
);

-- Insert Uranus-like narrow rings
INSERT INTO ref.ring_template (
    name, description, ring_type, planet_types,
    min_planet_mass_earth, max_planet_mass_earth,
    min_distance_au, max_distance_au,
    inner_radius_min_planet_radii, inner_radius_max_planet_radii,
    outer_radius_min_planet_radii, outer_radius_max_planet_radii,
    thickness_min_km, thickness_max_km,
    optical_depth_min, optical_depth_max,
    particle_size_min_m, particle_size_max_m,
    composition_type, composition_description,
    albedo_min, albedo_max, color, visibility,
    has_gaps_probability, has_shepherd_moons_probability,
    stability, origin_type, rarity_weight
) VALUES (
    'Narrow Dark Rings',
    'Thin, dark rings composed of rocky material',
    'NARROW',
    'Ice Giant, Gas Giant',
    10.0, NULL,
    5.0, 30.0, -- Outer system
    1.5, 2.0,
    2.0, 3.0,
    0.1, 5.0, -- Very thin
    0.1, 0.5, -- Moderate opacity
    0.001, 1.0, -- Smaller particles
    'ROCKY',
    'Carbonaceous Material 60%, Silicates 30%, Water Ice 10%',
    0.02, 0.08, -- Very dark
    'Charcoal gray to black',
    'FAINT',
    0.3, -- Few gaps
    0.9, -- Usually has shepherd moons
    'STABLE',
    'MOON_DISRUPTION',
    60 -- Less common
);

-- Insert Jupiter-like gossamer rings
INSERT INTO ref.ring_template (
    name, description, ring_type, planet_types,
    min_planet_mass_earth, max_planet_mass_earth,
    min_distance_au, max_distance_au,
    inner_radius_min_planet_radii, inner_radius_max_planet_radii,
    outer_radius_min_planet_radii, outer_radius_max_planet_radii,
    thickness_min_km, thickness_max_km,
    optical_depth_min, optical_depth_max,
    particle_size_min_m, particle_size_max_m,
    composition_type, composition_description,
    albedo_min, albedo_max, color, visibility,
    has_gaps_probability, has_shepherd_moons_probability,
    stability, origin_type, rarity_weight
) VALUES (
    'Gossamer Rings',
    'Faint, diffuse rings fed by dust from inner moons',
    'GOSSAMER',
    'Gas Giant',
    30.0, NULL, -- Large gas giants
    1.0, 10.0,
    1.1, 1.3,
    2.5, 4.0,
    1000, 12000, -- Very thick but diffuse
    0.00001, 0.0001, -- Nearly transparent
    0.000001, 0.001, -- Microscopic dust
    'MIXED',
    'Dust 70%, Water Ice 20%, Silicates 10%',
    0.05, 0.15, -- Dark
    'Reddish-brown',
    'BARELY_VISIBLE',
    0.1, -- Usually continuous
    0.3, -- Sometimes shepherd moons
    'SLOWLY_DISSIPATING',
    'MOON_DEBRIS',
    40 -- Uncommon
);

-- Insert young system debris rings
INSERT INTO ref.ring_template (
    name, description, ring_type, planet_types,
    min_planet_mass_earth, max_planet_mass_earth,
    min_distance_au, max_distance_au,
    inner_radius_min_planet_radii, inner_radius_max_planet_radii,
    outer_radius_min_planet_radii, outer_radius_max_planet_radii,
    thickness_min_km, thickness_max_km,
    optical_depth_min, optical_depth_max,
    particle_size_min_m, particle_size_max_m,
    composition_type, composition_description,
    albedo_min, albedo_max, color, visibility,
    has_gaps_probability, has_shepherd_moons_probability,
    stability, origin_type, rarity_weight
) VALUES (
    'Young System Debris Ring',
    'Temporary ring from recent collision or disruption event',
    'DIFFUSE',
    'Gas Giant, Ice Giant, Super Earth',
    5.0, NULL,
    0.5, 20.0,
    1.3, 1.8,
    1.8, 2.5,
    20, 200,
    0.05, 0.3,
    0.001, 5.0,
    'MIXED',
    'Rock 40%, Ice 35%, Dust 25%',
    0.2, 0.4,
    'Gray to tan',
    'MODERATE',
    0.5,
    0.4,
    'UNSTABLE',
    'MOON_COLLISION',
    30 -- Rare
);

-- Create index on ring templates
CREATE INDEX idx_ring_template_planet_types ON ref.ring_template(planet_types);
CREATE INDEX idx_ring_template_ring_type ON ref.ring_template(ring_type);

COMMENT ON TABLE ud.ring IS 'Planetary ring systems with detailed physical and compositional properties';
COMMENT ON TABLE ref.ring_template IS 'Templates for procedurally generating scientifically accurate planetary rings';
