package br.com.tigelah.acquirercore.application.usecase;

import br.com.tigelah.acquirercore.application.commands.AuthorizePaymentCommand;
import br.com.tigelah.acquirercore.application.dto.PaymentOutput;
import br.com.tigelah.acquirercore.domain.model.Payment;
import br.com.tigelah.acquirercore.domain.ports.*;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public class AuthorizePaymentUseCase {

    private final PaymentRepository payments;
    private final IdempotencyStore idempotency;
    private final EventPublisher events;
    private final CardCertifier certifier;
    private final BrandNetwork brandNetwork;
    private final Clock clock;

    public AuthorizePaymentUseCase(
            PaymentRepository payments,
            IdempotencyStore idempotency,
            EventPublisher events,
            CardCertifier certifier,
            BrandNetwork brandNetwork,
            Clock clock
    ) {
        this.payments = payments;
        this.idempotency = idempotency;
        this.events = events;
        this.certifier = certifier;
        this.brandNetwork = brandNetwork;
        this.clock = clock;
    }

    public PaymentOutput execute(AuthorizePaymentCommand cmd) {

        var maybePaymentId = idempotency.get(cmd.idempotencyKey());
        if (maybePaymentId.isPresent()) {
            return PaymentOutput.from(payments.getOrThrow(maybePaymentId.get()));
        }

        var existing = payments.findByMerchantAndOrder(cmd.merchantId(), cmd.orderId());
        if (existing.isPresent()) {
            idempotency.putIfAbsent(cmd.idempotencyKey(), existing.get().getId());
            return PaymentOutput.from(existing.get());
        }

        var cert = certifier.certify(cmd.card(), cmd.correlationId());
        if (!cert.valid()) {
            throw new IllegalArgumentException("Card certification failed: " + cert.reason());
        }

        var brandCheck = brandNetwork.check(cmd.card().pan(), cert.brand(), cmd.correlationId());
        if (!brandCheck.allowed()) {
            throw new IllegalArgumentException("Brand network rejected: " + brandCheck.reason());
        }

        var payment = new Payment(
                UUID.randomUUID(),
                cmd.merchantId(),
                cmd.orderId(),
                cmd.amountCents(),
                cmd.currency(),
                last4(cmd.card().pan()),
                Instant.now(clock)
        );

        payment.markAuthRequested();
        payments.save(payment);

        idempotency.putIfAbsent(cmd.idempotencyKey(), payment.getId());

        events.publishAuthorizeRequested(payment, cmd.correlationId(), cmd.idempotencyKey());
        return PaymentOutput.from(payment);
    }

    private static String last4(String pan) {
        if (pan == null || pan.length() < 4) throw new IllegalArgumentException("pan invalid");
        return pan.substring(pan.length() - 4);
    }
}
