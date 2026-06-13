# feat/subscription-access-gate (cooked-service)

Enforce that only users with active **COOKED** access in the Subscription
service can register/log in. All subscription calls are server-to-server with an
`X-Api-Key` header (never exposed to the browser).

## What changed

- **`SubscriptionGateService`** (new) — `RestClient` wrapper around two
  API-key-only subscription endpoints:
  - `provisionViaCode(...)` → `POST /api/subscription/code` — validates + redeems
    the registration code, creating/reusing the subscription-side client by email
    and granting COOKED access. Rejection → `400` with the upstream message;
    unreachable → `503`.
  - `assertActiveAccess(email)` → `GET /api/clients/access/by-email` — allows in
    only if an active record has `serviceCode == COOKED`. No record → `403`;
    unreachable/error → `503` (**fail-closed**).
  - Honors `subscription.gate.enabled` (set `false` to bypass in local dev).
- **`AppUserService.register`** — calls `provisionViaCode(...)` (after the
  duplicate-email check, before persisting the local user). Code must be valid or
  signup aborts.
- **`AppUserService.login`** — calls `assertActiveAccess(...)` after the password
  check.
- **`RegisterRequest`** — added optional `registrationCode` (enforced by the gate
  when enabled, so dev with the gate off still works).
- **Config** (`application.properties`) — `subscription.base-url`,
  `subscription.api-key`, `subscription.service-code` (default `COOKED`),
  `subscription.gate.enabled` (default `true`). Dev values in
  `application-dev.properties` point at the local subscription service on `:8080`.
- **`AuthControllerTest`** — updated the `RegisterRequest` constructor call for the
  new field. The subscription-side account reuses the user's chosen password.

## Prerequisite (subscription service — one-time)

The gate filters on service code `COOKED`, which must exist in the subscription
DB. Run against the subscription DB (`subscription_mgmt` schema):

```sql
INSERT INTO subscription_mgmt.services (name, code, description, is_active)
VALUES ('Cooked', 'COOKED', 'Pantry-first recipe app', TRUE)
ON CONFLICT (code) DO NOTHING;
```

Then generate test registration codes (admin JWT required):

```
POST /api/reg-codes/batch
{ "serviceId": <COOKED service id>, "quantity": 5,
  "batchRef": "cooked-launch", "createdBy": "admin" }
```

Existing Cooked users (e.g. the seed `chef@example.com`) must be provisioned a
COOKED access record, or they will be locked out under fail-closed. For local
dev you can instead set `SUBSCRIPTION_GATE_ENABLED=false`.

## Verification

- `mvn clean package` — BUILD SUCCESS, 16/16 tests pass.
