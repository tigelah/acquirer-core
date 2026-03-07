package br.com.tigelah.acquirercore.infrastructure.repositories;

import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.domain.model.PaymentFees;
import br.com.tigelah.acquirercore.domain.model.PaymentStatus;
import br.com.tigelah.acquirercore.domain.ports.PaymentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final JpaPaymentRepository jpa;

    public PaymentRepositoryAdapter(JpaPaymentRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Payment payment) {
        jpa.save(toEntity(payment));
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return jpa.findById(id).map(PaymentRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<Payment> findByMerchantAndOrder(String merchantId, String orderId) {
        return jpa.findByMerchantIdAndOrderId(merchantId, orderId)
                .map(PaymentRepositoryAdapter::toDomain);
    }

    @Override
    public Payment getOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));
    }

    @Override
    public List<Payment> findAuthorizedBefore(Instant cutoff, Integer limit) {
        return jpa.findAuthorizedBefore(cutoff, PageRequest.of(0, limit))
                .stream()
                .map(PaymentRepositoryAdapter::toDomain)
                .toList();
    }

    private static PaymentEntity toEntity(Payment p) {
        var e = new PaymentEntity();
        e.id = p.getId();
        e.merchantId = p.getMerchantId();
        e.orderId = p.getOrderId();
        e.amountCents = p.getAmountCents();
        e.currency = p.getCurrency();
        e.panLast4 = p.getPanLast4();
        e.status = p.getStatus().name();
        e.authCode = p.getAuthCode();
        e.createdAt = p.getCreatedAt();
        e.accountId = p.getAccountId();
        e.userId = p.getUserId();
        e.panHash = p.getPanHash();
        e.installments = p.getInstallments() == null ? 1 : p.getInstallments();
        e.refundedAmountCents = p.getRefundedAmountCents();
        e.mdrAmountCents = p.getFees() == null ? 0L : p.getFees().getMdrAmountCents();
        e.acquirerFeeAmountCents = p.getFees() == null ? 0L : p.getFees().getAcquirerFeeAmountCents();
        e.brandFeeAmountCents = p.getFees() == null ? 0L : p.getFees().getBrandFeeAmountCents();
        return e;
    }

    private static Payment toDomain(PaymentEntity e) {
        var p = new Payment(
                e.id,
                e.merchantId,
                e.orderId,
                e.amountCents,
                e.currency,
                e.panLast4,
                e.createdAt
        );

        if (e.accountId != null) {
            p.setAccountId(e.accountId);
        }

        if (e.userId != null && !e.userId.isBlank()) {
            p.setUserId(e.userId);
        }

        if (e.panHash != null && !e.panHash.isBlank()) {
            p.setPanHash(e.panHash);
        }

        p.setInstallments(e.installments == null ? 1 : e.installments);
        p.setRefundedAmountCents(e.refundedAmountCents == null ? 0L : e.refundedAmountCents);
        p.defineFees(new PaymentFees(
                e.mdrAmountCents == null ? 0L : e.mdrAmountCents,
                e.acquirerFeeAmountCents == null ? 0L : e.acquirerFeeAmountCents,
                e.brandFeeAmountCents == null ? 0L : e.brandFeeAmountCents
        ));

        return PaymentRehydrator.rehydrate(
                p,
                PaymentStatus.valueOf(e.status),
                e.authCode
        );
    }
}