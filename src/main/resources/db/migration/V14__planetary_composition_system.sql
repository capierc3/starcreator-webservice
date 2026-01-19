-- V14__planetary_composition_system.sql
-- Creates composition template tables and populates with realistic data for all 18 planet types

-- Create composition template table
CREATE TABLE ref.composition_template (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    classification VARCHAR(50) NOT NULL,
    planet_types VARCHAR(500),
    min_distance_au DOUBLE PRECISION,
    max_distance_au DOUBLE PRECISION,
    rarity_weight INTEGER DEFAULT 100,
    created_at TIMESTAMP DEFAULT NOW()
);

COMMENT ON TABLE ref.composition_template IS 'Pre-defined composition templates for different planet types';
COMMENT ON COLUMN ref.composition_template.planet_types IS 'Comma-separated list of applicable planet types';
COMMENT ON COLUMN ref.composition_template.rarity_weight IS 'Weight for random selection (higher = more common)';

-- Create composition components table
CREATE TABLE ref.composition_template_component (
    id SERIAL PRIMARY KEY,
    template_id INTEGER NOT NULL REFERENCES ref.composition_template(id) ON DELETE CASCADE,
    mineral VARCHAR(50) NOT NULL,
    layer_type VARCHAR(10) NOT NULL CHECK (layer_type IN ('MANTLE', 'CRUST')),
    min_percentage DOUBLE PRECISION NOT NULL,
    max_percentage DOUBLE PRECISION NOT NULL,
    CONSTRAINT valid_percentage CHECK (min_percentage >= 0 AND max_percentage <= 100 AND min_percentage <= max_percentage)
);

COMMENT ON TABLE ref.composition_template_component IS 'Mineral components for each composition template';
COMMENT ON COLUMN ref.composition_template_component.layer_type IS 'MANTLE or CRUST layer designation';

-- Add columns to planet table
ALTER TABLE ud.planet 
ADD COLUMN mantle_composition VARCHAR(500),
ADD COLUMN crust_composition VARCHAR(500),
ADD COLUMN composition_classification VARCHAR(50);

COMMENT ON COLUMN ud.planet.mantle_composition IS 'Mantle mineral composition as percentage string';
COMMENT ON COLUMN ud.planet.crust_composition IS 'Crust mineral composition as percentage string';
COMMENT ON COLUMN ud.planet.composition_classification IS 'Broad compositional category';

-- Create indexes
CREATE INDEX idx_composition_template_classification ON ref.composition_template(classification);
CREATE INDEX idx_composition_template_component ON ref.composition_template_component(template_id);
CREATE INDEX idx_planet_composition_classification ON ud.planet(composition_classification);

-- ============================================================================
-- SEED DATA: Composition Templates for all 18 Planet Types
-- ============================================================================

-- 1. Hot Rocky Planet (0.05-0.4 AU)
INSERT INTO ref.composition_template (name, classification, planet_types, min_distance_au, max_distance_au, rarity_weight)
VALUES ('Hot Rocky - Stripped Core', 'IRON_RICH', 'Hot Rocky Planet', 0.05, 0.4, 150);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Hot Rocky - Stripped Core'), 'IRON', 'MANTLE', 70, 85),
    ((SELECT id FROM ref.composition_template WHERE name = 'Hot Rocky - Stripped Core'), 'NICKEL', 'MANTLE', 10, 20),
    ((SELECT id FROM ref.composition_template WHERE name = 'Hot Rocky - Stripped Core'), 'MAGNESIUM_OXIDE', 'MANTLE', 5, 15),
    ((SELECT id FROM ref.composition_template WHERE name = 'Hot Rocky - Stripped Core'), 'IRON', 'CRUST', 80, 95),
    ((SELECT id FROM ref.composition_template WHERE name = 'Hot Rocky - Stripped Core'), 'NICKEL', 'CRUST', 5, 15);

