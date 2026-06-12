# Release Notes — feat-complete-api

## Summary

Complete REST API implementation for the `cooked` schema. Replaces the placeholder schema and stub endpoints with a production-ready API covering auth, ingredients, recipes, pantry, shopping list, cook history, saved recipes, and vocabulary (moods/cuisines).

## What Changed

### Schema (`db/setup.sql`, `db/seed.sql`)
- Replaced old `cooked_service` schema with `cooked` schema
- Old tables (`users`, `recipe`, `ingredient` with simple fields) replaced by full DDL
- New `unit_type` PostgreSQL enum (`g`, `kg`, `ml`, `L`, `pcs`)
- New tables: `app_user`, `mood`, `cuisine`, `ingredient`, `user_ingredient_edit`, `recipe`, `recipe_mood`, `recipe_ingredient`, `recipe_instruction`, `saved_recipe`, `pantry_item`, `shopping_item`, `cook_history`, `cook_history_item`, `user_settings`
- All IDs changed from `INTEGER` to `BIGINT`
- `password_hash` added to `app_user` via migration `db/feat-complete-api/01_add_password_hash.sql`
- Seed data updated: vocabulary tables, builtin ingredients, demo recipes with moods/steps

### Entities
- Replaced `User`, `Recipe`, `Ingredient` entities with new schema-aligned entities
- Added: `AppUser`, `Mood`, `Cuisine`, `UserIngredientEdit`, `RecipeMood`, `RecipeIngredient`, `RecipeInstruction`, `SavedRecipe`, `PantryItem`, `ShoppingItem`, `CookHistory`, `CookHistoryItem`, `UserSettings`
- Composite PKs use `@EmbeddedId` with `@Embeddable` id classes
- `PantryItem.unit` mapped to `unit_type` enum via `@ColumnTransformer(write = "?::unit_type")`

### Security / JWT
- `UserPrincipal` updated: `userId` is now `long` (was `int`)
- `JwtUtil.generate()` accepts `long userId`
- `JwtAuthenticationFilter` extracts userId via `((Number) claims.get("userId")).longValue()` — safe for both Integer and Long JWT serialization

### DTOs (all new)
`AuthResponse`, `RegisterRequest`, `LoginRequest`, `UserResponse`, `UserPatchRequest`, `UserSettingsResponse`, `UserSettingsPatchRequest`, `IngredientResponse`, `IngredientCreateRequest`, `UserIngredientEditRequest`, `RecipeSummaryResponse`, `RecipeDetailResponse`, `RecipeCreateRequest`, `RecipePatchRequest`, `RecipeIngredientResponse/Request`, `RecipeInstructionResponse/Request`, `RecipeMoodsRequest`, `PantryItemResponse/CreateRequest/PatchRequest`, `ShoppingItemResponse/AddRequest`, `CookRequest` (with nested `IngredientEntry`), `CookHistoryResponse/ItemResponse`

### Services (all new or rewritten)
- `AppUserService` — register/login (BCrypt), getMe/patchMe
- `IngredientService` — list (builtin + user-created) with user override effective values, create, putEdit, deleteEdit
- `RecipeService` — listVisible/listMine/getDetail/create/patch/delete + sub-resource PUT for moods/ingredients/instructions
- `PantryService` — CRUD + `deductFifo()` with unit-to-gram conversion (G, KG, ML, L, PCS)
- `ShoppingService` — CRUD + upsert (adds grams to existing row on duplicate ingredient)
- `CookHistoryService` — kcal calculation (respects user overrides), FIFO pantry deduction, cook snapshot
- `UserSettingsService` — get-or-create defaults, null-field-skipping PATCH
- `SavedRecipeService` — idempotent save/unsave
- `MoodService`, `CuisineService` — vocabulary lookups
- `UserService` — deprecated stub (replaced by `AppUserService`)

### Controllers (all new or rewritten)
| Controller | Endpoints |
|---|---|
| `AuthController` | `POST /auth/register`, `POST /auth/login` |
| `UserController` | `GET/PATCH /users/me`, `GET/PATCH /users/me/settings`, `GET /users/me/ingredients`, `PUT/DELETE /users/me/ingredients/{id}/edit` |
| `IngredientController` | `GET /ingredients`, `POST /ingredients` |
| `RecipeController` | `GET/POST /recipes`, `GET/PATCH/DELETE /recipes/{id}`, `PUT /recipes/{id}/moods`, `PUT /recipes/{id}/ingredients`, `PUT /recipes/{id}/instructions` |
| `SavedRecipeController` | `GET /saved-recipes`, `PUT/DELETE /saved-recipes/{recipeId}` |
| `PantryController` | `GET/POST /pantry`, `PATCH/DELETE /pantry/{id}` |
| `ShoppingController` | `GET/POST /shopping`, `DELETE /shopping/{id}`, `DELETE /shopping` |
| `CookHistoryController` | `GET/POST /cook-history`, `GET /cook-history/{id}` |
| `VocabularyController` | `GET /vocabulary/moods`, `GET /vocabulary/cuisines` |

### Tests
- `AuthControllerTest` — rewritten for `AppUserService` (4 tests)
- `IngredientControllerTest` — rewritten for new `IngredientController` (3 tests)
- `RecipeControllerTest` — rewritten for new `RecipeController` with DTOs (5 tests)
- `HealthCheckControllerTest` — unchanged (4 tests)
- Total: 16 tests, all passing

## Migration Steps (existing deployment)

1. Run `db/feat-complete-api/01_add_password_hash.sql` if upgrading from the previous schema
2. For a fresh database, run `db/setup.sql` then optionally `db/seed.sql`