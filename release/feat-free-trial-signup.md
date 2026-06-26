# feat-free-trial-signup

Adds a **code-less free trial** to Cooked, per the subscription-service
`FREE_TRIAL_INTEGRATION.md` spec. The registration code is now optional: a **blank/absent
code** starts a time-limited TRIAL that the subscription service owns end to end (it
auto-provisions the COOKED `Trial` plan, issues a one-use `TRIAL-AUTO` code, and redeems it).
A non-blank code keeps the existing paid redemption path. Builds on the trial scaffolding that
was already in place (temp-password registration, trial flag + full-access window, login-time
tier sync, and **Trial → Free** lapse-to-free).

## Changes

- **`service/SubscriptionGateService`** — new `provisionTrial(displayName, email, password)`:
  `POST /api/subscription/trial` with `X-Api-Key` and body
  `{ name, doctorName, email, password, serviceCode }`. No-op + log when the gate is disabled
  (local dev). Error mapping mirrors `provisionViaCode` (reusing `extractMessage`):
  `409 → idempotent no-op`, other `4xx → 400` with the message, anything else `→ 503`.
- **`service/AppUserService#register`** — branches on a blank code:
  `hasActiveAccess → skip` (idempotent), `else blank code → provisionTrial`,
  `else → provisionViaCode`. The trial flag is now
  `isTrial || code contains "TRIAL"` (was code-name only), so a blank-code signup is marked
  trial and `ensureTrialWindow` freezes its full-access window as before.

## Notes

- **No DB migration, no new config.** `subscription.*` and `cooked.trial.full-access-days`
  (14) already exist; the subscription `Trial` plan's `trialPeriodDays` (also 14 by default)
  owns the actual access window — keep the two aligned.
- **No login change.** `assertActiveAccess` / `effectiveTier` already resolve an active trial to
  the built-in TRIAL tier and a lapsed trial to `freeTierName()` ("Free"); expiry needs no
  Cooked-side code.
- Mirrors the proven `inventory-api` integration (`SubscriptionGateService#provisionTrial`,
  `AuthService#register`).

## Paired UI change

cooked-ui branch `feat-free-trial-signup` (`views/LoginView.tsx`): the registration code field
is **optional** (label "Registration code (optional)", hint "Leave blank to start a 14-day free
trial"); a blank code relabels the action **"Start free trial"** and shows a trial-flavored
success notice. The forced password-change gate is unchanged (already wired).
