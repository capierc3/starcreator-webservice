CREATE TABLE ud.star (
    id BIGINT PRIMARY KEY,
    solar_mass DOUBLE PRECISION,
    solar_radius DOUBLE PRECISION,
    spectral_type VARCHAR(50),
    solar_luminosity DOUBLE PRECISION,
    surface_temp DOUBLE PRECISION,
    star_type int,
    FOREIGN KEY (id) REFERENCES ud.celestial_body(id),
    FOREIGN KEY (star_type) REFERENCES ref.star_type(rarity)
);


CREATE TABLE ref.main_seq
(
    type char PRIMARY KEY,
    min_mass DOUBLE PRECISION,
    max_mass DOUBLE PRECISION,
    min_radius DOUBLE PRECISION,
    max_radius DOUBLE PRECISION
)



