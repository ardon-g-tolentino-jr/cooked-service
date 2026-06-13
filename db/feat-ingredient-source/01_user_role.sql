-- feat/ingredient-source — USER/ADMIN role on app_user.
-- Lets admins manage System (built-in) ingredients and users manage their own.
-- Run as the admin user against the `cooked` database (DDL):
--
--   psql -U postgres -d cooked -f db/feat-ingredient-source/01_user_role.sql
--
-- Idempotent.

ALTER TABLE cooked.app_user
  ADD COLUMN IF NOT EXISTS role TEXT NOT NULL DEFAULT 'USER'
  CHECK (role IN ('USER', 'ADMIN'));

-- Designate admins here (no promote/demote UI). Dev seed login is an admin:
UPDATE cooked.app_user SET role = 'ADMIN' WHERE email = 'chef@example.com';
