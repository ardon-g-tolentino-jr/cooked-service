-- Migration: add password_hash to app_user
-- Branch: feat-complete-api
-- The canonical DDL omits the password column; this migration adds it
-- for existing databases running the original schema.

SET search_path TO cooked;

ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS password_hash TEXT;