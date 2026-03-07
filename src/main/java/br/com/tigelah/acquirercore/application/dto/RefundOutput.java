package br.com.tigelah.acquirercore.application.dto;

import br.com.tigelah.acquirercore.domain.model.Refund;
import br.com.tigelah.acquirercore.domain.model.RefundStatus;
import br.com.tigelah.acquirercore.domain.model.RefundType;

import java.time.Instant;
import java.util.UUID;

public record RefundOutput(
        UUID id,
        UUID paymentId,
        String merchantId,
        long amountCents,
        String currency,
        RefundType type,
        String reason,
        RefundStatus status,
        long reversedMdrAmountCents,
        long reversedAcquirerFeeAmountCents,
        long reversedBrandFeeAmountCents,
        Instant createdAt
) {
    public static RefundOutput from(Refund refund) {
        return new RefundOutput(
                refund.getId(),
                refund.getPaymentId(),
                refund.getMerchantId(),
                refund.getAmountCents(),
                refund.getCurrency(),
                refund.getType(),
                refund.getReason().name(),
                refund.getStatus(),
                refund.getReversedFees().getMdrAmountCents(),
                refund.getReversedFees().getAcquirerFeeAmountCents(),
                refund.getReversedFees().getBrandFeeAmountCents(),
                refund.getCreatedAt()
        );
    }
}