-- 2. Terrestrial Planet (0.7-1.5 AU) - Earth-like
INSERT INTO ref.composition_template (name, classification, planet_types, min_distance_au, max_distance_au, rarity_weight)
VALUES ('Terrestrial - Earth-like', 'SILICATE_RICH', 'Terrestrial Planet', 0.7, 1.5, 200);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Terrestrial - Earth-like'), 'OLIVINE', 'MANTLE', 45, 55),
    ((SELECT id FROM ref.composition_template WHERE name = 'Terrestrial - Earth-like'), 'PYROXENE', 'MANTLE', 30, 40),
    ((SELECT id FROM ref.composition_template WHERE name = 'Terrestrial - Earth-like'), 'IRON', 'MANTLE', 10, 15),
    ((SELECT id FROM ref.composition_template WHERE name = 'Terrestrial - Earth-like'), 'FELDSPAR', 'CRUST', 45, 60),
    ((SELECT id FROM ref.composition_template WHERE name = 'Terrestrial - Earth-like'), 'QUARTZ', 'CRUST', 20, 30),
    ((SELECT id FROM ref.composition_template WHERE name = 'Terrestrial - Earth-like'), 'PYROXENE', 'CRUST', 10, 25);

-- 3. Super-Earth (0.5-2 AU)
INSERT INTO ref.composition_template (name, classification, planet_types, min_distance_au, max_distance_au, rarity_weight)
VALUES ('Super-Earth - Dense Silicate', 'SILICATE_RICH', 'Super-Earth', 0.5, 2.0, 180);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Super-Earth - Dense Silicate'), 'OLIVINE', 'MANTLE', 40, 50),
    ((SELECT id FROM ref.composition_template WHERE name = 'Super-Earth - Dense Silicate'), 'PYROXENE', 'MANTLE', 30, 40),
    ((SELECT id FROM ref.composition_template WHERE name = 'Super-Earth - Dense Silicate'), 'IRON', 'MANTLE', 15, 25),
    ((SELECT id FROM ref.composition_template WHERE name = 'Super-Earth - Dense Silicate'), 'MAGNESIUM_OXIDE', 'MANTLE', 5, 10),
    ((SELECT id FROM ref.composition_template WHERE name = 'Super-Earth - Dense Silicate'), 'BASALT', 'CRUST', 50, 70),
    ((SELECT id FROM ref.composition_template WHERE name = 'Super-Earth - Dense Silicate'), 'FELDSPAR', 'CRUST', 20, 35),
    ((SELECT id FROM ref.composition_template WHERE name = 'Super-Earth - Dense Silicate'), 'IRON', 'CRUST', 5, 15);

-- 4. Desert Planet (0.8-2 AU)
INSERT INTO ref.composition_template (name, classification, planet_types, min_distance_au, max_distance_au, rarity_weight)
VALUES ('Desert - Oxidized Silicate', 'SILICATE_RICH', 'Desert Planet', 0.8, 2.0, 120);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Desert - Oxidized Silicate'), 'OLIVINE', 'MANTLE', 40, 50),
    ((SELECT id FROM ref.composition_template WHERE name = 'Desert - Oxidized Silicate'), 'PYROXENE', 'MANTLE', 30, 40),
    ((SELECT id FROM ref.composition_template WHERE name = 'Desert - Oxidized Silicate'), 'IRON', 'MANTLE', 10, 20),
    ((SELECT id FROM ref.composition_template WHERE name = 'Desert - Oxidized Silicate'), 'SILICON_DIOXIDE', 'CRUST', 40, 60),
    ((SELECT id FROM ref.composition_template WHERE name = 'Desert - Oxidized Silicate'), 'IRON', 'CRUST', 15, 30),
    ((SELECT id FROM ref.composition_template WHERE name = 'Desert - Oxidized Silicate'), 'ALUMINUM_OXIDE', 'CRUST', 10, 20);

-- 5. Ocean Planet (0.9-1.8 AU)
INSERT INTO ref.composition_template (name, classification, planet_types, min_distance_au, max_distance_au, rarity_weight)
VALUES ('Ocean World - Water Rich', 'OCEAN_WORLD', 'Ocean Planet', 0.9, 1.8, 100);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Ocean World - Water Rich'), 'OLIVINE', 'MANTLE', 40, 50),
    ((SELECT id FROM ref.composition_template WHERE name = 'Ocean World - Water Rich'), 'PYROXENE', 'MANTLE', 30, 40),
    ((SELECT id FROM ref.composition_template WHERE name = 'Ocean World - Water Rich'), 'IRON', 'MANTLE', 10, 20),
    ((SELECT id FROM ref.composition_template WHERE name = 'Ocean World - Water Rich'), 'LIQUID_WATER', 'CRUST', 80, 100);

