package br.com.tigelah.acquirercore.application.usecase;

import br.com.tigelah.acquirercore.application.commands.CapturePaymentCommand;
import br.com.tigelah.acquirercore.application.dto.PaymentOutput;
import br.com.tigelah.acquirercore.domain.ports.EventPublisher;
import br.com.tigelah.acquirercore.domain.ports.PaymentRepository;

public class CapturePaymentUseCase {
    private final PaymentRepository payments;
    private final EventPublisher events;

    public CapturePaymentUseCase(PaymentRepository payments, EventPublisher events) {
        this.payments = payments;
        this.events = events;
    }

    public PaymentOutput execute(CapturePaymentCommand cmd) {
        var payment = payments.getOrThrow(cmd.paymentId());
        payment.markCaptureRequested();
        payments.save(payment);
        events.publishCaptureRequested(payment, cmd.correlationId());
        return PaymentOutput.from(payment);
    }
}