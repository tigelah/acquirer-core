package br.com.tigelah.acquirercore.domain.model;

public class PaymentFees {
    private final long mdrAmountCents;
    private final long acquirerFeeAmountCents;
    private final long brandFeeAmountCents;

    public PaymentFees(long mdrAmountCents, long acquirerFeeAmountCents, long brandFeeAmountCents) {
        if (mdrAmountCents < 0 || acquirerFeeAmountCents < 0 || brandFeeAmountCents < 0) {
            throw new IllegalArgumentException("fees must be >= 0");
        }
        this.mdrAmountCents = mdrAmountCents;
        this.acquirerFeeAmountCents = acquirerFeeAmountCents;
        this.brandFeeAmountCents = brandFeeAmountCents;
    }

    public long getMdrAmountCents() {
        return mdrAmountCents;
    }

    public long getAcquirerFeeAmountCents() {
        return acquirerFeeAmountCents;
    }

    public long getBrandFeeAmountCents() {
        return brandFeeAmountCents;
    }

    public long total() {
        return mdrAmountCents + acquirerFeeAmountCents + brandFeeAmountCents;
    }

    public static PaymentFees zero() {
        return new PaymentFees(0, 0, 0);
    }
}