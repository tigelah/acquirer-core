package br.com.tigelah.acquirercore.application.events;

import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.domain.model.Refund;
import com.fasterxml.jackson.databind.JsonNode;

public interface RefundEventHandler {
    void handle(Refund refund, Payment payment, JsonNode event);
}
