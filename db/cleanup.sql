-- Cooked — full teardown
-- Run as a superuser:
--   psql -U postgres -d cooked -f db/cleanup.sql

SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE usename IN ('cooked_user', 'cooked_readonly');

DROP SCHEMA IF EXISTS cooked CASCADE;

DO $$ BEGIN
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'cooked_user') THEN
    REVOKE CONNECT ON DATABASE cooked FROM cooked_user;
    DROP USER cooked_user;
  END IF;
END $$;

DO $$ BEGIN
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'cooked_readonly') THEN
    REVOKE CONNECT ON DATABASE cooked FROM cooked_readonly;
    DROP USER cooked_readonly;
  END IF;
END $$;
