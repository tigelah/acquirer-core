package br.com.tigelah.acquirercore.application.usecase;

import br.com.tigelah.acquirercore.application.events.RefundEventRouter;
import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.domain.model.Refund;
import br.com.tigelah.acquirercore.domain.ports.PaymentRepository;
import br.com.tigelah.acquirercore.domain.ports.RefundRepository;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public class HandleRefundEventUseCase {

    private final RefundRepository refunds;
    private final PaymentRepository payments;
    private final RefundEventRouter router;

    public HandleRefundEventUseCase(
            RefundRepository refunds,
            PaymentRepository payments,
            RefundEventRouter router
    ) {
        this.refunds = refunds;
        this.payments = payments;
        this.router = router;
    }

    public void execute(String topic, JsonNode root) {
        UUID refundId = parseRefundId(root);
        Refund refund = refunds.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("refund not found: " + refundId));

        UUID paymentId = refund.getPaymentId();
        Payment payment = payments.getOrThrow(paymentId);

        boolean handled = router.route(topic, refund, payment, root);
        if (!handled) {
            throw new IllegalArgumentException("no refund handler for topic: " + topic);
        }

        refunds.save(refund);
        payments.save(payment);
    }

    private UUID parseRefundId(JsonNode root) {
        JsonNode node = root.get("refundId");
        if (node == null || node.asText().isBlank()) {
            throw new IllegalArgumentException("refundId missing in event");
        }
        return UUID.fromString(node.asText());
    }
}
