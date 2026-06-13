-- Cooked — UI built-in catalog seed: moods, cuisines (incl. Filipino), the
-- full ingredient catalog, the built-in + community recipes (incl. the 16
-- Filipino classics) and the demo pantry. Matches the cooked-ui frontend
-- vocabulary in src/data/catalog.ts (capitalized moods; CUISINES list).
-- Relocated here from cooked-ui/db/seed.sql so all DB scripts live in the backend.
-- Run after db/setup.sql, against the `cooked` database:
--
--   psql -U postgres -d cooked -f db/seed_catalog.sql
--
-- Idempotent: safe to re-run (ON CONFLICT DO NOTHING / WHERE NOT EXISTS guards).

-- ──────────────────────────────────────────────
-- Vocabularies
-- ──────────────────────────────────────────────

INSERT INTO cooked.mood (name) VALUES
  ('Comfort'), ('Light'), ('Quick'), ('Hearty'), ('Healthy'), ('Sweet')
ON CONFLICT (name) DO NOTHING;

INSERT INTO cooked.cuisine (name) VALUES
  ('Italian'), ('Asian'), ('Mexican'), ('Mediterranean'), ('American'), ('Filipino')
ON CONFLICT (name) DO NOTHING;

-- ──────────────────────────────────────────────
-- Built-in ingredients (kcal per gram; grams_per_piece for countable items)
-- ──────────────────────────────────────────────

INSERT INTO cooked.ingredient (name, category, kcal_per_gram, grams_per_piece, is_builtin) VALUES
  -- Protein
  ('Chicken Breast',      'Protein',      1.65, NULL, TRUE),
  ('Beef (Ground)',       'Protein',      2.50, NULL, TRUE),
  ('Salmon',              'Protein',      2.08, NULL, TRUE),
  ('Eggs',                'Protein',      1.55,   50, TRUE),
  ('Tofu',                'Protein',      0.76, NULL, TRUE),
  ('Shrimp',              'Protein',      0.99, NULL, TRUE),
  ('Pork Chop',           'Protein',      2.42, NULL, TRUE),
  ('Turkey Breast',       'Protein',      1.35, NULL, TRUE),
  -- Carbs
  ('White Rice (cooked)', 'Carbs',        1.30, NULL, TRUE),
  ('Brown Rice (cooked)', 'Carbs',        1.12, NULL, TRUE),
  ('Pasta (cooked)',      'Carbs',        1.31, NULL, TRUE),
  ('Bread (white)',       'Carbs',        2.65,   25, TRUE),
  ('Potato',              'Carbs',        0.77,  170, TRUE),
  ('Sweet Potato',        'Carbs',        0.86,  130, TRUE),
  ('Quinoa (cooked)',     'Carbs',        1.20, NULL, TRUE),
  ('Oats',                'Carbs',        3.89, NULL, TRUE),
  -- Vegetables
  ('Broccoli',            'Vegetables',   0.34, NULL, TRUE),
  ('Spinach',             'Vegetables',   0.23, NULL, TRUE),
  ('Carrots',             'Vegetables',   0.41,   61, TRUE),
  ('Tomatoes',            'Vegetables',   0.18,  123, TRUE),
  ('Bell Pepper',         'Vegetables',   0.26,  120, TRUE),
  ('Onion',               'Vegetables',   0.40,  110, TRUE),
  ('Lettuce',             'Vegetables',   0.15, NULL, TRUE),
  ('Cucumber',            'Vegetables',   0.15,  200, TRUE),
  -- Dairy
  ('Milk (whole)',        'Dairy',        0.61, NULL, TRUE),
  ('Cheese (cheddar)',    'Dairy',        4.03, NULL, TRUE),
  ('Greek Yogurt',        'Dairy',        0.59, NULL, TRUE),
  ('Butter',              'Dairy',        7.17, NULL, TRUE),
  ('Cream',               'Dairy',        3.45, NULL, TRUE),
  -- Fats & Oils
  ('Olive Oil',           'Fats & Oils',  8.84, NULL, TRUE),
  ('Coconut Oil',         'Fats & Oils',  8.62, NULL, TRUE),
  ('Avocado',             'Fats & Oils',  1.60,  150, TRUE),
  -- Fruits
  ('Banana',              'Fruits',       0.89,  118, TRUE),
  ('Apple',               'Fruits',       0.52,  182, TRUE),
  ('Orange',               'Fruits',      0.47,  131, TRUE),
  ('Strawberries',        'Fruits',       0.32, NULL, TRUE),
  ('Blueberries',         'Fruits',       0.57, NULL, TRUE),
  -- Nuts & Seeds
  ('Almonds',             'Nuts & Seeds', 5.79, NULL, TRUE),
  ('Peanut Butter',       'Nuts & Seeds', 5.88, NULL, TRUE),
  ('Walnuts',             'Nuts & Seeds', 6.54, NULL, TRUE),
  ('Chia Seeds',          'Nuts & Seeds', 4.86, NULL, TRUE),
  -- Condiments
  ('Soy Sauce',           'Condiments',   0.53, NULL, TRUE),
  ('Honey',               'Condiments',   3.04, NULL, TRUE),
  ('Ketchup',             'Condiments',   1.12, NULL, TRUE),
  ('Mayonnaise',          'Condiments',   6.80, NULL, TRUE)
ON CONFLICT (name) DO NOTHING;

-- ──────────────────────────────────────────────
-- Filipino pantry staples (used by the Filipino recipe set below)
-- kcal/g from USDA / standard references; grams_per_piece for countable items
-- ──────────────────────────────────────────────

