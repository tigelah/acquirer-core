package br.com.tigelah.acquirercore.infrastructure.outbox;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
public class OutboxWriter {
    private final JpaOutboxRepository repo;
    private final Clock clock;

    public OutboxWriter(JpaOutboxRepository repo, Clock clock) {
        this.repo = repo;
        this.clock = clock;
    }

    @Transactional
    public void enqueue(String aggregateType, UUID aggregateId, String topic, String key, String payloadJson) {
        var e = new OutboxEventEntity();
        e.id = UUID.randomUUID();
        e.aggregateType = aggregateType;
        e.aggregateId = aggregateId;
        e.topic = topic;
        e.key = key;
        e.payloadJson = payloadJson;
        e.status = "PENDING";
        e.attempts = 0;
        e.createdAt = Instant.now(clock);
        repo.save(e);
    }
}