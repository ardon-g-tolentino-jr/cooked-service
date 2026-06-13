-- Cooked — Japanese recipe seed data (researched classics)
-- Run after db/seed_catalog.sql (needs the built-in catalog: rice, eggs,
-- salmon, shrimp, tofu, onion, carrots, potato, sweet potato, bell pepper,
-- soy sauce, honey, butter, pork chop). All other ingredients these recipes
-- need are (re)declared below, so this file depends only on that catalog.
--
--   psql -h <host> -p <port> -U cooked_user -d cooked -f db/japanese_recipes.sql
--
-- Recipes are seeded as COMMUNITY recipes (owner NULL, is_community TRUE),
-- NOT plain built-ins: the backend only serves "owner = me OR is_community =
-- true" (RecipeRepository.findVisibleToUser), so community is the only seed
-- form that surfaces in the app. Moods/cuisine use the frontend catalog.ts
-- vocabulary (capitalized moods; cuisine 'Japanese').
--
-- Idempotent: safe to re-run (ON CONFLICT DO NOTHING / WHERE NOT EXISTS).

-- ──────────────────────────────────────────────
-- Cuisine
-- ──────────────────────────────────────────────

INSERT INTO cooked.cuisine (name) VALUES ('Japanese')
ON CONFLICT (name) DO NOTHING;

-- ──────────────────────────────────────────────
-- Ingredients
-- kcal/g from USDA / standard references; grams_per_piece for countable items
-- ──────────────────────────────────────────────

-- Japanese pantry staples (new to the catalog)
INSERT INTO cooked.ingredient (name, category, kcal_per_gram, grams_per_piece, is_builtin) VALUES
  -- Condiments / seasonings
  ('Mirin',                   'Condiments',   2.40, NULL, TRUE),
  ('Sake (cooking)',          'Condiments',   1.34, NULL, TRUE),
  ('Miso Paste',              'Condiments',   2.00, NULL, TRUE),
  ('Tonkatsu Sauce',          'Condiments',   1.40, NULL, TRUE),
  ('Curry Roux',              'Condiments',   5.20, NULL, TRUE),
  ('Red Bean Paste (anko)',   'Condiments',   2.00, NULL, TRUE),
  -- Stocks / other
  ('Dashi Stock',             'Other',        0.05, NULL, TRUE),
  -- Carbs
  ('Panko Breadcrumbs',       'Carbs',        3.57, NULL, TRUE),
  ('Flour',                   'Carbs',        3.64, NULL, TRUE),
  ('Ramen Noodles (cooked)',  'Carbs',        1.40, NULL, TRUE),
  ('Gyoza Wrapper',           'Carbs',        3.10,    6, TRUE),
  -- Vegetables / seaweed
  ('Nori (seaweed)',          'Vegetables',   1.70,    3, TRUE),
  ('Wakame (dried)',          'Vegetables',   2.00, NULL, TRUE),
  ('Green Onion',             'Vegetables',   0.32,   15, TRUE),
  -- Fats & oils
  ('Vegetable Oil',           'Fats & Oils',  8.84, NULL, TRUE),
  ('Sesame Oil',              'Fats & Oils',  8.84, NULL, TRUE)
ON CONFLICT (name) DO NOTHING;

-- Shared staples that also live in seed.sql (the original catalog had no
-- garlic/ginger/etc.). Repeated here so this file depends only on the original
-- built-in catalog; ON CONFLICT keeps seed.sql's definitions when present.
INSERT INTO cooked.ingredient (name, category, kcal_per_gram, grams_per_piece, is_builtin) VALUES
  ('Chicken Thigh',           'Protein',      2.09, NULL, TRUE),
  ('Pork Belly',              'Protein',      5.18, NULL, TRUE),
  ('Beef Chuck',              'Protein',      2.15, NULL, TRUE),
  ('Ground Pork',             'Protein',      2.63, NULL, TRUE),
  ('Ginger',                  'Vegetables',   0.80, NULL, TRUE),
  ('Garlic',                  'Vegetables',   1.49,    3, TRUE),
  ('Cabbage',                 'Vegetables',   0.25, NULL, TRUE),
  ('White Sugar',             'Condiments',   3.87, NULL, TRUE)
ON CONFLICT (name) DO NOTHING;

-- ──────────────────────────────────────────────
-- Japanese recipes (community: owner NULL, is_community TRUE)
-- ──────────────────────────────────────────────

