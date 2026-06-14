# feat/trial-full-access-window — time-boxed trial (backend)

## What changed

A trial account (registered with a reg code containing "TRIAL") now gets **full**
(unlimited) app access for a configurable window of **X days**, after which the existing
trial limits (caps + locked components) apply. Previously trial limits applied immediately
and permanently.

The window is **frozen per user** at registration (and on first login if not yet set), so a
later config change only affects new accounts.

### Config
- `cooked.trial.full-access-days` (env `COOKED_TRIAL_FULL_ACCESS_DAYS`, default **14**) in
  `application.properties`.

### DB
- `app_user.trial_full_access_until TIMESTAMPTZ` (nullable). Added to `db/setup.sql` and
  migration `db/feat-trial-full-access/01_trial_full_access.sql` (idempotent; backfills
  existing trial users with `created_at + 14 days`).

### Code
- `entity/AppUser` — new `trialFullAccessUntil` field.
- `service/AppUserService` — injects the day count; `ensureTrialWindow(user)` freezes
  `created_at + days` the first time a user is known to be trial (called from `register`,
  `login`, `loginWithGoogle`). `authResponse(...)` emits the window into the JWT
  (`trialUntil` epoch-ms claim) and `AuthResponse.trialFullAccessUntil` (ISO).
- `security/JwtUtil` (+ `trialUntil` claim), `UserPrincipal` (+ `trialUntil`),
  `JwtAuthenticationFilter` (reads the claim), `SecurityUtils.getTrialUntil()`.
- `service/TrialLimitService` — new `isLimitedTrial()` (trial **and** `now >= trialUntil`,
  or claim absent → limited as a safety default). `assertEnabled`/`assertUnderLimit` now
  early-return on `!isLimitedTrial()` instead of `!isTrial()`. No call-site changes.
- `dto/AuthResponse` — new `trialFullAccessUntil` field.

## Operational note
For the post-window state to be **limited** (not locked out), trial reg codes must grant
**ongoing** subscription access (no / long `RegCode.expiresAt`). Once `ClientAccess.accessEnd`
passes, the subscription gate denies login entirely. cooked-service independently enforces
the full→limited downgrade. **No subscription-service changes.**

## Verification (local, real DB + backend)
- `mvn -o compile` — passes.
- Migration applied to a seeded local DB; `meal_plan` locked + `pantry` cap=0 configured.
- Minted trial JWTs and confirmed via the live API:
  | token | GET /meal-plan (locked) | POST /pantry (cap 0) |
  |---|---|---|
  | trial, window in future | **200** (full) | **201** (full) |
  | trial, window expired | **403** | **409** |
  | trial, no `trialUntil` claim | **403** | **409** |
  | non-trial | **200** | **201** |
- `POST /auth/register` with a `TRIAL-*` code → DB row has `is_trial=t` and
  `trial_full_access_until = created_at + 14 days`.
