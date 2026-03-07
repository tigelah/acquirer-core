package br.com.tigelah.acquirercore.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaRefundRepository extends JpaRepository<RefundEntity, UUID> {

    Optional<RefundEntity> findById(UUID id);

    List<RefundEntity> findByPaymentIdOrderByCreatedAtAsc(UUID paymentId);

    @Query("""
        select coalesce(sum(r.amountCents), 0)
        from RefundEntity r
        where r.paymentId = :paymentId
          and r.status in :statuses
    """)
    long sumByPaymentIdAndStatuses(UUID paymentId, Collection<String> statuses);
}
