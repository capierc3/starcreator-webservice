ALTER TABLE ud.star
ADD COLUMN habitable_zone_inner_au DOUBLE PRECISION,
ADD COLUMN habitable_zone_outer_au DOUBLE PRECISION;

COMMENT ON COLUMN ud.star.habitable_zone_inner_au IS
'Inner edge of habitable zone in AU (where liquid water can exist)';

COMMENT ON COLUMN ud.star.habitable_zone_outer_au IS
'Outer edge of habitable zone in AU (where liquid water can exist)';

-- Add index for queries about habitable planets
CREATE INDEX idx_star_habitable_zone
ON ud.star(habitable_zone_inner_au, habitable_zone_outer_au);