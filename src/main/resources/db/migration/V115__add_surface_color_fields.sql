-- V101: Add surface color fields for planet rendering
-- Primary/secondary hex colors derived from composition, temperature, and atmosphere.
-- Surface seed for deterministic procedural generation in the UI.

ALTER TABLE ud.physical_properties
    ADD COLUMN IF NOT EXISTS surface_color_primary VARCHAR(7),
    ADD COLUMN IF NOT EXISTS surface_color_secondary VARCHAR(7);

ALTER TABLE ud.planet
    ADD COLUMN IF NOT EXISTS surface_seed BIGINT;
