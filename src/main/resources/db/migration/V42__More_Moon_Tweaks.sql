-- =====================================================================
-- Flyway Migration V42: Expand Moon Atmosphere Templates
-- =====================================================================
-- Issues identified:
-- 1. Hot cryovolcanic moons (>300K) have no matching templates
-- 2. Medium-mass moons (0.01-0.05) at warm temps fall through gaps
-- 3. NONE template weight still too high, competing with real atmospheres
-- =====================================================================

-- =====================================================================
-- SECTION 1: Reduce NONE Template Weight
-- =====================================================================

UPDATE ref.atmosphere_template
SET rarity_weight = 5
WHERE classification = 'NONE';

-- =====================================================================
-- SECTION 2: Extend Existing Template Ranges
-- =====================================================================

-- Warm Nitrogen - reduce min mass to cover medium moons
UPDATE ref.atmosphere_template
SET min_planet_mass_earth = 0.01
WHERE name = 'Warm Nitrogen';

-- Thick Nitrogen - extend max temp for warmer moons
UPDATE ref.atmosphere_template
SET max_temperature_k = 350
WHERE name = 'Thick Nitrogen';

-- =====================================================================
-- SECTION 3: Add New Templates for Gap Coverage
-- =====================================================================

-- Hot Cryovolcanic: For tidally superheated icy moons (>300K)
-- These moons have intense tidal heating driving off volatiles
INSERT INTO ref.atmosphere_template
    (name, description, classification, is_breathable, typical_pressure_bar,
     min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Hot Cryovolcanic', 'Volatile-rich atmosphere on tidally superheated icy moon', 'TITAN_LIKE', false, 0.1,
     250, 500, 0.005, 0.5, 70);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'H2O', 40, 70, false FROM ref.atmosphere_template WHERE name = 'Hot Cryovolcanic';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'CO2', 15, 35, false FROM ref.atmosphere_template WHERE name = 'Hot Cryovolcanic';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'N2', 5, 20, false FROM ref.atmosphere_template WHERE name = 'Hot Cryovolcanic';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'SO2', 1, 10, true FROM ref.atmosphere_template WHERE name = 'Hot Cryovolcanic';


-- Small Icy Outgassing: For tiny icy moons with trace atmospheres
-- Covers moons smaller than current templates allow
INSERT INTO ref.atmosphere_template
    (name, description, classification, is_breathable, typical_pressure_bar,
     min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Small Icy Outgassing', 'Trace atmosphere from small active icy moon', 'TITAN_LIKE', false, 0.00000001,
     50, 200, 0.00001, 0.001, 50);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'H2O', 50, 80, false FROM ref.atmosphere_template WHERE name = 'Small Icy Outgassing';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'CO2', 10, 30, false FROM ref.atmosphere_template WHERE name = 'Small Icy Outgassing';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'N2', 5, 20, true FROM ref.atmosphere_template WHERE name = 'Small Icy Outgassing';


-- Ammonia-Rich: For outer system icy moons with ammonia
-- Referenced in compatibility but no dedicated template existed
INSERT INTO ref.atmosphere_template
    (name, description, classification, is_breathable, typical_pressure_bar,
     min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES
    ('Ammonia-Rich', 'Ammonia-dominated atmosphere on cold icy moon', 'AMMONIA', false, 0.001,
     50, 150, 0.001, 0.2, 60);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'NH3', 40, 70, false FROM ref.atmosphere_template WHERE name = 'Ammonia-Rich';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'N2', 15, 35, false FROM ref.atmosphere_template WHERE name = 'Ammonia-Rich';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'CH4', 5, 20, false FROM ref.atmosphere_template WHERE name = 'Ammonia-Rich';

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'H2O', 1, 10, true FROM ref.atmosphere_template WHERE name = 'Ammonia-Rich';

-- =====================================================================
-- SECTION 5: Reduce NONE Compatibility Weights
-- =====================================================================

-- Reduce NONE preference weight across all moon types
UPDATE ref.planet_atmosphere_compatibility
SET preference_weight = 20
WHERE atmosphere_classification = 'NONE'
  AND planet_type IN ('Cryovolcanic Moon', 'Icy Moon', 'Icy Moon Large', 'Volcanic Moon');

-- Keep NONE higher for truly dead moon types
UPDATE ref.planet_atmosphere_compatibility
SET preference_weight = 150
WHERE atmosphere_classification = 'NONE'
  AND planet_type IN ('Rocky Moon', 'Captured Body');

-- =====================================================================
-- SECTION 6: Summary of Template Coverage After Migration
-- =====================================================================
--
-- TITAN_LIKE templates now cover:
--   Thin Nitrogen:        30-200K,  0.0001-0.05 mass (Triton/Pluto-like)
--   Thick Nitrogen:       70-350K,  0.01-0.3 mass   (Titan-like) [EXTENDED]
--   Cryovolcanic Trace:   50-250K,  0.0001-0.1 mass (Enceladus-like)
--   Warm Nitrogen:       150-350K,  0.01-0.5 mass   (tidally heated) [EXTENDED]
--   Hot Cryovolcanic:    250-500K,  0.005-0.5 mass  (superheated) [NEW]
--   Small Icy Outgassing: 50-200K,  0.00001-0.001   (tiny moons) [NEW]
--
-- AMMONIA templates now cover:
--   Ammonia-Rich:         50-150K,  0.001-0.2 mass  [NEW]
--
-- VOLCANIC templates (unchanged):
--   Volcanic Outgassing:  80-800K,  0.001-0.3 mass
--   Volcanic Moon Atmo:  200-1000K, 0.01-0.3 mass
--   Extreme Volcanic:    500-1500K, 0.01-0.5 mass
-- =====================================================================