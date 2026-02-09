-- =====================================================================
-- Flyway Migration V63: Water System Rewrite & Planetary Habitability
-- =====================================================================
-- Description: Two-part migration:
--   1. Replaces the all-or-nothing water_coverage_percent with a
--      multi-phase water inventory system that accounts for ice, liquid,
--      subsurface water, and volatile delivery history.
--   2. Creates the planetary_habitability table for post-processing
--      analysis of planet and moon habitability.
--
-- Author: Chase
-- Date: 2026-02-07
-- =====================================================================

-- =====================================================================
-- PART 1: WATER SYSTEM REWRITE
-- =====================================================================
-- The existing water_coverage_percent is too binary - it only assigns
-- water to planets that pass ALL three gates (habitable flag, HZ position,
-- AND liquid water temp+pressure). This misses:
--   - Ice caps on cold planets (Mars has polar ice at 0.006 atm)
--   - Subsurface oceans (Europa, Enceladus)
--   - Trace water on thin-atmosphere worlds
--   - Frozen water on planets that formed beyond frost line
-- =====================================================================

-- New water inventory column - how much total water the planet has
ALTER TABLE ud.planet ADD COLUMN IF NOT EXISTS water_inventory VARCHAR(30);
COMMENT ON COLUMN ud.planet.water_inventory IS
    'Total water inventory: NONE, TRACE, SCARCE, MODERATE, ABUNDANT, OCEAN_WORLD. Separate from surface state - a frozen planet can have ABUNDANT water locked as ice.';

-- Break down existing water_coverage_percent into specific phases
ALTER TABLE ud.planet ADD COLUMN IF NOT EXISTS liquid_water_coverage_percent DOUBLE PRECISION;
COMMENT ON COLUMN ud.planet.liquid_water_coverage_percent IS
    'Percentage of surface covered by liquid water (oceans, lakes, rivers). Subset of water_coverage_percent.';

ALTER TABLE ud.planet ADD COLUMN IF NOT EXISTS ice_coverage_percent DOUBLE PRECISION;
COMMENT ON COLUMN ud.planet.ice_coverage_percent IS
    'Percentage of surface covered by water ice (polar caps, glaciers, ice sheets, permafrost). Subset of water_coverage_percent.';

ALTER TABLE ud.planet ADD COLUMN IF NOT EXISTS has_subsurface_water BOOLEAN DEFAULT FALSE;
COMMENT ON COLUMN ud.planet.has_subsurface_water IS
    'Whether liquid water exists underground (aquifers, subsurface oceans). Can be true even when surface is frozen or airless.';

ALTER TABLE ud.planet ADD COLUMN IF NOT EXISTS subsurface_water_depth_km DOUBLE PRECISION;
COMMENT ON COLUMN ud.planet.subsurface_water_depth_km IS
    'Estimated depth to subsurface water table or ocean in km. NULL if no subsurface water.';

-- Update existing column comment to clarify it is now TOTAL (liquid + ice)
COMMENT ON COLUMN ud.planet.water_coverage_percent IS
    'Total surface water coverage (liquid + ice) as percentage. Sum of liquid_water_coverage_percent and ice_coverage_percent.';

-- Index for water queries
CREATE INDEX IF NOT EXISTS idx_planet_water_inventory ON ud.planet(water_inventory);
CREATE INDEX IF NOT EXISTS idx_planet_has_subsurface_water ON ud.planet(has_subsurface_water) WHERE has_subsurface_water = TRUE;

-- =====================================================================
-- PART 2: PLANETARY HABITABILITY TABLE
-- =====================================================================
-- Post-processing analysis entity. Reads ALL other planet data and
-- produces a comprehensive habitability assessment. Applied to every
-- planet and moon - most will be INHOSPITABLE, making the rare
-- habitable ones feel earned rather than forced.
-- =====================================================================