-- 6. Lava Planet (0.01-0.15 AU)
INSERT INTO ref.composition_template (name, classification, planet_types, min_distance_au, max_distance_au, rarity_weight)
VALUES ('Lava - Molten Surface', 'MOLTEN_SURFACE', 'Lava Planet', 0.01, 0.15, 80);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Lava - Molten Surface'), 'OLIVINE', 'MANTLE', 45, 55),
    ((SELECT id FROM ref.composition_template WHERE name = 'Lava - Molten Surface'), 'PYROXENE', 'MANTLE', 30, 40),
    ((SELECT id FROM ref.composition_template WHERE name = 'Lava - Molten Surface'), 'IRON', 'MANTLE', 10, 20),
    ((SELECT id FROM ref.composition_template WHERE name = 'Lava - Molten Surface'), 'MOLTEN_ROCK', 'CRUST', 70, 90),
    ((SELECT id FROM ref.composition_template WHERE name = 'Lava - Molten Surface'), 'BASALT', 'CRUST', 10, 30);

-- 7. Mini-Neptune (0.5-5 AU)
INSERT INTO ref.composition_template (name, classification, planet_types, min_distance_au, max_distance_au, rarity_weight)
VALUES ('Mini-Neptune - Ice-Rock Core', 'MIXED_SILICATE_ICE', 'Mini-Neptune', 0.5, 5.0, 250);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Mini-Neptune - Ice-Rock Core'), 'OLIVINE', 'MANTLE', 25, 35),
    ((SELECT id FROM ref.composition_template WHERE name = 'Mini-Neptune - Ice-Rock Core'), 'WATER_ICE', 'MANTLE', 35, 50),
    ((SELECT id FROM ref.composition_template WHERE name = 'Mini-Neptune - Ice-Rock Core'), 'METHANE_ICE', 'MANTLE', 10, 20),
    ((SELECT id FROM ref.composition_template WHERE name = 'Mini-Neptune - Ice-Rock Core'), 'AMMONIA_ICE', 'MANTLE', 5, 15),
    ((SELECT id FROM ref.composition_template WHERE name = 'Mini-Neptune - Ice-Rock Core'), 'HYDROGEN_HELIUM_MIX', 'CRUST', 100, 100);

-- 8. Sub-Neptune (1-10 AU)
INSERT INTO ref.composition_template (name, classification, planet_types, min_distance_au, max_distance_au, rarity_weight)
VALUES ('Sub-Neptune - Ice Giant', 'ICE_RICH', 'Sub-Neptune', 1.0, 10.0, 200);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Sub-Neptune - Ice Giant'), 'WATER_ICE', 'MANTLE', 40, 55),
    ((SELECT id FROM ref.composition_template WHERE name = 'Sub-Neptune - Ice Giant'), 'METHANE_ICE', 'MANTLE', 20, 30),
    ((SELECT id FROM ref.composition_template WHERE name = 'Sub-Neptune - Ice Giant'), 'AMMONIA_ICE', 'MANTLE', 15, 25),
    ((SELECT id FROM ref.composition_template WHERE name = 'Sub-Neptune - Ice Giant'), 'OLIVINE', 'MANTLE', 5, 15),
    ((SELECT id FROM ref.composition_template WHERE name = 'Sub-Neptune - Ice Giant'), 'HYDROGEN_HELIUM_MIX', 'CRUST', 100, 100);

-- 9. Hot Jupiter (0.01-0.1 AU)
INSERT INTO ref.composition_template (name, classification, planet_types, min_distance_au, max_distance_au, rarity_weight)
VALUES ('Hot Jupiter - Inflated Gas', 'GAS_ENVELOPE', 'Hot Jupiter', 0.01, 0.1, 100);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Hot Jupiter - Inflated Gas'), 'HYDROGEN_HELIUM_MIX', 'MANTLE', 90, 98),
    ((SELECT id FROM ref.composition_template WHERE name = 'Hot Jupiter - Inflated Gas'), 'WATER_ICE', 'MANTLE', 1, 5),
    ((SELECT id FROM ref.composition_template WHERE name = 'Hot Jupiter - Inflated Gas'), 'HYDROGEN_HELIUM_MIX', 'CRUST', 100, 100);

