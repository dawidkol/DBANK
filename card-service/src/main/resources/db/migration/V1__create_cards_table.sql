CREATE TABLE IF NOT EXISTS cards
(
    id               VARCHAR(36)  NOT NULL PRIMARY KEY,
    card_number      VARCHAR(36)  NOT NULL,
    account_number   VARCHAR(36)  NOT NULL,
    user_id          VARCHAR(36)  NOT NULL,
    card_holder_name VARCHAR(150) NOT NULL,
    active_from      DATE         NOT NULL,
    expiry_date      DATE         NOT NULL,
    cvv              VARCHAR(3)   NOT NULL,
    card_type        VARCHAR(30)  NOT NULL,
    is_active        BOOLEAN      NOT NULL,
    created_at       TIMESTAMP,
    created_by       VARCHAR(100),
    updated_at       TIMESTAMP,
    updated_by       VARCHAR(100),
    active           BOOLEAN
)