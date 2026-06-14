# fix-register-idempotent-subscription-access

## Summary

Fixes registration becoming permanently blocked after a partial signup. The
subscription-side access grant happens via a remote call *before* the local
`app_user` row is persisted and outside any shared transaction, so an attempt
that fails after provisioning (rolled-back local save, email send error, etc.)
leaves an **orphaned state**: active COOKED access on the subscription side with
no local user. Every subsequent registration for that email then fails — the
subscription `/code` endpoint answers `409 DuplicateAccess` ("You already have
active access… your code was not consumed"), which `provisionViaCode` turned
into a `400`, aborting signup forever.

Registration is now idempotent and self-reconciling.

## What changed

### `service/AppUserService#register`
- Before redeeming a code, do a non-throwing `subscriptionGate.hasActiveAccess(email)`
  check. If the email already has active COOKED access, **skip code redemption**
  and proceed to create the local account (temp password issued + emailed as
  usual). This reconciles an orphaned subscription grant and lets an
  already-provisioned email register without a (possibly exhausted) code.

### `service/SubscriptionGateService`
- New `hasActiveAccess(String email)` — non-throwing companion to
  `assertActiveAccess`. Returns `true` only when the email has an active record
  for this `serviceCode`. Any error (gate disabled, 404, service unreachable)
  returns `false` so the caller falls back to normal code redemption, which
  keeps its own fail-closed error handling.
- `provisionViaCode` now treats a subscription `409 CONFLICT` (DuplicateAccess —
  code not consumed) as an **idempotent no-op success** instead of a `400`, as a
  safety net for the race where access is granted between the pre-check and the
  redemption call.

## Behavior notes

- Login is unchanged and still fully gated: `login` / `loginWithGoogle` continue
  to call `assertActiveAccess`, which throws `403` when no active access exists.
  The new path only relaxes *registration* when access already exists.
- A net-new user with no prior access is unaffected: `hasActiveAccess` returns
  `false`, so `provisionViaCode` runs and a valid code is still required (when
  the gate is enabled).
- Operational note: an email already orphaned by a pre-fix attempt can also be
  unblocked immediately via Google Sign-In (`loginWithGoogle` find-or-creates
  the local user once access is active) — no code needed.

## Also in this branch: HTML email notifications

Registration / password-reset emails were plain text and, in practice, not
reliably delivered. Reworked to the appointments-api approach — Thymeleaf HTML
templates rendered into MIME messages — and Cooked-branded.

### What changed

- **`pom.xml`** — added `spring-boot-starter-thymeleaf`.
- **`service/EmailService`** — rewritten to render Thymeleaf templates into
  `MimeMessage` HTML bodies (`MimeMessageHelper(..., true)`), mirroring
  appointments-api. Two typed methods: `sendWelcomeEmail(to, name, tmpPassword)`
  and `sendPasswordResetEmail(to, name, tmpPassword)`. Keeps the existing
  fail-soft behavior: when mail is disabled/unconfigured it logs the temp
  password instead of sending, and it never throws (delivery hiccups can't fail
  registration / reset). The old plain-text `send(to, subject, body)` method is
  removed.
- **New templates** (`src/main/resources/templates/`) — `welcome-email.html`
  and `password-reset.html`, ported from the appointments-api templates and
  re-skinned to Cooked branding (accent `#FF6B35`, pantry-first feature blurbs).
  Variables: `name`, `tmpPassword`, `loginUrl`.
- **`AppUserService`** — `register` now calls `sendWelcomeEmail`; `forgotPassword`
  calls `sendPasswordResetEmail`.
- **Config** — new `app.ui.base-url` (`APP_UI_BASE_URL`, default
  `https://cooked.humanworkstream.com`) for the email "Sign in" button.

### Deployment note

Email only sends when `MAIL_USERNAME` + `MAIL_PASSWORD` (Gmail app password) are
set in the environment — otherwise the temp password is logged, not emailed. The
default `MAIL_FROM` already points at `humanworkstream@gmail.com` (the same
account appointments-api uses), so the same credentials can be reused. See the
README env-var table.

## Tests

- `mvn -o test` — 18/18 pass (`AuthControllerTest` mocks `AppUserService`, so it
  covers the controller contract; service changes verified by clean compile).
  Template rendering ports the already-working appointments-api Thymeleaf setup.
