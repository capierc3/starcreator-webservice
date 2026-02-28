-- Make FK columns nullable on unidirectional @OneToMany child tables
-- Hibernate inserts children first with NULL FK, then updates it.
-- This is the standard JPA behavior for unidirectional @OneToMany + @JoinColumn.

-- PlanetaryClimate children (7 tables)
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ud' AND table_name='climate_zone' AND column_name='climate_id') THEN
        EXECUTE 'ALTER TABLE ud.climate_zone ALTER COLUMN climate_id DROP NOT NULL';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ud' AND table_name='cloud_layer' AND column_name='climate_id') THEN
        EXECUTE 'ALTER TABLE ud.cloud_layer ALTER COLUMN climate_id DROP NOT NULL';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ud' AND table_name='precipitation_type' AND column_name='climate_id') THEN
        EXECUTE 'ALTER TABLE ud.precipitation_type ALTER COLUMN climate_id DROP NOT NULL';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ud' AND table_name='extreme_climate_event' AND column_name='climate_id') THEN
        EXECUTE 'ALTER TABLE ud.extreme_climate_event ALTER COLUMN climate_id DROP NOT NULL';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ud' AND table_name='moon_sky_appearance' AND column_name='climate_id') THEN
        EXECUTE 'ALTER TABLE ud.moon_sky_appearance ALTER COLUMN climate_id DROP NOT NULL';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ud' AND table_name='eclipse_data' AND column_name='climate_id') THEN
        EXECUTE 'ALTER TABLE ud.eclipse_data ALTER COLUMN climate_id DROP NOT NULL';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ud' AND table_name='climate_hazard' AND column_name='climate_id') THEN
        EXECUTE 'ALTER TABLE ud.climate_hazard ALTER COLUMN climate_id DROP NOT NULL';
    END IF;
END $$;
