-- =====================================================================
-- Flyway Migration V73: Fix Super-Earth Atmosphere Diversity
-- =====================================================================
-- Problem: Super-Earths are 96% AMMONIA atmosphere.
-- All Super-Earths generate at 150-273K (sub-freezing), mass 2-10 M⊕.
--
-- At those ranges, the only matching atmosphere template is:
--   Ammonia (100-300K, 0.5-10 M⊕, weight 15)
--
-- Other templates fail to match:
--   Earth-like: min temp 240K — misses the 150-240K majority
--   Venus-like: min temp 320K — way too hot
--   Mars-like: max mass 2.0 M⊕ — Super-Earths start at 2 M⊕
--   Titan-like: max mass 5.0 M⊕ — misses 5-10 M⊕ Super-Earths
--
-- Fix: Widen existing template ranges and add a Dense CO2 template
-- for the 150-320K gap at Super-Earth masses.
-- =====================================================================

-- 1. Earth-like: lower min temp from 240K to 180K
-- A massive Super-Earth at 200K with strong magnetic field can retain
-- an Earth-like N2/O2 atmosphere. Still cold, but physically viable.
UPDATE ref.atmosphere_template
SET min_temperature_k = 180
WHERE name = 'Earth-like';

-- 2. Mars-like: extend max mass from 2.0 to 5.0 M⊕
-- Larger rocky planets can have thin CO2/N2 atmospheres, especially
-- if they've lost volatiles to stellar wind. Not all Super-Earths
-- retain thick envelopes.
UPDATE ref.atmosphere_template
SET max_planet_mass_earth = 5.0
WHERE name = 'Mars-like';

-- 3. Titan-like: extend max mass from 5.0 to 12.0 M⊕
-- Massive cold worlds can have thick N2/CH4 atmospheres.
UPDATE ref.atmosphere_template
SET max_planet_mass_earth = 12.0
WHERE name = 'Titan-like';

-- 4. Add Dense CO2 template for the 150-320K, 1-15 M⊕ gap
-- Thick CO2 atmosphere — colder than Venus but more substantial than
-- Mars. Common expected outcome for Super-Earths without biology to
-- convert CO2. Think "cold Venus" or "super-Mars with thick air."
INSERT INTO ref.atmosphere_template
    (name, description, classification, is_breathable, typical_pressure_bar,
     min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth,
     rarity_weight)
VALUES
    ('Dense CO2', 'Thick carbon dioxide atmosphere on cold massive world',
     'VENUS_LIKE', false, 5.0,
     150, 350, 1.0, 15.0,
     50);

-- Add gas components for Dense CO2
INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'CO2', 85.0, 96.0, false FROM ref.atmosphere_template WHERE name = 'Dense CO2'
UNION ALL
SELECT id, 'N2', 2.0, 10.0, false FROM ref.atmosphere_template WHERE name = 'Dense CO2'
UNION ALL
SELECT id, 'SO2', 0.01, 0.5, true FROM ref.atmosphere_template WHERE name = 'Dense CO2'
UNION ALL
SELECT id, 'H2O', 0.01, 1.0, true FROM ref.atmosphere_template WHERE name = 'Dense CO2';

-- 5. Add compatibility entry for Super-Earth + VENUS_LIKE if missing
-- (already exists at weight 120, so this is just a safety check)
