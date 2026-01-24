-- V24__fix_temperature_range_gaps.sql
-- Fix temperature range mismatches between planet_type_ref and composition_template

-- ISSUE 1: Desert Planet has a gap
-- Planet Type: Desert Planet (250-500K)
-- Composition: Desert - Oxidized Silicate (300-500K)
-- Gap: 250-299K is not covered
-- FIX: Lower the composition template min from 300K to 250K

UPDATE ref.composition_template
SET min_surface_temp_k = 250
WHERE name = 'Desert - Oxidized Silicate'
  AND planet_types = 'Desert Planet'
  AND min_surface_temp_k = 300;

-- ISSUE 2: Hot Rocky Planet range too high - would be molten above 1200K
-- Planet Type: Hot Rocky Planet (600-2000K) -- TOO HIGH
-- Should be: Hot Rocky Planet (600-1200K) -- stops at lava threshold
-- Composition: Hot Rocky - Stripped Core needs to match
-- FIX: Adjust both planet type and composition to 600-1200K range

UPDATE ref.planet_type_ref
SET max_formation_temp_k = 1200
WHERE name = 'Hot Rocky Planet'
  AND max_formation_temp_k = 2000;

UPDATE ref.composition_template
SET min_surface_temp_k = 600,
    max_surface_temp_k = 1200
WHERE name = 'Hot Rocky - Stripped Core'
  AND planet_types = 'Hot Rocky Planet';

-- ISSUE 3: Lava Planet - OK as is (1200-3000K matches composition 1200-2500K, extend to 3000K)
UPDATE ref.composition_template
SET max_surface_temp_k = 3000
WHERE name = 'Lava - Molten Surface'
  AND planet_types = 'Lava Planet'
  AND max_surface_temp_k = 2500;

-- ISSUE 4: Iron Planet gap
-- Planet Type: Iron Planet (500-1500K)
-- Composition: Iron - Stripped Core (500-1200K)
-- Gap: 1201-1500K at high end
-- FIX: Extend composition max to 1500K

UPDATE ref.composition_template
SET max_surface_temp_k = 1500
WHERE name = 'Iron - Stripped Core'
  AND planet_types = 'Iron Planet'
  AND max_surface_temp_k = 1200;

-- ISSUE 5: Gas Giant upper range
-- Planet Type: Gas Giant (80-400K)
-- Composition: Gas Giant - Jupiter-like (80-300K)
-- Gap: 301-400K at high end
-- FIX: Extend composition max to 400K

UPDATE ref.composition_template
SET max_surface_temp_k = 400
WHERE name = 'Gas Giant - Jupiter-like'
  AND planet_types = 'Gas Giant'
  AND max_surface_temp_k = 300;

-- ISSUE 6: Super-Jupiter upper range
-- Planet Type: Super-Jupiter (50-600K)
-- Composition: Super-Jupiter - Massive Gas (50-500K)
-- Gap: 501-600K at high end
-- FIX: Extend composition max to 600K

UPDATE ref.composition_template
SET max_surface_temp_k = 600
WHERE name = 'Super-Jupiter - Massive Gas'
  AND planet_types = 'Super-Jupiter'
  AND max_surface_temp_k = 500;

-- ISSUE 7: Dwarf Planet upper range
-- Planet Type: Dwarf Planet (30-150K)
-- Composition: Dwarf - Ice-Rock Mix (30-100K)
-- Gap: 101-150K at high end
-- FIX: Extend composition max to 150K

UPDATE ref.composition_template
SET max_surface_temp_k = 150
WHERE name = 'Dwarf - Ice-Rock Mix'
  AND planet_types = 'Dwarf Planet'
  AND max_surface_temp_k = 100;

-- ISSUE 8: Super-Earth lower range
-- Planet Type: Super-Earth (150-500K)
-- Composition: Super-Earth - Dense Silicate (200-450K)
-- Gap: 150-199K at low end
-- Gap: 451-500K at high end
-- FIX: Extend composition range to match planet type

UPDATE ref.composition_template
SET min_surface_temp_k = 150,
    max_surface_temp_k = 500
WHERE name = 'Super-Earth - Dense Silicate'
  AND planet_types = 'Super-Earth';

-- ISSUE 9: Ice Giant upper range
-- Planet Type: Ice Giant (40-100K)
-- Composition: Ice Giant - Neptune-like (40-80K)
-- Gap: 81-100K at high end
-- FIX: Extend composition max to 100K

UPDATE ref.composition_template
SET max_surface_temp_k = 100
WHERE name = 'Ice Giant - Neptune-like'
  AND planet_types = 'Ice Giant'
  AND max_surface_temp_k = 80;

-- ISSUE 10: Sub-Neptune exact match check
-- Planet Type: Sub-Neptune (40-120K)
-- Composition: Sub-Neptune - Ice Giant (40-100K)
-- Gap: 101-120K at high end
-- FIX: Extend composition max to 120K

UPDATE ref.composition_template
SET max_surface_temp_k = 120
WHERE name = 'Sub-Neptune - Ice Giant'
  AND planet_types = 'Sub-Neptune'
  AND max_surface_temp_k = 100;