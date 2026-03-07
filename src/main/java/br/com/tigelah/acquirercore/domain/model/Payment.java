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

    public void setInstallments(Integer installments) {
        if (installments == null || installments < 1) {
            throw new IllegalArgumentException("installments must be >= 1");
        }
        this.installments = installments;
    }

    public void markAuthRequested() {
        ensureStatus(PaymentStatus.CREATED, "auth request");
        this.status = PaymentStatus.AUTH_REQUESTED;
    }

    public void markRiskRejected() {
        ensureStatus(PaymentStatus.AUTH_REQUESTED, "risk rejected");
        this.status = PaymentStatus.RISK_REJECTED;
    }

    public void authorize(String authCode) {
        ensureStatus(PaymentStatus.AUTH_REQUESTED, "authorize");
        this.status = PaymentStatus.AUTHORIZED_HOLD;
        this.authCode = requireNonBlank(authCode, "authCode");
    }

    public void markAuthorizedHold(String authCode) {
        ensureStatus(PaymentStatus.AUTH_REQUESTED, "mark authorized hold");
        this.status = PaymentStatus.AUTHORIZED_HOLD;
        this.authCode = requireNonBlank(authCode, "authCode");
    }

    public void decline() {
        ensureStatus(PaymentStatus.AUTH_REQUESTED, "decline");
        this.status = PaymentStatus.DECLINED;
    }

    public boolean canCapture() {
        return this.status == PaymentStatus.AUTHORIZED_HOLD;
    }

    public void markCaptureRequested() {
        ensureStatus(PaymentStatus.AUTHORIZED_HOLD, "capture request");
        this.status = PaymentStatus.CAPTURE_REQUESTED;
    }

    public void markCaptured() {
        ensureStatus(PaymentStatus.CAPTURE_REQUESTED, "captured");
        this.status = PaymentStatus.CAPTURED;
    }

    public void markSettled() {
        if (this.status != PaymentStatus.CAPTURED
                && this.status != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new IllegalStateException("Payment must be CAPTURED or PARTIALLY_REFUNDED to be SETTLED");
        }
        this.status = PaymentStatus.SETTLED;
    }

    public void voidAuthorization() {
        ensureStatus(PaymentStatus.AUTHORIZED_HOLD, "void authorization");
        this.status = PaymentStatus.VOIDED;
    }

    public void expireAuthorization() {
        ensureStatus(PaymentStatus.AUTHORIZED_HOLD, "expire authorization");
        this.status = PaymentStatus.EXPIRED;
    }

    public boolean canRefund() {
        return this.status == PaymentStatus.CAPTURED
                || this.status == PaymentStatus.SETTLED
                || this.status == PaymentStatus.PARTIALLY_REFUNDED;
    }

    public long availableToRefund() {
        long remaining = amountCents - refundedAmountCents;
        return Math.max(remaining, 0);
    }

    public RefundType refundTypeFor(long refundAmountCents) {
        validateRefundAmount(refundAmountCents);
        return refundAmountCents == availableToRefund() ? RefundType.FULL : RefundType.PARTIAL;
    }

    public void registerRefund(long refundAmountCents) {
        if (!canRefund()) {
            throw new IllegalStateException("payment cannot be refunded from status " + status);
        }

        validateRefundAmount(refundAmountCents);

        this.refundedAmountCents += refundAmountCents;

        if (this.refundedAmountCents == this.amountCents) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
    }

    public void defineFees(PaymentFees fees) {
        this.fees = Objects.requireNonNull(fees);
    }

    public void setRefundedAmountCents(long refundedAmountCents) {
        if (refundedAmountCents < 0 || refundedAmountCents > amountCents) {
            throw new IllegalArgumentException("invalid refundedAmountCents");
        }
        this.refundedAmountCents = refundedAmountCents;
    }

    public void bindLimitScope(UUID accountId, String userId, String panHash) {
        this.accountId = accountId;
        this.userId = userId;
        this.panHash = panHash;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = Objects.requireNonNull(accountId, "accountId is required");
    }

    public void setUserId(String userId) {
        this.userId = (userId == null || userId.isBlank()) ? null : userId;
    }

    public void setPanHash(String panHash) {
        this.panHash = requireNonBlank(panHash, "panHash");
    }

    private void validateRefundAmount(long refundAmountCents) {
        if (refundAmountCents <= 0) {
            throw new IllegalArgumentException("refundAmountCents must be > 0");
        }
        if (refundAmountCents > availableToRefund()) {
            throw new IllegalArgumentException("refundAmountCents exceeds available refundable amount");
        }
    }

    private void ensureStatus(PaymentStatus expected, String action) {
        if (this.status != expected) {
            throw new IllegalStateException(
                    "Invalid state for " + action + ": expected " + expected + " but was " + this.status
            );
        }
    }

    private static String requireNonBlank(String v, String field) {
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return v;
    }

    public UUID getAccountId() { return accountId; }
    public String getUserId() { return userId; }
    public String getPanHash() { return panHash; }
    public UUID getId() { return id; }
    public String getMerchantId() { return merchantId; }
    public String getOrderId() { return orderId; }
    public Long getAmountCents() { return amountCents; }
    public String getCurrency() { return currency; }
    public String getPanLast4() { return panLast4; }
    public PaymentStatus getStatus() { return status; }
    public String getAuthCode() { return authCode; }
    public Instant getCreatedAt() { return createdAt; }
    public Integer getInstallments() { return installments; }
    public long getRefundedAmountCents() { return refundedAmountCents; }
    public PaymentFees getFees() { return fees; }
}