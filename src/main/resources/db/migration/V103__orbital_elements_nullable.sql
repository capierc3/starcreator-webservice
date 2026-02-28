-- Allow semi_major_axis and semi_major_axis_unit to be NULL
-- Stars in a single-star system don't have a meaningful semi-major axis
ALTER TABLE ud.orbital_elements ALTER COLUMN semi_major_axis DROP NOT NULL;
ALTER TABLE ud.orbital_elements ALTER COLUMN semi_major_axis_unit DROP NOT NULL;
