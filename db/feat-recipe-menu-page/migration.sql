-- feat-recipe-menu-page: add optional dish photo URL to recipes (backs the new Menu view's photo cards)
ALTER TABLE cooked.recipe ADD COLUMN IF NOT EXISTS photo_url TEXT;

-- Uploaded-file alternative to photo_url — stored directly in the DB, served via
-- GET /recipes/{id}/photo. Mutually exclusive with photo_url (setting one clears the other).
ALTER TABLE cooked.recipe ADD COLUMN IF NOT EXISTS photo_data BYTEA;
ALTER TABLE cooked.recipe ADD COLUMN IF NOT EXISTS photo_content_type TEXT;