INSERT INTO cooked.ingredient (name, category, kcal_per_gram, grams_per_piece, is_builtin) VALUES
  -- Protein
  ('Chicken Thigh',             'Protein',      2.09, NULL, TRUE),
  ('Pork Belly',                'Protein',      5.18, NULL, TRUE),
  ('Ground Pork',               'Protein',      2.63, NULL, TRUE),
  ('Beef Chuck',                'Protein',      2.15, NULL, TRUE),
  ('Hotdog',                    'Protein',      3.00,   45, TRUE),
  -- Carbs
  ('Rice Noodles (bihon)',      'Carbs',        3.64, NULL, TRUE),
  ('Spring Roll Wrapper',       'Carbs',        2.40,   12, TRUE),
  -- Vegetables
  ('Garlic',                    'Vegetables',   1.49,    3, TRUE),
  ('Ginger',                    'Vegetables',   0.80, NULL, TRUE),
  ('Eggplant',                  'Vegetables',   0.25,   80, TRUE),
  ('String Beans',              'Vegetables',   0.31, NULL, TRUE),
  ('Radish (white)',            'Vegetables',   0.18, NULL, TRUE),
  ('Squash (kalabasa)',         'Vegetables',   0.34, NULL, TRUE),
  ('Water Spinach (kangkong)',  'Vegetables',   0.19, NULL, TRUE),
  ('Bok Choy (pechay)',         'Vegetables',   0.13, NULL, TRUE),
  ('Cabbage',                   'Vegetables',   0.25, NULL, TRUE),
  ('Green Papaya',              'Vegetables',   0.39, NULL, TRUE),
  ('Green Peas',                'Vegetables',   0.81, NULL, TRUE),
  ('Chili Pepper',              'Vegetables',   0.40,    5, TRUE),
  ('Lemongrass',                'Vegetables',   0.99,   20, TRUE),
  -- Dairy
  ('Condensed Milk',            'Dairy',        3.21, NULL, TRUE),
  ('Evaporated Milk',           'Dairy',        1.34, NULL, TRUE),
  -- Other
  ('Coconut Milk',              'Other',        2.30, NULL, TRUE),
  -- Condiments
  ('Vinegar (cane)',            'Condiments',   0.18, NULL, TRUE),
  ('Fish Sauce',                'Condiments',   0.35, NULL, TRUE),
  ('Bay Leaf',                  'Condiments',   3.13,  0.2, TRUE),
  ('Black Pepper',              'Condiments',   2.51, NULL, TRUE),
  ('Tamarind Paste',            'Condiments',   1.80, NULL, TRUE),
  ('Tomato Sauce',              'Condiments',   0.32, NULL, TRUE),
  ('Banana Ketchup',            'Condiments',   1.00, NULL, TRUE),
  ('Shrimp Paste (bagoong)',    'Condiments',   1.20, NULL, TRUE),
  ('Brown Sugar',               'Condiments',   3.80, NULL, TRUE),
  ('White Sugar',               'Condiments',   3.87, NULL, TRUE),
  -- Fruits
  ('Calamansi',                 'Fruits',       0.30,   10, TRUE),
  ('Saba Banana (plantain)',    'Fruits',       1.22,   60, TRUE)
ON CONFLICT (name) DO NOTHING;

-- ──────────────────────────────────────────────
-- Built-in recipes (owner NULL) + community seeds (owner NULL, is_community)
-- ──────────────────────────────────────────────

INSERT INTO cooked.recipe (name, cuisine, prep_time_min, servings, author_label, is_community, is_shared)
SELECT v.name, v.cuisine, v.prep, v.servings, v.author, v.community, v.community
FROM (VALUES
  ('Grilled Chicken Salad',          'Mediterranean', 20, 2, 'Chef Marco',     FALSE),
  ('Chicken Fried Rice',             'Asian',         25, 3, 'Chef Li Wei',    FALSE),
  ('Salmon with Sweet Potato',       'Mediterranean', 30, 2, 'Chef Sofia',     FALSE),
  ('Greek Yogurt Parfait',           'Mediterranean',  5, 1, 'Chef Elena',     FALSE),
  ('Beef Taco Bowl',                 'Mexican',       25, 2, 'Chef Carlos',    FALSE),
  ('Vegetable Stir Fry',             'Asian',         20, 2, 'Chef Priya',     FALSE),
  ('Pasta Carbonara',                'Italian',       20, 2, 'Chef Marco',     FALSE),
  ('Turkey Sandwich',                'American',       5, 1, 'Chef James',     FALSE),
  ('Oatmeal with Fruits',            'American',      10, 1, 'Chef Elena',     FALSE),
  ('Shrimp Pasta',                   'Italian',       25, 2, 'Chef Sofia',     FALSE),
  ('Chicken Quinoa Bowl',            'American',      25, 2, 'Chef Priya',     FALSE),
  ('Pork Chop with Potato',          'American',      35, 2, 'Chef James',     FALSE),
  ('Avocado Toast',                  'American',      10, 1, 'Chef Carlos',    FALSE),
  ('Chicken Caesar Salad',           'American',      15, 2, 'Chef Marco',     FALSE),
  ('Banana Peanut Butter Smoothie',  'American',       5, 1, 'Chef Li Wei',    FALSE),
  -- community seeds
  ('Spicy Tofu Rice Bowl',           'Asian',         18, 2, '@priya.kitchen', TRUE),
  ('Berry Almond Overnight Oats',    'American',       5, 1, '@morning.maja',  TRUE),
  ('Creamy Chicken Spinach Pasta',   'Italian',       25, 2, '@marco.cooks',   TRUE),
  ('Loaded Baked Sweet Potato',      'Mexican',       40, 1, '@veg.victor',    TRUE)
) AS v(name, cuisine, prep, servings, author, community)
WHERE NOT EXISTS (
  SELECT 1 FROM cooked.recipe r
  WHERE lower(r.name) = lower(v.name) AND r.owner_user_id IS NULL
);

