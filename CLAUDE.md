# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Tech Stack

- **Backend:** Java 17 · Spring Boot 3.3.5 · PostgreSQL · Spring Security + JWT (jjwt 0.12.6) · Lombok
- **Frontend:** React 18 · TypeScript · Vite (refer to appointment-web for component and build structure)
- **Infra:** Hostinger VPS · Coolify · Nixpacks build type

## Repository

```
git@github.com:ardon-g-tolentino-jr/cooked-service.git
```
Production branch: `main`

## Reference Projects

| Project | Path |
|---|---|
| Frontend structure & component patterns | `/Users/ardontolentinojr./Documents/CLAUDE DEV/DEVELOPMENT/APPOINTMENTS/appointment-web` |
| Subscription API | `/Users/ardontolentinojr./Documents/CLAUDE DEV/DEVELOPMENT/SUBSCRIPTION/subscription-service` |

## Commands

```bash
# Build (skip tests)
mvn package -DskipTests

# Run locally (requires env vars — see .env.example)
mvn spring-boot:run

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=RecipeControllerTest

# Build + run with Docker Compose (includes Postgres, auto-runs db/setup.sql + db/seed.sql)
cp .env.example .env   # fill in values first
docker compose up --build
```

All sensitive config is injected via environment variables. Copy `.env.example` to `.env` and populate before running locally. Spring Boot's relaxed binding maps `UPPER_SNAKE_CASE` env vars to `application.properties` keys automatically.

**Local dev override:** Create `src/main/resources/application-dev.properties` to override any property for local runs (this file is gitignored). The active profile defaults to `dev` unless `SPRING_PROFILES_ACTIVE` is set.

**Port conflict:** If port 8082 is already in use locally, find and kill it — do not change the port in config.

## Development Process

Every change follows this sequence:

1. **Branch first** — create a feature or bugfix branch before making any changes.
2. **Migration script** — create `db/<branch-name>/<number>_<description>.sql` for any schema change (e.g. `db/feat-recipes-tags/01_add_tag_table.sql`). Scripts must be idempotent (`CREATE TABLE IF NOT EXISTS`, `INSERT … ON CONFLICT DO NOTHING`).
3. **Update setup.sql** — keep `db/setup.sql` current as the canonical fresh-install schema.
4. **Verify `/dist`** — always confirm the `/dist` folder is current and correct before committing frontend changes.
5. **Release notes** — create `release/<branch-name>.md` with a summary of what changed and why.
6. **Local testing only** — test against a local DB and local backend. Do not spin up remote instances for testing.
7. **No mock data when a backend is running** — if the backend is defined and running locally, test against it directly.

## Coding Standards

### Frontend (React)
- Refer to `appointment-web` for component structure, naming, and layout patterns.
- Reuse existing UI primitives (`src/components/ui/`) — `Btn`, `Card`, `Modal`, `Pill`, `Avatar`, `Field`/`Input`/`Textarea`/`Select`, `SectionHead`, `LoadShell` — before introducing new ones.
- All styling uses inline `style` props + CSS variables from `src/styles.css`. Use `var(--md-primary)`, `var(--md-ink)`, `var(--md-line)`, `var(--md-bg)` etc.; never hardcode colors.
- API calls go through `src/api/client.ts`. After any mutation call `refetch()` on the relevant hook — no optimistic updating.
- `src/types.ts` is the single source of truth for domain types. DTO → domain transforms live in `src/api/queries.ts`.
- Run `npm run typecheck` after every TypeScript change; the build fails on type errors.

### Backend (Spring Boot)
- One controller per domain. Services log with `[ServiceName]` prefix, e.g. `[RecipeService] Patching recipe id={}`.
- PATCH methods use null-field-skipping — only non-null fields in the body are applied.
- PostgreSQL custom enum columns require both a `PostgresEnumConverters` inner class (`autoApply = true`) and `@ColumnTransformer(write = "?::enum_type_t")` on the entity field.
- Ownership of user-scoped data always comes from `SecurityUtils.getCurrentUserId()` (JWT), never from the request body.

## Architecture

**Port:** 8082 (overridable via `SERVER_PORT`)

**Package layout:** `com.humanworkstream.cooked`
- `controller/` — REST layer, one controller per domain
- `service/` — business logic
- `repository/` — Spring Data JPA repositories
- `entity/` — JPA entities
- `enumeration/` — Java enums that mirror PostgreSQL custom enum types
- `config/` — Spring configuration beans (`SecurityConfig`, `CorsConfig`, `PostgresEnumConverters`)
- `security/` — JWT filter, token util, principal record, security helpers
- `dto/` — request/response records (Java records with Bean Validation annotations)

### Database

Schema name: `cooked_service` (set globally via `spring.jpa.properties.hibernate.default_schema`).  
`ddl-auto=none` — schema is managed externally via `db/setup.sql`; Hibernate never creates or alters tables. Identity columns start at 100000.

### Authentication & Authorization

Stateless JWT auth via `JwtAuthenticationFilter` → `UserPrincipal(email, userId)`. Passwords are BCrypt-hashed locally; no external auth service.

Public (no-JWT) endpoints:
- `POST /auth/register`, `POST /auth/login`
- `/healthcheck`, `/db/healthcheck`, `/db/schema/healthcheck`

### Domain Model

- **User → Recipe**: ownership comes from the JWT, not the request.
- **Recipe → Ingredient**: ordered by `position`; deleting a recipe cascades to its ingredients at both the service layer and via `ON DELETE CASCADE` in the schema.

### Testing

Controller tests use `@WebMvcTest` with security auto-configuration excluded and services mocked via `@MockBean`. `JwtUtil` must be `@MockBean` in every controller test because `JwtAuthenticationFilter` (a component-scanned `Filter`) depends on it at context startup.

Seed credentials (from `db/seed.sql`): `chef@example.com` / `Password123!`

## Working Style

Suggestions for process and code improvements are welcome — raise them proactively.  
Before deleting any component or class, flag any other components that depend on it so the impact is clear before proceeding.
