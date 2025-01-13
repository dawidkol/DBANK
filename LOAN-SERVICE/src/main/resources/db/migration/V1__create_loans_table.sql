DROP TABLE IF EXISTS loans;
CREATE TABLE loans
(
    id                     VARCHAR(36) PRIMARY KEY NOT NULL,
    user_id                VARCHAR(36)             NOT NULL,
    amount                 DECIMAL(19, 2)          NOT NULL,
    interest_rate          DECIMAL(19, 2)          NOT NULL,
    start_date             DATE                    NOT NULL,
    end_date               DATE                    NOT NULL,
    number_of_installments INT                     NOT NULL,
    remaining_amount       DECIMAL(19, 2)          NOT NULL,
    currency_type          VARCHAR(3)              NOT NULL,
    status                 VARCHAR(30)             NOT NULL,
    description            VARCHAR(300)            NOT NULL,
    created_at             TIMESTAMP,
    created_by             VARCHAR(100),
    updated_at             TIMESTAMP,
    updated_by             VARCHAR(100)
)