INSERT INTO cooked.recipe_mood (recipe_id, mood)
SELECT r.id, v.mood
FROM (VALUES
  ('Grilled Chicken Salad', 'Light'),   ('Grilled Chicken Salad', 'Healthy'),
  ('Chicken Fried Rice', 'Comfort'),    ('Chicken Fried Rice', 'Quick'),
  ('Salmon with Sweet Potato', 'Healthy'), ('Salmon with Sweet Potato', 'Hearty'),
  ('Greek Yogurt Parfait', 'Sweet'),    ('Greek Yogurt Parfait', 'Light'), ('Greek Yogurt Parfait', 'Quick'),
  ('Beef Taco Bowl', 'Hearty'),         ('Beef Taco Bowl', 'Comfort'),
  ('Vegetable Stir Fry', 'Healthy'),    ('Vegetable Stir Fry', 'Light'),
  ('Pasta Carbonara', 'Comfort'),       ('Pasta Carbonara', 'Hearty'),
  ('Turkey Sandwich', 'Quick'),         ('Turkey Sandwich', 'Light'),
  ('Oatmeal with Fruits', 'Comfort'),   ('Oatmeal with Fruits', 'Sweet'),
  ('Shrimp Pasta', 'Light'),            ('Shrimp Pasta', 'Hearty'),
  ('Chicken Quinoa Bowl', 'Healthy'),   ('Chicken Quinoa Bowl', 'Light'),
  ('Pork Chop with Potato', 'Hearty'),  ('Pork Chop with Potato', 'Comfort'),
  ('Avocado Toast', 'Quick'),           ('Avocado Toast', 'Healthy'),
  ('Chicken Caesar Salad', 'Light'),    ('Chicken Caesar Salad', 'Quick'),
  ('Banana Peanut Butter Smoothie', 'Sweet'), ('Banana Peanut Butter Smoothie', 'Quick'),
  ('Spicy Tofu Rice Bowl', 'Healthy'),  ('Spicy Tofu Rice Bowl', 'Quick'),
  ('Berry Almond Overnight Oats', 'Sweet'), ('Berry Almond Overnight Oats', 'Light'),
  ('Creamy Chicken Spinach Pasta', 'Comfort'), ('Creamy Chicken Spinach Pasta', 'Hearty'),
  ('Loaded Baked Sweet Potato', 'Comfort'), ('Loaded Baked Sweet Potato', 'Healthy')
) AS v(recipe, mood)
JOIN cooked.recipe r ON lower(r.name) = lower(v.recipe) AND r.owner_user_id IS NULL
ON CONFLICT (recipe_id, mood) DO NOTHING;

INSERT INTO cooked.recipe_ingredient (recipe_id, ingredient_id, grams)
SELECT r.id, i.id, v.grams
FROM (VALUES
  ('Grilled Chicken Salad', 'Chicken Breast', 150), ('Grilled Chicken Salad', 'Lettuce', 100),
  ('Grilled Chicken Salad', 'Tomatoes', 80),        ('Grilled Chicken Salad', 'Cucumber', 50),
  ('Grilled Chicken Salad', 'Olive Oil', 10),
  ('Chicken Fried Rice', 'Chicken Breast', 200),    ('Chicken Fried Rice', 'White Rice (cooked)', 300),
  ('Chicken Fried Rice', 'Eggs', 100),              ('Chicken Fried Rice', 'Carrots', 50),
  ('Chicken Fried Rice', 'Soy Sauce', 15),
  ('Salmon with Sweet Potato', 'Salmon', 200),      ('Salmon with Sweet Potato', 'Sweet Potato', 250),
  ('Salmon with Sweet Potato', 'Broccoli', 150),    ('Salmon with Sweet Potato', 'Olive Oil', 10),
  ('Greek Yogurt Parfait', 'Greek Yogurt', 200),    ('Greek Yogurt Parfait', 'Blueberries', 80),
  ('Greek Yogurt Parfait', 'Strawberries', 80),     ('Greek Yogurt Parfait', 'Honey', 20),
  ('Greek Yogurt Parfait', 'Almonds', 30),
  ('Beef Taco Bowl', 'Beef (Ground)', 200),         ('Beef Taco Bowl', 'Brown Rice (cooked)', 200),
  ('Beef Taco Bowl', 'Bell Pepper', 100),           ('Beef Taco Bowl', 'Tomatoes', 100),
  ('Beef Taco Bowl', 'Cheese (cheddar)', 50),       ('Beef Taco Bowl', 'Avocado', 80),
  ('Vegetable Stir Fry', 'Tofu', 200),              ('Vegetable Stir Fry', 'Broccoli', 150),
  ('Vegetable Stir Fry', 'Bell Pepper', 100),       ('Vegetable Stir Fry', 'Carrots', 80),
  ('Vegetable Stir Fry', 'Soy Sauce', 20),          ('Vegetable Stir Fry', 'White Rice (cooked)', 250),
  ('Pasta Carbonara', 'Pasta (cooked)', 300),       ('Pasta Carbonara', 'Eggs', 100),
  ('Pasta Carbonara', 'Cheese (cheddar)', 50),      ('Pasta Carbonara', 'Cream', 100),
  ('Turkey Sandwich', 'Turkey Breast', 100),        ('Turkey Sandwich', 'Bread (white)', 80),
  ('Turkey Sandwich', 'Lettuce', 30),               ('Turkey Sandwich', 'Tomatoes', 50),
  ('Turkey Sandwich', 'Mayonnaise', 15),
  ('Oatmeal with Fruits', 'Oats', 60),              ('Oatmeal with Fruits', 'Milk (whole)', 200),
  ('Oatmeal with Fruits', 'Banana', 100),           ('Oatmeal with Fruits', 'Blueberries', 50),
  ('Oatmeal with Fruits', 'Honey', 15),             ('Oatmeal with Fruits', 'Almonds', 20),
  ('Shrimp Pasta', 'Shrimp', 200),                  ('Shrimp Pasta', 'Pasta (cooked)', 250),
  ('Shrimp Pasta', 'Tomatoes', 150),                ('Shrimp Pasta', 'Olive Oil', 15),
  ('Shrimp Pasta', 'Spinach', 100),
  ('Chicken Quinoa Bowl', 'Chicken Breast', 150),   ('Chicken Quinoa Bowl', 'Quinoa (cooked)', 200),
  ('Chicken Quinoa Bowl', 'Broccoli', 100),         ('Chicken Quinoa Bowl', 'Carrots', 80),
  ('Chicken Quinoa Bowl', 'Avocado', 60),
  ('Pork Chop with Potato', 'Pork Chop', 200),      ('Pork Chop with Potato', 'Potato', 250),
  ('Pork Chop with Potato', 'Broccoli', 150),       ('Pork Chop with Potato', 'Butter', 10),
  ('Avocado Toast', 'Bread (white)', 60),           ('Avocado Toast', 'Avocado', 100),
  ('Avocado Toast', 'Eggs', 100),                   ('Avocado Toast', 'Tomatoes', 50),
  ('Chicken Caesar Salad', 'Chicken Breast', 150),  ('Chicken Caesar Salad', 'Lettuce', 150),
  ('Chicken Caesar Salad', 'Cheese (cheddar)', 30), ('Chicken Caesar Salad', 'Bread (white)', 40),
  ('Chicken Caesar Salad', 'Mayonnaise', 20),
  ('Banana Peanut Butter Smoothie', 'Banana', 120), ('Banana Peanut Butter Smoothie', 'Peanut Butter', 30),
  ('Banana Peanut Butter Smoothie', 'Milk (whole)', 250), ('Banana Peanut Butter Smoothie', 'Honey', 10),
  -- community seeds
  ('Spicy Tofu Rice Bowl', 'Tofu', 200),            ('Spicy Tofu Rice Bowl', 'White Rice (cooked)', 250),
  ('Spicy Tofu Rice Bowl', 'Broccoli', 100),        ('Spicy Tofu Rice Bowl', 'Soy Sauce', 20),
  ('Spicy Tofu Rice Bowl', 'Honey', 10),
  ('Berry Almond Overnight Oats', 'Oats', 50),      ('Berry Almond Overnight Oats', 'Milk (whole)', 150),
  ('Berry Almond Overnight Oats', 'Greek Yogurt', 100), ('Berry Almond Overnight Oats', 'Blueberries', 60),
  ('Berry Almond Overnight Oats', 'Almonds', 15),   ('Berry Almond Overnight Oats', 'Honey', 10),
  ('Creamy Chicken Spinach Pasta', 'Pasta (cooked)', 250), ('Creamy Chicken Spinach Pasta', 'Chicken Breast', 150),
  ('Creamy Chicken Spinach Pasta', 'Cream', 80),    ('Creamy Chicken Spinach Pasta', 'Spinach', 80),
  ('Loaded Baked Sweet Potato', 'Sweet Potato', 300), ('Loaded Baked Sweet Potato', 'Cheese (cheddar)', 40),
  ('Loaded Baked Sweet Potato', 'Greek Yogurt', 50), ('Loaded Baked Sweet Potato', 'Tomatoes', 60)
) AS v(recipe, ingredient, grams)
JOIN cooked.recipe r     ON lower(r.name) = lower(v.recipe) AND r.owner_user_id IS NULL
JOIN cooked.ingredient i ON i.name = v.ingredient
ON CONFLICT (recipe_id, ingredient_id) DO NOTHING;

