-- Mars-like: extend max from 0.5 to 2.0 ME to cover Desert and Terrestrial planets
UPDATE ref.atmosphere_template
SET max_planet_mass_earth = 2.0
WHERE name = 'Mars-like';

-- Earth-like: extend max from 2 to 10 ME to cover Super-Earths
UPDATE ref.atmosphere_template
SET max_planet_mass_earth = 10
WHERE name = 'Earth-like';

-- Venus-like: extend max from 2 to 10 ME to cover Super-Earths
UPDATE ref.atmosphere_template
SET max_planet_mass_earth = 10
WHERE name = 'Venus-like';

-- Volcanic: extend max from 3 to 10 ME to cover Super-Earths
UPDATE ref.atmosphere_template
SET max_planet_mass_earth = 10
WHERE name = 'Volcanic';

-- Volcanic: lower min from 0.3 to 0.05 ME to cover small Hot Rocky planets
UPDATE ref.atmosphere_template
SET min_planet_mass_earth = 0.05
WHERE name = 'Volcanic';

-- Exotic: lower min from 0.5 to 0.05 ME to cover Hot Rocky planets
UPDATE ref.atmosphere_template
SET min_planet_mass_earth = 0.05
WHERE name = 'Exotic Metallic';

-- Ammonia: extend max from 5 to 10 ME to cover larger Super-Earths
UPDATE ref.atmosphere_template
SET max_planet_mass_earth = 10
WHERE name = 'Ammonia';