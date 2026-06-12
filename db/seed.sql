-- Cooked Service — development seed data
-- Run after db/setup.sql:
--   psql -U postgres -d cooked -f db/seed.sql
--
-- Seed user password is "Password123!" (BCrypt, cost 10).

SET search_path TO cooked_service;

INSERT INTO users (id, email, password, name)
VALUES (1, 'chef@example.com', '$2y$10$MaYxR0ZQ3xbZVIkkL2K1B.WG8sW1aRh8QFvWaOQDo96DBgR5HdGwe', 'Demo Chef')
ON CONFLICT (email) DO NOTHING;

INSERT INTO recipe (id, user_id, name, description, cuisine, difficulty, prep_minutes, cook_minutes, servings, instructions)
VALUES
    (1, 1, 'Chicken Adobo', 'Classic Filipino braised chicken in soy sauce and vinegar.', 'Filipino', 'easy', 15, 45, 4,
     '1. Marinate chicken in soy sauce and garlic for 30 minutes.\n2. Brown chicken in oil.\n3. Add vinegar, bay leaves, and peppercorns; simmer 40 minutes.\n4. Serve over rice.'),
    (2, 1, 'Beef Bourguignon', 'Slow-braised beef in red wine.', 'French', 'hard', 40, 180, 6,
     '1. Sear beef in batches.\n2. Sauté mirepoix; deglaze with red wine.\n3. Braise covered at 160°C for 3 hours.\n4. Finish with pearl onions and mushrooms.')
ON CONFLICT (id) DO NOTHING;

INSERT INTO ingredient (recipe_id, name, quantity, unit, position)
VALUES
    (1, 'Chicken thighs', '1', 'kg', 1),
    (1, 'Soy sauce', '120', 'ml', 2),
    (1, 'Cane vinegar', '80', 'ml', 3),
    (1, 'Garlic cloves', '6', NULL, 4),
    (1, 'Bay leaves', '3', NULL, 5),
    (2, 'Beef chuck', '1.5', 'kg', 1),
    (2, 'Red wine', '750', 'ml', 2),
    (2, 'Pearl onions', '250', 'g', 3),
    (2, 'Button mushrooms', '300', 'g', 4);

-- Keep identity sequences ahead of explicitly inserted ids
SELECT setval(pg_get_serial_sequence('users', 'id'), GREATEST(100000, (SELECT MAX(id) FROM users)));
SELECT setval(pg_get_serial_sequence('recipe', 'id'), GREATEST(100000, (SELECT MAX(id) FROM recipe)));