-- =====================================================================
-- Flyway Migration V37: Atmosphere System
-- =====================================================================
-- Description: Creates atmosphere entity and component tables for storing
--              detailed atmospheric data for both planets and moons using
--              a flexible component-based approach
-- =====================================================================

-- =====================================================================
-- SECTION 1: Create Atmosphere Table
-- =====================================================================

CREATE TABLE ud.atmosphere (
    -- Primary Key
                               id BIGSERIAL PRIMARY KEY,

    -- Classification
                               classification VARCHAR(50) NOT NULL,

    -- Surface Pressure (in bar, where 1 bar = Earth sea level)
                               surface_pressure_bar DOUBLE PRECISION,

    -- Summary composition string (human-readable, for display)
                               composition_summary TEXT,

    -- Atmospheric properties
                               scale_height_km DOUBLE PRECISION,
                               greenhouse_effect_k DOUBLE PRECISION,

    -- Generation metadata
                               is_stripped BOOLEAN DEFAULT FALSE,
                               stripped_reason VARCHAR(100),

    -- Timestamps
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
                               CONSTRAINT chk_atmosphere_pressure_positive CHECK (surface_pressure_bar IS NULL OR surface_pressure_bar >= 0),
                               CONSTRAINT chk_atmosphere_scale_height_positive CHECK (scale_height_km IS NULL OR scale_height_km > 0),
                               CONSTRAINT chk_atmosphere_greenhouse_effect CHECK (greenhouse_effect_k IS NULL OR greenhouse_effect_k >= 0)
);

-- =====================================================================
-- SECTION 2: Create Atmosphere Component Table
-- =====================================================================

CREATE TABLE ud.atmosphere_component (
    -- Primary Key
                                         id BIGSERIAL PRIMARY KEY,

    -- Foreign key to atmosphere
                                         atmosphere_id BIGINT NOT NULL REFERENCES ud.atmosphere(id) ON DELETE CASCADE,

    -- Gas information (uses AtmosphereGas enum formula)
                                         gas_formula VARCHAR(10) NOT NULL,

    -- Percentage of atmosphere (0-100)
                                         percentage DOUBLE PRECISION NOT NULL,

    -- Whether this is a trace amount (< 0.1%)
                                         is_trace BOOLEAN DEFAULT FALSE,

    -- Timestamps
                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
                                         CONSTRAINT chk_atm_component_percentage CHECK (percentage >= 0 AND percentage <= 100),
                                         CONSTRAINT chk_atm_component_gas_not_empty CHECK (gas_formula IS NOT NULL AND gas_formula != '')
);

-- Create indexes for faster lookups
CREATE INDEX idx_atmosphere_component_atmosphere_id ON ud.atmosphere_component(atmosphere_id);
CREATE INDEX idx_atmosphere_component_gas ON ud.atmosphere_component(gas_formula);

-- =====================================================================
-- SECTION 3: Add Foreign Keys to Moon and Planet Tables
-- =====================================================================

-- Add atmosphere FK to Moon table
ALTER TABLE ud.moon
    ADD COLUMN atmosphere_id BIGINT REFERENCES ud.atmosphere(id) ON DELETE SET NULL;

CREATE INDEX idx_moon_atmosphere_id ON ud.moon(atmosphere_id);

-- Add atmosphere FK to Planet table (for future migration)
ALTER TABLE ud.planet
    ADD COLUMN atmosphere_id BIGINT REFERENCES ud.atmosphere(id) ON DELETE SET NULL;

CREATE INDEX idx_planet_atmosphere_id ON ud.planet(atmosphere_id);

-- =====================================================================
-- SECTION 4: Comments for Documentation
-- =====================================================================

COMMENT ON TABLE ud.atmosphere IS
    'Stores atmospheric properties for celestial bodies (planets and moons)';

COMMENT ON COLUMN ud.atmosphere.classification IS
    'Atmosphere type from AtmosphereClassification enum (EARTH_LIKE, VENUS_LIKE, JOVIAN, etc.)';

COMMENT ON COLUMN ud.atmosphere.surface_pressure_bar IS
    'Surface atmospheric pressure in bar (1 bar = Earth sea level, 92 bar = Venus)';

COMMENT ON COLUMN ud.atmosphere.composition_summary IS
    'Human-readable summary of atmospheric composition (e.g., "N2 78%, O2 21%, Ar 1%")';

COMMENT ON COLUMN ud.atmosphere.scale_height_km IS
    'Atmospheric scale height - altitude where pressure drops to 1/e of surface value';

COMMENT ON COLUMN ud.atmosphere.greenhouse_effect_k IS
    'Temperature increase in Kelvin due to greenhouse effect';

COMMENT ON COLUMN ud.atmosphere.is_stripped IS
    'Whether the atmosphere was stripped by stellar wind, magnetic field, or other process';

COMMENT ON COLUMN ud.atmosphere.stripped_reason IS
    'Reason for atmosphere loss (e.g., "PLANETARY_MAGNETIC_FIELD", "STELLAR_WIND", "LOW_MASS")';

COMMENT ON TABLE ud.atmosphere_component IS
    'Stores individual gas components of an atmosphere with their percentages';

COMMENT ON COLUMN ud.atmosphere_component.gas_formula IS
    'Chemical formula matching AtmosphereGas enum (N2, O2, CO2, H2, He, CH4, etc.)';

COMMENT ON COLUMN ud.atmosphere_component.percentage IS
    'Percentage of atmosphere composition (0-100). Sum should be ~100% per atmosphere.';

COMMENT ON COLUMN ud.atmosphere_component.is_trace IS
    'True if this is a trace gas (< 0.1%), often displayed as "(trace)" instead of percentage';

-- =====================================================================
-- SECTION 5: Update Triggers for modified_at
-- =====================================================================

CREATE OR REPLACE FUNCTION update_modified_at_column()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.modified_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_atmosphere_modified_at
    BEFORE UPDATE ON ud.atmosphere
    FOR EACH ROW
EXECUTE FUNCTION update_modified_at_column();

-- =====================================================================
-- SECTION 6: Verify Migration
-- =====================================================================

DO $$
    BEGIN
        RAISE NOTICE 'Migration V37 completed successfully';
        RAISE NOTICE 'Created tables: ud.atmosphere, ud.atmosphere_component';
        RAISE NOTICE 'Added atmosphere_id to: ud.moon, ud.planet';
    END $$;

-- =====================================================================
-- END OF MIGRATION V37
-- =====================================================================