-- ──────────────────────────────────────────────
-- Cooking instructions for built-in and community recipes
-- ──────────────────────────────────────────────

INSERT INTO cooked.recipe_instruction (recipe_id, step_no, instruction)
SELECT r.id, v.step_no, v.instruction
FROM (VALUES
  ('Grilled Chicken Salad', 1, 'Season the chicken breast and grill 6-7 min per side until cooked through; rest 5 min, then slice.'),
  ('Grilled Chicken Salad', 2, 'Chop the lettuce, tomatoes and cucumber into a large bowl.'),
  ('Grilled Chicken Salad', 3, 'Top with the sliced chicken, drizzle with olive oil, season and toss.'),
  ('Chicken Fried Rice', 1, 'Dice the chicken and stir-fry in a hot pan until golden; set aside.'),
  ('Chicken Fried Rice', 2, 'Scramble the eggs in the same pan, then add the diced carrots and fry 2 min.'),
  ('Chicken Fried Rice', 3, 'Add the rice and chicken, splash in the soy sauce and stir-fry on high heat 3-4 min.'),
  ('Salmon with Sweet Potato', 1, 'Roast sweet potato wedges at 200 C for about 25 min.'),
  ('Salmon with Sweet Potato', 2, 'Pan-sear the salmon in olive oil 3-4 min per side.'),
  ('Salmon with Sweet Potato', 3, 'Steam the broccoli 4 min, plate everything and season.'),
  ('Greek Yogurt Parfait', 1, 'Spoon half the yogurt into a glass.'),
  ('Greek Yogurt Parfait', 2, 'Layer in the berries, drizzle with honey, then repeat the layers.'),
  ('Greek Yogurt Parfait', 3, 'Top with the almonds.'),
  ('Beef Taco Bowl', 1, 'Brown the ground beef and season taco-style.'),
  ('Beef Taco Bowl', 2, 'Warm the rice in bowls and top with beef, diced bell pepper and tomatoes.'),
  ('Beef Taco Bowl', 3, 'Finish with the cheese and avocado.'),
  ('Vegetable Stir Fry', 1, 'Press the tofu, cube it and pan-fry until golden; set aside.'),
  ('Vegetable Stir Fry', 2, 'Stir-fry the broccoli, bell pepper and carrots 4 min on high heat.'),
  ('Vegetable Stir Fry', 3, 'Return the tofu, add the soy sauce and toss; serve over the rice.'),
  ('Pasta Carbonara', 1, 'Whisk the eggs, cream and cheese together.'),
  ('Pasta Carbonara', 2, 'Toss the hot pasta with the egg mixture off the heat until creamy.'),
  ('Pasta Carbonara', 3, 'Season generously with black pepper and serve immediately.'),
  ('Turkey Sandwich', 1, 'Spread the mayonnaise on the bread.'),
  ('Turkey Sandwich', 2, 'Layer on the turkey, lettuce and tomato; close and halve.'),
  ('Oatmeal with Fruits', 1, 'Simmer the oats in the milk about 5 min, stirring.'),
  ('Oatmeal with Fruits', 2, 'Top with banana, blueberries, honey and almonds.'),
  ('Shrimp Pasta', 1, 'Saute the shrimp in olive oil 2 min per side; set aside.'),
  ('Shrimp Pasta', 2, 'Soften the tomatoes in the same pan and wilt in the spinach.'),
  ('Shrimp Pasta', 3, 'Toss the pasta and shrimp through and season.'),
  ('Chicken Quinoa Bowl', 1, 'Grill the seasoned chicken and slice it.'),
  ('Chicken Quinoa Bowl', 2, 'Steam the broccoli and carrots until just tender.'),
  ('Chicken Quinoa Bowl', 3, 'Bowl the quinoa, vegetables and chicken; top with avocado.'),
  ('Pork Chop with Potato', 1, 'Roast potato chunks at 200 C for about 30 min.'),
  ('Pork Chop with Potato', 2, 'Pan-fry the pork chops in butter 4-5 min per side; rest 5 min.'),
  ('Pork Chop with Potato', 3, 'Steam the broccoli and plate everything.'),
  ('Avocado Toast', 1, 'Toast the bread and fry the eggs.'),
  ('Avocado Toast', 2, 'Mash the avocado onto the toast and season.'),
  ('Avocado Toast', 3, 'Top with the eggs and tomato slices.'),
  ('Chicken Caesar Salad', 1, 'Grill the chicken and slice it.'),
  ('Chicken Caesar Salad', 2, 'Toast the bread cubes for croutons.'),
  ('Chicken Caesar Salad', 3, 'Toss the lettuce with the mayonnaise dressing, chicken, croutons and cheese.'),
  ('Banana Peanut Butter Smoothie', 1, 'Add the banana, peanut butter, milk and honey to a blender.'),
  ('Banana Peanut Butter Smoothie', 2, 'Blend until completely smooth and pour into a glass.'),
  ('Spicy Tofu Rice Bowl', 1, 'Pan-fry the tofu cubes until crisp.'),
  ('Spicy Tofu Rice Bowl', 2, 'Steam the broccoli until just tender.'),
  ('Spicy Tofu Rice Bowl', 3, 'Glaze the tofu with soy sauce, honey and chili; serve over the rice.'),
  ('Berry Almond Overnight Oats', 1, 'Stir the oats, milk, yogurt and honey together in a jar.'),
  ('Berry Almond Overnight Oats', 2, 'Refrigerate overnight.'),
  ('Berry Almond Overnight Oats', 3, 'Top with the blueberries and almonds before eating.'),
  ('Creamy Chicken Spinach Pasta', 1, 'Saute the chicken strips until golden.'),
  ('Creamy Chicken Spinach Pasta', 2, 'Add the cream, simmer gently and wilt in the spinach.'),
  ('Creamy Chicken Spinach Pasta', 3, 'Toss the pasta through and season.'),
  ('Loaded Baked Sweet Potato', 1, 'Bake the sweet potato at 200 C for about 40 min until tender.'),
  ('Loaded Baked Sweet Potato', 2, 'Split it open and fluff the inside.'),
  ('Loaded Baked Sweet Potato', 3, 'Top with the cheese, Greek yogurt and tomatoes.')
) AS v(recipe, step_no, instruction)
JOIN cooked.recipe r ON lower(r.name) = lower(v.recipe) AND r.owner_user_id IS NULL
ON CONFLICT (recipe_id, step_no) DO NOTHING;

