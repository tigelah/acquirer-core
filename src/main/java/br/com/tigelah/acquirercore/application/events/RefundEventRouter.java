package br.com.tigelah.acquirercore.application.events;

import br.com.tigelah.acquirercore.application.events.handlers.RefundCompletedHandler;
import br.com.tigelah.acquirercore.application.events.handlers.RefundFailedHandler;
import br.com.tigelah.acquirercore.application.events.handlers.RefundIssuedHandler;
import br.com.tigelah.acquirercore.application.events.handlers.RefundLedgerAppliedHandler;
import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.domain.model.Refund;
import br.com.tigelah.acquirercore.infrastructure.messaging.Topics;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RefundEventRouter {

    private final Map<String, RefundEventHandler> handlers = new HashMap<>();

    public RefundEventRouter(
            RefundIssuedHandler issuedHandler,
            RefundLedgerAppliedHandler ledgerAppliedHandler,
            RefundCompletedHandler completedHandler,
            RefundFailedHandler failedHandler
    ) {
        handlers.put(Topics.REFUND_ISSUED, issuedHandler);
        handlers.put(Topics.REFUND_LEDGER_APPLIED, ledgerAppliedHandler);
        handlers.put(Topics.REFUND_COMPLETED, completedHandler);
        handlers.put(Topics.REFUND_FAILED, failedHandler);
    }

    public boolean route(String topic, Refund refund, Payment payment, JsonNode event) {
        RefundEventHandler handler = handlers.get(topic);
        if (handler == null) {
            return false;
        }
        handler.handle(refund, payment, event);
        return true;
    }
}
