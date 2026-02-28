-- V110: Add Trojan swarm support columns to orbital_band
-- Only stores truly random per-swarm data; constants and derivable fields are @Transient

ALTER TABLE ud.orbital_band
    ADD COLUMN lagrange_point VARCHAR(5),
    ADD COLUMN libration_amplitude_deg DOUBLE PRECISION;

COMMENT ON COLUMN ud.orbital_band.lagrange_point IS 'Lagrange point: L4 (60° ahead) or L5 (60° behind) — Trojan bands only';
COMMENT ON COLUMN ud.orbital_band.libration_amplitude_deg IS 'Angular spread from the Lagrange point in degrees (mean ~33°, range 0.6–88°) — Trojan bands only';
