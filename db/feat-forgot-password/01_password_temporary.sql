-- feat/forgot-password — temporary-password flag
-- Schema: cooked
--
--   psql -U postgres -d cooked -f db/feat-forgot-password/01_password_temporary.sql
--
-- Set true when a user's password was reset to a system-generated temp password;
-- cleared once they choose a new one. Drives the forced "set a new password" screen.

ALTER TABLE cooked.app_user
  ADD COLUMN IF NOT EXISTS password_temporary BOOLEAN NOT NULL DEFAULT false;
