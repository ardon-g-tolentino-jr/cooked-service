# feat/pantry-templates — saved pantry ingredient lists (backend)

## What changed

New backend feature letting a user save the **ingredient list** of their pantry (names
only — no quantity/unit/expiration) as one or more named **templates**, and read them
back. Loading a template into the pantry is done client-side (cooked-ui) by POSTing each
ingredient to the existing `POST /pantry`.

Mirrors the existing `meal-plan` feature (controller/service/repo) and the `recipe_mood`
join-table style (`@EmbeddedId`). User scoping via `securityUtils.getCurrentUserId()`.

### DB — `db/feat-pantry-templates/01_pantry_template.sql`
Two new tables in the `cooked` schema (idempotent, `IF NOT EXISTS`, with role grants):
- `pantry_template (id, user_id → app_user ON DELETE CASCADE, name, created_at)`
- `pantry_template_item (template_id → pantry_template ON DELETE CASCADE, ingredient_id →
  ingredient ON DELETE CASCADE, PRIMARY KEY (template_id, ingredient_id))`

The composite PK dedupes a given ingredient within a template; deleting a template
cascades to its items at the DB level.

### Java
- Entities: `PantryTemplate`, `PantryTemplateItem` + `entity/id/PantryTemplateItemId` (`@EmbeddedId`).
- Repositories: `PantryTemplateRepository` (`findByUserIdOrderByCreatedAtDesc`),
  `PantryTemplateItemRepository` (`findByIdTemplateId`, `findByIdTemplateIdIn`).
- DTOs: `PantryTemplateCreateRequest(name, ingredientIds)`, `PantryTemplateItemResponse`,
  `PantryTemplateResponse` (+ `from(...)`).
- `PantryTemplateService` — list (resolves ingredient names via `IngredientRepository`),
  create (validates every ingredientId exists; stores distinct ids), delete (ownership
  check → FORBIDDEN on mismatch).
- `PantryTemplateController` @ `/pantry-templates`.

### Endpoints (all authenticated by the existing JWT filter; `anyRequest().authenticated()`)
- `GET    /pantry-templates` — list the user's templates with their ingredient items.
- `POST   /pantry-templates` — `{ name, ingredientIds: number[] }` → 201 with the template.
- `DELETE /pantry-templates/{id}` — 204 (404 if missing, 403 if not owner).

## Not included (deliberate, MVP)
- No rename/PATCH endpoint.
- Not trial-gated. (Loading into the pantry already goes through the trial-gated
  `POST /pantry`.)

## Migration / deploy
Apply the migration to each environment's `cooked` DB:
```
psql -U postgres -d cooked -f db/feat-pantry-templates/01_pantry_template.sql
```

## Verification
- `mvn -o compile` (JDK 17) — passes.
- See cooked-ui `release/feat-pantry-templates.md` for the end-to-end UI test.
