create table if not exists payments (
    id uuid primary key,
    merchant_id varchar(64) not null,
    order_id varchar(64) not null,
    amount_cents bigint not null,
    currency varchar(8) not null,
    pan_last4 varchar(4) not null,
    status varchar(32) not null,
    auth_code varchar(32),
    created_at timestamptz not null,
    version bigint not null default 0
    );

create unique index if not exists idx_payments_merchant_order on payments(merchant_id, order_id);

CREATE TABLE IF NOT EXISTS outbox_event (
    id              UUID PRIMARY KEY,
    aggregate_type  VARCHAR(64) NOT NULL,
    aggregate_id    UUID NOT NULL,
    topic           VARCHAR(128) NOT NULL,
    message_key          VARCHAR(128) NOT NULL,
    payload_json    TEXT NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    attempts        INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    sent_at         TIMESTAMPTZ,
    version         BIGINT NOT NULL DEFAULT 0
    );

CREATE INDEX IF NOT EXISTS idx_outbox_status_created
    ON outbox_event (status, created_at);

CREATE INDEX IF NOT EXISTS idx_outbox_aggregate
    ON outbox_event (aggregate_type, aggregate_id);