-- 10. Gas Giant (3-10 AU)
INSERT INTO ref.composition_template (name, classification, planet_types, min_distance_au, max_distance_au, rarity_weight)
VALUES ('Gas Giant - Jupiter-like', 'GAS_ENVELOPE', 'Gas Giant', 3.0, 10.0, 300);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Gas Giant - Jupiter-like'), 'HYDROGEN_HELIUM_MIX', 'MANTLE', 85, 92),
    ((SELECT id FROM ref.composition_template WHERE name = 'Gas Giant - Jupiter-like'), 'WATER_ICE', 'MANTLE', 3, 8),
    ((SELECT id FROM ref.composition_template WHERE name = 'Gas Giant - Jupiter-like'), 'METHANE_ICE', 'MANTLE', 2, 5),
    ((SELECT id FROM ref.composition_template WHERE name = 'Gas Giant - Jupiter-like'), 'AMMONIA_ICE', 'MANTLE', 1, 3),
    ((SELECT id FROM ref.composition_template WHERE name = 'Gas Giant - Jupiter-like'), 'HYDROGEN_HELIUM_MIX', 'CRUST', 100, 100);

-- 11. Super-Jupiter (5-30 AU)
INSERT INTO ref.composition_template (name, classification, planet_types, min_distance_au, max_distance_au, rarity_weight)
VALUES ('Super-Jupiter - Massive Gas', 'GAS_ENVELOPE', 'Super-Jupiter', 5.0, 30.0, 80);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Super-Jupiter - Massive Gas'), 'HYDROGEN_HELIUM_MIX', 'MANTLE', 80, 90),
    ((SELECT id FROM ref.composition_template WHERE name = 'Super-Jupiter - Massive Gas'), 'METALLIC_HYDROGEN', 'MANTLE', 5, 15),
    ((SELECT id FROM ref.composition_template WHERE name = 'Super-Jupiter - Massive Gas'), 'WATER_ICE', 'MANTLE', 2, 5),
    ((SELECT id FROM ref.composition_template WHERE name = 'Super-Jupiter - Massive Gas'), 'HYDROGEN_HELIUM_MIX', 'CRUST', 100, 100);

-- 12. Ice Giant (8-30 AU)
INSERT INTO ref.composition_template (name, classification, planet_types, min_distance_au, max_distance_au, rarity_weight)
VALUES ('Ice Giant - Neptune-like', 'ICE_RICH', 'Ice Giant', 8.0, 30.0, 220);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Ice Giant - Neptune-like'), 'WATER_ICE', 'MANTLE', 45, 60),
    ((SELECT id FROM ref.composition_template WHERE name = 'Ice Giant - Neptune-like'), 'METHANE_ICE', 'MANTLE', 20, 30),
    ((SELECT id FROM ref.composition_template WHERE name = 'Ice Giant - Neptune-like'), 'AMMONIA_ICE', 'MANTLE', 10, 20),
    ((SELECT id FROM ref.composition_template WHERE name = 'Ice Giant - Neptune-like'), 'OLIVINE', 'MANTLE', 5, 10),
    ((SELECT id FROM ref.composition_template WHERE name = 'Ice Giant - Neptune-like'), 'HYDROGEN_HELIUM_MIX', 'CRUST', 100, 100);

-- 13. Puffy Planet (0.02-0.15 AU)
INSERT INTO ref.composition_template (name, classification, planet_types, min_distance_au, max_distance_au, rarity_weight)
VALUES ('Puffy - Low Density Gas', 'GAS_ENVELOPE', 'Puffy Planet', 0.02, 0.15, 70);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Puffy - Low Density Gas'), 'HYDROGEN_HELIUM_MIX', 'MANTLE', 92, 98),
    ((SELECT id FROM ref.composition_template WHERE name = 'Puffy - Low Density Gas'), 'WATER_ICE', 'MANTLE', 1, 4),
    ((SELECT id FROM ref.composition_template WHERE name = 'Puffy - Low Density Gas'), 'HYDROGEN_HELIUM_MIX', 'CRUST', 100, 100);

