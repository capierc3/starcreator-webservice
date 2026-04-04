-- ═══════════════════════════════════════════════════════════════════
-- StarCreator Initial Schema and Reference Data
-- Generated from Feb 27 backup + V113-V121 migrations
-- ═══════════════════════════════════════════════════════════════════

--
-- PostgreSQL database dump
--


-- Dumped from database version 18.1 (Debian 18.1-1.pgdg13+2)
-- Dumped by pg_dump version 18.1 (Debian 18.1-1.pgdg13+2)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: ref; Type: SCHEMA; Schema: -; Owner: starcreator_dev
--

CREATE SCHEMA ref;


ALTER SCHEMA ref OWNER TO starcreator_dev;


--
-- Name: ud; Type: SCHEMA; Schema: -; Owner: starcreator_dev
--

CREATE SCHEMA ud;


ALTER SCHEMA ud OWNER TO starcreator_dev;


--
-- Name: update_modified_at_column(); Type: FUNCTION; Schema: public; Owner: starcreator_dev
--

CREATE FUNCTION public.update_modified_at_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.modified_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_modified_at_column() OWNER TO starcreator_dev;

SET default_tablespace = '';

SET default_table_access_method = heap;


--
-- Name: asteroid_type; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.asteroid_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    code character varying(255) NOT NULL,
    spectral_class character varying(255),
    description character varying(255),
    primary_composition character varying(255),
    albedo_min double precision,
    albedo_max double precision,
    density_min double precision,
    density_max double precision,
    belt_affinity character varying(255),
    relative_abundance double precision,
    can_be_differentiated boolean DEFAULT false,
    typical_surface_features character varying(255),
    created_at timestamp without time zone DEFAULT now()
);


ALTER TABLE ref.asteroid_type OWNER TO starcreator_dev;


--
-- Name: TABLE asteroid_type; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.asteroid_type IS 'Reference data defining asteroid spectral and compositional types';



--
-- Name: asteroid_type_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

CREATE SEQUENCE ref.asteroid_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ref.asteroid_type_id_seq OWNER TO starcreator_dev;


--
-- Name: asteroid_type_id_seq; Type: SEQUENCE OWNED BY; Schema: ref; Owner: starcreator_dev
--

ALTER SEQUENCE ref.asteroid_type_id_seq OWNED BY ref.asteroid_type.id;



--
-- Name: atmosphere_template; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.atmosphere_template (
    id bigint NOT NULL,
    name character varying(50) NOT NULL,
    description text,
    classification character varying(50) NOT NULL,
    is_breathable boolean DEFAULT false,
    typical_pressure_bar double precision DEFAULT 1.0,
    min_temperature_k double precision,
    max_temperature_k double precision,
    min_planet_mass_earth double precision DEFAULT 0.0,
    max_planet_mass_earth double precision DEFAULT 1000.0,
    rarity_weight integer DEFAULT 100,
    created_at timestamp without time zone DEFAULT now()
);


ALTER TABLE ref.atmosphere_template OWNER TO starcreator_dev;


--
-- Name: TABLE atmosphere_template; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.atmosphere_template IS 'Pre-defined atmosphere templates (Earth-like, Venus-like, etc.)';



--
-- Name: COLUMN atmosphere_template.name; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.atmosphere_template.name IS 'Display name of atmosphere type';



--
-- Name: COLUMN atmosphere_template.classification; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.atmosphere_template.classification IS 'Enum value from AtmosphereClassification';



--
-- Name: COLUMN atmosphere_template.is_breathable; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.atmosphere_template.is_breathable IS 'Whether humans can breathe this atmosphere';



--
-- Name: COLUMN atmosphere_template.typical_pressure_bar; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.atmosphere_template.typical_pressure_bar IS 'Typical surface pressure in bar (Earth = 1.0)';



--
-- Name: COLUMN atmosphere_template.min_temperature_k; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.atmosphere_template.min_temperature_k IS 'Minimum viable temperature for this atmosphere';



--
-- Name: COLUMN atmosphere_template.max_temperature_k; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.atmosphere_template.max_temperature_k IS 'Maximum viable temperature for this atmosphere';



--
-- Name: COLUMN atmosphere_template.rarity_weight; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.atmosphere_template.rarity_weight IS 'Weight for random selection (higher = more common)';



--
-- Name: atmosphere_template_component; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.atmosphere_template_component (
    id bigint NOT NULL,
    template_id bigint NOT NULL,
    gas_formula character varying(10) NOT NULL,
    min_percentage double precision NOT NULL,
    max_percentage double precision NOT NULL,
    is_trace boolean DEFAULT false,
    CONSTRAINT valid_percentage CHECK (((min_percentage >= (0)::double precision) AND (max_percentage <= (100)::double precision) AND (min_percentage <= max_percentage)))
);


ALTER TABLE ref.atmosphere_template_component OWNER TO starcreator_dev;


--
-- Name: TABLE atmosphere_template_component; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.atmosphere_template_component IS 'Gas components for each atmosphere template';



--
-- Name: COLUMN atmosphere_template_component.gas_formula; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.atmosphere_template_component.gas_formula IS 'Chemical formula (N2, O2, etc.) matching AtmosphereGas enum';



--
-- Name: COLUMN atmosphere_template_component.min_percentage; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.atmosphere_template_component.min_percentage IS 'Minimum percentage of this gas in the atmosphere';



--
-- Name: COLUMN atmosphere_template_component.max_percentage; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.atmosphere_template_component.max_percentage IS 'Maximum percentage of this gas in the atmosphere';



--
-- Name: COLUMN atmosphere_template_component.is_trace; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.atmosphere_template_component.is_trace IS 'Whether this is a trace gas (<0.1%)';



--
-- Name: atmosphere_template_component_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

CREATE SEQUENCE ref.atmosphere_template_component_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ref.atmosphere_template_component_id_seq OWNER TO starcreator_dev;


--
-- Name: atmosphere_template_component_id_seq; Type: SEQUENCE OWNED BY; Schema: ref; Owner: starcreator_dev
--

ALTER SEQUENCE ref.atmosphere_template_component_id_seq OWNED BY ref.atmosphere_template_component.id;



--
-- Name: atmosphere_template_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

CREATE SEQUENCE ref.atmosphere_template_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ref.atmosphere_template_id_seq OWNER TO starcreator_dev;


--
-- Name: atmosphere_template_id_seq; Type: SEQUENCE OWNED BY; Schema: ref; Owner: starcreator_dev
--

ALTER SEQUENCE ref.atmosphere_template_id_seq OWNED BY ref.atmosphere_template.id;



--
-- Name: belt_type; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.belt_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    code character varying(255) NOT NULL,
    description character varying(255),
    typical_composition_type character varying(255),
    min_eccentricity double precision,
    max_eccentricity double precision,
    min_inclination_degrees double precision,
    max_inclination_degrees double precision,
    typical_mass_earth_masses_min double precision,
    typical_mass_earth_masses_max double precision,
    notable_object_chance double precision,
    max_notable_objects integer,
    requires_giant_planet boolean DEFAULT true,
    min_system_age_my double precision,
    location_description character varying(255),
    formation_description character varying(255),
    created_at timestamp without time zone DEFAULT now()
);


ALTER TABLE ref.belt_type OWNER TO starcreator_dev;


--
-- Name: TABLE belt_type; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.belt_type IS 'Reference data defining belt type characteristics';



--
-- Name: belt_type_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

CREATE SEQUENCE ref.belt_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ref.belt_type_id_seq OWNER TO starcreator_dev;


--
-- Name: belt_type_id_seq; Type: SEQUENCE OWNED BY; Schema: ref; Owner: starcreator_dev
--

ALTER SEQUENCE ref.belt_type_id_seq OWNED BY ref.belt_type.id;