-- ══════════════════════════════════════════════
-- Filipino recipes — researched classics, seeded as community recipes.
-- NOTE: these are community seeds (owner NULL, is_community TRUE) — NOT plain
-- built-ins — because the backend only serves recipes matching
-- "owner = me OR is_community = true" (RecipeRepository.findVisibleToUser).
-- Plain built-ins (owner NULL, is_community FALSE) are never returned by the
-- API, so community is the only seed form that surfaces in the app.
-- ══════════════════════════════════════════════

INSERT INTO cooked.recipe (name, cuisine, prep_time_min, servings, author_label, is_community, is_shared)
SELECT v.name, v.cuisine, v.prep, v.servings, v.author, TRUE, TRUE
FROM (VALUES
  ('Chicken Adobo',            'Filipino', 40, 4, '@lola.remedios'),
  ('Pork Sinigang',            'Filipino', 60, 5, '@tita.glenda'),
  ('Chicken Tinola',           'Filipino', 45, 4, '@chef.maria'),
  ('Pork Menudo',              'Filipino', 50, 5, '@kusina.tonio'),
  ('Beef Caldereta',           'Filipino', 70, 5, '@chef.andoy'),
  ('Kare-Kare',                'Filipino', 75, 5, '@bella.cooks'),
  ('Pancit Bihon',             'Filipino', 30, 5, '@iska.eats'),
  ('Lumpiang Shanghai',        'Filipino', 40, 6, '@carlo.kitchen'),
  ('Filipino-Style Spaghetti', 'Filipino', 35, 5, '@jun.cooks'),
  ('Ginataang Gulay',          'Filipino', 35, 4, '@diwa.veg'),
  ('Bicol Express',            'Filipino', 40, 4, '@fidel.bicol'),
  ('Chicken Inasal',           'Filipino', 45, 4, '@hiraya.grill'),
  ('Arroz Caldo',              'Filipino', 45, 4, '@lola.pinang'),
  ('Bistek Tagalog',           'Filipino', 40, 4, '@rommel.cooks'),
  ('Turon',                    'Filipino', 20, 4, '@tita.cory'),
  ('Leche Flan',               'Filipino', 60, 6, '@tita.baby')
) AS v(name, cuisine, prep, servings, author)
WHERE NOT EXISTS (
  SELECT 1 FROM cooked.recipe r
  WHERE lower(r.name) = lower(v.name) AND r.owner_user_id IS NULL
);

INSERT INTO cooked.recipe_mood (recipe_id, mood)
SELECT r.id, v.mood
FROM (VALUES
  ('Chicken Adobo', 'Comfort'),            ('Chicken Adobo', 'Hearty'),
  ('Pork Sinigang', 'Comfort'),            ('Pork Sinigang', 'Healthy'),
  ('Chicken Tinola', 'Light'),             ('Chicken Tinola', 'Healthy'),
  ('Pork Menudo', 'Hearty'),               ('Pork Menudo', 'Comfort'),
  ('Beef Caldereta', 'Hearty'),            ('Beef Caldereta', 'Comfort'),
  ('Kare-Kare', 'Hearty'),                 ('Kare-Kare', 'Comfort'),
  ('Pancit Bihon', 'Quick'),               ('Pancit Bihon', 'Comfort'),
  ('Lumpiang Shanghai', 'Quick'),          ('Lumpiang Shanghai', 'Comfort'),
  ('Filipino-Style Spaghetti', 'Sweet'),   ('Filipino-Style Spaghetti', 'Comfort'),
  ('Ginataang Gulay', 'Healthy'),          ('Ginataang Gulay', 'Comfort'),
  ('Bicol Express', 'Hearty'),             ('Bicol Express', 'Comfort'),
  ('Chicken Inasal', 'Healthy'),           ('Chicken Inasal', 'Hearty'),
  ('Arroz Caldo', 'Comfort'),              ('Arroz Caldo', 'Healthy'),
  ('Bistek Tagalog', 'Hearty'),            ('Bistek Tagalog', 'Comfort'),
  ('Turon', 'Sweet'),                      ('Turon', 'Quick'),
  ('Leche Flan', 'Sweet')
) AS v(recipe, mood)
JOIN cooked.recipe r ON lower(r.name) = lower(v.recipe) AND r.owner_user_id IS NULL
ON CONFLICT (recipe_id, mood) DO NOTHING;

