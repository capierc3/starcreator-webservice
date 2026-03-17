-- V120: Rename liquid_type to volatile_type
-- The field describes the dominant volatile substance, not just the liquid form.

DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'ud' AND table_name = 'hydrology_properties'
               AND column_name = 'liquid_type') THEN
        ALTER TABLE ud.hydrology_properties RENAME COLUMN liquid_type TO volatile_type;
    END IF;
END $$;

DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'ref' AND table_name = 'terrain_type_ref'
               AND column_name = 'liquid_type') THEN
        ALTER TABLE ref.terrain_type_ref RENAME COLUMN liquid_type TO volatile_type;
    END IF;
END $$;
