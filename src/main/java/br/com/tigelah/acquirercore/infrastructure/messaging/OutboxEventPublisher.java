package br.com.tigelah.acquirercore.infrastructure.messaging;

import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.domain.ports.EventPublisher;
import br.com.tigelah.acquirercore.infrastructure.outbox.OutboxWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class OutboxEventPublisher implements EventPublisher {
    private final OutboxWriter outbox;
    private final ObjectMapper mapper;

    public OutboxEventPublisher(OutboxWriter outbox, ObjectMapper mapper) {
        this.outbox = outbox;
        this.mapper = mapper;
    }

    @Override
    public void publishAuthorizeRequested(Payment payment, String correlationId, String idempotencyKey) {
        var payload = Map.of(
                "eventId", java.util.UUID.randomUUID().toString(),
                "occurredAt", Instant.now().toString(),
                "correlationId", correlationId,
                "idempotencyKey", idempotencyKey,
                "paymentId", payment.getId().toString(),
                "merchantId", payment.getMerchantId(),
                "orderId", payment.getOrderId(),
                "amountCents", payment.getAmountCents(),
                "currency", payment.getCurrency(),
                "panLast4", payment.getPanLast4()
        );
        enqueue(payment, Topics.PAYMENT_AUTHORIZE_REQUESTED, payment.getId().toString(), payload);
    }

    @Override
    public void publishCaptureRequested(Payment payment, String correlationId) {
        var payload = Map.of(
                "eventId", java.util.UUID.randomUUID().toString(),
                "occurredAt", Instant.now().toString(),
                "correlationId", correlationId,
                "paymentId", payment.getId().toString(),
                "merchantId", payment.getMerchantId(),
                "amountCents", payment.getAmountCents(),
                "currency", payment.getCurrency()
        );
        enqueue(payment, Topics.PAYMENT_CAPTURE_REQUESTED, payment.getId().toString(), payload);
    }

    private void enqueue(Payment payment, String topic, String key, Object payload) {
        try {
            outbox.enqueue("Payment", payment.getId(), topic, key, mapper.writeValueAsString(payload));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to enqueue outbox event", e);
        }
    }
}
