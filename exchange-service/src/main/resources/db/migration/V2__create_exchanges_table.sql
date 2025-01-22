CREATE TABLE IF NOT EXISTS exchanges
(
    id             VARCHAR(36)    NOT NULL PRIMARY KEY,
    account_number VARCHAR(36)    NOT NULL,
    currency_from  VARCHAR(3)     NOT NULL,
    value_from     DECIMAL(19, 2) NOT NULL,
    currency_to    VARCHAR(3)     NOT NULL,
    rate           DECIMAL(19, 2) NOT NULL,
    result         DECIMAL(19, 2) NOT NULL,
    created_at     TIMESTAMP,
    created_by     VARCHAR(100),
    updated_at     TIMESTAMP,
    updated_by     VARCHAR(100),
    active         BOOLEAN
)