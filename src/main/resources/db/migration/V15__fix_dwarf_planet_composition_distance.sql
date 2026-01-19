-- V15__fix_dwarf_planet_composition_distance.sql
-- Fix Dwarf Planet template to allow any orbital distance
-- Bug: Dwarf planets at <10 AU were matching Mini-Neptune template instead

-- Update the Dwarf Planet template to remove distance constraints
UPDATE ref.composition_template 
SET min_distance_au = NULL,
    max_distance_au = NULL
WHERE name = 'Dwarf - Ice-Rock Mix';
