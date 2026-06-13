-- Cooked Service — development seed data
-- Run after db/setup.sql:
--   psql -U postgres -d cooked -f db/seed.sql
--
-- Seed user password is "Password123!" (BCrypt, cost 10).

SET search_path TO cooked;

-- ── Vocabulary ─────────────────────────────────

INSERT INTO mood (name) VALUES
    ('comfort'), ('quick'), ('healthy'), ('indulgent'), ('festive'), ('light')
ON CONFLICT DO NOTHING;

INSERT INTO cuisine (name) VALUES
    ('Filipino'), ('French'), ('Japanese'), ('Italian'), ('Mexican'),
    ('Indian'), ('Chinese'), ('American'), ('Thai'), ('Mediterranean')
ON CONFLICT DO NOTHING;

-- ── Demo user (password: Password123!) ─────────

INSERT INTO app_user (id, email, display_name, handle, password_hash, role)
VALUES (1, 'chef@example.com', 'Demo Chef', 'demochef',
        '$2a$10$MaYxR0ZQ3xbZVIkkL2K1B.WG8sW1aRh8QFvWaOQDo96DBgR5HdGwe', 'ADMIN')
ON CONFLICT (email) DO NOTHING;

-- ── Builtin ingredients ─────────────────────────

INSERT INTO ingredient (id, name, category, kcal_per_gram, grams_per_piece, is_builtin)
VALUES
    (1, 'Chicken thighs (boneless)',  'Meat',     0.209, NULL, TRUE),
    (2, 'Soy sauce',                  'Condiment',0.006, NULL, TRUE),
    (3, 'Cane vinegar',               'Condiment',0.018, NULL, TRUE),
    (4, 'Garlic clove',               'Vegetable',1.490, 5.0,  TRUE),
    (5, 'Cooked white rice',          'Grain',    0.130, NULL, TRUE),
    (6, 'Beef chuck',                 'Meat',     2.500, NULL, TRUE),
    (7, 'Red wine',                   'Beverage', 0.070, NULL, TRUE),
    (8, 'Button mushrooms',           'Vegetable',0.220, NULL, TRUE),
    (9, 'Olive oil',                  'Oil',      8.840, NULL, TRUE),
    (10,'Egg',                        'Protein',  1.430, 50.0, TRUE)
ON CONFLICT (name) DO NOTHING;

-- ── Sample recipes ──────────────────────────────

INSERT INTO recipe (id, name, owner_user_id, cuisine, prep_time_min, servings, is_community)
VALUES
    (1, 'Chicken Adobo',    1, 'Filipino', 15, 4, FALSE),
    (2, 'Beef Bourguignon', 1, 'French',   40, 6, FALSE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO recipe_mood (recipe_id, mood) VALUES
    (1, 'comfort'), (1, 'quick'),
    (2, 'comfort'), (2, 'indulgent')
ON CONFLICT DO NOTHING;

INSERT INTO recipe_ingredient (recipe_id, ingredient_id, grams) VALUES
    (1, 1, 800), (1, 2, 120), (1, 3, 80), (1, 4, 30),
    (2, 6, 1500),(2, 7, 750), (2, 8, 300)
ON CONFLICT DO NOTHING;

INSERT INTO recipe_instruction (recipe_id, step_no, instruction) VALUES
    (1, 1, 'Marinate chicken in soy sauce and garlic for 30 minutes.'),
    (1, 2, 'Brown chicken in oil over medium-high heat.'),
    (1, 3, 'Add vinegar, bay leaves, and peppercorns; simmer 40 minutes.'),
    (1, 4, 'Serve over steamed rice.'),
    (2, 1, 'Sear beef chuck in batches until browned.'),
    (2, 2, 'Sauté mirepoix; deglaze with red wine.'),
    (2, 3, 'Braise covered at 160°C for 3 hours.'),
    (2, 4, 'Finish with mushrooms and adjust seasoning.')
ON CONFLICT DO NOTHING;

-- Keep identity sequences ahead of seeded ids
SELECT setval(pg_get_serial_sequence('app_user',  'id'), GREATEST(1000, (SELECT MAX(id) FROM app_user)));
SELECT setval(pg_get_serial_sequence('ingredient','id'), GREATEST(1000, (SELECT MAX(id) FROM ingredient)));
SELECT setval(pg_get_serial_sequence('recipe',    'id'), GREATEST(1000, (SELECT MAX(id) FROM recipe)));