-- V74__add_super_jupiter_to_ring_templates.sql
-- Fix: Super-Jupiters have 0% rings because no ring_template includes
-- "Super-Jupiter" in its planet_types list. The planet_type_ref has
-- ring_probability = 0.5 but template matching always fails.
-- 0 out of 14,699 Super-Jupiters had rings in the 100K run.

-- Saturn-Type Main Rings: add Super-Jupiter
UPDATE ref.ring_template
SET planet_types = 'Gas Giant, Ice Giant, Super-Jupiter'
WHERE name = 'Saturn-Type Main Rings';

-- Narrow Dark Rings: add Super-Jupiter
UPDATE ref.ring_template
SET planet_types = 'Ice Giant, Gas Giant, Super-Jupiter'
WHERE name = 'Narrow Dark Rings';

-- Gossamer Rings: add Super-Jupiter
UPDATE ref.ring_template
SET planet_types = 'Gas Giant, Super-Jupiter'
WHERE name = 'Gossamer Rings';

-- Young System Debris Ring: add Super-Jupiter
UPDATE ref.ring_template
SET planet_types = 'Gas Giant, Ice Giant, Super Earth, Super-Jupiter'
WHERE name = 'Young System Debris Ring';