# feat/sso-login (cooked-service)

Add Google Sign-In (SSO) as a login option, verified natively and subject to the
same Cooked access gate as password login.

## What changed

- **`GoogleTokenVerifier`** (new) — verifies a Google ID token against
  `google.client-id` using `google-api-client` (added to `pom.xml`, v2.2.0).
- **`POST /auth/google`** (new, public) — `AuthController.google` →
  `AppUserService.loginWithGoogle(idToken)`:
  1. verify the Google ID token (invalid → `401`),
  2. **enforce the subscription gate** on the Google email (no active COOKED
     access → `403`), checked before any local write so blocked accounts leave no
     orphan row,
  3. find-or-create the local `AppUser` by email (SSO accounts have no
     `password_hash`; `display_name` taken from the Google profile name),
  4. issue a Cooked JWT — same `AuthResponse` shape as password login.
- **`GoogleLoginRequest`** (new) — `{ idToken }`, `@NotBlank`.
- **`SecurityConfig`** — `POST /auth/google` permitted.
- **`application.properties`** — `google.client-id=${GOOGLE_CLIENT_ID:…}`, default
  is the shared subscription OAuth client (public value, env-overridable).
- **`AuthControllerTest`** — added valid/invalid `/auth/google` cases (now 6 in the
  class, 18 total).

## Prerequisite

The OAuth client must list Cooked's browser origins (e.g. `http://localhost:5173`,
`https://cooked.humanworkstream.com`) under **Authorized JavaScript origins** in
Google Cloud Console, or the browser button will not issue a token.

New Google users with no COOKED access are blocked by the gate — provision them on
the subscription side (redeem a code / subscription) first, same as password users.

## Verification

- `mvn clean package` — BUILD SUCCESS, 18/18 tests pass.
