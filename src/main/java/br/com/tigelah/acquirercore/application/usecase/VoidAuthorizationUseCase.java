package br.com.tigelah.acquirercore.application.usecase;

import br.com.tigelah.acquirercore.application.commands.VoidAuthorizationCommand;
import br.com.tigelah.acquirercore.application.dto.PaymentOutput;
import br.com.tigelah.acquirercore.domain.ports.EventPublisher;
import br.com.tigelah.acquirercore.domain.ports.PaymentRepository;

public class VoidAuthorizationUseCase {

    private final PaymentRepository payments;
    private final EventPublisher events;

    public VoidAuthorizationUseCase(PaymentRepository payments, EventPublisher events) {
        this.payments = payments;
        this.events = events;
    }

    public PaymentOutput execute(VoidAuthorizationCommand cmd) {
        var p = payments.getOrThrow(cmd.paymentId());

        p.voidAuthorization();
        payments.save(p);

        events.publishAuthorizationVoided(p, cmd.correlationId(), cmd.reason());
        return PaymentOutput.from(p);
    }
}