INSERT INTO cooked.recipe_ingredient (recipe_id, ingredient_id, grams)
SELECT r.id, i.id, v.grams
FROM (VALUES
  ('Chicken Adobo', 'Chicken Thigh', 700),      ('Chicken Adobo', 'Soy Sauce', 60),
  ('Chicken Adobo', 'Vinegar (cane)', 60),      ('Chicken Adobo', 'Garlic', 30),
  ('Chicken Adobo', 'Onion', 80),               ('Chicken Adobo', 'Bay Leaf', 1),
  ('Chicken Adobo', 'Black Pepper', 3),         ('Chicken Adobo', 'White Rice (cooked)', 400),
  ('Pork Sinigang', 'Pork Belly', 700),         ('Pork Sinigang', 'Tamarind Paste', 40),
  ('Pork Sinigang', 'Tomatoes', 150),           ('Pork Sinigang', 'Onion', 100),
  ('Pork Sinigang', 'Radish (white)', 150),     ('Pork Sinigang', 'String Beans', 120),
  ('Pork Sinigang', 'Eggplant', 120),           ('Pork Sinigang', 'Water Spinach (kangkong)', 100),
  ('Pork Sinigang', 'Fish Sauce', 30),
  ('Chicken Tinola', 'Chicken Thigh', 600),     ('Chicken Tinola', 'Ginger', 40),
  ('Chicken Tinola', 'Garlic', 20),             ('Chicken Tinola', 'Onion', 80),
  ('Chicken Tinola', 'Green Papaya', 250),      ('Chicken Tinola', 'Water Spinach (kangkong)', 100),
  ('Chicken Tinola', 'Fish Sauce', 25),
  ('Pork Menudo', 'Pork Chop', 500),            ('Pork Menudo', 'Tomato Sauce', 200),
  ('Pork Menudo', 'Potato', 250),               ('Pork Menudo', 'Carrots', 150),
  ('Pork Menudo', 'Bell Pepper', 120),          ('Pork Menudo', 'Green Peas', 100),
  ('Pork Menudo', 'Soy Sauce', 30),             ('Pork Menudo', 'Garlic', 20),
  ('Pork Menudo', 'Onion', 100),                ('Pork Menudo', 'White Rice (cooked)', 400),
  ('Beef Caldereta', 'Beef Chuck', 700),        ('Beef Caldereta', 'Tomato Sauce', 250),
  ('Beef Caldereta', 'Potato', 250),            ('Beef Caldereta', 'Carrots', 150),
  ('Beef Caldereta', 'Bell Pepper', 150),       ('Beef Caldereta', 'Cheese (cheddar)', 50),
  ('Beef Caldereta', 'Garlic', 20),             ('Beef Caldereta', 'Onion', 100),
  ('Beef Caldereta', 'White Rice (cooked)', 400),
  ('Kare-Kare', 'Beef Chuck', 700),             ('Kare-Kare', 'Peanut Butter', 120),
  ('Kare-Kare', 'Eggplant', 150),               ('Kare-Kare', 'String Beans', 150),
  ('Kare-Kare', 'Bok Choy (pechay)', 150),      ('Kare-Kare', 'Garlic', 20),
  ('Kare-Kare', 'Onion', 100),                  ('Kare-Kare', 'Shrimp Paste (bagoong)', 40),
  ('Kare-Kare', 'White Rice (cooked)', 400),
  ('Pancit Bihon', 'Rice Noodles (bihon)', 250),('Pancit Bihon', 'Chicken Breast', 200),
  ('Pancit Bihon', 'Shrimp', 150),              ('Pancit Bihon', 'Carrots', 120),
  ('Pancit Bihon', 'Cabbage', 200),             ('Pancit Bihon', 'Soy Sauce', 40),
  ('Pancit Bihon', 'Garlic', 20),               ('Pancit Bihon', 'Onion', 100),
  ('Lumpiang Shanghai', 'Ground Pork', 500),    ('Lumpiang Shanghai', 'Carrots', 100),
  ('Lumpiang Shanghai', 'Onion', 80),           ('Lumpiang Shanghai', 'Garlic', 15),
  ('Lumpiang Shanghai', 'Eggs', 50),            ('Lumpiang Shanghai', 'Spring Roll Wrapper', 120),
  ('Filipino-Style Spaghetti', 'Pasta (cooked)', 500), ('Filipino-Style Spaghetti', 'Beef (Ground)', 300),
  ('Filipino-Style Spaghetti', 'Hotdog', 200),  ('Filipino-Style Spaghetti', 'Banana Ketchup', 200),
  ('Filipino-Style Spaghetti', 'Tomato Sauce', 200), ('Filipino-Style Spaghetti', 'Brown Sugar', 30),
  ('Filipino-Style Spaghetti', 'Cheese (cheddar)', 60), ('Filipino-Style Spaghetti', 'Garlic', 15),
  ('Filipino-Style Spaghetti', 'Onion', 100),
  ('Ginataang Gulay', 'Squash (kalabasa)', 400),('Ginataang Gulay', 'String Beans', 200),
  ('Ginataang Gulay', 'Coconut Milk', 400),     ('Ginataang Gulay', 'Shrimp', 150),
  ('Ginataang Gulay', 'Garlic', 15),            ('Ginataang Gulay', 'Onion', 80),
  ('Ginataang Gulay', 'Ginger', 20),            ('Ginataang Gulay', 'Shrimp Paste (bagoong)', 20),
  ('Bicol Express', 'Pork Belly', 600),         ('Bicol Express', 'Coconut Milk', 400),
  ('Bicol Express', 'Chili Pepper', 40),        ('Bicol Express', 'Shrimp Paste (bagoong)', 40),
  ('Bicol Express', 'Garlic', 20),              ('Bicol Express', 'Onion', 100),
  ('Bicol Express', 'Ginger', 20),              ('Bicol Express', 'White Rice (cooked)', 400),
  ('Chicken Inasal', 'Chicken Thigh', 700),     ('Chicken Inasal', 'Calamansi', 60),
  ('Chicken Inasal', 'Vinegar (cane)', 40),     ('Chicken Inasal', 'Lemongrass', 40),
  ('Chicken Inasal', 'Garlic', 25),             ('Chicken Inasal', 'Ginger', 20),
  ('Chicken Inasal', 'Black Pepper', 3),
  ('Arroz Caldo', 'White Rice (cooked)', 400),  ('Arroz Caldo', 'Chicken Thigh', 400),
  ('Arroz Caldo', 'Ginger', 40),                ('Arroz Caldo', 'Garlic', 25),
  ('Arroz Caldo', 'Onion', 80),                 ('Arroz Caldo', 'Fish Sauce', 25),
  ('Arroz Caldo', 'Eggs', 100),                 ('Arroz Caldo', 'Calamansi', 20),
  ('Bistek Tagalog', 'Beef Chuck', 600),        ('Bistek Tagalog', 'Soy Sauce', 60),
  ('Bistek Tagalog', 'Calamansi', 50),          ('Bistek Tagalog', 'Onion', 200),
  ('Bistek Tagalog', 'Garlic', 20),             ('Bistek Tagalog', 'Black Pepper', 3),
  ('Bistek Tagalog', 'White Rice (cooked)', 400),
  ('Turon', 'Saba Banana (plantain)', 360),     ('Turon', 'Brown Sugar', 80),
  ('Turon', 'Spring Roll Wrapper', 96),         ('Turon', 'Coconut Oil', 40),
  ('Leche Flan', 'Eggs', 300),                  ('Leche Flan', 'Condensed Milk', 300),
  ('Leche Flan', 'Evaporated Milk', 350),       ('Leche Flan', 'White Sugar', 100),
  ('Leche Flan', 'Calamansi', 10)
) AS v(recipe, ingredient, grams)
JOIN cooked.recipe r     ON lower(r.name) = lower(v.recipe) AND r.owner_user_id IS NULL
JOIN cooked.ingredient i ON i.name = v.ingredient
ON CONFLICT (recipe_id, ingredient_id) DO NOTHING;

