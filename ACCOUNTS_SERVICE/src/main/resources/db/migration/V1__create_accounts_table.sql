DROP TABLE IF EXISTS accounts;
CREATE TABLE accounts
(
    account_number NUMERIC NOT NULL PRIMARY KEY,
    account_type   VARCHAR(20),
    balance        DECIMAL,
    user_id        VARCHAR(255) UNIQUE,
    created_at     TIMESTAMP,
    created_by     VARCHAR(100),
    updated_at     TIMESTAMP,
    updated_by     VARCHAR(100)
);

