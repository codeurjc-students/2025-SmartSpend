ALTER TABLE transactions ADD COLUMN recurrence_end_date DATE;

UPDATE transactions child
INNER JOIN transactions parent 
    ON child.account_id = parent.account_id 
    AND child.title = parent.title 
    AND child.amount = parent.amount 
    AND child.type = parent.type
SET child.parent_id = parent.id
WHERE child.is_recurring_series_parent = 0       -- El hijo no es un padre
  AND parent.is_recurring_series_parent = 1      -- El padre sí tiene el flag
  AND child.parent_id IS NULL                    -- Solo hijos que aún estén huérfanos
  AND child.date >= parent.date; 
