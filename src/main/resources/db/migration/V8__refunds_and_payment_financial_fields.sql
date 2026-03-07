ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS refunded_amount_cents BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS mdr_amount_cents BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS acquirer_fee_amount_cents BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS brand_fee_amount_cents BIGINT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS refunds (
                                       id UUID PRIMARY KEY,
                                       payment_id UUID NOT NULL,
                                       merchant_id VARCHAR(255) NOT NULL,
                                       amount_cents BIGINT NOT NULL,
                                       currency VARCHAR(3) NOT NULL,
                                       type VARCHAR(50) NOT NULL,
                                       reason VARCHAR(50) NOT NULL,
                                       status VARCHAR(50) NOT NULL,
                                       reversed_mdr_amount_cents BIGINT NOT NULL,
                                       reversed_acquirer_fee_amount_cents BIGINT NOT NULL,
                                       reversed_brand_fee_amount_cents BIGINT NOT NULL,
                                       created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_refunds_payment_id ON refunds(payment_id);
CREATE INDEX IF NOT EXISTS idx_refunds_status ON refunds(status);