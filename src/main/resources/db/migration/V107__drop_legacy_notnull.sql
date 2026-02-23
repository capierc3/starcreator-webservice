-- Drop NOT NULL constraints on ALL legacy columns that remain from before
-- the entity refactoring (fields moved to component entities like PhysicalProperties,
-- RotationProperties, Designation, etc.)
-- The ddl-auto=update keeps these columns but the code no longer writes to them directly.

-- This is a blanket approach: for every ud schema table, drop NOT NULL on all
-- non-PK, non-FK columns. Only structural columns (IDs, FKs, timestamps) should
-- be NOT NULL.

DO $$
DECLARE
    rec RECORD;
BEGIN
    FOR rec IN
        SELECT c.table_name, c.column_name
        FROM information_schema.columns c
        JOIN information_schema.tables t
            ON c.table_schema = t.table_schema AND c.table_name = t.table_name
        WHERE c.table_schema = 'ud'
          AND t.table_type = 'BASE TABLE'
          AND c.is_nullable = 'NO'
          AND c.column_name NOT IN ('id')  -- Keep PK not-null
          -- Keep structural FKs not-null
          AND c.column_name NOT LIKE '%_at'  -- timestamps are fine nullable
    LOOP
        BEGIN
            EXECUTE format(
                'ALTER TABLE ud.%I ALTER COLUMN %I DROP NOT NULL',
                rec.table_name, rec.column_name
            );
            RAISE NOTICE 'Dropped NOT NULL on ud.%.%', rec.table_name, rec.column_name;
        EXCEPTION WHEN OTHERS THEN
            RAISE NOTICE 'Could not drop NOT NULL on ud.%.%: %', rec.table_name, rec.column_name, SQLERRM;
        END;
    END LOOP;
END $$;
