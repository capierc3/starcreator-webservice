-- V44__rebalance_star_types_add_brown_dwarfs.sql

-- Remove redundant Red Dwarf (essentially duplicate of Main Sequence M)
DELETE FROM ref.star_type WHERE id = 14;

-- Update Main Sequence M description
UPDATE ref.star_type
SET description = 'Red dwarf stars (M-class), most common stellar type'
WHERE id = 7;

-- Rebalance M-type weight now that duplicate is removed
UPDATE ref.star_type SET rarity_weight = 7300 WHERE name = 'Main Sequence M';

-- Add Brown Dwarf types (L, T, Y spectral classes)
INSERT INTO ref.star_type (id, name, spectral_class, min_mass, max_mass, mass_radius_exponent,
                           radius_multiplier_min, radius_multiplier_max, description, rarity_weight, type)
VALUES
    (15, 'Brown Dwarf L', 'L', 0.05, 0.08, 0.08, NULL, NULL,
     'Warm brown dwarf with lithium and metal hydrides in atmosphere', 600, NULL),
    (16, 'Brown Dwarf T', 'T', 0.02, 0.05, 0.08, NULL, NULL,
     'Cool methane brown dwarf with characteristic blue tinge', 500, NULL),
    (17, 'Brown Dwarf Y', 'Y', 0.013, 0.02, 0.08, NULL, NULL,
     'Coldest brown dwarfs with ammonia clouds, near planetary temperatures', 400, NULL);

-- Reset sequence
SELECT setval('ref.star_type_id_seq', (SELECT MAX(id) FROM ref.star_type));