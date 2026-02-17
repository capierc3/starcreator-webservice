-- =====================================================================
-- Flyway Migration: Significant Moons + Additional Moonlets
-- =====================================================================
-- Changes min_moons/max_moons to represent "significant moons" only
-- (roughly Mimas-sized and up — moons worth generating full detail for).
-- Adds additional_moonlets column to planet for the swarm of tiny
-- irregular captured objects that don't need individual entities.
--
-- Rationale: Jupiter has ~8 significant moons but 95 total. Generating
-- 80+ full Moon entities with physics, atmosphere, habitability, and
-- weather is computationally expensive and narratively useless.
-- =====================================================================

-- SECTION 1: Add moonlet count to planet table
ALTER TABLE ud.planet
    ADD COLUMN additional_moonlets INTEGER DEFAULT 0;

COMMENT ON COLUMN ud.planet.additional_moonlets IS
    'Count of tiny irregular moonlets too small for individual generation. '
        'These are sub-Mimas captured objects. The moons list contains only '
        'significant moons with full physics and detail.';

-- SECTION 2: Update planet_type_ref to significant-moon ranges
-- Gas Giant: was 10-80, now 4-15 (Jupiter ~8 significant, Saturn ~8-10)
UPDATE ref.planet_type_ref SET min_moons = 4, max_moons = 15 WHERE name = 'Gas Giant';

-- Super-Jupiter: was 20-100, now 5-20 (more mass = more large moons, but not 100)
UPDATE ref.planet_type_ref SET min_moons = 5, max_moons = 20 WHERE name = 'Super-Jupiter';

-- Ice Giant: was 5-30, now 2-8 (Uranus 5 major, Neptune 1 major + ~6 mid)
UPDATE ref.planet_type_ref SET min_moons = 2, max_moons = 8 WHERE name = 'Ice Giant';

-- Sub-Neptune: was 2-15, now 1-6 (smaller than Ice Giants)
UPDATE ref.planet_type_ref SET min_moons = 1, max_moons = 6 WHERE name = 'Sub-Neptune';

-- Ice World: was 0-10, now 0-3 (KBOs rarely have >1-2)
UPDATE ref.planet_type_ref SET max_moons = 3 WHERE name = 'Ice World';

-- Dwarf Planet: was 0-5, now 0-2 (most moonless, binary pairs common)
UPDATE ref.planet_type_ref SET max_moons = 2 WHERE name = 'Dwarf Planet';

-- Mini-Neptune: was 0-5, now 0-3
UPDATE ref.planet_type_ref SET max_moons = 3 WHERE name = 'Mini-Neptune';

-- Warm Neptune: was 0-5, now 0-3
UPDATE ref.planet_type_ref SET max_moons = 3 WHERE name = 'Warm Neptune';

-- Super-Earth: was 0-4, now 0-3
UPDATE ref.planet_type_ref SET max_moons = 3 WHERE name = 'Super-Earth';

-- Hot Jupiter: was 0-2, now 0-1
UPDATE ref.planet_type_ref SET max_moons = 1 WHERE name = 'Hot Jupiter';

-- Puffy Planet: was 0-3, now 0-1
UPDATE ref.planet_type_ref SET max_moons = 1 WHERE name = 'Puffy Planet';

-- Ocean Planet: was 0-3, now 0-2
UPDATE ref.planet_type_ref SET max_moons = 2 WHERE name = 'Ocean Planet';

-- No changes needed: Hot Neptune (0-1), Terrestrial (0-2),
-- Desert Planet (0-2), Carbon Planet (0-2), Hot Rocky (0-0),
-- Iron Planet (0-0), Lava Planet (0-0), Rogue Planet (0-0)