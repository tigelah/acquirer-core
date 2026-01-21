package br.com.tigelah.acquirercore.infrastructure.repositories;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_merchant_order", columnList = "merchantId,orderId", unique = true)
})
public class PaymentEntity {
    @Id
    public UUID id;

    @Column(nullable = false)
    public String merchantId;

    @Column(nullable = false)
    public String orderId;

    @Column(nullable = false)
    public Long amountCents;

    @Column(nullable = false)
    public String currency;

    @Column(nullable = false)
    public String panLast4;

    @Column(nullable = false)
    public String status;

    public String authCode;

    @Column(nullable = false)
    public Instant createdAt;

    @Version
    public long version;
}

