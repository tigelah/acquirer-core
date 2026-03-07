package br.com.tigelah.acquirercore.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Payment {
    private final UUID id;
    private final String merchantId;
    private final String orderId;
    private final Long amountCents;
    private final String currency;
    private final String panLast4;
    private PaymentStatus status;
    private String authCode;
    private final Instant createdAt;
    private UUID accountId;
    private String userId;
    private String panHash;
    private Integer installments;
    private long refundedAmountCents;
    private PaymentFees fees;

    public Payment(
            UUID id,
            String merchantId,
            String orderId,
            Long amountCents,
            String currency,
            String panLast4,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.merchantId = requireNonBlank(merchantId, "merchantId");
        this.orderId = requireNonBlank(orderId, "orderId");
        if (amountCents == null || amountCents <= 0) {
            throw new IllegalArgumentException("amountCents must be > 0");
        }
        this.amountCents = amountCents;
        this.currency = requireNonBlank(currency, "currency");
        this.panLast4 = requireNonBlank(panLast4, "panLast4");
        this.status = PaymentStatus.CREATED;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.installments = 1;
        this.refundedAmountCents = 0L;
        this.fees = PaymentFees.zero();
    }

    public void markAuthRequested() {
        ensureStatus(PaymentStatus.CREATED);
        this.status = PaymentStatus.AUTH_REQUESTED;
    }

    public void authorize(String authCode) {
        ensureStatus(PaymentStatus.AUTH_REQUESTED);
        this.authCode = requireNonBlank(authCode, "authCode");
        this.status = PaymentStatus.AUTHORIZED_HOLD;
    }

    public void decline() {
        if (status != PaymentStatus.AUTH_REQUESTED) {
            throw new IllegalStateException("payment cannot be declined from " + status);
        }
        this.status = PaymentStatus.DECLINED;
    }

    public void rejectByRisk() {
        if (status != PaymentStatus.AUTH_REQUESTED) {
            throw new IllegalStateException("payment cannot be risk rejected from " + status);
        }
        this.status = PaymentStatus.RISK_REJECTED;
    }

    public void markCaptureRequested() {
        ensureStatus(PaymentStatus.AUTHORIZED_HOLD);
        this.status = PaymentStatus.CAPTURE_REQUESTED;
    }

    public void markCaptured() {
        if (status != PaymentStatus.CAPTURE_REQUESTED && status != PaymentStatus.AUTHORIZED_HOLD) {
            throw new IllegalStateException("payment cannot be captured from " + status);
        }
        this.status = PaymentStatus.CAPTURED;
    }

    public void markSettled() {
        if (status != PaymentStatus.CAPTURED && status != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new IllegalStateException("payment cannot be settled from " + status);
        }
        this.status = PaymentStatus.SETTLED;
    }

    public void voidAuthorization() {
        ensureStatus(PaymentStatus.AUTHORIZED_HOLD);
        this.status = PaymentStatus.VOIDED;
    }

    public void expireAuthorization() {
        ensureStatus(PaymentStatus.AUTHORIZED_HOLD);
        this.status = PaymentStatus.EXPIRED;
    }

    public boolean canRefund() {
        return status == PaymentStatus.CAPTURED
                || status == PaymentStatus.SETTLED
                || status == PaymentStatus.PARTIALLY_REFUNDED;
    }

    public long availableToRefund() {
        long available = amountCents - refundedAmountCents;
        return Math.max(available, 0);
    }

    public RefundType refundTypeFor(long refundAmountCents) {
        validateRefundAmount(refundAmountCents);
        return refundAmountCents == availableToRefund() ? RefundType.FULL : RefundType.PARTIAL;
    }

    public void registerRefund(long refundAmountCents) {
        if (!canRefund()) {
            throw new IllegalStateException("payment cannot be refunded from " + status);
        }

        validateRefundAmount(refundAmountCents);

        this.refundedAmountCents += refundAmountCents;

        if (this.refundedAmountCents == this.amountCents) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
    }

    public boolean isFullyRefunded() {
        return refundedAmountCents == amountCents;
    }

    public boolean isPartiallyRefunded() {
        return refundedAmountCents > 0 && refundedAmountCents < amountCents;
    }

    public void defineFees(PaymentFees fees) {
        this.fees = Objects.requireNonNull(fees);
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPanHash(String panHash) {
        this.panHash = panHash;
    }

    public void setInstallments(Integer installments) {
        if (installments == null || installments <= 0) {
            throw new IllegalArgumentException("installments must be > 0");
        }
        this.installments = installments;
    }

    public void setRefundedAmountCents(long refundedAmountCents) {
        if (refundedAmountCents < 0 || refundedAmountCents > amountCents) {
            throw new IllegalArgumentException("invalid refundedAmountCents");
        }
        this.refundedAmountCents = refundedAmountCents;
    }

    private void validateRefundAmount(long refundAmountCents) {
        if (refundAmountCents <= 0) {
            throw new IllegalArgumentException("refundAmountCents must be > 0");
        }
        if (refundAmountCents > availableToRefund()) {
            throw new IllegalArgumentException("refundAmountCents exceeds available refundable amount");
        }
    }

    private void ensureStatus(PaymentStatus expected) {
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

    public UUID getId() { return id; }
    public String getMerchantId() { return merchantId;}
    public String getOrderId() { return orderId;}
    public Long getAmountCents() { return amountCents;}
    public String getCurrency() { return currency;}
    public String getPanLast4() { return panLast4;}
    public PaymentStatus getStatus() { return status;}
    public String getAuthCode() { return authCode;}
    public Instant getCreatedAt() { return createdAt;}
    public UUID getAccountId() { return accountId;}
    public String getUserId() { return userId;}
    public String getPanHash() { return panHash;}
    public Integer getInstallments() { return installments; }
    public long getRefundedAmountCents() { return refundedAmountCents; }
    public PaymentFees getFees() { return fees; }
}