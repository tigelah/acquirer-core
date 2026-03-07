package br.com.tigelah.acquirercore.infrastructure.repositories;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refunds")
public class RefundEntity {

    @Id
    public UUID id;

    @Column(nullable = false)
    public UUID paymentId;

    @Column(nullable = false)
    public String merchantId;

    @Column(nullable = false)
    public Long amountCents;

    @Column(nullable = false, length = 3)
    public String currency;

    @Column(nullable = false)
    public String type;

    @Column(nullable = false)
    public String reason;

    @Column(nullable = false)
    public String status;

    @Column(nullable = false)
    public Long reversedMdrAmountCents;

    @Column(nullable = false)
    public Long reversedAcquirerFeeAmountCents;

    @Column(nullable = false)
    public Long reversedBrandFeeAmountCents;

    @Column(nullable = false)
    public Instant createdAt;
}
