-- Cooked Service — full teardown
-- Run as a superuser:
--   psql -U postgres -d cooked -f db/cleanup.sql

SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE usename = 'cooked_user';

DROP SCHEMA IF EXISTS cooked_service CASCADE;

REVOKE CONNECT ON DATABASE cooked FROM cooked_user;
DROP USER IF EXISTS cooked_user;