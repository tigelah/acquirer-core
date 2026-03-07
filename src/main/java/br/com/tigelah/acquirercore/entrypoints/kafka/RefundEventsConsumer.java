package br.com.tigelah.acquirercore.entrypoints.kafka;

import br.com.tigelah.acquirercore.application.usecase.HandleRefundEventUseCase;
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

@Component
public class RefundEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(RefundEventsConsumer.class);

    private final ObjectMapper mapper;
    private final HandleRefundEventUseCase handleRefundEventUseCase;

    public RefundEventsConsumer(
            ObjectMapper mapper,
            HandleRefundEventUseCase handleRefundEventUseCase
    ) {
        this.mapper = mapper;
        this.handleRefundEventUseCase = handleRefundEventUseCase;
    }

    @KafkaListener(
            topics = {
                    Topics.REFUND_ISSUED,
                    Topics.REFUND_LEDGER_APPLIED,
                    Topics.REFUND_COMPLETED,
                    Topics.REFUND_FAILED
            },
            groupId = "${kafka.consumer.group-id:acquirer-core}"
    )
    @Transactional
    public void onMessage(
            String message,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC, required = false) String receivedTopic
    ) {
        if (message == null || message.isBlank()) {
            log.warn("Ignoring empty refund kafka message. topic={}", receivedTopic);
            return;
        }

        final JsonNode root;
        try {
            root = mapper.readTree(message);
        } catch (Exception e) {
            log.error("Failed to parse refund kafka message as JSON. topic={}, message={}", receivedTopic, message, e);
            return;
        }

        try {
            handleRefundEventUseCase.execute(receivedTopic, root);
        } catch (Exception e) {
            log.error("Failed to handle refund kafka message. topic={}, payload={}", receivedTopic, root, e);
        }
    }
}
