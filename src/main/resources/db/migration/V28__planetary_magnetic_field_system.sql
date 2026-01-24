-- =====================================================================
-- Flyway Migration V28: Planetary Magnetic Field System
-- =====================================================================
-- Description: Comprehensive magnetic field modeling with dynamo theory,
--              field variations, reversals, and scientific accuracy
-- Author: Chase
-- Date: 2026-01-21
-- =====================================================================

-- =====================================================================
-- SECTION 1: Create Planetary Magnetic Field Table
-- =====================================================================

CREATE TABLE IF NOT EXISTS ud.planetary_magnetic_field (
                                                           id BIGSERIAL PRIMARY KEY,
                                                           planet_id BIGINT UNIQUE,  -- Nullable until planet is saved to DB

    -- ================================================================
    -- FIELD STRENGTH (Relative to Earth's ~50 µT surface field)
    -- ================================================================
                                                           strength_compared_to_earth DOUBLE PRECISION NOT NULL,
                                                           surface_field_microteslas_min DOUBLE PRECISION NOT NULL,
                                                           surface_field_microteslas_max DOUBLE PRECISION NOT NULL,
                                                           surface_field_microteslas_avg DOUBLE PRECISION NOT NULL,

    -- ================================================================
    -- DYNAMO CHARACTERISTICS (What generates the field)
    -- ================================================================
                                                           dynamo_type VARCHAR(50),  -- CORE_DYNAMO, METALLIC_HYDROGEN, REMNANT, INDUCED, NONE
                                                           dynamo_efficiency DOUBLE PRECISION, -- 0.0 to 1.0, how efficient the dynamo is
                                                           core_convection_intensity VARCHAR(50), -- NONE, WEAK, MODERATE, STRONG, EXTREME

    -- ================================================================
    -- FIELD GEOMETRY (Shape and structure)
    -- ================================================================
                                                           field_geometry VARCHAR(50), -- DIPOLE, QUADRUPOLE, MULTIPOLE, CHAOTIC, COMPRESSED
                                                           dipole_tilt_degrees DOUBLE PRECISION, -- Angle between magnetic and rotation axes
                                                           magnetic_axis_offset_km DOUBLE PRECISION, -- Distance from geometric center

    -- ================================================================
    -- SPATIAL VARIATION (How field strength varies across surface)
    -- ================================================================
                                                           variation_pattern VARCHAR(100) NOT NULL,
    -- NO_REGIONAL_VARIANCE, HIGHER_AT_NORTH_POLE, HIGHER_AT_SOUTH_POLE,
    -- HIGHER_AT_BOTH_POLES, HIGHER_AT_EQUATOR, HIGHER_IN_RANDOM_SPOTS,
    -- BANDED_ANOMALIES, CRUSTAL_ANOMALIES

                                                           pole_field_strength_multiplier DOUBLE PRECISION, -- Multiplier at poles vs equator
                                                           equatorial_field_strength_multiplier DOUBLE PRECISION,

    -- ================================================================
    -- TEMPORAL VARIATION (How field changes over time)
    -- ================================================================
                                                           temporal_stability VARCHAR(50), -- STABLE, FLUXING, UNSTABLE, REVERSING, DECAYING

    -- For FLUXING fields
                                                           flux_period_hours INTEGER,
                                                           flux_peak_microteslas DOUBLE PRECISION,
                                                           flux_low_microteslas DOUBLE PRECISION,
                                                           flux_amplitude_percent DOUBLE PRECISION, -- % variation from average

    -- For UNSTABLE fields
                                                           instability_frequency VARCHAR(50), -- OCCASIONAL, FREQUENT, CONSTANT
                                                           random_fluctuation_percent DOUBLE PRECISION,

    -- ================================================================
    -- MAGNETIC REVERSALS
    -- ================================================================
                                                           has_reversals BOOLEAN DEFAULT FALSE,
                                                           reversal_period_million_years DOUBLE PRECISION,
                                                           time_since_last_reversal_million_years DOUBLE PRECISION,
                                                           reversal_transition_duration_years INTEGER, -- How long a reversal takes
                                                           current_reversal_state VARCHAR(50), -- NORMAL, IN_TRANSITION, REVERSED

    -- ================================================================
    -- MAGNETOSPHERE PROPERTIES (Interaction with stellar wind)
    -- ================================================================
                                                           magnetosphere_exists BOOLEAN DEFAULT FALSE,
                                                           magnetopause_distance_planet_radii DOUBLE PRECISION, -- Where stellar wind stops field
                                                           magnetotail_length_planet_radii DOUBLE PRECISION, -- Length of magnetotail downwind
                                                           bow_shock_distance_planet_radii DOUBLE PRECISION, -- Where stellar wind starts slowing

    -- Van Allen Belt analogs
                                                           has_radiation_belts BOOLEAN DEFAULT FALSE,
                                                           inner_belt_intensity VARCHAR(50), -- NONE, LOW, MODERATE, HIGH, EXTREME
                                                           outer_belt_intensity VARCHAR(50),

    -- ================================================================
    -- AURORAL ACTIVITY
    -- ================================================================
                                                           has_auroras BOOLEAN DEFAULT FALSE,
                                                           auroral_zone_latitude_degrees DOUBLE PRECISION, -- Typical latitude of aurora
                                                           auroral_frequency VARCHAR(50), -- RARE, OCCASIONAL, FREQUENT, CONSTANT
                                                           auroral_intensity VARCHAR(50), -- FAINT, MODERATE, BRIGHT, SPECTACULAR

    -- ================================================================
    -- FIELD INTERACTIONS
    -- ================================================================
                                                           shields_from_stellar_wind BOOLEAN DEFAULT FALSE,
                                                           shields_from_cosmic_rays BOOLEAN DEFAULT FALSE,
                                                           protection_level VARCHAR(50), -- NONE, MINIMAL, MODERATE, STRONG, EXCEPTIONAL

                                                           atmospheric_loss_rate_factor DOUBLE PRECISION, -- Multiplier for atm loss (1.0 = normal)

    -- ================================================================
    -- SCIENTIFIC PROPERTIES
    -- ================================================================
                                                           magnetic_moment DOUBLE PRECISION, -- Earth's is 7.91 × 10^22 A·m²
                                                           surface_power_flux_watts_per_m2 DOUBLE PRECISION, -- Power dissipated at surface

    -- Paleomagnetism (if planet has crust with magnetic minerals)
                                                           has_paleomagnetic_record BOOLEAN DEFAULT FALSE,
                                                           oldest_magnetic_rocks_million_years DOUBLE PRECISION,

    -- ================================================================
    -- METADATA
    -- ================================================================
                                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                           modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- ================================================================
    -- CONSTRAINTS
    -- ================================================================
                                                           CONSTRAINT fk_planet
                                                               FOREIGN KEY (planet_id)
                                                                   REFERENCES ud.planet(id)
                                                                   ON DELETE CASCADE,

                                                           CONSTRAINT chk_strength_positive
                                                               CHECK (strength_compared_to_earth >= 0),

                                                           CONSTRAINT chk_microteslas_valid
                                                               CHECK (
                                                                   surface_field_microteslas_min >= 0 AND
                                                                   surface_field_microteslas_max >= surface_field_microteslas_min AND
                                                                   surface_field_microteslas_avg >= surface_field_microteslas_min AND
                                                                   surface_field_microteslas_avg <= surface_field_microteslas_max
                                                                   ),

                                                           CONSTRAINT chk_dynamo_efficiency_range
                                                               CHECK (dynamo_efficiency IS NULL OR (dynamo_efficiency >= 0 AND dynamo_efficiency <= 1)),

                                                           CONSTRAINT chk_dipole_tilt_range
                                                               CHECK (dipole_tilt_degrees IS NULL OR (dipole_tilt_degrees >= 0 AND dipole_tilt_degrees <= 180)),

                                                           CONSTRAINT chk_pole_multiplier_positive
                                                               CHECK (pole_field_strength_multiplier IS NULL OR pole_field_strength_multiplier > 0),

                                                           CONSTRAINT chk_equatorial_multiplier_positive
                                                               CHECK (equatorial_field_strength_multiplier IS NULL OR equatorial_field_strength_multiplier > 0),

                                                           CONSTRAINT chk_flux_period_positive
                                                               CHECK (flux_period_hours IS NULL OR flux_period_hours > 0),

                                                           CONSTRAINT chk_flux_amplitude_range
                                                               CHECK (flux_amplitude_percent IS NULL OR (flux_amplitude_percent >= 0 AND flux_amplitude_percent <= 100)),

                                                           CONSTRAINT chk_magnetopause_positive
                                                               CHECK (magnetopause_distance_planet_radii IS NULL OR magnetopause_distance_planet_radii > 0),

                                                           CONSTRAINT chk_auroral_latitude_range
                                                               CHECK (auroral_zone_latitude_degrees IS NULL OR
                                                                      (auroral_zone_latitude_degrees >= -90 AND auroral_zone_latitude_degrees <= 90)),

                                                           CONSTRAINT chk_atm_loss_factor_positive
                                                               CHECK (atmospheric_loss_rate_factor IS NULL OR atmospheric_loss_rate_factor >= 0)
);

-- ================================================================
-- INDEXES
-- ================================================================
CREATE INDEX idx_mag_field_planet_id ON ud.planetary_magnetic_field(planet_id);
CREATE INDEX idx_mag_field_dynamo_type ON ud.planetary_magnetic_field(dynamo_type);
CREATE INDEX idx_mag_field_has_reversals ON ud.planetary_magnetic_field(has_reversals);
CREATE INDEX idx_mag_field_has_auroras ON ud.planetary_magnetic_field(has_auroras);
CREATE INDEX idx_mag_field_protection_level ON ud.planetary_magnetic_field(protection_level);

-- ================================================================
-- COMMENTS
-- ================================================================
COMMENT ON TABLE ud.planetary_magnetic_field IS
    'Comprehensive magnetic field properties for planets including dynamo characteristics, field geometry, temporal variations, magnetosphere properties, and stellar wind interactions';

COMMENT ON COLUMN ud.planetary_magnetic_field.strength_compared_to_earth IS
    'Magnetic field strength relative to Earth (Earth = 1.0, approximately 50 microTesla at surface)';

COMMENT ON COLUMN ud.planetary_magnetic_field.dynamo_type IS
    'Mechanism generating the magnetic field: CORE_DYNAMO (liquid metal core), METALLIC_HYDROGEN (gas giant), REMNANT (frozen-in ancient field), INDUCED (external field induces currents), NONE';

COMMENT ON COLUMN ud.planetary_magnetic_field.field_geometry IS
    'Shape of magnetic field: DIPOLE (like Earth or bar magnet), QUADRUPOLE (4 poles), MULTIPOLE (complex multi-pole), CHAOTIC (disorganized), COMPRESSED (flattened by stellar wind)';

COMMENT ON COLUMN ud.planetary_magnetic_field.temporal_stability IS
    'How field changes over time: STABLE (constant), FLUXING (regular cycles), UNSTABLE (random variations), REVERSING (poles flip), DECAYING (weakening permanently)';

COMMENT ON COLUMN ud.planetary_magnetic_field.magnetopause_distance_planet_radii IS
    'Distance to magnetopause where stellar wind pressure equals magnetic pressure in planet radii. Earth is approximately 10 radii on day side and 200+ radii on night side (magnetotail)';

COMMENT ON COLUMN ud.planetary_magnetic_field.protection_level IS
    'How well magnetic field protects surface from radiation: NONE (no protection), MINIMAL (less than 25%), MODERATE (25-50%), STRONG (50-80%), EXCEPTIONAL (greater than 80%)';

COMMENT ON COLUMN ud.planetary_magnetic_field.atmospheric_loss_rate_factor IS
    'Multiplier for atmospheric loss rate. Strong fields reduce loss (0.1 equals 10x slower), no field increases loss (10.0 equals 10x faster). Normal equals 1.0';

-- =====================================================================
-- SECTION 2: Remove old magnetic_field_strength from planet table
-- =====================================================================
-- We'll keep this column for now but deprecate it in comments
-- This allows backward compatibility during migration

COMMENT ON COLUMN ud.planet.magnetic_field_strength IS
    'DEPRECATED: Use planetary_magnetic_field table for comprehensive field data. This simplified value remains for backward compatibility only.';

-- =====================================================================
-- SECTION 3: Create Reference Data for Magnetic Field Types
-- =====================================================================

-- Create enum-like reference table for common field patterns
CREATE TABLE IF NOT EXISTS ref.magnetic_field_pattern_ref (
                                                              id SERIAL PRIMARY KEY,
                                                              pattern_name VARCHAR(100) NOT NULL UNIQUE,
                                                              display_name VARCHAR(200),
                                                              description TEXT,
                                                              typical_geometry VARCHAR(50),
                                                              typical_stability VARCHAR(50),
                                                              requires_dynamo BOOLEAN DEFAULT TRUE,
                                                              probability_weight INTEGER DEFAULT 50,
                                                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE ref.magnetic_field_pattern_ref IS
    'Reference patterns for planetary magnetic field configurations';

-- Insert common magnetic field patterns
INSERT INTO ref.magnetic_field_pattern_ref
(pattern_name, display_name, description, typical_geometry, typical_stability, requires_dynamo, probability_weight)
VALUES
    -- Earth-like active dynamos
    ('EARTHLIKE_DIPOLE', 'Earth-like Dipole',
     'Stable dipole field from liquid iron core dynamo, similar to Earth',
     'DIPOLE', 'STABLE', TRUE, 100),

    ('RAPID_DIPOLE', 'Rapid Dipole',
     'Fast-rotating dipole with frequent field fluctuations',
     'DIPOLE', 'FLUXING', TRUE, 60),

    ('TILTED_DIPOLE', 'Highly Tilted Dipole',
     'Dipole field with large angle between magnetic and rotation axes (like Uranus)',
     'DIPOLE', 'STABLE', TRUE, 40),

    -- Multi-pole configurations
    ('QUADRUPOLE', 'Quadrupole Field',
     'Four-pole magnetic configuration, often seen during field reversals',
     'QUADRUPOLE', 'UNSTABLE', TRUE, 30),

    ('MULTIPOLE', 'Complex Multipole',
     'Chaotic field with many poles, unstable dynamo (like young Saturn)',
     'MULTIPOLE', 'CHAOTIC', TRUE, 25),

    -- Gas giant patterns
    ('METALLIC_HYDROGEN', 'Metallic Hydrogen Dynamo',
     'Powerful field from metallic hydrogen layer in gas giant',
     'DIPOLE', 'STABLE', TRUE, 50),

    ('OFFSET_DIPOLE', 'Offset Dipole',
     'Dipole with center significantly offset from planet center (like Uranus/Neptune)',
     'DIPOLE', 'UNSTABLE', TRUE, 35),

    -- Weak or dead fields
    ('REMNANT_CRUSTAL', 'Remnant Crustal',
     'Ancient field frozen into crust, dynamo now dead (like Mars)',
     'CHAOTIC', 'STABLE', FALSE, 70),

    ('DECAYING_FIELD', 'Decaying Field',
     'Weakening field from cooling/solidifying core',
     'DIPOLE', 'DECAYING', TRUE, 40),

    ('INDUCED_FIELD', 'Induced Field',
     'Weak field induced by external stellar wind (like Venus)',
     'COMPRESSED', 'FLUXING', FALSE, 60),

    -- Extreme cases
    ('REVERSING_FIELD', 'Frequently Reversing',
     'Field that reverses polarity frequently due to chaotic dynamo',
     'DIPOLE', 'REVERSING', TRUE, 20),

    ('PULSAR_LIKE', 'Pulsar-like',
     'Extremely powerful field (neutron star remnant or exotic core)',
     'DIPOLE', 'STABLE', FALSE, 5),

    ('NO_FIELD', 'No Magnetic Field',
     'No significant global magnetic field',
     'NONE', 'STABLE', FALSE, 80);

-- Migration complete