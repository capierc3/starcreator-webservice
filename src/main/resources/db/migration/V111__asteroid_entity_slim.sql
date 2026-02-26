-- ═══════════════════════════════════════════════════════════════
-- V111: Slim asteroid entity — flatten terrain/composition/water,
--       persist SMA directly, drop orbital/moon/notable columns.
--       Reduces per-asteroid DB rows from 8 to 4.
-- ═══════════════════════════════════════════════════════════════

-- ───────────────────────────────────────────────────────────────
-- 1. ADD NEW DIRECT COLUMNS to ud.asteroid
-- ───────────────────────────────────────────────────────────────

-- Terrain (flattened from terrain_properties)
ALTER TABLE ud.asteroid ADD COLUMN surface_features TEXT;
ALTER TABLE ud.asteroid ADD COLUMN cratering_level VARCHAR(20);
ALTER TABLE ud.asteroid ADD COLUMN has_regolith BOOLEAN;
ALTER TABLE ud.asteroid ADD COLUMN regolith_depth_m DECIMAL(8,2);

-- Composition (flattened from composition_properties)
ALTER TABLE ud.asteroid ADD COLUMN composition TEXT;
ALTER TABLE ud.asteroid ADD COLUMN is_differentiated BOOLEAN DEFAULT false;
ALTER TABLE ud.asteroid ADD COLUMN core_type VARCHAR(50);

-- Water/Ice (new — never previously populated for asteroids)
ALTER TABLE ud.asteroid ADD COLUMN ice_percent DECIMAL(5,2);

-- Orbital (only SMA persisted — unique per asteroid, needed for reports)
ALTER TABLE ud.asteroid ADD COLUMN semi_major_axis_au DECIMAL(12,6);

-- ───────────────────────────────────────────────────────────────
-- 2. MIGRATE DATA from child entities into new direct columns
-- ───────────────────────────────────────────────────────────────

-- Terrain → asteroid
UPDATE ud.asteroid a
SET surface_features = tp.surface_features,
    cratering_level  = tp.cratering_level,
    has_regolith     = tp.has_regolith,
    regolith_depth_m = tp.regolith_depth_m
FROM ud.terrain_properties tp
WHERE a.terrain_id = tp.id
  AND a.terrain_id IS NOT NULL;

-- Composition → asteroid
UPDATE ud.asteroid a
SET composition      = cp.composition,
    is_differentiated = cp.is_differentiated,
    core_type        = cp.core_type
FROM ud.composition_properties cp
WHERE a.composition_properties_id = cp.id
  AND a.composition_properties_id IS NOT NULL;

-- Orbital elements → asteroid (SMA only)
UPDATE ud.asteroid a
SET semi_major_axis_au = oe.semi_major_axis
FROM ud.orbital_elements oe
WHERE a.orbital_elements_id = oe.id
  AND a.orbital_elements_id IS NOT NULL;

-- ───────────────────────────────────────────────────────────────
-- 3. CLEAN UP orphaned child rows (asteroid-only)
--    Shared tables: only delete rows referenced by asteroids that
--    are NOT also referenced by planets, moons, or orbital bands.
-- ───────────────────────────────────────────────────────────────

-- Orphaned terrain_properties
DELETE FROM ud.terrain_properties
WHERE id IN (SELECT terrain_id FROM ud.asteroid WHERE terrain_id IS NOT NULL)
  AND id NOT IN (SELECT terrain_id FROM ud.planet WHERE terrain_id IS NOT NULL)
  AND id NOT IN (SELECT terrain_id FROM ud.moon WHERE terrain_id IS NOT NULL);

-- Orphaned composition_properties
DELETE FROM ud.composition_properties
WHERE id IN (SELECT composition_properties_id FROM ud.asteroid WHERE composition_properties_id IS NOT NULL)
  AND id NOT IN (SELECT composition_properties_id FROM ud.planet WHERE composition_properties_id IS NOT NULL)
  AND id NOT IN (SELECT composition_properties_id FROM ud.moon WHERE composition_properties_id IS NOT NULL);

-- Orphaned water_properties (all asteroid water rows are empty, but clean up)
DELETE FROM ud.water_properties
WHERE id IN (SELECT water_id FROM ud.asteroid WHERE water_id IS NOT NULL)
  AND id NOT IN (SELECT water_id FROM ud.planet WHERE water_id IS NOT NULL)
  AND id NOT IN (SELECT water_id FROM ud.moon WHERE water_id IS NOT NULL);

-- Orphaned orbital_elements (also used by band edges)
DELETE FROM ud.orbital_elements
WHERE id IN (SELECT orbital_elements_id FROM ud.asteroid WHERE orbital_elements_id IS NOT NULL)
  AND id NOT IN (SELECT orbital_elements_id FROM ud.planet WHERE orbital_elements_id IS NOT NULL)
  AND id NOT IN (SELECT orbital_elements_id FROM ud.moon WHERE orbital_elements_id IS NOT NULL)
  AND id NOT IN (SELECT inner_orbit_id FROM ud.orbital_band WHERE inner_orbit_id IS NOT NULL)
  AND id NOT IN (SELECT outer_orbit_id FROM ud.orbital_band WHERE outer_orbit_id IS NOT NULL);

-- ───────────────────────────────────────────────────────────────
-- 4. DROP FK COLUMNS and removed direct columns
-- ───────────────────────────────────────────────────────────────

-- FK columns for removed @OneToOne relationships
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS terrain_id;
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS water_id;
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS composition_properties_id;
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS orbital_elements_id;

-- Moon columns (asteroids no longer track moons)
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS has_moon;
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS moon_count;
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS moon_description;

-- is_notable (always true for all asteroids — redundant)
ALTER TABLE ud.asteroid DROP COLUMN IF EXISTS is_notable;
