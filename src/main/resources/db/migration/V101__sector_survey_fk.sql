-- ============================================================================
-- V101: Add survey FK and sector_code to Sector
-- ============================================================================
-- Links sectors to their parent survey and adds the sector-local identifier.
-- The sector name is computed as: survey.code + "-" + sector_code
-- ============================================================================

ALTER TABLE ud.sector ADD COLUMN IF NOT EXISTS survey_id BIGINT
    REFERENCES ud.survey(id) ON DELETE SET NULL;

ALTER TABLE ud.sector ADD COLUMN IF NOT EXISTS sector_code VARCHAR(20);

CREATE INDEX IF NOT EXISTS idx_sector_survey ON ud.sector(survey_id);

-- Update any existing sectors to reference the default survey
UPDATE ud.sector SET survey_id = (SELECT id FROM ud.survey WHERE code = 'SCS')
WHERE survey_id IS NULL;
