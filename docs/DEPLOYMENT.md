# Cooked — Deploy from Scratch

This guide deploys a brand-new Cooked database and backend. The entire database
is built from **three idempotent SQL scripts**, run in order. Every script is
plain SQL (no `psql` meta-commands), so you can paste each one straight into a
**database console** — pgAdmin, DBeaver, IntelliJ, or the Coolify / hosting
SQL console — and click *Run*. A terminal is not required.

| # | Script | What it does | Run as |
|---|--------|--------------|--------|
| 1 | `db/setup.sql` | Schema, login roles, enum types, all tables, indexes, grants, trial-limit config | DB superuser / owner (`postgres`) |
| 2 | `db/account_creation.sql` | The platform **admin** account `humanworkstream@gmail.com` | DB superuser / owner (`postgres`) |
| 3 | `db/seed.sql` | Vocabularies, curated ingredient catalog, built-in + community + Filipino + Japanese recipes, demo pantry | DB superuser / owner (`postgres`) |

All three are **idempotent** — safe to re-run. Re-running never duplicates rows
and never resets passwords you have rotated.

---

## Prerequisites

- **PostgreSQL 14+** with a database named `cooked`.
- A SQL console connected to that database with superuser/owner rights
  (the role that runs DDL — usually `postgres`).
- Java 17 + Maven, only when you reach [§5 Run the backend](#5-run-the-backend).

---

## 1. Create the database

If the `cooked` database does not exist yet, create it **first** (this one
statement must run while connected to another DB, e.g. `postgres`):

```sql
CREATE DATABASE cooked;
```

Then point your console at the new `cooked` database for every step below.

---

## 2. Step 1 — `db/setup.sql` (schema, roles, DDL)

Open `db/setup.sql`, copy its full contents into the console, and run it.

It creates:

- the login roles `cooked_user` (app CRUD) and `cooked_readonly` (reporting),
- the `cooked` schema and the `unit_type` enum,
- every table + index,
- the default `trial_limit` configuration rows,
- all grants and default privileges.

**Set the real role passwords immediately** (the script creates them with a
placeholder). Run once, in the same console:

```sql
ALTER ROLE cooked_user     PASSWORD '<app password>';      -- must match CUSTOM_DB_PASS
ALTER ROLE cooked_readonly PASSWORD '<readonly password>';
```

> Re-running `setup.sql` later never re-creates existing roles, so these rotated
> passwords are preserved.

---

## 3. Step 2 — `db/account_creation.sql` (admin user)

Copy the full contents of `db/account_creation.sql` into the console and run it.
This creates the administrator:

| Field | Value |
|-------|-------|
| Email | `humanworkstream@gmail.com` |
| Password | `Password123!` |
| Role | `ADMIN` |

**Change the password after the first sign-in.** To deploy with a different
password instead, replace the `password_hash` value in the script with your own
BCrypt hash (cost 10) before running it — for example:

```bash
htpasswd -nbBC 10 admin 'YourPassword' | cut -d: -f2
```

Spring Security accepts `$2a` / `$2b` / `$2y` prefixes.

---

## 4. Step 3 — `db/seed.sql` (application data)

Copy the full contents of `db/seed.sql` into the console and run it. This loads:

- moods + cuisines (matching the frontend vocabulary),
- the curated built-in ingredient catalog (incl. Filipino & Japanese staples),
- built-in recipes, community recipes, 16 Filipino classics, 15 Japanese classics,
- a display-only demo user `demo@cooked.local` with a starter pantry.

### Optional — full USDA ingredient catalog

For the complete 6,946-ingredient database, also run `db/seed_ingredients.sql`
(same way — paste and run). It is idempotent and independent of `seed.sql`.

### Optional — later feature migrations

Per-branch migrations live in `db/<branch-name>/NN_*.sql`. For a fresh install
from these three scripts you do **not** need them — `setup.sql` already includes
every schema change. They exist only for upgrading older databases.

---

## 5. Run the backend

Configure environment (copy `.env.example` → `.env` and fill in values). Key
variables:

| Variable | Notes |
|----------|-------|
| `CUSTOM_DB_URL` | `jdbc:postgresql://<host>:5432/cooked` |
| `CUSTOM_DB_USER` | `cooked_user` |
| `CUSTOM_DB_PASS` | must match the `ALTER ROLE cooked_user` password from §2 |
| `JWT_SECRET` | at least 32 characters |
| `SERVER_PORT` | defaults to `8082` |

Build and run:

```bash
mvn package -DskipTests
mvn spring-boot:run
```

Verify it is up:

```bash
curl http://localhost:8082/healthcheck
curl http://localhost:8082/db/healthcheck
curl http://localhost:8082/db/schema/healthcheck
```

Then sign in at the frontend (or `POST /auth/login`) with
`humanworkstream@gmail.com` / `Password123!`.

> If the subscription access gate is ON (default), the login user needs active
> `COOKED` access on the subscription side, **or** set
> `SUBSCRIPTION_GATE_ENABLED=false` for local dev. See `README.md` §6.

---

## One-shot with Docker Compose (local only)

`docker-compose.yml` automates the same flow for local development. On first
boot it runs, in order: `setup.sql` → `account_creation.sql` → `seed.sql` →
`seed_ingredients.sql` (against a fresh Postgres volume).

```bash
cp .env.example .env   # fill in DB_PASSWORD, JWT_SECRET, etc.
docker compose up --build
```

The init scripts only run when the Postgres data volume is empty. To re-seed
from scratch, remove the volume first (`docker compose down -v`).

---

## Quick reference (psql)

If you prefer a terminal, the same three steps are:

```bash
createdb cooked                                        # or: CREATE DATABASE cooked;
psql -U postgres -d cooked -f db/setup.sql
psql -U postgres -d cooked -f db/account_creation.sql
psql -U postgres -d cooked -f db/seed.sql
psql -U postgres -d cooked -f db/seed_ingredients.sql  # optional full catalog
```