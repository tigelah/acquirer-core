package br.com.tigelah.acquirercore.domain.ports;

import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.infrastructure.repositories.PaymentEntity;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    void save(Payment payment);
    Optional<Payment> findById(UUID id);
    Optional<Payment> findByMerchantAndOrder(String merchantId, String orderId);
    Payment getOrThrow(UUID id);
    List<Payment> findAuthorizedBefore(Instant cutoff, Integer limit);
}