INSERT INTO cooked.recipe (name, cuisine, prep_time_min, servings, author_label, is_community, is_shared)
SELECT v.name, v.cuisine, v.prep, v.servings, v.author, TRUE, TRUE
FROM (VALUES
  ('Chicken Teriyaki',     'Japanese', 25, 4, '@sakura.kitchen'),
  ('Salmon Teriyaki',      'Japanese', 25, 4, '@umi.eats'),
  ('Beef Gyudon',          'Japanese', 30, 4, '@donburi.dan'),
  ('Oyakodon',             'Japanese', 30, 4, '@chef.haru'),
  ('Katsudon',             'Japanese', 40, 4, '@katsu.ken'),
  ('Chicken Katsu',        'Japanese', 35, 4, '@tokyo.table'),
  ('Miso Soup',            'Japanese', 15, 4, '@obaachan.aiko'),
  ('Japanese Curry Rice',  'Japanese', 50, 5, '@kare.kohei'),
  ('Yakisoba',             'Japanese', 25, 4, '@yatai.yuki'),
  ('Gyoza',                'Japanese', 40, 5, '@gyoza.gen'),
  ('Tamagoyaki',           'Japanese', 15, 2, '@bento.mio'),
  ('Onigiri',              'Japanese', 20, 3, '@onigiri.ami'),
  ('Shrimp Tempura',       'Japanese', 35, 4, '@tempura.taro'),
  ('Shoyu Ramen',          'Japanese', 45, 4, '@ramen.ryo'),
  ('Dorayaki',             'Japanese', 30, 4, '@wagashi.wako')
) AS v(name, cuisine, prep, servings, author)
WHERE NOT EXISTS (
  SELECT 1 FROM cooked.recipe r
  WHERE lower(r.name) = lower(v.name) AND r.owner_user_id IS NULL
);

INSERT INTO cooked.recipe_mood (recipe_id, mood)
SELECT r.id, v.mood
FROM (VALUES
  ('Chicken Teriyaki', 'Comfort'),    ('Chicken Teriyaki', 'Quick'),
  ('Salmon Teriyaki', 'Healthy'),     ('Salmon Teriyaki', 'Quick'),
  ('Beef Gyudon', 'Comfort'),         ('Beef Gyudon', 'Hearty'),
  ('Oyakodon', 'Comfort'),            ('Oyakodon', 'Healthy'),
  ('Katsudon', 'Hearty'),             ('Katsudon', 'Comfort'),
  ('Chicken Katsu', 'Comfort'),       ('Chicken Katsu', 'Hearty'),
  ('Miso Soup', 'Light'),             ('Miso Soup', 'Healthy'),
  ('Japanese Curry Rice', 'Comfort'), ('Japanese Curry Rice', 'Hearty'),
  ('Yakisoba', 'Quick'),              ('Yakisoba', 'Comfort'),
  ('Gyoza', 'Quick'),                 ('Gyoza', 'Comfort'),
  ('Tamagoyaki', 'Quick'),            ('Tamagoyaki', 'Light'),
  ('Onigiri', 'Quick'),               ('Onigiri', 'Light'),
  ('Shrimp Tempura', 'Hearty'),       ('Shrimp Tempura', 'Comfort'),
  ('Shoyu Ramen', 'Comfort'),         ('Shoyu Ramen', 'Hearty'),
  ('Dorayaki', 'Sweet')
) AS v(recipe, mood)
JOIN cooked.recipe r ON lower(r.name) = lower(v.recipe) AND r.owner_user_id IS NULL
ON CONFLICT (recipe_id, mood) DO NOTHING;

