-- V75: Fix Hot Neptune atmosphere template coverage
-- Problem: planet_atmosphere_compatibility gives Hot Neptune weights for
-- REDUCING (180), EXOTIC (150), VOLCANIC (100), NONE (60), but no
-- atmosphere_template entries exist in the right temp/mass range for
-- REDUCING or EXOTIC. Only VOLCANIC (300-1500K, 0.05-10 M⊕) partially
-- matches, and the mass cap of 10 excludes larger Hot Neptunes (5-25 M⊕).
-- Result: findMatchingTemplates() falls through and Hot Neptunes that
-- survive stripping still can't get the atmospheres they're weighted for.

-- 1. REDUCING template for Hot Neptunes (residual H2 envelope)
INSERT INTO ref.atmosphere_template
(name, description, classification, is_breathable, typical_pressure_bar,
 min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Hot Neptune Reducing', 'Residual hydrogen envelope on irradiated Neptune',
     'REDUCING', false, 0.5, 300, 1200, 2, 30, 40);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'H2', 60, 85, false FROM ref.atmosphere_template WHERE name = 'Hot Neptune Reducing'
UNION ALL
SELECT id, 'He', 10, 25, false FROM ref.atmosphere_template WHERE name = 'Hot Neptune Reducing'
UNION ALL
SELECT id, 'H2O', 1, 10, false FROM ref.atmosphere_template WHERE name = 'Hot Neptune Reducing'
UNION ALL
SELECT id, 'CO2', 0.1, 5, true FROM ref.atmosphere_template WHERE name = 'Hot Neptune Reducing';

-- 2. EXOTIC template for Hot Neptunes (metallic vapors at high irradiation)
INSERT INTO ref.atmosphere_template
(name, description, classification, is_breathable, typical_pressure_bar,
 min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Hot Neptune Exotic', 'Metallic vapor atmosphere on heavily irradiated Neptune',
     'EXOTIC', false, 0.1, 700, 1500, 2, 30, 20);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'Na', 20, 40, false FROM ref.atmosphere_template WHERE name = 'Hot Neptune Exotic'
UNION ALL
SELECT id, 'K', 5, 15, false FROM ref.atmosphere_template WHERE name = 'Hot Neptune Exotic'
UNION ALL
SELECT id, 'H2', 20, 40, false FROM ref.atmosphere_template WHERE name = 'Hot Neptune Exotic'
UNION ALL
SELECT id, 'He', 10, 20, false FROM ref.atmosphere_template WHERE name = 'Hot Neptune Exotic';

-- 3. Widen Volcanic template mass range to cover Hot Neptunes (5-25 M⊕)
UPDATE ref.atmosphere_template
SET max_planet_mass_earth = 30
WHERE name = 'Volcanic' AND classification = 'VOLCANIC';