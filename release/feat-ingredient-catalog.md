# feat-ingredient-catalog

## Summary

Adds a full builtin-ingredient catalog (`db/seed_ingredients.sql`, 6,946 rows)
generated from the USDA FoodData Central **SR Legacy** dataset (April 2018
release, https://fdc.nal.usda.gov/download-datasets), and corrects two
miskeyed calorie values in the demo seed.

## What changed

### New: `db/seed_ingredients.sql`
- 6,946 ingredients with `kcal_per_gram` = FDC Energy (kcal per 100 g) ÷ 100.
- FDC food categories mapped to app categories: Meat, Vegetable, Baked, Grain,
  Beverage, Sweet, Fruit, Dairy & Eggs, Legume, Seafood, Condiment, Oil,
  Snack, Other, Nuts & Seeds, Spice.
- Excluded non-ingredient FDC categories: baby foods, fast foods, restaurant
  foods, prepared meals/entrees, quality-control materials.
- Ids 1001–7946 (below the 100000 identity floor for app-created rows);
  `is_builtin = TRUE`, `grams_per_piece` left NULL (FDC portion data is
  serving-based, not piece-based).
- Idempotent (bare `ON CONFLICT DO NOTHING`, absorbing both id and name
  collisions) — serves as its own migration for existing databases; just run
  the file.

### Fixed: `db/seed.sql` (values verified against FDC)
- Soy sauce `kcal_per_gram` 0.006 → **0.530** (FDC 174277: 53 kcal/100 g).
- Red wine `kcal_per_gram` 0.070 → **0.850** (FDC 173190: 85 kcal/100 g).
- Ingredient sequence floor raised 1000 → 100000 so app-created ingredient ids
  can never collide with catalog ids.
- All seed inserts switched from targeted conflict clauses (`ON CONFLICT
  (email)`, `(name)`, `(id)`) to bare `ON CONFLICT DO NOTHING`, so a violation
  of *any* unique constraint (id, email, handle, name) is skipped instead of
  erroring. Both seed files plus `setup.sql` verified by running each twice
  against a clean Postgres 18 instance with `ON_ERROR_STOP=1`.

### `docker-compose.yml`
- Mounts `db/seed_ingredients.sql` as `03-seed-ingredients.sql` in
  `/docker-entrypoint-initdb.d` so fresh compose databases get the catalog.

## How to apply to an existing database

```bash
psql -U postgres -d cooked -f db/seed.sql              # re-runs cleanly; updates nothing existing
psql -U postgres -d cooked -f db/seed_ingredients.sql  # inserts the catalog
-- existing rows keep old kcal values; to apply the two fixes:
UPDATE cooked.ingredient SET kcal_per_gram = 0.530 WHERE name = 'Soy sauce'  AND is_builtin;
UPDATE cooked.ingredient SET kcal_per_gram = 0.850 WHERE name = 'Red wine'   AND is_builtin;
```

## Why

The demo seed's calorie values were hand-typed; two were off by ~100× / ~12×
(soy sauce, red wine). Beyond fixing those, the app needs a real ingredient
database for users to pick from — USDA FoodData Central is the official
federal source (the basis for FDA nutrition labeling) and is licensed for
unrestricted public use.