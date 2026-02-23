-- Make legacy name columns nullable on all ud tables
-- Names are now stored exclusively in ud.designation.logged_name
-- The legacy name columns are kept only for backward compatibility queries

ALTER TABLE ud.star_system ALTER COLUMN name DROP NOT NULL;
ALTER TABLE ud.star ALTER COLUMN name DROP NOT NULL;

-- Planet and Moon may or may not have a name column depending on migration history
-- Use DO blocks to handle gracefully
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'ud' AND table_name = 'planet' AND column_name = 'name'
    ) THEN
        EXECUTE 'ALTER TABLE ud.planet ALTER COLUMN name DROP NOT NULL';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'ud' AND table_name = 'moon' AND column_name = 'name'
    ) THEN
        EXECUTE 'ALTER TABLE ud.moon ALTER COLUMN name DROP NOT NULL';
    END IF;
END $$;

ALTER TABLE ud.orbital_band ALTER COLUMN name DROP NOT NULL;
ALTER TABLE ud.asteroid ALTER COLUMN name DROP NOT NULL;
