-- Create new reference table for valid atmosphere types per planet type
CREATE TABLE IF NOT EXISTS ref.planet_atmosphere_compatibility (
                                                                   id SERIAL PRIMARY KEY,
                                                                   planet_type VARCHAR(100) NOT NULL,
                                                                   atmosphere_classification VARCHAR(50) NOT NULL,
                                                                   preference_weight INTEGER DEFAULT 100,
                                                                   notes TEXT,
                                                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                                   UNIQUE(planet_type, atmosphere_classification)
);

COMMENT ON TABLE ref.planet_atmosphere_compatibility IS
    'Defines which atmosphere classifications are scientifically appropriate for each planet type';

COMMENT ON COLUMN ref.planet_atmosphere_compatibility.preference_weight IS
    'Higher weight = more likely to be selected (100 = normal, 200 = preferred, 50 = rare but possible)';

-- Insert compatibility mappings
INSERT INTO ref.planet_atmosphere_compatibility (planet_type, atmosphere_classification, preference_weight, notes)
VALUES
    -- Hot Rocky Planet
    ('Hot Rocky Planet', 'VOLCANIC', 150, 'Primary atmosphere type for hot rocky worlds'),
    ('Hot Rocky Planet', 'EXOTIC', 100, 'For extremely hot variants'),
    ('Hot Rocky Planet', 'NONE', 80, 'Atmosphere blown away by stellar wind'),

    -- Terrestrial Planet
    ('Terrestrial Planet', 'EARTH_LIKE', 200, 'Ideal habitable atmosphere'),
    ('Terrestrial Planet', 'VENUS_LIKE', 100, 'Runaway greenhouse variant'),
    ('Terrestrial Planet', 'MARS_LIKE', 120, 'Thin atmosphere variant'),
    ('Terrestrial Planet', 'VOLCANIC', 80, 'Geologically active variant'),
    ('Terrestrial Planet', 'NONE', 30, 'Rare airless terrestrial'),

    -- Desert Planet
    ('Desert Planet', 'MARS_LIKE', 200, 'Primary atmosphere - thin CO2'),
    ('Desert Planet', 'VENUS_LIKE', 100, 'Thick CO2 hot desert'),
    ('Desert Planet', 'VOLCANIC', 80, 'Active desert world'),
    ('Desert Planet', 'NONE', 50, 'Airless desert'),

    -- Ocean Planet
    ('Ocean Planet', 'EARTH_LIKE', 200, 'Water world with breathable air'),
    ('Ocean Planet', 'VENUS_LIKE', 80, 'Hot water world with thick CO2'),
    ('Ocean Planet', 'TITAN_LIKE', 100, 'Cold ocean with thick N2'),

    -- Super-Earth
    ('Super-Earth', 'EARTH_LIKE', 150, 'Thick breathable atmosphere'),
    ('Super-Earth', 'VENUS_LIKE', 120, 'Super-Venus variant'),
    ('Super-Earth', 'VOLCANIC', 100, 'Geologically hyperactive'),
    ('Super-Earth', 'AMMONIA', 80, 'Cold super-earth variant'),

    -- Lava Planet
    ('Lava Planet', 'VOLCANIC', 200, 'Primary atmosphere - vaporized rock'),
    ('Lava Planet', 'EXOTIC', 150, 'Metallic vapors from extreme heat'),
    ('Lava Planet', 'NONE', 20, 'Rare airless lava world'),

    -- Mini-Neptune
    ('Mini-Neptune', 'TITAN_LIKE', 150, 'Thick nitrogen-methane'),
    ('Mini-Neptune', 'AMMONIA', 120, 'Ammonia-rich variant'),
    ('Mini-Neptune', 'REDUCING', 100, 'Hydrogen-rich primitive'),
    ('Mini-Neptune', 'ICE_GIANT', 130, 'Ice giant composition'),

    -- Sub-Neptune
    ('Sub-Neptune', 'ICE_GIANT', 200, 'Primary type'),
    ('Sub-Neptune', 'TITAN_LIKE', 120, 'Nitrogen-methane variant'),
    ('Sub-Neptune', 'AMMONIA', 100, 'Ammonia-dominated'),

    -- Hot Jupiter
    ('Hot Jupiter', 'JOVIAN', 200, 'Standard hot jupiter atmosphere'),
    ('Hot Jupiter', 'EXOTIC', 80, 'Extremely hot variant with metallic vapors'),

    -- Gas Giant
    ('Gas Giant', 'JOVIAN', 200, 'Standard jovian atmosphere'),

    -- Super-Jupiter
    ('Super-Jupiter', 'JOVIAN', 200, 'Massive jovian atmosphere'),

    -- Ice Giant
    ('Ice Giant', 'ICE_GIANT', 200, 'Primary ice giant atmosphere'),
    ('Ice Giant', 'TITAN_LIKE', 80, 'Methane-nitrogen variant'),

    -- Puffy Planet
    ('Puffy Planet', 'JOVIAN', 180, 'Inflated gas giant'),
    ('Puffy Planet', 'REDUCING', 120, 'Hydrogen-rich low density'),

    -- Carbon Planet
    ('Carbon Planet', 'MARS_LIKE', 120, 'CO/CO2 atmosphere'),
    ('Carbon Planet', 'VOLCANIC', 100, 'Carbon-rich volcanic gases'),
    ('Carbon Planet', 'CORROSIVE', 80, 'Exotic carbon chemistry'),
    ('Carbon Planet', 'NONE', 60, 'Airless carbon world'),

    -- Iron Planet
    ('Iron Planet', 'VOLCANIC', 150, 'Metallic vapors from heat'),
    ('Iron Planet', 'EXOTIC', 120, 'Extreme metallic atmosphere'),
    ('Iron Planet', 'NONE', 100, 'Airless stripped core'),

    -- Ice World
    ('Ice World', 'TITAN_LIKE', 150, 'Nitrogen-methane atmosphere'),
    ('Ice World', 'AMMONIA', 100, 'Ammonia atmosphere'),
    ('Ice World', 'NONE', 120, 'No atmosphere retained'),

    -- Dwarf Planet
    ('Dwarf Planet', 'NONE', 200, 'Too small to retain atmosphere'),
    ('Dwarf Planet', 'TITAN_LIKE', 30, 'Trace atmosphere only'),

    -- Rogue Planet
    ('Rogue Planet', 'NONE', 180, 'Frozen, no atmosphere'),
    ('Rogue Planet', 'REDUCING', 50, 'Primordial frozen atmosphere');
