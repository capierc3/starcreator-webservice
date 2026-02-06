-- ============================================
-- V58__additional_name_origins.sql
-- Additional cultural origins for name generation
-- ============================================

-- =====================================================================
-- SECTION 1: Additional Origins
-- =====================================================================

INSERT INTO ref.name_origin_ref (name) VALUES
                                           ('English'),
                                           ('Germanic'),
                                           ('Slavic'),
                                           ('Russian'),
                                           ('Polish'),
                                           ('Spanish'),
                                           ('Portuguese'),
                                           ('Latin American'),
                                           ('Japanese'),
                                           ('Chinese'),
                                           ('Korean'),
                                           ('Vietnamese'),
                                           ('Indian'),
                                           ('Arabic'),
                                           ('Persian'),
                                           ('Turkish'),
                                           ('Greek'),
                                           ('Italian'),
                                           ('Irish'),
                                           ('Scottish'),
                                           ('Scandinavian'),
                                           ('Dutch')
ON CONFLICT (name) DO NOTHING;

-- =====================================================================
-- SECTION 2: Additional Regions
-- =====================================================================

INSERT INTO ref.name_region_ref (name) VALUES
                                           ('East Asia'),
                                           ('South Asia'),
                                           ('Southeast Asia'),
                                           ('Middle East'),
                                           ('Latin America'),
                                           ('Eastern Europe'),
                                           ('Northern Europe'),
                                           ('Southern Europe'),
                                           ('British Isles')
ON CONFLICT (name) DO NOTHING;

-- =====================================================================
-- SECTION 3: Origin <-> Region mappings
-- =====================================================================

INSERT INTO ref.origin_region_mapping (origin_id, region_id)
SELECT o.id, r.id
FROM ref.name_origin_ref o
         JOIN ref.name_region_ref r ON
    -- British Isles
    (o.name IN ('English', 'Irish', 'Scottish') AND r.name = 'British Isles') OR
        -- Northern Europe
    (o.name IN ('Germanic', 'Dutch', 'Scandinavian') AND r.name = 'Northern Europe') OR
        -- Eastern Europe
    (o.name IN ('Slavic', 'Russian', 'Polish') AND r.name = 'Eastern Europe') OR
        -- Southern Europe
    (o.name IN ('Spanish', 'Portuguese', 'Italian', 'Greek') AND r.name = 'Southern Europe') OR
        -- Latin America
    (o.name IN ('Latin American', 'Spanish', 'Portuguese') AND r.name = 'Latin America') OR
        -- East Asia
    (o.name IN ('Japanese', 'Chinese', 'Korean') AND r.name = 'East Asia') OR
        -- Southeast Asia
    (o.name = 'Vietnamese' AND r.name = 'Southeast Asia') OR
        -- South Asia
    (o.name = 'Indian' AND r.name = 'South Asia') OR
        -- Middle East
    (o.name IN ('Arabic', 'Persian', 'Turkish') AND r.name = 'Middle East')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 4: English/British first names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('James',    TRUE, FALSE, 'male',   10),
                                                                           ('William',  TRUE, FALSE, 'male',   10),
                                                                           ('Thomas',   TRUE, FALSE, 'male',    9),
                                                                           ('Edward',   TRUE, FALSE, 'male',    8),
                                                                           ('Henry',    TRUE, FALSE, 'male',    8),
                                                                           ('George',   TRUE, FALSE, 'male',    7),
                                                                           ('Elizabeth',TRUE, FALSE, 'female', 10),
                                                                           ('Margaret', TRUE, FALSE, 'female',  8),
                                                                           ('Catherine',TRUE, FALSE, 'female',  8),
                                                                           ('Victoria', TRUE, FALSE, 'female',  7),
                                                                           ('Charlotte',TRUE, FALSE, 'female',  7),
                                                                           ('Oliver',   TRUE, FALSE, 'male',    9);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'English'
