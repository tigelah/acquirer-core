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

-- Busca rápida dos pendentes em ordem de criação
CREATE INDEX IF NOT EXISTS idx_outbox_status_created
    ON outbox_event (status, created_at);

-- Útil para rastrear por agregado
CREATE INDEX IF NOT EXISTS idx_outbox_aggregate
    ON outbox_event (aggregate_type, aggregate_id);