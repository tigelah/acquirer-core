package br.com.tigelah.acquirercore.infrastructure.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Component
public class OutboxPublisherScheduler {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherScheduler.class);

    private final JpaOutboxRepository outbox;
    private final KafkaTemplate<String, String> kafka;
    private final Clock clock;

    public OutboxPublisherScheduler(JpaOutboxRepository outbox, KafkaTemplate<String, String> kafka, Clock clock) {
        this.outbox = outbox;
        this.kafka = kafka;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay-ms:1000}")
    @Transactional
    public void publishPending() {
        var items = outbox.findPending(PageRequest.of(0, 50));
        for (var e : items) {
            try {
                kafka.send(e.topic, e.messageKey, e.payloadJson).get();
                e.status = "SENT";
                e.sentAt = Instant.now(clock);
            } catch (Exception ex) {
                e.attempts += 1;
                e.status = e.attempts >= 10 ? "FAILED" : "PENDING";
                log.warn("outbox publish failed id={} attempts={} topic={}", e.id, e.attempts, e.topic, ex);
            }
            outbox.save(e);
        }
    }
}