-- feat-recipe-menu-page: add optional dish photo URL to recipes (backs the new Menu view's photo cards)
ALTER TABLE cooked.recipe ADD COLUMN IF NOT EXISTS photo_url TEXT;
