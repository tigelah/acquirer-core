package br.com.tigelah.acquirercore.infrastructure.outbox;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event", indexes = {
        @Index(name = "idx_outbox_status", columnList = "status, created_at")
})
public class OutboxEventEntity {

    @Id
    public UUID id;

    @Column(name = "aggregate_type", nullable = false)
    public String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    public UUID aggregateId;

    @Column(name = "topic", nullable = false)
    public String topic;

    @Column(name = "message_key", nullable = false)
    public String messageKey;

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    public String payloadJson;

    @Column(name = "status", nullable = false)
    public String status;

    @Column(name = "attempts", nullable = false)
    public int attempts;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "sent_at")
    public Instant sentAt;

    @Version
    public long version;
}