-- Cooked — administrator account creation (idempotent)
-- Schema: cooked   ·   PostgreSQL 14+
--
-- Step 2 of the from-scratch deploy. Run AFTER db/setup.sql, pasted into any
-- SQL console already connected to the `cooked` database (pgAdmin, DBeaver,
-- the Coolify/hosting DB console, psql — no \meta-commands, plain SQL only).
--
-- Creates the platform administrator:
--   email:    humanworkstream@gmail.com
--   password: Password123!   (BCrypt, cost 10 — CHANGE IT after first sign-in)
--   role:     ADMIN
--
-- To use a different password, replace the hash on the password_hash line with
-- your own BCrypt hash (cost 10), e.g.:
--   htpasswd -nbBC 10 admin 'YourPassword' | cut -d: -f2
-- Spring Security accepts $2a / $2b / $2y prefixes.
--
-- Idempotent: re-running never duplicates the account and never clobbers a
-- password you rotated later (the row is only inserted when the email is
-- absent), but it always re-asserts the ADMIN role.

SET search_path TO cooked;

-- 1. Create the admin — only when the email is not already registered.
INSERT INTO app_user (email, display_name, handle, password_hash, password_temporary, role)
VALUES (
  'humanworkstream@gmail.com',
  'Administrator',
  'admin',
  '$2a$10$QJKBFo.lxwcQ7jpcs.loHumqcRSyV.YiQAFH9YzG80JmCR9ZgNiQO',  -- Password123!
  false,
  'ADMIN'
)
ON CONFLICT (email) DO NOTHING;

-- 2. Guarantee ADMIN privileges even if the account predated this script.
UPDATE app_user SET role = 'ADMIN' WHERE email = 'humanworkstream@gmail.com';

-- 3. Seed a default settings row for the admin (harmless if one already exists).
INSERT INTO user_settings (user_id)
SELECT id FROM app_user WHERE email = 'humanworkstream@gmail.com'
ON CONFLICT (user_id) DO NOTHING;