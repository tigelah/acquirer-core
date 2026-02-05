package br.com.tigelah.acquirercore.application.events;

import br.com.tigelah.acquirercore.application.events.handlers.*;
import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.infrastructure.messaging.Topics;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentEventRouter {

    private final Map<String, PaymentEventHandler> handlers = new HashMap<>();

    public PaymentEventRouter(
            PaymentAuthorizedHandler authorized,
            PaymentRiskRejectedHandler riskRejected,
            PaymentDeclinedHandler declined,
            PaymentCapturedHandler captured,
            AuthorizationExpiredHandler expired,      // ✅ novo
            AuthorizationVoidedHandler voided         // ✅ novo
    ) {
        handlers.put(Topics.PAYMENT_AUTHORIZED, authorized);
        handlers.put(Topics.PAYMENT_RISK_REJECTED, riskRejected);
        handlers.put(Topics.PAYMENT_DECLINED, declined);
        handlers.put(Topics.PAYMENT_CAPTURED, captured);
        handlers.put(Topics.AUTHORIZATION_EXPIRED, expired);
        handlers.put(Topics.AUTHORIZATION_VOIDED, voided);
    }

    public boolean route(String topic, Payment payment, JsonNode event) {
        var handler = handlers.get(topic);
        if (handler == null) return false;

        handler.handle(payment, event);
        return true;
    }
}