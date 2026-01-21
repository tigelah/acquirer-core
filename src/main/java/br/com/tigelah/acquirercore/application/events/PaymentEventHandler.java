package br.com.tigelah.acquirercore.application.events;

import br.com.tigelah.acquirercore.domain.model.Payment;
import com.fasterxml.jackson.databind.JsonNode;

public interface PaymentEventHandler {
    void handle(Payment payment, JsonNode event);
}
