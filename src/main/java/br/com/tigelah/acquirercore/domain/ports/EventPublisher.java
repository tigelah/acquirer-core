package br.com.tigelah.acquirercore.domain.ports;

import br.com.tigelah.acquirercore.domain.model.Payment;

public interface EventPublisher {
    void publishAuthorizeRequested(Payment payment, String correlationId, String idempotencyKey);
    void publishCaptureRequested(Payment payment, String correlationId);
}
