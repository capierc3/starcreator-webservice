CREATE SCHEMA ref;

CREATE TABLE ref.government_type (
    id SERIAL PRIMARY KEY,
    name character varying(255),
    faction_type character varying(255),
    description character varying(255)
);

CREATE TABLE ref.main_seq (
    type character(1) NOT NULL,
    min_mass double precision,
    max_mass double precision
);

CREATE TABLE ref.name_prefix (
    id SERIAL PRIMARY KEY,
    prefix character varying(50) NOT NULL
);

CREATE TABLE ref.name_suffix (
    id SERIAL PRIMARY KEY,
    suffix character varying(50) NOT NULL
);

CREATE TABLE ref.star_type (
    id SERIAL PRIMARY KEY,
    name character varying(255) NOT NULL,
    spectral_class character varying(255),
    min_mass double precision NOT NULL,
    max_mass double precision NOT NULL,
    mass_radius_exponent double precision,
    radius_multiplier_min double precision,
    radius_multiplier_max double precision,
    description character varying(255),
    rarity_weight integer DEFAULT 1 NOT NULL,
    type character varying(100)
);

