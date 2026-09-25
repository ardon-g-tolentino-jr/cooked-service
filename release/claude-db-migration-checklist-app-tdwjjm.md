# claude/db-migration-checklist-app (cooked-service)

Design spec for **dbmig (DB Migration Assistant)**, a standalone React + Spring Boot application
for database migration work. It is stored here as a document only. There are no changes to
Cooked's code, schema or behaviour.

## What changed

### v0.2: scope, standalone packaging, DDL input

- **Databases:** MySQL, PostgreSQL and Oracle, across all commonly used versions, are all in the
  first release (PostgreSQL 9.6–17, MySQL 5.7–9.x, Oracle 11g R2–23ai). The spec adds a version
  matrix listing each version's catalog differences. SQL Server and MariaDB are removed.
- **DDL snapshots:** either side of a schema comparison can be an uploaded DDL script instead of
  a live connection. Accepted inputs are a single file, a `.zip`, or an ordered set of Flyway
  scripts. The DDL is parsed and replayed into the same model a live database produces; it is
  never executed. Common exports load without editing (`pg_dump -s`, `mysqldump --no-data`,
  `DBMS_METADATA`, Data Pump `SQLFILE`). A parse report lists every ignored or failed statement
  with its file and line. Snapshots can also be captured from a live connection to serve as a
  baseline.
- **Standalone:** a single repo and a single Docker image (UI plus API) with its own PostgreSQL,
  installed on Coolify or on-premises next to the databases. The API moves under `/api`, the
  admin account is created from environment variables on first start, and the app needs no
  internet access at runtime.
- **Fix scripts and target health checks** now cover all three dialects, including Oracle
  invalid objects, MySQL `AUTO_INCREMENT` alignment and Oracle sequence cache caveats.
- **Quick compare:** choose two sides and run a schema comparison without setting up a project.
- The delivery plan, risks and open questions are updated. The questions you answered are marked
  as resolved.

### v0.1

- **`docs/DB_MIGRATION_ASSISTANT_SPEC.md`**, which covers:
  - a phase-based migration checklist: 81 standard items, of which 22 are gates and 14 are
    auto-checks that pass or fail from comparison results
  - schema (DDL) comparison with waivers and fix-script generation
  - data comparison at three levels (row counts, column profiles, full streaming merge-join)
  - reports (PDF, XLSX, CSV, JSON, HTML) including a go/no-go readiness report
  - architecture, REST API, UI wireframes, security, testing, deployment and a milestone plan

## Verification

- The proposed `db/setup.sql` in Appendix A was run twice against PostgreSQL 16. It applied
  cleanly and the second run was a no-op (21 tables, 27 enum types).
- Run side-kind constraints behaved as specified:
  - Accepted: connection ↔ DDL and DDL ↔ DDL schema runs, a data run between two connections,
    and a health run against a target connection.
  - Rejected: a data run with a DDL side, a health run against a DDL target, a schema run with
    no source, a snapshot reference on a connection side, and a SID on a non-Oracle connection.
  - Deleting a snapshot keeps the runs that used it, with their labels.
- Also tested in v0.1: the job-claim query, the checklist `CHECK` constraints, the append-only
  audit grant, and the sequence-alignment query.
- All five Mermaid diagrams render with the Mermaid CLI.
- No DB migration for Cooked: this branch makes no schema change.
