package br.com.tigelah.acquirercore.application.commands;

import br.com.tigelah.acquirercore.domain.model.RefundReason;

import java.util.UUID;

public record RequestRefundCommand(
        UUID paymentId,
        Long amountCents,
        RefundReason reason,
        String correlationId
) {
}
