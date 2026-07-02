INSERT INTO categories (name, color, icon, type, is_default, user_id)
SELECT 'Suscripciones', '#9b59b6', '📺', 'EXPENSE', true, NULL
WHERE NOT EXISTS (
  SELECT 1 FROM categories
  WHERE name = 'Suscripciones' AND type = 'EXPENSE' AND is_default = true
);
