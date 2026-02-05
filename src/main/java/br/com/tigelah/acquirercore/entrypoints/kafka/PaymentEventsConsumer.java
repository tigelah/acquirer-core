package br.com.tigelah.acquirercore.entrypoints.kafka;

import br.com.tigelah.acquirercore.application.events.PaymentEventRouter;
import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.domain.ports.PaymentRepository;
import br.com.tigelah.acquirercore.infrastructure.messaging.Topics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class PaymentEventsConsumer {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventsConsumer.class);

    private final PaymentRepository payments;
    private final ObjectMapper mapper;
    private final PaymentEventRouter router;

    public PaymentEventsConsumer(PaymentRepository payments, ObjectMapper mapper, PaymentEventRouter router) {
        this.payments = payments;
        this.mapper = mapper;
        this.router = router;
    }

    @KafkaListener(
            topics = {
                    Topics.PAYMENT_RISK_REJECTED,
                    Topics.PAYMENT_AUTHORIZED,
                    Topics.PAYMENT_DECLINED,
                    Topics.PAYMENT_CAPTURED,
                    Topics.SETTLEMENT_COMPLETED,
                    Topics.AUTHORIZATION_EXPIRED,
                    Topics.AUTHORIZATION_VOIDED
            },
            groupId = "${kafka.consumer.group-id:acquirer-core}"
    )
    public void onMessage(
            String message,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC, required = false) String receivedTopic
    ) {
        if (message == null || message.isBlank()) {
            log.warn("Ignoring empty kafka message. topic={}", receivedTopic);
            return;
        }

        final JsonNode root;
        try {
            root = mapper.readTree(message);
        } catch (Exception e) {
            log.error("Failed to parse kafka message as JSON. topic={}, message={}", receivedTopic, message, e);
            return;
        }

        try {
            if (root.hasNonNull("paymentIds") && root.get("paymentIds").isArray()) {
                handleSettlementBatch(root);
                return;
            }

            if (root.hasNonNull("paymentId")) {
                handleSinglePayment(root, receivedTopic);
                return;
            }

            log.warn("Unknown event shape (missing paymentId/paymentIds). topic={}, payload={}", receivedTopic, root);

        } catch (Exception e) {
            log.error("Failed to handle kafka message. topic={}, payload={}", receivedTopic, root, e);
        }
    }

    @Transactional
    void handleSinglePayment(JsonNode root, String receivedTopic) {
        UUID paymentId;
        try {
            paymentId = UUID.fromString(root.get("paymentId").asText());
        } catch (Exception e) {
            log.warn("Invalid paymentId in event. topic={}, payload={}", receivedTopic, root);
            return;
        }

        Payment payment = payments.getOrThrow(paymentId);


        String topicHint = (receivedTopic != null && !receivedTopic.isBlank())
                ? receivedTopic
                : root.path("type").asText("");

        if (!router.route(topicHint, payment, root)) {
            log.warn("No handler registered for event. topicHint={}, payload={}", topicHint, root);
            return;
        }

        payments.save(payment);
    }

    void handleSettlementBatch(JsonNode root) {
        for (JsonNode idNode : root.get("paymentIds")) {
            UUID paymentId;
            try {
                paymentId = UUID.fromString(idNode.asText());
            } catch (Exception e) {
                log.warn("Invalid paymentId in settlement batch. payload={}", root);
                continue;
            }
            handleSettlement(paymentId);
        }
    }

    @Transactional
    void handleSettlement(UUID paymentId) {
        Payment payment = payments.getOrThrow(paymentId);
        payment.markSettled();
        payments.save(payment);
    }
}