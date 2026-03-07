package br.com.tigelah.acquirercore.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Refund {
    private final UUID id;
    private final UUID paymentId;
    private final String merchantId;
    private final long amountCents;
    private final String currency;
    private final RefundType type;
    private final RefundReason reason;
    private RefundStatus status;
    private final PaymentFees reversedFees;
    private final Instant createdAt;

    public Refund(
            UUID id,
            UUID paymentId,
            String merchantId,
            long amountCents,
            String currency,
            RefundType type,
            RefundReason reason,
            RefundStatus status,
            PaymentFees reversedFees,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.paymentId = Objects.requireNonNull(paymentId);
        this.merchantId = requireNonBlank(merchantId, "merchantId");
        if (amountCents <= 0) {
            throw new IllegalArgumentException("amountCents must be > 0");
        }
        this.amountCents = amountCents;
        this.currency = requireNonBlank(currency, "currency");
        this.type = Objects.requireNonNull(type);
        this.reason = Objects.requireNonNull(reason);
        this.status = Objects.requireNonNull(status);
        this.reversedFees = Objects.requireNonNull(reversedFees);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public void markIssued() {
        ensureStatus(RefundStatus.REQUESTED);
        this.status = RefundStatus.ISSUED;
    }

    public void markLedgerApplied() {
        if (status != RefundStatus.ISSUED && status != RefundStatus.REQUESTED) {
            throw new IllegalStateException("refund cannot move to LEDGER_APPLIED from " + status);
        }
        this.status = RefundStatus.LEDGER_APPLIED;
    }

    public void markSettlementAdjustmentPending() {
        if (status != RefundStatus.LEDGER_APPLIED && status != RefundStatus.ISSUED) {
            throw new IllegalStateException("refund cannot move to SETTLEMENT_ADJUSTMENT_PENDING from " + status);
        }
        this.status = RefundStatus.SETTLEMENT_ADJUSTMENT_PENDING;
    }

    public void markCompleted() {
        if (status == RefundStatus.FAILED) {
            throw new IllegalStateException("failed refund cannot be completed");
        }
        this.status = RefundStatus.COMPLETED;
    }

    public void markFailed() {
        this.status = RefundStatus.FAILED;
    }

    private void ensureStatus(RefundStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException("expected status " + expected + " but was " + status);
        }
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public String getCurrency() {
        return currency;
    }

    public RefundType getType() {
        return type;
    }

    public RefundReason getReason() {
        return reason;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public PaymentFees getReversedFees() {
        return reversedFees;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
