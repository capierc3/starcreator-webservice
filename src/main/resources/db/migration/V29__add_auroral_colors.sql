-- V29__add_auroral_colors.sql
-- Add auroral color information to magnetic field table

ALTER TABLE ud.planetary_magnetic_field
    ADD COLUMN auroral_colors VARCHAR(200);

COMMENT ON COLUMN ud.planetary_magnetic_field.auroral_colors IS
    'Colors that auroras display based on atmospheric composition. Different gases emit different wavelengths when ionized by stellar wind particles.';
