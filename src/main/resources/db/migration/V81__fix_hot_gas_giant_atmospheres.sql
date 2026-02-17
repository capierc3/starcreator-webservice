-- =====================================================================
-- Flyway Migration V80: Fix Atmosphere Template Coverage Gaps
-- =====================================================================
-- Fixes Issues #16, #21, #22 from bug tracker.
--
-- Coverage analysis found these gaps:
--
-- 🔴🔴 Hot Jupiter (1000-2500K, 50-500 M⊕): ZERO template coverage
-- 🔴🔴 Puffy Planet (800-1800K, 50-300 M⊕): ZERO template coverage
-- 🔴   Super-Jupiter (50-600K, 400-3000 M⊕): gap at 500-600K
-- 🔴   Warm Neptune (150-400K, 2-20 M⊕): gap at 200-400K for mass >10 M⊕
-- 🟡   Mini-Neptune (40-150K, 2-10 M⊕): minor gap at 40-50K
--
-- ROOT CAUSE: The JOVIAN template maxes at 500K, but Hot Jupiters
-- and Puffy Planets form at 800-2500K. Template matching falls
-- through to inappropriate alternatives.
-- =====================================================================

-- =====================================================================
-- SECTION 1: New "Hot Jovian" Template (fixes #16, #21, Super-Jupiter gap)
-- =====================================================================
-- Hot gas giants still have H2/He atmospheres, just with alkali metals
-- (Na, K) and at the hottest temps TiO/VO in the upper atmosphere.
-- Real examples: HD 209458b (1450K), WASP-121b (2400K), HAT-P-7b (2700K)
-- — all confirmed H2/He dominated with trace alkali absorption features.
--
-- Temp range 400-3000K:
--   - Overlaps Jovian template at 400-500K (smooth transition)
--   - Covers Super-Jupiter gap at 500-600K  
--   - Covers Hot Jupiter full range 1000-2500K
--   - Covers Puffy Planet full range 800-1800K
--   - Extends to 3000K for ultra-hot Jupiters
-- Mass range 20-10000 M⊕:
--   - Covers Hot Jupiter (50-500), Puffy Planet (50-300), Super-Jupiter (400-3000)
--   - Min 20 M⊕ prevents matching small rocky planets at high temps

INSERT INTO ref.atmosphere_template
    (name, description, classification, is_breathable, typical_pressure_bar,
     min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth,
     rarity_weight)
VALUES
    ('Hot Jovian', 'Hydrogen-helium atmosphere on hot gas giant with alkali metal vapors',
     'JOVIAN', false, 500.0,
     400, 3000, 20.0, 10000.0,
     120);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'H2', 75.0, 90.0, false FROM ref.atmosphere_template WHERE name = 'Hot Jovian'
UNION ALL
SELECT id, 'He', 8.0, 18.0, false FROM ref.atmosphere_template WHERE name = 'Hot Jovian'
UNION ALL
SELECT id, 'Na', 0.01, 0.5, true FROM ref.atmosphere_template WHERE name = 'Hot Jovian'
UNION ALL
SELECT id, 'K', 0.005, 0.2, true FROM ref.atmosphere_template WHERE name = 'Hot Jovian'
UNION ALL
SELECT id, 'H2O', 0.01, 1.0, true FROM ref.atmosphere_template WHERE name = 'Hot Jovian';

-- =====================================================================
-- SECTION 2: New "Warm Ice Giant" Template (fixes Warm Neptune gap)
-- =====================================================================
-- Warm Neptunes at 200-400K with mass >10 M⊕ have NO matching template.
-- ICE_GIANT maxes at 200K, AMMONIA maxes at 10 M⊕.
-- 
-- A "warm ice giant" atmosphere is a transitional state: the H2/He
-- envelope is still dominant but the icy volatiles (CH4, NH3, H2O)
-- are more heavily dissociated than at cold ice giant temperatures.
-- Methane photolysis creates C2H2/C2H6 hazes. NH3 depletes faster.
-- Think: a Neptune moved to ~1 AU — still H2/He/CH4 but warmer.
--
-- Temp range 150-450K:
--   - Overlaps Ice Giant template at 150-200K (smooth transition)
--   - Covers the full Warm Neptune range 150-400K
--   - Small margin to 450K for edge cases
-- Mass range 2-30 M⊕:
--   - Full Warm Neptune mass range (2-20 M⊕)
--   - Also covers Mini-Neptune and Sub-Neptune at warmer temps

INSERT INTO ref.atmosphere_template
    (name, description, classification, is_breathable, typical_pressure_bar,
     min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth,
     rarity_weight)
VALUES
    ('Warm Ice Giant', 'Transitional H2/He/CH4 atmosphere on moderately irradiated Neptune-class world',
     'ICE_GIANT', false, 200.0,
     150, 450, 2.0, 30.0,
     70);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace)
