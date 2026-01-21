package br.com.tigelah.acquirercore.application.usecase;

import br.com.tigelah.acquirercore.application.dto.PaymentOutput;
import br.com.tigelah.acquirercore.application.queries.GetPaymentQuery;
import br.com.tigelah.acquirercore.domain.ports.PaymentRepository;

public class GetPaymentUseCase {
    private final PaymentRepository payments;

    public GetPaymentUseCase(PaymentRepository payments) {
        this.payments = payments;
    }

    public PaymentOutput execute(GetPaymentQuery query) {
        return PaymentOutput.from(payments.getOrThrow(query.paymentId()));
    }
}