WHERE n.name IN ('James','William','Thomas','Edward','Henry','George','Elizabeth','Margaret','Catherine','Victoria','Charlotte','Oliver')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 5: English/British last names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Smith',    FALSE, TRUE, 'unisex', 10),
                                                                           ('Jones',    FALSE, TRUE, 'unisex',  9),
                                                                           ('Williams', FALSE, TRUE, 'unisex',  9),
                                                                           ('Brown',    FALSE, TRUE, 'unisex',  9),
                                                                           ('Taylor',   FALSE, TRUE, 'unisex',  8),
                                                                           ('Davies',   FALSE, TRUE, 'unisex',  7),
                                                                           ('Wilson',   FALSE, TRUE, 'unisex',  8),
                                                                           ('Evans',    FALSE, TRUE, 'unisex',  7);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'English'
WHERE n.name IN ('Smith','Jones','Williams','Brown','Taylor','Davies','Wilson','Evans')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 6: Spanish/Latin American first names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Carlos',   TRUE, FALSE, 'male',   10),
                                                                           ('Miguel',   TRUE, FALSE, 'male',    9),
                                                                           ('Antonio',  TRUE, FALSE, 'male',    9),
                                                                           ('Jose',     TRUE, FALSE, 'male',   10),
                                                                           ('Diego',    TRUE, FALSE, 'male',    8),
                                                                           ('Rafael',   TRUE, FALSE, 'male',    7),
                                                                           ('Sofia',    TRUE, FALSE, 'female', 10),
                                                                           ('Isabella', TRUE, FALSE, 'female',  9),
                                                                           ('Carmen',   TRUE, FALSE, 'female',  8),
                                                                           ('Elena',    TRUE, FALSE, 'female',  8),
                                                                           ('Lucia',    TRUE, FALSE, 'female',  9),
                                                                           ('Valentina',TRUE, FALSE, 'female',  8);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name IN ('Spanish', 'Latin American')
WHERE n.name IN ('Carlos','Miguel','Antonio','Jose','Diego','Rafael','Sofia','Isabella','Carmen','Elena','Lucia','Valentina')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 7: Spanish/Latin American last names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Garcia',    FALSE, TRUE, 'unisex', 10),
                                                                           ('Rodriguez', FALSE, TRUE, 'unisex', 10),
                                                                           ('Martinez',  FALSE, TRUE, 'unisex',  9),
                                                                           ('Lopez',     FALSE, TRUE, 'unisex',  9),
                                                                           ('Hernandez', FALSE, TRUE, 'unisex',  8),
                                                                           ('Gonzalez',  FALSE, TRUE, 'unisex',  9),
                                                                           ('Perez',     FALSE, TRUE, 'unisex',  8),
                                                                           ('Sanchez',   FALSE, TRUE, 'unisex',  8);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name IN ('Spanish', 'Latin American')
WHERE n.name IN ('Garcia','Rodriguez','Martinez','Lopez','Hernandez','Gonzalez','Perez','Sanchez')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 8: Russian/Slavic first names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Dmitri',   TRUE, FALSE, 'male',    9),
                                                                           ('Ivan',     TRUE, FALSE, 'male',   10),
                                                                           ('Alexei',   TRUE, FALSE, 'male',    8),
                                                                           ('Nikolai',  TRUE, FALSE, 'male',    8),
                                                                           ('Sergei',   TRUE, FALSE, 'male',    7),
                                                                           ('Viktor',   TRUE, FALSE, 'male',    7),
                                                                           ('Anastasia',TRUE, FALSE, 'female',  9),
                                                                           ('Natasha',  TRUE, FALSE, 'female',  8),
                                                                           ('Katya',    TRUE, FALSE, 'female',  7),
                                                                           ('Olga',     TRUE, FALSE, 'female',  7),
                                                                           ('Irina',    TRUE, FALSE, 'female',  7),
                                                                           ('Svetlana', TRUE, FALSE, 'female',  6);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name IN ('Russian', 'Slavic')
