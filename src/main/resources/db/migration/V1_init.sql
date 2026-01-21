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

create table if not exists outbox_event (
                                            id uuid primary key,
                                            aggregate_type varchar(64) not null,
    aggregate_id uuid not null,
    topic varchar(128) not null,
    key varchar(128) not null,
    payload_json text not null,
    status varchar(16) not null,
    attempts int not null,
    created_at timestamptz not null,
    sent_at timestamptz,
    version bigint not null default 0
    );

create index if not exists idx_outbox_status on outbox_event(status, created_at);