# cooked-service — Setup Guide

Backend REST API for **Cooked** (pantry-first recipe app). Spring Boot 3.3.5 / Java 17,
PostgreSQL (`cooked` schema). Package root: `com.humanworkstream.cooked`.

> See `CLAUDE.md` for architecture/coding conventions and `db/` for all schema, seed, and
> migration SQL (this repo is the canonical home for Cooked's database scripts).

---

## 1. Prerequisites

| Tool | Version | Notes |
|---|---|---|
| JDK | **17** (Amazon Corretto) | Maven on this machine defaults to Java 23, which breaks Lombok — always use the Java 17 `JAVA_HOME` below. |
| Maven | 3.9+ | Wrapper not included; use system `mvn`. |
| PostgreSQL | 14+ | Local instance for dev. |
| subscription-service | running on `:8080` | Only needed if the **access gate** is enabled (default). See §6. |

```bash
export JAVA_HOME="/Users/ardontolentinojr./Library/Java/JavaVirtualMachines/corretto-17.0.10/Contents/Home"
```

---

## 2. Database setup

All SQL lives in `db/`. A from-scratch deploy is **three idempotent scripts**, run in
order against the `cooked` database — see [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) for the
full step-by-step guide (incl. browser/DB-console instructions).

```bash
createdb cooked                                # or: CREATE DATABASE cooked;
psql -U postgres -d cooked -f db/setup.sql            # 1. schema, roles, types, tables, grants
psql -U postgres -d cooked -f db/account_creation.sql # 2. admin user (humanworkstream@gmail.com)
psql -U postgres -d cooked -f db/seed.sql             # 3. catalog + sample/community recipes
```

`setup.sql` is idempotent and creates the roles (`cooked_user`, `cooked_readonly`) with
placeholder passwords. **Set the real passwords immediately:**

```sql
ALTER ROLE cooked_user     PASSWORD '<app password>';      -- match CUSTOM_DB_PASS
ALTER ROLE cooked_readonly PASSWORD '<readonly password>';
```

Optional bulk catalog (6,946 USDA ingredients), if you want the full ingredient database:

```bash
psql -U postgres -d cooked -f db/seed_ingredients.sql
```

Per-branch migrations live in `db/<branch-name>/NN_*.sql` — apply any that post-date your
schema (e.g. `db/feat-ingredient-source/01_user_role.sql`,
`db/feat-complete-api/01_add_password_hash.sql`, `db/feat-meal-planner/01_meal_plan.sql`,
`db/feat-pantry-templates/01_pantry_template.sql`,
`db/feat-trial-full-access/01_trial_full_access.sql`,
`db/feat-tier-limits/01_tier_limits.sql`).

> **Subscription tiers** (`feat-tier-limits`): `db/feat-tier-limits/01_tier_limits.sql` adds the
> sparse `tier_limit` settings table (per `(tier, component)`; a missing row = allowed) and an
> `app_user.tier` column. The **tier list is owned by subscription-service** — a tier is a COOKED
> plan name; the gate resolves the user's tier at login (highest-priced active plan via
> `GET /api/plans?serviceCode=COOKED`) into the JWT `tier` claim, and `GET /tiers` proxies the list
> for the admin matrix. Enforced independently of the trial axis. The COOKED service and its plan
> tiers are created in the **subscription admin UI** (not seeded) and discovered at runtime.

Admin login: **`humanworkstream@gmail.com` / `Password123!`** (`ADMIN` — change the password
after first sign-in). `seed.sql` also creates a display-only demo user (`demo@cooked.local`)
with a starter pantry.
> With the access gate ON (default), the login user must have active `COOKED` access on the
> subscription side, **or** set `SUBSCRIPTION_GATE_ENABLED=false` for local dev (see §6).

---

## 3. Configuration

Dev config lives in `src/main/resources/application-dev.properties` (**gitignored** — not in
the repo; create it locally). The `dev` profile is active by default. Minimal file:

```properties
CUSTOM_DB_URL=jdbc:postgresql://localhost:5432/cooked
CUSTOM_DB_USER=cooked_user
CUSTOM_DB_PASS=Password123!
JPA_SHOW_SQL=true
JPA_FORMAT_SQL=true
JWT_SECRET=<any 32+ char string for dev>

# Access gate → local subscription-service on :8080
SUBSCRIPTION_BASE_URL=http://localhost:8080
SUBSCRIPTION_API_KEY=<subscription dev api.key>
SUBSCRIPTION_SERVICE_CODE=COOKED
SUBSCRIPTION_GATE_ENABLED=true        # set false to bypass the gate entirely in local dev
```

### Environment variables (full list)

