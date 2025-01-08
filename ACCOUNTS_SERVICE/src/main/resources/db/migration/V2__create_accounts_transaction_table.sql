DROP TABLE IF EXISTS accounts_transactions;
CREATE TABLE accounts_transactions (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    balance_after_transaction DECIMAL(19,2) NOT NULL,
    balance_before_transaction DECIMAL(19,2) NOT NULL,
    account_number VARCHAR(36) NOT NULL,
    CONSTRAINT fk_account_transaction_id FOREIGN KEY (account_number) REFERENCES accounts(account_number)
);