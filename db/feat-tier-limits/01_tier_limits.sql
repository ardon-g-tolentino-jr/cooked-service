-- feat-tier-limits — per-tier (plan) access control, layered alongside the trial limits
-- Schema: cooked
--
--   psql -U postgres -d cooked -f db/feat-tier-limits/01_tier_limits.sql
--
-- Idempotent. Adds a tier registry, a per-(tier, component) limit matrix, and caches the
-- resolved subscription tier on each user. Independent of the existing trial_limit axis —
-- both are enforced; either can block.

-- Cache the tier (subscription plan name) resolved at login by the subscription gate.
ALTER TABLE cooked.app_user
  ADD COLUMN IF NOT EXISTS tier TEXT;

-- Registry of known tiers. `rank` orders them (higher = more access); used to pick the
-- winning tier when a user has multiple active accesses, and to define "upgrade" direction.
CREATE TABLE IF NOT EXISTS cooked.tier (
  name  TEXT PRIMARY KEY,            -- matches the subscription plan name
  label TEXT,                        -- human-friendly display label
  rank  INTEGER NOT NULL DEFAULT 0
);

-- Admin-configurable limits, one row per (tier, component).
--   access_enabled = false → users on this tier are blocked from the component
--   max_count      = N      → users on this tier may hold at most N items (NULL = unlimited)
CREATE TABLE IF NOT EXISTS cooked.tier_limit (
  tier           TEXT        NOT NULL REFERENCES cooked.tier(name) ON DELETE CASCADE,
  component      TEXT        NOT NULL,
  access_enabled BOOLEAN     NOT NULL DEFAULT true,
  max_count      INTEGER,
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (tier, component)
);

-- Seed the tiers. Names must match the subscription COOKED plan names.
INSERT INTO cooked.tier (name, label, rank) VALUES
  ('basic',   'Basic',   1),
  ('premium', 'Premium', 2)
ON CONFLICT (name) DO NOTHING;

-- Default per-tier limits for the defined components (config, not user data; idempotent).
-- Basic: capped recipes/pantry, no meal planner. Premium: everything unlimited.
INSERT INTO cooked.tier_limit (tier, component, access_enabled, max_count) VALUES
  ('basic',   'meal_plan',   false, NULL),
  ('basic',   'recipes',     true,  25),
  ('basic',   'pantry',      true,  50),
  ('basic',   'shopping',    true,  NULL),
  ('basic',   'history',     true,  NULL),
  ('basic',   'ingredients', true,  NULL),
  ('premium', 'meal_plan',   true,  NULL),
  ('premium', 'recipes',     true,  NULL),
  ('premium', 'pantry',      true,  NULL),
  ('premium', 'shopping',    true,  NULL),
  ('premium', 'history',     true,  NULL),
  ('premium', 'ingredients', true,  NULL)
ON CONFLICT (tier, component) DO NOTHING;

GRANT SELECT, INSERT, UPDATE, DELETE ON cooked.tier       TO cooked_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON cooked.tier_limit TO cooked_user;
