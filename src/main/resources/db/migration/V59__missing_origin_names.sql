-- ============================================
-- V59__missing_origin_names.sql
-- Names for origins that were missing data
-- ============================================

-- =====================================================================
-- SECTION 1: Vietnamese names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Minh',    TRUE, FALSE, 'male',    9),
                                                                           ('Duc',     TRUE, FALSE, 'male',    8),
                                                                           ('Tuan',    TRUE, FALSE, 'male',    8),
                                                                           ('Hung',    TRUE, FALSE, 'male',    7),
                                                                           ('Thanh',   TRUE, FALSE, 'unisex',  8),
                                                                           ('Linh',    TRUE, FALSE, 'female',  9),
                                                                           ('Huong',   TRUE, FALSE, 'female',  8),
                                                                           ('Thao',    TRUE, FALSE, 'female',  7),
                                                                           ('Mai',     TRUE, FALSE, 'female',  8),
                                                                           ('Ngoc',    TRUE, FALSE, 'female',  7);

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Nguyen',  FALSE, TRUE, 'unisex', 10),
                                                                           ('Tran',    FALSE, TRUE, 'unisex',  9),
                                                                           ('Le',      FALSE, TRUE, 'unisex',  9),
                                                                           ('Pham',    FALSE, TRUE, 'unisex',  8),
                                                                           ('Hoang',   FALSE, TRUE, 'unisex',  7),
                                                                           ('Vu',      FALSE, TRUE, 'unisex',  7);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Vietnamese'
WHERE n.name IN ('Minh','Duc','Tuan','Hung','Thanh','Linh','Huong','Thao','Mai','Ngoc',
                 'Nguyen','Tran','Le','Pham','Hoang','Vu')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 2: Korean names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Joon',    TRUE, FALSE, 'male',    9),
                                                                           ('Min-jun', TRUE, FALSE, 'male',    8),
                                                                           ('Seo-jun', TRUE, FALSE, 'male',    8),
                                                                           ('Ji-hoon', TRUE, FALSE, 'male',    7),
                                                                           ('Hyun',    TRUE, FALSE, 'unisex',  7),
                                                                           ('Ji-yeon', TRUE, FALSE, 'female',  9),
                                                                           ('Soo-min', TRUE, FALSE, 'female',  8),
                                                                           ('Yuna',    TRUE, FALSE, 'female',  8),
                                                                           ('Hana',    TRUE, FALSE, 'female',  7),
                                                                           ('Eunji',   TRUE, FALSE, 'female',  7);

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Kim',     FALSE, TRUE, 'unisex', 10),
                                                                           ('Park',    FALSE, TRUE, 'unisex',  9),
                                                                           ('Lee',     FALSE, TRUE, 'unisex', 10),
                                                                           ('Choi',    FALSE, TRUE, 'unisex',  8),
                                                                           ('Jung',    FALSE, TRUE, 'unisex',  7),
                                                                           ('Kang',    FALSE, TRUE, 'unisex',  7);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Korean'
WHERE n.name IN ('Joon','Min-jun','Seo-jun','Ji-hoon','Hyun','Ji-yeon','Soo-min','Yuna','Eunji',
                 'Kim','Park','Lee','Choi','Jung','Kang')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 3: Greek names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Nikolaos', TRUE, FALSE, 'male',    9),
                                                                           ('Dimitris', TRUE, FALSE, 'male',    8),
                                                                           ('Kostas',   TRUE, FALSE, 'male',    8),
                                                                           ('Yannis',   TRUE, FALSE, 'male',    8),
                                                                           ('Stavros',  TRUE, FALSE, 'male',    7),
                                                                           ('Eleni',    TRUE, FALSE, 'female',  9),
                                                                           ('Maria',    TRUE, FALSE, 'female', 10),
                                                                           ('Katerina', TRUE, FALSE, 'female',  8),
                                                                           ('Sofia',    TRUE, FALSE, 'female',  8),
                                                                           ('Athena',   TRUE, FALSE, 'female',  7);

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Papadopoulos', FALSE, TRUE, 'unisex',  9),
                                                                           ('Nikolaidis',   FALSE, TRUE, 'unisex',  8),
                                                                           ('Georgiou',     FALSE, TRUE, 'unisex',  8),
                                                                           ('Konstantinos', FALSE, TRUE, 'unisex',  7),
                                                                           ('Dimitriou',    FALSE, TRUE, 'unisex',  7),
                                                                           ('Alexopoulos',  FALSE, TRUE, 'unisex',  6);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Greek'
