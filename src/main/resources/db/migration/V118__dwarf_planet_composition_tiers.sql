-- V118__dwarf_planet_composition_tiers.sql
-- BUG-002: All dwarf planets get ICE_RICH composition, even in inner belts.
--
-- Real-world context:
--   Ceres (2.77 AU, ~168K): ~73% rock/silicate, ~27% ice  -> SILICATE_RICH
--   Jupiter trojans (~5.2 AU, ~124K): rock-ice mix          -> MIXED_SILICATE_ICE
--   Pluto (39.5 AU, ~44K): dominated by frozen volatiles    -> ICE_RICH
--
-- Fix: Create three overlapping composition tiers keyed by surface temperature.
-- The temperature formula (278 * (L*(1-a))^0.25 / sqrt(d)) naturally gives
-- warmer temps for inner-belt dwarfs and colder for outer-belt, so no code
-- changes are needed — template matching handles it.
--
-- Tier overlap zones:
--   60-100K  -> ICE_RICH and MIXED compete
--   100-180K -> MIXED and ROCKY compete

-- ── 1. Narrow existing ice template from 10-250K to 10-100K ──
UPDATE ref.composition_template
SET max_surface_temp_k = 100
WHERE name = 'Dwarf - Ice-Rock Mix';

-- ── 2. New: Dwarf - Mixed Silicate-Ice (transition zone, 60-180K) ──
INSERT INTO ref.composition_template
    (name, classification, planet_types, min_surface_temp_k, max_surface_temp_k, rarity_weight)
VALUES
    ('Dwarf - Mixed Silicate-Ice', 'MIXED_SILICATE_ICE', 'Dwarf Planet', 60, 180, 100);

INSERT INTO ref.composition_template_component
    (template_id, mineral, layer_type, min_percentage, max_percentage)
VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Mixed Silicate-Ice'), 'OLIVINE',    'INTERIOR', 35, 50),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Mixed Silicate-Ice'), 'PYROXENE',   'INTERIOR', 15, 25),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Mixed Silicate-Ice'), 'WATER_ICE',  'INTERIOR', 20, 35),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Mixed Silicate-Ice'), 'IRON',       'INTERIOR',  5, 15),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Mixed Silicate-Ice'), 'REGOLITH',   'ENVELOPE', 40, 60),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Mixed Silicate-Ice'), 'WATER_ICE',  'ENVELOPE', 20, 35),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Mixed Silicate-Ice'), 'OLIVINE',    'ENVELOPE', 10, 25);

-- ── 3. New: Dwarf - Rocky (inner belt, Ceres-like, 100-500K) ──
INSERT INTO ref.composition_template
    (name, classification, planet_types, min_surface_temp_k, max_surface_temp_k, rarity_weight)
VALUES
    ('Dwarf - Rocky', 'SILICATE_RICH', 'Dwarf Planet', 100, 500, 100);

INSERT INTO ref.composition_template_component
    (template_id, mineral, layer_type, min_percentage, max_percentage)
VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Rocky'), 'OLIVINE',    'INTERIOR', 35, 50),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Rocky'), 'PYROXENE',   'INTERIOR', 20, 30),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Rocky'), 'IRON',       'INTERIOR', 15, 25),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Rocky'), 'FELDSPAR',   'INTERIOR',  5, 15),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Rocky'), 'REGOLITH',   'ENVELOPE', 50, 70),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Rocky'), 'OLIVINE',    'ENVELOPE', 15, 30),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Rocky'), 'PYROXENE',   'ENVELOPE', 10, 20);