| Variable | Required | Default | Purpose |
|---|---|---|---|
| `CUSTOM_DB_URL` | ✅ | — | JDBC URL to the `cooked` database |
| `CUSTOM_DB_USER` | ✅ | — | App DB user (`cooked_user`) |
| `CUSTOM_DB_PASS` | ✅ (secret) | — | App DB password |
| `JWT_SECRET` | ✅ (secret) | — | HS256 signing key, **≥ 32 chars** |
| `SUBSCRIPTION_BASE_URL` | ✅ in prod | `http://localhost:8080` | Subscription-service base URL |
| `SUBSCRIPTION_API_KEY` | ✅ in prod (secret) | _(empty)_ | `X-Api-Key` for the gate; must equal the subscription deployment's key. Empty → gate fails closed |
| `SPRING_PROFILES_ACTIVE` | recommended | `dev` | Set to `prod` in production (avoids loading dev props) |
| `SERVER_PORT` | — | `8082` | HTTP port |
| `JWT_EXPIRATION_MS` | — | `86400000` | Token lifetime (24h) |
| `SUBSCRIPTION_SERVICE_CODE` | — | `COOKED` | Service code filtered for access |
| `SUBSCRIPTION_GATE_ENABLED` | — | `true` | `false` disables the access gate |
| `COOKED_TRIAL_FULL_ACCESS_DAYS` | — | `14` | Days a trial account has full access before trial limits apply (frozen per user at registration) |
| `GOOGLE_CLIENT_ID` | — | shared subscription client | OAuth client the Google ID token is verified against |
| `MAIL_USERNAME` | ✅ in prod (secret) | _(empty)_ | SMTP/Gmail account used to send the welcome + password-reset emails. **Blank → emails are not sent**; the temp password is logged instead |
| `MAIL_PASSWORD` | ✅ in prod (secret) | _(empty)_ | SMTP/Gmail **app password** for `MAIL_USERNAME` |
| `MAIL_HOST` / `MAIL_PORT` | — | `smtp.gmail.com` / `587` | SMTP server (STARTTLS) |
| `MAIL_FROM` | — | `Cooked <humanworkstream@gmail.com>` | `From` header on outgoing mail |
| `MAIL_ENABLED` | — | `true` | `false` logs the temp password instead of sending |
| `APP_UI_BASE_URL` | — | `https://cooked.humanworkstream.com` | Public UI URL used for the "Sign in" button in emails |
| `JPA_SHOW_SQL` / `JPA_FORMAT_SQL` | — | `false` | SQL logging |

---

## 4. Build & run

```bash
export JAVA_HOME="/Users/ardontolentinojr./Library/Java/JavaVirtualMachines/corretto-17.0.10/Contents/Home"

mvn clean package          # build + run tests (fails on test failure)
mvn spring-boot:run        # run (dev profile, :8082)
mvn test -Dtest=ClassName  # single test class
```

API base: `http://localhost:8082`. Health: `GET /healthcheck`.

---

## 5. Auth endpoints

| Endpoint | Auth | Purpose |
|---|---|---|
| `POST /auth/register` | public | Passwordless signup (`{ displayName, email, registrationCode }`). Issues a system-generated temporary password by email; returns `201 { message }`, not a session. Requires a `registrationCode` redeemed on the subscription side. |
| `POST /auth/login` | public | Email+password login. Gated on active `COOKED` access. |
| `POST /auth/google` | public | Google SSO — verifies the ID token, applies the same gate, find-or-creates the user. Body `{ idToken }`. |

All issue a Cooked JWT (`AuthResponse { token, userId, email, displayName, role }`).

---

## 6. Integrations

### Subscription access gate
Only users with active `COOKED` access in the subscription-service (redeemed registration
code **or** active subscription) can register/log in. Enforced server-to-server with
`SUBSCRIPTION_API_KEY` (never exposed to the browser); **fail-closed** — any error denies
entry. Prerequisites: a `COOKED` row in the subscription DB's `subscription_mgmt.services`,
and provisioned access for each user. Disable locally with `SUBSCRIPTION_GATE_ENABLED=false`.

### Google SSO
`GoogleTokenVerifier` validates the ID token against `google.client-id` (defaults to the
shared subscription OAuth client). The browser origin (e.g. `cooked.humanworkstream.com`,
`http://localhost:5173`) must be an **Authorized JavaScript origin** on that OAuth client in
Google Cloud Console. SSO users are subject to the same access gate.

---

## 7. Production deployment

Hostinger VPS via **Dokploy**, Nixpacks build, production branch `main`. Set the env vars
from §3 (Required + `SPRING_PROFILES_ACTIVE=prod`) in Dokploy. Ensure the subscription side
has the `COOKED` service + provisioned users, and the Google OAuth client lists the prod
origin — otherwise logins fail closed.
