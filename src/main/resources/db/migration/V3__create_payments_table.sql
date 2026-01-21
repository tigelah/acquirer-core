CREATE TABLE IF NOT EXISTS payments (
     id              UUID PRIMARY KEY,
     amount          BIGINT NOT NULL,
     currency        VARCHAR(3) NOT NULL,
     status          VARCHAR(32) NOT NULL,
     pan_last4       VARCHAR(4) NOT NULL,
     card_holder     VARCHAR(128) NOT NULL,
     card_brand      VARCHAR(32),
     auth_code       VARCHAR(64),
     created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
     updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
     version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_payments_status
    ON payments (status);

CREATE INDEX IF NOT EXISTS idx_payments_created_at
    ON payments (created_at);
