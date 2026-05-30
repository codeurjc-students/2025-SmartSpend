UPDATE categories
SET icon = '⇄'
WHERE name IN ('Traspaso (Salida)', 'Traspaso (Entrada)')
  AND is_default = true
  AND (icon IS NULL OR icon = '' OR icon = 'transfer');
