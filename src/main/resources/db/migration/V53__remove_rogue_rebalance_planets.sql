-- V45__remove_rogue_rebalance_planets.sql

-- Remove Rogue Planet from normal generation
DELETE FROM ref.planet_type_ref WHERE name = 'Rogue Planet';

-- Also remove from composition templates
DELETE FROM ref.composition_template WHERE name = 'Rogue - Frozen Ejected';

-- Rebalance planet types for more realistic distribution
-- Boost rocky/terrestrial types (currently underrepresented)
UPDATE ref.planet_type_ref SET rarity_weight = 350 WHERE name = 'Super-Earth';        -- was 200
UPDATE ref.planet_type_ref SET rarity_weight = 250 WHERE name = 'Terrestrial Planet'; -- was 200
UPDATE ref.planet_type_ref SET rarity_weight = 180 WHERE name = 'Ocean Planet';       -- was 100

-- Reduce ice/dwarf types (currently overrepresented)
UPDATE ref.planet_type_ref SET rarity_weight = 100 WHERE name = 'Ice World';          -- was 150
UPDATE ref.planet_type_ref SET rarity_weight = 60 WHERE name = 'Dwarf Planet';        -- was 100

-- Reduce gas giant dominance slightly
UPDATE ref.planet_type_ref SET rarity_weight = 250 WHERE name = 'Gas Giant';          -- was 300
UPDATE ref.planet_type_ref SET rarity_weight = 60 WHERE name = 'Super-Jupiter';       -- was 80

-- Slight boost to Sub-Neptune to maintain small planet dominance (realistic)
UPDATE ref.planet_type_ref SET rarity_weight = 220 WHERE name = 'Sub-Neptune';        -- was 200

-- Keep Mini-Neptune competitive with Sub-Neptune
UPDATE ref.planet_type_ref SET rarity_weight = 200 WHERE name = 'Mini-Neptune';       -- was 250