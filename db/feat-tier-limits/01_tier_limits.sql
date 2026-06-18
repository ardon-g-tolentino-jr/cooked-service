-- feat-tier-limits — per-tier feature settings, layered alongside the trial limits
-- Schema: cooked
--
--   psql -U postgres -d cooked -f db/feat-tier-limits/01_tier_limits.sql
--
-- Idempotent. The TIER LIST is owned by the subscription service (a tier = a COOKED plan
-- name); Cooked stores only a settings table of per-(tier, component) feature toggles + caps.
-- Independent of the trial_limit axis — both are enforced; either can block.

-- Cache the tier (subscription plan name) resolved at login by the subscription gate.
ALTER TABLE cooked.app_user
  ADD COLUMN IF NOT EXISTS tier TEXT;

-- Settings table: one row per (tier, component) the admin has configured. Sparse — a missing
-- row means the feature is allowed for that tier. `tier` is a free-form subscription plan name
-- (no local registry / FK; the subscription service is the source of truth for tiers).
--   access_enabled = false → users on this tier are blocked from the component
--   max_count      = N      → users on this tier may hold at most N items (NULL = unlimited)
CREATE TABLE IF NOT EXISTS cooked.tier_limit (
  tier           TEXT        NOT NULL,
  component      TEXT        NOT NULL,
  access_enabled BOOLEAN     NOT NULL DEFAULT true,
  max_count      INTEGER,
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (tier, component)
);

-- No tier rows are seeded here — the admin configures restrictions per tier in the app, and the
-- tier names are discovered from the subscription COOKED plans.

GRANT SELECT, INSERT, UPDATE, DELETE ON cooked.tier_limit TO cooked_user;
