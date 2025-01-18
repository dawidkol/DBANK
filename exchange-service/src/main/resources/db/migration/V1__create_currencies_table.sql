CREATE TABLE IF NOT EXISTS currencies
(
    id             VARCHAR(36)    NOT NULL PRIMARY KEY,
    name           VARCHAR(50)    NOT NULL,
    currency_type  VARCHAR(3)     NOT NULL UNIQUE,
    effective_date DATE           NOT NULL,
    bid            DECIMAL(19, 2) NOT NULL,
    ask            DECIMAL(19, 2) NOT NULL,
    created_at     TIMESTAMP,
    created_by     VARCHAR(100),
    updated_at     TIMESTAMP,
    updated_by     VARCHAR(100),
    active         BOOLEAN
)