INSERT INTO cooked.recipe_ingredient (recipe_id, ingredient_id, grams)
SELECT r.id, i.id, v.grams
FROM (VALUES
  ('Chicken Teriyaki', 'Chicken Thigh', 600),  ('Chicken Teriyaki', 'Soy Sauce', 60),
  ('Chicken Teriyaki', 'Mirin', 50),           ('Chicken Teriyaki', 'Sake (cooking)', 30),
  ('Chicken Teriyaki', 'White Sugar', 20),      ('Chicken Teriyaki', 'Ginger', 15),
  ('Chicken Teriyaki', 'White Rice (cooked)', 400),
  ('Salmon Teriyaki', 'Salmon', 600),          ('Salmon Teriyaki', 'Soy Sauce', 50),
  ('Salmon Teriyaki', 'Mirin', 50),            ('Salmon Teriyaki', 'Sake (cooking)', 30),
  ('Salmon Teriyaki', 'White Sugar', 20),       ('Salmon Teriyaki', 'White Rice (cooked)', 400),
  ('Beef Gyudon', 'Beef Chuck', 500),          ('Beef Gyudon', 'Onion', 200),
  ('Beef Gyudon', 'Soy Sauce', 60),            ('Beef Gyudon', 'Mirin', 50),
  ('Beef Gyudon', 'Sake (cooking)', 40),       ('Beef Gyudon', 'White Sugar', 20),
  ('Beef Gyudon', 'Dashi Stock', 200),         ('Beef Gyudon', 'White Rice (cooked)', 500),
  ('Beef Gyudon', 'Green Onion', 30),          ('Beef Gyudon', 'Eggs', 100),
  ('Oyakodon', 'Chicken Thigh', 400),          ('Oyakodon', 'Eggs', 200),
  ('Oyakodon', 'Onion', 150),                  ('Oyakodon', 'Dashi Stock', 200),
  ('Oyakodon', 'Soy Sauce', 40),               ('Oyakodon', 'Mirin', 40),
  ('Oyakodon', 'White Rice (cooked)', 500),    ('Oyakodon', 'Green Onion', 20),
  ('Katsudon', 'Pork Chop', 500),              ('Katsudon', 'Panko Breadcrumbs', 100),
  ('Katsudon', 'Eggs', 250),                   ('Katsudon', 'Flour', 50),
  ('Katsudon', 'Onion', 150),                  ('Katsudon', 'Dashi Stock', 200),
  ('Katsudon', 'Soy Sauce', 40),               ('Katsudon', 'Mirin', 40),
  ('Katsudon', 'White Rice (cooked)', 500),    ('Katsudon', 'Vegetable Oil', 60),
  ('Chicken Katsu', 'Chicken Thigh', 600),     ('Chicken Katsu', 'Panko Breadcrumbs', 120),
  ('Chicken Katsu', 'Eggs', 100),              ('Chicken Katsu', 'Flour', 60),
  ('Chicken Katsu', 'Tonkatsu Sauce', 80),     ('Chicken Katsu', 'Cabbage', 200),
  ('Chicken Katsu', 'Vegetable Oil', 80),      ('Chicken Katsu', 'White Rice (cooked)', 400),
  ('Miso Soup', 'Dashi Stock', 800),           ('Miso Soup', 'Miso Paste', 80),
  ('Miso Soup', 'Tofu', 200),                  ('Miso Soup', 'Wakame (dried)', 10),
  ('Miso Soup', 'Green Onion', 30),
  ('Japanese Curry Rice', 'Beef Chuck', 500),  ('Japanese Curry Rice', 'Potato', 300),
  ('Japanese Curry Rice', 'Carrots', 200),     ('Japanese Curry Rice', 'Onion', 200),
  ('Japanese Curry Rice', 'Curry Roux', 120),  ('Japanese Curry Rice', 'Vegetable Oil', 30),
  ('Japanese Curry Rice', 'White Rice (cooked)', 600), ('Japanese Curry Rice', 'Garlic', 10),
  ('Yakisoba', 'Ramen Noodles (cooked)', 500), ('Yakisoba', 'Pork Chop', 250),
  ('Yakisoba', 'Cabbage', 200),                ('Yakisoba', 'Carrots', 100),
  ('Yakisoba', 'Bell Pepper', 100),            ('Yakisoba', 'Tonkatsu Sauce', 80),
  ('Yakisoba', 'Vegetable Oil', 30),           ('Yakisoba', 'Green Onion', 20),
  ('Gyoza', 'Ground Pork', 400),               ('Gyoza', 'Cabbage', 200),
  ('Gyoza', 'Garlic', 15),                     ('Gyoza', 'Ginger', 15),
  ('Gyoza', 'Green Onion', 30),                ('Gyoza', 'Gyoza Wrapper', 180),
  ('Gyoza', 'Soy Sauce', 30),                  ('Gyoza', 'Sesame Oil', 15),
  ('Gyoza', 'Vegetable Oil', 20),
  ('Tamagoyaki', 'Eggs', 200),                 ('Tamagoyaki', 'Mirin', 20),
  ('Tamagoyaki', 'Soy Sauce', 10),             ('Tamagoyaki', 'White Sugar', 10),
  ('Tamagoyaki', 'Dashi Stock', 30),           ('Tamagoyaki', 'Vegetable Oil', 10),
  ('Onigiri', 'White Rice (cooked)', 450),     ('Onigiri', 'Nori (seaweed)', 9),
  ('Onigiri', 'Salmon', 100),                  ('Onigiri', 'Soy Sauce', 10),
  ('Shrimp Tempura', 'Shrimp', 300),           ('Shrimp Tempura', 'Sweet Potato', 200),
  ('Shrimp Tempura', 'Bell Pepper', 100),      ('Shrimp Tempura', 'Flour', 150),
  ('Shrimp Tempura', 'Eggs', 100),             ('Shrimp Tempura', 'Vegetable Oil', 100),
  ('Shrimp Tempura', 'Soy Sauce', 30),         ('Shrimp Tempura', 'Dashi Stock', 100),
  ('Shrimp Tempura', 'Mirin', 20),
  ('Shoyu Ramen', 'Ramen Noodles (cooked)', 480), ('Shoyu Ramen', 'Pork Belly', 300),
  ('Shoyu Ramen', 'Eggs', 200),                ('Shoyu Ramen', 'Dashi Stock', 1000),
  ('Shoyu Ramen', 'Soy Sauce', 100),           ('Shoyu Ramen', 'Mirin', 30),
  ('Shoyu Ramen', 'Green Onion', 40),          ('Shoyu Ramen', 'Nori (seaweed)', 12),
  ('Shoyu Ramen', 'Garlic', 10),               ('Shoyu Ramen', 'Ginger', 10),
  ('Shoyu Ramen', 'Sesame Oil', 10),
  ('Dorayaki', 'Flour', 200),                  ('Dorayaki', 'Eggs', 150),
  ('Dorayaki', 'White Sugar', 80),             ('Dorayaki', 'Honey', 30),
  ('Dorayaki', 'Red Bean Paste (anko)', 240),  ('Dorayaki', 'Vegetable Oil', 10)
) AS v(recipe, ingredient, grams)
JOIN cooked.recipe r     ON lower(r.name) = lower(v.recipe) AND r.owner_user_id IS NULL
JOIN cooked.ingredient i ON i.name = v.ingredient
ON CONFLICT (recipe_id, ingredient_id) DO NOTHING;

