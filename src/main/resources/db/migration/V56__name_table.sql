-- ============================================
-- V56__name_generation_tables.sql
-- Name generation schema for character/NPC names
-- ============================================

-- =====================================================================
-- SECTION 1: Core names reference table
-- =====================================================================

CREATE TABLE ref.name_ref (
                              id BIGSERIAL PRIMARY KEY,
                              name VARCHAR(100) NOT NULL,
                              is_first BOOLEAN NOT NULL DEFAULT FALSE,
                              is_last BOOLEAN NOT NULL DEFAULT FALSE,
                              gender VARCHAR(20) NOT NULL DEFAULT 'unisex',
                              popularity INTEGER NOT NULL DEFAULT 1,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT name_ref_usage_chk CHECK (is_first OR is_last),
                              CONSTRAINT name_ref_popularity_chk CHECK (popularity > 0),
                              CONSTRAINT name_ref_gender_chk CHECK (gender IN ('male', 'female', 'unisex'))
);

COMMENT ON TABLE ref.name_ref IS 'Reference data for first and last names with cultural origins';
COMMENT ON COLUMN ref.name_ref.gender IS 'Gender association: male, female, or unisex';
COMMENT ON COLUMN ref.name_ref.popularity IS 'Weight for random selection (higher = more common)';

-- =====================================================================
-- SECTION 2: Origins reference table
-- =====================================================================

CREATE TABLE ref.name_origin_ref (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(100) NOT NULL UNIQUE,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE ref.name_origin_ref IS 'Cultural/linguistic origins for names (e.g., French, Yoruba)';

-- =====================================================================
-- SECTION 3: Regions reference table
-- =====================================================================

CREATE TABLE ref.name_region_ref (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(100) NOT NULL UNIQUE,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE ref.name_region_ref IS 'Geographic regions for grouping origins';

-- =====================================================================
-- SECTION 4: Name <-> Origin mapping (many-to-many)
-- =====================================================================

CREATE TABLE ref.name_origin_mapping (
                                         name_id BIGINT NOT NULL,
                                         origin_id BIGINT NOT NULL,

                                         PRIMARY KEY (name_id, origin_id),

                                         CONSTRAINT fk_name_origin_mapping_name
                                             FOREIGN KEY (name_id)
                                                 REFERENCES ref.name_ref (id)
                                                 ON DELETE CASCADE,

                                         CONSTRAINT fk_name_origin_mapping_origin
                                             FOREIGN KEY (origin_id)
                                                 REFERENCES ref.name_origin_ref (id)
                                                 ON DELETE CASCADE
);

COMMENT ON TABLE ref.name_origin_mapping IS 'Links names to their cultural origins';

-- =====================================================================
-- SECTION 5: Origin <-> Region mapping (many-to-many)
-- =====================================================================

CREATE TABLE ref.origin_region_mapping (
                                           origin_id BIGINT NOT NULL,
                                           region_id BIGINT NOT NULL,

                                           PRIMARY KEY (origin_id, region_id),

                                           CONSTRAINT fk_origin_region_mapping_origin
                                               FOREIGN KEY (origin_id)
                                                   REFERENCES ref.name_origin_ref (id)
                                                   ON DELETE CASCADE,

                                           CONSTRAINT fk_origin_region_mapping_region
                                               FOREIGN KEY (region_id)
                                                   REFERENCES ref.name_region_ref (id)
                                                   ON DELETE CASCADE
);

COMMENT ON TABLE ref.origin_region_mapping IS 'Links cultural origins to geographic regions';

-- =====================================================================
-- SECTION 6: Indexes for query performance
-- =====================================================================

CREATE INDEX idx_name_ref_gender ON ref.name_ref (gender);
CREATE INDEX idx_name_ref_is_first ON ref.name_ref (is_first);
CREATE INDEX idx_name_ref_is_last ON ref.name_ref (is_last);
CREATE INDEX idx_name_ref_popularity ON ref.name_ref (popularity);
CREATE INDEX idx_name_origin_mapping_origin ON ref.name_origin_mapping (origin_id);
CREATE INDEX idx_origin_region_mapping_region ON ref.origin_region_mapping (region_id);

-- =====================================================================
-- SECTION 7: Update triggers for modified_at
-- =====================================================================

CREATE TRIGGER update_name_ref_modified_at
    BEFORE UPDATE ON ref.name_ref
    FOR EACH ROW
EXECUTE FUNCTION public.update_modified_at_column();

CREATE TRIGGER update_name_origin_ref_modified_at
    BEFORE UPDATE ON ref.name_origin_ref
    FOR EACH ROW
EXECUTE FUNCTION public.update_modified_at_column();

CREATE TRIGGER update_name_region_ref_modified_at
    BEFORE UPDATE ON ref.name_region_ref
    FOR EACH ROW
EXECUTE FUNCTION public.update_modified_at_column();

-- =====================================================================
-- SECTION 8: Verify Migration
-- =====================================================================

DO $$
    BEGIN
        RAISE NOTICE 'Migration V56 completed successfully';
        RAISE NOTICE 'Created tables: ref.name_ref, ref.name_origin_ref, ref.name_region_ref';
        RAISE NOTICE 'Created mapping tables: ref.name_origin_mapping, ref.origin_region_mapping';
    END $$;