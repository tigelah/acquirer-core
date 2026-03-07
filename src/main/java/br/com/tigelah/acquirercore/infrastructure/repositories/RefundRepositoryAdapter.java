package br.com.tigelah.acquirercore.infrastructure.repositories;

import br.com.tigelah.acquirercore.domain.model.PaymentFees;
import br.com.tigelah.acquirercore.domain.model.Refund;
import br.com.tigelah.acquirercore.domain.model.RefundReason;
import br.com.tigelah.acquirercore.domain.model.RefundStatus;
import br.com.tigelah.acquirercore.domain.model.RefundType;
import br.com.tigelah.acquirercore.domain.ports.RefundRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RefundRepositoryAdapter implements RefundRepository {

    private final JpaRefundRepository jpaRefundRepository;

    public RefundRepositoryAdapter(JpaRefundRepository jpaRefundRepository) {
        this.jpaRefundRepository = jpaRefundRepository;
    }

    @Override
    public void save(Refund refund) {
        jpaRefundRepository.save(toEntity(refund));
    }

    @Override
    public Optional<Refund> findById(UUID id) {
        return jpaRefundRepository.findById(id).map(RefundRepositoryAdapter::toDomain);
    }

    @Override
    public List<Refund> findByPaymentId(UUID paymentId) {
        return jpaRefundRepository.findByPaymentIdOrderByCreatedAtAsc(paymentId)
                .stream()
                .map(RefundRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public long sumByPaymentIdAndStatuses(UUID paymentId, List<RefundStatus> statuses) {
        List<String> dbStatuses = statuses.stream().map(Enum::name).toList();
        return jpaRefundRepository.sumByPaymentIdAndStatuses(paymentId, dbStatuses);
    }

    private static RefundEntity toEntity(Refund refund) {
        RefundEntity entity = new RefundEntity();
        entity.id = refund.getId();
        entity.paymentId = refund.getPaymentId();
        entity.merchantId = refund.getMerchantId();
        entity.amountCents = refund.getAmountCents();
        entity.currency = refund.getCurrency();
        entity.type = refund.getType().name();
        entity.reason = refund.getReason().name();
        entity.status = refund.getStatus().name();
        entity.reversedMdrAmountCents = refund.getReversedFees().getMdrAmountCents();
        entity.reversedAcquirerFeeAmountCents = refund.getReversedFees().getAcquirerFeeAmountCents();
        entity.reversedBrandFeeAmountCents = refund.getReversedFees().getBrandFeeAmountCents();
        entity.createdAt = refund.getCreatedAt();
        return entity;
    }

    private static Refund toDomain(RefundEntity entity) {
        return new Refund(
                entity.id,
                entity.paymentId,
                entity.merchantId,
                entity.amountCents,
                entity.currency,
                RefundType.valueOf(entity.type),
                RefundReason.valueOf(entity.reason),
                RefundStatus.valueOf(entity.status),
                new PaymentFees(
                        entity.reversedMdrAmountCents,
                        entity.reversedAcquirerFeeAmountCents,
                        entity.reversedBrandFeeAmountCents
                ),
                entity.createdAt
        );
    }
}
