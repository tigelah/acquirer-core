package br.com.tigelah.acquirercore.domain.ports;

import br.com.tigelah.acquirercore.domain.model.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    void save(Payment payment);
    Optional<Payment> findById(UUID id);
    Optional<Payment> findByMerchantAndOrder(String merchantId, String orderId);
    Payment getOrThrow(UUID id);
}