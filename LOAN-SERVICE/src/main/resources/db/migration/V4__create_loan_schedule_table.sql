CREATE TABLE IF NOT EXISTS loan_schedules
(
    id             VARCHAR(36)    NOT NULL PRIMARY KEY,
    installment    DECIMAL(19, 2) NOT NULL,
    payment_date   DATE,
    deadline       DATE           NOT NULL,
    payment_status VARCHAR(25)    NOT NULL,
    loan_id        VARCHAR(36)    NOT NULL,
    transfer_id    VARCHAR(36),
    created_at     TIMESTAMP,
    created_by     VARCHAR(100),
    updated_at     TIMESTAMP,
    updated_by     VARCHAR(100),
    CONSTRAINT fk_loan_schedules_loans FOREIGN KEY (loan_id) REFERENCES loans (id)
)

