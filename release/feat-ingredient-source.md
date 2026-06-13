# feat/ingredient-source — ingredient roles, add/edit/delete + creator name

## What changed
USER/ADMIN roles + real ingredient CRUD with ownership/admin authorization, plus the
creator's display name on ingredient responses.

### Roles (mirrors subscription-service's role model)
- `app_user.role TEXT NOT NULL DEFAULT 'USER' CHECK (role IN ('USER','ADMIN'))` —
  migration `db/feat-ingredient-source/01_user_role.sql` + canonical `db/setup.sql`;
  dev seed `chef@example.com` = ADMIN.
- `enumeration/UserRole` + `converter/UserRoleConverter`; `AppUser.role` (default USER).
- JWT carries a `role` claim (`JwtUtil.generate`); `JwtAuthenticationFilter` builds
  `ROLE_<role>` authorities; `UserPrincipal.role`; `SecurityUtils.getCurrentUserRole()` /
  `isAdmin()`; `AuthResponse.role` (login + register).

### Ingredient CRUD + authz (`IngredientController` / `IngredientService`)
- `POST /ingredients` — admin → System (`isBuiltin=true`, `createdBy=null`); user → custom
  (`createdBy=user`).
- `PATCH /ingredients/{id}` — edit name/category/kcalPerGram/gramsPerPiece (name-collision → 409).
- `DELETE /ingredients/{id}`.
- Authz (`requireManage`): System ingredients require ADMIN; custom require owner or ADMIN
  (else 403). Deleting an in-use ingredient (recipe/pantry/shopping FK RESTRICT) → 409.
- `IngredientResponse.createdByName` (creator display name; `null` for System).

## Verification
- `mvn -DskipTests package` — clean (incl. updated controller tests for the new signatures).
- Ran against the dev DB; curl matrix all pass:
  admin add → System; admin PATCH System → 200; admin DELETE unused System → 204;
  user add → custom (`createdByName` set); user PATCH own → 200;
  user PATCH/DELETE a System → **403**; admin DELETE in-use built-in → **409**;
  login `role` = ADMIN (chef) / USER (new register).

## Notes
- Admins are designated in the DB (no promote/demote UI), per the chosen approach.
- Visibility unchanged: custom ingredients stay private to their creator.
- Frontend: cooked-ui `feat/ingredient-source` (role in session, manage controls).
