# feat/recipe-seed-catalog — UI catalog + recipe seeds (relocated from cooked-ui)

## What changed

Per the rule that **all DB scripts live in the backend repo**, the UI catalog
seed and the recipe seeds that previously lived in `cooked-ui/db/` are relocated
here:

- `db/seed_catalog.sql` — the cooked-ui built-in catalog: moods, cuisines (incl.
  `Filipino`), the full ingredient catalog (80 ingredients), the built-in +
  community recipes **including the 16 Filipino classics**, and the demo pantry.
  Matches the cooked-ui frontend vocabulary (`src/data/catalog.ts`: capitalized
  moods, `CUISINES`). Relocated verbatim from `cooked-ui/db/seed.sql`.
- `db/japanese_recipes.sql` — the 15 Japanese community recipes + 16 Japanese
  pantry ingredients + the `Japanese` cuisine. Relocated verbatim from
  `cooked-ui/db/japanese_recipes.sql`.

Both are seeded as **community** recipes (owner NULL, `is_community`) because the
backend only serves `owner = me OR is_community = true`
(`RecipeRepository.findVisibleToUser`); plain built-ins are never returned.

The matching cooked-ui branches (`feat/filipino-recipes`, `feat/japanese-recipes`)
now keep only the frontend changes (`src/data/catalog.ts` `CUISINES`) — their SQL
was removed in favor of these files.

## How to apply

```bash
psql -U postgres -d cooked -f db/setup.sql            # schema
psql -U postgres -d cooked -f db/seed.sql             # demo login user
psql -U postgres -d cooked -f db/seed_catalog.sql     # UI catalog + built-in/community/Filipino recipes
psql -U postgres -d cooked -f db/japanese_recipes.sql # Japanese recipes (after seed_catalog.sql)
```

All idempotent (`ON CONFLICT DO NOTHING` / `WHERE NOT EXISTS`).

## Verification

- Both files re-run against the running backend DB under `ON_ERROR_STOP=1`: all
  statements idempotent no-ops (data already validated end-to-end earlier — 16
  Filipino + 15 Japanese community recipes served by `GET /recipes?scope=visible`,
  every ingredient resolves, calories sane).

## Notes

- `seed_catalog.sql` uses the cooked-ui frontend vocabulary (capitalized moods,
  its `CUISINES`/ingredient names) and coexists with this repo's existing
  `db/seed.sql` (the small `chef@example.com` demo set with lowercase moods) — the
  two vocabularies are not yet unified; that remains a separate consolidation.