INSERT INTO cooked.recipe_instruction (recipe_id, step_no, instruction)
SELECT r.id, v.step_no, v.instruction
FROM (VALUES
  ('Chicken Adobo', 1, 'Marinate the chicken thighs in the soy sauce, half the garlic and the black pepper for 20 minutes.'),
  ('Chicken Adobo', 2, 'Sear the chicken in a hot pot until browned, then add the onion and remaining garlic.'),
  ('Chicken Adobo', 3, 'Pour in the marinade, vinegar and bay leaf and let it boil 2 min without stirring.'),
  ('Chicken Adobo', 4, 'Cover and simmer 25-30 min until tender and the sauce reduces; serve over the rice.'),
  ('Pork Sinigang', 1, 'Boil the pork belly with the onion and tomatoes in plenty of water, skimming, until tender (about 40 min).'),
  ('Pork Sinigang', 2, 'Stir in the tamarind paste and fish sauce to build a sour broth.'),
  ('Pork Sinigang', 3, 'Add the radish and string beans, simmer 5 min, then add the eggplant.'),
  ('Pork Sinigang', 4, 'Drop in the water spinach for the last 2 min and serve hot.'),
  ('Chicken Tinola', 1, 'Saute the ginger, garlic and onion until fragrant.'),
  ('Chicken Tinola', 2, 'Add the chicken, sear, then season with the fish sauce.'),
  ('Chicken Tinola', 3, 'Pour in water and simmer 25 min until the chicken is tender.'),
  ('Chicken Tinola', 4, 'Add the green papaya, cook 8 min, then wilt in the water spinach before serving.'),
  ('Pork Menudo', 1, 'Saute the garlic and onion, then brown the diced pork.'),
  ('Pork Menudo', 2, 'Add the soy sauce and tomato sauce with a little water and simmer 20 min.'),
  ('Pork Menudo', 3, 'Add the potato and carrots and cook until tender, about 12 min.'),
  ('Pork Menudo', 4, 'Stir in the bell pepper and green peas, simmer 5 min, and serve over the rice.'),
  ('Beef Caldereta', 1, 'Brown the beef chunks and set aside.'),
  ('Beef Caldereta', 2, 'Saute the garlic and onion, return the beef and add the tomato sauce with water.'),
  ('Beef Caldereta', 3, 'Cover and simmer 50-60 min until the beef is fork-tender.'),
  ('Beef Caldereta', 4, 'Add the potato and carrots, then stir in the bell pepper and grated cheese; serve with the rice.'),
  ('Kare-Kare', 1, 'Simmer the beef in water until very tender (about 1 hour), reserving the broth.'),
  ('Kare-Kare', 2, 'Saute the garlic and onion, then whisk the peanut butter into the broth for a thick sauce.'),
  ('Kare-Kare', 3, 'Return the beef and simmer; blanch the eggplant, string beans and bok choy separately.'),
  ('Kare-Kare', 4, 'Arrange the vegetables over the beef and serve with the shrimp paste on the side and the rice.'),
  ('Pancit Bihon', 1, 'Soak the rice noodles in warm water until pliable, then drain.'),
  ('Pancit Bihon', 2, 'Saute the garlic and onion, cook the chicken and shrimp, and set the shrimp aside.'),
  ('Pancit Bihon', 3, 'Add the carrots and cabbage with a splash of water and the soy sauce.'),
  ('Pancit Bihon', 4, 'Toss in the noodles and stir-fry until they absorb the sauce, then top with the shrimp.'),
  ('Lumpiang Shanghai', 1, 'Mix the ground pork with finely chopped carrots, onion, garlic and the egg, and season well.'),
  ('Lumpiang Shanghai', 2, 'Spoon the filling onto each wrapper and roll tightly, sealing the edge.'),
  ('Lumpiang Shanghai', 3, 'Deep-fry in batches until golden and cooked through, about 4 min.'),
  ('Lumpiang Shanghai', 4, 'Drain and slice; serve with a sweet-and-sour or banana ketchup dip.'),
  ('Filipino-Style Spaghetti', 1, 'Saute the garlic and onion, then brown the ground beef.'),
  ('Filipino-Style Spaghetti', 2, 'Add the sliced hotdogs, banana ketchup, tomato sauce and brown sugar; simmer 15 min into a sweet, thick sauce.'),
  ('Filipino-Style Spaghetti', 3, 'Toss the cooked spaghetti through the sauce.'),
  ('Filipino-Style Spaghetti', 4, 'Top generously with the grated cheese.'),
  ('Ginataang Gulay', 1, 'Saute the garlic, onion and ginger, then stir in the shrimp paste.'),
  ('Ginataang Gulay', 2, 'Pour in the coconut milk and bring to a gentle simmer.'),
  ('Ginataang Gulay', 3, 'Add the squash and cook until almost tender, about 10 min.'),
  ('Ginataang Gulay', 4, 'Add the string beans and shrimp and simmer 5 min until cooked.'),
  ('Bicol Express', 1, 'Render the pork belly in a hot pan until lightly browned.'),
  ('Bicol Express', 2, 'Saute the garlic, onion and ginger, then stir in the shrimp paste.'),
  ('Bicol Express', 3, 'Pour in the coconut milk and simmer 20 min until the pork is tender.'),
  ('Bicol Express', 4, 'Add the sliced chilies, simmer 5 more min, and serve with the rice.'),
  ('Chicken Inasal', 1, 'Pound the lemongrass, garlic and ginger and mix with the calamansi juice, vinegar and pepper.'),
  ('Chicken Inasal', 2, 'Marinate the chicken in the mixture for at least 30 min.'),
  ('Chicken Inasal', 3, 'Grill over hot coals, basting with annatto oil, 6-7 min per side.'),
  ('Chicken Inasal', 4, 'Rest briefly and serve with rice and a soy-calamansi dip.'),
  ('Arroz Caldo', 1, 'Saute the ginger, garlic and onion until fragrant.'),
  ('Arroz Caldo', 2, 'Add the chicken, season with the fish sauce, and pour in plenty of water.'),
  ('Arroz Caldo', 3, 'Add the rice and simmer, stirring often, until thick and porridge-like (about 25 min).'),
  ('Arroz Caldo', 4, 'Serve topped with halved boiled eggs and a squeeze of calamansi.'),
  ('Bistek Tagalog', 1, 'Marinate the thinly sliced beef in the soy sauce, calamansi juice, garlic and pepper for 30 min.'),
  ('Bistek Tagalog', 2, 'Sear the beef quickly over high heat and set aside.'),
  ('Bistek Tagalog', 3, 'Saute the onion rings until just softened.'),
  ('Bistek Tagalog', 4, 'Return the beef with the marinade, simmer until tender, top with the onions, and serve with the rice.'),
  ('Turon', 1, 'Roll each banana piece in the brown sugar.'),
  ('Turon', 2, 'Wrap snugly in a spring roll wrapper, sealing the edge with water.'),
  ('Turon', 3, 'Fry in hot oil, adding a little more sugar to caramelize, until golden and crisp.'),
  ('Turon', 4, 'Drain and cool slightly before serving.'),
  ('Leche Flan', 1, 'Caramelize the white sugar in the mold until amber, then set aside to harden.'),
  ('Leche Flan', 2, 'Gently whisk the eggs with the condensed and evaporated milk, avoiding bubbles, and add a little calamansi zest.'),
  ('Leche Flan', 3, 'Pour over the caramel and cover with foil.'),
  ('Leche Flan', 4, 'Steam 30-40 min until just set, chill, then invert to serve.')
) AS v(recipe, step_no, instruction)
JOIN cooked.recipe r ON lower(r.name) = lower(v.recipe) AND r.owner_user_id IS NULL
ON CONFLICT (recipe_id, step_no) DO NOTHING;

