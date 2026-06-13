# feat/forgot-password (cooked-service)

Self-service password reset: a user requests a reset, gets a system-generated
temporary password by email, and is forced to set a new password after signing in.

## What changed

### DB — `db/feat-forgot-password/01_password_temporary.sql` (run as schema owner)
- `app_user.password_temporary BOOLEAN NOT NULL DEFAULT false` — true while a temp
  password is in effect; drives the forced "set a new password" screen.

### Email
- Added `spring-boot-starter-mail`; `EmailService` sends plain-text mail via
  `JavaMailSender`, falling back to logging when mail is unconfigured. SMTP is
  env-driven (`MAIL_HOST/PORT/USERNAME/PASSWORD`, `MAIL_FROM`, `MAIL_ENABLED`),
  defaulting to Gmail SMTP (mirrors subscription-service).

### Endpoints
- `POST /auth/forgot-password { email }` (public) — generates a 12-char temp password
  (`PasswordGenerator`), bcrypts it, sets `password_temporary=true`, emails it. Always
  returns 200 with a neutral message (no account enumeration). SSO-only accounts are
  skipped silently.
- `PUT /users/me/password { currentPassword, newPassword }` (authenticated) — verifies
  the current password, sets the new one, clears `password_temporary`.

### Auth response
- `AuthResponse` gained `mustChangePassword` (= the user's `password_temporary`), so the
  frontend can force a change after a temp-password login. New DTOs
  `ForgotPasswordRequest`, `ChangePasswordRequest`. `AuthControllerTest` stub updated.

## Notes
- Resets only the **cooked-service** password (what Cooked login uses); the subscription
  account's password, synced at registration, is unaffected.
- Emailing a plaintext temp password (as requested) is less secure than a reset link;
  the forced change on next login limits the exposure window.

## Verification
- `mvn clean package` (Java 17) — BUILD SUCCESS, 18/18 tests pass.
- Migration must be applied by the schema owner (`postgres`), like `setup.sql`.
