package br.com.tigelah.acquirercore.infrastructure.repositories;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "payments",
        indexes = {@Index(name = "idx_payments_merchant_order", columnList = "merchant_id, order_id", unique = true)}
)
public class PaymentEntity {
    @Id
    public UUID id;

    @Column(name = "merchant_id", nullable = false)
    public String merchantId;

    @Column(name = "order_id", nullable = false)
    public String orderId;

    @Column(name = "amount_cents", nullable = false)
    public Long amountCents;

    @Column(nullable = false)
    public String currency;

    @Column(name = "pan_last4", nullable = false)
    public String panLast4;

    @Column(nullable = false)
    public String status;

    @Column(name = "auth_code")
    public String authCode;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Version
    public long version;
}

