-- Remove legacy meanSurfaceTempHabitable field from planetary_habitability.
-- This global-mean check was a write-only field superseded by detailed climate
-- zone temperatures, surfaceLiquidWaterPossible, and the habitability classification.
-- For tidally locked worlds the global mean is misleading (nightside drags it below
-- 273K even when the substellar zone is comfortably warm with liquid water).

ALTER TABLE ud.planetary_habitability DROP COLUMN IF EXISTS mean_surface_temp_habitable;
