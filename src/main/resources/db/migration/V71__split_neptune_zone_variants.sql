-- =====================================================================
-- Flyway Migration V69: Split Neptune Types Into Zone-Specific Variants
-- =====================================================================
-- Problem: Mini-Neptune at 0.2% with frost_line zone (too restrictive).
-- Setting formation_zone = NULL made it 32-45% (too dominant).
-- Weight alone cannot balance a universally-eligible type.
--
-- Solution: Create zone-specific Neptune variants, matching how the
-- system already handles hot/cold variants for other types (Hot Jupiter
-- vs Gas Giant, Hot Rocky Planet vs Terrestrial, etc).
--
-- New types:
--   Warm Neptune  — habitable zone, 150-400K, irradiated envelope
--   Hot Neptune   — inner zone, 400-1000K, evaporating/inflated
--
-- Existing types adjusted:
--   Mini-Neptune  — stays frost_line, temp range restored to 50-150K
--   Sub-Neptune   — stays outer, max temp widened to 200K
--
-- Scientific basis:
--   Warm Neptunes are common Kepler finds at moderate irradiation.
--   Hot Neptunes are rare (the "hot Neptune desert") because
--   photoevaporation strips them, but they exist at low frequency.
-- =====================================================================

-- =====================================================================
-- SECTION 1: Revert Mini-Neptune to original frost_line configuration
-- =====================================================================

UPDATE ref.planet_type_ref
SET formation_zone = 'frost_line',
    min_formation_temp_k = 50,
    max_formation_temp_k = 150,
    min_formation_distance_au = 0.5,
    max_formation_distance_au = 5.0,
    rarity_weight = 200
WHERE name = 'Mini-Neptune';

-- Sub-Neptune: widen upper temp limit
UPDATE ref.planet_type_ref
SET max_formation_temp_k = 200
WHERE name = 'Sub-Neptune';

-- =====================================================================
-- SECTION 2: Add Warm Neptune (habitable zone)
-- =====================================================================
-- Irradiated enough to lose some volatiles, thinner H/He envelope,
-- transitional between ice giant and rocky super-Earth.
-- Common Kepler find — weight reflects this.

INSERT INTO ref.planet_type_ref (
    name, description,
    min_mass_earth, max_mass_earth,
    min_radius_earth, max_radius_earth,
    typical_density_g_cm3,
    min_formation_distance_au, max_formation_distance_au,
    formation_zone,
    can_have_atmosphere, typical_atmosphere,
    can_have_rings, ring_probability,
    min_moons, max_moons,
    habitable, typical_albedo, typical_core_type,
    rarity_weight,
    min_formation_temp_k, max_formation_temp_k,
    min_moon_system_mass_ratio, max_moon_system_mass_ratio
) VALUES (
    'Warm Neptune',
    'Moderately irradiated Neptune-class planet with partially depleted volatile envelope',
    2.0, 20.0,
    2.0, 5.0,
    2.0,
    0.5, 3.0,
    'habitable',
    true, 'H2, He, H2O, CH4',
    false, 0.0,
    0, 5,
    false, 0.4, 'Rocky-Ice',
    180,
    150, 400,
    0.00005, 0.001
);

-- =====================================================================
-- SECTION 3: Add Hot Neptune (inner zone)
-- =====================================================================
-- Heavily irradiated, inflated or actively evaporating atmosphere.
-- The "hot Neptune desert" means these are genuinely rare — low weight.
-- Photoevaporation strips smaller ones, so surviving examples tend to
-- be on the heavier end.

INSERT INTO ref.planet_type_ref (
    name, description,
    min_mass_earth, max_mass_earth,
    min_radius_earth, max_radius_earth,
    typical_density_g_cm3,
    min_formation_distance_au, max_formation_distance_au,
    formation_zone,
    can_have_atmosphere, typical_atmosphere,
    can_have_rings, ring_probability,
    min_moons, max_moons,
    habitable, typical_albedo, typical_core_type,
    rarity_weight,
    min_formation_temp_k, max_formation_temp_k,
    min_moon_system_mass_ratio, max_moon_system_mass_ratio
) VALUES (
    'Hot Neptune',
    'Highly irradiated Neptune-class planet with inflated, evaporating atmosphere',
    5.0, 25.0,
    3.0, 6.0,
    1.2,
    0.03, 0.5,
    'inner',
    true, 'H2, He, metallic vapors',
    false, 0.0,
    0, 1,
    false, 0.15, 'Rocky-Ice',
    40,
    400, 1200,
    0.00001, 0.0001
);

