# chore/db-canonical-setup — db/setup.sql becomes the single canonical schema

## Summary

There were two diverging `setup.sql` files (cooked-ui and this repo). The
cooked-ui copy is removed; **`db/setup.sql` in this repo is now the single
source of truth** for the `cooked` schema. All future schema changes and
per-branch migrations happen here.

## Changes

- **`db/setup.sql`** replaced with the merged canonical schema. It combines
  the richer cooked-ui definition with this repo's auth column:
  - CHECK constraints, secondary indexes, `IDENTITY (START WITH 100000)`,
    partial unique index on built-in recipe names, schema-qualified DDL;
  - `app_user.password_hash` in the base schema (previously added only by
    `db/feat-complete-api/01_add_password_hash.sql`, which remains for
    existing databases);
  - roles: `cooked_user` (app, CRUD) and new `cooked_readonly` (SELECT only),
    created **only if missing** with `CHANGE_ME` placeholder passwords —
    re-running never drops roles or resets rotated passwords (the previous
    script dropped and recreated `cooked_user` on every run);
  - default privileges so future tables inherit grants; `search_path`
    pinned to `cooked, public` for both roles.
- **`db/cleanup.sql`** fixed: it dropped the nonexistent `cooked_service`
  schema — now drops `cooked`, tears down both roles, and is safe to re-run.
- **`CLAUDE.md`** updated: setup.sql marked canonical for both repos; stale
  schema name `cooked_service` corrected to `cooked`.

## Migration impact

None for existing databases — no DDL change to a running `cooked` schema.
`setup.sql` is idempotent (`CREATE … IF NOT EXISTS`) and harmless to re-run.
Note for Docker Compose: the init script now creates `cooked_user` with a
`CHANGE_ME` password; on first boot of a fresh volume, set the real password
(`ALTER ROLE cooked_user PASSWORD '…'`) to match `CUSTOM_DB_PASS`.

## Verification

Tested in a throwaway Postgres 14 (Alpine) container with `ON_ERROR_STOP`:
fresh install, second run (idempotency), `db/seed.sql` on top (demo user
`chef@example.com` present with password hash), and `db/cleanup.sql`
teardown + re-run all passed.

## Known follow-up (not in this change)

`db/seed.sql` here and cooked-ui's `db/seed.sql` contain different catalogs
(small demo set with lowercase moods vs. the full built-in catalog the UI
expects, with capitalized moods). They should be consolidated here in a
follow-up branch.
