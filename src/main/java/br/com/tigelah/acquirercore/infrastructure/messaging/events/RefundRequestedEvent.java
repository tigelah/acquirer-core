package br.com.tigelah.acquirercore.infrastructure.messaging.events;

import java.time.Instant;
import java.util.UUID;

public record RefundRequestedEvent(
        UUID eventId,
        Instant occurredAt,
        String correlationId,
        UUID refundId,
        UUID paymentId,
        String merchantId,
        long amountCents,
        String currency,
        String refundType,
        String refundReason,
        String paymentStatusAtRequest,
        long originalAmountCents,
        long alreadyRefundedAmountCents,
        long remainingRefundableAmountCents,
        long reversedMdrAmountCents,
        long reversedAcquirerFeeAmountCents,
        long reversedBrandFeeAmountCents
) {
}
