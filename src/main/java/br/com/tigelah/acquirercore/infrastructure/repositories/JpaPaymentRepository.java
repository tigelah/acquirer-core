package br.com.tigelah.acquirercore.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaPaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    Optional<PaymentEntity> findByMerchantIdAndOrderId(String merchantId, String orderId);
}
