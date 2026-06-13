-- feat/recipe-rating — community recipe ratings (1 per user per recipe)
-- Schema: cooked
--
--   psql -U postgres -d cooked -f db/feat-recipe-rating/01_recipe_rating.sql
--
-- A rating is only meaningful for community recipes the user has cooked; that gate is
-- enforced in the service layer. Idempotent.

CREATE TABLE IF NOT EXISTS cooked.recipe_rating (
  recipe_id  BIGINT      NOT NULL REFERENCES cooked.recipe(id)   ON DELETE CASCADE,
  user_id    BIGINT      NOT NULL REFERENCES cooked.app_user(id) ON DELETE CASCADE,
  stars      SMALLINT    NOT NULL CHECK (stars BETWEEN 1 AND 5),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (recipe_id, user_id)
);
CREATE INDEX IF NOT EXISTS ix_recipe_rating_recipe ON cooked.recipe_rating(recipe_id);

GRANT SELECT, INSERT, UPDATE, DELETE ON cooked.recipe_rating TO cooked_user;
