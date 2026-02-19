-- V83__planet_orbital_stability.sql
-- Adds orbital stability metadata columns to the planet table.
-- These are populated during generation by the stability analysis pass
-- that runs after all planets in a system are placed.
--
-- orbit_stability: STABLE / MARGINAL / CROSSING / UNSTABLE
-- orbit_stability_timescale_my: estimated Myr until orbit destabilizes (null = indefinite)
-- orbit_crossing_neighbor: name of the adjacent planet involved in the worst interaction

ALTER TABLE ud.planet
    ADD COLUMN orbit_stability VARCHAR(20),
    ADD COLUMN orbit_stability_timescale_my DOUBLE PRECISION,
    ADD COLUMN orbit_crossing_neighbor VARCHAR(255);

COMMENT ON COLUMN ud.planet.orbit_stability IS
    'Gladman criterion result: STABLE (Δ>5.2), MARGINAL (3.46<Δ<5.2), CROSSING (orbits overlap but timescale>age), UNSTABLE (should have destabilized)';

COMMENT ON COLUMN ud.planet.orbit_stability_timescale_my IS
    'Estimated instability timescale in millions of years via Chambers (1996) relation. NULL = indefinitely stable.';

COMMENT ON COLUMN ud.planet.orbit_crossing_neighbor IS
    'Name of adjacent planet involved in the closest/most dangerous orbital interaction';

CREATE INDEX idx_planet_orbit_stability ON ud.planet(orbit_stability);
