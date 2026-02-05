package br.com.tigelah.acquirercore.infrastructure.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaPaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    Optional<PaymentEntity> findByMerchantIdAndOrderId(String merchantId, String orderId);
    @Query("""
        select p from PaymentEntity p
        where p.status = 'AUTHORIZED'
          and p.createdAt < :cutoff
        order by p.createdAt asc
    """)
    List<PaymentEntity> findAuthorizedBefore(Instant cutoff, Pageable pageable);
}
