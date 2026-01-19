-- V17__rename_composition_columns_to_universal_terms.sql
-- Rename mantle/crust to interior/envelope to work for all planet types
-- "Mantle" and "Crust" are rocky planet terms that don't fit gas giants

-- Rename columns in planet table
ALTER TABLE ud.planet
    RENAME COLUMN mantle_composition TO interior_composition;

ALTER TABLE ud.planet
    RENAME COLUMN crust_composition TO envelope_composition;

-- Update column comments to reflect new terminology
COMMENT ON COLUMN ud.planet.interior_composition IS 'Interior/core composition (mantle for rocky planets, core for gas giants)';
COMMENT ON COLUMN ud.planet.envelope_composition IS 'Outer layer composition (crust for rocky planets, atmosphere for gas giants)';

-- Drop check constraint BEFORE updating data
ALTER TABLE ref.composition_template_component
    DROP CONSTRAINT IF EXISTS composition_template_component_layer_type_check;

-- Now rename layer_type values in composition templates
UPDATE ref.composition_template_component
SET layer_type = 'INTERIOR'
WHERE layer_type = 'MANTLE';

UPDATE ref.composition_template_component
SET layer_type = 'ENVELOPE'
WHERE layer_type = 'CRUST';

-- Add new check constraint with updated values
ALTER TABLE ref.composition_template_component
    ADD CONSTRAINT composition_template_component_layer_type_check
        CHECK (layer_type IN ('INTERIOR', 'ENVELOPE'));

-- Update column comment
COMMENT ON COLUMN ref.composition_template_component.layer_type IS 'INTERIOR (core/mantle) or ENVELOPE (crust/atmosphere/surface)';