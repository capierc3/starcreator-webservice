CREATE SCHEMA ud;

CREATE TABLE ud.celestial_body (
    id SERIAL PRIMARY KEY,
    name character varying(255),
    mass double precision,
    radius double precision,
    circumference double precision,
    created_at timestamp without time zone,
    modified_at timestamp without time zone
);

CREATE TABLE ud.factions (
    id SERIAL PRIMARY KEY,
    alignment character varying(255),
    description character varying(255),
    name character varying(255),
    type character varying(255),
    influence integer,
    government_type integer,
    ai_created boolean,
    created_at timestamp without time zone,
    modified_on timestamp without time zone
);

CREATE TABLE ud.star (
    id SERIAL PRIMARY KEY,
    solar_mass double precision,
    solar_radius double precision,
    spectral_type character varying(255),
    solar_luminosity double precision,
    surface_temp double precision,
    star_type integer,
    type character varying(255)
);

ALTER TABLE ud.factions
    ADD CONSTRAINT factions_government_type_id_fk FOREIGN KEY (government_type) REFERENCES ref.government_type(id);

ALTER TABLE ud.factions
    ADD CONSTRAINT fkslqdxy6wf9yo0mn9eq7h4byu2 FOREIGN KEY (government_type) REFERENCES ref.government_type(id);

ALTER TABLE ud.star
    ADD CONSTRAINT star_id_fkey FOREIGN KEY (id) REFERENCES ud.celestial_body(id);

