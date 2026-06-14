# feat/passwordless-registration

Registration no longer accepts a user-chosen password. Instead it issues a
system-generated **temporary password** by email (reusing the forgot-password
mechanism), which the user replaces on first sign-in. Passwords are only supplied
at login.

## Changes

- **`dto/RegisterRequest`** — removed the `password` field. Body is now
  `{ displayName, email, registrationCode }`.
- **`service/AppUserService#register`** — now returns `void`. Generates a temp
  password via `PasswordGenerator.generate(12)`, provisions the subscription with
  it (`provisionViaCode`), stores it `passwordEncoder.encode(temp)` with
  `passwordTemporary = true`, and emails it (welcome variant of the temp-password
  mail). No `AuthResponse` / auto-login.
- **`controller/AuthController#register`** — returns `201 { message }` instead of
  an `AuthResponse`.
- **README** — updated the `POST /auth/register` row.

## Notes

- No DB migration: `password_temporary` already exists (used by forgot-password).
- The temp password is also what the subscription side is provisioned with, exactly
  as the user's chosen password was before — behavior is consistent with the
  existing forgot-password reset (which never re-syncs the subscription password).
- Login already forces a password change when `passwordTemporary` is set, so the
  temporary → permanent handoff is unchanged.

## Paired UI change

cooked-ui branch `fix/registration-prod`: removes the register password field and
shows a "temporary password emailed — check your inbox, then sign in" notice.
