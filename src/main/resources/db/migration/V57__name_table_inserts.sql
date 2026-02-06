-- ============================================
-- V57__seed_name_data.sql
-- Seed data for name generation
-- ============================================

-- =====================================================================
-- SECTION 1: Origins
-- =====================================================================

INSERT INTO ref.name_origin_ref (name) VALUES
                                           ('French'),
                                           ('West African'),
                                           ('North African'),
                                           ('Yoruba'),
                                           ('Akan'),
                                           ('Berber'),
                                           ('Franco-African')
ON CONFLICT (name) DO NOTHING;

-- =====================================================================
-- SECTION 2: Regions
-- =====================================================================

INSERT INTO ref.name_region_ref (name) VALUES
                                           ('Europe'),
                                           ('West Africa'),
                                           ('North Africa'),
                                           ('Central Africa')
ON CONFLICT (name) DO NOTHING;

-- =====================================================================
-- SECTION 3: Origin <-> Region mappings
-- =====================================================================

INSERT INTO ref.origin_region_mapping (origin_id, region_id)
SELECT o.id, r.id
FROM ref.name_origin_ref o
         JOIN ref.name_region_ref r ON
    (o.name = 'French' AND r.name = 'Europe') OR
    (o.name IN ('West African', 'Yoruba', 'Akan') AND r.name = 'West Africa') OR
    (o.name IN ('North African', 'Berber') AND r.name = 'North Africa')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 4: French first names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Jean',   TRUE, FALSE, 'male',   10),
                                                                           ('Luc',    TRUE, FALSE, 'male',    8),
                                                                           ('Pierre', TRUE, FALSE, 'male',    9),
                                                                           ('Louis',  TRUE, FALSE, 'male',   10),
                                                                           ('Paul',   TRUE, FALSE, 'male',    8),
                                                                           ('Marie',  TRUE, FALSE, 'female', 10),
                                                                           ('Claire', TRUE, FALSE, 'female',  7),
                                                                           ('Anne',   TRUE, FALSE, 'female',  6),
                                                                           ('Julien', TRUE, FALSE, 'male',    7),
                                                                           ('Alex',   TRUE, FALSE, 'unisex',  6);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'French'
WHERE n.name IN ('Jean','Luc','Pierre','Louis','Paul','Marie','Claire','Anne','Julien','Alex')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 5: African first names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Amadou',  TRUE, FALSE, 'male',   9),
                                                                           ('Kwame',   TRUE, FALSE, 'male',   8),
                                                                           ('Kofi',    TRUE, FALSE, 'male',   7),
                                                                           ('Ibrahim', TRUE, FALSE, 'male',   9),
                                                                           ('Youssef', TRUE, FALSE, 'male',   8),
                                                                           ('Amina',   TRUE, FALSE, 'female', 9),
                                                                           ('Fatou',   TRUE, FALSE, 'female', 8),
                                                                           ('Mariam',  TRUE, FALSE, 'female', 7),
                                                                           ('Sadiq',   TRUE, FALSE, 'male',   6),
                                                                           ('Nia',     TRUE, FALSE, 'female', 6);

-- West African origins
INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'West African'
WHERE n.name IN ('Amadou','Kwame','Kofi','Amina','Fatou','Mariam','Nia')
ON CONFLICT DO NOTHING;

-- North African origins
INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'North African'
WHERE n.name IN ('Ibrahim','Youssef','Sadiq')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 6: French last names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Dubois',  FALSE, TRUE, 'unisex', 10),
                                                                           ('Moreau',  FALSE, TRUE, 'unisex',  8),
                                                                           ('Lefevre', FALSE, TRUE, 'unisex',  7),
                                                                           ('Martin',  FALSE, TRUE, 'unisex', 10),
                                                                           ('Bernard', FALSE, TRUE, 'unisex',  8);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'French'
WHERE n.name IN ('Dubois','Moreau','Lefevre','Martin','Bernard')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 7: African last names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Diallo',   FALSE, TRUE, 'unisex', 10),
                                                                           ('Mensah',   FALSE, TRUE, 'unisex',  9),
                                                                           ('Traore',   FALSE, TRUE, 'unisex',  8),
                                                                           ('Keita',    FALSE, TRUE, 'unisex',  9),
                                                                           ('Benali',   FALSE, TRUE, 'unisex',  7),
                                                                           ('Ouattara', FALSE, TRUE, 'unisex',  8);

-- West African / Akan origins
INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name IN ('West African','Akan')
WHERE n.name IN ('Diallo','Mensah','Traore','Keita','Ouattara')
ON CONFLICT DO NOTHING;

-- North African / Berber origins
INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name IN ('North African','Berber')
WHERE n.name IN ('Benali')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 8: Franco-African crossover names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Ismael', TRUE, FALSE, 'male',   6),
                                                                           ('Malik',  TRUE, FALSE, 'male',   7),
                                                                           ('Nadia',  TRUE, FALSE, 'female', 7);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Franco-African'
WHERE n.name IN ('Ismael','Malik','Nadia')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 9: Verify Migration
-- =====================================================================

DO $$
    DECLARE
        name_count INTEGER;
        origin_count INTEGER;
        region_count INTEGER;
    BEGIN
        SELECT COUNT(*) INTO name_count FROM ref.name_ref;
        SELECT COUNT(*) INTO origin_count FROM ref.name_origin_ref;
        SELECT COUNT(*) INTO region_count FROM ref.name_region_ref;

        RAISE NOTICE 'Migration V57 completed successfully';
        RAISE NOTICE 'Inserted % names, % origins, % regions', name_count, origin_count, region_count;
    END $$;