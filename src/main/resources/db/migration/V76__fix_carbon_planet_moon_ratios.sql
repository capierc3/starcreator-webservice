-- V__fix_carbon_planet_moon_ratios.sql
-- Fix Carbon Planets being 99%+ moonless
-- Root cause: moon system mass ratio was 10-20x too low compared to
-- similar-mass rocky planets, AND the low budget triggered an integer
-- division bug in determineNumberOfMoons() that collapsed range to [0,0]

-- Carbon planets (0.5-5 M⊕) should have moon formation potential
-- comparable to other rocky planets. Carbon-rich disks have ample
-- solid material for moon-forming impacts and co-accretion.
UPDATE ref.planet_type_ref
SET
    min_moon_system_mass_ratio = 0.00005,
    max_moon_system_mass_ratio = 0.002
WHERE name = 'Carbon Planet';