# feat/trial-limits (cooked-service)

Admin-configurable TRIAL access limits. A user whose registration code contains
`TRIAL` (case-insensitive) is flagged `is_trial`; limits then apply per component.

## What changed

### DB — `db/feat-trial-limits/01_trial_access.sql` (run as the schema owner)
- `app_user.is_trial BOOLEAN NOT NULL DEFAULT false`.
- `trial_limit (component PK, access_enabled, max_count, updated_at)` seeded with:
  `meal_plan`(off), `recipes`(cap 10), `pantry`, `shopping`, `history`, `ingredients`.

### Trial detection + JWT
- `SubscriptionGateService.assertActiveAccess` now returns `boolean` (true when the
  active COOKED access came from a `TRIAL` regCode); `AccessRecord` gained `regCode`.
- `AppUserService` sets `is_trial` on register (typed code) and login/Google (access
  record), persisted and refreshed each login.
- `trial` claim added to the JWT (`JwtUtil`/`JwtAuthenticationFilter`/`UserPrincipal`);
  `SecurityUtils.isTrial()`.

### Enforcement — `TrialLimitService`
- `assertEnabled(component)` (403 when disabled) and `assertUnderLimit(component, count)`
  (409 at cap) — both no-ops for non-trial users. Wired into:
  RecipeService.create (cap), MealPlanService list/add/delete (block), PantryService.add
  (block + cap), ShoppingService.add, CookHistoryService.list, IngredientService custom
  create. Cross-cutting reads (pantry/ingredient lists feeding Kitchen) are left open.

### API
- `GET /trial-limits` (any authed) — config for the frontend.
- `PUT /admin/trial-limits/{component}` (admin via `SecurityUtils.isAdmin()`) — toggle/cap.
- `AuthResponse` + `UserResponse` gained `trial`. New DTOs `TrialLimitResponse`,
  `TrialLimitUpdateRequest`. `AuthControllerTest` stub updated.

## Verification
- `mvn clean package` (Java 17) — BUILD SUCCESS, 18/18 tests pass.
- Migration SQL validated against local `cooked` (must be applied by the schema owner /
  `postgres`, like `setup.sql` — `cooked_user` lacks ALTER on `app_user`).

## Notes
- Login trial-detection relies on the subscription `access/by-email` response carrying
  `regCode`; the typed code at register is the primary reliable signal.