INSERT INTO cooked.recipe_instruction (recipe_id, step_no, instruction)
SELECT r.id, v.step_no, v.instruction
FROM (VALUES
  ('Chicken Teriyaki', 1, 'Mix the soy sauce, mirin, sake and sugar into a teriyaki sauce.'),
  ('Chicken Teriyaki', 2, 'Sear the chicken thighs skin-side down until golden, then flip and cook through.'),
  ('Chicken Teriyaki', 3, 'Add the grated ginger and the sauce; simmer until it thickens and glazes the chicken.'),
  ('Chicken Teriyaki', 4, 'Slice and serve over the rice, spooning the glaze on top.'),
  ('Salmon Teriyaki', 1, 'Pat the salmon dry and pan-sear about 3 min per side.'),
  ('Salmon Teriyaki', 2, 'Mix the soy sauce, mirin, sake and sugar and pour into the pan.'),
  ('Salmon Teriyaki', 3, 'Simmer until the sauce reduces to a glossy glaze, basting the salmon.'),
  ('Salmon Teriyaki', 4, 'Serve over the rice with the glaze.'),
  ('Beef Gyudon', 1, 'Simmer the sliced onion in the dashi, soy sauce, mirin, sake and sugar until soft.'),
  ('Beef Gyudon', 2, 'Add the thinly sliced beef and simmer until just cooked.'),
  ('Beef Gyudon', 3, 'Ladle the beef and onion with some broth over bowls of rice.'),
  ('Beef Gyudon', 4, 'Top with green onion and a soft egg.'),
  ('Oyakodon', 1, 'Simmer the sliced onion in the dashi, soy sauce and mirin for 3 min.'),
  ('Oyakodon', 2, 'Add the bite-sized chicken and cook until done.'),
  ('Oyakodon', 3, 'Pour the beaten eggs over the top and cover until just set.'),
  ('Oyakodon', 4, 'Slide over the rice and finish with green onion.'),
  ('Katsudon', 1, 'Bread the pork in flour, beaten egg and panko, then deep-fry until golden; slice.'),
  ('Katsudon', 2, 'Simmer the sliced onion in the dashi, soy sauce and mirin.'),
  ('Katsudon', 3, 'Lay the sliced cutlet in the pan and pour the remaining beaten egg over it.'),
  ('Katsudon', 4, 'Cover until the egg just sets and slide over the rice.'),
  ('Chicken Katsu', 1, 'Bread the chicken in flour, beaten egg and panko.'),
  ('Chicken Katsu', 2, 'Deep-fry in the oil until golden and cooked through.'),
  ('Chicken Katsu', 3, 'Rest, then slice into strips.'),
  ('Chicken Katsu', 4, 'Serve with tonkatsu sauce, shredded cabbage and the rice.'),
  ('Miso Soup', 1, 'Heat the dashi until steaming but not boiling.'),
  ('Miso Soup', 2, 'Whisk the miso paste through a ladle until fully dissolved.'),
  ('Miso Soup', 3, 'Add the cubed tofu and rehydrated wakame and warm through.'),
  ('Miso Soup', 4, 'Finish with green onion and serve right away.'),
  ('Japanese Curry Rice', 1, 'Brown the beef, then saute the onion and garlic.'),
  ('Japanese Curry Rice', 2, 'Add the potato and carrots with enough water to cover and simmer until tender.'),
  ('Japanese Curry Rice', 3, 'Turn off the heat, melt in the curry roux, then simmer to thicken.'),
  ('Japanese Curry Rice', 4, 'Serve ladled over the rice.'),
  ('Yakisoba', 1, 'Stir-fry the sliced pork until cooked.'),
  ('Yakisoba', 2, 'Add the cabbage, carrots and bell pepper and stir-fry until just tender.'),
  ('Yakisoba', 3, 'Add the noodles and the sauce and toss over high heat.'),
  ('Yakisoba', 4, 'Finish with green onion.'),
  ('Gyoza', 1, 'Mix the ground pork, chopped cabbage, garlic, ginger, green onion, soy sauce and sesame oil.'),
  ('Gyoza', 2, 'Spoon filling into each wrapper and pleat to seal.'),
  ('Gyoza', 3, 'Fry the bases in oil until golden, then add water and cover to steam.'),
  ('Gyoza', 4, 'Cook until the water evaporates and the bottoms are crisp; serve with a dipping sauce.'),
  ('Tamagoyaki', 1, 'Whisk the eggs with the mirin, soy sauce, sugar and dashi.'),
  ('Tamagoyaki', 2, 'Oil a pan and pour a thin layer of egg, rolling it as it sets.'),
  ('Tamagoyaki', 3, 'Add more egg under the roll and repeat to build the layers.'),
  ('Tamagoyaki', 4, 'Shape, cool slightly and slice into pieces.'),
  ('Onigiri', 1, 'Season the warm rice lightly with salt.'),
  ('Onigiri', 2, 'Flake the cooked salmon and mix with a little soy sauce for the filling.'),
  ('Onigiri', 3, 'Wet your hands and form rice around the filling into triangles.'),
  ('Onigiri', 4, 'Wrap each with a strip of nori.'),
  ('Shrimp Tempura', 1, 'Make a tentsuyu dip by warming the dashi, soy sauce and mirin.'),
  ('Shrimp Tempura', 2, 'Whisk the flour with the egg and ice-cold water into a lumpy batter.'),
  ('Shrimp Tempura', 3, 'Dip the shrimp and vegetables and deep-fry until pale and crisp.'),
  ('Shrimp Tempura', 4, 'Drain and serve with the tentsuyu.'),
  ('Shoyu Ramen', 1, 'Braise the pork belly in soy sauce and mirin, then slice for chashu.'),
  ('Shoyu Ramen', 2, 'Build the broth from the dashi, soy sauce, garlic, ginger and sesame oil.'),
  ('Shoyu Ramen', 3, 'Cook the ramen noodles and divide into bowls, then pour the hot broth over.'),
  ('Shoyu Ramen', 4, 'Top with the chashu, halved soft eggs, green onion and nori.'),
  ('Dorayaki', 1, 'Whisk the eggs, sugar and honey, then fold in the flour into a smooth batter.'),
  ('Dorayaki', 2, 'Cook small pancakes on a lightly oiled pan until golden on both sides.'),
  ('Dorayaki', 3, 'Spread red bean paste on one pancake and sandwich with another.'),
  ('Dorayaki', 4, 'Wrap and rest briefly so they hold together.')
) AS v(recipe, step_no, instruction)
JOIN cooked.recipe r ON lower(r.name) = lower(v.recipe) AND r.owner_user_id IS NULL
ON CONFLICT (recipe_id, step_no) DO NOTHING;
