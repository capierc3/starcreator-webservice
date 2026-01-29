-- =====================================================================
-- Flyway Migration V39: Moon Atmosphere System
-- =====================================================================
-- Description: Adds moon-specific atmosphere types, templates, and
--              compatibility entries for realistic moon atmosphere generation
-- =====================================================================

-- =====================================================================
-- SECTION 1: Add Sulfur Monoxide to Support Io-like Atmospheres
-- =====================================================================
-- Note: This is handled in AtmosphereGas.java enum, but documenting here
-- SO (Sulfur Monoxide) - produced when SO2 breaks down from UV radiation

-- =====================================================================
-- SECTION 2: Add Moon-Specific Atmosphere Templates
-- These have lower mass ranges appropriate for moons (0.0001 - 0.1 Earth masses)
-- =====================================================================

-- Thin Nitrogen (for smaller icy bodies - Triton, Pluto-like)
-- Triton has ~14 microbar N2 atmosphere
INSERT INTO ref.atmosphere_template
(name, description, classification, is_breathable, typical_pressure_bar,
 min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Thin Nitrogen', 'Tenuous nitrogen atmosphere like Triton or Pluto', 'TITAN_LIKE', false, 0.000014,
     30, 120, 0.0001, 0.03, 60);

-- Thick Nitrogen (Titan-like, for larger moons)
-- Titan has 1.5 bar N2/CH4 atmosphere at 0.022 Earth masses
INSERT INTO ref.atmosphere_template
(name, description, classification, is_breathable, typical_pressure_bar,
 min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Thick Nitrogen', 'Dense nitrogen-methane atmosphere like Titan', 'TITAN_LIKE', false, 1.5,
     70, 150, 0.01, 0.1, 80);

-- Volcanic Outgassing (for Io-like moons - transient SO2)
-- Io has ~1 nanobar SO2 atmosphere from volcanic activity
INSERT INTO ref.atmosphere_template
(name, description, classification, is_breathable, typical_pressure_bar,
 min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Volcanic Outgassing', 'Transient SO2 atmosphere from active volcanism like Io', 'VOLCANIC', false, 0.000000001,
     80, 500, 0.001, 0.05, 70);

-- Cryovolcanic Trace (for active icy moons - Enceladus-like)
-- Extremely thin from geyser activity
INSERT INTO ref.atmosphere_template
(name, description, classification, is_breathable, typical_pressure_bar,
 min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Cryovolcanic Trace', 'Trace atmosphere from cryovolcanic outgassing like Enceladus', 'TITAN_LIKE', false, 0.000000001,
     50, 150, 0.0001, 0.02, 50);

-- =====================================================================
-- SECTION 3: Add Components for New Templates
-- =====================================================================

-- Thin Nitrogen components (Triton/Pluto-like)
INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'N2', 90, 99, false FROM ref.atmosphere_template WHERE name = 'Thin Nitrogen';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'CH4', 0.5, 5, false FROM ref.atmosphere_template WHERE name = 'Thin Nitrogen';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'CO', 0.01, 1, true FROM ref.atmosphere_template WHERE name = 'Thin Nitrogen';

-- Thick Nitrogen components (Titan-like)
INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'N2', 94, 98, false FROM ref.atmosphere_template WHERE name = 'Thick Nitrogen';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'CH4', 1.5, 5, false FROM ref.atmosphere_template WHERE name = 'Thick Nitrogen';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'H2', 0.1, 0.5, true FROM ref.atmosphere_template WHERE name = 'Thick Nitrogen';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'Ar', 0.01, 0.1, true FROM ref.atmosphere_template WHERE name = 'Thick Nitrogen';

-- Volcanic Outgassing components (Io-like)
INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'SO2', 85, 95, false FROM ref.atmosphere_template WHERE name = 'Volcanic Outgassing';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'SO', 2, 10, false FROM ref.atmosphere_template WHERE name = 'Volcanic Outgassing';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'O2', 0.1, 3, true FROM ref.atmosphere_template WHERE name = 'Volcanic Outgassing';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'Na', 0.01, 1, true FROM ref.atmosphere_template WHERE name = 'Volcanic Outgassing';

