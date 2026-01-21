package br.com.tigelah.acquirercore.infrastructure.outbox;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event", indexes = {
        @Index(name = "idx_outbox_status", columnList = "status,createdAt")
})
public class OutboxEventEntity {
    @Id
    public UUID id;

    @Column(nullable = false)
    public String aggregateType;

    @Column(nullable = false)
    public UUID aggregateId;

    @Column(nullable = false)
    public String topic;

    @Column(nullable = false)
    public String key;

    @Lob
    @Column(nullable = false)
    public String payloadJson;

    @Column(nullable = false)
    public String status;

    @Column(nullable = false)
    public int attempts;

    @Column(nullable = false)
    public Instant createdAt;

    public Instant sentAt;

    @Version
    public long version;
}