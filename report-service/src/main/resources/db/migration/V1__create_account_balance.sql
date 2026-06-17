CREATE TABLE account_balance
(
    user_id    UUID PRIMARY KEY,
    balance    NUMERIC(19, 2) NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);