-- Cryovolcanic Trace components (Enceladus-like)
INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'H2O', 85, 95, false FROM ref.atmosphere_template WHERE name = 'Cryovolcanic Trace';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'N2', 2, 8, false FROM ref.atmosphere_template WHERE name = 'Cryovolcanic Trace';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'CO2', 1, 5, true FROM ref.atmosphere_template WHERE name = 'Cryovolcanic Trace';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'CH4', 0.1, 2, true FROM ref.atmosphere_template WHERE name = 'Cryovolcanic Trace';

-- =====================================================================
-- SECTION 4: Add Moon Types to Atmosphere Compatibility
-- These are distinct from planet types to avoid overlap
-- =====================================================================

-- Icy Moon (small icy moons - Europa, most icy satellites)
INSERT INTO ref.planet_atmosphere_compatibility (planet_type, atmosphere_classification, preference_weight, notes)
VALUES
    ('Icy Moon', 'NONE', 200, 'Most small icy moons cannot retain atmosphere'),
    ('Icy Moon', 'TITAN_LIKE', 20, 'Rare trace nitrogen possible');

-- Icy Moon Large (Ganymede, Callisto sized)
INSERT INTO ref.planet_atmosphere_compatibility (planet_type, atmosphere_classification, preference_weight, notes)
VALUES
    ('Icy Moon Large', 'TITAN_LIKE', 150, 'Can retain nitrogen atmosphere like Titan'),
    ('Icy Moon Large', 'AMMONIA', 80, 'Ammonia possible in cold conditions'),
    ('Icy Moon Large', 'NONE', 100, 'May have lost atmosphere');

-- Cryovolcanic Moon (Titan, Enceladus, Triton-like with active ice volcanism)
INSERT INTO ref.planet_atmosphere_compatibility (planet_type, atmosphere_classification, preference_weight, notes)
VALUES
    ('Cryovolcanic Moon', 'TITAN_LIKE', 200, 'Thick N2/CH4 from cryovolcanic activity'),
    ('Cryovolcanic Moon', 'AMMONIA', 100, 'Ammonia from subsurface ocean'),
    ('Cryovolcanic Moon', 'NONE', 50, 'Atmosphere too thin to detect');

-- Volcanic Moon (Io-like with active silicate volcanism)
INSERT INTO ref.planet_atmosphere_compatibility (planet_type, atmosphere_classification, preference_weight, notes)
VALUES
    ('Volcanic Moon', 'VOLCANIC', 200, 'SO2 from active volcanism'),
    ('Volcanic Moon', 'NONE', 80, 'Atmosphere collapses between eruptions');

-- Rocky Moon (Earth Moon, dead rocky satellites)
INSERT INTO ref.planet_atmosphere_compatibility (planet_type, atmosphere_classification, preference_weight, notes)
VALUES
    ('Rocky Moon', 'NONE', 200, 'Too small and geologically dead'),
    ('Rocky Moon', 'MARS_LIKE', 10, 'Extremely rare trace CO2');

-- Rocky Moon Large (large rocky/mixed moons)
INSERT INTO ref.planet_atmosphere_compatibility (planet_type, atmosphere_classification, preference_weight, notes)
VALUES
    ('Rocky Moon Large', 'MARS_LIKE', 120, 'Can retain thin CO2 atmosphere'),
    ('Rocky Moon Large', 'VOLCANIC', 80, 'If geologically active'),
    ('Rocky Moon Large', 'NONE', 150, 'Most have lost atmosphere');

-- Captured Body (irregular moons, captured asteroids)
INSERT INTO ref.planet_atmosphere_compatibility (planet_type, atmosphere_classification, preference_weight, notes)
VALUES
    ('Captured Body', 'NONE', 250, 'Far too small for any atmosphere');