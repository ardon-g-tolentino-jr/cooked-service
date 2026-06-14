-- feat/trial-full-access-window — time-boxed trial access.
-- A trial account gets FULL access until cooked.app_user.trial_full_access_until;
-- after that the configurable trial limits (db/feat-trial-limits) apply. The window
-- is frozen per user at registration (now + cooked.trial.full-access-days).
-- Run as the admin user against the `cooked` database (DDL):
--
--   psql -U postgres -d cooked -f db/feat-trial-full-access/01_trial_full_access.sql
--
-- Idempotent: safe to re-run (IF NOT EXISTS / guarded backfill).

ALTER TABLE cooked.app_user
  ADD COLUMN IF NOT EXISTS trial_full_access_until TIMESTAMPTZ;

-- Backfill existing trial users with the current default window (14 days) so they get
-- a defined window rather than being treated as already-limited. Matches the default of
-- cooked.trial.full-access-days; adjust if you deploy a different default.
UPDATE cooked.app_user
   SET trial_full_access_until = created_at + INTERVAL '14 days'
 WHERE is_trial AND trial_full_access_until IS NULL;
