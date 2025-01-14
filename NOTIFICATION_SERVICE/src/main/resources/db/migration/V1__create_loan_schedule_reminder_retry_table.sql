CREATE TABLE IF NOT EXISTS loan_reminder_retry
(
    id             VARCHAR(36)    NOT NULL PRIMARY KEY,
    installment    decimal(19, 2) NOT NULL,
    deadline       date           NOT NULL,
    payment_status VARCHAR(25)    NOT NULL,
    user_id        VARCHAR(36)    NOT NULL,
    sent           BOOLEAN,
    created_at     TIMESTAMP,
    created_by     VARCHAR(100),
    updated_at     TIMESTAMP,
    updated_by     VARCHAR(100)
)