--
-- Name: cloud_composition_template; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.cloud_composition_template (
    id integer NOT NULL,
    atmosphere_classification character varying(50) NOT NULL,
    substance character varying(50) NOT NULL,
    min_temperature_k double precision,
    max_temperature_k double precision,
    typical_altitude_scale_heights double precision,
    opacity character varying(30),
    color character varying(50),
    description character varying(200),
    weight integer DEFAULT 100,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE ref.cloud_composition_template OWNER TO starcreator_dev;


--
-- Name: TABLE cloud_composition_template; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.cloud_composition_template IS 'Reference data for cloud layer generation by atmosphere type and temperature range.';



--
-- Name: cloud_composition_template_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ref.cloud_composition_template ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME ref.cloud_composition_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



--
-- Name: composition_template; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.composition_template (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    description character varying(500),
    classification character varying(50) NOT NULL,
    planet_types character varying(500),
    rarity_weight integer DEFAULT 100,
    created_at timestamp without time zone DEFAULT now(),
    min_surface_temp_k double precision,
    max_surface_temp_k double precision,
    CONSTRAINT valid_temp_range CHECK (((min_surface_temp_k IS NULL) OR (max_surface_temp_k IS NULL) OR (min_surface_temp_k <= max_surface_temp_k)))
);


ALTER TABLE ref.composition_template OWNER TO starcreator_dev;


--
-- Name: TABLE composition_template; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.composition_template IS 'After V49: All Small moon templates include SHEPHERD. Note: SHEPHERD moons should have formationType=CO_FORMED not CAPTURED (code fix needed in MoonCreator.java setFormationType method)';



--
-- Name: COLUMN composition_template.planet_types; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.composition_template.planet_types IS 'Comma-separated list of applicable planet types';



--
-- Name: COLUMN composition_template.rarity_weight; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.composition_template.rarity_weight IS 'Weight for random selection (higher = more common)';



--
-- Name: COLUMN composition_template.min_surface_temp_k; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.composition_template.min_surface_temp_k IS 'Minimum surface temperature in Kelvin for this composition to be viable';



--
-- Name: COLUMN composition_template.max_surface_temp_k; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.composition_template.max_surface_temp_k IS 'Maximum surface temperature in Kelvin for this composition to be viable';



--
-- Name: composition_template_component; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.composition_template_component (
    id integer NOT NULL,
    template_id integer NOT NULL,
    mineral character varying(50) NOT NULL,
    layer_type character varying(10) NOT NULL,
    min_percentage double precision NOT NULL,
    max_percentage double precision NOT NULL,
    CONSTRAINT composition_template_component_layer_type_check CHECK (((layer_type)::text = ANY (ARRAY[('INTERIOR'::character varying)::text, ('ENVELOPE'::character varying)::text]))),
    CONSTRAINT valid_percentage CHECK (((min_percentage >= (0)::double precision) AND (max_percentage <= (100)::double precision) AND (min_percentage <= max_percentage)))
);


ALTER TABLE ref.composition_template_component OWNER TO starcreator_dev;


--
-- Name: TABLE composition_template_component; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.composition_template_component IS 'Mineral components for each composition template';



--
-- Name: COLUMN composition_template_component.layer_type; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.composition_template_component.layer_type IS 'INTERIOR (core/mantle) or ENVELOPE (crust/atmosphere/surface)';



--
-- Name: composition_template_component_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

CREATE SEQUENCE ref.composition_template_component_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ref.composition_template_component_id_seq OWNER TO starcreator_dev;


--
-- Name: composition_template_component_id_seq; Type: SEQUENCE OWNED BY; Schema: ref; Owner: starcreator_dev
--

ALTER SEQUENCE ref.composition_template_component_id_seq OWNED BY ref.composition_template_component.id;



--
-- Name: composition_template_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

CREATE SEQUENCE ref.composition_template_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ref.composition_template_id_seq OWNER TO starcreator_dev;


--
-- Name: composition_template_id_seq; Type: SEQUENCE OWNED BY; Schema: ref; Owner: starcreator_dev
--

ALTER SEQUENCE ref.composition_template_id_seq OWNED BY ref.composition_template.id;



--
-- Name: geological_template; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.geological_template (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    description text,
    planet_types character varying(500),
    activity_level character varying(50) NOT NULL,
    min_activity_score double precision,
    max_activity_score double precision,
    min_planet_mass_earth double precision DEFAULT 0.0,
    max_planet_mass_earth double precision DEFAULT 10000.0,
    rarity_weight integer DEFAULT 100,
    created_at timestamp without time zone DEFAULT now(),
    composition_types character varying(200)
);


ALTER TABLE ref.geological_template OWNER TO starcreator_dev;


--
-- Name: TABLE geological_template; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.geological_template IS 'Pre-defined geological activity templates for planets and moons';



--
-- Name: COLUMN geological_template.planet_types; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.geological_template.planet_types IS 'Comma-separated list of planet types OR moon types (REGULAR_LARGE, COLLISION_DEBRIS, etc.)';



--
-- Name: COLUMN geological_template.activity_level; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.geological_template.activity_level IS 'Activity level - for planets: calculated from mass/age, for moons: maps from geologicalActivity field (NONE→Dead, LOW→Low Activity, MODERATE→Moderately Active, HIGH→Highly Active)';



--
-- Name: COLUMN geological_template.min_activity_score; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.geological_template.min_activity_score IS 'Minimum activity score (mass/age ratio)';



--
-- Name: COLUMN geological_template.max_activity_score; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.geological_template.max_activity_score IS 'Maximum activity score (mass/age ratio)';



--
-- Name: geological_template_feature; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.geological_template_feature (
    id integer NOT NULL,
    template_id integer NOT NULL,
    feature_type character varying(50) NOT NULL,
    feature_value character varying(100) NOT NULL,
    min_value double precision,
    max_value double precision,
    description text
);


ALTER TABLE ref.geological_template_feature OWNER TO starcreator_dev;


--
-- Name: TABLE geological_template_feature; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.geological_template_feature IS 'Features associated with each geological template';



--
-- Name: COLUMN geological_template_feature.feature_type; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.geological_template_feature.feature_type IS 'Type of feature (VOLCANISM, TECTONICS, TERRAIN, EROSION, etc.)';



--
-- Name: COLUMN geological_template_feature.feature_value; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.geological_template_feature.feature_value IS 'String value for categorical features';



--
-- Name: COLUMN geological_template_feature.min_value; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.geological_template_feature.min_value IS 'Minimum value for numeric features';



--
-- Name: COLUMN geological_template_feature.max_value; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.geological_template_feature.max_value IS 'Maximum value for numeric features';



--
-- Name: geological_template_feature_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

CREATE SEQUENCE ref.geological_template_feature_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ref.geological_template_feature_id_seq OWNER TO starcreator_dev;


--
-- Name: geological_template_feature_id_seq; Type: SEQUENCE OWNED BY; Schema: ref; Owner: starcreator_dev
--

ALTER SEQUENCE ref.geological_template_feature_id_seq OWNED BY ref.geological_template_feature.id;



--
-- Name: geological_template_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

CREATE SEQUENCE ref.geological_template_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ref.geological_template_id_seq OWNER TO starcreator_dev;


--
-- Name: geological_template_id_seq; Type: SEQUENCE OWNED BY; Schema: ref; Owner: starcreator_dev
--

ALTER SEQUENCE ref.geological_template_id_seq OWNED BY ref.geological_template.id;



--
-- Name: government_type; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.government_type (
    id integer NOT NULL,
    name character varying(255),
    faction_type character varying(255),
    description character varying(255)
);


ALTER TABLE ref.government_type OWNER TO starcreator_dev;


--
-- Name: moon_type_ref; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.moon_type_ref (
    id bigint NOT NULL,
    moon_type character varying(50) NOT NULL,
    description text,
    typical_formation character varying(50),
    min_mass_earth_masses double precision,
    max_mass_earth_masses double precision,
    typical_composition character varying(50),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    mass_distribution_priority integer DEFAULT 5,
    CONSTRAINT chk_mass_priority_non_negative CHECK ((mass_distribution_priority >= 0))
);


ALTER TABLE ref.moon_type_ref OWNER TO starcreator_dev;


--
-- Name: COLUMN moon_type_ref.mass_distribution_priority; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.moon_type_ref.mass_distribution_priority IS 'Priority order for mass distribution (lower = gets mass first, 0 = highest priority)';



--
-- Name: moon_type_ref_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

CREATE SEQUENCE ref.moon_type_ref_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ref.moon_type_ref_id_seq OWNER TO starcreator_dev;


--
-- Name: moon_type_ref_id_seq; Type: SEQUENCE OWNED BY; Schema: ref; Owner: starcreator_dev
--

ALTER SEQUENCE ref.moon_type_ref_id_seq OWNED BY ref.moon_type_ref.id;



--
-- Name: name_origin_mapping; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.name_origin_mapping (
    name_id bigint NOT NULL,
    origin_id bigint NOT NULL
);


ALTER TABLE ref.name_origin_mapping OWNER TO starcreator_dev;


--
-- Name: TABLE name_origin_mapping; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.name_origin_mapping IS 'Links names to their cultural origins';



--
-- Name: name_origin_ref; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.name_origin_ref (
    id bigint NOT NULL,
    name character varying(100) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE ref.name_origin_ref OWNER TO starcreator_dev;


--
-- Name: TABLE name_origin_ref; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.name_origin_ref IS 'Cultural/linguistic origins for names (e.g., French, Yoruba)';



--
-- Name: name_origin_ref_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

CREATE SEQUENCE ref.name_origin_ref_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ref.name_origin_ref_id_seq OWNER TO starcreator_dev;


--
-- Name: name_origin_ref_id_seq; Type: SEQUENCE OWNED BY; Schema: ref; Owner: starcreator_dev
--

ALTER SEQUENCE ref.name_origin_ref_id_seq OWNED BY ref.name_origin_ref.id;



--
-- Name: name_ref; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.name_ref (
    id bigint NOT NULL,
    name character varying(100) NOT NULL,
    is_first boolean DEFAULT false NOT NULL,
    is_last boolean DEFAULT false NOT NULL,
    gender character varying(20) DEFAULT 'unisex'::character varying NOT NULL,
    popularity integer DEFAULT 1 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT name_ref_gender_chk CHECK (((gender)::text = ANY ((ARRAY['male'::character varying, 'female'::character varying, 'unisex'::character varying])::text[]))),
    CONSTRAINT name_ref_popularity_chk CHECK ((popularity > 0)),
    CONSTRAINT name_ref_usage_chk CHECK ((is_first OR is_last))
);


ALTER TABLE ref.name_ref OWNER TO starcreator_dev;


--
-- Name: TABLE name_ref; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.name_ref IS 'Reference data for first and last names with cultural origins';



--
-- Name: COLUMN name_ref.gender; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.name_ref.gender IS 'Gender association: male, female, or unisex';



--
-- Name: COLUMN name_ref.popularity; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.name_ref.popularity IS 'Weight for random selection (higher = more common)';



--
-- Name: name_ref_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

CREATE SEQUENCE ref.name_ref_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ref.name_ref_id_seq OWNER TO starcreator_dev;


--
-- Name: name_ref_id_seq; Type: SEQUENCE OWNED BY; Schema: ref; Owner: starcreator_dev
--

ALTER SEQUENCE ref.name_ref_id_seq OWNED BY ref.name_ref.id;



--
-- Name: name_region_ref; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.name_region_ref (
    id bigint NOT NULL,
    name character varying(100) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE ref.name_region_ref OWNER TO starcreator_dev;


--
-- Name: TABLE name_region_ref; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.name_region_ref IS 'Geographic regions for grouping origins';



--
-- Name: name_region_ref_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

CREATE SEQUENCE ref.name_region_ref_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ref.name_region_ref_id_seq OWNER TO starcreator_dev;


--
-- Name: name_region_ref_id_seq; Type: SEQUENCE OWNED BY; Schema: ref; Owner: starcreator_dev
--

ALTER SEQUENCE ref.name_region_ref_id_seq OWNED BY ref.name_region_ref.id;



--
-- Name: origin_region_mapping; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.origin_region_mapping (
    origin_id bigint NOT NULL,
    region_id bigint NOT NULL
);


ALTER TABLE ref.origin_region_mapping OWNER TO starcreator_dev;


--
-- Name: TABLE origin_region_mapping; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.origin_region_mapping IS 'Links cultural origins to geographic regions';



--
-- Name: planet_atmosphere_compatibility; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.planet_atmosphere_compatibility (
    id integer NOT NULL,
    planet_type character varying(100) NOT NULL,
    atmosphere_classification character varying(50) CONSTRAINT planet_atmosphere_compatibil_atmosphere_classification_not_null NOT NULL,
    preference_weight integer DEFAULT 100,
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE ref.planet_atmosphere_compatibility OWNER TO starcreator_dev;


--
-- Name: TABLE planet_atmosphere_compatibility; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.planet_atmosphere_compatibility IS 'After V43: Rocky Moon type removed from VOLCANIC - only Volcanic Moon type gets volcanic atmospheres';



--
-- Name: COLUMN planet_atmosphere_compatibility.preference_weight; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.planet_atmosphere_compatibility.preference_weight IS 'Higher weight = more likely to be selected (100 = normal, 200 = preferred, 50 = rare but possible)';



--
-- Name: planet_atmosphere_compatibility_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

CREATE SEQUENCE ref.planet_atmosphere_compatibility_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ref.planet_atmosphere_compatibility_id_seq OWNER TO starcreator_dev;


--
-- Name: planet_atmosphere_compatibility_id_seq; Type: SEQUENCE OWNED BY; Schema: ref; Owner: starcreator_dev
--

ALTER SEQUENCE ref.planet_atmosphere_compatibility_id_seq OWNED BY ref.planet_atmosphere_compatibility.id;



--
-- Name: planet_type_ref; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.planet_type_ref (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    description character varying(255),
    min_mass_earth double precision NOT NULL,
    max_mass_earth double precision NOT NULL,
    min_radius_earth double precision NOT NULL,
    max_radius_earth double precision NOT NULL,
    typical_density_g_cm3 double precision,
    typical_albedo double precision DEFAULT 0.3,
    typical_core_type character varying(255),
    formation_zone character varying(255),
    can_have_atmosphere boolean DEFAULT true,
    typical_atmosphere character varying(255),
    can_have_rings boolean DEFAULT false,
    ring_probability double precision DEFAULT 0.0,
    min_moons integer DEFAULT 0,
    max_moons integer DEFAULT 0,
    habitable boolean DEFAULT false,
    rarity_weight integer DEFAULT 100 NOT NULL,
    min_formation_temp_k double precision,
    max_formation_temp_k double precision,
    min_moon_system_mass_ratio double precision,
    max_moon_system_mass_ratio double precision,
    CONSTRAINT chk_moon_mass_ratio_positive CHECK (((min_moon_system_mass_ratio IS NULL) OR (min_moon_system_mass_ratio >= (0)::double precision))),
    CONSTRAINT chk_moon_mass_ratio_range CHECK (((min_moon_system_mass_ratio IS NULL) OR (max_moon_system_mass_ratio IS NULL) OR (min_moon_system_mass_ratio <= max_moon_system_mass_ratio))),
    CONSTRAINT chk_planet_type_albedo_range CHECK (((typical_albedo >= (0)::double precision) AND (typical_albedo <= (1)::double precision))),
    CONSTRAINT chk_planet_type_density_positive CHECK (((typical_density_g_cm3 IS NULL) OR (typical_density_g_cm3 > (0)::double precision))),
    CONSTRAINT chk_planet_type_mass_range CHECK ((max_mass_earth >= min_mass_earth)),
    CONSTRAINT chk_planet_type_moons_range CHECK ((max_moons >= min_moons)),
    CONSTRAINT chk_planet_type_radius_range CHECK ((max_radius_earth >= min_radius_earth)),
    CONSTRAINT chk_planet_type_rarity_positive CHECK ((rarity_weight > 0)),
    CONSTRAINT chk_planet_type_ring_probability CHECK (((ring_probability >= (0)::double precision) AND (ring_probability <= (1)::double precision))),
    CONSTRAINT valid_formation_temp_range CHECK (((min_formation_temp_k IS NULL) OR (max_formation_temp_k IS NULL) OR (min_formation_temp_k <= max_formation_temp_k)))
);


ALTER TABLE ref.planet_type_ref OWNER TO starcreator_dev;


--
-- Name: TABLE planet_type_ref; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.planet_type_ref IS 'Reference data for planet types defining characteristics and generation parameters';



--
-- Name: COLUMN planet_type_ref.formation_zone; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.planet_type_ref.formation_zone IS 'Preferred formation location: inner (hot), habitable, frost_line, outer (cold)';



--
-- Name: COLUMN planet_type_ref.rarity_weight; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.planet_type_ref.rarity_weight IS 'Weighted probability for random generation - higher values are more common';



--
-- Name: COLUMN planet_type_ref.min_formation_temp_k; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.planet_type_ref.min_formation_temp_k IS 'Minimum equilibrium temperature in Kelvin for this planet type to form/exist';



--
-- Name: COLUMN planet_type_ref.max_formation_temp_k; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.planet_type_ref.max_formation_temp_k IS 'Maximum equilibrium temperature in Kelvin for this planet type to form/exist';



--
-- Name: COLUMN planet_type_ref.min_moon_system_mass_ratio; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.planet_type_ref.min_moon_system_mass_ratio IS 'Minimum ratio of total moon system mass to planet mass (e.g., 0.0001 = 0.01% of planet mass)';



--
-- Name: COLUMN planet_type_ref.max_moon_system_mass_ratio; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.planet_type_ref.max_moon_system_mass_ratio IS 'Maximum ratio of total moon system mass to planet mass (e.g., 0.0005 = 0.05% of planet mass)';



--
-- Name: planet_type_ref_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

CREATE SEQUENCE ref.planet_type_ref_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ref.planet_type_ref_id_seq OWNER TO starcreator_dev;


--
-- Name: planet_type_ref_id_seq; Type: SEQUENCE OWNED BY; Schema: ref; Owner: starcreator_dev
--

ALTER SEQUENCE ref.planet_type_ref_id_seq OWNED BY ref.planet_type_ref.id;



--
-- Name: precipitation_template; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.precipitation_template (
    id integer NOT NULL,
    cloud_substance character varying(50) NOT NULL,
    surface_temp_min_k double precision,
    surface_temp_max_k double precision,
    phase character varying(30) NOT NULL,
    reaches_surface boolean DEFAULT true,
    description character varying(200),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE ref.precipitation_template OWNER TO starcreator_dev;


--
-- Name: TABLE precipitation_template; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.precipitation_template IS 'Maps cloud substance + surface temperature to precipitation type and phase.';



--
-- Name: precipitation_template_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ref.precipitation_template ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME ref.precipitation_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



--
-- Name: ring_template; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.ring_template (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    description character varying(500),
    ring_type character varying(50) NOT NULL,
    planet_types character varying(500),
    min_planet_mass_earth double precision,
    max_planet_mass_earth double precision,
    min_distance_au double precision,
    max_distance_au double precision,
    inner_radius_min_planet_radii double precision,
    inner_radius_max_planet_radii double precision,
    outer_radius_min_planet_radii double precision,
    outer_radius_max_planet_radii double precision,
    thickness_min_km double precision,
    thickness_max_km double precision,
    optical_depth_min double precision,
    optical_depth_max double precision,
    particle_size_min_m double precision,
    particle_size_max_m double precision,
    composition_type character varying(50),
    composition_description character varying(500),
    albedo_min double precision,
    albedo_max double precision,
    color character varying(100),
    visibility character varying(30),
    has_gaps_probability double precision,
    has_shepherd_moons_probability double precision,
    stability character varying(30),
    origin_type character varying(50),
    rarity_weight integer DEFAULT 100,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE ref.ring_template OWNER TO starcreator_dev;


--
-- Name: TABLE ring_template; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.ring_template IS 'Templates for procedurally generating scientifically accurate planetary rings';



--
-- Name: ring_template_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

CREATE SEQUENCE ref.ring_template_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ref.ring_template_id_seq OWNER TO starcreator_dev;


--
-- Name: ring_template_id_seq; Type: SEQUENCE OWNED BY; Schema: ref; Owner: starcreator_dev
--

ALTER SEQUENCE ref.ring_template_id_seq OWNED BY ref.ring_template.id;



--
-- Name: star_type; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.star_type (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    spectral_class character varying(255),
    min_mass double precision NOT NULL,
    max_mass double precision NOT NULL,
    mass_radius_exponent double precision,
    radius_multiplier_min double precision,
    radius_multiplier_max double precision,
    description character varying(255),
    rarity_weight integer DEFAULT 1 NOT NULL,
    type character varying(100),
    min_planet_formation_au double precision,
    max_planet_formation_au double precision,
    CONSTRAINT chk_planet_formation_positive CHECK ((((min_planet_formation_au IS NULL) OR (min_planet_formation_au > (0)::double precision)) AND ((max_planet_formation_au IS NULL) OR (max_planet_formation_au > (0)::double precision)))),
    CONSTRAINT chk_planet_formation_range CHECK (((min_planet_formation_au IS NULL) OR (max_planet_formation_au IS NULL) OR (min_planet_formation_au <= max_planet_formation_au)))
);


ALTER TABLE ref.star_type OWNER TO starcreator_dev;


--
-- Name: COLUMN star_type.min_planet_formation_au; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.star_type.min_planet_formation_au IS 'Minimum orbital distance in AU where planets can form (inner edge of protoplanetary disk)';



--
-- Name: COLUMN star_type.max_planet_formation_au; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.star_type.max_planet_formation_au IS 'Maximum orbital distance in AU for planetary system extent (outer disk boundary)';



--
-- Name: star_type_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

CREATE SEQUENCE ref.star_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ref.star_type_id_seq OWNER TO starcreator_dev;


--
-- Name: star_type_id_seq; Type: SEQUENCE OWNED BY; Schema: ref; Owner: starcreator_dev
--

ALTER SEQUENCE ref.star_type_id_seq OWNED BY ref.star_type.id;



--
-- Name: terrain_category_ref; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.terrain_category_ref (
    id integer NOT NULL,
    category character varying(50) NOT NULL,
    display_name character varying(100) NOT NULL,
    description text,
    base_weight integer DEFAULT 100 NOT NULL,
    typical_min_coverage double precision DEFAULT 0.0,
    typical_max_coverage double precision DEFAULT 100.0,
    is_major_terrain boolean DEFAULT true,
    is_rare boolean DEFAULT false,
    created_at timestamp without time zone DEFAULT now()
);


ALTER TABLE ref.terrain_category_ref OWNER TO starcreator_dev;


--
-- Name: TABLE terrain_category_ref; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.terrain_category_ref IS 'Controls how terrain categories are distributed across planet surfaces';



--
-- Name: COLUMN terrain_category_ref.base_weight; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.terrain_category_ref.base_weight IS 'Higher weight = more likely to be selected and allocated more surface area';



--
-- Name: COLUMN terrain_category_ref.is_major_terrain; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.terrain_category_ref.is_major_terrain IS 'Major terrains (plains, mountains) can cover large percentages';



--
-- Name: COLUMN terrain_category_ref.is_rare; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.terrain_category_ref.is_rare IS 'Rare terrains (exotic, artificial) appear in small amounts';



--
-- Name: terrain_category_ref_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

CREATE SEQUENCE ref.terrain_category_ref_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ref.terrain_category_ref_id_seq OWNER TO starcreator_dev;


--
-- Name: terrain_category_ref_id_seq; Type: SEQUENCE OWNED BY; Schema: ref; Owner: starcreator_dev
--

ALTER SEQUENCE ref.terrain_category_ref_id_seq OWNED BY ref.terrain_category_ref.id;



--
-- Name: terrain_type_ref; Type: TABLE; Schema: ref; Owner: starcreator_dev
--

CREATE TABLE ref.terrain_type_ref (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    display_name character varying(100) NOT NULL,
    description text,
    category character varying(50) NOT NULL,
    requires_liquid boolean DEFAULT false,
    requires_atmosphere boolean DEFAULT false,
    min_temperature_k double precision,
    max_temperature_k double precision,
    min_pressure_atm double precision,
    is_volcanic boolean DEFAULT false,
    is_frozen boolean DEFAULT false,
    is_aquatic boolean DEFAULT false,
    is_artificial boolean DEFAULT false,
    rarity_weight integer DEFAULT 100,
    typical_coverage_min double precision DEFAULT 0.0,
    typical_coverage_max double precision DEFAULT 100.0,
    created_at timestamp without time zone DEFAULT now(),
    cratering_weight_boost integer DEFAULT 0,
    volcanic_weight_boost integer DEFAULT 0,
    excluded_planet_types text[],
    required_composition_classes text[],
    excluded_composition_classes text[],
    volatile_type character varying(30)
);


ALTER TABLE ref.terrain_type_ref OWNER TO starcreator_dev;


--
-- Name: TABLE terrain_type_ref; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON TABLE ref.terrain_type_ref IS 'Reference data for terrain types with environmental requirements and rarity weights';



--
-- Name: COLUMN terrain_type_ref.category; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.terrain_type_ref.category IS 'Terrain category (AQUATIC, ICE, TEMPERATE, ARID, VOLCANIC, MOUNTAIN, EXOTIC, ARTIFICIAL)';



--
-- Name: COLUMN terrain_type_ref.requires_liquid; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.terrain_type_ref.requires_liquid IS 'Whether this terrain requires liquid water';



--
-- Name: COLUMN terrain_type_ref.requires_atmosphere; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.terrain_type_ref.requires_atmosphere IS 'Whether this terrain requires an atmosphere';



--
-- Name: COLUMN terrain_type_ref.rarity_weight; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.terrain_type_ref.rarity_weight IS 'Weight for random selection (higher = more common)';



--
-- Name: COLUMN terrain_type_ref.cratering_weight_boost; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.terrain_type_ref.cratering_weight_boost IS 'Additional weight when planet has heavy cratering (added to rarity_weight)';



--
-- Name: COLUMN terrain_type_ref.volcanic_weight_boost; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.terrain_type_ref.volcanic_weight_boost IS 'Additional weight when planet has volcanic activity (added to rarity_weight)';



--
-- Name: COLUMN terrain_type_ref.excluded_planet_types; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.terrain_type_ref.excluded_planet_types IS 'Array of planet type names that CANNOT have this terrain (e.g., Iron Planet cannot have glaciers). Note: Gas giants are excluded via code, not database.';



--
-- Name: COLUMN terrain_type_ref.required_composition_classes; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.terrain_type_ref.required_composition_classes IS 'Array of composition classification names required for this terrain (e.g., ICE_RICH for cryovolcanic features). If specified, planet MUST have one of these compositions.';



--
-- Name: COLUMN terrain_type_ref.excluded_composition_classes; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON COLUMN ref.terrain_type_ref.excluded_composition_classes IS 'Array of composition classification names that exclude this terrain (e.g., IRON_RICH excludes ice features). More flexible than required_composition_classes.';



--
-- Name: terrain_type_ref_id_seq; Type: SEQUENCE; Schema: ref; Owner: starcreator_dev
--

CREATE SEQUENCE ref.terrain_type_ref_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ref.terrain_type_ref_id_seq OWNER TO starcreator_dev;


--
-- Name: terrain_type_ref_id_seq; Type: SEQUENCE OWNED BY; Schema: ref; Owner: starcreator_dev
--

ALTER SEQUENCE ref.terrain_type_ref_id_seq OWNED BY ref.terrain_type_ref.id;



--
-- Name: v_plains_terrain_summary; Type: VIEW; Schema: ref; Owner: starcreator_dev
--

CREATE VIEW ref.v_plains_terrain_summary AS
 SELECT name,
    display_name,
    category,
    min_temperature_k,
    max_temperature_k,
    requires_liquid,
    requires_atmosphere,
    min_pressure_atm,
    excluded_planet_types,
    required_composition_classes,
    excluded_composition_classes,
    rarity_weight
   FROM ref.terrain_type_ref
  WHERE ((name)::text = ANY (ARRAY[('CRYOPLAINS'::character varying)::text, ('TUNDRA'::character varying)::text, ('BOREAL_PLAINS'::character varying)::text, ('GRASSLAND'::character varying)::text, ('STEPPE'::character varying)::text, ('SALT_FLATS'::character varying)::text, ('REGOLITH_FLATS'::character varying)::text, ('LAVA_PLAINS'::character varying)::text, ('CARBON_FLATS'::character varying)::text]))
  ORDER BY min_temperature_k, name;


ALTER VIEW ref.v_plains_terrain_summary OWNER TO starcreator_dev;


--
-- Name: VIEW v_plains_terrain_summary; Type: COMMENT; Schema: ref; Owner: starcreator_dev
--

COMMENT ON VIEW ref.v_plains_terrain_summary IS 'Summary of all plains/grassland terrain variants for easy comparison';



--
-- Name: asteroid; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.asteroid (
    id bigint NOT NULL,
    asteroid_type_id integer,
    name character varying(255),
    notable_reason character varying(255),
    created_at timestamp without time zone DEFAULT now(),
    modified_at timestamp without time zone DEFAULT now(),
    band_id bigint,
    rotation_properties_id bigint,
    physical_properties_id bigint,
    designation_id bigint,
    surface_features text,
    cratering_level character varying(20),
    has_regolith boolean,
    regolith_depth_m double precision,
    composition text,
    is_differentiated boolean DEFAULT false,
    core_type character varying(50),
    ice_percent double precision,
    semi_major_axis_au double precision
);


ALTER TABLE ud.asteroid OWNER TO starcreator_dev;


--
-- Name: TABLE asteroid; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON TABLE ud.asteroid IS 'Notable asteroids and KBOs within belt systems';



--
-- Name: asteroid_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.asteroid_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.asteroid_id_seq OWNER TO starcreator_dev;


--
-- Name: asteroid_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.asteroid_id_seq OWNED BY ud.asteroid.id;



--
-- Name: atmosphere; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.atmosphere (
    id bigint NOT NULL,
    classification character varying(50),
    surface_pressure_bar double precision,
    composition_summary text,
    scale_height_km double precision,
    greenhouse_effect_k double precision,
    is_stripped boolean DEFAULT false,
    stripped_reason character varying(100),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    modified_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    has_great_storm boolean,
    number_of_major_storms integer,
    atmospheric_convection_level character varying(50),
    CONSTRAINT chk_atmosphere_greenhouse_effect CHECK (((greenhouse_effect_k IS NULL) OR (greenhouse_effect_k >= (0)::double precision))),
    CONSTRAINT chk_atmosphere_pressure_positive CHECK (((surface_pressure_bar IS NULL) OR (surface_pressure_bar >= (0)::double precision))),
    CONSTRAINT chk_atmosphere_scale_height_positive CHECK (((scale_height_km IS NULL) OR (scale_height_km > (0)::double precision)))
);


ALTER TABLE ud.atmosphere OWNER TO starcreator_dev;


--
-- Name: TABLE atmosphere; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON TABLE ud.atmosphere IS 'Stores atmospheric properties for celestial bodies (planets and moons)';



--
-- Name: COLUMN atmosphere.classification; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.atmosphere.classification IS 'Atmosphere type from AtmosphereClassification enum (EARTH_LIKE, VENUS_LIKE, JOVIAN, etc.)';



--
-- Name: COLUMN atmosphere.surface_pressure_bar; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.atmosphere.surface_pressure_bar IS 'Surface atmospheric pressure in bar (1 bar = Earth sea level, 92 bar = Venus)';



--
-- Name: COLUMN atmosphere.composition_summary; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.atmosphere.composition_summary IS 'Human-readable summary of atmospheric composition (e.g., "N2 78%, O2 21%, Ar 1%")';



--
-- Name: COLUMN atmosphere.scale_height_km; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.atmosphere.scale_height_km IS 'Atmospheric scale height - altitude where pressure drops to 1/e of surface value';



--
-- Name: COLUMN atmosphere.greenhouse_effect_k; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.atmosphere.greenhouse_effect_k IS 'Temperature increase in Kelvin due to greenhouse effect';



--
-- Name: COLUMN atmosphere.is_stripped; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.atmosphere.is_stripped IS 'Whether the atmosphere was stripped by stellar wind, magnetic field, or other process';



--
-- Name: COLUMN atmosphere.stripped_reason; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.atmosphere.stripped_reason IS 'Reason for atmosphere loss (e.g., "PLANETARY_MAGNETIC_FIELD", "STELLAR_WIND", "LOW_MASS")';



--
-- Name: atmosphere_component; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.atmosphere_component (
    id bigint NOT NULL,
    atmosphere_id bigint,
    gas_formula character varying(10),
    percentage double precision,
    is_trace boolean DEFAULT false,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_atm_component_gas_not_empty CHECK (((gas_formula IS NOT NULL) AND ((gas_formula)::text <> ''::text))),
    CONSTRAINT chk_atm_component_percentage CHECK (((percentage >= (0)::double precision) AND (percentage <= (100)::double precision)))
);


ALTER TABLE ud.atmosphere_component OWNER TO starcreator_dev;


--
-- Name: TABLE atmosphere_component; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON TABLE ud.atmosphere_component IS 'Stores individual gas components of an atmosphere with their percentages';



--
-- Name: COLUMN atmosphere_component.gas_formula; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.atmosphere_component.gas_formula IS 'Chemical formula matching AtmosphereGas enum (N2, O2, CO2, H2, He, CH4, etc.)';



--
-- Name: COLUMN atmosphere_component.percentage; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.atmosphere_component.percentage IS 'Percentage of atmosphere composition (0-100). Sum should be ~100% per atmosphere.';



--
-- Name: COLUMN atmosphere_component.is_trace; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.atmosphere_component.is_trace IS 'True if this is a trace gas (< 0.1%), often displayed as "(trace)" instead of percentage';



--
-- Name: atmosphere_component_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.atmosphere_component_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.atmosphere_component_id_seq OWNER TO starcreator_dev;


--
-- Name: atmosphere_component_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.atmosphere_component_id_seq OWNED BY ud.atmosphere_component.id;



--
-- Name: atmosphere_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.atmosphere_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.atmosphere_id_seq OWNER TO starcreator_dev;


--
-- Name: atmosphere_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.atmosphere_id_seq OWNED BY ud.atmosphere.id;



--
-- Name: band_dwarf_planet; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.band_dwarf_planet (
    band_id bigint NOT NULL,
    planet_id bigint NOT NULL
);


ALTER TABLE ud.band_dwarf_planet OWNER TO starcreator_dev;


--
-- Name: composition_properties; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.composition_properties (
    id bigint NOT NULL,
    core_type character varying(255),
    interior_composition character varying(500),
    envelope_composition character varying(500),
    composition_classification character varying(50),
    composition_type character varying(50),
    composition character varying(255),
    is_differentiated boolean,
    created_at timestamp without time zone DEFAULT now(),
    modified_at timestamp without time zone DEFAULT now()
);


ALTER TABLE ud.composition_properties OWNER TO starcreator_dev;


--
-- Name: TABLE composition_properties; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON TABLE ud.composition_properties IS 'Composition properties for any celestial body (planet, moon, or asteroid)';



--
-- Name: composition_properties_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.composition_properties_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.composition_properties_id_seq OWNER TO starcreator_dev;


--
-- Name: composition_properties_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.composition_properties_id_seq OWNED BY ud.composition_properties.id;



--
-- Name: designation; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.designation (
    id bigint NOT NULL,
    body_type character varying(20),
    logged_name character varying(255),
    display_name character varying(255),
    age_my double precision,
    survey_history character varying(500),
    object_type character varying(100),
    habitable_zone_position character varying(50),
    parent_body_name character varying(255),
    spectral_type character varying(20),
    star_role character varying(20),
    color_index character varying(50),
    evolutionary_stage character varying(50),
    formation_type character varying(50),
    band_category character varying(20),
    designation_code character varying(100),
    created_at timestamp without time zone DEFAULT now(),
    modified_at timestamp without time zone DEFAULT now()
);


ALTER TABLE ud.designation OWNER TO starcreator_dev;


--
-- Name: TABLE designation; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON TABLE ud.designation IS 'Identity card for any celestial object: system, star, planet, moon, belt, ring, or asteroid';



--
-- Name: designation_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.designation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.designation_id_seq OWNER TO starcreator_dev;


--
-- Name: designation_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.designation_id_seq OWNED BY ud.designation.id;



--
-- Name: faction_presence; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.faction_presence (
    id bigint NOT NULL,
    faction_id bigint,
    system_id bigint,
    influence_level integer DEFAULT 0,
    is_controlling boolean DEFAULT false,
    since_date timestamp without time zone DEFAULT now()
);


ALTER TABLE ud.faction_presence OWNER TO starcreator_dev;


--
-- Name: faction_presence_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.faction_presence_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.faction_presence_id_seq OWNER TO starcreator_dev;


--
-- Name: faction_presence_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.faction_presence_id_seq OWNED BY ud.faction_presence.id;



--
-- Name: factions; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.factions (
    id bigint,
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


ALTER TABLE ud.factions OWNER TO starcreator_dev;


--
-- Name: moon; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.moon (
    id bigint NOT NULL,
    name character varying(255),
    mass double precision,
    radius double precision,
    circumference double precision,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    planet_id bigint,
    tidal_heating_level character varying(20),
    tidal_heating_watt_per_m2 double precision,
    is_shepherd_moon boolean DEFAULT false,
    shepherds_ring_name character varying(50),
    atmosphere_id bigint,
    orbital_elements_id bigint,
    terrain_id bigint,
    hydrology_id bigint,
    magnetic_field_id bigint,
    physical_properties_id bigint,
    rotation_properties_id bigint,
    composition_properties_id bigint,
    designation_id bigint,
    climate_seed bigint
);


ALTER TABLE ud.moon OWNER TO starcreator_dev;


--
-- Name: moon_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.moon_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.moon_id_seq OWNER TO starcreator_dev;


--
-- Name: moon_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.moon_id_seq OWNED BY ud.moon.id;



--
-- Name: orbital_band; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.orbital_band (
    id bigint NOT NULL,
    planet_id bigint,
    band_category character varying(20),
    belt_type_id integer,
    inner_orbit_id bigint,
    outer_orbit_id bigint,
    name character varying(100),
    description text,
    total_mass_kg double precision,
    total_mass_earth_masses double precision,
    composition_type character varying(50),
    primary_composition text,
    albedo double precision,
    peak_density_distance double precision,
    average_eccentricity double precision,
    max_eccentricity double precision,
    average_inclination_deg double precision,
    max_inclination_deg double precision,
    estimated_object_count bigint,
    has_gaps boolean DEFAULT false,
    gap_description text,
    has_collisional_families boolean DEFAULT false,
    family_count integer,
    thickness_km double precision,
    optical_depth double precision,
    particle_size_min_m double precision,
    particle_size_max_m double precision,
    color character varying(100),
    visibility character varying(30),
    has_shepherd_moons boolean DEFAULT false,
    orbital_resonances text,
    stability character varying(30),
    origin_type character varying(50),
    formation_description text,
    estimated_age_my double precision,
    created_at timestamp without time zone DEFAULT now(),
    modified_at timestamp without time zone DEFAULT now(),
    designation_id bigint,
    star_id bigint,
    lagrange_point character varying(5),
    libration_amplitude_deg double precision,
    CONSTRAINT chk_band_parent CHECK ((((star_id IS NOT NULL) AND (planet_id IS NULL)) OR ((star_id IS NULL) AND (planet_id IS NOT NULL))))
);


ALTER TABLE ud.orbital_band OWNER TO starcreator_dev;


--
-- Name: TABLE orbital_band; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON TABLE ud.orbital_band IS 'Unified annular band — replaces both belt (star-orbiting) and ring (planet-orbiting)';



--
-- Name: COLUMN orbital_band.band_category; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.orbital_band.band_category IS 'BELT for star-orbiting bands, RING for planet-orbiting bands';



--
-- Name: COLUMN orbital_band.inner_orbit_id; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.orbital_band.inner_orbit_id IS 'Orbital elements defining the inner edge of the band';



--
-- Name: COLUMN orbital_band.outer_orbit_id; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.orbital_band.outer_orbit_id IS 'Orbital elements defining the outer edge of the band';



--
-- Name: COLUMN orbital_band.lagrange_point; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.orbital_band.lagrange_point IS 'Lagrange point: L4 (60° ahead) or L5 (60° behind) — Trojan bands only';



--
-- Name: COLUMN orbital_band.libration_amplitude_deg; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.orbital_band.libration_amplitude_deg IS 'Angular spread from the Lagrange point in degrees (mean ~33°, range 0.6–88°) — Trojan bands only';



--
-- Name: orbital_band_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.orbital_band_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.orbital_band_id_seq OWNER TO starcreator_dev;


--
-- Name: orbital_band_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.orbital_band_id_seq OWNED BY ud.orbital_band.id;



--
-- Name: orbital_elements; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.orbital_elements (
    id bigint NOT NULL,
    semi_major_axis double precision,
    semi_major_axis_unit character varying(5) DEFAULT 'AU'::character varying,
    orbital_period_days double precision,
    eccentricity double precision,
    inclination_degrees double precision,
    longitude_of_ascending_node_deg double precision,
    argument_of_periapsis_deg double precision,
    mean_anomaly_deg double precision,
    orbit_stability character varying(20),
    orbit_stability_timescale_my double precision,
    orbit_crossing_neighbor character varying(100),
    label character varying(100),
    created_at timestamp without time zone DEFAULT now(),
    modified_at timestamp without time zone DEFAULT now(),
    distance_from_parent double precision,
    orbital_order integer,
    hill_sphere_radius_km double precision,
    roche_limit_km double precision
);


ALTER TABLE ud.orbital_elements OWNER TO starcreator_dev;


--
-- Name: TABLE orbital_elements; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON TABLE ud.orbital_elements IS 'Reusable Keplerian orbital parameters for any orbiting object or orbital boundary';



--
-- Name: COLUMN orbital_elements.semi_major_axis; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.orbital_elements.semi_major_axis IS 'Semi-major axis value in the unit specified by semi_major_axis_unit';



--
-- Name: COLUMN orbital_elements.semi_major_axis_unit; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.orbital_elements.semi_major_axis_unit IS 'Distance unit: AU for star-orbiting bodies, KM for planet-orbiting bodies';



--
-- Name: COLUMN orbital_elements.label; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.orbital_elements.label IS 'Human-readable label for debugging (e.g. Planet orbit, Belt inner edge)';



--
-- Name: COLUMN orbital_elements.distance_from_parent; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.orbital_elements.distance_from_parent IS 'Distance from parent body (planet→star in AU, moon→planet in km, star→barycenter in AU)';



--
-- Name: COLUMN orbital_elements.orbital_order; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.orbital_elements.orbital_order IS 'Position in orbital sequence (1 = innermost)';



--
-- Name: orbital_elements_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.orbital_elements_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.orbital_elements_id_seq OWNER TO starcreator_dev;


--
-- Name: orbital_elements_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.orbital_elements_id_seq OWNED BY ud.orbital_elements.id;



--
-- Name: physical_properties; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.physical_properties (
    id bigint NOT NULL,
    mass double precision,
    radius double precision,
    circumference double precision,
    earth_mass double precision,
    earth_radius double precision,
    density_g_cm3 double precision,
    surface_gravity double precision,
    escape_velocity double precision,
    albedo double precision,
    solar_mass double precision,
    solar_radius double precision,
    solar_luminosity double precision,
    surface_temp double precision,
    created_at timestamp without time zone DEFAULT now(),
    modified_at timestamp without time zone DEFAULT now(),
    dimensions_km character varying(255),
    surface_color_primary character varying(7),
    surface_color_secondary character varying(7)
);


ALTER TABLE ud.physical_properties OWNER TO starcreator_dev;


--
-- Name: TABLE physical_properties; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON TABLE ud.physical_properties IS 'Physical properties for any celestial body (planet, moon, or star). Body-type-specific fields are nullable.';



--
-- Name: physical_properties_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.physical_properties_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.physical_properties_id_seq OWNER TO starcreator_dev;


--
-- Name: physical_properties_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.physical_properties_id_seq OWNED BY ud.physical_properties.id;



--
-- Name: planet; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.planet (
    id bigint NOT NULL,
    name character varying(255),
    mass double precision,
    radius double precision,
    circumference double precision,
    orbital_position integer,
    star_id bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    modified_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    atmosphere_id bigint,
    additional_moonlets integer DEFAULT 0,
    orbital_elements_id bigint,
    terrain_id bigint,
    hydrology_id bigint,
    magnetic_field_id bigint,
    physical_properties_id bigint,
    rotation_properties_id bigint,
    composition_properties_id bigint,
    designation_id bigint,
    climate_seed bigint,
    surface_seed bigint
);


ALTER TABLE ud.planet OWNER TO starcreator_dev;


--
-- Name: TABLE planet; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON TABLE ud.planet IS 'Stores planet data - inherits from celestial_body via JPA JOINED strategy';



--
-- Name: COLUMN planet.star_id; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.planet.star_id IS 'Foreign key to parent star - planets orbit specific stars';



--
-- Name: COLUMN planet.additional_moonlets; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.planet.additional_moonlets IS 'Count of tiny irregular moonlets too small for individual generation. These are sub-Mimas captured objects. The moons list contains only significant moons with full physics and detail.';



--
-- Name: planet_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.planet_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.planet_id_seq OWNER TO starcreator_dev;


--
-- Name: planet_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.planet_id_seq OWNED BY ud.planet.id;



--
-- Name: terrain_distribution; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.terrain_distribution (
    id bigint CONSTRAINT planet_terrain_distribution_id_not_null NOT NULL,
    terrain_type_id integer,
    coverage_percent double precision,
    description text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    terrain_properties_id bigint,
    CONSTRAINT chk_terrain_coverage_range CHECK (((coverage_percent >= (0)::double precision) AND (coverage_percent <= (100)::double precision)))
);


ALTER TABLE ud.terrain_distribution OWNER TO starcreator_dev;


--
-- Name: TABLE terrain_distribution; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON TABLE ud.terrain_distribution IS 'Stores terrain type distribution for planets';



--
-- Name: COLUMN terrain_distribution.coverage_percent; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.terrain_distribution.coverage_percent IS 'Percentage of planet surface covered by this terrain type';



--
-- Name: planet_terrain_distribution_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.planet_terrain_distribution_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.planet_terrain_distribution_id_seq OWNER TO starcreator_dev;


--
-- Name: planet_terrain_distribution_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.planet_terrain_distribution_id_seq OWNED BY ud.terrain_distribution.id;



--
-- Name: planetary_magnetic_field; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.planetary_magnetic_field (
    id bigint NOT NULL,
    strength_compared_to_earth double precision,
    surface_field_microteslas_min double precision,
    surface_field_microteslas_max double precision,
    surface_field_microteslas_avg double precision,
    dynamo_type character varying(50),
    dynamo_efficiency double precision,
    core_convection_intensity character varying(50),
    field_geometry character varying(50),
    dipole_tilt_degrees double precision,
    magnetic_axis_offset_km double precision,
    variation_pattern character varying(100),
    pole_field_strength_multiplier double precision,
    equatorial_field_strength_multiplier double precision,
    temporal_stability character varying(50),
    flux_period_hours integer,
    flux_peak_microteslas double precision,
    flux_low_microteslas double precision,
    flux_amplitude_percent double precision,
    instability_frequency character varying(50),
    random_fluctuation_percent double precision,
    has_reversals boolean DEFAULT false,
    reversal_period_million_years double precision,
    time_since_last_reversal_million_years double precision,
    reversal_transition_duration_years integer,
    current_reversal_state character varying(50),
    magnetosphere_exists boolean DEFAULT false,
    magnetopause_distance_planet_radii double precision,
    magnetotail_length_planet_radii double precision,
    bow_shock_distance_planet_radii double precision,
    has_radiation_belts boolean DEFAULT false,
    inner_belt_intensity character varying(50),
    outer_belt_intensity character varying(50),
    has_auroras boolean DEFAULT false,
    auroral_zone_latitude_degrees double precision,
    auroral_frequency character varying(50),
    auroral_intensity character varying(50),
    shields_from_stellar_wind boolean DEFAULT false,
    shields_from_cosmic_rays boolean DEFAULT false,
    protection_level character varying(50),
    atmospheric_loss_rate_factor double precision,
    magnetic_moment double precision,
    surface_power_flux_watts_per_m2 double precision,
    has_paleomagnetic_record boolean DEFAULT false,
    oldest_magnetic_rocks_million_years double precision,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    modified_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    auroral_colors character varying(200),
    CONSTRAINT chk_atm_loss_factor_positive CHECK (((atmospheric_loss_rate_factor IS NULL) OR (atmospheric_loss_rate_factor >= (0)::double precision))),
    CONSTRAINT chk_auroral_latitude_range CHECK (((auroral_zone_latitude_degrees IS NULL) OR ((auroral_zone_latitude_degrees >= ('-90'::integer)::double precision) AND (auroral_zone_latitude_degrees <= (90)::double precision)))),
    CONSTRAINT chk_dipole_tilt_range CHECK (((dipole_tilt_degrees IS NULL) OR ((dipole_tilt_degrees >= (0)::double precision) AND (dipole_tilt_degrees <= (180)::double precision)))),
    CONSTRAINT chk_dynamo_efficiency_range CHECK (((dynamo_efficiency IS NULL) OR ((dynamo_efficiency >= (0)::double precision) AND (dynamo_efficiency <= (1)::double precision)))),
    CONSTRAINT chk_equatorial_multiplier_positive CHECK (((equatorial_field_strength_multiplier IS NULL) OR (equatorial_field_strength_multiplier > (0)::double precision))),
    CONSTRAINT chk_flux_amplitude_range CHECK (((flux_amplitude_percent IS NULL) OR ((flux_amplitude_percent >= (0)::double precision) AND (flux_amplitude_percent <= (100)::double precision)))),
    CONSTRAINT chk_flux_period_positive CHECK (((flux_period_hours IS NULL) OR (flux_period_hours > 0))),
    CONSTRAINT chk_magnetopause_positive CHECK (((magnetopause_distance_planet_radii IS NULL) OR (magnetopause_distance_planet_radii > (0)::double precision))),
    CONSTRAINT chk_microteslas_valid CHECK (((surface_field_microteslas_min >= (0)::double precision) AND (surface_field_microteslas_max >= surface_field_microteslas_min) AND (surface_field_microteslas_avg >= surface_field_microteslas_min) AND (surface_field_microteslas_avg <= surface_field_microteslas_max))),
    CONSTRAINT chk_pole_multiplier_positive CHECK (((pole_field_strength_multiplier IS NULL) OR (pole_field_strength_multiplier > (0)::double precision))),
    CONSTRAINT chk_strength_positive CHECK ((strength_compared_to_earth >= (0)::double precision))
);


ALTER TABLE ud.planetary_magnetic_field OWNER TO starcreator_dev;


--
-- Name: TABLE planetary_magnetic_field; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON TABLE ud.planetary_magnetic_field IS 'Comprehensive magnetic field properties for planets including dynamo characteristics, field geometry, temporal variations, magnetosphere properties, and stellar wind interactions';



--
-- Name: COLUMN planetary_magnetic_field.strength_compared_to_earth; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.planetary_magnetic_field.strength_compared_to_earth IS 'Magnetic field strength relative to Earth (Earth = 1.0, approximately 50 microTesla at surface)';



--
-- Name: COLUMN planetary_magnetic_field.dynamo_type; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.planetary_magnetic_field.dynamo_type IS 'Mechanism generating the magnetic field: CORE_DYNAMO (liquid metal core), METALLIC_HYDROGEN (gas giant), REMNANT (frozen-in ancient field), INDUCED (external field induces currents), NONE';



--
-- Name: COLUMN planetary_magnetic_field.field_geometry; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.planetary_magnetic_field.field_geometry IS 'Shape of magnetic field: DIPOLE (like Earth or bar magnet), QUADRUPOLE (4 poles), MULTIPOLE (complex multi-pole), CHAOTIC (disorganized), COMPRESSED (flattened by stellar wind)';



--
-- Name: COLUMN planetary_magnetic_field.temporal_stability; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.planetary_magnetic_field.temporal_stability IS 'How field changes over time: STABLE (constant), FLUXING (regular cycles), UNSTABLE (random variations), REVERSING (poles flip), DECAYING (weakening permanently)';



--
-- Name: COLUMN planetary_magnetic_field.magnetopause_distance_planet_radii; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.planetary_magnetic_field.magnetopause_distance_planet_radii IS 'Distance to magnetopause where stellar wind pressure equals magnetic pressure in planet radii. Earth is approximately 10 radii on day side and 200+ radii on night side (magnetotail)';



--
-- Name: COLUMN planetary_magnetic_field.protection_level; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.planetary_magnetic_field.protection_level IS 'How well magnetic field protects surface from radiation: NONE (no protection), MINIMAL (less than 25%), MODERATE (25-50%), STRONG (50-80%), EXCEPTIONAL (greater than 80%)';



--
-- Name: COLUMN planetary_magnetic_field.atmospheric_loss_rate_factor; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.planetary_magnetic_field.atmospheric_loss_rate_factor IS 'Multiplier for atmospheric loss rate. Strong fields reduce loss (0.1 equals 10x slower), no field increases loss (10.0 equals 10x faster). Normal equals 1.0';



--
-- Name: COLUMN planetary_magnetic_field.auroral_colors; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.planetary_magnetic_field.auroral_colors IS 'Colors that auroras display based on atmospheric composition. Different gases emit different wavelengths when ionized by stellar wind particles.';



--
-- Name: planetary_magnetic_field_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.planetary_magnetic_field_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.planetary_magnetic_field_id_seq OWNER TO starcreator_dev;


--
-- Name: planetary_magnetic_field_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.planetary_magnetic_field_id_seq OWNED BY ud.planetary_magnetic_field.id;



--
-- Name: rotation_properties; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.rotation_properties (
    id bigint NOT NULL,
    rotation_period_hours double precision,
    axial_tilt double precision,
    tidally_locked boolean,
    created_at timestamp without time zone DEFAULT now(),
    modified_at timestamp without time zone DEFAULT now()
);


ALTER TABLE ud.rotation_properties OWNER TO starcreator_dev;


--
-- Name: TABLE rotation_properties; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON TABLE ud.rotation_properties IS 'Rotation and spin-axis properties for any celestial body (planet, moon, or asteroid)';



--
-- Name: rotation_properties_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.rotation_properties_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.rotation_properties_id_seq OWNER TO starcreator_dev;


--
-- Name: rotation_properties_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.rotation_properties_id_seq OWNED BY ud.rotation_properties.id;



--
-- Name: sector; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.sector (
    id bigint NOT NULL,
    name character varying(255),
    description character varying(255),
    x integer,
    y integer,
    z integer,
    created_at timestamp without time zone DEFAULT now(),
    modified_at timestamp without time zone DEFAULT now(),
    survey_id bigint,
    sector_code character varying(255)
);


ALTER TABLE ud.sector OWNER TO starcreator_dev;


--
-- Name: sector_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.sector_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.sector_id_seq OWNER TO starcreator_dev;


--
-- Name: sector_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.sector_id_seq OWNED BY ud.sector.id;



--
-- Name: star; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.star (
    id bigint NOT NULL,
    star_type integer,
    metallicity double precision,
    rotation_days double precision,
    is_variable boolean DEFAULT false,
    variability_period_days double precision,
    star_role character varying(255),
    habitable_zone_inner_au double precision,
    habitable_zone_outer_au double precision,
    habitable_zone_innerau double precision,
    habitable_zone_outerau double precision,
    activity_cycle_years double precision,
    activity_cycle_phase double precision,
    activity_level character varying(255),
    in_grand_minimum boolean DEFAULT false,
    grand_minimum_duration_years double precision,
    grand_minimum_depth double precision,
    flare_frequency_per_day double precision,
    max_flare_energy_ergs double precision,
    flare_class character varying(255),
    superflare_capable boolean DEFAULT false,
    starspot_coverage_percent double precision,
    starspot_temp_contrast_k double precision,
    has_polar_spots boolean DEFAULT false,
    stellar_wind_mass_loss_rate double precision,
    stellar_wind_velocity_km_s double precision,
    stellar_wind_density_at_1au double precision,
    coronal_temp_mk double precision,
    xray_luminosity_class character varying(255),
    has_corona boolean DEFAULT true,
    main_sequence_fraction double precision,
    estimated_remaining_ms_my double precision,
    log_r_prime_hk double precision,
    rossby_number double precision,
    logrprime_hk double precision,
    physical_properties_id bigint,
    orbital_elements_id bigint,
    created_at timestamp without time zone DEFAULT now(),
    modified_at timestamp without time zone DEFAULT now(),
    system_id bigint,
    name character varying(255),
    designation_id bigint
);


ALTER TABLE ud.star OWNER TO starcreator_dev;


--
-- Name: COLUMN star.habitable_zone_inner_au; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.habitable_zone_inner_au IS 'Inner edge of habitable zone in AU (where liquid water can exist)';



--
-- Name: COLUMN star.habitable_zone_outer_au; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.habitable_zone_outer_au IS 'Outer edge of habitable zone in AU (where liquid water can exist)';



--
-- Name: COLUMN star.activity_cycle_years; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.activity_cycle_years IS 'Duration of the magnetic activity cycle in years (Sun = 11.07 years). Derived from rotation period via Noyes relationship.';



--
-- Name: COLUMN star.activity_cycle_phase; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.activity_cycle_phase IS 'Current phase in the activity cycle, 0.0 (minimum) to 1.0 (maximum). Determines current flare rates and spot coverage.';



--
-- Name: COLUMN star.activity_level; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.activity_level IS 'Overall chromospheric activity level: INACTIVE, LOW, MODERATE, ACTIVE, VERY_ACTIVE, HYPERACTIVE';



--
-- Name: COLUMN star.in_grand_minimum; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.in_grand_minimum IS 'Whether the star is currently in a Maunder-minimum-like grand minimum — dramatically reduced activity for decades/centuries.';



--
-- Name: COLUMN star.grand_minimum_duration_years; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.grand_minimum_duration_years IS 'If in grand minimum, estimated total duration in years (typical 50-200 years).';



--
-- Name: COLUMN star.grand_minimum_depth; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.grand_minimum_depth IS 'Depth of the grand minimum: 0.0 (shallow, some spots remain) to 1.0 (deep, virtually no spots). Maunder Minimum was ~0.85.';



--
-- Name: COLUMN star.flare_frequency_per_day; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.flare_frequency_per_day IS 'Average number of detectable flares per day at current activity level. Sun at solar max ~ 0.5/day. M dwarfs can be 5-50/day.';



--
-- Name: COLUMN star.max_flare_energy_ergs; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.max_flare_energy_ergs IS 'Estimated maximum flare energy in ergs (log10 scale stored). Sun typical max ~ 32 (10^32 ergs). Superflare stars can reach 36+.';



--
-- Name: COLUMN star.flare_class; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.flare_class IS 'Dominant flare class: NANOFLARE, MICROFLARE, C_CLASS, M_CLASS, X_CLASS, SUPERFLARE';



--
-- Name: COLUMN star.superflare_capable; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.superflare_capable IS 'Whether this star can produce superflares (10-1000x largest solar flares). More common in young, fast-rotating stars.';



--
-- Name: COLUMN star.starspot_coverage_percent; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.starspot_coverage_percent IS 'Current fractional coverage of stellar surface by starspots (%). Sun at solar max ~ 0.3%. Active M/K dwarfs can reach 20-40%.';



--
-- Name: COLUMN star.starspot_temp_contrast_k; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.starspot_temp_contrast_k IS 'Temperature difference between quiet photosphere and spot umbra in Kelvin. Sun ~ 1500K. Cooler stars have smaller contrasts.';



--
-- Name: COLUMN star.has_polar_spots; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.has_polar_spots IS 'Whether the star has large polar spot regions. Common on fast rotators and tidally locked binaries. Absent on slow rotators like the Sun.';



--
-- Name: COLUMN star.stellar_wind_mass_loss_rate; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.stellar_wind_mass_loss_rate IS 'Mass loss rate in solar masses per year. Sun = ~2e-14. Red giants can be 1e-8 to 1e-5.';



--
-- Name: COLUMN star.stellar_wind_velocity_km_s; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.stellar_wind_velocity_km_s IS 'Terminal velocity of stellar wind in km/s. Sun slow wind ~ 400, fast wind ~ 750. Hot stars can reach 2000+.';



--
-- Name: COLUMN star.stellar_wind_density_at_1au; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.stellar_wind_density_at_1au IS 'Proton density of stellar wind at 1 AU in particles/cm^3. Sun average ~ 6.';



--
-- Name: COLUMN star.coronal_temp_mk; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.coronal_temp_mk IS 'Peak coronal temperature in millions of Kelvin. Sun ~ 1-2 MK. Active stars can reach 10-30 MK.';



--
-- Name: COLUMN star.xray_luminosity_class; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.xray_luminosity_class IS 'X-ray luminosity class: DARK (white dwarfs, very old), DIM, MODERATE, BRIGHT, INTENSE (young active stars)';



--
-- Name: COLUMN star.has_corona; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.has_corona IS 'Whether the star has a traditional hot corona. White dwarfs, neutron stars, and brown dwarfs typically do not.';



--
-- Name: COLUMN star.estimated_remaining_ms_my; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.estimated_remaining_ms_my IS 'Estimated remaining main sequence lifetime in millions of years. NULL for post-MS stars. Gives writers a "ticking clock" narrative element.';



--
-- Name: COLUMN star.log_r_prime_hk; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.log_r_prime_hk IS 'Mount Wilson S-index converted to log(R''HK) chromospheric emission ratio. Ranges from -5.1 (very inactive) to -4.0 (very active). Standard measure of stellar activity.';



--
-- Name: COLUMN star.rossby_number; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.star.rossby_number IS 'Rossby number (Ro = P_rot / τ_conv). Stars with Ro < 0.1 are in saturated activity regime. Key predictor of activity level.';



--
-- Name: star_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.star_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.star_id_seq OWNER TO starcreator_dev;


--
-- Name: star_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.star_id_seq OWNED BY ud.star.id;



--
-- Name: star_system; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.star_system (
    id bigint NOT NULL,
    name character varying(255),
    sector_id bigint,
    size_au double precision,
    habitable_low double precision,
    habitable_high double precision,
    description character varying(5000),
    x integer,
    y integer,
    z integer,
    created_at timestamp without time zone DEFAULT now(),
    modified_at timestamp without time zone DEFAULT now(),
    binary_configuration character varying(255),
    primary_star_id bigint,
    binary_separation_au double precision,
    binary_orbital_period_days double precision,
    designation_id bigint
);


ALTER TABLE ud.star_system OWNER TO starcreator_dev;


--
-- Name: star_system_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.star_system_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.star_system_id_seq OWNER TO starcreator_dev;


--
-- Name: star_system_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.star_system_id_seq OWNED BY ud.star_system.id;



--
-- Name: survey; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.survey (
    id bigint NOT NULL,
    name character varying(255),
    code character varying(10),
    description character varying(255),
    user_id bigint,
    created_at timestamp without time zone DEFAULT now(),
    modified_at timestamp without time zone DEFAULT now()
);


ALTER TABLE ud.survey OWNER TO starcreator_dev;


--
-- Name: TABLE survey; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON TABLE ud.survey IS 'Survey campaigns that organize sectors and star systems under a common designation code';



--
-- Name: survey_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.survey_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.survey_id_seq OWNER TO starcreator_dev;


--
-- Name: survey_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.survey_id_seq OWNED BY ud.survey.id;



--
-- Name: terrain_properties; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.terrain_properties (
    id bigint NOT NULL,
    surface_features text,
    geological_activity character varying(50),
    activity_score double precision,
    has_volcanic_activity boolean,
    volcanism_type character varying(50),
    estimated_active_volcanoes integer,
    volcanic_intensity character varying(50),
    mountain_coverage_percent double precision,
    average_elevation_km double precision,
    max_elevation_km double precision,
    min_elevation_km double precision,
    terrain_roughness double precision,
    cratering_level character varying(50),
    estimated_visible_craters integer,
    erosion_level character varying(50),
    primary_erosion_agent character varying(50),
    has_plate_tectonics boolean,
    number_of_tectonic_plates integer,
    tectonic_activity_level character varying(50),
    has_cryovolcanism boolean,
    has_regolith boolean,
    regolith_depth_m double precision,
    label character varying(100),
    created_at timestamp without time zone DEFAULT now(),
    modified_at timestamp without time zone DEFAULT now()
);


ALTER TABLE ud.terrain_properties OWNER TO starcreator_dev;


--
-- Name: TABLE terrain_properties; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON TABLE ud.terrain_properties IS 'Terrain, geology, and surface morphology data for any celestial body with a surface';



--
-- Name: COLUMN terrain_properties.label; Type: COMMENT; Schema: ud; Owner: starcreator_dev
--

COMMENT ON COLUMN ud.terrain_properties.label IS 'Human-readable label for debugging (e.g. Planet terrain, Moon terrain)';



--
-- Name: terrain_properties_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.terrain_properties_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.terrain_properties_id_seq OWNER TO starcreator_dev;


--
-- Name: terrain_properties_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.terrain_properties_id_seq OWNED BY ud.terrain_properties.id;



--
-- Name: hydrology_properties; Type: TABLE; Schema: ud; Owner: starcreator_dev
--

CREATE TABLE ud.hydrology_properties (
    id bigint NOT NULL,
    liquid_inventory character varying(30),
    liquid_coverage_percent double precision,
    liquid_liquid_coverage_percent double precision,
    water_ice_coverage_percent double precision,
    has_subsurface_liquid boolean,
    subsurface_liquid_depth_km double precision,
    has_subsurface_ocean boolean,
    ocean_depth_km double precision,
    ice_shell_thickness_km double precision,
    label character varying(100),
    created_at timestamp without time zone DEFAULT now(),
    modified_at timestamp without time zone DEFAULT now(),
    volatile_type character varying(30),
    liquid_composition character varying(100),
    freezing_point_k double precision,
    boiling_point_k double precision,
    frozen_liquid_coverage_percent double precision,
    liquid_color_primary character varying(7),
    liquid_color_secondary character varying(7)
);


ALTER TABLE ud.hydrology_properties OWNER TO starcreator_dev;


--
-- Name: hydrology_properties_id_seq; Type: SEQUENCE; Schema: ud; Owner: starcreator_dev
--

CREATE SEQUENCE ud.hydrology_properties_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ud.hydrology_properties_id_seq OWNER TO starcreator_dev;


--
-- Name: hydrology_properties_id_seq; Type: SEQUENCE OWNED BY; Schema: ud; Owner: starcreator_dev
--

ALTER SEQUENCE ud.hydrology_properties_id_seq OWNED BY ud.hydrology_properties.id;



--
-- Name: asteroid_type id; Type: DEFAULT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.asteroid_type ALTER COLUMN id SET DEFAULT nextval('ref.asteroid_type_id_seq'::regclass);



--
-- Name: atmosphere_template id; Type: DEFAULT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.atmosphere_template ALTER COLUMN id SET DEFAULT nextval('ref.atmosphere_template_id_seq'::regclass);



--
-- Name: atmosphere_template_component id; Type: DEFAULT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.atmosphere_template_component ALTER COLUMN id SET DEFAULT nextval('ref.atmosphere_template_component_id_seq'::regclass);



--
-- Name: belt_type id; Type: DEFAULT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.belt_type ALTER COLUMN id SET DEFAULT nextval('ref.belt_type_id_seq'::regclass);



--
-- Name: composition_template id; Type: DEFAULT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.composition_template ALTER COLUMN id SET DEFAULT nextval('ref.composition_template_id_seq'::regclass);



--
-- Name: composition_template_component id; Type: DEFAULT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.composition_template_component ALTER COLUMN id SET DEFAULT nextval('ref.composition_template_component_id_seq'::regclass);



--
-- Name: geological_template id; Type: DEFAULT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.geological_template ALTER COLUMN id SET DEFAULT nextval('ref.geological_template_id_seq'::regclass);



--
-- Name: geological_template_feature id; Type: DEFAULT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.geological_template_feature ALTER COLUMN id SET DEFAULT nextval('ref.geological_template_feature_id_seq'::regclass);



--
-- Name: moon_type_ref id; Type: DEFAULT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.moon_type_ref ALTER COLUMN id SET DEFAULT nextval('ref.moon_type_ref_id_seq'::regclass);



--
-- Name: name_origin_ref id; Type: DEFAULT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.name_origin_ref ALTER COLUMN id SET DEFAULT nextval('ref.name_origin_ref_id_seq'::regclass);



--
-- Name: name_ref id; Type: DEFAULT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.name_ref ALTER COLUMN id SET DEFAULT nextval('ref.name_ref_id_seq'::regclass);



--
-- Name: name_region_ref id; Type: DEFAULT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.name_region_ref ALTER COLUMN id SET DEFAULT nextval('ref.name_region_ref_id_seq'::regclass);



--
-- Name: planet_atmosphere_compatibility id; Type: DEFAULT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.planet_atmosphere_compatibility ALTER COLUMN id SET DEFAULT nextval('ref.planet_atmosphere_compatibility_id_seq'::regclass);



--
-- Name: planet_type_ref id; Type: DEFAULT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.planet_type_ref ALTER COLUMN id SET DEFAULT nextval('ref.planet_type_ref_id_seq'::regclass);



--
-- Name: ring_template id; Type: DEFAULT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.ring_template ALTER COLUMN id SET DEFAULT nextval('ref.ring_template_id_seq'::regclass);



--
-- Name: star_type id; Type: DEFAULT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.star_type ALTER COLUMN id SET DEFAULT nextval('ref.star_type_id_seq'::regclass);



--
-- Name: terrain_category_ref id; Type: DEFAULT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.terrain_category_ref ALTER COLUMN id SET DEFAULT nextval('ref.terrain_category_ref_id_seq'::regclass);



--
-- Name: terrain_type_ref id; Type: DEFAULT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.terrain_type_ref ALTER COLUMN id SET DEFAULT nextval('ref.terrain_type_ref_id_seq'::regclass);



--
-- Name: asteroid id; Type: DEFAULT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.asteroid ALTER COLUMN id SET DEFAULT nextval('ud.asteroid_id_seq'::regclass);



--
-- Name: atmosphere id; Type: DEFAULT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.atmosphere ALTER COLUMN id SET DEFAULT nextval('ud.atmosphere_id_seq'::regclass);



--
-- Name: atmosphere_component id; Type: DEFAULT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.atmosphere_component ALTER COLUMN id SET DEFAULT nextval('ud.atmosphere_component_id_seq'::regclass);



--
-- Name: composition_properties id; Type: DEFAULT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.composition_properties ALTER COLUMN id SET DEFAULT nextval('ud.composition_properties_id_seq'::regclass);



--
-- Name: designation id; Type: DEFAULT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.designation ALTER COLUMN id SET DEFAULT nextval('ud.designation_id_seq'::regclass);



--
-- Name: moon id; Type: DEFAULT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.moon ALTER COLUMN id SET DEFAULT nextval('ud.moon_id_seq'::regclass);



--
-- Name: orbital_band id; Type: DEFAULT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.orbital_band ALTER COLUMN id SET DEFAULT nextval('ud.orbital_band_id_seq'::regclass);



--
-- Name: orbital_elements id; Type: DEFAULT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.orbital_elements ALTER COLUMN id SET DEFAULT nextval('ud.orbital_elements_id_seq'::regclass);



--
-- Name: physical_properties id; Type: DEFAULT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.physical_properties ALTER COLUMN id SET DEFAULT nextval('ud.physical_properties_id_seq'::regclass);



--
-- Name: planet id; Type: DEFAULT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.planet ALTER COLUMN id SET DEFAULT nextval('ud.planet_id_seq'::regclass);



--
-- Name: planetary_magnetic_field id; Type: DEFAULT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.planetary_magnetic_field ALTER COLUMN id SET DEFAULT nextval('ud.planetary_magnetic_field_id_seq'::regclass);



--
-- Name: rotation_properties id; Type: DEFAULT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.rotation_properties ALTER COLUMN id SET DEFAULT nextval('ud.rotation_properties_id_seq'::regclass);



--
-- Name: sector id; Type: DEFAULT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.sector ALTER COLUMN id SET DEFAULT nextval('ud.sector_id_seq'::regclass);



--
-- Name: star id; Type: DEFAULT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.star ALTER COLUMN id SET DEFAULT nextval('ud.star_id_seq'::regclass);



--
-- Name: star_system id; Type: DEFAULT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.star_system ALTER COLUMN id SET DEFAULT nextval('ud.star_system_id_seq'::regclass);



--
-- Name: survey id; Type: DEFAULT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.survey ALTER COLUMN id SET DEFAULT nextval('ud.survey_id_seq'::regclass);



--
-- Name: terrain_distribution id; Type: DEFAULT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.terrain_distribution ALTER COLUMN id SET DEFAULT nextval('ud.planet_terrain_distribution_id_seq'::regclass);



--
-- Name: terrain_properties id; Type: DEFAULT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.terrain_properties ALTER COLUMN id SET DEFAULT nextval('ud.terrain_properties_id_seq'::regclass);



--
-- Name: hydrology_properties id; Type: DEFAULT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.hydrology_properties ALTER COLUMN id SET DEFAULT nextval('ud.hydrology_properties_id_seq'::regclass);



--
-- Data for Name: asteroid_type; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.asteroid_type (id, name, code, spectral_class, description, primary_composition, albedo_min, albedo_max, density_min, density_max, belt_affinity, relative_abundance, can_be_differentiated, typical_surface_features, created_at) FROM stdin;
1	Carbonaceous	C	C	Dark, primitive asteroids rich in carbon compounds	Carbon 15%, Hydrated Silicates 60%, Organics 25%	0.03	0.1	1.1	2.3	INNER_ROCKY	0.35	f	Very dark surface, primitive composition, clay minerals	2026-02-05 23:08:23.862413
2	Silicaceous	S	S	Rocky asteroids with silicate minerals and metal	Olivine 35%, Pyroxene 40%, Iron-Nickel 25%	0.1	0.28	2.3	3.5	INNER_ROCKY	0.3	t	Rocky terrain, metallic flecks, moderate cratering	2026-02-05 23:08:23.862413
3	Metallic	M	M	Metal-rich asteroids, possibly exposed planetary cores	Iron 85%, Nickel 10%, Cobalt 3%, Silicates 2%	0.1	0.2	4.5	7.5	INNER_ROCKY	0.08	t	Metallic luster, radar-bright, possible core fragment	2026-02-05 23:08:23.862413
4	Basaltic	V	V	Igneous asteroids with basaltic crust	Basalt 50%, Pyroxene 30%, Plagioclase 20%	0.2	0.45	2.8	3.5	INNER_ROCKY	0.05	t	Volcanic surface, lava flow features, large impact basins	2026-02-05 23:08:23.862413
5	Enstatite	E	E	High-albedo asteroids with enstatite composition	Enstatite 70%, Iron-Nickel 20%, Sulfides 10%	0.25	0.6	2.8	3.5	INNER_ROCKY	0.02	t	Bright surface, formed in reducing conditions	2026-02-05 23:08:23.862413
6	Primitive Dark	D	D	Very dark, organic-rich outer belt asteroids	Organics 40%, Carbon 30%, Anhydrous Silicates 25%, Ice 5%	0.02	0.06	0.8	1.8	OUTER_ROCKY	0.1	f	Extremely dark, reddish tint, possibly cometary origin	2026-02-05 23:08:23.862413
7	Primitive	P	P	Low-albedo primitive outer belt asteroids	Organics 30%, Carbon 25%, Silicates 40%, Ice 5%	0.02	0.07	1	2	OUTER_ROCKY	0.07	f	Dark surface, organic-rich, transitional composition	2026-02-05 23:08:23.862413
8	Blue Carbonaceous	B	B	Primitive carbonaceous with blue spectral slope	Hydrated Silicates 50%, Carbon 20%, Organics 20%, Volatiles 10%	0.04	0.1	1.2	2.2	INNER_ROCKY	0.03	f	Dark surface, hydration features, volatile content	2026-02-05 23:08:23.862413
9	Cold Classical KBO	KC	IR	Primordial KBO with circular, low-inclination orbit	Water Ice 45%, Methane Ice 15%, Ammonia Ice 10%, Rock 25%, Tholins 5%	0.04	0.15	0.8	1.8	KUIPER	0.3	f	Icy surface, ultra-red coloring from tholins, pristine	2026-02-05 23:08:23.862413
10	Hot Classical KBO	KH	IR	Dynamically excited KBO with higher inclination	Water Ice 50%, Ammonia Ice 15%, Rock 30%, Organics 5%	0.04	0.12	1	2	KUIPER	0.2	f	Icy surface, less red than cold classicals	2026-02-05 23:08:23.862413
11	Resonant KBO	KR	IR	KBO locked in mean-motion resonance with giant planet	Nitrogen Ice 20%, Water Ice 35%, Methane Ice 15%, Rock 25%, Organics 5%	0.04	0.8	1.4	2.2	KUIPER	0.15	t	Variable surface, possible seasonal atmosphere	2026-02-05 23:08:23.862413
12	Scattered Disk Object	SD	IR	Highly eccentric outer system object	Water Ice 55%, Methane Ice 10%, Ammonia Ice 10%, Rock 20%, Organics 5%	0.03	0.1	0.9	1.8	SCATTERED_DISK	0.25	f	Icy surface, possible cometary activity near perihelion	2026-02-05 23:08:23.862413
13	Centaur	CE	IR	Transitional object crossing giant planet orbits	Water Ice 40%, Carbon 20%, Silicates 30%, Organics 10%	0.02	0.08	0.8	1.5	KUIPER	0.1	f	Dark icy surface, cometary outgassing, chaotic orbit	2026-02-05 23:08:23.862413
\.



--
-- Data for Name: atmosphere_template; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.atmosphere_template (id, name, description, classification, is_breathable, typical_pressure_bar, min_temperature_k, max_temperature_k, min_planet_mass_earth, max_planet_mass_earth, rarity_weight, created_at) FROM stdin;
5	Jovian	Hydrogen-helium atmosphere, gas giant	JOVIAN	f	1000	50	500	50	5000	100	2026-01-19 09:59:01.990752
6	Ice Giant	Hydrogen-helium with methane and ammonia	ICE_GIANT	f	500	30	200	10	50	80	2026-01-19 09:59:01.990752
9	Reducing	Hydrogen-rich, oxygen-poor primitive atmosphere	REDUCING	f	1	200	400	0.5	2	25	2026-01-19 09:59:01.990752
10	Corrosive	Chlorine or fluorine-based atmosphere	CORROSIVE	f	1	250	600	0.5	3	5	2026-01-19 09:59:01.990752
21	Volcanic Moon Atmosphere	CO2/SO2 atmosphere from silicate volcanism	VOLCANIC	f	0.001	200	1000	0.01	0.3	60	2026-01-28 19:20:54.752923
22	Extreme Volcanic	Intense volcanic atmosphere on tidally superheated moon	VOLCANIC	f	0.01	500	1500	0.01	0.5	80	2026-01-28 19:39:21.775067
35	Moon Earth-like	Thin N2/O2 atmosphere on large geologically active moon in HZ	EARTH_LIKE	t	0.3	180	320	0.008	0.5	15	2026-02-13 13:13:38.952315
36	Moon Reducing	Primordial H2-rich atmosphere on large icy moon with hydrothermal activity	REDUCING	f	0.05	80	350	0.005	0.5	25	2026-02-13 13:13:38.952315
37	Hot Neptune Reducing	Residual hydrogen envelope on irradiated Neptune	REDUCING	f	0.5	300	1200	2	30	40	2026-02-14 13:34:10.230204
38	Hot Neptune Exotic	Metallic vapor atmosphere on heavily irradiated Neptune	EXOTIC	f	0.1	700	1500	2	30	20	2026-02-14 13:34:10.230204
2	Venus-like	Dense CO2 atmosphere with extreme greenhouse effect	VENUS_LIKE	f	92	320	800	0.5	10	50	2026-01-19 09:59:01.990752
7	Volcanic	Sulfur dioxide and volcanic gases	VOLCANIC	f	2	300	1500	0.05	30	20	2026-01-19 09:59:01.990752
11	Exotic Metallic	Metallic vapors from extreme heat	EXOTIC	f	0.5	1500	3000	0.05	10	5	2026-01-19 09:59:01.990752
39	Hot Jovian	Hydrogen-helium atmosphere on hot gas giant with alkali metal vapors	JOVIAN	f	500	400	3000	20	10000	120	2026-02-15 12:27:43.821995
40	Warm Ice Giant	Transitional H2/He/CH4 atmosphere on moderately irradiated Neptune-class world	ICE_GIANT	f	200	150	450	2	30	70	2026-02-15 12:27:43.821995
8	Ammonia	Ammonia-dominated atmosphere, extremely toxic	AMMONIA	f	1.5	100	300	0.5	25	15	2026-01-19 09:59:01.990752
13	Thin Nitrogen	Tenuous nitrogen atmosphere like Triton or Pluto	TITAN_LIKE	f	1.4e-05	30	200	0.0001	0.05	60	2026-01-27 18:27:47.170564
4	Titan-like	Dense nitrogen atmosphere with methane	TITAN_LIKE	f	1.5	35	150	0.5	12	40	2026-01-19 09:59:01.990752
16	Cryovolcanic Trace	Trace atmosphere from cryovolcanic outgassing like Enceladus	TITAN_LIKE	f	1e-09	50	250	0.0001	0.1	50	2026-01-27 18:27:47.170564
15	Volcanic Outgassing	Transient SO2 atmosphere from active volcanism like Io	VOLCANIC	f	1e-09	80	800	0.001	0.3	70	2026-01-27 18:27:47.170564
12	None	No atmosphere or extremely thin	NONE	f	0	0	10000	0	0.3	5	2026-01-19 09:59:01.990752
20	Warm Nitrogen	Dense nitrogen atmosphere on tidally heated icy moon	TITAN_LIKE	f	2	150	350	0.01	0.5	70	2026-01-28 19:20:54.752923
14	Thick Nitrogen	Dense nitrogen-methane atmosphere like Titan	TITAN_LIKE	f	1.5	70	350	0.01	0.3	80	2026-01-27 18:27:47.170564
29	Hot Cryovolcanic	Volatile-rich atmosphere on tidally superheated icy moon	TITAN_LIKE	f	0.1	250	500	0.005	0.5	70	2026-01-28 20:04:47.385432
30	Small Icy Outgassing	Trace atmosphere from small active icy moon	TITAN_LIKE	f	1e-08	50	200	1e-05	0.001	50	2026-01-28 20:04:47.385432
31	Ammonia-Rich	Ammonia-dominated atmosphere on cold icy moon	AMMONIA	f	0.001	50	150	0.001	0.2	60	2026-01-28 20:04:47.385432
1	Earth-like	Nitrogen-oxygen atmosphere, breathable by humans	EARTH_LIKE	t	1	180	320	0.5	10	30	2026-01-19 09:59:01.990752
3	Mars-like	Thin CO2 atmosphere, minimal surface pressure	MARS_LIKE	f	0.006	150	400	0.1	5	70	2026-01-19 09:59:01.990752
32	Dense CO2	Thick carbon dioxide atmosphere on cold massive world	VENUS_LIKE	f	5	150	350	1	15	50	2026-02-10 21:16:58.528791
\.



--
-- Data for Name: atmosphere_template_component; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.atmosphere_template_component (id, template_id, gas_formula, min_percentage, max_percentage, is_trace) FROM stdin;
1	1	N2	75	80	f
2	1	O2	18	24	f
3	1	Ar	0.8	1.2	f
4	1	CO2	0.03	0.1	t
5	1	H2O	0.5	5	f
6	2	CO2	95	97	f
7	2	N2	3	5	f
8	2	SO2	0.01	0.2	t
9	3	CO2	94	96	f
10	3	N2	2	3	f
11	3	Ar	1.5	2	f
12	3	O2	0.1	0.2	t
13	3	CO	0.05	0.1	t
14	4	N2	92	96	f
15	4	CH4	2	6	f
16	4	H2	0.1	1	t
17	5	H2	85	90	f
18	5	He	10	15	f
19	5	CH4	0.1	0.5	t
20	5	NH3	0.05	0.3	t
21	5	H2O	0.05	0.2	t
22	6	H2	80	85	f
23	6	He	10	15	f
24	6	CH4	2	4	f
25	6	NH3	0.5	2	f
26	6	H2O	0.5	1.5	t
27	7	SO2	40	70	f
28	7	CO2	20	50	f
29	7	H2S	1	10	f
30	8	NH3	60	85	f
31	8	N2	10	30	f
32	8	CH4	2	10	f
33	9	H2	50	70	f
34	9	CH4	15	30	f
35	9	NH3	5	15	f
36	9	H2O	2	10	f
37	10	Cl2	40	70	f
38	10	F2	10	30	f
39	10	Ar	5	20	f
40	11	Fe	40	60	f
41	11	Na	20	40	f
42	11	K	10	20	f
43	13	N2	90	99	f
44	13	CH4	0.5	5	f
45	13	CO	0.01	1	t
46	14	N2	94	98	f
47	14	CH4	1.5	5	f
48	14	H2	0.1	0.5	t
49	14	Ar	0.01	0.1	t
50	15	SO2	85	95	f
51	15	SO	2	10	f
52	15	O2	0.1	3	t
53	15	Na	0.01	1	t
54	16	H2O	85	95	f
55	16	N2	2	8	f
56	16	CO2	1	5	t
57	16	CH4	0.1	2	t
72	20	N2	85	95	f
73	20	CH4	2	8	f
74	20	CO2	0.5	3	f
75	20	H2O	0.1	2	t
76	20	Ar	0.01	0.5	t
77	21	CO2	60	80	f
78	21	SO2	10	25	f
79	21	H2S	1	5	t
80	21	SO	0.5	3	t
81	22	SO2	50	70	f
82	22	SO	10	20	f
83	22	O2	5	15	f
84	22	Na	1	5	t
85	22	K	0.5	3	t
108	29	H2O	40	70	f
109	29	CO2	15	35	f
110	29	N2	5	20	f
111	29	SO2	1	10	t
112	30	H2O	50	80	f
113	30	CO2	10	30	f
114	30	N2	5	20	t
115	31	NH3	40	70	f
116	31	N2	15	35	f
117	31	CH4	5	20	f
118	31	H2O	1	10	t
119	32	CO2	85	96	f
120	32	N2	2	10	f
121	32	SO2	0.01	0.5	t
122	32	H2O	0.01	1	t
133	35	N2	70	85	f
134	35	O2	12	22	f
135	35	Ar	0.5	2	f
136	35	CO2	0.05	0.5	t
137	35	H2O	0.1	3	t
138	36	H2	40	65	f
139	36	N2	15	30	f
140	36	CH4	5	20	f
141	36	CO2	1	5	t
142	36	H2O	0.1	3	t
143	37	H2	60	85	f
144	37	He	10	25	f
145	37	H2O	1	10	f
146	37	CO2	0.1	5	t
147	38	Na	20	40	f
148	38	K	5	15	f
149	38	H2	20	40	f
150	38	He	10	20	f
151	39	H2	75	90	f
152	39	He	8	18	f
153	39	Na	0.01	0.5	t
154	39	K	0.005	0.2	t
155	39	H2O	0.01	1	t
156	40	H2	75	85	f
157	40	He	12	18	f
158	40	CH4	0.5	3	f
159	40	H2O	0.5	3	f
160	40	NH3	0.01	0.5	t
\.



--
-- Data for Name: belt_type; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.belt_type (id, name, code, description, typical_composition_type, min_eccentricity, max_eccentricity, min_inclination_degrees, max_inclination_degrees, typical_mass_earth_masses_min, typical_mass_earth_masses_max, notable_object_chance, max_notable_objects, requires_giant_planet, min_system_age_my, location_description, formation_description, created_at) FROM stdin;
1	Inner Asteroid Belt	INNER_ROCKY	Rocky asteroid belt between inner planets and gas giants, analogous to the Main Belt	ROCKY	0.05	0.3	0	30	1e-07	0.0005	0.8	6	t	100	Located between the outermost rocky planet and innermost giant planet	Formed from planetesimals that failed to accrete due to gravitational perturbation from the nearby giant planet	2026-02-05 23:08:23.862413
2	Outer Asteroid Belt	OUTER_ROCKY	Secondary asteroid belt between giant planets	MIXED	0.05	0.25	0	25	1e-08	0.0001	0.4	3	t	100	Located in the gap between two giant planets	Remnant material trapped between giant planet orbital resonances	2026-02-05 23:08:23.862413
3	Kuiper Belt	KUIPER	Trans-giant icy belt in the outer system	ICY	0.01	0.2	0	35	0.001	0.1	0.7	5	t	500	Beyond the outermost giant planet, extending to the edge of the system	Remnant population of icy planetesimals from the outer protoplanetary disk	2026-02-05 23:08:23.862413
4	Scattered Disk	SCATTERED_DISK	Highly eccentric population scattered by giant planet migration	ICY	0.25	0.85	10	60	0.0001	0.01	0.3	2	t	1000	Overlapping and beyond the Kuiper Belt with highly eccentric orbits	Objects scattered into eccentric orbits during giant planet migration in early system history	2026-02-05 23:08:23.862413
\.



--
-- Data for Name: cloud_composition_template; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.cloud_composition_template (id, atmosphere_classification, substance, min_temperature_k, max_temperature_k, typical_altitude_scale_heights, opacity, color, description, weight, created_at) FROM stdin;
1	EARTH_LIKE	H2O	200	373	1	MODERATE	White	Water vapor clouds — cumulus, stratus, cirrus	200	2026-02-12 22:38:07.086864
2	EARTH_LIKE	H2O_ICE	180	240	2	THIN	White	High-altitude ice crystal cirrus clouds	150	2026-02-12 22:38:07.086864
3	VENUS_LIKE	H2SO4	320	800	1.5	OPAQUE	Yellow-White	Sulfuric acid cloud deck	200	2026-02-12 22:38:07.086864
4	VENUS_LIKE	H2SO4	400	700	1	THICK	Yellow	Lower sulfuric acid haze layer	150	2026-02-12 22:38:07.086864
5	MARS_LIKE	CO2_ICE	130	200	1.5	THIN	White	CO2 ice crystal clouds at altitude	150	2026-02-12 22:38:07.086864
6	MARS_LIKE	H2O_ICE	170	230	1	THIN	White	Thin water ice cirrus clouds	120	2026-02-12 22:38:07.086864
7	MARS_LIKE	DUST	150	400	0.5	MODERATE	Salmon-Pink	Suspended dust haze	180	2026-02-12 22:38:07.086864
8	TITAN_LIKE	CH4	60	95	0.5	MODERATE	Orange-Brown	Methane rain clouds	180	2026-02-12 22:38:07.086864
9	TITAN_LIKE	HYDROCARBON_HAZE	70	200	2	THICK	Orange	Photochemical hydrocarbon haze layers	200	2026-02-12 22:38:07.086864
10	TITAN_LIKE	C2H6	70	110	0.8	THIN	Brown	Ethane condensation clouds	100	2026-02-12 22:38:07.086864
11	JOVIAN	NH3	110	160	1	THICK	White	Ammonia ice crystal clouds — visible top layer	200	2026-02-12 22:38:07.086864
12	JOVIAN	NH4SH	180	270	0.5	THICK	Brown-Red	Ammonium hydrosulfide clouds — gives brown/red banding	180	2026-02-12 22:38:07.086864
13	JOVIAN	H2O	250	350	0.3	THICK	White-Grey	Deep water clouds below visible deck	150	2026-02-12 22:38:07.086864
14	ICE_GIANT	CH4	50	90	1.5	MODERATE	Cyan-Blue	Methane ice clouds — absorb red light	200	2026-02-12 22:38:07.086864
15	ICE_GIANT	H2S	100	200	0.8	THIN	Pale Yellow	Hydrogen sulfide haze layer	120	2026-02-12 22:38:07.086864
16	ICE_GIANT	NH3	100	160	0.5	MODERATE	White	Ammonia clouds at depth	150	2026-02-12 22:38:07.086864
17	VOLCANIC	SO2	200	600	1	MODERATE	Yellow-Grey	Sulfur dioxide volcanic clouds	180	2026-02-12 22:38:07.086864
18	VOLCANIC	SULFUR	350	800	0.5	THIN	Yellow	Elemental sulfur aerosol haze	120	2026-02-12 22:38:07.086864
19	AMMONIA	NH3	100	250	1	THICK	White	Ammonia ice and liquid clouds	200	2026-02-12 22:38:07.086864
20	AMMONIA	NH3_LIQUID	200	300	0.5	MODERATE	Pale Blue	Liquid ammonia droplet clouds	120	2026-02-12 22:38:07.086864
21	REDUCING	HYDROCARBON_HAZE	100	400	2	MODERATE	Brown-Orange	Photochemical haze from H2/CH4	150	2026-02-12 22:38:07.086864
22	CORROSIVE	HCL	200	500	1	MODERATE	Pale Green	Hydrochloric acid aerosol clouds	150	2026-02-12 22:38:07.086864
23	CORROSIVE	HF	250	500	1	THIN	Colorless-White	Hydrofluoric acid mist	100	2026-02-12 22:38:07.086864
24	EXOTIC	SILICATE	1500	3000	0.5	THICK	Red-Orange	Silicate rock vapor clouds on ultrahot worlds	150	2026-02-12 22:38:07.086864
25	EXOTIC	IRON	1800	3000	0.5	MODERATE	Dark Grey	Iron vapor condensation clouds	120	2026-02-12 22:38:07.086864
26	EXOTIC	CORUNDUM	1600	2800	0.8	MODERATE	Ruby Red	Aluminum oxide crystal clouds	80	2026-02-12 22:38:07.086864
\.



--
-- Data for Name: composition_template; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.composition_template (id, name, description, classification, planet_types, rarity_weight, created_at, min_surface_temp_k, max_surface_temp_k) FROM stdin;
2	Terrestrial - Earth-like	\N	SILICATE_RICH	Terrestrial Planet	200	2026-01-19 10:58:13.586308	250	350
5	Ocean World - Water Rich	\N	OCEAN_WORLD	Ocean Planet	100	2026-01-19 10:58:13.586308	273	373
9	Hot Jupiter - Inflated Gas	\N	GAS_ENVELOPE	Hot Jupiter	100	2026-01-19 10:58:13.586308	1000	2500
13	Puffy - Low Density Gas	\N	GAS_ENVELOPE	Puffy Planet	70	2026-01-19 10:58:13.586308	800	1800
14	Carbon - Diamond Core	\N	CARBON_RICH	Carbon Planet	60	2026-01-19 10:58:13.586308	200	600
19	Terrestrial - Metal Rich	\N	SILICATE_RICH	Terrestrial Planet,Super-Earth	100	2026-01-19 10:58:13.586308	250	400
20	Ocean - Deep Ice Shell	\N	OCEAN_WORLD	Ocean Planet	80	2026-01-19 10:58:13.586308	100	273
4	Desert - Oxidized Silicate	\N	SILICATE_RICH	Desert Planet	120	2026-01-19 10:58:13.586308	250	500
1	Hot Rocky - Stripped Core	\N	IRON_RICH	Hot Rocky Planet	150	2026-01-19 10:58:13.586308	600	1200
6	Lava - Molten Surface	\N	MOLTEN_SURFACE	Lava Planet	80	2026-01-19 10:58:13.586308	1200	3000
15	Iron - Stripped Core	\N	IRON_RICH	Iron Planet	50	2026-01-19 10:58:13.586308	500	1500
10	Gas Giant - Jupiter-like	\N	GAS_ENVELOPE	Gas Giant	300	2026-01-19 10:58:13.586308	80	400
11	Super-Jupiter - Massive Gas	\N	GAS_ENVELOPE	Super-Jupiter	80	2026-01-19 10:58:13.586308	50	600
3	Super-Earth - Dense Silicate	\N	SILICATE_RICH	Super-Earth	180	2026-01-19 10:58:13.586308	150	500
37	Moon - Large Regular Rocky	Large regular moon with rocky composition	SILICATE_RICH	REGULAR_LARGE	140	2026-01-30 13:27:21.134634	100	400
39	Moon - Medium Rocky	Medium-sized rocky moon	SILICATE_RICH	REGULAR_MEDIUM	150	2026-01-30 13:27:21.134634	80	400
41	Moon - Small Rocky	Small regular rocky moon	SILICATE_RICH	REGULAR_SMALL	130	2026-01-30 13:27:21.134634	80	400
43	Moon - Collision Debris	Moon formed from giant impact	SILICATE_RICH	COLLISION_DEBRIS	200	2026-01-30 13:27:21.134634	50	400
47	Moon - Shepherd Tiny	Tiny shepherd moon maintaining ring structure	SILICATE_RICH	SHEPHERD	200	2026-01-30 13:27:21.134634	50	300
48	Moon - Trojan Rocky	Rocky moon at Lagrange point	SILICATE_RICH	TROJAN	150	2026-01-30 13:27:21.134634	50	300
50	Moon - Captured Asteroid	Irregular captured asteroid	SILICATE_RICH	IRREGULAR_CAPTURED	180	2026-01-30 13:27:21.134634	50	300
38	Moon - Large Regular Icy	Large regular moon with thick ice layers	ICE_RICH	REGULAR_LARGE	180	2026-01-30 13:27:21.134634	30	250
40	Moon - Medium Icy	Medium-sized icy moon	ICE_RICH	REGULAR_MEDIUM	170	2026-01-30 13:27:21.134634	30	250
42	Moon - Small Icy	Small icy moon	ICE_RICH	REGULAR_SMALL	200	2026-01-30 13:27:21.134634	30	220
45	Moon - Ice Shell Ocean	Subsurface ocean beneath ice shell (Europa/Enceladus)	OCEAN_WORLD	Cryovolcanic Moon	180	2026-01-30 13:27:21.134634	30	300
46	Moon - Nitrogen Ice Cryo	Cold cryovolcanic moon (Triton-like)	ICE_RICH	Cryovolcanic Moon	120	2026-01-30 13:27:21.134634	30	150
49	Moon - Trojan Icy	Icy moon at Lagrange point	ICE_RICH	TROJAN	100	2026-01-30 13:27:21.134634	30	200
51	Moon - Captured Comet	Icy captured object	ICE_RICH	IRREGULAR_CAPTURED	120	2026-01-30 13:27:21.134634	30	200
53	Moon - Warm Ice Transition	Tidally superheated ice moon transitioning to mixed	MIXED_SILICATE_ICE	REGULAR_LARGE,REGULAR_MEDIUM,Cryovolcanic Moon	100	2026-01-30 14:06:24.353784	250	400
54	Moon - Large Icy Very Cold	Large icy moon in very cold environment	ICE_RICH	REGULAR_LARGE,COLLISION_DEBRIS	100	2026-01-30 14:49:31.66425	10	100
55	Moon - Large Icy Moderate	Large icy moon with moderate heating	MIXED_SILICATE_ICE	REGULAR_LARGE,COLLISION_DEBRIS,Cryovolcanic Moon	120	2026-01-30 14:49:31.66425	250	400
56	Moon - Large Icy Warm	Large moon transitioning from ice to rock	SILICATE_RICH	REGULAR_LARGE,COLLISION_DEBRIS	90	2026-01-30 14:49:31.66425	400	600
57	Moon - Large Icy Hot	Large moon with all ice vaporized	SILICATE_RICH	REGULAR_LARGE,COLLISION_DEBRIS	70	2026-01-30 14:49:31.66425	600	1000
58	Moon - Large Rocky Very Cold	Large rocky moon in very cold environment	SILICATE_RICH	REGULAR_LARGE,COLLISION_DEBRIS	90	2026-01-30 14:49:31.66425	10	100
59	Moon - Large Rocky Warm	Large warm rocky moon	SILICATE_RICH	REGULAR_LARGE,COLLISION_DEBRIS,Volcanic Moon	120	2026-01-30 14:49:31.66425	400	600
60	Moon - Large Rocky Hot	Large extremely hot rocky moon	SILICATE_RICH	REGULAR_LARGE,COLLISION_DEBRIS,Volcanic Moon	100	2026-01-30 14:49:31.66425	600	1000
61	Moon - Large Mixed Very Cold	Large mixed composition moon, very cold	MIXED_SILICATE_ICE	REGULAR_LARGE	100	2026-01-30 14:49:31.66425	10	100
62	Moon - Large Mixed Cold	Large mixed composition moon, cold	MIXED_SILICATE_ICE	REGULAR_LARGE	120	2026-01-30 14:49:31.66425	100	250
63	Moon - Large Mixed Warm	Large mixed moon with melting ice	MIXED_SILICATE_ICE	REGULAR_LARGE	100	2026-01-30 14:49:31.66425	400	600
64	Moon - Medium Icy Very Cold	Medium icy moon, very cold	ICE_RICH	REGULAR_MEDIUM	100	2026-01-30 14:49:31.66425	10	100
65	Moon - Medium Icy Moderate	Medium icy moon with moderate heating	MIXED_SILICATE_ICE	REGULAR_MEDIUM,Cryovolcanic Moon	110	2026-01-30 14:49:31.66425	250	400
66	Moon - Medium Icy Warm	Medium moon with vaporized ice	SILICATE_RICH	REGULAR_MEDIUM	90	2026-01-30 14:49:31.66425	400	600
67	Moon - Medium Icy Hot	Medium moon extremely heated	SILICATE_RICH	REGULAR_MEDIUM	70	2026-01-30 14:49:31.66425	600	1000
68	Moon - Medium Rocky Very Cold	Medium rocky moon, very cold	SILICATE_RICH	REGULAR_MEDIUM	90	2026-01-30 14:49:31.66425	10	100
69	Moon - Medium Rocky Warm	Medium warm rocky moon	SILICATE_RICH	REGULAR_MEDIUM,Volcanic Moon	110	2026-01-30 14:49:31.66425	400	600
70	Moon - Medium Rocky Hot	Medium extremely hot rocky moon	SILICATE_RICH	REGULAR_MEDIUM,Volcanic Moon	90	2026-01-30 14:49:31.66425	600	1000
7	Mini-Neptune - Ice-Rock Core	\N	MIXED_SILICATE_ICE	Mini-Neptune	250	2026-01-19 10:58:13.586308	20	300
8	Sub-Neptune - Ice Giant	\N	ICE_RICH	Sub-Neptune	200	2026-01-19 10:58:13.586308	20	200
12	Ice Giant - Neptune-like	\N	ICE_RICH	Ice Giant	220	2026-01-19 10:58:13.586308	20	200
71	Moon - Medium Mixed Very Cold	Medium mixed moon, very cold	MIXED_SILICATE_ICE	REGULAR_MEDIUM	100	2026-01-30 14:49:31.66425	10	100
72	Moon - Medium Mixed Cold	Medium mixed moon, cold	MIXED_SILICATE_ICE	REGULAR_MEDIUM	120	2026-01-30 14:49:31.66425	100	250
73	Moon - Medium Mixed Warm	Medium mixed moon with melting ice	MIXED_SILICATE_ICE	REGULAR_MEDIUM	100	2026-01-30 14:49:31.66425	400	600
77	Moon - Small Rocky Very Cold	Small rocky moon, very cold	SILICATE_RICH	REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED	100	2026-01-30 14:49:31.66425	10	100
78	Moon - Small Rocky Warm	Small warm rocky moon	SILICATE_RICH	REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED	100	2026-01-30 14:49:31.66425	400	600
79	Moon - Small Rocky Hot	Small extremely hot rocky moon	SILICATE_RICH	REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED	80	2026-01-30 14:49:31.66425	600	1000
44	Moon - Volcanic Active	Volcanically active moon like Io	SILICATE_RICH	Volcanic Moon,REGULAR_LARGE,REGULAR_MEDIUM	200	2026-01-30 13:27:21.134634	200	2000
52	Moon - Metal Rich	High metal content moon	IRON_RICH	Metal Rich Moon	150	2026-01-30 13:27:21.134634	10	2000
74	Moon - Small Icy Very Cold	Small icy moon, very cold	ICE_RICH	REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED	110	2026-01-30 14:49:31.66425	10	100
75	Moon - Small Icy Moderate	Small icy moon with moderate heating	MIXED_SILICATE_ICE	REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED	100	2026-01-30 14:49:31.66425	250	400
76	Moon - Small Icy Warm	Small moon with vaporized ice	SILICATE_RICH	REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED	80	2026-01-30 14:49:31.66425	400	600
80	Moon - Small Mixed Very Cold	Small mixed moon, very cold	MIXED_SILICATE_ICE	REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED	110	2026-01-30 14:49:31.66425	10	100
81	Moon - Small Mixed Cold	Small mixed moon, cold	MIXED_SILICATE_ICE	REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED	120	2026-01-30 14:49:31.66425	100	250
82	Moon - Small Mixed Warm	Small mixed moon with melting ice	MIXED_SILICATE_ICE	REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED	100	2026-01-30 14:49:31.66425	400	600
83	Moon - Small Mixed Moderate	Small mixed moon with moderate heating	MIXED_SILICATE_ICE	REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED	100	2026-01-30 15:05:05.501389	250	400
84	Moon - Small Rocky Moderate	Small rocky moon with moderate heating	SILICATE_RICH	REGULAR_SMALL,SHEPHERD,TROJAN,IRREGULAR_CAPTURED	110	2026-01-30 15:05:05.501389	250	400
16	Ice World - Frozen Volatiles	\N	ICE_RICH	Ice World	150	2026-01-19 10:58:13.586308	10	250
17	Dwarf - Ice-Rock Mix	\N	ICE_RICH	Dwarf Planet	100	2026-01-19 10:58:13.586308	10	250
85	Mini-Neptune - Warm Volatile-Poor	Close-in Mini-Neptune with partially evaporated ices	GAS_ENVELOPE	Mini-Neptune	150	2026-02-09 22:42:31.272584	300	800
86	Sub-Neptune - Warm Transitional	Warm Sub-Neptune with reduced ice fraction	MIXED_SILICATE_ICE	Sub-Neptune	120	2026-02-09 22:42:31.272584	200	600
87	Warm Neptune - Depleted Ice	Warm Neptune with reduced volatile envelope	MIXED_SILICATE_ICE	Warm Neptune	200	2026-02-10 20:06:36.756907	150	400
88	Hot Neptune - Stripped Envelope	Hot Neptune with heavily eroded atmosphere	GAS_ENVELOPE	Hot Neptune	200	2026-02-10 20:06:36.756907	400	1200
\.



--
-- Data for Name: composition_template_component; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.composition_template_component (id, template_id, mineral, layer_type, min_percentage, max_percentage) FROM stdin;
1	1	IRON	INTERIOR	70	85
2	1	NICKEL	INTERIOR	10	20
3	1	MAGNESIUM_OXIDE	INTERIOR	5	15
6	2	OLIVINE	INTERIOR	45	55
7	2	PYROXENE	INTERIOR	30	40
8	2	IRON	INTERIOR	10	15
12	3	OLIVINE	INTERIOR	40	50
13	3	PYROXENE	INTERIOR	30	40
14	3	IRON	INTERIOR	15	25
15	3	MAGNESIUM_OXIDE	INTERIOR	5	10
19	4	OLIVINE	INTERIOR	40	50
20	4	PYROXENE	INTERIOR	30	40
21	4	IRON	INTERIOR	10	20
25	5	OLIVINE	INTERIOR	40	50
26	5	PYROXENE	INTERIOR	30	40
27	5	IRON	INTERIOR	10	20
29	6	OLIVINE	INTERIOR	45	55
30	6	PYROXENE	INTERIOR	30	40
31	6	IRON	INTERIOR	10	20
34	7	OLIVINE	INTERIOR	25	35
35	7	WATER_ICE	INTERIOR	35	50
36	7	METHANE_ICE	INTERIOR	10	20
37	7	AMMONIA_ICE	INTERIOR	5	15
39	8	WATER_ICE	INTERIOR	40	55
40	8	METHANE_ICE	INTERIOR	20	30
41	8	AMMONIA_ICE	INTERIOR	15	25
42	8	OLIVINE	INTERIOR	5	15
44	9	HYDROGEN_HELIUM_MIX	INTERIOR	90	98
45	9	WATER_ICE	INTERIOR	1	5
47	10	HYDROGEN_HELIUM_MIX	INTERIOR	85	92
48	10	WATER_ICE	INTERIOR	3	8
49	10	METHANE_ICE	INTERIOR	2	5
50	10	AMMONIA_ICE	INTERIOR	1	3
52	11	HYDROGEN_HELIUM_MIX	INTERIOR	80	90
53	11	METALLIC_HYDROGEN	INTERIOR	5	15
54	11	WATER_ICE	INTERIOR	2	5
56	12	WATER_ICE	INTERIOR	45	60
57	12	METHANE_ICE	INTERIOR	20	30
58	12	AMMONIA_ICE	INTERIOR	10	20
59	12	OLIVINE	INTERIOR	5	10
61	13	HYDROGEN_HELIUM_MIX	INTERIOR	92	98
62	13	WATER_ICE	INTERIOR	1	4
64	14	GRAPHITE	INTERIOR	40	50
65	14	SILICON_CARBIDE	INTERIOR	25	35
66	14	DIAMOND	INTERIOR	10	20
67	14	IRON_CARBIDE	INTERIOR	5	15
71	15	IRON	INTERIOR	80	90
72	15	NICKEL	INTERIOR	10	20
75	16	WATER_ICE	INTERIOR	50	70
76	16	METHANE_ICE	INTERIOR	15	30
77	16	NITROGEN_ICE	INTERIOR	5	15
78	16	OLIVINE	INTERIOR	5	10
82	17	OLIVINE	INTERIOR	30	45
83	17	WATER_ICE	INTERIOR	30	45
84	17	METHANE_ICE	INTERIOR	10	20
85	17	NITROGEN_ICE	INTERIOR	5	15
96	19	OLIVINE	INTERIOR	35	45
97	19	PYROXENE	INTERIOR	25	35
98	19	IRON	INTERIOR	20	30
102	20	OLIVINE	INTERIOR	35	45
103	20	PYROXENE	INTERIOR	25	35
104	20	IRON	INTERIOR	15	25
4	1	IRON	ENVELOPE	80	95
5	1	NICKEL	ENVELOPE	5	15
9	2	FELDSPAR	ENVELOPE	45	60
10	2	QUARTZ	ENVELOPE	20	30
11	2	PYROXENE	ENVELOPE	10	25
16	3	BASALT	ENVELOPE	50	70
17	3	FELDSPAR	ENVELOPE	20	35
18	3	IRON	ENVELOPE	5	15
22	4	SILICON_DIOXIDE	ENVELOPE	40	60
23	4	IRON	ENVELOPE	15	30
24	4	ALUMINUM_OXIDE	ENVELOPE	10	20
28	5	LIQUID_WATER	ENVELOPE	80	100
32	6	MOLTEN_ROCK	ENVELOPE	70	90
33	6	BASALT	ENVELOPE	10	30
38	7	HYDROGEN_HELIUM_MIX	ENVELOPE	100	100
43	8	HYDROGEN_HELIUM_MIX	ENVELOPE	100	100
46	9	HYDROGEN_HELIUM_MIX	ENVELOPE	100	100
51	10	HYDROGEN_HELIUM_MIX	ENVELOPE	100	100
55	11	HYDROGEN_HELIUM_MIX	ENVELOPE	100	100
60	12	HYDROGEN_HELIUM_MIX	ENVELOPE	100	100
63	13	HYDROGEN_HELIUM_MIX	ENVELOPE	100	100
68	14	GRAPHITE	ENVELOPE	50	70
69	14	DIAMOND	ENVELOPE	15	30
70	14	AMORPHOUS_CARBON	ENVELOPE	5	20
73	15	IRON	ENVELOPE	85	95
74	15	NICKEL	ENVELOPE	5	15
79	16	WATER_ICE	ENVELOPE	60	80
80	16	METHANE_ICE	ENVELOPE	10	25
81	16	NITROGEN_ICE	ENVELOPE	5	20
86	17	WATER_ICE	ENVELOPE	50	70
87	17	METHANE_ICE	ENVELOPE	15	30
88	17	NITROGEN_ICE	ENVELOPE	5	20
99	19	BASALT	ENVELOPE	40	60
100	19	IRON	ENVELOPE	20	35
101	19	FELDSPAR	ENVELOPE	15	25
105	20	HIGH_PRESSURE_ICE	ENVELOPE	50	70
106	20	WATER_ICE	ENVELOPE	30	50
188	37	OLIVINE	INTERIOR	35	45
189	37	PYROXENE	INTERIOR	25	35
190	37	IRON	INTERIOR	15	25
191	37	PLAGIOCLASE	ENVELOPE	40	60
192	37	BASALT	ENVELOPE	30	50
193	38	OLIVINE	INTERIOR	30	40
194	38	PYROXENE	INTERIOR	25	35
195	38	IRON	INTERIOR	15	25
196	38	WATER_ICE	ENVELOPE	80	95
197	38	SILICATE	ENVELOPE	3	10
198	39	OLIVINE	INTERIOR	40	50
199	39	PYROXENE	INTERIOR	25	35
200	39	IRON	INTERIOR	10	20
201	39	BASALT	ENVELOPE	50	70
202	39	PLAGIOCLASE	ENVELOPE	25	40
203	40	OLIVINE	INTERIOR	35	45
204	40	PYROXENE	INTERIOR	25	35
205	40	IRON	INTERIOR	15	25
206	40	WATER_ICE	ENVELOPE	75	90
207	40	SILICATE	ENVELOPE	5	15
208	41	OLIVINE	INTERIOR	40	55
209	41	PYROXENE	INTERIOR	25	35
210	41	IRON	INTERIOR	8	18
211	41	REGOLITH	ENVELOPE	50	70
212	41	BASALT	ENVELOPE	25	40
213	42	OLIVINE	INTERIOR	30	40
214	42	PYROXENE	INTERIOR	20	30
215	42	WATER_ICE	INTERIOR	25	40
216	42	WATER_ICE	ENVELOPE	85	95
217	42	SILICATE	ENVELOPE	3	10
218	43	OLIVINE	INTERIOR	35	45
219	43	PYROXENE	INTERIOR	25	35
220	43	IRON	INTERIOR	10	20
221	43	PLAGIOCLASE	ENVELOPE	40	60
222	43	BASALT	ENVELOPE	30	50
223	44	OLIVINE	INTERIOR	40	50
224	44	PYROXENE	INTERIOR	30	40
225	44	IRON	INTERIOR	5	15
226	44	SULFUR	ENVELOPE	30	50
227	44	BASALT	ENVELOPE	40	60
228	45	OLIVINE	INTERIOR	40	50
229	45	PYROXENE	INTERIOR	25	35
230	45	IRON	INTERIOR	15	25
231	45	WATER_ICE	ENVELOPE	70	90
232	45	SILICATE	ENVELOPE	5	15
233	46	OLIVINE	INTERIOR	30	40
234	46	PYROXENE	INTERIOR	25	35
235	46	WATER_ICE	INTERIOR	20	30
236	46	NITROGEN_ICE	ENVELOPE	40	60
237	46	METHANE_ICE	ENVELOPE	20	40
238	46	THOLINS	ENVELOPE	5	15
239	47	OLIVINE	INTERIOR	35	50
240	47	PYROXENE	INTERIOR	25	40
241	47	IRON	INTERIOR	10	25
242	47	REGOLITH	ENVELOPE	60	80
243	47	BASALT	ENVELOPE	15	30
244	48	OLIVINE	INTERIOR	35	50
245	48	PYROXENE	INTERIOR	20	35
246	48	IRON	INTERIOR	15	30
247	48	REGOLITH	ENVELOPE	50	70
248	48	BASALT	ENVELOPE	20	40
249	49	WATER_ICE	INTERIOR	50	70
250	49	SILICATE	INTERIOR	20	35
251	49	ORGANIC	INTERIOR	5	15
252	49	WATER_ICE	ENVELOPE	70	90
253	49	METHANE_ICE	ENVELOPE	5	20
254	50	OLIVINE	INTERIOR	35	50
255	50	PYROXENE	INTERIOR	20	35
256	50	IRON	INTERIOR	15	30
257	50	REGOLITH	ENVELOPE	60	80
258	50	BASALT	ENVELOPE	15	30
259	51	WATER_ICE	INTERIOR	50	70
260	51	SILICATE	INTERIOR	20	35
261	51	ORGANIC	INTERIOR	5	15
262	51	WATER_ICE	ENVELOPE	60	80
263	51	METHANE_ICE	ENVELOPE	10	25
264	52	IRON	INTERIOR	50	70
265	52	NICKEL	INTERIOR	10	20
266	52	OLIVINE	INTERIOR	10	20
267	52	IRON	ENVELOPE	30	50
268	52	BASALT	ENVELOPE	20	40
269	53	OLIVINE	INTERIOR	35	45
270	53	PYROXENE	INTERIOR	25	35
271	53	WATER_ICE	INTERIOR	15	25
272	53	WATER_ICE	ENVELOPE	40	60
273	53	SILICATE	ENVELOPE	30	50
274	54	OLIVINE	INTERIOR	25	35
275	54	PYROXENE	INTERIOR	20	30
276	54	WATER_ICE	INTERIOR	30	45
277	54	NITROGEN_ICE	ENVELOPE	30	50
278	54	METHANE_ICE	ENVELOPE	25	40
279	54	WATER_ICE	ENVELOPE	15	25
280	55	OLIVINE	INTERIOR	30	40
281	55	PYROXENE	INTERIOR	25	35
282	55	WATER_ICE	INTERIOR	20	30
283	55	WATER_ICE	ENVELOPE	40	60
284	55	SILICATE	ENVELOPE	35	50
285	56	OLIVINE	INTERIOR	35	45
286	56	PYROXENE	INTERIOR	25	35
287	56	IRON	INTERIOR	15	25
288	56	BASALT	ENVELOPE	50	70
289	56	SILICATE	ENVELOPE	25	40
290	57	OLIVINE	INTERIOR	40	50
291	57	PYROXENE	INTERIOR	25	35
292	57	IRON	INTERIOR	15	25
293	57	BASALT	ENVELOPE	55	75
294	57	PLAGIOCLASE	ENVELOPE	20	35
295	58	OLIVINE	INTERIOR	35	45
296	58	PYROXENE	INTERIOR	25	35
297	58	IRON	INTERIOR	15	25
298	58	PLAGIOCLASE	ENVELOPE	40	60
299	58	BASALT	ENVELOPE	30	50
300	59	OLIVINE	INTERIOR	38	48
301	59	PYROXENE	INTERIOR	25	35
302	59	IRON	INTERIOR	12	22
303	59	BASALT	ENVELOPE	45	65
304	59	PLAGIOCLASE	ENVELOPE	30	45
305	60	OLIVINE	INTERIOR	35	50
306	60	PYROXENE	INTERIOR	25	40
307	60	IRON	INTERIOR	10	25
308	60	BASALT	ENVELOPE	50	70
309	60	SILICATE	ENVELOPE	25	40
310	61	OLIVINE	INTERIOR	25	35
311	61	PYROXENE	INTERIOR	20	30
312	61	WATER_ICE	INTERIOR	30	40
313	61	WATER_ICE	ENVELOPE	50	70
314	61	SILICATE	ENVELOPE	25	40
315	62	OLIVINE	INTERIOR	28	38
316	62	PYROXENE	INTERIOR	22	32
317	62	WATER_ICE	INTERIOR	25	35
318	62	WATER_ICE	ENVELOPE	45	65
319	62	SILICATE	ENVELOPE	30	45
320	63	OLIVINE	INTERIOR	35	45
321	63	PYROXENE	INTERIOR	25	35
322	63	IRON	INTERIOR	15	25
323	63	SILICATE	ENVELOPE	50	70
324	63	BASALT	ENVELOPE	25	40
325	64	OLIVINE	INTERIOR	25	35
326	64	PYROXENE	INTERIOR	20	30
327	64	WATER_ICE	INTERIOR	30	45
328	64	NITROGEN_ICE	ENVELOPE	35	55
329	64	METHANE_ICE	ENVELOPE	25	40
330	64	WATER_ICE	ENVELOPE	10	20
331	65	OLIVINE	INTERIOR	30	40
332	65	PYROXENE	INTERIOR	25	35
333	65	WATER_ICE	INTERIOR	20	30
334	65	WATER_ICE	ENVELOPE	40	60
335	65	SILICATE	ENVELOPE	35	50
336	66	OLIVINE	INTERIOR	35	45
337	66	PYROXENE	INTERIOR	25	35
338	66	IRON	INTERIOR	15	25
339	66	BASALT	ENVELOPE	50	70
340	66	SILICATE	ENVELOPE	25	40
341	67	OLIVINE	INTERIOR	40	50
342	67	PYROXENE	INTERIOR	25	35
343	67	IRON	INTERIOR	15	25
344	67	BASALT	ENVELOPE	55	75
345	67	PLAGIOCLASE	ENVELOPE	20	35
346	68	OLIVINE	INTERIOR	38	48
347	68	PYROXENE	INTERIOR	25	35
348	68	IRON	INTERIOR	10	20
349	68	BASALT	ENVELOPE	50	70
350	68	PLAGIOCLASE	ENVELOPE	25	40
351	69	OLIVINE	INTERIOR	38	48
352	69	PYROXENE	INTERIOR	25	35
353	69	IRON	INTERIOR	10	20
354	69	BASALT	ENVELOPE	45	65
355	69	PLAGIOCLASE	ENVELOPE	30	45
356	70	OLIVINE	INTERIOR	35	50
357	70	PYROXENE	INTERIOR	25	40
358	70	IRON	INTERIOR	10	25
359	70	BASALT	ENVELOPE	50	70
360	70	SILICATE	ENVELOPE	25	40
361	71	OLIVINE	INTERIOR	25	35
362	71	PYROXENE	INTERIOR	20	30
363	71	WATER_ICE	INTERIOR	30	40
364	71	WATER_ICE	ENVELOPE	50	70
365	71	SILICATE	ENVELOPE	25	40
366	72	OLIVINE	INTERIOR	28	38
367	72	PYROXENE	INTERIOR	22	32
368	72	WATER_ICE	INTERIOR	25	35
369	72	WATER_ICE	ENVELOPE	45	65
370	72	SILICATE	ENVELOPE	30	45
371	73	OLIVINE	INTERIOR	35	45
372	73	PYROXENE	INTERIOR	25	35
373	73	IRON	INTERIOR	15	25
374	73	SILICATE	ENVELOPE	50	70
375	73	BASALT	ENVELOPE	25	40
376	74	OLIVINE	INTERIOR	20	30
377	74	PYROXENE	INTERIOR	15	25
378	74	WATER_ICE	INTERIOR	35	50
379	74	NITROGEN_ICE	ENVELOPE	35	55
380	74	METHANE_ICE	ENVELOPE	25	40
381	74	WATER_ICE	ENVELOPE	10	20
382	75	OLIVINE	INTERIOR	28	38
383	75	PYROXENE	INTERIOR	20	30
384	75	WATER_ICE	INTERIOR	25	35
385	75	WATER_ICE	ENVELOPE	40	60
386	75	SILICATE	ENVELOPE	35	50
387	76	OLIVINE	INTERIOR	35	50
388	76	PYROXENE	INTERIOR	25	40
389	76	IRON	INTERIOR	8	18
390	76	REGOLITH	ENVELOPE	50	70
391	76	BASALT	ENVELOPE	25	40
392	77	OLIVINE	INTERIOR	40	55
393	77	PYROXENE	INTERIOR	25	35
394	77	IRON	INTERIOR	8	18
395	77	REGOLITH	ENVELOPE	50	70
396	77	BASALT	ENVELOPE	25	40
397	78	OLIVINE	INTERIOR	40	55
398	78	PYROXENE	INTERIOR	25	35
399	78	IRON	INTERIOR	8	18
400	78	REGOLITH	ENVELOPE	50	70
401	78	BASALT	ENVELOPE	25	40
402	79	OLIVINE	INTERIOR	35	50
403	79	PYROXENE	INTERIOR	25	40
404	79	IRON	INTERIOR	10	25
405	79	BASALT	ENVELOPE	50	70
406	79	SILICATE	ENVELOPE	25	40
407	80	OLIVINE	INTERIOR	22	32
408	80	PYROXENE	INTERIOR	18	28
409	80	WATER_ICE	INTERIOR	35	45
410	80	WATER_ICE	ENVELOPE	50	70
411	80	SILICATE	ENVELOPE	25	40
412	81	OLIVINE	INTERIOR	25	35
413	81	PYROXENE	INTERIOR	20	30
414	81	WATER_ICE	INTERIOR	30	40
415	81	WATER_ICE	ENVELOPE	45	65
416	81	SILICATE	ENVELOPE	30	45
417	82	OLIVINE	INTERIOR	32	42
418	82	PYROXENE	INTERIOR	25	35
419	82	IRON	INTERIOR	15	25
420	82	SILICATE	ENVELOPE	50	70
421	82	BASALT	ENVELOPE	25	40
422	83	OLIVINE	INTERIOR	28	38
423	83	PYROXENE	INTERIOR	22	32
424	83	WATER_ICE	INTERIOR	20	30
425	83	WATER_ICE	ENVELOPE	35	55
426	83	SILICATE	ENVELOPE	40	55
427	84	OLIVINE	INTERIOR	40	55
428	84	PYROXENE	INTERIOR	25	35
429	84	IRON	INTERIOR	8	18
430	84	REGOLITH	ENVELOPE	45	65
431	84	BASALT	ENVELOPE	30	45
432	85	OLIVINE	INTERIOR	35	50
433	85	PYROXENE	INTERIOR	20	30
434	85	IRON	INTERIOR	15	25
435	85	HYDROGEN_HELIUM_MIX	ENVELOPE	70	85
436	85	WATER_ICE	ENVELOPE	10	25
437	86	WATER_ICE	INTERIOR	25	40
438	86	OLIVINE	INTERIOR	25	35
439	86	PYROXENE	INTERIOR	15	25
440	86	HYDROGEN_HELIUM_MIX	ENVELOPE	80	95
441	86	WATER_ICE	ENVELOPE	5	15
442	87	OLIVINE	INTERIOR	30	45
443	87	PYROXENE	INTERIOR	15	25
444	87	WATER_ICE	INTERIOR	15	30
445	87	IRON	INTERIOR	10	20
446	87	HYDROGEN_HELIUM_MIX	ENVELOPE	60	80
447	87	WATER_ICE	ENVELOPE	15	30
448	88	OLIVINE	INTERIOR	35	50
449	88	PYROXENE	INTERIOR	20	30
450	88	IRON	INTERIOR	20	30
451	88	HYDROGEN_HELIUM_MIX	ENVELOPE	50	70
452	88	WATER_ICE	ENVELOPE	5	15
\.



--
-- Data for Name: geological_template; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.geological_template (id, name, description, planet_types, activity_level, min_activity_score, max_activity_score, min_planet_mass_earth, max_planet_mass_earth, rarity_weight, created_at) FROM stdin;
5	Molten Lava World	Surface entirely molten, extreme volcanic activity	Lava Planet	Highly Active	0.5	100	0.5	2	60	2026-01-19 19:19:25.03809
31	Moon - Ancient Dead	Geologically dead moon with extreme cratering	REGULAR_SMALL,REGULAR_MEDIUM,IRREGULAR_CAPTURED,TROJAN,COLLISION_DEBRIS	Dead	0	100	0	1	300	2026-01-30 16:50:50.562933
32	Moon - Old Inactive	Inactive moon with moderate cratering	REGULAR_MEDIUM,REGULAR_LARGE,COLLISION_DEBRIS	Low Activity	0	100	0	1	200	2026-01-30 16:50:50.562933
33	Moon - Active Rocky	Tidally heated moon with volcanic activity	REGULAR_MEDIUM,REGULAR_LARGE,COLLISION_DEBRIS	Moderately Active	0	100	0	1	150	2026-01-30 16:50:50.562933
34	Moon - Hyperactive Volcanic	Extremely active volcanic moon (Io-like)	REGULAR_MEDIUM,REGULAR_LARGE	Highly Active	0	100	0	1	50	2026-01-30 16:50:50.562933
35	Moon - Inactive Icy	Cold icy moon with no cryovolcanic activity	REGULAR_SMALL,REGULAR_MEDIUM,IRREGULAR_CAPTURED,TROJAN	Dead	0	100	0	1	250	2026-01-30 16:50:50.562933
36	Moon - Cryovolcanic	Active icy moon with cryovolcanic features (Europa/Enceladus-like)	REGULAR_MEDIUM,REGULAR_LARGE	Moderately Active	0	100	0	1	100	2026-01-30 16:50:50.562933
37	Moon - Hyperactive Cryovolcanic	Extremely active icy moon with powerful geysers	REGULAR_MEDIUM,REGULAR_LARGE	Highly Active	0	100	0	1	40	2026-01-30 16:50:50.562933
1	Dead Rocky World	Ancient, geologically dead world with heavily cratered surface	Hot Rocky Planet,Terrestrial Planet,Desert Planet,Ocean Planet,Super-Earth,Ice World,Iron Planet,Carbon Planet,Dwarf Planet,Rogue Planet	Geologically Dead	0	0.1	0.1	2	100	2026-01-19 19:19:25.03809
2	Stagnant Lid World	Minimal geological activity, occasional volcanism	Terrestrial Planet,Super-Earth,Desert Planet,Hot Rocky Planet,Ocean Planet,Ice World,Iron Planet,Carbon Planet,Dwarf Planet,Rogue Planet	Low Activity	0.1	0.5	0.3	3	120	2026-01-19 19:19:25.03809
3	Active Rocky World	Moderate geological activity with plate tectonics and volcanism	Terrestrial Planet,Super-Earth,Ocean Planet,Desert Planet,Hot Rocky Planet,Ice World,Iron Planet,Carbon Planet	Moderately Active	0.5	2	0.5	3	150	2026-01-19 19:19:25.03809
4	Hyperactive World	Extremely active geology with intense volcanism and tectonics	Terrestrial Planet,Super-Earth,Lava Planet,Desert Planet,Hot Rocky Planet,Ocean Planet,Iron Planet,Carbon Planet	Highly Active	2	100	0.8	5	80	2026-01-19 19:19:25.03809
6	Cryovolcanic Ice World	Ice world with cryovolcanic activity	Ice World,Ocean Planet	Moderately Active	0.1	2	0.3	3	70	2026-01-19 19:19:25.03809
38	Dead Ice World	Ancient frozen world with no geological activity	Ice World,Rogue Planet,Dwarf Planet	Geologically Dead	0	0.1	0.001	1	100	2026-02-03 22:05:53.338062
39	Hyperactive Ice World	Ice world with intense cryovolcanism and tectonic activity	Ice World,Ocean Planet	Highly Active	2	100	0.3	3	50	2026-02-03 22:05:53.338062
7	Calm Gas Giant	Gas giant with minimal atmospheric disturbances	Mini-Neptune,Sub-Neptune,Gas Giant,Ice Giant,Hot Jupiter,Super-Jupiter,Puffy Planet,Warm Neptune,Hot Neptune	Low Activity	0	0.5	10	100	80	2026-01-19 19:19:25.03809
8	Active Gas Giant	Gas giant with moderate storms and atmospheric activity	Gas Giant,Ice Giant,Sub-Neptune,Mini-Neptune,Hot Jupiter,Super-Jupiter,Puffy Planet,Warm Neptune,Hot Neptune	Moderately Active	0.5	2	80	400	120	2026-01-19 19:19:25.03809
9	Turbulent Super-Jupiter	Massive gas giant with extreme storms and atmospheric chaos	Gas Giant,Super-Jupiter,Hot Jupiter,Ice Giant,Sub-Neptune,Mini-Neptune,Puffy Planet,Warm Neptune,Hot Neptune	Highly Active	2	100	300	3000	60	2026-01-19 19:19:25.03809
\.



--
-- Data for Name: geological_template_feature; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.geological_template_feature (id, template_id, feature_type, feature_value, min_value, max_value, description) FROM stdin;
1	1	TECTONICS	None	\N	\N	\N
2	1	VOLCANISM_TYPE	None	\N	\N	\N
3	1	VOLCANIC_ACTIVITY	false	\N	\N	\N
4	1	VOLCANIC_INTENSITY	None	\N	\N	\N
5	1	ACTIVE_VOLCANOES	numeric	0	0	\N
6	1	MOUNTAIN_COVERAGE	numeric	2	8	\N
7	1	MAX_ELEVATION	numeric	0.5	3	\N
8	1	TERRAIN_ROUGHNESS	numeric	1	2.5	\N
9	1	CRATERING_LEVEL	Saturated	\N	\N	\N
10	1	VISIBLE_CRATERS	numeric	100000	1000000	\N
11	1	EROSION_LEVEL	None	\N	\N	\N
12	1	EROSION_AGENT	None	\N	\N	\N
13	2	TECTONICS	Stagnant Lid	\N	\N	\N
14	2	VOLCANISM_TYPE	Silicate	\N	\N	\N
15	2	VOLCANIC_ACTIVITY	true	\N	\N	\N
16	2	VOLCANIC_INTENSITY	Rare	\N	\N	\N
17	2	ACTIVE_VOLCANOES	numeric	1	10	\N
18	2	MOUNTAIN_COVERAGE	numeric	5	12	\N
19	2	MAX_ELEVATION	numeric	2	6	\N
20	2	TERRAIN_ROUGHNESS	numeric	1.5	3.5	\N
21	2	CRATERING_LEVEL	Heavy	\N	\N	\N
22	2	VISIBLE_CRATERS	numeric	10000	100000	\N
23	2	EROSION_LEVEL	Minimal	\N	\N	\N
24	2	EROSION_AGENT	Wind	\N	\N	\N
25	3	TECTONICS	Active	\N	\N	\N
26	3	PLATE_TECTONICS	true	\N	\N	\N
27	3	TECTONIC_PLATES	numeric	5	15	\N
28	3	VOLCANISM_TYPE	Silicate	\N	\N	\N
29	3	VOLCANIC_ACTIVITY	true	\N	\N	\N
30	3	VOLCANIC_INTENSITY	Moderate	\N	\N	\N
31	3	ACTIVE_VOLCANOES	numeric	10	50	\N
32	3	MOUNTAIN_COVERAGE	numeric	12	25	\N
33	3	MAX_ELEVATION	numeric	6	10	\N
34	3	TERRAIN_ROUGHNESS	numeric	4	6.5	\N
35	3	CRATERING_LEVEL	Moderate	\N	\N	\N
36	3	VISIBLE_CRATERS	numeric	1000	10000	\N
37	3	EROSION_LEVEL	Moderate	\N	\N	\N
38	3	EROSION_AGENT	Water	\N	\N	\N
39	4	TECTONICS	Hyperactive	\N	\N	\N
40	4	PLATE_TECTONICS	true	\N	\N	\N
41	4	TECTONIC_PLATES	numeric	8	20	\N
42	4	VOLCANISM_TYPE	Silicate	\N	\N	\N
43	4	VOLCANIC_ACTIVITY	true	\N	\N	\N
44	4	VOLCANIC_INTENSITY	Continuous	\N	\N	\N
45	4	ACTIVE_VOLCANOES	numeric	200	800	\N
46	4	MOUNTAIN_COVERAGE	numeric	20	35	\N
47	4	MAX_ELEVATION	numeric	8	15	\N
48	4	TERRAIN_ROUGHNESS	numeric	6	8.5	\N
49	4	CRATERING_LEVEL	Pristine	\N	\N	\N
50	4	VISIBLE_CRATERS	numeric	10	100	\N
51	4	EROSION_LEVEL	Heavy	\N	\N	\N
52	4	EROSION_AGENT	Volcanic	\N	\N	\N
53	5	TECTONICS	Molten	\N	\N	\N
54	5	VOLCANISM_TYPE	Silicate	\N	\N	\N
55	5	VOLCANIC_ACTIVITY	true	\N	\N	\N
56	5	VOLCANIC_INTENSITY	Continuous	\N	\N	\N
57	5	ACTIVE_VOLCANOES	numeric	1000	10000	\N
58	5	MOUNTAIN_COVERAGE	numeric	30	50	\N
59	5	MAX_ELEVATION	numeric	5	12	\N
60	5	TERRAIN_ROUGHNESS	numeric	8	10	\N
61	5	CRATERING_LEVEL	None	\N	\N	\N
62	5	VISIBLE_CRATERS	numeric	0	10	\N
63	5	EROSION_LEVEL	Extreme	\N	\N	\N
64	5	EROSION_AGENT	Lava	\N	\N	\N
65	6	TECTONICS	Ice Shell	\N	\N	\N
66	6	VOLCANISM_TYPE	Cryovolcanic	\N	\N	\N
67	6	VOLCANIC_ACTIVITY	true	\N	\N	\N
68	6	VOLCANIC_INTENSITY	Moderate	\N	\N	\N
69	6	ACTIVE_VOLCANOES	numeric	5	30	\N
70	6	MOUNTAIN_COVERAGE	numeric	8	18	\N
71	6	MAX_ELEVATION	numeric	3	8	\N
72	6	TERRAIN_ROUGHNESS	numeric	3	6	\N
73	6	CRATERING_LEVEL	Light	\N	\N	\N
74	6	VISIBLE_CRATERS	numeric	100	5000	\N
75	6	EROSION_LEVEL	Moderate	\N	\N	\N
76	6	EROSION_AGENT	Ice	\N	\N	\N
77	7	TECTONICS	N/A	\N	\N	\N
78	7	VOLCANISM_TYPE	Atmospheric	\N	\N	\N
79	7	ATMOSPHERIC_CONVECTION	Minimal	\N	\N	\N
80	7	GREAT_STORM	false	\N	\N	\N
81	7	MAJOR_STORMS	numeric	0	2	\N
82	7	TERRAIN_ROUGHNESS	numeric	3	5	\N
83	8	TECTONICS	N/A	\N	\N	\N
84	8	VOLCANISM_TYPE	Atmospheric	\N	\N	\N
85	8	ATMOSPHERIC_CONVECTION	Moderate	\N	\N	\N
86	8	GREAT_STORM	random_50	\N	\N	\N
87	8	MAJOR_STORMS	numeric	2	10	\N
88	8	TERRAIN_ROUGHNESS	numeric	6	8	\N
89	9	TECTONICS	N/A	\N	\N	\N
90	9	VOLCANISM_TYPE	Atmospheric	\N	\N	\N
91	9	ATMOSPHERIC_CONVECTION	Extreme	\N	\N	\N
92	9	GREAT_STORM	true	\N	\N	\N
93	9	MAJOR_STORMS	numeric	5	20	\N
94	9	TERRAIN_ROUGHNESS	numeric	9	10	\N
95	31	VOLCANISM_TYPE	None	\N	\N	\N
96	31	VOLCANIC_INTENSITY	None	\N	\N	\N
97	31	ACTIVE_VOLCANOES	numeric	0	0	\N
98	31	MOUNTAIN_COVERAGE	numeric	1	5	\N
99	31	MAX_ELEVATION	numeric	0.3	2	\N
100	31	TERRAIN_ROUGHNESS	numeric	0.5	2	\N
101	31	EROSION_LEVEL	None	\N	\N	\N
102	31	EROSION_AGENT	None	\N	\N	\N
103	32	VOLCANISM_TYPE	None	\N	\N	\N
104	32	VOLCANIC_INTENSITY	None	\N	\N	\N
105	32	ACTIVE_VOLCANOES	numeric	0	0	\N
106	32	MOUNTAIN_COVERAGE	numeric	3	10	\N
107	32	MAX_ELEVATION	numeric	1	4	\N
108	32	TERRAIN_ROUGHNESS	numeric	1.5	3	\N
109	32	EROSION_LEVEL	Minimal	\N	\N	\N
110	32	EROSION_AGENT	Meteorite	\N	\N	\N
111	33	VOLCANISM_TYPE	Silicate	\N	\N	\N
112	33	VOLCANIC_INTENSITY	Moderate	\N	\N	\N
113	33	ACTIVE_VOLCANOES	numeric	5	50	\N
114	33	MOUNTAIN_COVERAGE	numeric	8	20	\N
115	33	MAX_ELEVATION	numeric	3	8	\N
116	33	TERRAIN_ROUGHNESS	numeric	3	5.5	\N
117	33	EROSION_LEVEL	Moderate	\N	\N	\N
118	33	EROSION_AGENT	Volcanic	\N	\N	\N
119	34	VOLCANISM_TYPE	Silicate	\N	\N	\N
120	34	VOLCANIC_INTENSITY	Continuous	\N	\N	\N
121	34	ACTIVE_VOLCANOES	numeric	100	400	\N
122	34	MOUNTAIN_COVERAGE	numeric	15	35	\N
123	34	MAX_ELEVATION	numeric	5	17	\N
124	34	TERRAIN_ROUGHNESS	numeric	6	9	\N
125	34	EROSION_LEVEL	Heavy	\N	\N	\N
126	34	EROSION_AGENT	Volcanic	\N	\N	\N
127	35	VOLCANISM_TYPE	None	\N	\N	\N
128	35	VOLCANIC_INTENSITY	None	\N	\N	\N
129	35	ACTIVE_VOLCANOES	numeric	0	0	\N
130	35	MOUNTAIN_COVERAGE	numeric	0.5	4	\N
131	35	MAX_ELEVATION	numeric	0.1	1.5	\N
132	35	TERRAIN_ROUGHNESS	numeric	0.3	1.5	\N
133	35	EROSION_LEVEL	None	\N	\N	\N
134	35	EROSION_AGENT	None	\N	\N	\N
135	36	VOLCANISM_TYPE	Cryovolcanic	\N	\N	\N
136	36	VOLCANIC_INTENSITY	Moderate	\N	\N	\N
137	36	ACTIVE_VOLCANOES	numeric	3	30	\N
138	36	MOUNTAIN_COVERAGE	numeric	5	15	\N
139	36	MAX_ELEVATION	numeric	1	5	\N
140	36	TERRAIN_ROUGHNESS	numeric	2	4.5	\N
141	36	EROSION_LEVEL	Moderate	\N	\N	\N
142	36	EROSION_AGENT	Cryovolcanic	\N	\N	\N
143	37	VOLCANISM_TYPE	Cryovolcanic	\N	\N	\N
144	37	VOLCANIC_INTENSITY	Continuous	\N	\N	\N
145	37	ACTIVE_VOLCANOES	numeric	50	200	\N
146	37	MOUNTAIN_COVERAGE	numeric	10	25	\N
147	37	MAX_ELEVATION	numeric	2	8	\N
148	37	TERRAIN_ROUGHNESS	numeric	4	7.5	\N
149	37	EROSION_LEVEL	Heavy	\N	\N	\N
150	37	EROSION_AGENT	Cryovolcanic	\N	\N	\N
151	38	TECTONICS	None	\N	\N	\N
152	38	VOLCANISM_TYPE	None	\N	\N	\N
153	38	VOLCANIC_ACTIVITY	false	\N	\N	\N
154	38	VOLCANIC_INTENSITY	None	\N	\N	\N
155	38	ACTIVE_VOLCANOES	numeric	0	0	\N
156	38	MOUNTAIN_COVERAGE	numeric	1	5	\N
157	38	MAX_ELEVATION	numeric	0.5	2	\N
158	38	TERRAIN_ROUGHNESS	numeric	1	2	\N
159	38	CRATERING_LEVEL	Saturated	\N	\N	\N
160	38	VISIBLE_CRATERS	numeric	50000	500000	\N
161	38	EROSION_LEVEL	None	\N	\N	\N
162	38	EROSION_AGENT	None	\N	\N	\N
163	39	TECTONICS	Active	\N	\N	\N
164	39	PLATE_TECTONICS	true	\N	\N	\N
165	39	TECTONIC_PLATES	numeric	5	12	\N
166	39	VOLCANISM_TYPE	Cryovolcanic	\N	\N	\N
167	39	VOLCANIC_ACTIVITY	true	\N	\N	\N
168	39	VOLCANIC_INTENSITY	Continuous	\N	\N	\N
169	39	ACTIVE_VOLCANOES	numeric	50	300	\N
170	39	MOUNTAIN_COVERAGE	numeric	15	30	\N
171	39	MAX_ELEVATION	numeric	5	12	\N
172	39	TERRAIN_ROUGHNESS	numeric	5	8	\N
173	39	CRATERING_LEVEL	Pristine	\N	\N	\N
174	39	VISIBLE_CRATERS	numeric	10	100	\N
175	39	EROSION_LEVEL	Heavy	\N	\N	\N
176	39	EROSION_AGENT	Cryovolcanic	\N	\N	\N
\.



--
-- Data for Name: government_type; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.government_type (id, name, faction_type, description) FROM stdin;
1	Monarchy	Classic	Rule by a single hereditary sovereign whose authority is often legitimized by tradition or lineage.
2	Oligarchy	Classic	Power is concentrated among a small group of elites, often defined by wealth, status, or influence.
3	Democracy	Classic	Governance by the populace, where citizens vote directly or through elected representatives.
4	Republic	Classic	A representative system where leaders are elected to govern according to established laws.
5	Theocracy	Classic	Religious leaders govern according to divine doctrine or sacred law.
6	Dictatorship	Classic	Absolute power is held by a single ruler, typically enforced through coercion or control.
7	Autocracy	Classic	Centralized authority vested in one individual with minimal legal or institutional restraint.
8	Meritocracy	Classic	Leadership roles are assigned based on skill, achievement, or perceived competence.
9	Guildocracy	Classic	Trade guilds or professional organizations collectively control political power.
10	Military Junta	Classic	A ruling council of military leaders maintains order through force and discipline.
11	Galactic Council	Galactic	A multi-world representative body that governs through debate, diplomacy, and collective decision-making.
12	Feudal Space Empire	Galactic	A vast empire where planetary lords swear fealty to a central ruler in exchange for autonomy.
13	Planetary Confederacy	Galactic	A loose alliance of worlds united for mutual defense and economic cooperation.
14	Stellar Dominion	Galactic	An expansionist regime asserting control over star systems through authority and conquest.
15	Interstellar Federation	Galactic	A structured union of planets sharing power between local governments and a central authority.
16	Cosmic Assembly	Galactic	A ceremonial and legislative body representing diverse species and cultures across the galaxy.
17	Council of Elders	Galactic	Respected leaders chosen for wisdom and age guide policy across multiple worlds.
18	Corporate State	Galactic	Government is operated as a business, prioritizing profit, contracts, and shareholder interests.
19	Technocracy	Galactic	Scientists and engineers rule, valuing efficiency, data, and rational planning above all.
20	AI Governance	Galactic	An artificial intelligence administers law and policy based on predictive logic and optimization.
21	Trade Consortium	Galactic	Economic power blocs dominate governance, regulating commerce and interstellar trade routes.
22	Hive Mind	Exotic	A collective consciousness where individual identity is subsumed into a unified will.
23	Nomadic Tribal Council	Exotic	Mobile clans governed by elders who guide survival, migration, and tradition.
24	Sectocracy	Exotic	Rule by ideological or religious sects competing or cooperating for dominance.
25	Eldritch Rule	Exotic	Governance influenced or controlled by incomprehensible, otherworldly entities.
26	Chronocracy	Exotic	Leadership determined by control or mastery over time and temporal phenomena.
27	Psychic Oligarchy	Exotic	A small class of powerful psychics governs through mental dominance and foresight.
28	Starborn Assembly	Exotic	Celestial or ascended beings collectively rule, claiming cosmic legitimacy.
29	Arcane Order	Exotic	Mystics or sorcerers govern through esoteric knowledge and ritual authority.
30	Celestial Synod	Exotic	A sacred council interpreting cosmic signs to guide governance.
31	Void Council	Exotic	An enigmatic ruling body drawing power from the unknown depths of space.
32	Anarchy	Outlaw	No central authority exists; order is maintained through personal power or local agreements.
33	Rogue Council	Outlaw	An informal leadership group operating outside recognized law or authority.
34	Pirate Coalition	Outlaw	Independent pirate bands united for mutual profit and protection.
35	Freehold	Outlaw	A self-governing settlement claiming independence from external powers.
36	Shadow Syndicate	Outlaw	A covert criminal organization exerting influence through secrecy and control.
37	Underground Network	Outlaw	Decentralized cells coordinating resistance, smuggling, or espionage.
38	Resistance Front	Outlaw	An organized insurgency opposing an established regime.
39	Nomadic Clan	Outlaw	A roaming group bound by kinship and survival rather than formal law.
40	Insurgent Directorate	Outlaw	A centralized command structure directing revolutionary or guerrilla actions.
41	Fringe Assembly	Outlaw	A loose gathering of marginalized groups operating beyond mainstream society.
42	Corporate Technocracy	Hybrid	Corporate leaders and technical experts jointly govern for profit and efficiency.
43	Feudal Hive Empire	Hybrid	A hierarchical empire combining feudal loyalty with collective hive control.
44	Theocratic Galactic Council	Hybrid	Religious authority blended with interstellar representative governance.
45	Mercantile Oligarchy	Hybrid	Merchant elites dominate political power through economic leverage.
46	Military Corporate Junta	Hybrid	Corporate and military leaders rule together to enforce order and profitability.
47	Cybernetic Autocracy	Hybrid	A single ruler augmented by cybernetic systems wields absolute control.
48	Stellar Sectocracy	Hybrid	Religious sects govern star systems according to shared cosmic doctrine.
49	Pirate Confederation	Hybrid	Semi-independent pirate factions loosely united under common rules.
50	Rebel Commune	Hybrid	Collective self-rule formed from revolutionary or post-collapse communities.
51	Frontier Council	Hybrid	Local leaders govern remote territories through pragmatic cooperation.
52	Nomadic Federation	Hybrid	Mobile groups united under a shared charter while retaining independence.
\.



--
-- Data for Name: moon_type_ref; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.moon_type_ref (id, moon_type, description, typical_formation, min_mass_earth_masses, max_mass_earth_masses, typical_composition, created_at, modified_at, mass_distribution_priority) FROM stdin;
7	COLLISION_DEBRIS	Moon formed from planetary collision (e.g., Earth's Moon)	COLLISION_DEBRIS	0.001	0.02	ROCKY	2026-01-24 21:54:42.265611	2026-01-24 21:54:42.265611	0
1	REGULAR_LARGE	Large regular moon (e.g., Ganymede, Titan, Callisto)	CO_FORMED	0.01	0.025	MIXED	2026-01-24 21:54:42.265611	2026-01-24 21:54:42.265611	1
2	REGULAR_MEDIUM	Medium regular moon (e.g., Io, Europa, Triton)	CO_FORMED	0.001	0.01	MIXED	2026-01-24 21:54:42.265611	2026-01-24 21:54:42.265611	2
3	REGULAR_SMALL	Small regular moon (e.g., Mimas, Enceladus)	CO_FORMED	1e-05	0.001	ICY	2026-01-24 21:54:42.265611	2026-01-24 21:54:42.265611	3
5	SHEPHERD	Shepherd moon maintaining ring structure	CO_FORMED	1e-10	1e-05	ROCKY	2026-01-24 21:54:42.265611	2026-01-24 21:54:42.265611	4
6	TROJAN	Moon in planet's Lagrange points	CAPTURED	1e-07	0.0001	ROCKY	2026-01-24 21:54:42.265611	2026-01-24 21:54:42.265611	4
4	IRREGULAR_CAPTURED	Captured asteroid or comet	CAPTURED	1e-09	0.0001	ROCKY	2026-01-24 21:54:42.265611	2026-01-24 21:54:42.265611	5
\.



--
-- Data for Name: name_origin_mapping; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.name_origin_mapping (name_id, origin_id) FROM stdin;
1	1
2	1
3	1
4	1
5	1
6	1
7	1
8	1
9	1
10	1
11	2
12	2
13	2
16	2
17	2
18	2
20	2
14	3
15	3
19	3
21	1
22	1
23	1
24	1
25	1
26	2
26	5
27	2
27	5
28	2
28	5
29	2
29	5
31	2
31	5
30	3
30	6
32	7
33	7
34	7
35	8
36	8
37	8
38	8
39	8
40	8
41	8
42	8
43	8
44	8
45	8
46	8
47	8
48	8
49	8
50	8
51	8
52	8
53	8
54	8
55	13
55	15
56	13
56	15
57	13
57	15
58	13
58	15
59	13
59	15
60	13
60	15
61	13
61	15
62	13
62	15
63	13
63	15
64	13
64	15
65	13
65	15
66	13
66	15
67	13
67	15
68	13
68	15
69	13
69	15
70	13
70	15
71	13
71	15
72	13
72	15
73	13
73	15
74	13
74	15
75	10
75	11
76	10
76	11
77	10
77	11
78	10
78	11
79	10
79	11
80	10
80	11
81	10
81	11
82	10
82	11
83	10
83	11
84	10
84	11
85	10
85	11
86	10
86	11
87	10
87	11
88	10
88	11
89	10
89	11
90	10
90	11
91	10
91	11
92	10
92	11
93	16
94	16
95	16
96	16
97	16
98	16
99	16
100	16
101	16
102	16
103	16
104	16
105	16
106	16
107	16
108	16
109	16
110	16
111	16
112	16
113	17
114	17
115	17
116	17
117	17
118	17
119	17
120	17
121	17
122	17
123	17
124	17
125	17
126	17
127	17
128	17
129	17
130	17
131	20
132	20
133	20
134	20
135	20
136	20
137	20
138	20
139	20
140	20
141	20
142	20
143	20
144	20
145	20
146	20
147	20
148	20
149	20
150	20
151	21
152	21
153	21
154	21
155	21
156	21
157	21
158	21
159	21
160	21
161	21
162	21
163	21
164	21
165	21
166	21
167	21
168	21
169	9
169	28
170	9
170	28
171	9
171	28
172	9
172	28
173	9
173	28
174	9
174	28
175	9
175	28
176	9
176	28
177	9
177	28
178	9
178	28
179	9
179	28
180	9
180	28
181	28
182	28
183	28
184	28
185	9
186	9
187	9
188	9
189	25
190	25
191	25
192	25
193	25
194	25
195	25
196	25
197	25
198	25
199	25
201	25
202	25
203	25
204	25
205	25
206	25
207	25
208	25
209	26
209	27
210	26
210	27
211	26
211	27
212	26
212	27
213	26
213	27
214	26
214	27
215	26
215	27
216	26
216	27
217	26
217	27
218	26
218	27
219	26
219	27
220	26
220	27
221	26
222	26
223	26
224	26
225	27
226	27
227	27
228	27
229	19
230	19
231	19
232	19
233	19
234	19
235	19
236	19
237	19
238	19
239	19
240	19
241	19
242	19
243	19
244	19
245	18
246	18
247	18
248	18
249	18
250	18
251	18
252	18
254	18
255	18
256	18
257	18
258	18
259	18
260	18
261	24
262	24
263	24
264	24
265	24
266	24
268	24
270	24
271	24
272	24
273	24
274	24
275	24
276	24
61	24
267	24
269	24
277	23
278	23
279	23
280	23
281	23
282	23
283	23
284	23
285	23
286	23
287	23
288	23
289	23
290	23
291	23
292	23
293	22
294	22
295	22
296	22
297	22
298	22
299	22
300	22
301	22
302	22
303	22
304	22
305	22
306	22
307	22
308	22
309	12
310	12
311	12
312	12
313	12
314	12
315	12
316	12
317	12
318	12
319	12
320	12
321	12
322	12
323	12
324	12
325	29
326	29
327	29
328	29
329	29
330	29
331	29
332	29
333	29
334	29
335	29
336	29
337	29
338	29
339	29
340	29
\.



--
-- Data for Name: name_origin_ref; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.name_origin_ref (id, name, created_at, modified_at) FROM stdin;
1	French	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
2	West African	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
3	North African	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
4	Yoruba	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
5	Akan	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
6	Berber	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
7	Franco-African	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
8	English	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
9	Germanic	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
10	Slavic	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
11	Russian	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
12	Polish	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
13	Spanish	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
14	Portuguese	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
15	Latin American	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
16	Japanese	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
17	Chinese	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
18	Korean	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
19	Vietnamese	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
20	Indian	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
21	Arabic	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
22	Persian	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
23	Turkish	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
24	Greek	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
25	Italian	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
26	Irish	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
27	Scottish	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
28	Scandinavian	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
29	Dutch	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
\.



--
-- Data for Name: name_ref; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.name_ref (id, name, is_first, is_last, gender, popularity, created_at, modified_at) FROM stdin;
1	Jean	t	f	male	10	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
2	Luc	t	f	male	8	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
3	Pierre	t	f	male	9	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
4	Louis	t	f	male	10	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
5	Paul	t	f	male	8	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
6	Marie	t	f	female	10	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
7	Claire	t	f	female	7	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
8	Anne	t	f	female	6	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
9	Julien	t	f	male	7	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
10	Alex	t	f	unisex	6	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
11	Amadou	t	f	male	9	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
12	Kwame	t	f	male	8	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
13	Kofi	t	f	male	7	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
14	Ibrahim	t	f	male	9	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
15	Youssef	t	f	male	8	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
16	Amina	t	f	female	9	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
17	Fatou	t	f	female	8	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
18	Mariam	t	f	female	7	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
19	Sadiq	t	f	male	6	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
20	Nia	t	f	female	6	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
21	Dubois	f	t	unisex	10	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
22	Moreau	f	t	unisex	8	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
23	Lefevre	f	t	unisex	7	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
24	Martin	f	t	unisex	10	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
25	Bernard	f	t	unisex	8	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
26	Diallo	f	t	unisex	10	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
27	Mensah	f	t	unisex	9	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
28	Traore	f	t	unisex	8	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
29	Keita	f	t	unisex	9	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
30	Benali	f	t	unisex	7	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
31	Ouattara	f	t	unisex	8	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
32	Ismael	t	f	male	6	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
33	Malik	t	f	male	7	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
34	Nadia	t	f	female	7	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
35	James	t	f	male	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
36	William	t	f	male	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
37	Thomas	t	f	male	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
38	Edward	t	f	male	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
39	Henry	t	f	male	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
40	George	t	f	male	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
41	Elizabeth	t	f	female	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
42	Margaret	t	f	female	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
43	Catherine	t	f	female	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
44	Victoria	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
45	Charlotte	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
46	Oliver	t	f	male	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
47	Smith	f	t	unisex	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
48	Jones	f	t	unisex	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
49	Williams	f	t	unisex	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
50	Brown	f	t	unisex	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
51	Taylor	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
52	Davies	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
53	Wilson	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
54	Evans	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
55	Carlos	t	f	male	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
56	Miguel	t	f	male	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
57	Antonio	t	f	male	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
58	Jose	t	f	male	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
59	Diego	t	f	male	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
60	Rafael	t	f	male	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
61	Sofia	t	f	female	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
62	Isabella	t	f	female	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
63	Carmen	t	f	female	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
64	Elena	t	f	female	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
65	Lucia	t	f	female	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
66	Valentina	t	f	female	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
67	Garcia	f	t	unisex	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
68	Rodriguez	f	t	unisex	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
69	Martinez	f	t	unisex	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
70	Lopez	f	t	unisex	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
71	Hernandez	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
72	Gonzalez	f	t	unisex	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
73	Perez	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
74	Sanchez	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
75	Dmitri	t	f	male	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
76	Ivan	t	f	male	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
77	Alexei	t	f	male	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
78	Nikolai	t	f	male	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
79	Sergei	t	f	male	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
80	Viktor	t	f	male	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
81	Anastasia	t	f	female	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
82	Natasha	t	f	female	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
83	Katya	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
84	Olga	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
85	Irina	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
86	Svetlana	t	f	female	6	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
87	Volkov	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
88	Petrov	f	t	unisex	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
89	Ivanov	f	t	unisex	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
90	Sokolov	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
91	Kuznetsov	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
92	Popov	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
93	Hiroshi	t	f	male	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
94	Kenji	t	f	male	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
95	Takeshi	t	f	male	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
96	Yuki	t	f	unisex	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
97	Akira	t	f	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
98	Ren	t	f	male	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
99	Sakura	t	f	female	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
100	Yumi	t	f	female	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
101	Hana	t	f	female	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
102	Aiko	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
103	Mei	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
104	Haruki	t	f	male	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
105	Tanaka	f	t	unisex	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
106	Yamamoto	f	t	unisex	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
107	Watanabe	f	t	unisex	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
108	Suzuki	f	t	unisex	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
109	Takahashi	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
110	Nakamura	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
111	Kobayashi	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
112	Sato	f	t	unisex	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
113	Wei	t	f	unisex	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
114	Jun	t	f	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
115	Ming	t	f	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
116	Jian	t	f	male	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
117	Lei	t	f	male	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
118	Fang	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
119	Xiu	t	f	female	6	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
120	Lan	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
121	Hong	t	f	unisex	6	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
122	Chao	t	f	male	6	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
123	Wang	f	t	unisex	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
124	Li	f	t	unisex	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
125	Zhang	f	t	unisex	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
126	Liu	f	t	unisex	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
127	Chen	f	t	unisex	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
128	Yang	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
129	Huang	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
130	Wu	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
131	Raj	t	f	male	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
132	Vikram	t	f	male	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
133	Arjun	t	f	male	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
134	Sanjay	t	f	male	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
135	Anil	t	f	male	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
136	Deepak	t	f	male	6	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
137	Priya	t	f	female	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
138	Anita	t	f	female	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
139	Sunita	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
140	Kavita	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
141	Neha	t	f	female	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
142	Pooja	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
143	Patel	f	t	unisex	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
144	Sharma	f	t	unisex	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
145	Singh	f	t	unisex	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
146	Kumar	f	t	unisex	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
147	Gupta	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
148	Reddy	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
149	Rao	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
150	Verma	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
151	Mohammed	t	f	male	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
152	Ahmed	t	f	male	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
153	Hassan	t	f	male	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
154	Omar	t	f	male	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
155	Khalid	t	f	male	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
156	Tariq	t	f	male	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
157	Fatima	t	f	female	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
158	Aisha	t	f	female	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
159	Layla	t	f	female	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
160	Zahra	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
161	Noor	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
162	Sara	t	f	female	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
163	Al-Rashid	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
164	Al-Hassan	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
165	Al-Farsi	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
166	Mansour	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
167	Nasser	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
168	Bakir	f	t	unisex	6	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
169	Erik	t	f	male	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
170	Lars	t	f	male	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
171	Karl	t	f	male	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
172	Hans	t	f	male	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
173	Bjorn	t	f	male	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
174	Gunnar	t	f	male	6	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
175	Ingrid	t	f	female	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
176	Freya	t	f	female	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
177	Astrid	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
178	Helga	t	f	female	6	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
179	Sigrid	t	f	female	6	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
180	Greta	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
181	Lindqvist	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
182	Eriksson	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
183	Johansson	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
184	Larsson	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
185	Mueller	f	t	unisex	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
186	Schmidt	f	t	unisex	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
187	Weber	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
188	Fischer	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
189	Marco	t	f	male	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
190	Giuseppe	t	f	male	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
191	Lorenzo	t	f	male	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
192	Matteo	t	f	male	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
193	Luca	t	f	male	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
194	Giovanni	t	f	male	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
195	Giulia	t	f	female	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
196	Francesca	t	f	female	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
197	Chiara	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
198	Alessia	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
199	Beatrice	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
200	Valentina	t	f	female	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
201	Rossi	f	t	unisex	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
202	Russo	f	t	unisex	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
203	Ferrari	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
204	Esposito	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
205	Bianchi	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
206	Romano	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
207	Colombo	f	t	unisex	6	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
208	Ricci	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
209	Sean	t	f	male	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
210	Connor	t	f	male	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
211	Liam	t	f	male	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
212	Declan	t	f	male	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
213	Finn	t	f	male	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
214	Angus	t	f	male	6	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
215	Siobhan	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
216	Maeve	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
217	Aoife	t	f	female	6	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
218	Fiona	t	f	female	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
219	Moira	t	f	female	6	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
220	Bridget	t	f	female	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
221	Murphy	f	t	unisex	10	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
222	Kelly	f	t	unisex	9	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
223	Sullivan	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
224	Walsh	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
225	MacLeod	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
226	Campbell	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
227	MacDonald	f	t	unisex	8	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
228	Stewart	f	t	unisex	7	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
229	Minh	t	f	male	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
230	Duc	t	f	male	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
231	Tuan	t	f	male	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
232	Hung	t	f	male	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
233	Thanh	t	f	unisex	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
234	Linh	t	f	female	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
235	Huong	t	f	female	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
236	Thao	t	f	female	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
237	Mai	t	f	female	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
238	Ngoc	t	f	female	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
239	Nguyen	f	t	unisex	10	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
240	Tran	f	t	unisex	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
241	Le	f	t	unisex	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
242	Pham	f	t	unisex	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
243	Hoang	f	t	unisex	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
244	Vu	f	t	unisex	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
245	Joon	t	f	male	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
246	Min-jun	t	f	male	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
247	Seo-jun	t	f	male	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
248	Ji-hoon	t	f	male	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
249	Hyun	t	f	unisex	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
250	Ji-yeon	t	f	female	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
251	Soo-min	t	f	female	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
252	Yuna	t	f	female	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
253	Hana	t	f	female	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
254	Eunji	t	f	female	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
255	Kim	f	t	unisex	10	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
256	Park	f	t	unisex	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
257	Lee	f	t	unisex	10	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
258	Choi	f	t	unisex	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
259	Jung	f	t	unisex	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
260	Kang	f	t	unisex	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
261	Nikolaos	t	f	male	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
262	Dimitris	t	f	male	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
263	Kostas	t	f	male	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
264	Yannis	t	f	male	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
265	Stavros	t	f	male	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
266	Eleni	t	f	female	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
267	Maria	t	f	female	10	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
268	Katerina	t	f	female	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
269	Sofia	t	f	female	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
270	Athena	t	f	female	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
271	Papadopoulos	f	t	unisex	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
272	Nikolaidis	f	t	unisex	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
273	Georgiou	f	t	unisex	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
274	Konstantinos	f	t	unisex	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
275	Dimitriou	f	t	unisex	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
276	Alexopoulos	f	t	unisex	6	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
277	Mehmet	t	f	male	10	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
278	Mustafa	t	f	male	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
279	Ahmet	t	f	male	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
280	Ali	t	f	male	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
281	Emre	t	f	male	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
282	Ayse	t	f	female	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
283	Fatma	t	f	female	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
284	Emine	t	f	female	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
285	Zeynep	t	f	female	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
286	Elif	t	f	female	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
287	Yilmaz	f	t	unisex	10	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
288	Kaya	f	t	unisex	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
289	Demir	f	t	unisex	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
290	Celik	f	t	unisex	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
291	Sahin	f	t	unisex	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
292	Ozturk	f	t	unisex	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
293	Darius	t	f	male	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
294	Cyrus	t	f	male	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
295	Reza	t	f	male	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
296	Amir	t	f	male	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
297	Farhad	t	f	male	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
298	Shirin	t	f	female	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
299	Leila	t	f	female	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
300	Nazanin	t	f	female	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
301	Parisa	t	f	female	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
302	Yasmin	t	f	female	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
303	Tehrani	f	t	unisex	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
304	Shirazi	f	t	unisex	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
305	Isfahani	f	t	unisex	6	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
306	Bakhtiari	f	t	unisex	6	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
307	Rahimi	f	t	unisex	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
308	Hosseini	f	t	unisex	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
309	Jakub	t	f	male	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
310	Mateusz	t	f	male	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
311	Kacper	t	f	male	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
312	Piotr	t	f	male	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
313	Tomasz	t	f	male	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
314	Anna	t	f	female	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
315	Zofia	t	f	female	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
316	Maja	t	f	female	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
317	Kasia	t	f	female	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
318	Agnieszka	t	f	female	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
319	Kowalski	f	t	unisex	10	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
320	Nowak	f	t	unisex	10	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
321	Wisniewski	f	t	unisex	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
322	Wojcik	f	t	unisex	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
323	Kowalczyk	f	t	unisex	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
324	Kaminski	f	t	unisex	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
325	Jan	t	f	male	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
326	Pieter	t	f	male	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
327	Willem	t	f	male	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
328	Hendrik	t	f	male	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
329	Joost	t	f	male	6	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
330	Emma	t	f	female	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
331	Sophie	t	f	female	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
332	Julia	t	f	female	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
333	Lotte	t	f	female	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
334	Fleur	t	f	female	6	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
335	De Vries	f	t	unisex	10	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
336	Van Dijk	f	t	unisex	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
337	Bakker	f	t	unisex	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
338	Jansen	f	t	unisex	9	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
339	Visser	f	t	unisex	7	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
340	Smit	f	t	unisex	8	2026-02-04 15:17:02.220299	2026-02-04 15:17:02.220299
\.



--
-- Data for Name: name_region_ref; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.name_region_ref (id, name, created_at, modified_at) FROM stdin;
1	Europe	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
2	West Africa	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
3	North Africa	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
4	Central Africa	2026-02-04 15:10:30.819555	2026-02-04 15:10:30.819555
5	East Asia	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
6	South Asia	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
7	Southeast Asia	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
8	Middle East	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
9	Latin America	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
10	Eastern Europe	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
11	Northern Europe	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
12	Southern Europe	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
13	British Isles	2026-02-04 15:10:30.945398	2026-02-04 15:10:30.945398
\.



--
-- Data for Name: origin_region_mapping; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.origin_region_mapping (origin_id, region_id) FROM stdin;
1	1
2	2
3	3
4	2
5	2
6	3
8	13
9	11
10	10
11	10
12	10
13	9
13	12
14	9
14	12
15	9
16	5
17	5
18	5
19	7
20	6
21	8
22	8
23	8
24	12
25	12
26	13
27	13
28	11
29	11
\.



--
-- Data for Name: planet_atmosphere_compatibility; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.planet_atmosphere_compatibility (id, planet_type, atmosphere_classification, preference_weight, notes, created_at) FROM stdin;
1	Hot Rocky Planet	VOLCANIC	150	Primary atmosphere type for hot rocky worlds	2026-01-23 19:16:19.751601
2	Hot Rocky Planet	EXOTIC	100	For extremely hot variants	2026-01-23 19:16:19.751601
3	Hot Rocky Planet	NONE	80	Atmosphere blown away by stellar wind	2026-01-23 19:16:19.751601
4	Terrestrial Planet	EARTH_LIKE	200	Ideal habitable atmosphere	2026-01-23 19:16:19.751601
5	Terrestrial Planet	VENUS_LIKE	100	Runaway greenhouse variant	2026-01-23 19:16:19.751601
6	Terrestrial Planet	MARS_LIKE	120	Thin atmosphere variant	2026-01-23 19:16:19.751601
7	Terrestrial Planet	VOLCANIC	80	Geologically active variant	2026-01-23 19:16:19.751601
8	Terrestrial Planet	NONE	30	Rare airless terrestrial	2026-01-23 19:16:19.751601
9	Desert Planet	MARS_LIKE	200	Primary atmosphere - thin CO2	2026-01-23 19:16:19.751601
10	Desert Planet	VENUS_LIKE	100	Thick CO2 hot desert	2026-01-23 19:16:19.751601
11	Desert Planet	VOLCANIC	80	Active desert world	2026-01-23 19:16:19.751601
12	Desert Planet	NONE	50	Airless desert	2026-01-23 19:16:19.751601
13	Ocean Planet	EARTH_LIKE	200	Water world with breathable air	2026-01-23 19:16:19.751601
14	Ocean Planet	VENUS_LIKE	80	Hot water world with thick CO2	2026-01-23 19:16:19.751601
15	Ocean Planet	TITAN_LIKE	100	Cold ocean with thick N2	2026-01-23 19:16:19.751601
16	Super-Earth	EARTH_LIKE	150	Thick breathable atmosphere	2026-01-23 19:16:19.751601
17	Super-Earth	VENUS_LIKE	120	Super-Venus variant	2026-01-23 19:16:19.751601
18	Super-Earth	VOLCANIC	100	Geologically hyperactive	2026-01-23 19:16:19.751601
19	Super-Earth	AMMONIA	80	Cold super-earth variant	2026-01-23 19:16:19.751601
20	Lava Planet	VOLCANIC	200	Primary atmosphere - vaporized rock	2026-01-23 19:16:19.751601
21	Lava Planet	EXOTIC	150	Metallic vapors from extreme heat	2026-01-23 19:16:19.751601
22	Lava Planet	NONE	20	Rare airless lava world	2026-01-23 19:16:19.751601
23	Mini-Neptune	TITAN_LIKE	150	Thick nitrogen-methane	2026-01-23 19:16:19.751601
24	Mini-Neptune	AMMONIA	120	Ammonia-rich variant	2026-01-23 19:16:19.751601
25	Mini-Neptune	REDUCING	100	Hydrogen-rich primitive	2026-01-23 19:16:19.751601
26	Mini-Neptune	ICE_GIANT	130	Ice giant composition	2026-01-23 19:16:19.751601
27	Sub-Neptune	ICE_GIANT	200	Primary type	2026-01-23 19:16:19.751601
28	Sub-Neptune	TITAN_LIKE	120	Nitrogen-methane variant	2026-01-23 19:16:19.751601
29	Sub-Neptune	AMMONIA	100	Ammonia-dominated	2026-01-23 19:16:19.751601
30	Hot Jupiter	JOVIAN	200	Standard hot jupiter atmosphere	2026-01-23 19:16:19.751601
31	Hot Jupiter	EXOTIC	80	Extremely hot variant with metallic vapors	2026-01-23 19:16:19.751601
32	Gas Giant	JOVIAN	200	Standard jovian atmosphere	2026-01-23 19:16:19.751601
33	Super-Jupiter	JOVIAN	200	Massive jovian atmosphere	2026-01-23 19:16:19.751601
34	Ice Giant	ICE_GIANT	200	Primary ice giant atmosphere	2026-01-23 19:16:19.751601
35	Ice Giant	TITAN_LIKE	80	Methane-nitrogen variant	2026-01-23 19:16:19.751601
38	Carbon Planet	MARS_LIKE	120	CO/CO2 atmosphere	2026-01-23 19:16:19.751601
39	Carbon Planet	VOLCANIC	100	Carbon-rich volcanic gases	2026-01-23 19:16:19.751601
40	Carbon Planet	CORROSIVE	80	Exotic carbon chemistry	2026-01-23 19:16:19.751601
41	Carbon Planet	NONE	60	Airless carbon world	2026-01-23 19:16:19.751601
42	Iron Planet	VOLCANIC	150	Metallic vapors from heat	2026-01-23 19:16:19.751601
43	Iron Planet	EXOTIC	120	Extreme metallic atmosphere	2026-01-23 19:16:19.751601
44	Iron Planet	NONE	100	Airless stripped core	2026-01-23 19:16:19.751601
45	Ice World	TITAN_LIKE	150	Nitrogen-methane atmosphere	2026-01-23 19:16:19.751601
46	Ice World	AMMONIA	100	Ammonia atmosphere	2026-01-23 19:16:19.751601
47	Ice World	NONE	120	No atmosphere retained	2026-01-23 19:16:19.751601
48	Dwarf Planet	NONE	200	Too small to retain atmosphere	2026-01-23 19:16:19.751601
49	Dwarf Planet	TITAN_LIKE	30	Trace atmosphere only	2026-01-23 19:16:19.751601
50	Rogue Planet	NONE	180	Frozen, no atmosphere	2026-01-23 19:16:19.751601
51	Rogue Planet	REDUCING	50	Primordial frozen atmosphere	2026-01-23 19:16:19.751601
53	Icy Moon	TITAN_LIKE	20	Rare trace nitrogen possible	2026-01-27 18:27:47.170564
55	Icy Moon Large	AMMONIA	80	Ammonia possible in cold conditions	2026-01-27 18:27:47.170564
58	Cryovolcanic Moon	AMMONIA	100	Ammonia from subsurface ocean	2026-01-27 18:27:47.170564
65	Rocky Moon Large	VOLCANIC	20	Very rare - only if moon happens to be geologically active despite large size	2026-01-27 18:27:47.170564
57	Cryovolcanic Moon	TITAN_LIKE	220	Thick N2/CH4 from cryovolcanic activity	2026-01-27 18:27:47.170564
54	Icy Moon Large	TITAN_LIKE	180	Can retain nitrogen atmosphere like Titan	2026-01-27 18:27:47.170564
63	Rocky Moon	MARS_LIKE	80	Extremely rare trace CO2	2026-01-27 18:27:47.170564
66	Rocky Moon Large	NONE	100	Most have lost atmosphere	2026-01-27 18:27:47.170564
60	Volcanic Moon	VOLCANIC	250	SO2 from active volcanism	2026-01-27 18:27:47.170564
62	Rocky Moon	NONE	250	Too small and geologically dead	2026-01-27 18:27:47.170564
52	Icy Moon	NONE	20	Most small icy moons cannot retain atmosphere	2026-01-27 18:27:47.170564
56	Icy Moon Large	NONE	20	May have lost atmosphere	2026-01-27 18:27:47.170564
59	Cryovolcanic Moon	NONE	20	Atmosphere too thin to detect	2026-01-27 18:27:47.170564
61	Volcanic Moon	NONE	20	Atmosphere collapses between eruptions	2026-01-27 18:27:47.170564
67	Captured Body	NONE	150	Far too small for any atmosphere	2026-01-27 18:27:47.170564
64	Rocky Moon Large	MARS_LIKE	180	Can retain thin CO2 atmosphere	2026-01-27 18:27:47.170564
79	Hot Neptune	REDUCING	180	Residual hydrogen envelope	2026-02-10 20:06:36.756907
80	Hot Neptune	EXOTIC	150	Metallic vapors and photodissociated species	2026-02-10 20:06:36.756907
81	Hot Neptune	VOLCANIC	100	Outgassed from exposed rocky core	2026-02-10 20:06:36.756907
82	Hot Neptune	NONE	60	Fully stripped by photoevaporation	2026-02-10 20:06:36.756907
86	Rocky Moon Large	EARTH_LIKE	15	Very rare - massive rocky moon with sustained geological outgassing in HZ	2026-02-13 13:13:38.952315
87	Icy Moon Large	EARTH_LIKE	8	Extremely rare - requires HZ location, large mass, and atmospheric evolution	2026-02-13 13:13:38.952315
88	Icy Moon Large	REDUCING	25	Primordial H2-rich atmosphere retained by large icy moon	2026-02-13 13:13:38.952315
89	Cryovolcanic Moon	REDUCING	30	H2-rich outgassing from subsurface ocean hydrothermal activity	2026-02-13 13:13:38.952315
90	Puffy Planet	EXOTIC	60	Extreme heat variant with metallic vapors at highest temperatures	2026-02-15 12:27:43.821995
36	Puffy Planet	JOVIAN	200	Inflated gas giant	2026-01-23 19:16:19.751601
74	Warm Neptune	ICE_GIANT	200	Retained ice giant atmosphere	2026-02-10 20:06:36.756907
75	Warm Neptune	AMMONIA	160	Ammonia-rich at cooler end	2026-02-10 20:06:36.756907
76	Warm Neptune	TITAN_LIKE	130	Nitrogen-methane dominated	2026-02-10 20:06:36.756907
\.



--
-- Data for Name: planet_type_ref; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.planet_type_ref (id, name, description, min_mass_earth, max_mass_earth, min_radius_earth, max_radius_earth, typical_density_g_cm3, typical_albedo, typical_core_type, formation_zone, can_have_atmosphere, typical_atmosphere, can_have_rings, ring_probability, min_moons, max_moons, habitable, rarity_weight, min_formation_temp_k, max_formation_temp_k, min_moon_system_mass_ratio, max_moon_system_mass_ratio) FROM stdin;
1	Hot Rocky Planet	Small rocky planet very close to its star, extremely hot with little to no atmosphere	0.05	0.4	0.3	0.6	5.4	0.12	Iron-Nickel	inner	f	None	f	0	0	0	f	150	600	1200	\N	\N
10	Gas Giant	Large planet composed primarily of hydrogen and helium	100	400	9	12	1.3	0.5	Diffuse Gas-Metallic H	outer	t	H2, He, CH4, NH3	t	0.4	4	15	f	250	80	400	5e-05	0.0003
11	Super-Jupiter	Extremely massive gas giant	400	3000	12	20	1.8	0.45	Metallic Hydrogen	outer	t	H2, He, exotic compounds	t	0.5	5	20	f	60	50	600	\N	\N
12	Ice Giant	Planet composed of water, methane, and ammonia ices with small rocky core	10	25	3.5	5	1.6	0.6	Ice-Rock	outer	t	H2, He, CH4, NH3, H2O	t	0.4	2	8	f	220	40	100	8e-05	0.0005
8	Sub-Neptune	Neptune-sized planet with substantial atmosphere	10	20	2.5	4	1.8	0.55	Ice-Rock	outer	t	H2, He, CH4, H2O	t	0.3	1	6	f	220	40	200	8e-05	0.0005
16	Ice World	Frozen planet beyond the frost line, covered in water and methane ice	0.1	5	0.5	2	2	0.7	Ice-Rock	outer	t	N2, CH4, CO	f	0	0	3	f	100	10	150	5e-05	0.001
4	Desert Planet	Rocky world with minimal water, thin atmosphere	0.4	1.5	0.6	1.2	5.2	0.25	Iron-Nickel	inner	t	CO2, N2, trace H2O	f	0	0	2	f	120	250	500	5e-05	0.002
6	Lava Planet	Extremely hot world with molten surface due to tidal heating or proximity to star	0.5	2	0.7	1.2	5.5	0.1	Molten Iron	inner	t	Vaporized rock	f	0	0	0	f	80	1200	3000	1e-05	0.0001
7	Mini-Neptune	Small gas planet with thick hydrogen-helium atmosphere over rocky core	2	10	1.5	3	2.5	0.5	Rocky-Ice	outer	t	H2, He, CH4, NH3	t	0.2	0	3	f	90	40	150	\N	\N
15	Iron Planet	Stripped rocky core, primarily metallic composition	0.1	1	0.4	0.8	8	0.1	Pure Iron	inner	f	None	f	0	0	0	f	50	500	1500	1e-05	0.0001
2	Terrestrial Planet	Rocky planet with potential for atmosphere and liquid water	0.5	2	0.7	1.3	5.5	0.3	Iron-Nickel	habitable	t	N2, O2, trace gases	f	0	0	2	t	250	200	350	0.0001	0.015
3	Super-Earth	Large rocky planet with high gravity, can retain thick atmosphere	2	10	1.3	2	5	0.35	Iron-Silicate	habitable	t	N2, CO2, H2O vapor	f	0	0	3	t	350	150	500	0.0001	0.01
9	Hot Jupiter	Massive gas giant orbiting very close to its star	50	500	8	15	0.7	0.15	Diffuse Gas	inner	t	H2, He, hot metals	t	0.1	0	1	f	100	1000	2500	5e-05	0.0003
14	Carbon Planet	Rocky planet rich in carbon compounds, graphite, and possibly diamonds	0.5	5	0.7	1.8	6	0.2	Carbon-Iron	inner	t	CO, CO2, CH4	f	0	0	2	f	60	200	600	2e-05	0.0008
13	Puffy Planet	Low-density gas giant with inflated atmosphere due to stellar heating	50	300	10	18	0.4	0.2	Diffuse Gas	inner	t	H2, He, hot volatiles	t	0.15	0	1	f	70	800	1800	\N	\N
5	Ocean Planet	Water-rich world with deep global oceans	0.8	3	0.9	1.5	4	0.4	Silicate-Water	habitable	t	H2O vapor, N2, O2	f	0	0	2	t	180	273	373	0.0001	0.01
39	Hot Neptune	Highly irradiated Neptune-class planet with inflated, evaporating atmosphere	5	25	3	6	1.2	0.15	Rocky-Ice	inner	t	H2, He, metallic vapors	f	0	0	1	f	40	400	1200	1e-05	0.0001
17	Dwarf Planet	Small planetary body, often icy, in outer system	0.001	0.1	0.1	0.5	2.5	0.6	Ice-Rock	outer	t	N2, CH4, CO (trace)	f	0	0	2	f	60	10	150	5e-05	0.001
38	Warm Neptune	Moderately irradiated Neptune-class planet with partially depleted volatile envelope	2	20	2	5	2	0.4	Rocky-Ice	habitable	t	H2, He, H2O, CH4	f	0	0	3	f	180	150	400	5e-05	0.001
\.



--
-- Data for Name: precipitation_template; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.precipitation_template (id, cloud_substance, surface_temp_min_k, surface_temp_max_k, phase, reaches_surface, description, created_at) FROM stdin;
1	H2O	273	500	RAIN	t	Liquid water rain	2026-02-12 22:38:07.086864
2	H2O	200	273	SNOW	t	Water ice snow and frost	2026-02-12 22:38:07.086864
3	H2O	250	320	HAIL	t	Ice pellets from strong convective updrafts	2026-02-12 22:38:07.086864
4	H2O_ICE	150	250	SNOW	t	High-altitude ice crystal precipitation	2026-02-12 22:38:07.086864
5	CH4	80	95	RAIN	t	Liquid methane rain — Titan-like	2026-02-12 22:38:07.086864
6	CH4	60	80	SNOW	t	Methane ice crystal precipitation	2026-02-12 22:38:07.086864
7	NH3	195	240	RAIN	t	Liquid ammonia rain	2026-02-12 22:38:07.086864
8	NH3	130	195	SNOW	t	Ammonia ice crystal snow	2026-02-12 22:38:07.086864
9	H2SO4	300	500	VIRGA	f	Sulfuric acid rain that evaporates before reaching surface	2026-02-12 22:38:07.086864
10	H2SO4	200	300	DRIZZLE	t	Dilute sulfuric acid drizzle at lower temperatures	2026-02-12 22:38:07.086864
11	CO2_ICE	130	200	SNOW	t	Dry ice snowfall — CO2 condensation	2026-02-12 22:38:07.086864
12	SO2	200	400	RAIN	t	Sulfur dioxide condensation rain	2026-02-12 22:38:07.086864
13	IRON	1500	2500	RAIN	t	Molten iron rain on ultrahot worlds	2026-02-12 22:38:07.086864
14	SILICATE	1800	3000	RAIN	t	Liquid rock rain on ultrahot gas giants	2026-02-12 22:38:07.086864
15	HYDROCARBON_HAZE	70	200	DRIZZLE	t	Hydrocarbon tar drizzle from photochemical haze	2026-02-12 22:38:07.086864
\.



--
-- Data for Name: ring_template; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.ring_template (id, name, description, ring_type, planet_types, min_planet_mass_earth, max_planet_mass_earth, min_distance_au, max_distance_au, inner_radius_min_planet_radii, inner_radius_max_planet_radii, outer_radius_min_planet_radii, outer_radius_max_planet_radii, thickness_min_km, thickness_max_km, optical_depth_min, optical_depth_max, particle_size_min_m, particle_size_max_m, composition_type, composition_description, albedo_min, albedo_max, color, visibility, has_gaps_probability, has_shepherd_moons_probability, stability, origin_type, rarity_weight, created_at) FROM stdin;
1	Saturn-Type Main Rings	Prominent, icy main ring system with multiple bands and divisions	MAIN	Gas Giant, Ice Giant, Super-Jupiter	15	\N	2	15	1.2	1.5	2	2.8	10	100	0.4	1.2	0.01	10	ICY	Water Ice 93%, Silicates 5%, Organics 2%	0.5	0.8	White to pale yellow	PROMINENT	0.7	0.8	STABLE	PRIMORDIAL	100	2026-01-31 16:22:10.928996
2	Narrow Dark Rings	Thin, dark rings composed of rocky material	NARROW	Ice Giant, Gas Giant, Super-Jupiter	10	\N	5	30	1.5	2	2	3	0.1	5	0.1	0.5	0.001	1	ROCKY	Carbonaceous Material 60%, Silicates 30%, Water Ice 10%	0.02	0.08	Charcoal gray to black	FAINT	0.3	0.9	STABLE	MOON_DISRUPTION	60	2026-01-31 16:22:10.928996
3	Gossamer Rings	Faint, diffuse rings fed by dust from inner moons	GOSSAMER	Gas Giant, Super-Jupiter	30	\N	1	10	1.1	1.3	2.5	4	1000	12000	1e-05	0.0001	1e-06	0.001	MIXED	Dust 70%, Water Ice 20%, Silicates 10%	0.05	0.15	Reddish-brown	BARELY_VISIBLE	0.1	0.3	SLOWLY_DISSIPATING	MOON_DEBRIS	40	2026-01-31 16:22:10.928996
4	Young System Debris Ring	Temporary ring from recent collision or disruption event	DIFFUSE	Gas Giant, Ice Giant, Super Earth, Super-Jupiter	5	\N	0.5	20	1.3	1.8	1.8	2.5	20	200	0.05	0.3	0.001	5	MIXED	Rock 40%, Ice 35%, Dust 25%	0.2	0.4	Gray to tan	MODERATE	0.5	0.4	UNSTABLE	MOON_COLLISION	30	2026-01-31 16:22:10.928996
\.



--
-- Data for Name: star_type; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.star_type (id, name, spectral_class, min_mass, max_mass, mass_radius_exponent, radius_multiplier_min, radius_multiplier_max, description, rarity_weight, type, min_planet_formation_au, max_planet_formation_au) FROM stdin;
1	Main Sequence O	O	16	90	0.5	\N	\N	Hottest, bluest main sequence stars	1	\N	1	200
2	Main Sequence B	B	2.1	16	0.5	\N	\N	Hot, blue-white stars	10	\N	0.5	150
3	Main Sequence A	A	1.4	2.1	0.57	\N	\N	White stars like Sirius	60	\N	0.3	100
4	Main Sequence F	F	1.04	1.4	0.57	\N	\N	Yellow-white stars	300	\N	0.15	80
5	Main Sequence G	G	0.8	1.04	0.57	\N	\N	Sun-like yellow stars	760	\N	0.1	60
6	Main Sequence K	K	0.45	0.8	0.8	\N	\N	Orange dwarf stars	1210	\N	0.05	40
7	Main Sequence M	M	0.08	0.45	0.8	\N	\N	Red dwarf stars (M-class), most common stellar type	7300	\N	0.01	20
15	Brown Dwarf L	L	0.05	0.08	0.08	\N	\N	Warm brown dwarf with lithium and metal hydrides in atmosphere	600	\N	0.005	10
16	Brown Dwarf T	T	0.02	0.05	0.08	\N	\N	Cool methane brown dwarf with characteristic blue tinge	500	\N	0.003	5
17	Brown Dwarf Y	Y	0.013	0.02	0.08	\N	\N	Coldest brown dwarfs with ammonia clouds, near planetary temperatures	400	\N	0.002	3
11	T Tauri	\N	0.5	3	0.8	2	5	Young pre-main sequence stars	30	\N	0.05	50
10	Proto	\N	0.1	3	\N	2	10	Protostars still forming	20	\N	0.02	20
12	White Dwarf	\N	0.17	1.4	-0.333	\N	\N	Dense stellar remnant, Earth-sized	50	\N	0.5	30
8	Red Giant	\N	0.3	8	0.57	15	60	Expanded late-stage stars	5	\N	2	100
9	Super Giant	\N	10	70	0.5	50	200	Enormous, short-lived stars	1	\N	5	200
13	Neutron Star	\N	1.4	2.16	\N	1e-05	3e-05	Incredibly dense collapsed core	2	\N	0.2	20
\.



--
-- Data for Name: terrain_category_ref; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.terrain_category_ref (id, category, display_name, description, base_weight, typical_min_coverage, typical_max_coverage, is_major_terrain, is_rare, created_at) FROM stdin;
1	PLAINS	Plains and Lowlands	Flat or gently rolling terrain - common on most worlds	200	10	60	t	f	2026-01-20 23:43:06.257811
2	MOUNTAIN	Mountains and Highlands	Elevated terrain and mountain ranges - common on geologically active worlds	150	5	40	t	f	2026-01-20 23:43:06.257811
3	AQUATIC	Aquatic Features	Oceans, lakes, and water bodies - depends on water coverage	180	0	80	t	f	2026-01-20 23:43:06.257811
4	ICE	Ice and Frozen	Glaciers, ice sheets, and frozen terrain - common on cold worlds	140	5	80	t	f	2026-01-20 23:43:06.257811
5	ARID	Arid and Desert	Deserts and dry regions - common on low-water worlds	130	10	70	t	f	2026-01-20 23:43:06.257811
6	TEMPERATE	Temperate Biomes	Forests, grasslands, and moderate climates - requires specific conditions	120	10	60	t	f	2026-01-20 23:43:06.257811
7	VOLCANIC	Volcanic Features	Lava flows, volcanic fields - appears on geologically active worlds	80	1	30	f	f	2026-01-20 23:43:06.257811
8	EXOTIC	Exotic Features	Unusual geological features - rare and unique	40	0.5	20	f	t	2026-01-20 23:43:06.257811
9	ARTIFICIAL	Artificial Features	Signs of civilization - extremely rare	5	0.1	5	f	t	2026-01-20 23:43:06.257811
10	BARREN	Barren Terrain	Lifeless rocky/icy terrain for cold or airless worlds	180	20	100	t	f	2026-01-21 11:13:05.202933
\.



--
-- Data for Name: terrain_type_ref; Type: TABLE DATA; Schema: ref; Owner: starcreator_dev
--

COPY ref.terrain_type_ref (id, name, display_name, description, category, requires_liquid, requires_atmosphere, min_temperature_k, max_temperature_k, min_pressure_atm, is_volcanic, is_frozen, is_aquatic, is_artificial, rarity_weight, typical_coverage_min, typical_coverage_max, created_at, cratering_weight_boost, volcanic_weight_boost, excluded_planet_types, required_composition_classes, excluded_composition_classes) FROM stdin;
13	TAIGA	Taiga/Boreal Forest	Northern coniferous forests	TEMPERATE	f	t	250	290	\N	f	f	f	f	90	5	25	2026-01-20 17:36:22.573303	0	0	\N	\N	\N
14	FOREST_TEMPERATE	Temperate Forest	Deciduous and mixed forests	TEMPERATE	t	t	270	300	\N	f	f	f	f	120	5	40	2026-01-20 17:36:22.573303	0	0	\N	\N	\N
15	FOREST_TROPICAL	Tropical Rainforest	Dense tropical and subtropical forests	TEMPERATE	t	t	290	320	\N	f	f	f	f	100	5	35	2026-01-20 17:36:22.573303	0	0	\N	\N	\N
17	SHRUBLAND	Shrubland	Mediterranean scrub and chaparral	TEMPERATE	f	t	280	310	\N	f	f	f	f	100	5	30	2026-01-20 17:36:22.573303	0	0	\N	\N	\N
21	DESERT_ICE	Polar Desert	Dry polar regions with minimal precipitation	ARID	f	t	200	270	\N	f	f	f	f	80	5	40	2026-01-20 17:36:22.573303	0	0	\N	\N	\N
42	ARTIFICIAL_TERRAFORMED	Terraformed	Artificially modified terrain	ARTIFICIAL	f	f	\N	\N	\N	f	f	f	t	5	1	100	2026-01-20 17:36:22.573303	0	0	\N	\N	\N
43	ARTIFICIAL_MEGASTRUCTURE	Megastructures	Cities, arcologies, megastructures	ARTIFICIAL	f	f	\N	\N	\N	f	f	f	t	3	0.1	30	2026-01-20 17:36:22.573303	0	0	\N	\N	\N
44	ARTIFICIAL_RUINS	Ancient Ruins	Archaeological sites and ruins	ARTIFICIAL	f	f	\N	\N	\N	f	f	f	t	8	0.1	10	2026-01-20 17:36:22.573303	0	0	\N	\N	\N
46	GLASS_FIELDS	Glass Fields	Vast fields of natural glass from ancient impacts or volcanic activity	EXOTIC	f	f	200	800	\N	f	f	f	f	40	1	15	2026-01-20 23:43:06.206713	0	0	\N	\N	\N
47	METAL_PLAINS	Metal Plains	Exposed metallic minerals creating reflective plains	EXOTIC	f	f	100	600	\N	f	f	f	f	30	2	20	2026-01-20 23:43:06.206713	0	0	\N	\N	\N
50	SUBLIMATION_ZONES	Sublimation Zones	Areas where ices sublimate directly to gas creating unique landscapes	EXOTIC	f	f	70	150	\N	f	t	f	f	45	2	20	2026-01-20 23:43:06.206713	0	0	\N	\N	\N
37	WASTELAND	Barren Wasteland	Lifeless, barren terrain	BARREN	f	f	0	800	\N	f	f	f	f	180	5	80	2026-01-20 17:36:22.573303	0	0	\N	\N	\N
45	ISLANDS	Islands	Land masses surrounded by water	AQUATIC	t	t	270	320	\N	f	f	t	f	90	1	20	2026-01-20 17:36:22.573303	0	0	{"Iron Planet","Hot Rocky Planet","Lava Planet"}	\N	{IRON_RICH,GAS_ENVELOPE,MOLTEN_SURFACE}
7	WETLANDS	Wetlands and Marshes	Swamps, bogs, marshes, and wetlands	AQUATIC	t	t	273	320	\N	f	f	t	f	80	1	15	2026-01-20 17:36:22.573303	0	0	{"Iron Planet","Hot Rocky Planet","Lava Planet"}	\N	{IRON_RICH,GAS_ENVELOPE,MOLTEN_SURFACE}
30	PLAINS	Plains	Flat or gently rolling lowlands	PLAINS	f	f	100	\N	\N	f	f	f	f	150	10	50	2026-01-20 17:36:22.573303	0	0	\N	\N	\N
31	MUD_FLATS	Mud Flats	Tidal flats and sediment plains	PLAINS	t	t	273	310	\N	f	f	f	f	60	0.5	8	2026-01-20 17:36:22.573303	0	0	\N	\N	\N
18	STEPPE	Steppe	Semi-arid grasslands with sparse vegetation and seasonal grasses	ARID	f	t	250	320	0.1	f	f	f	f	90	5	35	2026-01-20 17:36:22.573303	0	0	{"Iron Planet","Hot Rocky Planet","Lava Planet","Ice World"}	\N	{IRON_RICH,MOLTEN_SURFACE,GAS_ENVELOPE}
29	KARST	Karst Terrain	Limestone terrain with caves and sinkholes	MOUNTAIN	t	t	273	310	\N	f	f	f	f	60	1	10	2026-01-20 17:36:22.573303	0	0	\N	\N	\N
23	DUNES	Dune Fields	Active sand dune systems	ARID	f	t	50	350	\N	f	f	f	f	90	2	30	2026-01-20 17:36:22.573303	0	0	\N	\N	\N
16	GRASSLAND	Verdant Grasslands	Lush prairies, savannas, and meadows with rich seasonal vegetation	TEMPERATE	t	t	275	310	0.5	f	f	f	f	150	10	50	2026-01-20 17:36:22.573303	0	0	{"Iron Planet","Hot Rocky Planet","Lava Planet","Ice World","Desert Planet"}	\N	{IRON_RICH,MOLTEN_SURFACE,GAS_ENVELOPE,ICE_RICH}
22	SALT_FLATS	Salt Flats	Vast evaporite plains and dry lake beds with crystalline salt deposits	ARID	f	f	273	340	\N	f	f	f	f	70	1	15	2026-01-20 17:36:22.573303	0	0	{"Ice World","Ice Giant","Gas Giant"}	\N	\N
1	OCEAN_DEEP	Deep Ocean	Ocean deeper than 200m, abyssal zones	AQUATIC	t	f	273	373	\N	f	f	t	f	200	0	70	2026-01-20 17:36:22.573303	0	0	{"Iron Planet","Hot Rocky Planet","Lava Planet"}	\N	{IRON_RICH,GAS_ENVELOPE,MOLTEN_SURFACE}
2	OCEAN_SHALLOW	Shallow Ocean	Continental shelf, depths less than 200m	AQUATIC	t	f	273	373	\N	f	f	t	f	150	0	30	2026-01-20 17:36:22.573303	0	0	{"Iron Planet","Hot Rocky Planet","Lava Planet"}	\N	{IRON_RICH,GAS_ENVELOPE,MOLTEN_SURFACE}
3	OCEAN_ABYSSAL	Abyssal Depths	Extremely deep ocean trenches (>4000m)	AQUATIC	t	f	273	373	\N	f	f	t	f	80	0	10	2026-01-20 17:36:22.573303	0	0	{"Iron Planet","Hot Rocky Planet","Lava Planet"}	\N	{IRON_RICH,GAS_ENVELOPE,MOLTEN_SURFACE}
4	COASTAL	Coastal Zone	Shoreline and littoral zones	AQUATIC	t	t	273	323	\N	f	f	t	f	100	1	15	2026-01-20 17:36:22.573303	0	0	{"Iron Planet","Hot Rocky Planet","Lava Planet"}	\N	{IRON_RICH,GAS_ENVELOPE,MOLTEN_SURFACE}
5	LAKES	Lakes	Inland freshwater bodies	AQUATIC	t	t	273	323	\N	f	f	t	f	120	1	10	2026-01-20 17:36:22.573303	0	0	{"Iron Planet","Hot Rocky Planet","Lava Planet"}	\N	{IRON_RICH,GAS_ENVELOPE,MOLTEN_SURFACE}
6	RIVERS	Rivers and Waterways	Flowing freshwater systems	AQUATIC	t	t	273	323	\N	f	f	t	f	100	0.5	5	2026-01-20 17:36:22.573303	0	0	{"Iron Planet","Hot Rocky Planet","Lava Planet"}	\N	{IRON_RICH,GAS_ENVELOPE,MOLTEN_SURFACE}
51	RADIATION_SCORCHED	Radiation Scorched	Terrain heavily altered by stellar radiation	EXOTIC	f	f	0	1500	\N	f	f	f	f	35	1	25	2026-01-20 23:43:06.206713	0	0	\N	\N	\N
52	NITROGEN_GEYSERS	Nitrogen Geyser Fields	Active nitrogen geysers (Triton-like)	EXOTIC	f	f	35	60	\N	f	t	f	f	25	0.5	10	2026-01-20 23:43:06.206713	0	0	\N	\N	\N
53	HYDROCARBON_SEAS	Hydrocarbon Seas	Ethane/propane lakes and seas	EXOTIC	f	f	70	110	\N	f	f	f	f	30	5	40	2026-01-20 23:43:06.206713	0	0	\N	\N	\N
38	CRYSTAL_FORMATIONS	Crystal Formations	Exposed quartz and silicate crystal formations	EXOTIC	f	f	200	600	\N	f	f	f	f	30	1	10	2026-01-20 17:36:22.573303	0	0	\N	\N	\N
56	ROCKY_BADLANDS	Rocky Badlands	Rugged, eroded rocky terrain	BARREN	f	f	0	1000	\N	f	f	f	f	120	5	35	2026-01-21 11:13:05.202933	0	0	\N	\N	\N
57	FROZEN_REGOLITH	Frozen Regolith	Ice-bound rocky debris plains on extremely cold bodies	BARREN	f	f	0	120	\N	f	t	f	f	160	15	60	2026-01-21 11:13:05.202933	0	0	\N	\N	\N
59	NITROGEN_ICE_FIELDS	Nitrogen Ice Fields	Expanses of frozen nitrogen (Pluto-like)	BARREN	f	f	20	60	\N	f	t	f	f	80	10	40	2026-01-21 11:13:05.202933	0	0	\N	\N	\N
61	THERMAL_FRACTURES	Thermal Fracture Zones	Terrain cracked by extreme temperature cycling	BARREN	f	f	100	800	\N	f	f	f	f	80	5	20	2026-01-21 11:13:05.202933	0	0	\N	\N	\N
36	IMPACT_CRATERS	Impact Crater Fields	Heavy impact cratering	BARREN	f	f	0	1000	\N	f	f	f	f	150	5	60	2026-01-20 17:36:22.573303	100	0	\N	\N	\N
55	CRATERED_HIGHLANDS	Cratered Highlands	Elevated terrain heavily pocked with impact craters	BARREN	f	f	0	1500	\N	f	f	f	f	140	10	40	2026-01-21 11:13:05.202933	80	0	\N	\N	\N
54	REGOLITH_PLAINS	Regolith Plains	Flat expanses of loose rocky debris and dust	BARREN	f	f	0	1500	\N	f	f	f	f	200	20	70	2026-01-21 11:13:05.202933	30	0	\N	\N	\N
26	HILLS	Hills and Highlands	Rolling hills and low highlands	MOUNTAIN	f	f	\N	\N	\N	f	f	f	f	140	10	40	2026-01-20 17:36:22.573303	0	0	\N	\N	\N
24	MOUNTAINS_HIGH	High Alpine Mountains	High elevation mountain peaks and ranges	MOUNTAIN	f	f	\N	\N	\N	f	f	f	f	100	5	25	2026-01-20 17:36:22.573303	0	0	\N	\N	\N
25	MOUNTAINS_MID	Mid-elevation Mountains	Moderate elevation mountains and foothills	MOUNTAIN	f	f	\N	\N	\N	f	f	f	f	120	10	35	2026-01-20 17:36:22.573303	0	0	\N	\N	\N
27	PLATEAU	Plateaus	Elevated flat tablelands	MOUNTAIN	f	f	\N	\N	\N	f	f	f	f	90	5	25	2026-01-20 17:36:22.573303	0	0	\N	\N	\N
28	CANYON	Canyons and Gorges	Deep valleys and canyon systems	MOUNTAIN	f	f	\N	\N	\N	f	f	f	f	80	1	15	2026-01-20 17:36:22.573303	0	0	\N	\N	\N
12	TUNDRA	Tundra	Cold plains with permafrost, moss, lichen, and low-lying hardy vegetation	ICE	f	t	240	280	0.15	f	t	f	f	100	5	30	2026-01-20 17:36:22.573303	0	0	{"Hot Rocky Planet","Lava Planet"}	\N	{MOLTEN_SURFACE}
40	SULFUR_PLAINS	Sulfur Plains	Sulfurous volcanic plains (Io-like)	VOLCANIC	f	f	110	400	\N	f	f	f	f	30	10	40	2026-01-20 17:36:22.573303	0	100	\N	\N	\N
70	LUNAR_MARE	Lunar Mare	Dark basaltic plains from ancient volcanic flooding	BARREN	f	f	0	1500	\N	f	f	f	f	100	10	50	2026-01-21 11:41:41.826211	50	0	\N	\N	\N
71	RILLES	Rilles and Channels	Collapsed lava tube channels and sinuous valleys	BARREN	f	f	0	1000	\N	f	f	f	f	60	1	10	2026-01-21 11:41:41.826211	30	0	\N	\N	\N
72	EJECTA_FIELDS	Ejecta Fields	Debris fields surrounding major impact craters	BARREN	f	f	0	1500	\N	f	f	f	f	80	2	20	2026-01-21 11:41:41.826211	120	0	\N	\N	\N
73	PEDESTAL_CRATERS	Pedestal Craters	Elevated impact craters from differential erosion	BARREN	f	t	150	400	\N	f	f	f	f	40	1	10	2026-01-21 11:41:41.826211	80	0	\N	\N	\N
75	TAR_PITS	Tar Pits and Hydrocarbon Bogs	Thick hydrocarbon deposits and tar lakes	EXOTIC	f	t	90	150	\N	f	f	f	f	20	1	15	2026-01-21 11:41:41.826211	0	0	\N	\N	\N
76	BANDED_TERRAIN	Banded Terrain	Linear ridges and grooves from tidal flexing	EXOTIC	f	f	50	200	\N	f	t	f	f	35	5	30	2026-01-21 11:41:41.826211	0	0	\N	\N	\N
77	CHAOTIC_TERRAIN	Chaotic Terrain	Disrupted and jumbled surface blocks from subsurface activity	EXOTIC	f	f	50	250	\N	f	t	f	f	40	3	20	2026-01-21 11:41:41.826211	0	0	\N	\N	\N
78	METAL_FROST	Metal Frost	Condensed metallic deposits on cooler regions	EXOTIC	f	f	400	800	\N	f	f	f	f	20	1	15	2026-01-21 11:41:41.826211	0	0	\N	\N	\N
79	TESSERA	Tessera Terrain	Highly deformed and folded crustal terrain	EXOTIC	f	t	400	800	\N	f	f	f	f	35	5	25	2026-01-21 11:41:41.826211	0	0	\N	\N	\N
85	YARDANGS	Yardang Fields	Streamlined wind-eroded rock formations	ARID	f	t	150	400	\N	f	f	f	f	50	2	15	2026-01-21 11:41:41.826211	0	0	\N	\N	\N
86	BADLANDS	Badlands	Heavily eroded terrain with dramatic formations	ARID	f	t	240	330	\N	f	f	f	f	80	3	20	2026-01-21 11:41:41.826211	0	0	\N	\N	\N
87	OASIS	Oases	Isolated water sources in arid regions	ARID	t	t	280	330	\N	f	f	f	f	30	0.1	3	2026-01-21 11:41:41.826211	0	0	\N	\N	\N
88	MESAS	Mesas and Buttes	Flat-topped erosional remnants	MOUNTAIN	f	f	200	350	\N	f	f	f	f	70	2	15	2026-01-21 11:41:41.826211	0	0	\N	\N	\N
89	TIDAL_FORESTS	Tidal Forests	Coastal forests adapted to tidal flooding	TEMPERATE	t	t	285	310	\N	f	f	f	f	40	1	10	2026-01-21 11:41:41.826211	0	0	\N	\N	\N
90	ALPINE_MEADOW	Alpine Meadows	High-altitude grasslands above treeline	TEMPERATE	f	t	260	290	\N	f	f	f	f	70	2	15	2026-01-21 11:41:41.826211	0	0	\N	\N	\N
62	CRYOVOLCANIC_FIELDS	Cryovolcanic Fields	Ice volcanoes erupting water, ammonia, or methane slurries	VOLCANIC	f	f	30	200	\N	t	t	f	f	70	2	20	2026-01-21 11:20:52.504394	0	120	{"Hot Rocky Planet","Lava Planet","Iron Planet"}	{ICE_RICH,OCEAN_WORLD}	\N
63	CRYOLAVA_PLAINS	Cryolava Plains	Smooth plains formed by cryolava flows	VOLCANIC	f	f	30	180	\N	t	t	f	f	60	5	25	2026-01-21 11:20:52.504394	0	100	{"Hot Rocky Planet","Lava Planet","Iron Planet"}	{ICE_RICH,OCEAN_WORLD}	\N
64	ICE_GEYSER_FIELDS	Ice Geyser Fields	Active regions of ice and vapor geysers	VOLCANIC	f	f	30	150	\N	t	t	f	f	50	1	10	2026-01-21 11:20:52.504394	0	100	{"Hot Rocky Planet","Lava Planet","Iron Planet"}	{ICE_RICH,OCEAN_WORLD}	\N
91	CORAL_REEFS	Reef Systems	Shallow water carbonate structures	AQUATIC	t	f	285	305	\N	f	f	t	f	60	0.5	8	2026-01-21 11:41:41.826211	0	0	{"Iron Planet","Hot Rocky Planet","Lava Planet"}	\N	{IRON_RICH,GAS_ENVELOPE,MOLTEN_SURFACE}
92	RIVER_DELTA	River Deltas	Sediment deposits at river mouths	AQUATIC	t	t	273	320	\N	f	f	t	f	70	0.5	5	2026-01-21 11:41:41.826211	0	0	{"Iron Planet","Hot Rocky Planet","Lava Planet"}	\N	{IRON_RICH,GAS_ENVELOPE,MOLTEN_SURFACE}
93	FJORDS	Fjords	Steep-walled glacially carved coastal inlets	AQUATIC	t	t	260	290	\N	f	f	t	f	50	1	8	2026-01-21 11:41:41.826211	0	0	{"Iron Planet","Hot Rocky Planet","Lava Planet"}	\N	{IRON_RICH,GAS_ENVELOPE,MOLTEN_SURFACE}
32	VOLCANIC_ACTIVE	Active Volcanic Fields	Currently active lava flows and volcanic zones	VOLCANIC	f	f	300	1500	\N	t	f	f	f	50	1	20	2026-01-20 17:36:22.573303	0	150	{"Ice World"}	\N	{ICE_RICH}
33	VOLCANIC_DORMANT	Dormant Volcanic Terrain	Inactive volcanic features	VOLCANIC	f	f	250	350	\N	t	f	f	f	80	2	25	2026-01-20 17:36:22.573303	0	100	{"Ice World"}	\N	{ICE_RICH}
34	LAVA_TUBES	Lava Tubes and Caves	Volcanic cave systems	VOLCANIC	f	f	250	400	\N	t	f	f	f	40	0.1	5	2026-01-20 17:36:22.573303	0	60	{"Ice World"}	\N	{ICE_RICH}
65	VOLCANIC_VENTS	Volcanic Vents	Localized volcanic vents and fumaroles	VOLCANIC	f	f	0	500	\N	t	f	f	f	80	1	12	2026-01-21 11:20:52.504394	0	80	{"Ice World"}	\N	{ICE_RICH}
66	THERMAL_ANOMALIES	Thermal Anomaly Zones	Warm regions from subsurface volcanic heating on cold worlds	VOLCANIC	f	f	40	250	\N	t	f	f	f	50	0.5	8	2026-01-21 11:20:52.504394	0	90	{"Ice World"}	\N	{ICE_RICH}
35	GEOTHERMAL	Geothermal Fields	Hot springs, geysers, and thermal features	VOLCANIC	f	f	200	380	\N	t	f	f	f	60	0.5	10	2026-01-20 17:36:22.573303	0	80	{"Ice World"}	\N	{ICE_RICH}
80	OBSIDIAN_FLOWS	Obsidian Flows	Volcanic glass formations from rapid lava cooling	VOLCANIC	f	f	200	800	\N	t	f	f	f	40	1	12	2026-01-21 11:41:41.826211	0	70	{"Ice World"}	\N	{ICE_RICH}
81	FUMAROLE_FIELDS	Fumarole Fields	Dense concentrations of volcanic gas vents	VOLCANIC	f	f	150	600	\N	t	f	f	f	50	0.5	8	2026-01-21 11:41:41.826211	0	90	{"Ice World"}	\N	{ICE_RICH}
83	MAGMA_OCEAN	Magma Ocean	Planet-spanning molten rock surface	VOLCANIC	f	f	1200	3000	\N	t	f	f	f	15	20	90	2026-01-21 11:41:41.826211	0	200	{"Ice World"}	\N	{ICE_RICH}
60	SCORCHED_PLAINS	Scorched Plains	Heat-blasted flat terrain	BARREN	f	f	400	1500	\N	f	f	f	f	100	10	50	2026-01-21 11:13:05.202933	0	0	{"Ice World","Ocean Planet"}	\N	{ICE_RICH,OCEAN_WORLD}
39	METHANE_LAKES	Methane Lakes	Liquid methane/ethane bodies (Titan-like)	EXOTIC	f	f	70	100	\N	f	f	f	f	35	5	25	2026-01-20 17:36:22.573303	0	0	\N	{ICE_RICH}	\N
74	AMMONIA_SEAS	Ammonia Seas	Liquid ammonia bodies on cold worlds	EXOTIC	f	f	195	240	\N	f	f	f	f	25	5	40	2026-01-21 11:41:41.826211	0	0	\N	{ICE_RICH}	\N
82	LAVA_LAKES	Lava Lakes	Persistent pools of molten rock	VOLCANIC	f	f	800	1500	\N	t	f	f	f	30	0.5	10	2026-01-21 11:41:41.826211	0	150	{"Ice World"}	\N	{ICE_RICH}
19	DESERT_SAND	Sand Desert	Sandy deserts and ergs	ARID	f	t	150	350	\N	f	f	f	f	120	10	70	2026-01-20 17:36:22.573303	0	0	{"Ocean Planet","Ice World"}	\N	{OCEAN_WORLD}
20	DESERT_ROCK	Rock Desert	Rocky badlands and hammada	ARID	f	f	100	350	\N	f	f	f	f	110	10	60	2026-01-20 17:36:22.573303	0	0	{"Ocean Planet","Ice World"}	\N	{OCEAN_WORLD}
99	CRYOPLAINS	Cryogenic Plains	Frozen barren lowlands with frost-covered rocky surfaces and ice deposits	ICE	f	f	50	200	\N	f	f	f	f	110	10	50	2026-01-21 16:26:42.876937	0	0	{"Hot Rocky Planet","Lava Planet"}	\N	{MOLTEN_SURFACE}
100	BOREAL_PLAINS	Boreal Plains	Cold grassy plains transitioning between tundra and temperate zones	TEMPERATE	f	t	260	280	0.2	f	f	f	f	90	10	40	2026-01-21 16:26:42.876937	0	0	{"Hot Rocky Planet","Lava Planet","Ice World"}	\N	{MOLTEN_SURFACE,IRON_RICH}
101	REGOLITH_FLATS	Regolith Flats	Pulverized rocky plains blanketed in fine dust and impact debris	BARREN	f	f	0	800	\N	f	f	f	f	140	10	60	2026-01-21 16:26:42.876937	50	0	\N	\N	{GAS_ENVELOPE}
102	LAVA_PLAINS	Lava Plains	Vast basaltic plains formed by ancient volcanic flood basalts	VOLCANIC	f	f	200	600	\N	f	f	f	f	80	10	50	2026-01-21 16:26:42.876937	0	60	{"Ice World"}	\N	{ICE_RICH,GAS_ENVELOPE}
84	PENITENTES	Penitentes Fields	Tall blade-like ice formations from sublimation	ICE	f	f	150	273	\N	f	t	f	f	30	1	10	2026-01-21 11:41:41.826211	0	0	{"Iron Planet","Hot Rocky Planet","Lava Planet"}	{ICE_RICH,OCEAN_WORLD,MIXED_SILICATE_ICE}	{IRON_RICH,MOLTEN_SURFACE,GAS_ENVELOPE}
8	POLAR_ICE	Polar Ice Caps	Permanent polar ice coverage	ICE	f	f	0	273	\N	f	t	f	f	150	5	50	2026-01-20 17:36:22.573303	0	0	{"Iron Planet","Hot Rocky Planet","Lava Planet"}	{ICE_RICH,OCEAN_WORLD,MIXED_SILICATE_ICE}	{IRON_RICH,MOLTEN_SURFACE,GAS_ENVELOPE}
9	GLACIER	Glaciers	Massive ice rivers and sheets	ICE	f	f	0	273	\N	f	t	f	f	120	2	40	2026-01-20 17:36:22.573303	0	0	{"Iron Planet","Hot Rocky Planet","Lava Planet"}	{ICE_RICH,OCEAN_WORLD,MIXED_SILICATE_ICE}	{IRON_RICH,MOLTEN_SURFACE,GAS_ENVELOPE}
10	ICE_SHEET	Continental Ice Sheets	Continent-spanning ice coverage	ICE	f	f	0	250	\N	f	t	f	f	100	10	80	2026-01-20 17:36:22.573303	0	0	{"Iron Planet","Hot Rocky Planet","Lava Planet"}	{ICE_RICH,OCEAN_WORLD,MIXED_SILICATE_ICE}	{IRON_RICH,MOLTEN_SURFACE,GAS_ENVELOPE}
11	PERMAFROST	Permafrost	Permanently frozen ground	ICE	f	t	0	273	\N	f	t	f	f	110	5	60	2026-01-20 17:36:22.573303	0	0	{"Iron Planet","Hot Rocky Planet","Lava Planet"}	{ICE_RICH,OCEAN_WORLD,MIXED_SILICATE_ICE}	{IRON_RICH,MOLTEN_SURFACE,GAS_ENVELOPE}
58	ICY_CRATERS	Icy Impact Basins	Large impact craters with frozen volatiles	BARREN	f	f	0	150	\N	f	t	f	f	100	5	30	2026-01-21 11:13:05.202933	50	0	\N	{ICE_RICH,OCEAN_WORLD,MIXED_SILICATE_ICE}	\N
48	DIAMOND_DEPOSITS	Diamond Deposits	Carbon-rich deposits with exposed diamonds (carbon planets)	EXOTIC	f	f	200	1000	\N	f	f	f	f	20	0.5	10	2026-01-20 23:43:06.206713	0	0	\N	{CARBON_RICH}	\N
49	GRAPHITE_FIELDS	Graphite Fields	Vast graphite deposits (carbon planets)	EXOTIC	f	f	200	800	\N	f	f	f	f	35	5	30	2026-01-20 23:43:06.206713	0	0	\N	{CARBON_RICH}	\N
103	CARBON_FLATS	Carbon Flats	Graphite and silicon carbide plains characteristic of carbon-rich worlds	EXOTIC	f	f	200	700	\N	f	f	f	f	40	5	30	2026-01-21 16:26:42.876937	0	0	\N	{CARBON_RICH}	\N
\.



--
-- Data for Name: factions; Type: TABLE DATA; Schema: ud; Owner: starcreator_dev
--

COPY ud.factions (id, alignment, description, name, type, influence, government_type, ai_created, created_at, modified_on) FROM stdin;
2	Lawful Evil	Expansive and authoritarian, imposing their ideology on conquered worlds under the guise of “enlightenment.”	Solari Dominion	Galactic Empire	0	14	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
3	Neutral Evil	A calculating, profit-driven empire that exploits weaker planets and species.	Kronarchate of Tethys	Galactic Empire	0	45	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
4	Lawful Good	An interstellar government seeking peace and justice through diplomacy and regulated trade.	United Stellar Coalition	Galactic Empire	0	15	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
5	Lawful Neutral	A council of elders governing with rigid laws, valuing precedent and procedure above morality.	Arcturian Senate	Galactic Empire	0	17	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
6	Lawful Evil	Militaristic rulers seeking dominance over the galaxy with strict social hierarchies.	Zenith Empire	Galactic Empire	0	12	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
8	Chaotic Evil	A tyrannical, unpredictable dictatorship that thrives on fear and suppression.	Nova Regime	Galactic Empire	0	7	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
9	Neutral Good	A council of scholars and scientists working to maintain peace and knowledge across galaxies.	The Aether Conclave	Galactic Empire	0	11	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
10	Lawful Neutral	Triad leadership governing with rigid checks and balances to maintain order.	Galactic Triumvirate	Galactic Empire	0	11	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
11	Lawful Good	Oversees interstellar law, diplomacy, and conflict resolution for the greater good.	The Ecliptic Council	Galactic Empire	0	11	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
12	Neutral Good	Loose coalition of planets cooperating for mutual benefit and protection.	Vesperian Confederacy	Galactic Empire	0	13	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
13	Lawful Evil	An empire obsessed with order, conquest, and cultural assimilation.	The Dominion of Cindral	Galactic Empire	0	12	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
15	Lawful Good	An impartial interstellar judiciary dedicated to justice and fairness.	The Stellar Tribunal	Galactic Empire	0	11	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
16	Chaotic Good	Rebels fighting for freedom from oppressive regimes using guerrilla tactics.	Nova Vanguard	Rebel/Resistance	0	50	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
17	Chaotic Neutral	Mercenaries and spies with no allegiance beyond profit and survival.	Shadowfleet	Rebel/Resistance	0	33	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
18	Chaotic Good	Radical freedom fighters resisting tyranny through daring raids.	Ember Rebellion	Rebel/Resistance	0	38	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
19	Neutral Good	Coalition of planets advocating self-governance and peaceful resistance.	Free Stars Alliance	Rebel/Resistance	0	50	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
20	Chaotic Neutral	Phantom-like operatives striking unpredictably against oppressors.	Ghosts of Lyra	Rebel/Resistance	0	36	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
21	Chaotic Evil	A violent faction using terror and sabotage to achieve power.	Crimson Insurgency	Rebel/Resistance	0	41	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
23	Neutral Good	Idealists seeking to restore civilization after corruption and war.	Radiant Dawn Collective	Rebel/Resistance	0	50	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
24	Chaotic Evil	Radical faction aiming to destabilize galactic powers through violence.	The Obsidian Front	Rebel/Resistance	0	36	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
25	Chaotic Good	Spacefarers breaking oppressive monopolies and liberating enslaved worlds.	Voidbreakers	Rebel/Resistance	0	38	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
26	Neutral Good	Organized group advocating for peace and planetary independence.	Starlit Resistance	Rebel/Resistance	0	38	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
27	Lawful Neutral	Militaristic rebels with a strict code, balancing rebellion with order.	The Iron Phalanx	Rebel/Resistance	0	10	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
28	Chaotic Neutral	Rogue faction blending smuggling, sabotage, and rebellion.	Emberwing Syndicate	Rebel/Resistance	0	33	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
30	Neutral Good	Regional fighters striving to overthrow tyranny through coordinated efforts.	Dawnward Rebels	Rebel/Resistance	0	50	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
31	Neutral Evil	Corporate empire exploiting resources and people for profit.	SynthCorp Dominion	Corporate/Technocratic	0	18	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
32	Lawful Neutral	Tech conglomerate operating under strict corporate laws, morally indifferent.	Helix Industries	Corporate/Technocratic	0	19	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
33	Neutral Good	Innovators advancing technology to improve lives across galaxies.	Quantum Horizons	Corporate/Technocratic	0	42	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
35	Lawful Neutral	Biotechnology company with strict regulations and ethical oversight.	Orion Biotech Consortium	Corporate/Technocratic	0	18	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
36	Lawful Evil	Technocracy using AI and surveillance to control populations.	Cyberion Collective	Corporate/Technocratic	0	47	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
37	Neutral Evil	Corporate giants profiting from genetic manipulation and resource exploitation.	NovaGen Corporation	Corporate/Technocratic	0	18	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
38	Lawful Neutral	Engineers and mercenaries operating within strict codes of contract.	Apex Dynamics	Corporate/Technocratic	0	46	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
39	Neutral Good	Group of scientists collaborating to bring technological aid to underdeveloped worlds.	TechnoCore Alliance	Corporate/Technocratic	0	19	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
40	Neutral Evil	Space industrialists exploiting planets for energy and raw materials.	VoidWorks Incorporated	Corporate/Technocratic	0	18	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
42	Lawful Neutral	Masters of time-related technologies, bound by strict corporate regulations.	ChronoTech Conglomerate	Corporate/Technocratic	0	26	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
43	Chaotic Neutral	Rogue engineers selling experimental tech to the highest bidder.	Vortex Solutions	Corporate/Technocratic	0	43	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
44	Neutral Evil	Nanotech company using covert methods to dominate markets.	NanoDyne Industries	Corporate/Technocratic	0	18	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
45	Lawful Good	Combining technology and ethics to create helpful AI and robots for society.	Arcane Robotics	Corporate/Technocratic	0	42	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
46	Lawful Neutral	Hive-mind species valuing order and consensus above individuality.	Xylar Collective	Alien/Extraterrestrial	0	22	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
47	Neutral Evil	Parasitic species seeking to expand influence through subtle domination.	Vorthax Enclave	Alien/Extraterrestrial	0	27	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
51	Lawful Evil	Hierarchical insectoid species with strict societal rules and conquest goals.	Arak’Nid Ascendancy	Alien/Extraterrestrial	0	43	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
52	Chaotic Neutral	Shadowy aliens manipulating galactic events for mysterious purposes.	Veilborn	Alien/Extraterrestrial	0	30	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
53	Neutral Good	Peaceful explorers seeking knowledge and cultural exchange.	Lumari Constellation	Alien/Extraterrestrial	0	28	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
54	Lawful Evil	Expansionist empire imposing strict control over conquered worlds.	Qirathi Dominion	Alien/Extraterrestrial	0	14	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
57	Lawful Good	Coalition of species promoting interstellar peace and stability.	The S’var Alliance	Alien/Extraterrestrial	0	15	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
58	Chaotic Neutral	Nomadic aliens living by instinct and tradition, resisting hierarchy.	Orionid Tribes	Alien/Extraterrestrial	0	23	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
59	Neutral Good	Telepathic species helping other worlds reach enlightenment.	Nytheri Collective	Alien/Extraterrestrial	0	27	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
60	Lawful Neutral	Galactic rulers focused on structured governance and cultural preservation.	Eryndari Ascendancy	Alien/Extraterrestrial	0	14	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
61	Chaotic Neutral	Space pirates operating freely without allegiance.	Void Corsairs	Pirate/Outlaw	0	34	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
63	Lawful Evil	Mercenaries with strict codes, but no morality beyond profit.	Iron Scourge	Pirate/Outlaw	0	46	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
64	Chaotic Neutral	Smugglers and raiders thriving on chaos and opportunity.	Neon Jackals	Pirate/Outlaw	0	34	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
65	Neutral Evil	Criminal engineers using technology for gain and sabotage.	Rogue Helix	Pirate/Outlaw	0	36	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
66	Chaotic Good	Outlaws fighting oppression while pursuing freedom.	Starward Rogues	Pirate/Outlaw	0	49	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
67	Chaotic Evil	Bloodthirsty pirates seeking dominance through terror.	Crimson Eclipse	Pirate/Outlaw	0	34	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
69	Chaotic Evil	Pirates who manipulate dark technology for pillaging.	Darklight Corsairs	Pirate/Outlaw	0	34	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
70	Neutral Evil	Organized gang controlling a dangerous asteroid stronghold.	The Shattered Spire	Pirate/Outlaw	0	36	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
71	Neutral Good	Rogue vigilantes protecting settlements from other pirates.	Wraith Hunters	Pirate/Outlaw	0	35	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
72	Chaotic Evil	Brutal marauders who leave nothing behind.	Ashen Raiders	Pirate/Outlaw	0	34	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
73	Lawful Evil	Mafia-like organization dominating black-market trade.	Voidclaw Syndicate	Pirate/Outlaw	0	36	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
75	Chaotic Good	Outlaws fighting tyranny while living outside the law.	Specter Drifters	Pirate/Outlaw	0	49	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
76	Lawful Neutral	Monastic order controlling dark energies with strict rules.	Order of the Void	Mystical/Occult	0	29	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
77	Neutral Good	Astral scholars guiding civilizations spiritually and intellectually.	Celestial Mystics	Mystical/Occult	0	31	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
78	Neutral Evil	Occult group exploiting mystical artifacts for power.	The Shard Circle	Mystical/Occult	0	24	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
80	Lawful Evil	Dark sorcerers imposing their will on civilizations.	Eclipse Order	Mystical/Occult	0	48	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
82	Neutral Good	Warrior monks protecting the innocent with enchanted tech.	Starforged Acolytes	Mystical/Occult	0	31	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
83	Lawful Evil	Secretive organization spreading fear to control societies.	The Obsidian Covenant	Mystical/Occult	0	48	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
84	Chaotic Neutral	Travelers exploiting rifts between dimensions.	Riftwalkers	Mystical/Occult	0	26	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
85	Neutral Good	Scholars combining magic and science to benefit all.	The Etherium Circle	Mystical/Occult	0	29	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
86	Chaotic Evil	Malevolent sect spreading corruption and darkness.	Nightfall Cult	Mystical/Occult	0	24	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
88	Lawful Neutral	Time-manipulating technomancers enforcing strict temporal rules.	Chronomancers’ Guild	Mystical/Occult	0	26	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
89	Chaotic Evil	Darkly gifted beings seeking domination through chaos.	The Voidborn	Mystical/Occult	0	25	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
90	Neutral Good	Cosmic philosophers guiding societies with moral insight.	The Starlight Synod	Mystical/Occult	0	31	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
91	Chaotic Good	Nomads exploring the galaxy to help frontier worlds.	Wandering Suns	Nomadic/Frontier	0	52	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
93	Lawful Neutral	Organized merchants maintaining order across trade routes.	Startrail Caravans	Nomadic/Frontier	0	21	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
94	Chaotic Neutral	Mercenary wanderers living by their own rules.	Iron Nomads	Nomadic/Frontier	0	39	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
95	Neutral Good	Peaceful voyagers spreading knowledge and aid to new worlds.	The Cosmic Wanderers	Nomadic/Frontier	0	52	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
96	Chaotic Good	Freelance heroes protecting isolated colonies.	Solar Drifters	Nomadic/Frontier	0	52	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
97	Lawful Neutral	Tribal community adhering to strict customs and laws.	The Horizon Clan	Nomadic/Frontier	0	40	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
99	Chaotic Neutral	Rebels seeking a home outside galactic powers.	The Lost Constellation	Nomadic/Frontier	0	41	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
100	Neutral Good	Nomadic explorers promoting diplomacy across star systems.	Galactic Wayfarers	Nomadic/Frontier	0	52	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
101	Chaotic Neutral	Traders avoiding law and authority for profit and freedom.	The Rogue Caravans	Nomadic/Frontier	0	39	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
102	Chaotic Good	Vigilant nomads protecting isolated planets.	Aurora Riders	Nomadic/Frontier	0	52	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
103	Lawful Good	Cooperative communities sustaining order in unsettled space.	The Frontier Kin	Nomadic/Frontier	0	51	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
55	Neutral Evil	Mercenary species selling military services to the highest bidder.	Draxis Pact	Alien/Extraterrestrial	0	32	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
7	Neutral Evil	A pragmatic, expansionist power focused on resources and influence rather than ideology.	The Orion Hegemony	Galactic Empire	0	14	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
14	Lawful Neutral	Enforces strict laws to maintain galactic stability, regardless of personal cost.	Hyperion Authority	Galactic Empire	0	10	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
22	Chaotic Good	Independent raiders defending frontier worlds from corrupt overlords.	Lunar Outriders	Rebel/Resistance	0	39	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
29	Chaotic Good	Swift strike force targeting oppressive regimes in covert operations.	Eclipse Strike	Rebel/Resistance	0	38	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
34	Chaotic Neutral	Independent technocrats pushing innovation with little regard for law.	Mechatech Syndicate	Corporate/Technocratic	0	19	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
41	Neutral Good	Research-focused corporation striving for universal advancement.	Stellar Innovations Group	Corporate/Technocratic	0	19	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
49	Chaotic Neutral	Tribal warriors with shifting alliances and unpredictable strategies.	Zyn’Rath Clans	Alien/Extraterrestrial	0	23	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
56	Chaotic Evil	Violent marauders with little strategy beyond aggression.	Helion Brood	Alien/Extraterrestrial	0	22	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
62	Chaotic Evil	Ruthless raiders preying on unprotected trade routes.	Black Star Marauders	Pirate/Outlaw	0	34	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
68	Chaotic Neutral	Opportunistic raiders exploiting interstellar gaps.	The Rift Bandits	Pirate/Outlaw	0	33	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
74	Chaotic Neutral	Freelance space bandits drifting between alliances.	Nebula Marauders	Pirate/Outlaw	0	34	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
79	Chaotic Neutral	Mystics embracing chaos to manipulate reality.	The Astral Sect	Mystical/Occult	0	29	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
81	Lawful Good	Mystical order using knowledge and magic to aid the weak.	The Luminous Path	Mystical/Occult	0	29	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
87	Lawful Good	Spiritual order promoting peace and wisdom.	The Radiant Monastery	Mystical/Occult	0	29	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
92	Neutral Evil	Raiders and scavengers exploiting harsh frontier planets.	The Dustborne	Nomadic/Frontier	0	39	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
98	Neutral Good	Peaceful voyagers navigating dangerous space for discovery.	Voidfarers	Nomadic/Frontier	0	52	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
104	Neutral Good	Scientists and explorers mapping unknown sectors.	Nebula Seekers	Nomadic/Frontier	0	52	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
105	Chaotic Neutral	Wanderers guided by fate and intuition, resisting structure.	Starborn Travelers	Nomadic/Frontier	0	52	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
1	Lawful Neutral	A highly structured empire valuing order and hierarchy above all, ruling multiple star systems with strict bureaucracy.	Celestian Imperium	Galactic Empire	0	12	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
48	Lawful Good	Alien diplomats striving for interstellar cooperation.	Thal’kari Conclave	Alien/Extraterrestrial	0	11	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
50	Chaotic Evil	Aggressive, expansionist species consuming planets indiscriminately.	Krelian Swarm	Alien/Extraterrestrial	0	22	t	2026-01-11 23:21:05.73115	2026-01-11 23:21:05.750002
106	Lawful Good	A pioneering alliance of the Sol, Alpha Centauri, and Proxima systems, the first multi-system attempt at interstellar governance, promoting diplomacy, cooperation, and shared defense.	The Tri-Systems	Galactic	100	15	f	2026-01-12 01:22:42.952052	2026-01-12 01:22:42.952052
107	Lawful Evil	A coalition of powerful interstellar corporations united to resist The Tri-Systems' influence. They are cold, militaristic, and prioritize profit over ethics, often exploiting slaves or workers under harsh conditions.	The Corporate Powers	Galactic	100	18	f	2026-01-12 01:29:32.260798	2026-01-12 01:29:32.260798
108	Chaotic Good	A covert resistance network composed mostly of former corporate citizens who escaped exploitation. The Union exposes The Powers through espionage, rebel newscasts, and supports uprisings in corporation-controlled systems.	The Union	Outlaw	100	38	f	2026-01-12 01:36:15.301826	2026-01-12 01:36:15.301826
\.



--
-- Data for Name: survey; Type: TABLE DATA; Schema: ud; Owner: starcreator_dev
--

COPY ud.survey (id, name, code, description, user_id, created_at, modified_at) FROM stdin;
1	Star Creator Survey	SCS	Basic system mapping survey	\N	2026-02-22 18:31:17.280775	2026-02-22 18:31:17.280775
\.



--
-- Name: asteroid_type_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.asteroid_type_id_seq', 13, true);



--
-- Name: atmosphere_template_component_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.atmosphere_template_component_id_seq', 160, true);



--
-- Name: atmosphere_template_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.atmosphere_template_id_seq', 40, true);



--
-- Name: belt_type_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.belt_type_id_seq', 4, true);



--
-- Name: cloud_composition_template_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.cloud_composition_template_id_seq', 26, true);



--
-- Name: composition_template_component_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.composition_template_component_id_seq', 452, true);



--
-- Name: composition_template_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.composition_template_id_seq', 88, true);



--
-- Name: geological_template_feature_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.geological_template_feature_id_seq', 176, true);



--
-- Name: geological_template_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.geological_template_id_seq', 39, true);



--
-- Name: moon_type_ref_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.moon_type_ref_id_seq', 7, true);



--
-- Name: name_origin_ref_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.name_origin_ref_id_seq', 29, true);



--
-- Name: name_ref_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.name_ref_id_seq', 340, true);



--
-- Name: name_region_ref_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.name_region_ref_id_seq', 13, true);



--
-- Name: planet_atmosphere_compatibility_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.planet_atmosphere_compatibility_id_seq', 90, true);



--
-- Name: planet_type_ref_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.planet_type_ref_id_seq', 39, true);



--
-- Name: precipitation_template_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.precipitation_template_id_seq', 15, true);



--
-- Name: ring_template_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.ring_template_id_seq', 4, true);



--
-- Name: star_type_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.star_type_id_seq', 17, true);



--
-- Name: terrain_category_ref_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.terrain_category_ref_id_seq', 10, true);



--
-- Name: terrain_type_ref_id_seq; Type: SEQUENCE SET; Schema: ref; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ref.terrain_type_ref_id_seq', 103, true);



--
-- Name: asteroid_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.asteroid_id_seq', 2263, true);



--
-- Name: atmosphere_component_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.atmosphere_component_id_seq', 19474, true);



--
-- Name: atmosphere_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.atmosphere_id_seq', 20279, true);



--
-- Name: composition_properties_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.composition_properties_id_seq', 22130, true);



--
-- Name: designation_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.designation_id_seq', 27146, true);



--
-- Name: faction_presence_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.faction_presence_id_seq', 1, false);



--
-- Name: moon_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.moon_id_seq', 15144, true);



--
-- Name: orbital_band_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.orbital_band_id_seq', 2226, true);



--
-- Name: orbital_elements_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.orbital_elements_id_seq', 27943, true);



--
-- Name: physical_properties_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.physical_properties_id_seq', 23893, true);



--
-- Name: planet_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.planet_id_seq', 5126, true);



--
-- Name: planet_terrain_distribution_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.planet_terrain_distribution_id_seq', 17571, true);



--
-- Name: planetary_magnetic_field_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.planetary_magnetic_field_id_seq', 14950, true);



--
-- Name: rotation_properties_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.rotation_properties_id_seq', 22531, true);



--
-- Name: sector_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.sector_id_seq', 1025, true);



--
-- Name: star_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.star_id_seq', 1363, true);



--
-- Name: star_system_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.star_system_id_seq', 1025, true);



--
-- Name: survey_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.survey_id_seq', 1, true);



--
-- Name: terrain_properties_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.terrain_properties_id_seq', 22129, true);



--
-- Name: hydrology_properties_id_seq; Type: SEQUENCE SET; Schema: ud; Owner: starcreator_dev
--

SELECT pg_catalog.setval('ud.hydrology_properties_id_seq', 17785, true);



--
-- Name: asteroid_type asteroid_type_code_key; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.asteroid_type
    ADD CONSTRAINT asteroid_type_code_key UNIQUE (code);



--
-- Name: asteroid_type asteroid_type_name_key; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.asteroid_type
    ADD CONSTRAINT asteroid_type_name_key UNIQUE (name);



--
-- Name: asteroid_type asteroid_type_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.asteroid_type
    ADD CONSTRAINT asteroid_type_pkey PRIMARY KEY (id);



--
-- Name: atmosphere_template_component atmosphere_template_component_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.atmosphere_template_component
    ADD CONSTRAINT atmosphere_template_component_pkey PRIMARY KEY (id);



--
-- Name: atmosphere_template atmosphere_template_name_key; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.atmosphere_template
    ADD CONSTRAINT atmosphere_template_name_key UNIQUE (name);



--
-- Name: atmosphere_template atmosphere_template_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.atmosphere_template
    ADD CONSTRAINT atmosphere_template_pkey PRIMARY KEY (id);



--
-- Name: belt_type belt_type_code_key; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.belt_type
    ADD CONSTRAINT belt_type_code_key UNIQUE (code);



--
-- Name: belt_type belt_type_name_key; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.belt_type
    ADD CONSTRAINT belt_type_name_key UNIQUE (name);



--
-- Name: belt_type belt_type_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.belt_type
    ADD CONSTRAINT belt_type_pkey PRIMARY KEY (id);



--
-- Name: cloud_composition_template cloud_composition_template_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.cloud_composition_template
    ADD CONSTRAINT cloud_composition_template_pkey PRIMARY KEY (id);



--
-- Name: composition_template_component composition_template_component_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.composition_template_component
    ADD CONSTRAINT composition_template_component_pkey PRIMARY KEY (id);



--
-- Name: composition_template composition_template_name_key; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.composition_template
    ADD CONSTRAINT composition_template_name_key UNIQUE (name);



--
-- Name: composition_template composition_template_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.composition_template
    ADD CONSTRAINT composition_template_pkey PRIMARY KEY (id);



--
-- Name: geological_template_feature geological_template_feature_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.geological_template_feature
    ADD CONSTRAINT geological_template_feature_pkey PRIMARY KEY (id);



--
-- Name: geological_template geological_template_name_key; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.geological_template
    ADD CONSTRAINT geological_template_name_key UNIQUE (name);



--
-- Name: geological_template geological_template_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.geological_template
    ADD CONSTRAINT geological_template_pkey PRIMARY KEY (id);



--
-- Name: government_type government_type_pk; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.government_type
    ADD CONSTRAINT government_type_pk PRIMARY KEY (id);



--
-- Name: moon_type_ref moon_type_ref_moon_type_key; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.moon_type_ref
    ADD CONSTRAINT moon_type_ref_moon_type_key UNIQUE (moon_type);



--
-- Name: moon_type_ref moon_type_ref_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.moon_type_ref
    ADD CONSTRAINT moon_type_ref_pkey PRIMARY KEY (id);



--
-- Name: name_origin_mapping name_origin_mapping_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.name_origin_mapping
    ADD CONSTRAINT name_origin_mapping_pkey PRIMARY KEY (name_id, origin_id);



--
-- Name: name_origin_ref name_origin_ref_name_key; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.name_origin_ref
    ADD CONSTRAINT name_origin_ref_name_key UNIQUE (name);



--
-- Name: name_origin_ref name_origin_ref_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.name_origin_ref
    ADD CONSTRAINT name_origin_ref_pkey PRIMARY KEY (id);



--
-- Name: name_ref name_ref_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.name_ref
    ADD CONSTRAINT name_ref_pkey PRIMARY KEY (id);



--
-- Name: name_region_ref name_region_ref_name_key; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.name_region_ref
    ADD CONSTRAINT name_region_ref_name_key UNIQUE (name);



--
-- Name: name_region_ref name_region_ref_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.name_region_ref
    ADD CONSTRAINT name_region_ref_pkey PRIMARY KEY (id);



--
-- Name: origin_region_mapping origin_region_mapping_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.origin_region_mapping
    ADD CONSTRAINT origin_region_mapping_pkey PRIMARY KEY (origin_id, region_id);



--
-- Name: planet_atmosphere_compatibility planet_atmosphere_compatibili_planet_type_atmosphere_classi_key; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.planet_atmosphere_compatibility
    ADD CONSTRAINT planet_atmosphere_compatibili_planet_type_atmosphere_classi_key UNIQUE (planet_type, atmosphere_classification);



--
-- Name: planet_atmosphere_compatibility planet_atmosphere_compatibility_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.planet_atmosphere_compatibility
    ADD CONSTRAINT planet_atmosphere_compatibility_pkey PRIMARY KEY (id);



--
-- Name: planet_type_ref planet_type_ref_name_key; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.planet_type_ref
    ADD CONSTRAINT planet_type_ref_name_key UNIQUE (name);



--
-- Name: planet_type_ref planet_type_ref_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.planet_type_ref
    ADD CONSTRAINT planet_type_ref_pkey PRIMARY KEY (id);



--
-- Name: precipitation_template precipitation_template_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.precipitation_template
    ADD CONSTRAINT precipitation_template_pkey PRIMARY KEY (id);



--
-- Name: ring_template ring_template_name_key; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.ring_template
    ADD CONSTRAINT ring_template_name_key UNIQUE (name);



--
-- Name: ring_template ring_template_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.ring_template
    ADD CONSTRAINT ring_template_pkey PRIMARY KEY (id);



--
-- Name: star_type star_type_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.star_type
    ADD CONSTRAINT star_type_pkey PRIMARY KEY (id);



--
-- Name: terrain_category_ref terrain_category_ref_category_key; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.terrain_category_ref
    ADD CONSTRAINT terrain_category_ref_category_key UNIQUE (category);



--
-- Name: terrain_category_ref terrain_category_ref_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.terrain_category_ref
    ADD CONSTRAINT terrain_category_ref_pkey PRIMARY KEY (id);



--
-- Name: terrain_type_ref terrain_type_ref_name_key; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.terrain_type_ref
    ADD CONSTRAINT terrain_type_ref_name_key UNIQUE (name);



--
-- Name: terrain_type_ref terrain_type_ref_pkey; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.terrain_type_ref
    ADD CONSTRAINT terrain_type_ref_pkey PRIMARY KEY (id);



--
-- Name: atmosphere_template_component unique_gas_per_template; Type: CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.atmosphere_template_component
    ADD CONSTRAINT unique_gas_per_template UNIQUE (template_id, gas_formula);



--
-- Name: asteroid asteroid_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.asteroid
    ADD CONSTRAINT asteroid_pkey PRIMARY KEY (id);



--
-- Name: atmosphere_component atmosphere_component_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.atmosphere_component
    ADD CONSTRAINT atmosphere_component_pkey PRIMARY KEY (id);



--
-- Name: atmosphere atmosphere_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.atmosphere
    ADD CONSTRAINT atmosphere_pkey PRIMARY KEY (id);



--
-- Name: band_dwarf_planet band_dwarf_planet_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.band_dwarf_planet
    ADD CONSTRAINT band_dwarf_planet_pkey PRIMARY KEY (band_id, planet_id);



--
-- Name: composition_properties composition_properties_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.composition_properties
    ADD CONSTRAINT composition_properties_pkey PRIMARY KEY (id);



--
-- Name: designation designation_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.designation
    ADD CONSTRAINT designation_pkey PRIMARY KEY (id);



--
-- Name: factions faction_pk; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.factions
    ADD CONSTRAINT faction_pk UNIQUE (id);



--
-- Name: faction_presence faction_presence_faction_id_system_id_key; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.faction_presence
    ADD CONSTRAINT faction_presence_faction_id_system_id_key UNIQUE (faction_id, system_id);



--
-- Name: faction_presence faction_presence_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.faction_presence
    ADD CONSTRAINT faction_presence_pkey PRIMARY KEY (id);



--
-- Name: moon moon_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.moon
    ADD CONSTRAINT moon_pkey PRIMARY KEY (id);



--
-- Name: orbital_band orbital_band_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.orbital_band
    ADD CONSTRAINT orbital_band_pkey PRIMARY KEY (id);



--
-- Name: orbital_elements orbital_elements_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.orbital_elements
    ADD CONSTRAINT orbital_elements_pkey PRIMARY KEY (id);



--
-- Name: physical_properties physical_properties_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.physical_properties
    ADD CONSTRAINT physical_properties_pkey PRIMARY KEY (id);



--
-- Name: planet planet_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.planet
    ADD CONSTRAINT planet_pkey PRIMARY KEY (id);



--
-- Name: terrain_distribution planet_terrain_distribution_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.terrain_distribution
    ADD CONSTRAINT planet_terrain_distribution_pkey PRIMARY KEY (id);



--
-- Name: planetary_magnetic_field planetary_magnetic_field_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.planetary_magnetic_field
    ADD CONSTRAINT planetary_magnetic_field_pkey PRIMARY KEY (id);



--
-- Name: rotation_properties rotation_properties_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.rotation_properties
    ADD CONSTRAINT rotation_properties_pkey PRIMARY KEY (id);



--
-- Name: sector sector_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.sector
    ADD CONSTRAINT sector_pkey PRIMARY KEY (id);



--
-- Name: sector sector_x_y_z_key; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.sector
    ADD CONSTRAINT sector_x_y_z_key UNIQUE (x, y, z);



--
-- Name: star star_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.star
    ADD CONSTRAINT star_pkey PRIMARY KEY (id);



--
-- Name: star_system star_system_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.star_system
    ADD CONSTRAINT star_system_pkey PRIMARY KEY (id);



--
-- Name: survey survey_code_key; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.survey
    ADD CONSTRAINT survey_code_key UNIQUE (code);



--
-- Name: survey survey_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.survey
    ADD CONSTRAINT survey_pkey PRIMARY KEY (id);



--
-- Name: terrain_properties terrain_properties_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.terrain_properties
    ADD CONSTRAINT terrain_properties_pkey PRIMARY KEY (id);



--
-- Name: faction_presence uk9p4eumavcy7v2v01ledwg228w; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.faction_presence
    ADD CONSTRAINT uk9p4eumavcy7v2v01ledwg228w UNIQUE (faction_id, system_id);



--
-- Name: hydrology_properties hydrology_properties_pkey; Type: CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.hydrology_properties
    ADD CONSTRAINT hydrology_properties_pkey PRIMARY KEY (id);



--
-- Name: idx_atm_template_classification; Type: INDEX; Schema: ref; Owner: starcreator_dev
--

CREATE INDEX idx_atm_template_classification ON ref.atmosphere_template USING btree (classification);



--
-- Name: idx_atm_template_component; Type: INDEX; Schema: ref; Owner: starcreator_dev
--

CREATE INDEX idx_atm_template_component ON ref.atmosphere_template_component USING btree (template_id);



--
-- Name: idx_composition_template_classification; Type: INDEX; Schema: ref; Owner: starcreator_dev
--

CREATE INDEX idx_composition_template_classification ON ref.composition_template USING btree (classification);



--
-- Name: idx_composition_template_component; Type: INDEX; Schema: ref; Owner: starcreator_dev
--

CREATE INDEX idx_composition_template_component ON ref.composition_template_component USING btree (template_id);



--
-- Name: idx_geological_template_activity; Type: INDEX; Schema: ref; Owner: starcreator_dev
--

CREATE INDEX idx_geological_template_activity ON ref.geological_template USING btree (activity_level);



--
-- Name: idx_geological_template_feature; Type: INDEX; Schema: ref; Owner: starcreator_dev
--

CREATE INDEX idx_geological_template_feature ON ref.geological_template_feature USING btree (template_id);



--
-- Name: idx_name_origin_mapping_origin; Type: INDEX; Schema: ref; Owner: starcreator_dev
--

CREATE INDEX idx_name_origin_mapping_origin ON ref.name_origin_mapping USING btree (origin_id);



--
-- Name: idx_name_ref_gender; Type: INDEX; Schema: ref; Owner: starcreator_dev
--

CREATE INDEX idx_name_ref_gender ON ref.name_ref USING btree (gender);



--
-- Name: idx_name_ref_is_first; Type: INDEX; Schema: ref; Owner: starcreator_dev
--

CREATE INDEX idx_name_ref_is_first ON ref.name_ref USING btree (is_first);



--
-- Name: idx_name_ref_is_last; Type: INDEX; Schema: ref; Owner: starcreator_dev
--

CREATE INDEX idx_name_ref_is_last ON ref.name_ref USING btree (is_last);



--
-- Name: idx_name_ref_popularity; Type: INDEX; Schema: ref; Owner: starcreator_dev
--

CREATE INDEX idx_name_ref_popularity ON ref.name_ref USING btree (popularity);



--
-- Name: idx_origin_region_mapping_region; Type: INDEX; Schema: ref; Owner: starcreator_dev
--

CREATE INDEX idx_origin_region_mapping_region ON ref.origin_region_mapping USING btree (region_id);



--
-- Name: idx_planet_type_formation_zone; Type: INDEX; Schema: ref; Owner: starcreator_dev
--

CREATE INDEX idx_planet_type_formation_zone ON ref.planet_type_ref USING btree (formation_zone);



--
-- Name: idx_planet_type_habitable; Type: INDEX; Schema: ref; Owner: starcreator_dev
--

CREATE INDEX idx_planet_type_habitable ON ref.planet_type_ref USING btree (habitable) WHERE (habitable = true);



--
-- Name: idx_ring_template_planet_types; Type: INDEX; Schema: ref; Owner: starcreator_dev
--

CREATE INDEX idx_ring_template_planet_types ON ref.ring_template USING btree (planet_types);



--
-- Name: idx_ring_template_ring_type; Type: INDEX; Schema: ref; Owner: starcreator_dev
--

CREATE INDEX idx_ring_template_ring_type ON ref.ring_template USING btree (ring_type);



--
-- Name: idx_terrain_category_major; Type: INDEX; Schema: ref; Owner: starcreator_dev
--

CREATE INDEX idx_terrain_category_major ON ref.terrain_category_ref USING btree (is_major_terrain);



--
-- Name: idx_terrain_category_rare; Type: INDEX; Schema: ref; Owner: starcreator_dev
--

CREATE INDEX idx_terrain_category_rare ON ref.terrain_category_ref USING btree (is_rare);



--
-- Name: idx_terrain_type_category; Type: INDEX; Schema: ref; Owner: starcreator_dev
--

CREATE INDEX idx_terrain_type_category ON ref.terrain_type_ref USING btree (category);



--
-- Name: idx_asteroid_designation; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_asteroid_designation ON ud.asteroid USING btree (designation_id);



--
-- Name: idx_asteroid_physical_properties; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_asteroid_physical_properties ON ud.asteroid USING btree (physical_properties_id);



--
-- Name: idx_asteroid_rotation_properties; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_asteroid_rotation_properties ON ud.asteroid USING btree (rotation_properties_id);



--
-- Name: idx_asteroid_type_id; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_asteroid_type_id ON ud.asteroid USING btree (asteroid_type_id);



--
-- Name: idx_atmosphere_component_atmosphere_id; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_atmosphere_component_atmosphere_id ON ud.atmosphere_component USING btree (atmosphere_id);



--
-- Name: idx_atmosphere_component_gas; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_atmosphere_component_gas ON ud.atmosphere_component USING btree (gas_formula);



--
-- Name: idx_faction_presence_faction; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_faction_presence_faction ON ud.faction_presence USING btree (faction_id);



--
-- Name: idx_faction_presence_system; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_faction_presence_system ON ud.faction_presence USING btree (system_id);



--
-- Name: idx_mag_field_dynamo_type; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_mag_field_dynamo_type ON ud.planetary_magnetic_field USING btree (dynamo_type);



--
-- Name: idx_mag_field_has_auroras; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_mag_field_has_auroras ON ud.planetary_magnetic_field USING btree (has_auroras);



--
-- Name: idx_mag_field_has_reversals; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_mag_field_has_reversals ON ud.planetary_magnetic_field USING btree (has_reversals);



--
-- Name: idx_mag_field_protection_level; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_mag_field_protection_level ON ud.planetary_magnetic_field USING btree (protection_level);



--
-- Name: idx_moon_atmosphere_id; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_moon_atmosphere_id ON ud.moon USING btree (atmosphere_id);



--
-- Name: idx_moon_composition_properties; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_moon_composition_properties ON ud.moon USING btree (composition_properties_id);



--
-- Name: idx_moon_designation; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_moon_designation ON ud.moon USING btree (designation_id);



--
-- Name: idx_moon_magnetic_field_id; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_moon_magnetic_field_id ON ud.moon USING btree (magnetic_field_id);



--
-- Name: idx_moon_physical_properties; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_moon_physical_properties ON ud.moon USING btree (physical_properties_id);



--
-- Name: idx_moon_planet_id; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_moon_planet_id ON ud.moon USING btree (planet_id);



--
-- Name: idx_moon_rotation_properties; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_moon_rotation_properties ON ud.moon USING btree (rotation_properties_id);



--
-- Name: idx_orbital_band_designation; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_orbital_band_designation ON ud.orbital_band USING btree (designation_id);



--
-- Name: idx_orbital_band_planet; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_orbital_band_planet ON ud.orbital_band USING btree (planet_id) WHERE (planet_id IS NOT NULL);



--
-- Name: idx_orbital_band_star; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_orbital_band_star ON ud.orbital_band USING btree (star_id) WHERE (star_id IS NOT NULL);



--
-- Name: idx_planet_atmosphere_id; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_planet_atmosphere_id ON ud.planet USING btree (atmosphere_id);



--
-- Name: idx_planet_composition_properties; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_planet_composition_properties ON ud.planet USING btree (composition_properties_id);



--
-- Name: idx_planet_created_at; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_planet_created_at ON ud.planet USING btree (created_at);



--
-- Name: idx_planet_designation; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_planet_designation ON ud.planet USING btree (designation_id);



--
-- Name: idx_planet_magnetic_field_id; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_planet_magnetic_field_id ON ud.planet USING btree (magnetic_field_id);



--
-- Name: idx_planet_orbital_position; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_planet_orbital_position ON ud.planet USING btree (star_id, orbital_position);



--
-- Name: idx_planet_physical_properties; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_planet_physical_properties ON ud.planet USING btree (physical_properties_id);



--
-- Name: idx_planet_rotation_properties; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_planet_rotation_properties ON ud.planet USING btree (rotation_properties_id);



--
-- Name: idx_planet_star_id; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_planet_star_id ON ud.planet USING btree (star_id);



--
-- Name: idx_sector_coordinates; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_sector_coordinates ON ud.sector USING btree (x, y, z);



--
-- Name: idx_sector_survey; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_sector_survey ON ud.sector USING btree (survey_id);



--
-- Name: idx_star_activity_level; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_star_activity_level ON ud.star USING btree (activity_level);



--
-- Name: idx_star_designation; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_star_designation ON ud.star USING btree (designation_id);



--
-- Name: idx_star_flare_class; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_star_flare_class ON ud.star USING btree (flare_class);



--
-- Name: idx_star_habitable_zone; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_star_habitable_zone ON ud.star USING btree (habitable_zone_inner_au, habitable_zone_outer_au);



--
-- Name: idx_star_in_grand_minimum; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_star_in_grand_minimum ON ud.star USING btree (in_grand_minimum);



--
-- Name: idx_star_is_variable; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_star_is_variable ON ud.star USING btree (is_variable);



--
-- Name: idx_star_orbital_elements; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_star_orbital_elements ON ud.star USING btree (orbital_elements_id);



--
-- Name: idx_star_physical_properties; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_star_physical_properties ON ud.star USING btree (physical_properties_id);



--
-- Name: idx_star_role; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_star_role ON ud.star USING btree (star_role);



--
-- Name: idx_star_system; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_star_system ON ud.star USING btree (system_id);



--
-- Name: idx_star_system_config; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_star_system_config ON ud.star_system USING btree (binary_configuration);



--
-- Name: idx_star_system_coordinates; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_star_system_coordinates ON ud.star_system USING btree (x, y, z);



--
-- Name: idx_star_system_designation; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_star_system_designation ON ud.star_system USING btree (designation_id);



--
-- Name: idx_star_system_sector; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_star_system_sector ON ud.star_system USING btree (sector_id);



--
-- Name: idx_survey_code; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE UNIQUE INDEX idx_survey_code ON ud.survey USING btree (code);



--
-- Name: idx_terrain_distribution_type; Type: INDEX; Schema: ud; Owner: starcreator_dev
--

CREATE INDEX idx_terrain_distribution_type ON ud.terrain_distribution USING btree (terrain_type_id);



--
-- Name: name_origin_ref update_name_origin_ref_modified_at; Type: TRIGGER; Schema: ref; Owner: starcreator_dev
--

CREATE TRIGGER update_name_origin_ref_modified_at BEFORE UPDATE ON ref.name_origin_ref FOR EACH ROW EXECUTE FUNCTION public.update_modified_at_column();



--
-- Name: name_ref update_name_ref_modified_at; Type: TRIGGER; Schema: ref; Owner: starcreator_dev
--

CREATE TRIGGER update_name_ref_modified_at BEFORE UPDATE ON ref.name_ref FOR EACH ROW EXECUTE FUNCTION public.update_modified_at_column();



--
-- Name: name_region_ref update_name_region_ref_modified_at; Type: TRIGGER; Schema: ref; Owner: starcreator_dev
--

CREATE TRIGGER update_name_region_ref_modified_at BEFORE UPDATE ON ref.name_region_ref FOR EACH ROW EXECUTE FUNCTION public.update_modified_at_column();



--
-- Name: atmosphere update_atmosphere_modified_at; Type: TRIGGER; Schema: ud; Owner: starcreator_dev
--

CREATE TRIGGER update_atmosphere_modified_at BEFORE UPDATE ON ud.atmosphere FOR EACH ROW EXECUTE FUNCTION public.update_modified_at_column();



--
-- Name: atmosphere_template_component atmosphere_template_component_template_id_fkey; Type: FK CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.atmosphere_template_component
    ADD CONSTRAINT atmosphere_template_component_template_id_fkey FOREIGN KEY (template_id) REFERENCES ref.atmosphere_template(id) ON DELETE CASCADE;



--
-- Name: composition_template_component composition_template_component_template_id_fkey; Type: FK CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.composition_template_component
    ADD CONSTRAINT composition_template_component_template_id_fkey FOREIGN KEY (template_id) REFERENCES ref.composition_template(id) ON DELETE CASCADE;



--
-- Name: name_origin_mapping fk_name_origin_mapping_name; Type: FK CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.name_origin_mapping
    ADD CONSTRAINT fk_name_origin_mapping_name FOREIGN KEY (name_id) REFERENCES ref.name_ref(id) ON DELETE CASCADE;



--
-- Name: name_origin_mapping fk_name_origin_mapping_origin; Type: FK CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.name_origin_mapping
    ADD CONSTRAINT fk_name_origin_mapping_origin FOREIGN KEY (origin_id) REFERENCES ref.name_origin_ref(id) ON DELETE CASCADE;



--
-- Name: origin_region_mapping fk_origin_region_mapping_origin; Type: FK CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.origin_region_mapping
    ADD CONSTRAINT fk_origin_region_mapping_origin FOREIGN KEY (origin_id) REFERENCES ref.name_origin_ref(id) ON DELETE CASCADE;



--
-- Name: origin_region_mapping fk_origin_region_mapping_region; Type: FK CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.origin_region_mapping
    ADD CONSTRAINT fk_origin_region_mapping_region FOREIGN KEY (region_id) REFERENCES ref.name_region_ref(id) ON DELETE CASCADE;



--
-- Name: geological_template_feature geological_template_feature_template_id_fkey; Type: FK CONSTRAINT; Schema: ref; Owner: starcreator_dev
--

ALTER TABLE ONLY ref.geological_template_feature
    ADD CONSTRAINT geological_template_feature_template_id_fkey FOREIGN KEY (template_id) REFERENCES ref.geological_template(id) ON DELETE CASCADE;



--
-- Name: asteroid asteroid_asteroid_type_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.asteroid
    ADD CONSTRAINT asteroid_asteroid_type_id_fkey FOREIGN KEY (asteroid_type_id) REFERENCES ref.asteroid_type(id);



--
-- Name: asteroid asteroid_band_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.asteroid
    ADD CONSTRAINT asteroid_band_id_fkey FOREIGN KEY (band_id) REFERENCES ud.orbital_band(id) ON DELETE SET NULL;



--
-- Name: asteroid asteroid_designation_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.asteroid
    ADD CONSTRAINT asteroid_designation_id_fkey FOREIGN KEY (designation_id) REFERENCES ud.designation(id) ON DELETE SET NULL;



--
-- Name: asteroid asteroid_physical_properties_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.asteroid
    ADD CONSTRAINT asteroid_physical_properties_id_fkey FOREIGN KEY (physical_properties_id) REFERENCES ud.physical_properties(id) ON DELETE SET NULL;



--
-- Name: asteroid asteroid_rotation_properties_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.asteroid
    ADD CONSTRAINT asteroid_rotation_properties_id_fkey FOREIGN KEY (rotation_properties_id) REFERENCES ud.rotation_properties(id) ON DELETE SET NULL;



--
-- Name: atmosphere_component atmosphere_component_atmosphere_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.atmosphere_component
    ADD CONSTRAINT atmosphere_component_atmosphere_id_fkey FOREIGN KEY (atmosphere_id) REFERENCES ud.atmosphere(id) ON DELETE CASCADE;



--
-- Name: band_dwarf_planet band_dwarf_planet_band_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.band_dwarf_planet
    ADD CONSTRAINT band_dwarf_planet_band_id_fkey FOREIGN KEY (band_id) REFERENCES ud.orbital_band(id) ON DELETE CASCADE;



--
-- Name: factions factions_government_type_id_fk; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.factions
    ADD CONSTRAINT factions_government_type_id_fk FOREIGN KEY (government_type) REFERENCES ref.government_type(id);



--
-- Name: orbital_band fk1gaw4xswtrk56k30e0evife3g; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.orbital_band
    ADD CONSTRAINT fk1gaw4xswtrk56k30e0evife3g FOREIGN KEY (planet_id) REFERENCES ud.planet(id);



--
-- Name: moon fk_moon_planet; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.moon
    ADD CONSTRAINT fk_moon_planet FOREIGN KEY (planet_id) REFERENCES ud.planet(id) ON DELETE CASCADE;



--
-- Name: orbital_band fk_orbital_band_planet; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.orbital_band
    ADD CONSTRAINT fk_orbital_band_planet FOREIGN KEY (planet_id) REFERENCES ud.planet(id) ON DELETE CASCADE;



--
-- Name: terrain_distribution fk_terrain_dist_terrain; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.terrain_distribution
    ADD CONSTRAINT fk_terrain_dist_terrain FOREIGN KEY (terrain_properties_id) REFERENCES ud.terrain_properties(id) ON DELETE CASCADE;



--
-- Name: band_dwarf_planet fkopvwdstmyxrqjs7qul0e39vyy; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.band_dwarf_planet
    ADD CONSTRAINT fkopvwdstmyxrqjs7qul0e39vyy FOREIGN KEY (planet_id) REFERENCES ud.planet(id);



--
-- Name: factions fkslqdxy6wf9yo0mn9eq7h4byu2; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.factions
    ADD CONSTRAINT fkslqdxy6wf9yo0mn9eq7h4byu2 FOREIGN KEY (government_type) REFERENCES ref.government_type(id);



--
-- Name: faction_presence fp_faction_fk; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.faction_presence
    ADD CONSTRAINT fp_faction_fk FOREIGN KEY (faction_id) REFERENCES ud.factions(id) ON DELETE CASCADE;



--
-- Name: faction_presence fp_system_fk; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.faction_presence
    ADD CONSTRAINT fp_system_fk FOREIGN KEY (system_id) REFERENCES ud.star_system(id) ON DELETE CASCADE;



--
-- Name: moon moon_atmosphere_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.moon
    ADD CONSTRAINT moon_atmosphere_id_fkey FOREIGN KEY (atmosphere_id) REFERENCES ud.atmosphere(id) ON DELETE SET NULL;



--
-- Name: moon moon_composition_properties_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.moon
    ADD CONSTRAINT moon_composition_properties_id_fkey FOREIGN KEY (composition_properties_id) REFERENCES ud.composition_properties(id) ON DELETE SET NULL;



--
-- Name: moon moon_designation_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.moon
    ADD CONSTRAINT moon_designation_id_fkey FOREIGN KEY (designation_id) REFERENCES ud.designation(id) ON DELETE SET NULL;



--
-- Name: moon moon_magnetic_field_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.moon
    ADD CONSTRAINT moon_magnetic_field_id_fkey FOREIGN KEY (magnetic_field_id) REFERENCES ud.planetary_magnetic_field(id) ON DELETE SET NULL;



--
-- Name: moon moon_orbital_elements_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.moon
    ADD CONSTRAINT moon_orbital_elements_id_fkey FOREIGN KEY (orbital_elements_id) REFERENCES ud.orbital_elements(id) ON DELETE SET NULL;



--
-- Name: moon moon_physical_properties_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.moon
    ADD CONSTRAINT moon_physical_properties_id_fkey FOREIGN KEY (physical_properties_id) REFERENCES ud.physical_properties(id) ON DELETE SET NULL;



--
-- Name: moon moon_rotation_properties_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.moon
    ADD CONSTRAINT moon_rotation_properties_id_fkey FOREIGN KEY (rotation_properties_id) REFERENCES ud.rotation_properties(id) ON DELETE SET NULL;



--
-- Name: moon moon_terrain_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.moon
    ADD CONSTRAINT moon_terrain_id_fkey FOREIGN KEY (terrain_id) REFERENCES ud.terrain_properties(id) ON DELETE SET NULL;



--
-- Name: moon moon_hydrology_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.moon
    ADD CONSTRAINT moon_hydrology_id_fkey FOREIGN KEY (hydrology_id) REFERENCES ud.hydrology_properties(id);



--
-- Name: orbital_band orbital_band_belt_type_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.orbital_band
    ADD CONSTRAINT orbital_band_belt_type_id_fkey FOREIGN KEY (belt_type_id) REFERENCES ref.belt_type(id);



--
-- Name: orbital_band orbital_band_designation_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.orbital_band
    ADD CONSTRAINT orbital_band_designation_id_fkey FOREIGN KEY (designation_id) REFERENCES ud.designation(id) ON DELETE SET NULL;



--
-- Name: orbital_band orbital_band_inner_orbit_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.orbital_band
    ADD CONSTRAINT orbital_band_inner_orbit_id_fkey FOREIGN KEY (inner_orbit_id) REFERENCES ud.orbital_elements(id) ON DELETE CASCADE;



--
-- Name: orbital_band orbital_band_outer_orbit_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.orbital_band
    ADD CONSTRAINT orbital_band_outer_orbit_id_fkey FOREIGN KEY (outer_orbit_id) REFERENCES ud.orbital_elements(id) ON DELETE CASCADE;



--
-- Name: orbital_band orbital_band_star_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.orbital_band
    ADD CONSTRAINT orbital_band_star_id_fkey FOREIGN KEY (star_id) REFERENCES ud.star(id) ON DELETE CASCADE;



--
-- Name: planet planet_atmosphere_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.planet
    ADD CONSTRAINT planet_atmosphere_id_fkey FOREIGN KEY (atmosphere_id) REFERENCES ud.atmosphere(id) ON DELETE SET NULL;



--
-- Name: planet planet_composition_properties_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.planet
    ADD CONSTRAINT planet_composition_properties_id_fkey FOREIGN KEY (composition_properties_id) REFERENCES ud.composition_properties(id) ON DELETE SET NULL;



--
-- Name: planet planet_designation_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.planet
    ADD CONSTRAINT planet_designation_id_fkey FOREIGN KEY (designation_id) REFERENCES ud.designation(id) ON DELETE SET NULL;



--
-- Name: planet planet_magnetic_field_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.planet
    ADD CONSTRAINT planet_magnetic_field_id_fkey FOREIGN KEY (magnetic_field_id) REFERENCES ud.planetary_magnetic_field(id) ON DELETE SET NULL;



--
-- Name: planet planet_orbital_elements_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.planet
    ADD CONSTRAINT planet_orbital_elements_id_fkey FOREIGN KEY (orbital_elements_id) REFERENCES ud.orbital_elements(id) ON DELETE SET NULL;



--
-- Name: planet planet_physical_properties_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.planet
    ADD CONSTRAINT planet_physical_properties_id_fkey FOREIGN KEY (physical_properties_id) REFERENCES ud.physical_properties(id) ON DELETE SET NULL;



--
-- Name: planet planet_rotation_properties_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.planet
    ADD CONSTRAINT planet_rotation_properties_id_fkey FOREIGN KEY (rotation_properties_id) REFERENCES ud.rotation_properties(id) ON DELETE SET NULL;



--
-- Name: planet planet_star_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.planet
    ADD CONSTRAINT planet_star_id_fkey FOREIGN KEY (star_id) REFERENCES ud.star(id) ON DELETE CASCADE;



--
-- Name: terrain_distribution planet_terrain_distribution_terrain_type_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.terrain_distribution
    ADD CONSTRAINT planet_terrain_distribution_terrain_type_id_fkey FOREIGN KEY (terrain_type_id) REFERENCES ref.terrain_type_ref(id);



--
-- Name: planet planet_terrain_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.planet
    ADD CONSTRAINT planet_terrain_id_fkey FOREIGN KEY (terrain_id) REFERENCES ud.terrain_properties(id) ON DELETE SET NULL;



--
-- Name: planet planet_hydrology_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.planet
    ADD CONSTRAINT planet_hydrology_id_fkey FOREIGN KEY (hydrology_id) REFERENCES ud.hydrology_properties(id);



--
-- Name: sector sector_survey_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.sector
    ADD CONSTRAINT sector_survey_id_fkey FOREIGN KEY (survey_id) REFERENCES ud.survey(id) ON DELETE SET NULL;



--
-- Name: star star_designation_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.star
    ADD CONSTRAINT star_designation_id_fkey FOREIGN KEY (designation_id) REFERENCES ud.designation(id) ON DELETE SET NULL;



--
-- Name: star star_orbital_elements_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.star
    ADD CONSTRAINT star_orbital_elements_id_fkey FOREIGN KEY (orbital_elements_id) REFERENCES ud.orbital_elements(id) ON DELETE SET NULL;



--
-- Name: star star_physical_properties_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.star
    ADD CONSTRAINT star_physical_properties_id_fkey FOREIGN KEY (physical_properties_id) REFERENCES ud.physical_properties(id) ON DELETE SET NULL;



--
-- Name: star_system star_system_designation_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.star_system
    ADD CONSTRAINT star_system_designation_id_fkey FOREIGN KEY (designation_id) REFERENCES ud.designation(id) ON DELETE SET NULL;



--
-- Name: star star_system_id_fkey; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.star
    ADD CONSTRAINT star_system_id_fkey FOREIGN KEY (system_id) REFERENCES ud.star_system(id) ON DELETE CASCADE;



--
-- Name: star_system star_system_sector_fk; Type: FK CONSTRAINT; Schema: ud; Owner: starcreator_dev
--

ALTER TABLE ONLY ud.star_system
    ADD CONSTRAINT star_system_sector_fk FOREIGN KEY (sector_id) REFERENCES ud.sector(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--




-- ═══════════════════════════════════════════════════════════════════
-- Post-migration data updates (V116-V121 changes applied to seed data)
-- ═══════════════════════════════════════════════════════════════════

-- V116: Geological template composition filters
UPDATE ref.geological_template SET composition_types = 'ROCKY,MIXED'
WHERE name = 'Moon - Active Rocky';
UPDATE ref.geological_template SET composition_types = 'ROCKY,MIXED'
WHERE name = 'Moon - Hyperactive Volcanic';
UPDATE ref.geological_template SET composition_types = 'ICY,MIXED'
WHERE name = 'Moon - Cryovolcanic';
UPDATE ref.geological_template SET composition_types = 'ICY,MIXED'
WHERE name = 'Moon - Hyperactive Cryovolcanic';

-- V117: Fix ocean planet generation weights/temps
UPDATE ref.planet_type_ref
SET min_formation_temp_k = 220, max_formation_temp_k = 370, rarity_weight = 200
WHERE name = 'Ocean Planet';
UPDATE ref.planet_type_ref SET rarity_weight = 280 WHERE name = 'Super-Earth';
UPDATE ref.planet_type_ref SET rarity_weight = 300 WHERE name = 'Terrestrial Planet';

-- V118: Narrow existing dwarf ice template
UPDATE ref.composition_template SET max_surface_temp_k = 100
WHERE name = 'Dwarf - Ice-Rock Mix';

-- V118: New dwarf composition templates
INSERT INTO ref.composition_template
    (name, classification, planet_types, min_surface_temp_k, max_surface_temp_k, rarity_weight)
VALUES
    ('Dwarf - Mixed Silicate-Ice', 'MIXED_SILICATE_ICE', 'Dwarf Planet', 60, 180, 100),
    ('Dwarf - Rocky', 'SILICATE_RICH', 'Dwarf Planet', 100, 500, 100);

INSERT INTO ref.composition_template_component
    (template_id, mineral, layer_type, min_percentage, max_percentage)
VALUES
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Mixed Silicate-Ice'), 'OLIVINE',    'INTERIOR', 35, 50),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Mixed Silicate-Ice'), 'PYROXENE',   'INTERIOR', 15, 25),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Mixed Silicate-Ice'), 'WATER_ICE',  'INTERIOR', 20, 35),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Mixed Silicate-Ice'), 'IRON',       'INTERIOR',  5, 15),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Mixed Silicate-Ice'), 'REGOLITH',   'ENVELOPE', 40, 60),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Mixed Silicate-Ice'), 'WATER_ICE',  'ENVELOPE', 20, 35),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Mixed Silicate-Ice'), 'OLIVINE',    'ENVELOPE', 10, 25),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Rocky'), 'OLIVINE',    'INTERIOR', 35, 50),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Rocky'), 'PYROXENE',   'INTERIOR', 20, 30),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Rocky'), 'IRON',       'INTERIOR', 15, 25),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Rocky'), 'FELDSPAR',   'INTERIOR',  5, 15),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Rocky'), 'REGOLITH',   'ENVELOPE', 50, 70),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Rocky'), 'OLIVINE',    'ENVELOPE', 15, 30),
    ((SELECT id FROM ref.composition_template WHERE name = 'Dwarf - Rocky'), 'PYROXENE',   'ENVELOPE', 10, 20);