-- 14. Carbon Planet (0.5-3 AU)
INSERT INTO ref.composition_template (name, classification, planet_types, min_distance_au, max_distance_au, rarity_weight)
VALUES ('Carbon - Diamond Core', 'CARBON_RICH', 'Carbon Planet', 0.5, 3.0, 60);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Carbon - Diamond Core'), 'GRAPHITE', 'MANTLE', 40, 50),
    ((SELECT id FROM ref.composition_template WHERE name = 'Carbon - Diamond Core'), 'SILICON_CARBIDE', 'MANTLE', 25, 35),
    ((SELECT id FROM ref.composition_template WHERE name = 'Carbon - Diamond Core'), 'DIAMOND', 'MANTLE', 10, 20),
    ((SELECT id FROM ref.composition_template WHERE name = 'Carbon - Diamond Core'), 'IRON_CARBIDE', 'MANTLE', 5, 15),
    ((SELECT id FROM ref.composition_template WHERE name = 'Carbon - Diamond Core'), 'GRAPHITE', 'CRUST', 50, 70),
    ((SELECT id FROM ref.composition_template WHERE name = 'Carbon - Diamond Core'), 'DIAMOND', 'CRUST', 15, 30),
    ((SELECT id FROM ref.composition_template WHERE name = 'Carbon - Diamond Core'), 'AMORPHOUS_CARBON', 'CRUST', 5, 20);

-- 15. Iron Planet (0.1-0.5 AU)
INSERT INTO ref.composition_template (name, classification, planet_types, min_distance_au, max_distance_au, rarity_weight)
VALUES ('Iron - Stripped Core', 'IRON_RICH', 'Iron Planet', 0.1, 0.5, 50);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Iron - Stripped Core'), 'IRON', 'MANTLE', 80, 90),
    ((SELECT id FROM ref.composition_template WHERE name = 'Iron - Stripped Core'), 'NICKEL', 'MANTLE', 10, 20),
    ((SELECT id FROM ref.composition_template WHERE name = 'Iron - Stripped Core'), 'IRON', 'CRUST', 85, 95),
    ((SELECT id FROM ref.composition_template WHERE name = 'Iron - Stripped Core'), 'NICKEL', 'CRUST', 5, 15);

-- 16. Ice World (5-50 AU)
INSERT INTO ref.composition_template (name, classification, planet_types, min_distance_au, max_distance_au, rarity_weight)
VALUES ('Ice World - Frozen Volatiles', 'ICE_RICH', 'Ice World', 5.0, 50.0, 150);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Ice World - Frozen Volatiles'), 'WATER_ICE', 'MANTLE', 50, 70),
    ((SELECT id FROM ref.composition_template WHERE name = 'Ice World - Frozen Volatiles'), 'METHANE_ICE', 'MANTLE', 15, 30),
    ((SELECT id FROM ref.composition_template WHERE name = 'Ice World - Frozen Volatiles'), 'NITROGEN_ICE', 'MANTLE', 5, 15),
    ((SELECT id FROM ref.composition_template WHERE name = 'Ice World - Frozen Volatiles'), 'OLIVINE', 'MANTLE', 5, 10),
    ((SELECT id FROM ref.composition_template WHERE name = 'Ice World - Frozen Volatiles'), 'WATER_ICE', 'CRUST', 60, 80),
    ((SELECT id FROM ref.composition_template WHERE name = 'Ice World - Frozen Volatiles'), 'METHANE_ICE', 'CRUST', 10, 25),
    ((SELECT id FROM ref.composition_template WHERE name = 'Ice World - Frozen Volatiles'), 'NITROGEN_ICE', 'CRUST', 5, 20);

-- 17. Dwarf Planet (10-100 AU)
INSERT INTO ref.composition_template (name, classification, planet_types, min_distance_au, max_distance_au, rarity_weight)
VALUES ('Dwarf - Ice-Rock Mix', 'ICE_RICH', 'Dwarf Planet', 10.0, 100.0, 100);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Ice-Rock Mix'), 'OLIVINE', 'MANTLE', 30, 45),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Ice-Rock Mix'), 'WATER_ICE', 'MANTLE', 30, 45),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Ice-Rock Mix'), 'METHANE_ICE', 'MANTLE', 10, 20),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Ice-Rock Mix'), 'NITROGEN_ICE', 'MANTLE', 5, 15),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Ice-Rock Mix'), 'WATER_ICE', 'CRUST', 50, 70),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Ice-Rock Mix'), 'METHANE_ICE', 'CRUST', 15, 30),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Ice-Rock Mix'), 'NITROGEN_ICE', 'CRUST', 5, 20);

