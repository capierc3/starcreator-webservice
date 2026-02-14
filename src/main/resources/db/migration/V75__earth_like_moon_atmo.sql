-- V__moon_earth_like_reducing_templates.sql
-- Add EARTH_LIKE and REDUCING atmosphere templates scaled for moons,
-- plus compatibility entries to enable diverse moon sky colors.
--
-- Problem: Moon sky colors are limited to ORANGE, YELLOW_GREY, PALE_YELLOW
-- because no EARTH_LIKE or REDUCING templates exist at moon mass ranges.
-- Planet templates require min 0.5 M⊕; largest moons are ~0.025 M⊕.

-- =====================================================================
-- SECTION 1: Moon-Scale EARTH_LIKE Templates
-- =====================================================================

-- Large moon with evolved N2/O2 atmosphere (hypothetical Titan-mass in HZ)
-- Requires substantial mass, geological activity to sustain O2 cycle
-- Think: a Ganymede-mass moon orbiting a gas giant in the habitable zone
INSERT INTO ref.atmosphere_template
(name, description, classification, is_breathable, typical_pressure_bar,
 min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Moon Earth-like', 'Thin N2/O2 atmosphere on large geologically active moon in HZ',
     'EARTH_LIKE', true, 0.3,
     180, 320, 0.008, 0.5, 15);

-- Components: thinner than Earth but breathable
INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'N2', 70.0, 85.0, false FROM ref.atmosphere_template WHERE name = 'Moon Earth-like'
UNION ALL
SELECT id, 'O2', 12.0, 22.0, false FROM ref.atmosphere_template WHERE name = 'Moon Earth-like'
UNION ALL
SELECT id, 'Ar', 0.5, 2.0, false FROM ref.atmosphere_template WHERE name = 'Moon Earth-like'
UNION ALL
SELECT id, 'CO2', 0.05, 0.5, true FROM ref.atmosphere_template WHERE name = 'Moon Earth-like'
UNION ALL
SELECT id, 'H2O', 0.1, 3.0, true FROM ref.atmosphere_template WHERE name = 'Moon Earth-like';

-- =====================================================================
-- SECTION 2: Moon-Scale REDUCING Template
-- =====================================================================

-- Primordial H2-rich atmosphere retained by large icy moon
-- Subsurface ocean outgassing H2 via serpentinization
INSERT INTO ref.atmosphere_template
(name, description, classification, is_breathable, typical_pressure_bar,
 min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Moon Reducing', 'Primordial H2-rich atmosphere on large icy moon with hydrothermal activity',
     'REDUCING', false, 0.05,
     80, 350, 0.005, 0.5, 25);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'H2', 40.0, 65.0, false FROM ref.atmosphere_template WHERE name = 'Moon Reducing'
UNION ALL
SELECT id, 'N2', 15.0, 30.0, false FROM ref.atmosphere_template WHERE name = 'Moon Reducing'
UNION ALL
SELECT id, 'CH4', 5.0, 20.0, false FROM ref.atmosphere_template WHERE name = 'Moon Reducing'
UNION ALL
SELECT id, 'CO2', 1.0, 5.0, true FROM ref.atmosphere_template WHERE name = 'Moon Reducing'
UNION ALL
SELECT id, 'H2O', 0.1, 3.0, true FROM ref.atmosphere_template WHERE name = 'Moon Reducing';

INSERT INTO ref.planet_atmosphere_compatibility
(planet_type, atmosphere_classification, preference_weight, notes)
VALUES
    ('Rocky Moon Large', 'EARTH_LIKE', 15,
     'Very rare - massive rocky moon with sustained geological outgassing in HZ');

INSERT INTO ref.planet_atmosphere_compatibility
(planet_type, atmosphere_classification, preference_weight, notes)
VALUES
    ('Icy Moon Large', 'EARTH_LIKE', 8,
     'Extremely rare - requires HZ location, large mass, and atmospheric evolution');

INSERT INTO ref.planet_atmosphere_compatibility
(planet_type, atmosphere_classification, preference_weight, notes)
VALUES
    ('Icy Moon Large', 'REDUCING', 25,
     'Primordial H2-rich atmosphere retained by large icy moon');

INSERT INTO ref.planet_atmosphere_compatibility
(planet_type, atmosphere_classification, preference_weight, notes)
VALUES
    ('Cryovolcanic Moon', 'REDUCING', 30,
     'H2-rich outgassing from subsurface ocean hydrothermal activity');