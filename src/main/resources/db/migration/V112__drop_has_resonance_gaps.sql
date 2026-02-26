-- V112: Remove redundant has_resonance_gaps column from orbital_band.
-- Belt gap data is now consolidated into the shared has_gaps column.
ALTER TABLE ud.orbital_band DROP COLUMN IF EXISTS has_resonance_gaps;
