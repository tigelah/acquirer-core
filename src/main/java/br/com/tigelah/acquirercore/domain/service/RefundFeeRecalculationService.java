package br.com.tigelah.acquirercore.domain.service;

import br.com.tigelah.acquirercore.domain.model.PaymentFees;

public class RefundFeeRecalculationService {

    public PaymentFees calculateProportionalReversal(
            long originalAmountCents,
            long refundAmountCents,
            PaymentFees originalFees
    ) {
        if (originalAmountCents <= 0) {
            throw new IllegalArgumentException("originalAmountCents must be > 0");
        }
        if (refundAmountCents <= 0) {
            throw new IllegalArgumentException("refundAmountCents must be > 0");
        }
        if (refundAmountCents > originalAmountCents) {
            throw new IllegalArgumentException("refundAmountCents cannot exceed originalAmountCents");
        }

        long mdr = proportional(originalFees.getMdrAmountCents(), refundAmountCents, originalAmountCents);
        long acquirer = proportional(originalFees.getAcquirerFeeAmountCents(), refundAmountCents, originalAmountCents);
        long brand = proportional(originalFees.getBrandFeeAmountCents(), refundAmountCents, originalAmountCents);

        return new PaymentFees(mdr, acquirer, brand);
    }

    private long proportional(long feeAmount, long refundAmountCents, long originalAmountCents) {
        return Math.round((double) feeAmount * refundAmountCents / originalAmountCents);
    }
}
