-- V12__atmosphere_reference_tables.sql
-- Creates reference tables for atmosphere templates with customizable compositions

-- ============================================================================
-- ATMOSPHERE TEMPLATE TABLE
-- ============================================================================
CREATE TABLE ref.atmosphere_template (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    classification VARCHAR(50) NOT NULL,
    is_breathable BOOLEAN DEFAULT FALSE,
    typical_pressure_bar DOUBLE PRECISION DEFAULT 1.0,
    min_temperature_k DOUBLE PRECISION,
    max_temperature_k DOUBLE PRECISION,
    min_planet_mass_earth DOUBLE PRECISION DEFAULT 0.0,
    max_planet_mass_earth DOUBLE PRECISION DEFAULT 1000.0,
    rarity_weight INTEGER DEFAULT 100,
    created_at TIMESTAMP DEFAULT NOW()
);

COMMENT ON TABLE ref.atmosphere_template IS 'Pre-defined atmosphere templates (Earth-like, Venus-like, etc.)';
COMMENT ON COLUMN ref.atmosphere_template.name IS 'Display name of atmosphere type';
COMMENT ON COLUMN ref.atmosphere_template.classification IS 'Enum value from AtmosphereClassification';
COMMENT ON COLUMN ref.atmosphere_template.is_breathable IS 'Whether humans can breathe this atmosphere';
COMMENT ON COLUMN ref.atmosphere_template.typical_pressure_bar IS 'Typical surface pressure in bar (Earth = 1.0)';
COMMENT ON COLUMN ref.atmosphere_template.min_temperature_k IS 'Minimum viable temperature for this atmosphere';
COMMENT ON COLUMN ref.atmosphere_template.max_temperature_k IS 'Maximum viable temperature for this atmosphere';
COMMENT ON COLUMN ref.atmosphere_template.rarity_weight IS 'Weight for random selection (higher = more common)';

-- ============================================================================
-- ATMOSPHERE COMPONENT TABLE
-- ============================================================================
CREATE TABLE ref.atmosphere_template_component (
    id SERIAL PRIMARY KEY,
    template_id INTEGER NOT NULL REFERENCES ref.atmosphere_template(id) ON DELETE CASCADE,
    gas_formula VARCHAR(10) NOT NULL,
    min_percentage DOUBLE PRECISION NOT NULL,
    max_percentage DOUBLE PRECISION NOT NULL,
    is_trace BOOLEAN DEFAULT FALSE,
    CONSTRAINT valid_percentage CHECK (min_percentage >= 0 AND max_percentage <= 100 AND min_percentage <= max_percentage),
    CONSTRAINT unique_gas_per_template UNIQUE(template_id, gas_formula)
);

COMMENT ON TABLE ref.atmosphere_template_component IS 'Gas components for each atmosphere template';
COMMENT ON COLUMN ref.atmosphere_template_component.gas_formula IS 'Chemical formula (N2, O2, etc.) matching AtmosphereGas enum';
COMMENT ON COLUMN ref.atmosphere_template_component.min_percentage IS 'Minimum percentage of this gas in the atmosphere';
COMMENT ON COLUMN ref.atmosphere_template_component.max_percentage IS 'Maximum percentage of this gas in the atmosphere';
COMMENT ON COLUMN ref.atmosphere_template_component.is_trace IS 'Whether this is a trace gas (<0.1%)';

CREATE INDEX idx_atm_template_component ON ref.atmosphere_template_component(template_id);
CREATE INDEX idx_atm_template_classification ON ref.atmosphere_template(classification);

-- ============================================================================
-- SEED DATA: COMMON ATMOSPHERE TYPES
-- ============================================================================

-- 1. EARTH-LIKE
INSERT INTO ref.atmosphere_template 
(name, description, classification, is_breathable, typical_pressure_bar, min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES 
('Earth-like', 'Nitrogen-oxygen atmosphere, breathable by humans', 'EARTH_LIKE', true, 1.0, 250, 320, 0.5, 2.0, 30);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace) VALUES
(1, 'N2', 75.0, 80.0, false),
(1, 'O2', 18.0, 24.0, false),
(1, 'Ar', 0.8, 1.2, false),
(1, 'CO2', 0.03, 0.1, true),
(1, 'H2O', 0.5, 5.0, false);

-- 2. VENUS-LIKE
INSERT INTO ref.atmosphere_template 
(name, description, classification, is_breathable, typical_pressure_bar, min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES 
('Venus-like', 'Dense CO2 atmosphere with extreme greenhouse effect', 'VENUS_LIKE', false, 92.0, 400, 800, 0.5, 2.0, 50);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace) VALUES
(2, 'CO2', 95.0, 97.0, false),
(2, 'N2', 3.0, 5.0, false),
(2, 'SO2', 0.01, 0.2, true);

-- 3. MARS-LIKE
INSERT INTO ref.atmosphere_template 
(name, description, classification, is_breathable, typical_pressure_bar, min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES 
('Mars-like', 'Thin CO2 atmosphere, minimal surface pressure', 'MARS_LIKE', false, 0.006, 150, 300, 0.1, 0.5, 70);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace) VALUES
(3, 'CO2', 94.0, 96.0, false),
(3, 'N2', 2.0, 3.0, false),
(3, 'Ar', 1.5, 2.0, false),
(3, 'O2', 0.1, 0.2, true),
(3, 'CO', 0.05, 0.1, true);

