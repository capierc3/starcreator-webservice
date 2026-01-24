-- V30: Convert composition templates from distance-based to temperature-based filtering
-- This fixes issues in binary/multiple star systems where distance doesn't correlate with temperature

-- Step 1: Add temperature columns
ALTER TABLE ref.composition_template
    ADD COLUMN min_surface_temp_k NUMERIC(10,2),
    ADD COLUMN max_surface_temp_k NUMERIC(10,2);

-- Step 2: Update existing templates with scientifically accurate temperature ranges
-- Temperature ranges are based on thermal equilibrium conditions for each composition type

-- Hot Rocky Planet (Mercury-like, stripped core, very close to star)
UPDATE ref.composition_template
SET min_surface_temp_k = 400, max_surface_temp_k = 700
WHERE id = 1;

-- Terrestrial Planet - Earth-like (habitable zone range)
UPDATE ref.composition_template
SET min_surface_temp_k = 250, max_surface_temp_k = 350
WHERE id = 2;

-- Super-Earth - Dense Silicate (wider range, can retain more heat)
UPDATE ref.composition_template
SET min_surface_temp_k = 200, max_surface_temp_k = 450
WHERE id = 3;

-- Desert Planet - Oxidized Silicate (warm/hot, little water retention)
UPDATE ref.composition_template
SET min_surface_temp_k = 300, max_surface_temp_k = 500
WHERE id = 4;

-- Ocean World - Water Rich (liquid water range)
UPDATE ref.composition_template
SET min_surface_temp_k = 273, max_surface_temp_k = 373
WHERE id = 5;

-- Lava Planet - Molten Surface (extremely hot, surface melting)
UPDATE ref.composition_template
SET min_surface_temp_k = 1200, max_surface_temp_k = 2500
WHERE id = 6;

-- Mini-Neptune - Ice-Rock Core (cold enough for volatiles)
UPDATE ref.composition_template
SET min_surface_temp_k = 50, max_surface_temp_k = 150
WHERE id = 7;

-- Sub-Neptune - Ice Giant (very cold)
UPDATE ref.composition_template
SET min_surface_temp_k = 40, max_surface_temp_k = 100
WHERE id = 8;

-- Hot Jupiter - Inflated Gas (very hot from stellar proximity, tidally heated)
UPDATE ref.composition_template
SET min_surface_temp_k = 1000, max_surface_temp_k = 2500
WHERE id = 9;

-- Gas Giant - Jupiter-like (moderate temp, far enough to retain atmosphere)
UPDATE ref.composition_template
SET min_surface_temp_k = 80, max_surface_temp_k = 300
WHERE id = 10;

-- Super-Jupiter - Massive Gas (can exist at various temperatures)
UPDATE ref.composition_template
SET min_surface_temp_k = 50, max_surface_temp_k = 500
WHERE id = 11;

-- Ice Giant - Neptune-like (very cold, icy composition stable)
UPDATE ref.composition_template
SET min_surface_temp_k = 40, max_surface_temp_k = 80
WHERE id = 12;

-- Puffy Planet - Low Density Gas (hot, expanded atmosphere)
UPDATE ref.composition_template
SET min_surface_temp_k = 800, max_surface_temp_k = 1800
WHERE id = 13;

-- Carbon Planet - Diamond Core (moderate temps for carbon stability)
UPDATE ref.composition_template
SET min_surface_temp_k = 200, max_surface_temp_k = 600
WHERE id = 14;

-- Iron Planet - Stripped Core (very hot, close to star)
UPDATE ref.composition_template
SET min_surface_temp_k = 500, max_surface_temp_k = 1200
WHERE id = 15;

-- Ice World - Frozen Volatiles (extremely cold)
UPDATE ref.composition_template
SET min_surface_temp_k = 30, max_surface_temp_k = 150
WHERE id = 16;

-- Dwarf Planet - Ice-Rock Mix (very cold, outer system)
UPDATE ref.composition_template
SET min_surface_temp_k = 30, max_surface_temp_k = 100
WHERE id = 17;

-- Rogue Planet - Frozen Ejected (no stellar heating, extremely cold)
UPDATE ref.composition_template
SET min_surface_temp_k = 10, max_surface_temp_k = 50
WHERE id = 18;

-- Terrestrial - Metal Rich (similar to Earth-like but more metal)
UPDATE ref.composition_template
SET min_surface_temp_k = 250, max_surface_temp_k = 400
WHERE id = 19;

-- Ocean - Deep Ice Shell (cold, subsurface ocean beneath ice)
UPDATE ref.composition_template
SET min_surface_temp_k = 100, max_surface_temp_k = 273
WHERE id = 20;

-- Step 3: Add constraints to ensure temperature ranges are valid
ALTER TABLE ref.composition_template
    ADD CONSTRAINT valid_temp_range CHECK (
        min_surface_temp_k IS NULL OR
        max_surface_temp_k IS NULL OR
        min_surface_temp_k <= max_surface_temp_k
        );

-- Step 4: Drop the old distance columns (they're no longer needed)
ALTER TABLE ref.composition_template
    DROP COLUMN min_distance_au,
    DROP COLUMN max_distance_au;

-- Step 5: Add helpful comments
COMMENT ON COLUMN ref.composition_template.min_surface_temp_k IS 'Minimum surface temperature in Kelvin for this composition to be viable';
COMMENT ON COLUMN ref.composition_template.max_surface_temp_k IS 'Maximum surface temperature in Kelvin for this composition to be viable';