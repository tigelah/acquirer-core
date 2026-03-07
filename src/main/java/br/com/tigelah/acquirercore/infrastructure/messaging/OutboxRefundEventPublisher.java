package br.com.tigelah.acquirercore.infrastructure.messaging;

import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.domain.model.Refund;
import br.com.tigelah.acquirercore.domain.ports.RefundEventPublisher;
import br.com.tigelah.acquirercore.infrastructure.messaging.events.RefundRequestedEvent;
import br.com.tigelah.acquirercore.infrastructure.outbox.OutboxWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class OutboxRefundEventPublisher implements RefundEventPublisher {

    private final OutboxWriter outboxWriter;
    private final ObjectMapper objectMapper;

    public OutboxRefundEventPublisher(OutboxWriter outboxWriter, ObjectMapper objectMapper) {
        this.outboxWriter = outboxWriter;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishRefundRequested(Refund refund, Payment payment, String correlationId) {
        RefundRequestedEvent event = new RefundRequestedEvent(
                UUID.randomUUID(),
                Instant.now(),
                correlationId,
                refund.getId(),
                refund.getPaymentId(),
                refund.getMerchantId(),
                refund.getAmountCents(),
                refund.getCurrency(),
                refund.getType().name(),
                refund.getReason().name(),
                payment.getStatus().name(),
                payment.getAmountCents(),
                payment.getRefundedAmountCents(),
                payment.availableToRefund(),
                refund.getReversedFees().getMdrAmountCents(),
                refund.getReversedFees().getAcquirerFeeAmountCents(),
                refund.getReversedFees().getBrandFeeAmountCents()
        );

        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxWriter.enqueue(
                    "Refund",
                    refund.getId(),
                    Topics.REFUND_REQUESTED,
                    refund.getPaymentId().toString(),
                    payload
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize refund requested event", e);
        }
    }
}
