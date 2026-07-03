-- Rollback for feat-recipe-menu-page.
-- NOTE: irreversible data loss — any photo_url/photo_data values saved by users are dropped permanently.
ALTER TABLE cooked.recipe DROP COLUMN IF EXISTS photo_url;
ALTER TABLE cooked.recipe DROP COLUMN IF EXISTS photo_data;
ALTER TABLE cooked.recipe DROP COLUMN IF EXISTS photo_content_type;
