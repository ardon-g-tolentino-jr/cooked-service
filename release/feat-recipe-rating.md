# feat/recipe-rating (cooked-service)

Community recipe ratings (1–5 stars), one per user per recipe, allowed only after the
user has cooked the recipe at least once.

## What changed

- **DB** `db/feat-recipe-rating/01_recipe_rating.sql` (run as schema owner): `recipe_rating
  (recipe_id, user_id, stars 1–5, created_at, updated_at, PK(recipe_id,user_id))` with FKs
  to recipe + app_user; grant to `cooked_user`.
- **`RecipeRating`** entity (composite `@IdClass`) + **`RecipeRatingRepository`**
  (`findByRecipeIdAndUserId`, `countByRecipeId`, `avgByRecipeId`).
- **`CookHistoryRepository.existsByUserIdAndRecipeId`** — the cooked-once gate (cook_history
  already has user_id + recipe_id).
- **`RecipeService.rate(userId, recipeId, stars)`**: requires the recipe `isCommunity`
  (else 400) and that the user has cooked it (else **403**); upserts the rating.
  `buildDetail(r, userId)` now includes `ratingAvg`, `ratingCount`, `myRating`.
- **`RecipeDetailResponse`** gained `ratingAvg` (Double, null when unrated), `ratingCount`
  (long), `myRating` (Integer, null). **`PUT /recipes/{id}/rating { stars }`**
  (`RecipeController`). New `RatingRequest` DTO. `RecipeControllerTest` stub updated.

## Verification
- `mvn clean package` (Java 17) — BUILD SUCCESS, 18/18 tests pass.
- Migration must be applied by the `postgres` superuser (like prior migrations).