CREATE TABLE IF NOT EXISTS ud.planetary_habitability (
    id BIGSERIAL PRIMARY KEY,
    planet_id BIGINT UNIQUE,  -- Nullable until planet is saved to DB

    -- ================================================================
    -- EARTH SIMILARITY INDEX (ESI)
    -- ================================================================
    esi_total DOUBLE PRECISION,
    esi_interior DOUBLE PRECISION,
    esi_surface DOUBLE PRECISION,

    -- ================================================================
    -- RADIATION ENVIRONMENT
    -- ================================================================
    uv_surface_flux_earth DOUBLE PRECISION,
    uv_hazard_level VARCHAR(30),
    ionizing_radiation_surface_msv_yr DOUBLE PRECISION,
    cosmic_ray_flux_earth DOUBLE PRECISION,
    stellar_particle_flux DOUBLE PRECISION,
    radiation_belt_surface_dose VARCHAR(30),

    -- ================================================================
    -- ATMOSPHERIC HABITABILITY
    -- ================================================================
    is_breathable BOOLEAN DEFAULT FALSE,
    breathability_issues VARCHAR(500),
    oxygen_percentage DOUBLE PRECISION,
    oxygen_source VARCHAR(30),
    has_ozone_layer BOOLEAN DEFAULT FALSE,
    ozone_column_dobson DOUBLE PRECISION,
    greenhouse_warming_k DOUBLE PRECISION,
    atmospheric_retention_score DOUBLE PRECISION,
    toxic_gas_hazard VARCHAR(30),

    -- ================================================================
    -- LIQUID WATER POTENTIAL
    -- ================================================================
    surface_liquid_water_possible BOOLEAN DEFAULT FALSE,
    water_phase_at_surface VARCHAR(30),
    habitable_surface_fraction DOUBLE PRECISION,
    subsurface_ocean_possible BOOLEAN DEFAULT FALSE,
    water_source_likelihood VARCHAR(30),
    mean_surface_temp_habitable BOOLEAN DEFAULT FALSE,

    -- ================================================================
    -- GEOLOGICAL HABITABILITY
    -- ================================================================
    has_carbon_cycle BOOLEAN DEFAULT FALSE,
    carbon_cycle_strength VARCHAR(30),
    geothermal_heat_flux_mw_m2 DOUBLE PRECISION,
    magnetic_protection_adequate BOOLEAN DEFAULT FALSE,
    tidal_heating_contribution VARCHAR(30),
    nutrient_cycling_potential VARCHAR(30),

    -- ================================================================
    -- ORBITAL & STELLAR ENVIRONMENT
    -- ================================================================
    stellar_lifetime_remaining_my DOUBLE PRECISION,
    time_in_habitable_zone_my DOUBLE PRECISION,
    orbital_stability_score DOUBLE PRECISION,
    tidal_lock_risk VARCHAR(30),
    obliquity_stability VARCHAR(30),
    flare_exposure_risk VARCHAR(30),
    day_night_temp_range_k DOUBLE PRECISION,

    -- ================================================================
    -- BIOSIGNATURE & LIFE ASSESSMENT
    -- ================================================================
    biosignature_potential VARCHAR(30),
    biosignature_indicators VARCHAR(500),
    life_complexity_potential VARCHAR(50),
    photosynthesis_viable BOOLEAN DEFAULT FALSE,
    energy_sources_for_life VARCHAR(500),

    -- ================================================================
    -- HABITABILITY CLASSIFICATION
    -- ================================================================
    habitability_class VARCHAR(50) NOT NULL,
    habitability_score DOUBLE PRECISION,
    terraforming_potential VARCHAR(30),
    terraforming_challenges VARCHAR(500),
    colonization_suitability VARCHAR(30),
    limiting_factor VARCHAR(500),

    -- ================================================================
    -- METADATA
    -- ================================================================
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- ================================================================
    -- CONSTRAINTS
    -- ================================================================
    CONSTRAINT chk_hab_esi_range CHECK (esi_total IS NULL OR (esi_total >= 0 AND esi_total <= 1)),
    CONSTRAINT chk_hab_esi_interior_range CHECK (esi_interior IS NULL OR (esi_interior >= 0 AND esi_interior <= 1)),
    CONSTRAINT chk_hab_esi_surface_range CHECK (esi_surface IS NULL OR (esi_surface >= 0 AND esi_surface <= 1)),
    CONSTRAINT chk_hab_score_range CHECK (habitability_score IS NULL OR (habitability_score >= 0 AND habitability_score <= 100)),
    CONSTRAINT chk_hab_retention_range CHECK (atmospheric_retention_score IS NULL OR (atmospheric_retention_score >= 0 AND atmospheric_retention_score <= 1)),
    CONSTRAINT chk_hab_orbital_stability_range CHECK (orbital_stability_score IS NULL OR (orbital_stability_score >= 0 AND orbital_stability_score <= 1)),
    CONSTRAINT chk_hab_surface_fraction_range CHECK (habitable_surface_fraction IS NULL OR (habitable_surface_fraction >= 0 AND habitable_surface_fraction <= 1))
);

-- ================================================================
-- INDEXES
-- ================================================================
CREATE INDEX idx_habitability_planet_id ON ud.planetary_habitability(planet_id);
CREATE INDEX idx_habitability_class ON ud.planetary_habitability(habitability_class);
CREATE INDEX idx_habitability_score ON ud.planetary_habitability(habitability_score);
CREATE INDEX idx_habitability_esi ON ud.planetary_habitability(esi_total);
CREATE INDEX idx_habitability_breathable ON ud.planetary_habitability(is_breathable) WHERE is_breathable = TRUE;
CREATE INDEX idx_habitability_colonization ON ud.planetary_habitability(colonization_suitability);
CREATE INDEX idx_habitability_terraforming ON ud.planetary_habitability(terraforming_potential);

-- ================================================================
-- COMMENTS
-- ================================================================
COMMENT ON TABLE ud.planetary_habitability IS
    'Post-processing habitability assessment for planets and moons. Calculated from all other planet data. Every planet and moon gets an assessment.';

COMMENT ON COLUMN ud.planetary_habitability.esi_total IS
    'Earth Similarity Index (0-1). Weighted geometric mean comparing radius, density, escape velocity, and surface temp to Earth. Earth=1.0, Mars≈0.56, Venus≈0.44.';

COMMENT ON COLUMN ud.planetary_habitability.habitability_class IS
    'Top-level classification: EARTH_ANALOG, HABITABLE_MARGINAL, BIOSPHERE_POSSIBLE, SUBSURFACE_HABITABLE, TERRAFORMABLE_EASY, TERRAFORMABLE_HARD, EXTREME_ENVIRONMENT, RESOURCE_WORLD, INHOSPITABLE, STERILIZED';

COMMENT ON COLUMN ud.planetary_habitability.habitability_score IS
    'Composite score 0-100 weighting all habitability factors. 80+ is very Earth-like, 50-80 is marginally habitable, 20-50 has potential, below 20 is hostile.';

COMMENT ON COLUMN ud.planetary_habitability.colonization_suitability IS
    'Practical assessment: SHIRT_SLEEVE (walk outside), ASSISTED (need O2 supplement), DOME_ONLY (enclosed habitats), UNINHABITABLE (no permanent presence)';
