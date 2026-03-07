package br.com.tigelah.acquirercore.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Chargeback {
    private final UUID id;
    private final UUID paymentId;
    private final String merchantId;
    private final long amountCents;
    private final String currency;
    private final ChargebackReason reason;
    private ChargebackStatus status;
    private final Instant createdAt;

    public Chargeback(
            UUID id,
            UUID paymentId,
            String merchantId,
            long amountCents,
            String currency,
            ChargebackReason reason,
            ChargebackStatus status,
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
        this.reason = Objects.requireNonNull(reason);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public void markUnderReview() {
        if (status != ChargebackStatus.OPEN) {
            throw new IllegalStateException("chargeback cannot move to UNDER_REVIEW from " + status);
        }
        this.status = ChargebackStatus.UNDER_REVIEW;
    }

    public void markWon() {
        if (status != ChargebackStatus.OPEN && status != ChargebackStatus.UNDER_REVIEW) {
            throw new IllegalStateException("chargeback cannot move to WON from " + status);
        }
        this.status = ChargebackStatus.WON;
    }

    public void markLost() {
        if (status != ChargebackStatus.OPEN && status != ChargebackStatus.UNDER_REVIEW) {
            throw new IllegalStateException("chargeback cannot move to LOST from " + status);
        }
        this.status = ChargebackStatus.LOST;
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

    public ChargebackReason getReason() {
        return reason;
    }

    public ChargebackStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
