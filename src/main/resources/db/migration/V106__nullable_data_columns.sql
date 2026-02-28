-- Drop NOT NULL constraints on data columns that can legitimately be null
-- depending on the type of celestial body being modeled.

-- PlanetaryMagneticField: fields can be null for bodies with NONE dynamo type
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ud' AND table_name='planetary_magnetic_field' AND column_name='strength_compared_to_earth') THEN
        EXECUTE 'ALTER TABLE ud.planetary_magnetic_field ALTER COLUMN strength_compared_to_earth DROP NOT NULL';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ud' AND table_name='planetary_magnetic_field' AND column_name='surface_field_microteslas_min') THEN
        EXECUTE 'ALTER TABLE ud.planetary_magnetic_field ALTER COLUMN surface_field_microteslas_min DROP NOT NULL';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ud' AND table_name='planetary_magnetic_field' AND column_name='surface_field_microteslas_max') THEN
        EXECUTE 'ALTER TABLE ud.planetary_magnetic_field ALTER COLUMN surface_field_microteslas_max DROP NOT NULL';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ud' AND table_name='planetary_magnetic_field' AND column_name='surface_field_microteslas_avg') THEN
        EXECUTE 'ALTER TABLE ud.planetary_magnetic_field ALTER COLUMN surface_field_microteslas_avg DROP NOT NULL';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ud' AND table_name='planetary_magnetic_field' AND column_name='variation_pattern') THEN
        EXECUTE 'ALTER TABLE ud.planetary_magnetic_field ALTER COLUMN variation_pattern DROP NOT NULL';
    END IF;
END $$;

-- PlanetaryHabitability: habitability_class can be null during generation
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ud' AND table_name='planetary_habitability' AND column_name='habitability_class') THEN
        EXECUTE 'ALTER TABLE ud.planetary_habitability ALTER COLUMN habitability_class DROP NOT NULL';
    END IF;
END $$;

-- Atmosphere: classification and is_stripped can be null
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ud' AND table_name='atmosphere' AND column_name='classification') THEN
        EXECUTE 'ALTER TABLE ud.atmosphere ALTER COLUMN classification DROP NOT NULL';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ud' AND table_name='atmosphere' AND column_name='is_stripped') THEN
        EXECUTE 'ALTER TABLE ud.atmosphere ALTER COLUMN is_stripped DROP NOT NULL';
    END IF;
END $$;
