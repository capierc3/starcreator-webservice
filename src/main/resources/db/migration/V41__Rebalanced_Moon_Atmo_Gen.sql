-- =====================================================================
-- Flyway Migration V41: Rebalance Moon Atmosphere Generation
-- =====================================================================
-- Issues identified:
-- 1. Temperature ranges too narrow - hot volcanic moons (>600K) don't match any template
-- 2. Rocky Moon weights favor NONE too heavily (200 vs 10 for MARS_LIKE)
-- 3. Too many atmospheres being stripped by magnetosphere
-- =====================================================================

-- =====================================================================
-- SECTION 1: Extend Temperature Ranges for Volcanic Templates
-- =====================================================================

-- Volcanic Outgassing - extend max temp for Io-like extreme volcanism
UPDATE ref.atmosphere_template
SET max_temperature_k = 800
WHERE name = 'Volcanic Outgassing';

-- Volcanic Moon Atmosphere - extend max temp for hot volcanic moons
UPDATE ref.atmosphere_template
SET max_temperature_k = 1000
WHERE name = 'Volcanic Moon Atmosphere';

-- =====================================================================
-- SECTION 2: Add Extreme Volcanic Template for Very Hot Moons
-- =====================================================================

INSERT INTO ref.atmosphere_template
(name, description, classification, is_breathable, typical_pressure_bar,
 min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Extreme Volcanic', 'Intense volcanic atmosphere on tidally superheated moon', 'VOLCANIC', false, 0.01,
     500, 1500, 0.01, 0.5, 80);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'SO2', 50, 70, false FROM ref.atmosphere_template WHERE name = 'Extreme Volcanic';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'SO', 10, 20, false FROM ref.atmosphere_template WHERE name = 'Extreme Volcanic';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'O2', 5, 15, false FROM ref.atmosphere_template WHERE name = 'Extreme Volcanic';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'Na', 1, 5, true FROM ref.atmosphere_template WHERE name = 'Extreme Volcanic';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'K', 0.5, 3, true FROM ref.atmosphere_template WHERE name = 'Extreme Volcanic';

-- =====================================================================
-- SECTION 3: Rebalance Rocky Moon Compatibility Weights
-- =====================================================================

-- Rocky Moon - increase MARS_LIKE chance significantly
UPDATE ref.planet_atmosphere_compatibility
SET preference_weight = 80
WHERE planet_type = 'Rocky Moon' AND atmosphere_classification = 'MARS_LIKE';

UPDATE ref.planet_atmosphere_compatibility
SET preference_weight = 120
WHERE planet_type = 'Rocky Moon' AND atmosphere_classification = 'NONE';

-- Rocky Moon Large - increase atmosphere chances
UPDATE ref.planet_atmosphere_compatibility
SET preference_weight = 150
WHERE planet_type = 'Rocky Moon Large' AND atmosphere_classification = 'MARS_LIKE';

UPDATE ref.planet_atmosphere_compatibility
SET preference_weight = 120
WHERE planet_type = 'Rocky Moon Large' AND atmosphere_classification = 'VOLCANIC';

UPDATE ref.planet_atmosphere_compatibility
SET preference_weight = 100
WHERE planet_type = 'Rocky Moon Large' AND atmosphere_classification = 'NONE';

-- =====================================================================
-- SECTION 4: Add VOLCANIC Compatibility for Rocky Moon (small but active)
-- =====================================================================

INSERT INTO ref.planet_atmosphere_compatibility (planet_type, atmosphere_classification, preference_weight, notes)
VALUES
    ('Rocky Moon', 'VOLCANIC', 60, 'Small but geologically active rocky moon');

-- =====================================================================
-- SECTION 5: Improve Volcanic Moon Weights
-- =====================================================================

-- Volcanic Moon should almost always have atmosphere if active
UPDATE ref.planet_atmosphere_compatibility
SET preference_weight = 250
WHERE planet_type = 'Volcanic Moon' AND atmosphere_classification = 'VOLCANIC';

UPDATE ref.planet_atmosphere_compatibility
SET preference_weight = 30
WHERE planet_type = 'Volcanic Moon' AND atmosphere_classification = 'NONE';