WHERE n.name IN ('Dmitri','Ivan','Alexei','Nikolai','Sergei','Viktor','Anastasia','Natasha','Katya','Olga','Irina','Svetlana')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 9: Russian/Slavic last names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Volkov',    FALSE, TRUE, 'unisex',  8),
                                                                           ('Petrov',    FALSE, TRUE, 'unisex',  9),
                                                                           ('Ivanov',    FALSE, TRUE, 'unisex', 10),
                                                                           ('Sokolov',   FALSE, TRUE, 'unisex',  7),
                                                                           ('Kuznetsov', FALSE, TRUE, 'unisex',  8),
                                                                           ('Popov',     FALSE, TRUE, 'unisex',  7);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name IN ('Russian', 'Slavic')
WHERE n.name IN ('Volkov','Petrov','Ivanov','Sokolov','Kuznetsov','Popov')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 10: Japanese first names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Hiroshi',  TRUE, FALSE, 'male',    9),
                                                                           ('Kenji',    TRUE, FALSE, 'male',    8),
                                                                           ('Takeshi',  TRUE, FALSE, 'male',    8),
                                                                           ('Yuki',     TRUE, FALSE, 'unisex',  9),
                                                                           ('Akira',    TRUE, FALSE, 'unisex',  8),
                                                                           ('Ren',      TRUE, FALSE, 'male',    7),
                                                                           ('Sakura',   TRUE, FALSE, 'female',  9),
                                                                           ('Yumi',     TRUE, FALSE, 'female',  8),
                                                                           ('Hana',     TRUE, FALSE, 'female',  8),
                                                                           ('Aiko',     TRUE, FALSE, 'female',  7),
                                                                           ('Mei',      TRUE, FALSE, 'female',  7),
                                                                           ('Haruki',   TRUE, FALSE, 'male',    7);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Japanese'
WHERE n.name IN ('Hiroshi','Kenji','Takeshi','Yuki','Akira','Ren','Sakura','Yumi','Hana','Aiko','Mei','Haruki')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 11: Japanese last names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Tanaka',    FALSE, TRUE, 'unisex', 10),
                                                                           ('Yamamoto',  FALSE, TRUE, 'unisex',  9),
                                                                           ('Watanabe',  FALSE, TRUE, 'unisex',  9),
                                                                           ('Suzuki',    FALSE, TRUE, 'unisex', 10),
                                                                           ('Takahashi', FALSE, TRUE, 'unisex',  8),
                                                                           ('Nakamura',  FALSE, TRUE, 'unisex',  8),
                                                                           ('Kobayashi', FALSE, TRUE, 'unisex',  7),
                                                                           ('Sato',      FALSE, TRUE, 'unisex', 10);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Japanese'
WHERE n.name IN ('Tanaka','Yamamoto','Watanabe','Suzuki','Takahashi','Nakamura','Kobayashi','Sato')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 12: Chinese first names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Wei',      TRUE, FALSE, 'unisex',  9),
                                                                           ('Jun',      TRUE, FALSE, 'unisex',  8),
                                                                           ('Ming',     TRUE, FALSE, 'unisex',  8),
                                                                           ('Jian',     TRUE, FALSE, 'male',    7),
                                                                           ('Lei',      TRUE, FALSE, 'male',    7),
                                                                           ('Fang',     TRUE, FALSE, 'female',  7),
                                                                           ('Xiu',      TRUE, FALSE, 'female',  6),
                                                                           ('Lan',      TRUE, FALSE, 'female',  7),
                                                                           ('Hong',     TRUE, FALSE, 'unisex',  6),
                                                                           ('Chao',     TRUE, FALSE, 'male',    6);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Chinese'