-- =====================================================================
-- SECTION 4: Composition Templates
-- =====================================================================

-- Warm Neptune — reduced ice, thicker silicate fraction
INSERT INTO ref.composition_template
    (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES
    ('Warm Neptune - Depleted Ice', 'Warm Neptune with reduced volatile envelope',
     'MIXED_SILICATE_ICE', 'Warm Neptune', 200, 150, 400);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 30, 45 FROM ref.composition_template WHERE name = 'Warm Neptune - Depleted Ice'
UNION ALL
SELECT id, 'PYROXENE', 'INTERIOR', 15, 25 FROM ref.composition_template WHERE name = 'Warm Neptune - Depleted Ice'
UNION ALL
SELECT id, 'WATER_ICE', 'INTERIOR', 15, 30 FROM ref.composition_template WHERE name = 'Warm Neptune - Depleted Ice'
UNION ALL
SELECT id, 'IRON', 'INTERIOR', 10, 20 FROM ref.composition_template WHERE name = 'Warm Neptune - Depleted Ice'
UNION ALL
SELECT id, 'HYDROGEN_HELIUM_MIX', 'ENVELOPE', 60, 80 FROM ref.composition_template WHERE name = 'Warm Neptune - Depleted Ice'
UNION ALL
SELECT id, 'WATER_ICE', 'ENVELOPE', 15, 30 FROM ref.composition_template WHERE name = 'Warm Neptune - Depleted Ice';

-- Hot Neptune — mostly rocky core, thin residual gas envelope
INSERT INTO ref.composition_template
    (name, description, classification, planet_types, rarity_weight, min_surface_temp_k, max_surface_temp_k)
VALUES
    ('Hot Neptune - Stripped Envelope', 'Hot Neptune with heavily eroded atmosphere',
     'GAS_ENVELOPE', 'Hot Neptune', 200, 400, 1200);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage)
SELECT id, 'OLIVINE', 'INTERIOR', 35, 50 FROM ref.composition_template WHERE name = 'Hot Neptune - Stripped Envelope'
UNION ALL
SELECT id, 'PYROXENE', 'INTERIOR', 20, 30 FROM ref.composition_template WHERE name = 'Hot Neptune - Stripped Envelope'
UNION ALL
SELECT id, 'IRON', 'INTERIOR', 20, 30 FROM ref.composition_template WHERE name = 'Hot Neptune - Stripped Envelope'
UNION ALL
SELECT id, 'HYDROGEN_HELIUM_MIX', 'ENVELOPE', 50, 70 FROM ref.composition_template WHERE name = 'Hot Neptune - Stripped Envelope'
UNION ALL
SELECT id, 'WATER_ICE', 'ENVELOPE', 5, 15 FROM ref.composition_template WHERE name = 'Hot Neptune - Stripped Envelope';

-- =====================================================================
-- SECTION 5: Atmosphere Compatibilities
-- =====================================================================

INSERT INTO ref.planet_atmosphere_compatibility
    (planet_type, atmosphere_classification, preference_weight, notes)
VALUES
    -- Warm Neptune — transitional atmospheres
    ('Warm Neptune', 'ICE_GIANT', 180, 'Retained ice giant atmosphere'),
    ('Warm Neptune', 'AMMONIA', 150, 'Ammonia-rich at cooler end'),
    ('Warm Neptune', 'TITAN_LIKE', 120, 'Nitrogen-methane dominated'),
    ('Warm Neptune', 'EARTH_LIKE', 40, 'Rare — heavily processed atmosphere'),
    ('Warm Neptune', 'REDUCING', 80, 'Hydrogen-rich primitive'),

    -- Hot Neptune — stripped/exotic atmospheres
    ('Hot Neptune', 'REDUCING', 180, 'Residual hydrogen envelope'),
    ('Hot Neptune', 'EXOTIC', 150, 'Metallic vapors and photodissociated species'),
    ('Hot Neptune', 'VOLCANIC', 100, 'Outgassed from exposed rocky core'),
    ('Hot Neptune', 'NONE', 60, 'Fully stripped by photoevaporation');

-- =====================================================================
-- SECTION 6: Geological Templates — add new types to gas giant templates
-- =====================================================================

UPDATE ref.geological_template
SET planet_types = planet_types || ',Warm Neptune,Hot Neptune'
WHERE name IN ('Calm Gas Giant', 'Active Gas Giant', 'Turbulent Super-Jupiter');
