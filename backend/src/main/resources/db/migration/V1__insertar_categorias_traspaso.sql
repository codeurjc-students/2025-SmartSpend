INSERT INTO categories (name, color, icon, type, is_default, user_id)
SELECT 'Traspaso (Salida)', '#6c757d', 'transfer', 'EXPENSE', true, NULL
WHERE NOT EXISTS (
  SELECT 1 FROM categories
  WHERE name = 'Traspaso (Salida)' AND type = 'EXPENSE' AND is_default = true
);

INSERT INTO categories (name, color, icon, type, is_default, user_id)
SELECT 'Traspaso (Entrada)', '#6c757d', 'transfer', 'INCOME', true, NULL
WHERE NOT EXISTS (
  SELECT 1 FROM categories
  WHERE name = 'Traspaso (Entrada)' AND type = 'INCOME' AND is_default = true
);