SELECT id, 'H2', 75.0, 85.0, false FROM ref.atmosphere_template WHERE name = 'Warm Ice Giant'
UNION ALL
SELECT id, 'He', 12.0, 18.0, false FROM ref.atmosphere_template WHERE name = 'Warm Ice Giant'
UNION ALL
SELECT id, 'CH4', 0.5, 3.0, false FROM ref.atmosphere_template WHERE name = 'Warm Ice Giant'
UNION ALL
SELECT id, 'H2O', 0.5, 3.0, false FROM ref.atmosphere_template WHERE name = 'Warm Ice Giant'
UNION ALL
SELECT id, 'NH3', 0.01, 0.5, true FROM ref.atmosphere_template WHERE name = 'Warm Ice Giant';

-- =====================================================================
-- SECTION 3: Extend AMMONIA template mass range
-- =====================================================================
-- AMMONIA template currently maxes at 10 M⊕, but Warm Neptunes go to
-- 20 M⊕. A 15 M⊕ Neptune-class world can absolutely have an ammonia-
-- rich atmosphere — NH3 is abundant in ice giant formation environments.

UPDATE ref.atmosphere_template
SET max_planet_mass_earth = 25.0
WHERE name = 'Ammonia' AND classification = 'AMMONIA';

-- =====================================================================
-- SECTION 4: Fix Mini-Neptune 40-50K gap
-- =====================================================================
-- Titan-like template starts at 50K, but Mini-Neptunes can form at 40K.
-- Lower min temp to 35K — dense N2/CH4 atmospheres can exist at very
-- cold temps (Titan is 94K, Triton is 38K with thin N2).

UPDATE ref.atmosphere_template
SET min_temperature_k = 35
WHERE name = 'Titan-like' AND classification = 'TITAN_LIKE';

-- =====================================================================
-- SECTION 5: Fix Puffy Planet Compatibility
-- =====================================================================
-- Current: JOVIAN(180), REDUCING(120)
-- REDUCING is wrong — Puffy Planets are inflated hot gas giants with
-- H2/He envelopes, not primordial reducing atmospheres on small rocky
-- worlds. Replace with EXOTIC for the hottest variants.

DELETE FROM ref.planet_atmosphere_compatibility
WHERE planet_type = 'Puffy Planet' AND atmosphere_classification = 'REDUCING';

INSERT INTO ref.planet_atmosphere_compatibility
    (planet_type, atmosphere_classification, preference_weight, notes)
VALUES
    ('Puffy Planet', 'EXOTIC', 60, 'Extreme heat variant with metallic vapors at highest temperatures');

UPDATE ref.planet_atmosphere_compatibility
SET preference_weight = 200
WHERE planet_type = 'Puffy Planet' AND atmosphere_classification = 'JOVIAN';

-- =====================================================================
-- SECTION 6: Fix Warm Neptune Compatibility
-- =====================================================================
-- Remove EARTH_LIKE — N2/O2 is physically impossible on a gas envelope
-- world with MIXED_SILICATE_ICE composition. No mechanism produces
-- free O2 in a hydrogen-dominated envelope.
-- Remove REDUCING — the original Reducing template (0.5-2 M⊕) is for
-- small primordial rocky worlds, not 2-20 M⊕ ice giants.

DELETE FROM ref.planet_atmosphere_compatibility
WHERE planet_type = 'Warm Neptune' AND atmosphere_classification = 'EARTH_LIKE';

DELETE FROM ref.planet_atmosphere_compatibility
WHERE planet_type = 'Warm Neptune' AND atmosphere_classification = 'REDUCING';

-- Rebalance remaining weights
UPDATE ref.planet_atmosphere_compatibility
SET preference_weight = 200
WHERE planet_type = 'Warm Neptune' AND atmosphere_classification = 'ICE_GIANT';

UPDATE ref.planet_atmosphere_compatibility
SET preference_weight = 160
WHERE planet_type = 'Warm Neptune' AND atmosphere_classification = 'AMMONIA';

UPDATE ref.planet_atmosphere_compatibility
SET preference_weight = 130
WHERE planet_type = 'Warm Neptune' AND atmosphere_classification = 'TITAN_LIKE';
