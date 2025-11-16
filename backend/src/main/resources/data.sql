-- Insertar usuarios de prueba
INSERT INTO users (user_name, user_email, user_hashed_password)
VALUES ('testuser', 'test@smartspend.com', 'hashed_password_123');

INSERT INTO users (user_name, user_email, user_hashed_password)  
VALUES ('johndoe', 'john@example.com', 'hashed_password_456');

-- Insertar cuentas bancarias de prueba  
INSERT INTO bank_accounts (user_id, account_name, current_balance, created_at)
VALUES (1, 'Cuenta Corriente Principal', 2500.00, NOW());

INSERT INTO bank_accounts (user_id, account_name, current_balance, created_at)
VALUES (1, 'Cuenta Ahorros', 5000.00, NOW());

-- Categorías por defecto
INSERT INTO categories (name, icon, color, type, is_default, user_id)
VALUES ('Nómina', '💰', '#27ae60', 'INCOME', true, null);

INSERT INTO categories (name, icon, color, type, is_default, user_id)
VALUES ('Freelance', '💻', '#16a085', 'INCOME', true, null);

INSERT INTO categories (name, icon, color, type, is_default, user_id)
VALUES ('Inversiones', '📈', '#2980b9', 'INCOME', true, null);

INSERT INTO categories (name, icon, color, type, is_default, user_id)
VALUES ('Ventas', '🛒', '#8e44ad', 'INCOME', true, null);

INSERT INTO categories (name, icon, color, type, is_default, user_id)
VALUES ('Regalos', '🎁', '#e67e22', 'INCOME', true, null);

INSERT INTO categories (name, icon, color, type, is_default, user_id)
VALUES ('Comida', '🍔', '#e74c3c', 'EXPENSE', true, null);

INSERT INTO categories (name, icon, color, type, is_default, user_id)
VALUES ('Transporte', '🚗', '#3498db', 'EXPENSE', true, null);

-- Transacciones con IDs correctos
INSERT INTO transactions (title, description, amount, date, type, category_id, recurrence, account_id)
VALUES ('Nómina Septiembre', 'Salario mensual', 1200.00, '2025-09-01', 'INCOME', 1, 'NONE', 1);

INSERT INTO transactions (title, description, amount, date, type, category_id, recurrence, account_id)
VALUES ('Compra supermercado', 'Compra semanal', 45.30, '2025-09-06', 'EXPENSE', 2, 'NONE', 1);

INSERT INTO transactions (title, description, amount, date, type, category_id, recurrence, account_id)
VALUES ('Suscripción Netflix', 'Plan mensual streaming', 12.99, '2025-09-07', 'EXPENSE', 3, 'MONTHLY', 1);