package br.com.tigelah.acquirercore.application.commands;

import br.com.tigelah.acquirercore.domain.ports.CardCertifier;

public record AuthorizePaymentCommand(
        String merchantId,
        String orderId,
        Long amountCents,
        String currency,
        CardCertifier.CardData card,
        String correlationId,
        String idempotencyKey
) {}
