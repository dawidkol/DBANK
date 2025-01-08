CREATE TABLE IF NOT EXISTS account_balances
(
    id             VARCHAR(36)    NOT NULL PRIMARY KEY,
    currency_type  VARCHAR(3)     NOT NULL,
    balance        decimal(19, 2) NOT NULL,
    account_number VARCHAR(36)    NOT NULL,
    created_at     TIMESTAMP,
    created_by     VARCHAR(100),
    updated_at     TIMESTAMP,
    updated_by     VARCHAR(100),
    active         BOOLEAN,
    CONSTRAINT fk_account_balances_accounts FOREIGN KEY (account_number) REFERENCES accounts(account_number)
)