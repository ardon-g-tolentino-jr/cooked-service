-- feat/trial-limits — TRIAL tier access control
-- Schema: cooked
--
--   psql -U postgres -d cooked -f db/feat-trial-limits/01_trial_access.sql
--
-- Idempotent. Adds a per-user trial flag and an admin-configurable table of
-- per-component limits applied only to trial users.

-- Mark which users are on a TRIAL registration code.
ALTER TABLE cooked.app_user
  ADD COLUMN IF NOT EXISTS is_trial BOOLEAN NOT NULL DEFAULT false;

-- Admin-configurable limits, one row per gated component.
--   access_enabled = false  → trial users are blocked from the component
--   max_count      = N       → trial users may hold at most N items (NULL = unlimited)
CREATE TABLE IF NOT EXISTS cooked.trial_limit (
  component      TEXT PRIMARY KEY,
  access_enabled BOOLEAN     NOT NULL DEFAULT true,
  max_count      INTEGER,
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Seed the defined components with sensible trial defaults.
INSERT INTO cooked.trial_limit (component, access_enabled, max_count) VALUES
  ('meal_plan',   false, NULL),
  ('recipes',     true,  10),
  ('pantry',      true,  NULL),
  ('shopping',    true,  NULL),
  ('history',     true,  NULL),
  ('ingredients', true,  NULL)
ON CONFLICT (component) DO NOTHING;

GRANT SELECT, INSERT, UPDATE, DELETE ON cooked.trial_limit TO cooked_user;