-- 4. TITAN-LIKE
INSERT INTO ref.atmosphere_template 
(name, description, classification, is_breathable, typical_pressure_bar, min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES 
('Titan-like', 'Dense nitrogen atmosphere with methane', 'TITAN_LIKE', false, 1.5, 50, 150, 0.5, 5.0, 40);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace) VALUES
(4, 'N2', 92.0, 96.0, false),
(4, 'CH4', 2.0, 6.0, false),
(4, 'H2', 0.1, 1.0, true);

-- 5. JOVIAN (Gas Giant)
INSERT INTO ref.atmosphere_template 
(name, description, classification, is_breathable, typical_pressure_bar, min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES 
('Jovian', 'Hydrogen-helium atmosphere, gas giant', 'JOVIAN', false, 1000.0, 50, 500, 50.0, 5000.0, 100);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace) VALUES
(5, 'H2', 85.0, 90.0, false),
(5, 'He', 10.0, 15.0, false),
(5, 'CH4', 0.1, 0.5, true),
(5, 'NH3', 0.05, 0.3, true),
(5, 'H2O', 0.05, 0.2, true);

-- 6. ICE GIANT
INSERT INTO ref.atmosphere_template 
(name, description, classification, is_breathable, typical_pressure_bar, min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES 
('Ice Giant', 'Hydrogen-helium with methane and ammonia', 'ICE_GIANT', false, 500.0, 30, 200, 10.0, 50.0, 80);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace) VALUES
(6, 'H2', 80.0, 85.0, false),
(6, 'He', 10.0, 15.0, false),
(6, 'CH4', 2.0, 4.0, false),
(6, 'NH3', 0.5, 2.0, false),
(6, 'H2O', 0.5, 1.5, true);

-- 7. VOLCANIC
INSERT INTO ref.atmosphere_template 
(name, description, classification, is_breathable, typical_pressure_bar, min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES 
('Volcanic', 'Sulfur dioxide and volcanic gases', 'VOLCANIC', false, 2.0, 300, 1000, 0.3, 3.0, 20);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace) VALUES
(7, 'SO2', 40.0, 70.0, false),
(7, 'CO2', 20.0, 50.0, false),
(7, 'H2S', 1.0, 10.0, false);

-- 8. AMMONIA
INSERT INTO ref.atmosphere_template 
(name, description, classification, is_breathable, typical_pressure_bar, min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES 
('Ammonia', 'Ammonia-dominated atmosphere, extremely toxic', 'AMMONIA', false, 1.5, 100, 300, 0.5, 5.0, 15);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace) VALUES
(8, 'NH3', 60.0, 85.0, false),
(8, 'N2', 10.0, 30.0, false),
(8, 'CH4', 2.0, 10.0, false);

-- 9. REDUCING (Early Earth-like)
INSERT INTO ref.atmosphere_template 
(name, description, classification, is_breathable, typical_pressure_bar, min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES 
('Reducing', 'Hydrogen-rich, oxygen-poor primitive atmosphere', 'REDUCING', false, 1.0, 200, 400, 0.5, 2.0, 25);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace) VALUES
(9, 'H2', 50.0, 70.0, false),
(9, 'CH4', 15.0, 30.0, false),
(9, 'NH3', 5.0, 15.0, false),
(9, 'H2O', 2.0, 10.0, false);

-- 10. CORROSIVE
INSERT INTO ref.atmosphere_template 
(name, description, classification, is_breathable, typical_pressure_bar, min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES 
('Corrosive', 'Chlorine or fluorine-based atmosphere', 'CORROSIVE', false, 1.0, 250, 600, 0.5, 3.0, 5);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace) VALUES
(10, 'Cl2', 40.0, 70.0, false),
(10, 'F2', 10.0, 30.0, false),
(10, 'Ar', 5.0, 20.0, false);

-- 11. EXOTIC (Metallic Vapors)
INSERT INTO ref.atmosphere_template 
(name, description, classification, is_breathable, typical_pressure_bar, min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES 
('Exotic Metallic', 'Metallic vapors from extreme heat', 'EXOTIC', false, 0.5, 1500, 3000, 0.5, 10.0, 5);

INSERT INTO ref.atmosphere_template_component (template_id, gas_formula, min_percentage, max_percentage, is_trace) VALUES
(11, 'Fe', 40.0, 60.0, false),
(11, 'Na', 20.0, 40.0, false),
(11, 'K', 10.0, 20.0, false);

-- 12. NONE (Trace/No Atmosphere)
INSERT INTO ref.atmosphere_template 
(name, description, classification, is_breathable, typical_pressure_bar, min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight)
VALUES 
('None', 'No atmosphere or extremely thin', 'NONE', false, 0.0, 0, 10000, 0.0, 0.3, 60);

-- No components for NONE atmosphere

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Verify atmosphere templates loaded
SELECT COUNT(*) as template_count FROM ref.atmosphere_template;
-- Expected: 12

-- Verify components loaded
SELECT COUNT(*) as component_count FROM ref.atmosphere_template_component;
-- Expected: 50+

-- Show all templates with component counts
SELECT 
    t.id,
    t.name,
    t.classification,
    t.is_breathable,
    COUNT(c.id) as num_gases
FROM ref.atmosphere_template t
LEFT JOIN ref.atmosphere_template_component c ON t.id = c.template_id
GROUP BY t.id, t.name, t.classification, t.is_breathable
ORDER BY t.id;
