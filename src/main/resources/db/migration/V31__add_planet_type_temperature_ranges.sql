-- V31: Add temperature-based filtering for planet type selection
-- This ensures planet types are only selected when temperature conditions are appropriate

-- Add temperature columns
ALTER TABLE ref.planet_type_ref
    ADD COLUMN min_formation_temp_k NUMERIC(10,2),
    ADD COLUMN max_formation_temp_k NUMERIC(10,2);

-- Update existing planet types with scientifically accurate temperature ranges

-- Hot Rocky Planet (very close to star, extreme heat)
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 600, max_formation_temp_k = 2000
WHERE name = 'Hot Rocky Planet';

-- Lava Planet (molten surface, extreme heat)
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 1200, max_formation_temp_k = 3000
WHERE name = 'Lava Planet';

-- Terrestrial Planet (habitable zone temps)
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 200, max_formation_temp_k = 350
WHERE name = 'Terrestrial Planet';

-- Desert Planet (warm/hot, but not molten - liquid water has evaporated)
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 250, max_formation_temp_k = 500
WHERE name = 'Desert Planet';

-- Ocean Planet (liquid water stable)
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 273, max_formation_temp_k = 373
WHERE name = 'Ocean Planet';

-- Super-Earth (wider range, can be various types)
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 150, max_formation_temp_k = 500
WHERE name = 'Super-Earth';

-- Ice World (frozen, beyond frost line)
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 30, max_formation_temp_k = 150
WHERE name = 'Ice World';

-- Mini-Neptune (cold enough for volatile retention)
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 50, max_formation_temp_k = 150
WHERE name = 'Mini-Neptune';

-- Sub-Neptune (cold)
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 40, max_formation_temp_k = 120
WHERE name = 'Sub-Neptune';

-- Hot Jupiter (extreme heat from stellar proximity)
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 1000, max_formation_temp_k = 2500
WHERE name = 'Hot Jupiter';

-- Gas Giant (moderate temps, far enough to retain atmosphere)
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 80, max_formation_temp_k = 400
WHERE name = 'Gas Giant';

-- Super-Jupiter (can exist at various temperatures)
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 50, max_formation_temp_k = 600
WHERE name = 'Super-Jupiter';

-- Ice Giant (very cold)
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 40, max_formation_temp_k = 100
WHERE name = 'Ice Giant';

-- Puffy Planet (hot, inflated atmosphere)
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 800, max_formation_temp_k = 1800
WHERE name = 'Puffy Planet';

-- Carbon Planet (moderate temps)
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 200, max_formation_temp_k = 600
WHERE name = 'Carbon Planet';

-- Iron Planet (very hot, stripped)
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 500, max_formation_temp_k = 1500
WHERE name = 'Iron Planet';

-- Dwarf Planet (cold, outer system)
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 30, max_formation_temp_k = 150
WHERE name = 'Dwarf Planet';

-- Rogue Planet (no stellar heating, extremely cold)
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 10, max_formation_temp_k = 50
WHERE name = 'Rogue Planet';

-- Add constraint
ALTER TABLE ref.planet_type_ref
    ADD CONSTRAINT valid_formation_temp_range CHECK (
        min_formation_temp_k IS NULL OR
        max_formation_temp_k IS NULL OR
        min_formation_temp_k <= max_formation_temp_k
        );

-- Add comments
COMMENT ON COLUMN ref.planet_type_ref.min_formation_temp_k IS 'Minimum equilibrium temperature in Kelvin for this planet type to form/exist';
COMMENT ON COLUMN ref.planet_type_ref.max_formation_temp_k IS 'Maximum equilibrium temperature in Kelvin for this planet type to form/exist';