WHERE n.name IN ('Wei','Jun','Ming','Jian','Lei','Fang','Xiu','Lan','Hong','Chao')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 13: Chinese last names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Wang',     FALSE, TRUE, 'unisex', 10),
                                                                           ('Li',       FALSE, TRUE, 'unisex', 10),
                                                                           ('Zhang',    FALSE, TRUE, 'unisex', 10),
                                                                           ('Liu',      FALSE, TRUE, 'unisex',  9),
                                                                           ('Chen',     FALSE, TRUE, 'unisex',  9),
                                                                           ('Yang',     FALSE, TRUE, 'unisex',  8),
                                                                           ('Huang',    FALSE, TRUE, 'unisex',  8),
                                                                           ('Wu',       FALSE, TRUE, 'unisex',  7);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Chinese'
WHERE n.name IN ('Wang','Li','Zhang','Liu','Chen','Yang','Huang','Wu')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 14: Indian first names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Raj',      TRUE, FALSE, 'male',    9),
                                                                           ('Vikram',   TRUE, FALSE, 'male',    8),
                                                                           ('Arjun',    TRUE, FALSE, 'male',    9),
                                                                           ('Sanjay',   TRUE, FALSE, 'male',    7),
                                                                           ('Anil',     TRUE, FALSE, 'male',    7),
                                                                           ('Deepak',   TRUE, FALSE, 'male',    6),
                                                                           ('Priya',    TRUE, FALSE, 'female',  9),
                                                                           ('Anita',    TRUE, FALSE, 'female',  8),
                                                                           ('Sunita',   TRUE, FALSE, 'female',  7),
                                                                           ('Kavita',   TRUE, FALSE, 'female',  7),
                                                                           ('Neha',     TRUE, FALSE, 'female',  8),
                                                                           ('Pooja',    TRUE, FALSE, 'female',  7);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Indian'
WHERE n.name IN ('Raj','Vikram','Arjun','Sanjay','Anil','Deepak','Priya','Anita','Sunita','Kavita','Neha','Pooja')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 15: Indian last names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Patel',    FALSE, TRUE, 'unisex', 10),
                                                                           ('Sharma',   FALSE, TRUE, 'unisex', 10),
                                                                           ('Singh',    FALSE, TRUE, 'unisex', 10),
                                                                           ('Kumar',    FALSE, TRUE, 'unisex',  9),
                                                                           ('Gupta',    FALSE, TRUE, 'unisex',  8),
                                                                           ('Reddy',    FALSE, TRUE, 'unisex',  7),
                                                                           ('Rao',      FALSE, TRUE, 'unisex',  7),
                                                                           ('Verma',    FALSE, TRUE, 'unisex',  7);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Indian'
WHERE n.name IN ('Patel','Sharma','Singh','Kumar','Gupta','Reddy','Rao','Verma')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 16: Arabic/Middle Eastern first names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Mohammed', TRUE, FALSE, 'male',   10),
                                                                           ('Ahmed',    TRUE, FALSE, 'male',    9),
                                                                           ('Hassan',   TRUE, FALSE, 'male',    8),
                                                                           ('Omar',     TRUE, FALSE, 'male',    8),
                                                                           ('Khalid',   TRUE, FALSE, 'male',    7),
                                                                           ('Tariq',    TRUE, FALSE, 'male',    7),
                                                                           ('Fatima',   TRUE, FALSE, 'female',  9),
                                                                           ('Aisha',    TRUE, FALSE, 'female',  8),
                                                                           ('Layla',    TRUE, FALSE, 'female',  8),
                                                                           ('Zahra',    TRUE, FALSE, 'female',  7),
                                                                           ('Noor',     TRUE, FALSE, 'female',  7),
                                                                           ('Sara',     TRUE, FALSE, 'female',  8);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Arabic'
WHERE n.name IN ('Mohammed','Ahmed','Hassan','Omar','Khalid','Tariq','Fatima','Aisha','Layla','Zahra','Noor','Sara')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 17: Arabic/Middle Eastern last names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Al-Rashid', FALSE, TRUE, 'unisex',  8),
                                                                           ('Al-Hassan', FALSE, TRUE, 'unisex',  8),
                                                                           ('Al-Farsi',  FALSE, TRUE, 'unisex',  7),
                                                                           ('Mansour',   FALSE, TRUE, 'unisex',  7),
                                                                           ('Nasser',    FALSE, TRUE, 'unisex',  7),
                                                                           ('Bakir',     FALSE, TRUE, 'unisex',  6);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Arabic'
