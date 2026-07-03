# feat-recipe-menu-page (cooked-service)

Adds an optional dish photo to recipes, backing a new photo-menu view on the cooked-ui side.

## What changed

- **DB** `db/feat-recipe-menu-page/migration.sql`: `ALTER TABLE cooked.recipe ADD COLUMN IF
  NOT EXISTS photo_url TEXT;` (nullable, no backfill). Rollback in
  `db/feat-recipe-menu-page/rollback.sql`. Also appended to `db/setup.sql` for fresh installs.
- **`Recipe`** entity gained `photoUrl` (`photo_url` column).
- **`RecipeSummaryResponse`** and **`RecipeDetailResponse`** gained `photoUrl`.
- **`RecipeCreateRequest`** and **`RecipePatchRequest`** gained optional `photoUrl`.
  `RecipeService.create()` sets it from the request; `patch()` updates it only when the
  request field is non-null (same null-means-unchanged convention as the other patchable
  fields).
- `RecipeControllerTest` stub fixtures updated for the new trailing record field.

## Verification
- `mvn -DskipTests compile` — BUILD SUCCESS.
- `mvn test -Dtest=RecipeControllerTest` — all tests pass.
- Migration must be applied by the `postgres` superuser (like prior migrations) — not run in
  this session; local DB credentials weren't available.