-- 18. Rogue Planet (no orbital constraints)
INSERT INTO ref.composition_template (name, classification, planet_types, rarity_weight)
VALUES ('Rogue - Frozen Ejected', 'ICE_RICH', 'Rogue Planet', 30);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Rogue - Frozen Ejected'), 'OLIVINE', 'MANTLE', 35, 45),
    ((SELECT id FROM ref.composition_template WHERE name = 'Rogue - Frozen Ejected'), 'WATER_ICE', 'MANTLE', 30, 40),
    ((SELECT id FROM ref.composition_template WHERE name = 'Rogue - Frozen Ejected'), 'METHANE_ICE', 'MANTLE', 10, 20),
    ((SELECT id FROM ref.composition_template WHERE name = 'Rogue - Frozen Ejected'), 'IRON', 'MANTLE', 5, 15),
    ((SELECT id FROM ref.composition_template WHERE name = 'Rogue - Frozen Ejected'), 'WATER_ICE', 'CRUST', 60, 80),
    ((SELECT id FROM ref.composition_template WHERE name = 'Rogue - Frozen Ejected'), 'METHANE_ICE', 'CRUST', 10, 25),
    ((SELECT id FROM ref.composition_template WHERE name = 'Rogue - Frozen Ejected'), 'NITROGEN_ICE', 'CRUST', 5, 15);

-- ============================================================================
-- Additional variant templates for diversity
-- ============================================================================

-- Terrestrial variant: High metal content
INSERT INTO ref.composition_template (name, classification, planet_types, min_distance_au, max_distance_au, rarity_weight)
VALUES ('Terrestrial - Metal Rich', 'SILICATE_RICH', 'Terrestrial Planet,Super-Earth', 0.7, 1.5, 100);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Terrestrial - Metal Rich'), 'OLIVINE', 'MANTLE', 35, 45),
    ((SELECT id FROM ref.composition_template WHERE name = 'Terrestrial - Metal Rich'), 'PYROXENE', 'MANTLE', 25, 35),
    ((SELECT id FROM ref.composition_template WHERE name = 'Terrestrial - Metal Rich'), 'IRON', 'MANTLE', 20, 30),
    ((SELECT id FROM ref.composition_template WHERE name = 'Terrestrial - Metal Rich'), 'BASALT', 'CRUST', 40, 60),
    ((SELECT id FROM ref.composition_template WHERE name = 'Terrestrial - Metal Rich'), 'IRON', 'CRUST', 20, 35),
    ((SELECT id FROM ref.composition_template WHERE name = 'Terrestrial - Metal Rich'), 'FELDSPAR', 'CRUST', 15, 25);

-- Ocean variant: Deep subsurface ocean
INSERT INTO ref.composition_template (name, classification, planet_types, min_distance_au, max_distance_au, rarity_weight)
VALUES ('Ocean - Deep Ice Shell', 'OCEAN_WORLD', 'Ocean Planet', 1.5, 3.0, 80);

INSERT INTO ref.composition_template_component (template_id, mineral, layer_type, min_percentage, max_percentage) VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Ocean - Deep Ice Shell'), 'OLIVINE', 'MANTLE', 35, 45),
    ((SELECT id FROM ref.composition_template WHERE name = 'Ocean - Deep Ice Shell'), 'PYROXENE', 'MANTLE', 25, 35),
    ((SELECT id FROM ref.composition_template WHERE name = 'Ocean - Deep Ice Shell'), 'IRON', 'MANTLE', 15, 25),
    ((SELECT id FROM ref.composition_template WHERE name = 'Ocean - Deep Ice Shell'), 'HIGH_PRESSURE_ICE', 'CRUST', 50, 70),
    ((SELECT id FROM ref.composition_template WHERE name = 'Ocean - Deep Ice Shell'), 'WATER_ICE', 'CRUST', 30, 50);