WHERE n.name IN ('Al-Rashid','Al-Hassan','Al-Farsi','Mansour','Nasser','Bakir')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 18: Germanic/Scandinavian first names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Erik',     TRUE, FALSE, 'male',    9),
                                                                           ('Lars',     TRUE, FALSE, 'male',    8),
                                                                           ('Karl',     TRUE, FALSE, 'male',    8),
                                                                           ('Hans',     TRUE, FALSE, 'male',    7),
                                                                           ('Bjorn',    TRUE, FALSE, 'male',    7),
                                                                           ('Gunnar',   TRUE, FALSE, 'male',    6),
                                                                           ('Ingrid',   TRUE, FALSE, 'female',  8),
                                                                           ('Freya',    TRUE, FALSE, 'female',  8),
                                                                           ('Astrid',   TRUE, FALSE, 'female',  7),
                                                                           ('Helga',    TRUE, FALSE, 'female',  6),
                                                                           ('Sigrid',   TRUE, FALSE, 'female',  6),
                                                                           ('Greta',    TRUE, FALSE, 'female',  7);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name IN ('Germanic', 'Scandinavian')
WHERE n.name IN ('Erik','Lars','Karl','Hans','Bjorn','Gunnar','Ingrid','Freya','Astrid','Helga','Sigrid','Greta')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 19: Germanic/Scandinavian last names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Lindqvist', FALSE, TRUE, 'unisex',  7),
                                                                           ('Eriksson',  FALSE, TRUE, 'unisex',  8),
                                                                           ('Johansson', FALSE, TRUE, 'unisex',  8),
                                                                           ('Larsson',   FALSE, TRUE, 'unisex',  7),
                                                                           ('Mueller',   FALSE, TRUE, 'unisex',  9),
                                                                           ('Schmidt',   FALSE, TRUE, 'unisex',  9),
                                                                           ('Weber',     FALSE, TRUE, 'unisex',  7),
                                                                           ('Fischer',   FALSE, TRUE, 'unisex',  7);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Scandinavian'
WHERE n.name IN ('Lindqvist','Eriksson','Johansson','Larsson')
ON CONFLICT DO NOTHING;

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Germanic'
WHERE n.name IN ('Mueller','Schmidt','Weber','Fischer')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 20: Italian first names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Marco',    TRUE, FALSE, 'male',    9),
                                                                           ('Giuseppe', TRUE, FALSE, 'male',    8),
                                                                           ('Lorenzo',  TRUE, FALSE, 'male',    8),
                                                                           ('Matteo',   TRUE, FALSE, 'male',    8),
                                                                           ('Luca',     TRUE, FALSE, 'male',    9),
                                                                           ('Giovanni', TRUE, FALSE, 'male',    7),
                                                                           ('Giulia',   TRUE, FALSE, 'female',  9),
                                                                           ('Francesca',TRUE, FALSE, 'female',  8),
                                                                           ('Chiara',   TRUE, FALSE, 'female',  7),
                                                                           ('Alessia',  TRUE, FALSE, 'female',  7),
                                                                           ('Beatrice', TRUE, FALSE, 'female',  7),
                                                                           ('Valentina',TRUE, FALSE, 'female',  8);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Italian'
