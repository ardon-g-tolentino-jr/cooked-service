-- Rollback for feat-recipe-menu-page.
-- NOTE: irreversible data loss — any photo_url values saved by users are dropped permanently.
ALTER TABLE cooked.recipe DROP COLUMN IF EXISTS photo_url;
