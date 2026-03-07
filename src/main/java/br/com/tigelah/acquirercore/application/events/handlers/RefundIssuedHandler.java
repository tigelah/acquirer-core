package br.com.tigelah.acquirercore.application.events.handlers;

import br.com.tigelah.acquirercore.application.events.RefundEventHandler;
import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.domain.model.Refund;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class RefundIssuedHandler implements RefundEventHandler {

    @Override
    public void handle(Refund refund, Payment payment, JsonNode event) {
        refund.markIssued();
    }
}
