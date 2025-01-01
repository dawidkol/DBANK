CREATE TABLE IF NOT EXISTS loan_details(
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    loan_account_number VARCHAR(36) NOT NULL,
    loan_id VARCHAR(36) NOT NULL,
    created_at       TIMESTAMP,
    created_by       VARCHAR(100),
    updated_at       TIMESTAMP,
    updated_by       VARCHAR(100),
    schedule_available BOOLEAN NOT NULL,
    CONSTRAINT fk_loan_details_loans FOREIGN KEY (loan_id) REFERENCES loans(id)
);