package br.com.tigelah.acquirercore.application.events;

import br.com.tigelah.acquirercore.application.events.handlers.PaymentAuthorizedHandler;
import br.com.tigelah.acquirercore.application.events.handlers.PaymentCapturedHandler;
import br.com.tigelah.acquirercore.application.events.handlers.PaymentDeclinedHandler;
import br.com.tigelah.acquirercore.application.events.handlers.PaymentRiskRejectedHandler;
import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.infrastructure.messaging.Topics;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentEventRouter {

    private final Map<String, PaymentEventHandler> handlers;

    public PaymentEventRouter(
            PaymentAuthorizedHandler authorized,
            PaymentRiskRejectedHandler riskRejected,
            PaymentDeclinedHandler declined,
            PaymentCapturedHandler captured
    ) {
        this.handlers = Map.of(
                Topics.PAYMENT_AUTHORIZED, authorized,
                Topics.PAYMENT_RISK_REJECTED, riskRejected,
                Topics.PAYMENT_DECLINED, declined,
                Topics.PAYMENT_CAPTURED, captured
        );
    }

    public boolean route(String topic, Payment payment, JsonNode event) {
        var handler = handlers.get(topic);

        if (handler == null) {
            return false;
        }

        handler.handle(payment, event);
        return true;
    }
}
