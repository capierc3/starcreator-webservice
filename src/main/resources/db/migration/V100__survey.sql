-- ============================================================================
-- V100: Create Survey entity
-- ============================================================================
-- Surveys are the top-level organizational unit for system generation.
-- Each survey has a short code (e.g. "SCS") used in system designations.
-- Naming convention: {survey.code}-{sectorCode}-{systemId}
-- ============================================================================

CREATE TABLE ud.survey (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    code            VARCHAR(10) NOT NULL UNIQUE,
    description     TEXT,
    user_id         BIGINT,
    created_at      TIMESTAMP DEFAULT NOW(),
    modified_at     TIMESTAMP DEFAULT NOW()
);

COMMENT ON TABLE ud.survey IS
'Survey campaigns that organize sectors and star systems under a common designation code';

CREATE UNIQUE INDEX idx_survey_code ON ud.survey(code);

-- Seed the default survey
INSERT INTO ud.survey (name, code, description)
VALUES ('Star Creator Survey', 'SCS', 'Basic system mapping survey');
