package br.com.tigelah.acquirercore.application.commands;

import br.com.tigelah.acquirercore.domain.ports.CardCertifier;

import java.util.UUID;

public record AuthorizePaymentCommand(
        String merchantId,
        String orderId,
        Long amountCents,
        String currency,
        CardCertifier.CardData card,
        String correlationId,
        String idempotencyKey,
        UUID accountId,
        String userId,
        Integer installments
) {}