-- ──────────────────────────────────────────────
-- Demo user with the demo pantry (matches seedInventory() in src/utils/store.ts)
-- ──────────────────────────────────────────────

INSERT INTO cooked.app_user (email, display_name, handle)
VALUES ('demo@cooked.local', 'Demo User', '@demo')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cooked.user_settings (user_id)
SELECT u.id FROM cooked.app_user u WHERE u.email = 'demo@cooked.local'
ON CONFLICT (user_id) DO NOTHING;

-- seeded only when the demo user's pantry is empty, so re-running seed.sql
-- never duplicates lots or resurrects consumed stock
INSERT INTO cooked.pantry_item (user_id, ingredient_id, qty, unit, expires_on)
SELECT u.id, i.id, v.qty, v.unit::cooked.unit_type, CURRENT_DATE + v.exp_days
FROM (VALUES
  ('Chicken Breast',      600, 'g',     5),
  ('Eggs',                  8, 'pcs',  12),
  ('White Rice (cooked)', 500, 'g',     3),
  ('Lettuce',             200, 'g',     4),
  ('Tomatoes',              4, 'pcs',   6),
  ('Cucumber',              1, 'pcs',   7),
  ('Carrots',             300, 'g',    10),
  ('Broccoli',            300, 'g',     5),
  ('Greek Yogurt',        400, 'g',     9),
  ('Blueberries',         150, 'g',     4),
  ('Banana',                3, 'pcs',   4),
  ('Oats',                500, 'g',   180),
  ('Milk (whole)',          1, 'L',     6),
  ('Almonds',             200, 'g',   120),
  ('Olive Oil',           400, 'ml',  300),
  ('Soy Sauce',           150, 'ml',  200),
  ('Honey',               250, 'g',   400)
) AS v(ingredient, qty, unit, exp_days)
JOIN cooked.app_user u   ON u.email = 'demo@cooked.local'
JOIN cooked.ingredient i ON i.name = v.ingredient
WHERE NOT EXISTS (
  SELECT 1 FROM cooked.pantry_item p WHERE p.user_id = u.id
);
