package br.com.tigelah.acquirercore.domain.ports;

import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.domain.model.Refund;

public interface RefundEventPublisher {
    void publishRefundRequested(Refund refund, Payment payment, String correlationId);
}
