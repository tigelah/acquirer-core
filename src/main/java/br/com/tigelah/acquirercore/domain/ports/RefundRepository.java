package br.com.tigelah.acquirercore.domain.ports;

import br.com.tigelah.acquirercore.domain.model.Refund;
import br.com.tigelah.acquirercore.domain.model.RefundStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefundRepository {
    void save(Refund refund);
    Optional<Refund> findById(UUID id);
    List<Refund> findByPaymentId(UUID paymentId);
    long sumByPaymentIdAndStatuses(UUID paymentId, List<RefundStatus> statuses);
}