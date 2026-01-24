-- V24__fix_atmosphere_template_gaps.sql
-- Fix temperature range gaps in atmosphere_template
-- Add planet_type → atmosphere_classification compatibility mapping

-- ========================================
-- PART 1: ATMOSPHERE TEMPLATE TEMPERATURE FIXES
-- ========================================

UPDATE ref.atmosphere_template
SET max_temperature_k = 400
WHERE name = 'Mars-like'
  AND classification = 'MARS_LIKE'
  AND max_temperature_k = 300;

UPDATE ref.atmosphere_template
SET min_temperature_k = 320
WHERE name = 'Venus-like'
  AND classification = 'VENUS_LIKE'
  AND min_temperature_k = 400;

UPDATE ref.atmosphere_template
SET max_temperature_k = 1500
WHERE name = 'Volcanic'
  AND classification = 'VOLCANIC'
  AND max_temperature_k = 1000;

UPDATE ref.atmosphere_template
SET min_temperature_k = 240
WHERE name = 'Earth-like'
  AND classification = 'EARTH_LIKE'
  AND min_temperature_k = 250;