INSERT INTO categories (name, color, icon, type, is_default, user_id)
SELECT 'Restaurantes', '#e74c3c', '🍽️', 'EXPENSE', true, NULL
WHERE NOT EXISTS (
  SELECT 1 FROM categories
  WHERE name = 'Restaurantes' AND type = 'EXPENSE' AND is_default = true
);
