package br.com.tigelah.acquirercore.application.commands;

import java.util.UUID;

public record VoidAuthorizationCommand(
        UUID paymentId,
        String correlationId,
        String reason
) {}