-- V119: Tag existing terrains with volatile_type
UPDATE ref.terrain_type_ref SET volatile_type = 'METHANE' WHERE name = 'METHANE_LAKES';
UPDATE ref.terrain_type_ref SET volatile_type = 'METHANE_ETHANE' WHERE name = 'HYDROCARBON_SEAS';
UPDATE ref.terrain_type_ref SET volatile_type = 'AMMONIA' WHERE name = 'AMMONIA_SEAS';
UPDATE ref.terrain_type_ref SET volatile_type = 'WATER'
WHERE category = 'AQUATIC' AND volatile_type IS NULL;

-- V119: New liquid-type-specific terrain types
INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_liquid, requires_atmosphere,
 min_temperature_k, max_temperature_k, is_aquatic, rarity_weight,
 typical_coverage_min, typical_coverage_max, cratering_weight_boost, volcanic_weight_boost, volatile_type)
SELECT *
FROM (VALUES
    ('METHANE_SEA', 'Methane Sea', 'Vast methane-ethane sea covering large surface areas', 'AQUATIC',
     true, false, 70, 112, true, 80, 10.0, 60.0, 0, 0, 'METHANE'),
    ('METHANE_COAST', 'Methane Coastline', 'Shoreline between methane seas and solid terrain', 'AQUATIC',
     true, false, 70, 112, true, 50, 2.0, 15.0, 0, 0, 'METHANE'),
    ('METHANE_RIVER', 'Methane River', 'Flowing methane channels carving through icy terrain', 'AQUATIC',
     true, true, 80, 112, true, 40, 1.0, 8.0, 0, 0, 'METHANE'),
    ('AMMONIA_OCEAN', 'Ammonia Ocean', 'Deep ammonia or ammonia-water ocean basin', 'AQUATIC',
     true, false, 195, 280, true, 70, 15.0, 70.0, 0, 0, 'AMMONIA_WATER'),
    ('AMMONIA_LAKE', 'Ammonia Lake', 'Ammonia-water lakes pooled in low-lying terrain', 'AQUATIC',
     true, false, 195, 280, true, 50, 2.0, 20.0, 0, 0, 'AMMONIA_WATER'),
    ('AMMONIA_WETLAND', 'Ammonia Wetland', 'Saturated terrain with ammonia-water seepage and frost', 'AQUATIC',
     true, true, 195, 280, true, 35, 1.0, 10.0, 0, 0, 'AMMONIA_WATER')
) AS v(name, display_name, description, category, requires_liquid, requires_atmosphere,
       min_temperature_k, max_temperature_k, is_aquatic, rarity_weight,
       typical_coverage_min, typical_coverage_max, cratering_weight_boost, volcanic_weight_boost, volatile_type)