WHERE n.name IN ('Nikolaos','Dimitris','Kostas','Yannis','Stavros','Eleni','Katerina','Athena',
                 'Papadopoulos','Nikolaidis','Georgiou','Konstantinos','Dimitriou','Alexopoulos')
ON CONFLICT DO NOTHING;

-- Note: Maria and Sofia already exist from other origins, so we just add the Greek mapping
INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Greek'
WHERE n.name IN ('Maria', 'Sofia') AND n.is_first = TRUE
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 4: Turkish names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Mehmet',  TRUE, FALSE, 'male',    10),
                                                                           ('Mustafa', TRUE, FALSE, 'male',     9),
                                                                           ('Ahmet',   TRUE, FALSE, 'male',     9),
                                                                           ('Ali',     TRUE, FALSE, 'male',     8),
                                                                           ('Emre',    TRUE, FALSE, 'male',     7),
                                                                           ('Ayse',    TRUE, FALSE, 'female',   9),
                                                                           ('Fatma',   TRUE, FALSE, 'female',   8),
                                                                           ('Emine',   TRUE, FALSE, 'female',   7),
                                                                           ('Zeynep',  TRUE, FALSE, 'female',   8),
                                                                           ('Elif',    TRUE, FALSE, 'female',   7);

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Yilmaz',   FALSE, TRUE, 'unisex', 10),
                                                                           ('Kaya',     FALSE, TRUE, 'unisex',  9),
                                                                           ('Demir',    FALSE, TRUE, 'unisex',  8),
                                                                           ('Celik',    FALSE, TRUE, 'unisex',  8),
                                                                           ('Sahin',    FALSE, TRUE, 'unisex',  7),
                                                                           ('Ozturk',   FALSE, TRUE, 'unisex',  8);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Turkish'
WHERE n.name IN ('Mehmet','Mustafa','Ahmet','Ali','Emre','Ayse','Fatma','Emine','Zeynep','Elif',
                 'Yilmaz','Kaya','Demir','Celik','Sahin','Ozturk')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 5: Persian names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Darius',   TRUE, FALSE, 'male',    8),
                                                                           ('Cyrus',    TRUE, FALSE, 'male',    8),
                                                                           ('Reza',     TRUE, FALSE, 'male',    9),
                                                                           ('Amir',     TRUE, FALSE, 'male',    9),
                                                                           ('Farhad',   TRUE, FALSE, 'male',    7),
                                                                           ('Shirin',   TRUE, FALSE, 'female',  8),
                                                                           ('Leila',    TRUE, FALSE, 'female',  8),
                                                                           ('Nazanin',  TRUE, FALSE, 'female',  7),
                                                                           ('Parisa',   TRUE, FALSE, 'female',  7),
                                                                           ('Yasmin',   TRUE, FALSE, 'female',  8);

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Tehrani',   FALSE, TRUE, 'unisex',  8),
                                                                           ('Shirazi',   FALSE, TRUE, 'unisex',  7),
                                                                           ('Isfahani',  FALSE, TRUE, 'unisex',  6),
                                                                           ('Bakhtiari', FALSE, TRUE, 'unisex',  6),
                                                                           ('Rahimi',    FALSE, TRUE, 'unisex',  8),
                                                                           ('Hosseini',  FALSE, TRUE, 'unisex',  8);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Persian'
