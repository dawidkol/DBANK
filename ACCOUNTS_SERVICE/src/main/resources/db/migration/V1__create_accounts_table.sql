DROP TABLE IF EXISTS accounts;
CREATE TABLE accounts
(
    account_number VARCHAR(26) NOT NULL PRIMARY KEY,
    account_type   VARCHAR(20),
    balance        DECIMAL,
    user_id        VARCHAR(255),
    created_at     TIMESTAMP,
    created_by     VARCHAR(100),
    updated_at     TIMESTAMP,
    updated_by     VARCHAR(100),
    active         BOOLEAN
);

