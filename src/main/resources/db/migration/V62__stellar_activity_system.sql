-- =====================================================================
-- Flyway Migration V48: Stellar Activity & Lifecycle System
-- =====================================================================
-- Description: Adds comprehensive stellar activity properties to make
--              stars feel alive — flare frequency, activity cycles,
--              stellar wind, coronal properties, and evolutionary state.
--
-- New columns on ud.star:
--   ACTIVITY CYCLE (magnetic dynamo rhythm)
--   FLARE ACTIVITY (frequency, intensity, energy)
--   STELLAR WIND (mass loss, velocity, pressure)
--   CORONAL PROPERTIES (temperature, emission)
--   EVOLUTIONARY STATE (main sequence lifetime fraction, grand minima)
--   STARSPOT PROPERTIES (coverage, temperature contrast)
-- =====================================================================

-- =====================================================================
-- SECTION 1: Activity Cycle Properties
-- =====================================================================
-- Stars have magnetic activity cycles (Sun = ~11 years).
-- Faster rotators have shorter cycles. Old slow stars = longer cycles.
-- =====================================================================

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS activity_cycle_years DOUBLE PRECISION;
COMMENT ON COLUMN ud.star.activity_cycle_years IS
    'Duration of the magnetic activity cycle in years (Sun = 11.07 years). Derived from rotation period via Noyes relationship.';

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS activity_cycle_phase DOUBLE PRECISION;
COMMENT ON COLUMN ud.star.activity_cycle_phase IS
    'Current phase in the activity cycle, 0.0 (minimum) to 1.0 (maximum). Determines current flare rates and spot coverage.';

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS activity_level VARCHAR(30);
COMMENT ON COLUMN ud.star.activity_level IS
    'Overall chromospheric activity level: INACTIVE, LOW, MODERATE, ACTIVE, VERY_ACTIVE, HYPERACTIVE';

-- =====================================================================
-- SECTION 2: Grand Minimum / Maximum State
-- =====================================================================
-- Stars can enter extended quiet periods (like the Maunder Minimum
-- 1645-1715) or extended active periods. These last decades to centuries.
-- =====================================================================

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS in_grand_minimum BOOLEAN DEFAULT FALSE;
COMMENT ON COLUMN ud.star.in_grand_minimum IS
    'Whether the star is currently in a Maunder-minimum-like grand minimum — dramatically reduced activity for decades/centuries.';

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS grand_minimum_duration_years DOUBLE PRECISION;
COMMENT ON COLUMN ud.star.grand_minimum_duration_years IS
    'If in grand minimum, estimated total duration in years (typical 50-200 years).';

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS grand_minimum_depth DOUBLE PRECISION;
COMMENT ON COLUMN ud.star.grand_minimum_depth IS
    'Depth of the grand minimum: 0.0 (shallow, some spots remain) to 1.0 (deep, virtually no spots). Maunder Minimum was ~0.85.';

-- =====================================================================
-- SECTION 3: Flare Activity
-- =====================================================================
-- Flare frequency follows a power law. Small flares are common,
-- superflares are rare. Mass and rotation drive the envelope.
-- =====================================================================

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS flare_frequency_per_day DOUBLE PRECISION;
COMMENT ON COLUMN ud.star.flare_frequency_per_day IS
    'Average number of detectable flares per day at current activity level. Sun at solar max ~ 0.5/day. M dwarfs can be 5-50/day.';

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS max_flare_energy_ergs DOUBLE PRECISION;
COMMENT ON COLUMN ud.star.max_flare_energy_ergs IS
    'Estimated maximum flare energy in ergs (log10 scale stored). Sun typical max ~ 32 (10^32 ergs). Superflare stars can reach 36+.';

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS flare_class VARCHAR(30);
COMMENT ON COLUMN ud.star.flare_class IS
    'Dominant flare class: NANOFLARE, MICROFLARE, C_CLASS, M_CLASS, X_CLASS, SUPERFLARE';

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS superflare_capable BOOLEAN DEFAULT FALSE;
COMMENT ON COLUMN ud.star.superflare_capable IS
    'Whether this star can produce superflares (10-1000x largest solar flares). More common in young, fast-rotating stars.';

-- =====================================================================
-- SECTION 4: Starspot Properties
-- =====================================================================
-- Spots are the visible face of the magnetic cycle. Coverage varies
-- dramatically — the Sun has <1% at max, active K dwarfs can hit 30%.
-- =====================================================================

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS starspot_coverage_percent DOUBLE PRECISION;
COMMENT ON COLUMN ud.star.starspot_coverage_percent IS
    'Current fractional coverage of stellar surface by starspots (%). Sun at solar max ~ 0.3%. Active M/K dwarfs can reach 20-40%.';

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS starspot_temp_contrast_k DOUBLE PRECISION;
COMMENT ON COLUMN ud.star.starspot_temp_contrast_k IS
    'Temperature difference between quiet photosphere and spot umbra in Kelvin. Sun ~ 1500K. Cooler stars have smaller contrasts.';

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS has_polar_spots BOOLEAN DEFAULT FALSE;
COMMENT ON COLUMN ud.star.has_polar_spots IS
    'Whether the star has large polar spot regions. Common on fast rotators and tidally locked binaries. Absent on slow rotators like the Sun.';

