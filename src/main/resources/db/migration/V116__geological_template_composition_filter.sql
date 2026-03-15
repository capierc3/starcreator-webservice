-- Add composition_types column to geological_template for filtering moon templates
-- by moon composition (ICY, MIXED, ROCKY). NULL means "matches all compositions".

ALTER TABLE ref.geological_template
    ADD COLUMN composition_types VARCHAR(200);

COMMENT ON COLUMN ref.geological_template.composition_types IS
    'Comma-separated list of applicable composition types (ICY, MIXED, ROCKY). NULL matches all.';

-- Rocky/silicate moon templates: match ROCKY and MIXED compositions
UPDATE ref.geological_template SET composition_types = 'ROCKY,MIXED'
WHERE name = 'Moon - Active Rocky';

UPDATE ref.geological_template SET composition_types = 'ROCKY,MIXED'
WHERE name = 'Moon - Hyperactive Volcanic';

-- Icy/cryovolcanic moon templates: match ICY and MIXED compositions
UPDATE ref.geological_template SET composition_types = 'ICY,MIXED'
WHERE name = 'Moon - Cryovolcanic';

UPDATE ref.geological_template SET composition_types = 'ICY,MIXED'
WHERE name = 'Moon - Hyperactive Cryovolcanic';
