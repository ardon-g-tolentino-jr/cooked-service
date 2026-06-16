# Integrating an app with the Subscription API

How a new system should integrate with the Subscription service, generalized from the
working implementation in cooked-service (`service/SubscriptionGateService.java` plus the
register/login flow in `service/AppUserService.java`). Use this as the integration
requirement/spec for any consumer app.

---

## 1. Model & principles

- **Server-to-server only.** Every call to the subscription API is made from your
  **backend**, authenticated with a secret `X-Api-Key` header. This key must **never**
  reach the browser/client.
- **The subscription service is the source of truth** for "may this email use service X".
  Your app keeps a local user row but **re-derives access on every login** from the
  subscription side.
- **Fail-closed.** If access cannot be positively verified (error, timeout, 404,
  unreachable), you **deny** entry — never default to "allowed".
- **Per-service scoping.** Each consumer app has a **service code** (Cooked uses `COOKED`).
  All access checks filter to your service code.

## 2. Provider-side prerequisites (must exist before you integrate)

1. A **service** registered in the subscription system with a unique **service code**
   (e.g. `COOKED`).
2. An **API key** issued to your app for the `X-Api-Key` header.
3. **Registration codes** generated for that service (single- or multi-use, with an
   optional expiry). By convention, codes whose name contains the substring `TRIAL` are
   treated as trial codes (see §7).

## 3. Configuration your backend needs

| Setting | Example | Notes |
|---|---|---|
| `subscription.base-url` | `https://subscription.example.com` | Subscription API base |
| `subscription.api-key` | _(secret)_ | Sent as `X-Api-Key`; inject via env, never commit |
| `subscription.service-code` | `COOKED` | Your service code |
| `subscription.gate.enabled` | `true` | `false` bypasses the gate for local dev (see §8) |

## 4. The two integration endpoints

### A. Provision/redeem at sign-up — `POST {base}/api/subscription/code`

Headers: `X-Api-Key: <key>`, `Content-Type: application/json`

Body:
```json
{
  "name": "<display name>",
  "doctorName": "<display name>",
  "email": "<email>",
  "password": "<password>",
  "registrationCode": "<code>"
}
```
> `doctorName` is a legacy field from the subscription API's origin domain — required by
> the endpoint. Cooked sends the display name for both `name` and `doctorName`.

This **upserts the client by email and redeems the code in one call**, granting your
service's access.

Responses:
- **2xx** → success (body ignored).
- **409 Conflict** → the email already has active access; the code is **not** consumed.
  **Treat as success** (idempotent — lets a retried/partial signup finish).
- **other 4xx** → rejection; surface the JSON `{"message": "..."}` to the user (fall back
  to a generic message if absent).
- **5xx / network error** → **abort the signup** with a "try again later" error.

### B. Access check at login — `GET {base}/api/clients/access/by-email?email=<email>`

Headers: `X-Api-Key: <key>`

Returns `200` with a JSON array of access records. The fields you need (ignore the rest):
```json
[
  { "serviceCode": "COOKED", "isActive": true, "regCode": "TRIAL-AB12-3CD" }
]
```
Decision logic:
- **Has access** ⇔ at least one record with `isActive == true` AND `serviceCode` equals
  your code (case-insensitive).
- **404** → no client / no access → **deny** (403).
- **other error / unreachable** → **deny** (503, fail-closed).

### (optional) Pre-check — same GET, non-throwing

Before redeeming a code at signup, you may call the same endpoint and, if active access
already exists, **skip redemption** and just create the local account. Any error returns
"no" so you fall through to normal redemption. (Cooked: `hasActiveAccess`.)

## 5. Required workflows

**Registration (passwordless, as Cooked does it):**
1. Reject if the email already exists locally.
2. (optional) Pre-check active access (§4 optional).
3. If no active access → `POST /api/subscription/code` (§4A). Abort on rejection.
4. Create the local user; generate a temporary password; email it (the user sets their own
   on first login).

**Login (password):**
1. Verify local credentials.
2. `GET .../access/by-email` (§4B). Deny if no active access.
3. Refresh any access-derived flags (e.g. trial) from the response, then issue your
   session / JWT.

**SSO login (e.g. Google):** identical gate — verify the IdP token, then run the **same**
access check on the email **before** find-or-creating the local user. The Google client ID
is only for verifying the IdP token; it does **not** grant access — the subscription gate
still applies.

## 6. Error-handling contract (must implement)

| Situation | Subscription response | Your behavior |
|---|---|---|
| Code valid, redeemed | 2xx | proceed |
| Email already has access | 409 | treat as success (idempotent) |
| Bad / expired / used code | 4xx + `{message}` | abort signup, show message |
| Subscription down at signup | 5xx / timeout | abort signup, "try later" |
| Active access at login | 200 + matching record | allow |
| No access at login | 404 / empty array | deny (403) |
| Subscription down at login | 5xx / timeout | **deny** (fail-closed) |

## 7. Trial convention (and its caveat)

Cooked infers "trial" by checking whether the matched record's `regCode` **contains
`TRIAL`** (case-insensitive). This is a **naming convention**, not a typed field — the
subscription `RegCode`/`ClientAccess` model has an `accessType` of `CODE` vs `SUBSCRIPTION`
but no explicit trial flag, and `accessEnd` is the only time bound. If you need robust
trial/tier semantics, prefer an explicit field over substring matching, or own the trial
logic on your side. (Cooked added its own per-user trial window — see
`feat/trial-full-access-window` — rather than relying on `accessEnd`.)

## 8. Security & ops requirements

- `X-Api-Key` is a **secret**: backend-only, injected via env, rotated as needed, never
  logged in full, never sent to the browser.
- All calls over **HTTPS** in production.
- Provide a **gate-disable switch** for local dev (`subscription.gate.enabled=false`) —
  when off, skip both calls and treat users as non-gated (Cooked returns "not trial").
  Production must keep it `true`.
- Log decisions (allow / deny + reason) but not secrets.

## 9. Integration checklist

- [ ] Service code + API key provisioned with the subscription team
- [ ] Backend config (base URL, key, service code, gate toggle) wired from env
- [ ] Sign-up calls `POST /api/subscription/code` with 409-as-success handling
- [ ] Login + SSO call `GET /api/clients/access/by-email` and **fail closed**
- [ ] Access decision filters to your service code + `isActive`
- [ ] Trial / tier handling decided (convention vs. explicit)
- [ ] Key kept server-side; HTTPS; dev bypass documented

---

## Reference implementation

- `cooked-service/src/main/java/com/humanworkstream/cooked/service/SubscriptionGateService.java`
  — `provisionViaCode` (§4A), `assertActiveAccess` (§4B), `hasActiveAccess` (§4 optional),
  fail-closed helpers, and the `AccessRecord` response subset.
- `cooked-service/src/main/java/com/humanworkstream/cooked/service/AppUserService.java`
  — `register`, `login`, `loginWithGoogle` (§5 workflows).
- Config keys: `cooked-service/src/main/resources/application.properties`
  (`subscription.*`).
