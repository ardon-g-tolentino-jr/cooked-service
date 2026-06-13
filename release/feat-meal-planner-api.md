# feat/meal-planner-api — weekly meal planner (backend)

## What changed
Adds server-side storage + REST API for the weekly meal planner (calendar-dated,
multiple recipes per day/slot). Mirrors the existing Shopping slice.

- **Schema** — `db/feat-meal-planner/01_meal_plan.sql` (migration) + canonical
  `db/setup.sql`:
  - `cooked.meal_plan` (`id`, `user_id`→app_user CASCADE, `plan_date DATE`,
    `meal_slot TEXT CHECK in (breakfast,lunch,snack,dinner)`, `recipe_id`→recipe
    CASCADE, `created_at`, `UNIQUE(user_id, plan_date, meal_slot, recipe_id)`,
    indexes on `(user_id, plan_date)` and `recipe_id`).
  - `meal_slot` is **TEXT + CHECK** (same pattern as `user_settings.kcal_mode`),
    not a Postgres enum — avoids enum/Hibernate cast friction in derived queries.
- **Java** (`com.humanworkstream.cooked`):
  - `enumeration/MealSlot` (`@JsonValue`/`@JsonCreator`, lowercase values),
    `converter/MealSlotConverter` (`@Converter(autoApply)` ↔ TEXT).
  - `entity/MealPlanEntry`, `repository/MealPlanRepository`
    (`findByUserIdAndPlanDateBetween…`, dedup finder), `service/MealPlanService`
    (list joins recipe names; add validates recipe + dedupes; delete ownership-checks),
    `controller/MealPlanController`.
  - `dto/MealPlanAddRequest`, `dto/MealPlanEntryResponse`.

## Endpoints (JWT-auth, current user from `SecurityUtils`)
- `GET /meal-plan?from=YYYY-MM-DD&to=YYYY-MM-DD` → entries in range (recipe names resolved).
- `POST /meal-plan` `{planDate, mealSlot, recipeId}` → 201 (idempotent on the unique key).
- `DELETE /meal-plan/{id}` → 204 (403 if not owner).

## Verification
Migration applied under `ON_ERROR_STOP=1`; service built (`mvn -DskipTests package`) and
run against the dev DB. Exercised via curl: POST→201, **multiple recipes per slot**,
**dedup** (4 POSTs → 3 rows), **bad slot → 400**, GET returns ordered entries with
`recipeName`, DELETE→204.

## Notes
Frontend lives in cooked-ui (`feat/meal-planner`): a Planner view that reuses the
already-loaded recipe list for search/calories and the existing cook/shopping handlers;
this API only stores `{date, slot, recipeId}`.