WHERE n.name IN ('Marco','Giuseppe','Lorenzo','Matteo','Luca','Giovanni','Giulia','Francesca','Chiara','Alessia','Beatrice')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 21: Italian last names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Rossi',    FALSE, TRUE, 'unisex', 10),
                                                                           ('Russo',    FALSE, TRUE, 'unisex',  9),
                                                                           ('Ferrari',  FALSE, TRUE, 'unisex',  8),
                                                                           ('Esposito', FALSE, TRUE, 'unisex',  7),
                                                                           ('Bianchi',  FALSE, TRUE, 'unisex',  8),
                                                                           ('Romano',   FALSE, TRUE, 'unisex',  7),
                                                                           ('Colombo',  FALSE, TRUE, 'unisex',  6),
                                                                           ('Ricci',    FALSE, TRUE, 'unisex',  7);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Italian'
WHERE n.name IN ('Rossi','Russo','Ferrari','Esposito','Bianchi','Romano','Colombo','Ricci')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 22: Irish/Scottish first names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Sean',     TRUE, FALSE, 'male',    9),
                                                                           ('Connor',   TRUE, FALSE, 'male',    8),
                                                                           ('Liam',     TRUE, FALSE, 'male',   10),
                                                                           ('Declan',   TRUE, FALSE, 'male',    7),
                                                                           ('Finn',     TRUE, FALSE, 'male',    8),
                                                                           ('Angus',    TRUE, FALSE, 'male',    6),
                                                                           ('Siobhan',  TRUE, FALSE, 'female',  7),
                                                                           ('Maeve',    TRUE, FALSE, 'female',  7),
                                                                           ('Aoife',    TRUE, FALSE, 'female',  6),
                                                                           ('Fiona',    TRUE, FALSE, 'female',  8),
                                                                           ('Moira',    TRUE, FALSE, 'female',  6),
                                                                           ('Bridget',  TRUE, FALSE, 'female',  7);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name IN ('Irish', 'Scottish')
WHERE n.name IN ('Sean','Connor','Liam','Declan','Finn','Angus','Siobhan','Maeve','Aoife','Fiona','Moira','Bridget')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 23: Irish/Scottish last names
-- =====================================================================

INSERT INTO ref.name_ref (name, is_first, is_last, gender, popularity) VALUES
                                                                           ('Murphy',    FALSE, TRUE, 'unisex', 10),
                                                                           ('Kelly',     FALSE, TRUE, 'unisex',  9),
                                                                           ('Sullivan',  FALSE, TRUE, 'unisex',  8),
                                                                           ('Walsh',     FALSE, TRUE, 'unisex',  7),
                                                                           ('MacLeod',   FALSE, TRUE, 'unisex',  7),
                                                                           ('Campbell',  FALSE, TRUE, 'unisex',  8),
                                                                           ('MacDonald', FALSE, TRUE, 'unisex',  8),
                                                                           ('Stewart',   FALSE, TRUE, 'unisex',  7);

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Irish'
WHERE n.name IN ('Murphy','Kelly','Sullivan','Walsh')
ON CONFLICT DO NOTHING;

INSERT INTO ref.name_origin_mapping (name_id, origin_id)
SELECT n.id, o.id
FROM ref.name_ref n
         JOIN ref.name_origin_ref o ON o.name = 'Scottish'
WHERE n.name IN ('MacLeod','Campbell','MacDonald','Stewart')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- SECTION 24: Verify Migration
-- =====================================================================

DO $$
    DECLARE
        name_count INTEGER;
        origin_count INTEGER;
        region_count INTEGER;
        first_count INTEGER;
        last_count INTEGER;
    BEGIN
        SELECT COUNT(*) INTO name_count FROM ref.name_ref;
        SELECT COUNT(*) INTO origin_count FROM ref.name_origin_ref;
        SELECT COUNT(*) INTO region_count FROM ref.name_region_ref;
        SELECT COUNT(*) INTO first_count FROM ref.name_ref WHERE is_first = TRUE;
        SELECT COUNT(*) INTO last_count FROM ref.name_ref WHERE is_last = TRUE;

        RAISE NOTICE 'Migration V58 completed successfully';
        RAISE NOTICE 'Total names: % (% first, % last)', name_count, first_count, last_count;
        RAISE NOTICE 'Total origins: %, regions: %', origin_count, region_count;
    END $$;