WHERE NOT EXISTS (SELECT 1 FROM ref.terrain_type_ref t WHERE t.name = v.name);

-- V121: Ocean world terrain types
INSERT INTO ref.terrain_type_ref
(name, display_name, description, category, requires_liquid, requires_atmosphere,
 min_temperature_k, max_temperature_k, is_volcanic, is_aquatic, rarity_weight,
 typical_coverage_min, typical_coverage_max, cratering_weight_boost, volcanic_weight_boost)
SELECT v.*
FROM (VALUES
    ('VOLCANIC_ISLANDS'::varchar, 'Volcanic Islands'::varchar,
     'Shield volcano islands rising from ocean floor with active or recent eruptions'::text,
     'VOLCANIC'::varchar, true, false, 270::double precision, 373::double precision,
     true, false, 80, 2.0, 20.0, 0, 100),
    ('BASALT_PLATEAU'::varchar, 'Basalt Plateau'::varchar,
     'Elevated basaltic platforms formed by flood volcanism exposed above ocean surface'::text,
     'MOUNTAIN'::varchar, false, false, 100::double precision, 500::double precision,
     false, false, 90, 3.0, 25.0, 0, 50),
    ('BLACK_SAND_SHORES'::varchar, 'Black Sand Shores'::varchar,
     'Dark volcanic sand beaches and littoral zones surrounding volcanic land masses'::text,
     'AQUATIC'::varchar, true, false, 270::double precision, 373::double precision,
     false, true, 60, 1.0, 10.0, 0, 0),
    ('HYDROTHERMAL_VENTS'::varchar, 'Hydrothermal Vent Fields'::varchar,
     'Submarine volcanic vents releasing mineral-rich superheated fluid'::text,
     'VOLCANIC'::varchar, true, false, 270::double precision, 500::double precision,
     true, false, 50, 0.5, 8.0, 0, 120),
    ('SUBMARINE_RIDGE'::varchar, 'Submarine Ridge'::varchar,
     'Mid-ocean volcanic ridges and seamount chains partially breaching the surface'::text,
     'AQUATIC'::varchar, true, false, 100::double precision, 400::double precision,
     false, true, 70, 2.0, 15.0, 0, 60),
    ('VOLCANIC_ARCHIPELAGO'::varchar, 'Volcanic Archipelago'::varchar,
     'Chains of volcanic islands formed by hotspot or subduction activity'::text,
     'VOLCANIC'::varchar, true, true, 270::double precision, 373::double precision,
     true, false, 60, 3.0, 20.0, 0, 80),
    ('BARE_ROCK_COAST'::varchar, 'Bare Rock Coast'::varchar,
     'Exposed rock surfaces and cliff faces battered by waves devoid of soil'::text,
     'BARREN'::varchar, false, false, 100::double precision, 500::double precision,
     false, false, 80, 1.0, 15.0, 0, 0)
) AS v(name, display_name, description, category, requires_liquid, requires_atmosphere,
       min_temperature_k, max_temperature_k, is_volcanic, is_aquatic, rarity_weight,
       typical_coverage_min, typical_coverage_max, cratering_weight_boost, volcanic_weight_boost)
