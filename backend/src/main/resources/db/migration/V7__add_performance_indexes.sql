-- Unique constraint fixes missing DB-level enforcement for user_email
ALTER TABLE users ADD UNIQUE INDEX idx_users_email (user_email);

-- Covers pagination, recent transactions, and balance calculation (most-hit queries)
CREATE INDEX idx_transactions_account_date
    ON transactions(account_id, `date` DESC);

-- Covers charts and reports queries that filter by account + date range + type
CREATE INDEX idx_transactions_account_date_type
    ON transactions(account_id, `date`, type);

-- Covers the recurring transaction scheduler (avoids full table scan)
CREATE INDEX idx_transactions_recurring
    ON transactions(is_recurring_series_parent, next_recurrence_date);

-- Covers category dropdown queries split by user and type
CREATE INDEX idx_categories_user_type
    ON categories(user_id, type);

-- Covers default category queries split by type
CREATE INDEX idx_categories_default_type
    ON categories(is_default, type);

-- Covers pending debts widget (transaction_id FK + is_paid filter)
CREATE INDEX idx_debts_transaction_paid
    ON debts(transaction_id, is_paid);
