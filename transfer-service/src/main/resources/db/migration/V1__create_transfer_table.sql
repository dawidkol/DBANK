DROP TABLE IF EXISTS transfers;
CREATE TABLE transfers
(
    id                       VARCHAR(255)   NOT NULL PRIMARY KEY,
    sender_account_number    VARCHAR(255)   NOT NULL,
    recipient_account_number VARCHAR(255)   NOT NULL,
    amount                   DECIMAL(19, 4) NOT NULL,
    currency_type            VARCHAR(10)    NOT NULL,
    transfer_date            TIMESTAMP      NOT NULL,
    transfer_status          VARCHAR(20)    NOT NULL,
    description              VARCHAR(255),
    balance_after_transfer   DECIMAL(19, 4),
    created_at               TIMESTAMP      NOT NULL,
    created_by               VARCHAR(100),
    updated_at               TIMESTAMP,
    updated_by               VARCHAR(100)
);