WHERE NOT EXISTS (SELECT 1 FROM ref.terrain_type_ref t WHERE t.name = v.name);

-- V121: Composition filtering for ocean world terrains
UPDATE ref.terrain_type_ref
SET required_composition_classes = ARRAY['OCEAN_WORLD']
WHERE name IN ('VOLCANIC_ISLANDS', 'VOLCANIC_ARCHIPELAGO', 'BLACK_SAND_SHORES',
               'HYDROTHERMAL_VENTS', 'SUBMARINE_RIDGE');

UPDATE ref.terrain_type_ref
SET excluded_composition_classes = ARRAY['ICE_RICH', 'IRON_RICH', 'GAS_ENVELOPE']
WHERE name IN ('BASALT_PLATEAU', 'BARE_ROCK_COAST');

UPDATE ref.terrain_type_ref
SET volatile_type = 'WATER'
WHERE name IN ('BLACK_SAND_SHORES', 'SUBMARINE_RIDGE')
  AND volatile_type IS NULL;

-- V121: Exclude continental biomes from ocean worlds
UPDATE ref.terrain_type_ref
SET excluded_composition_classes = COALESCE(excluded_composition_classes, ARRAY[]::text[]) || ARRAY['OCEAN_WORLD']
WHERE category = 'TEMPERATE'
  AND NOT ('OCEAN_WORLD' = ANY(COALESCE(excluded_composition_classes, ARRAY[]::text[])));

UPDATE ref.terrain_type_ref
SET excluded_composition_classes = COALESCE(excluded_composition_classes, ARRAY[]::text[]) || ARRAY['OCEAN_WORLD']
WHERE category = 'PLAINS'
  AND NOT ('OCEAN_WORLD' = ANY(COALESCE(excluded_composition_classes, ARRAY[]::text[])));

-- V121: Update existing ISLANDS terrain
UPDATE ref.terrain_type_ref
SET min_temperature_k = 70, max_temperature_k = 373, requires_atmosphere = false
WHERE name = 'ISLANDS';