WHERE n.name IN ('Darius','Cyrus','Reza','Amir','Farhad','Shirin','Leila','Nazanin','Parisa','Yasmin',
                 'Tehrani','Shirazi','Isfahani','Bakhtiari','Rahimi','Hosseini')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 6: Polish names (also missing)
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Jakub',    TRUE, FALSE, 'male',    9),
                                                                           ('Mateusz',  TRUE, FALSE, 'male',    8),
                                                                           ('Kacper',   TRUE, FALSE, 'male',    7),
                                                                           ('Piotr',    TRUE, FALSE, 'male',    8),
                                                                           ('Tomasz',   TRUE, FALSE, 'male',    7),
                                                                           ('Anna',     TRUE, FALSE, 'female',  9),
                                                                           ('Zofia',    TRUE, FALSE, 'female',  8),
                                                                           ('Maja',     TRUE, FALSE, 'female',  7),
                                                                           ('Kasia',    TRUE, FALSE, 'female',  7),
                                                                           ('Agnieszka',TRUE, FALSE, 'female',  7);

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Kowalski',   FALSE, TRUE, 'unisex', 10),
                                                                           ('Nowak',      FALSE, TRUE, 'unisex', 10),
                                                                           ('Wisniewski', FALSE, TRUE, 'unisex',  8),
                                                                           ('Wojcik',     FALSE, TRUE, 'unisex',  7),
                                                                           ('Kowalczyk',  FALSE, TRUE, 'unisex',  8),
                                                                           ('Kaminski',   FALSE, TRUE, 'unisex',  7);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Polish'
WHERE n.name IN ('Jakub','Mateusz','Kacper','Piotr','Tomasz','Anna','Zofia','Maja','Kasia','Agnieszka',
                 'Kowalski','Nowak','Wisniewski','Wojcik','Kowalczyk','Kaminski')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 7: Dutch names (also missing)
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Jan',      TRUE, FALSE, 'male',    9),
                                                                           ('Pieter',   TRUE, FALSE, 'male',    8),
                                                                           ('Willem',   TRUE, FALSE, 'male',    8),
                                                                           ('Hendrik',  TRUE, FALSE, 'male',    7),
                                                                           ('Joost',    TRUE, FALSE, 'male',    6),
                                                                           ('Emma',     TRUE, FALSE, 'female',  9),
                                                                           ('Sophie',   TRUE, FALSE, 'female',  8),
                                                                           ('Julia',    TRUE, FALSE, 'female',  8),
                                                                           ('Lotte',    TRUE, FALSE, 'female',  7),
                                                                           ('Fleur',    TRUE, FALSE, 'female',  6);

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('De Vries',  FALSE, TRUE, 'unisex', 10),
                                                                           ('Van Dijk',  FALSE, TRUE, 'unisex',  9),
                                                                           ('Bakker',    FALSE, TRUE, 'unisex',  8),
                                                                           ('Jansen',    FALSE, TRUE, 'unisex',  9),
                                                                           ('Visser',    FALSE, TRUE, 'unisex',  7),
                                                                           ('Smit',      FALSE, TRUE, 'unisex',  8);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Dutch'
WHERE n.name IN ('Jan','Pieter','Willem','Hendrik','Joost','Emma','Sophie','Julia','Lotte','Fleur',
                 'De Vries','Van Dijk','Bakker','Jansen','Visser','Smit')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 8: Verify Migration
-- =====================================================================

DO $$
    DECLARE
        name_count INTEGER;
        first_count INTEGER;
        last_count INTEGER;
    BEGIN
        SELECT COUNT(*) INTO name_count FROM ref.name_ref;
        SELECT COUNT(*) INTO first_count FROM ref.name_ref WHERE is_first = TRUE;
        SELECT COUNT(*) INTO last_count FROM ref.name_ref WHERE is_last = TRUE;

        RAISE NOTICE 'Migration V59 completed successfully';
        RAISE NOTICE 'Total names: % (% first, % last)', name_count, first_count, last_count;
    END $$;