-- =====================================================================
-- SECTION 5: Stellar Wind Properties
-- =====================================================================
-- The stellar wind shapes everything downstream — magnetospheres,
-- atmospheric stripping, habitability. Mass-loss rate is key.
-- =====================================================================

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS stellar_wind_mass_loss_rate DOUBLE PRECISION;
COMMENT ON COLUMN ud.star.stellar_wind_mass_loss_rate IS
    'Mass loss rate in solar masses per year. Sun = ~2e-14. Red giants can be 1e-8 to 1e-5.';

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS stellar_wind_velocity_km_s DOUBLE PRECISION;
COMMENT ON COLUMN ud.star.stellar_wind_velocity_km_s IS
    'Terminal velocity of stellar wind in km/s. Sun slow wind ~ 400, fast wind ~ 750. Hot stars can reach 2000+.';

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS stellar_wind_density_at_1au DOUBLE PRECISION;
COMMENT ON COLUMN ud.star.stellar_wind_density_at_1au IS
    'Proton density of stellar wind at 1 AU in particles/cm^3. Sun average ~ 6.';

-- =====================================================================
-- SECTION 6: Coronal Properties
-- =====================================================================
-- The corona is the million-degree outer atmosphere. Its brightness
-- in X-rays is a proxy for magnetic activity.
-- =====================================================================

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS coronal_temp_mk DOUBLE PRECISION;
COMMENT ON COLUMN ud.star.coronal_temp_mk IS
    'Peak coronal temperature in millions of Kelvin. Sun ~ 1-2 MK. Active stars can reach 10-30 MK.';

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS xray_luminosity_class VARCHAR(20);
COMMENT ON COLUMN ud.star.xray_luminosity_class IS
    'X-ray luminosity class: DARK (white dwarfs, very old), DIM, MODERATE, BRIGHT, INTENSE (young active stars)';

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS has_corona BOOLEAN DEFAULT TRUE;
COMMENT ON COLUMN ud.star.has_corona IS
    'Whether the star has a traditional hot corona. White dwarfs, neutron stars, and brown dwarfs typically do not.';

-- =====================================================================
-- SECTION 7: Evolutionary State
-- =====================================================================
-- Where is this star in its life? This drives narrative context.
-- =====================================================================

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS main_sequence_fraction DOUBLE PRECISION;
COMMENT ON COLUMN ud.star.main_sequence_fraction IS
    'Fraction of main sequence lifetime elapsed: 0.0 (just arrived) to 1.0 (about to leave). NULL for non-MS stars.';

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS evolutionary_stage VARCHAR(50);
COMMENT ON COLUMN ud.star.evolutionary_stage IS
    'Descriptive evolutionary stage: PRE_MAIN_SEQUENCE, EARLY_MAIN_SEQUENCE, MID_MAIN_SEQUENCE, LATE_MAIN_SEQUENCE, SUBGIANT_TRANSITION, RED_GIANT_BRANCH, HORIZONTAL_BRANCH, ASYMPTOTIC_GIANT, POST_AGB, WHITE_DWARF_COOLING, NEUTRON_STAR, BROWN_DWARF_COOLING';

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS estimated_remaining_ms_my DOUBLE PRECISION;
COMMENT ON COLUMN ud.star.estimated_remaining_ms_my IS
    'Estimated remaining main sequence lifetime in millions of years. NULL for post-MS stars. Gives writers a "ticking clock" narrative element.';

-- =====================================================================
-- SECTION 8: Chromospheric Activity Index
-- =====================================================================

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS log_r_prime_hk DOUBLE PRECISION;
COMMENT ON COLUMN ud.star.log_r_prime_hk IS
    'Mount Wilson S-index converted to log(R''HK) chromospheric emission ratio. Ranges from -5.1 (very inactive) to -4.0 (very active). Standard measure of stellar activity.';

ALTER TABLE ud.star ADD COLUMN IF NOT EXISTS rossby_number DOUBLE PRECISION;
COMMENT ON COLUMN ud.star.rossby_number IS
    'Rossby number (Ro = P_rot / τ_conv). Stars with Ro < 0.1 are in saturated activity regime. Key predictor of activity level.';

-- =====================================================================
-- SECTION 9: Indexes for Common Queries
-- =====================================================================

CREATE INDEX IF NOT EXISTS idx_star_activity_level ON ud.star(activity_level);
CREATE INDEX IF NOT EXISTS idx_star_evolutionary_stage ON ud.star(evolutionary_stage);
CREATE INDEX IF NOT EXISTS idx_star_flare_class ON ud.star(flare_class);
CREATE INDEX IF NOT EXISTS idx_star_in_grand_minimum ON ud.star(in_grand_minimum);