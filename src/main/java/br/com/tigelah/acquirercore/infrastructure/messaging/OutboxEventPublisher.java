package br.com.tigelah.acquirercore.infrastructure.messaging;

import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.domain.ports.EventPublisher;
import br.com.tigelah.acquirercore.infrastructure.outbox.OutboxWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.UUID;

@Component
public class OutboxEventPublisher implements EventPublisher {

    private final OutboxWriter outbox;
    private final ObjectMapper mapper;
    private final Clock clock;

    public OutboxEventPublisher(OutboxWriter outbox, ObjectMapper mapper, Clock clock) {
        this.outbox = outbox;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Override
    public void publishAuthorizeRequested(Payment payment, String correlationId, String idempotencyKey) {
        var payload = baseEnvelope(correlationId, Topics.PAYMENT_AUTHORIZE_REQUESTED);
        payload.put("idempotencyKey", idempotencyKey);

        payload.put("paymentId", payment.getId().toString());
        payload.put("merchantId", payment.getMerchantId());
        payload.put("orderId", payment.getOrderId());
        payload.put("amountCents", payment.getAmountCents());
        payload.put("currency", payment.getCurrency());
        payload.put("panLast4", payment.getPanLast4());
        if (payment.getAccountId() != null) payload.put("accountId", payment.getAccountId().toString());
        if (payment.getUserId() != null && !payment.getUserId().isBlank()) payload.put("userId", payment.getUserId());
        if (payment.getPanHash() != null && !payment.getPanHash().isBlank()) payload.put("panHash", payment.getPanHash());
        payload.put("installments", payment.getInstallments() == null ? 1 : payment.getInstallments());
        enqueue(payment, Topics.PAYMENT_AUTHORIZE_REQUESTED, payment.getId().toString(), payload);
    }

    @Override
    public void publishCaptureRequested(Payment payment, String correlationId) {
        var payload = baseEnvelope(correlationId, Topics.PAYMENT_CAPTURE_REQUESTED);

        payload.put("paymentId", payment.getId().toString());
        payload.put("merchantId", payment.getMerchantId());
        payload.put("amountCents", payment.getAmountCents());
        payload.put("currency", payment.getCurrency());
        payload.put("panLast4", payment.getPanLast4());
        if (payment.getAccountId() != null) payload.put("accountId", payment.getAccountId().toString());
        if (payment.getUserId() != null && !payment.getUserId().isBlank()) payload.put("userId", payment.getUserId());
        if (payment.getPanHash() != null && !payment.getPanHash().isBlank()) payload.put("panHash", payment.getPanHash());

        enqueue(payment, Topics.PAYMENT_CAPTURE_REQUESTED, payment.getId().toString(), payload);
    }

    private LinkedHashMap<String, Object> baseEnvelope(String correlationId, String type) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("eventId", UUID.randomUUID().toString());
        payload.put("occurredAt", Instant.now(clock).toString());
        payload.put("correlationId", correlationId);
        payload.put("type", type);
        return payload;
    }

    private void enqueue(Payment payment, String topic, String key, Object payload) {
        try {
            outbox.enqueue("Payment", payment.getId(), topic, key, mapper.writeValueAsString(payload));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to enqueue outbox event", e);
        }
    }
}