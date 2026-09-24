# claude/db-migration-checklist-app (cooked-service)

Design spec for **dbmig (DB Migration Assistant)**, a new, separate React + Spring Boot
application for database migration work. It is stored here as a document only. There are no
changes to Cooked's code, schema or behaviour.

## What changed

- **`docs/DB_MIGRATION_ASSISTANT_SPEC.md`** (new) covers:
  - a phase-based **migration checklist** (81 standard items: 22 gates and 13 auto-checks that
    pass or fail from comparison results)
  - cross-dialect **schema (DDL) comparison** with waivers and PostgreSQL fix-script generation
  - **data comparison** at three levels: row counts, column profiles, and a full streaming
    merge-join
  - **reports** in PDF, XLSX, CSV, JSON and HTML, including a go/no-go readiness report
  - architecture, REST API, UI wireframes, security (read-only sessions, JDBC hardening,
    encrypted credentials), testing, deployment and a milestone plan
- The spec reuses Cooked's conventions (package layout, JWT, PostgreSQL enums, `db/setup.sql`
  as the canonical schema, UI primitives) so dbmig can be built and deployed the same way.

## Verification

- The proposed `db/setup.sql` in Appendix A was run twice against PostgreSQL 16. It applied
  cleanly and the second run was a no-op (19 tables, 24 enum types).
- Also tested against that database: the job-claim query (`FOR UPDATE SKIP LOCKED`), the
  status-note and evidence `CHECK` constraints, the append-only `audit_log` grant for
  `dbmig_user`, and the sequence-alignment health query (it correctly flags sequences that were
  never used after explicit-ID inserts).
- No DB migration for Cooked: this branch makes no schema change.
