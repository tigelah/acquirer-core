package br.com.tigelah.acquirercore.application.events.handlers;

import br.com.tigelah.acquirercore.application.events.PaymentEventHandler;
import br.com.tigelah.acquirercore.domain.model.Payment;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationVoidedHandler implements PaymentEventHandler {

    @Override
    public void handle(Payment payment, JsonNode event) {
        payment.voidAuthorization();
    }
}
