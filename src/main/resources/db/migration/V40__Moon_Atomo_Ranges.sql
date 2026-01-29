-- =====================================================================
-- Flyway Migration V40: Fix Moon Atmosphere Template Ranges
-- =====================================================================
-- Issue: Moon templates have mass/temperature ranges that are too restrictive
-- - Large moons (>0.1 Earth mass) weren't matching any templates
-- - Warmer moons (>150K) from tidal heating weren't matching
-- =====================================================================

-- =====================================================================
-- SECTION 1: Update Existing Moon Template Ranges
-- =====================================================================

-- Thin Nitrogen - extend temp range for tidally heated moons
UPDATE ref.atmosphere_template
SET max_temperature_k = 200,
    max_planet_mass_earth = 0.05
WHERE name = 'Thin Nitrogen';

-- Thick Nitrogen - extend ranges significantly
UPDATE ref.atmosphere_template
SET max_temperature_k = 300,
    max_planet_mass_earth = 0.3
WHERE name = 'Thick Nitrogen';

-- Cryovolcanic Trace - extend for warmer cryovolcanic moons
UPDATE ref.atmosphere_template
SET max_temperature_k = 250,
    max_planet_mass_earth = 0.1
WHERE name = 'Cryovolcanic Trace';

-- Volcanic Outgassing - extend mass range
UPDATE ref.atmosphere_template
SET max_planet_mass_earth = 0.3
WHERE name = 'Volcanic Outgassing';

-- =====================================================================
-- SECTION 2: Add New Template for Large Warm Icy Moons
-- =====================================================================

INSERT INTO ref.atmosphere_template
(name, description, classification, is_breathable, typical_pressure_bar,
 min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Warm Nitrogen', 'Dense nitrogen atmosphere on tidally heated icy moon', 'TITAN_LIKE', false, 2.0,
     150, 350, 0.05, 0.5, 70);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'N2', 85, 95, false FROM ref.atmosphere_template WHERE name = 'Warm Nitrogen';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'CH4', 2, 8, false FROM ref.atmosphere_template WHERE name = 'Warm Nitrogen';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'CO2', 0.5, 3, false FROM ref.atmosphere_template WHERE name = 'Warm Nitrogen';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'H2O', 0.1, 2, true FROM ref.atmosphere_template WHERE name = 'Warm Nitrogen';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'Ar', 0.01, 0.5, true FROM ref.atmosphere_template WHERE name = 'Warm Nitrogen';

-- =====================================================================
-- SECTION 3: Update Existing Compatibility Weights
-- =====================================================================

UPDATE ref.planet_atmosphere_compatibility
SET preference_weight = 220
WHERE planet_type = 'Cryovolcanic Moon' AND atmosphere_classification = 'TITAN_LIKE';

UPDATE ref.planet_atmosphere_compatibility
SET preference_weight = 180
WHERE planet_type = 'Icy Moon Large' AND atmosphere_classification = 'TITAN_LIKE';

-- =====================================================================
-- SECTION 4: Add Template for Rocky Moon with Silicate Volcanism
-- =====================================================================

INSERT INTO ref.atmosphere_template
(name, description, classification, is_breathable, typical_pressure_bar,
 min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Volcanic Moon Atmosphere', 'CO2/SO2 atmosphere from silicate volcanism', 'VOLCANIC', false, 0.001,
     200, 600, 0.01, 0.3, 60);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'CO2', 60, 80, false FROM ref.atmosphere_template WHERE name = 'Volcanic Moon Atmosphere';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'SO2', 10, 25, false FROM ref.atmosphere_template WHERE name = 'Volcanic Moon Atmosphere';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'H2S', 1, 5, true FROM ref.atmosphere_template WHERE name = 'Volcanic Moon Atmosphere';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'SO', 0.5, 3, true FROM ref.atmosphere_template WHERE name = 'Volcanic Moon Atmosphere';